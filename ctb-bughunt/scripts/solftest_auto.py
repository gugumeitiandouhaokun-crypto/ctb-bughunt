# -*- coding: utf-8 -*-
"""
iLearning 软件测试考试(SoftTest)自动化客户端
============================================

把浏览器里手动"保存代码 / 执行 / 提交 / 看结果"的整个流程脚本化。

【模式 A · 软件测试实操 softTestingAnswer】
接口契约来自 network_20260909_203052.jsonl:
  题目信息     GET  /sxz/api/iexercise/v1/solftest/getSubjectInfo/{subjectId}
  拉取代码     GET  /sxz/api/iexercise/v1/solftest/loadAnswerFile/{subjectId}/{语言}
  保存代码     POST /sxz/api/iexercise/v1/solftest/saveFile
  执行/提交    POST /sxz/api/iexercise/v1/solftest/submitAnswer   type=2/1
  判题结果     GET  /sxz/api/iexercise/v1/solftest/getJudgingResult/{subjectId}/{judgeId}/{type}

【模式 B · 竞赛门户 contestContent】(network_20260929_164127)
  科目列表     GET  /edx/lmsManagerAuth/api/exam/api/contestProgram/getContestSubjectList/{contestId}
  确认承诺     POST .../contestProgram/isComfirmLetter
  进入答题     POST .../contestProgram/answerContestSubject → contestResultId + 题目/附件
  拉取代码     GET  .../subject/v1/loadAnswerFile/{contestResultId}/{subjectId}/{lang}
                 (常返回 "OK";可编辑文件取自 subjectTree.answerFile / getUserAnswer)
  执行/提交    POST .../contestProgram/submitContestSubject  type=2/1
  调试结果     GET  .../subject/v1/getJudgeResult/{subjectId}/{judgeId}
  评分结果     GET  .../contestProgram/getContestSubjectJudgeResult?subjectId=&contestResultId=

依赖:pip install requests
Cookie:由同目录 get_ilearning_cookies.py 生成的 cookies.json 提供;
       请求遇 401 时会自动重新运行 get_ilearning_cookies.py 刷新 Cookie。

用法:
  python solftest_auto.py pull   [目录]
  python solftest_auto.py save   [目录]
  python solftest_auto.py run    [目录]
  python solftest_auto.py submit [目录]
  python solftest_auto.py info
  python solftest_auto.py watch <judgeId> [type]

选项:
  --subject <id|URL>  实操 subjectId / softTestingAnswer URL /
                      竞赛 contestContent?contestId=...[&subjectId=...] /
                      或 contest:{contestId}[:{subjectId}]
  --cookies <file>   Cookie 文件(默认 cookies.json)
  --timeout <秒>     判题等待超时,默认 600
  --framework <lang> pull 时选框架包: python(默认) / java / c

典型工作流:
  python solftest_auto.py pull --subject "https://ilearning.huawei.com/edx/exam-portal/contestContent?contestId=..."
  # ...在 answer/ 里改代码...
  python solftest_auto.py run --subject contest:{contestId}:{subjectId}
  python solftest_auto.py submit --subject contest:{contestId}:{subjectId}
"""

import argparse
import json
import os
import re
import subprocess
import sys
import time
import zipfile
from datetime import datetime
from html import unescape
from urllib.parse import parse_qs, urlparse

try:
    import requests
    import urllib3
except ImportError:
    sys.exit("缺少 requests,请先执行: pip install requests")

# ---------------- 配置 ----------------
BASE = "https://ilearning.huawei.com"
PROXY_BASE = "https://proxy.ilearning.huawei.com"
SUBJECT_ID = "1590273249147121665"   # 本次抓包对应的考试题
LANG = 5                             # 5 = Python(与抓包一致)
SCORE = 100                          # 与浏览器请求体一致
COOKIE_FILE = "cookies.json"
ANSWER_DIR = "answer"
JUDGE_POLL_SEC = 5                   # 判题轮询间隔(判题接口本身就要跑 2 秒左右,别太快)
JUDGE_TIMEOUT_SEC = 600

UA = ("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
      "(KHTML, like Gecko) Chrome/152.0.0.0 Safari/537.36")


def load_cookie_header(path):
    if not os.path.exists(path):
        sys.exit("找不到 %s。请先运行: python get_ilearning_cookies.py" % path)
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    header = data.get("cookie_header")
    if not header:
        sys.exit("%s 里没有 cookie_header 字段,请用 get_ilearning_cookies.py 重新生成" % path)
    return header


def normalize_subject(arg):
    """规范化题目引用。

    接受:
      - 纯 subjectId(软件测试实操)
      - softTestingAnswer?subjectId=... URL
      - 竞赛页 contestContent?contestId=...[&subjectId=...]
      - 已规范化的 contest:{contestId} 或 contest:{contestId}:{subjectId}
    返回:
      - 实操: subjectId 字符串
      - 竞赛: contest:{contestId} 或 contest:{contestId}:{subjectId}
    """
    arg = (arg or "").strip()
    if not arg:
        return SUBJECT_ID
    if arg.startswith("contest:"):
        return arg
    # 竞赛门户 URL(优先于 subjectId,避免误判)
    if "contestId=" in arg or "contestContent" in arg:
        qs = parse_qs(urlparse(arg).query)
        cid = (qs.get("contestId") or [""])[0].strip()
        if not cid:
            sys.exit("没能从 URL 里解析出 contestId:%s" % arg)
        sid = (qs.get("subjectId") or [""])[0].strip()
        if sid:
            print("从 URL 提取到竞赛: contestId=%s subjectId=%s" % (cid, sid))
            return "contest:%s:%s" % (cid, sid)
        print("从 URL 提取到竞赛: contestId=%s(未指定 subjectId,将取科目列表第一题)" % cid)
        return "contest:%s" % cid
    if "subjectId=" in arg:
        sid = (parse_qs(urlparse(arg).query).get("subjectId") or [""])[0]
        if not sid:
            sys.exit("没能从 URL 里解析出 subjectId:%s" % arg)
        print("从 URL 提取到题目 ID:%s" % sid)
        return sid
    return arg


def parse_subject_ref(ref):
    """解析 normalize_subject 的返回值 → (mode, contest_id|None, subject_id|None)。

    mode: \"soft\" | \"contest\"
    """
    ref = (ref or "").strip()
    if ref.startswith("contest:"):
        parts = ref.split(":")
        if len(parts) < 2 or not parts[1]:
            sys.exit("竞赛引用格式错误,期望 contest:{contestId}[:{subjectId}],得到: %s" % ref)
        cid = parts[1]
        sid = parts[2] if len(parts) >= 3 and parts[2] else None
        return "contest", cid, sid
    return "soft", None, ref or SUBJECT_ID


def make_client(cookie_file, subject_ref, verify=False):
    """按 subject 引用创建 SoftTest 或 ContestSoftTest。"""
    mode, contest_id, subject_id = parse_subject_ref(subject_ref)
    if mode == "contest":
        return ContestSoftTest(cookie_file, contest_id, subject_id=subject_id, verify=verify)
    return SoftTest(cookie_file, subject_id, verify=verify)


# ---------------- 题目信息 / 框架包 ----------------
# 框架包地址来自 getSubjectInfo → data.subjectAttachmentVOList[].fileUrl
# (按 fileName 匹配 c/java/python_framework.zip;题目树等其它字段仍用深搜兜底)

def _deep_strings(obj):
    """递归取出 JSON 里所有字符串值"""
    out = []
    if isinstance(obj, dict):
        for v in obj.values():
            out += _deep_strings(v)
    elif isinstance(obj, list):
        for v in obj:
            out += _deep_strings(v)
    elif isinstance(obj, str):
        out.append(obj)
    return out


# 框架包附件名 → CLI --framework 取值
FRAMEWORK_FILES = {
    "python": "python_framework.zip",
    "java": "java_framework.zip",
    "c": "c_framework.zip",
}


def find_framework_zip(data, framework="python"):
    """从 getSubjectInfo.data.subjectAttachmentVOList 取对应语言框架包的 fileUrl。

    列表元素通常为 c/java/python 三份 zip,默认取 python_framework.zip。
    """
    want = FRAMEWORK_FILES.get((framework or "python").lower())
    if not want:
        return None
    items = data.get("subjectAttachmentVOList") or []
    if not isinstance(items, list):
        return None
    for item in items:
        if not isinstance(item, dict):
            continue
        if (item.get("fileName") or "").strip() != want:
            continue
        url = (item.get("fileUrl") or "").strip()
        if not url:
            return None
        if not url.startswith("http"):
            url = PROXY_BASE + (url if url.startswith("/") else "/" + url)
        return url
    return None


def find_tree_url(data):
    for s in _deep_strings(data):
        if "subjectTree" in s and s.strip().endswith(".json"):
            s = s.strip()
            return s if s.startswith("http") else PROXY_BASE + (s if s.startswith("/") else "/" + s)
    return None


def _html_to_text(s):
    """富文本转纯文本;内容不含 HTML 标签时原样返回(避免误伤代码里的 <...> 片段)"""
    if not re.search(r"<(p|br|div|span|img|h[1-6]|ul|ol|li|table|tr|td|strong|em)\b", s, re.I):
        return s.strip()
    s = re.sub(r"<br\s*/?>", "\n", s, flags=re.I)
    s = re.sub(r"</(p|div|li|h[1-6]|tr|table)>", "\n", s, flags=re.I)
    s = re.sub(r"<[^>]+>", "", s)
    s = unescape(s)
    return re.sub(r"\n{3,}", "\n\n", s).strip()


def build_description_md(data):
    """题目描述.md 的内容:纯题目描述(getSubjectInfo → data.subjectVO.titlieContent)
    注意:接口字段名本身就是拼错的 titlieContent(titlie),不是 titleContent"""
    vo = data.get("subjectVO") if isinstance(data.get("subjectVO"), dict) else {}
    desc = vo.get("titlieContent")
    if not isinstance(desc, str) or not desc.strip():
        desc = "(题目信息里没有找到描述字段,详见 题目补充信息.md 的原始数据)"
    return _html_to_text(desc)


def build_extra_info_md(client, data, zip_url):
    """题目补充信息.md:题目结构 / 框架包地址 / 原始数据(与纯描述分开存放)"""
    vo = data.get("subjectVO") if isinstance(data.get("subjectVO"), dict) else {}
    title = ""
    for k in ("subjectTitle", "subjectName", "name", "title"):
        v = vo.get(k) or data.get(k)
        if v:
            title = str(v)
            break
    lines = ["# 题目补充信息", "",
             "> %s  |  subjectId: %s  |  拉取时间: %s" % (
                 title, client.subject_id,
                 datetime.now().strftime("%Y-%m-%d %H:%M:%S")),
             ""]

    # 题目树(结构):优先用数据里给出的地址,否则按已知规律拼一个,失败就跳过
    tree_url = find_tree_url(data) or (
        PROXY_BASE + "/edxs3proxy/exam-public/softTest/subjectTree/%s_25_tree.json" % client.subject_id)
    try:
        r = client.s.get(tree_url, timeout=60)
        r.raise_for_status()
        lines += ["## 题目结构(subjectTree)", "", "```json", r.text.strip()[:20000], "```", ""]
    except Exception:
        pass

    if zip_url:
        lines += ["## 框架包", "", "- 下载地址:`%s`(pull 时已自动下载解压)" % zip_url, ""]

    lines += ["## 原始数据(getSubjectInfo)", "", "```json",
              json.dumps(data, ensure_ascii=False, indent=2)[:20000], "```", ""]
    return "\n".join(lines)


def _zip_name_fix(n):
    """Windows 下打包的中文 zip 文件名常按 GBK 存,Python 默认按 cp437 解,尝试纠正"""
    try:
        return n.encode("cp437").decode("gbk")
    except (UnicodeEncodeError, UnicodeDecodeError):
        return n


def extract_zip(zip_path, dest_dir):
    """解压到 dest_dir;若所有条目都在同一个顶层目录里,则剥掉这层再解压。
    返回解出的相对路径列表(用于展示)"""
    written = []
    with zipfile.ZipFile(zip_path) as zf:
        entries = [n for n in zf.namelist() if not n.endswith("/")]
        tops = {n.split("/")[0] for n in entries if "/" in n}
        strip = bool(tops) and len(tops) == 1 and all("/" in n for n in entries)
        for n in entries:
            rel = _zip_name_fix(n)
            if strip:
                rel = rel.split("/", 1)[1]
            parts = rel.replace("\\", "/").split("/")
            if not rel or ".." in parts:
                continue  # 防路径穿越
            target = os.path.join(dest_dir, *parts)
            os.makedirs(os.path.dirname(target) or dest_dir, exist_ok=True)
            with open(target, "wb") as f:
                f.write(zf.read(n))
            written.append("/".join(parts))
    return written


def download_and_extract_zip(client, url, dest_dir):
    """下载框架包并解压到 dest_dir(覆盖同名文件)"""
    print("下载框架包: %s" % url)
    r = client.s.get(url, timeout=300)
    r.raise_for_status()
    if not r.content:
        sys.exit("框架包下载内容为空:%s" % url)
    os.makedirs(dest_dir, exist_ok=True)
    zip_name = os.path.basename(url.split("?")[0]) or "framework.zip"
    zip_path = os.path.join(dest_dir, zip_name)
    with open(zip_path, "wb") as f:
        f.write(r.content)
    print("  已保存 %s(%.1f KB),解压 ..." % (zip_name, len(r.content) / 1024.0))
    written = extract_zip(zip_path, dest_dir)
    print("  解压出 %d 个文件/目录条目(唯一顶层目录已剥平):" % len(written))
    for w in written[:30]:
        print("    %s" % w)
    if len(written) > 30:
        print("    ... 其余 %d 个略" % (len(written) - 30))


# ---------------- 接口客户端 ----------------

class SoftTest:
    def __init__(self, cookie_file, subject_id, verify=False):
        self.cookie_file = cookie_file     # 401 时从这个文件刷新
        self.subject_id = subject_id
        self.s = requests.Session()
        self.s.verify = verify
        if not verify:
            urllib3.disable_warnings()
        self.s.headers.update({
            "Cookie": load_cookie_header(cookie_file),
            "Accept": "application/json, text/plain, */*",
            "sxz-lang": "zh_CN",
            "User-Agent": UA,
            "Referer": ("%s/iexam/100000/ilearningExercise/softTestingAnswer?subjectId=%s"
                        % (BASE, subject_id)),
        })

    def _json(self, r, what):
        if r.status_code == 401:
            sys.exit("Cookie 已失效(401),且自动刷新后仍未通过。"
                     "请手动运行 get_ilearning_cookies.py 排查登录问题。")
        r.raise_for_status()
        try:
            j = r.json()
        except ValueError:
            sys.exit("%s 返回的不是 JSON:%s" % (what, r.text[:200]))
        # 业务码:常规接口 200,base-info 风格的接口用 20000,都算成功
        if j.get("code") not in (200, 20000):
            sys.exit("%s 失败:%s" % (what, json.dumps(j, ensure_ascii=False)[:300]))
        return j

    def _api(self, method, path, what, body=None):
        """发请求;遇 401 先自动刷新 Cookie,再重试一次"""
        r = self.s.request(method, BASE + path, json=body, timeout=90)
        if r.status_code == 401:
            self._refresh_cookies()
            r = self.s.request(method, BASE + path, json=body, timeout=90)
        return self._json(r, what)

    def _refresh_cookies(self):
        """重新运行 get_ilearning_cookies.py 换取新 Cookie 并更新到当前会话"""
        script = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                              "get_ilearning_cookies.py")
        if not os.path.exists(script):
            sys.exit("Cookie 失效(401),且找不到 %s,无法自动刷新" % script)
        print("Cookie 失效(401),自动刷新(会弹出浏览器完成 SPES 免密登录,通常几秒)...")
        proc = subprocess.run(
            [sys.executable, script, "--out", os.path.abspath(self.cookie_file)])
        if proc.returncode != 0:
            sys.exit("自动刷新 Cookie 失败(退出码 %d)。"
                     "请手动运行 get_ilearning_cookies.py 排查" % proc.returncode)
        self.s.headers["Cookie"] = load_cookie_header(self.cookie_file)
        print("Cookie 已刷新,重试请求 ...")

    def _api_soft(self, method, path, what, body=None):
        """软失败版 _api:出错不退出,返回 None(供 pull 这类尽力而为的流程用)"""
        try:
            return self._api(method, path, what, body)
        except SystemExit as ex:
            print("  (跳过:%s)" % ex)
        except Exception as ex:
            print("  (跳过 %s:%s)" % (what, ex))
        return None

    def subject_info(self, required=True):
        """getSubjectInfo 的 data;required=False 时失败返回 {}"""
        fn = self._api if required else self._api_soft
        j = fn("GET", "/sxz/api/iexercise/v1/solftest/getSubjectInfo/%s" % self.subject_id, "题目信息")
        return (j or {}).get("data") or {}

    def status(self):
        return self._api("GET", "/sxz/api/iexercise/v1/solftest/%s" % self.subject_id, "题目状态")

    def submit_page(self):
        return self._api("GET", "/sxz/api/iexercise/v1/solftest/getSubjectSubmitPage"
                         "?currentPage=1&pageSize=999&subjectId=%s" % self.subject_id, "提交记录")

    def load_files(self, required=True):
        """返回服务端已保存的 {文件名: 内容};required=False 时失败返回 {}"""
        fn = self._api if required else self._api_soft
        j = fn("GET", "/sxz/api/iexercise/v1/solftest/loadAnswerFile/%s/%d"
               % (self.subject_id, LANG), "拉取代码")
        url = (j or {}).get("data")
        if not url or not str(url).startswith("http"):
            if required:
                sys.exit("loadAnswerFile 未返回下载地址:%s" % json.dumps(j, ensure_ascii=False)[:300])
            return {}
        r = self.s.get(url, timeout=90)
        # 注意:预签名直链返回的是裸 JSON 文件 {"文件名": "内容"},没有 code 包装,不走 _json
        r.raise_for_status()
        try:
            code = r.json()
        except ValueError:
            sys.exit("下载代码返回的不是 JSON:%s" % r.text[:200])
        if not isinstance(code, dict) or not code:
            sys.exit("下载的代码文件内容异常:%s" % r.text[:200])
        return code

    def save_files(self, code):
        return self._api("POST", "/sxz/api/iexercise/v1/solftest/saveFile", "保存代码",
                         {"subjectId": self.subject_id, "languageParam": LANG, "code": code})

    def submit(self, code, type_):
        """type_: 2=执行(调试) 1=提交(评分);返回 judgeId"""
        body = {"subjectId": self.subject_id, "languageParam": LANG, "code": code,
                "score": SCORE, "type": type_}
        return self._api("POST", "/sxz/api/iexercise/v1/solftest/submitAnswer",
                         "执行" if type_ == 2 else "提交", body)

    def judging(self, judge_id, type_):
        return self._api("GET", "/sxz/api/iexercise/v1/solftest/getJudgingResult/%s/%s/%d"
                         % (self.subject_id, judge_id, type_), "判题结果")

    def wait_judging(self, judge_id, type_, timeout):
        """轮询直到终态,返回 getJudgingResult 的 data"""
        start = time.time()
        while time.time() - start < timeout:
            d = (self.judging(judge_id, type_).get("data") or {})
            if d.get("judgeResult") and d["judgeResult"] != "JUDGING":
                return d
            print("  ... 判题中,已等 %.0f 秒" % (time.time() - start))
            time.sleep(JUDGE_POLL_SEC)
        sys.exit("等待判题超时(%d 秒)。可稍后执行: python solftest_auto.py watch %s %d"
                 % (timeout, judge_id, type_))


# ---------------- 竞赛平台客户端(contestProgram) ----------------
# 接口契约来自 network_20260929_164127 抓包:
#   科目列表   GET  .../contestProgram/getContestSubjectList/{contestId}
#   确认承诺   POST .../contestProgram/isComfirmLetter
#   进入答题   POST .../contestProgram/answerContestSubject
#              → contestResultId + subjectList(含描述/附件/框架包)
#   拉取代码   GET  .../subject/v1/loadAnswerFile/{contestResultId}/{subjectId}/{lang}
#              → 竞赛侧常返回 "OK";可编辑文件改从 subjectTree(answerFile) /
#                getUserAnswer 取
#   执行/提交  POST .../contestProgram/submitContestSubject  type=2/1
#   调试结果   GET  .../subject/v1/getJudgeResult/{subjectId}/{judgeId}
#   评分结果   GET  .../contestProgram/getContestSubjectJudgeResult
#              ?subjectId=&contestResultId=

CONTEST_API = "/edx/lmsManagerAuth/api/exam/api"
CONTEST_ENTRY_MODE = 2
CONTEST_SUBJECT_TYPE = 7
CONTEST_DUTY_TYPE = 2


def _collect_answer_files_from_tree(nodes, out=None):
    """递归收集 subjectTree 里 answerFile=true 的 {fileName: content}。"""
    if out is None:
        out = {}
    if not isinstance(nodes, list):
        return out
    for node in nodes:
        if not isinstance(node, dict):
            continue
        if node.get("answerFile") and node.get("fileName"):
            content = node.get("content")
            if isinstance(content, str):
                out[node["fileName"]] = content
        children = node.get("child")
        if children:
            _collect_answer_files_from_tree(children, out)
    return out


class ContestSoftTest(SoftTest):
    """竞赛门户 SoftTest 兼容客户端(同 pull/save/run/submit 方法签名)。"""

    def __init__(self, cookie_file, contest_id, subject_id=None, verify=False):
        SoftTest.__init__(self, cookie_file, subject_id or "0", verify=verify)
        self.contest_id = str(contest_id)
        self.preferred_subject_id = str(subject_id) if subject_id else None
        self.subject_id = self.preferred_subject_id or ""
        self.contest_result_id = None
        self.paper_id = None
        self.contest_item_id = None
        self._subject_payload = None
        self._ensured = False
        self.s.headers["Referer"] = (
            "%s/edx/exam-portal/contestContent?contestId=%s" % (BASE, self.contest_id))

    def ensure_session(self):
        """科目列表 → 确认承诺书 → 进入答题,拿到 contestResultId / 题目详情。"""
        if self._ensured:
            return
        j = self._api("GET",
                      "%s/contestProgram/getContestSubjectList/%s"
                      % (CONTEST_API, self.contest_id), "竞赛科目列表")
        subjects = j.get("data") or []
        if not subjects:
            sys.exit("竞赛 %s 没有可答科目" % self.contest_id)
        chosen = None
        if self.preferred_subject_id:
            for item in subjects:
                if str(item.get("subjectId")) == self.preferred_subject_id:
                    chosen = item
                    break
            if chosen is None:
                sys.exit("竞赛 %s 中找不到 subjectId=%s。可选: %s"
                         % (self.contest_id, self.preferred_subject_id,
                            ", ".join("%s(%s)" % (s.get("subjectName"), s.get("subjectId"))
                                      for s in subjects)))
        else:
            chosen = subjects[0]
            if len(subjects) > 1:
                print("未指定 subjectId,默认取第一题。本场共 %d 题:" % len(subjects))
                for s in subjects:
                    mark = " ←" if str(s.get("subjectId")) == str(chosen.get("subjectId")) else ""
                    print("  - %s  subjectId=%s%s"
                          % (s.get("subjectName"), s.get("subjectId"), mark))
                print("如需指定,URL 加 &subjectId=... 或使用 contest:{contestId}:{subjectId}")

        self.subject_id = str(chosen.get("subjectId"))
        self.paper_id = str(chosen.get("paperId") or "")
        self.contest_item_id = str(chosen.get("contestItemId") or "")
        print("进入竞赛答题: contestId=%s subjectId=%s (%s)"
              % (self.contest_id, self.subject_id, chosen.get("subjectName") or ""))

        confirm_body = {
            "contestItemId": self.contest_item_id,
            "dutyType": CONTEST_DUTY_TYPE,
            "entryMode": CONTEST_ENTRY_MODE,
            "paperId": self.paper_id,
            "contestId": self.contest_id,
            "subjectId": 0,
            "subjectType": CONTEST_SUBJECT_TYPE,
        }
        cj = self._api("POST", "%s/contestProgram/isComfirmLetter" % CONTEST_API,
                       "确认承诺书", confirm_body)
        self.contest_result_id = str((cj.get("data") or {}).get("contestResultId")
                                     or self.contest_result_id or "")

        answer_body = {
            "contestId": self.contest_id,
            "contestItemId": self.contest_item_id,
            "paperId": self.paper_id,
            "subjectId": self.subject_id,
            "entryMode": CONTEST_ENTRY_MODE,
            "subjectType": CONTEST_SUBJECT_TYPE,
            "dutyType": CONTEST_DUTY_TYPE,
        }
        aj = self._api("POST", "%s/contestProgram/answerContestSubject" % CONTEST_API,
                       "进入竞赛答题", answer_body)
        data = aj.get("data") or {}
        self.contest_result_id = str(data.get("contestResultId") or self.contest_result_id)
        if data.get("paperId"):
            self.paper_id = str(data.get("paperId"))
        if not self.contest_result_id:
            sys.exit("answerContestSubject 未返回 contestResultId")

        sl = data.get("subjectList") or []
        item = sl[0] if sl else {}
        # 对齐 SoftTest.subject_info 结构,复用 find_framework_zip / build_description_md
        self._subject_payload = {
            "subjectVO": item.get("subjectVO") or {},
            "subjectAttachmentVOList": item.get("subjectAttachmentVOList") or [],
            "programSubjectExtraInfoVO": item.get("programSubjectExtraInfoVO") or {},
            "contestResultId": self.contest_result_id,
            "contestId": self.contest_id,
            "paperId": self.paper_id,
        }
        self._ensured = True

    def subject_info(self, required=True):
        try:
            self.ensure_session()
        except SystemExit:
            if required:
                raise
            print("  (跳过:竞赛会话建立失败)")
            return {}
        except Exception as ex:
            if required:
                raise
            print("  (跳过 竞赛会话:%s)" % ex)
            return {}
        return self._subject_payload or {}

    def status(self):
        self.ensure_session()
        j = self._api("GET",
                      "%s/contestProgram/getContestSubjectList/%s"
                      % (CONTEST_API, self.contest_id), "竞赛科目列表")
        for item in (j.get("data") or []):
            if str(item.get("subjectId")) == self.subject_id:
                return {"data": item}
        return {"data": {}}

    def submit_page(self):
        self.ensure_session()
        return self._api(
            "GET",
            ("%s/contestProgram/getSoftSubmitList?subjectId=%s&contestResultId=%s"
             "&currentPage=1&pageSize=999")
            % (CONTEST_API, self.subject_id, self.contest_result_id),
            "竞赛提交记录")

    def _tree_answer_files(self):
        tree_url = (PROXY_BASE
                    + "/edxs3proxy/exam-public/softTest/subjectTree/%s_25_tree.json"
                    % self.subject_id)
        try:
            r = self.s.get(tree_url, timeout=60)
            r.raise_for_status()
            nodes = r.json()
        except Exception as ex:
            print("  (subjectTree 拉取失败:%s)" % ex)
            return {}
        return _collect_answer_files_from_tree(nodes)

    def _latest_user_answer(self):
        """从提交记录取最近一次 getUserAnswer(若有)。"""
        try:
            page = self.submit_page().get("data") or {}
            records = page.get("records") or []
            if not records:
                return {}
            answer_id = records[0].get("id")
            if not answer_id:
                return {}
            j = self._api("GET",
                          "%s/contestProgram/getUserAnswer/%s" % (CONTEST_API, answer_id),
                          "用户已保存答案")
            data = j.get("data")
            return data if isinstance(data, dict) else {}
        except SystemExit as ex:
            print("  (跳过 getUserAnswer:%s)" % ex)
        except Exception as ex:
            print("  (跳过 getUserAnswer:%s)" % ex)
        return {}

    def load_files(self, required=True):
        """竞赛可编辑文件:优先上次提交内容,否则 subjectTree 模板。"""
        try:
            self.ensure_session()
        except SystemExit:
            if required:
                raise
            return {}
        fn = self._api if required else self._api_soft
        j = fn("GET",
               "%s/subject/v1/loadAnswerFile/%s/%s/%d"
               % (CONTEST_API, self.contest_result_id, self.subject_id, LANG),
               "拉取代码")
        data = (j or {}).get("data")
        code = {}
        if isinstance(data, str) and data.startswith("http"):
            r = self.s.get(data, timeout=90)
            r.raise_for_status()
            try:
                code = r.json()
            except ValueError:
                if required:
                    sys.exit("下载代码返回的不是 JSON:%s" % r.text[:200])
                return {}
        elif isinstance(data, dict):
            code = data
        # "OK" 或其它:用 tree + 历史答案
        if not code:
            code = self._tree_answer_files()
            saved = self._latest_user_answer()
            if saved:
                code.update(saved)
        if not code and required:
            sys.exit("未能获取竞赛可编辑文件(loadAnswerFile/tree/getUserAnswer 皆空)")
        return code if isinstance(code, dict) else {}

    def save_files(self, code):
        """竞赛平台无独立 saveFile;保存随 submit 一并上传,此处为兼容 no-op。"""
        self.ensure_session()
        print("  (竞赛模式无独立保存接口,代码将在 run/submit 时上传)")
        return {"code": 200, "data": None}

    def submit(self, code, type_):
        """type_: 2=执行(调试) 1=提交(评分);返回 {\"data\": judgeId}(评分时可能为 None)。"""
        self.ensure_session()
        body = {
            "subjectId": self.subject_id,
            "contestResultId": self.contest_result_id,
            "type": type_,
            "paperId": self.paper_id,
            "languageParam": LANG,
            "code": code,
        }
        j = self._api("POST", "%s/contestProgram/submitContestSubject" % CONTEST_API,
                      "执行" if type_ == 2 else "提交", body)
        data = j.get("data") or {}
        judge_id = data.get("judgeId")
        # 与 SoftTest.submit 对齐:外层 data 直接是 judgeId,供 cmd_run / SoftTestBridge 使用
        return {"code": 200, "data": judge_id, "contestData": data}

    def judging(self, judge_id, type_):
        self.ensure_session()
        if type_ == 2:
            if not judge_id:
                sys.exit("调试判题缺少 judgeId")
            return self._api(
                "GET",
                "%s/subject/v1/getJudgeResult/%s/%s"
                % (CONTEST_API, self.subject_id, judge_id),
                "判题结果")
        return self._api(
            "GET",
            ("%s/contestProgram/getContestSubjectJudgeResult"
             "?subjectId=%s&contestResultId=%s")
            % (CONTEST_API, self.subject_id, self.contest_result_id),
            "竞赛评分结果")

    def wait_judging(self, judge_id, type_, timeout):
        start = time.time()
        while time.time() - start < timeout:
            d = (self.judging(judge_id, type_).get("data") or {})
            result = d.get("judgeResult")
            if result and result != "JUDGING":
                return d
            print("  ... 判题中,已等 %.0f 秒" % (time.time() - start))
            time.sleep(JUDGE_POLL_SEC)
        tip = judge_id or self.contest_result_id
        sys.exit("等待判题超时(%d 秒)。可稍后执行: python solftest_auto.py watch %s %d"
                 % (timeout, tip, type_))


# ---------------- 结果展示 ----------------

def build_judge_summary(d):
    """把 getJudgingResult / 竞赛判题 data 整理成与控制台报告对应的结构化 dict。"""
    cost = d.get("judgeCost")
    try:
        cost_ms = float(cost) if cost is not None else None
    except (TypeError, ValueError):
        cost_ms = None
    summary = {
        "judgeResult": d.get("judgeResult"),
        "judgeCostMs": cost_ms,
        "judgeCostSec": (round(cost_ms / 1000.0, 1) if cost_ms is not None else None),
    }
    raw = d.get("judgeResultInfo")
    if not raw:
        return summary
    try:
        info = json.loads(raw)
    except (TypeError, ValueError):
        summary["judgeResultInfoRaw"] = str(raw)[:2000]
        return summary
    # 实操接口常包一层 data;竞赛 getContestSubjectJudgeResult 直接是扁平结构
    data = info.get("data") if isinstance(info.get("data"), dict) else None
    if data is None:
        data = info if isinstance(info, dict) else {}
    summary["submitTime"] = data.get("submitTime")
    summary["finishTime"] = data.get("finishTime")
    summary["score"] = data.get("subjectResultScore")

    report = data.get("reportData") or {}
    bugs = report.get("bugInfoVos") or []
    if bugs:
        items = []
        for b in bugs:
            cases = b.get("triggerBugCaseNames") or []
            items.append({
                "bugId": b.get("bugId"),
                "detected": bool(b.get("isPass")),
                "cases": list(cases),
            })
        summary["bugDetect"] = {
            "passed": sum(1 for i in items if i["detected"]),
            "total": len(items),
            "items": items,
        }

    invs = report.get("investigationItems") or []
    if invs:
        summary["investigation"] = [{
            "point": i.get("investigationPoint"),
            "correct": i.get("correct"),
            "num": i.get("num"),
            "correctRate": i.get("correctRate"),
        } for i in invs]

    def _deduction(c):
        try:
            return float(c.get("singleDiscountScore") or 0) > 0
        except (TypeError, ValueError):
            return False

    deductions = [c for c in (report.get("codeQualityItems") or []) if _deduction(c)]
    if deductions:
        summary["codeQualityDeductions"] = [{
            "checkItem": c.get("checkItem"),
            "number": c.get("number"),
            "discountScoreRule": c.get("discountScoreRule"),
        } for c in deductions]
    return summary


def print_judge_report(d, out_dir=None, judge_id=None, type_=None):
    """打印判题报告;若给了 out_dir,同时写成 JSON 文件。"""
    summary = build_judge_summary(d)
    if judge_id is not None:
        summary["judgeId"] = judge_id
    if type_ is not None:
        summary["type"] = type_
        summary["typeName"] = "submit" if type_ == 1 else "run" if type_ == 2 else str(type_)

    print("判题状态: %s" % summary.get("judgeResult"))
    if summary.get("judgeCostSec") is not None:
        print("判题耗时: %.1f 秒" % summary["judgeCostSec"])
    if "judgeResultInfoRaw" in summary:
        print("judgeResultInfo(原文): %s" % summary["judgeResultInfoRaw"][:500])
    else:
        if summary.get("submitTime") is not None or summary.get("finishTime") is not None:
            print("提交时间: %s    完成时间: %s" % (
                summary.get("submitTime"), summary.get("finishTime")))
        if summary.get("score") is not None:
            print("得分: %s" % summary["score"])

        bug = summary.get("bugDetect") or {}
        items = bug.get("items") or []
        if items:
            print("\nbug 检出: %d / %d" % (bug.get("passed", 0), bug.get("total", 0)))
            for b in items:
                suffix = ("  ← %s" % ", ".join(str(c) for c in b["cases"])) if b.get("cases") else ""
                print("  bug %s: %s%s" % (
                    b.get("bugId"),
                    "√ 检出" if b.get("detected") else "× 未检出",
                    suffix))

        invs = summary.get("investigation") or []
        if invs:
            print("\n考察点:")
            for i in invs:
                print("  考察点%s: 正确 %s / 用例 %s(%s)" % (
                    i.get("point"), i.get("correct"), i.get("num"), i.get("correctRate")))

        deductions = summary.get("codeQualityDeductions") or []
        if deductions:
            print("\n代码质量扣分项:")
            for c in deductions:
                print("  %s: 数值 %s(规则:%s)" % (
                    c.get("checkItem"), c.get("number"),
                    (c.get("discountScoreRule") or "").replace("\n", "; ")))

    if out_dir:
        os.makedirs(out_dir, exist_ok=True)
        name = "judge_result_%s.json" % (judge_id or datetime.now().strftime("%Y%m%d_%H%M%S"))
        path = os.path.join(out_dir, name)
        with open(path, "w", encoding="utf-8", newline="") as f:
            json.dump(summary, f, ensure_ascii=False, indent=2)
        print("\n判题结果已写入 %s" % path)
    return summary


# ---------------- 本地代码目录 ----------------

def read_local_code(d, only_names=None):
    """读取要提交的代码文件,统一 LF(与浏览器提交格式一致,BOM 等原样保留)。
    only_names:服务端期望的文件名集合(来自 loadAnswerFile),只读这些文件,
                避免把 zip 框架里的 testtool.py、config.properties 等一并提交;
                为空时兜底读目录下所有 .py"""
    if not os.path.isdir(d):
        sys.exit("目录不存在:%s(第一次请先 pull 把题目拉下来)" % d)
    if only_names:
        wanted = sorted(only_names)
    else:
        wanted = [n for n in sorted(os.listdir(d))
                  if n.endswith(".py") and os.path.isfile(os.path.join(d, n))]
    code, missing = {}, []
    for name in wanted:
        p = os.path.join(d, name)
        if os.path.isfile(p):
            with open(p, "r", encoding="utf-8", newline="") as f:
                code[name] = f.read().replace("\r\n", "\n")
        else:
            missing.append(name)
    if missing:
        sys.exit("本地缺少服务端已有的文件:%s\n(先 pull 同步,再修改)" % ", ".join(missing))
    if not code:
        sys.exit("目录 %s 里没有可提交的 .py 文件" % d)
    return code


def write_local_code(d, code):
    os.makedirs(d, exist_ok=True)
    for name, content in code.items():
        with open(os.path.join(d, name), "w", encoding="utf-8", newline="") as f:
            f.write(content)
        print("  %s(%d 字符)" % (name, len(content)))


# ---------------- 命令 ----------------

def cmd_pull(client, args):
    os.makedirs(args.dir, exist_ok=True)
    # 1) 题目信息 → 题目描述.md(纯描述) + 题目补充信息.md(结构/框架包/原始数据)
    data = client.subject_info(required=False)
    zip_url = None
    if data:
        zip_url = find_framework_zip(data, getattr(args, "framework", "python"))
        desc_path = os.path.join(args.dir, "题目描述.md")
        with open(desc_path, "w", encoding="utf-8", newline="") as f:
            f.write(build_description_md(data))
        print("题目描述已写入 %s" % desc_path)
        extra_path = os.path.join(args.dir, "题目补充信息.md")
        with open(extra_path, "w", encoding="utf-8", newline="") as f:
            f.write(build_extra_info_md(client, data, zip_url))
        print("补充信息已写入 %s" % extra_path)
    else:
        print("(题目信息获取失败,跳过题目描述)")
    # 2) 服务端已保存的可编辑代码
    code = client.load_files(required=False)
    if code:
        print("服务端已保存 %d 个文件,写入 %s/(同名文件会被覆盖):" % (len(code), args.dir))
        write_local_code(args.dir, code)
    else:
        print("(服务端没有已保存的代码,跳过)")
    # 3) 框架包 zip 下载并解压
    if zip_url:
        try:
            download_and_extract_zip(client, zip_url, args.dir)
        except SystemExit:
            raise
        except Exception as ex:
            print("(框架包下载/解压失败:%s)" % ex)
    else:
        print("(未在题目信息里找到框架包地址,跳过;可从页面下载按钮手动获取后解压到本目录)")
    print("\n完成。在 %s/ 里改代码,然后 run(调试)/ submit(提交)。" % args.dir)


def cmd_save(client, args):
    code = read_local_code(args.dir, only_names=client.load_files(required=False))
    print("保存 %d 个文件到服务端 ..." % len(code))
    client.save_files(code)
    print("保存成功。")


def cmd_run(client, args, type_):
    code = read_local_code(args.dir, only_names=client.load_files(required=False))
    print("先保存 %d 个文件到服务端 ..." % len(code))
    client.save_files(code)
    print("%s ..." % ("执行(调试)" if type_ == 2 else "提交(评分)"))
    judge_id = client.submit(code, type_).get("data")
    print("已受理,judgeId = %s,等待判题 ..." % judge_id)
    d = client.wait_judging(judge_id, type_, args.timeout)
    print_judge_report(d, out_dir=args.dir, judge_id=judge_id, type_=type_)
    print("\n(之后可用 python solftest_auto.py watch %s %d 再次查看)" % (judge_id, type_))


def cmd_info(client, args):
    d = (client.status().get("data") or {})
    print("题目状态: 通过 %s 人 / 已提交 %s 人 / exerciseStatus=%s"
          % (d.get("passNum"), d.get("submitNum"), d.get("exerciseStatus")))
    j = client.submit_page().get("data")
    print("\n提交记录(原始返回,截断至 3000 字符):")
    print(json.dumps(j, ensure_ascii=False, indent=2)[:3000])


def cmd_watch(client, args):
    d = client.wait_judging(args.judge_id, args.type, args.timeout)
    out_dir = getattr(args, "dir", None) or ANSWER_DIR
    print_judge_report(d, out_dir=out_dir, judge_id=args.judge_id, type_=args.type)


def main():
    ap = argparse.ArgumentParser(description="iLearning 软件测试考试自动化客户端",
                                 formatter_class=argparse.RawDescriptionHelpFormatter,
                                 epilog=__doc__.split("典型工作流:")[-1])
    ap.add_argument("command", choices=["pull", "save", "run", "submit", "info", "watch"])
    ap.add_argument("dir_or_id", nargs="?", default=None,
                    help="pull/save/run/submit: 本地目录(默认 answer);watch: judgeId")
    ap.add_argument("watch_type", nargs="?", type=int, default=1,
                    help="watch 的判题类型: 1=提交(默认) 2=执行")
    ap.add_argument("--subject", default=SUBJECT_ID,
                    help="题目 ID / softTestingAnswer URL / 竞赛 contestContent?contestId=... URL;"
                         "竞赛也可写 contest:{contestId}[:{subjectId}];"
                         "默认 %s" % SUBJECT_ID)
    ap.add_argument("--cookies", default=COOKIE_FILE, help="Cookie 文件路径")
    ap.add_argument("--timeout", type=int, default=JUDGE_TIMEOUT_SEC, help="判题等待超时秒数")
    ap.add_argument("--verify", action="store_true", help="开启 TLS 证书校验")
    ap.add_argument("--ca", default=None, metavar="PEM",
                    help="CA 证书捆绑包路径(如 huawei-ca-bundle.pem),给出后启用正式证书校验")
    ap.add_argument("--framework", choices=sorted(FRAMEWORK_FILES), default="python",
                    help="pull 时下载的框架包语言(对应 subjectAttachmentVOList 里的 "
                         "c/java/python_framework.zip),默认 python")
    args = ap.parse_args()
    args.subject = normalize_subject(args.subject)

    if args.command == "watch":
        if not args.dir_or_id:
            ap.error("watch 需要 judgeId 参数")
        args.judge_id = args.dir_or_id
        args.type = args.watch_type
    else:
        args.dir = args.dir_or_id or ANSWER_DIR

    # TLS 校验优先级:--ca 证书包 > --verify > 默认不校验(内网证书链常不全)
    client = make_client(os.path.abspath(args.cookies), args.subject,
                         verify=args.ca or args.verify or False)

    {"pull": lambda: cmd_pull(client, args),
     "save": lambda: cmd_save(client, args),
     "run": lambda: cmd_run(client, args, 2),
     "submit": lambda: cmd_run(client, args, 1),
     "info": lambda: cmd_info(client, args),
     "watch": lambda: cmd_watch(client, args)}[args.command]()


if __name__ == "__main__":
    main()
