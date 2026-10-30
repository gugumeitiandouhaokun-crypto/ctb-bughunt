# 测试框架规则

## 文件

| 文件 | 能否修改 | 说明 |
|---|---|---|
| `udpunittest.py` | 只能改 `MyTestCase1` 中「从下面开始编写测试用例」之后、`main` 段之前的部分 | 测试用例，提交 |
| `testaw.py` | 可以 | 可复用的调用封装、断言辅助、公式函数，提交 |
| `testtool.py`、`timeout.py` | 禁止 | 框架 |
| `config.properties` | 只有 `udpPort` 由脚本改为本地专用端口，其余禁止 | 配置，不提交 |
| `*.jar` | 禁止 | 被测程序；本地验证用无 bug 版本，答题目录根下只能有一个 jar |

## 编码约束

1. 只使用 `MyTestCase1` 一个测试类，不新增 TestCase 类。
2. 命名前缀（便于与队友代码合并，`check` 强制检查）：
   - 用例方法以 `test_cjl_` 开头（unittest 只执行 `test` 开头的方法），只用英文、数字、下划线，不能重名（重名时前一个会被静默覆盖）；
   - `testaw.py` 中新增的函数、类，`udpunittest.py` 中新增的顶层函数与 `MyTestCase1` 中新增的非用例方法，以 `cjl_` 开头；
   - 新增的全局变量以 `cjl_` 或 `CJL_` 开头（不带前缀时 `check` 告警）。
3. 每个用例方法前加 `@timeout(global_default_timeout)`，单个用例 300 秒内完成。
4. 只使用 Python 标准库；代码兼容 Python 3.8（不用 `match`、`str.removeprefix`、`dict | dict`、内置泛型下标注解等 3.9+ 特性）。
5. 命令字符串不含空格：命令名与括号之间、括号内侧、逗号之后都不能有空格。
6. `main` 段（`if __name__ == '__main__':` 至文件末尾，以及其上方的提示注释）不增删改、不改缩进。
7. 不在 `udpunittest.py` 定义与标准库或内置函数同名的全局名称（如 `open`、`id`、`input`）。
8. 不修改、删除 `udpunittest.log` 等日志相关逻辑。

## 框架行为

- `setUpClass` 启动被测程序（`tool.start_dut()`），`tearDownClass` 关闭。
- 每个用例结束后 `tearDown` 自动发送 `init()` 复位，用例之间状态独立。
- `self.assertTrue` / `self.assertFalse` 已被重写：失败时先调用 `tool.case_failed()` 记录，再抛出断言错误。断言请统一用这两个方法。
- 通信：`tool.send_cmd('命令(参数1,参数2)')` 返回字符串，`tool.logger_info('说明')` 写日志。
- 返回格式以题目描述和探针为准，常见形式：`E000:Success` / `E000:<值>` 表示成功，`E0xx:<文本>` 表示失败。

## 用例结构

```python
    @timeout(global_default_timeout)
    def test_cjl_01_multi_object_cycle(self):
        """多对象分段累积 + 非法分支混合"""
        testaw.cjl_expect(self, 'init()', 'E000:Success')
        for oid in testaw.cjl_object_ids(12):
            testaw.cjl_expect(self, 'create_obj(%s)' % oid, 'E000:Success')
```

## 本地运行与判定

- `ctb.py local` 在答题目录执行 `python udpunittest.py`，结果写入 `udpunittest.log`。
- 以日志末尾 `Ran N tests in Xs` 后的 `OK` / `FAILED (...)` 判定；进程退出码非 0 不代表失败。
- 每个用例耗时记录在 `time.log` 的 `[CASE COST]` 行，`ctb.py local` 会列出并对接近 300 秒的用例告警；耗时同时写入 `notes/local_last.json`，供判题后精简用例。
- 被测程序端口：`pull` 与每次启动被测程序（`probe`、`record`、`local`、`coverage`）前，脚本把 `udpPort` 改为本工作目录专用端口（20000-29999 中由路径决定、UDP/TCP 均空闲的端口，不在系统临时端口范围内）。
  - 专用端口上残留的 java 进程按端口查 PID 结束；被其他程序占用时换端口，不结束该程序。
  - 运行结束后被测程序未退出时，同样按端口清理。
  - 手动停止用 `ctb.py stop`（先发 `EXIT()`，仍占用再按端口结束 java 进程）。
  - 不按进程名结束进程（如 `taskkill /IM java.exe`、`pkill java`），会误杀其他 java 程序。
- 后台执行：`local`、`coverage`、`record`、`run`、`submit` 在后台运行，输出写入 `notes/jobs/<任务ID>.log`。
  - 命令最多等待 50 秒，未结束时输出"后台任务进行中"；之后执行 `ctb.py wait` 读取新增输出，直到"后台任务结束"。
  - 同一工作目录同时只运行一个后台任务，任务未结束时再次启动会被拒绝。
  - `ctb.py cancel` 终止任务并停止被测程序。

## 判题结果

`ctb.py run` / `submit` 打印并保存（`notes/judge/`）：

- 得分与判题状态。
- bug 检出表：每个 bug 编号是否检出、由哪些用例触发。
- 考察点正确率。
- 代码质量扣分项：检查项、数量、扣分规则。按扣分项重构代码，扣分规则记入 `notes/复盘.md`。
