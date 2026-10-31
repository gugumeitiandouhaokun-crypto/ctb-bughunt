# 录制报告（C 版本，2026-10-31 19:46:05）

| 测试 | 步骤 | 录制耗时(s) | 与预期不同 | 各轮不一致 | 状态 |
|---|---|---|---|---|---|
| test_cjl_ca01_register_params | 20 | 0.6 | 1 | 0 | 已写入 |
| test_cjl_ca02_activate_rules | 29 | 0.9 | 0 | 0 | 已写入 |
| test_cjl_ca03_activate_in_time | 8 | 0.2 | 0 | 0 | 已写入 |
| test_cjl_ca04_unregister | 23 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_ca05_unregister_with_car_in_park | 15 | 0.5 | 0 | 0 | 已写入 |
| test_cjl_ca06_bind_rules | 26 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_ca07_unbind_12h | 21 | 0.6 | 0 | 0 | 已写入 |
| test_cjl_ca08_temp_account | 19 | 0.6 | 0 | 0 | 已写入 |
| test_cjl_ca09_temp_car_bind_in_park | 26 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_ca10_query_params | 18 | 0.5 | 0 | 0 | 已写入 |
| test_cjl_ca11_change_gate_manual | 28 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_ca12_tide_and_restore | 29 | 0.9 | 0 | 0 | 已写入 |
| test_cjl_ca13_tide_non_workday | 28 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_ca14_switch_while_passing | 24 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_ca15_gate_flow_limit | 20 | 0.6 | 1 | 0 | 已写入 |
| test_cjl_ca16_car_status_params | 18 | 0.5 | 0 | 0 | 已写入 |
| test_cjl_ca17_inpark_timeout | 20 | 0.6 | 0 | 0 | 已写入 |
| test_cjl_ca19_unregister_blocked | 26 | 0.8 | 1 | 0 | 已写入 |
| test_cjl_ca20_unbind_blocked | 18 | 0.5 | 0 | 0 | 已写入 |
| test_cjl_ca21_bind_while_passing | 18 | 0.5 | 0 | 0 | 已写入 |
| test_cjl_ca22_member_capacity_500 | 1006 | 30.3 | 0 | 0 | 已写入 |
| test_cjl_ca18_time_pass_params | 11 | 0.3 | 0 | 0 | 已写入 |

## 与预期不同的步骤

用例中写入的是实际返回；预期值是编写步骤时按规格推测的，不同处说明对规则的理解需要修正或可能是有价值的场景。

### test_cjl_ca01_register_params
- 第 12 行 `register_account(13801000006,abc12~)` 预期 `E001:Fail` 实际 `E000:CID22543629,727369`

### test_cjl_ca15_gate_flow_limit
- 第 4 行 `car_entry(S38152,G1)` 预期 `E001:Fail` 实际 `E000:p9781899991683108,CID05884529`

### test_cjl_ca19_unregister_blocked
- 第 16 行 `activate_account(CID97941379,	)` 预期 `E001:InvalidPara` 实际 `E002:InvalidCode`

## 各轮不一致的步骤

同一步骤在多轮录制中返回不同（会话号、账号、验证码已归一化）；这些断言在平台上可能不稳定。

无

## 录制异常

无

## 验证

未执行
