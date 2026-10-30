# -*- coding: utf-8 -*-
"""覆盖率：用 JaCoCo 统计无 bug 被测程序在一次本地运行/录制中被执行到的业务代码。

挂载方式：通过环境变量 JAVA_TOOL_OPTIONS 注入 -javaagent，被测程序退出（EXIT()）时写出 .exec。
报告：jacococli report 生成 XML，只统计业务类（排除 decompile.LIB_PREFIXES 中的三方库），
输出行/分支/方法覆盖率、未覆盖方法与部分覆盖方法，并与上一次结果对比。

用法：
  python jacoco_cov.py --exec notes/coverage/x.exec --jar answer/xxx.jar --out notes/coverage
"""
from __future__ import annotations

import argparse
import json
import os
import shutil
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
import zipfile
from datetime import datetime
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from decompile import is_lib  # noqa: E402

TOOLS = Path(__file__).resolve().parent.parent / "tools"
AGENT = TOOLS / "jacocoagent.jar"
CLI = TOOLS / "jacococli.jar"
BOOT_CLASSES = "BOOT-INF/classes/"
COUNTERS = ("LINE", "BRANCH", "METHOD")
LABELS = {"LINE": "行", "BRANCH": "分支", "METHOD": "方法"}
SKIP_METHOD_PREFIX = ("lambda$", "access$")
MAX_UNCOVERED = 80
MAX_PARTIAL = 30


def _short_path(path: Path) -> str:
    """JAVA_TOOL_OPTIONS 按空白切分且对非 ASCII 路径不可靠，Windows 下改用 8.3 短路径。"""
    text = str(path)
    if os.name != "nt" or (text.isascii() and " " not in text):
        return text
    import ctypes

    target = path if path.exists() else path.parent
    buf = ctypes.create_unicode_buffer(1024)
    if ctypes.windll.kernel32.GetShortPathNameW(str(target), buf, 1024) == 0:
        return text
    short = buf.value if target == path else str(Path(buf.value) / path.name)
    return short


def agent_env(exec_path: Path) -> dict:
    if not AGENT.is_file():
        raise SystemExit(f"找不到 JaCoCo agent：{AGENT}")
    exec_path = exec_path.resolve()
    exec_path.parent.mkdir(parents=True, exist_ok=True)
    exec_path.unlink(missing_ok=True)
    dest = _short_path(exec_path)
    if " " in dest or not dest.isascii():
        raise SystemExit(f"覆盖率文件路径含空格或非 ASCII 且无法转短路径：{exec_path}，请换到纯英文路径的工作目录")
    opts = f"-javaagent:{_short_path(AGENT)}=destfile={dest},append=true"
    old = os.environ.get("JAVA_TOOL_OPTIONS", "").strip()
    return {"JAVA_TOOL_OPTIONS": f"{old} {opts}".strip()}


def wait_exec(exec_path: Path, timeout: float = 15.0) -> bool:
    last, stable_since = -1, None
    end = time.time() + timeout
    while time.time() < end:
        size = exec_path.stat().st_size if exec_path.is_file() else -1
        if size > 0 and size == last:
            if stable_since and time.time() - stable_since >= 1.0:
                return True
            stable_since = stable_since or time.time()
        else:
            stable_since = None
        last = size
        time.sleep(0.3)
    return exec_path.is_file() and exec_path.stat().st_size > 0


def business_classes(jar: Path, cache_dir: Path) -> Path:
    """只解出业务类：避开三方库与 META-INF/versions 下的重复类（多版本 jar 会让 jacococli 报同名类冲突）。"""
    stamp = f"{jar.name}:{jar.stat().st_size}:{int(jar.stat().st_mtime)}"
    marker = cache_dir / ".source"
    if marker.is_file() and marker.read_text(encoding="utf-8") == stamp:
        return cache_dir
    shutil.rmtree(cache_dir, ignore_errors=True)
    cache_dir.mkdir(parents=True)
    count = 0
    with zipfile.ZipFile(jar) as zf:
        for info in zf.infolist():
            name = info.filename
            if not name.endswith(".class") or name.startswith("META-INF/"):
                continue
            rel = name[len(BOOT_CLASSES):] if name.startswith(BOOT_CLASSES) else name
            if rel.startswith("BOOT-INF/") or is_lib(rel):
                continue
            dst = cache_dir / rel
            dst.parent.mkdir(parents=True, exist_ok=True)
            dst.write_bytes(zf.read(info))
            count += 1
    if not count:
        raise SystemExit(f"{jar} 中没有找到业务类")
    marker.write_text(stamp, encoding="utf-8")
    return cache_dir


def run_report(exec_path: Path, classes: Path, xml_path: Path) -> None:
    if not CLI.is_file():
        raise SystemExit(f"找不到 JaCoCo cli：{CLI}")
    if shutil.which("java") is None:
        raise SystemExit("找不到 java，请先安装 JDK 并加入 PATH")
    env = dict(os.environ)
    env.pop("JAVA_TOOL_OPTIONS", None)
    proc = subprocess.run(
        ["java", "-jar", str(CLI), "report", str(exec_path), "--classfiles", str(classes), "--xml", str(xml_path)],
        capture_output=True, text=True, encoding="utf-8", errors="replace", env=env,
    )
    if proc.returncode != 0 or not xml_path.is_file():
        raise SystemExit(f"jacococli report 失败：\n{proc.stdout}\n{proc.stderr}")


def _counters(node) -> dict:
    out = {}
    for c in node.findall("counter"):
        out[c.get("type")] = (int(c.get("covered")), int(c.get("missed")))
    return out


def parse_xml(xml_path: Path) -> dict:
    root = ET.parse(str(xml_path)).getroot()
    totals = {k: [0, 0] for k in COUNTERS}
    classes, uncovered, partial = [], [], []
    for cls in root.iter("class"):
        cname = cls.get("name") or ""
        if is_lib(cname):
            continue
        cc = _counters(cls)
        for k in COUNTERS:
            cov, miss = cc.get(k, (0, 0))
            totals[k][0] += cov
            totals[k][1] += cov + miss
        line_cov, line_miss = cc.get("LINE", (0, 0))
        br_cov, br_miss = cc.get("BRANCH", (0, 0))
        classes.append({"name": cname.replace("/", "."), "line": (line_cov, line_cov + line_miss),
                        "branch": (br_cov, br_cov + br_miss), "method": cc.get("METHOD", (0, 0))})
        short = cname.rsplit("/", 1)[-1]
        for m in cls.findall("method"):
            mname = m.get("name") or ""
            if mname == "<clinit>" or mname.startswith(SKIP_METHOD_PREFIX):
                continue
            mc = _counters(m)
            label = f"{short}.{mname}{m.get('desc') or ''}" + (f" L{m.get('line')}" if m.get("line") else "")
            if mc.get("METHOD", (0, 0))[0] == 0:
                uncovered.append(label)
                continue
            lc, lm = mc.get("LINE", (0, 0))
            bc, bm = mc.get("BRANCH", (0, 0))
            if lm or bm:
                partial.append({"label": label, "line": (lc, lc + lm), "branch": (bc, bc + bm), "missed": lm + bm})
    partial.sort(key=lambda p: -p["missed"])
    classes.sort(key=lambda c: -(c["line"][1] - c["line"][0]))
    return {"totals": {k: tuple(v) for k, v in totals.items()}, "classes": classes,
            "uncovered": uncovered, "partial": partial}


def _pct(pair) -> str:
    cov, total = pair
    return f"{cov}/{total} ({cov * 100.0 / total:.1f}%)" if total else "0/0 (-)"


def _rate(pair) -> float:
    cov, total = pair
    return cov * 100.0 / total if total else 0.0


def render(data: dict, source: str, prev: dict | None) -> str:
    t = data["totals"]
    out = [f"# 覆盖率（业务类）：{source}", "", f"时间：{datetime.now():%Y-%m-%d %H:%M:%S}", ""]
    for k in COUNTERS:
        delta = ""
        if prev and k in prev:
            delta = f"  较上次 {_rate(t[k]) - prev[k]:+.1f}%"
        out.append(f"- {LABELS[k]}：{_pct(t[k])}{delta}")
    out += ["", "## 按类（未覆盖行多的在前）", "", "| 类 | 行 | 分支 | 方法 |", "|---|---|---|---|"]
    for c in data["classes"]:
        mc, mm = c["method"]
        out.append(f"| {c['name']} | {_pct(c['line'])} | {_pct(c['branch'])} | {mc}/{mc + mm} |")
    out += ["", f"## 未执行的方法（共 {len(data['uncovered'])} 个）", ""]
    out += [f"- {u}" for u in data["uncovered"][:MAX_UNCOVERED]] or ["无"]
    if len(data["uncovered"]) > MAX_UNCOVERED:
        out.append(f"- ... 另有 {len(data['uncovered']) - MAX_UNCOVERED} 个")
    out += ["", "## 部分覆盖的方法（未覆盖行+分支多的在前）", ""]
    out += [f"- {p['label']}  行 {_pct(p['line'])}  分支 {_pct(p['branch'])}" for p in data["partial"][:MAX_PARTIAL]] or ["无"]
    return "\n".join(out) + "\n"


def report(exec_path: Path, jar: Path, out_dir: Path, source: str) -> dict:
    out_dir.mkdir(parents=True, exist_ok=True)
    if not wait_exec(exec_path):
        raise SystemExit(f"没有生成覆盖率数据 {exec_path}（被测程序可能未正常退出）")
    stamp = f"{datetime.now():%Y%m%d_%H%M%S}"
    xml_path = out_dir / f"{stamp}.xml"
    run_report(exec_path, business_classes(jar, out_dir / "classes"), xml_path)
    data = parse_xml(xml_path)
    last_path = out_dir / "last.json"
    last_all = json.loads(last_path.read_text(encoding="utf-8-sig")) if last_path.is_file() else {}
    if not isinstance(last_all, dict) or any(not isinstance(v, dict) for v in last_all.values()):
        last_all = {}
    prev = last_all.get(source)
    md = render(data, source, prev)
    md_path = out_dir / f"{stamp}.md"
    md_path.write_text(md, encoding="utf-8")
    rates = {k: round(_rate(data["totals"][k]), 2) for k in COUNTERS}
    last_all[source] = rates
    last_path.write_text(json.dumps(last_all, ensure_ascii=False, indent=1), encoding="utf-8")
    parts = []
    for k in COUNTERS:
        delta = f"({rates[k] - prev[k]:+.1f})" if prev and k in prev else ""
        parts.append(f"{LABELS[k]} {_pct(data['totals'][k])}{delta}")
    line = f"- {datetime.now():%Y-%m-%d %H:%M:%S} {source} " + " ".join(parts)
    with (out_dir / "history.md").open("a", encoding="utf-8") as f:
        f.write(line + "\n")
    summary = [line.lstrip("- "), f"未执行方法 {len(data['uncovered'])} 个，部分覆盖方法 {len(data['partial'])} 个",
               f"报告：{md_path}"]
    print("\n".join(summary))
    return {"data": data, "md": md_path, "line": line}


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--exec", required=True, dest="exec_path")
    ap.add_argument("--jar", required=True)
    ap.add_argument("--out", default="notes/coverage")
    ap.add_argument("--source", default="manual")
    args = ap.parse_args(argv)
    report(Path(args.exec_path), Path(args.jar), Path(args.out), args.source)
    return 0


if __name__ == "__main__":
    sys.exit(main())
