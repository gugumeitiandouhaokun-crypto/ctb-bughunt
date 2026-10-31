# -*- coding: utf-8 -*-
"""B版本 3.3 账户充值子系统与车辆出入、车位、计费、出入口、黑名单的交叉场景。
want 为按规格推测的值，只用于录制报告对比，写入用例的是实际返回。"""
from datetime import datetime as D, timedelta

from record import rF, rS, scene

PSW = 'Abc123!'


def members(r, phones, plates_of=(), recharge=()):
    """注册并激活会员，按 plates_of[i] 绑定车牌、按 recharge[i] 依次充值"""
    out = []
    for i, phone in enumerate(phones):
        cid = r.member(phone)
        for p in (plates_of[i] if i < len(plates_of) else ()):
            r.call('bind_plate_id', cid, p, want=rS)
        for v in (recharge[i] if i < len(recharge) else ()):
            r.call('recharge_account', cid, v, want=rS)
        out.append(cid)
    return out


def stay(r, plate, space, minutes=0):
    """入场、占位，停留 minutes 分钟后返回会话号（未离位）"""
    sid, _ = r.arrive(plate)
    r.time_pass('sec', 5)
    r.occupy(sid, space)
    if minutes:
        r.time_pass('min', minutes)
    return sid


def settle(r, sid, kind='auto'):
    return r.pay(sid, kind, want=rS if r.due(sid)['due'] > 0 else rF)


def gone(r, sid):
    return not r.due(sid).get('ok')


def passed(r, sid, plate, expect=True):
    """出入口放行时刻之后调用：按会话是否仍存在校验车辆状态"""
    left = gone(r, sid)
    if left != expect:
        r.diffs.append((len(r.lines), f'{plate} 过闸', '离场' if expect else '留在场内', '离场' if left else '留在场内'))
    r.status(plate, sid, 'outpark' if left else 'inpark', want=rS)
    return left


def out(r, sid, plate, expect=True):
    r.leave(sid)
    r.time_pass('sec', 5)
    return passed(r, sid, plate, expect)


def balance(r, cid):
    return float(r.asset(cid).split(':')[1].split('|')[0])


# ---------------------------------------------------------------- 出场途中的账户操作

@scene('test_cjl_x01_exit_window_recharge_or_transfer_out')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ma, mb, mc = members(r, ['13000003011', '13000003012', '13000003013'],
                         [['M330301'], ['L330301'], ['M330302']], [(1,), (), (50,)])
    a = stay(r, 'M330301', 'M001')
    b = stay(r, 'L330301', 'L001')
    c = stay(r, 'M330302', 'M002')
    r.goto(D(2026, 3, 2, 12, 0))
    r.release(a)
    r.release(c)
    r.leave(a)
    r.status('M330301', a, 'exiting')
    r.call('recharge_account', ma, 50, want=rS)
    r.time_pass('sec', 5)
    passed(r, a, 'M330301')
    r.asset(ma)
    r.leave(c)
    r.status('M330302', c, 'exiting')
    r.call('transfer_account', mc, ma, 50, want=rS)
    r.time_pass('sec', 5)
    passed(r, c, 'M330302', expect=False)
    r.asset(mc)
    r.asset(ma)
    settle(r, c, 'temp')
    out(r, c, 'M330302')
    r.goto(D(2026, 3, 3, 12, 0))
    r.release(b)
    r.leave(b)
    r.call('recharge_account', mb, 100, want=rS)
    r.time_pass('sec', 5)
    passed(r, b, 'L330301', expect=False)
    r.asset(mb)
    r.pay(b, 'bal', want=rF)
    r.pay(b, 'temp', want=rS)
    out(r, b, 'L330301')
    r.asset(mb)
    r.revenue_all(('day', 'month'))


@scene('test_cjl_x02_exit_window_moncard_buy_or_receive')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ma, mb, md = members(r, ['13000003021', '13000003022', '13000003024'],
                         [['S330321'], ['S330322'], ['S330324']], [(500,), (500,), (500,)])
    r.call('purchase_moncard', md, 'S330324', want=rS)
    a = stay(r, 'S330321', 'S001')
    b = stay(r, 'S330322', 'S002')
    r.goto(D(2026, 3, 2, 12, 0))
    r.release(a)
    r.release(b)
    r.leave(a)
    r.call('purchase_moncard', ma, 'S330321', want=rS)
    r.time_pass('sec', 5)
    passed(r, a, 'S330321')
    r.asset(ma, want='E000:220.00|S330321:030|Null:000|Null:000')
    r.leave(b)
    r.call('transfer_moncard', 'S330324', 'S330322', 10, want=rS)
    r.time_pass('sec', 5)
    passed(r, b, 'S330322')
    r.asset(mb)
    r.asset(md)
    r.revenue_all(('day',))


@scene('test_cjl_x03_exit_window_account_ops_then_cancel_and_temp_reentry')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m, other = members(r, ['13000003031', '13000003032'], [['S330331'], []], [(), (50,)])
    s = stay(r, 'S330331', 'S001', 20)
    r.release(s)
    r.leave(s)
    r.status('S330331', s, 'exiting')
    r.call('unregister_account', m, PSW, want=rF)
    r.call('unbind_plate_id', m, 'S330331', want=rF)
    r.call('bind_plate_id', other, 'S330331', want=rF)
    r.pay(s, 'bal', want=rF)
    r.time_pass('sec', 5)
    passed(r, s, 'S330331')
    r.call('unregister_account', m, PSW, want=rS)
    r.call('query_account', m, want=rF)
    sid, cid = r.arrive('S330331')
    r.call('query_account', cid, want='E000:temp|null|active')
    r.time_pass('sec', 5)
    r.occupy(sid, 'S001')
    r.call('bind_plate_id', other, 'S330331', want=rF)
    r.time_pass('hour', 2)
    r.release(sid)
    r.pay(sid, 'bal', want=rF)
    settle(r, sid, 'temp')
    r.revenue('day', 'temppay', want=rS)
    out(r, sid, 'S330331')
    r.call('query_account', cid)
    r.time_pass('hour', 10)
    r.call('bind_plate_id', other, 'S330331', want=rS)
    r.asset(other, want='E000:50.00|S330331:000|Null:000|Null:000')


# ---------------------------------------------------------------- 入场途中与黑名单

@scene('test_cjl_x04_entry_window_recharge_and_blacklisted_member_car')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13000003041'], [['L330341', 'S330341']])[0]
    sid, _ = r.arrive('L330341')
    r.status('L330341', sid, 'entrying')
    r.call('recharge_account', m, 100, want=rS)
    r.time_pass('sec', 5)
    r.occupy(sid, 'L001')
    r.call('add_blacklist', 'S330341', want=rS)
    s2, _ = r.arrive('S330341')
    r.time_pass('sec', 5)
    passed(r, s2, 'S330341', expect=True)
    r.asset(m, want='E000:120.00|L330341:000|S330341:000|Null:000')
    r.call('del_blacklist', 'S330341', want=rS)
    r.goto(D(2026, 3, 3, 12, 0))
    r.release(sid)
    r.pay(sid, 'bal', want=rF)
    r.pay(sid, 'temp', want=rS)
    r.asset(m)
    out(r, sid, 'L330341')
    r.revenue_all(('day',))


@scene('test_cjl_x05_blacklisted_parked_car_moncard_and_exit')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m, rcv = members(r, ['13000003051', '13000003052'], [['S330351'], ['S330352']], [(500,), (500,)])
    s = stay(r, 'S330351', 'S001', 60)
    r.call('add_blacklist', 'S330351', want=rS)
    r.call('purchase_moncard', m, 'S330351', want=rS)
    r.call('transfer_moncard', 'S330351', 'S330352', 5, want=rS)
    r.asset(m)
    r.asset(rcv)
    r.release(s)
    settle(r, s)
    out(r, s, 'S330351')
    r.asset(m)
    s2, _ = r.arrive('S330351')
    r.time_pass('sec', 5)
    passed(r, s2, 'S330351')
    r.call('del_blacklist', 'S330351', want=rS)
    s3 = stay(r, 'S330351', 'S001', 60)
    r.release(s3)
    settle(r, s3)
    out(r, s3, 'S330351')
    r.asset(m)
    r.revenue_all(('day',))


# ---------------------------------------------------------------- 出场失败退款

@scene('test_cjl_x06_exit_refund_pushes_balance_over_5000')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m, o = members(r, ['13000003061', '13000003062'], [['M330361'], []], [(500,) * 7 + (50,) * 13, (50,)])
    s = stay(r, 'M330361', 'M001', 60)
    r.release(s)
    r.pay(s, 'bal', want=rS)
    top = int(round(5000 - balance(r, m)))
    r.call('recharge_account', m, top, want=rS)
    r.asset(m, want='E000:5000.00|M330361:000|Null:000|Null:000')
    r.call('change_gate', 'G1', 'I', want=rS)
    r.exit(s, 'G1', want=rS)
    r.time_pass('sec', 5)
    passed(r, s, 'M330361', expect=False)
    r.asset(m)
    r.revenue('day', 'balpay', want=rS)
    r.call('recharge_account', m, 1, want=rF)
    r.call('transfer_account', o, m, 1, want=rF)
    r.call('transfer_account', m, o, 10, want=rS)
    r.asset(m)
    r.call('change_gate', 'G1', 'IO', want=rS)
    out(r, s, 'M330361')
    r.asset(m)
    r.asset(o)
    r.revenue_all(('day',))


@scene('test_cjl_x07_exit_refund_across_midnight_and_month')
def _(r):
    r.goto(D(2026, 3, 31, 21, 0))
    m = members(r, ['13000003071'], [['M330371']], [(500,)])[0]
    t = stay(r, 'S330379', 'S001')
    a = stay(r, 'M330371', 'M001')
    r.goto(D(2026, 3, 31, 23, 59, 57))
    r.release(t)
    r.pay(t, 'temp', want=rS)
    r.release(a)
    r.pay(a, 'bal', want=rS)
    r.revenue('day', 'temppay', want=rS)
    r.revenue('day', 'balpay', want=rS)
    r.call('change_gate', 'G1', 'I', want=rS)
    r.call('change_gate', 'G2', 'I', want=rS)
    r.exit(t, 'G1', want=rS)
    r.exit(a, 'G2', want=rS)
    r.time_pass('sec', 10)
    passed(r, t, 'S330379', expect=False)
    passed(r, a, 'M330371', expect=False)
    r.asset(m)
    r.revenue_all(('day', 'month', 'year'))
    r.call('change_gate', 'G1', 'IO', want=rS)
    r.call('change_gate', 'G2', 'IO', want=rS)
    settle(r, t, 'temp')
    settle(r, a, 'bal')
    out(r, t, 'S330379')
    out(r, a, 'M330371')
    r.asset(m)
    r.revenue_all(('day', 'month', 'year'))


@scene('test_cjl_x11_tidal_gate_exit_refund_then_auto_deduct')
def _(r):
    r.goto(D(2026, 3, 2, 6, 0))
    m = members(r, ['13000003111'], [['M330411']], [(500,)])[0]
    s = stay(r, 'M330411', 'M001', 60)
    r.goto(D(2026, 3, 2, 7, 30))
    r.release(s)
    r.pay(s, 'bal', want=rS)
    r.asset(m)
    r.call('query_gate_status', 'G1')
    r.exit(s, 'G1', want=rS)
    r.time_pass('sec', 5)
    passed(r, s, 'M330411', expect=False)
    r.asset(m)
    r.revenue('day', 'balpay', want=rS)
    r.exit(s, 'G4', want=rS)
    r.time_pass('sec', 5)
    passed(r, s, 'M330411')
    r.asset(m)
    r.revenue_all(('day',))


# ---------------------------------------------------------------- 月卡与停车时间线

@scene('test_cjl_x08_zombie_car_moncard_transfer_forced_settle')
def _(r):
    t0 = D(2026, 3, 2, 10, 0)
    r.goto(t0)
    m, rcv = members(r, ['13000003081', '13000003082'], [['S330381'], ['S330382']], [(500,), ()])
    r.call('purchase_moncard', m, 'S330381', want=rS)
    s = stay(r, 'S330381', 'S001')
    r.goto(t0 + timedelta(days=5))
    r.call('transfer_moncard', 'S330381', 'S330382', 10, want=rS)
    r.asset(m)
    r.asset(rcv)
    r.goto(t0 + timedelta(days=15, minutes=1))
    r.status('S330381', s, 'parked')
    r.call('unbind_plate_id', m, 'S330381', want=rF)
    r.asset(m)
    r.goto(t0 + timedelta(days=30, minutes=5))
    r.status('S330381', s, 'outpark')
    r.asset(m)
    r.revenue_all(('day', 'month'))
    s2, _ = r.arrive('S330381')
    r.time_pass('sec', 5)
    passed(r, s2, 'S330381')
    r.call('del_blacklist', 'S330381', want=rS)
    r.call('unregister_account', m, PSW)
    r.asset(m)


@scene('test_cjl_x09_reoccupy_after_pay_extra_fee_gift_locked')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13000003091'], [['L330391']], [(1,)])[0]
    s, _ = r.arrive('L330391')
    r.time_pass('sec', 5)
    r.call('recharge_account', m, 100, want=rS)
    r.occupy(s, 'L001')
    r.time_pass('hour', 1)
    r.release(s)
    r.pay(s, 'bal', want=rS)
    r.asset(m)
    r.occupy(s, 'L002')
    r.goto(D(2026, 3, 3, 12, 0))
    r.release(s)
    out(r, s, 'L330391', expect=False)
    r.asset(m)
    r.revenue('day', 'balpay', want=rS)
    r.pay(s, 'bal', want=rF)
    r.pay(s, 'temp', want=rS)
    r.asset(m)
    out(r, s, 'L330391')
    r.revenue_all(('day', 'month'))


@scene('test_cjl_x10_moncard_bought_mid_session_and_after_release')
def _(r):
    r.goto(D(2026, 3, 2, 20, 0))
    m = members(r, ['13000003101'], [['S330401', 'M330401']], [(500, 500)])[0]
    a = stay(r, 'S330401', 'L001')
    b = stay(r, 'M330401', 'L002')
    r.goto(D(2026, 3, 3, 10, 0))
    r.call('purchase_moncard', m, 'S330401', want=rS)
    r.asset(m)
    r.goto(D(2026, 3, 4, 12, 0))
    r.release(a)
    settle(r, a, 'bal')
    r.asset(m)
    out(r, a, 'S330401')
    r.release(b)
    stale = r.pay_value(b, 'bal')
    r.call('purchase_moncard', m, 'M330401', want=rS)
    r.call('pay_parkcost', b, 'bal', stale, want=rF)
    settle(r, b, 'bal')
    out(r, b, 'M330401')
    r.asset(m)
    r.revenue_all(('day', 'month'))


@scene('test_cjl_x12_paid_then_unregister_attempts')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m, o = members(r, ['13000003121', '13000003122'], [['S330421', 'M330421'], []], [(50,), ()])
    a = stay(r, 'S330421', 'S001', 60)
    b = stay(r, 'M330421', 'M001', 60)
    r.pay(a, 'bal', want=rS)
    r.call('unregister_account', m, PSW, want=rF)
    left = balance(r, m)
    r.call('transfer_account', m, o, int(left), want=rS)
    r.asset(m)
    r.call('unregister_account', m, PSW, want=rF)
    r.release(a)
    out(r, a, 'S330421')
    r.call('unregister_account', m, PSW, want=rF)
    r.release(b)
    out(r, b, 'M330421', expect=False)
    settle(r, b, 'temp')
    out(r, b, 'M330421')
    r.asset(m)
    r.call('unregister_account', m, PSW, want=rS)
    r.call('pay_parkcost', a, 'bal', '1.00', want=rF)
    r.call('transfer_account', o, m, 1, want=rF)
    sid, cid = r.arrive('M330421')
    r.call('query_account', cid, want='E000:temp|null|active')
    r.time_pass('sec', 5)
    out(r, sid, 'M330421')
    r.asset(o)
    r.revenue_all(('day',))


@scene('test_cjl_x13_moncard_transfer_between_two_parked_cars')
def _(r):
    r.goto(D(2026, 3, 2, 9, 0))
    src, dst = members(r, ['13000003131', '13000003132'], [['S330431'], ['S330432']], [(500,), (100,)])
    r.call('purchase_moncard', src, 'S330431', want=rS)
    a = stay(r, 'S330431', 'S001')
    b = stay(r, 'S330432', 'S002')
    r.goto(D(2026, 3, 2, 12, 0))
    r.call('transfer_moncard', 'S330431', 'S330432', 29, want=rS)
    r.asset(src)
    r.asset(dst)
    r.goto(D(2026, 3, 4, 12, 0))
    r.release(a)
    r.release(b)
    settle(r, a)
    settle(r, b)
    r.asset(src)
    r.asset(dst)
    out(r, a, 'S330431')
    out(r, b, 'S330432')
    r.revenue_all(('day',))


@scene('test_cjl_x14_holiday_free_day_gift_lock_month_boundary')
def _(r):
    r.goto(D(2026, 4, 30, 20, 0))
    m = members(r, ['13000003141'], [['S330441']])[0]
    s = stay(r, 'S330441', 'M001')
    r.call('recharge_account', m, 100, want=rS)
    r.revenue('day', 'recharge', want=rS)
    r.goto(D(2026, 5, 1, 12, 0))
    r.release(s)
    if r.pay(s, 'bal') != rS:
        settle(r, s, 'temp')
    r.asset(m)
    out(r, s, 'S330441')
    r.revenue_all(('day', 'month', 'year'))
