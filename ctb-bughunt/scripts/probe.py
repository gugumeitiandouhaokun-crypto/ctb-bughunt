# -*- coding: utf-8 -*-
"""探针：在答题目录启动无 bug 被测程序，逐条发送命令并打印真实返回。

不在答题目录生成任何文件（testtool 自身打开的 py_program.log / time.log 除外）。

命令来源（可混用，按顺序执行）：
  -f FILE   每行一条命令；空行跳过；# 开头为注释；--- 表示发送 init() 复位
  -c CMD    直接给出一条命令，可重复（复位直接写 init()）

每条命令输出 [耗时] 命令 => 返回，其下以 "| " 缩进列出被测程序自身打印的日志（如业务异常提示）。

用法：
  python probe.py --answer answer -f notes/probes/plan_a.txt
  python probe.py --answer answer -c "init()" -c "cmd_a(1,2)"
"""
from __future__ import annotations

import argparse
import logging
import os
import re
import sys
import time
from datetime import datetime
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import dut  # noqa: E402

RESET = "---"
JAR_LOG_RE = re.compile(r"^\d{4}-\d\d-\d\d [\d:.]+ \[[^\]]*\]\s+(\w+)\s+([\w.$]+)\s+-\s+(.*)$")
SETTLE_SEC = 0.02


class _JarLog(logging.Handler):
    """收集被测程序自身输出（框架以 DEBUG 转发），去掉收发回显与命令回显。"""

    def __init__(self):
        super().__init__(logging.DEBUG)
        self.lines: list[str] = []

    def emit(self, record: logging.LogRecord) -> None:
        for raw in record.getMessage().splitlines():
            raw = raw.strip()
            if not raw or raw.startswith(("Send:", "Receive:")) or "[CASE][CMD]" in raw:
                continue
            m = JAR_LOG_RE.match(raw)
            self.lines.append(f"{m.group(1)} {m.group(2).rsplit('.', 1)[-1]} - {m.group(3)}" if m else raw)

    def take(self) -> list[str]:
        out, self.lines = self.lines, []
        return out


def read_commands(files: list[str], cmds: list[str]) -> list[str]:
    out: list[str] = []
    for f in files or []:
        for line in Path(f).read_text(encoding="utf-8-sig").splitlines():
            line = line.strip()
            if line and not line.startswith("#"):
                out.append(line)
    out.extend(c.strip() for c in cmds or [] if c.strip())
    return out


def run_probe(answer_dir: Path, commands: list[str], log_dir: Path | None = None, verbose: bool = False) -> int:
    answer_dir = answer_dir.resolve()
    if not commands:
        print("没有要发送的命令")
        return 1
    jars = sorted(answer_dir.glob("*.jar"))
    if len(jars) != 1:
        print(f"答题目录根下必须恰好有一个 jar，当前 {[j.name for j in jars]}")
        return 1
    log_path = None
    if log_dir is not None:
        log_dir = log_dir.resolve()
        log_dir.mkdir(parents=True, exist_ok=True)
        log_path = log_dir / f"{datetime.now():%Y%m%d_%H%M%S}.txt"

    dut.prepare(answer_dir)
    cwd = os.getcwd()
    os.chdir(answer_dir)
    sys.path.insert(0, str(answer_dir))
    lines: list[str] = []
    costs: list[float] = []
    try:
        from testtool import tool  # noqa: E402  框架自带

        if not verbose:
            for h in logging.getLogger().handlers:
                h.setLevel(logging.WARNING)
        jar_log = _JarLog()
        logging.getLogger("case").addHandler(jar_log)
        tool.start_dut()
        jar_log.take()
        try:
            for cmd in commands:
                real = "init()" if cmd == RESET else cmd
                t0 = time.time()
                ret = tool.send_cmd(real)
                ms = (time.time() - t0) * 1000
                costs.append(ms)
                time.sleep(SETTLE_SEC)
                block = [f"[{ms:7.1f}ms] {real} => {ret}"] + [f"    | {x}" for x in jar_log.take()]
                print("\n".join(block), flush=True)
                lines.extend(block)
        finally:
            try:
                tool.stop_dut()
            except Exception as exc:  # noqa: BLE001
                print(f"(stop_dut 异常，可忽略：{exc})")
    finally:
        os.chdir(cwd)
        dut.ensure_stopped(answer_dir)

    if costs:
        summary = f"# {len(costs)} 条命令，平均 {sum(costs) / len(costs):.1f}ms，最长 {max(costs):.1f}ms"
        print(summary)
        lines.append(summary)
    if log_path is not None:
        log_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
        print(f"已保存 {log_path}")
    return 0


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--answer", default="answer", help="答题目录（含 jar 与 testtool.py）")
    ap.add_argument("--log-dir", default=None, help="探针记录保存目录")
    ap.add_argument("-f", "--file", action="append", default=[], help="命令文件，可重复")
    ap.add_argument("-c", "--cmd", action="append", default=[], help="单条命令，可重复")
    ap.add_argument("-v", "--verbose", action="store_true", help="显示框架全部调试日志")
    args = ap.parse_args(argv)
    commands = read_commands(args.file, args.cmd)
    return run_probe(Path(args.answer), commands, Path(args.log_dir) if args.log_dir else None, args.verbose)


if __name__ == "__main__":
    sys.exit(main())
