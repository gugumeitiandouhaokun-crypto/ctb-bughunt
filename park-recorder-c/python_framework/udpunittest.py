#!/usr/bin/python
# -*- coding: UTF-8 -*-

import os
import unittest
import testaw
from testtool import tool
from timeout import timeout

global_default_timeout = 300


class MyTestCase1(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        tool.logger_info('setUpClass：这个类所有用例执行前执行此方法')
        tool.start_dut()

    @classmethod
    def tearDownClass(cls):
        tool.logger_info('tearDownClass：这个类所有用例执行后执行此方法')
        tool.stop_dut()

    def setUp(self):
        tool.logger_info('setUp：这个类的每个测试用例执行前执行此方法')
        tool.case_start(self.id())

    # 在每个测试用例结束之后,发送init()命令,复位被测程序,保证测试用例之间的隔离
    def tearDown(self):
        tool.logger_info('tearDown：这个类的每个测试用例执行后执行此方法')
        tool.case_finished(self.id())
        tool.send_cmd('init()')

    def assertTrue(self, expr, msg=None):
        if not expr:
            tool.case_failed(self.id())
        super(MyTestCase1, self).assertTrue(expr, msg)

    def assertFalse(self, expr, msg=None):
        if expr:
            tool.case_failed(self.id())
        super(MyTestCase1, self).assertFalse(expr, msg)

    # 从下面开始编写测试用例
    #  1. 注意具体的测试用例一定要以test开头，用例名禁止使用中文
    #  2. 只支持一个测试用例套，请使用自动化脚本默认的class类，不要修改和自定义class类。
    #  3. 只能使用原生python自带的模块，不能引用第三方模块，阅卷时阅卷系统不能识别第三方模块
    #  4. 考生如果需要封装AW，请在testaw.py中进行封装
    #  5. 如果出现端口冲突，请修改config.properties文件，重新指定其他的端口号。
    #  6. 注意每个测试用例前增加@timeout(seconds)修饰符，默认seconds为300秒， 测试用例执行超过seconds后用例会超时异常退出
    #  7. 如果需要单步调试，请注释掉装饰器 @timeout
    #  8. pycharm点击左侧绿色三角形按钮,运行单个testcase
    #  9. 考生只应该编写udpunittest.py、testaw.py。testtool.py和timeout.py不允许考生修改
    # 10. 单个用例执行时长限制为300s内完成，超过300s的用例为无效用例
    # 11. 导致main方法执行失败：1）不要在udpunittest.py中定义open全局变量或者与引用库的函数名冲突的变量或函数；2）不要修改main方法的内容、格式和缩进等，执行失败会导致阅卷异常
    # 12. 每次考试前请下载最新附件框架，并在最新框架下进行用例编写（udpunittest.py文件可能会更新，使用旧版本会导致0分）
    # 13. 接口参数与逗号、参数与括号之间不要预留空格，如fun1(空格param1空格,空格param2空格)，空格会被当作param的一部分导致失败。
    # 14. bug/bug.log、udpunittest.log日志文件流不要修改或删除，会影响阅卷判分。

    @timeout(global_default_timeout)
    def test_01(self):
        tool.logger_info('用例01描述：这是一个测试用例的样例')

        ret = tool.send_cmd('init()')
        self.assertTrue(ret == 'E000:Success', '系统初始化')

        ret = tool.send_cmd('check_park_statistics(0|0|0,0|0|0,0|0|0)')
        self.assertTrue(ret == 'E000:Success', '检查停车场车位统计信息')

        ret = tool.send_cmd('reserve_space(S100001,S001)')
        self.assertTrue(ret.startswith('E001'), '车位预约')

        ret = tool.send_cmd('cancel_reserve(r12345678)')
        self.assertTrue(ret.startswith('E001'), '取消车位预约')

        ret = tool.send_cmd('check_reserve_status(r12345678,locked)')
        self.assertTrue(ret.startswith('E001'), '检查预约状态')

        ret = tool.send_cmd('query_reserve(CID12345678)')
        self.assertTrue(ret.startswith('E001'), '查询车位预约订单')

        ret = tool.send_cmd('take_wash_order(S12345)')
        self.assertTrue(ret.startswith('E001'), '洗车取号')

        ret = tool.send_cmd('cancel_wash_order(001)')
        self.assertTrue(ret.startswith('E001'), '取消洗车服务')

        ret = tool.send_cmd('query_wash_status(001)')
        self.assertTrue(ret.startswith('E001'), '查询洗车订单状态')

        ret = tool.send_cmd('start_wash_service(001)')
        self.assertTrue(ret.startswith('E001'), '开始洗车服务')

        ret = tool.send_cmd('end_wash_service(001)')
        self.assertTrue(ret.startswith('E001'),  '结束洗车服务')

        ret = tool.send_cmd('trigger_wash_balance()')
        self.assertTrue(ret.startswith('E000'), '触发洗车队列均衡')

        ret = tool.send_cmd('query_wash_queue()')
        self.assertTrue(
            ret == 'E000:WS1|null|[null,null,null,null,null,null];WS2|null|[null,null,null,null,null,null];WS3|null|[null,null,null,null,null,null];WM1|null|[null,null,null,null,null,null];WM2|null|[null,null,null,null,null,null];WL1|null|[null,null,null,null,null,null]',
            '查询洗车工位队列')


# 提示：联调函数
# 1. 该方法不要增删改里面的内容，不要加缩进等（总之，不要修改，格式化代码注意缩进）
# 2. 答案用例请写在本函数上方
if __name__ == '__main__':
    logfile = os.path.join(os.path.dirname(__file__), 'udpunittest.log')
    stream = open(logfile, 'w')

    unittest.main(testRunner=unittest.TextTestRunner(stream, verbosity=2))
