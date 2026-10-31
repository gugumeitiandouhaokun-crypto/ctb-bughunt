# -*- coding: utf-8 -*-
"""带 JaCoCo 执行答题目录中的 udpunittest.py，报告 3.3 账户充值子系统相关方法的覆盖情况。

用法：python cov33.py [--work _work33] [--no-run]
"""
import argparse
import os
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

HERE = Path(__file__).resolve().parent
TOOLS = HERE.parent / 'ctb-bughunt' / 'tools'
PKG = 'com/huawei/tools/g/b/a/'
TARGETS = {
    'a': {'b', 'l', 'a'},
    'f': {'a', 'c'},
    'b': {'h', 'd', 'f', 'e'},
    'c': {'a', 'd'},
    'g': {'z', 'A'},
    'e': {'c', 'm', 'a'},
}
DESCS = {
    ('a', 'b', '(Ljava/lang/String;I)Ljava/lang/String;'): 'recharge_account',
    ('a', 'l', '(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;'): 'purchase_moncard',
    ('a', 'b', '(Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;'): 'transfer_moncard',
    ('a', 'a', '(JLjava/lang/String;D)V'): '营收累加',
    ('f', 'a', '(Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;'): 'transfer_account',
    ('f', 'c', '(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;'): 'pay_parkcost',
    ('b', 'h', '(Ljava/lang/String;)Ljava/lang/String;'): 'query_account_asset',
    ('b', 'd', '(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;'): 'unregister_account',
    ('b', 'f', '(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;'): 'unbind_plate_id',
    ('b', 'e', '(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;'): 'bind_plate_id',
    ('c', 'a', '(Lcom/huawei/tools/g/a/f;)V'): '强制结算扣余额',
    ('c', 'd', '(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;'): 'check_revenue',
    ('g', 'z', '(Ljava/lang/String;)I'): '可转让天数',
    ('g', 'A', '(Ljava/lang/String;)I'): '剩余天数',
    ('e', 'c', '(Lcom/huawei/tools/g/a/f;)V'): '出场失败退款',
    ('e', 'm', '(Ljava/lang/String;)V'): '强制出场',
    ('e', 'a', '(Ljava/lang/String;I)Ljava/lang/String;'): '时间推进（出场扣费）',
}


def run(work: Path, exec_path: Path) -> None:
    if exec_path.exists():
        exec_path.unlink()
    agent = TOOLS / 'jacocoagent.jar'
    env = dict(os.environ)
    env['JAVA_TOOL_OPTIONS'] = f'-javaagent:{agent}=destfile={exec_path},includes=com.huawei.tools.*'
    t0 = time.time()
    proc = subprocess.run([sys.executable, 'udpunittest.py'], cwd=work, env=env, capture_output=True)
    for _ in range(30):
        if exec_path.exists() and exec_path.stat().st_size > 0:
            break
        time.sleep(1)
    log = (work / 'udpunittest.log').read_text(encoding='utf-8', errors='replace')
    bad = [ln for ln in log.splitlines() if ln.startswith(('FAIL:', 'ERROR:'))]
    tail = [ln for ln in log.splitlines() if ln.startswith(('Ran ', 'OK', 'FAILED'))]
    print(f'udpunittest 退出码 {proc.returncode}，耗时 {time.time() - t0:.0f}s，{" ".join(tail)}')
    for ln in bad:
        print('  ' + ln)
    if not exec_path.exists():
        raise SystemExit('没有生成 jacoco.exec（被测程序可能未正常退出）')


def counters(node):
    out = {}
    for c in node.findall('counter'):
        out[c.get('type')] = (int(c.get('missed')), int(c.get('covered')))
    return out


def report(work: Path, exec_path: Path) -> None:
    xml_path = work / 'jacoco33.xml'
    subprocess.run(['java', '-jar', str(TOOLS / 'jacococli.jar'), 'report', str(exec_path),
                    '--classfiles', str(work / 'tp000.jar'), '--xml', str(xml_path)],
                   check=True, capture_output=True, env={k: v for k, v in os.environ.items() if k != 'JAVA_TOOL_OPTIONS'})
    root = ET.parse(xml_path).getroot()
    rows = []
    for cls in root.iter('class'):
        name = cls.get('name')
        if not name.startswith(PKG):
            continue
        short = name[len(PKG):]
        if short not in TARGETS:
            continue
        wanted = TARGETS[short]
        methods = cls.findall('method')
        for m in methods:
            key = (short, m.get('name'), m.get('desc'))
            if key not in DESCS and not (wanted is None and m.get('name') not in ('<init>', '<clinit>')):
                continue
            if wanted is not None and key not in DESCS:
                continue
            c = counters(m)
            rows.append((short, m.get('name'), m.get('desc'), DESCS.get(key, ''), c, int(m.get('line') or 0)))
    print('| 方法 | 用途 | 行 覆盖/总 | 分支 覆盖/总 |')
    print('|---|---|---|---|')
    for short, mname, desc, label, c, _ in rows:
        lm, lc = c.get('LINE', (0, 0))
        bm, bc = c.get('BRANCH', (0, 0))
        print(f'| {short}.{mname}{desc.split(")")[0]}) | {label} | {lc}/{lm + lc} | {bc}/{bm + bc} |')


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--work', default=str(HERE / '_work33'))
    ap.add_argument('--no-run', action='store_true')
    args = ap.parse_args()
    work = Path(args.work).resolve()
    exec_path = work / 'jacoco33.exec'
    if not args.no_run:
        run(work, exec_path)
    report(work, exec_path)


if __name__ == '__main__':
    main()
