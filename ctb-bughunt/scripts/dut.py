# -*- coding: utf-8 -*-
"""被测程序（本地无 bug jar）的端口与进程管理。

- 启动前把 config.properties 的 udpPort 改到本工作目录专用的端口：
  取 20000-29999（低于 Windows 49152+、Linux 32768+ 的系统临时端口范围，不会被系统分配给其他连接），
  起点由答题目录路径决定，逐个检查 UDP/TCP 均可绑定且无进程占用。
- 专用端口上残留的 java 进程视为本工作目录上次未退出的被测程序，按端口查 PID 结束；
  其他程序占用时换端口，不结束任何进程。
- 停止时先发 EXIT()，端口仍被 java 占用再按端口结束进程；从不按进程名结束。

用法：
  python dut.py --answer answer prepare | stop | show
"""
from __future__ import annotations

import argparse
import os
import re
import signal
import socket
import subprocess
import sys
import time
import zlib
from pathlib import Path

PORT_LO, PORT_HI = 20000, 30000
PORT_KEY_RE = re.compile(r"^(\s*udpPort\s*=\s*)(\S*)(.*)$", re.M)


def read_port(answer_dir: Path) -> int | None:
    cfg = answer_dir / "config.properties"
    if not cfg.is_file():
        return None
    for line in cfg.read_text(encoding="utf-8", errors="replace").splitlines():
        line = line.strip()
        if not line.startswith("#") and "=" in line:
            k, v = line.split("=", 1)
            if k.strip() == "udpPort":
                try:
                    return int(v.strip())
                except ValueError:
                    return None
    return None


def write_port(answer_dir: Path, port: int) -> None:
    cfg = answer_dir / "config.properties"
    raw = cfg.read_bytes().decode("utf-8", errors="replace")
    new, n = PORT_KEY_RE.subn(lambda m: f"{m.group(1)}{port}{m.group(3)}", raw, count=1)
    if not n:
        raise SystemExit(f"{cfg} 中没有 udpPort 配置")
    cfg.write_bytes(new.encode("utf-8"))


def _run(args: list[str]) -> str:
    try:
        proc = subprocess.run(args, capture_output=True, timeout=15)
    except (OSError, subprocess.TimeoutExpired):
        return ""
    for enc in ("utf-8", "gbk", "latin-1"):
        try:
            return proc.stdout.decode(enc)
        except UnicodeDecodeError:
            continue
    return ""


def _proc_name(pid: int) -> str:
    if os.name == "nt":
        out = _run(["tasklist", "/FI", f"PID eq {pid}", "/FO", "CSV", "/NH"])
        m = re.match(r'\s*"([^"]+)"', out)
        return m.group(1) if m else ""
    comm = Path(f"/proc/{pid}/comm")
    if comm.is_file():
        return comm.read_text(errors="replace").strip()
    return _run(["ps", "-p", str(pid), "-o", "comm="]).strip()


def owners(port: int) -> list[tuple[int, str]]:
    """占用该端口（UDP 或 TCP 监听）的进程 [(pid, 进程名)]。"""
    pids: set[int] = set()
    if os.name == "nt":
        for line in _run(["netstat", "-ano"]).splitlines():
            cols = line.split()
            if len(cols) < 4 or cols[0].upper() not in ("UDP", "TCP"):
                continue
            if cols[1].rsplit(":", 1)[-1] != str(port):
                continue
            if cols[0].upper() == "TCP" and "LISTEN" not in line.upper():
                continue
            if cols[-1].isdigit() and cols[-1] != "0":
                pids.add(int(cols[-1]))
    else:
        out = _run(["ss", "-H", "-uanp"]) + _run(["ss", "-H", "-tlnp"])
        if out:
            for line in out.splitlines():
                cols = line.split()
                if len(cols) >= 5 and cols[3].rsplit(":", 1)[-1] == str(port):
                    pids.update(int(p) for p in re.findall(r"pid=(\d+)", line))
        else:
            for line in _run(["lsof", "-nP", f"-iUDP:{port}", f"-iTCP:{port}", "-sTCP:LISTEN", "-Fp"]).splitlines():
                if line.startswith("p") and line[1:].isdigit():
                    pids.add(int(line[1:]))
    pids.discard(os.getpid())
    return [(pid, _proc_name(pid)) for pid in sorted(pids)]


def _bindable(port: int) -> bool:
    for kind in (socket.SOCK_DGRAM, socket.SOCK_STREAM):
        for host in ("127.0.0.1", "0.0.0.0"):
            s = socket.socket(socket.AF_INET, kind)
            try:
                s.bind((host, port))
            except OSError:
                return False
            finally:
                s.close()
    return True


def is_free(port: int) -> bool:
    return _bindable(port) and not owners(port)


def _is_java(name: str) -> bool:
    return name.lower().startswith("java")


def _kill(pid: int) -> None:
    if os.name == "nt":
        _run(["taskkill", "/PID", str(pid), "/T", "/F"])
        return
    try:
        os.kill(pid, signal.SIGTERM)
    except OSError:
        return
    for _ in range(20):
        time.sleep(0.1)
        try:
            os.kill(pid, 0)
        except OSError:
            return
    try:
        os.kill(pid, signal.SIGKILL)
    except OSError:
        pass


def kill_port(port: int) -> tuple[list[tuple[int, str]], list[tuple[int, str]]]:
    """结束占用端口的 java 进程。返回 (已结束, 非 java 占用者)。"""
    killed, foreign = [], []
    for pid, name in owners(port):
        if _is_java(name):
            _kill(pid)
            killed.append((pid, name))
        else:
            foreign.append((pid, name))
    return killed, foreign


def _wait_free(port: int, seconds: float) -> bool:
    end = time.time() + seconds
    while True:
        if is_free(port):
            return True
        if time.time() >= end:
            return False
        time.sleep(0.3)


def pick_port(answer_dir: Path, exclude: set[int] | None = None) -> int:
    span = PORT_HI - PORT_LO
    start = zlib.crc32(str(answer_dir.resolve()).lower().encode("utf-8")) % span
    for i in range(span):
        port = PORT_LO + (start + i) % span
        if exclude and port in exclude:
            continue
        if is_free(port):
            return port
    raise SystemExit(f"{PORT_LO}-{PORT_HI - 1} 内找不到可用端口")


def prepare(answer_dir: Path, quiet: bool = False) -> int:
    """启动被测程序前调用：保证 udpPort 是本工作目录专用且空闲的端口，返回端口号。"""
    answer_dir = answer_dir.resolve()
    port = read_port(answer_dir)
    if port is None:
        raise SystemExit(f"{answer_dir / 'config.properties'} 缺少有效的 udpPort")
    note = ""
    if not PORT_LO <= port < PORT_HI:
        new = pick_port(answer_dir)
        note = f"udpPort {port} -> {new}（改到本工作目录专用端口）"
        port = new
    elif not is_free(port):
        killed, foreign = kill_port(port)
        if killed:
            note = f"结束端口 {port} 上残留的被测程序 PID {', '.join(str(p) for p, _ in killed)}"
        if foreign or not _wait_free(port, 5):
            new = pick_port(answer_dir, {port})
            who = ", ".join(f"{n or '?'}({p})" for p, n in foreign) or "未知进程"
            note = (note + "；" if note else "") + f"端口 {port} 被 {who} 占用，udpPort -> {new}"
            port = new
    if read_port(answer_dir) != port:
        write_port(answer_dir, port)
    if note and not quiet:
        print(f"[被测程序] {note}", flush=True)
    return port


def stop(answer_dir: Path, wait: float = 3.0) -> str:
    """发 EXIT() 让被测程序退出；端口仍被 java 占用时按端口结束进程。"""
    port = read_port(answer_dir.resolve())
    if not port:
        return "没有 udpPort 配置"
    if is_free(port):
        return f"端口 {port} 上没有被测程序在运行"
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.sendto(b"EXIT()", ("127.0.0.1", port))
    except OSError:
        pass
    finally:
        s.close()
    if _wait_free(port, wait):
        return f"端口 {port} 上的被测程序已退出"
    killed, foreign = kill_port(port)
    msg = []
    if killed:
        msg.append(f"已按端口 {port} 结束 java 进程 PID {', '.join(str(p) for p, _ in killed)}")
    if foreign:
        msg.append(f"端口 {port} 被非 java 进程占用：{', '.join(f'{n}({p})' for p, n in foreign)}，未处理")
    if killed and not _wait_free(port, 3):
        msg.append(f"端口 {port} 仍未释放")
    return "；".join(msg) or f"端口 {port} 仍被占用，但查不到占用进程"


def ensure_stopped(answer_dir: Path) -> None:
    """一次运行结束后调用：被测程序没有自行退出时按端口清理。"""
    port = read_port(answer_dir.resolve())
    if port and not _wait_free(port, 3):
        killed, _ = kill_port(port)
        if killed:
            print(f"[被测程序] 运行结束后仍占用端口 {port}，已结束 PID {', '.join(str(p) for p, _ in killed)}",
                  flush=True)


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--answer", default="answer")
    ap.add_argument("action", choices=["prepare", "stop", "show"])
    args = ap.parse_args(argv)
    answer = Path(args.answer)
    if args.action == "prepare":
        print(f"udpPort={prepare(answer)}")
    elif args.action == "stop":
        print(stop(answer))
    else:
        port = read_port(answer)
        state = "空闲" if port and is_free(port) else f"占用 {owners(port) if port else '-'}"
        print(f"udpPort={port} {state}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
