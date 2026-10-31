# -*- coding: utf-8 -*-
"""C版本新增子系统（预约、洗车）的复杂组合场景。
want 为按文档推测的值，只用于录制报告对比，写入用例的是实际返回。"""
import re
from datetime import datetime as D

from record import rF, rS, scene

V = 'E002:Verify_fail'


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


def enter(r, plate, want_ok=True):
    sid, _ = r.arrive(plate)
    r.time_pass('sec', 5)
    r.status(plate, sid, 'inpark' if want_ok else 'outpark')
    return sid


def out(r, sid, plate, ok=True):
    r.leave(sid, want=None)
    r.time_pass('sec', 5)
    r.status(plate, sid, 'outpark' if ok else 'inpark', want=None)


def reserve(r, plate, space, want=None):
    ret = r.call('reserve_space', plate, space, want=want)
    m = re.match(r'E000:(r\d{8})$', ret)
    return m.group(1) if m else 'r00000000'


def rstat(r, rid, st, want=rS):
    return r.call('check_reserve_status', rid, st, want=want)


def wash(r, plate, want=None):
    ret = r.call('take_wash_order', plate, want=want)
    m = re.match(r'E000:(\d{3})$', ret)
    return m.group(1) if m else '000'


def wst(r, oid, want=None):
    return r.call('query_wash_status', oid, want=want)


def wq(r, bay=None):
    return r.call('query_wash_queue', *([] if bay is None else [bay]))


def asset(r, cid):
    return r.call('query_account_asset', cid)


@scene('test_cjl_cx01_reserve_across_midnight')
def _(r):
    r.goto(D(2026, 3, 2, 23, 0))
    m = members(r, ['13930000001'], [['S393001']])[0]
    for sp in ('S001', 'S002'):
        rid = reserve(r, 'S393001', sp, want=None)
        r.call('cancel_reserve', rid, want=rS)
        r.time_pass('sec', 1)
    r.goto(D(2026, 3, 2, 23, 50))
    a = reserve(r, 'S393001', 'S003', want=None)
    reserve(r, 'S393001', 'S004', want=rF)
    r.call('check_park_statistics', '0|0|1', '0|0|0', '0|0|0', want=rS)
    r.goto(D(2026, 3, 3, 0, 0, 0))
    rstat(r, a, 'closed')
    r.call('check_park_statistics', '0|0|0', '0|0|0', '0|0|0', want=rS)
    s = enter(r, 'S393001')
    rstat(r, a, 'closed')
    r.occupy(s, 'S003')
    reserve(r, 'S393001', 'S005', want=rF)
    r.release(s)
    out(r, s, 'S393001')
    for sp in ('S005', 'S006', 'S007'):
        rid = reserve(r, 'S393001', sp, want=None)
        r.call('cancel_reserve', rid, want=rS)
        r.time_pass('sec', 1)
    reserve(r, 'S393001', 'S008', want=rF)
    r.call('query_reserve', m)


@scene('test_cjl_cx02_reserve_last_space_against_locks')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    members(r, ['13930000002'], [['S393002']])
    plist = [f'S39{i:04d}' for i in range(1, 70)]
    r.enter(plist, [f'S{i:03d}' for i in range(1, 70)])
    s = enter(r, 'S393099')
    r.call('check_park_statistics', '69|1|0', '0|0|0', '0|0|0')
    reserve(r, 'S393002', 'S070')
    reserve(r, 'S393002', 'M001')
    for st in ('69|1|1', '69|0|1'):
        r.call('check_park_statistics', st, '0|0|0', '0|0|0')
    t = enter(r, 'S393098', want_ok=None)
    for st in (('69|1|1', '0|1|0'), ('69|0|1', '0|1|0'), ('69|1|1', '0|0|0'), ('69|2|1', '0|0|0')):
        r.call('check_park_statistics', st[0], st[1], '0|0|0')
    r.occupy(t, 'S070', want=None)
    r.occupy(t, 'M002', want=None)
    r.occupy(s, 'S070', want=None)
    r.occupy(s, 'M003', want=None)
    r.call('check_occupied_spaces', '71', '|'.join([f'S{i:03d}' for i in range(1, 70)] + ['M002', 'M003']))


@scene('test_cjl_cx03_reserved_space_grab_by_others')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13930000003'], [['M393003']], money=[500])[0]
    a = reserve(r, 'M393003', 'L001', want=None)
    x = enter(r, 'S393031')
    y = enter(r, 'L393032')
    r.occupy(x, 'L001', want=rF)
    r.occupy(y, 'L001', want=rF)
    r.occupy(y, 'L002')
    s = enter(r, 'M393003')
    rstat(r, a, 'used')
    r.occupy(s, 'M001', want=rF)
    r.occupy(s, 'L001')
    r.occupy(x, 'L003')
    r.time_pass('hour', 2)
    r.release(s)
    r.release(x)
    r.release(y)
    out(r, s, 'M393003')
    asset(r, m)
    rstat(r, a, 'closed')
    r.occupy(x, 'L001')
    r.call('check_park_statistics', '0|0|0', '0|0|0', '1|2|0')


@scene('test_cjl_cx04_reserve_used_counts_daily')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    members(r, ['13930000004'], [['S393004']], money=[100])
    for sp in ('S001', 'S002', 'S003'):
        rid = reserve(r, 'S393004', sp, want=None)
        s = enter(r, 'S393004')
        rstat(r, rid, 'used')
        r.occupy(s, sp)
        r.release(s)
        out(r, s, 'S393004')
        rstat(r, rid, 'closed')
    reserve(r, 'S393004', 'S004', want=rF)
    r.goto(D(2026, 3, 3, 0, 0, 1))
    reserve(r, 'S393004', 'S004', want=None)


@scene('test_cjl_cx05_wash_order_id_next_day')
def _(r):
    r.goto(D(2026, 3, 2, 16, 50))
    ps = ['S393051', 'S393052', 'S393053', 'S393054']
    sids = [sid for sid, _ in r.enter(ps)]
    for p in ps:
        wash(r, p)
    r.call('cancel_wash_order', '004', want=rS)
    r.goto(D(2026, 3, 2, 17, 30))
    for oid in ('001', '002', '003', '004'):
        wst(r, oid)
    r.goto(D(2026, 3, 3, 9, 0))
    for oid in ('001', '004'):
        wst(r, oid, want=rF)
    for p in ps:
        r.status(p, None, 'outpark', want=None)
    s = enter(r, 'S393055')
    wash(r, 'S393055', want='E000:001')
    wst(r, '001', want='E000:queuing')
    wq(r, 'WS1')
    r.note(str(sids))


@scene('test_cjl_cx06_queued_car_leaves_park')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13930000006'], [['S393064']], money=[100])[0]
    ps = ['S393061', 'S393062', 'S393063', 'S393064', 'S393065']
    sids = [sid for sid, _ in r.enter(ps)]
    for p in ps:
        wash(r, p)
    wq(r)
    r.call('start_wash_service', '001', want=rS)
    out(r, sids[3], 'S393064')
    asset(r, m)
    wst(r, '004')
    r.call('end_wash_service', '001', want=rS)
    wq(r, 'WS1')
    wst(r, '004')
    r.time_pass('min', 5)
    r.time_pass('sec', 1)
    wst(r, '004')
    asset(r, m)
    r.call('start_wash_service', '004', want=rF)
    wq(r, 'WS1')


@scene('test_cjl_cx07_parked_car_called_timeout')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13930000007'], [['S393071']], money=[100])[0]
    s = enter(r, 'S393071')
    r.occupy(s, 'S001')
    oid = wash(r, 'S393071', want='E000:001')
    r.call('start_wash_service', oid, want=rF)
    r.time_pass('min', 5)
    wst(r, oid, want='E000:queuing')
    r.time_pass('sec', 1)
    wst(r, oid, want='E000:expired')
    r.release(s)
    r.call('start_wash_service', oid, want=rF)
    wash(r, 'S393071', want=rF)
    out(r, s, 'S393071')
    asset(r, m)


@scene('test_cjl_cx08_blacklisted_car_washing')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13930000008'], [['S393081']], money=[100])[0]
    s = enter(r, 'S393081')
    r.call('add_blacklist', 'S393081', want=rS)
    oid = wash(r, 'S393081', want=None)
    t = enter(r, 'S393082')
    o2 = wash(r, 'S393082', want=None)
    r.call('add_blacklist', 'S393082', want=rS)
    r.call('start_wash_service', o2, want=None)
    r.call('end_wash_service', o2, want=None)
    r.call('pay_parkcost', t, 'temp', '45.00', want=rS)
    out(r, t, 'S393082')
    enter(r, 'S393082', want_ok=False)
    out(r, s, 'S393081')
    asset(r, m)
    r.note(oid)


@scene('test_cjl_cx09_large_queue_full')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ls = [f'L3930{i:02d}' for i in range(91, 99)]
    sids = [sid for sid, _ in r.enter(ls)]
    for p in ls[:7]:
        wash(r, p)
    r.call('start_wash_service', '001', want=rS)
    wash(r, ls[7], want=None)
    wq(r, 'WL1')
    r.call('cancel_wash_order', '007', want=rS)
    wash(r, ls[7], want=None)
    wash(r, ls[6], want=rF)
    m = enter(r, 'M393099')
    wash(r, 'M393099', want=None)
    r.call('trigger_wash_balance', want=rS)
    wq(r)
    r.note(str(len(sids)))


@scene('test_cjl_cx10_reserved_car_washes')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13930000010'], [['S393100']], money=[500])[0]
    a = reserve(r, 'S393100', 'L001', want=None)
    s = enter(r, 'S393100')
    rstat(r, a, 'used')
    oid = wash(r, 'S393100', want='E000:001')
    r.call('start_wash_service', oid, want=rS)
    r.call('check_park_statistics', '0|0|0', '0|0|0', '0|0|1')
    r.time_pass('min', 20)
    r.call('end_wash_service', oid, want=rS)
    r.occupy(s, 'L001')
    r.time_pass('hour', 2)
    r.release(s)
    out(r, s, 'S393100')
    rstat(r, a, 'closed')
    asset(r, m)


@scene('test_cjl_cx11_moncard_large_space_wash')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13930000011'], [['S393111']], money=[500])[0]
    r.call('purchase_moncard', m, 'S393111', want=rS)
    asset(r, m)
    s = enter(r, 'S393111')
    r.occupy(s, 'L001')
    r.time_pass('hour', 2)
    r.release(s)
    oid = wash(r, 'S393111')
    r.call('start_wash_service', oid, want=rS)
    r.time_pass('min', 20)
    r.call('end_wash_service', oid, want=rS)
    r.occupy(s, 'L001')
    r.time_pass('hour', 2)
    r.release(s)
    out(r, s, 'S393111')
    asset(r, m)


@scene('test_cjl_cx12_balance_with_washing_bays')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    r.call('trigger_wash_balance', want=rS)
    wq(r)
    ps = [f'S3931{i:02d}' for i in range(20, 32)]
    r.enter(ps)
    for p in ps:
        wash(r, p)
    r.call('start_wash_service', '001', want=rS)
    r.call('start_wash_service', '002', want=rS)
    for oid in ('006', '009', '012'):
        r.call('cancel_wash_order', oid, want=rS)
    wq(r)
    r.call('trigger_wash_balance', want=rS)
    wq(r)
    for oid in ('004', '007', '010'):
        wst(r, oid)
