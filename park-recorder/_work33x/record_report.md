# 录制报告（B 版本，2026-10-31 17:52:59）

| 测试 | 步骤 | 录制耗时(s) | 与预期不同 | 各轮不一致 | 状态 |
|---|---|---|---|---|---|
| test_cjl_x01_exit_window_recharge_or_transfer_out | 67 | 2.1 | 0 | 0 | 已写入 |
| test_cjl_x02_exit_window_moncard_buy_or_receive | 41 | 1.2 | 0 | 0 | 已写入 |
| test_cjl_x03_exit_window_account_ops_then_cancel_and_temp_reentry | 40 | 1.2 | 0 | 0 | 已写入 |
| test_cjl_x04_entry_window_recharge_and_blacklisted_member_car | 32 | 1.0 | 0 | 0 | 已写入 |
| test_cjl_x05_blacklisted_parked_car_moncard_and_exit | 43 | 1.3 | 0 | 0 | 已写入 |
| test_cjl_x06_exit_refund_pushes_balance_over_5000 | 57 | 1.7 | 0 | 0 | 已写入 |
| test_cjl_x07_exit_refund_across_midnight_and_month | 64 | 1.9 | 0 | 0 | 已写入 |
| test_cjl_x11_tidal_gate_exit_refund_then_auto_deduct | 29 | 0.9 | 0 | 0 | 已写入 |
| test_cjl_x08_zombie_car_moncard_transfer_forced_settle | 43 | 1.3 | 0 | 0 | 已写入 |
| test_cjl_x09_reoccupy_after_pay_extra_fee_gift_locked | 38 | 1.1 | 0 | 0 | 已写入 |
| test_cjl_x10_moncard_bought_mid_session_and_after_release | 43 | 1.3 | 0 | 0 | 已写入 |
| test_cjl_x12_paid_then_unregister_attempts | 51 | 1.5 | 0 | 0 | 已写入 |
| test_cjl_x13_moncard_transfer_between_two_parked_cars | 40 | 1.2 | 0 | 0 | 已写入 |
| test_cjl_x14_holiday_free_day_gift_lock_month_boundary | 31 | 0.9 | 0 | 0 | 已写入 |

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
