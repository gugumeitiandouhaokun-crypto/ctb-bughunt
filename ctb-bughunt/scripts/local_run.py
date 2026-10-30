# -*- coding: utf-8 -*-
"""本地验证：在答题目录对无 bug 被测程序运行 udpunittest.py，按日志判定成败。

以 udpunittest.log 中的 "Ran N tests" 与 "OK" / "FAILED" 为准，忽略进程退出码
（框架在全部通过时也可能返回非 0）。单个用例耗时取自 time.log 的 [CASE COST]。

用法：
  python local_run.py --answer answer [--notes notes] [--timeout 1800]
退出码：全部通过为 0，否则为 1。
"""
from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import dut  # noqa: E402

CASE_LIMIT_SEC = 300
CASE_WARN_SEC = 250

RESULT_RE = re.compile(r"^(test\w*) \(([^)]*)\).*?\.\.\. ?(ok|FAIL|ERROR|skipped.*|expected failure|unexpected success)\s*$")
BLOCK_RE = re.compile(r"^(FAIL|ERROR): (test\w*) ")
RAN_RE = re.compile(r"^Ran (\d+) tests? in ([\d.]+)s")
FINAL_RE = re.compile(r"^(OK|FAILED)\b(.*)$")
COST_RE = re.compile(r"\[CASE COST\](.*?)\[TIME\]([\d.]+)")
LINE_RE = re.compile(r'File ".*?udpunittest\.py", line (\d+)')
TEST_FRAME_RE = re.compile(r'File ".*?udpunittest\.py", line (\d+), in (test\w*)')


def parse_log(text: str) -> dict:
    results: dict[str, str] = {}
    failures: list[dict] = []
    ran = None
    elapsed = None
    final = None
    lines = text.splitlines()
    i = 0
    while i < len(lines):
        line = lines[i]
        m = RESULT_RE.match(line)
        if m:
            results[m.group(1)] = m.group(3)
        m = BLOCK_RE.match(line)
        if m:
            body: list[str] = []
            i += 1
            while i < len(lines) and not lines[i].startswith("=" * 10) and not RAN_RE.match(lines[i]):
                body.append(lines[i])
                i += 1
            body = [b for b in body if not b.startswith("-" * 10)]
            msg = next((b.strip() for b in reversed(body) if b.strip()), "")
            src_lines = LINE_RE.findall("\n".join(body))
            failures.append({
                "kind": m.group(1),
                "test": m.group(2),
                "message": msg,
                "line": int(src_lines[-1]) if src_lines else None,
                "traceback": "\n".join(body).strip(),
            })
            continue
        m = RAN_RE.match(line)
        if m:
            ran, elapsed = int(m.group(1)), float(m.group(2))
        m = FINAL_RE.match(line.strip())
        if m and ran is not None:
            final = m.group(1) + m.group(2)
        i += 1
    ok = ran is not None and ran > 0 and final is not None and final.startswith("OK")
    return {"ran": ran, "elapsed": elapsed, "final": final, "ok": ok, "results": results, "failures": failures}


def parse_thread_errors(stderr: str) -> dict[str, dict]:
    """timeout 装饰器在子线程里执行用例，真实异常（含断言消息）只打印在 stderr 的线程异常里。"""
    out: dict[str, dict] = {}
    lines = stderr.splitlines()
    i = 0
    while i < len(lines):
        if not lines[i].startswith("Exception in thread"):
            i += 1
            continue
        block: list[str] = []
        i += 1
        while i < len(lines):
            line = lines[i]
            block.append(line)
            i += 1
            if line and not line.startswith((" ", "Traceback")):
                break
        frames = TEST_FRAME_RE.findall("\n".join(block))
        if frames:
            lineno, test = frames[-1]
            out[test] = {"message": block[-1].strip() if block else "", "line": int(lineno)}
    return out


def parse_costs(text: str) -> dict[str, float]:
    out: dict[str, float] = {}
    for name, sec in COST_RE.findall(text):
        out[name.rsplit(".", 1)[-1]] = float(sec)
    return out


def format_report(rep: dict) -> str:
    out = []
    if rep.get("timeout"):
        out.append(f"超时：{rep['timeout']} 秒内没有跑完，已尝试关闭被测程序")
    out.append(f"结果：{'全部通过' if rep['ok'] else '未通过'}  Ran={rep['ran']}  {rep['final'] or '无结论行'}"
               f"  用时={rep['elapsed']}s  退出码={rep.get('returncode')}（退出码仅供参考）")
    for name, st in rep["results"].items():
        cost = rep["costs"].get(name)
        flag = ""
        if cost is not None and cost >= CASE_WARN_SEC:
            flag = f"  <-- 接近/超过 {CASE_LIMIT_SEC}s 上限"
        out.append(f"  {name}: {st}" + (f"  {cost:.1f}s" if cost is not None else "") + flag)
    for f in rep["failures"]:
        loc = f" (udpunittest.py:{f['line']})" if f["line"] else ""
        out.append(f"[{f['kind']}] {f['test']}{loc}: {f['message']}")
    if not rep["ran"] and rep.get("stderr_tail"):
        out.append("stderr 末尾：\n" + rep["stderr_tail"])
    return "\n".join(out)


def run_local(answer_dir: Path, notes_dir: Path | None = None, timeout: int = 1800,
              extra_env: dict | None = None) -> dict:
    answer_dir = answer_dir.resolve()
    jars = sorted(answer_dir.glob("*.jar"))
    if len(jars) != 1:
        raise SystemExit(f"答题目录根下必须恰好有一个 jar，当前 {[j.name for j in jars]}")
    if not (answer_dir / "udpunittest.py").is_file():
        raise SystemExit(f"缺少 {answer_dir / 'udpunittest.py'}")
    log = answer_dir / "udpunittest.log"
    log.unlink(missing_ok=True)
    dut.prepare(answer_dir)
    start = time.time()
    rep: dict = {}
    try:
        proc = subprocess.run(
            [sys.executable, "udpunittest.py"], cwd=str(answer_dir), capture_output=True,
            text=True, encoding="utf-8", errors="replace", timeout=timeout,
            env={**os.environ, **extra_env} if extra_env else None,
        )
        rep["returncode"] = proc.returncode
        rep["stderr_full"] = proc.stderr or ""
        rep["stderr_tail"] = (proc.stderr or "")[-3000:]
    except subprocess.TimeoutExpired:
        rep["timeout"] = timeout
        rep["returncode"] = None
        print(f"[被测程序] {dut.stop(answer_dir)}", flush=True)
    dut.ensure_stopped(answer_dir)
    text = log.read_text(encoding="utf-8", errors="replace") if log.is_file() else ""
    rep.update(parse_log(text))
    thread_errors = parse_thread_errors(rep.get("stderr_full") or "")
    for f in rep["failures"]:
        real = thread_errors.get(f["test"])
        if real:
            f["message"], f["line"] = real["message"], real["line"]
    rep.pop("stderr_full", None)
    rep["wall"] = round(time.time() - start, 1)
    tl = answer_dir / "time.log"
    rep["costs"] = parse_costs(tl.read_text(encoding="utf-8", errors="replace")) if tl.is_file() else {}
    report = format_report(rep)
    rep["report"] = report
    if notes_dir is not None:
        notes_dir.mkdir(parents=True, exist_ok=True)
        (notes_dir / "local_last.txt").write_text(report + "\n", encoding="utf-8")
        (notes_dir / "local_last.json").write_text(json.dumps(
            {"ok": rep["ok"], "results": rep["results"], "costs": rep["costs"]}, ensure_ascii=False, indent=1),
            encoding="utf-8")
    return rep


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--answer", default="answer")
    ap.add_argument("--notes", default=None, help="结果摘要写入 <notes>/local_last.txt")
    ap.add_argument("--timeout", type=int, default=1800, help="整体超时秒数")
    args = ap.parse_args(argv)
    rep = run_local(Path(args.answer), Path(args.notes) if args.notes else None, args.timeout)
    print(rep["report"])
    return 0 if rep["ok"] else 1


if __name__ == "__main__":
    sys.exit(main())
