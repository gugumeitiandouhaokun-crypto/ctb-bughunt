# -*- coding: utf-8 -*-
"""C版本跨子系统的极端组合场景。
want 为按文档推测的值，只用于录制报告对比，写入用例的是实际返回。"""
import re
from datetime import datetime as D

from record import rS, scene


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


def reserve(r, plate, space):
    ret = r.call('reserve_space', plate, space, want=None)
    m = re.match(r'E000:(r\d{8})$', ret)
    return m.group(1) if m else 'r00000000'


def wash(r, plate):
    ret = r.call('take_wash_order', plate, want=None)
    m = re.match(r'E000:(\d{3})$', ret)
    return m.group(1) if m else '000'


ALL = ('WS1', 'WS2', 'WS3', 'WM1', 'WM2', 'WL1')


@scene('test_cjl_cv01_holiday_eve_reserve_wash_cross_two_days')
def _(r):
    m = members(r, ['13970000001'], [['S397011', 'M397012']], money=[100])[0]
    r.goto(D(2026, 4, 30, 15, 50))
    a = reserve(r, 'S397011', 'L003')
    s = enter(r, 'S397011')
    t = enter(r, 'M397012')
    r.occupy(s, 'L003')
    r.occupy(t, 'L004')
    r.time_pass('min', 15)
    r.release(s)
    o = wash(r, 'S397011')
    r.call('start_wash_service', o, want=None)
    r.time_pass('min', 25)
    r.call('end_wash_service', o, want=None)
    r.occupy(s, 'L003', want=None)
    r.occupy(s, 'L005', want=None)
    r.call('recharge_account', m, '100', want=None)
    r.call('query_account_asset', m)
    r.goto(D(2026, 5, 2, 10, 0))
    r.release(s)
    r.release(t)
    out(r, s, 'S397011')
    r.call('query_account_asset', m)
    out(r, t, 'M397012')
    r.call('query_account_asset', m)
    for v in ('0.00', '50.00', '100.00', '120.00'):
        r.call('pay_parkcost', t, 'temp', v)
    out(r, t, 'M397012')
    r.call('query_account_asset', m)
    r.call('check_reserve_status', a, 'closed', want=None)


@scene('test_cjl_cv02_wash_chaos_cancel_timeout_balance')
def _(r):
    r.goto(D(2026, 3, 7, 9, 0))
    ss = [f'S3972{i:02d}' for i in range(1, 25)]
    ms = [f'M3972{i:02d}' for i in range(51, 59)]
    r.enter(ss + ms, [f'S{i:03d}' for i in range(1, 25)] + [f'M{i:03d}' for i in range(1, 9)])
    for p in ss[:12] + ms + ss[12:]:
        wash(r, p)
    r.call('start_wash_service', '001', want=None)
    r.call('start_wash_service', '013', want=None)
    for o in ('004', '007', '014', '016', '030'):
        r.call('cancel_wash_order', o, want=None)
    wq_all = lambda: [r.call('query_wash_queue', b) for b in ALL]
    wq_all()
    r.call('trigger_wash_balance', want=rS)
    wq_all()
    r.time_pass('min', 5)
    r.time_pass('sec', 1)
    wq_all()
    r.call('end_wash_service', '001', want=None)
    r.call('trigger_wash_balance', want=rS)
    wq_all()
    for o in ('002', '003', '010', '021', '032'):
        r.call('query_wash_status', o)


@scene('test_cjl_cv03_one_account_three_cars_everything')
def _(r):
    m = members(r, ['13970000003'], [['S397031', 'M397032', 'L397033']], money=[50])[0]
    r.goto(D(2026, 3, 2, 9, 0))
    r.call('recharge_account', m, '500', want=None)
    r.call('purchase_moncard', m, 'L397033', want=None)
    r.call('query_account_asset', m)
    a = reserve(r, 'M397032', 'L001')
    s = enter(r, 'S397031')
    t = enter(r, 'M397032')
    u = enter(r, 'L397033')
    r.occupy(s, 'M010')
    r.occupy(t, 'L001')
    r.occupy(u, 'L002')
    r.goto(D(2026, 3, 2, 12, 0))
    for x in (s, t, u):
        r.release(x)
    os_, ot, ou = wash(r, 'S397031'), wash(r, 'M397032'), wash(r, 'L397033')
    r.call('start_wash_service', ou, want=None)
    r.time_pass('min', 11)
    r.call('cancel_wash_order', ot, want=None)
    r.call('cancel_wash_order', os_, want=None)
    r.call('end_wash_service', ou, want=None)
    r.goto(D(2026, 3, 2, 12, 30))
    r.call('transfer_moncard', m, 'L397033', 'M397032', want=None)
    r.call('query_account_asset', m)
    for x, p in ((u, 'L397033'), (t, 'M397032'), (s, 'S397031')):
        out(r, x, p)
        r.call('query_account_asset', m)
    r.call('check_reserve_status', a, 'closed', want=None)
    r.call('check_revenue', 'day', 'balpay', '0.00', want=None)
