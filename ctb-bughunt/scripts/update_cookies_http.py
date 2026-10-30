# -*- coding: utf-8 -*-
"""
纯 HTTP 更新 ilearning Cookie(不启动浏览器)
==========================================

依据抓包 network_20260928_203552 还原的登录链:

  0. GET  login.huawei.com/login1/?redirect=...   ← 拿 JSESSIONID
     并写入 idss_cid(=fingerPrint.cid)、lang=zh   ← 与登录页 JS 一致
  1. GET  .../hwidcenter/spes-info
  2. POST https://localhost:18080/getSpesInfo
  3. POST .../hwidcenter/login  (账号+密码+fingerPrint+spesLoginReq)
     → Set-Cookie(hwsso_login / x_sso_token / ...)
  4. 可选探测 base-info,写出 cookies.json

上次失败原因(statusText=SUCCESS 但 uid=null、只有 JSESSIONID):
  跳过了第 0 步,服务端不种 SSO Cookie。本版已补上。

用法:
  python update_cookies_http.py -a c30062068 --probe --out cookies.json
  python update_cookies_http.py -a c30062068 --mode spes --probe   # 尝试免密 spes-verify/login
"""

from __future__ import annotations

import argparse
import getpass
import hashlib
import json
import os
import sys
import time
import uuid
from datetime import datetime
from typing import Any, Dict, List, Optional, Tuple
from urllib.parse import parse_qs, quote

try:
    import requests
    import urllib3
except ImportError:
    sys.exit("需要 requests: pip install requests")

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

LOGIN_PAGE = "https://login.huawei.com/login1/"
SPES_INFO_URL = "https://login.huawei.com/login1/rest/hwidcenter/spes-info"
GET_SPES_URL = "https://localhost:18080/getSpesInfo"
LOGIN_URL = "https://login.huawei.com/login1/rest/hwidcenter/login?x_app_id="
SPES_VERIFY_URL = "https://login.huawei.com/login1/rest/hwidcenter/spes-verify?x_app_id="
SPES_LOGIN_URL = "https://login.huawei.com/login1/rest/hwidcenter/spes-login?x_app_id="
UEMCONFIG_URL = "https://login.huawei.com/login1/rest/hwidcenter/get-uemconfig?x_app_id="
DOUBLE_FACTOR_PAGE = "https://login.huawei.com/login1/login-doubleFactor.html"
AUTH_PROBE = "https://ilearning.huawei.com/sxz/api/ischool/v1/member/base-info"
DEFAULT_TARGET = (
    "https://ilearning.huawei.com/iexam/100000/ilearningExercise/"
    "softTestingAnswer?subjectId=1590273249147121665"
)
UA = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    "AppleWebKit/537.36 (KHTML, like Gecko) "
    "Chrome/152.0.0.0 Safari/537.36"
)
SSO_COOKIE_NAMES = ("hwsso_login", "x_sso_token", "login_uid", "login_sid", "hwssot")


def _md5(text: str) -> str:
    return hashlib.md5(text.encode("utf-8")).hexdigest()


def build_fingerprint(seed: Optional[str] = None) -> Dict[str, Any]:
    seed = seed or str(uuid.uuid4())
    cid = str(uuid.uuid4())
    now_ms = int(time.time() * 1000)
    data = {
        "canvas": _md5("canvas:" + seed),
        "webgl": _md5("webgl:" + seed),
        "ips": [],
        "devs": [],
        "epl": 5,
        "ep": _md5("ep:" + seed),
        "epls": ",".join(
            [
                "P" + _md5("p:" + seed)[:31],
                "C" + _md5("c:" + seed)[:31],
                "M" + _md5("m:" + seed)[:31],
                "W" + _md5("w:" + seed)[:31],
            ]
        ),
        "fonts": _md5("fonts:" + seed),
        "erd": ",",
        "nacn": "Mozilla",
        "nan": "Netscape",
        "nce": True,
        "nlg": "zh-CN",
        "npf": "Win32",
        "scd": 24,
        "etz": "Asia/Shanghai",
        "etzo": -480,
        "ndm": 32,
        "ua": UA,
        "nhc": 20,
        "nwd": False,
        "math": _md5("math:" + seed),
        "nts": _md5("nts:" + seed),
        "nmtp": 0,
    }
    return {
        "version": "1.7.4",
        "cid": cid,
        "meta": {
            "timeStart": now_ms,
            "time": 20,
            "domFp": "ready",
            "domFpStart": now_ms + 100,
            "domFpTime": 120,
        },
        "data": data,
    }


def new_session() -> requests.Session:
    s = requests.Session()
    s.verify = False
    s.headers.update(
        {
            "User-Agent": UA,
            "Accept": "application/json, text/plain, */*",
            "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8",
        }
    )
    return s


def login_referer(target_url: str) -> str:
    return LOGIN_PAGE + "?redirect=%s" % quote(target_url, safe="")


def set_browser_like_cookies(session: requests.Session, fingerprint: Dict[str, Any]) -> None:
    """对齐抓包:登录请求前 Cookie 含 lang / idss_cid(=fingerPrint.cid)。"""
    cid = fingerprint["cid"]
    session.cookies.set("lang", "zh", domain="login.huawei.com", path="/")
    session.cookies.set("idss_cid", cid, domain="login.huawei.com", path="/")
    # 风控脚本常见字段,无则生成
    session.cookies.set(
        "ztsg_ruuid",
        "01" + uuid.uuid4().hex[:8] + "-" + str(uuid.uuid4()),
        domain="login.huawei.com",
        path="/",
    )


def bootstrap_login_page(session: requests.Session, target_url: str) -> None:
    r = session.get(
        login_referer(target_url),
        headers={"Accept": "text/html,application/xhtml+xml,*/*"},
        timeout=30,
    )
    # 页面本身 200 即可;部分环境证书/跳转异常也不阻断,只要有 JSESSIONID
    names = {c.name for c in session.cookies}
    print("      login 页 HTTP %s, Cookie=%s" % (r.status_code, sorted(names)))


def ingest_set_cookies(session: requests.Session, response: requests.Response) -> List[str]:
    """把响应里所有 Set-Cookie 强制写入 jar(避免 Domain=huawei.com 被忽略)。"""
    raw_headers = getattr(response.raw, "headers", None)
    lines: List[str] = []
    if raw_headers is not None:
        if hasattr(raw_headers, "get_all"):
            lines = list(raw_headers.get_all("Set-Cookie") or [])
        elif hasattr(raw_headers, "getlist"):
            lines = list(raw_headers.getlist("Set-Cookie") or [])
    if not lines:
        sc = response.headers.get("Set-Cookie")
        if sc:
            # 单行或多行粘在一起时尽量按逗号粗分不可靠;优先用上面 get_all
            lines = [sc]

    ingested: List[str] = []
    for line in lines:
        if not line or "=" not in line:
            continue
        # 只解析第一段 name=value,属性单独扫
        first, _, rest = line.partition(";")
        name, _, value = first.partition("=")
        name, value = name.strip(), value.strip()
        if not name:
            continue
        domain = "huawei.com"
        path = "/"
        secure = False
        for part in rest.split(";"):
            part = part.strip()
            if not part:
                continue
            pk, _, pv = part.partition("=")
            pk_l = pk.strip().lower()
            pv = pv.strip()
            if pk_l == "domain" and pv:
                domain = pv.lstrip(".")
            elif pk_l == "path" and pv:
                path = pv
            elif pk_l == "secure":
                secure = True
        # Domain=huawei.com → 对 ilearning / login 都可见
        try:
            session.cookies.set(name, value, domain=domain, path=path, secure=secure)
        except Exception:
            session.cookies.set(name, value, domain="login.huawei.com", path=path)
        ingested.append(name)
    return ingested


def fetch_spes_info(session: requests.Session, target_url: str) -> Dict[str, Any]:
    t_ms = int(time.time() * 1000)
    r = session.get(
        SPES_INFO_URL,
        params={"_t": t_ms, "x_app_id": ""},
        headers={"Referer": login_referer(target_url), "Accept": "*/*"},
        timeout=30,
    )
    r.raise_for_status()
    body = r.json()
    result = body.get("result") or body
    for key in ("certificate", "stimeStamp"):
        if not result.get(key):
            raise RuntimeError("spes-info 缺少字段 %s: %s" % (key, json.dumps(body, ensure_ascii=False)[:300]))
    return result


def fetch_spes_credential(session: requests.Session, spes_info: Dict[str, Any]) -> Dict[str, str]:
    form = {
        "version": "v3",
        "certificate": spes_info["certificate"],
        "sTimeStamp": spes_info["stimeStamp"],
    }
    try:
        r = session.post(
            GET_SPES_URL,
            data=form,
            headers={
                "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
                "Referer": "https://login.huawei.com/",
                "Origin": "https://login.huawei.com",
            },
            timeout=30,
        )
    except requests.exceptions.ConnectionError as e:
        raise RuntimeError(
            "连不上本机 SPES 代理 https://localhost:18080/getSpesInfo。\n"
            "请确认华为企业客户端已登录且 SPES 代理在跑。\n原始错误: %s" % e
        ) from e
    if r.status_code != 200:
        raise RuntimeError("getSpesInfo HTTP %s: %s" % (r.status_code, r.text[:300]))
    parsed = {k: (v[0] if v else "") for k, v in parse_qs(r.text, keep_blank_values=True).items()}
    if not parsed.get("key") or not parsed.get("content"):
        raise RuntimeError("getSpesInfo 响应缺少 key/content: %s" % r.text[:300])
    return {
        "version": parsed.get("version") or "v3",
        "key": parsed["key"],
        "content": parsed["content"],
        "enablePwFree": parsed.get("enablePwFree") or "",
    }


def touch_uemconfig(session: requests.Session, target_url: str) -> None:
    try:
        session.get(
            UEMCONFIG_URL,
            headers={"Referer": login_referer(target_url)},
            timeout=15,
        )
    except Exception as e:
        print("      get-uemconfig 跳过: %s" % e)


def summarize_login_json(login_json: Any) -> str:
    if not isinstance(login_json, dict):
        return "非 JSON"
    info = login_json.get("accountInfo") or {}
    parts = [
        "statusCode=%s" % login_json.get("statusCode"),
        "statusText=%s" % login_json.get("statusText"),
        "uid=%s" % info.get("uid"),
        "maskUid=%s" % info.get("maskUid"),
        "redirect=%s" % info.get("redirect"),
        "bifactor=%s" % info.get("bifactor"),
        "mfaKey=%s" % (("..." + str(info.get("mfaKey"))[-8:]) if info.get("mfaKey") else None),
        "has_encryUid=%s" % bool(info.get("encryUid")),
    ]
    err = login_json.get("errorCode") or login_json.get("errorMessage")
    if err:
        parts.append("error=%s" % err)
    return ", ".join(parts)


def bifactor_hint(login_json: Any) -> str:
    """密码已过、双因子未过时的说明。"""
    if not isinstance(login_json, dict):
        return ""
    info = login_json.get("accountInfo") or {}
    if not info.get("bifactor"):
        return ""
    mfa_key = info.get("mfaKey") or ""
    mask = info.get("maskUid") or "?"
    url = double_factor_url(mfa_key, mask) if mfa_key else "(无 mfaKey)"
    return (
        "\n\n原因: 账号密码已通过,但触发了双因子(bifactor=true)。\n"
        "此时 uid/redirect 故意为空,也不会种 hwsso_login——要等 MFA 完成才会填。\n"
        "maskUid=%s\n"
        "双因子页:\n  %s\n"
        "可选:\n"
        "  1) 加 --mfa-browser: HTTP 走到 bifactor 后自动弹浏览器只完成 MFA 并收割 Cookie\n"
        "  2) 或直接用: python get_ilearning_cookies.py\n"
        "说明: 当前环境 getSpesInfo.enablePwFree=tplogin(不含 w3login),浏览器本身也不会走 SPES 免密。"
        % (mask, url)
    )


def do_password_login(
    session: requests.Session,
    account: str,
    password: str,
    target_url: str,
    spes_cred: Dict[str, str],
    fingerprint: Dict[str, Any],
) -> requests.Response:
    payload = {
        "loginAccount": account,
        "password": password,
        "encryptedPasswordSwitch": "off",
        "appId": "",
        "uid": account,
        "cid": "",
        "gradeState": "",
        "lang": "zh_CN",
        "rememberAccountName": False,
        "targetUrl": quote(target_url, safe=""),
        "headers": {"Content-Type": "application/json; charset=UTF-8"},
        "fingerPrint": fingerprint,
        "spesLoginReq": {
            "version": spes_cred.get("version") or "v3",
            "key": spes_cred["key"],
            "content": spes_cred["content"],
        },
    }
    return session.post(
        LOGIN_URL,
        json=payload,
        headers={
            "Content-Type": "application/json",
            "Referer": login_referer(target_url),
            "Origin": "https://login.huawei.com",
            "Accept": "application/json, text/plain, */*",
            "X-Requested-With": "XMLHttpRequest",
        },
        timeout=30,
    )


def can_try_spes_passwordless(spes_cred: Dict[str, str]) -> Tuple[bool, str]:
    """与登录页一致: enablePwFree 必须包含 w3login 才自动免密。"""
    ep = spes_cred.get("enablePwFree") or ""
    if "w3login" in ep:
        return True, ep
    return False, (
        "getSpesInfo.enablePwFree=%r, 不含 'w3login'。"
        "登录页在这种情况下会直接放弃免密,改走账号密码(+spesLoginReq)。"
        "因此 spes-verify 会返回 code=-1 / spesMsgError——这是预期,不是脚本偶发错误。"
        % ep
    )


def do_spes_verify_login(
    session: requests.Session,
    target_url: str,
    spes_info: Dict[str, Any],
    spes_cred: Dict[str, str],
    fingerprint: Dict[str, Any],
) -> Tuple[requests.Response, Optional[requests.Response], str]:
    """免密路径。请求体对齐登录页 verifySpes / spesAuth。"""
    encoded_target = quote(target_url, safe="")
    random_val = spes_info.get("random") or ""
    verify_body = {
        "version": spes_cred.get("version") or "v3",
        "key": spes_cred["key"],
        "content": spes_cred["content"],
        "fingerPrint": fingerprint,
        "appId": "",
        "random": random_val,
        "targetUrl": encoded_target,
    }
    verify_resp = session.post(
        SPES_VERIFY_URL,
        json=verify_body,
        headers={
            "Content-Type": "application/json",
            "Referer": login_referer(target_url),
            "Origin": "https://login.huawei.com",
            "X-Requested-With": "XMLHttpRequest",
        },
        timeout=30,
    )
    ingest_set_cookies(session, verify_resp)
    try:
        vj = verify_resp.json()
    except Exception:
        vj = {}
    result = (vj.get("result") if isinstance(vj, dict) else None) or {}
    if not isinstance(result, dict):
        result = {}
    # 页面判断: dataResult.code == 200 才继续 spes-login
    if str(result.get("code")) != "200":
        return (
            verify_resp,
            None,
            "spes-verify code=%s account=%s" % (result.get("code"), result.get("account")),
        )
    spes_key = result.get("spesKey") or ""
    login_body = {
        "version": spes_cred.get("version") or "v3",
        "key": spes_cred["key"],
        "content": spes_cred["content"],
        "targetUrl": encoded_target,
        "appId": "",
        "gradeState": "",
        "spesKey": spes_key,
        "random": random_val,
    }
    login_resp = session.post(
        SPES_LOGIN_URL,
        json=login_body,
        headers={
            "Content-Type": "application/json",
            "Referer": login_referer(target_url),
            "Origin": "https://login.huawei.com",
            "X-Requested-With": "XMLHttpRequest",
        },
        timeout=30,
    )
    return verify_resp, login_resp, ""


def double_factor_url(mfa_key: str, mask_uid: str, lang: str = "zh") -> str:
    return (
        "%s?language=%s&mfaKey=%s&maskUid=%s&x_app_id="
        % (DOUBLE_FACTOR_PAGE, quote(lang), quote(mfa_key), quote(mask_uid or ""))
    )


def harvest_cookies_via_mfa_browser(
    mfa_key: str,
    mask_uid: str,
    timeout: int = 180,
) -> Tuple[str, List[Dict[str, Any]]]:
    """密码已过、卡在双因子时:只开浏览器完成 MFA,再收割 Cookie。"""
    try:
        import network_recorder as nr
    except ImportError as e:
        raise RuntimeError("需要同目录 network_recorder.py + selenium 才能 --mfa-browser") from e

    url = double_factor_url(mfa_key, mask_uid)
    print("      打开双因子页,请在浏览器里完成验证 ...")
    print("      %s" % url)
    driver, browser = nr.launch_browser()
    if driver is None:
        raise RuntimeError("启动浏览器失败: %s" % browser)
    print("      已启动: %s" % browser)
    try:
        driver.get(url)
        deadline = time.time() + timeout
        last = ""
        while time.time() < deadline:
            try:
                cur = driver.current_url or ""
                if "ilearning.huawei.com" in cur:
                    status = driver.execute_script(
                        "return fetch(%s, {credentials: 'include'})"
                        ".then(function(r){ return r.status; })"
                        ".catch(function(){ return 0; })"
                        % json.dumps("/sxz/api/ischool/v1/member/base-info")
                    )
                    if status == 200:
                        break
                    last = "已跳回业务域, base-info=%s" % status
                else:
                    last = "等待双因子完成: %s" % cur[:90]
            except Exception as e:
                last = str(e)
            time.sleep(1)
        else:
            raise RuntimeError("等待 MFA 超时(%ds)。最后状态: %s" % (timeout, last))

        raw = driver.get_cookies()
        raw.sort(key=lambda c: (c.get("domain", ""), c["name"]))
        header = "; ".join("%s=%s" % (c["name"], c["value"]) for c in raw)
        items = [
            {
                "name": c["name"],
                "value": c["value"],
                "domain": c.get("domain", ""),
                "path": c.get("path", "/"),
                "secure": bool(c.get("secure")),
                "expiry": c.get("expiry"),
            }
            for c in raw
        ]
        return header, items
    finally:
        try:
            driver.quit()
        except Exception:
            pass


def cookie_header_from_session(session: requests.Session) -> Tuple[str, List[Dict[str, Any]]]:
    # 按 name 去重,优先保留 Domain 含 huawei 的
    by_name: Dict[str, Dict[str, Any]] = {}
    for c in session.cookies:
        item = {
            "name": c.name,
            "value": c.value,
            "domain": c.domain or "",
            "path": c.path or "/",
            "secure": bool(c.secure),
            "expiry": c.expires,
        }
        prev = by_name.get(c.name)
        if prev is None or ("huawei" in (item["domain"] or "") and "huawei" not in (prev.get("domain") or "")):
            by_name[c.name] = item
    items = sorted(by_name.values(), key=lambda x: (x.get("domain", ""), x["name"]))
    header = "; ".join("%s=%s" % (c["name"], c["value"]) for c in items)
    return header, items


def has_sso_cookies(session: requests.Session) -> bool:
    names = {c.name for c in session.cookies}
    return bool(set(SSO_COOKIE_NAMES) & names)


def probe_auth(session: requests.Session) -> Tuple[int, str]:
    r = session.get(AUTH_PROBE, timeout=30)
    snippet = (r.text or "")[:200].replace("\n", " ")
    return r.status_code, snippet


def save_cookies(
    path: str,
    target_url: str,
    header: str,
    cookies: List[Dict[str, Any]],
    probe_status: Optional[int],
    method: str,
) -> None:
    payload = {
        "harvested_at": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "method": method,
        "account_probe": AUTH_PROBE,
        "probe_status": probe_status,
        "target": target_url,
        "cookies": cookies,
        "cookie_header": header,
    }
    with open(path, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, indent=2)


def resolve_credentials(args: argparse.Namespace, need_password: bool) -> Tuple[str, str]:
    account = (args.account or os.environ.get("ILEARNING_ACCOUNT") or os.environ.get("HW_ACCOUNT") or "").strip()
    password = args.password or os.environ.get("ILEARNING_PASSWORD") or os.environ.get("HW_PASSWORD") or ""
    if not account and need_password:
        account = input("企业账号 (loginAccount): ").strip()
    if need_password and not password:
        password = getpass.getpass("密码 (不回显): ")
    if need_password and (not account or not password):
        sys.exit("密码模式必须提供 --account/--password 或环境变量 ILEARNING_ACCOUNT / ILEARNING_PASSWORD")
    if not need_password and not account:
        account = (args.account or "").strip()
    return account, password


def main() -> None:
    ap = argparse.ArgumentParser(description="纯 HTTP 更新 ilearning cookies.json(需本机 SPES)")
    ap.add_argument("--account", "-a", help="企业账号,也可用环境变量 ILEARNING_ACCOUNT")
    ap.add_argument("--password", "-p", help="密码,也可用环境变量 ILEARNING_PASSWORD")
    ap.add_argument("--target", default=DEFAULT_TARGET, help="登录成功后的跳转目标 URL")
    ap.add_argument("--out", default="cookies.json", help="输出路径(默认 cookies.json)")
    ap.add_argument("--probe", action="store_true", help="登录后请求 base-info 校验 Cookie")
    ap.add_argument("--fp-seed", default="", help="指纹随机种子(可选,默认每次随机)")
    ap.add_argument(
        "--mode",
        choices=("password", "spes", "auto"),
        default="password",
        help="password=账号密码(默认); spes=免密(仅 enablePwFree 含 w3login 时可用); auto=具备条件才试免密",
    )
    ap.add_argument(
        "--mfa-browser",
        action="store_true",
        help="密码登录若返回 bifactor,则弹浏览器只完成双因子并收割 Cookie",
    )
    ap.add_argument("--mfa-timeout", type=int, default=180, help="--mfa-browser 等待秒数(默认180)")
    ap.add_argument("--dump-login-body", action="store_true", help="调试:打印 login 完整响应 JSON")
    args = ap.parse_args()

    need_password = args.mode in ("password", "auto")
    account, password = resolve_credentials(args, need_password=need_password)
    if args.mode == "spes":
        account, password = resolve_credentials(args, need_password=False)

    target_url = args.target.strip()
    session = new_session()
    fingerprint = build_fingerprint(args.fp_seed or None)

    print("[0/5] 打开登录页 + 写入 idss_cid ...")
    bootstrap_login_page(session, target_url)
    set_browser_like_cookies(session, fingerprint)
    print("      idss_cid=%s" % fingerprint["cid"])

    print("[1/5] GET spes-info ...")
    spes_info = fetch_spes_info(session, target_url)
    print("      ok  certificate=%dB  stimeStamp=%dB  random=%s" % (
        len(spes_info.get("certificate") or ""),
        len(spes_info.get("stimeStamp") or ""),
        (spes_info.get("random") or "")[:16] + "...",
    ))

    print("[2/5] POST localhost:18080/getSpesInfo ...")
    spes_cred = fetch_spes_credential(session, spes_info)
    print("      ok  enablePwFree=%s  key=%dB  content=%dB" % (
        spes_cred.get("enablePwFree") or "-",
        len(spes_cred["key"]),
        len(spes_cred["content"]),
    ))
    spes_ok, spes_reason = can_try_spes_passwordless(spes_cred)
    if not spes_ok:
        print("      注意: %s" % spes_reason)

    touch_uemconfig(session, target_url)

    login_resp: Optional[requests.Response] = None
    login_json: Any = None
    method_used = "http_api_password"
    modes_to_try: List[str] = []
    if args.mode == "auto":
        modes_to_try = (["spes"] if spes_ok else []) + ["password"]
    elif args.mode == "spes":
        modes_to_try = ["spes"]
    else:
        modes_to_try = ["password"]

    for mode in modes_to_try:
        if mode == "spes":
            if not spes_ok:
                print("[3/5] 跳过免密: %s" % spes_reason)
                if args.mode == "spes":
                    sys.exit(
                        "当前不能走 SPES 免密。\n%s\n"
                        "请改用: python update_cookies_http.py -a 账号 --mfa-browser --probe --out cjl.json\n"
                        "或:     python get_ilearning_cookies.py"
                        % spes_reason
                    )
                continue
            print("[3/5] POST spes-verify + spes-login (免密) ...")
            verify_resp, login_resp, verr = do_spes_verify_login(
                session, target_url, spes_info, spes_cred, fingerprint
            )
            print("      verify HTTP %s: %s" % (verify_resp.status_code, (verify_resp.text or "")[:180]))
            if args.dump_login_body:
                print("      verify body: %s" % (verify_resp.text or "")[:800])
            if login_resp is None:
                print("      免密中止: %s" % verr)
                if args.mode == "spes":
                    sys.exit("SPES 免密失败: %s" % verr)
                continue
            ingest_set_cookies(session, login_resp)
            try:
                login_json = login_resp.json()
            except Exception:
                login_json = None
            print("      login  %s" % summarize_login_json(login_json))
            if args.dump_login_body:
                print("      login  body: %s" % (login_resp.text or "")[:800])
            method_used = "http_api_spes"
            if has_sso_cookies(session):
                break
            print("      免密未拿到 SSO Cookie,继续..." if args.mode == "auto" else "")
        else:
            if not account or not password:
                sys.exit("密码模式需要账号和密码")
            print("[3/5] POST hwidcenter/login (account=%s) ..." % account)
            login_resp = do_password_login(session, account, password, target_url, spes_cred, fingerprint)
            set_names = ingest_set_cookies(session, login_resp)
            print("      Set-Cookie 写入: %s" % (set_names or "(无)"))
            try:
                login_json = login_resp.json()
            except Exception:
                login_json = None
            print("      %s" % summarize_login_json(login_json))
            if args.dump_login_body:
                print(
                    "      body: %s"
                    % (json.dumps(login_json, ensure_ascii=False)[:1200] if login_json else (login_resp.text or "")[:800])
                )
            method_used = "http_api_password"
            if has_sso_cookies(session):
                break

            # 双因子:可选弹浏览器只完成 MFA
            info = (login_json or {}).get("accountInfo") or {}
            if args.mfa_browser and info.get("bifactor") and info.get("mfaKey"):
                print("[3b/5] bifactor=true, 转浏览器完成双因子 ...")
                header, cookies = harvest_cookies_via_mfa_browser(
                    info["mfaKey"], info.get("maskUid") or account, timeout=args.mfa_timeout
                )
                method_used = "http_api_password+mfa_browser"
                probe_status: Optional[int] = None
                if args.probe:
                    # 用写出的 header 探测
                    pr = requests.get(
                        AUTH_PROBE,
                        headers={"Cookie": header, "User-Agent": UA},
                        verify=False,
                        timeout=30,
                    )
                    probe_status = pr.status_code
                    print("      probe status=%s body=%s" % (probe_status, (pr.text or "")[:160].replace("\n", " ")))
                save_cookies(args.out, target_url, header, cookies, probe_status, method_used)
                print("已写入 %s" % os.path.abspath(args.out))
                print("Cookie 数=%d: %s" % (len(cookies), ", ".join(c["name"] for c in cookies)))
                return

    if login_resp is None and not has_sso_cookies(session):
        sys.exit("没有可用的登录结果")

    if login_resp is not None and login_resp.status_code != 200 and not has_sso_cookies(session):
        sys.exit("登录失败 HTTP %s: %s" % (login_resp.status_code, (login_resp.text or "")[:400]))

    header, cookies = cookie_header_from_session(session)
    sso_names = {c["name"] for c in cookies}
    if not has_sso_cookies(session):
        hint = bifactor_hint(login_json)
        if not hint and isinstance(login_json, dict):
            st = login_json.get("statusText")
            if st and st not in ("SUCCESS", 0, "0"):
                hint = "\n登录 statusText=%s。若为 spesMsgError: 当前 enablePwFree 不含 w3login,免密不可用。" % st
            elif not spes_ok:
                hint = "\n" + spes_reason
        sys.exit(
            "登录响应未种下 SSO Cookie(缺少 hwsso_login/x_sso_token)。\n"
            "当前 Cookie: %s\n摘要: %s\n响应: %s%s"
            % (
                sorted(sso_names),
                summarize_login_json(login_json),
                ((login_resp.text if login_resp is not None else "") or "")[:400],
                hint,
            )
        )

    print("      ok  收到 %d 个 Cookie: %s" % (len(cookies), ", ".join(sorted(sso_names))))

    probe_status = None
    if args.probe:
        print("[4/5] GET base-info 探测 ...")
        probe_status, snippet = probe_auth(session)
        print("      status=%s  body=%s" % (probe_status, snippet))
        if probe_status != 200:
            print("警告: Cookie 可能尚未对 ilearning 生效(status=%s)" % probe_status)
    else:
        print("[4/5] 跳过探测(加 --probe 可校验)")

    print("[5/5] 写文件 ...")
    save_cookies(args.out, target_url, header, cookies, probe_status, method_used)
    print("已写入 %s" % os.path.abspath(args.out))
    shown = header if len(header) <= 360 else header[:360] + " ..."
    print("Cookie 请求头:\n  %s" % shown)


if __name__ == "__main__":
    main()
