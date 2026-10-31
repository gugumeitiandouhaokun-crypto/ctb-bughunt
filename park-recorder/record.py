# -*- coding: utf-8 -*-
"""在有无 bug jar 的答题目录里按步骤录制真实返回，生成逐条精确断言的 udpunittest.py。

答题目录（--work）需要：平台下发的 testtool.py、timeout.py、config.properties、udpunittest.py，以及恰好一个 jar。

流程：
  1. 把本目录的 testaw.py 复制到答题目录（原文件不同则备份为 testaw.py.bak）；
  2. 启动 jar，按 scenes_a.py / scenes_b.py 中的步骤逐个测试录制：每个测试前发送 init()，
     每条命令的真实返回直接写成该行的 expects；整套录制执行 --runs 轮（默认 2），比较各轮返回是否一致；
  3. 把录制的测试方法写入答题目录的 udpunittest.py：同名测试整块替换，其他测试（原有用例）原样保留，
     原文件备份为 udpunittest.py.bak_<时间>；
  4. 停止 jar，执行一次 python udpunittest.py 验证全部用例通过（--no-verify 跳过）；
  5. 报告写到答题目录的 record_report.md：步骤数、耗时、与预期值不同的步骤、各轮不一致的步骤、录制异常。

用法：
  python record.py --work <答题目录> --version B
  python record.py --work <答题目录> --version B --only s1q s1r
  python record.py --version A --list
"""
from __future__ import annotations

import argparse
import importlib
import json
import logging
import math
import os
import re
import shutil
import socket
import subprocess
import sys
import time
import traceback
from datetime import datetime, timedelta
from pathlib import Path

HERE = Path(__file__).resolve().parent
rS, rF = 'E000:Success', 'E001:Fail'
NAMED = {rS: 'rS', rF: 'rF', 'E002:Locked': 'rL'}
GATES = ('G1', 'G2', 'G3', 'G4')
UNITS = {'day': 'days', 'hour': 'hours', 'min': 'minutes', 'sec': 'seconds'}
START = datetime(2026, 1, 1)
GATE_GAP = 5
MISSING = 'p0000000000000000'
HOLIDAYS = {'2026-01-01', '2026-01-02', '2026-01-03', '2026-02-15', '2026-02-16', '2026-02-17', '2026-02-18',
            '2026-02-19', '2026-02-20', '2026-02-21', '2026-02-22', '2026-02-23', '2026-04-04', '2026-04-05',
            '2026-04-06', '2026-05-01', '2026-05-02', '2026-05-03', '2026-05-04', '2026-05-05', '2026-06-19',
            '2026-06-20', '2026-06-21', '2026-09-25', '2026-09-26', '2026-09-27', '2026-10-01', '2026-10-02',
            '2026-10-03', '2026-10-04', '2026-10-05', '2026-10-06', '2026-10-07'}
MAKEUP_DAYS = {'2026-01-04', '2026-02-14', '2026-02-28', '2026-05-09', '2026-09-20', '2026-10-10'}
ID_RE = re.compile(r'p\d{16}|CID\d{8}')
TIME_RE = re.compile(r'^E000:(\d{4}-\d\d-\d\d \d\d:\d\d:\d\d)$')
DEFAULT_TIMEOUT = 300
TIMEOUT_FACTOR = 3


class NoReply(RuntimeError):
    pass


def q(v) -> str:
    if isinstance(v, (int, float)) and not isinstance(v, bool):
        return str(v)
    return "'" + str(v).replace('\\', '\\\\').replace("'", "\\'") + "'"


class Rec:
    """一个测试方法的录制：发送命令、记录真实返回并生成 aw 调用行。"""

    def __init__(self, name: str, send, version: str, ask=None):
        self.name = name
        self.version = version
        self._send = send
        self._ask = ask
        self.lines: list[str] = []
        self.trace: list[tuple[str, str]] = []
        self.diffs: list[tuple[int, str, str, str]] = []
        self.notes: list[str] = []
        self.now = START
        self.gate_at: dict[str, datetime] = {}
        self.occupied: list[str] = []
        self.space_of: dict[str, str] = {}

    # ---------------- 基础发送
    def call(self, cmd: str, *args, want=None) -> str:
        text = f"{cmd}({','.join(str(a) for a in args)})"
        ret = self._send(text).replace('\n', '')
        if ret == '':
            raise NoReply(f'{text} 没有返回（超时）')
        self.trace.append((text, ret))
        argstr = ','.join(q(a) for a in args)
        self.lines.append(f"        aw.cjl_{cmd}({argstr + ', ' if args else ''}expects={NAMED.get(ret, q(ret))})")
        if want is not None and ret != want:
            self.diffs.append((len(self.lines), text, want, ret))
        return ret

    def note(self, text: str) -> None:
        self.notes.append(text)

    # ---------------- 时间与出入口
    def time_pass(self, unit: str, value: int):
        self.now += timedelta(**{UNITS[unit]: value})
        ret = self.call('time_pass', unit, value, want=f'E000:{self.now:%Y-%m-%d %H:%M:%S}')
        m = TIME_RE.match(ret)
        if m:
            self.now = datetime.strptime(m.group(1), '%Y-%m-%d %H:%M:%S')
        return ret

    def goto(self, target: datetime):
        """推进到指定时刻，依次按天、小时、分、秒各推进一次"""
        left = int((target - self.now).total_seconds())
        if left < 0:
            raise ValueError(f'{self.name}: 目标时刻 {target} 早于当前 {self.now}')
        for unit, size in (('day', 86400), ('hour', 3600), ('min', 60), ('sec', 1)):
            n, left = divmod(left, size)
            if n:
                self.time_pass(unit, n)

    def gates(self, direction: str, at: datetime | None = None) -> list[str]:
        """按潮汐规则返回此刻可用于入场(I)或出场(O)的出入口（不考虑手动 change_gate）"""
        t = at or self.now
        day = t.strftime('%Y-%m-%d')
        workday = day in MAKEUP_DAYS or (day not in HOLIDAYS and t.weekday() < 5)
        sec = t.hour * 3600 + t.minute * 60 + t.second
        if workday and 25200 <= sec < 32400:
            return ['G1', 'G2', 'G3'] if direction == 'I' else ['G4']
        if workday and 61200 <= sec < 68400:
            return ['G4'] if direction == 'I' else ['G1', 'G2', 'G3']
        return list(GATES)

    def next_gate(self, direction: str) -> str:
        """可用出入口中最早空闲的一个"""
        def ready(g):
            last = self.gate_at.get(g)
            return last if last is not None else datetime.min
        return min(self.gates(direction), key=ready)

    def arrive(self, plate: str, want=None):
        return self.entry(plate, self.next_gate('I'), want=want)

    def leave(self, sid: str, want=rS):
        return self.exit(sid, self.next_gate('O'), want=want)

    def wait_gate(self, gate: str):
        last = self.gate_at.get(gate)
        if last is not None:
            gap = GATE_GAP - int((self.now - last).total_seconds())
            if gap > 0:
                self.time_pass('sec', gap)
        self.gate_at[gate] = self.now

    # ---------------- 车辆
    def entry(self, plate: str, gate: str, want=None):
        self.wait_gate(gate)
        ret = self.call('car_entry', plate, gate, want=want)
        m = re.match(r'E000:(p\d{16}),(CID\d{8})$', ret)
        return (m.group(1), m.group(2)) if m else (MISSING, 'CID00000000')

    def enter(self, plates, spaces=None, park=False):
        """每批4辆分别走G1~G4，流逝5秒完成入场；spaces 给出时每批入场后按序占位，park=True 时无车位号占位（A版本）"""
        out = []
        for i in range(0, len(plates), len(GATES)):
            batch = [self.entry(p, g) for p, g in zip(plates[i:i + len(GATES)], GATES)]
            self.time_pass('sec', GATE_GAP)
            if spaces:
                for (sid, _), sp in zip(batch, spaces[i:i + len(GATES)]):
                    self.occupy(sid, sp)
            elif park:
                for sid, _ in batch:
                    self.occupy(sid)
            out += batch
        return out

    def occupy(self, sid: str, space: str | None = None, want=rS):
        ret = self.call('occupy_park', sid, *([] if space is None else [space]), want=want)
        if ret == rS and space is not None:
            self.occupied.append(space)
            self.space_of[sid] = space
        return ret

    def release(self, sid: str, want=None):
        if want is None:
            want = f'E000:{self.space_of[sid]}' if self.version == 'B' and sid in self.space_of else rS
        ret = self.call('release_park', sid, want=want)
        space = self.space_of.pop(sid, None)
        if ret.startswith('E000:') and space in self.occupied:
            self.occupied.remove(space)
        return ret

    def exit(self, sid: str, gate: str, want=rS):
        self.wait_gate(gate)
        return self.call('car_exit', sid, gate, want=want)

    def status(self, plate: str, sid, st: str, want=rS):
        """出场后会话被删除，outpark 只能按车牌（会话号传 null）校验"""
        if st == 'outpark':
            sid = None
        return self.call('check_car_status', plate, sid or 'null', st, want=want)

    # ---------------- 车位（B版本）
    def stats(self, small: str | None = None, medium: str | None = None, large: str | None = None, want=rS):
        """不给参数时由 oracle 读取被测程序当前的占用|锁定统计"""
        if small is None:
            small, medium, large = self.ask('stats').split(',')
        return self.call('check_park_statistics', small, medium, large, want=want)

    def check_occupied(self, want=rS):
        if self._ask:
            spaces = [s for s in self.ask('occupied').split('|') if s]
        else:
            spaces = self.occupied
        if not spaces:
            return None
        return self.call('check_occupied_spaces', str(len(spaces)), '|'.join(spaces), want=want)

    # ---------------- oracle（与被测程序同进程的只读查询）
    def ask(self, query: str) -> str:
        if not self._ask:
            raise RuntimeError(f'{self.name}: 需要 oracle 才能计算输入值（{query}）')
        ret = self._ask(query)
        if ret.startswith('error') or ret == 'unknown':
            raise RuntimeError(f'oracle {query}: {ret}')
        return ret

    def due(self, sid: str) -> dict:
        return json.loads(self.ask(f'due {sid}'))

    def pay_value(self, sid: str, kind: str) -> str:
        """按被测程序的结算规则算出 pay_parkcost 应填的金额"""
        d = self.due(sid)
        if not d['ok']:
            return '1.00'
        amount = d['due']
        if kind == 'temp' and d['member']:
            if d['balance'] < 0:
                amount = d['due'] - d['balance']
            else:
                avail = max(0.0, d['balance'] - max(0.0, d['gift'] - d['giftAtEntry']))
                amount = d['due'] - min(avail, d['due'])
        return f'{round(amount + 1e-9, 2):.2f}'

    def pay(self, sid: str, kind: str = 'auto', delta: float = 0.0, want=None):
        """kind=auto 时会员余额足够用 bal，否则 temp；delta 用于构造金额偏差"""
        if kind == 'auto':
            d = self.due(sid)
            avail = max(0.0, d.get('balance', 0) - max(0.0, d.get('gift', 0) - d.get('giftAtEntry', 0)))
            kind = 'bal' if d.get('member') and d['balance'] >= 0 and avail >= d.get('due', 0) else 'temp'
        value = self.pay_value(sid, kind)
        if delta:
            value = f'{float(value) + delta:.2f}'
        return self.call('pay_parkcost', sid, kind, value, want=want)

    def revenue(self, scope: str, kind: str, delta: float = 0.0, want=None):
        value = self.ask(f'revenue {scope} {kind}')
        if delta:
            value = f'{float(value) + delta:.2f}'
        return self.call('check_revenue', scope, kind, value, want=want)

    def revenue_all(self, scopes=('day', 'month', 'year')):
        for scope in scopes:
            for kind in ('recharge', 'monthcard', 'temppay', 'balpay'):
                self.revenue(scope, kind, want=rS)

    def asset(self, cid: str, want=None):
        return self.call('query_account_asset', cid, want=want)

    # ---------------- 账户
    def register(self, phone: str, password: str, want=None):
        ret = self.call('register_account', phone, password, want=want)
        m = re.match(r'E000:(CID\d{8}),(\d{6})$', ret)
        return (m.group(1), m.group(2)) if m else ('CID00000000', '000000')

    def member(self, phone: str, password: str = 'Abc123!'):
        """注册并立即激活，返回 member_id"""
        cid, code = self.register(phone, password)
        self.call('activate_account', cid, code, want=rS)
        return cid


def plates(kind: str, start: int, n: int) -> list[str]:
    return [f'{kind}{i:05d}' for i in range(start, start + n)]


def spaces(kind: str, start: int, n: int) -> list[str]:
    return [f'{kind}{i:03d}' for i in range(start, start + n)]


SCENES: list[tuple[str, object]] = []


def scene(name: str):
    def deco(fn):
        SCENES.append((name, fn))
        return fn
    return deco


# ---------------------------------------------------------------- 一致性比较

def normalize(trace: list[tuple[str, str]]) -> list[tuple[str, str]]:
    """会话号、CID、验证码按首次出现顺序替换为占位符；验证码只在注册返回和激活参数中出现"""
    tokens: dict[str, str] = {}

    def ids(text: str) -> str:
        return ID_RE.sub(lambda m: tokens.setdefault(m.group(0), f'<id{len(tokens) + 1}>'), text)

    def code(c: str) -> str:
        return tokens.setdefault('code:' + c, f'<code{len(tokens) + 1}>')

    out = []
    for cmd, ret in trace:
        cmd, ret = ids(cmd), ids(ret)
        m = re.match(r'(activate_account\([^,]*,)(\d{6})\)$', cmd)
        if m:
            cmd = f'{m.group(1)}{code(m.group(2))})'
        m = re.match(r'(E000:<id\d+>,)(\d{6})$', ret)
        if m and cmd.startswith('register_account'):
            ret = m.group(1) + code(m.group(2))
        out.append((cmd, ret))
    return out


def compare_runs(runs: list[list[tuple[str, str]]]) -> list[str]:
    base = normalize(runs[0])
    out = []
    for k, other in enumerate(runs[1:], 2):
        other = normalize(other)
        if len(other) != len(base):
            out.append(f'第 {k} 轮步骤数 {len(other)} 与第 1 轮 {len(base)} 不同')
        for i, (a, b) in enumerate(zip(base, other), 1):
            if a != b:
                out.append(f'步骤 {i}: {a[0]} 第1轮 {a[1]} / 第{k}轮 {b[1]}')
    return out


# ---------------------------------------------------------------- 答题目录与被测程序

def read_port(work: Path) -> int:
    for line in (work / 'config.properties').read_text(encoding='utf-8', errors='replace').splitlines():
        line = line.strip()
        if not line.startswith('#') and '=' in line:
            k, v = line.split('=', 1)
            if k.strip() == 'udpPort':
                return int(v.strip())
    raise SystemExit('config.properties 中没有 udpPort')


def port_free(port: int) -> bool:
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.bind(('127.0.0.1', port))
        return True
    except OSError:
        return False
    finally:
        s.close()


def wait_port_free(port: int, seconds: float) -> bool:
    end = time.time() + seconds
    while time.time() < end:
        if port_free(port):
            return True
        time.sleep(0.5)
    return port_free(port)


def check_work(work: Path) -> int:
    need = ['testtool.py', 'timeout.py', 'config.properties', 'udpunittest.py']
    missing = [n for n in need if not (work / n).is_file()]
    if missing:
        raise SystemExit(f'答题目录缺少 {missing}')
    jars = sorted(p.name for p in work.glob('*.jar'))
    if len(jars) != 1:
        raise SystemExit(f'答题目录必须恰好有一个 jar，当前 {jars}')
    port = read_port(work)
    if not port_free(port):
        raise SystemExit(f'udpPort {port} 已被占用：结束占用该端口的进程，或修改 config.properties 中的 udpPort')
    return port


def install_aw(work: Path) -> None:
    src, dst = HERE / 'testaw.py', work / 'testaw.py'
    if dst.is_file() and dst.read_bytes() != src.read_bytes():
        shutil.copy2(dst, work / 'testaw.py.bak')
        print(f'原 testaw.py 已备份为 testaw.py.bak')
    shutil.copy2(src, dst)


def find_jjs() -> str | None:
    found = shutil.which('jjs')
    if found:
        return found
    try:
        out = subprocess.run(['java', '-XshowSettings:properties', '-version'], capture_output=True, text=True).stderr
    except OSError:
        return None
    m = re.search(r'java\.home = (.+)', out)
    if not m:
        return None
    for name in ('jjs.exe', 'jjs'):
        p = Path(m.group(1).strip()) / 'bin' / name
        if p.is_file():
            return str(p)
    return None


class Oracle:
    """用 jjs 在同一 JVM 中启动被测 jar（走 jar 自身入口）并提供只读状态查询"""

    def __init__(self, work: Path, port: int, jjs: str):
        self.port = port + 1
        jar = next(work.glob('*.jar')).name
        self.log = open(work / 'oracle.log', 'w')
        self.proc = subprocess.Popen([jjs, '-cp', jar, str(HERE / 'oracle.js'), '--', 'config.properties', str(self.port)],
                                     cwd=work, stdout=self.log, stderr=subprocess.STDOUT)
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.sock.settimeout(5)

    def ask(self, query: str) -> str:
        self.sock.sendto(query.encode('utf-8'), ('127.0.0.1', self.port))
        return self.sock.recvfrom(1 << 20)[0].decode('utf-8')

    def wait(self, send, seconds=30):
        end = time.time() + seconds
        while time.time() < end:
            if self.proc.poll() is not None:
                raise SystemExit(f'oracle 进程退出（{self.proc.returncode}），见答题目录 oracle.log')
            try:
                if send('init()') == rS and self.ask('ping') == 'pong':
                    return
            except OSError:
                pass
            time.sleep(1)
        raise SystemExit('oracle 启动超时，见答题目录 oracle.log')

    def close(self):
        self.proc.kill()
        self.proc.wait()
        self.log.close()
        self.sock.close()


def record_all(work: Path, version: str, selected, runs: int, use_oracle: bool):
    os.chdir(work)
    sys.path.insert(0, str(work))
    from testtool import tool  # noqa: E402  平台框架
    for h in logging.getLogger().handlers:
        h.setLevel(logging.WARNING)
    results = {}
    oracle = None
    if use_oracle:
        oracle = Oracle(work, read_port(work), find_jjs())
        oracle.wait(tool.send_cmd)
    else:
        tool.start_dut()
    try:
        for name, fn in selected:
            per_run, rec0, error, cost = [], None, None, 0.0
            for k in range(runs):
                tool.send_cmd('init()')
                rec = Rec(name, tool.send_cmd, version, oracle.ask if oracle else None)
                t0 = time.time()
                try:
                    fn(rec)
                except Exception:  # noqa: BLE001
                    error = traceback.format_exc(limit=3)
                    print(f'  {name} 第 {k + 1} 轮录制异常：{error.strip().splitlines()[-1]}')
                    break
                if k == 0:
                    rec0, cost = rec, time.time() - t0
                per_run.append(rec.trace)
            results[name] = {'rec': rec0, 'error': error, 'cost': cost,
                             'unstable': compare_runs(per_run) if len(per_run) > 1 and not error else []}
            r = results[name]
            if rec0 and not error:
                print(f'  {name}: {len(rec0.lines)} 步，{cost:.1f}s，与预期不同 {len(rec0.diffs)}，'
                      f'各轮不一致 {len(r["unstable"])}', flush=True)
        tool.send_cmd('init()')
    finally:
        if oracle:
            oracle.close()
        else:
            try:
                tool.stop_dut()
            except Exception:  # noqa: BLE001
                try:
                    tool.send_cmd_only('EXIT()')
                except Exception:  # noqa: BLE001
                    pass
    return results


# ---------------------------------------------------------------- 生成 udpunittest.py

TEST_HEAD_RE = re.compile(r'^    (?:@timeout\(.*\)|def (test_\w+)\(self\):)\s*$')


def method_code(name: str, rec: Rec, cost: float) -> str:
    limit = max(DEFAULT_TIMEOUT, math.ceil(cost * TIMEOUT_FACTOR))
    deco = 'global_default_timeout' if limit == DEFAULT_TIMEOUT else str(limit)
    return '\n'.join([f'    @timeout({deco})', f'    def {name}(self):', '        cjl_init_service()'] + rec.lines)


def merge_tests(text: str, blocks: dict[str, str]) -> str:
    lines = text.splitlines()
    out, i = [], 0
    while i < len(lines):
        line = lines[i]
        name = None
        if line.startswith('    @timeout(') and i + 1 < len(lines):
            m = re.match(r'^    def (test_\w+)\(self\):', lines[i + 1])
            name = m.group(1) if m else None
        elif re.match(r'^    def (test_\w+)\(self\):', line):
            name = re.match(r'^    def (test_\w+)\(self\):', line).group(1)
        if name in blocks:
            j = i + (2 if line.startswith('    @timeout(') else 1)
            while j < len(lines) and (lines[j].startswith('        ') or not lines[j].strip()):
                j += 1
            i = j
            continue
        out.append(line)
        i += 1
    text = '\n'.join(out) + '\n'
    idx = text.rfind('\nif __name__')
    if idx < 0:
        raise SystemExit('udpunittest.py 中没有 if __name__ 段，无法确定插入位置')
    head, tail = text[:idx], text[idx + 1:]
    return head.rstrip('\n') + '\n' + '\n'.join(blocks.values()) + '\n\n\n' + tail


def write_tests(work: Path, results: dict) -> Path:
    blocks = {n: method_code(n, r['rec'], r['cost']) for n, r in results.items() if r['rec'] and not r['error']}
    path = work / 'udpunittest.py'
    if not blocks:
        print(f'没有录制成功的测试，{path.name} 未改动')
        return path
    backup = work / f'udpunittest.py.bak_{datetime.now():%Y%m%d_%H%M%S}'
    shutil.copy2(path, backup)
    text = path.read_bytes().decode('utf-8-sig')
    path.write_text(merge_tests(text, blocks), encoding='utf-8')
    print(f'已写入 {len(blocks)} 个测试到 {path}（原文件备份为 {backup.name}）')
    return path


def verify(work: Path, port: int) -> str:
    if not wait_port_free(port, 15):
        return f'端口 {port} 未释放，跳过验证'
    print('验证：执行 python udpunittest.py ...', flush=True)
    proc = subprocess.run([sys.executable, 'udpunittest.py'], cwd=work, capture_output=True)
    log = work / 'udpunittest.log'
    text = log.read_text(encoding='utf-8', errors='replace') if log.is_file() else ''
    bad = [ln for ln in text.splitlines() if ln.startswith(('FAIL:', 'ERROR:'))]
    ran = re.search(r'^Ran (\d+) tests?', text, re.M)
    head = f"验证：执行 {ran.group(1) if ran else '?'} 个测试，退出码 {proc.returncode}"
    return head + ('，全部通过' if not bad and proc.returncode == 0 else '\n' + '\n'.join(bad or [text[-2000:]]))


def write_report(work: Path, version: str, results: dict, verify_text: str) -> Path:
    out = [f'# 录制报告（{version} 版本，{datetime.now():%Y-%m-%d %H:%M:%S}）', '',
           '| 测试 | 步骤 | 录制耗时(s) | 与预期不同 | 各轮不一致 | 状态 |', '|---|---|---|---|---|---|']
    for name, r in results.items():
        rec = r['rec']
        state = '录制异常，未写入' if r['error'] else '已写入'
        out.append(f"| {name} | {len(rec.lines) if rec else '-'} | {r['cost']:.1f} | "
                   f"{len(rec.diffs) if rec else '-'} | {len(r['unstable'])} | {state} |")
    out += ['', '## 与预期不同的步骤', '', '用例中写入的是实际返回；预期值是编写步骤时按规格推测的，不同处说明对规则的理解需要修正或可能是有价值的场景。', '']
    if not any(r['rec'] and r['rec'].diffs for r in results.values()):
        out += ['无', '']
    for name, r in results.items():
        if r['rec'] and r['rec'].diffs:
            out.append(f'### {name}')
            out += [f'- 第 {no} 行 `{cmd}` 预期 `{want}` 实际 `{got}`' for no, cmd, want, got in r['rec'].diffs]
            out.append('')
    out += ['## 各轮不一致的步骤', '', '同一步骤在多轮录制中返回不同（会话号、账号、验证码已归一化）；这些断言在平台上可能不稳定。', '']
    if not any(r['unstable'] for r in results.values()):
        out += ['无', '']
    for name, r in results.items():
        if r['unstable']:
            out.append(f'### {name}')
            out += [f'- {x}' for x in r['unstable'][:30]]
            out.append('')
    out += ['## 录制异常', '']
    if not any(r['error'] for r in results.values()):
        out += ['无', '']
    for name, r in results.items():
        if r['error']:
            out += [f'### {name}', '```', r['error'].strip(), '```', '']
    out += ['## 验证', '', verify_text or '未执行', '']
    path = work / 'record_report.md'
    path.write_text('\n'.join(out), encoding='utf-8')
    return path


def load_scenes(version: str, module: str | None = None):
    sys.path.insert(0, str(HERE))
    SCENES.clear()
    sys.modules['record'] = sys.modules[__name__]
    importlib.import_module(module or f'scenes_{version.lower()}')
    return list(SCENES)


def main(argv=None) -> int:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('--work', help='答题目录（含 jar、testtool.py、config.properties、udpunittest.py）')
    ap.add_argument('--version', required=True, choices=['A', 'B'], help='题目版本，决定使用 scenes_a.py 还是 scenes_b.py')
    ap.add_argument('--scenes', help='场景模块名，默认 scenes_a / scenes_b')
    ap.add_argument('--only', nargs='*', default=[], help='只录制名称包含这些片段的测试')
    ap.add_argument('--runs', type=int, default=2, help='录制轮数，用于检查返回是否稳定，默认 2')
    ap.add_argument('--no-verify', action='store_true', help='写入后不执行 udpunittest.py 验证')
    ap.add_argument('--list', action='store_true', help='只列出测试名')
    ap.add_argument('--no-oracle', action='store_true', help='不使用 oracle.js，按平台框架 start_dut 启动 jar')
    args = ap.parse_args(argv)
    scenes = load_scenes(args.version, args.scenes)
    if args.only:
        scenes = [(n, f) for n, f in scenes if any(k in n for k in args.only)]
    if args.list:
        print('\n'.join(n for n, _ in scenes))
        return 0
    if not args.work:
        ap.error('需要 --work')
    if not scenes:
        print('没有匹配的测试')
        return 1
    work = Path(args.work).resolve()
    port = check_work(work)
    install_aw(work)
    use_oracle = not args.no_oracle and (HERE / 'oracle.js').is_file() and find_jjs() is not None
    if use_oracle and not port_free(port + 1):
        raise SystemExit(f'oracle 端口 {port + 1} 已被占用')
    print(f'录制 {len(scenes)} 个测试，每个 {args.runs} 轮（{args.version} 版本，端口 {port}，'
          f'{"oracle 模式" if use_oracle else "无 oracle，需要计算输入值的步骤会报错"}）')
    cwd = os.getcwd()
    try:
        results = record_all(work, args.version, scenes, max(1, args.runs), use_oracle)
    finally:
        os.chdir(cwd)
    write_tests(work, results)
    verify_text = '' if args.no_verify else verify(work, port)
    if verify_text:
        print(verify_text)
    report = write_report(work, args.version, results, verify_text)
    print(f'报告：{report}')
    return 0 if not any(r['error'] for r in results.values()) else 2


if __name__ == '__main__':
    sys.exit(main())
