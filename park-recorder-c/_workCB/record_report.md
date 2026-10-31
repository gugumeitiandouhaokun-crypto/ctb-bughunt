# 录制报告（C 版本，2026-10-31 19:54:40）

| 测试 | 步骤 | 录制耗时(s) | 与预期不同 | 各轮不一致 | 状态 |
|---|---|---|---|---|---|
| test_cjl_cb01_recharge_tiers_and_cap | 45 | 1.4 | 0 | 0 | 已写入 |
| test_cjl_cb02_transfer_account | 24 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_cb03_moncard_purchase | 33 | 1.0 | 0 | 0 | 已写入 |
| test_cjl_cb04_moncard_limit_and_year_end | 34 | 1.0 | 0 | 0 | 已写入 |
| test_cjl_cb05_moncard_transfer | 30 | 0.9 | 1 | 0 | 已写入 |
| test_cjl_cb06_member_fee_basics | 54 | 1.6 | 0 | 0 | 已写入 |
| test_cjl_cb07_cross_day_and_space_diff | 41 | 1.2 | 0 | 0 | 已写入 |
| test_cjl_cb08_holiday_fee | 44 | 1.3 | 0 | 0 | 已写入 |
| test_cjl_cb09_moncard_fee | 42 | 1.3 | 0 | 0 | 已写入 |
| test_cjl_cb10_temp_pay | 27 | 0.8 | 2 | 0 | 已写入 |
| test_cjl_cb11_paid_window_and_reoccupy | 34 | 1.0 | 0 | 0 | 已写入 |
| test_cjl_cb12_member_pay_bal_and_refund | 42 | 1.3 | 0 | 0 | 已写入 |
| test_cjl_cb13_negative_balance | 44 | 1.3 | 0 | 0 | 已写入 |
| test_cjl_cb14_blacklist | 27 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_cb15_zombie_15_days | 38 | 1.1 | 1 | 0 | 已写入 |
| test_cjl_cb16_revenue_scopes | 25 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_cb17_session_gift_and_moncard | 18 | 0.5 | 0 | 0 | 已写入 |
| test_cjl_cb18_query_asset_params | 16 | 0.5 | 0 | 0 | 已写入 |

## 与预期不同的步骤

用例中写入的是实际返回；预期值是编写步骤时按规格推测的，不同处说明对规则的理解需要修正或可能是有价值的场景。

### test_cjl_cb05_moncard_transfer
- 第 23 行 `query_account_asset(CID20492237)` 预期 `E000:840.00|S39051:020|M39052:000|S39053:008` 实际 `E000:840.00|S39051:018|M39052:000|S39053:008`

### test_cjl_cb10_temp_pay
- 第 12 行 `pay_parkcost(p5746853541947855,cash,6.00)` 预期 `E001:Fail` 实际 `E000:Success`
- 第 15 行 `pay_parkcost(p5746853541947855,temp,6.00)` 预期 `E000:Success` 实际 `E001:Fail`

### test_cjl_cb15_zombie_15_days
- 第 31 行 `check_car_status(S39151,null,outpark)` 预期 `E000:Success` 实际 `E002:Verify_fail`

## 各轮不一致的步骤

同一步骤在多轮录制中返回不同（会话号、账号、验证码已归一化）；这些断言在平台上可能不稳定。

无

## 录制异常

无

## 验证

未执行
