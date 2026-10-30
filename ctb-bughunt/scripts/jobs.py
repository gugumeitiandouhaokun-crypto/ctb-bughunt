# -*- coding: utf-8 -*-
"""后台任务：耗时长的子命令在后台进程中执行，调用方立即返回，再用 wait 分段查看进度。

- 同一工作目录同时只允许一个后台任务（也防止重复提交判题）。
- 任务输出写入 notes/jobs/<任务号>.log，退出码写入 <任务号>.exit。
- 当前任务记录在 notes/jobs/current.json，结束后由 wait 移到 last.json。
"""
from __future__ import annotations

import json
import os
import subprocess
import sys
import time
from datetime import datetime
from pathlib import Path

ENV_JOB = "CTB_JOB_ID"
WAIT_SEC = 50
POLL_SEC = 2
MAX_LINES = 120


def _dir(notes: Path) -> Path:
    d = notes / "jobs"
    d.mkdir(parents=True, exist_ok=True)
    return d


def _load(path: Path) -> dict | None:
    if path.is_file():
        try:
            return json.loads(path.read_text(encoding="utf-8"))
        except (OSError, ValueError):
            return None
    return None


def _save(path: Path, data: dict) -> None:
    path.write_text(json.dumps(data, ensure_ascii=False, indent=1), encoding="utf-8")


def alive(pid: int) -> bool:
    if os.name == "nt":
        import ctypes
        handle = ctypes.windll.kernel32.OpenProcess(0x1000, False, pid)
        if not handle:
            return False
        code = ctypes.c_ulong()
        ok = ctypes.windll.kernel32.GetExitCodeProcess(handle, ctypes.byref(code))
        ctypes.windll.kernel32.CloseHandle(handle)
        return bool(ok) and code.value == 259
    try:
        os.kill(pid, 0)
    except OSError:
        return False
    return True


def current(notes: Path) -> dict | None:
    return _load(_dir(notes) / "current.json")


def _show(job: dict) -> str:
    return job.get("show") or " ".join(job["argv"])


def describe(job: dict) -> str:
    return f"任务 {job['id']}：ctb.py {_show(job)}（{job['started']} 启动）"


def start(root: Path, notes: Path, script: Path, argv: list[str], show: str = "") -> dict:
    jdir = _dir(notes)
    job = current(notes)
    if job and (alive(job["pid"]) or (jdir / f"{job['id']}.exit").is_file()):
        raise SystemExit(f"已有后台任务未结束：{describe(job)}\n"
                         f"不要重复执行，用 ctb.py wait 查看进度与结果（ctb.py cancel 可终止）")
    job_id = datetime.now().strftime("%Y%m%d_%H%M%S")
    log = jdir / f"{job_id}.log"
    env = {**os.environ, ENV_JOB: job_id, "PYTHONIOENCODING": "utf-8", "PYTHONUNBUFFERED": "1"}
    kwargs: dict = {}
    if os.name == "nt":
        kwargs["creationflags"] = 0x08000000 | 0x00000200  # CREATE_NO_WINDOW | CREATE_NEW_PROCESS_GROUP
    else:
        kwargs["start_new_session"] = True
    with log.open("wb") as out:
        proc = subprocess.Popen([sys.executable, "-u", str(script), *argv, "--fg"], cwd=str(root),
                                stdout=out, stderr=subprocess.STDOUT, stdin=subprocess.DEVNULL, env=env, **kwargs)
    job = {"id": job_id, "argv": argv, "show": show, "pid": proc.pid, "started": f"{datetime.now():%Y-%m-%d %H:%M:%S}",
           "t0": time.time(), "log": str(log), "offset": 0}
    _save(jdir / "current.json", job)
    return job


def finish_marker(notes: Path, job_id: str, code: int) -> None:
    (_dir(notes) / f"{job_id}.exit").write_text(str(code), encoding="utf-8")


def _new_output(job: dict, final: bool) -> tuple[list[str], int]:
    """日志中 offset 之后的新行；未结束时只取到最后一个完整行。"""
    log = Path(job["log"])
    if not log.is_file():
        return [], job["offset"]
    chunk = log.read_bytes()[job["offset"]:]
    end = len(chunk) if final else chunk.rfind(b"\n") + 1
    return chunk[:end].decode("utf-8", errors="replace").splitlines(), job["offset"] + end


def _trim(lines: list[str]) -> list[str]:
    if len(lines) <= MAX_LINES:
        return lines
    head, tail = 20, MAX_LINES - 20
    return lines[:head] + [f"...（省略 {len(lines) - head - tail} 行，完整输出见日志）"] + lines[-tail:]


def wait(notes: Path, seconds: int = WAIT_SEC) -> int:
    """等待当前任务最多 seconds 秒，打印期间新增的输出。返回：0 已结束且成功，1 已结束且失败，2 仍在进行，3 没有任务。"""
    jdir = _dir(notes)
    job = current(notes)
    if not job:
        last = _load(jdir / "last.json")
        print("没有进行中的后台任务" + (f"；上一个：{describe(last)}，退出码 {last.get('code')}" if last else ""))
        return 3
    exit_path = jdir / f"{job['id']}.exit"
    end = time.time() + max(1, seconds)
    while time.time() < end and not exit_path.is_file() and alive(job["pid"]):
        time.sleep(POLL_SEC)
    finished = exit_path.is_file() or not alive(job["pid"])
    lines, job["offset"] = _new_output(job, finished)
    elapsed = time.time() - job["t0"]
    if lines:
        print("\n".join(_trim(lines)))
    if finished:
        code = int(exit_path.read_text().strip() or 1) if exit_path.is_file() else None
        job["code"] = code
        job["ended"] = f"{datetime.now():%Y-%m-%d %H:%M:%S}"
        _save(jdir / "last.json", job)
        (jdir / "current.json").unlink(missing_ok=True)
        state = f"退出码 {code}" if code is not None else "进程已消失且没有退出码（被终止或崩溃）"
        print(f"\n== 后台任务结束：ctb.py {_show(job)}，{state}，用时 {elapsed:.0f}s，日志 {job['log']} ==")
        return 0 if code == 0 else 1
    _save(jdir / "current.json", job)
    print(f"\n== 后台任务进行中：ctb.py {_show(job)}，已用 {elapsed:.0f}s"
          f"{'' if lines else '，本次无新输出'}。继续执行 ctb.py wait，不要重新执行原命令 ==")
    return 2


def cancel(notes: Path) -> str:
    job = current(notes)
    if not job:
        return "没有进行中的后台任务"
    if alive(job["pid"]):
        if os.name == "nt":
            subprocess.run(["taskkill", "/PID", str(job["pid"]), "/T", "/F"], capture_output=True)
        else:
            import signal
            try:
                os.killpg(job["pid"], signal.SIGTERM)
            except OSError:
                pass
    finish_marker(notes, job["id"], 130)
    return f"已终止 {describe(job)}"
