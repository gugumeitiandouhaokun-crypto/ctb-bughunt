# 录制报告（C 版本，2026-10-31 20:09:57）

| 测试 | 步骤 | 录制耗时(s) | 与预期不同 | 各轮不一致 | 状态 |
|---|---|---|---|---|---|
| test_cjl_cw01_price_by_take_time_not_start | 33 | 1.0 | 0 | 0 | 已写入 |
| test_cjl_cw02_adjusted_workday_peak | 31 | 0.9 | 0 | 0 | 已写入 |
| test_cjl_cw03_waiver_under_daily_cap | 33 | 1.0 | 0 | 0 | 已写入 |
| test_cjl_cw04_wash_then_park_over_midnight | 25 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_cw05_temp_car_holiday_wash | 21 | 0.6 | 0 | 0 | 已写入 |
| test_cjl_cw06_balance_short_then_temp_topup | 25 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_cw07_member_pay_bal_with_wash | 23 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_cw08_pay_first_then_wash | 22 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_cw09_paid_window_while_queued | 17 | 0.5 | 0 | 0 | 已写入 |
| test_cjl_cw10_two_plates_one_balance | 36 | 1.1 | 0 | 0 | 已写入 |
| test_cjl_cw11_temp_call_timeout_fee | 17 | 0.5 | 0 | 0 | 已写入 |
| test_cjl_cw12_temp_cancel_fee | 37 | 1.1 | 0 | 0 | 已写入 |
| test_cjl_cw13_temp_expired_at_1730_free | 25 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_cw14_temp_washing_closed_at_1800 | 27 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_cw15_moncard_last_day_wash | 28 | 0.8 | 2 | 0 | 已写入 |
| test_cjl_cw16_temp_forced_exit_unpaid_wash | 15 | 0.4 | 0 | 0 | 已写入 |
| test_cjl_cw17_short_stay_with_wash | 41 | 1.2 | 0 | 0 | 已写入 |
| test_cjl_cw18_pay_bal_wrong_then_right | 24 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_cw19_overflow_bay_type_price | 62 | 1.9 | 0 | 0 | 已写入 |
| test_cjl_cw20_month_boundary_revenue | 26 | 0.8 | 0 | 0 | 已写入 |

## 与预期不同的步骤

用例中写入的是实际返回；预期值是编写步骤时按规格推测的，不同处说明对规则的理解需要修正或可能是有价值的场景。

### test_cjl_cw15_moncard_last_day_wash
- 第 20 行 `occupy_park(p8342635288601051,S001)` 预期 `E000:Success` 实际 `E001:Fail`
- 第 24 行 `release_park(p8342635288601051)` 预期 `E000:Success` 实际 `E001:Fail`

## 各轮不一致的步骤

同一步骤在多轮录制中返回不同（会话号、账号、验证码已归一化）；这些断言在平台上可能不稳定。

无

## 录制异常

无

## 验证

未执行
