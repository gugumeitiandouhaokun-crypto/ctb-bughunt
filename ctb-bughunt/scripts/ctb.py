# -*- coding: utf-8 -*-
"""ctb-bughunt 统一入口。在一道题的工作目录（默认当前目录）下执行。

工作目录约定：
  cookies.json   iLearning 登录 Cookie
  answer/        pull 下来的题目描述、框架、jar、testaw.py、udpunittest.py（提交的就是这里的文件）
  decompiled/    反编译源码 src/ 与 index.md
  notes/         ctb.json、pristine/（pull 原件）、数值模型.md、复盘.md、probes/、steps/、record/、
                 coverage/、judge/、local_last.txt

子命令：
  cookies                 获取/刷新登录 Cookie（弹浏览器免密登录）
  pull --subject <id|URL> 拉取题目到 answer/，保存原件与 subject
  info                    题目状态与提交记录
  decompile [--jar X]     反编译 answer/ 下的 jar 到 decompiled/
  probe -f F | -c CMD     探针：在无 bug 程序上发送命令看真实返回
  record -f F | --gen G   只写步骤，录制真实返回生成数据表断言用例（--dry-run 只出报告，--coverage 统计覆盖率）
  coverage                带 JaCoCo 跑一次 local，输出业务代码覆盖率
  local                   本地对无 bug 程序跑 udpunittest.py
  check                   提交前规范检查
  run                     check + local 通过后调试（type=2），记录判题结果
  submit --yes            check + local 通过后正式提交（type=1）
  status                  查看当前进度
"""
from __future__ import annotations

import argparse
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
import local_run  # noqa: E402
import probe as probe_mod  # noqa: E402
import record as record_mod  # noqa: E402

SOLFTEST = SCRIPTS / "solftest_auto.py"
COOKIE_SCRIPT = SCRIPTS / "get_ilearning_cookies.py"
PRISTINE_FILES = ("udpunittest.py", "testaw.py", "testtool.py", "timeout.py", "config.properties")

MODEL_SKELETON = """# 数值模型

## 硬性约束（探针确认）

## 接口返回格式

## 计算公式与常量（每条注明来源探针记录）

## 时间与资源排布要点

## 待验证的假设
"""

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
    for name, text in (("数值模型.md", MODEL_SKELETON), ("复盘.md", REVIEW_SKELETON)):
        p = work.notes / name
        if not p.is_file():
            p.write_text(text, encoding="utf-8")
    (work.notes / "probes").mkdir(exist_ok=True)
    jars = sorted(work.answer.glob("*.jar"))
    print(f"\n已保存原件到 {work.pristine}；答题目录 jar：{[j.name for j in jars] or '无'}")
    if len(jars) != 1:
        print("注意：本地运行要求答题目录根下恰好一个 jar（无 bug 版本）")
    return 0


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


def cmd_record(work: Work, args) -> int:
    text, name = _steps_text(work, args)
    jar = _single_jar(work)
    exec_path = work.notes / "coverage" / f"record_{name}.exec"
    env = coverage_mod.agent_env(exec_path) if args.coverage else None
    record_mod.record(work.answer, text, name, work.notes / "record", args.dry_run, env)
    if args.coverage:
        print("\n== 覆盖率 ==")
        coverage_mod.report(exec_path, jar, work.notes / "coverage", f"record:{name}")
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
    coverage_mod.report(exec_path, jar, work.notes / "coverage", "local")
    return 0 if rep["ok"] else 1


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


def _collect_judge(work: Work, kind: str) -> None:
    jdir = work.notes / "judge"
    jdir.mkdir(parents=True, exist_ok=True)
    moved = []
    for p in sorted(work.answer.glob("judge_result_*.json")):
        dst = jdir / p.name
        shutil.move(str(p), str(dst))
        moved.append(dst)
    if not moved:
        return
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


def cmd_run(work: Work, args) -> int:
    subject = work.subject()
    if not _gate(work, args):
        return 1
    code = _solftest(work, "run", subject)
    _collect_judge(work, "run")
    return code


def cmd_submit(work: Work, args) -> int:
    if not args.yes:
        raise SystemExit("正式提交会计入成绩，确认后加 --yes 再执行")
    subject = work.subject()
    if not _gate(work, args):
        return 1
    code = _solftest(work, "submit", subject)
    _collect_judge(work, "submit")
    return code


def cmd_status(work: Work, args) -> int:
    meta = work.meta()
    print(f"工作目录：{work.root}")
    print(f"题目：{meta.get('subject') or '未拉取'}  拉取时间：{meta.get('pulled_at') or '-'}")
    print(f"Cookie：{'有' if work.cookies.is_file() else '无'}")
    print(f"答题目录 jar：{[j.name for j in sorted(work.answer.glob('*.jar'))] if work.answer.is_dir() else '无'}")
    print(f"反编译索引：{'有' if (work.decompiled / 'index.md').is_file() else '无'}")
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
        if name == "record":
            p.add_argument("-f", "--file", default=None, help="步骤文件")
            p.add_argument("--gen", default=None, help="生成脚本，其 stdout 即步骤")
            p.add_argument("--name", default=None, help="录制名称，默认取文件名；同名再录整块替换")
            p.add_argument("--dry-run", action="store_true", help="只录制并出报告，不写答题文件")
            p.add_argument("--coverage", action="store_true", help="录制时统计覆盖率")
    sub.add_parser("status")

    args = ap.parse_args(argv)
    work = Work(Path(args.work))
    handler = {
        "cookies": cmd_cookies, "pull": cmd_pull, "info": cmd_info, "decompile": cmd_decompile,
        "probe": cmd_probe, "record": cmd_record, "coverage": cmd_coverage, "local": cmd_local, "check": cmd_check, "run": cmd_run,
        "submit": cmd_submit, "status": cmd_status,
    }[args.command]
    return handler(work, args)


if __name__ == "__main__":
    sys.exit(main())
