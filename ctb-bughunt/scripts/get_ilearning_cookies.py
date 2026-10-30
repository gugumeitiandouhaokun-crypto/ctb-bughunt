# -*- coding: utf-8 -*-
"""
动态获取 ilearning.huawei.com 的登录 Cookie
==========================================

背景(对抓包 network_20260909_203052.jsonl 的分析结论):

  直接访问业务页时,/sxz/api/* 接口返回 401,页面整窗跳转到
  https://login.huawei.com/login1/?redirect=<原地址> ,随后自动完成华为 SPES 免密登录:

    1. GET  login.huawei.com/login1/rest/hwidcenter/spes-info
       → 返回登录服务器 TLS 证书、加密时间戳、random
    2. POST https://localhost:18080/getSpesInfo        ← 本机 SPES 代理
       带上一步的证书+时间戳 → 代理确认"对方是真登录服务器"后,
       返回当前企业账号的加密凭据 key/content
    3. POST login.huawei.com/login1/rest/hwidcenter/spes-verify
       带凭据+设备指纹 → 返回账号信息(c30062068)+ spesKey
    4. POST login.huawei.com/login1/rest/hwidcenter/spes-login
       带凭据+spesKey+random+targetUrl → 服务端种下 SSO Cookie(.huawei.com 域)
    5. 页面跳回原地址,浏览器带上 Cookie,后续 /sxz/api/* 全部 200

  全程无需密码。前提只有一个:本机 SPES 代理(localhost:18080)在运行、
  企业账号已在该客户端登录(录制的那次就是这么自动完成的)。

  (注:具体是哪几个 Cookie 键、由哪个响应的 Set-Cookie 种下,本次录制看不到——
   CDP 把 Cookie/Set-Cookie 头放在 *ExtraInfo 事件里,旧版录制器未捕获;
   network_recorder.py 已升级,再录一次即可在 jsonl 的 cookie/set_cookie 字段看到。)

本脚本的做法:用真实浏览器把上面整条链自动走完,然后把 Cookie 收割出来,
供 requests 等纯 HTTP 客户端复用。

依赖:同目录的 network_recorder.py(复用其浏览器启动逻辑)、selenium

用法:
  python get_ilearning_cookies.py                # 默认起始页,自动登录后收割
  python get_ilearning_cookies.py <起始页URL>
  python get_ilearning_cookies.py --timeout 120 --out my.json --keep-open

输出:
  - cookies.json: ilearning.huawei.com 可见的全部 Cookie(含域/路径/过期时间)
  - 控制台: Cookie 请求头字符串 + requests 调用示例
"""

import argparse
import json
import os
import sys
import time
from datetime import datetime
from urllib.parse import quote

try:
    import network_recorder as nr  # 复用浏览器启动(Chrome/Edge 自动选择等)
except ImportError:
    sys.exit("找不到 network_recorder.py,请把本脚本和它放在同一目录运行")

# 默认起始页:用这次抓包里验证过能触发完整登录流程的考试页。
# 任何 ilearning.huawei.com 下的页面都可以,登录成功后浏览器会回到该页。
DEFAULT_TARGET = "https://ilearning.huawei.com/iexam/100000/ilearningExercise/softTestingAnswer?subjectId=1590273249147121665"
# 登录页地址(带 redirect 参数),SPES 免密登录后自动跳回目标页
LOGIN_PAGE = "https://login.huawei.com/login1/?redirect=%s"
# 鉴权探测接口(相对当前 ilearning 页面):200 即已登录
AUTH_PROBE = "/sxz/api/ischool/v1/member/base-info"


def wait_until_authed(driver, timeout):
    """等待自动登录完成:窗口回到 ilearning 域 且 鉴权探测接口返回 200"""
    deadline = time.time() + timeout
    last_state = ""
    while time.time() < deadline:
        try:
            url = driver.current_url
            if "login.huawei.com" in url:
                last_state = "登录页(SPES 免密进行中): %s" % url[:90]
            elif "ilearning.huawei.com" in url:
                # 在页面上下文里发一个探测请求,200 说明 Cookie 已就位
                status = driver.execute_script(
                    "return fetch(%s, {credentials: 'include'})"
                    ".then(function(r){ return r.status; })"
                    ".catch(function(){ return 0; })" % json.dumps(AUTH_PROBE))
                if status == 200:
                    return
                last_state = "已回到 ilearning,鉴权接口状态=%s" % status
            else:
                last_state = "当前页面: %s" % url[:90]
        except nr.WebDriverException:
            raise RuntimeError("浏览器被关闭了")
        time.sleep(1)
    raise RuntimeError(
        "等待登录超时(%d 秒)。最后状态: %s\n"
        "常见原因: 本机 SPES 代理(https://localhost:18080)没在运行,"
        "或企业账号未在该客户端登录,登录页停在二维码/密码界面。" % (timeout, last_state))


def fmt_expiry(c):
    exp = c.get("expiry")
    if not exp:
        return "会话结束"
    try:
        return datetime.fromtimestamp(exp).strftime("%Y-%m-%d %H:%M")
    except (ValueError, OSError):
        return str(exp)


def main():
    ap = argparse.ArgumentParser(description="动态获取 ilearning.huawei.com 登录 Cookie")
    ap.add_argument("url", nargs="?", default=DEFAULT_TARGET,
                    help="起始页,任意 ilearning.huawei.com 页面(默认为抓包验证过的考试页)")
    ap.add_argument("--timeout", type=int, default=90, help="等待自动登录的超时秒数(默认90)")
    ap.add_argument("--out", default="cookies.json", help="Cookie 保存路径(默认 cookies.json)")
    ap.add_argument("--keep-open", action="store_true", help="拿到 Cookie 后不关浏览器,便于接着手动操作")
    args = ap.parse_args()

    print("正在启动浏览器 ...")
    driver, browser = nr.launch_browser()
    if driver is None:
        print("启动浏览器失败:\n%s" % browser)
        sys.exit(1)
    print("已启动:%s" % browser)

    try:
        # 直接打开带 redirect 的登录页:未登录时 SPES 免密自动进行,完成后跳回目标页。
        # (比先开业务页再等它自己 401 跳转更直接,且不依赖目标页自身的跳转逻辑)
        login_url = LOGIN_PAGE % quote(args.url, safe="")
        print("打开登录页并等待 SPES 免密自动登录 ...")
        driver.get(login_url)
        wait_until_authed(driver, args.timeout)
        print("登录成功,已回到: %s" % driver.current_url[:90])

        # ---- 收割 Cookie ----
        cookies = driver.get_cookies()  # 当前 ilearning 页面可见的全部 Cookie(含 .huawei.com 域)
        cookies.sort(key=lambda c: (c.get("domain", ""), c["name"]))
        header = "; ".join("%s=%s" % (c["name"], c["value"]) for c in cookies)

        payload = {
            "harvested_at": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            "account_probe": "https://ilearning.huawei.com" + AUTH_PROBE,
            "target": args.url,
            "cookies": cookies,
            "cookie_header": header,
        }
        with open(args.out, "w", encoding="utf-8") as f:
            json.dump(payload, f, ensure_ascii=False, indent=2)

        print("\n==== 收割到 %d 个 Cookie ====" % len(cookies))
        for c in cookies:
            print("  %-30s 域=%-24s 过期=%s" % (c["name"], c.get("domain", ""), fmt_expiry(c)))
        shown = header if len(header) <= 400 else header[:400] + " ..."
        print("\nCookie 请求头(直接塞进请求头即可):\n  %s" % shown)
        print("\n已保存到 %s" % os.path.abspath(args.out))

        print('\nrequests 用法示例:')
        print('  import requests, json, urllib3')
        print('  urllib3.disable_warnings()  # 内网证书链不全时')
        print('  data = json.load(open(%s, encoding="utf-8"))' % json.dumps(args.out))
        print('  r = requests.get(')
        print('      "https://ilearning.huawei.com/sxz/api/iexercise/v1/solftest/getSubjectInfo/1590273249147121665",')
        print('      headers={"Cookie": data["cookie_header"]},')
        print('      verify=False)')
        print('  print(r.json())')
    finally:
        if args.keep_open:
            print("\n浏览器保持打开(--keep-open),手动关闭即可。")
        else:
            try:
                driver.quit()
            except Exception:
                pass


if __name__ == "__main__":
    main()
