# -*- coding: utf-8 -*-
"""ctb-bughunt 统一入口。在一道题的工作目录（默认当前目录）下执行。

工作目录约定：
  cookies.json   iLearning 登录 Cookie
  answer/        pull 下来的题目描述、框架、jar、testaw.py、udpunittest.py（提交的就是这里的文件）
  decompiled/    反编译源码 src/ 与 index.md
  notes/         ctb.json、pristine/（pull 原件）、题目分析.md、数值模型.md、复盘.md、场景库.md、hits.json、
                 spec.json、matrix.md、probes/、steps/、record/、coverage/（含 scope.json）、judge/、local_last.*

子命令：
  cookies                 获取/刷新登录 Cookie（弹浏览器免密登录）
  pull --subject <id|URL> 拉取题目到 answer/，保存原件与 subject，udpPort 改为本工作目录专用端口
  info                    题目状态与提交记录
  decompile [--jar X]     反编译 answer/ 下的 jar 到 decompiled/
  analysis [--done]       题目分析固化状态；--done 记录分析对应的题目与 jar 指纹
  spec [--force]          从题目描述与反编译源码起草接口规格 notes/spec.json（需 agent 审阅）
  probe -f F | -c CMD     探针：在无 bug 程序上发送命令看真实返回
  record -f F | --gen G   只写步骤，录制真实返回生成数据表断言用例（--dry-run 只出报告，--coverage 统计覆盖率）
  matrix                  接口 × 返回码矩阵：与 spec 对照，列出未调用/未触发/单一返回码/规格外
  coverage                带 JaCoCo 跑一次 local，输出分层覆盖率
  local                   本地对无 bug 程序跑 udpunittest.py
  check                   提交前规范检查
  run [--no-prune]        check + local 通过后调试（type=2），记录判题结果与命中，删除未命中/被替代的用例
  submit --yes            check + local 通过后正式提交（type=1），同样记录命中并精简
  hits                    命中记录与场景库摘要
  prune                   对最近一轮判题执行精简（run --no-prune 之后用）
  stop                    停止本工作目录的被测程序（按端口查进程，不按进程名）
  status                  查看当前进度
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import shutil
import subprocess
import sys
from datetime import datetime
from pathlib import Path

SCRIPTS = Path(__file__).resolve().parent
sys.path.insert(0, str(SCRIPTS))

import check_submission  # noqa: E402
import jacoco_cov as coverage_mod  # noqa: E402
import decompile as decompile_mod  # noqa: E402
import dut  # noqa: E402
import hits as hits_mod  # noqa: E402
import local_run  # noqa: E402
import matrix as matrix_mod  # noqa: E402
import probe as probe_mod  # noqa: E402
import record as record_mod  # noqa: E402
import spec as spec_mod  # noqa: E402

SOLFTEST = SCRIPTS / "solftest_auto.py"
COOKIE_SCRIPT = SCRIPTS / "get_ilearning_cookies.py"
PRISTINE_FILES = ("udpunittest.py", "testaw.py", "testtool.py", "timeout.py", "config.properties")

ANALYSIS_SKELETON = """# 题目分析

## 系统概述

## 接口

## 业务规则

## 数值与边界

## 时间规则

## 资源与容量

## 未确认
"""

MODEL_SKELETON = """# 数值模型

## 硬性约束（探针确认）

## 接口返回格式（探针确认）

## 计算公式与常量（每条注明来源探针记录）

## 时间与资源排布要点
"""
ANALYSIS_SOURCES = ("题目描述.md", "题目补充信息.md")

REVIEW_SKELETON = """# 复盘

## 轮次记录
"""


class Work:
    def __init__(self, root: Path):
        self.root = root.resolve()
        self.answer = self.root / "answer"
        self.notes = self.root / "notes"
        self.pristine = self.notes / "pristine"
        self.decompiled = self.root / "decompiled"
        self.cookies = self.root / "cookies.json"
        self.meta_path = self.notes / "ctb.json"

    def meta(self) -> dict:
        if self.meta_path.is_file():
            return json.loads(self.meta_path.read_text(encoding="utf-8-sig"))
        return {}

    def save_meta(self, data: dict) -> None:
        self.notes.mkdir(parents=True, exist_ok=True)
        self.meta_path.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")

    def subject(self, override: str | None = None) -> str:
        subject = override or self.meta().get("subject")
        if not subject:
            raise SystemExit("还没有题目，请先执行：ctb.py pull --subject <题目ID或URL>")
        return subject


def _solftest(work: Work, command: str, subject: str, *extra: str) -> int:
    if not work.cookies.is_file():
        print("没有 cookies.json，先获取 Cookie ...")
        if cmd_cookies(work, argparse.Namespace(keep_open=False)) != 0:
            return 1
    args = [sys.executable, str(SOLFTEST), command]
    if command in ("pull", "save", "run", "submit"):
        args.append(str(work.answer))
    args += ["--subject", subject, "--cookies", str(work.cookies), *extra]
    return subprocess.run(args, cwd=str(work.root)).returncode


def cmd_cookies(work: Work, args) -> int:
    cmd = [sys.executable, str(COOKIE_SCRIPT), "--out", str(work.cookies)]
    if getattr(args, "keep_open", False):
        cmd.append("--keep-open")
    return subprocess.run(cmd, cwd=str(work.root)).returncode


def cmd_pull(work: Work, args) -> int:
    if work.answer.is_dir() and any(work.answer.iterdir()) and not args.force:
        raise SystemExit(f"{work.answer} 已存在且非空。每道题请用独立的工作目录；确需覆盖请加 --force")
    code = _solftest(work, "pull", args.subject, "--framework", args.framework)
    if code != 0:
        return code
    work.pristine.mkdir(parents=True, exist_ok=True)
    for name in PRISTINE_FILES:
        src = work.answer / name
        if src.is_file():
            shutil.copy2(src, work.pristine / name)
    meta = work.meta()
    meta.update(subject=args.subject, framework=args.framework, pulled_at=f"{datetime.now():%Y-%m-%d %H:%M:%S}")
    work.save_meta(meta)
    for name, text in (("题目分析.md", ANALYSIS_SKELETON), ("数值模型.md", MODEL_SKELETON),
                       ("复盘.md", REVIEW_SKELETON)):
        p = work.notes / name
        if not p.is_file():
            p.write_text(text, encoding="utf-8")
    (work.notes / "probes").mkdir(exist_ok=True)
    jars = sorted(work.answer.glob("*.jar"))
    print(f"\n已保存原件到 {work.pristine}；答题目录 jar：{[j.name for j in jars] or '无'}")
    if len(jars) != 1:
        print("注意：本地运行要求答题目录根下恰好一个 jar（无 bug 版本）")
    if (work.answer / "config.properties").is_file():
        print(f"本地运行端口 udpPort={dut.prepare(work.answer, quiet=True)}")
    return 0


def _analysis_digest(work: Work) -> str:
    h = hashlib.sha256()
    for name in ANALYSIS_SOURCES:
        p = work.answer / name
        if p.is_file():
            h.update(name.encode("utf-8"))
            h.update(p.read_bytes().replace(b"\r\n", b"\n"))
    for jar in sorted(work.answer.glob("*.jar")):
        h.update(jar.read_bytes())
    return h.hexdigest()[:16]


def _empty_sections(path: Path) -> list[str]:
    sections: dict[str, int] = {}
    current = None
    for line in path.read_text(encoding="utf-8").splitlines():
        if line.startswith("## "):
            current = line[3:].strip()
            sections[current] = 0
        elif current and line.strip():
            sections[current] += 1
    return [k for k, v in sections.items() if not v]


def _analysis_state(work: Work) -> str:
    p = work.notes / "题目分析.md"
    if not p.is_file():
        return "无"
    meta = work.meta()
    if not meta.get("analysis_digest"):
        return "未固化"
    if meta["analysis_digest"] != _analysis_digest(work):
        return f"已固化（{meta.get('analysis_at')}），但题目描述或 jar 已变化，需复核"
    return f"已固化（{meta.get('analysis_at')}）"


def cmd_analysis(work: Work, args) -> int:
    p = work.notes / "题目分析.md"
    if not p.is_file():
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(ANALYSIS_SKELETON, encoding="utf-8")
        print(f"已创建 {p}")
    empty = _empty_sections(p)
    if args.done:
        if len(empty) == len(_section_names(p)):
            raise SystemExit("题目分析还是空的，先写入分析结果")
        meta = work.meta()
        meta.update(analysis_digest=_analysis_digest(work), analysis_at=f"{datetime.now():%Y-%m-%d %H:%M:%S}")
        work.save_meta(meta)
    print(f"题目分析：{_analysis_state(work)}  {p}")
    if empty:
        print(f"空章节：{', '.join(empty)}")
    return 0


def _section_names(path: Path) -> list[str]:
    return [line[3:].strip() for line in path.read_text(encoding="utf-8").splitlines() if line.startswith("## ")]


def cmd_info(work: Work, args) -> int:
    return _solftest(work, "info", work.subject())


def cmd_decompile(work: Work, args) -> int:
    index = decompile_mod.decompile(work.answer, work.decompiled, args.jar)
    print(f"已反编译到 {work.decompiled / 'src'}\n索引：{index}")
    return 0


def cmd_probe(work: Work, args) -> int:
    commands = probe_mod.read_commands(args.file, args.cmd)
    return probe_mod.run_probe(work.answer, commands, work.notes / "probes", args.verbose)


def _single_jar(work: Work) -> Path:
    jars = sorted(work.answer.glob("*.jar"))
    if len(jars) != 1:
        raise SystemExit(f"答题目录根下必须恰好有一个 jar，当前 {[j.name for j in jars]}")
    return jars[0]


def _steps_text(work: Work, args) -> tuple[str, str]:
    if bool(args.file) == bool(args.gen):
        raise SystemExit("record 需要且只能指定 -f 步骤文件 或 --gen 生成脚本 之一")
    src = Path(args.file or args.gen)
    if not src.is_file():
        raise SystemExit(f"找不到 {src}")
    name = args.name or src.stem.split(".")[0]
    if args.file:
        return src.read_text(encoding="utf-8-sig"), name
    proc = subprocess.run(
        [sys.executable, str(src)], cwd=str(work.root), capture_output=True, text=True,
        encoding="utf-8", errors="replace", env={**os.environ, "PYTHONIOENCODING": "utf-8"},
    )
    if proc.returncode != 0:
        raise SystemExit(f"生成脚本执行失败（退出码 {proc.returncode}）：\n{proc.stderr}")
    steps_dir = work.notes / "steps"
    steps_dir.mkdir(parents=True, exist_ok=True)
    out = steps_dir / f"{name}.steps.txt"
    out.write_text(proc.stdout, encoding="utf-8")
    print(f"生成脚本输出已保存：{out}")
    return proc.stdout, name


def _coverage_ctx(work: Work) -> "coverage_mod.Context":
    spec = spec_mod.load(work.notes / "spec.json")
    commands = spec_mod.commands_of(spec)
    if not commands:
        commands = sorted({matrix_mod.command_of(c) for c, _, _ in matrix_mod.observations(work.notes)})
    return coverage_mod.Context(commands, work.decompiled / "src", work.notes / "coverage" / "scope.json")


def cmd_record(work: Work, args) -> int:
    text, name = _steps_text(work, args)
    jar = _single_jar(work)
    session = coverage_mod.TcpSession(work.notes / "coverage" / f"record_{name}") if args.coverage else None
    record_mod.record(work.answer, text, name, work.notes / "record", args.dry_run,
                      session.env() if session else None, session, work.notes)
    print("\n== 接口 × 返回码矩阵 ==")
    matrix_mod.build(work.notes)
    if session:
        print("\n== 覆盖率 ==")
        coverage_mod.report(None, jar, work.notes / "coverage", f"record:{name}", _coverage_ctx(work), session)
    if args.dry_run:
        return 0
    print("\n== 规范检查 ==")
    if cmd_check(work, args) != 0:
        return 1
    print("\n== 本地验证 ==")
    return cmd_local(work, args)


def cmd_coverage(work: Work, args) -> int:
    jar = _single_jar(work)
    exec_path = work.notes / "coverage" / f"local_{datetime.now():%Y%m%d_%H%M%S}.exec"
    rep = local_run.run_local(work.answer, work.notes, args.timeout, coverage_mod.agent_env(exec_path))
    print(rep["report"])
    print("\n== 覆盖率 ==")
    coverage_mod.report(exec_path, jar, work.notes / "coverage", "local", _coverage_ctx(work))
    return 0 if rep["ok"] else 1


def cmd_spec(work: Work, args) -> int:
    return spec_mod.build(work.answer, work.decompiled / "src", work.notes / "spec.json", args.force)


def cmd_matrix(work: Work, args) -> int:
    matrix_mod.build(work.notes)
    return 0


def cmd_local(work: Work, args) -> int:
    rep = local_run.run_local(work.answer, work.notes, args.timeout)
    print(rep["report"])
    return 0 if rep["ok"] else 1


def cmd_check(work: Work, args) -> int:
    errors, warnings, info = check_submission.check(work.answer, work.pristine)
    print(check_submission.format_result(errors, warnings, info))
    return 1 if errors else 0


def _gate(work: Work, args) -> bool:
    print("== 规范检查 ==")
    if cmd_check(work, args) != 0:
        print("规范检查未通过，已停止")
        return False
    print("\n== 本地验证（无 bug 版本必须全部通过）==")
    if cmd_local(work, args) != 0:
        print("本地验证未全部通过，已停止")
        return False
    return True


def _collect_judge(work: Work, kind: str) -> dict | None:
    jdir = work.notes / "judge"
    jdir.mkdir(parents=True, exist_ok=True)
    moved = []
    for p in sorted(work.answer.glob("judge_result_*.json")):
        dst = jdir / p.name
        shutil.move(str(p), str(dst))
        moved.append(dst)
    if not moved:
        return None
    data = json.loads(moved[-1].read_text(encoding="utf-8-sig"))
    bug = data.get("bugDetect") or {}
    missed = [str(i.get("bugId")) for i in bug.get("items") or [] if not i.get("detected")]
    deductions = [f"{c.get('checkItem')}={c.get('number')}" for c in data.get("codeQualityDeductions") or []]
    line = (f"- {datetime.now():%Y-%m-%d %H:%M:%S} {kind} judgeId={data.get('judgeId')} "
            f"score={data.get('score')} result={data.get('judgeResult')} "
            f"检出={bug.get('passed', '?')}/{bug.get('total', '?')} "
            f"未检出={','.join(missed) or '无'} 扣分项={';'.join(deductions) or '无'}")
    with (jdir / "history.md").open("a", encoding="utf-8") as f:
        f.write(line + "\n")
    print(f"\n判题结果已移到 {jdir}，摘要追加到 history.md：\n{line}")
    return data


def _record_hits(work: Work, judge: dict | None, kind: str, prune: bool) -> None:
    if judge is None:
        return
    print("\n== 命中记录 ==")
    print(hits_mod.apply(work.answer, work.notes, judge, kind, prune))


def cmd_run(work: Work, args) -> int:
    subject = work.subject()
    if not _gate(work, args):
        return 1
    code = _solftest(work, "run", subject)
    _record_hits(work, _collect_judge(work, "run"), "run", not args.no_prune)
    return code


def cmd_submit(work: Work, args) -> int:
    if not args.yes:
        raise SystemExit("正式提交会计入成绩，确认后加 --yes 再执行")
    subject = work.subject()
    if not _gate(work, args):
        return 1
    code = _solftest(work, "submit", subject)
    _record_hits(work, _collect_judge(work, "submit"), "submit", not args.no_prune)
    return code


def cmd_hits(work: Work, args) -> int:
    print(hits_mod.summary(work.notes))
    data = hits_mod.load(work.notes)
    if data["rounds"]:
        r = data["rounds"][-1]
        for name, t in sorted(r["tests"].items()):
            sec = t.get("seconds")
            print(f"  {name}: bug {', '.join(t['bugs']) or '-'}" + (f"  {sec:.1f}s" if sec is not None else ""))
        if r.get("lost"):
            print(f"以前命中过、最近一轮未命中：{', '.join(r['lost'])}")
        if r.get("remove") and not r.get("pruned"):
            print(f"待删除（ctb.py prune）：{', '.join(r['remove'])}")
    lib = work.notes / "场景库.md"
    print(f"场景库：{lib if lib.is_file() else '无'}")
    return 0


def cmd_prune(work: Work, args) -> int:
    print(hits_mod.prune_last(work.answer, work.notes))
    return 0


def cmd_stop(work: Work, args) -> int:
    print(dut.stop(work.answer))
    return 0


def cmd_status(work: Work, args) -> int:
    meta = work.meta()
    print(f"工作目录：{work.root}")
    print(f"题目：{meta.get('subject') or '未拉取'}  拉取时间：{meta.get('pulled_at') or '-'}")
    print(f"Cookie：{'有' if work.cookies.is_file() else '无'}")
    print(f"答题目录 jar：{[j.name for j in sorted(work.answer.glob('*.jar'))] if work.answer.is_dir() else '无'}")
    port = dut.read_port(work.answer) if work.answer.is_dir() else None
    if port:
        print(f"本地运行端口：udpPort={port} {'空闲' if dut.is_free(port) else '被占用（ctb.py stop 可停止残留的被测程序）'}")
    print(f"反编译索引：{'有' if (work.decompiled / 'index.md').is_file() else '无'}")
    print(f"题目分析：{_analysis_state(work)}")
    spec = spec_mod.load(work.notes / "spec.json")
    if spec:
        print(f"接口规格：{len(spec_mod.commands_of(spec))} 个命令，{'已审阅' if spec.get('reviewed') else '未审阅（reviewed=false）'}")
    else:
        print("接口规格：无（执行 spec）")
    mx = work.notes / "matrix.md"
    if mx.is_file():
        head = [l for l in mx.read_text(encoding="utf-8").splitlines() if l.startswith("缺口")]
        print("矩阵：" + (head[0] if head else "-"))
    probes = sorted((work.notes / "probes").glob("*.txt")) if (work.notes / "probes").is_dir() else []
    print(f"探针记录：{len(probes)} 份")
    last = work.notes / "local_last.txt"
    if last.is_file():
        print("最近一次本地验证：" + last.read_text(encoding="utf-8").splitlines()[0])
    cov = work.notes / "coverage" / "history.md"
    if cov.is_file():
        lines = [l for l in cov.read_text(encoding="utf-8").splitlines() if l.strip()]
        print("最近一次覆盖率：" + (lines[-1] if lines else "-"))
    hist = work.notes / "judge" / "history.md"
    if hist.is_file():
        lines = [l for l in hist.read_text(encoding="utf-8").splitlines() if l.strip()]
        print("最近一次判题：" + (lines[-1] if lines else "-"))
    print("命中：" + hits_mod.summary(work.notes))
    return 0


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--work", default=".", help="题目工作目录，默认当前目录")
    sub = ap.add_subparsers(dest="command", required=True)

    p = sub.add_parser("cookies")
    p.add_argument("--keep-open", action="store_true")
    p = sub.add_parser("pull")
    p.add_argument("--subject", required=True)
    p.add_argument("--framework", default="python", choices=["python", "java", "c"])
    p.add_argument("--force", action="store_true")
    sub.add_parser("info")
    p = sub.add_parser("decompile")
    p.add_argument("--jar", default=None)
    p = sub.add_parser("probe")
    p.add_argument("-f", "--file", action="append", default=[])
    p.add_argument("-c", "--cmd", action="append", default=[])
    p.add_argument("-v", "--verbose", action="store_true")
    for name in ("local", "check", "run", "submit", "coverage", "record"):
        p = sub.add_parser(name)
        p.add_argument("--timeout", type=int, default=1800, help="本地验证整体超时秒数")
        if name == "submit":
            p.add_argument("--yes", action="store_true")
        if name in ("run", "submit"):
            p.add_argument("--no-prune", action="store_true", help="只记录命中，不删除用例")
        if name == "record":
            p.add_argument("-f", "--file", default=None, help="步骤文件")
            p.add_argument("--gen", default=None, help="生成脚本，其 stdout 即步骤")
            p.add_argument("--name", default=None, help="录制名称，默认取文件名；同名再录整块替换")
            p.add_argument("--dry-run", action="store_true", help="只录制并出报告，不写答题文件")
            p.add_argument("--coverage", action="store_true", help="录制时统计覆盖率")
    p = sub.add_parser("spec")
    p.add_argument("--force", action="store_true", help="忽略已有 spec.json 重新生成")
    sub.add_parser("matrix")
    p = sub.add_parser("analysis")
    p.add_argument("--done", action="store_true", help="分析已写完，记录对应的题目与 jar 指纹")
    sub.add_parser("hits")
    sub.add_parser("prune")
    sub.add_parser("stop")
    sub.add_parser("status")

    args = ap.parse_args(argv)
    work = Work(Path(args.work))
    handler = {
        "cookies": cmd_cookies, "pull": cmd_pull, "info": cmd_info, "decompile": cmd_decompile,
        "analysis": cmd_analysis, "spec": cmd_spec, "probe": cmd_probe, "record": cmd_record,
        "matrix": cmd_matrix, "coverage": cmd_coverage, "local": cmd_local, "check": cmd_check,
        "run": cmd_run, "submit": cmd_submit, "hits": cmd_hits, "prune": cmd_prune, "stop": cmd_stop,
        "status": cmd_status,
    }[args.command]
    return handler(work, args)


if __name__ == "__main__":
    sys.exit(main())
