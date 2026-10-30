---
name: ctb-bughunt
description: 华为 iLearning 软件测试实操题（UDP 黑盒被测系统 + Python 测试框架）的找 bug 用例开发全流程：拉取题目、反编译无 bug jar、探针摸清接口、只写步骤由无 bug 版本录制真实返回自动生成逐条精确断言、JaCoCo 覆盖率指引补充场景、本地验证、调试判题、复盘迭代、正式提交。Use when developing bug-hunting test cases for iLearning softTest / CTB UDP system-under-test exams.
compatibility: opencode
---

# ctb-bughunt：找 bug 测试用例开发

目标：针对题目给出的被测系统，写出在**无 bug 版本上全部通过**、在植入 bug 的版本上**检出尽量多 bug** 的测试用例。
方法论：**探针摸清接口 → 生成脚本描述超大密集多对象场景 → 录制得到逐条精确断言 → 覆盖率找缺口补场景 → 判题复盘闭环**。
开始前必须读完 `references/methodology.md` 与 `references/recording.md`。

## 脚本位置与调用方式

`<SKILL>` 指本 SKILL.md 所在目录，常见位置（存在哪个用哪个）：
- `~/.config/opencode/skills/ctb-bughunt`（Windows：`%USERPROFILE%\.config\opencode\skills\ctb-bughunt`）
- 项目内 `.opencode/skills/ctb-bughunt`

所有操作都通过统一入口完成，**在题目工作目录（cwd）下执行**：

```
python "<SKILL>/scripts/ctb.py" <子命令> [参数]
```

| 子命令 | 作用 |
|---|---|
| `cookies` | 获取/刷新 iLearning 登录 Cookie（弹出浏览器免密登录） |
| `pull --subject <题目ID或URL>` | 拉取题目描述、框架、jar 与服务端代码到 `answer/`，保存原件到 `notes/pristine/` |
| `decompile` | 反编译 `answer/` 下的无 bug jar 到 `decompiled/src/`，生成 `decompiled/index.md` |
| `probe -f <命令文件>` / `probe -c "<命令>"` | 探针：启动无 bug 程序逐条发送命令，打印真实返回与耗时，记录到 `notes/probes/` |
| `record --gen <生成脚本>` / `record -f <步骤文件>` | 录制：在无 bug 程序上双跑步骤，生成数据表断言写入 `testaw.py` / `udpunittest.py`，随后自动 `check` + `local`；`--dry-run` 只出报告不写文件，`--coverage` 同时统计覆盖率，`--name` 指定录制名称 |
| `coverage` | 带 JaCoCo 跑一次 `local`，输出业务代码覆盖率与未覆盖方法到 `notes/coverage/` |
| `check` | 提交前规范检查（框架铁律、Python 3.8 语法、标准库、残留文件等） |
| `local` | 在无 bug 程序上运行 `answer/udpunittest.py`，以日志 `Ran N tests ... OK` 判定 |
| `run` | 先 `check` + `local` 全部通过，再提交平台调试（type=2），判题结果进 `notes/judge/` |
| `submit --yes` | 同 `run` 的前置检查，正式提交（type=1，计入成绩） |
| `status` / `info` | 查看本地进度 / 平台题目状态与提交记录 |

依赖：Python 3.10+（`pip install requests selenium`）、`java` 在 PATH 中、iLearning 免密登录环境。

## 工作目录

每道题使用一个独立的空目录：

```
<work>/
  cookies.json
  answer/       题目与框架；提交的就是这里的 testaw.py、udpunittest.py
  decompiled/   src/ 反编译源码，index.md 类/方法/字符串常量索引
  notes/        ctb.json、pristine/、数值模型.md、复盘.md、probes/、judge/history.md、local_last.txt
                steps/     生成脚本与其输出的步骤
                record/    录制报告
                coverage/  覆盖率报告与 history.md
```

## 流程（按序执行）

1. **拉题与通读**
   - `ctb.py pull --subject <ID或URL>`（没有 Cookie 时会自动获取）。
   - 通读 `answer/题目描述.md`、`answer/题目补充信息.md`、`answer/udpunittest.py`、`answer/testaw.py`、`references/framework_rules.md`。
   - 列出全部接口、参数、返回格式、业务规则、数值边界、时间规则，写入 `notes/数值模型.md`。
2. **反编译梳理规则**
   - `ctb.py decompile`，先读 `decompiled/index.md`，再按需打开具体源码文件，不要整份读入。
   - 从命令名字符串定位命令分发处，逐个接口梳理：参数校验顺序、返回码与返回文本、状态字段、计算公式与常量、时间/周期处理、容量上限。
   - 结论作为**待验证的假设**写入 `notes/数值模型.md`。源码只提供假设，断言用的数值必须来自探针。
3. **探针摸清接口**
   - 在 `notes/probes/` 下写命令文件，`ctb.py probe -f notes/probes/<名称>.txt` 确认每个接口的返回格式、非法分支返回码、系统生成标识的形态、时间边界与资源上限。
   - 记下单条命令平均耗时，用于估算每个测试的步骤数上限；结论写回 `notes/数值模型.md` 并注明探针记录文件名。
4. **生成脚本 + 录制迭代**（格式与约束见 `references/recording.md`）
   - 在 `notes/steps/` 下按场景主题写生成脚本，按 `references/methodology.md` 的规模锚点与多对象骨架输出步骤。
   - `ctb.py record --gen notes/steps/<名称>.py --dry-run --coverage`，读录制报告（命令分布、返回码分布、两轮不一致清单）与覆盖率报告（部分覆盖/未执行的业务方法）。
   - 对照反编译源码补对象、轮次、状态组合与非法分支，重复录制，直到业务方法覆盖不再提升、命令分布合理、不一致步骤已处理。
5. **写入并本地全绿**
   - 去掉 `--dry-run` 执行 `ctb.py record --gen ...`，自动写入并执行 `check` + `local`。
   - 需要表达公式或累计关系的断言，在录制标记之外手写用例（写法见 `references/assertion_patterns.md`），数值先探针确认。
   - 手写或调整后 `ctb.py check` + `ctb.py local` 直到全部通过；`ctb.py coverage` 查看全部用例的覆盖率。
6. **调试判题与复盘**
   - `ctb.py run`，阅读输出的 bug 检出表与代码质量扣分项（摘要在 `notes/judge/history.md`）。
   - 在 `notes/复盘.md` 记录本轮：得分、未检出 bug、覆盖率、推测缺口（规则/非法分支/状态深度/未覆盖方法）、下一轮计划；然后回到第 4 步。
7. **正式提交**：检出不再提升或用户确认后，执行 `ctb.py submit --yes`。正式提交计入成绩，除非用户已明确要求，否则先征得用户同意。

## 硬性纪律

1. 不修改 `testtool.py`、`timeout.py`、`config.properties`；`udpunittest.py` 的 `if __name__ == '__main__':` 段一个字符都不改；只使用 `MyTestCase1` 一个测试类。
2. 断言优先由 `record` 从无 bug 版本录制生成；手写断言中的每个数字都必须先经探针确认，并能在 `notes/数值模型.md` 里找到来源；禁止凭感觉或只凭源码写死数值。不手工修改 `ctb-record` 标记内的内容，需要调整就改步骤后重录。
3. 任何改动后都要重新 `check` + `local`，保持无 bug 版本全部通过；`run` / `submit` 已强制执行这两步，不要绕过。
4. 探针通过 `ctb.py probe` 进行，不在 `answer/` 下创建文件；确需临时文件时用 `_probe*` 前缀并在用完后立即删除（`check` 会拦截残留）。
5. 只用 Python 标准库，代码兼容 Python 3.8；单个用例必须在 300 秒内完成（`local` 会给出每个用例耗时）。
6. 无 bug 版本运行时进程退出码可能非 0，以日志 `Ran N tests ... OK` 为准。
7. 录制只保证用例与无 bug 版本一致，能否检出 bug 取决于步骤设计；用录制报告的命令分布与覆盖率检验步骤质量，不以"录制通过"作为完成标准。
8. 长任务中上下文会被压缩：数值模型、探针结论、复盘结论必须写进 `notes/`，每轮开始先重读 `notes/数值模型.md` 与 `notes/复盘.md`。

## 参考资料

- `references/methodology.md`：方法论（规模锚点、多对象骨架、时间与资源排布、断言分层、复盘循环），已按本平台约束改写。
- `references/recording.md`：步骤文件与生成脚本格式、录制断言规则、录制报告与覆盖率报告的读法、迭代方式。
- `references/framework_rules.md`：框架铁律、文件结构、命令格式、返回码、代码质量要求。
- `references/assertion_patterns.md`：手写用例时的 testaw 辅助函数、公式函数、非法分支写法示例。
