# -*- coding: utf-8 -*-
"""C版本 3.1 补充：空白参数、锁定配额溢出到其他车型、长期在场强制出场。"""
import re
from datetime import datetime as D

from record import rF, rS, scene

T = '\t'
W = '\u3000'


def reserve(r, plate, space, want=None):
    ret = r.call('reserve_space', plate, space, want=want)
    m = re.match(r'E000:(r\d{8})$', ret)
    return m.group(1) if m else 'r00000000'


def stats(r, s, m, l, want=rS):
    return r.call('check_park_statistics', s, m, l, want=want)


@scene('test_cjl_c3121_blank_params')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    r.call('check_occupied_spaces', T, 'S001', want=rF)
    r.call('check_occupied_spaces', '0', T, want=rS)
    r.call('check_occupied_spaces', '1', T, want='E002:Verify_fail')
    stats(r, T, '0|0|0', '0|0|0', want=rF)
    stats(r, '0|0|0', T, '0|0|0', want=rF)
    stats(r, '0|0|0', '0|0|0', T, want=rF)
    r.call('reserve_space', T, 'S001', want=rF)
    r.call('reserve_space', 'S312101', T, want=rF)
    r.call('query_reserve', T, want=None)
    r.call('query_reserve', W, want=rF)
    r.call('check_reserve_status', T, 'locked', want=rF)
    r.call('check_reserve_status', 'r00000000', T, want=rF)
    r.call('cancel_reserve', W, want=rF)
    r.call('add_blacklist', W, want=None)
    r.call('del_blacklist', W, want=None)
    stats(r, '0|0|0', '0|0|0', '0|0|0')


@scene('test_cjl_c3122_small_lock_overflow_to_medium')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    sids = []
    for i in range(71):
        sid, _ = r.entry(f'S312{i:03d}', f'G{i % 4 + 1}')
        sids.append(sid)
    r.time_pass('sec', 90)
    stats(r, '0|70|0', '0|1|0', '0|0|0')
    r.occupy(sids[0], 'S001')
    stats(r, '1|69|0', '0|1|0', '0|0|0')
    r.occupy(sids[1], 'M001')
    stats(r, '1|69|0', '1|0|0', '0|0|0')
    r.release(sids[0])
    stats(r, '0|70|0', '1|0|0', '0|0|0')


@scene('test_cjl_c3123_large_lock_no_space')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a, _ = r.entry('L312301', 'G1')
    r.time_pass('sec', 5)
    stats(r, '0|0|0', '0|0|0', '0|1|0')
    sids = r.enter([f'S3123{i:02d}' for i in range(10)], [f'L{i:03d}' for i in range(1, 11)])
    stats(r, '0|0|0', '0|0|0', '10|0|0')
    r.release(sids[0][0])
    stats(r, '0|1|0', '0|0|0', '9|1|0')
    r.occupy(a, 'L001')
    stats(r, '0|1|0', '0|0|0', '10|0|0')


@scene('test_cjl_c3124_long_stay_forced_exit_occupied')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    cid = r.member('13100001241')
    r.call('bind_plate_id', cid, 'S312401', want=rS)
    rid = reserve(r, 'S312401', 'S001')
    a, _ = r.entry('S312401', 'G1')
    b, _ = r.entry('M312402', 'G2')
    r.time_pass('sec', 5)
    r.occupy(a, 'S001')
    r.occupy(b, 'M001')
    stats(r, '1|0|0', '1|0|0', '0|0|0')
    r.time_pass('day', 15)
    r.status('S312401', a, 'parked')
    r.status('M312402', b, 'parked')
    r.call('add_blacklist', 'M312402', want=rS)
    r.time_pass('day', 15)
    r.time_pass('min', 1)
    r.status('S312401', a, 'outpark')
    r.status('M312402', b, 'outpark')
    r.call('check_reserve_status', rid, 'closed', want=rS)
    stats(r, '0|0|0', '0|0|0', '0|0|0')
    r.call('check_occupied_spaces', '0', T, want=rS)
