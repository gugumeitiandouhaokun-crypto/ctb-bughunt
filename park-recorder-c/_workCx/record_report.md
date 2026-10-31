# 录制报告（C 版本，2026-10-31 20:03:48）

| 测试 | 步骤 | 录制耗时(s) | 与预期不同 | 各轮不一致 | 状态 |
|---|---|---|---|---|---|
| test_cjl_cx01_reserve_across_midnight | 40 | 1.2 | 0 | 0 | 已写入 |
| test_cjl_cx02_reserve_last_space_against_locks | 181 | 5.5 | 1 | 0 | 已写入 |
| test_cjl_cx03_reserved_space_grab_by_others | 34 | 1.0 | 0 | 0 | 已写入 |
| test_cjl_cx04_reserve_used_counts_daily | 44 | 1.3 | 0 | 0 | 已写入 |
| test_cjl_cx05_wash_order_id_next_day | 33 | 1.0 | 0 | 0 | 已写入 |
| test_cjl_cx06_queued_car_leaves_park | 34 | 1.0 | 0 | 0 | 已写入 |
| test_cjl_cx07_parked_car_called_timeout | 23 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_cx08_blacklisted_car_washing | 29 | 0.9 | 0 | 0 | 已写入 |
| test_cjl_cx09_large_queue_full | 31 | 0.9 | 1 | 0 | 已写入 |
| test_cjl_cx10_reserved_car_washes | 24 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_cx11_moncard_large_space_wash | 25 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_cx12_balance_with_washing_bays | 42 | 1.3 | 0 | 0 | 已写入 |

## 与预期不同的步骤

用例中写入的是实际返回；预期值是编写步骤时按规格推测的，不同处说明对规则的理解需要修正或可能是有价值的场景。

### test_cjl_cx02_reserve_last_space_against_locks
- 第 172 行 `check_car_status(S393098,null,outpark)` 预期 `E000:Success` 实际 `E002:Verify_fail`

### test_cjl_cx09_large_queue_full
- 第 25 行 `take_wash_order(L393097)` 预期 `E001:Fail` 实际 `E000:008`

## 各轮不一致的步骤

同一步骤在多轮录制中返回不同（会话号、账号、验证码已归一化）；这些断言在平台上可能不稳定。

无

## 录制异常

无

## 验证

未执行
