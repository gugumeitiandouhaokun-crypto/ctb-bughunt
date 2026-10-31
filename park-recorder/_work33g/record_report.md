# 录制报告（B 版本，2026-10-31 18:22:33）

| 测试 | 步骤 | 录制耗时(s) | 与预期不同 | 各轮不一致 | 状态 |
|---|---|---|---|---|---|
| test_cjl_g01_temp_car_unpaid_exit | 25 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_g02_temp_car_paid_then_overtime | 22 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_g03_paid_then_moncard_overtime | 36 | 1.1 | 0 | 0 | 已写入 |
| test_cjl_g04_negative_balance_unoccupied_exit | 32 | 1.0 | 0 | 0 | 已写入 |
| test_cjl_g05_time_pass_params | 27 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_g06_gate_mode_blocks_pass | 36 | 1.1 | 0 | 0 | 已写入 |

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
