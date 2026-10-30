# -*- coding: utf-8 -*-
"""接口规格草稿：从题目描述与反编译源码中提取命令清单与各命令的返回码，写入 notes/spec.json。

提取规则（均为启发式，结果必须由 agent 审阅修正，审阅后把 reviewed 改为 true）：
  命令    描述中出现的 名称(参数) 形式；反编译业务代码中既是字符串常量又是方法名的标识符
  码形态  描述中 "<码>:<文本>" 示例里 <码> 的最常见形状，写入 code_pattern
  返回码  按命令在描述中首次出现的位置切段，段内匹配 code_pattern 的码归该命令；
          第一个命令之前出现的码归 common_codes

再次执行只把"相对上次自动提取新增"的命令与码追加到 spec.json，不覆盖人工修改；--force 重建。

用法：
  python spec.py --answer answer --decompiled decompiled --out notes/spec.json [--force]
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from collections import Counter
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from decompile import is_lib, scan_file  # noqa: E402

DESC_FILES = ("题目描述.md", "题目补充信息.md")
CALL_RE = re.compile(r"(?<![\w.$])([A-Za-z_]\w*)\(([^()\n]*)\)")
CODE_BEFORE_COLON_RE = re.compile(r"(?<![\w])([A-Za-z]{0,4}\d{1,5}|[A-Z][A-Z_]{1,15})\s*[:：]")
JAR_CODE_RE = re.compile(r"[A-Za-z]{1,4}\d{1,5}")
FENCE_RE = re.compile(r"```.*?```", re.S)
IDENT_RE = re.compile(r"^[A-Za-z_]\w*$")


def norm(name: str) -> str:
    return name.replace("_", "").lower()


def shape(token: str) -> str:
    out = []
    for run in re.findall(r"[A-Z]+|[a-z]+|\d+|_+", token):
        if run[0].isdigit():
            out.append(r"\d" + (f"{{{len(run)}}}" if len(run) > 1 else ""))
        elif run[0] == "_":
            out.append("_" * len(run))
        else:
            cls = "[A-Z]" if run[0].isupper() else "[a-z]"
            out.append(cls + (f"{{{len(run)}}}" if len(run) > 1 else ""))
    return "".join(out)


def read_desc(answer_dir: Path) -> str:
    parts = []
    for name in DESC_FILES:
        p = answer_dir / name
        if p.is_file():
            parts.append(FENCE_RE.sub("", p.read_text(encoding="utf-8-sig", errors="replace")))
    return "\n".join(parts)


def jar_symbols(src_dir: Path) -> tuple[set[str], set[str]]:
    strings: set[str] = set()
    methods: set[str] = set()
    if not src_dir.is_dir():
        return strings, methods
    for p in src_dir.rglob("*.java"):
        if is_lib(p.relative_to(src_dir).as_posix()):
            continue
        _, mlist, slist = scan_file(p)
        methods.update(m.split("(", 1)[0] for _, m in mlist)
        strings.update(s for _, s in slist)
    return strings, methods


def extract(desc: str, strings: set[str], methods: set[str]) -> dict:
    first_pos: dict[str, int] = {}
    signature: dict[str, str] = {}
    for m in CALL_RE.finditer(desc):
        name = m.group(1)
        if name not in first_pos:
            first_pos[name] = m.start()
            signature[name] = f"{name}({m.group(2).strip()})"

    jar_prefixes = [s.split(":", 1)[0] for s in strings if ":" in s]
    jar_prefixes += [s for s in strings if JAR_CODE_RE.fullmatch(s)]
    shapes = Counter(shape(t) for t in CODE_BEFORE_COLON_RE.findall(desc)
                     + [p for p in jar_prefixes if JAR_CODE_RE.fullmatch(p)])
    shapes.pop("", None)
    pattern = shapes.most_common(1)[0][0] if shapes else ""
    code_re = re.compile(rf"(?<![\w])({pattern})(?![\w])") if pattern else None

    method_norm = {norm(x) for x in methods}
    string_norm = {norm(x) for x in strings if IDENT_RE.match(x)}
    commands: dict[str, dict] = {}
    ordered = sorted(first_pos, key=first_pos.get)
    bounds = [first_pos[n] for n in ordered] + [len(desc)]
    for i, name in enumerate(ordered):
        seg = desc[bounds[i]:bounds[i + 1]]
        codes = sorted(set(code_re.findall(seg))) if code_re else []
        src = ["desc"]
        if norm(name) in method_norm or norm(name) in string_norm:
            src.append("jar")
        commands[name] = {"signature": signature[name], "codes": codes, "source": src}
    for s in sorted(strings):
        if IDENT_RE.match(s) and norm(s) in method_norm and not any(norm(s) == norm(c) for c in commands):
            if len(s) > 2 and not s[0].isupper():
                commands[s] = {"signature": f"{s}(...)", "codes": [], "source": ["jar"]}

    common = sorted(set(code_re.findall(desc[:bounds[0]]))) if code_re and ordered else []
    jar_codes = sorted({s.split(":", 1)[0] for s in strings if ":" in s and code_re
                        and code_re.fullmatch(s.split(":", 1)[0])} |
                       {s for s in strings if code_re and code_re.fullmatch(s)})
    return {"reviewed": False, "code_pattern": pattern, "common_codes": common,
            "jar_codes": jar_codes, "commands": commands}


def merge(current: dict, old_auto: dict, new_auto: dict) -> tuple[dict, list[str]]:
    added: list[str] = []
    old_cmds = old_auto.get("commands", {})
    cmds = current.setdefault("commands", {})
    for name, info in new_auto["commands"].items():
        if name not in old_cmds:
            if name not in cmds:
                cmds[name] = info
                added.append(f"新命令 {name}")
            continue
        new_codes = set(info["codes"]) - set(old_cmds[name].get("codes", []))
        if new_codes and name in cmds:
            cmds[name]["codes"] = sorted(set(cmds[name].get("codes", [])) | new_codes)
            added.append(f"{name} 新返回码 {','.join(sorted(new_codes))}")
    for key in ("common_codes", "jar_codes"):
        new = set(new_auto.get(key, [])) - set(old_auto.get(key, []))
        if new:
            current[key] = sorted(set(current.get(key, [])) | new)
            added.append(f"{key} 新增 {','.join(sorted(new))}")
    if not current.get("code_pattern") and new_auto.get("code_pattern"):
        current["code_pattern"] = new_auto["code_pattern"]
    if added:
        current["reviewed"] = False
    return current, added


def load(path: Path) -> dict:
    if path.is_file():
        return json.loads(path.read_text(encoding="utf-8-sig"))
    return {}


def commands_of(spec: dict) -> list[str]:
    return list((spec or {}).get("commands", {}))


def build(answer_dir: Path, src_dir: Path, out: Path, force: bool = False) -> int:
    desc = read_desc(answer_dir)
    if not desc.strip():
        print(f"警告：{answer_dir} 下没有题目描述文件，只能从反编译源码提取")
    strings, methods = jar_symbols(src_dir)
    if not strings and not methods:
        print("提示：没有反编译源码（先执行 decompile），命令无法与 jar 交叉确认")
    auto = extract(desc, strings, methods)
    auto_path = out.with_name(out.stem + ".auto.json")
    out.parent.mkdir(parents=True, exist_ok=True)
    if force or not out.is_file():
        spec, added = auto, []
        action = "已生成"
    else:
        spec, added = merge(load(out), load(auto_path), auto)
        action = "已合并"
    out.write_text(json.dumps(spec, ensure_ascii=False, indent=2), encoding="utf-8")
    auto_path.write_text(json.dumps(auto, ensure_ascii=False, indent=2), encoding="utf-8")

    print(f"{action} {out}（code_pattern={spec.get('code_pattern') or '未识别'}）")
    for name, info in spec.get("commands", {}).items():
        print(f"  {info.get('signature', name)}  码={','.join(info.get('codes', [])) or '-'}  来源={'+'.join(info.get('source', []))}")
    if spec.get("common_codes"):
        print(f"  通用返回码：{','.join(spec['common_codes'])}")
    for a in added:
        print(f"  新增：{a}")
    todo = [n for n, i in spec.get("commands", {}).items() if i.get("source") != ["desc", "jar"]]
    todo += [n for n, i in spec.get("commands", {}).items() if not i.get("codes")]
    if todo or not spec.get("code_pattern"):
        print("待确认：" + "，".join(sorted(set(todo))) + ("；code_pattern 未识别" if not spec.get("code_pattern") else ""))
    if not spec.get("reviewed"):
        print("请对照题目描述与源码审阅修正 spec.json（删掉误识别的命令、补全返回码），完成后把 reviewed 改为 true")
    return 0


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--answer", default="answer")
    ap.add_argument("--decompiled", default="decompiled")
    ap.add_argument("--out", default="notes/spec.json")
    ap.add_argument("--force", action="store_true")
    args = ap.parse_args(argv)
    return build(Path(args.answer), Path(args.decompiled) / "src", Path(args.out), args.force)


if __name__ == "__main__":
    sys.exit(main())
