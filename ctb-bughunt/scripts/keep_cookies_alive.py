# -*- coding: utf-8 -*-
"""
ilearning Cookie 保活
====================

每 N 秒(默认 300=5 分钟)带 cookies.json 请求:
  1. /sxz/api/ischool/v1/member/base-info          ← 鉴权探测
  2. /edx/lmsManagerAuth/api/system/only4ssoTimeUpdate.do  ← SSO 活动心跳

写入同目录旁路状态文件 <cookies>.keepalive.json,供前端 / API 展示。
遇 401: 置 alert=true,追加告警日志,可选 webhook;默认继续轮询(方便你上传新 Cookie 后自动恢复)。

用法:
  python keep_cookies_alive.py
  python keep_cookies_alive.py --cookies ../../cookies.json --interval 300
  python keep_cookies_alive.py --once          # 只探测一次
  python keep_cookies_alive.py --exit-on-401   # 401 后进程退出码 2

注意:保活只能缓解「闲置超时」,绝对过期/强制 MFA 仍需重新登录并上传 cookies.json。
"""

from __future__ import annotations

import argparse
import json
import os
import sys
import time
import traceback
from datetime import datetime
from typing import Any, Dict, Optional, Tuple
from urllib.parse import urljoin

try:
    import requests
    import urllib3
except ImportError:
    sys.exit("需要 requests: pip install requests")

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

BASE = "https://ilearning.huawei.com"
AUTH_PROBE = "/sxz/api/ischool/v1/member/base-info"
SSO_TOUCH = "/edx/lmsManagerAuth/api/system/only4ssoTimeUpdate.do"
UA = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    "AppleWebKit/537.36 (KHTML, like Gecko) "
    "Chrome/152.0.0.0 Safari/537.36"
)


def now_str() -> str:
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")


def status_path_for(cookies_path: str) -> str:
    return cookies_path + ".keepalive.json"


def alert_log_path_for(cookies_path: str) -> str:
    return cookies_path + ".keepalive.alert.log"


def load_cookie_header(path: str) -> str:
    if not os.path.isfile(path):
        raise FileNotFoundError("找不到 Cookie 文件: %s" % path)
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    header = (data.get("cookie_header") or "").strip()
    if not header:
        raise ValueError("%s 缺少 cookie_header,请先用 get_ilearning_cookies.py 或前端上传" % path)
    return header


def write_status(path: str, payload: Dict[str, Any]) -> None:
    payload = dict(payload)
    payload["updated_at"] = now_str()
    tmp = path + ".tmp"
    with open(tmp, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, indent=2)
    os.replace(tmp, path)


def append_alert(log_path: str, line: str) -> None:
    with open(log_path, "a", encoding="utf-8") as f:
        f.write("[%s] %s\n" % (now_str(), line))


def post_webhook(url: str, body: Dict[str, Any]) -> None:
    try:
        requests.post(url, json=body, timeout=10, verify=False)
    except Exception as e:
        print("webhook 失败: %s" % e)


def ping_once(cookie_header: str, verify: bool = False) -> Tuple[int, int, str]:
    """返回 (base_info_status, sso_touch_status, 摘要)。"""
    headers = {
        "Cookie": cookie_header,
        "User-Agent": UA,
        "Accept": "application/json, text/plain, */*",
        "sxz-lang": "zh_CN",
        "Referer": BASE + "/",
    }
    s = requests.Session()
    s.verify = verify
    s.headers.update(headers)

    r1 = s.get(urljoin(BASE, AUTH_PROBE), timeout=30)
    touch_status = 0
    touch_err = ""
    try:
        r2 = s.get(urljoin(BASE, SSO_TOUCH), timeout=30)
        touch_status = r2.status_code
    except Exception as e:
        touch_err = str(e)

    snippet = (r1.text or "")[:120].replace("\n", " ")
    summary = "base-info=%s touch=%s%s body=%s" % (
        r1.status_code,
        touch_status or "-",
        ("(%s)" % touch_err) if touch_err else "",
        snippet,
    )
    return r1.status_code, touch_status, summary


def run_loop(
    cookies_path: str,
    interval: int = 300,
    *,
    once: bool = False,
    exit_on_401: bool = False,
    webhook: str = "",
    verify: bool = False,
    stop_event: Optional[Any] = None,
    log: Optional[Any] = None,
) -> int:
    """保活主循环。stop_event 为 threading.Event 时可被外部停止。"""
    _log = log.info if log else (lambda m: print(m))
    _warn = log.warning if log else (lambda m: print(m))

    cookies_path = os.path.abspath(cookies_path)
    st_path = status_path_for(cookies_path)
    alert_path = alert_log_path_for(cookies_path)
    interval = max(30, int(interval))
    consecutive_fail = 0
    last_alert_sent = 0.0

    _log("Cookie 保活启动 cookies=%s interval=%ds" % (cookies_path, interval))

    while True:
        if stop_event is not None and stop_event.is_set():
            _log("Cookie 保活已停止")
            return 0

        status: Dict[str, Any] = {
            "cookies_path": cookies_path,
            "interval_sec": interval,
            "ok": False,
            "alert": False,
            "last_check_at": now_str(),
            "base_info_status": None,
            "sso_touch_status": None,
            "message": "",
            "consecutive_failures": consecutive_fail,
            "runner": "agent" if stop_event is not None else "cli",
        }
        try:
            header = load_cookie_header(cookies_path)
            mtime = os.path.getmtime(cookies_path)
            status["cookies_mtime"] = datetime.fromtimestamp(mtime).strftime("%Y-%m-%d %H:%M:%S")
            status["cookies_bytes"] = os.path.getsize(cookies_path)

            code, touch, summary = ping_once(header, verify=verify)
            status["base_info_status"] = code
            status["sso_touch_status"] = touch
            status["message"] = summary

            if code == 200:
                consecutive_fail = 0
                status["ok"] = True
                status["alert"] = False
                status["consecutive_failures"] = 0
                status["last_ok_at"] = now_str()
                _log("[%s] OK  %s" % (now_str(), summary))
            elif code == 401:
                consecutive_fail += 1
                status["ok"] = False
                status["alert"] = True
                status["consecutive_failures"] = consecutive_fail
                msg = "Cookie 已失效(401)。请重新登录并上传 cookies.json → %s" % cookies_path
                status["message"] = msg + " | " + summary
                _warn("[%s] ALERT  %s" % (now_str(), msg))
                append_alert(alert_path, msg + " | " + summary)
                if webhook and (time.time() - last_alert_sent) > 60:
                    post_webhook(
                        webhook,
                        {
                            "event": "cookie_401",
                            "cookies_path": cookies_path,
                            "at": now_str(),
                            "detail": summary,
                        },
                    )
                    last_alert_sent = time.time()
                write_status(st_path, status)
                if exit_on_401:
                    return 2
            else:
                consecutive_fail += 1
                status["ok"] = False
                status["alert"] = consecutive_fail >= 3
                status["consecutive_failures"] = consecutive_fail
                _warn("[%s] WARN  %s" % (now_str(), summary))
                if status["alert"]:
                    append_alert(alert_path, "连续失败: " + summary)
        except Exception as e:
            consecutive_fail += 1
            status["ok"] = False
            # 文件暂时不存在时不算严重告警(等用户上传)
            missing = isinstance(e, FileNotFoundError)
            status["alert"] = not missing
            status["consecutive_failures"] = consecutive_fail
            status["message"] = "%s: %s" % (type(e).__name__, e)
            _warn("[%s] ERROR  %s" % (now_str(), status["message"]))
            if not missing:
                append_alert(alert_path, status["message"] + "\n" + traceback.format_exc())
            write_status(st_path, status)
            if exit_on_401 and missing:
                return 2

        write_status(st_path, status)

        if once:
            return 0 if status.get("ok") else 1

        end = time.time() + interval
        while time.time() < end:
            if stop_event is not None and stop_event.is_set():
                _log("Cookie 保活已停止")
                return 0
            time.sleep(min(1.0, max(0.05, end - time.time())))


class KeepAliveService:
    """给 Agent Web 用的后台保活线程(单文件)。"""

    def __init__(
        self,
        cookies_path: str,
        interval: int = 300,
        webhook: str = "",
        verify: bool = False,
    ) -> None:
        import threading

        self.cookies_path = os.path.abspath(cookies_path)
        self.interval = interval
        self.webhook = webhook
        self.verify = verify
        self._stop = threading.Event()
        self._thread: Optional[threading.Thread] = None

    @property
    def running(self) -> bool:
        return bool(self._thread and self._thread.is_alive())

    def start(self) -> None:
        import logging
        import threading

        if self.running:
            return
        self._stop.clear()
        log = logging.getLogger("ctbagent.keepalive")

        def _target() -> None:
            try:
                run_loop(
                    self.cookies_path,
                    self.interval,
                    webhook=self.webhook,
                    verify=self.verify,
                    stop_event=self._stop,
                    log=log,
                )
            except Exception:
                log.exception("Cookie 保活线程异常退出")

        self._thread = threading.Thread(
            target=_target,
            name="cookie-keepalive",
            daemon=True,
        )
        self._thread.start()

    def stop(self, timeout: float = 5.0) -> None:
        self._stop.set()
        if self._thread and self._thread.is_alive():
            self._thread.join(timeout=timeout)

    def snapshot(self) -> Dict[str, Any]:
        return {
            "running": self.running,
            "mode": "single",
            "cookies_path": self.cookies_path,
            "interval_sec": self.interval,
        }


class MultiAccountKeepAliveService:
    """扫描 agent/accounts/*/cookies.json，按间隔逐个保活。"""

    def __init__(
        self,
        interval: int = 300,
        webhook: str = "",
        verify: bool = False,
    ) -> None:
        import threading

        self.interval = max(30, int(interval))
        self.webhook = webhook
        self.verify = verify
        self._stop = threading.Event()
        self._thread: Optional[threading.Thread] = None
        self._last_paths: list[str] = []

    @property
    def running(self) -> bool:
        return bool(self._thread and self._thread.is_alive())

    def _discover(self) -> list[str]:
        paths: list[str] = []
        try:
            # 延迟导入，避免脚本被独立运行时强依赖 agent 包布局
            from agent.auth import list_account_cookie_files

            paths = [str(p.resolve()) for p in list_account_cookie_files()]
        except Exception:
            pass
        return paths

    def start(self) -> None:
        import logging
        import threading

        if self.running:
            return
        self._stop.clear()
        log = logging.getLogger("ctbagent.keepalive")

        def _target() -> None:
            log.info("多账号 Cookie 保活启动 interval=%ds", self.interval)
            while not self._stop.is_set():
                paths = self._discover()
                self._last_paths = paths
                if not paths:
                    log.info("[%s] 尚无账号 cookies.json，等待上传", now_str())
                for p in paths:
                    if self._stop.is_set():
                        break
                    try:
                        run_loop(
                            p,
                            self.interval,
                            once=True,
                            webhook=self.webhook,
                            verify=self.verify,
                            log=log,
                        )
                    except Exception:
                        log.exception("保活失败: %s", p)
                # 整轮后等待 interval
                end = time.time() + self.interval
                while time.time() < end:
                    if self._stop.is_set():
                        break
                    time.sleep(min(1.0, max(0.05, end - time.time())))
            log.info("多账号 Cookie 保活已停止")

        self._thread = threading.Thread(
            target=_target,
            name="cookie-keepalive-multi",
            daemon=True,
        )
        self._thread.start()

    def stop(self, timeout: float = 5.0) -> None:
        self._stop.set()
        if self._thread and self._thread.is_alive():
            self._thread.join(timeout=timeout)

    def snapshot(self) -> Dict[str, Any]:
        return {
            "running": self.running,
            "mode": "multi_account",
            "interval_sec": self.interval,
            "accounts_watched": len(self._last_paths),
            "paths": list(self._last_paths),
        }


def main() -> None:
    ap = argparse.ArgumentParser(description="ilearning Cookie 保活(默认每 5 分钟)")
    ap.add_argument(
        "--cookies",
        default=os.environ.get("ILEARNING_COOKIES", "cookies.json"),
        help="cookies.json 路径(默认 cookies.json 或环境变量 ILEARNING_COOKIES)",
    )
    ap.add_argument("--interval", type=int, default=300, help="间隔秒数(默认 300=5 分钟)")
    ap.add_argument("--once", action="store_true", help="只探测一次后退出")
    ap.add_argument("--exit-on-401", action="store_true", help="遇 401 或缺少文件时以退出码 2 结束")
    ap.add_argument("--webhook", default="", help="401 时 POST JSON 到该 URL(可选)")
    ap.add_argument("--verify", action="store_true", help="校验证书(默认关闭)")
    args = ap.parse_args()
    try:
        code = run_loop(
            args.cookies,
            args.interval,
            once=args.once,
            exit_on_401=args.exit_on_401,
            webhook=args.webhook,
            verify=args.verify,
        )
    except KeyboardInterrupt:
        print("\n已停止保活")
        code = 0
    sys.exit(code)


if __name__ == "__main__":
    main()
