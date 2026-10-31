# -*- coding: utf-8 -*-
"""C版本 3.1 车位资源管理子系统（刷新版）：车位状态、预约、统计、黑名单与入场满位。
want 为按规格推测的值，只用于录制报告对比，写入用例的是实际返回。"""
import re
from datetime import datetime as D

from record import rF, rS, scene

V = 'E002:Verify_fail'


def members(r, phones, plates_of=()):
    out = []
    for i, phone in enumerate(phones):
        cid = r.member(phone)
        for p in (plates_of[i] if i < len(plates_of) else ()):
            r.call('bind_plate_id', cid, p, want=rS)
        out.append(cid)
    return out


def reserve(r, plate, space, want=None):
    ret = r.call('reserve_space', plate, space, want=want)
    m = re.match(r'E000:(r\d{8})$', ret)
    return m.group(1) if m else 'r00000000'


def rstat(r, rid, st, want=rS):
    return r.call('check_reserve_status', rid, st, want=want)


def stats(r, s, m, l, want=rS):
    return r.call('check_park_statistics', s, m, l, want=want)


def enter(r, plate, want_ok=True):
    sid, _ = r.arrive(plate)
    r.time_pass('sec', 5)
    r.status(plate, sid, 'inpark' if want_ok else 'outpark')
    return sid


def out(r, sid, plate, passed=True):
    r.leave(sid)
    r.time_pass('sec', 5)
    r.status(plate, sid, 'outpark' if passed else 'inpark')


@scene('test_cjl_c3101_reserve_plate_checks')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    members(r, ['13100000011'], [['S31001', 'S310011', 'S310012']])
    for p in ('X310001', 'S31A001', 'S3100011', 'S3100', 'M310019'):
        reserve(r, p, 'S001', want=rF)
    reserve(r, 'S31001', 'S001', want=rF)
    r.call('add_blacklist', 'S310011', want=rS)
    reserve(r, 'S310011', 'S001', want=rF)
    r.call('del_blacklist', 'S310011', want=rS)
    reserve(r, 'S310011', 'S001', want=None)
    enter(r, 'S310012')
    reserve(r, 'S310012', 'S002', want=rF)
    stats(r, '0|1|1', '0|0|0', '0|0|0')


@scene('test_cjl_c3102_reserve_space_checks')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13100000021'], [['S310021', 'M310022', 'L310023']])[0]
    for sp in ('S071', 'M201', 'L011', 'X001', 'S00A'):
        reserve(r, 'S310021', sp, want=rF)
    reserve(r, 'M310022', 'S001', want=rF)
    reserve(r, 'L310023', 'M001', want=rF)
    reserve(r, 'L310023', 'S001', want=rF)
    a = reserve(r, 'S310021', 'L001', want=None)
    reserve(r, 'S310021', 'S002', want=rF)
    reserve(r, 'M310022', 'L001', want=rF)
    r.time_pass('sec', 1)
    b = reserve(r, 'M310022', 'M001', want=None)
    r.time_pass('sec', 1)
    c = reserve(r, 'L310023', 'L002', want=None)
    stats(r, '0|0|0', '0|0|1', '0|0|2')
    r.call('query_reserve', m)
    for rid in (a, b, c):
        rstat(r, rid, 'locked')


@scene('test_cjl_c3103_reserve_occupied_space')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    members(r, ['13100000031', '13100000032'], [['S310031'], ['S310032', 'M310033']])
    sid = enter(r, 'S310039')
    r.occupy(sid, 'S001')
    reserve(r, 'S310031', 'S001', want=rF)
    reserve(r, 'S310031', 'S002', want=None)
    reserve(r, 'S310032', 'S002', want=rF)
    reserve(r, 'M310033', 'M002', want=None)
    stats(r, '1|0|1', '0|0|1', '0|0|0')
    r.check_occupied()


@scene('test_cjl_c3104_reserve_daily_limit')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13100000041'], [['S310041']])[0]
    rids = []
    for sp in ('S001', 'S002', 'S003'):
        rid = reserve(r, 'S310041', sp, want=None)
        r.call('cancel_reserve', rid, want=rS)
        rids.append(rid)
        r.time_pass('sec', 1)
    reserve(r, 'S310041', 'S004', want=rF)
    r.call('query_reserve', m)
    for rid in rids:
        rstat(r, rid, 'cancel')
    stats(r, '0|0|0', '0|0|0', '0|0|0')
    r.goto(D(2026, 3, 3, 0, 0, 1))
    for rid in rids:
        rstat(r, rid, 'closed')
    rid = reserve(r, 'S310041', 'S004', want=None)
    rstat(r, rid, 'locked')
    r.call('query_reserve', m)


@scene('test_cjl_c3105_reserve_expire_30min')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    members(r, ['13100000051'], [['S310051']])
    rid = reserve(r, 'S310051', 'S001', want=None)
    r.time_pass('min', 30)
    rstat(r, rid, 'locked')
    stats(r, '0|0|1', '0|0|0', '0|0|0')
    r.time_pass('sec', 1)
    rstat(r, rid, 'expired')
    rstat(r, rid, 'locked', want=V)
    stats(r, '0|0|0', '0|0|0', '0|0|0')
    r.call('cancel_reserve', rid, want=rF)
    reserve(r, 'S310051', 'S001', want=None)
    r.goto(D(2026, 3, 3, 0, 0, 1))
    rstat(r, rid, 'closed')
    stats(r, '0|0|0', '0|0|0', '0|0|0')


@scene('test_cjl_c3106_reserve_status_param')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    members(r, ['13100000061'], [['S310061']])
    rid = reserve(r, 'S310061', 'S001', want=None)
    for st in ('locked', 'used', 'expired', 'cancel', 'closed', 'canceled', 'LOCKED', 'x'):
        rstat(r, rid, st, want=None)
    for bad in ('r1234567', 'r123456789', 'x12345678', 'R12345678'):
        rstat(r, bad, 'locked', want=rF)
    rstat(r, 'r00000000', 'locked', want=rF)
    r.call('cancel_reserve', 'r00000000', want=rF)


@scene('test_cjl_c3107_reserved_car_enter_and_occupy')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    members(r, ['13100000071'], [['S310071']])
    rid = reserve(r, 'S310071', 'S001', want=None)
    sid = enter(r, 'S310071')
    rstat(r, rid, 'used')
    r.call('cancel_reserve', rid, want=rF)
    stats(r, '0|0|1', '0|0|0', '0|0|0')
    other = enter(r, 'S310079')
    r.occupy(other, 'S001', want=rF)
    r.occupy(sid, 'S002', want=rF)
    r.occupy(sid, 'S001')
    stats(r, '1|1|0', '0|0|0', '0|0|0')
    r.occupy(other, 'S002')
    r.release(sid)
    stats(r, '1|0|1', '0|0|0', '0|0|0')
    r.occupy(sid, 'S003', want=rF)
    r.occupy(sid, 'S001')
    r.release(sid)
    out(r, sid, 'S310071')
    rstat(r, rid, 'closed')
    stats(r, '1|0|0', '0|0|0', '0|0|0')
    r.check_occupied()


@scene('test_cjl_c3108_reserved_car_bypass_full')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    members(r, ['13100000081'], [['L310081', 'L310189']])
    c = reserve(r, 'L310189', 'L002', want=None)
    r.call('cancel_reserve', c, want=rS)
    rid = reserve(r, 'L310081', 'L001', want=None)
    r.enter([f'L3101{i:02d}' for i in range(9)], [f'L{i:03d}' for i in range(2, 11)])
    stats(r, '0|0|0', '0|0|0', '9|0|1')
    enter(r, 'L310189', want_ok=False)
    sid = enter(r, 'L310081')
    rstat(r, rid, 'used')
    r.occupy(sid, 'L001')
    stats(r, '0|0|0', '0|0|0', '10|0|0')
    r.check_occupied()


@scene('test_cjl_c3109_reserved_car_entry_rejected')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    members(r, ['13100000091'], [['S310091', 'M310092']])
    a = reserve(r, 'S310091', 'S001', want=None)
    b = reserve(r, 'M310092', 'M001', want=None)
    r.call('add_blacklist', 'S310091', want=rS)
    r.call('add_blacklist', 'S310091', want=rS)
    enter(r, 'S310091', want_ok=False)
    rstat(r, a, 'locked')
    r.call('change_gate', 'G1', 'O', want=rS)
    s, _ = r.entry('M310092', 'G1')
    r.time_pass('sec', 5)
    r.status('M310092', s, 'outpark')
    rstat(r, b, 'locked')
    stats(r, '0|0|1', '0|0|1', '0|0|0')
    r.call('del_blacklist', 'S310091', want=rS)
    r.call('del_blacklist', 'S310091', want=rS)
    enter(r, 'S310091')
    rstat(r, a, 'used')


@scene('test_cjl_c3110_entrying_blocks_cancel_and_expire')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    members(r, ['13100000101'], [['S310101', 'S310102']])
    a = reserve(r, 'S310101', 'S001', want=None)
    r.time_pass('min', 29)
    r.time_pass('sec', 58)
    s, _ = r.entry('S310101', 'G1')
    r.call('cancel_reserve', a, want=rF)
    r.time_pass('sec', 3)
    rstat(r, a, 'locked')
    r.status('S310101', s, 'entrying')
    r.time_pass('sec', 2)
    rstat(r, a, 'used')
    r.occupy(s, 'S001')
    r.goto(D(2026, 3, 2, 23, 50))
    b = reserve(r, 'S310102', 'S002', want=None)
    r.time_pass('min', 9)
    r.time_pass('sec', 58)
    t, _ = r.entry('S310102', 'G2')
    r.time_pass('sec', 3)
    rstat(r, b, 'locked')
    r.time_pass('sec', 2)
    rstat(r, b, 'used')
    rstat(r, a, 'used')
    stats(r, '1|0|1', '0|0|0', '0|0|0')


@scene('test_cjl_c3111_day_change_closes_locked')
def _(r):
    r.goto(D(2026, 3, 2, 23, 50))
    members(r, ['13100000111'], [['S310111', 'S310112']])
    a = reserve(r, 'S310111', 'S001', want=None)
    sid = enter(r, 'S310112')
    r.time_pass('min', 10)
    r.time_pass('sec', 1)
    rstat(r, a, 'closed')
    stats(r, '0|1|0', '0|0|0', '0|0|0')
    r.occupy(sid, 'S001')
    stats(r, '1|0|0', '0|0|0', '0|0|0')


@scene('test_cjl_c3112_forced_exit_releases_reserved')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    members(r, ['13100000121'], [['S310121', 'S310122']])
    a = reserve(r, 'S310121', 'S001', want=None)
    b = reserve(r, 'S310122', 'S002', want=None)
    sa = enter(r, 'S310121')
    sb = enter(r, 'S310122')
    r.occupy(sb, 'S002')
    r.time_pass('min', 10)
    r.release(sb)
    stats(r, '0|0|2', '0|0|0', '0|0|0')
    r.time_pass('min', 5)
    r.status('S310121', sa, 'outpark')
    rstat(r, a, 'closed')
    r.status('S310122', sb, 'inpark')
    r.time_pass('min', 10)
    r.status('S310122', sb, 'outpark')
    rstat(r, b, 'closed')
    stats(r, '0|0|0', '0|0|0', '0|0|0')


@scene('test_cjl_c3113_used_reservation_over_midnight')
def _(r):
    r.goto(D(2026, 3, 2, 23, 40))
    m = members(r, ['13100000131'], [['S310131']])[0]
    rid = reserve(r, 'S310131', 'S001', want=None)
    sid = enter(r, 'S310131')
    r.occupy(sid, 'S001')
    r.goto(D(2026, 3, 3, 0, 5))
    rstat(r, rid, 'used')
    r.call('query_reserve', m)
    reserve(r, 'S310131', 'S002', want=rF)
    r.release(sid)
    out(r, sid, 'S310131')
    rstat(r, rid, 'closed')
    reserve(r, 'S310131', 'S002', want=None)


@scene('test_cjl_c3114_query_reserve_accounts')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13100000141'], [['S310141', 'M310142']])[0]
    gone = members(r, ['13100000142'])[0]
    idle = members(r, ['13100000143'])[0]
    inactive, _ = r.register('13100000144', 'Abc123!')
    r.call('unregister_account', gone, 'Abc123!', want=rS)
    for cid in ('CID99999999', gone, inactive, idle):
        r.call('query_reserve', cid, want=rF)
    reserve(r, 'M310142', 'M001', want=None)
    r.time_pass('sec', 1)
    reserve(r, 'S310141', 'S001', want=None)
    r.call('query_reserve', m)
    r.call('bind_plate_id', idle, 'S310143', want=rS)
    r.time_pass('sec', 1)
    reserve(r, 'S310143', 'S002', want=None)
    r.call('query_reserve', m)
    r.call('query_reserve', idle)
    r.call('unregister_account', idle, 'Abc123!', want=None)
    reserve(r, 'S310143', 'S003', want=rF)


@scene('test_cjl_c3115_park_statistics_params')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    for args in (('0|0', '0|0|0', '0|0|0'), ('0|0|0', '0|0', '0|0|0'), ('0|0|0', '0|0|0', '0|0'),
                 ('a|0|0', '0|0|0', '0|0|0'), ('99999999999|0|0', '0|0|0', '0|0|0')):
        stats(r, *args, want=rF)
    stats(r, '0|0|0', '0|0|0', '0|0|0')
    for args in (('1|0|0', '0|0|0', '0|0|0'), ('0|1|0', '0|0|0', '0|0|0'), ('0|0|1', '0|0|0', '0|0|0'),
                 ('0|0|0', '1|0|0', '0|0|0'), ('0|0|0', '0|1|0', '0|0|0'), ('0|0|0', '0|0|1', '0|0|0'),
                 ('0|0|0', '0|0|0', '1|0|0'), ('0|0|0', '0|0|0', '0|1|0'), ('0|0|0', '0|0|0', '0|0|1')):
        stats(r, *args, want=V)


@scene('test_cjl_c3116_occupied_spaces_params')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = enter(r, 'S310161')
    b = enter(r, 'M310162')
    r.occupy(a, 'S001')
    r.occupy(b, 'L001')
    for args in (('x', 'S001'), ('-1', 'S001'), ('99999999999', 'S001'), ('2', 'S001|X01'), ('2', 'S0001|L001')):
        r.call('check_occupied_spaces', *args, want=rF)
    for args in (('1', 'S001'), ('2', 'S001|L002'), ('2', 'S001'), ('3', 'S001|L001')):
        r.call('check_occupied_spaces', *args, want=V)
    for args in (('2', 'S001|L001'), ('2', 'L001|S001'), ('2', 'S001||L001'), ('2', 'S001|L001|S001'), ('2', ' S001 |L001')):
        r.call('check_occupied_spaces', *args, want=rS)


@scene('test_cjl_c3117_lock_quota_counting')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    members(r, ['13100000171'], [['M310171']])
    rid = reserve(r, 'M310171', 'M001', want=None)
    s1, _ = r.entry('S310172', 'G1')
    s2, _ = r.entry('M310173', 'G2')
    s3, _ = r.entry('L310174', 'G3')
    s4, _ = r.entry('M310171', 'G4')
    stats(r, '0|0|0', '0|0|1', '0|0|0')
    r.time_pass('sec', 5)
    stats(r, '0|1|0', '0|1|1', '0|1|0')
    r.occupy(s1, 'M002')
    r.occupy(s3, 'L001')
    stats(r, '0|0|0', '1|1|1', '1|0|0')
    r.occupy(s4, 'M001')
    stats(r, '0|0|0', '2|1|0', '1|0|0')
    r.release(s1)
    stats(r, '0|1|0', '1|1|0', '1|0|0')
    r.occupy(s1, 'S001', want=rF)
    r.occupy(s1, 'M003')
    stats(r, '0|0|0', '2|1|0', '1|0|0')


@scene('test_cjl_c3118_occupy_release_rules')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    r.occupy('p0000000000000001', 'S001', want=rF)
    r.release('p0000000000000001', want=rF)
    s, _ = r.arrive('M310181')
    r.occupy(s, 'M001', want=rF)
    r.release(s, want=rF)
    r.time_pass('sec', 5)
    r.release(s, want=rF)
    r.occupy(s, 'M201', want=rF)
    r.occupy(s, 'S001', want=rF)
    t = enter(r, 'M310182')
    r.occupy(t, 'M001')
    r.occupy(s, 'M001', want=rF)
    r.occupy(s, 'L001')
    r.release(s)
    r.occupy(s, 'M002', want=rF)
    r.occupy(s, 'L002')
    r.release(s)
    r.leave(s)
    r.occupy(s, 'L003', want=rF)
    r.release(s, want=rF)
    r.time_pass('sec', 5)
    r.status('M310181', s, 'outpark')
    r.check_occupied()


@scene('test_cjl_c3119_exited_session_and_reentry')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    members(r, ['13100000191'], [['S310191']])
    r.call('car_entry', 'S310191', 'G5', want=rF)
    s = enter(r, 'S310191')
    r.call('car_entry', 'S310191', 'G2', want=rF)
    r.occupy(s, 'S001')
    r.release(s)
    r.leave(s)
    r.time_pass('sec', 1)
    r.time_pass('sec', 4)
    r.status('S310191', s, 'outpark')
    r.occupy(s, 'S001', want=rF)
    r.release(s, want=rF)
    rid = reserve(r, 'S310191', 'S002', want=None)
    r.call('cancel_reserve', rid, want=rS)
    rstat(r, rid, 'cancel')
    t = enter(r, 'S310191')
    r.occupy(t, 'S003')
    stats(r, '1|0|0', '0|0|0', '0|0|0')


@scene('test_cjl_c3120_forced_exit_plain_car')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = enter(r, 'S310201')
    b = enter(r, 'M310202')
    r.occupy(a, 'S001')
    r.occupy(b, 'M001')
    r.release(a)
    stats(r, '0|1|0', '1|0|0', '0|0|0')
    r.time_pass('min', 14)
    r.time_pass('sec', 59)
    r.status('S310201', a, 'inpark')
    r.time_pass('sec', 1)
    r.status('S310201', a, 'outpark')
    r.status('M310202', b, 'parked')
    stats(r, '0|0|0', '1|0|0', '0|0|0')
    r.check_occupied()
