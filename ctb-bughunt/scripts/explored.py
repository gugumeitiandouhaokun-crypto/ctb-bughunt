# -*- coding: utf-8 -*-
"""已探索范围：历次正式提交的用例执行过的业务代码行（核心+相关类）与触发过的返回码。

每次正式提交判题结束后，把提交版本的覆盖行与返回码并入 notes/explored.json；
被精简删除的用例探索过的范围仍保留在集合里。之后的覆盖率报告与提交门槛都相对这个集合计算新增，
避免把已经测过且未命中的区域当成新进展。

  notes/explored.json                 {"lines": {"包/文件:行号": 已覆盖分支数}, "codes": ["命令|返回码"], "submits": [...]}
  notes/coverage/answer_<指纹>.json   run 时统计的某个答题文件版本的覆盖行与返回码
"""
from __future__ import annotations

import json
from datetime import datetime
from pathlib import Path

import cov_analyze as ca
import hits
import matrix

WANTED = {ca.CORE, ca.RELATED}


def _path(notes: Path) -> Path:
    return notes / "explored.json"


def load(notes: Path) -> dict:
    p = _path(notes)
    if p.is_file():
        data = json.loads(p.read_text(encoding="utf-8"))
        data.setdefault("lines", {})
        data.setdefault("codes", [])
        data.setdefault("submits", [])
        return data
    return {"lines": {}, "codes": [], "submits": []}


def save(notes: Path, data: dict) -> None:
    _path(notes).write_text(json.dumps(data, ensure_ascii=False, indent=0), encoding="utf-8")


def line_key(source: tuple[str, str], nr: int) -> str:
    return f"{source[0]}/{source[1]}:{nr}"


def lines_of(model: dict, tiers: dict) -> dict[str, int]:
    return {line_key(src, nr): cb for (src, nr), cb in ca.line_set(model, tiers, WANTED).items()}


def codes_of(answer_dir: Path, notes: Path, pattern: str = "") -> list[str]:
    """当前答题文件中录制用例触发的 命令|返回码（取自对应录制的原始结果）。"""
    info = hits.describe(answer_dir, notes)
    records = hits._record_tests(notes)
    out = set()
    for name, t in info.items():
        for r in records.get(name, []):
            if hits.fingerprint(r.get("template") or []) == t["fp"]:
                out.update(f"{matrix.command_of(s['cmd'])}|{matrix.code_of(s['ret_a'], pattern)}"
                           for s in r.get("steps") or [])
                break
    return sorted(out)


def novelty(explored: dict, lines: dict[str, int], codes: list[str]) -> dict:
    old = explored["lines"]
    new_lines = [k for k in lines if k not in old]
    new_branches = sum(max(0, cb - old.get(k, 0)) for k, cb in lines.items())
    new_codes = sorted(set(codes) - set(explored["codes"]))
    return {"lines": len(new_lines), "branches": new_branches, "codes": new_codes}


def describe_novelty(nov: dict) -> str:
    text = f"新增行 {nov['lines']}，新增分支 {nov['branches']}"
    if nov.get("codes") is None:
        return text
    codes = "、".join(nov["codes"][:10]) + (" 等" if len(nov["codes"]) > 10 else "")
    return text + f"，新增返回码 {len(nov['codes'])}" + (f"（{codes}）" if nov["codes"] else "")


def is_new(nov: dict) -> bool:
    return bool(nov["lines"] or nov["branches"] or nov["codes"])


def merge(notes: Path, lines: dict[str, int], codes: list[str], name: str) -> dict:
    data = load(notes)
    for k, cb in lines.items():
        data["lines"][k] = max(cb, data["lines"].get(k, 0))
    data["codes"] = sorted(set(data["codes"]) | set(codes))
    data["submits"].append(name)
    save(notes, data)
    return data


def _answer_path(notes: Path, digest: str) -> Path:
    return notes / "coverage" / f"answer_{digest}.json"


def save_answer(notes: Path, digest: str, lines: dict[str, int], codes: list[str]) -> None:
    p = _answer_path(notes, digest)
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps({"digest": digest, "at": f"{datetime.now():%Y-%m-%d %H:%M:%S}",
                             "lines": lines, "codes": codes}, ensure_ascii=False, indent=0), encoding="utf-8")


def load_answer(notes: Path, digest: str) -> dict | None:
    p = _answer_path(notes, digest)
    return json.loads(p.read_text(encoding="utf-8")) if p.is_file() else None


def summary(notes: Path) -> str:
    data = load(notes)
    if not data["submits"]:
        return "无（还没有正式提交）"
    return f"{len(data['lines'])} 行、{len(data['codes'])} 个 命令|返回码，来自 {len(data['submits'])} 次提交"
