# -*- coding: utf-8 -*-
"""B版本功能（在C版本jar上）：充值、转让、月卡、计费、支付、营收、黑名单。
want 为按文档推测的值，只用于录制报告对比，写入用例的是实际返回。"""
import re
from datetime import datetime as D

from record import rF, rS, scene

PWD = 'Abc123!'


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
    r.leave(sid)
    r.time_pass('sec', 5)
    r.status(plate, sid, 'outpark' if ok else 'inpark')


def park(r, plate, space, hours=0, mins=0):
    sid = enter(r, plate)
    r.occupy(sid, space)
    if hours:
        r.time_pass('hour', hours)
    if mins:
        r.time_pass('min', mins)
    r.release(sid)
    return sid


def asset(r, cid, want=None):
    return r.call('query_account_asset', cid, want=want)


@scene('test_cjl_cb01_recharge_tiers_and_cap')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = members(r, ['13901000001'])[0]
    for v in (0, -1, 51, 99, 101, 200, 499, 501, 1000):
        r.call('recharge_account', a, v, want=rF)
    for v in (1, 50, 100, 500):
        r.call('recharge_account', a, v, want=rS)
    asset(r, a, want='E000:791.00|Null:000|Null:000|Null:000')
    for _ in range(6):
        r.call('recharge_account', a, 500, want=rS)
    asset(r, a, want='E000:4511.00|Null:000|Null:000|Null:000')
    r.call('recharge_account', a, 500, want=rF)
    r.call('recharge_account', a, 100, want=rS)
    for _ in range(7):
        r.call('recharge_account', a, 50, want=rS)
    r.call('recharge_account', a, 50, want=None)
    r.call('recharge_account', a, 19, want=None)
    asset(r, a)
    r.call('recharge_account', a, 1, want=None)
    i, _ = r.register('13901000002', PWD)
    r.call('recharge_account', i, 100, want=rF)
    r.call('recharge_account', 'CID00000000', 100, want=rF)
    r.call('recharge_account', 'CID123', 100, want=rF)
    s, cid = r.entry('S39011', 'G1')
    r.call('recharge_account', cid, 100, want=rF)
    r.call('check_revenue', 'day', 'recharge', '3951.00')


@scene('test_cjl_cb02_transfer_account')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a, b = members(r, ['13902000001', '13902000002'], money=[100])
    i, _ = r.register('13902000003', PWD)
    for v in (0, -1, 121):
        r.call('transfer_account', a, b, v, want=rF)
    r.call('transfer_account', a, a, 10, want=None)
    r.call('transfer_account', a, i, 10, want=rF)
    r.call('transfer_account', a, 'CID00000000', 10, want=rF)
    r.call('transfer_account', i, a, 10, want=rF)
    r.call('transfer_account', a, b, 120, want=rS)
    asset(r, a, want='E000:0.00|Null:000|Null:000|Null:000')
    asset(r, b, want='E000:240.00|Null:000|Null:000|Null:000')
    r.call('transfer_account', a, b, 1, want=rF)
    r.call('recharge_account', a, 500, want=rS)
    r.call('transfer_account', b, a, 240, want=None)
    asset(r, a)
    r.call('check_revenue', 'day', 'recharge', '700.00', want=rS)


@scene('test_cjl_cb03_moncard_purchase')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a, b = members(r, ['13903000001', '13903000002'], [['S39031', 'M39032', 'L39033'], ['S39034']])
    r.call('purchase_moncard', a, 'S39031', want=rF)
    r.call('recharge_account', a, 500, want=rS)
    r.call('recharge_account', a, 500, want=rS)
    r.call('recharge_account', a, 500, want=rS)
    r.call('purchase_moncard', a, 'S39034', want=rF)
    r.call('purchase_moncard', a, 'S39099', want=rF)
    r.call('purchase_moncard', 'CID00000000', 'S39031', want=rF)
    r.call('purchase_moncard', a, 'S39031', want=rS)
    asset(r, a, want='E000:1460.00|S39031:030|M39032:000|L39033:000')
    r.call('purchase_moncard', a, 'M39032', want=rS)
    r.call('purchase_moncard', a, 'L39033', want=rS)
    asset(r, a, want='E000:60.00|S39031:030|M39032:030|L39033:030')
    r.call('purchase_moncard', a, 'S39031', want=rF)
    r.call('check_revenue', 'day', 'monthcard', '1800.00', want=rS)
    r.time_pass('day', 1)
    asset(r, a)
    r.goto(D(2026, 3, 31, 23, 59, 59))
    asset(r, a)
    r.time_pass('sec', 1)
    asset(r, a)


@scene('test_cjl_cb04_moncard_limit_and_year_end')
def _(r):
    r.goto(D(2026, 1, 5, 10, 0))
    a = members(r, ['13904000001'], [['S39041', 'S39042']], money=[500] * 8)[0]
    for _ in range(12):
        r.call('purchase_moncard', a, 'S39041', want=rS)
    asset(r, a)
    r.call('purchase_moncard', a, 'S39041', want=rF)
    r.goto(D(2026, 12, 10, 10, 0))
    r.call('recharge_account', a, 500, want=rS)
    r.call('purchase_moncard', a, 'S39042', want=rS)
    asset(r, a)
    r.call('purchase_moncard', a, 'S39042', want=None)
    asset(r, a)


@scene('test_cjl_cb05_moncard_transfer')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a, b = members(r, ['13905000001', '13905000002'],
                   [['S39051', 'M39052', 'S39053'], ['S39054']], money=[500, 500])
    r.call('purchase_moncard', a, 'S39051', want=rS)
    r.call('transfer_moncard', 'S39051', 'M39052', 10, want=rF)
    r.call('transfer_moncard', 'S39051', 'S39051', 10, want=None)
    r.call('transfer_moncard', 'S39051', 'S39053', 0, want=rF)
    r.call('transfer_moncard', 'S39051', 'S39053', 30, want=rF)
    r.call('transfer_moncard', 'S39053', 'S39051', 1, want=rF)
    r.call('transfer_moncard', 'S39051', 'S39099', 1, want=rF)
    r.call('transfer_moncard', 'S39051', 'S39053', 10, want=rS)
    asset(r, a, want='E000:840.00|S39051:020|M39052:000|S39053:008')
    r.call('transfer_moncard', 'S39051', 'S39054', 7, want=rS)
    r.call('transfer_moncard', 'S39051', 'S39054', 1, want=rS)
    asset(r, a)
    asset(r, b)
    r.call('transfer_moncard', 'S39053', 'S39054', 7, want=None)
    asset(r, a)
    asset(r, b)


@scene('test_cjl_cb06_member_fee_basics')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a, b = members(r, ['13906000001', '13906000002'],
                   [['S39061', 'S39062', 'S39063'], ['S39064', 'S39065']], money=[500])
    s = park(r, 'S39061', 'S001', mins=30)
    out(r, s, 'S39061')
    asset(r, a)
    s = park(r, 'S39062', 'S002', mins=31)
    out(r, s, 'S39062')
    asset(r, a)
    s = park(r, 'S39063', 'S003', hours=1, mins=30)
    out(r, s, 'S39063')
    asset(r, a)
    s = park(r, 'S39064', 'S004', hours=13)
    out(r, s, 'S39064')
    asset(r, b)


@scene('test_cjl_cb07_cross_day_and_space_diff')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = members(r, ['13907000001'], [['S39071', 'S39072', 'M39073']], money=[500])[0]
    s = park(r, 'S39071', 'M001', hours=1)
    out(r, s, 'S39071')
    asset(r, a)
    s = park(r, 'S39072', 'L001', hours=13)
    out(r, s, 'S39072')
    asset(r, a)
    r.goto(D(2026, 3, 3, 23, 0))
    s = park(r, 'M39073', 'L002', hours=2)
    out(r, s, 'M39073')
    asset(r, a)


@scene('test_cjl_cb08_holiday_fee')
def _(r):
    a = members(r, ['13908000001'], [['S39081', 'S39082']], money=[500])[0]
    r.goto(D(2026, 4, 6, 10, 0))
    s = park(r, 'S39082', 'S001', hours=2)
    out(r, s, 'S39082')
    asset(r, a)
    r.goto(D(2026, 4, 30, 23, 0))
    s = park(r, 'S39081', 'S002', hours=2)
    out(r, s, 'S39081')
    asset(r, a)
    r.goto(D(2026, 5, 1, 10, 0))
    s = park(r, 'S39081', 'L001', hours=2)
    out(r, s, 'S39081')
    asset(r, a)


@scene('test_cjl_cb09_moncard_fee')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = members(r, ['13909000001'], [['S39091']], money=[500])[0]
    r.call('purchase_moncard', a, 'S39091', want=rS)
    asset(r, a)
    s = park(r, 'S39091', 'S001', hours=2)
    out(r, s, 'S39091')
    asset(r, a)
    s = park(r, 'S39091', 'L001', hours=2)
    out(r, s, 'S39091')
    asset(r, a)
    r.goto(D(2026, 3, 31, 23, 0))
    s = park(r, 'S39091', 'S002', hours=2)
    out(r, s, 'S39091')
    asset(r, a)


@scene('test_cjl_cb10_temp_pay')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    s = park(r, 'S39101', 'S001', hours=1)
    r.call('pay_parkcost', s, 'temp', '5.98', want=rF)
    r.call('pay_parkcost', s, 'temp', '6.02', want=rF)
    r.call('pay_parkcost', s, 'bal', '6.00', want=rF)
    r.call('pay_parkcost', s, 'cash', '6.00', want=rF)
    r.call('pay_parkcost', s, 'temp', 'abc', want=rF)
    r.call('pay_parkcost', 'p0000000000000000', 'temp', '6.00', want=rF)
    r.call('pay_parkcost', s, 'temp', '6.00', want=rS)
    r.call('pay_parkcost', s, 'temp', '6.00', want=None)
    out(r, s, 'S39101')
    r.call('check_revenue', 'day', 'temppay', '6.00', want=rS)
    t = enter(r, 'S39102')
    r.call('pay_parkcost', t, 'temp', '0.00', want=None)
    out(r, t, 'S39102')


@scene('test_cjl_cb11_paid_window_and_reoccupy')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    s = enter(r, 'S39111')
    t = enter(r, 'S39112')
    r.occupy(s, 'S001')
    r.occupy(t, 'S002')
    r.time_pass('hour', 1)
    r.release(s)
    r.release(t)
    r.call('pay_parkcost', s, 'temp', '6.00', want=rS)
    r.call('pay_parkcost', t, 'temp', '6.00', want=rS)
    r.occupy(s, 'S001')
    r.time_pass('min', 20)
    r.release(s)
    r.leave(s, want=None)
    r.time_pass('sec', 5)
    r.status('S39111', s, 'inpark')
    r.status('S39112', t, 'outpark')
    for v in ('1.50', '7.50', '3.00', '1.00', '2.00', '6.00', '9.00'):
        r.call('pay_parkcost', s, 'temp', v)
    r.leave(s, want=None)
    r.time_pass('sec', 5)
    r.status('S39111', s, 'outpark', want=None)
    r.call('check_revenue', 'day', 'temppay', '13.50')
    r.call('check_revenue', 'day', 'temppay', '19.50')


@scene('test_cjl_cb12_member_pay_bal_and_refund')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = members(r, ['13912000001'], [['S39121']], money=[100])[0]
    s = park(r, 'S39121', 'S001', hours=1)
    r.call('pay_parkcost', s, 'bal', '3.00', want=rF)
    r.call('pay_parkcost', s, 'bal', '4.00', want=rS)
    asset(r, a, want='E000:116.00|S39121:000|Null:000|Null:000')
    r.call('change_gate', 'G1', 'I', want=rS)
    r.exit(s, 'G1', want=None)
    r.time_pass('sec', 5)
    r.status('S39121', s, 'inpark')
    asset(r, a)
    r.call('check_revenue', 'day', 'balpay', '0.00')
    out(r, s, 'S39121')
    asset(r, a)
    r.call('check_revenue', 'day', 'balpay', '4.00')
    t = park(r, 'S39122', 'S002', hours=1)
    r.call('pay_parkcost', t, 'temp', '6.00', want=rS)
    r.exit(t, 'G1', want=None)
    r.time_pass('sec', 5)
    r.status('S39122', t, 'inpark')
    r.call('check_revenue', 'day', 'temppay', '0.00')
    r.call('pay_parkcost', t, 'temp', '6.00', want=None)
    out(r, t, 'S39122')
    r.call('check_revenue', 'day', 'temppay', '6.00')


@scene('test_cjl_cb13_negative_balance')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a, b = members(r, ['13913000001', '13913000002'], [['S39131', 'S39132']])
    s = park(r, 'S39131', 'S001', hours=1)
    r.time_pass('min', 15)
    r.status('S39131', s, 'outpark')
    asset(r, a, want='E000:-4.00|S39131:000|S39132:000|Null:000')
    r.call('transfer_account', a, b, 1, want=rF)
    r.call('purchase_moncard', a, 'S39131', want=rF)
    r.call('unregister_account', a, PWD, want=rF)
    t = park(r, 'S39132', 'S002', mins=10)
    out(r, t, 'S39132')
    t = park(r, 'S39132', 'S002', hours=1)
    out(r, t, 'S39132', ok=False)
    r.call('recharge_account', a, 100, want=rS)
    asset(r, a, want='E000:116.00|S39131:000|S39132:000|Null:000')
    out(r, t, 'S39132')
    asset(r, a)


@scene('test_cjl_cb14_blacklist')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    for p in ('X39141', 'S3914', 'S3914100', 's39141'):
        r.call('add_blacklist', p)
        r.call('del_blacklist', p)
    r.call('del_blacklist', 'S39141', want=None)
    r.call('add_blacklist', 'S39141', want=rS)
    r.call('add_blacklist', 'S39141', want=None)
    enter(r, 'S39141', want_ok=False)
    r.call('del_blacklist', 'S39141', want=rS)
    s = enter(r, 'S39141')
    r.call('add_blacklist', 'S39141', want=rS)
    out(r, s, 'S39141')
    enter(r, 'S39141', want_ok=False)


@scene('test_cjl_cb15_zombie_15_days')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = members(r, ['13915000001'], [['S39151']], money=[500])[0]
    s = enter(r, 'S39151')
    r.occupy(s, 'S001')
    r.time_pass('hour', 359)
    r.time_pass('min', 59)
    r.time_pass('sec', 54)
    t = enter(r, 'S39152')
    r.occupy(t, 'S002')
    r.release(s)
    out(r, s, 'S39151')
    asset(r, a)
    r.time_pass('day', 15)
    r.release(t)
    r.call('pay_parkcost', t, 'temp', '480.00')
    out(r, t, 'S39152')
    enter(r, 'S39151', want_ok=None)
    enter(r, 'S39152', want_ok=False)
    r.call('del_blacklist', 'S39152', want=rS)
    enter(r, 'S39152')


@scene('test_cjl_cb16_revenue_scopes')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = members(r, ['13916000001'], [['S39161']])[0]
    r.call('recharge_account', a, 100, want=rS)
    r.time_pass('day', 1)
    r.call('recharge_account', a, 50, want=rS)
    r.call('check_revenue', 'day', 'recharge', '50.00', want=rS)
    r.call('check_revenue', 'month', 'recharge', '150.00', want=rS)
    r.goto(D(2026, 4, 1, 0, 0))
    r.call('recharge_account', a, 500, want=rS)
    r.call('check_revenue', 'month', 'recharge', '500.00', want=rS)
    r.call('check_revenue', 'year', 'recharge', '650.00', want=rS)
    r.call('purchase_moncard', a, 'S39161', want=rS)
    r.call('check_revenue', 'year', 'monthcard', '400.00', want=rS)
    r.call('check_revenue', 'day', 'recharge', '500', want=None)
    r.call('check_revenue', 'day', 'recharge', '500.0', want=None)
    for args in (('week', 'recharge', '0.00'), ('day', 'cash', '0.00'), ('day', 'recharge', 'abc'),
                 ('day', 'recharge', '-1.00')):
        r.call('check_revenue', *args, want=rF)
    r.call('check_revenue', 'day', 'recharge', '499.99')
    r.call('check_revenue', 'day', 'recharge', '499.98', want=rF)


@scene('test_cjl_cb17_session_gift_and_moncard')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = members(r, ['13917000001'], [['S39171']])[0]
    s = enter(r, 'S39171')
    r.call('recharge_account', a, 500, want=rS)
    r.call('purchase_moncard', a, 'S39171', want=rS)
    asset(r, a)
    r.occupy(s, 'L001')
    r.time_pass('hour', 2)
    r.release(s)
    out(r, s, 'S39171')
    asset(r, a)


@scene('test_cjl_cb18_query_asset_params')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = members(r, ['13918000001'], [['S39181', 'M391812']])[0]
    i, _ = r.register('13918000002', PWD)
    asset(r, a, want='E000:0.00|S39181:000|M391812:000|Null:000')
    for cid in (i, 'CID00000000', 'CID123', 'cid12345678'):
        asset(r, cid, want=rF)
    s, cid = r.entry('S39182', 'G1')
    asset(r, cid, want=rF)
    r.call('unregister_account', a, PWD, want=rS)
    asset(r, a, want=rF)
