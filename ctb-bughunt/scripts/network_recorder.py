# -*- coding: utf-8 -*-
"""
Chrome 网络请求记录器
====================

功能:
  1. 启动一个 Chrome 浏览器窗口(由你手动操作)
  2. 你在浏览器里正常访问网站、点击、登录、提交等
  3. 脚本在后台持续记录所有 HTTP/HTTPS 请求 —— 即 F12 -> Network 面板里看到的内容
  4. 结果实时写入本地两个文件:
       network_年月日_时分秒.log    人类可读,每个请求一行
       network_年月日_时分秒.jsonl  结构化数据,每行一个 JSON(含请求头/响应头/请求体/响应体)
  5. 直接关闭浏览器窗口,或在脚本窗口按 Ctrl+C,脚本自动收尾保存

依赖(只需装一次):
  pip install selenium
  (selenium >= 4.6 自带 Selenium Manager; launch_browser 先离线用本地/缓存驱动,失败再联网下载)

用法:
  python network_recorder.py                      # 打开默认起始页(见下方 START_URL)
  python network_recorder.py https://example.com  # 启动后直接打开指定网址

与 F12 Network 面板的一致性:
  - 页面内的请求(Document/XHR/Fetch/JS/CSS/Img/Font/WebSocket握手 等)全部记录
  - 请求头里的 Cookie、响应头里的 Set-Cookie 不在常规 Network 事件里,而在
    *ExtraInfo 事件中,脚本会捕获并记入条目的 cookie / set_cookie 字段(jsonl)
  - 浏览器扩展、Chrome 后台组件发出的请求不属于页面 Network,同样不记录
  - Service Worker 内部发起的请求可能记录不全
  - 响应体仅对 XHR/Fetch/Document 且小于 MAX_BODY_BYTES 的请求抓取;
    多标签页时,非当前焦点标签页的响应体可能拿不到(请求本身仍然会记录)
"""

import json
import os
import shutil
import sys
import time
from collections import Counter
from datetime import datetime
from pathlib import Path

try:
    from selenium import webdriver
    from selenium.webdriver.chrome.options import Options as ChromeOptions
    from selenium.webdriver.edge.options import Options as EdgeOptions
    from selenium.common.exceptions import WebDriverException
except ImportError:
    sys.exit("缺少 selenium,请先执行: pip install selenium")

# ============================ 配置区(按需修改) ============================
BROWSER = "auto"                      # "auto" | "chrome" | "edge"
                                     # auto: 优先 Chrome,本机没装 Chrome 则用 Edge(同为 Chromium 内核)
START_URL = "https://www.baidu.com"   # 启动后打开的网址;设为 "" 则只开一个空白新标签页
CHROME_BINARY = ""                    # Chrome 装在非默认位置时填写,
                                     # 例如 r"C:\Program Files\Google\Chrome\Application\chrome.exe"
EDGE_BINARY = ""                      # Edge 装在非默认位置时填写
USER_DATA_DIR = ""                    # 留空 = 每次使用全新临时配置(干净的浏览器);
                                     # 填写 = 复用某个浏览器用户目录,可以保留历史登录态,例如
                                     # r"F:\ctbtest\webtest\browser_profile"
                                     # 注意:填写后需先关闭其它正在运行的同类浏览器窗口,否则启动失败
CAPTURE_RESPONSE_BODY = True          # 是否尝试抓取响应体(仅 XHR/Fetch/Document,写入 jsonl)
MAX_BODY_BYTES = 512 * 1024           # 请求体/响应体超过该字节数则截断
POLL_INTERVAL = 0.3                   # 轮询间隔(秒)。不要调太大,否则浏览器内部日志缓冲可能溢出丢请求
URL_MAX_DISPLAY = 500                 # 文本日志里 URL 的最大显示长度(jsonl 中保留完整 URL)
# ========================================================================


# ------------------------------ 小工具函数 ------------------------------

def _norm_ts(v):
    """把 CDP / chromedriver 的时间戳统一成 Unix 秒"""
    try:
        v = float(v)
    except (TypeError, ValueError):
        return None
    return v / 1000.0 if v > 1e12 else v


def _fmt_time(sec):
    if not sec:
        return "??:??:??.???"
    dt = datetime.fromtimestamp(sec)
    return "%s.%03d" % (dt.strftime("%H:%M:%S"), dt.microsecond // 1000)


def _fmt_size(n):
    if n is None:
        return "-"
    n = float(n)
    for unit in ("B", "KB", "MB", "GB"):
        if n < 1024 or unit == "GB":
            return ("%.0f%s" if unit == "B" else "%.1f%s") % (n, unit)
        n /= 1024.0


def _truncate(s, limit=MAX_BODY_BYTES):
    if s is None:
        return None
    if len(s) > limit:
        return s[:limit] + "...[已截断,原始长度 %d 字符]" % len(s)
    return s


def _clean_url(url):
    """只处理超长的 data: URL,避免把日志撑爆;普通 URL 原样保留"""
    if url and url.startswith("data:") and len(url) > 200:
        return url[:200] + "...(data: 已缩短)"
    return url


def new_log_base():
    return os.path.join(os.getcwd(), "network_%s" % datetime.now().strftime("%Y%m%d_%H%M%S"))


# ------------------------------ 浏览器启动 ------------------------------

def _apply_common_args(opts):
    if USER_DATA_DIR:
        opts.add_argument("--user-data-dir=%s" % USER_DATA_DIR)
    opts.add_argument("--start-maximized")
    opts.add_argument("--no-first-run")
    opts.add_argument("--no-default-browser-check")
    opts.add_argument("--remote-allow-origins=*")
    # 去掉"正受到自动测试软件控制"的提示条,让手动操作更接近正常浏览器
    opts.add_experimental_option("excludeSwitches", ["enable-automation"])
    opts.add_experimental_option("useAutomationExtension", False)
    opts.add_argument("--disable-blink-features=AutomationControlled")
    return opts


def build_chrome_options():
    opts = _apply_common_args(ChromeOptions())
    if CHROME_BINARY:
        opts.binary_location = CHROME_BINARY
    # 核心:打开 performance 日志,driver 才会收集 Network 事件
    opts.set_capability("goog:loggingPrefs", {"performance": "ALL"})
    return opts


def build_edge_options():
    opts = _apply_common_args(EdgeOptions())
    if EDGE_BINARY:
        opts.binary_location = EDGE_BINARY
    # Edge(msedgedriver)对应的是 ms: 前缀;同时也带上 goog: 以兼容不同版本
    opts.set_capability("ms:loggingPrefs", {"performance": "ALL"})
    opts.set_capability("goog:loggingPrefs", {"performance": "ALL"})
    return opts


def _find_driver_executable(browser_key):
    """PATH 上的 chromedriver/msedgedriver，或 Selenium 缓存里最新一份。"""
    cli = "chromedriver" if browser_key == "chrome" else "msedgedriver"
    on_path = shutil.which(cli)
    if on_path:
        return on_path
    folder = "chromedriver" if browser_key == "chrome" else "msedgedriver"
    exe = _driver_exe_name(browser_key)
    base = _selenium_cache_root() / folder
    if not base.is_dir():
        return None
    candidates = sorted(
        base.rglob(exe),
        key=lambda p: p.stat().st_mtime,
        reverse=True,
    )
    return str(candidates[0]) if candidates else None


def _try_launch(order, *, prefer_local=False):
    """prefer_local=True 时设 SE_OFFLINE，优先用本地/缓存驱动，不联网检查版本。"""
    from selenium.webdriver.chrome.service import Service as ChromeService
    from selenium.webdriver.edge.service import Service as EdgeService

    if prefer_local:
        os.environ["SE_OFFLINE"] = "true"
    else:
        os.environ.pop("SE_OFFLINE", None)

    errors = []
    try:
        for name in order:
            try:
                driver_path = _find_driver_executable(name)
                if name == "chrome":
                    if driver_path:
                        svc = ChromeService(driver_path)
                        return webdriver.Chrome(service=svc, options=build_chrome_options()), "chrome"
                    return webdriver.Chrome(options=build_chrome_options()), "chrome"
                if driver_path:
                    svc = EdgeService(driver_path)
                    return webdriver.Edge(service=svc, options=build_edge_options()), "edge"
                return webdriver.Edge(options=build_edge_options()), "edge"
            except WebDriverException as ex:
                errors.append("%s: %s" % (name, str(ex).splitlines()[0][:200]))
        return None, "\n".join(errors)
    finally:
        if prefer_local:
            os.environ.pop("SE_OFFLINE", None)


def _driver_exe_name(browser_key):
    return "chromedriver.exe" if browser_key == "chrome" else "msedgedriver.exe"


def _driver_on_path(browser_key):
    name = "chromedriver" if browser_key == "chrome" else "msedgedriver"
    return shutil.which(name) is not None


def _selenium_cache_root():
    raw = os.environ.get("SE_CACHE_PATH", "").strip()
    if raw:
        return Path(raw)
    return Path.home() / ".cache" / "selenium"


def _has_cached_driver(browser_key):
    folder = "chromedriver" if browser_key == "chrome" else "msedgedriver"
    base = _selenium_cache_root() / folder
    if not base.is_dir():
        return False
    exe = _driver_exe_name(browser_key)
    return any(base.rglob(exe))


def _has_driver(browser_key):
    return _driver_on_path(browser_key) or _has_cached_driver(browser_key)


def _prefetch_driver(browser_key):
    """通过 Selenium Manager 联网解析/下载与浏览器匹配的驱动。"""
    from selenium.webdriver.common.selenium_manager import SeleniumManager

    browser = "chrome" if browser_key == "chrome" else "MicrosoftEdge"
    args = ["--browser", browser]
    binary = CHROME_BINARY if browser_key == "chrome" else EDGE_BINARY
    if binary:
        args.extend(["--browser-path", binary])
    os.environ.pop("SE_OFFLINE", None)
    SeleniumManager().binary_paths(args)


def launch_browser():
    """按 BROWSER 配置启动浏览器,返回 (driver, 'chrome'|'edge');失败返回 (None, 原因)"""
    order = {
        "chrome": ["chrome"],
        "edge": ["edge"],
        "auto": ["chrome", "edge"],
    }.get(BROWSER.lower(), ["chrome", "edge"])

    # 1) 离线优先: PATH / Selenium 缓存驱动,不触发 Selenium Manager 联网
    driver, err = _try_launch(order, prefer_local=True)
    if driver is not None:
        return driver, err

    # 2) 本地启动失败再联网下载或更新驱动后重试
    missing = [name for name in order if not _has_driver(name)]
    if missing:
        print("未检测到 %s 驱动,正在联网下载 ..." % " / ".join(missing))
    else:
        print("本地驱动启动失败,正在联网下载/更新驱动后重试 ...")
    for name in order:
        try:
            _prefetch_driver(name)
        except WebDriverException as ex:
            print("  %s 驱动获取失败: %s" % (name, str(ex).splitlines()[0][:200]))

    driver, err2 = _try_launch(order, prefer_local=False)
    if driver is not None:
        return driver, err2
    if err2:
        return None, err + "\n--- 重试后 ---\n" + err2
    return None, err


# ------------------------------ 记录器本体 ------------------------------

class Recorder:
    """解析 performance 日志里的 Network 事件,整理成请求记录并写入文件"""

    def __init__(self, driver, base_path):
        self._driver = driver
        self.log_path = base_path + ".log"
        self.jsonl_path = base_path + ".jsonl"
        self._log_f = open(self.log_path, "w", encoding="utf-8")
        self._jsonl_f = open(self.jsonl_path, "w", encoding="utf-8")
        self._log_f.write("# Chrome 网络请求记录  开始于 %s\n" % datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
        self._log_f.write("# 字段: 时间 方法 状态 资源类型 大小 耗时 URL\n\n")
        self._pending = {}      # requestId -> 请求条目(等响应/完成后落盘)
        self._stash_cookie = {}     # ExtraInfo 先到而条目未建时,暂存 Cookie 请求头
        self._stash_set_cookie = {} # 同上,暂存 Set-Cookie 响应头
        self.count = 0
        self.by_type = Counter()
        self.by_status = Counter()

    # ---------------- 对外接口 ----------------

    def feed(self, entries):
        """消费一次 driver.get_log('performance') 返回的结果"""
        for item in entries:
            try:
                msg = json.loads(item.get("message", "{}")).get("message", {})
            except (ValueError, AttributeError):
                continue
            method = msg.get("method", "")
            if not method.startswith("Network."):
                continue
            params = msg.get("params", {})
            # 两个时钟,不能混用:
            #   wall —— 日志条目自带的时间戳,是真正的墙钟(Unix 秒),用于显示
            #   mono —— CDP 事件里的 timestamp,是开机以来的单调秒数,只能做差算耗时
            wall = _norm_ts(item.get("timestamp"))
            mono = _norm_ts(params.get("timestamp"))
            try:
                self._dispatch(method, wall, mono, params)
            except Exception:
                pass  # 单个事件解析失败不影响整体记录

    def finish(self):
        """结束记录:把仍在进行中的请求也写出去,追加统计,关闭文件"""
        for rid in list(self._pending):
            e = self._pending.pop(rid)
            e["note"] = "未完成(结束时仍在进行中)"
            self._emit(e)
        self._log_f.write("\n# ===== 统计 =====\n")
        self._log_f.write("# 共 %d 个请求\n" % self.count)
        for t, n in self.by_type.most_common():
            self._log_f.write("#   %-12s %d\n" % (t, n))
        for s, n in self.by_status.most_common(15):
            self._log_f.write("#   状态 %-12s %d\n" % (s, n))
        self._log_f.write("# 结束于 %s\n" % datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
        self._log_f.close()
        self._jsonl_f.close()

    # ---------------- 事件处理 ----------------

    def _dispatch(self, method, wall, mono, params):
        if method == "Network.requestWillBeSent":
            self._on_request(wall, mono, params)
        elif method == "Network.responseReceived":
            self._on_response(params)
        elif method == "Network.loadingFinished":
            self._on_finished(mono, params)
        elif method == "Network.loadingFailed":
            self._on_failed(mono, params)
        elif method == "Network.webSocketCreated":
            self._on_ws_created(wall, params)
        elif method == "Network.webSocketWillSendHandshakeRequest":
            self._on_ws_handshake_req(mono, params)
        elif method == "Network.webSocketHandshakeResponseReceived":
            self._on_ws_response(mono, params)
        elif method == "Network.webSocketClosed":
            self._on_ws_closed(mono, params)
        elif method == "Network.requestWillBeSentExtraInfo":
            self._on_req_extra(params)
        elif method == "Network.responseReceivedExtraInfo":
            self._on_resp_extra(params)
        # 其余 Network.* 事件(dataReceived/WebSocket帧数据 等)信息价值低,忽略

    def _on_request(self, wall, mono, params):
        rid = params.get("requestId")
        if rid in self._pending and "redirectResponse" in params:
            # 重定向时同一个 requestId 会被复用:先把重定向前的那一跳落盘
            e = self._pending.pop(rid)
            rr = params.get("redirectResponse", {})
            e["status"] = rr.get("status")
            e["size"] = rr.get("encodedDataLength")
            e["duration_ms"] = self._dur(mono, e)
            e["note"] = "redirect"
            self._emit(e)
        req = params.get("request", {})
        e = {
            "requestId": rid,
            "time": _fmt_time(wall),
            "method": req.get("method", "?"),
            "url": _clean_url(req.get("url", "")),
            "type": params.get("type", ""),
            "status": None,
            "request_headers": req.get("headers", {}),
            "post_data": _truncate(req.get("postData")),
            "initiator": (params.get("initiator") or {}).get("type", ""),
            "_start": mono,
        }
        if rid in self._stash_cookie:
            e["cookie"] = self._stash_cookie.pop(rid)
        if req.get("hasPostData") and not e["post_data"]:
            e["post_data"] = self._get_post_data(rid)  # 大请求体不在事件里,单独取
        self._pending[rid] = e

    def _on_response(self, params):
        e = self._pending.get(params.get("requestId"))
        if not e:
            return
        resp = params.get("response", {})
        e["status"] = resp.get("status")
        e["status_text"] = resp.get("statusText", "")
        e["response_headers"] = resp.get("headers", {})
        e["mime_type"] = resp.get("mimeType", "")
        e["from_cache"] = bool(resp.get("fromDiskCache") or resp.get("fromPrefetchCache"))
        e["remote_ip"] = resp.get("remoteIPAddress", "")
        if params.get("requestId") in self._stash_set_cookie:
            e["set_cookie"] = self._stash_set_cookie.pop(params.get("requestId"))

    def _on_req_extra(self, params):
        # 真正的 Cookie 请求头只出现在 requestWillBeSentExtraInfo 里
        headers = params.get("headers") or {}
        cookie = headers.get("cookie") or headers.get("Cookie")
        if not cookie:
            return
        rid = params.get("requestId")
        e = self._pending.get(rid)
        if e is not None:
            e["cookie"] = cookie
        else:
            self._stash_cookie[rid] = cookie  # 条目还没建,先存着

    def _on_resp_extra(self, params):
        # 真正的 Set-Cookie 响应头只出现在 responseReceivedExtraInfo 里
        headers = params.get("headers") or {}
        sc = headers.get("set-cookie") or headers.get("Set-Cookie")
        if not sc:
            return
        if isinstance(sc, list):
            sc = "\n".join(sc)
        rid = params.get("requestId")
        e = self._pending.get(rid)
        if e is not None:
            e["set_cookie"] = sc
        else:
            self._stash_set_cookie[rid] = sc

    def _on_finished(self, mono, params):
        e = self._pending.pop(params.get("requestId"), None)
        if not e:
            return
        e["size"] = params.get("encodedDataLength")
        e["duration_ms"] = self._dur(mono, e)
        self._maybe_get_body(e)
        self._emit(e)

    def _on_failed(self, mono, params):
        e = self._pending.pop(params.get("requestId"), None)
        if not e:
            return
        err = params.get("errorText", "")
        if params.get("canceled"):
            err += " (canceled)"
        e["error"] = err
        e["duration_ms"] = self._dur(mono, e)
        self._emit(e)

    def _on_ws_created(self, wall, params):
        # WebSocket 握手不产生 requestWillBeSent,用独立事件记一条
        rid = params.get("requestId")
        self._pending[rid] = {
            "requestId": rid,
            "time": _fmt_time(wall),
            "method": "GET",  # 握手固定为 GET 升级请求
            "url": _clean_url(params.get("url", "")),
            "type": "WebSocket",
            "status": None,
            "initiator": (params.get("initiator") or {}).get("type", ""),
        }

    def _on_ws_handshake_req(self, mono, params):
        e = self._pending.get(params.get("requestId"))
        if not e:
            return
        e["request_headers"] = params.get("request", {}).get("headers", {})
        e["_start"] = mono  # created 事件不带 timestamp,耗时从这里起算

    def _on_ws_response(self, mono, params):
        # 握手完成即落盘;之后连接保持多久、何时断开与本条记录无关
        e = self._pending.pop(params.get("requestId"), None)
        if not e:
            return
        resp = params.get("response", {})
        e["status"] = resp.get("status")
        e["status_text"] = resp.get("statusText", "")
        e["response_headers"] = resp.get("headers", {})
        e["duration_ms"] = self._dur(mono, e)
        e["note"] = "websocket握手"
        self._emit(e)

    def _on_ws_closed(self, mono, params):
        # 握手没完成就断开的(如连不上服务器),把错误信息带出去
        e = self._pending.pop(params.get("requestId"), None)
        if not e:
            return
        e["error"] = params.get("errorText") or "websocket握手未完成即关闭"
        e["duration_ms"] = self._dur(mono, e)
        self._emit(e)

    # ---------------- 辅助 ----------------

    @staticmethod
    def _dur(sec, e):
        if sec and e.get("_start"):
            return round((sec - e["_start"]) * 1000, 1)
        return None

    def _get_post_data(self, rid):
        try:
            r = self._driver.execute_cdp_cmd("Network.getRequestPostData", {"requestId": rid})
            return _truncate(r.get("postData"))
        except Exception:
            return None

    def _maybe_get_body(self, e):
        if not CAPTURE_RESPONSE_BODY or not self._driver:
            return
        if e.get("type") not in ("XHR", "Fetch", "Document"):
            return
        if e.get("error") or e.get("from_cache"):
            return
        size = e.get("size") or 0
        if size and size > MAX_BODY_BYTES:
            e["response_body_note"] = "响应超过 %d 字节,未捕获" % MAX_BODY_BYTES
            return
        try:
            r = self._driver.execute_cdp_cmd("Network.getResponseBody", {"requestId": e["requestId"]})
            if r.get("base64Encoded"):
                e["response_body_b64"] = _truncate(r.get("body", ""))
            else:
                e["response_body"] = _truncate(r.get("body", ""))
        except Exception:
            pass  # 响应体已释放 / 请求不在当前标签页等,拿不到就作罢(请求本身已记录)

    def _emit(self, e):
        e.pop("_start", None)
        self.count += 1
        self.by_type[e.get("type") or "?"] += 1
        st = e.get("status")
        key = str(st) if st is not None else (e.get("error") or "未完成")
        self.by_status[key] += 1
        status_disp = key if st is not None else (e.get("error") or "----")
        line = "[%s] %-7s %-26s %-10s %8s %9s %s" % (
            e.get("time", "?"),
            e.get("method", "?"),
            status_disp[:26],
            (e.get("type") or "")[:10],
            _fmt_size(e.get("size")),
            ("%.0fms" % e["duration_ms"]) if e.get("duration_ms") is not None else "-",
            (e.get("url") or "")[:URL_MAX_DISPLAY],
        )
        if e.get("note"):
            line += "  <%s>" % e["note"]
        self._log_f.write(line + "\n")
        self._log_f.flush()
        self._jsonl_f.write(json.dumps(e, ensure_ascii=False) + "\n")
        self._jsonl_f.flush()
        try:
            print(line)
        except UnicodeEncodeError:
            print(line.encode("ascii", "replace").decode("ascii"))


# ------------------------------ 主流程 ------------------------------

def main():
    if hasattr(sys.stdout, "reconfigure"):
        try:
            sys.stdout.reconfigure(errors="replace")
        except Exception:
            pass

    start_url = sys.argv[1].strip() if len(sys.argv) > 1 else START_URL

    print("正在启动浏览器 ...")
    driver, browser = launch_browser()
    if driver is None:
        print("启动浏览器失败:\n%s" % browser)
        print("排查建议:")
        print("  1) 确认本机已安装 Chrome(或 Edge)浏览器")
        print("  2) 升级 selenium: pip install -U selenium")
        print("  3) 浏览器装在非默认位置时,修改脚本顶部 CHROME_BINARY / EDGE_BINARY")
        print("  4) 设置了 USER_DATA_DIR 时,先关闭其它正在运行的同类浏览器窗口")
        sys.exit(1)
    print("已启动:%s" % browser)

    recorder = Recorder(driver, new_log_base())
    if start_url:
        try:
            driver.get(start_url)
        except WebDriverException:
            pass  # 打不开起始页也不影响后续手动浏览和记录

    print("=" * 74)
    print(" 浏览器已启动,请在这个窗口里自由操作(打开网站、点击、登录、提交等)")
    print(" 所有网络请求正被实时记录到:")
    print("   文本日志: %s" % os.path.abspath(recorder.log_path))
    print("   JSON日志: %s" % os.path.abspath(recorder.jsonl_path))
    print(" 结束方式:直接关闭浏览器窗口,或在本窗口按 Ctrl+C")
    print("=" * 74)

    known_handles = set(driver.window_handles)
    try:
        while True:
            # ---- 取一批 Network 事件 ----
            try:
                entries = driver.get_log("performance")
                if entries:
                    recorder.feed(entries)
            except WebDriverException:
                # 可能整个浏览器关了,也可能只是当前标签页关了,再确认一下
                try:
                    driver.window_handles
                except WebDriverException:
                    print("\n浏览器已关闭。")
                    break
            # ---- 用户新开/关闭标签页时,让驱动跟随最新标签页(响应体捕获尽量可用) ----
            try:
                handles = driver.window_handles
                if set(handles) != known_handles:
                    known_handles = set(handles)
                    if handles:
                        driver.switch_to.window(handles[-1])
            except WebDriverException:
                pass
            time.sleep(POLL_INTERVAL)
    except KeyboardInterrupt:
        print("\n收到 Ctrl+C,停止记录。")
    finally:
        recorder.finish()
        try:
            driver.quit()
        except Exception:
            pass
        print("\n共记录 %d 个请求,日志已保存:" % recorder.count)
        print("  文本日志: %s" % os.path.abspath(recorder.log_path))
        print("  JSON日志: %s" % os.path.abspath(recorder.jsonl_path))


if __name__ == "__main__":
    main()
