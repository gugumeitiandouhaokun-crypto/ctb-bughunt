# -*- coding: utf-8 -*-
"""B版本步骤。want 为按规格推测的值，只用于录制报告对比，写入用例的是实际返回。"""
from datetime import datetime as D, timedelta

from record import plates, rF, rS, scene, spaces

TEMP = 'E000:temp|null|active'
SEC = timedelta(seconds=1)
MIN = timedelta(minutes=1)
HOUR = timedelta(hours=1)


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


def free_space(r, kind, taken):
    used = set(s for s in r.ask('occupied').split('|') if s) | taken
    total = {'S': 70, 'M': 200, 'L': 10}[kind]
    for i in range(1, total + 1):
        sp = f'{kind}{i:03d}'
        if sp not in used:
            return sp
    raise ValueError(f'{kind} 车位不足')


def park(r, specs, lead=5 * MIN, lag=MIN):
    """按时间线执行一批停车会话。
    spec: plate, space(车位类型或编号), occ, rel, pay(bal/temp/auto/none), probes(金额偏差列表)
    入场在 occ-lead，离位后立即支付，rel+lag 发起出场；occ/rel 必须准点执行。
    返回 [(session_id, account_id)]。"""
    events = []
    for i, s in enumerate(specs):
        events += [(s['occ'] - lead, 3, i, 'in'), (s['occ'], 1, i, 'occ'), (s['rel'], 0, i, 'rel'),
                   (s['rel'] + lag, 2, i, 'out')]
    events.sort()
    ids, taken = {}, set()
    for t, _, i, act in events:
        s = specs[i]
        if t > r.now:
            r.goto(t)
        elif act in ('occ', 'rel') and t < r.now:
            raise ValueError(f'{r.name}: {s["plate"]} {act} 计划 {t} 已晚于当前 {r.now}')
        if act == 'in':
            ids[i] = r.arrive(s['plate'])
        elif act == 'occ':
            sp = s['space'] if len(s['space']) > 1 else free_space(r, s['space'], taken)
            r.occupy(ids[i][0], sp)
        elif act == 'rel':
            sid = ids[i][0]
            r.release(sid)
            kind = s.get('pay', 'auto')
            for d in s.get('probes', ()):
                r.pay(sid, kind if kind in ('bal', 'temp') else 'auto', delta=d)
            if kind != 'none':
                r.pay(sid, kind, want=rS if r.due(sid)['due'] > 0 else rF)
        else:
            r.leave(ids[i][0])
    r.time_pass('sec', 5)
    return [ids[i] for i in range(len(specs))]


def spec(plate, space, occ, rel, pay='auto', probes=()):
    return {'plate': plate, 'space': space, 'occ': occ, 'rel': rel, 'pay': pay, 'probes': probes}


@scene('test_cjl_02b_recharge_cap_exact_5000')
def _(r):
    cid, code = r.register('13800138011', 'Abc123!')
    r.call('activate_account', cid, code, want=rS)
    for _ in range(7):
        r.call('recharge_account', cid, 500, want=rS)
    r.call('recharge_account', cid, 40, want=rS)
    r.call('query_account_asset', cid, want='E000:4380.00|Null:000|Null:000|Null:000')
    r.call('recharge_account', cid, 500, want=rS)
    r.call('query_account_asset', cid, want='E000:5000.00|Null:000|Null:000|Null:000')
    r.call('recharge_account', cid, 1, want=rF)
    r.call('recharge_account', cid, 51, want=rF)
    r.call('recharge_account', cid, 0, want=rF)
    r.call('query_account_asset', cid, want='E000:5000.00|Null:000|Null:000|Null:000')


@scene('test_cjl_s1b2_reoccupy_first_type_after_release')
def _(r):
    a, b = r.enter(['S00001', 'S00002'])
    r.occupy(a[0], 'S001')
    r.occupy(b[0], 'L001')
    r.stats('1|0', '0|0', '1|0')
    r.release(a[0])
    r.release(b[0])
    r.stats('0|2', '0|0', '0|0')
    r.occupy(a[0], 'M001', want=rF)
    r.occupy(a[0], 'L002', want=rF)
    r.occupy(b[0], 'S001', want=rF)
    r.occupy(b[0], 'M001', want=rF)
    r.stats('0|2', '0|0', '0|0')
    r.occupy(a[0], 'S002')
    r.occupy(b[0], 'L002')
    r.stats('1|0', '0|0', '1|0')
    r.check_occupied()


@scene('test_cjl_s1m2_l_full_entry_fail_outpark')
def _(r):
    r.enter(plates('S', 1, 10), spaces('L', 1, 10))
    r.stats('0|0', '0|0', '10|0')
    rej = r.enter(['L00001'])
    r.status('L00001', None, 'outpark')
    r.call('query_account', rej[0][1], want=rF)
    r.stats('0|0', '0|0', '10|0')
    r.check_occupied()


@scene('test_cjl_s1n2_release_relock_then_l_reentry')
def _(r):
    cars = r.enter(plates('S', 1, 10), spaces('L', 1, 10))
    r.enter(['L00001'])
    r.status('L00001', None, 'outpark')
    r.release(cars[0][0])
    r.stats('0|1', '0|0', '9|0')
    big = r.enter(['L00001'])
    r.status('L00001', big[0][0], 'inpark')
    r.stats('0|1', '0|0', '9|1')
    r.check_occupied()


@scene('test_cjl_s1q_s_full_then_lock_m')
def _(r):
    r.enter(plates('S', 1, 70), spaces('S', 1, 70))
    r.stats('70|0', '0|0', '0|0')
    r.check_occupied()
    extra = r.enter(plates('S', 71, 4))
    r.stats('70|0', '0|4', '0|0')
    for p, (sid, cid) in zip(plates('S', 71, 4), extra):
        r.status(p, sid, 'inpark')
        r.call('query_account', cid, want=TEMP)
    r.occupy(extra[0][0], 'S001', want=rF)
    r.occupy(extra[0][0], 'M001')
    r.stats('70|0', '1|3', '0|0')
    r.occupy(extra[1][0], 'L001')
    r.stats('70|0', '1|2', '1|0')
    r.occupy(extra[2][0], 'S070', want=rF)
    r.occupy(extra[2][0], 'M001', want=rF)
    r.occupy(extra[2][0], 'M200')
    r.stats('70|0', '2|1', '1|0')
    r.check_occupied()


@scene('test_cjl_s1r_m_full_then_lock_l_and_reject')
def _(r):
    r.enter(plates('M', 1, 200), spaces('M', 1, 200))
    r.stats('0|0', '200|0', '0|0')
    over = r.enter(plates('M', 201, 10))
    r.stats('0|0', '200|0', '0|10')
    rej = r.enter(['M00211', 'L00001'])
    r.status('M00211', None, 'outpark')
    r.status('L00001', None, 'outpark')
    r.call('query_account', rej[0][1], want=rF)
    r.call('query_account', rej[1][1], want=rF)
    r.stats('0|0', '200|0', '0|10')
    small = r.enter(['S00001'])
    r.status('S00001', small[0][0], 'inpark')
    r.stats('0|1', '200|0', '0|10')
    r.occupy(over[0][0], 'L001')
    r.stats('0|1', '200|0', '1|9')
    r.enter(['L00002'])
    r.status('L00002', None, 'outpark')
    r.stats('0|1', '200|0', '1|9')
    r.occupy(over[1][0], 'S001', want=rF)
    r.occupy(over[1][0], 'L002')
    r.occupy(small[0][0], 'S070')
    r.stats('1|0', '200|0', '2|8')
    r.check_occupied()


@scene('test_cjl_s1s_s_overflow_to_l_then_all_reject')
def _(r):
    r.enter(plates('S', 1, 70), spaces('S', 1, 70))
    r.enter(plates('M', 1, 200), spaces('M', 1, 200))
    r.stats('70|0', '200|0', '0|0')
    over = r.enter(plates('S', 71, 10))
    r.stats('70|0', '200|0', '0|10')
    r.enter(['L00001', 'M00201', 'S00081'])
    r.status('L00001', None, 'outpark')
    r.status('M00201', None, 'outpark')
    r.status('S00081', None, 'outpark')
    r.stats('70|0', '200|0', '0|10')
    r.occupy(over[0][0], 'S001', want=rF)
    r.occupy(over[0][0], 'M001', want=rF)
    r.occupy(over[0][0], 'L001')
    r.stats('70|0', '200|0', '1|9')
    r.enter(['L00002', 'S00082'])
    r.status('L00002', None, 'outpark')
    r.status('S00082', None, 'outpark')
    r.stats('70|0', '200|0', '1|9')
    for (sid, _), sp in zip(over[1:], spaces('L', 2, 9)):
        r.occupy(sid, sp)
    r.stats('70|0', '200|0', '10|0')
    r.check_occupied()


@scene('test_cjl_s1t_lock_only_counts_toward_full')
def _(r):
    locked = r.enter(plates('L', 1, 10))
    r.stats('0|0', '0|0', '0|10')
    mixed = r.enter(['L00011', 'M00001', 'S00001'])
    r.status('L00011', None, 'outpark')
    r.call('query_account', mixed[0][1], want=rF)
    r.status('M00001', mixed[1][0], 'inpark')
    r.status('S00001', mixed[2][0], 'inpark')
    r.call('query_account', mixed[1][1], want=TEMP)
    r.stats('0|1', '0|1', '0|10')
    r.occupy(locked[4][0], 'L005')
    r.stats('0|1', '0|1', '1|9')
    r.enter(['L00012'])
    r.status('L00012', None, 'outpark')
    r.occupy(mixed[1][0], 'L006')
    r.stats('0|1', '0|0', '2|9')
    r.occupy(mixed[2][0], 'L007')
    r.stats('0|0', '0|0', '3|9')
    r.check_occupied()


@scene('test_cjl_s1u_concurrent_requests_for_last_slot')
def _(r):
    r.enter(plates('L', 1, 9), spaces('L', 1, 9))
    r.stats('0|0', '0|0', '9|0')
    first = r.entry('L00010', 'G1')
    r.entry('L00011', 'G2')
    r.time_pass('sec', 2)
    r.status('L00010', first[0], 'entrying')
    r.stats('0|0', '0|0', '9|0')
    r.time_pass('sec', 3)
    r.status('L00010', first[0], 'inpark')
    r.status('L00011', None, 'outpark')
    r.stats('0|0', '0|0', '9|1')
    r.occupy(first[0], 'L010')
    r.stats('0|0', '0|0', '10|0')
    r.check_occupied()


@scene('test_cjl_s1v_first_type_l_stuck_then_forced_exit')
def _(r):
    cars = r.enter(plates('S', 1, 10), spaces('L', 1, 10))
    r.stats('0|0', '0|0', '10|0')
    r.release(cars[0][0])
    r.stats('0|1', '0|0', '9|0')
    r.status('S00001', cars[0][0], 'inpark')
    big = r.enter(['L00001'])
    r.status('L00001', big[0][0], 'inpark')
    r.stats('0|1', '0|0', '9|1')
    r.occupy(big[0][0], 'L001')
    r.stats('0|1', '0|0', '10|0')
    r.occupy(cars[0][0], 'S001', want=rF)
    r.occupy(cars[0][0], 'M001', want=rF)
    r.occupy(cars[0][0], 'L002', want=rF)
    r.stats('0|1', '0|0', '10|0')
    r.time_pass('min', 16)
    r.status('S00001', None, 'outpark')
    r.stats('0|0', '0|0', '10|0')
    r.check_occupied()


@scene('test_cjl_s1w_m_locked_s_car_takes_freed_s_space')
def _(r):
    cars = r.enter(plates('S', 1, 70), spaces('S', 1, 70))
    over = r.enter(plates('S', 71, 2))
    r.stats('70|0', '0|2', '0|0')
    r.release(cars[0][0])
    r.stats('69|1', '0|2', '0|0')
    r.exit(cars[0][0], 'G1')
    r.time_pass('sec', 5)
    r.status('S00001', None, 'outpark')
    r.stats('69|0', '0|2', '0|0')
    r.occupy(over[0][0], 'S001')
    r.stats('70|0', '0|1', '0|0')
    r.enter(['S00073'])
    r.stats('70|0', '0|2', '0|0')
    r.occupy(over[1][0], 'M001')
    r.stats('70|0', '1|1', '0|0')
    r.check_occupied()


@scene('test_cjl_s1x_l_full_exit_then_refill_exactly_one')
def _(r):
    cars = r.enter(plates('L', 1, 10), spaces('L', 1, 10))
    r.enter(['L00011'])
    r.status('L00011', None, 'outpark')
    r.release(cars[0][0])
    r.stats('0|0', '0|0', '9|1')
    r.enter(['L00011'])
    r.status('L00011', None, 'outpark')
    r.exit(cars[0][0], 'G1')
    r.time_pass('sec', 5)
    r.status('L00001', None, 'outpark')
    r.stats('0|0', '0|0', '9|0')
    back = r.enter(['L00011', 'L00012'])
    r.status('L00011', back[0][0], 'inpark')
    r.status('L00012', None, 'outpark')
    r.stats('0|0', '0|0', '9|1')
    r.occupy(back[0][0], 'L001')
    r.stats('0|0', '0|0', '10|0')
    r.check_occupied()


@scene('test_cjl_s1y_priority_split_within_one_batch')
def _(r):
    r.enter(plates('S', 1, 68), spaces('S', 1, 68))
    r.stats('68|0', '0|0', '0|0')
    batch = r.enter(plates('S', 69, 4))
    r.stats('68|2', '0|2', '0|0')
    r.occupy(batch[0][0], 'S069')
    r.stats('69|1', '0|2', '0|0')
    r.occupy(batch[2][0], 'M001')
    r.stats('69|1', '1|1', '0|0')
    r.occupy(batch[1][0], 'S070')
    r.occupy(batch[3][0], 'L001')
    r.stats('70|0', '1|0', '1|0')
    r.enter(['M00001'])
    r.stats('70|0', '1|1', '1|0')
    r.check_occupied()


# ======================================================================== 复杂场景

def slot(occ, plan, prefix, pays=('auto',), probes=(), probe_first=False):
    """plan: [((车型, 车位类型), [时长秒...])]；同车型车牌按 prefix 顺序分配"""
    idx, out = {}, []
    for (car, sp), durs in plan:
        for k, dur in enumerate(durs):
            j = idx.get(car, 0) + 1
            idx[car] = j
            out.append(spec(f'{car}{prefix}{j:04d}', sp, occ, occ + timedelta(seconds=dur),
                            pays[(j - 1) % len(pays)], probes if probe_first and k == 0 else ()))
    return out


def end_checks(r, cids=(), scopes=('day',)):
    r.stats()
    r.check_occupied()
    for cid in cids:
        r.asset(cid)
    r.revenue_all(scopes)


@scene('test_cjl_b3_billing_matrix_workdays')
def _(r):
    r.goto(D(2026, 1, 5, 9, 0))
    n = 12
    mplates = [[f'S1{i:04d}', f'M1{i:04d}', f'L1{i:04d}'] for i in range(1, n + 1)]
    cids = members(r, [f'1370000{i:04d}' for i in range(1, n + 1)], mplates, [(500,)] * (n - 1) + [(10,)])
    short_m = [(('S', 'S'), [1800, 1801, 3600, 3601]), (('S', 'M'), [1800, 1801, 3600, 3601]),
               (('M', 'M'), [1800, 1801, 3600, 3601]), (('S', 'L'), [1800, 1801, 3601]),
               (('M', 'L'), [1801, 3600, 3601]), (('L', 'L'), [1801, 3600])]
    short_t = [(('S', 'S'), [1800, 1801, 3600, 3601]), (('S', 'M'), [1800, 1801, 3600, 3601]),
               (('M', 'M'), [1800, 1801, 3600, 3601]), (('S', 'L'), [1801, 3600]),
               (('M', 'L'), [1800, 3601]), (('L', 'L'), [1800, 1801, 3601])]
    combos = (('S', 'S'), ('S', 'M'), ('S', 'L'), ('M', 'M'), ('M', 'L'), ('L', 'L'))
    m1 = slot(D(2026, 1, 5, 10, 0), short_m, '1', pays=('bal', 'none', 'auto'), probes=(0.05, -0.05, 1), probe_first=True)
    ids = park(r, m1)
    for s, (sid, _) in zip(m1, ids):
        r.status(s['plate'], sid, 'outpark')
    end_checks(r, cids)
    t1 = slot(D(2026, 1, 5, 12, 0), short_t, '2', pays=('temp',), probes=(0.05, -0.05, -1), probe_first=True)
    ids = park(r, t1)
    for s, (sid, _) in zip(t1, ids):
        r.status(s['plate'], sid, 'outpark')
    end_checks(r)
    occ = D(2026, 1, 5, 13, 30)
    long_plan_m = [(c, [4 * 3600 + 300]) for c in combos] + [(('S', 'S'), [32400, 34200])]
    long_plan_t = [(c, [5 * 3600 + 1]) for c in combos] + [(('S', 'S'), [32400]), (('L', 'L'), [32400])]
    park(r, slot(occ, long_plan_m, '1', pays=('bal', 'none')) + slot(occ, long_plan_t, '2', pays=('temp',)))
    end_checks(r, cids)
    for occ, rel in ((D(2026, 1, 5, 23, 40), D(2026, 1, 6, 0, 11)),
                     (D(2026, 1, 6, 23, 30), D(2026, 1, 7, 23, 0)),
                     (D(2026, 1, 7, 23, 50), D(2026, 1, 9, 10, 0))):
        dur = int((rel - occ).total_seconds())
        park(r, slot(occ, [(c, [dur]) for c in combos], '1', pays=('auto', 'none', 'bal'))
             + slot(occ, [(c, [dur]) for c in combos], '2', pays=('temp',)))
        end_checks(r, cids)
    r.revenue_all(('month', 'year'))
    r.revenue('day', 'temppay', delta=0.05)
    r.revenue('month', 'balpay', delta=-0.05)


def compatible(car):
    return {'S': 'SML', 'M': 'ML', 'L': 'L'}[car]


@scene('test_cjl_b2_reoccupy_first_type_and_quota_timeout')
def _(r):
    r.goto(D(2026, 1, 6, 10, 0))
    cids = members(r, ['13600000001', '13600000002', '13600000003', '13600000004'],
                   [['S30001', 'M30001', 'L30001'], ['S30002', 'M30002', 'L30002'],
                    ['S30003', 'M30003', 'S30005'], ['S30004', 'M30004', 'M30005']],
                   [(), (50,), (1, 1), (500,)])
    plan = [('S30001', 'L001'), ('M30001', 'L002'), ('L30001', 'L003'), ('S30002', 'M001'), ('M30002', 'M002'),
            ('L30002', 'L004'), ('S30003', 'S001'), ('M30003', 'L005'), ('S30005', 'M003'), ('S30004', 'L006'),
            ('M30004', 'M004'), ('M30005', 'L007'), ('S40001', 'L008'), ('S40002', 'M005'), ('S40003', 'S002'),
            ('M40001', 'L009'), ('M40002', 'M006'), ('L40001', 'L010'), ('S40004', 'S003'), ('M40003', 'M007')]
    first = dict(plan)
    ids = {}
    for p, _ in plan:
        ids[p] = r.arrive(p)
    r.status(plan[-1][0], ids[plan[-1][0]][0], 'entrying')
    r.time_pass('sec', 5)
    r.stats()
    for p, sp in plan:
        r.occupy(ids[p][0], sp)
    r.stats()
    r.check_occupied()
    r.time_pass('hour', 2)
    group_a = [p for p, _ in plan[0::2]]
    group_b = [p for p, _ in plan[1::2]]
    for p in group_a:
        r.release(ids[p][0])
    t_rel1 = r.now
    r.stats()
    r.check_occupied()
    reocc, idle = group_a[:5], group_a[5:]
    for p in reocc:
        sid, kind = ids[p][0], first[p][0]
        for other in compatible(p[0]):
            if other != kind:
                r.occupy(sid, free_space(r, other, set()), want=rF)
        r.occupy(sid, free_space(r, kind, {first[p]}), want=rS)
        r.stats()
    r.check_occupied()
    r.time_pass('min', 10)
    for p in reocc:
        r.release(ids[p][0])
    t_rel2 = r.now
    r.stats()
    r.goto(t_rel1 + 15 * MIN - SEC)
    for p in idle:
        r.status(p, ids[p][0], 'inpark')
    r.stats()
    r.time_pass('sec', 1)
    for p in idle:
        r.status(p, ids[p][0], 'outpark')
    r.stats()
    r.check_occupied()
    late = [r.arrive(p) for p in ('S40010', 'M40010', 'L40010')]
    t_late = r.now
    r.goto(t_rel2 + 10 * MIN)
    for p in reocc[2:4]:
        r.pay(ids[p][0], want=rS)
        r.leave(ids[p][0])
    r.goto(t_rel2 + 15 * MIN - SEC)
    for p in reocc:
        r.status(p, ids[p][0], 'outpark' if p in reocc[2:4] else 'inpark')
    r.time_pass('sec', 1)
    for p in reocc:
        r.status(p, ids[p][0], 'outpark')
    for cid in cids:
        r.asset(cid)
    r.revenue_all(('day',))
    r.goto(t_late + 5 * SEC + 15 * MIN - SEC)
    for p, (sid, _) in zip(('S40010', 'M40010', 'L40010'), late):
        r.status(p, sid, 'inpark')
    r.stats()
    r.time_pass('sec', 1)
    for p, (sid, _) in zip(('S40010', 'M40010', 'L40010'), late):
        r.status(p, sid, 'outpark')
    r.stats()
    debtor = 'M30001'
    sid = ids[debtor][0]
    r.release(sid)
    r.leave(sid)
    r.time_pass('sec', 5)
    r.status(debtor, sid, 'inpark')
    r.pay(sid, 'bal')
    r.pay(sid, 'temp', delta=0.05)
    r.pay(sid, 'temp', want=rS)
    r.asset(cids[0])
    r.leave(sid)
    r.time_pass('sec', 5)
    r.status(debtor, sid, 'outpark')
    for p in group_b:
        if p == debtor:
            continue
        r.release(ids[p][0])
        r.pay(ids[p][0])
        r.leave(ids[p][0])
    r.time_pass('sec', 5)
    for p in group_b:
        r.status(p, ids[p][0], 'outpark')
    end_checks(r, cids, ('day', 'month', 'year'))


@scene('test_cjl_b8_pay_exit_timing_refund')
def _(r):
    r.goto(D(2026, 1, 7, 10, 0))
    m1, m2 = members(r, ['13500000001', '13500000002'], [['S50001', 'M50001'], ['L50001', 'S50002']],
                     [(500,), (5,)])
    cars = ['S50001', 'M50001', 'L50001', 'S50002', 'S60001', 'M60001', 'L60001', 'S60002', 'M60002', 'S60003']
    sps = ['M010', 'L001', 'L002', 'S010', 'L003', 'M011', 'L004', 'S011', 'M012', 'S012']
    ids = {p: r.arrive(p) for p in cars}
    r.time_pass('sec', 5)
    for p, s in zip(cars, sps):
        r.occupy(ids[p][0], s)
    r.time_pass('hour', 1)
    r.time_pass('min', 7)

    def done(p):
        r.time_pass('sec', 5)
        r.status(p, ids[p][0], 'outpark')

    sid = ids['S60001'][0]
    r.release(sid)
    r.leave(sid)
    r.time_pass('sec', 5)
    r.status('S60001', sid, 'inpark')
    r.pay(sid, 'temp', want=rS)
    r.leave(sid)
    done('S60001')

    sid = ids['M60001'][0]
    r.release(sid)
    r.pay(sid, 'temp', want=rS)
    r.revenue('day', 'temppay')
    r.call('change_gate', 'G2', 'I', want=rS)
    r.exit(sid, 'G2')
    r.time_pass('sec', 5)
    r.status('M60001', sid, 'inpark')
    r.revenue('day', 'temppay')
    r.pay(sid, 'temp', want=rS)
    r.call('change_gate', 'G2', 'IO', want=rS)
    r.exit(sid, 'G2')
    done('M60001')
    r.revenue('day', 'temppay')

    sid = ids['S50001'][0]
    r.release(sid)
    r.pay(sid, 'bal', want=rS)
    r.asset(m1)
    r.revenue('day', 'balpay')
    r.call('change_gate', 'G3', 'B', want=rS)
    r.exit(sid, 'G3')
    r.time_pass('sec', 5)
    r.status('S50001', sid, 'inpark')
    r.asset(m1)
    r.revenue('day', 'balpay')
    r.call('change_gate', 'G3', 'IO', want=rS)
    r.exit(sid, 'G3')
    done('S50001')
    r.asset(m1)
    r.revenue('day', 'balpay')

    sid = ids['L50001'][0]
    r.release(sid)
    r.pay(sid, 'bal', want=rF)
    r.pay(sid, 'temp', want=rS)
    r.asset(m2)
    r.call('change_gate', 'G1', 'I', want=rS)
    r.exit(sid, 'G1')
    r.time_pass('sec', 5)
    r.status('L50001', sid, 'inpark')
    r.asset(m2)
    r.revenue('day', 'temppay')
    r.revenue('day', 'balpay')
    r.call('change_gate', 'G1', 'IO', want=rS)
    r.pay(sid, 'temp', want=rS)
    r.exit(sid, 'G1')
    done('L50001')
    r.asset(m2)

    sid = ids['S60002'][0]
    r.release(sid)
    r.pay(sid, 'temp', want=rS)
    r.occupy(sid, free_space(r, 'S', {'S011'}))
    r.time_pass('min', 10)
    r.release(sid)
    r.pay(sid, 'temp', want=rF)
    r.leave(sid)
    done('S60002')

    sid = ids['M60002'][0]
    r.release(sid)
    r.pay(sid, 'temp', want=rS)
    r.occupy(sid, free_space(r, 'M', {'M012'}))
    r.time_pass('min', 20)
    r.release(sid)
    r.pay(sid, 'temp', delta=-0.05)
    r.pay(sid, 'temp', want=rS)
    r.leave(sid)
    done('M60002')

    sid = ids['S60003'][0]
    r.pay(sid, 'temp', want=rS)
    r.time_pass('min', 5)
    r.release(sid)
    r.leave(sid)
    done('S60003')

    sid = ids['L60001'][0]
    r.pay(sid, 'temp', want=rS)
    r.time_pass('min', 20)
    r.release(sid)
    r.leave(sid)
    r.time_pass('sec', 5)
    r.status('L60001', sid, 'inpark')
    r.revenue('day', 'temppay')
    r.pay(sid, 'temp', want=rS)
    r.leave(sid)
    done('L60001')

    r.pay(ids['S60001'][0], 'temp', want=rF)
    sid = ids['M50001'][0]
    r.leave(sid, want=rF)
    r.release(sid)
    r.pay(sid, 'bal', want=rS)
    r.pay(sid, 'bal', want=rF)
    r.leave(sid)
    done('M50001')

    sid = ids['S50002'][0]
    r.release(sid)
    r.call('pay_parkcost', sid, 'card', r.pay_value(sid, 'temp'))
    r.pay(sid, want=rS)
    r.leave(sid)
    done('S50002')
    end_checks(r, (m1, m2), ('day', 'month', 'year'))


def asset_days(r, cid, plate):
    ret = r.asset(cid)
    for part in ret.split('|')[1:]:
        p, _, days = part.partition(':')
        if p == plate:
            return int(days)
    return 0


COMBOS = (('S', 'S'), ('S', 'M'), ('S', 'L'), ('M', 'M'), ('M', 'L'), ('L', 'L'))


@scene('test_cjl_b4_holiday_discount_free_and_moncard_priority')
def _(r):
    r.goto(D(2026, 1, 1, 9, 0))
    card_plates = [[f'S80000{i}', f'M80000{i}', f'L80000{i}'] for i in (1, 2, 3)]
    plain_plates = [[f'S81000{i}', f'M81000{i}', f'L81000{i}'] for i in (1, 2, 3)]
    ccard = members(r, ['13300000001', '13300000002', '13300000003'], card_plates, [(500, 500, 500, 500)] * 3)
    cplain = members(r, ['13300000011', '13300000012', '13300000013'], plain_plates, [(500, 500)] * 3)
    groups = [(D(2026, 1, 1, 10, 0), D(2026, 1, 1, 12, 0)),
              (D(2026, 1, 3, 23, 40), D(2026, 1, 4, 0, 11)),
              (D(2026, 2, 14, 23, 30), D(2026, 2, 15, 1, 0)),
              (D(2026, 2, 23, 23, 0), D(2026, 2, 24, 2, 0)),
              (D(2026, 4, 3, 22, 0), D(2026, 4, 4, 3, 0)),
              (D(2026, 4, 30, 22, 0), D(2026, 5, 1, 2, 0)),
              (D(2026, 5, 5, 23, 40), D(2026, 5, 6, 0, 10, 1)),
              (D(2026, 6, 21, 12, 0), D(2026, 6, 22, 12, 0)),
              (D(2026, 9, 24, 23, 0), D(2026, 9, 25, 3, 0)),
              (D(2026, 9, 30, 23, 40), D(2026, 10, 1, 0, 10)),
              (D(2026, 10, 7, 23, 40), D(2026, 10, 8, 0, 11)),
              (D(2026, 10, 9, 22, 0), D(2026, 10, 10, 10, 0))]
    bought = False
    for gi, (occ, rel) in enumerate(groups):
        if not bought and occ >= D(2026, 9, 20):
            r.goto(D(2026, 9, 20, 10, 0))
            for cid, ps in zip(ccard, card_plates):
                for p in ps:
                    r.call('purchase_moncard', cid, p, want=rS)
                r.asset(cid)
            r.revenue_all(('day',))
            bought = True
        dur = int((rel - occ).total_seconds())
        plan = [(c, [dur]) for c in COMBOS]
        specs = slot(occ, plan, '81', pays=('bal', 'none')) + slot(occ, plan, '82', pays=('temp',))
        if bought:
            specs += slot(occ, plan, '80', pays=('auto', 'none'))
        park(r, specs)
        for cid in ccard + cplain:
            r.asset(cid)
        r.revenue_all(('day', 'month'))
        r.stats()
    r.revenue_all(('year',))


@scene('test_cjl_b5_moncard_stack_truncate_and_limit')
def _(r):
    r.goto(D(2026, 1, 1, 9, 0))
    ca, cb = members(r, ['13200000001', '13200000002'], [['S71001', 'M71001', 'L71001'], ['M71002', 'L71002', 'S71002']],
                     [(500,) * 8, (500, 100)])
    for k in range(12):
        r.call('purchase_moncard', ca, 'S71001', want=rS)
        if k in (0, 5, 11):
            r.asset(ca)
    r.call('purchase_moncard', ca, 'S71001', want=rF)
    r.call('purchase_moncard', ca, 'M71001', want=rF)
    r.asset(ca)
    r.revenue_all(('day',))
    r.call('purchase_moncard', cb, 'M71002', want=rS)
    r.call('purchase_moncard', cb, 'L71002', want=rF)
    r.call('recharge_account', cb, 500, want=rS)
    r.call('purchase_moncard', cb, 'L71002', want=rF)
    r.call('recharge_account', cb, 40, want=rS)
    r.call('purchase_moncard', cb, 'L71002', want=rS)
    r.asset(cb)
    r.call('purchase_moncard', cb, 'S71001', want=rF)
    r.call('purchase_moncard', ca, 'M71002', want=rF)
    r.call('purchase_moncard', cb, 'S79999', want=rF)
    cx, code = r.register('13200000009', 'Abc123!')
    r.call('purchase_moncard', cx, 'S71001', want=rF)
    r.call('unbind_plate_id', ca, 'S71001', want=rF)
    r.call('unregister_account', ca, 'Abc123!', want=rF)
    r.call('unbind_plate_id', cb, 'S71002', want=rS)
    r.time_pass('hour', 11)
    r.call('bind_plate_id', cb, 'S71002', want=rF)
    r.time_pass('hour', 1)
    r.call('bind_plate_id', cb, 'S71002', want=rS)
    r.revenue_all(('day',))
    r.goto(D(2026, 1, 15, 9, 0))
    r.asset(ca)
    r.asset(cb)
    park(r, [spec('L71002', 'L', D(2026, 1, 30, 22, 0), D(2026, 1, 31, 2, 0), 'auto'),
             spec('M71002', 'L', D(2026, 1, 30, 22, 0), D(2026, 1, 31, 2, 0), 'auto'),
             spec('S71001', 'M', D(2026, 1, 30, 22, 0), D(2026, 1, 31, 2, 0), 'auto')])
    r.asset(ca)
    r.asset(cb)
    r.call('unbind_plate_id', cb, 'M71002', want=rF)
    r.call('unbind_plate_id', cb, 'L71002', want=rS)
    r.revenue_all(('day', 'month'))
    r.goto(D(2026, 11, 15, 9, 0))
    cc, cd = members(r, ['13200000003', '13200000004'], [['S71003', 'L71003'], ['M71004']], [(500, 500), (500,)])
    r.call('purchase_moncard', cc, 'S71003', want=rS)
    r.asset(cc)
    r.call('purchase_moncard', cc, 'S71003', want=rS)
    r.asset(cc)
    r.call('purchase_moncard', cc, 'S71003', want=rF)
    r.asset(cc)
    r.asset(ca)
    r.revenue_all(('day', 'month'))
    r.goto(D(2026, 12, 26, 23, 59, 59))
    r.asset(ca)
    r.time_pass('sec', 1)
    r.asset(ca)
    r.call('purchase_moncard', ca, 'S71001', want=rF)
    r.goto(D(2026, 12, 31, 8, 0))
    r.call('purchase_moncard', cd, 'M71004', want=rS)
    r.asset(cd)
    r.call('purchase_moncard', cd, 'M71004', want=rF)
    r.asset(cc)
    r.call('unbind_plate_id', cc, 'S71003', want=rF)
    r.revenue_all(('day', 'month', 'year'))
    r.goto(D(2026, 12, 31, 23, 59, 59))
    r.asset(cc)
    r.asset(cd)


@scene('test_cjl_b6_moncard_transfer_floor_type_and_chain')
def _(r):
    r.goto(D(2026, 3, 1, 9, 0))
    ca, cb, cc = members(r, ['13100000001', '13100000002', '13100000003'],
                         [['S72001', 'M72001', 'L72001'], ['S72002', 'M72002', 'L72002'], ['S72003', 'M72003']],
                         [(500,) * 4, (500,), (1,)])
    for p in ('S72001', 'S72001', 'M72001', 'L72001'):
        r.call('purchase_moncard', ca, p, want=rS)
    r.call('purchase_moncard', cb, 'S72002', want=rS)
    for cid in (ca, cb, cc):
        r.asset(cid)
    early = r.arrive('S72003')
    r.time_pass('sec', 5)
    r.occupy(early[0], 'M001')
    r.time_pass('min', 30)
    for v in (1, 4, 5, 9):
        r.call('transfer_moncard', 'S72001', 'S72002', v, want=rS)
        r.asset(ca)
        r.asset(cb)
    r.call('transfer_moncard', 'S72001', 'M72002', 3, want=rF)
    r.call('transfer_moncard', 'L72001', 'M72002', 3, want=rF)
    r.call('transfer_moncard', 'S72001', 'S72003', 10, want=rS)
    r.asset(cc)
    r.time_pass('hour', 1)
    r.release(early[0])
    r.pay(early[0], want=rS)
    r.leave(early[0])
    r.time_pass('sec', 5)
    r.status('S72003', None, 'outpark')
    left = asset_days(r, ca, 'M72001')
    r.call('transfer_moncard', 'M72001', 'M72002', left, want=rF)
    r.call('transfer_moncard', 'M72001', 'M72002', left - 1, want=rS)
    r.asset(ca)
    r.asset(cb)
    r.call('transfer_moncard', 'M72001', 'M72002', 1, want=rF)
    r.call('transfer_moncard', 'S72002', 'S72003', 5, want=rS)
    r.call('transfer_moncard', 'S72001', 'S72001', 3)
    r.call('transfer_moncard', 'L72002', 'L72001', 1, want=rF)
    r.call('transfer_moncard', 'S79999', 'S72001', 1, want=rF)
    r.call('transfer_moncard', 'S72001', 'S72002', 0, want=rF)
    r.call('transfer_moncard', 'S72001', 'S72002', -1, want=rF)
    for cid in (ca, cb, cc):
        r.asset(cid)
    park(r, [spec('S72003', 'M', D(2026, 3, 2, 10, 0), D(2026, 3, 2, 12, 0), 'auto'),
             spec('S72002', 'L', D(2026, 3, 2, 10, 0), D(2026, 3, 2, 12, 0), 'auto'),
             spec('M72002', 'L', D(2026, 3, 2, 10, 0), D(2026, 3, 2, 12, 0), 'auto')])
    for cid in (ca, cb, cc):
        r.asset(cid)
    r.revenue_all(('day', 'month'))
    r.goto(D(2026, 12, 1, 9, 0))
    r.call('recharge_account', cb, 500, want=rS)
    r.call('recharge_account', ca, 500, want=rS)
    r.call('purchase_moncard', ca, 'L72001', want=rS)
    r.goto(D(2026, 12, 20, 9, 0))
    r.call('purchase_moncard', cb, 'L72002')
    r.asset(ca)
    r.asset(cb)
    r.call('transfer_moncard', 'L72001', 'L72002', 5, want=rS)
    r.asset(ca)
    r.asset(cb)
    r.call('transfer_moncard', 'S72003', 'S72001', 1)
    for cid in (ca, cb, cc):
        r.asset(cid)
    r.revenue_all(('day', 'month', 'year'))


@scene('test_cjl_b7_recharge_gift_cap_negative_and_transfer')
def _(r):
    r.goto(D(2026, 1, 12, 10, 0))
    c1, c2, c3, c4 = members(r, ['13000000001', '13000000002', '13000000003', '13000000004'])
    for v in (0, -1, 51, 99, 101, 499, 501, 1000, 10000):
        r.call('recharge_account', c1, v, want=rF)
    for v in (1, 50, 100, 500):
        r.call('recharge_account', c1, v, want=rS)
    r.asset(c1)
    r.revenue('day', 'recharge')
    for v in (500,) * 7 + (40,):
        r.call('recharge_account', c2, v, want=rS)
    r.asset(c2)
    r.call('recharge_account', c2, 500, want=rS)
    r.call('recharge_account', c2, 1, want=rF)
    r.asset(c2)
    for v in (500,) * 7 + (40, 1, 500, 100, 500, 50, 50, 50, 50):
        r.call('recharge_account', c3, v)
    r.asset(c3)
    for v in (500,) * 7 + (100,) * 5 + (50, 9, 1, 1):
        r.call('recharge_account', c4, v)
    r.asset(c4)
    r.call('recharge_account', 'CID00000000', 10, want=rF)
    cx, code = r.register('13000000009', 'Abc123!')
    r.call('recharge_account', cx, 10, want=rF)
    c5, c6 = members(r, ['13000000005', '13000000006'], [['L73001', 'S73001'], ['M73001']])
    big = r.arrive('L73001')
    r.time_pass('sec', 5)
    r.occupy(big[0], 'L001')
    r.call('recharge_account', c5, 100, want=rS)
    r.asset(c5)
    r.goto(D(2026, 1, 13, 12, 0))
    r.release(big[0])
    r.pay(big[0], 'bal', want=rF)
    r.pay(big[0], 'temp', delta=0.05)
    r.pay(big[0], 'temp', want=rS)
    r.asset(c5)
    r.leave(big[0])
    r.time_pass('sec', 5)
    small = r.arrive('S73001')
    r.time_pass('sec', 5)
    r.occupy(small[0], 'M001')
    r.call('recharge_account', c5, 100, want=rS)
    r.asset(c5)
    r.call('transfer_account', c5, c6, 121, want=rF)
    r.call('transfer_account', c5, c6, 120, want=rS)
    r.asset(c5)
    r.asset(c6)
    r.time_pass('hour', 2)
    r.release(small[0])
    r.pay(small[0], 'bal', want=rF)
    r.pay(small[0], 'temp', want=rS)
    r.leave(small[0])
    r.time_pass('sec', 5)
    r.asset(c5)
    mid = r.arrive('M73001')
    r.time_pass('sec', 5)
    r.occupy(mid[0], 'L002')
    r.time_pass('hour', 3)
    r.call('transfer_account', c6, c1, 120, want=rS)
    r.asset(c6)
    r.release(mid[0])
    r.time_pass('min', 14)
    r.time_pass('sec', 59)
    r.status('M73001', mid[0], 'inpark')
    r.time_pass('sec', 1)
    r.status('M73001', None, 'outpark')
    r.asset(c6)
    r.revenue('day', 'balpay')
    r.call('transfer_account', c6, c1, 1, want=rF)
    r.call('recharge_account', c6, 1, want=rS)
    r.asset(c6)
    r.call('unregister_account', c6, 'Abc123!', want=rF)
    for v in (500,) * 9:
        r.call('recharge_account', c6, v)
    r.asset(c6)
    again = r.arrive('M73001')
    r.time_pass('sec', 5)
    r.leave(again[0])
    r.time_pass('sec', 5)
    r.status('M73001', None, 'outpark')
    r.call('transfer_account', c1, c2, 1, want=rF)
    r.call('transfer_account', c1, c1, 10)
    r.call('transfer_account', c1, cx, 10, want=rF)
    r.call('transfer_account', c1, c3, 0, want=rF)
    r.asset(c1)
    bal = float(r.asset(c1).split(':')[1].split('|')[0])
    r.call('transfer_account', c1, c4, int(bal) + 1, want=rF)
    r.call('transfer_account', c1, c4, int(bal))
    r.asset(c1)
    r.asset(c4)
    r.call('unregister_account', c1, 'Abc123!')
    r.call('query_account', c1)
    for cid in (c2, c3, c4, c5, c6):
        r.asset(cid)
    r.revenue_all(('day', 'month', 'year'))


@scene('test_cjl_b9_blacklist_zombie_360h_and_clean_720h')
def _(r):
    r.goto(D(2026, 3, 2, 10, 0))
    cz, cy = members(r, ['12900000001', '12900000002'], [['S74001', 'S74002', 'S74003'], ['M74001']],
                     [(500,) * 8, (50,)])
    cars = ['S74001', 'S74002', 'S75001', 'M75001', 'M74001', 'S74003']
    ids = {}
    for p in cars[:4]:
        ids[p] = r.entry(p, {'S74001': 'G1', 'S74002': 'G2', 'S75001': 'G3', 'M75001': 'G4'}[p])
    t0 = r.now + 5 * SEC
    r.time_pass('sec', 5)
    for p in cars[4:]:
        ids[p] = r.arrive(p)
    r.time_pass('sec', 5)
    for p, sp in zip(cars, ('S001', 'M001', 'L001', 'L002', 'M002', 'S002')):
        r.occupy(ids[p][0], sp)
    r.stats()
    r.goto(t0 + 360 * HOUR - SEC)
    for p in ('S74001', 'S74002'):
        r.release(ids[p][0])
    r.leave(ids['S74001'][0])
    r.time_pass('sec', 1)
    r.leave(ids['S74002'][0])
    r.time_pass('sec', 5)
    r.status('S74001', None, 'outpark')
    r.status('S74002', None, 'outpark')
    r.asset(cz)
    back1 = r.arrive('S74001')
    back2 = r.arrive('S74002')
    r.time_pass('sec', 5)
    r.status('S74001', back1[0], 'inpark')
    r.status('S74002', None, 'outpark')
    r.call('del_blacklist', 'S74002', want=rS)
    back2 = r.arrive('S74002')
    r.time_pass('sec', 5)
    r.status('S74002', back2[0], 'inpark')
    sid = ids['S75001'][0]
    r.release(sid)
    r.pay(sid, 'temp', want=rS)
    r.leave(sid)
    r.time_pass('sec', 5)
    r.status('S75001', None, 'outpark')
    r.revenue_all(('day', 'month'))
    r.arrive('S75001')
    r.time_pass('sec', 5)
    r.status('S75001', None, 'outpark')
    r.call('add_blacklist', 'S75001', want=rS)
    r.call('del_blacklist', 'S75001', want=rS)
    r.call('del_blacklist', 'S75001', want=rS)
    again = r.arrive('S75001')
    r.time_pass('sec', 5)
    r.status('S75001', again[0], 'inpark')
    r.call('add_blacklist', 'S74003', want=rS)
    sid = ids['S74003'][0]
    r.release(sid)
    r.leave(sid)
    r.time_pass('sec', 5)
    r.status('S74003', None, 'outpark')
    r.arrive('S74003')
    r.time_pass('sec', 5)
    r.status('S74003', None, 'outpark')
    probe = r.arrive('S76001')
    r.time_pass('sec', 2)
    r.call('add_blacklist', 'S76001', want=rS)
    r.status('S76001', probe[0], 'entrying')
    r.time_pass('sec', 3)
    r.status('S76001', None, 'outpark')
    r.call('query_account', probe[1], want=rF)
    r.stats()
    for sid, _ in (back1, back2, again):
        r.occupy(sid, free_space(r, 'S', set()))
    r.goto(t0 + 720 * HOUR - SEC)
    r.status('M75001', ids['M75001'][0], 'parked')
    r.status('M74001', ids['M74001'][0], 'parked')
    r.stats()
    r.time_pass('sec', 1)
    r.status('M75001', None, 'outpark')
    r.status('M74001', ids['M74001'][0], 'parked')
    r.time_pass('sec', 4)
    r.status('M74001', ids['M74001'][0], 'parked')
    r.time_pass('sec', 1)
    r.status('M74001', None, 'outpark')
    r.stats()
    r.check_occupied()
    r.asset(cz)
    r.asset(cy)
    r.revenue_all(('day', 'month', 'year'))
    for sid, _ in (back1, back2, again):
        r.release(sid)
        r.pay(sid, want=rS)
        r.leave(sid)
    r.time_pass('sec', 5)
    end_checks(r, (cz, cy), ('day', 'month', 'year'))


@scene('test_cjl_b10_full_lot_mixed_day_settlement')
def _(r):
    r.goto(D(2026, 3, 3, 9, 30))
    mplates = [f'S9{i:04d}' for i in range(1, 21)] + [f'M9{i:04d}' for i in range(1, 31)] + ['L90001', 'L90002']
    cids = members(r, [f'1280000{i:04d}' for i in range(1, 27)],
                   [mplates[2 * i:2 * i + 2] for i in range(26)], [(100,)] * 20 + [(5,)] * 6)
    s_cars = mplates[:20] + [f'S8{i:04d}' for i in range(1, 61)]
    m_cars = mplates[20:50] + [f'M8{i:04d}' for i in range(1, 163)]
    l_cars = ['L90001', 'L90002'] + [f'L8{i:04d}' for i in range(1, 7)]
    plan = ([(p, 'S') for p in s_cars[:70]] + [(p, 'M') for p in s_cars[70:78]] + [(p, 'L') for p in s_cars[78:80]]
            + [(p, 'M') for p in m_cars] + [(p, 'L') for p in l_cars])
    order = []
    for i in range(max(len(s_cars), len(m_cars), len(l_cars))):
        for group in (s_cars, m_cars, l_cars):
            if i < len(group):
                order.append(group[i])
    ids = {}
    start = r.now
    for p in order[:140]:
        ids[p] = r.arrive(p)
    r.time_pass('sec', 5)
    r.stats()
    target = dict(plan)
    for p in order[:140]:
        r.occupy(ids[p][0], free_space(r, target[p], set()))
    for p in order[140:]:
        ids[p] = r.arrive(p)
    r.time_pass('sec', 5)
    r.stats()
    for p in order[140:]:
        r.occupy(ids[p][0], free_space(r, target[p], set()))
    r.stats()
    r.check_occupied()
    for p in ('S89999', 'M89999', 'L89999'):
        r.arrive(p)
    r.time_pass('sec', 5)
    for p in ('S89999', 'M89999', 'L89999'):
        r.status(p, None, 'outpark')
    r.stats()
    leave_at = start + 90 * MIN
    for k in range(0, len(order), 20):
        r.goto(leave_at + (k // 20) * 20 * MIN)
        batch = order[k:k + 20]
        for j, p in enumerate(batch):
            sid = ids[p][0]
            r.release(sid)
            if j % 3 != 2:
                r.pay(sid, want=rS)
        for p in batch:
            r.leave(ids[p][0])
        r.time_pass('sec', 5)
        if k % 60 == 0:
            r.stats()
            r.check_occupied()
            r.revenue_all(('day',))
    r.time_pass('min', 20)
    for p in order[::25]:
        r.status(p, None, 'outpark')
    end_checks(r, cids, ('day', 'month', 'year'))
