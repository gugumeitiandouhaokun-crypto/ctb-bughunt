"""C 版本覆盖率：带 JaCoCo 运行答题目录的 udpunittest.py，输出目标方法覆盖表，--gaps 时列出未覆盖行对应的反编译源码。

用法: python covc.py --work _workC [--no-run] [--gaps] [--exec a.exec b.exec ...]
"""
import argparse
import os
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
import zipfile

from gap33 import CLI, PKG, cfr_key, cfr_map, parse_class

HERE = os.path.dirname(os.path.abspath(__file__))
AGENT = os.path.join(HERE, '..', 'ctb-bughunt', 'tools', 'jacocoagent.jar')
S, S2, S3, F = 'Ljava/lang/String;', 'Ljava/lang/String;Ljava/lang/String;', \
    'Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;', 'Lcom/huawei/tools/g/a/f;'

TARGETS = [
    ('h', None, None, '预约类（全部方法）'),
    ('f', 'g', f'({S2})', 'car_entry'),
    ('f', 'j', f'({S2})', 'occupy_park'),
    ('f', 'b', f'({S})L', 'release_park'),
    ('f', 'f', f'({S})L', 'add_blacklist'),
    ('f', 'g', f'({S})L', 'del_blacklist'),
    ('f', 'i', f'({S})L', 'cancel_reserve'),
    ('f', 'n', f'({S2})', 'check_reserve_status'),
    ('e', 'k', f'({S2})', 'check_occupied_spaces'),
    ('e', 'b', f'({S3})', 'check_park_statistics'),
    ('e', 'p', f'({S2})Z', '满位判定'),
    ('e', 'l', '()V', '锁定配额重算'),
    ('e', 'c', f'({F})V', '出场释放车位/预约车位'),
    ('e', 'z', f'({S})V', '强制出场'),
    ('e', 'h', '()V', '场内超时扫描'),
    ('e', 'a', f'({S}I)', '时间推进（放行）'),
    ('g', 'f', f'({F})Z', '是否进场中'),
    ('g', 'r', f'({S2})V', '预约数+1'),
    ('g', 's', f'({S2})V', '预约数-1'),
    ('g', 'F', f'({S})Z', '车牌是否在场'),
    ('g', 'J', f'({S})Z', '是否黑名单'),
]


def wanted(cls, name, desc):
    for c, n, d, label in TARGETS:
        if c != cls:
            continue
        if n is None:
            if name not in ('<init>', '<clinit>') and not name.startswith('lambda$'):
                return label + f' {name}{desc.split(")")[0]})'
        elif n == name and desc.startswith(d):
            return label
    return None


def run(work, exec_path):
    if os.path.exists(exec_path):
        os.remove(exec_path)
    env = dict(os.environ)
    env['JAVA_TOOL_OPTIONS'] = f'-javaagent:{AGENT}=destfile={exec_path},includes=com.huawei.tools.*'
    t0 = time.time()
    proc = subprocess.run([sys.executable, 'udpunittest.py'], cwd=work, env=env, capture_output=True)
    for _ in range(30):
        if os.path.exists(exec_path) and os.path.getsize(exec_path) > 0:
            break
        time.sleep(1)
    log = open(os.path.join(work, 'udpunittest.log'), encoding='utf-8', errors='replace').read().splitlines()
    tail = [ln for ln in log if ln.startswith(('Ran ', 'OK', 'FAILED'))]
    print(f'udpunittest 退出码 {proc.returncode}，耗时 {time.time() - t0:.0f}s，{" ".join(tail)}')
    for ln in log:
        if ln.startswith(('FAIL:', 'ERROR:')):
            print('  ' + ln)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--work', default='_workC')
    ap.add_argument('--no-run', action='store_true')
    ap.add_argument('--gaps', action='store_true')
    ap.add_argument('--exec', nargs='*')
    args = ap.parse_args()
    work = os.path.join(HERE, args.work)
    exec_path = os.path.join(work, 'jacocoC.exec')
    if not args.no_run and not args.exec:
        run(work, exec_path)
    execs = args.exec or [exec_path]
    jar = [f for f in os.listdir(work) if f.endswith('.jar')][0]
    z = zipfile.ZipFile(os.path.join(work, jar))
    env = {k: v for k, v in os.environ.items() if k != 'JAVA_TOOL_OPTIONS'}
    total = [0, 0, 0, 0]
    rows, gaps = [], []
    for cls in sorted({t[0] for t in TARGETS}):
        d = os.path.join(work, 'gap', 'cls_' + cls)
        os.makedirs(os.path.join(d, PKG), exist_ok=True)
        data = z.read(PKG + cls + '.class')
        open(os.path.join(d, PKG, cls + '.class'), 'wb').write(data)
        out = os.path.join(work, 'gap', f'{cls}.xml')
        subprocess.run(['java', '-jar', CLI, 'report', *execs, '--classfiles', d, '--xml', out],
                       check=True, capture_output=True, env=env)
        root = ET.parse(out).getroot()
        lines = {int(ln.get('nr')): ln for ln in root.iter('line')}
        mtab = parse_class(data)
        src, ctab = cfr_map(d, cls, os.path.join(work, 'gap', 'cfr')) if args.gaps else ([], {})
        for m in root.iter('method'):
            name, desc = m.get('name'), m.get('desc')
            label = wanted(cls, name, desc)
            if not label:
                continue
            c = {x.get('type'): (int(x.get('missed')), int(x.get('covered'))) for x in m.findall('counter')}
            lm, lc = c.get('LINE', (0, 0))
            bm, bc = c.get('BRANCH', (0, 0))
            total = [total[0] + lc, total[1] + lm + lc, total[2] + bc, total[3] + bm + bc]
            rows.append(f'| {cls}.{name}{desc.split(")")[0]}) | {label} | {lc}/{lm + lc} | {bc}/{bm + bc} |')
            if not args.gaps or (lm == 0 and bm == 0):
                continue
            lnt = mtab.get((name, desc), [])
            cmap = ctab.get(cfr_key(name, desc), [])
            gaps.append(f'\n== {cls}.{name}{desc.split(")")[0]}) [{label}]')
            for l in sorted({x for _, x in lnt}):
                el = lines.get(l)
                if el is None or not (int(el.get('mi')) or int(el.get('mb'))):
                    continue
                pcs = [(pc, lnt[k + 1][0] if k + 1 < len(lnt) else 1 << 30) for k, (pc, x) in enumerate(lnt) if x == l]
                outs = sorted({ol for ol, pc in cmap if any(a <= pc < b for a, b in pcs)})
                if not outs and pcs and cmap:
                    near = [(pc, ol) for ol, pc in cmap if pc <= pcs[0][0]]
                    outs = [max(near)[1]] if near else []
                text = ' | '.join(src[o - 1].strip() for o in outs[:2]) if outs else '?'
                gaps.append(f'  L{l} mi={el.get("mi")} mb={el.get("mb")}/{int(el.get("mb")) + int(el.get("cb"))}'
                            f' -> {outs[:2]} {text[:180]}')
    print('| 方法 | 用途 | 行 覆盖/总 | 分支 覆盖/总 |')
    print('|---|---|---|---|')
    print('\n'.join(rows))
    print(f'合计：行 {total[0]}/{total[1]}，分支 {total[2]}/{total[3]}')
    if gaps:
        print('\n'.join(gaps))


if __name__ == '__main__':
    main()
