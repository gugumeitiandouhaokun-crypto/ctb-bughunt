# -*- coding: utf-8 -*-
"""C版本：保留预约过期与预约车入场被拒两个用例，新增洗车服务。
want 为按代码推测的值，只用于录制报告对比，写入用例的是实际返回。"""
import re
from datetime import datetime as D

from record import rF, rS, scene

V = 'E002:Verify_fail'
W = '\u3000'


def members(r, phones, plates_of=()):
    out = []
    for i, phone in enumerate(phones):
        cid = r.member(phone)
        for p in (plates_of[i] if i < len(plates_of) else ()):
            r.call('bind_plate_id', cid, p, want=rS)
        out.append(cid)
    return out


def reserve(r, plate, space, want=None):
    ret = r.call('reserve_space', plate, space, want=want)
    m = re.match(r'E000:(r\d{8})$', ret)
    return m.group(1) if m else 'r00000000'


def rstat(r, rid, st, want=rS):
    return r.call('check_reserve_status', rid, st, want=want)


def stats(r, s, m, l, want=rS):
    return r.call('check_park_statistics', s, m, l, want=want)


def enter(r, plate, want_ok=True):
    sid, _ = r.arrive(plate)
    r.time_pass('sec', 5)
    r.status(plate, sid, 'inpark' if want_ok else 'outpark')
    return sid


def cars(r, plate_list):
    """每批4辆走G1~G4，流逝5秒完成入场，返回会话号列表"""
    return [sid for sid, _ in r.enter(plate_list)]


def wash(r, plate, want=None):
    ret = r.call('take_wash_order', plate, want=want)
    m = re.match(r'E000:(\d{3})$', ret)
    return m.group(1) if m else '000'


def wst(r, oid, want=None):
    return r.call('query_wash_status', oid, want=want)


def wq(r, bay=None, want=None):
    return r.call('query_wash_queue', *([] if bay is None else [bay]), want=want)


@scene('test_cjl_c3105_reserve_expire_30min')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    members(r, ['13100000051'], [['S310051']])
    rid = reserve(r, 'S310051', 'S001', want=None)
    r.time_pass('min', 30)
    rstat(r, rid, 'locked')
    stats(r, '0|0|1', '0|0|0', '0|0|0')
    r.time_pass('sec', 1)
    rstat(r, rid, 'expired')
    rstat(r, rid, 'locked', want=V)
    stats(r, '0|0|0', '0|0|0', '0|0|0')
    r.call('cancel_reserve', rid, want=rF)
    reserve(r, 'S310051', 'S001', want=None)
    r.goto(D(2026, 3, 3, 0, 0, 1))
    rstat(r, rid, 'closed')
    stats(r, '0|0|0', '0|0|0', '0|0|0')


@scene('test_cjl_c3109_reserved_car_entry_rejected')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    members(r, ['13100000091'], [['S310091', 'M310092']])
    a = reserve(r, 'S310091', 'S001', want=None)
    b = reserve(r, 'M310092', 'M001', want=None)
    r.call('add_blacklist', 'S310091', want=rS)
    r.call('add_blacklist', 'S310091', want=rS)
    enter(r, 'S310091', want_ok=False)
    rstat(r, a, 'locked')
    r.call('change_gate', 'G1', 'O', want=rS)
    s, _ = r.entry('M310092', 'G1')
    r.time_pass('sec', 5)
    r.status('M310092', s, 'outpark')
    rstat(r, b, 'locked')
    stats(r, '0|0|1', '0|0|1', '0|0|0')
    r.call('del_blacklist', 'S310091', want=rS)
    r.call('del_blacklist', 'S310091', want=rS)
    enter(r, 'S310091')
    rstat(r, a, 'used')


@scene('test_cjl_c3201_wash_order_checks')
def _(r):
    r.goto(D(2026, 3, 2, 8, 59, 50))
    a, b, c = cars(r, ['S320101', 'S320102', 'S320103'])
    wash(r, 'S320101', want=rF)
    r.time_pass('sec', 5)
    wash(r, 'S320101', want='E000:001')
    wash(r, 'S320101', want=rF)
    wash(r, 'S320199', want=rF)
    r.occupy(b, 'S001')
    wash(r, 'S320102', want='E000:002')
    r.call('add_blacklist', 'S320103', want=rS)
    wash(r, 'S320103', want=rF)
    r.goto(D(2026, 3, 2, 9, 1))
    d = cars(r, ['S320104'])[0]
    r.exit(d, 'G4')
    r.status('S320104', d, 'exiting')
    wash(r, 'S320104', want=rF)
    e, _ = r.entry('S320105', 'G2')
    wash(r, 'S320105', want=rF)
    wst(r, '001', want='E000:queuing')
    wst(r, '999', want=rF)
    wq(r, 'WX1', want=rF)
    wq(r, 'WS2')
    r.goto(D(2026, 3, 2, 16, 59, 50))
    cars(r, ['S320106', 'S320107'])
    r.time_pass('sec', 5)
    wash(r, 'S320106', want='E000:003')
    r.time_pass('sec', 1)
    wash(r, 'S320107', want=rF)


@scene('test_cjl_c3202_wash_bay_rotation')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ps = ['S320201', 'S320202', 'S320203', 'S320204', 'M320205', 'M320206', 'M320207', 'L320208', 'L320209']
    cars(r, ps)
    for i, p in enumerate(ps):
        wash(r, p, want=f'E000:{i + 1:03d}')
    wq(r)
    wq(r, 'WS1')
    wq(r, 'WM1')
    wq(r, 'WL1')


@scene('test_cjl_c3203_wash_start_end')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a, b, c, d = cars(r, ['S320301', 'S320302', 'S320303', 'S320304'])
    r.occupy(a, 'S001')
    for p in ('S320301', 'S320302', 'S320303', 'S320304'):
        wash(r, p)
    r.call('start_wash_service', '001', want=rF)
    r.call('start_wash_service', '004', want=rF)
    r.release(a)
    r.call('end_wash_service', '001', want=rF)
    r.call('start_wash_service', '001', want=rS)
    wst(r, '001', want='E000:washing')
    r.call('start_wash_service', '001', want=rF)
    r.call('cancel_wash_order', '001', want=rF)
    r.call('start_wash_service', '004', want=rF)
    wq(r, 'WS1')
    r.time_pass('min', 10)
    r.call('end_wash_service', '001', want=rS)
    r.call('end_wash_service', '001', want=rF)
    wst(r, '001', want='E000:closed')
    r.call('start_wash_service', '004', want=rS)
    r.call('start_wash_service', '999', want=rF)
    r.call('end_wash_service', '999', want=rF)
    wq(r, 'WS1')
    r.status('S320301', a, 'inpark')


@scene('test_cjl_c3204_wash_call_timeout')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    cars(r, ['S320401', 'S320402', 'S320403', 'S320404'])
    for p in ('S320401', 'S320402', 'S320403', 'S320404'):
        wash(r, p)
    r.time_pass('sec', 300)
    wst(r, '001', want='E000:queuing')
    r.time_pass('sec', 1)
    wst(r, '001', want='E000:expired')
    wst(r, '004', want='E000:queuing')
    r.call('cancel_wash_order', '004', want=rF)
    r.call('start_wash_service', '001', want=rF)
    r.time_pass('sec', 299)
    wst(r, '004', want='E000:queuing')
    r.time_pass('sec', 1)
    wst(r, '004', want='E000:expired')
    wq(r)


@scene('test_cjl_c3205_wash_cancel_and_inpark_timer')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a, b, c, d, e, f, g = cars(r, [f'S32050{i}' for i in range(1, 8)])
    for p in ('S320501', 'S320502', 'S320503', 'S320504', 'S320505', 'S320506', 'S320507'):
        wash(r, p)
    r.call('start_wash_service', '001', want=rS)
    r.call('cancel_wash_order', '007', want=rS)
    r.call('cancel_wash_order', '007', want=rF)
    r.call('cancel_wash_order', '999', want=rF)
    r.time_pass('sec', 601)
    r.call('cancel_wash_order', '004', want=rS)
    r.time_pass('min', 6)
    r.status('S320504', d, 'inpark')
    r.time_pass('min', 9)
    r.status('S320504', d, 'outpark')
    r.status('S320501', a, 'inpark')
    wq(r)


@scene('test_cjl_c3206_wash_day_end')
def _(r):
    r.goto(D(2026, 3, 2, 16, 50))
    cars(r, ['S320601', 'S320602', 'S320603', 'S320604'])
    for p in ('S320601', 'S320602', 'S320603', 'S320604'):
        wash(r, p)
    r.call('start_wash_service', '001', want=rS)
    r.call('start_wash_service', '002', want=rS)
    r.call('start_wash_service', '003', want=rS)
    r.goto(D(2026, 3, 2, 17, 29, 59))
    wst(r, '004', want='E000:queuing')
    r.time_pass('sec', 1)
    wst(r, '004', want='E000:expired')
    r.time_pass('min', 10)
    r.call('end_wash_service', '001', want=rS)
    r.goto(D(2026, 3, 2, 17, 59, 59))
    wst(r, '002', want='E000:washing')
    r.time_pass('sec', 1)
    wst(r, '002', want='E000:closed')
    wst(r, '004', want='E000:closed')
    wq(r)
    r.goto(D(2026, 3, 3, 9, 5))
    wst(r, '001', want=rF)
    s5, _ = r.entry('S320605', 'G4')
    r.time_pass('sec', 5)
    r.status('S320605', s5, 'inpark')
    wash(r, 'S320605', want='E000:001')
    wq(r, 'WS1')


@scene('test_cjl_c3207_wash_balance_same_type')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ps = [f'S32070{i}' for i in range(1, 8)]
    cars(r, ps)
    for p in ps:
        wash(r, p)
    r.call('cancel_wash_order', '005', want=rS)
    r.call('cancel_wash_order', '006', want=rS)
    wq(r)
    r.call('trigger_wash_balance', want=rS)
    wq(r)


@scene('test_cjl_c3208_wash_balance_small_to_medium')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ps = [f'S32080{i}' for i in range(1, 8)]
    cars(r, ps)
    for p in ps:
        wash(r, p)
    r.call('trigger_wash_balance', want=rS)
    wq(r)
    r.call('start_wash_service', '007', want=None)
    wst(r, '007')
    r.call('end_wash_service', '007', want=None)
    r.call('trigger_wash_balance', want=rS)
    wq(r)


@scene('test_cjl_c3209_wash_bay_overflow')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ss = [f'S3209{i:02d}' for i in range(31)]
    ls = ['M320901', 'L320902', 'L320903', 'L320904', 'L320905', 'L320906']
    cars(r, ss + ls)
    for p in ss[:18]:
        wash(r, p)
    wq(r)
    for p in ss[18:]:
        wash(r, p)
    wq(r)
    wash(r, 'M320901', want='E000:032')
    for p in ls[1:5]:
        wash(r, p)
    wash(r, 'L320906', want=rF)
    wq(r, 'WL1')


@scene('test_cjl_c3210_wash_blank_params')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    for cmd in ('take_wash_order', 'cancel_wash_order', 'query_wash_status', 'start_wash_service', 'end_wash_service'):
        r.call(cmd, W, want=rF)
    wq(r, W)
    wq(r, '\t', want=None)


@scene('test_cjl_c3211_wash_fee_negative_balance')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13100003211'], [['S321101', 'S321102']])[0]
    a = enter(r, 'S321101')
    t = enter(r, 'S321103')
    wash(r, 'S321101', want='E000:001')
    wash(r, 'S321103', want='E000:002')
    r.call('start_wash_service', '001', want=rS)
    r.call('start_wash_service', '002', want=rS)
    r.time_pass('min', 20)
    r.status('S321101', a, 'inpark')
    r.call('end_wash_service', '001', want=rS)
    r.call('end_wash_service', '002', want=rS)
    r.time_pass('min', 15)
    r.status('S321101', a, 'outpark')
    r.status('S321103', t, 'outpark')
    r.call('query_account_asset', m)
    enter(r, 'S321102')
    wash(r, 'S321102', want=rF)


@scene('test_cjl_c3212_wash_fee_paid_at_exit')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13100003212'], [['S321201']])[0]
    r.call('recharge_account', m, 100, want=rS)
    a = enter(r, 'S321201')
    t = enter(r, 'S321202')
    wash(r, 'S321201')
    wash(r, 'S321202')
    r.call('start_wash_service', '001', want=rS)
    r.call('start_wash_service', '002', want=rS)
    r.time_pass('min', 5)
    r.call('end_wash_service', '001', want=rS)
    r.call('end_wash_service', '002', want=rS)
    r.leave(a)
    r.leave(t)
    r.time_pass('sec', 5)
    r.status('S321201', a, 'outpark')
    r.status('S321202', t, 'exiting', want=None)
    r.call('query_account_asset', m)


def wash_cycle(r, sid, plate, oid):
    wash(r, plate, want=f'E000:{oid}')
    r.call('start_wash_service', oid, want=rS)
    r.call('end_wash_service', oid, want=rS)
    r.leave(sid)
    r.time_pass('sec', 5)
    r.status(plate, sid, 'outpark')


@scene('test_cjl_c3213_wash_parking_fee_at_exit')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13100003213'], [['S321301']])[0]
    r.call('recharge_account', m, 500, want=rS)
    a = enter(r, 'S321301')
    r.occupy(a, 'S001')
    r.time_pass('hour', 1)
    r.release(a)
    wash(r, 'S321301', want='E000:001')
    r.call('start_wash_service', '001', want=rS)
    r.time_pass('min', 30)
    r.call('end_wash_service', '001', want=rS)
    r.occupy(a, 'S002')
    r.time_pass('hour', 2)
    r.release(a)
    r.leave(a)
    r.time_pass('sec', 5)
    r.status('S321301', a, 'outpark')
    r.call('query_account_asset', m)


@scene('test_cjl_c3214_wash_forced_exit_with_parking_fee')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = members(r, ['13100003214'], [['S321401']])[0]
    r.call('recharge_account', m, 500, want=rS)
    a = enter(r, 'S321401')
    t = enter(r, 'S321402')
    r.occupy(a, 'S001')
    r.occupy(t, 'S002')
    r.time_pass('hour', 2)
    r.release(a)
    r.release(t)
    wash(r, 'S321401', want='E000:001')
    wash(r, 'S321402', want='E000:002')
    r.call('start_wash_service', '001', want=rS)
    r.call('start_wash_service', '002', want=rS)
    r.time_pass('min', 20)
    r.call('end_wash_service', '001', want=rS)
    r.call('end_wash_service', '002', want=rS)
    r.time_pass('min', 15)
    r.status('S321401', a, 'outpark')
    r.status('S321402', t, 'outpark')
    r.call('query_account_asset', m)


@scene('test_cjl_c3215_wash_price_workday')
def _(r):
    m = members(r, ['13100003215'], [['S321501', 'S321502', 'S321503']])[0]
    r.call('recharge_account', m, 500, want=rS)
    r.goto(D(2026, 2, 28, 10, 0))
    wash_cycle(r, enter(r, 'S321501'), 'S321501', '001')
    r.call('query_account_asset', m)
    r.goto(D(2026, 3, 2, 12, 30))
    wash_cycle(r, enter(r, 'S321502'), 'S321502', '001')
    r.call('query_account_asset', m)
    r.goto(D(2026, 3, 2, 16, 30))
    wash_cycle(r, enter(r, 'S321503'), 'S321503', '002')
    r.call('query_account_asset', m)


@scene('test_cjl_c3216_wash_price_weekend_holiday')
def _(r):
    m = members(r, ['13100003216'], [['S321601']])[0]
    r.call('recharge_account', m, 500, want=rS)
    for day in (D(2026, 3, 7, 10, 0), D(2026, 3, 8, 10, 0), D(2026, 4, 6, 10, 0), D(2026, 5, 1, 10, 0)):
        r.goto(day)
        wash_cycle(r, enter(r, 'S321601'), 'S321601', '001')
        r.call('query_account_asset', m)


@scene('test_cjl_c3217_wash_balance_tie_and_large_to_medium')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ss = [f'S32170{i}' for i in range(1, 7)]
    cars(r, ss)
    for p in ss:
        wash(r, p)
    r.call('cancel_wash_order', '004', want=rS)
    r.call('cancel_wash_order', '005', want=rS)
    r.call('trigger_wash_balance', want=rS)
    wq(r)
    ms = [f'M3217{i:02d}' for i in range(18)]
    cars(r, ms)
    for p in ms:
        wash(r, p)
    wq(r)
    for i in range(9, 19):
        r.call('cancel_wash_order', f'{i:03d}', want=rS)
    wq(r)
    r.call('trigger_wash_balance', want=rS)
    wq(r)


@scene('test_cjl_c3218_wash_order_after_car_exit')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a, b, c, d = cars(r, ['S321801', 'S321802', 'S321803', 'S321804'])
    for p in ('S321801', 'S321802', 'S321803', 'S321804'):
        wash(r, p)
    r.call('start_wash_service', '001', want=rS)
    r.leave(d, want=None)
    r.time_pass('sec', 5)
    r.status('S321804', d, 'outpark', want=None)
    wst(r, '004')
    r.call('end_wash_service', '001', want=rS)
    r.call('start_wash_service', '004', want=None)
    wst(r, '004')
    r.call('end_wash_service', '004', want=None)
    wq(r, 'WS1')


@scene('test_cjl_c3219_wash_balance_large_car_stays')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    ps = ['L321901', 'L321902', 'L321903', 'S321904', 'M321905']
    cars(r, ps)
    for p in ps:
        wash(r, p)
    wq(r)
    r.call('trigger_wash_balance', want=rS)
    wq(r)
