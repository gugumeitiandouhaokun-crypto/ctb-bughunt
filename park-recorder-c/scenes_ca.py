# -*- coding: utf-8 -*-
"""A版本功能（在C版本jar上）：账户管理、车牌绑定、出入口管制、车辆进出。
want 为按文档推测的值，只用于录制报告对比，写入用例的是实际返回。"""
import re
from datetime import datetime as D

from record import rF, rS, scene

V = 'E002:Verify_fail'
PWD = 'Abc123!'


def enter(r, plate, gate=None, want_ok=True):
    sid, cid = r.entry(plate, gate) if gate else r.arrive(plate)
    r.time_pass('sec', 5)
    r.status(plate, sid, 'inpark' if want_ok else 'outpark')
    return sid, cid


def out(r, sid, plate, gate=None):
    r.exit(sid, gate) if gate else r.leave(sid)
    r.time_pass('sec', 5)
    r.status(plate, sid, 'outpark')


def code_of(ret):
    m = re.match(r'E000:(CID\d{8}),(\d{6})$', ret)
    return (m.group(1), m.group(2)) if m else ('CID00000000', '000000')


@scene('test_cjl_ca01_register_params')
def _(r):
    for phone in ('1380013800', '138001380001', '23800138000', '1380013800a', '0'):
        r.call('register_account', phone, PWD, want=rF)
    for i, pwd in enumerate(('Ab1!', 'Ab12!', 'Abc123!xy', 'abcdef', '123456', '!!!!!!', 'abc12~')):
        r.call('register_account', f'1380100{i:04d}', pwd, want=rF)
    for i, pwd in enumerate(('abc123', 'abc!!!', '123!@#', 'Ab1!Ab1!', 'ABCDE1')):
        r.call('register_account', f'1380200{i:04d}', pwd, want=None)
    r.call('register_account', '13802000000', PWD, want=rF)
    cid, _ = code_of(r.call('register_account', '13802000009', 'abcde$'))
    r.call('query_account', cid, want='E000:member|13802000009|inactive')


@scene('test_cjl_ca02_activate_rules')
def _(r):
    a, ca = code_of(r.call('register_account', '13803000001', PWD))
    b, cb = code_of(r.call('register_account', '13803000002', PWD))
    c, cc = code_of(r.call('register_account', '13803000003', PWD))
    r.call('activate_account', 'CID1234567', ca, want='E001:InvalidPara')
    r.call('activate_account', 'CID99999999', ca, want=None)
    r.call('activate_account', a, '12345', want=None)
    wrong = '000000' if ca != '000000' else '111111'
    r.call('activate_account', a, wrong, want='E002:InvalidCode')
    r.call('query_account', a, want='E000:member|13803000001|expired')
    r.call('activate_account', a, ca, want=None)
    r.call('activate_account', b, cb, want=rS)
    r.call('activate_account', b, cb, want=None)
    r.call('query_account', b, want='E000:member|13803000002|active')
    r.time_pass('min', 10)
    r.time_pass('sec', 1)
    r.call('activate_account', c, cc, want='E003:ExpiredCode')
    r.call('query_account', c, want='E000:member|13803000003|expired')
    r.call('register_account', '13803000001', PWD, want=rF)
    r.time_pass('hour', 23)
    r.time_pass('min', 49)
    r.time_pass('sec', 58)
    r.call('query_account', a)
    r.time_pass('sec', 2)
    r.call('query_account', a)
    r.time_pass('min', 10)
    r.call('query_account', a)
    r.call('query_account', c)
    r.time_pass('hour', 1)
    r.call('query_account', c)
    r.call('register_account', '13803000001', PWD, want=None)


@scene('test_cjl_ca03_activate_in_time')
def _(r):
    a, ca = code_of(r.call('register_account', '13803100001', PWD))
    r.time_pass('min', 9)
    r.time_pass('sec', 59)
    r.call('activate_account', a, ca, want=rS)
    b, cb = code_of(r.call('register_account', '13803100002', PWD))
    r.time_pass('min', 10)
    r.call('activate_account', b, cb, want=None)
    r.call('query_account', b)


@scene('test_cjl_ca04_unregister')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = r.member('13804000001')
    b = r.member('13804000002')
    i, _ = r.register('13804000003', PWD)
    r.call('bind_plate_id', a, 'S38041', want=rS)
    r.call('bind_plate_id', a, 'M380412', want=rS)
    r.call('unregister_account', a, 'Abc124!', want=rF)
    r.call('unregister_account', i, PWD, want=rF)
    r.call('unregister_account', 'CID00000000', PWD, want=rF)
    r.call('unregister_account', 'cid1234567', PWD, want=rF)
    r.call('unregister_account', a, PWD, want=rS)
    r.call('unregister_account', a, PWD, want=rF)
    r.call('query_account', a, want=rF)
    r.call('query_bindcar_info', a, want=rF)
    r.call('bind_plate_id', b, 'S38041', want=None)
    r.call('bind_plate_id', a, 'L38043', want=rF)
    c = r.member('13804000001')
    r.call('query_account', c, want='E000:member|13804000001|active')
    r.call('query_bindcar_info', b)


@scene('test_cjl_ca05_unregister_with_car_in_park')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = r.member('13804100001')
    r.call('bind_plate_id', a, 'S38051', want=rS)
    s, cid = enter(r, 'S38051')
    r.call('unregister_account', a, PWD, want=None)
    r.call('query_account', a)
    r.call('query_account', cid)
    r.status('S38051', s, 'inpark')
    out(r, s, 'S38051')


@scene('test_cjl_ca06_bind_rules')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = r.member('13805000001')
    b = r.member('13805000002')
    i, _ = r.register('13805000003', PWD)
    for p in ('X12345', 'L1234', 'L1234567', 'l12345', 'L1234A', 'LL12345', 'S'):
        r.call('bind_plate_id', a, p, want=rF)
    r.call('bind_plate_id', 'CID00000000', 'S38061', want=rF)
    r.call('bind_plate_id', i, 'S38061', want=rF)
    r.call('bind_plate_id', a, 'L38061', want=rS)
    r.call('bind_plate_id', a, 'L38061', want=rF)
    r.call('bind_plate_id', b, 'L38061', want=rF)
    r.call('bind_plate_id', a, 'M380612', want=rS)
    r.call('bind_plate_id', a, 'S38063', want=rS)
    r.call('bind_plate_id', a, 'S38064', want=rF)
    r.call('query_bindcar_info', a, want='E000:L38061|M380612|S38063')
    r.call('query_bindcar_info', b, want=None)
    r.call('query_bindcar_info', i, want=None)
    r.call('query_bindcar_info', 'CID123', want=rF)


@scene('test_cjl_ca07_unbind_12h')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = r.member('13806000001')
    b = r.member('13806000002')
    r.call('bind_plate_id', a, 'S38071', want=rS)
    r.call('unbind_plate_id', a, 'S38072', want=rF)
    r.call('unbind_plate_id', b, 'S38071', want=rF)
    r.call('unbind_plate_id', 'CID00000000', 'S38071', want=rF)
    r.call('unbind_plate_id', a, 'S38071', want=rS)
    r.call('unbind_plate_id', a, 'S38071', want=rF)
    r.call('query_bindcar_info', a)
    r.call('bind_plate_id', a, 'S38071', want=rF)
    r.time_pass('hour', 11)
    r.time_pass('min', 59)
    r.time_pass('sec', 59)
    r.call('bind_plate_id', b, 'S38071', want=rF)
    r.time_pass('sec', 1)
    r.call('bind_plate_id', b, 'S38071', want=None)
    r.call('query_bindcar_info', b)


@scene('test_cjl_ca08_temp_account')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    s, cid = enter(r, 'S38081')
    r.call('query_account', cid, want='E000:temp|null|active')
    r.call('query_bindcar_info', cid, want=None)
    r.call('unregister_account', cid, PWD, want=rF)
    r.call('bind_plate_id', cid, 'S38082', want=rF)
    out(r, s, 'S38081')
    r.call('query_account', cid, want=rF)
    r.call('change_gate', 'G1', 'B', want=rS)
    t, tc = r.entry('S38083', 'G1')
    r.call('query_account', tc)
    r.time_pass('sec', 5)
    r.status('S38083', t, 'outpark')
    r.call('query_account', tc, want=rF)


@scene('test_cjl_ca09_temp_car_bind_in_park')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    m = r.member('13809000001')
    s, cid = enter(r, 'S38091')
    t, tc = enter(r, 'S38092')
    r.call('bind_plate_id', m, 'S38091', want=rS)
    r.occupy(t, 'S001')
    r.call('bind_plate_id', m, 'S38092', want=rS)
    r.call('query_account', cid)
    r.call('query_bindcar_info', m)
    r.release(t)
    out(r, s, 'S38091')
    out(r, t, 'S38092')
    _, cid2 = enter(r, 'S38091')
    r.call('query_account', cid2)


@scene('test_cjl_ca10_query_params')
def _(r):
    for cid in ('CID1234567', 'CID123456789', 'cid12345678', 'XID12345678', 'CID00000000'):
        r.call('query_account', cid, want=rF)
        r.call('query_bindcar_info', cid, want=rF)
    for g in ('G0', 'G5', 'g1', 'G'):
        r.call('query_gate_status', g, want=rF)
    for g in ('G1', 'G2', 'G3', 'G4'):
        r.call('query_gate_status', g, want='E000:IO|default')


@scene('test_cjl_ca11_change_gate_manual')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    for g, d in (('G5', 'I'), ('G1', 'X'), ('G1', 'io'), ('G0', 'O')):
        r.call('change_gate', g, d, want=rF)
    r.call('change_gate', 'G1', 'B', want=rS)
    r.call('query_gate_status', 'G1', want='E000:B|manual')
    r.call('car_entry', 'S38111', 'G1', want=None)
    r.time_pass('sec', 5)
    r.status('S38111', None, 'outpark')
    r.call('change_gate', 'G1', 'O', want=rS)
    r.call('change_gate', 'G2', 'I', want=rS)
    s, _ = r.entry('S38112', 'G1')
    r.time_pass('sec', 5)
    r.status('S38112', s, 'outpark')
    s, _ = enter(r, 'S38112', 'G2')
    r.exit(s, 'G2', want=None)
    r.time_pass('sec', 5)
    r.status('S38112', s, 'inpark')
    out(r, s, 'S38112', 'G1')
    r.call('change_gate', 'G1', 'IO', want=rS)
    r.call('query_gate_status', 'G1', want='E000:IO|manual')
    r.call('query_gate_status', 'G2', want='E000:I|manual')


@scene('test_cjl_ca12_tide_and_restore')
def _(r):
    r.goto(D(2026, 3, 2, 6, 59, 59))
    r.call('change_gate', 'G2', 'B', want=rS)
    r.call('change_gate', 'G4', 'I', want=rS)
    r.time_pass('sec', 1)
    for g in ('G1', 'G2', 'G3', 'G4'):
        r.call('query_gate_status', g)
    r.call('change_gate', 'G1', 'O', want=None)
    r.call('query_gate_status', 'G1')
    r.time_pass('hour', 1)
    r.time_pass('min', 59)
    r.time_pass('sec', 59)
    r.call('query_gate_status', 'G4')
    r.time_pass('sec', 1)
    for g in ('G1', 'G2', 'G3', 'G4'):
        r.call('query_gate_status', g)
    r.goto(D(2026, 3, 2, 17, 0))
    for g in ('G1', 'G4'):
        r.call('query_gate_status', g)
    r.time_pass('hour', 2)
    for g in ('G1', 'G2', 'G4'):
        r.call('query_gate_status', g)


@scene('test_cjl_ca13_tide_non_workday')
def _(r):
    for day in (D(2026, 3, 7, 8, 0), D(2026, 4, 6, 8, 0), D(2026, 5, 9, 8, 0), D(2026, 5, 11, 8, 0)):
        r.goto(day)
        r.call('query_gate_status', 'G1')
        r.call('query_gate_status', 'G4')
        r.goto(day.replace(hour=18))
        r.call('query_gate_status', 'G1')
        r.call('query_gate_status', 'G4')


@scene('test_cjl_ca14_switch_while_passing')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    s, _ = r.entry('S38141', 'G1')
    r.call('change_gate', 'G1', 'O', want=rS)
    r.call('query_gate_status', 'G1')
    r.time_pass('sec', 5)
    r.status('S38141', s, 'inpark')
    r.call('query_gate_status', 'G1')
    t, _ = r.entry('S38142', 'G1')
    r.time_pass('sec', 5)
    r.status('S38142', t, 'outpark')
    r.exit(s, 'G1')
    r.call('change_gate', 'G1', 'I', want=rS)
    r.time_pass('sec', 5)
    r.status('S38141', s, 'outpark')
    r.goto(D(2026, 3, 2, 16, 59, 57))
    u, _ = r.entry('S38143', 'G1')
    r.time_pass('sec', 3)
    r.call('query_gate_status', 'G1')
    r.time_pass('sec', 2)
    r.status('S38143', u, 'inpark')
    r.call('query_gate_status', 'G1')


@scene('test_cjl_ca15_gate_flow_limit')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    s, _ = r.entry('S38151', 'G1')
    r.call('car_entry', 'S38152', 'G1', want=rF)
    r.time_pass('sec', 4)
    r.call('car_entry', 'S38152', 'G1', want=rF)
    r.time_pass('sec', 1)
    t = r.call('car_entry', 'S38152', 'G1', want=None)
    r.call('car_entry', 'S38151', 'G2', want=rF)
    r.time_pass('sec', 5)
    r.status('S38151', s, 'inpark')
    r.exit(s, 'G3')
    r.call('car_exit', s, 'G3', want=rF)
    r.call('car_exit', s, 'G4', want=rF)
    r.status('S38151', s, 'exiting')
    r.time_pass('sec', 5)
    r.status('S38151', s, 'outpark')
    r.call('car_exit', s, 'G4', want=rF)
    r.call('car_exit', 'p0000000000000000', 'G4', want=rF)
    r.call('car_exit', 'p123', 'G4', want=rF)
    r.note(t)


@scene('test_cjl_ca16_car_status_params')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    s, _ = r.entry('S38161', 'G1')
    r.status('S38161', s, 'entrying')
    r.status('S38161', s, 'inpark', want=V)
    r.call('check_car_status', 'S38161', s, 'flying', want=rF)
    r.call('check_car_status', 'S3816', s, 'entrying', want=rF)
    r.call('check_car_status', 'S38162', s, 'entrying', want=None)
    r.call('check_car_status', 'S38161', 'p1', 'entrying', want=rF)
    r.call('check_car_status', 'S38161', 'null', 'entrying', want=None)
    r.call('check_car_status', 'S38163', 'null', 'outpark', want=rS)
    r.call('check_car_status', 'S38163', 'null', 'inpark', want=None)
    r.time_pass('sec', 5)
    r.occupy(s, 'S001')
    r.status('S38161', s, 'parked')
    r.call('car_exit', s, 'G2', want=None)
    r.release(s)
    r.status('S38161', s, 'inpark')


@scene('test_cjl_ca17_inpark_timeout')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    s, _ = enter(r, 'S38171')
    t, _ = enter(r, 'S38172')
    r.occupy(t, 'S001')
    r.time_pass('min', 14)
    r.time_pass('sec', 54)
    r.status('S38171', s, 'inpark')
    r.time_pass('sec', 1)
    r.status('S38171', s, 'outpark')
    r.release(t)
    r.time_pass('min', 14)
    r.time_pass('sec', 59)
    r.status('S38172', t, 'inpark')
    r.time_pass('sec', 1)
    r.status('S38172', t, 'outpark')


@scene('test_cjl_ca19_unregister_blocked')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = r.member('13819000001')
    r.call('recharge_account', a, 10, want=rS)
    r.call('unregister_account', a, PWD, want=rF)
    b = r.member('13819000002')
    r.call('bind_plate_id', b, 'S38192', want=rS)
    s, _ = enter(r, 'S38192')
    r.occupy(s, 'S001')
    r.call('unregister_account', b, PWD, want=rF)
    c, cc = code_of(r.call('register_account', '13819000003', PWD))
    r.call('activate_account', c, '\t', want='E001:InvalidPara')
    r.call('activate_account', c, '000000' if cc != '000000' else '111111', want='E002:InvalidCode')
    r.call('unregister_account', c, PWD, want=rF)
    d = r.member('13819000004')
    r.call('bind_plate_id', d, 'S381904', want=rS)
    rid = r.call('reserve_space', 'S381904', 'S002')
    r.call('unregister_account', d, PWD, want=rF)
    r.call('unbind_plate_id', d, 'S381904', want=rF)
    r.call('cancel_reserve', rid.split(':')[-1], want=rS)
    r.call('unregister_account', d, PWD, want=rS)


@scene('test_cjl_ca20_unbind_blocked')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = r.member('13820000001')
    i, _ = r.register('13820000002', PWD)
    r.call('unbind_plate_id', i, 'S38201', want=rF)
    r.call('bind_plate_id', a, 'S38201', want=rS)
    s, _ = r.entry('S38201', 'G1')
    r.call('unbind_plate_id', a, 'S38201', want=rF)
    r.time_pass('sec', 5)
    r.call('unbind_plate_id', a, 'S38201', want=rF)
    r.occupy(s, 'S001')
    r.call('unbind_plate_id', a, 'S38201', want=rF)
    r.release(s)
    out(r, s, 'S38201')
    r.call('unbind_plate_id', a, 'S38201', want=rS)


@scene('test_cjl_ca21_bind_while_passing')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    a = r.member('13821000001')
    s, _ = r.entry('S38211', 'G1')
    r.call('bind_plate_id', a, 'S38211', want=rF)
    r.call('car_exit', s, 'G2', want=rF)
    r.time_pass('sec', 5)
    t, _ = enter(r, 'S38212', 'G2')
    r.exit(t, 'G3')
    r.call('bind_plate_id', a, 'S38212', want=rF)
    r.time_pass('sec', 5)
    r.status('S38212', t, 'outpark')
    r.call('bind_plate_id', a, 'S38212', want=rS)
    r.call('bind_plate_id', a, 'S38211', want=rS)
    r.call('query_bindcar_info', a)


@scene('test_cjl_ca22_member_capacity_500')
def _(r):
    accs = [code_of(r.call('register_account', f'139{i:08d}', PWD)) for i in range(501)]
    for cid, code in accs[:500]:
        r.call('activate_account', cid, code)
    cid, code = accs[500]
    r.call('activate_account', cid, code, want='E004:CapacityFull')
    r.call('query_account', cid)
    r.call('register_account', '13999999998', PWD, want=rF)
    r.call('unregister_account', accs[0][0], PWD, want=rS)
    r.call('register_account', '13999999998', PWD)


@scene('test_cjl_ca18_time_pass_params')
def _(r):
    for unit, v in (('week', 1), ('sec', 0), ('sec', -1), ('min', 10001), ('Hour', 1)):
        r.call('time_pass', unit, v, want=rF)
    r.time_pass('day', 364)
    r.time_pass('hour', 23)
    r.time_pass('min', 59)
    r.call('time_pass', 'sec', 60, want=None)
    r.call('time_pass', 'sec', 59, want=None)
    r.call('time_pass', 'sec', 1, want=None)
