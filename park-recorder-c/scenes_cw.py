# -*- coding: utf-8 -*-
"""C版本洗车与计费的复杂组合场景（第二批）。
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
    return r.status(plate, sid, 'outpark', want=None)


def wash(r, plate, want=None):
    ret = r.call('take_wash_order', plate, want=want)
    m = re.match(r'E000:(\d{3})$', ret)
    return m.group(1) if m else '000'


def done(r, oid, mins=0):
    r.call('start_wash_service', oid, want=None)
    if mins:
        r.time_pass('min', mins)
    r.call('end_wash_service', oid, want=None)


def asset(r, cid):
    return r.call('query_account_asset', cid)


def pay_try(r, sid, kind, values):
    for v in values:
        if r.call('pay_parkcost', sid, kind, v) == rS:
            return v
    return None


def revenue(r, scope, kind, values):
    for v in values:
        if r.call('check_revenue', scope, kind, v) == rS:
            return v
    return None


@scene('test_cjl_cw01_price_by_take_time_not_start')
def _(r):
    m = members(r, ['13940000001'], [['S394011', 'M394012']], money=[500])[0]
    r.goto(D(2026, 3, 2, 11, 59, 30))
    a = enter(r, 'S394011')
    b = enter(r, 'M394012')
    o1 = wash(r, 'S394011')
    r.time_pass('sec', 20)
    o2 = wash(r, 'M394012')
    r.time_pass('min', 15)
    done(r, o1, 10)
    done(r, o2, 10)
    out(r, a, 'S394011')
    asset(r, m)
    out(r, b, 'M394012')
    asset(r, m)


@scene('test_cjl_cw02_adjusted_workday_peak')
def _(r):
    m = members(r, ['13940000002'], [['S394021', 'L394022']], money=[500])[0]
    r.goto(D(2026, 2, 28, 12, 30))
    a = enter(r, 'S394021')
    done(r, wash(r, 'S394021'))
    out(r, a, 'S394021')
    asset(r, m)
    r.goto(D(2026, 2, 28, 15, 0))
    b = enter(r, 'L394022')
    done(r, wash(r, 'L394022'))
    out(r, b, 'L394022')
    asset(r, m)


@scene('test_cjl_cw03_waiver_under_daily_cap')
def _(r):
    m = members(r, ['13940000003'], [['S394031', 'S394032']], money=[500])[0]
    r.goto(D(2026, 3, 2, 9, 0))
    a = enter(r, 'S394031')
    b = enter(r, 'S394032')
    r.occupy(a, 'S001')
    r.occupy(b, 'S002')
    r.time_pass('hour', 6)
    r.release(a)
    done(r, wash(r, 'S394031'), 20)
    r.occupy(a, 'S001')
    r.time_pass('hour', 2)
    r.release(a)
    r.release(b)
    out(r, a, 'S394031')
    asset(r, m)
    out(r, b, 'S394032')
    asset(r, m)


@scene('test_cjl_cw04_wash_then_park_over_midnight')
def _(r):
    m = members(r, ['13940000004'], [['S394041']], money=[500])[0]
    r.goto(D(2026, 3, 2, 16, 30))
    a = enter(r, 'S394041')
    done(r, wash(r, 'S394041'), 20)
    r.occupy(a, 'L001')
    r.goto(D(2026, 3, 3, 2, 0))
    r.release(a)
    out(r, a, 'S394041')
    asset(r, m)
    revenue(r, 'day', 'balpay', ['74.00', '89.00'])


@scene('test_cjl_cw05_temp_car_holiday_wash')
def _(r):
    r.goto(D(2026, 5, 1, 10, 0))
    a = enter(r, 'S394051')
    r.occupy(a, 'M001')
    r.time_pass('hour', 3)
    r.release(a)
    done(r, wash(r, 'S394051'), 20)
    pay_try(r, a, 'temp', ['50.00', '53.00', '54.00', '56.00', '45.00'])
    out(r, a, 'S394051')
    revenue(r, 'day', 'temppay', ['50.00', '53.00', '54.00', '56.00', '45.00'])


@scene('test_cjl_cw06_balance_short_then_temp_topup')
def _(r):
    m = members(r, ['13940000006'], [['S394061']], money=[30])[0]
    r.goto(D(2026, 3, 2, 10, 0))
    a = enter(r, 'S394061')
    done(r, wash(r, 'S394061'))
    out(r, a, 'S394061')
    asset(r, m)
    r.call('pay_parkcost', a, 'bal', '45.00')
    r.call('pay_parkcost', a, 'temp', '45.00')
    pay_try(r, a, 'temp', ['15.00'])
    out(r, a, 'S394061')
    asset(r, m)
    revenue(r, 'day', 'temppay', ['15.00', '45.00'])
    revenue(r, 'day', 'balpay', ['30.00', '0.00'])


@scene('test_cjl_cw07_member_pay_bal_with_wash')
def _(r):
    m = members(r, ['13940000007'], [['S394071']], money=[100])[0]
    r.goto(D(2026, 3, 2, 10, 0))
    a = enter(r, 'S394071')
    r.occupy(a, 'S001')
    r.time_pass('hour', 2)
    r.release(a)
    done(r, wash(r, 'S394071'), 10)
    pay_try(r, a, 'bal', ['45.00', '54.00'])
    asset(r, m)
    out(r, a, 'S394071')
    asset(r, m)


@scene('test_cjl_cw08_pay_first_then_wash')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = enter(r, 'S394081')
    r.occupy(a, 'S001')
    r.time_pass('hour', 1)
    r.release(a)
    r.call('pay_parkcost', a, 'temp', '6.00', want=rS)
    oid = wash(r, 'S394081')
    done(r, oid, 10)
    out(r, a, 'S394081')
    pay_try(r, a, 'temp', ['45.00', '51.00', '50.00', '39.00'])
    out(r, a, 'S394081')
    revenue(r, 'day', 'temppay', ['51.00', '45.00', '6.00'])


@scene('test_cjl_cw09_paid_window_while_queued')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ps = ['S394091', 'S394092', 'S394093', 'S394094']
    sids = [sid for sid, _ in r.enter(ps)]
    for p in ps[:3]:
        wash(r, p)
    r.call('pay_parkcost', sids[3], 'temp', '0.00')
    o4 = wash(r, 'S394094')
    r.time_pass('min', 15)
    r.time_pass('sec', 1)
    r.status('S394094', sids[3], 'inpark', want=None)
    r.call('query_wash_status', o4)
    r.call('query_wash_queue', 'WS1')


@scene('test_cjl_cw10_two_plates_one_balance')
def _(r):
    m = members(r, ['13940000010'], [['S394101', 'M394102']], money=[100])[0]
    r.goto(D(2026, 3, 2, 12, 30))
    a = enter(r, 'S394101')
    b = enter(r, 'M394102')
    done(r, wash(r, 'S394101'))
    done(r, wash(r, 'M394102'))
    out(r, a, 'S394101')
    asset(r, m)
    out(r, b, 'M394102')
    asset(r, m)
    pay_try(r, b, 'temp', ['60.00', '40.00', '10.00', '30.00'])
    out(r, b, 'M394102')
    asset(r, m)


@scene('test_cjl_cw11_temp_call_timeout_fee')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = enter(r, 'S394111')
    r.occupy(a, 'S001')
    wash(r, 'S394111')
    r.time_pass('min', 5)
    r.time_pass('sec', 1)
    r.release(a)
    out(r, a, 'S394111')
    pay_try(r, a, 'temp', ['10.00', '55.00', '45.00'])
    out(r, a, 'S394111')


@scene('test_cjl_cw12_temp_cancel_fee')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ps = ['S394121', 'S394122', 'S394123', 'S394124', 'S394125']
    sids = [sid for sid, _ in r.enter(ps)]
    for p in ps:
        wash(r, p)
    for o in ('001', '002', '003'):
        r.call('start_wash_service', o, want=None)
    r.call('cancel_wash_order', '004', want=rS)
    r.time_pass('min', 10)
    r.time_pass('sec', 1)
    r.call('cancel_wash_order', '005', want=rS)
    out(r, sids[3], 'S394124')
    pay_try(r, sids[3], 'temp', ['0.00', '45.00', '5.00'])
    out(r, sids[3], 'S394124')
    out(r, sids[4], 'S394125')
    pay_try(r, sids[4], 'temp', ['5.00', '50.00', '45.00'])
    out(r, sids[4], 'S394125')


@scene('test_cjl_cw13_temp_expired_at_1730_free')
def _(r):
    r.goto(D(2026, 3, 2, 16, 55))
    ps = ['S394131', 'S394132', 'S394133', 'S394134']
    sids = [sid for sid, _ in r.enter(ps)]
    for p in ps:
        wash(r, p)
    r.call('start_wash_service', '001', want=None)
    r.goto(D(2026, 3, 2, 17, 30))
    r.call('query_wash_status', '004')
    out(r, sids[3], 'S394134')
    pay_try(r, sids[3], 'temp', ['0.00', '50.00', '45.00'])
    out(r, sids[3], 'S394134')


@scene('test_cjl_cw14_temp_washing_closed_at_1800')
def _(r):
    r.goto(D(2026, 3, 2, 16, 55))
    a = enter(r, 'S394141')
    done(r, wash(r, 'S394141'))
    b = enter(r, 'S394142')
    oid = wash(r, 'S394142')
    r.call('start_wash_service', oid, want=None)
    r.goto(D(2026, 3, 2, 18, 0))
    r.call('query_wash_status', oid)
    pay_try(r, a, 'temp', ['50.00', '45.00'])
    pay_try(r, b, 'temp', ['50.00', '45.00', '0.00'])
    out(r, a, 'S394141')
    out(r, b, 'S394142')


@scene('test_cjl_cw15_moncard_last_day_wash')
def _(r):
    m = members(r, ['13940000015'], [['S394151']], money=[500])[0]
    r.goto(D(2026, 3, 2, 10, 0))
    r.call('purchase_moncard', m, 'S394151', want=rS)
    r.goto(D(2026, 3, 31, 16, 0))
    a = enter(r, 'S394151')
    r.occupy(a, 'S001')
    r.time_pass('hour', 1)
    r.release(a)
    done(r, wash(r, 'S394151'), 20)
    r.occupy(a, 'S001')
    r.goto(D(2026, 4, 1, 1, 0))
    r.release(a)
    out(r, a, 'S394151')
    asset(r, m)


@scene('test_cjl_cw16_temp_forced_exit_unpaid_wash')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = enter(r, 'S394161')
    done(r, wash(r, 'S394161'), 10)
    r.time_pass('min', 14)
    r.time_pass('sec', 59)
    r.status('S394161', a, 'inpark', want=None)
    r.time_pass('sec', 1)
    r.status('S394161', None, 'outpark', want=None)
    revenue(r, 'day', 'temppay', ['0.00', '45.00'])


@scene('test_cjl_cw17_short_stay_with_wash')
def _(r):
    m = members(r, ['13940000017'], [['S394171', 'S394172']], money=[500])[0]
    r.goto(D(2026, 3, 2, 10, 0))
    a = enter(r, 'S394171')
    r.occupy(a, 'M001')
    r.time_pass('min', 5)
    r.release(a)
    done(r, wash(r, 'S394171'), 20)
    r.occupy(a, 'M001')
    r.time_pass('min', 4)
    r.release(a)
    out(r, a, 'S394171')
    asset(r, m)
    b = enter(r, 'S394172')
    r.occupy(b, 'M002')
    r.time_pass('min', 10)
    r.release(b)
    done(r, wash(r, 'S394172'), 20)
    r.occupy(b, 'M002')
    r.time_pass('min', 10)
    r.release(b)
    out(r, b, 'S394172')
    asset(r, m)


@scene('test_cjl_cw18_pay_bal_wrong_then_right')
def _(r):
    m = members(r, ['13940000018'], [['M394181']], money=[100])[0]
    r.goto(D(2026, 3, 7, 10, 0))
    a = enter(r, 'M394181')
    done(r, wash(r, 'M394181'), 10)
    for v in ('54.00', '0.00', '59.98', '60.02'):
        r.call('pay_parkcost', a, 'bal', v)
    pay_try(r, a, 'bal', ['60.00'])
    asset(r, m)
    r.call('pay_parkcost', a, 'bal', '60.00')
    out(r, a, 'M394181')
    asset(r, m)


@scene('test_cjl_cw19_overflow_bay_type_price')
def _(r):
    m = members(r, ['13940000019'], [['M394197', 'S394198']], money=[500])[0]
    r.goto(D(2026, 3, 2, 10, 0))
    ms = [f'M3941{i:02d}' for i in range(1, 15)] + ['M394197']
    sids = r.enter(ms)
    for p in ms:
        wash(r, p)
    r.call('query_wash_queue', 'WL1')
    s = enter(r, 'S394198')
    wash(r, 'S394198')
    r.call('cancel_wash_order', '001', want=None)
    r.call('trigger_wash_balance', want=rS)
    r.call('query_wash_queue')
    for sid, plate in ((sids[-1][0], 'M394197'), (s, 'S394198')):
        r.call('cancel_wash_order', '015', want=None)
        r.note(plate)
    r.goto(D(2026, 3, 2, 17, 30))
    out(r, sids[-1][0], 'M394197')
    asset(r, m)
    out(r, s, 'S394198')
    asset(r, m)


@scene('test_cjl_cw20_month_boundary_revenue')
def _(r):
    m = members(r, ['13940000020'], [['L394201']], money=[500])[0]
    r.goto(D(2026, 3, 31, 16, 30))
    a = enter(r, 'L394201')
    done(r, wash(r, 'L394201'), 20)
    r.occupy(a, 'L001')
    r.goto(D(2026, 4, 1, 10, 0))
    r.release(a)
    out(r, a, 'L394201')
    asset(r, m)
    revenue(r, 'month', 'balpay', ['80.00', '152.00'])
    revenue(r, 'year', 'recharge', ['500.00'])
