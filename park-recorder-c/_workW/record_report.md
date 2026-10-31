# 录制报告（C 版本，2026-10-31 19:20:46）

| 测试 | 步骤 | 录制耗时(s) | 与预期不同 | 各轮不一致 | 状态 |
|---|---|---|---|---|---|
| test_cjl_c3105_reserve_expire_30min | 19 | 0.6 | 0 | 0 | 已写入 |
| test_cjl_c3109_reserved_car_entry_rejected | 26 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_c3201_wash_order_checks | 39 | 1.2 | 0 | 0 | 已写入 |
| test_cjl_c3202_wash_bay_rotation | 27 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_c3203_wash_start_end | 31 | 0.9 | 0 | 0 | 已写入 |
| test_cjl_c3204_wash_call_timeout | 23 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_c3205_wash_cancel_and_inpark_timer | 30 | 0.9 | 0 | 0 | 已写入 |
| test_cjl_c3206_wash_day_end | 37 | 1.1 | 0 | 0 | 已写入 |
| test_cjl_c3207_wash_balance_same_type | 23 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_c3208_wash_balance_small_to_medium | 25 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_c3209_wash_bay_overflow | 89 | 2.7 | 0 | 0 | 已写入 |
| test_cjl_c3210_wash_blank_params | 9 | 0.3 | 0 | 0 | 已写入 |
| test_cjl_c3211_wash_fee_negative_balance | 28 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_c3212_wash_fee_paid_at_exit | 25 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_c3213_wash_parking_fee_at_exit | 23 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_c3214_wash_forced_exit_with_parking_fee | 28 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_c3215_wash_price_workday | 45 | 1.4 | 0 | 0 | 已写入 |
| test_cjl_c3216_wash_price_weekend_holiday | 57 | 1.7 | 0 | 0 | 已写入 |
| test_cjl_c3217_wash_balance_tie_and_large_to_medium | 75 | 2.3 | 0 | 0 | 已写入 |
| test_cjl_c3218_wash_order_after_car_exit | 21 | 0.6 | 0 | 0 | 已写入 |
| test_cjl_c3219_wash_balance_large_car_stays | 17 | 0.5 | 0 | 0 | 已写入 |

## 与预期不同的步骤

用例中写入的是实际返回；预期值是编写步骤时按规格推测的，不同处说明对规则的理解需要修正或可能是有价值的场景。

无

## 各轮不一致的步骤

同一步骤在多轮录制中返回不同（会话号、账号、验证码已归一化）；这些断言在平台上可能不稳定。

无

## 录制异常

无

## 验证

未执行
