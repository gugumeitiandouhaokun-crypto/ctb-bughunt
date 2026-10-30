# -*- coding: utf-8 -*-
"""反编译无 bug 被测 jar，并生成便于检索的索引。

产物：
  <out>/src/...        CFR 反编译出的 Java 源码
  <out>/index.md       业务代码的类、方法（含行号）与字符串常量（含位置），第三方库只统计数量

用法：
  python decompile.py --answer answer --out decompiled [--jar path/to/x.jar] [--cfr tools/cfr-0.152.jar]
"""
from __future__ import annotations

import argparse
import re
import shutil
import subprocess
import sys
from collections import defaultdict
from pathlib import Path

DEFAULT_CFR = Path(__file__).resolve().parent.parent / "tools" / "cfr-0.152.jar"
LIB_PREFIXES = (
    "org/apache/", "org/slf4j/", "org/json/", "org/yaml/", "org/springframework/", "org/codehaus/",
    "com/google/", "com/alibaba/", "com/fasterxml/", "io/netty/", "ch/qos/", "javax/", "lombok/",
    "kotlin/", "net/sf/", "org/jboss/", "org/hibernate/", "org/objectweb/", "com/sun/",
)
TYPE_RE = re.compile(r"^\s*(?:(?:public|private|protected|static|final|abstract|sealed)\s+)*(class|interface|enum)\s+(\w+)")
METHOD_RE = re.compile(
    r"^\s+(?:(?:public|private|protected|static|final|synchronized|abstract|native|default)\s+)*"
    r"(?:<[^>]+>\s+)?[\w$<>\[\],.? ]+?\s+(\w+)\s*\(([^;{]*)\)\s*(?:throws\s+[\w$., ]+)?\s*\{?\s*$"
)
STRING_RE = re.compile(r'"((?:[^"\\\n]|\\.)*)"')
NOT_METHODS = {"if", "for", "while", "switch", "catch", "return", "new", "else", "synchronized", "try"}
MAX_STRING_LEN = 160
MAX_LOCATIONS = 4


def find_jar(answer_dir: Path, jar: str | None) -> Path:
    if jar:
        p = Path(jar)
        if not p.is_file():
            raise SystemExit(f"找不到 jar：{p}")
        return p
    jars = sorted(answer_dir.glob("*.jar"))
    if not jars:
        raise SystemExit(f"{answer_dir} 根目录下没有 jar")
    if len(jars) > 1:
        raise SystemExit(f"{answer_dir} 有多个 jar {[j.name for j in jars]}，请用 --jar 指定")
    return jars[0]


def run_cfr(jar: Path, src_dir: Path, cfr: Path) -> None:
    if not cfr.is_file():
        raise SystemExit(f"找不到 CFR：{cfr}")
    if shutil.which("java") is None:
        raise SystemExit("找不到 java 命令，请先安装 JDK/JRE 并加入 PATH")
    if src_dir.exists():
        shutil.rmtree(src_dir)
    src_dir.mkdir(parents=True)
    proc = subprocess.run(
        ["java", "-jar", str(cfr), str(jar), "--outputdir", str(src_dir), "--silent", "true"],
        capture_output=True, text=True, encoding="utf-8", errors="replace",
    )
    if proc.returncode != 0:
        raise SystemExit(f"CFR 失败（退出码 {proc.returncode}）：{(proc.stderr or proc.stdout)[-2000:]}")


def is_lib(rel: str) -> bool:
    return rel.startswith(LIB_PREFIXES)


def scan_file(path: Path) -> tuple[list[tuple[int, str]], list[tuple[int, str]], list[tuple[int, str]]]:
    types: list[tuple[int, str]] = []
    methods: list[tuple[int, str]] = []
    strings: list[tuple[int, str]] = []
    for no, line in enumerate(path.read_text(encoding="utf-8", errors="replace").splitlines(), 1):
        stripped = line.strip()
        if stripped.startswith(("//", "*", "/*", "import ", "package ")):
            continue
        m = TYPE_RE.match(line)
        if m:
            types.append((no, f"{m.group(1)} {m.group(2)}"))
        else:
            m = METHOD_RE.match(line)
            if m and m.group(1) not in NOT_METHODS and "=" not in line.split("(")[0]:
                params = " ".join(m.group(2).split())
                methods.append((no, f"{m.group(1)}({params})"))
        for s in STRING_RE.findall(line):
            if s and len(s) <= MAX_STRING_LEN:
                strings.append((no, s))
    return types, methods, strings


def build_index(jar: Path, src_dir: Path) -> str:
    files = sorted(p for p in src_dir.rglob("*.java"))
    biz: list[Path] = []
    libs: dict[str, int] = defaultdict(int)
    for p in files:
        rel = p.relative_to(src_dir).as_posix()
        if is_lib(rel):
            libs["/".join(rel.split("/")[:2])] += 1
        else:
            biz.append(p)

    out = [f"# 反编译索引：{jar.name}", "",
           f"- 源码目录：`{src_dir.as_posix()}`",
           f"- Java 文件 {len(files)} 个，业务代码 {len(biz)} 个，第三方库 {len(files) - len(biz)} 个", ""]
    if libs:
        out.append("## 第三方库（未展开）")
        out += [f"- `{k}/`：{v} 个文件" for k, v in sorted(libs.items())]
        out.append("")

    string_locs: dict[str, list[str]] = defaultdict(list)
    out.append("## 业务类与方法（文件:行号）")
    for p in biz:
        rel = p.relative_to(src_dir).as_posix()
        types, methods, strings = scan_file(p)
        lines = p.read_text(encoding="utf-8", errors="replace").count("\n") + 1
        out.append(f"### `{rel}`（{lines} 行）")
        out += [f"- {t} :{no}" for no, t in types]
        out += [f"  - {m} :{no}" for no, m in methods]
        for no, s in strings:
            locs = string_locs[s]
            if len(locs) < MAX_LOCATIONS:
                locs.append(f"{rel}:{no}")
            elif len(locs) == MAX_LOCATIONS:
                locs.append("...")
    out.append("")
    out.append("## 业务字符串常量（按内容排序，含返回码、提示文本、命令名等）")
    for s in sorted(string_locs):
        out.append(f"- `{s}` — {', '.join(string_locs[s])}")
    out.append("")
    return "\n".join(out)


def decompile(answer_dir: Path, out_dir: Path, jar: str | None = None, cfr: Path = DEFAULT_CFR) -> Path:
    jar_path = find_jar(answer_dir, jar).resolve()
    src_dir = (out_dir / "src").resolve()
    run_cfr(jar_path, src_dir, cfr)
    index = out_dir / "index.md"
    index.write_text(build_index(jar_path, src_dir), encoding="utf-8")
    return index


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--answer", default="answer")
    ap.add_argument("--out", default="decompiled")
    ap.add_argument("--jar", default=None)
    ap.add_argument("--cfr", default=str(DEFAULT_CFR))
    args = ap.parse_args(argv)
    index = decompile(Path(args.answer), Path(args.out), args.jar, Path(args.cfr))
    print(f"已反编译，索引：{index}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
