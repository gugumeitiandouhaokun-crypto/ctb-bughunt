# 录制报告（C 版本，2026-10-31 19:32:05）

| 测试 | 步骤 | 录制耗时(s) | 与预期不同 | 各轮不一致 | 状态 |
|---|---|---|---|---|---|
| test_cjl_c3301_wash_bay_outside_standard_spaces | 17 | 0.5 | 0 | 0 | 已写入 |
| test_cjl_c3302_wash_once_per_day | 22 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_c3303_wash_bay_daily_limit_20 | 172 | 5.2 | 0 | 0 | 已写入 |
| test_cjl_c3304_wash_price_medium_large | 51 | 1.5 | 0 | 0 | 已写入 |
| test_cjl_c3305_wash_peak_boundaries | 100 | 3.0 | 0 | 0 | 已写入 |
| test_cjl_c3306_wash_cancel_and_timeout_fee | 45 | 1.4 | 0 | 0 | 已写入 |
| test_cjl_c3307_wash_parking_waiver_moncard | 41 | 1.2 | 0 | 0 | 已写入 |
| test_cjl_c3308_wash_parking_on_holiday | 20 | 0.6 | 0 | 0 | 已写入 |
| test_cjl_c3309_wash_fee_temp_pay_and_revenue | 38 | 1.1 | 0 | 0 | 已写入 |
| test_cjl_c3310_wash_fee_cross_day_revenue | 25 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_c3311_wash_transition_and_close | 40 | 1.2 | 0 | 0 | 已写入 |
| test_cjl_c3312_wash_balance_stack_insert | 52 | 1.6 | 0 | 0 | 已写入 |
| test_cjl_c3314_wash_cancel_free_settlement | 22 | 0.7 | 1 | 0 | 已写入 |
| test_cjl_c3313_reserved_car_normal_parking_fee | 29 | 0.9 | 0 | 0 | 已写入 |

## 与预期不同的步骤

用例中写入的是实际返回；预期值是编写步骤时按规格推测的，不同处说明对规则的理解需要修正或可能是有价值的场景。

### test_cjl_c3314_wash_cancel_free_settlement
- 第 17 行 `query_wash_status(004)` 预期 `E000:canceled` 实际 `E000:cancel`

## 各轮不一致的步骤

同一步骤在多轮录制中返回不同（会话号、账号、验证码已归一化）；这些断言在平台上可能不稳定。

无

## 录制异常

无

## 验证

未执行
