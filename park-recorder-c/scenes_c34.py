# -*- coding: utf-8 -*-
"""C版本：洗车、预约与A/B原有计费、充值、满位规则的交叉场景。
want 为按文档推测的值，只用于录制报告对比，写入用例的是实际返回。"""
import re
from datetime import datetime as D

from record import rF, rS, scene


def members(r, phones, plates_of=(), money=0):
    out = []
    for i, phone in enumerate(phones):
        cid = r.member(phone)
        if money:
            r.call('recharge_account', cid, money, want=rS)
        for p in (plates_of[i] if i < len(plates_of) else ()):
            r.call('bind_plate_id', cid, p, want=rS)
        out.append(cid)
    return out


def enter(r, plate, want_ok=True):
    sid, _ = r.arrive(plate)
    r.time_pass('sec', 5)
    r.status(plate, sid, 'inpark' if want_ok else 'outpark')
    return sid


def wash(r, plate, want=None):
    ret = r.call('take_wash_order', plate, want=want)
    m = re.match(r'E000:(\d{3})$', ret)
    return m.group(1) if m else '000'


def out(r, sid, plate):
    r.leave(sid)
    r.time_pass('sec', 5)
    r.status(plate, sid, 'outpark')


def balance(r, cid):
    ret = r.call('query_account_asset', cid)
    m = re.search(r'-?\d+\.\d+', ret)
    return float(m.group(0)) if m else 0.0


def park_then_wash(r, plate, sid, space, hours, wash_min=20):
    r.occupy(sid, space)
    r.time_pass('hour', hours)
    r.release(sid)
    oid = wash(r, plate)
    r.call('start_wash_service', oid, want=rS)
    if wash_min:
        r.time_pass('min', wash_min)
    r.call('end_wash_service', oid, want=rS)


@scene('test_cjl_c3315_wash_waiver_on_discount_holiday')
def _(r):
    r.goto(D(2026, 4, 6, 10, 0))
    m = members(r, ['13100003315'], [['S331501', 'S331502']], money=500)[0]
    a = enter(r, 'S331501')
    b = enter(r, 'S331502')
    r.occupy(b, 'S002')
    park_then_wash(r, 'S331501', a, 'S001', 3)
    r.release(b)
    b0 = balance(r, m)
    out(r, a, 'S331501')
    b1 = balance(r, m)
    out(r, b, 'S331502')
    b2 = balance(r, m)
    r.call('check_revenue', 'day', 'balpay', f'{(b0 - b1) + (b1 - b2):.2f}', want=rS)


@scene('test_cjl_c3316_wash_space_diff_fee')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13100003316'], [['S331601', 'S331602']], money=500)[0]
    a = enter(r, 'S331601')
    b = enter(r, 'S331602')
    r.occupy(b, 'L002')
    park_then_wash(r, 'S331601', a, 'L001', 2)
    r.occupy(a, 'L001')
    r.time_pass('hour', 2)
    r.release(a)
    r.release(b)
    out(r, a, 'S331601')
    balance(r, m)
    out(r, b, 'S331602')
    balance(r, m)


@scene('test_cjl_c3317_wash_temp_car_pay')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = enter(r, 'S331701')
    r.occupy(a, 'S001')
    r.time_pass('hour', 3)
    r.release(a)
    oid = wash(r, 'S331701', want='E000:001')
    r.call('start_wash_service', oid, want=rS)
    r.call('end_wash_service', oid, want=rS)
    r.call('pay_parkcost', a, 'temp', '50.00', want=rF)
    r.call('pay_parkcost', a, 'temp', '59.00', want=rS)
    out(r, a, 'S331701')
    r.call('check_revenue', 'day', 'temppay', '59.00', want=rS)


@scene('test_cjl_c3318_wash_fee_session_bonus')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13100003318'], [['L331801']])[0]
    a = enter(r, 'L331801')
    r.call('recharge_account', m, 100, want=rS)
    balance(r, m)
    park_then_wash(r, 'L331801', a, 'L001', 6, wash_min=0)
    r.leave(a, want=None)
    r.time_pass('sec', 5)
    r.call('check_car_status', 'L331801', a, 'inpark')
    r.status('L331801', a, 'outpark', want=None)
    balance(r, m)
    r.call('pay_parkcost', a, 'temp', '4.00')
    r.call('pay_parkcost', a, 'bal', '104.00')
    r.leave(a, want=None)
    r.time_pass('sec', 5)
    r.status('L331801', a, 'outpark', want=None)
    balance(r, m)
    r.call('check_revenue', 'day', 'temppay', '4.00')
    r.call('check_revenue', 'day', 'balpay', '100.00')


@scene('test_cjl_c3319_reserve_large_space_by_small_car')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    members(r, ['13100003319'], [['S331901']])
    ret = r.call('reserve_space', 'S331901', 'L001')
    rid = ret.split(':')[1] if ret.startswith('E000:') else 'r00000000'
    r.enter([f'L3319{i:02d}' for i in range(10, 19)], [f'L{i:03d}' for i in range(2, 11)])
    r.call('check_park_statistics', '0|0|0', '0|0|0', '9|0|1', want=rS)
    enter(r, 'L331999', want_ok=False)
    m = enter(r, 'M331998')
    r.call('check_park_statistics', '0|0|0', '0|1|0', '9|0|1', want=rS)
    s = enter(r, 'S331901')
    r.call('check_reserve_status', rid, 'used', want=rS)
    r.occupy(s, 'L001')
    r.occupy(m, 'M001')
    r.call('check_park_statistics', '0|0|0', '1|0|0', '10|0|0', want=rS)
