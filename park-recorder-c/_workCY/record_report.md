# 录制报告（C 版本，2026-10-31 20:17:17）

| 测试 | 步骤 | 录制耗时(s) | 与预期不同 | 各轮不一致 | 状态 |
|---|---|---|---|---|---|
| test_cjl_cy01_reserve_expire_boundary | 30 | 0.9 | 2 | 0 | 已写入 |
| test_cjl_cy02_reserved_car_enters_never_parks | 26 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_cy03_reserve_large_space_diff_fee | 21 | 0.6 | 0 | 0 | 已写入 |
| test_cjl_cy04_reserve_then_blacklist_and_unbind | 26 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_cy05_reserve_occupied_or_wash_bay | 21 | 0.6 | 0 | 0 | 已写入 |
| test_cjl_cy06_take_order_hour_boundaries | 32 | 1.0 | 1 | 0 | 已写入 |
| test_cjl_cy07_illegal_wash_transitions | 33 | 1.0 | 0 | 0 | 已写入 |
| test_cjl_cy08_cancel_600s_boundary_fee | 36 | 1.1 | 0 | 0 | 已写入 |
| test_cjl_cy09_balance_stage1_ws_internal | 51 | 1.5 | 0 | 0 | 已写入 |
| test_cjl_cy10_balance_stage3_ws_overflow | 73 | 2.2 | 0 | 0 | 已写入 |
| test_cjl_cy11_balance_stage2_wl_backfill | 66 | 2.0 | 0 | 0 | 已写入 |
| test_cjl_cy12_wash_status_after_close_and_next_day | 50 | 1.5 | 0 | 0 | 已写入 |
| test_cjl_cy13_reserved_car_wash_and_cancel_reserve | 27 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_cy14_reserve_full_lot_entry | 38 | 1.1 | 0 | 0 | 已写入 |

## 与预期不同的步骤

用例中写入的是实际返回；预期值是编写步骤时按规格推测的，不同处说明对规则的理解需要修正或可能是有价值的场景。

### test_cjl_cy01_reserve_expire_boundary
- 第 16 行 `cancel_reserve(r36829424)` 预期 `E001:Fail` 实际 `E000:Success`
- 第 26 行 `occupy_park(p3164246850248232,S001)` 预期 `E000:Success` 实际 `E001:Fail`

### test_cjl_cy06_take_order_hour_boundaries
- 第 13 行 `occupy_park(p2448495537687482,S004)` 预期 `E000:Success` 实际 `E001:Fail`

## 各轮不一致的步骤

同一步骤在多轮录制中返回不同（会话号、账号、验证码已归一化）；这些断言在平台上可能不稳定。

无

## 录制异常

无

## 验证

未执行
