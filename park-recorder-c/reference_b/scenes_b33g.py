# -*- coding: utf-8 -*-
"""B版本 3.3 补充：出场结算中无账户车辆、已缴后补缴为零、负余额会员车出场等分支。
want 为按规格推测的值，只用于录制报告对比，写入用例的是实际返回。"""
from datetime import datetime as D

from record import rF, rS, scene


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


def out(r, sid, plate, passed=True):
    """发起出场，5 秒后按会话是否仍存在校验车辆状态，返回是否已出场"""
    r.leave(sid)
    r.time_pass('sec', 5)
    gone = not r.due(sid).get('ok')
    if gone != passed:
        r.diffs.append((len(r.lines), f'{plate} 出场', '出场' if passed else '留在场内', '出场' if gone else '留在场内'))
    r.status(plate, sid, 'outpark' if gone else 'inpark', want=rS)
    return gone


@scene('test_cjl_g01_temp_car_unpaid_exit')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    free, _ = r.arrive('M330402')
    r.time_pass('min', 5)
    out(r, free, 'M330402')
    sid = stay(r, 'S330401', 'S001', 60)
    r.release(sid)
    out(r, sid, 'S330401', passed=False)
    r.revenue('day', 'temppay', want=rS)
    r.pay(sid, 'bal', want=rF)
    settle(r, sid, 'temp')
    out(r, sid, 'S330401')
    r.revenue_all(('day',))


@scene('test_cjl_g02_temp_car_paid_then_overtime')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    sid = stay(r, 'S330411', 'S001', 60)
    settle(r, sid, 'temp')
    r.revenue('day', 'temppay', want=rS)
    r.time_pass('min', 20)
    r.release(sid)
    out(r, sid, 'S330411', passed=False)
    r.revenue('day', 'temppay', want=rS)
    settle(r, sid, 'temp')
    out(r, sid, 'S330411')
    r.revenue_all(('day',))


@scene('test_cjl_g03_paid_then_moncard_overtime')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13000000431'], [['S330421', 'S330422']], [(500, 500)])[0]
    a = stay(r, 'S330421', 'S001', 60)
    b = stay(r, 'S330422', 'M001', 60)
    r.pay(a, 'bal', want=rS)
    r.pay(b, 'bal', want=rS)
    r.call('purchase_moncard', m, 'S330421', want=rS)
    r.call('purchase_moncard', m, 'S330422', want=rS)
    r.asset(m)
    r.time_pass('min', 20)
    r.release(a)
    r.release(b)
    out(r, a, 'S330421')
    r.asset(m)
    out(r, b, 'S330422')
    r.asset(m)
    r.revenue_all(('day',))


@scene('test_cjl_g04_negative_balance_unoccupied_exit')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13000000441'], [['S330441', 'M330442']], [(5,)])[0]
    a = stay(r, 'S330441', 'S001', 120)
    r.release(a)
    r.time_pass('min', 11)
    b, _ = r.arrive('M330442')
    r.time_pass('min', 4)
    r.time_pass('sec', 1)
    r.status('S330441', a, 'outpark')
    r.asset(m)
    out(r, b, 'M330442', passed=False)
    r.pay(b, 'bal', want=rF)
    r.call('recharge_account', m, 50, want=rS)
    r.asset(m)
    out(r, b, 'M330442')
    r.asset(m)
    r.revenue_all(('day',))


@scene('test_cjl_g05_time_pass_params')
def _(r):
    r.goto(D(2026, 12, 1, 10, 0))
    m = members(r, ['13000000451'], [['S330451']], [(500,)])[0]
    r.call('purchase_moncard', m, 'S330451', want=rS)
    r.asset(m)
    for unit, n in (('sec', 0), ('sec', -1), ('sec', 10001), ('day', 10001), ('week', 1), ('SEC', 1), ('day', 31)):
        r.call('time_pass', unit, n, want=rF)
    r.asset(m)
    r.time_pass('sec', 10000)
    r.time_pass('day', 10)
    r.goto(D(2026, 12, 31, 23, 59, 0))
    r.call('time_pass', 'min', 1, want=rF)
    r.call('time_pass', 'sec', 60, want=rF)
    r.time_pass('sec', 59)
    r.call('time_pass', 'sec', 1, want=rF)
    r.asset(m)


@scene('test_cjl_g06_gate_mode_blocks_pass')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13000000461'], [['S330461', 'M330462']], [(500,)])[0]
    r.call('change_gate', 'G1', 'O', want=rS)
    r.call('change_gate', 'G2', 'B', want=rS)
    a, _ = r.entry('S330461', 'G1')
    b, _ = r.entry('M330462', 'G2')
    r.time_pass('sec', 5)
    r.status('S330461', a, 'outpark')
    r.status('M330462', b, 'outpark')
    r.call('change_gate', 'G1', 'IO', want=rS)
    r.call('change_gate', 'G2', 'IO', want=rS)
    s = stay(r, 'S330461', 'S001', 60)
    r.release(s)
    r.call('change_gate', 'G3', 'B', want=rS)
    r.exit(s, 'G3', want=None)
    r.time_pass('sec', 5)
    r.status('S330461', s, 'inpark')
    r.asset(m)
    r.revenue('day', 'balpay', want=rS)
    r.call('change_gate', 'G3', 'IO', want=rS)
    out(r, s, 'S330461')
    r.asset(m)
    r.revenue_all(('day',))
