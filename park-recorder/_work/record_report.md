# 录制报告（B 版本，2026-10-31 17:13:48）

| 测试 | 步骤 | 录制耗时(s) | 与预期不同 | 各轮不一致 | 状态 |
|---|---|---|---|---|---|
| test_cjl_02b_recharge_cap_exact_5000 | 17 | 0.5 | 0 | 0 | 已写入 |
| test_cjl_s1b2_reoccupy_first_type_after_release | 18 | 0.6 | 0 | 0 | 已写入 |
| test_cjl_s1m2_l_full_entry_fail_outpark | 30 | 0.9 | 0 | 0 | 已写入 |
| test_cjl_s1n2_release_relock_then_l_reentry | 33 | 1.0 | 0 | 0 | 已写入 |
| test_cjl_s1q_s_full_then_lock_m | 184 | 5.5 | 0 | 0 | 已写入 |
| test_cjl_s1r_m_full_then_lock_l_and_reject | 488 | 14.7 | 0 | 0 | 已写入 |
| test_cjl_s1s_s_overflow_to_l_then_all_reject | 652 | 19.7 | 0 | 0 | 已写入 |
| test_cjl_s1t_lock_only_counts_toward_full | 34 | 1.0 | 2 | 0 | 已写入 |
| test_cjl_s1u_concurrent_requests_for_last_slot | 34 | 1.0 | 0 | 0 | 已写入 |
| test_cjl_s1v_first_type_l_stuck_then_forced_exit | 41 | 1.2 | 0 | 0 | 已写入 |
| test_cjl_s1w_m_locked_s_car_takes_freed_s_space | 176 | 5.3 | 1 | 0 | 已写入 |
| test_cjl_s1x_l_full_exit_then_refill_exactly_one | 44 | 1.3 | 0 | 0 | 已写入 |
| test_cjl_s1y_priority_split_within_one_batch | 171 | 5.2 | 0 | 0 | 已写入 |
| test_cjl_b3_billing_matrix_workdays | 788 | 23.8 | 0 | 0 | 已写入 |
| test_cjl_b2_reoccupy_first_type_and_quota_timeout | 237 | 7.2 | 0 | 0 | 已写入 |
| test_cjl_b8_pay_exit_timing_refund | 157 | 4.7 | 1 | 0 | 已写入 |
| test_cjl_b4_holiday_discount_free_and_moncard_priority | 1231 | 37.1 | 0 | 0 | 已写入 |
| test_cjl_b5_moncard_stack_truncate_and_limit | 165 | 5.0 | 1 | 0 | 已写入 |
| test_cjl_b6_moncard_transfer_floor_type_and_chain | 134 | 4.0 | 0 | 0 | 已写入 |
| test_cjl_b7_recharge_gift_cap_negative_and_transfer | 175 | 5.3 | 0 | 0 | 已写入 |
| test_cjl_b9_blacklist_zombie_360h_and_clean_720h | 152 | 4.6 | 0 | 0 | 已写入 |
| test_cjl_b10_full_lot_mixed_day_settlement | 1712 | 51.6 | 0 | 0 | 已写入 |

## 与预期不同的步骤

用例中写入的是实际返回；预期值是编写步骤时按规格推测的，不同处说明对规则的理解需要修正或可能是有价值的场景。

### test_cjl_s1t_lock_only_counts_toward_full
- 第 31 行 `check_park_statistics(0|1,0|0,2|9)` 预期 `E000:Success` 实际 `E002:Verify_fail`
- 第 33 行 `check_park_statistics(0|0,0|0,3|9)` 预期 `E000:Success` 实际 `E002:Verify_fail`

### test_cjl_s1w_m_locked_s_car_takes_freed_s_space
- 第 168 行 `check_park_statistics(69|0,0|2,0|0)` 预期 `E000:Success` 实际 `E002:Verify_fail`

### test_cjl_b8_pay_exit_timing_refund
- 第 139 行 `pay_parkcost(p8761273726330979,bal,0.00)` 预期 `E000:Success` 实际 `E001:Fail`

### test_cjl_b5_moncard_stack_truncate_and_limit
- 第 95 行 `unbind_plate_id(CID92185482,M71002)` 预期 `E001:Fail` 实际 `E000:Success`

## 各轮不一致的步骤

同一步骤在多轮录制中返回不同（会话号、账号、验证码已归一化）；这些断言在平台上可能不稳定。

无

## 录制异常

无

## 验证

未执行
