# ctb-bughunt（OpenCode skill）

iLearning 软件测试实操题的找 bug 用例开发 skill：拉题、反编译无 bug jar、探针、只写步骤录制真实返回生成断言、JaCoCo 覆盖率、本地验证、调试判题、复盘、正式提交。方法论与流程见 `ctb-bughunt/SKILL.md`。

## 打包

```
python pack.py
```

生成 `dist/ctb-bughunt.zip`，zip 根目录为 `ctb-bughunt/`。

## 安装

把 zip 解压到以下任一位置（解压后应得到 `.../skills/ctb-bughunt/SKILL.md`）：

- 全局：`~/.config/opencode/skills/`（Windows：`%USERPROFILE%\.config\opencode\skills\`）
- 单个项目：`<项目>/.opencode/skills/`

## 环境依赖

- Python 3.10+，`pip install requests selenium`
- Java 8+，`java` 在 PATH 中（本地运行被测 jar、反编译）
- iLearning 免密登录环境（SPES 代理 + Chrome/Edge），首次 `cookies` 会弹浏览器
- 反编译器 CFR 0.152、JaCoCo 0.8.12（agent 与 cli）已内置在 `ctb-bughunt/tools/`
- 覆盖率要求题目工作目录为纯英文、无空格路径（或系统可生成 8.3 短路径）

## 使用

1. 新建一个空目录作为题目工作目录，在该目录启动 opencode。
2. 对 opencode 说："用 ctb-bughunt 做题，题目 ID 是 <ID或考试页URL>"。
3. 模型按 SKILL.md 流程执行；也可以手动调用：

```
python <skills目录>/ctb-bughunt/scripts/ctb.py pull --subject <ID或URL>
python <skills目录>/ctb-bughunt/scripts/ctb.py decompile
python <skills目录>/ctb-bughunt/scripts/ctb.py probe -c "init()"
python <skills目录>/ctb-bughunt/scripts/ctb.py record --gen notes/steps/main.py --dry-run --coverage
python <skills目录>/ctb-bughunt/scripts/ctb.py record --gen notes/steps/main.py
python <skills目录>/ctb-bughunt/scripts/ctb.py coverage
python <skills目录>/ctb-bughunt/scripts/ctb.py check
python <skills目录>/ctb-bughunt/scripts/ctb.py local
python <skills目录>/ctb-bughunt/scripts/ctb.py run
python <skills目录>/ctb-bughunt/scripts/ctb.py submit --yes
python <skills目录>/ctb-bughunt/scripts/ctb.py status
```

## 目录

```
ctb-bughunt/
  SKILL.md
  references/   methodology.md  recording.md  framework_rules.md  assertion_patterns.md
  scripts/      ctb.py  decompile.py  probe.py  record.py  jacoco_cov.py  local_run.py  check_submission.py
                solftest_auto.py  get_ilearning_cookies.py  network_recorder.py
                keep_cookies_alive.py  update_cookies_http.py
  tools/        cfr-0.152.jar  jacocoagent.jar  jacococli.jar
```

`scripts/` 下的 solftest 相关脚本复制自 `ctbagent/agent/solftest_auto/`，那边修改后需要同步过来。
