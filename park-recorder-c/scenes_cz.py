# -*- coding: utf-8 -*-
"""C版本预约与洗车子系统的组合场景（第四批）。
want 为按文档推测的值，只用于录制报告对比，写入用例的是实际返回。"""
import re
from datetime import datetime as D

from record import rF, rS, scene


def members(r, phones, plates_of=(), money=()):
    out = []
    for i, phone in enumerate(phones):
        cid = r.member(phone)
        for v in money:
            r.call('recharge_account', cid, v, want=rS)
        for p in (plates_of[i] if i < len(plates_of) else ()):
            r.call('bind_plate_id', cid, p, want=rS)
        out.append(cid)
    return out


def enter(r, plate):
    sid, _ = r.arrive(plate)
    r.time_pass('sec', 5)
    r.status(plate, sid, 'inpark', want=None)
    return sid


def out(r, sid, plate):
    r.leave(sid, want=None)
    r.time_pass('sec', 5)
    r.status(plate, sid, 'outpark', want=None)


def reserve(r, plate, space, want=None):
    ret = r.call('reserve_space', plate, space, want=want)
    m = re.match(r'E000:(r\d{8})$', ret)
    return m.group(1) if m else 'r00000000'


def rstat(r, rid, *states):
    for st in states:
        r.call('check_reserve_status', rid, st, want=None)


def wash(r, plate, want=None):
    ret = r.call('take_wash_order', plate, want=want)
    m = re.match(r'E000:(\d{3})$', ret)
    return m.group(1) if m else '000'


def wq(r, *bays):
    for b in bays:
        r.call('query_wash_queue', b)


def asset(r, cid):
    return r.call('query_account_asset', cid)


ALL = ('WS1', 'WS2', 'WS3', 'WM1', 'WM2', 'WL1')


@scene('test_cjl_cz01_balance_stage1_wm_internal')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ps = [f'M3961{i:02d}' for i in range(1, 13)]
    r.enter(ps)
    for p in ps:
        wash(r, p)
    for o in ('003', '005', '007', '009'):
        r.call('cancel_wash_order', o, want=None)
    wq(r, 'WM1', 'WM2')
    r.call('trigger_wash_balance', want=rS)
    wq(r, 'WM1', 'WM2', 'WL1')


@scene('test_cjl_cz02_reserve_quota_expire_cancel_mix')
def _(r):
    members(r, ['13960000002'], [['S396021']])
    r.goto(D(2026, 3, 2, 22, 0))
    a = reserve(r, 'S396021', 'S001')
    r.time_pass('min', 31)
    rstat(r, a, 'expired', 'locked')
    b = reserve(r, 'S396021', 'S001')
    r.time_pass('min', 31)
    c = reserve(r, 'S396021', 'S002')
    r.call('cancel_reserve', c, want=rS)
    reserve(r, 'S396021', 'S003', want=rF)
    r.goto(D(2026, 3, 3, 0, 0))
    rstat(r, a, 'closed')
    rstat(r, b, 'closed')
    rstat(r, c, 'closed')
    d = reserve(r, 'S396021', 'S003')
    rstat(r, d, 'locked')


@scene('test_cjl_cz03_reserve_before_midnight_enter_after')
def _(r):
    m = members(r, ['13960000003'], [['M396031']], money=[100])[0]
    r.goto(D(2026, 3, 2, 23, 45))
    a = reserve(r, 'M396031', 'M001')
    r.call('check_park_statistics', '0|0|0', '0|0|1', '0|0|0')
    r.goto(D(2026, 3, 3, 0, 5))
    rstat(r, a, 'locked', 'expired', 'closed')
    r.call('check_park_statistics', '0|0|0', '0|0|0', '0|0|0')
    s = enter(r, 'M396031')
    rstat(r, a, 'used', 'closed')
    r.occupy(s, 'M001', want=None)
    r.time_pass('hour', 2)
    r.release(s)
    out(r, s, 'M396031')
    asset(r, m)


@scene('test_cjl_cz04_two_members_race_same_space')
def _(r):
    members(r, ['13960000004', '13960000005'], [['S396041'], ['S396042']])
    r.goto(D(2026, 3, 2, 10, 0))
    a = reserve(r, 'S396041', 'M002')
    b = reserve(r, 'S396042', 'M002', want=rF)
    r.call('cancel_reserve', a, want=rS)
    b = reserve(r, 'S396042', 'M002', want=rS)
    s = enter(r, 'S396041')
    r.occupy(s, 'M002', want=rF)
    t = enter(r, 'S396042')
    r.occupy(t, 'M002')
    r.release(t)
    r.occupy(s, 'M002', want=None)
    rstat(r, b, 'used')


@scene('test_cjl_cz05_reserved_washed_never_parked')
def _(r):
    m = members(r, ['13960000006'], [['L396061']], money=[500])[0]
    r.goto(D(2026, 3, 2, 14, 30))
    a = reserve(r, 'L396061', 'L005')
    s = enter(r, 'L396061')
    oid = wash(r, 'L396061')
    r.call('start_wash_service', oid, want=rS)
    r.time_pass('min', 20)
    r.call('end_wash_service', oid, want=rS)
    r.call('check_park_statistics', '0|0|0', '0|0|0', '0|0|1')
    out(r, s, 'L396061')
    asset(r, m)
    rstat(r, a, 'closed')
    r.call('check_park_statistics', '0|0|0', '0|0|0', '0|0|0')


@scene('test_cjl_cz06_reserve_by_invalid_owner')
def _(r):
    m = members(r, ['13960000007'], [['S396071']])[0]
    r.goto(D(2026, 3, 2, 10, 0))
    reserve(r, 'S396079', 'S001')
    s = enter(r, 'S396071')
    reserve(r, 'S396071', 'S001')
    r.occupy(s, 'S002')
    r.release(s)
    out(r, s, 'S396071')
    reserve(r, 'S396071', 'S001')


@scene('test_cjl_cz07_wash_while_occupying_space')
def _(r):
    m = members(r, ['13960000009'], [['S396091']], money=[500])[0]
    r.goto(D(2026, 3, 2, 10, 0))
    s = enter(r, 'S396091')
    r.occupy(s, 'S001')
    r.time_pass('hour', 1)
    oid = wash(r, 'S396091')
    r.call('start_wash_service', oid, want=None)
    r.time_pass('min', 30)
    r.call('end_wash_service', oid, want=None)
    r.time_pass('hour', 2)
    r.release(s)
    out(r, s, 'S396091')
    asset(r, m)


@scene('test_cjl_cz08_s_order_spills_to_wl')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ps = [f'S3962{i:02d}' for i in range(1, 37)]
    r.enter(ps)
    for p in ps:
        wash(r, p)
    wq(r, *ALL)
    wash(r, 'L396299')
    x = enter(r, 'L396299')
    wash(r, 'L396299')
    wq(r, 'WL1')


@scene('test_cjl_cz09_expired_then_retake_and_balance')
def _(r):
    m = members(r, ['13960000010'], [['S396104']], money=[100])[0]
    r.goto(D(2026, 3, 2, 10, 0))
    ps = ['S396101', 'S396102', 'S396103', 'S396104']
    sids = [sid for sid, _ in r.enter(ps, ['S001', 'S002', 'S003', 'S004'])]
    for p in ps:
        wash(r, p)
    r.call('start_wash_service', '001', want=rS)
    r.call('end_wash_service', '001', want=rS)
    r.time_pass('min', 5)
    r.time_pass('sec', 1)
    for o in ('002', '003', '004'):
        r.call('query_wash_status', o)
    wash(r, 'S396104', want=rF)
    r.call('trigger_wash_balance', want=rS)
    wq(r, *ALL)
    r.release(sids[3])
    out(r, sids[3], 'S396104')
    asset(r, m)


@scene('test_cjl_cz10_balance_after_1730_expiry')
def _(r):
    r.goto(D(2026, 3, 2, 16, 50))
    ps = [f'S3963{i:02d}' for i in range(1, 11)]
    r.enter(ps)
    for p in ps:
        wash(r, p)
    for o in ('001', '002', '003'):
        r.call('start_wash_service', o, want=None)
    r.goto(D(2026, 3, 2, 17, 30))
    wq(r, *ALL[:3])
    r.call('trigger_wash_balance', want=None)
    wq(r, *ALL)
    r.call('end_wash_service', '001', want=None)
    wq(r, 'WS1')
