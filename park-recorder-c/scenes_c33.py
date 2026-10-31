# -*- coding: utf-8 -*-
"""C版本：按正式题目文档补充洗车与预约的规格校验。
want 为按文档推测的值，只用于录制报告对比，写入用例的是实际返回。"""
import re
from datetime import datetime as D
from datetime import timedelta

from record import rF, rS, scene

T = '\t'


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


def enter(r, plate):
    sid, _ = r.arrive(plate)
    r.time_pass('sec', 5)
    r.status(plate, sid, 'inpark')
    return sid


def cars(r, plate_list):
    return [sid for sid, _ in r.enter(plate_list)]


def wash(r, plate, want=None):
    ret = r.call('take_wash_order', plate, want=want)
    m = re.match(r'E000:(\d{3})$', ret)
    return m.group(1) if m else '000'


def wst(r, oid, want=None):
    return r.call('query_wash_status', oid, want=want)


def wq(r, bay=None, want=None):
    return r.call('query_wash_queue', *([] if bay is None else [bay]), want=want)


def out(r, sid, plate):
    r.leave(sid)
    r.time_pass('sec', 5)
    r.status(plate, sid, 'outpark')


def balance(r, cid):
    ret = r.call('query_account_asset', cid)
    m = re.search(r'-?\d+\.\d+', ret)
    return float(m.group(0)) if m else 0.0


def full_wash(r, plate, sid):
    oid = wash(r, plate)
    r.call('start_wash_service', oid, want=rS)
    r.call('end_wash_service', oid, want=rS)
    out(r, sid, plate)


@scene('test_cjl_c3301_wash_bay_outside_standard_spaces')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    members(r, ['13100003301'], [['S330102']])
    s = enter(r, 'S330101')
    wash(r, 'S330101', want='E000:001')
    r.call('start_wash_service', '001', want=rS)
    r.call('reserve_space', 'S330102', 'WS1', want=rF)
    r.call('reserve_space', 'S330102', 'WM1', want=rF)
    r.occupy(s, 'WS1', want=rF)
    r.call('check_park_statistics', '0|1|0', '0|0|0', '0|0|0', want=None)
    r.call('check_occupied_spaces', '0', T, want=rS)
    r.call('reserve_space', 'S330102', 'S001', want=None)
    wq(r, 'WS1', want='E000:WS1|001|[null,null,null,null,null,null]')


@scene('test_cjl_c3302_wash_once_per_day')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a, b, c, d = cars(r, ['S330201', 'S330202', 'S330203', 'S330204'])
    r.occupy(d, 'S004')
    for p in ('S330201', 'S330202', 'S330203', 'S330204'):
        wash(r, p)
    r.call('cancel_wash_order', '004', want=rS)
    wash(r, 'S330204', want=rF)
    r.time_pass('sec', 301)
    wst(r, '002', want='E000:expired')
    wash(r, 'S330202', want=rF)
    r.goto(D(2026, 3, 3, 10, 0))
    r.status('S330204', d, 'parked')
    wash(r, 'S330204', want='E000:001')


@scene('test_cjl_c3303_wash_bay_daily_limit_20')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    for i in range(1, 21):
        p = f'L3303{i:02d}'
        s = enter(r, p)
        oid = wash(r, p, want=f'E000:{i:03d}')
        r.call('start_wash_service', oid, want=rS)
        r.call('end_wash_service', oid, want=rS)
        r.call('pay_parkcost', s, 'temp', '72.00', want=rS)
        r.leave(s)
    r.time_pass('sec', 5)
    enter(r, 'L330321')
    wash(r, 'L330321', want=rF)
    enter(r, 'M330322')
    wash(r, 'M330322', want='E000:021')
    wq(r)


@scene('test_cjl_c3304_wash_price_medium_large')
def _(r):
    m = members(r, ['13100003304'], [['M330401', 'L330402']], money=500)[0]
    for day in (D(2026, 3, 2, 10, 0), D(2026, 3, 7, 10, 0)):
        r.goto(day)
        for p in ('M330401', 'L330402'):
            full_wash(r, p, enter(r, p))
            balance(r, m)


@scene('test_cjl_c3305_wash_peak_boundaries')
def _(r):
    ma, mb = members(r, ['13100003305', '13100003306'],
                     [['S330501', 'S330502', 'S330503'], ['S330504', 'S330505', 'S330506']], money=500)
    times = [(2, 11, 59, 59), (3, 12, 0, 0), (4, 13, 59, 59), (5, 14, 0, 0), (6, 15, 59, 59), (9, 16, 0, 0)]
    for i, (d, h, mi, se) in enumerate(times):
        p = f'S33050{i + 1}'
        at = D(2026, 3, d, h, mi, se)
        r.goto(at - timedelta(seconds=30))
        s = enter(r, p)
        r.goto(at)
        full_wash(r, p, s)
        balance(r, ma if i < 3 else mb)


@scene('test_cjl_c3306_wash_cancel_and_timeout_fee')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ma, mb = members(r, ['13100003306', '13100003307'], [['S330604', 'S330607'], ['S330602']], money=100)
    sids = cars(r, [f'S33060{i}' for i in range(1, 8)])
    for i in range(1, 8):
        wash(r, f'S33060{i}', want=f'E000:{i:03d}')
    r.call('start_wash_service', '001', want=rS)
    r.time_pass('sec', 301)
    wst(r, '002', want='E000:expired')
    r.time_pass('sec', 299)
    r.call('cancel_wash_order', '004', want=rS)
    r.time_pass('sec', 1)
    r.call('cancel_wash_order', '007', want=rS)
    for i in (2, 4, 7):
        out(r, sids[i - 1], f'S33060{i}')
    balance(r, ma)
    balance(r, mb)


@scene('test_cjl_c3307_wash_parking_waiver_moncard')
def _(r):
    r.goto(D(2026, 3, 2, 9, 30))
    m = members(r, ['13100003307'], [['S330701', 'S330702']], money=500)[0]
    r.call('recharge_account', m, 500, want=rS)
    r.call('purchase_moncard', m, 'S330701', want=rS)
    b0 = balance(r, m)
    a, b = cars(r, ['S330701', 'S330702'])
    r.occupy(a, 'S001')
    r.occupy(b, 'S002')
    r.time_pass('hour', 1)
    for s in (a, b):
        r.release(s)
    wash(r, 'S330701', want='E000:001')
    wash(r, 'S330702', want='E000:002')
    r.call('start_wash_service', '001', want=rS)
    r.call('start_wash_service', '002', want=rS)
    r.time_pass('min', 20)
    r.call('end_wash_service', '001', want=rS)
    r.call('end_wash_service', '002', want=rS)
    r.occupy(a, 'S001')
    r.occupy(b, 'S002')
    r.time_pass('hour', 2)
    for s, p in ((a, 'S330701'), (b, 'S330702')):
        r.release(s)
        out(r, s, p)
        balance(r, m)
    r.call('check_revenue', 'day', 'balpay', f'{b0 - balance(r, m):.2f}', want=rS)


@scene('test_cjl_c3308_wash_parking_on_holiday')
def _(r):
    r.goto(D(2026, 5, 1, 9, 30))
    m = members(r, ['13100003308'], [['S330801']], money=500)[0]
    s = enter(r, 'S330801')
    r.occupy(s, 'S001')
    r.time_pass('hour', 2)
    r.release(s)
    full_wash(r, 'S330801', s)
    balance(r, m)


@scene('test_cjl_c3309_wash_fee_temp_pay_and_revenue')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m0, m1 = members(r, ['13100003309', '13100003310'], [['S330901'], ['S330902']])
    r.call('recharge_account', m1, 100, want=rS)
    a = enter(r, 'S330901')
    b = enter(r, 'S330902')
    for p in ('S330901', 'S330902'):
        oid = wash(r, p)
        r.call('start_wash_service', oid, want=rS)
        r.call('end_wash_service', oid, want=rS)
    out(r, b, 'S330902')
    balance(r, m1)
    r.leave(a, want=None)
    r.call('check_car_status', 'S330901', a, 'exiting')
    r.time_pass('sec', 5)
    r.call('check_car_status', 'S330901', a, 'inpark')
    balance(r, m0)
    r.call('pay_parkcost', a, 'temp', '45.00', want=rS)
    r.call('check_car_status', 'S330901', a, 'inpark')
    out(r, a, 'S330901')
    balance(r, m0)
    r.call('check_revenue', 'day', 'temppay', '45.00', want=rS)
    r.call('check_revenue', 'day', 'balpay', '45.00', want=rS)


@scene('test_cjl_c3310_wash_fee_cross_day_revenue')
def _(r):
    r.goto(D(2026, 3, 2, 16, 30))
    m = members(r, ['13100003310'], [['S331001']], money=500)[0]
    s = enter(r, 'S331001')
    wash(r, 'S331001', want='E000:001')
    r.call('start_wash_service', '001', want=rS)
    r.call('end_wash_service', '001', want=rS)
    r.occupy(s, 'S001')
    b0 = balance(r, m)
    r.goto(D(2026, 3, 3, 10, 0))
    r.release(s)
    out(r, s, 'S331001')
    cost = b0 - balance(r, m)
    r.call('check_revenue', 'day', 'balpay', f'{cost:.2f}', want=rS)
    r.call('check_revenue', 'month', 'balpay', f'{cost:.2f}', want=rS)


@scene('test_cjl_c3311_wash_transition_and_close')
def _(r):
    r.goto(D(2026, 3, 2, 16, 55))
    m = members(r, ['13100003311'], [['S331104']], money=100)[0]
    cars(r, ['S331101', 'S331102', 'S331103', 'S331104'])
    for p in ('S331101', 'S331102', 'S331103', 'S331104'):
        wash(r, p)
    r.call('start_wash_service', '002', want=rS)
    r.goto(D(2026, 3, 2, 17, 0, 0))
    wst(r, '001', want='E000:queuing')
    r.time_pass('sec', 6)
    wst(r, '001', want='E000:expired')
    r.time_pass('min', 2)
    r.call('start_wash_service', '004', want=rS)
    r.goto(D(2026, 3, 2, 17, 59, 59))
    wst(r, '004', want='E000:washing')
    r.time_pass('sec', 1)
    for oid in ('001', '002', '003', '004'):
        wst(r, oid, want='E000:closed')
    r.time_pass('min', 15)
    balance(r, m)
    r.goto(D(2026, 3, 2, 23, 59, 59))
    wst(r, '004', want='E000:closed')
    r.time_pass('sec', 1)
    wst(r, '004', want=rF)


@scene('test_cjl_c3312_wash_balance_stack_insert')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ps = [f'S3312{i:02d}' for i in range(1, 19)]
    cars(r, ps)
    for p in ps:
        wash(r, p)
    for oid in ('005', '008', '011', '014'):
        r.call('cancel_wash_order', oid, want=rS)
    wq(r)
    r.call('trigger_wash_balance', want=rS)
    wq(r)
    r.call('trigger_wash_balance', want=rS)
    wq(r)


@scene('test_cjl_c3314_wash_cancel_free_settlement')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13100003314'], [['S331404']], money=100)[0]
    sids = cars(r, ['S331401', 'S331402', 'S331403', 'S331404'])
    for p in ('S331401', 'S331402', 'S331403', 'S331404'):
        wash(r, p)
    r.call('cancel_wash_order', '004', want=rS)
    wst(r, '004', want='E000:canceled')
    out(r, sids[3], 'S331404')
    balance(r, m)
    r.call('check_revenue', 'day', 'balpay', '0.00')


@scene('test_cjl_c3313_reserved_car_normal_parking_fee')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13100003313'], [['S331301', 'S331302']], money=500)[0]
    rid = r.call('reserve_space', 'S331301', 'S001')
    a = enter(r, 'S331301')
    b = enter(r, 'S331302')
    r.occupy(a, 'S001')
    r.occupy(b, 'S002')
    r.time_pass('hour', 2)
    b0 = balance(r, m)
    r.release(a)
    r.release(b)
    out(r, a, 'S331301')
    b1 = balance(r, m)
    out(r, b, 'S331302')
    b2 = balance(r, m)
    r.call('check_revenue', 'day', 'balpay', f'{(b0 - b1) + (b1 - b2):.2f}', want=rS)
