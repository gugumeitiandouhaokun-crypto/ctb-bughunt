# -*- coding: utf-8 -*-
"""把 ctb-bughunt/ 打包为 dist/ctb-bughunt.zip（zip 根目录为 ctb-bughunt/）。"""
from __future__ import annotations

import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
SKILL = ROOT / "ctb-bughunt"
DIST = ROOT / "dist"
SKIP_DIRS = {"__pycache__", ".pytest_cache"}
SKIP_SUFFIXES = {".pyc", ".log", ".jsonl"}
REQUIRED = ("SKILL.md", "scripts/ctb.py", "scripts/record.py", "scripts/jacoco_cov.py", "scripts/cov_analyze.py",
            "scripts/classfile.py", "scripts/spec.py", "scripts/matrix.py", "scripts/dut.py", "scripts/hits.py",
            "scripts/naming.py",
            "scripts/jobs.py",
            "scripts/explored.py",
            "references/recording.md",
            "tools/cfr-0.152.jar", "tools/jacocoagent.jar", "tools/jacococli.jar")
NAME_RE = re.compile(r"^name:\s*(\S+)\s*$", re.M)


def check_skill() -> None:
    for rel in REQUIRED:
        if not (SKILL / rel).is_file():
            raise SystemExit(f"缺少 {SKILL / rel}")
    head = (SKILL / "SKILL.md").read_text(encoding="utf-8")
    m = NAME_RE.search(head.split("---")[1] if head.startswith("---") else "")
    if not m or m.group(1) != SKILL.name:
        raise SystemExit(f"SKILL.md 的 name 必须与目录名 {SKILL.name} 一致")


def main() -> int:
    check_skill()
    DIST.mkdir(exist_ok=True)
    out = DIST / f"{SKILL.name}.zip"
    count = 0
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as zf:
        for p in sorted(SKILL.rglob("*")):
            if not p.is_file() or SKIP_DIRS & set(p.parts) or p.suffix in SKIP_SUFFIXES:
                continue
            zf.write(p, p.relative_to(ROOT).as_posix())
            count += 1
    print(f"已打包 {count} 个文件 -> {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
