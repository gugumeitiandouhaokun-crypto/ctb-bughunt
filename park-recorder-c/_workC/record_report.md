# 录制报告（C 版本，2026-10-31 18:53:45）

| 测试 | 步骤 | 录制耗时(s) | 与预期不同 | 各轮不一致 | 状态 |
|---|---|---|---|---|---|
| test_cjl_c3101_reserve_plate_checks | 22 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_c3102_reserve_space_checks | 27 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_c3103_reserve_occupied_space | 19 | 0.6 | 0 | 0 | 已写入 |
| test_cjl_c3104_reserve_daily_limit | 29 | 0.9 | 0 | 0 | 已写入 |
| test_cjl_c3105_reserve_expire_30min | 19 | 0.6 | 0 | 0 | 已写入 |
| test_cjl_c3106_reserve_status_param | 20 | 0.6 | 0 | 0 | 已写入 |
| test_cjl_c3107_reserved_car_enter_and_occupy | 31 | 0.9 | 0 | 0 | 已写入 |
| test_cjl_c3108_reserved_car_bypass_full | 41 | 1.2 | 0 | 0 | 已写入 |
| test_cjl_c3109_reserved_car_entry_rejected | 26 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_c3110_entrying_blocks_cancel_and_expire | 30 | 0.9 | 0 | 0 | 已写入 |
| test_cjl_c3111_day_change_closes_locked | 17 | 0.5 | 0 | 0 | 已写入 |
| test_cjl_c3112_forced_exit_releases_reserved | 26 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_c3113_used_reservation_over_midnight | 22 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_c3114_query_reserve_accounts | 27 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_c3115_park_statistics_params | 17 | 0.5 | 0 | 0 | 已写入 |
| test_cjl_c3116_occupied_spaces_params | 24 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_c3117_lock_quota_counting | 23 | 0.7 | 0 | 0 | 已写入 |
| test_cjl_c3118_occupy_release_rules | 27 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_c3119_exited_session_and_reentry | 26 | 0.8 | 0 | 0 | 已写入 |
| test_cjl_c3120_forced_exit_plain_car | 20 | 0.6 | 0 | 0 | 已写入 |

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
