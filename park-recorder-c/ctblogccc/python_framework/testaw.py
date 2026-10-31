#!/usr/bin/python
# -*- coding: UTF-8 -*-

from testtool import tool


# 自定义的AW请写作这个文件中

def init_service():
    return tool.send_cmd('init()')


def run_turns(times=1):
    for i in range(times):
        tool.send_cmd('init()')
