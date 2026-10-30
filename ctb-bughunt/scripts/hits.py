# -*- coding: utf-8 -*-
"""判题命中记录、用例精简与场景库。

判题结果中每个 bug 带有触发它的用例名。每次判题后：
  1. 按用例统计命中的 bug，写入 notes/hits.json 的轮次记录；
  2. 每个已检出的 bug 只保留命中它且本地耗时最短的用例，其余用例（未命中、或命中的 bug 都已由更快的用例覆盖）删除；
  3. 每个用例的场景（描述、规模、命令与返回码分布、命中情况）追加到 notes/场景库.md，
     步骤指纹记入 hits.json，之后录制与已删除场景步骤完全相同的测试会被跳过。
有已检出的 bug 无法对应到用例时不做删除。
"""
from __future__ import annotations

import ast
import hashlib
import json
import re
from collections import Counter
from datetime import datetime
from pathlib import Path

from naming import TABLE_PREFIX

KEPT, REPLACED, MISSED = "命中保留", "命中已替代", "未命中"
CASE_TOKEN_RE = re.compile(r"(?<![A-Za-z0-9_])test[A-Za-z0-9_]*")
EMPTY_BLOCK_RE = re.compile(
    r"^[ \t]*# ==== ctb-record (tests|tables): (\w+) begin ====\n(?:[ \t]*\n)*"
    r"[ \t]*# ==== ctb-record \1: \2 end ====\n?", re.M)
INF = float("inf")


# ---------------------------------------------------------------- 读取

def _read(path: Path) -> tuple[str, str]:
    if not path.is_file():
        return "", "\n"
    raw = path.read_bytes().decode("utf-8-sig")
    return raw.replace("\r\n", "\n"), ("\r\n" if "\r\n" in raw else "\n")


def _write(path: Path, text: str, eol: str) -> None:
    path.write_bytes(text.replace("\n", eol).encode("utf-8"))


def answer_digest(answer_dir: Path) -> str:
    h = hashlib.sha256()
    for name in ("udpunittest.py", "testaw.py"):
        h.update(name.encode())
        h.update(_read(answer_dir / name)[0].encode("utf-8"))
    return h.hexdigest()[:16]


def _case_class(tree: ast.Module) -> ast.ClassDef | None:
    for node in tree.body:
        if isinstance(node, ast.ClassDef) and node.name == "MyTestCase1":
            return node
    return None


def test_methods(udp_text: str) -> dict[str, ast.FunctionDef]:
    cls = _case_class(ast.parse(udp_text))
    if cls is None:
        return {}
    return {n.name: n for n in cls.body if isinstance(n, ast.FunctionDef) and n.name.startswith("test")}


def step_tables(aw_text: str) -> dict[str, list[str]]:
    """testaw.py 中录制生成的 cjl_steps_<测试名> 表 -> 命令模板列表。"""
    out: dict[str, list[str]] = {}
    try:
        tree = ast.parse(aw_text)
    except SyntaxError:
        return out
    for node in tree.body:
        if (isinstance(node, ast.Assign) and len(node.targets) == 1 and isinstance(node.targets[0], ast.Name)
                and node.targets[0].id.startswith(TABLE_PREFIX)):
            try:
                rows = ast.literal_eval(node.value)
                out[node.targets[0].id[len(TABLE_PREFIX):]] = [str(r[0]) for r in rows]
            except (ValueError, TypeError, IndexError):
                continue
    return out


def fingerprint(cmds: list[str]) -> str:
    return hashlib.sha1("\n".join(cmds).encode("utf-8")).hexdigest()[:16]


def _source_fp(udp_text: str, fn: ast.FunctionDef) -> str:
    seg = ast.get_source_segment(udp_text, fn) or fn.name
    body = "\n".join(line.strip() for line in seg.splitlines()[1:] if line.strip())
    return "src:" + hashlib.sha1(body.encode("utf-8")).hexdigest()[:16]


def _record_tests(notes: Path) -> dict[str, list[dict]]:
    out: dict[str, list[dict]] = {}
    rdir = notes / "record"
    for p in sorted(rdir.glob("*.json")) if rdir.is_dir() else []:
        try:
            data = json.loads(p.read_text(encoding="utf-8"))
        except (OSError, ValueError):
            continue
        for t in data.get("tests") or []:
            out.setdefault(t.get("name", ""), []).append({**t, "_record": data.get("name") or p.stem})
    return out


def _local_costs(notes: Path) -> dict[str, float]:
    p = notes / "local_last.json"
    if p.is_file():
        try:
            return {k: float(v) for k, v in (json.loads(p.read_text(encoding="utf-8")).get("costs") or {}).items()}
        except (OSError, ValueError):
            pass
    return {}


def load(notes: Path) -> dict:
    p = notes / "hits.json"
    if p.is_file():
        return json.loads(p.read_text(encoding="utf-8"))
    return {"rounds": [], "scenarios": {}}


def save(notes: Path, data: dict) -> None:
    (notes / "hits.json").write_text(json.dumps(data, ensure_ascii=False, indent=1), encoding="utf-8")


# ---------------------------------------------------------------- 场景

def describe(answer_dir: Path, notes: Path) -> dict[str, dict]:
    """当前每个测试的指纹、来源、场景描述、规模与分布。"""
    udp_text = _read(answer_dir / "udpunittest.py")[0]
    tables = step_tables(_read(answer_dir / "testaw.py")[0])
    records = _record_tests(notes)
    costs = _local_costs(notes)
    out: dict[str, dict] = {}
    for name, fn in test_methods(udp_text).items():
        info: dict = {"seconds": costs.get(name)}
        cmds = tables.get(name) if f"{TABLE_PREFIX}{name}" in (ast.get_source_segment(udp_text, fn) or "") else None
        if cmds is not None:
            fp = fingerprint(cmds)
            info.update(fp=fp, source="录制", steps=len(cmds))
            for r in records.get(name, []):
                if fingerprint(r.get("template") or []) == fp:
                    info.update(source=f"录制 {r['_record']}", scenario=r.get("scenario") or "")
                    if info["seconds"] is None:
                        info["seconds"] = r.get("seconds")
                    calls = Counter(s["cmd"].split("(", 1)[0] for s in r["steps"])
                    codes = Counter(s["ret_a"].split(":", 1)[0] for s in r["steps"])
                    info["calls"] = " ".join(f"{k}×{v}" for k, v in calls.most_common())
                    info["codes"] = " ".join(f"{k}×{v}" for k, v in codes.most_common())
                    break
        else:
            info.update(fp=_source_fp(udp_text, fn), source="手写", scenario=(ast.get_docstring(fn) or "").strip())
        out[name] = info
    return out


# ---------------------------------------------------------------- 命中与精简

def map_cases(cases: list, names: set[str]) -> set[str]:
    out = set()
    for c in cases or []:
        out.update(tok for tok in CASE_TOKEN_RE.findall(str(c)) if tok in names)
    return out


def plan(items: list[dict], names: list[str], costs: dict[str, float]) -> dict:
    nameset = set(names)
    hits: dict[str, list] = {n: [] for n in names}
    unattributed = []
    detected = []
    for b in items:
        if not b.get("detected"):
            continue
        bug = str(b.get("bugId"))
        tests = map_cases(b.get("cases") or [], nameset)
        if not tests:
            unattributed.append(bug)
            continue
        detected.append(bug)
        for t in tests:
            hits[t].append(bug)
    keep: dict[str, list] = {}
    for bug in detected:
        cands = [t for t in names if bug in hits[t]]
        best = min(cands, key=lambda t: (costs.get(t) if costs.get(t) is not None else INF, -len(hits[t]), t))
        keep.setdefault(best, []).append(bug)
    replaced = {t: b for t, b in hits.items() if b and t not in keep}
    missed = [t for t in names if not hits[t]]
    return {"hits": hits, "keep": keep, "replaced": replaced, "missed": missed, "unattributed": unattributed}


def _drop_lines(lines: list[str], start: int, end: int) -> None:
    """删除第 start..end 行（1 起，含），连同其后一个空行。"""
    s, e = start - 1, end
    if e < len(lines) and not lines[e].strip():
        e += 1
    del lines[s:e]


def remove_tests(answer_dir: Path, names: list[str]) -> None:
    ut_path, aw_path = answer_dir / "udpunittest.py", answer_dir / "testaw.py"
    ut, ut_eol = _read(ut_path)
    aw, aw_eol = _read(aw_path)
    fns = test_methods(ut)
    lines = ut.split("\n")
    spans = []
    for n in names:
        fn = fns.get(n)
        if fn is not None:
            spans.append((min([d.lineno for d in fn.decorator_list] + [fn.lineno]), fn.end_lineno))
    for start, end in sorted(spans, reverse=True):
        _drop_lines(lines, start, end)
    new_ut = EMPTY_BLOCK_RE.sub("", "\n".join(lines))

    aw_lines = aw.split("\n")
    spans = []
    for node in ast.parse(aw).body if aw else []:
        if not (isinstance(node, ast.Assign) and len(node.targets) == 1 and isinstance(node.targets[0], ast.Name)):
            continue
        var = node.targets[0].id
        if (var.startswith(TABLE_PREFIX) and var[len(TABLE_PREFIX):] in names
                and not re.search(rf"\b{re.escape(var)}\b", new_ut)):
            spans.append((node.lineno, node.end_lineno))
    for start, end in sorted(spans, reverse=True):
        _drop_lines(aw_lines, start, end)
    new_aw = EMPTY_BLOCK_RE.sub("", "\n".join(aw_lines))

    try:
        ast.parse(new_ut)
        ast.parse(new_aw)
    except SyntaxError as exc:
        raise SystemExit(f"删除用例后语法错误（{exc}），未做任何修改；请手工删除：{', '.join(names)}") from None
    left = set(test_methods(new_ut))
    if left & set(names):
        raise SystemExit(f"未能删除：{', '.join(sorted(left & set(names)))}，未做任何修改")
    _write(ut_path, new_ut, ut_eol)
    if aw_path.is_file():
        _write(aw_path, new_aw, aw_eol)


def _library_block(rnd: dict, info: dict[str, dict], p: dict) -> str:
    out = [f"## {rnd['time']} {rnd['kind']} judgeId={rnd.get('judge_id')} "
           f"检出 {rnd.get('passed')}/{rnd.get('total')}", ""]
    if p["unattributed"]:
        out += [f"无法对应到用例的已检出 bug：{', '.join(p['unattributed'])}（本轮未删除用例）", ""]
    order = list(p["keep"]) + list(p["replaced"]) + p["missed"]
    for name in order:
        i = info.get(name, {})
        if name in p["keep"]:
            status = f"{KEPT}：bug {', '.join(p['hits'][name])}（是 bug {', '.join(p['keep'][name])} 耗时最短的命中用例）"
        elif name in p["replaced"]:
            status = f"{REPLACED}：bug {', '.join(p['hits'][name])} 已由更快的用例覆盖"
        else:
            status = MISSED
        sec = i.get("seconds")
        out += [f"### {name}：{status}",
                f"- 来源：{i.get('source', '?')}",
                f"- 场景：{i.get('scenario') or '（未描述）'}",
                f"- 规模：步骤 {i.get('steps', '?')}，耗时 {f'{sec:.1f}s' if isinstance(sec, (int, float)) else '?'}"]
        if i.get("calls"):
            out.append(f"- 命令：{i['calls']}")
        if i.get("codes"):
            out.append(f"- 返回码：{i['codes']}")
        out.append("")
    return "\n".join(out)


def apply(answer_dir: Path, notes: Path, judge: dict, kind: str, prune: bool = True) -> str:
    bug = judge.get("bugDetect") or {}
    items = bug.get("items") or []
    if not items:
        return "判题结果没有 bug 检出明细，未记录命中"
    info = describe(answer_dir, notes)
    names = sorted(info)
    costs = {n: i["seconds"] for n, i in info.items() if i.get("seconds") is not None}
    p = plan(items, names, costs)
    data = load(notes)
    before = {b for r in data["rounds"] for t in r.get("tests", {}).values() for b in t.get("bugs", [])}
    remove = [] if p["unattributed"] else sorted(set(p["replaced"]) | set(p["missed"]))
    rnd = {
        "time": f"{datetime.now():%Y-%m-%d %H:%M:%S}", "kind": kind, "judge_id": judge.get("judgeId"),
        "score": judge.get("score"), "passed": bug.get("passed"), "total": bug.get("total"),
        "digest": answer_digest(answer_dir), "unattributed": p["unattributed"], "remove": remove,
        "tests": {n: {"bugs": p["hits"][n], "seconds": info[n].get("seconds"), "fp": info[n]["fp"]} for n in names},
    }
    now = {b for t in rnd["tests"].values() for b in t["bugs"]} | set(p["unattributed"])
    lost = sorted(before - now)
    rnd["lost"] = lost
    data["rounds"].append(rnd)
    for n in names:
        status = KEPT if n in p["keep"] else REPLACED if n in p["replaced"] else MISSED
        data["scenarios"][info[n]["fp"]] = {"name": n, "status": status, "bugs": p["hits"][n],
                                            "round": len(data["rounds"]), "source": info[n].get("source")}
    lib = notes / "场景库.md"
    head = "" if lib.is_file() else "# 场景库\n\n"
    with lib.open("a", encoding="utf-8") as f:
        f.write(head + _library_block(rnd, info, p) + "\n")

    msg = [f"命中：{len(p['keep'])} 个用例保留（覆盖 bug {', '.join(b for v in p['keep'].values() for b in v) or '无'}），"
           f"命中但被替代 {len(p['replaced'])} 个，未命中 {len(p['missed'])} 个；场景已追加到 {lib}"]
    if lost:
        msg.append(f"注意：以前命中过、本轮未命中的 bug：{', '.join(lost)}（判题结果稳定，说明命中这些 bug 的用例被修改或删除了）")
    if p["unattributed"]:
        msg.append(f"已检出 bug {', '.join(p['unattributed'])} 无法对应到用例，本轮不删除用例")
    elif prune and remove:
        remove_tests(answer_dir, remove)
        rnd["digest_after"] = answer_digest(answer_dir)
        rnd["pruned"] = True
        msg.append(f"已删除 {len(remove)} 个用例：{', '.join(remove)}")
    elif remove:
        msg.append(f"待删除 {len(remove)} 个用例（未执行，用 ctb.py prune 删除）：{', '.join(remove)}")
    save(notes, data)
    return "\n".join(msg)


def prune_last(answer_dir: Path, notes: Path) -> str:
    data = load(notes)
    if not data["rounds"]:
        return "还没有命中记录"
    rnd = data["rounds"][-1]
    if rnd.get("pruned"):
        return "最近一轮已删除过"
    if rnd.get("unattributed"):
        return f"最近一轮有无法对应到用例的已检出 bug（{', '.join(rnd['unattributed'])}），不删除"
    if rnd["digest"] != answer_digest(answer_dir):
        return "答题文件在判题后已修改，最近一轮的命中结果不再对应当前用例；请重新判题后再删除"
    if not rnd["remove"]:
        return "没有需要删除的用例"
    remove_tests(answer_dir, rnd["remove"])
    rnd["pruned"] = True
    rnd["digest_after"] = answer_digest(answer_dir)
    save(notes, data)
    return f"已删除 {len(rnd['remove'])} 个用例：{', '.join(rnd['remove'])}"


def screen(notes: Path, tests: list[dict]) -> tuple[dict[str, str], list[str]]:
    """录制前比对场景库：返回 (要跳过写入的测试 -> 原因, 警告)。"""
    data = load(notes)
    scen = data.get("scenarios") or {}
    skip: dict[str, str] = {}
    warns: list[str] = []
    kept_fp = {v["name"]: fp for fp, v in scen.items() if v.get("status") == KEPT}
    for t in tests:
        fp = fingerprint([s["cmd"] for s in t["steps"]])
        hit = scen.get(fp)
        if hit and hit["status"] != KEPT:
            skip[t["name"]] = f"与场景库中 {hit['name']}（{hit['status']}）步骤完全相同"
        elif hit and hit["name"] != t["name"]:
            skip[t["name"]] = f"与已有用例 {hit['name']}（{hit['status']}）步骤完全相同"
        elif t["name"] in kept_fp and kept_fp[t["name"]] != fp:
            warns.append(f"{t['name']} 是命中保留的用例，本次步骤已变化，可能不再命中原来的 bug；新场景请用新测试名")
    return skip, warns


def summary(notes: Path) -> str:
    data = load(notes)
    if not data["rounds"]:
        return "无"
    r = data["rounds"][-1]
    kept = sum(1 for v in data["scenarios"].values() if v["status"] == KEPT)
    tried = len(data["scenarios"])
    ever = sorted({b for x in data["rounds"] for t in x["tests"].values() for b in t["bugs"]},
                  key=lambda s: (len(s), s))
    return (f"最近 {r['kind']} 检出 {r.get('passed')}/{r.get('total')}；累计命中过 bug {len(ever)} 个；"
            f"场景库 {tried} 条（命中保留 {kept}）")
