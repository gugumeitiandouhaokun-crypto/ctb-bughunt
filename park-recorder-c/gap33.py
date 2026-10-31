"""列出 3.3 相关方法里未覆盖的分支/指令，并映射到 CFR 反编译源码行。

用法: python gap33.py [--work _work33] [--exec x.exec ...]
"""
import argparse
import os
import re
import struct
import subprocess
import xml.etree.ElementTree as ET
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
TOOLS = os.path.join(HERE, '..', 'ctb-bughunt', 'tools')
CLI = os.path.join(TOOLS, 'jacococli.jar')
CFR = os.path.join(TOOLS, 'cfr-0.152.jar')
PKG = 'com/huawei/tools/g/b/a/'

TARGETS = {
    ('a', 'b', '(Ljava/lang/String;I)Ljava/lang/String;'): '充值',
    ('a', 'l', '(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;'): '购月卡',
    ('a', 'b', '(Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;'): '转月卡',
    ('a', 'a', '(JLjava/lang/String;D)V'): '营收累加',
    ('f', 'a', '(Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;'): '转余额',
    ('f', 'c', '(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;'): '缴费',
    ('b', 'h', '(Ljava/lang/String;)Ljava/lang/String;'): '资产查询',
    ('b', 'd', '(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;'): '注销',
    ('b', 'f', '(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;'): '解绑',
    ('c', 'a', '(Lcom/huawei/tools/g/a/f;)V'): '强制结算',
    ('c', 'd', '(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;'): 'check_revenue',
    ('g', 'z', '(Ljava/lang/String;)I'): '可转天数',
    ('g', 'A', '(Ljava/lang/String;)I'): '剩余天数',
    ('e', 'c', '(Lcom/huawei/tools/g/a/f;)V'): '退款',
    ('e', 'm', '(Ljava/lang/String;)V'): '强制出场',
    ('e', 'a', '(Ljava/lang/String;I)Ljava/lang/String;'): '时间推进',
    ('b', 'e', '(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;'): '绑定',
}


def parse_class(data):
    """返回 {(name, desc): [(start_pc, line), ...]}"""
    pos = 8
    count = struct.unpack_from('>H', data, pos)[0]
    pos += 2
    cp = [None] * count
    i = 1
    while i < count:
        tag = data[pos]
        pos += 1
        if tag == 1:
            ln = struct.unpack_from('>H', data, pos)[0]
            cp[i] = data[pos + 2:pos + 2 + ln].decode('utf-8', 'replace')
            pos += 2 + ln
        elif tag in (3, 4):
            pos += 4
        elif tag in (5, 6):
            pos += 8
            i += 1
        elif tag in (7, 8, 16, 19, 20):
            pos += 2
        elif tag == 15:
            pos += 3
        else:
            pos += 4
        i += 1
    pos += 6
    ic = struct.unpack_from('>H', data, pos)[0]
    pos += 2 + 2 * ic

    def skip_members(pos, keep):
        n = struct.unpack_from('>H', data, pos)[0]
        pos += 2
        out = {}
        for _ in range(n):
            _, ni, di, ac = struct.unpack_from('>HHHH', data, pos)
            pos += 8
            lines = []
            for _ in range(ac):
                an, alen = struct.unpack_from('>HI', data, pos)
                body = pos + 6
                if keep and cp[an] == 'Code':
                    code_len = struct.unpack_from('>I', data, body + 4)[0]
                    p = body + 8 + code_len
                    el = struct.unpack_from('>H', data, p)[0]
                    p += 2 + 8 * el
                    sac = struct.unpack_from('>H', data, p)[0]
                    p += 2
                    for _ in range(sac):
                        san, salen = struct.unpack_from('>HI', data, p)
                        if cp[san] == 'LineNumberTable':
                            k = struct.unpack_from('>H', data, p + 6)[0]
                            for j in range(k):
                                lines.append(struct.unpack_from('>HH', data, p + 8 + 4 * j))
                        p += 6 + salen
                pos = body + alen
            if keep:
                out[(cp[ni], cp[di])] = sorted(lines)
        return pos, out

    pos, _ = skip_members(pos, False)
    _, methods = skip_members(pos, True)
    return methods


def cfr_map(cls_dir, cls, outdir):
    """返回 (源码行列表, {方法签名前缀: [(out_line, pc)]})"""
    subprocess.run(['java', '-jar', CFR, os.path.join(cls_dir, PKG, cls + '.class'),
                    '--trackbytecodeloc', 'true', '--outputdir', outdir],
                   check=True, capture_output=True)
    base = os.path.join(outdir, PKG, cls + '.java')
    src = open(base, encoding='utf-8').read().splitlines()
    table, cur = {}, None
    for ln in open(base + '.lineNumberTable', encoding='utf-8'):
        ln = ln.rstrip('\n')
        m = re.match(r'Line (\d+)\s*: (\d+)', ln)
        if m and cur is not None:
            table[cur].append((int(m.group(1)), int(m.group(2))))
        elif ln and not ln.startswith(('-', 'Line number')) and '(' in ln:
            cur = ln.strip()
            table.setdefault(cur, [])
    return src, table


JTYPES = {'I': 'int', 'J': 'long', 'D': 'double', 'Z': 'boolean', 'V': 'void'}


def cfr_key(name, desc):
    args = desc[1:desc.index(')')]
    out, i = [], 0
    while i < len(args):
        arr = ''
        while args[i] == '[':
            arr += '[]'
            i += 1
        if args[i] == 'L':
            j = args.index(';', i)
            out.append(args[i + 1:j].replace('/', '.') + arr)
            i = j + 1
        else:
            out.append(JTYPES.get(args[i], args[i]) + arr)
            i += 1
    return f'{name}({"".join(a + " " for a in out)})'


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--work', default='_work33')
    ap.add_argument('--exec', nargs='*')
    args = ap.parse_args()
    work = os.path.join(HERE, args.work)
    execs = args.exec or [os.path.join(work, 'jacoco33.exec')]
    jar = [f for f in os.listdir(work) if f.endswith('.jar')][0]
    z = zipfile.ZipFile(os.path.join(work, jar))
    for cls in sorted({k[0] for k in TARGETS}):
        d = os.path.join(work, 'gap', 'cls_' + cls)
        os.makedirs(os.path.join(d, PKG), exist_ok=True)
        data = z.read(PKG + cls + '.class')
        open(os.path.join(d, PKG, cls + '.class'), 'wb').write(data)
        out = os.path.join(work, 'gap', f'{cls}.xml')
        subprocess.run(['java', '-jar', CLI, 'report', *execs, '--classfiles', d,
                        '--xml', out], check=True, capture_output=True)
        lines = {int(ln.get('nr')): ln for ln in ET.parse(out).getroot().iter('line')}
        mtab = parse_class(data)
        src, ctab = cfr_map(d, cls, os.path.join(work, 'gap', 'cfr'))
        for (c, name, desc), label in TARGETS.items():
            if c != cls:
                continue
            lnt = mtab.get((name, desc), [])
            own = sorted({l for _, l in lnt})
            miss = [(l, lines[l]) for l in own if l in lines
                    and (int(lines[l].get('mi')) or int(lines[l].get('mb')))]
            print(f'\n== {cls}.{name} [{label}] 未覆盖行 {len(miss)}')
            cmap = ctab.get(cfr_key(name, desc), [])
            for l, el in miss:
                pcs = []
                for k, (pc, line) in enumerate(lnt):
                    if line == l:
                        nxt = lnt[k + 1][0] if k + 1 < len(lnt) else 1 << 30
                        pcs.append((pc, nxt))
                outs = sorted({ol for ol, pc in cmap if any(a <= pc < b for a, b in pcs)})
                if not outs and pcs and cmap:
                    near = [(pc, ol) for ol, pc in cmap if pc <= pcs[0][0]]
                    if near:
                        outs = [max(near)[1]]
                text = ' | '.join(src[o - 1].strip() for o in outs[:3]) if outs else '?'
                print(f'  L{l} mi={el.get("mi")} mb={el.get("mb")}/{int(el.get("mb")) + int(el.get("cb"))}'
                      f' -> {outs[:3]} {text[:200]}')


if __name__ == '__main__':
    main()
