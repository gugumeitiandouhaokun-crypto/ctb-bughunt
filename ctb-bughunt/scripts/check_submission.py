# -*- coding: utf-8 -*-
"""提交前规范检查（任何一项错误都应先修复，再本地验证与提交）。

错误：
  - udpunittest.py / testaw.py 语法错误，或使用了 Python 3.8 不支持的语法
  - udpunittest.py 的 main 段（if __name__ == '__main__': 到文件末尾）与 pull 原件不一致
  - TestCase 类不是唯一的 MyTestCase1；没有 test 方法；test 方法名不以 test_cjl_ 开头（题目原件自带的样例用例除外）、
    非 ASCII、重名或缺少 @timeout
  - testaw.py / udpunittest.py 新增的函数、类与 MyTestCase1 中新增的非用例方法不以 cjl_ 开头
  - import 了标准库与 testaw / testtool / timeout 以外的模块
  - testtool.py / timeout.py / config.properties 与 pull 原件不一致（udpPort 的值除外，本地运行会改用专用端口）
  - 答题目录残留 _probe* / _fix* / _find* 临时文件
警告：
  - 形如 命令(参数) 的字符串字面量中，命令名与括号之间、括号内侧或逗号后有空格
  - 新增的全局变量不以 cjl_ 开头

用法：
  python check_submission.py --answer answer --pristine notes/pristine
退出码：无错误为 0，否则为 1。
"""
from __future__ import annotations

import argparse
import ast
import hashlib
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from naming import AW_PREFIX, TEST_NAME_RE, TEST_PREFIX, is_aw_name  # noqa: E402

LOCAL_MODULES = {"testaw", "testtool", "timeout"}
FRAMEWORK_METHODS = {"setUpClass", "tearDownClass", "setUp", "tearDown", "assertTrue", "assertFalse"}
FROZEN_FILES = ("testtool.py", "timeout.py", "config.properties")
RESIDUAL_PATTERNS = ("_probe*", "_fix*", "_find*")
MAIN_RE = re.compile(r"^if\s+__name__\s*==\s*['\"]__main__['\"]\s*:", re.M)
CMD_LIKE_RE = re.compile(r"^[A-Za-z_][A-Za-z0-9_]*\s*\(.*\)$", re.S)
CMD_SPACE_RE = re.compile(r"^\w+\s+\(|\(\s|\s\)|,\s")
TARGET_VERSION = (3, 8)
PORT_LINE_RE = re.compile(rb"^(\s*udpPort\s*=)[^\n]*", re.M)


def _read(path: Path) -> str:
    return path.read_text(encoding="utf-8-sig", errors="replace").replace("\r\n", "\n")


def _digest(path: Path) -> str:
    data = path.read_bytes().replace(b"\r\n", b"\n")
    if path.name == "config.properties":
        data = PORT_LINE_RE.sub(rb"\1", data)
    return hashlib.sha256(data).hexdigest()


def _stdlib() -> set[str] | None:
    names = getattr(sys, "stdlib_module_names", None)
    return set(names) if names else None


def _parse(name: str, text: str, errors: list[str]) -> ast.Module | None:
    try:
        tree = ast.parse(text, filename=name)
    except SyntaxError as exc:
        errors.append(f"{name} 语法错误：第 {exc.lineno} 行 {exc.msg}")
        return None
    try:
        ast.parse(text, filename=name, feature_version=TARGET_VERSION)
    except SyntaxError as exc:
        errors.append(f"{name} 第 {exc.lineno} 行使用了 Python 3.8 不支持的语法：{exc.msg}")
    return tree


def _check_imports(name: str, tree: ast.Module, errors: list[str], warnings: list[str]) -> None:
    std = _stdlib()
    if std is None:
        warnings.append("当前 Python 低于 3.10，无法校验 import 是否为标准库")
        return
    for node in ast.walk(tree):
        mods: list[str] = []
        if isinstance(node, ast.Import):
            mods = [a.name.split(".")[0] for a in node.names]
        elif isinstance(node, ast.ImportFrom):
            if node.level:
                errors.append(f"{name} 第 {node.lineno} 行使用了相对导入")
                continue
            mods = [(node.module or "").split(".")[0]]
        for m in mods:
            if m and m not in std and m not in LOCAL_MODULES:
                errors.append(f"{name} 第 {node.lineno} 行 import {m}：只能使用标准库与 testaw / testtool / timeout")


def _is_testcase_base(base: ast.expr) -> bool:
    if isinstance(base, ast.Attribute):
        return base.attr == "TestCase"
    return isinstance(base, ast.Name) and base.id == "TestCase"


def _has_timeout(fn: ast.FunctionDef) -> bool:
    for d in fn.decorator_list:
        target = d.func if isinstance(d, ast.Call) else d
        if isinstance(target, ast.Name) and target.id == "timeout":
            return True
        if isinstance(target, ast.Attribute) and target.attr == "timeout":
            return True
    return False


def _check_cases(tree: ast.Module, errors: list[str], orig: ast.Module | None = None) -> int:
    sample_tests = {m for m in _class_methods(orig) if m.startswith("test")}
    cases = [n for n in tree.body if isinstance(n, ast.ClassDef) and any(_is_testcase_base(b) for b in n.bases)]
    names = [c.name for c in cases]
    if names != ["MyTestCase1"]:
        errors.append(f"udpunittest.py 只能有一个 TestCase 类 MyTestCase1，当前为 {names or '无'}")
    total = 0
    for cls in cases:
        seen: dict[str, int] = {}
        for fn in cls.body:
            if not isinstance(fn, (ast.FunctionDef, ast.AsyncFunctionDef)) or not fn.name.startswith("test"):
                continue
            total += 1
            if fn.name not in sample_tests and (not fn.name.isascii() or not TEST_NAME_RE.match(fn.name)):
                errors.append(f"第 {fn.lineno} 行用例名 {fn.name!r} 须以 {TEST_PREFIX} 开头，只含英文、数字、下划线")
            if fn.name in seen:
                errors.append(f"第 {fn.lineno} 行用例名 {fn.name} 与第 {seen[fn.name]} 行重名，前一个会被覆盖")
            seen[fn.name] = fn.lineno
            if not _has_timeout(fn):
                errors.append(f"第 {fn.lineno} 行用例 {fn.name} 缺少 @timeout 装饰器")
    if cases and total == 0:
        errors.append("MyTestCase1 中没有 test 开头的用例")
    return total


def _top_names(tree: ast.Module | None) -> tuple[set[str], set[str]]:
    """顶层 (函数与类名, 赋值变量名)。"""
    defs, assigns = set(), set()
    for node in tree.body if tree else []:
        if isinstance(node, (ast.FunctionDef, ast.AsyncFunctionDef, ast.ClassDef)):
            defs.add(node.name)
        elif isinstance(node, (ast.Assign, ast.AnnAssign)):
            targets = node.targets if isinstance(node, ast.Assign) else [node.target]
            for t in targets:
                for n in ast.walk(t):
                    if isinstance(n, ast.Name):
                        assigns.add(n.id)
    return defs, assigns


def _class_methods(tree: ast.Module | None) -> set[str]:
    for node in tree.body if tree else []:
        if isinstance(node, ast.ClassDef) and node.name == "MyTestCase1":
            return {n.name for n in node.body if isinstance(n, (ast.FunctionDef, ast.AsyncFunctionDef))}
    return set()


def _check_naming(name: str, tree: ast.Module, orig: ast.Module | None,
                  errors: list[str], warnings: list[str]) -> None:
    """新增的顶层函数/类与 MyTestCase1 中的非用例方法须以 cjl_ 开头；新增的顶层变量不带前缀时告警。"""
    orig_defs, orig_assigns = _top_names(orig)
    defs, assigns = _top_names(tree)
    for d in sorted(defs - orig_defs - {"MyTestCase1"}):
        if not is_aw_name(d):
            errors.append(f"{name} 的函数/类 {d} 须以 {AW_PREFIX} 开头")
    for a in sorted(assigns - orig_assigns):
        if not is_aw_name(a):
            warnings.append(f"{name} 的全局变量 {a} 建议以 {AW_PREFIX} 或 {AW_PREFIX.upper()} 开头，避免与队友代码重名")
    framework = _class_methods(orig) or FRAMEWORK_METHODS
    for m in sorted(_class_methods(tree) - framework):
        if m.lower().startswith(AW_PREFIX + "test"):
            errors.append(f"{name} 的 MyTestCase1.{m} 不以 test 开头，unittest 不会执行；用例名应为 {TEST_PREFIX}...")
        elif not m.startswith("test") and not is_aw_name(m):
            errors.append(f"{name} 的 MyTestCase1.{m} 须以 {AW_PREFIX} 开头")


def _parse_quiet(path: Path) -> ast.Module | None:
    if not path.is_file():
        return None
    try:
        return ast.parse(_read(path))
    except SyntaxError:
        return None


def _cmd_literals(tree: ast.Module):
    fstring_parts = set()
    for node in ast.walk(tree):
        if isinstance(node, ast.JoinedStr):
            fstring_parts.update(id(v) for v in node.values)
            text = "".join(v.value if isinstance(v, ast.Constant) else "0" for v in node.values)
            if CMD_LIKE_RE.match(text):
                yield node.lineno, text
        elif isinstance(node, ast.Constant) and isinstance(node.value, str) and id(node) not in fstring_parts:
            if CMD_LIKE_RE.match(node.value):
                yield node.lineno, node.value


def _check_cmd_format(name: str, tree: ast.Module, warnings: list[str]) -> None:
    for lineno, text in _cmd_literals(tree):
        if CMD_SPACE_RE.search(text):
            warnings.append(f"{name} 第 {lineno} 行命令 {text!r} 含多余空格，命令格式不允许空格")


def _main_block(text: str) -> str | None:
    m = MAIN_RE.search(text)
    return text[m.start():].rstrip() if m else None


def check(answer_dir: Path, pristine_dir: Path | None) -> tuple[list[str], list[str], dict]:
    errors: list[str] = []
    warnings: list[str] = []
    info: dict = {}
    udp = answer_dir / "udpunittest.py"
    if not udp.is_file():
        return [f"缺少 {udp}"], warnings, info
    has_pristine = pristine_dir is not None and pristine_dir.is_dir()
    udp_text = _read(udp)
    tree = _parse("udpunittest.py", udp_text, errors)
    if tree is not None:
        orig_udp = _parse_quiet(pristine_dir / "udpunittest.py") if has_pristine else None
        info["tests"] = _check_cases(tree, errors, orig_udp)
        _check_imports("udpunittest.py", tree, errors, warnings)
        _check_cmd_format("udpunittest.py", tree, warnings)
        _check_naming("udpunittest.py", tree, orig_udp, errors, warnings)

    aw = answer_dir / "testaw.py"
    if aw.is_file():
        aw_tree = _parse("testaw.py", _read(aw), errors)
        if aw_tree is not None:
            _check_imports("testaw.py", aw_tree, errors, warnings)
            _check_cmd_format("testaw.py", aw_tree, warnings)
            _check_naming("testaw.py", aw_tree,
                          _parse_quiet(pristine_dir / "testaw.py") if has_pristine else None, errors, warnings)
    else:
        warnings.append("答题目录没有 testaw.py")

    if pristine_dir is None or not pristine_dir.is_dir():
        warnings.append("没有 pull 原件目录，跳过 main 段与框架文件比对")
    else:
        orig = pristine_dir / "udpunittest.py"
        if orig.is_file():
            want = _main_block(_read(orig))
            got = _main_block(udp_text)
            if want is not None and got != want:
                errors.append("udpunittest.py 的 main 段（if __name__ == '__main__': 至文件末尾）与原件不一致，必须原样保留")
        for fname in FROZEN_FILES:
            src, dst = pristine_dir / fname, answer_dir / fname
            if src.is_file() and (not dst.is_file() or _digest(src) != _digest(dst)):
                errors.append(f"{fname} 与原件不一致或被删除，禁止修改")

    for pat in RESIDUAL_PATTERNS:
        for p in sorted(answer_dir.glob(pat)):
            errors.append(f"残留临时文件 {p.name}，请删除")
    return errors, warnings, info


def format_result(errors: list[str], warnings: list[str], info: dict) -> str:
    out = [f"规范检查：{'通过' if not errors else '不通过'}（用例 {info.get('tests', '?')} 个，错误 {len(errors)}，警告 {len(warnings)}）"]
    out += [f"[错误] {e}" for e in errors]
    out += [f"[警告] {w}" for w in warnings]
    return "\n".join(out)


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--answer", default="answer")
    ap.add_argument("--pristine", default="notes/pristine")
    args = ap.parse_args(argv)
    errors, warnings, info = check(Path(args.answer), Path(args.pristine))
    print(format_result(errors, warnings, info))
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
