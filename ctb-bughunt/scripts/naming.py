# -*- coding: utf-8 -*-
"""提交代码的命名前缀，便于与队友的代码合并时不冲突。

unittest 只收集以 test 开头的方法，所以用例方法的前缀是 test_cjl_；
testaw.py 与 MyTestCase1 中新增的其他函数、类以 cjl_ 开头（大小写不敏感）。
"""
import re

TEST_PREFIX = "test_cjl_"
AW_PREFIX = "cjl_"
TABLE_PREFIX = "cjl_steps_"
TEST_NAME_RE = re.compile(r"^test_cjl_[A-Za-z0-9_]+$")


def is_aw_name(name: str) -> bool:
    return name.lower().startswith(AW_PREFIX)
