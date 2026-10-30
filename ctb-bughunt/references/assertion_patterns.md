# 断言与代码组织写法

约束：
- 主体用例优先用 `ctb.py record` 录制生成（见 `references/recording.md`）；本文的写法用于录制标记之外的手写用例：公式、累计关系、跨对象关系等需要表达推导过程的断言。
- 手写辅助函数放在 `testaw.py` 的录制标记之外，不要使用以 `_ctb_` 开头的名称，也不要定义 `run_steps` 或 `STEPS_` 开头的名称。
- 接口名、参数、返回文本、公式与常量都以题目描述和探针结果为准；下面的命令名与数值只是示例值。
- 辅助函数放在 `testaw.py`，用例方法只描述场景数据与流程。
- 断言消息写清命令、期望、实际，便于本地失败时定位。
- 所有公式函数都必须有对应的探针记录支撑，并在 `notes/数值模型.md` 中注明。

## testaw.py：调用与断言辅助

```python
from testtool import tool

OK = 'E000:Success'
FAIL = 'E001:Fail'


def send(cmd):
    return tool.send_cmd(cmd)


def expect(case, cmd, expected):
    ret = tool.send_cmd(cmd)
    case.assertTrue(ret == expected, '%s 期望 %s 实际 %s' % (cmd, expected, ret))
    return ret


def expect_prefix(case, cmd, prefix):
    ret = tool.send_cmd(cmd)
    case.assertTrue(ret.startswith(prefix), '%s 期望前缀 %s 实际 %s' % (cmd, prefix, ret))
    return ret


def value_of(ret):
    return ret.split(':', 1)[1] if ':' in ret else ''


def object_ids(count, start=1001):
    return [str(start + i) for i in range(count)]
```

## testaw.py：公式函数（探针确认后编写）

```python
def settle(base, rate_percent):
    return base * rate_percent // 100


def reward(paid, ratio_percent):
    return paid * ratio_percent // 100


def level_of(total, thresholds=(0, 500, 2000, 5000)):
    level = 0
    for i, t in enumerate(thresholds):
        if total >= t:
            level = i
    return level
```

## udpunittest.py：数据表 + 循环 + 期望状态

```python
    @timeout(global_default_timeout)
    def test_01_multi_object_staged_accumulation(self):
        tool.logger_info('12 个对象分 4 段累积，逐段精确断言累计值与等级，穿插非法分支')
        ids = testaw.object_ids(12)
        totals = dict((oid, 0) for oid in ids)
        for oid in ids:
            testaw.expect(self, 'create_obj(%s)' % oid, testaw.OK)
        for stage, amount in enumerate((100, 300, 800, 1500), 1):
            for idx, oid in enumerate(ids):
                if (idx + stage) % 3 == 0:
                    testaw.expect(self, 'use_obj(%s,-1)' % oid, testaw.FAIL)
                    continue
                paid = testaw.settle(amount, 90)
                testaw.expect(self, 'use_obj(%s,%d)' % (oid, amount), 'E000:%d' % paid)
                totals[oid] += testaw.reward(paid, 10)
                testaw.expect(self, 'query_obj(%s)' % oid,
                              'E000:%d,%d' % (totals[oid], testaw.level_of(totals[oid])))
```

## testaw.py：非法分支表

```python
ILLEGAL_CASES = (
    ('use_obj(9999,100)', 'E001:Fail'),
    ('use_obj(1001,0)', 'E001:Fail'),
    ('create_obj(1001)', 'E001:Fail'),
)
```

## udpunittest.py：遍历非法分支表

```python
        for cmd, expected in testaw.ILLEGAL_CASES:
            testaw.expect(self, cmd, expected)
```

## udpunittest.py：系统生成的值

```python
        ret = testaw.expect_prefix(self, 'create_obj(alpha)', 'E000:')
        oid = testaw.value_of(ret)
        testaw.expect(self, 'query_obj(%s)' % oid, 'E000:0,0')
```

## udpunittest.py：单调时钟推进

```python
        now = 10
        for oid in ids:
            now += 2
            testaw.expect(self, 'set_time(%d)' % now, testaw.OK)
            testaw.expect(self, 'use_obj(%s,100)' % oid, 'E000:90')
```
