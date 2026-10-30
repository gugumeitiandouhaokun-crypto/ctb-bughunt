# -*- coding: utf-8 -*-
"""录制：只写步骤，在无 bug 被测程序上录制真实返回，自动生成数据表断言用例。

步骤文件格式（UTF-8，可由生成脚本 stdout 产出）：
  == test_xxx ==            开始一个测试方法（名称须以 test 开头，仅 ASCII 字母数字下划线）
  cmd(args)                 一条命令
  $name = cmd(args)         发送命令，并把返回中第一个 ':' 之后的文本存入变量 name
  $name/regex/ = cmd(args)  发送命令，并把 regex 第 1 个分组（无分组取整体匹配）存入 name
  ${name}                   在后续命令中引用变量（变量只在本测试方法内有效）
  # ...                     注释；空行忽略

录制流程：
  1. 启动无 bug 程序，各测试按名称排序依次执行（每个测试前发送 init()），得到第 A 轮返回；
  2. 不重启程序，再执行一轮得到第 B 轮返回；
  3. 每一步按两轮结果生成断言：
       两轮一致                         -> 精确断言 (cmd, ret)
       不一致但可用捕获变量模板化       -> 模板断言 (cmd, tpl, 't', ...)
       仍不一致                         -> 只断言返回码 (cmd, ret, 'p', ...)
  4. 写入 testaw.py（数据表 + 运行时 run_steps）与 udpunittest.py（测试方法），按名称整块替换。

用法：
  python record.py --answer answer -f notes/steps/a.txt [--name a] [--dry-run] [--coverage-exec X.exec]
"""
from __future__ import annotations

import argparse
import ast
import logging
import os
import re
import sys
import time
from collections import Counter, defaultdict
from pathlib import Path

HEADER_RE = re.compile(r"^==\s*(\S+)\s*==$")
TEST_NAME_RE = re.compile(r"^test[A-Za-z0-9_]*$")
CAPTURE_RE = re.compile(r"^\$([A-Za-z_]\w*)(?:/(.*)/)?\s*=\s*(.+)$")
VAR_RE = re.compile(r"\$\{(\w+)\}")
BLOCK_NAME_RE = re.compile(r"^[A-Za-z0-9_]+$")
CASE_WARN_SEC = 250
MIN_TEMPLATE_LEN = 2

RUNTIME_BEGIN = "# ==== ctb-bughunt runtime begin ===="
RUNTIME_END = "# ==== ctb-bughunt runtime end ===="
RUNTIME_BLOCK = RUNTIME_BEGIN + '''
import re as _ctb_re

from testtool import tool as _ctb_tool

_CTB_VAR = _ctb_re.compile(r'\\$\\{(\\w+)\\}')


def _ctb_fill(text, env):
    return _CTB_VAR.sub(lambda m: env.get(m.group(1), m.group(0)), text)


def _ctb_capture(ret, regex):
    if regex:
        m = _ctb_re.search(regex, ret)
        if not m:
            return ''
        return m.group(1) if m.groups() else m.group(0)
    return ret.split(':', 1)[1] if ':' in ret else ''


def run_steps(case, steps):
    env = {}
    for no, step in enumerate(steps, 1):
        cmd = _ctb_fill(step[0], env)
        ret = _ctb_tool.send_cmd(cmd)
        mode = step[2] if len(step) > 2 else 'e'
        if len(step) > 3 and step[3]:
            env[step[3]] = _ctb_capture(ret, step[4] if len(step) > 4 else '')
        expected = _ctb_fill(step[1], env) if mode == 't' else step[1]
        if mode == 'p':
            ok = ret.split(':', 1)[0] == expected.split(':', 1)[0]
        else:
            ok = ret == expected
        case.assertTrue(ok, 'step %d: %s expect %s actual %s' % (no, cmd, expected, ret))
''' + RUNTIME_END


class StepError(ValueError):
    pass


# ---------------------------------------------------------------- 解析

def parse_steps(text: str) -> list[dict]:
    tests: list[dict] = []
    names: set[str] = set()
    current = None
    for lineno, raw in enumerate(text.splitlines(), 1):
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        m = HEADER_RE.match(line)
        if m:
            name = m.group(1)
            if not TEST_NAME_RE.match(name):
                raise StepError(f"第 {lineno} 行：测试名 {name!r} 须以 test 开头且只含 ASCII 字母、数字、下划线")
            if name in names:
                raise StepError(f"第 {lineno} 行：测试名 {name} 重复")
            names.add(name)
            current = {"name": name, "steps": [], "vars": set()}
            tests.append(current)
            continue
        if current is None:
            raise StepError(f"第 {lineno} 行：命令出现在第一个 '== test_xxx ==' 之前")
        cap, regex, cmd = "", "", line
        m = CAPTURE_RE.match(line)
        if m:
            cap, regex, cmd = m.group(1), m.group(2) or "", m.group(3).strip()
            if regex:
                try:
                    re.compile(regex)
                except re.error as exc:
                    raise StepError(f"第 {lineno} 行：正则 {regex!r} 无效：{exc}") from None
        elif line.startswith("$"):
            raise StepError(f"第 {lineno} 行：捕获写法应为 $name = cmd 或 $name/regex/ = cmd")
        for ref in VAR_RE.findall(cmd):
            if ref not in current["vars"]:
                raise StepError(f"第 {lineno} 行：变量 ${{{ref}}} 在本测试中尚未捕获")
        current["steps"].append({"cmd": cmd, "cap": cap, "regex": regex, "line": lineno})
        if cap:
            current["vars"].add(cap)
    if not tests:
        raise StepError("没有任何测试（缺少 '== test_xxx ==' 行）")
    empty = [t["name"] for t in tests if not t["steps"]]
    if empty:
        raise StepError(f"以下测试没有步骤：{', '.join(empty)}")
    for t in tests:
        t.pop("vars")
    return sorted(tests, key=lambda t: t["name"])


def fill(text: str, env: dict) -> str:
    return VAR_RE.sub(lambda m: env.get(m.group(1), m.group(0)), text)


def capture(ret: str, regex: str) -> str:
    if regex:
        m = re.search(regex, ret)
        if not m:
            return ""
        return m.group(1) if m.groups() else m.group(0)
    return ret.split(":", 1)[1] if ":" in ret else ""


def code_of(ret: str) -> str:
    return ret.split(":", 1)[0]


# ---------------------------------------------------------------- 执行

def execute_round(tests: list[dict], send) -> list[dict]:
    """执行一轮；返回每个测试的 {init, seconds, steps:[{cmd, ret, env}]}。env 为本步捕获后的变量快照。"""
    out = []
    for t in tests:
        init_ret = send("init()")
        env: dict[str, str] = {}
        steps = []
        t0 = time.time()
        for s in t["steps"]:
            real = fill(s["cmd"], env)
            ret = send(real)
            if s["cap"]:
                env[s["cap"]] = capture(ret, s["regex"])
            steps.append({"cmd": real, "ret": ret, "env": dict(env)})
        out.append({"init": init_ret, "seconds": time.time() - t0, "steps": steps})
    return out


def record_on_dut(answer_dir: Path, tests: list[dict], coverage_env: dict | None = None) -> tuple[list, list]:
    answer_dir = answer_dir.resolve()
    jars = sorted(answer_dir.glob("*.jar"))
    if len(jars) != 1:
        raise SystemExit(f"答题目录根下必须恰好有一个 jar，当前 {[j.name for j in jars]}")
    cwd = os.getcwd()
    saved_env = {k: os.environ.get(k) for k in (coverage_env or {})}
    os.chdir(answer_dir)
    sys.path.insert(0, str(answer_dir))
    try:
        os.environ.update(coverage_env or {})
        from testtool import tool  # noqa: E402  框架自带

        for h in logging.getLogger().handlers:
            h.setLevel(logging.WARNING)
        tool.start_dut()
        try:
            total = sum(len(t["steps"]) for t in tests)
            print(f"第 A 轮：{len(tests)} 个测试，{total} 步 ...", flush=True)
            run_a = execute_round(tests, tool.send_cmd)
            print("第 B 轮（同一进程继续执行）...", flush=True)
            run_b = execute_round(tests, tool.send_cmd)
        finally:
            try:
                tool.stop_dut()
            except Exception as exc:  # noqa: BLE001
                print(f"(stop_dut 异常，可忽略：{exc})")
    finally:
        for k, v in saved_env.items():
            if v is None:
                os.environ.pop(k, None)
            else:
                os.environ[k] = v
        os.chdir(cwd)
    return run_a, run_b


# ---------------------------------------------------------------- 生成断言

def make_template(ret_a: str, env_a: dict, ret_b: str, env_b: dict) -> str | None:
    tpl = ret_a
    for name, val in sorted(env_a.items(), key=lambda kv: -len(kv[1])):
        if len(val) >= MIN_TEMPLATE_LEN and val in tpl:
            tpl = tpl.replace(val, "${%s}" % name)
    if tpl == ret_a:
        return None
    if fill(tpl, env_a) == ret_a and fill(tpl, env_b) == ret_b:
        return tpl
    return None


def build_tables(tests: list[dict], run_a: list[dict], run_b: list[dict]) -> tuple[list[dict], list[dict]]:
    built, unstable = [], []
    for t, ra, rb in zip(tests, run_a, run_b):
        rows = []
        for no, (s, a, b) in enumerate(zip(t["steps"], ra["steps"], rb["steps"]), 1):
            mode, expected = "e", a["ret"]
            if a["ret"] != b["ret"]:
                tpl = make_template(a["ret"], a["env"], b["ret"], b["env"])
                if tpl is not None:
                    mode, expected = "t", tpl
                else:
                    mode = "p"
                    unstable.append({"test": t["name"], "no": no, "cmd": a["cmd"], "a": a["ret"],
                                     "b": b["ret"], "line": s["line"]})
            row = [s["cmd"], expected, mode, s["cap"], s["regex"]]
            while len(row) > 2 and row[-1] == "":
                row.pop()
            if len(row) == 3 and row[2] == "e":
                row.pop()
            rows.append(tuple(row))
        built.append({"name": t["name"], "rows": rows, "seconds": ra["seconds"], "init": ra["init"]})
    return built, unstable


# ---------------------------------------------------------------- 写入答题文件

def _markers(kind: str, name: str, indent: str = "") -> tuple[str, str]:
    return (f"{indent}# ==== ctb-record {kind}: {name} begin ====",
            f"{indent}# ==== ctb-record {kind}: {name} end ====")


def _replace_block(text: str, begin: str, end: str, block: str) -> tuple[str, bool]:
    i = text.find(begin)
    if i < 0:
        return text, False
    j = text.find(end, i)
    if j < 0:
        raise SystemExit(f"找到 {begin.strip()} 但缺少对应的 end 标记，请手工修复")
    return text[:i] + block + text[j + len(end):], True


def render_tables(name: str, built: list[dict]) -> str:
    begin, end = _markers("tables", name)
    out = [begin]
    for t in built:
        out.append(f"STEPS_{t['name']} = (")
        for row in t["rows"]:
            out.append("    (" + ", ".join(repr(x) for x in row) + "),")
        out.append(")")
        out.append("")
    if out[-1] == "":
        out.pop()
    out.append(end)
    return "\n".join(out)


def render_tests(name: str, built: list[dict]) -> str:
    begin, end = _markers("tests", name, "    ")
    out = [begin]
    for t in built:
        out += ["    @timeout(global_default_timeout)",
                f"    def {t['name']}(self):",
                f"        testaw.run_steps(self, testaw.STEPS_{t['name']})",
                ""]
    out.append(end)
    return "\n".join(out)


def merge_testaw(text: str, name: str, built: list[dict]) -> str:
    if RUNTIME_BEGIN not in text:
        text = text.rstrip("\n") + "\n\n\n" + RUNTIME_BLOCK + "\n"
    else:
        text, _ = _replace_block(text, RUNTIME_BEGIN, RUNTIME_END, RUNTIME_BLOCK)
    begin, end = _markers("tables", name)
    block = render_tables(name, built)
    text, done = _replace_block(text, begin, end, block)
    if not done:
        text = text.rstrip("\n") + "\n\n\n" + block + "\n"
    return text


def _insert_point(lines: list[str]) -> int:
    for i, line in enumerate(lines):
        if line.startswith("# 提示：联调函数"):
            return i
    for i, line in enumerate(lines):
        if line.startswith("if __name__"):
            return i
    raise SystemExit("udpunittest.py 中找不到插入位置（'# 提示：联调函数' 或 'if __name__'）")


def merge_udpunittest(text: str, name: str, built: list[dict]) -> str:
    if not re.search(r"^import testaw\b", text, re.M):
        raise SystemExit("udpunittest.py 缺少 'import testaw'，未做任何修改")
    if not re.search(r"^from timeout import timeout\b", text, re.M):
        raise SystemExit("udpunittest.py 缺少 'from timeout import timeout'，未做任何修改")
    if "global_default_timeout" not in text:
        raise SystemExit("udpunittest.py 缺少 global_default_timeout 定义，未做任何修改")
    begin, end = _markers("tests", name, "    ")
    block = render_tests(name, built)
    new, done = _replace_block(text, begin, end, block)
    if not done:
        lines = text.split("\n")
        at = _insert_point(lines)
        while at > 0 and lines[at - 1].strip() == "":
            at -= 1
        lines[at:at] = ["", block]
        new = "\n".join(lines)

    tree_names = _class_methods(new)
    others = _class_methods(_strip_block(new, begin, end))
    clash = sorted(n for n in (t["name"] for t in built) if n in others)
    if clash:
        raise SystemExit(f"测试名与 udpunittest.py 中已有方法重名：{', '.join(clash)}，未做任何修改")
    missing = [t["name"] for t in built if t["name"] not in tree_names]
    if missing:
        raise SystemExit(f"插入后 {', '.join(missing)} 不在 MyTestCase1 类中（类后可能还有顶层代码），未做任何修改")
    return new


def _strip_block(text: str, begin: str, end: str) -> str:
    i = text.find(begin)
    if i < 0:
        return text
    j = text.find(end, i)
    return text[:i] + text[j + len(end):]


def _class_methods(text: str) -> set[str]:
    try:
        tree = ast.parse(text)
    except SyntaxError as exc:
        raise SystemExit(f"udpunittest.py 合并后语法错误：{exc}") from None
    for node in tree.body:
        if isinstance(node, ast.ClassDef) and node.name == "MyTestCase1":
            return {n.name for n in node.body if isinstance(n, ast.FunctionDef)}
    return set()


def _read_keep_eol(path: Path) -> tuple[str, str]:
    if not path.is_file():
        return "", "\n"
    raw = path.read_bytes().decode("utf-8-sig")
    eol = "\r\n" if "\r\n" in raw else "\n"
    return raw.replace("\r\n", "\n"), eol


def write_answer(answer_dir: Path, name: str, built: list[dict]) -> None:
    aw, ut = answer_dir / "testaw.py", answer_dir / "udpunittest.py"
    aw_text, aw_eol = _read_keep_eol(aw)
    ut_text, ut_eol = _read_keep_eol(ut)
    new_ut = merge_udpunittest(ut_text, name, built)
    new_aw = merge_testaw(aw_text, name, built)
    try:
        ast.parse(new_aw)
    except SyntaxError as exc:
        raise SystemExit(f"testaw.py 合并后语法错误：{exc}，未做任何修改") from None
    aw.write_bytes(new_aw.replace("\n", aw_eol).encode("utf-8"))
    ut.write_bytes(new_ut.replace("\n", ut_eol).encode("utf-8"))


# ---------------------------------------------------------------- 报告

def render_report(name: str, built: list[dict], unstable: list[dict], run_a: list[dict]) -> str:
    out = [f"# 录制报告：{name}", ""]
    total = sum(len(t["rows"]) for t in built)
    modes = Counter(r[2] if len(r) > 2 else "e" for t in built for r in t["rows"])
    out.append(f"测试 {len(built)} 个，步骤 {total} 条；精确 {modes['e']}，模板 {modes['t']}，仅返回码 {modes['p']}")
    bad_init = [t["name"] for t in built if t["init"] != "E000:Success"]
    if bad_init:
        out.append(f"init() 未返回 E000:Success 的测试：{', '.join(bad_init)}")
    out += ["", "## 测试", "", "| 测试 | 步骤 | 录制耗时(s) | 备注 |", "|---|---|---|---|"]
    for t in built:
        note = f"超过 {CASE_WARN_SEC}s，需拆分" if t["seconds"] >= CASE_WARN_SEC else ""
        out.append(f"| {t['name']} | {len(t['rows'])} | {t['seconds']:.1f} | {note} |")

    calls: Counter = Counter()
    codes: dict[str, Counter] = defaultdict(Counter)
    distinct: dict[str, set] = defaultdict(set)
    for r in run_a:
        for s in r["steps"]:
            api = s["cmd"].split("(", 1)[0].strip()
            calls[api] += 1
            codes[api][code_of(s["ret"])] += 1
            distinct[api].add(s["ret"])
    out += ["", "## 命令分布", "", "| 命令 | 调用 | 返回码分布 | 不同返回 |", "|---|---|---|---|"]
    for api, n in calls.most_common():
        dist = " ".join(f"{c}×{k}" for c, k in codes[api].most_common())
        out.append(f"| {api} | {n} | {dist} | {len(distinct[api])} |")

    out += ["", "## 两轮不一致且无法模板化（仅断言返回码）", ""]
    if not unstable:
        out.append("无")
    for u in unstable:
        out.append(f"- {u['test']} 第 {u['no']} 步（步骤文件第 {u['line']} 行）`{u['cmd']}`：A=`{u['a']}` B=`{u['b']}`")
    return "\n".join(out) + "\n"


def summarize(name: str, built: list[dict], unstable: list[dict]) -> str:
    total = sum(len(t["rows"]) for t in built)
    lines = [f"录制 {name}：{len(built)} 个测试，{total} 步，仅返回码断言 {len(unstable)} 步"]
    for t in built:
        flag = f"  <-- 超过 {CASE_WARN_SEC}s" if t["seconds"] >= CASE_WARN_SEC else ""
        lines.append(f"  {t['name']}: {len(t['rows'])} 步 {t['seconds']:.1f}s{flag}")
    for u in unstable[:20]:
        lines.append(f"  不稳定 {u['test']}#{u['no']} {u['cmd']}  A={u['a']}  B={u['b']}")
    if len(unstable) > 20:
        lines.append(f"  ... 另有 {len(unstable) - 20} 条，见报告")
    return "\n".join(lines)


def record(answer_dir: Path, steps_text: str, name: str, report_dir: Path,
           dry_run: bool = False, coverage_env: dict | None = None) -> dict:
    if not BLOCK_NAME_RE.match(name):
        raise SystemExit(f"录制名称 {name!r} 只能包含 ASCII 字母、数字、下划线")
    try:
        tests = parse_steps(steps_text)
    except StepError as exc:
        raise SystemExit(f"步骤解析失败：{exc}") from None
    if not dry_run:
        ut_text, _ = _read_keep_eol(answer_dir / "udpunittest.py")
        merge_udpunittest(ut_text, name, [{"name": t["name"], "rows": []} for t in tests])
    run_a, run_b = record_on_dut(answer_dir, tests, coverage_env)
    built, unstable = build_tables(tests, run_a, run_b)
    report_dir.mkdir(parents=True, exist_ok=True)
    report_path = report_dir / f"{name}.md"
    report_path.write_text(render_report(name, built, unstable, run_a), encoding="utf-8")
    if not dry_run:
        write_answer(answer_dir.resolve(), name, built)
    print(summarize(name, built, unstable))
    print(f"报告：{report_path}" + ("（dry-run，未写入答题文件）" if dry_run else "；已写入 testaw.py / udpunittest.py"))
    return {"built": built, "unstable": unstable, "report": report_path}


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--answer", default="answer")
    ap.add_argument("-f", "--file", required=True, help="步骤文件")
    ap.add_argument("--name", default=None, help="录制名称（默认取文件名），同名再次录制整块替换")
    ap.add_argument("--report-dir", default="notes/record")
    ap.add_argument("--dry-run", action="store_true", help="只录制并出报告，不写答题文件")
    args = ap.parse_args(argv)
    path = Path(args.file)
    name = args.name or path.stem.split(".")[0]
    record(Path(args.answer), path.read_text(encoding="utf-8-sig"), name, Path(args.report_dir), args.dry_run)
    return 0


if __name__ == "__main__":
    sys.exit(main())
