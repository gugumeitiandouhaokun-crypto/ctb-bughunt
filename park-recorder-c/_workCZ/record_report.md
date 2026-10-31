# 录制报告（C 版本，2026-10-31 20:22:19）

| 测试 | 步骤 | 录制耗时(s) | 与预期不同 | 各轮不一致 | 状态 |
|---|---|---|---|---|---|
| test_cjl_cz01_balance_stage1_wm_internal | 39 | 1.2 | 0 | 0 | 已写入 |
| test_cjl_cz02_reserve_quota_expire_cancel_mix | 20 | 0.6 | 0 | 0 | 已写入 |
| test_cjl_cz03_reserve_before_midnight_enter_after | 26 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_cz04_two_members_race_same_space | 23 | 0.7 | 1 | 0 | 已写入 |
| test_cjl_cz05_reserved_washed_never_parked | 22 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_cz06_reserve_by_invalid_owner | 16 | 0.5 | 0 | 0 | 已写入 |
| test_cjl_cz07_wash_while_occupying_space | 21 | 0.6 | 0 | 0 | 已写入 |
| test_cjl_cz08_s_order_spills_to_wl | 95 | 2.9 | 0 | 0 | 已写入 |
| test_cjl_cz09_expired_then_retake_and_balance | 39 | 1.2 | 2 | 0 | 已写入 |
| test_cjl_cz10_balance_after_1730_expiry | 43 | 1.3 | 0 | 0 | 已写入 |

## 与预期不同的步骤

用例中写入的是实际返回；预期值是编写步骤时按规格推测的，不同处说明对规则的理解需要修正或可能是有价值的场景。

### test_cjl_cz04_two_members_race_same_space
- 第 12 行 `reserve_space(S396042,M002)` 预期 `E000:Success` 实际 `E000:r29341392`

### test_cjl_cz09_expired_then_retake_and_balance
- 第 20 行 `start_wash_service(001)` 预期 `E000:Success` 实际 `E001:Fail`
- 第 21 行 `end_wash_service(001)` 预期 `E000:Success` 实际 `E001:Fail`

## 各轮不一致的步骤

同一步骤在多轮录制中返回不同（会话号、账号、验证码已归一化）；这些断言在平台上可能不稳定。

无

## 录制异常

无

## 验证

未执行
