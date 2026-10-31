#!/usr/bin/python
# -*- coding: UTF-8 -*-
import logging
import os
import re
import socket
import subprocess
import threading
import time
import platform

logging.basicConfig(level=logging.DEBUG)
case_logger = logging.getLogger('case')
root_logger = logging.getLogger('root')
file_output = open("py_program.log", "w")
java_run_version = "1.8.0_272"
python_run_version = "3.8.6"
timestream = open('time.log', 'w')
starttime = time.time()


class UDPClient(object):

    def __init__(self, socket_timeout=3):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.socket.settimeout(socket_timeout)

    def sendOnly(self, msg, address):
        self.socket.sendto(bytes(msg, 'utf-8'), address)

    def send(self, msg, address, wait_time=0, buff_size=163840):
        self.socket.sendto(bytes(msg, 'utf-8'), address)
        case_logger.debug('Send: %s', msg)
        try:
            pre_data, address = self.socket.recvfrom(buff_size)
            data = pre_data.decode('utf-8')
        except socket.timeout:
            if msg != 'init()':
                case_logger.warning('Receive message time out for: %s', msg)
            data = ''
        except Exception as ex:
            if msg != 'init()':
                case_logger.error('Receive message error for: %s. %s', msg, ex)
            data = ''
        if wait_time > 0:
            time.sleep(wait_time)
        case_logger.debug('Receive: %s', data)
        return data

    def close(self):
        self.socket.close()


class TestToolException(Exception):
    def __init__(self, *args, **kwargs):
        pass


class MyThread(threading.Thread):
    def __init__(
            self, group=None, target=None, name=None, args=(), kwargs=None, daemon=True):
        threading.Thread.__init__(
            self, group=group, target=target, name=name, args=args, kwargs=kwargs)

        if kwargs is None:
            kwargs = {}
        self.__target = target
        self.__args = args
        self.__kwargs = kwargs
        self.result = None
        self.daemon = daemon

    def run(self):
        try:
            if self.__target:
                self.result = self.__target(*self.__args, **self.__kwargs)
        finally:
            del self.__target, self.__args, self.__kwargs

    def kill(self):
        try:
            self._Thread__stop()
        except:
            pass


class TestTool(object):

    def __init__(self):
        self.client = UDPClient(socket_timeout=5)
        self.address = self._init_address()

    @staticmethod
    def _init_address():
        path = os.path.join(os.path.dirname(__file__), 'config.properties')
        if os.path.isfile(path):
            fopen = open(path, 'r')
            try:
                for line in fopen:
                    line = line.strip()
                    if line.find('=') > 0 and not line.startswith('#'):
                        strs = line.split('=')
                        if (strs[0].strip() == 'udpPort'):
                            port = strs[1].strip()
                            return "127.0.0.1", int(port)
            finally:
                fopen.close()
        raise TestToolException('load config.properties error, pls check it')

    def close_udp(self):
        self.client.close()

    def logger_info(self, msg, *args, **kwargs):
        case_logger.info(msg, *args, **kwargs)

    def logger_endtime(self, info):
        timestream.writelines("[CASE COST]{}[TIME]{}\n".format(info, time.time() - self.starttime))

    def _logger_exam(self, info):
        self.client.send('[LOG]' + info, self.address)

    def case_start(self, info):
        self._logger_exam('[CASE][START] %s' % info)
        self.starttime = time.time()

    def case_finished(self, info):
        self._logger_exam('[CASE][FINISHED] %s' % info)
        self.logger_endtime(info)

    def case_failed(self, info):
        self._logger_exam('[CASE][FAILED] %s' % info)

    def send_cmd(self, cmd):
        return self.client.send(cmd, self.address)

    def send_cmd_only(self, cmd):
        return self.client.sendOnly(cmd, self.address)

    def print_log(self):
        while True:
            line = self.game_process.stdout.readline().decode('utf-8', 'ignore')
            if not line:
                continue
            else:
                case_logger.debug(line)
            if subprocess.Popen.poll(self.game_process) == 0:  # 判断子进程是否结束
                break

    def start_dut(self):
        self.check_java()
        self.check_python()
        self.start_program()
        self.wait_dut_start()

    def start_program(self):
        path = os.path.join(os.path.dirname(__file__), 'config.properties')
        triggered = False
        if os.path.isfile(path):
            fopen = open(path, 'r')
            try:
                for line in fopen:
                    line = line.strip()
                    if line.find('=') > 0 and not line.startswith('#'):
                        strs = line.split('=')
                        if (strs[0].strip() == 'trigger' and strs[1].strip() == 'judge'):
                            triggered = True
                            break
            finally:
                fopen.close()

        if not triggered:
            root_logger.info('start test program by framework')
            game_path = os.path.join(os.path.dirname(__file__))
            files = os.listdir(game_path)
            jarpath = None
            for filename in files:
                # 工作目录只能有一个jar
                if filename.endswith('jar'):
                    jarpath = filename
                    break
            if (jarpath == None):
                raise TestToolException('test program start error')
            print(platform.system())
            if 'Windows' == platform.system():
                self.game_process = subprocess.Popen(
                    'java -Xms128m -Xmx128m -jar ' + jarpath + ' config.properties', shell=True, stdout=subprocess.PIPE,
                    stderr=subprocess.STDOUT)
                self.print_thread = MyThread(target=self.print_log, daemon=True)
                self.print_thread.start()
            else:
                self.game_process = subprocess.Popen(
                    'java -Xms128m -Xmx128m -jar ' + jarpath + ' config.properties', shell=True, stdout=file_output,
                    cwd=game_path)

    def wait_dut_start(self):
        root_logger.info('wait test program start')
        for x in range(10):
            if hasattr(self, 'game_process') and self.game_process.poll() == 1:
                raise TestToolException(
                    "Address already in use: Cannot bind, Please modify config.properties udpPort Parameter")
            if self.client.send('init()', self.address) == u'E000:Success':
                root_logger.info('test program start success')
                return
            time.sleep(1)
        root_logger.info('test program start failed')
        self.stop_dut()
        raise TestToolException('test program start failed')

    def stop_dut(self):
        if hasattr(self, "print_thread") and self.print_thread.is_alive():
            self.print_thread.kill()
        root_logger.info('stop test program')
        self.send_cmd_only('EXIT()')
        self.close_udp()
        file_output.close()
        timestream.close()
        time.sleep(2)

    @staticmethod
    def check_java():
        try:
            output = subprocess.check_output(['java', '-version'], stderr=subprocess.STDOUT)
            java_version_str = str(output, encoding="utf-8")
            version_compile = re.compile("1.[0-9]+.[0-9]+_[0-9]+")
            span = version_compile.search(java_version_str).span()
            java_version = java_version_str[span[0]:span[1]]
            if java_version < java_run_version:
                root_logger.warning("The current Java version is:" + java_version + ", Earlier than Java:1.8.0_272")
        except Exception as e:
            root_logger.warning("An exception occurred when verifying the Java version. %s" % e)

    @staticmethod
    def check_python():
        try:
            output = subprocess.check_output(['python', '-V'], stderr=subprocess.STDOUT)
            python_version_str = str(output, encoding="utf-8")
            version_compile = re.compile("[1-9].[0-9]+.[0-9]+")
            span = version_compile.search(python_version_str).span()
            python_version = python_version_str[span[0]:span[1]]
            if python_version < python_run_version:
                root_logger.warning("The current Python version is:" + python_version + ", Earlier than Python:3.8.6")
        except Exception as e:
            root_logger.warning("An exception occurred when verifying the Python version. %s" % e)


tool = TestTool()
