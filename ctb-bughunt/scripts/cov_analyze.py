# -*- coding: utf-8 -*-
"""覆盖率分析：解析 JaCoCo XML、业务类分层、方法内未覆盖行定位、每个测试的增量贡献。

分层（优先级从高到低）：
  notes/coverage/scope.json  按类名前缀指定 core / related / exclude，始终优先
  核心   含入口方法的类：方法名与命令名去下划线、忽略大小写后相同；
         且匹配到至少 2 个命令，或匹配的命令只在这一个类中有同名方法（只有 1 个命令时不限）
         没有任何入口方法时，改用"启动基线中未执行、命令执行后才执行"的类
  相关   含命令名字符串常量的类（命令分发）；核心类引用的业务类；
         再由这些类引用、且与某个核心类同包的类（第 2 跳）；核心/相关类的内部类
  基础设施  其余业务类，不计入主指标
"""
from __future__ import annotations

import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path

from classfile import ClassFileError, parse as parse_class
from decompile import is_lib

COUNTERS = ("LINE", "BRANCH", "METHOD")
CORE, RELATED, INFRA = "核心", "相关", "基础设施"
SYNTHETIC_PREFIX = ("lambda$", "access$")
IMPORT_RE = re.compile(r"^import\s+([\w.]+)\s*;", re.M)
TYPE_TOKEN_RE = re.compile(r"\b([A-Z]\w*)\b")
STRING_RE = re.compile(r'"((?:[^"\\\n]|\\.)*)"')
NOT_DECL_START = ("if", "while", "for", "return", "else", "new", "throw", "switch", "case", "catch", "do", "try")
CLUE_WEIGHT = {"str": 3, "num": 2, "name": 1, "type": 1}


def norm(name: str) -> str:
    return name.replace("_", "").lower()


def dotted(cname: str) -> str:
    return cname.replace("/", ".")


def simple(cname: str) -> str:
    return cname.rsplit("/", 1)[-1]


def package(cname: str) -> str:
    return cname.rsplit("/", 1)[0] if "/" in cname else ""


def outer(cname: str) -> str:
    return cname.split("$", 1)[0]


def _counters(node) -> dict:
    return {c.get("type"): (int(c.get("covered")), int(c.get("missed"))) for c in node.findall("counter")}


# ---------------------------------------------------------------- XML

def parse_xml(xml_path: Path) -> dict:
    root = ET.parse(str(xml_path)).getroot()
    classes: dict = {}
    sources: dict = {}
    for pkg in root.iter("package"):
        pname = pkg.get("name") or ""
        for cls in pkg.findall("class"):
            cname = cls.get("name") or ""
            if is_lib(cname):
                continue
            methods = []
            for m in cls.findall("method"):
                line = m.get("line")
                methods.append({"name": m.get("name") or "", "desc": m.get("desc") or "",
                                "line": int(line) if line else None, "c": _counters(m)})
            classes[cname] = {"c": _counters(cls), "methods": methods,
                              "source": (pname, cls.get("sourcefilename") or "")}
        for sf in pkg.findall("sourcefile"):
            sources[(pname, sf.get("name") or "")] = {
                int(ln.get("nr")): (int(ln.get("mi")), int(ln.get("ci")), int(ln.get("mb")), int(ln.get("cb")))
                for ln in sf.findall("line")}
    return {"classes": classes, "sources": sources}


def executed_classes(model: dict) -> set[str]:
    return {c for c, info in model["classes"].items() if info["c"].get("METHOD", (0, 0))[0] > 0}


# ---------------------------------------------------------------- 分层

def load_scope(path: Path | None) -> dict:
    if path and path.is_file():
        data = json.loads(path.read_text(encoding="utf-8-sig"))
        return {k: [p.replace("/", ".") for p in data.get(k, [])] for k in ("core", "related", "exclude")}
    return {"core": [], "related": [], "exclude": []}


class SourceIndex:
    """反编译源码访问（按外部类定位 .java 文件）。"""

    def __init__(self, src_dir: Path | None):
        self.src_dir = src_dir if src_dir and src_dir.is_dir() else None
        self._text: dict[str, str | None] = {}

    def path(self, cname: str) -> Path | None:
        if not self.src_dir:
            return None
        p = self.src_dir / (outer(cname) + ".java")
        return p if p.is_file() else None

    def text(self, cname: str) -> str | None:
        key = outer(cname)
        if key not in self._text:
            p = self.path(cname)
            self._text[key] = p.read_text(encoding="utf-8", errors="replace") if p else None
        return self._text[key]

    def lines(self, cname: str) -> list[str]:
        t = self.text(cname)
        return t.splitlines() if t is not None else []


def _refs(cname: str, text: str, outer_names: set[str]) -> set[str]:
    pkg = package(cname)
    out = set()
    for imp in IMPORT_RE.findall(text):
        c = imp.replace(".", "/")
        if c in outer_names:
            out.add(c)
    for tok in set(TYPE_TOKEN_RE.findall(text)):
        c = f"{pkg}/{tok}" if pkg else tok
        if c in outer_names:
            out.add(c)
    out.discard(outer(cname))
    return out


def classify(model: dict, commands: list[str], src: SourceIndex, scope: dict,
             started: set[str] | None = None) -> tuple[dict, list[str]]:
    classes = model["classes"]
    tiers: dict[str, tuple[str, str]] = {}
    notes: list[str] = []
    ncmd = {norm(c) for c in commands}
    hits_by_class: dict[str, set[str]] = {}
    for c, info in classes.items():
        hits = {norm(m["name"]) for m in info["methods"] if norm(m["name"]) in ncmd}
        if hits:
            hits_by_class[c] = hits
    owners: dict[str, int] = {}
    for hits in hits_by_class.values():
        for h in hits:
            owners[h] = owners.get(h, 0) + 1
    weak = []
    for c, hits in hits_by_class.items():
        names = sorted({m["name"] for m in classes[c]["methods"] if norm(m["name"]) in hits})
        if len(hits) >= 2 or len(ncmd) == 1 or owners[next(iter(hits))] == 1:
            tiers[c] = (CORE, "入口方法 " + ",".join(names))
        else:
            weak.append(f"{simple(c)}.{names[0]}")
    if weak:
        notes.append("以下类只匹配到一个在多个类中同名的命令方法，未计入核心：" + "，".join(sorted(weak)[:10]))
    if not commands:
        notes.append("没有命令清单（notes/spec.json 或录制步骤），无法按入口方法识别核心类")
    if not tiers and started:
        for c in sorted(started):
            if "$" not in c:
                tiers[c] = (CORE, "启动基线中未执行、命令执行后才执行")
        notes.append("没有匹配到入口方法，核心类改用启动基线差集判断")
    if not tiers:
        notes.append("未识别出核心类：请在 notes/coverage/scope.json 的 core 中按类名前缀指定")

    outer_names = {c for c in classes if "$" not in c}
    core = [c for c, t in tiers.items() if t[0] == CORE]
    cmd_lower = {c.lower() for c in commands}
    for c in sorted(outer_names - set(tiers)):
        text = src.text(c)
        if text is None:
            continue
        hit = sorted({s for s in STRING_RE.findall(text) if s.lower() in cmd_lower})
        if hit:
            tiers[c] = (RELATED, "含命令名字符串 " + ",".join(hit[:5]))
    hop1 = []
    for c in core:
        text = src.text(c)
        for r in sorted(_refs(c, text, outer_names)) if text else []:
            if r not in tiers:
                tiers[r] = (RELATED, f"被 {simple(c)} 引用")
                hop1.append(r)
    core_pkgs = {package(c) for c in core}
    for c in hop1:
        text = src.text(c)
        for r in sorted(_refs(c, text, outer_names)) if text else []:
            if r not in tiers and package(r) in core_pkgs:
                tiers[r] = (RELATED, f"被 {simple(c)} 引用（第 2 跳，同包）")
    if src.src_dir is None:
        notes.append("没有反编译源码，相关类只能靠 scope.json 指定（先执行 decompile）")
    for c in classes:
        if "$" in c and c not in tiers and outer(c) in tiers:
            tiers[c] = (tiers[outer(c)][0], "内部类")
    for tier, key in ((INFRA, "exclude"), (RELATED, "related"), (CORE, "core")):
        for c in classes:
            if any(dotted(c).startswith(p) for p in scope.get(key, [])):
                tiers[c] = (tier, f"scope.json {key}")
    for c in classes:
        tiers.setdefault(c, (INFRA, ""))
    return tiers, notes


def totals(model: dict, tiers: dict, wanted: set[str]) -> dict:
    out = {k: [0, 0] for k in COUNTERS}
    for c, info in model["classes"].items():
        if tiers.get(c, (INFRA,))[0] not in wanted:
            continue
        for k in COUNTERS:
            cov, miss = info["c"].get(k, (0, 0))
            out[k][0] += cov
            out[k][1] += cov + miss
    return {k: tuple(v) for k, v in out.items()}


# ---------------------------------------------------------------- 方法内缺口

def method_lines(model: dict) -> dict:
    """(类, 方法名, 描述符) -> 方法覆盖的原始行号列表（按同一源文件中下一个方法的起始行切分）。"""
    starts: dict = {}
    for c, info in model["classes"].items():
        for m in info["methods"]:
            if m["line"] and not m["name"].startswith(SYNTHETIC_PREFIX):
                starts.setdefault(info["source"], set()).add(m["line"])
    out = {}
    for c, info in model["classes"].items():
        lines = sorted(model["sources"].get(info["source"], {}))
        bounds = sorted(starts.get(info["source"], ()))
        for m in info["methods"]:
            if not m["line"]:
                continue
            nxt = next((b for b in bounds if b > m["line"]), None)
            out[(c, m["name"], m["desc"])] = [n for n in lines if n >= m["line"] and (nxt is None or n < nxt)]
    return out


def method_gaps(model: dict, cname: str, lines: list[int]) -> tuple[list[int], list[tuple[int, int, int]]]:
    data = model["sources"].get(model["classes"][cname]["source"], {})
    missed, branches = [], []
    for n in lines:
        mi, ci, mb, cb = data.get(n, (0, 0, 0, 0))
        if ci == 0 and mi > 0:
            missed.append(n)
        if mb > 0:
            branches.append((n, mb, mb + cb))
    return missed, branches


def _java_literal(s: str) -> str:
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + '"'


def _clue_hits(clue: tuple, line: str) -> bool:
    kind, val = clue
    if kind == "str":
        return _java_literal(val) in line
    if kind == "num":
        text = str(val)
        if isinstance(val, float) and val.is_integer():
            text = str(int(val))
        return re.search(rf"(?<![\w.]){re.escape(text.lstrip('-'))}[LlFfDd]?(?![\w.])", line) is not None
    return re.search(rf"\b{re.escape(str(val))}\b", line) is not None


class Locator:
    """把原始行号映射到反编译源码行：用 class 文件中该行引用的常量/方法/字段在方法体内打分匹配。"""

    def __init__(self, src: SourceIndex, classes_dir: Path):
        self.src = src
        self.classes_dir = classes_dir
        self._clues: dict = {}

    def clues(self, cname: str, mname: str, desc: str) -> dict:
        if cname not in self._clues:
            p = self.classes_dir / (cname + ".class")
            try:
                self._clues[cname] = parse_class(p.read_bytes()) if p.is_file() else {}
            except (ClassFileError, OSError, IndexError, ValueError):
                self._clues[cname] = {}
        return self._clues[cname].get((mname, desc), {})

    def region(self, cname: str, mname: str) -> tuple[int, int] | None:
        lines = self.src.lines(cname)
        if not lines:
            return None
        name = simple(cname).rsplit("$", 1)[-1] if mname == "<init>" else mname
        pat = re.compile(rf"\b{re.escape(name)}\s*\(")
        for i, line in enumerate(lines):
            s = line.strip()
            head = s.split("(")[0]
            first = head.split()[0] if head.split() else ""
            if not pat.search(s) or s.endswith(";") or "=" in head or first in NOT_DECL_START or first.startswith("."):
                continue
            depth, seen = 0, False
            for j in range(i, len(lines)):
                depth += lines[j].count("{") - lines[j].count("}")
                seen = seen or "{" in lines[j]
                if seen and depth <= 0:
                    return i + 1, j + 1
            return i + 1, len(lines)
        return None

    def decl_line(self, cname: str, mname: str) -> int | None:
        r = self.region(cname, mname)
        return r[0] if r else None

    def locate(self, cname: str, mname: str, desc: str, line: int) -> tuple[int, str] | None:
        clues = self.clues(cname, mname, desc).get(line)
        lines = self.src.lines(cname)
        if not clues or not lines:
            return None
        lo, hi = self.region(cname, mname) or (1, len(lines))
        best, best_no = 0, None
        for no in range(lo, hi + 1):
            text = lines[no - 1]
            score = sum(CLUE_WEIGHT.get(c[0], 1) for c in clues if _clue_hits(c, text))
            if score > best:
                best, best_no = score, no
        return (best_no, lines[best_no - 1].strip()) if best_no else None

    def clue_text(self, cname: str, mname: str, desc: str, line: int) -> str:
        clues = self.clues(cname, mname, desc).get(line) or set()
        parts = [_java_literal(v) if k == "str" else str(v) for k, v in sorted(clues, key=lambda c: (c[0], str(c[1])))]
        return ", ".join(parts[:6])


# ---------------------------------------------------------------- 每测试贡献

def line_set(model: dict, tiers: dict, wanted: set[str]) -> dict:
    keep = {info["source"] for c, info in model["classes"].items() if tiers.get(c, (INFRA,))[0] in wanted}
    out = {}
    for key in keep:
        for nr, (mi, ci, mb, cb) in model["sources"].get(key, {}).items():
            if ci > 0:
                out[(key, nr)] = cb
    return out


def per_test_stats(baseline: dict | None, tests: list[tuple[str, dict]], tiers: dict, wanted: set[str]) -> list[dict]:
    base = set(line_set(baseline, tiers, wanted)) if baseline else set()
    sets = [(name, {k: v for k, v in line_set(m, tiers, wanted).items() if k not in base}) for name, m in tests]
    rows, cum = [], {}
    for i, (name, s) in enumerate(sets):
        others = set().union(*(set(o) for j, (_, o) in enumerate(sets) if j != i)) if len(sets) > 1 else set()
        new_br = 0
        for k, cb in s.items():
            prev = cum.get(k, 0)
            if cb > prev:
                new_br += cb - prev
                cum[k] = cb
        rows.append({"test": name, "lines": len(s), "unique": len(set(s) - others), "new_branches": new_br})
    return rows
