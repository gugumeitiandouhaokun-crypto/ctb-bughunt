# -*- coding: utf-8 -*-
"""C版本预约与洗车子系统的组合场景（第三批）。
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


def wst(r, *oids):
    for o in oids:
        r.call('query_wash_status', o, want=None)


def wq(r, *bays):
    for b in bays:
        r.call('query_wash_queue', b)


def asset(r, cid):
    return r.call('query_account_asset', cid)


ALL = ('WS1', 'WS2', 'WS3', 'WM1', 'WM2', 'WL1')


@scene('test_cjl_cy01_reserve_expire_boundary')
def _(r):
    members(r, ['13950000001'], [['S395011']])
    r.goto(D(2026, 3, 2, 10, 0))
    a = reserve(r, 'S395011', 'S001')
    r.goto(D(2026, 3, 2, 10, 29, 59))
    rstat(r, a, 'locked', 'expired')
    r.call('check_park_statistics', '0|0|1', '0|0|0', '0|0|0')
    r.time_pass('sec', 1)
    rstat(r, a, 'locked', 'expired')
    r.call('check_park_statistics', '0|0|0', '0|0|0', '0|0|0')
    r.call('cancel_reserve', a, want=rF)
    b = reserve(r, 'S395011', 'S001')
    s = enter(r, 'S395011')
    rstat(r, b, 'used')
    r.goto(D(2026, 3, 2, 11, 0))
    rstat(r, b, 'used', 'expired')
    r.occupy(s, 'S001')
    r.goto(D(2026, 3, 3, 0, 0))
    rstat(r, a, 'closed')
    rstat(r, b, 'used', 'closed')


@scene('test_cjl_cy02_reserved_car_enters_never_parks')
def _(r):
    m = members(r, ['13950000002'], [['M395021']], money=[100])[0]
    r.goto(D(2026, 3, 2, 10, 0))
    a = reserve(r, 'M395021', 'M001')
    s = enter(r, 'M395021')
    r.call('cancel_reserve', a, want=rF)
    r.call('check_park_statistics', '0|0|0', '0|0|1', '0|0|0')
    r.call('check_park_statistics', '0|0|0', '1|0|0', '0|0|0')
    x = enter(r, 'M395022')
    r.occupy(x, 'M001', want=None)
    r.time_pass('min', 14)
    r.status('M395021', s, 'inpark', want=None)
    r.time_pass('min', 1)
    r.status('M395021', None, 'outpark', want=None)
    rstat(r, a, 'used', 'closed')
    r.occupy(x, 'M001', want=None)
    r.call('check_park_statistics', '0|0|0', '1|0|0', '0|0|0')
    asset(r, m)


@scene('test_cjl_cy03_reserve_large_space_diff_fee')
def _(r):
    m = members(r, ['13950000003'], [['S395031']], money=[500])[0]
    r.goto(D(2026, 3, 2, 8, 0))
    a = reserve(r, 'S395031', 'L001')
    s = enter(r, 'S395031')
    r.occupy(s, 'L001')
    r.goto(D(2026, 3, 2, 20, 0))
    r.release(s)
    out(r, s, 'S395031')
    asset(r, m)
    rstat(r, a, 'closed')
    reserve(r, 'S395031', 'L001')


@scene('test_cjl_cy04_reserve_then_blacklist_and_unbind')
def _(r):
    m = members(r, ['13950000004'], [['S395041', 'S395042']])[0]
    r.goto(D(2026, 3, 2, 10, 0))
    a = reserve(r, 'S395041', 'S001')
    r.time_pass('sec', 1)
    b = reserve(r, 'S395042', 'S002')
    r.call('unbind_plate_id', m, 'S395042', want=None)
    rstat(r, b, 'locked', 'cancel')
    r.call('check_park_statistics', '0|0|2', '0|0|0', '0|0|0')
    s = enter(r, 'S395042')
    r.occupy(s, 'S002', want=None)
    r.occupy(s, 'S003', want=None)
    rstat(r, b, 'locked', 'used')
    r.call('cancel_reserve', a, want=None)
    r.call('cancel_reserve', a, want=None)
    reserve(r, 'S395041', 'S002', want=None)
    reserve(r, 'S395041', 'S004', want=None)
    reserve(r, 'S395041', 'S005', want=None)
    r.call('query_reserve', m)


@scene('test_cjl_cy05_reserve_occupied_or_wash_bay')
def _(r):
    members(r, ['13950000005'], [['L395051']])
    r.goto(D(2026, 3, 2, 10, 0))
    x = enter(r, 'S395052')
    r.occupy(x, 'L001')
    for sp in ('L001', 'WL1', 'M001', 'S001', 'L041', 'L000', 'l002', ''):
        reserve(r, 'L395051', sp, want=None)
    a = reserve(r, 'L395051', 'L002')
    r.release(x)
    r.occupy(x, 'L002', want=None)
    rstat(r, a, 'locked')


@scene('test_cjl_cy06_take_order_hour_boundaries')
def _(r):
    r.goto(D(2026, 3, 2, 8, 59, 50))
    ps = ['S395061', 'S395062', 'S395063', 'S395064']
    sids = [sid for sid, _ in r.enter(ps, ['S001', 'S002', 'S003', 'S004'])]
    r.goto(D(2026, 3, 2, 8, 59, 59))
    wash(r, 'S395061')
    r.time_pass('sec', 1)
    wash(r, 'S395061')
    r.goto(D(2026, 3, 2, 16, 59, 59))
    wash(r, 'S395062')
    r.time_pass('sec', 1)
    wash(r, 'S395063')
    r.time_pass('sec', 1)
    wash(r, 'S395064')
    wq(r, 'WS1', 'WS2', 'WS3')
    for p, sid in zip(ps, sids):
        r.status(p, sid, 'inpark', want=None)


@scene('test_cjl_cy07_illegal_wash_transitions')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ps = ['S395071', 'S395072', 'S395073', 'S395074']
    r.enter(ps)
    for p in ps:
        wash(r, p)
    r.call('end_wash_service', '001', want=rF)
    r.call('start_wash_service', '004', want=rF)
    r.call('start_wash_service', '001', want=rS)
    r.call('start_wash_service', '001', want=rF)
    r.call('cancel_wash_order', '001', want=rF)
    r.call('end_wash_service', '001', want=rS)
    r.call('end_wash_service', '001', want=rF)
    r.call('start_wash_service', '001', want=rF)
    wst(r, '001', '004')
    r.call('start_wash_service', '004', want=None)
    r.call('cancel_wash_order', '004', want=rF)
    for o in ('000', '005', '01', 'abc', '999'):
        r.call('start_wash_service', o, want=rF)
        r.call('query_wash_status', o, want=None)


@scene('test_cjl_cy08_cancel_600s_boundary_fee')
def _(r):
    m = members(r, ['13950000008'], [['S395085', 'S395086']], money=[100])[0]
    r.goto(D(2026, 3, 2, 10, 0))
    ps = ['S395081', 'S395082', 'S395083', 'S395085', 'S395086']
    sids = [sid for sid, _ in r.enter(ps)]
    for p in ps:
        wash(r, p)
    for o in ('001', '002', '003'):
        r.call('start_wash_service', o, want=rS)
    r.goto(D(2026, 3, 2, 10, 10, 5))
    r.call('cancel_wash_order', '004', want=rS)
    r.time_pass('sec', 1)
    r.call('cancel_wash_order', '005', want=rS)
    wash(r, 'S395085', want=rF)
    out(r, sids[3], 'S395085')
    asset(r, m)
    out(r, sids[4], 'S395086')
    asset(r, m)


@scene('test_cjl_cy09_balance_stage1_ws_internal')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ps = [f'S3951{i:02d}' for i in range(1, 16)]
    r.enter(ps)
    for p in ps:
        wash(r, p)
    for o in ('004', '007', '010', '013'):
        r.call('cancel_wash_order', o, want=None)
    wq(r, *ALL[:3])
    r.call('trigger_wash_balance', want=rS)
    wq(r, *ALL[:3])
    r.call('trigger_wash_balance', want=rS)
    wq(r, *ALL[:3])


@scene('test_cjl_cy10_balance_stage3_ws_overflow')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ps = [f'S3952{i:02d}' for i in range(1, 22)]
    r.enter(ps)
    for p in ps:
        wash(r, p)
    for o in ('001', '002', '003'):
        r.call('start_wash_service', o, want=None)
    wq(r, *ALL)
    r.call('trigger_wash_balance', want=rS)
    wq(r, *ALL)
    r.call('trigger_wash_balance', want=rS)
    wq(r, *ALL)


@scene('test_cjl_cy11_balance_stage2_wl_backfill')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ps = [f'M3953{i:02d}' for i in range(1, 19)]
    r.enter(ps)
    for p in ps:
        wash(r, p)
    for o in ('001', '002', '003'):
        r.call('start_wash_service', o, want=None)
    wq(r, *ALL)
    for o in ('004', '006', '008', '010'):
        r.call('cancel_wash_order', o, want=None)
    wq(r, 'WM1', 'WM2', 'WL1')
    r.call('trigger_wash_balance', want=rS)
    wq(r, *ALL)


@scene('test_cjl_cy12_wash_status_after_close_and_next_day')
def _(r):
    r.goto(D(2026, 3, 2, 16, 50))
    ps = ['S395401', 'S395402', 'S395403', 'S395404', 'S395405']
    r.enter(ps)
    for p in ps:
        wash(r, p)
    r.call('start_wash_service', '001', want=rS)
    r.call('cancel_wash_order', '005', want=rS)
    r.goto(D(2026, 3, 2, 17, 29, 59))
    wst(r, '001', '002', '004', '005')
    r.time_pass('sec', 1)
    wst(r, '001', '002', '004', '005')
    r.call('start_wash_service', '004', want=None)
    r.call('cancel_wash_order', '004', want=None)
    r.goto(D(2026, 3, 2, 17, 59, 59))
    wst(r, '001')
    r.time_pass('sec', 1)
    wst(r, '001', '002', '005')
    r.call('end_wash_service', '001', want=rF)
    wq(r, *ALL)
    r.goto(D(2026, 3, 3, 0, 0))
    wst(r, '001')
    r.goto(D(2026, 3, 3, 9, 0))
    wash(r, 'S395404')
    wash(r, 'S395401')
    wq(r, 'WS1')


@scene('test_cjl_cy13_reserved_car_wash_and_cancel_reserve')
def _(r):
    m = members(r, ['13950000013'], [['M395131']], money=[500])[0]
    r.goto(D(2026, 3, 2, 12, 0))
    a = reserve(r, 'M395131', 'M001')
    s = enter(r, 'M395131')
    oid = wash(r, 'M395131')
    r.call('cancel_reserve', a, want=None)
    r.call('start_wash_service', oid, want=None)
    r.occupy(s, 'M001', want=None)
    r.call('end_wash_service', oid, want=None)
    r.occupy(s, 'M001', want=None)
    r.call('check_park_statistics', '0|0|0', '0|1|0', '0|0|0')
    r.call('check_park_statistics', '0|0|0', '0|0|1', '0|0|0')
    r.time_pass('hour', 1)
    r.release(s)
    out(r, s, 'M395131')
    asset(r, m)
    rstat(r, a, 'used', 'closed', 'cancel')


@scene('test_cjl_cy14_reserve_full_lot_entry')
def _(r):
    members(r, ['13950000014'], [['L395141']], money=[100])
    r.goto(D(2026, 3, 2, 10, 0))
    a = reserve(r, 'L395141', 'L010')
    plist = [f'L3952{i:02d}' for i in range(1, 10)]
    r.enter(plist, [f'L{i:03d}' for i in range(1, 10)])
    r.call('check_park_statistics', '0|0|0', '0|0|0', '9|0|1')
    x = enter(r, 'L395199')
    s = enter(r, 'L395141')
    rstat(r, a, 'used')
    r.occupy(s, 'L010')
    r.call('check_park_statistics', '0|0|0', '0|0|0', '10|0|0')
