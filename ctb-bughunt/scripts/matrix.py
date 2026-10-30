# -*- coding: utf-8 -*-
"""接口 × 返回码矩阵：汇总所有录制与探针中实际触发的返回码，与 notes/spec.json 对照找缺口。

数据来源：notes/record/*.json（录制第 A 轮）与 notes/probes/*.txt（"[耗时] 命令 => 返回" 行）。
返回码：spec.json 的 code_pattern 在返回开头的匹配；没有 code_pattern 时取第一个 ':' 之前的部分。

缺口类别：
  未调用      spec 中的命令从未被调用
  未触发      spec 中命令的返回码从未出现
  单一返回码  命令只出现过一种返回码
  规格外      实际出现但 spec 中没有的命令或返回码（spec 可能漏了）

用法：
  python matrix.py --notes notes
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from collections import Counter, defaultdict
from pathlib import Path

PROBE_LINE_RE = re.compile(r"^\[\s*[\d.]+ms\] (.+?) => (.*)$")


def code_of(ret: str, pattern: str = "") -> str:
    if pattern:
        m = re.match(f"(?:{pattern})", ret)
        if m:
            return m.group(0)
    return ret.split(":", 1)[0]


def command_of(cmd: str) -> str:
    return cmd.split("(", 1)[0].strip()


def observations(notes: Path) -> list[tuple[str, str, str]]:
    """返回 [(命令全文, 返回, 来源)]。"""
    out: list[tuple[str, str, str]] = []
    for p in sorted((notes / "record").glob("*.json")):
        try:
            data = json.loads(p.read_text(encoding="utf-8-sig"))
        except (ValueError, OSError):
            continue
        for t in data.get("tests", []):
            if t.get("init"):
                out.append(("init()", t["init"], f"record:{data.get('name', p.stem)}"))
            for s in t.get("steps", []):
                out.append((s["cmd"], s["ret_a"], f"record:{data.get('name', p.stem)}"))
    for p in sorted((notes / "probes").glob("*.txt")):
        for line in p.read_text(encoding="utf-8-sig", errors="replace").splitlines():
            m = PROBE_LINE_RE.match(line)
            if m:
                out.append((m.group(1), m.group(2), f"probe:{p.stem}"))
    return out


def analyze(spec: dict, obs: list[tuple[str, str, str]]) -> dict:
    pattern = (spec or {}).get("code_pattern") or ""
    spec_cmds: dict = (spec or {}).get("commands", {})
    common = set((spec or {}).get("common_codes", []))
    seen: dict[str, Counter] = defaultdict(Counter)
    examples: dict[tuple[str, str], str] = {}
    for cmd, ret, _ in obs:
        name, code = command_of(cmd), code_of(ret, pattern)
        seen[name][code] += 1
        examples.setdefault((name, code), f"{cmd} => {ret}")
    lookup = {k.lower(): k for k in spec_cmds}
    gaps = {"未调用": [], "未触发": [], "单一返回码": [], "规格外": []}
    rows = []
    for name, info in spec_cmds.items():
        got = Counter()
        for obs_name, cnt in seen.items():
            if obs_name.lower() == name.lower():
                got.update(cnt)
        expected = list(info.get("codes", []))
        if not got:
            gaps["未调用"].append(name)
        else:
            missing = [c for c in expected if c not in got]
            for c in missing:
                gaps["未触发"].append(f"{name} {c}")
            if len(got) == 1:
                gaps["单一返回码"].append(f"{name} 只出现 {next(iter(got))}")
            extra = [c for c in got if c not in expected and c not in common]
            for c in extra:
                gaps["规格外"].append(f"{name} 出现规格外返回码 {c}：{examples.get((_match(seen, name), c), '')}")
        rows.append((name, expected, got))
    for obs_name, cnt in seen.items():
        if spec_cmds and obs_name.lower() not in lookup:
            gaps["规格外"].append(f"命令 {obs_name} 不在 spec 中（{sum(cnt.values())} 次）")
        if not spec_cmds:
            rows.append((obs_name, [], cnt))
    return {"rows": rows, "gaps": gaps, "total": len(obs), "has_spec": bool(spec_cmds)}


def _match(seen: dict, name: str) -> str:
    for k in seen:
        if k.lower() == name.lower():
            return k
    return name


def render(result: dict, reviewed: bool) -> str:
    gaps = result["gaps"]
    n = sum(len(v) for v in gaps.values())
    out = ["# 接口 × 返回码矩阵", "", f"缺口 {n} 项；观测 {result['total']} 次调用"]
    if not result["has_spec"]:
        out.append("没有 notes/spec.json，只列出实际分布；先执行 ctb.py spec 并审阅")
    elif not reviewed:
        out.append("spec.json 尚未审阅（reviewed=false），缺口可能来自误识别")
    out += ["", "| 命令 | 规格返回码 | 实际分布 |", "|---|---|---|"]
    for name, expected, got in result["rows"]:
        dist = " ".join(f"{c}×{k}" for c, k in got.most_common()) or "未调用"
        out.append(f"| {name} | {','.join(expected) or '-'} | {dist} |")
    for kind, items in gaps.items():
        out += ["", f"## {kind}（{len(items)}）", ""]
        out += [f"- {x}" for x in items] or ["无"]
    return "\n".join(out) + "\n"


def summary(result: dict) -> str:
    gaps = result["gaps"]
    n = sum(len(v) for v in gaps.values())
    lines = [f"矩阵缺口 {n} 项：" + "，".join(f"{k} {len(v)}" for k, v in gaps.items())]
    for kind in ("未调用", "未触发", "单一返回码", "规格外"):
        for x in gaps[kind][:8]:
            lines.append(f"  [{kind}] {x}")
        if len(gaps[kind]) > 8:
            lines.append(f"  [{kind}] ... 另有 {len(gaps[kind]) - 8} 项")
    return "\n".join(lines)


def build(notes: Path, quiet: bool = False) -> dict:
    spec_path = notes / "spec.json"
    spec = json.loads(spec_path.read_text(encoding="utf-8-sig")) if spec_path.is_file() else {}
    result = analyze(spec, observations(notes))
    md = notes / "matrix.md"
    md.write_text(render(result, bool(spec.get("reviewed"))), encoding="utf-8")
    if not quiet:
        print(summary(result))
        print(f"矩阵：{md}")
    return result


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--notes", default="notes")
    args = ap.parse_args(argv)
    build(Path(args.notes))
    return 0


if __name__ == "__main__":
    sys.exit(main())
