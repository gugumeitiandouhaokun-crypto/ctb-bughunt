# -*- coding: utf-8 -*-
"""覆盖率：用 JaCoCo 统计无 bug 被测程序执行到的业务代码，按核心/相关/基础设施分层报告。

挂载方式（均通过环境变量 JAVA_TOOL_OPTIONS 注入 -javaagent）：
  文件模式    被测程序退出（EXIT()）时写出 .exec；用于 ctb.py coverage（完整 local）
  tcpserver   录制时使用：启动后 dump 一次作为启动基线，每个测试结束后 dump --reset，得到每个测试的增量

报告（notes/coverage/<时间>.md）：
  核心/相关类的行、分支、方法覆盖率（主指标为核心类），基础设施只汇总一行
  分层依据；测试贡献（录制时）；未执行方法与部分覆盖方法，列出未执行行、缺分支行，并定位到反编译源码行
分层规则见 cov_analyze.py；notes/coverage/scope.json 可覆盖自动判断。

用法：
  python jacoco_cov.py --exec notes/coverage/x.exec --jar answer/xxx.jar --out notes/coverage
"""
from __future__ import annotations

import argparse
import json
import os
import shutil
import socket
import subprocess
import sys
import time
import zipfile
from dataclasses import dataclass, field
from datetime import datetime
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import cov_analyze as ca  # noqa: E402
import explored as ex  # noqa: E402
from decompile import is_lib  # noqa: E402

TOOLS = Path(__file__).resolve().parent.parent / "tools"
AGENT = TOOLS / "jacocoagent.jar"
CLI = TOOLS / "jacococli.jar"
BOOT_CLASSES = "BOOT-INF/classes/"
COUNTERS = ca.COUNTERS
LABELS = {"LINE": "行", "BRANCH": "分支", "METHOD": "方法"}
MAX_METHODS = 40
MAX_LINES_PER_METHOD = 12
WANTED = {ca.CORE, ca.RELATED}


@dataclass
class Context:
    commands: list[str] = field(default_factory=list)
    src_dir: Path | None = None
    scope_path: Path | None = None


# ---------------------------------------------------------------- JaCoCo 调用

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
    return buf.value if target == path else str(Path(buf.value) / path.name)


def _agent_opts(params: str) -> dict:
    if not AGENT.is_file():
        raise SystemExit(f"找不到 JaCoCo agent：{AGENT}")
    agent = _short_path(AGENT)
    if " " in agent or not agent.isascii():
        raise SystemExit(f"JaCoCo agent 路径含空格或非 ASCII 且无法转短路径：{AGENT}")
    old = os.environ.get("JAVA_TOOL_OPTIONS", "").strip()
    return {"JAVA_TOOL_OPTIONS": f"{old} -javaagent:{agent}={params}".strip()}


def agent_env(exec_path: Path) -> dict:
    exec_path = exec_path.resolve()
    exec_path.parent.mkdir(parents=True, exist_ok=True)
    exec_path.unlink(missing_ok=True)
    dest = _short_path(exec_path)
    if " " in dest or not dest.isascii():
        raise SystemExit(f"覆盖率文件路径含空格或非 ASCII 且无法转短路径：{exec_path}，请换到纯英文路径的工作目录")
    return _agent_opts(f"destfile={dest},append=true")


def _cli(args: list[str]) -> subprocess.CompletedProcess:
    if not CLI.is_file():
        raise SystemExit(f"找不到 JaCoCo cli：{CLI}")
    if shutil.which("java") is None:
        raise SystemExit("找不到 java，请先安装 Java 并加入 PATH")
    env = dict(os.environ)
    env.pop("JAVA_TOOL_OPTIONS", None)
    return subprocess.run(["java", "-jar", str(CLI), *args], capture_output=True, text=True,
                          encoding="utf-8", errors="replace", env=env)


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
    proc = _cli(["report", str(exec_path), "--classfiles", str(classes), "--xml", str(xml_path)])
    if proc.returncode != 0 or not xml_path.is_file():
        raise SystemExit(f"jacococli report 失败：\n{proc.stdout}\n{proc.stderr}")


def _free_port() -> int:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind(("127.0.0.1", 0))
        return s.getsockname()[1]


class TcpSession:
    """录制用：tcpserver 模式，按启动基线 / 每个测试 / 第 B 轮切分 exec。"""

    def __init__(self, out_dir: Path):
        self.dir = out_dir.resolve()
        shutil.rmtree(self.dir, ignore_errors=True)
        self.dir.mkdir(parents=True)
        self.port = _free_port()
        self.baseline: Path | None = None
        self.tests: list[tuple[str, Path]] = []
        self.rest: list[Path] = []
        self.errors: list[str] = []

    def env(self) -> dict:
        return _agent_opts(f"output=tcpserver,address=127.0.0.1,port={self.port}")

    def _dump(self, label: str) -> Path | None:
        dest = self.dir / f"{len(self.tests) + len(self.rest) + 1:03d}_{label}.exec"
        for _ in range(3):
            proc = _cli(["dump", "--address", "127.0.0.1", "--port", str(self.port),
                         "--destfile", str(dest), "--reset", "--retry", "3"])
            if proc.returncode == 0 and dest.is_file():
                return dest
            time.sleep(0.5)
        self.errors.append(f"dump {label} 失败：{(proc.stderr or proc.stdout).strip()[-300:]}")
        return None

    def started(self) -> None:
        self.baseline = self._dump("baseline")

    def test_done(self, name: str) -> None:
        p = self._dump(name)
        if p:
            self.tests.append((name, p))

    def finished(self) -> None:
        p = self._dump("round_b")
        if p:
            self.rest.append(p)

    def merged(self) -> Path:
        parts = [p for p in [self.baseline, *(p for _, p in self.tests), *self.rest] if p]
        if not parts:
            raise SystemExit("覆盖率 dump 全部失败：" + "；".join(self.errors))
        total = self.dir / "total.exec"
        proc = _cli(["merge", *map(str, parts), "--destfile", str(total)])
        if proc.returncode != 0 or not total.is_file():
            raise SystemExit(f"jacococli merge 失败：\n{proc.stdout}\n{proc.stderr}")
        return total


# ---------------------------------------------------------------- 报告

def _pct(pair) -> str:
    cov, total = pair
    return f"{cov}/{total} ({cov * 100.0 / total:.1f}%)" if total else "0/0 (-)"


def _rate(pair) -> float:
    cov, total = pair
    return cov * 100.0 / total if total else 0.0


def _class_row(info: dict) -> tuple:
    c = info["c"]
    get = lambda k: (c.get(k, (0, 0))[0], sum(c.get(k, (0, 0))))  # noqa: E731
    return get("LINE"), get("BRANCH"), get("METHOD")


def _method_label(cname: str, m: dict) -> str:
    return f"{ca.simple(cname)}.{m['name']}{m['desc']}" + (f" L{m['line']}" if m["line"] else "")


def _explored_line(model: dict, cname: str, n: int, need_cb: int, seen: dict) -> bool:
    key = ex.line_key(model["classes"][cname]["source"], n)
    return key in seen and seen[key] >= need_cb


def _gap_lines(model: dict, cname: str, m: dict, lines: list[int], loc: ca.Locator, seen: dict) -> list[str]:
    missed, branches = ca.method_gaps(model, cname, lines)
    out = []
    items = [(_explored_line(model, cname, n, 0, seen), n, "未执行") for n in missed]
    items += [(_explored_line(model, cname, n, tot, seen), n, f"缺 {mb}/{tot} 分支") for n, mb, tot in branches]
    for done, n, what in sorted(items)[:MAX_LINES_PER_METHOD]:
        what += "（已探索：历次提交的用例执行过）" if done else ""
        hit = loc.locate(cname, m["name"], m["desc"], n)
        if hit:
            out.append(f"  - L{n} {what} → :{hit[0]} `{hit[1][:120]}`")
        else:
            clue = loc.clue_text(cname, m["name"], m["desc"], n)
            out.append(f"  - L{n} {what}" + (f"（线索：{clue}）" if clue else ""))
    if len(items) > MAX_LINES_PER_METHOD:
        out.append(f"  - ... 另有 {len(items) - MAX_LINES_PER_METHOD} 处")
    return out


def render(source: str, model: dict, tiers: dict, notes: list[str], prev: dict | None,
           loc: ca.Locator, src: ca.SourceIndex, contrib: list[dict] | None, errors: list[str],
           seen: dict, nov: dict | None) -> tuple[str, dict]:
    core_t = ca.totals(model, tiers, {ca.CORE})
    rel_t = ca.totals(model, tiers, {ca.RELATED})
    infra_t = ca.totals(model, tiers, {ca.INFRA})
    counts = {t: sum(1 for c in model["classes"] if tiers[c][0] == t and "$" not in c) for t in (ca.CORE, ca.RELATED, ca.INFRA)}
    main_t = core_t if core_t["LINE"][1] else ca.totals(model, tiers, {ca.CORE, ca.RELATED, ca.INFRA})
    main_label = "核心" if core_t["LINE"][1] else "全部业务类（未识别核心类）"

    out = [f"# 覆盖率：{source}", "", f"时间：{datetime.now():%Y-%m-%d %H:%M:%S}", ""]
    head = []
    for k in COUNTERS:
        delta = f"（较上次 {_rate(main_t[k]) - prev[k]:+.1f}%）" if prev and k in prev else ""
        head.append(f"{LABELS[k]} {_pct(main_t[k])}{delta}")
    out.append(f"- 主指标 {main_label}（{counts[ca.CORE]} 类）：" + "，".join(head))
    out.append(f"- 相关（{counts[ca.RELATED]} 类）：" + "，".join(f"{LABELS[k]} {_pct(rel_t[k])}" for k in COUNTERS))
    out.append(f"- 基础设施（{counts[ca.INFRA]} 类，不计入主指标）：行 {_pct(infra_t['LINE'])}")
    if nov is not None:
        out.append(f"- 相对已探索（核心+相关，历次正式提交的用例执行过的行）：{ex.describe_novelty(nov)}")
    out += [f"- 注意：{n}" for n in notes + errors]

    if contrib:
        out += ["", "## 测试贡献（核心+相关，已扣除启动基线）", "",
                "| 测试 | 覆盖行 | 独有行 | 按顺序新增分支 | 相对已探索新增行 | 备注 |", "|---|---|---|---|---|---|"]
        for r in contrib:
            note = "冗余候选：没有独有行" if r["unique"] == 0 and len(contrib) > 1 else ""
            fresh = r.get("fresh", "-") if seen else "-"
            out.append(f"| {r['test']} | {r['lines']} | {r['unique']} | {r['new_branches']} | {fresh} | {note} |")

    shown = sorted((c for c in model["classes"] if tiers[c][0] in WANTED),
                   key=lambda c: (tiers[c][0] != ca.CORE, -(sum(model["classes"][c]["c"].get("LINE", (0, 0))) - model["classes"][c]["c"].get("LINE", (0, 0))[0])))
    out += ["", "## 核心与相关类", "", "源码路径相对 `decompiled/src/`。", "",
            "| 类 | 层 | 依据 | 行 | 分支 | 方法 | 反编译源码 |", "|---|---|---|---|---|---|---|"]
    for c in shown:
        line, br, meth = _class_row(model["classes"][c])
        p = src.path(c)
        rel = p.relative_to(src.src_dir).as_posix() if p else "-"
        out.append(f"| {ca.dotted(c)} | {tiers[c][0]} | {tiers[c][1]} | {_pct(line)} | {_pct(br)} | {meth[0]}/{meth[1]} | {rel} |")

    ranges = ca.method_lines(model)
    unexec, partial = [], []
    for c in shown:
        for m in model["classes"][c]["methods"]:
            if m["name"] in ("<clinit>",) or m["name"].startswith(ca.SYNTHETIC_PREFIX):
                continue
            mc = m["c"]
            if mc.get("METHOD", (0, 0))[0] == 0:
                unexec.append((c, m))
            elif mc.get("LINE", (0, 0))[1] or mc.get("BRANCH", (0, 0))[1]:
                partial.append((c, m, mc.get("LINE", (0, 0))[1] + mc.get("BRANCH", (0, 0))[1]))
    partial.sort(key=lambda x: (tiers[x[0]][0] != ca.CORE, -x[2]))

    out += ["", f"## 部分覆盖的方法（{len(partial)} 个，核心在前、缺口多的在前）", ""]
    for c, m, _ in partial[:MAX_METHODS]:
        lc, lt = m["c"].get("LINE", (0, 0))[0], sum(m["c"].get("LINE", (0, 0)))
        bc, bt = m["c"].get("BRANCH", (0, 0))[0], sum(m["c"].get("BRANCH", (0, 0)))
        decl = loc.decl_line(c, m["name"])
        where = f"  → {src.path(c).relative_to(src.src_dir).as_posix()}:{decl}" if decl else ""
        out.append(f"- [{tiers[c][0]}] {_method_label(c, m)}  行 {_pct((lc, lt))}  分支 {_pct((bc, bt))}{where}")
        out += _gap_lines(model, c, m, ranges.get((c, m["name"], m["desc"]), []), loc, seen)
    if not partial:
        out.append("无")

    def _done(c, m) -> bool:
        return any(_explored_line(model, c, n, 0, seen) for n in ranges.get((c, m["name"], m["desc"]), []))

    unexec.sort(key=lambda x: _done(*x))
    out += ["", f"## 未执行的方法（{len(unexec)} 个）", ""]
    for c, m in unexec[:MAX_METHODS * 2]:
        decl = loc.decl_line(c, m["name"])
        where = f"  → {src.path(c).relative_to(src.src_dir).as_posix()}:{decl}" if decl else ""
        mark = "（已探索：历次提交的用例执行过）" if _done(c, m) else ""
        out.append(f"- [{tiers[c][0]}] {_method_label(c, m)}{mark}{where}")
    if not unexec:
        out.append("无")

    infra = sorted(ca.dotted(c) for c in model["classes"] if tiers[c][0] == ca.INFRA and "$" not in c)
    out += ["", f"## 基础设施类（{len(infra)} 个，未展开；如有误判在 notes/coverage/scope.json 中调整）", ""]
    out.append("、".join(infra[:60]) + (" ..." if len(infra) > 60 else "") if infra else "无")
    rates = {k: round(_rate(main_t[k]), 2) for k in COUNTERS}
    return "\n".join(out) + "\n", {"rates": rates, "main_t": main_t, "rel_t": rel_t, "main_label": main_label,
                                    "partial": len(partial), "unexec": len(unexec)}


def report(exec_path: Path | None, jar: Path, out_dir: Path, source: str, ctx: Context,
           session: TcpSession | None = None) -> dict:
    out_dir = out_dir.resolve()
    out_dir.mkdir(parents=True, exist_ok=True)
    classes_dir = business_classes(jar, out_dir / "classes")
    if session is not None:
        exec_path = session.merged()
    elif not exec_path or not wait_exec(exec_path):
        raise SystemExit(f"没有生成覆盖率数据 {exec_path}（被测程序可能未正常退出）")
    stamp = f"{datetime.now():%Y%m%d_%H%M%S}"
    xml_path = out_dir / f"{stamp}.xml"
    run_report(exec_path, classes_dir, xml_path)
    model = ca.parse_xml(xml_path)

    base_model, test_models, started = None, [], None
    if session is not None:
        def _model(p: Path) -> dict:
            x = p.with_suffix(".xml")
            run_report(p, classes_dir, x)
            return ca.parse_xml(x)
        base_model = _model(session.baseline) if session.baseline else None
        test_models = [(name, _model(p)) for name, p in session.tests]
        executed = set().union(*(ca.executed_classes(m) for _, m in test_models)) if test_models else set()
        started = executed - (ca.executed_classes(base_model) if base_model else set())

    src = ca.SourceIndex(ctx.src_dir)
    tiers, notes = ca.classify(model, ctx.commands, src, ca.load_scope(ctx.scope_path), started)
    explored = ex.load(out_dir.parent)
    seen = explored["lines"] if explored["submits"] else {}
    cur_lines = ex.lines_of(model, tiers)
    nov = {**ex.novelty(explored, cur_lines, []), "codes": None} if explored["submits"] else None
    contrib = ca.per_test_stats(base_model, test_models, tiers, WANTED) if test_models else None
    if contrib and seen:
        for r, (_, m) in zip(contrib, test_models):
            r["fresh"] = sum(1 for k in ex.lines_of(m, tiers) if k not in seen)
    loc = ca.Locator(src, classes_dir)

    last_path = out_dir / "last.json"
    last_all = json.loads(last_path.read_text(encoding="utf-8-sig")) if last_path.is_file() else {}
    if not isinstance(last_all, dict) or any(not isinstance(v, dict) or "LINE" not in v for v in last_all.values()):
        last_all = {}
    prev = last_all.get(source)
    md, info = render(source, model, tiers, notes, prev, loc, src, contrib, session.errors if session else [],
                      seen, nov)
    md_path = out_dir / f"{stamp}.md"
    md_path.write_text(md, encoding="utf-8")
    last_all[source] = info["rates"]
    last_path.write_text(json.dumps(last_all, ensure_ascii=False, indent=1), encoding="utf-8")

    parts = []
    for k in COUNTERS:
        delta = f"({info['rates'][k] - prev[k]:+.1f})" if prev and k in prev else ""
        parts.append(f"{LABELS[k]} {_pct(info['main_t'][k])}{delta}")
    line = (f"- {datetime.now():%Y-%m-%d %H:%M:%S} {source} {info['main_label']} " + " ".join(parts)
            + f" | 相关 行 {_pct(info['rel_t']['LINE'])} 分支 {_pct(info['rel_t']['BRANCH'])}")
    with (out_dir / "history.md").open("a", encoding="utf-8") as f:
        f.write(line + "\n")
    print(line.lstrip("- "))
    print(f"部分覆盖方法 {info['partial']} 个，未执行方法 {info['unexec']} 个（核心+相关）")
    if nov is not None:
        print(f"相对已探索：{ex.describe_novelty(nov)}")
    if contrib:
        redundant = [r["test"] for r in contrib if r["unique"] == 0 and len(contrib) > 1]
        print("测试贡献：" + "，".join(f"{r['test']} 行{r['lines']}/独有{r['unique']}"
                                   + (f"/相对已探索新增{r['fresh']}" if "fresh" in r else "") for r in contrib))
        if redundant:
            print("冗余候选（没有独有行）：" + "，".join(redundant))
    for n in notes + (session.errors if session else []):
        print(f"注意：{n}")
    print(f"报告：{md_path}")
    return {"md": md_path, "line": line, "tiers": tiers, "contrib": contrib, "lines": cur_lines, "novelty": nov}


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--exec", required=True, dest="exec_path")
    ap.add_argument("--jar", required=True)
    ap.add_argument("--out", default="notes/coverage")
    ap.add_argument("--source", default="manual")
    ap.add_argument("--spec", default="notes/spec.json")
    ap.add_argument("--src", default="decompiled/src")
    args = ap.parse_args(argv)
    spec = Path(args.spec)
    commands = list(json.loads(spec.read_text(encoding="utf-8-sig")).get("commands", {})) if spec.is_file() else []
    ctx = Context(commands, Path(args.src), Path(args.out) / "scope.json")
    report(Path(args.exec_path), Path(args.jar), Path(args.out), args.source, ctx)
    return 0


if __name__ == "__main__":
    sys.exit(main())
