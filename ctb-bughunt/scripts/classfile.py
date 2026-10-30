# -*- coding: utf-8 -*-
"""最小 class 文件解析：读取方法的行号表，以及每个原始源码行的字节码引用的常量、方法、字段、类型。

用途：JaCoCo 报告中的行号是原始源码行号，与反编译源码不对应；用每行引用的"线索"在反编译源码中搜索对应行。
只依赖标准库，不需要 JDK 的 javap。
"""
from __future__ import annotations

import struct
from collections import defaultdict

OPERAND_LEN = {
    0x10: 1, 0x11: 2, 0x12: 1, 0x13: 2, 0x14: 2, 0x84: 2, 0xa9: 1, 0xb9: 4, 0xba: 4,
    0xbc: 1, 0xc5: 3, 0xc8: 4, 0xc9: 4,
}
for _op in range(0x15, 0x1a):
    OPERAND_LEN[_op] = 1
for _op in range(0x36, 0x3b):
    OPERAND_LEN[_op] = 1
for _op in list(range(0x99, 0xa9)) + list(range(0xb2, 0xb9)) + [0xbb, 0xbd, 0xc0, 0xc1, 0xc6, 0xc7]:
    OPERAND_LEN[_op] = 2

SKIP_CALL_OWNERS = ("java/lang/StringBuilder", "java/lang/StringBuffer")
SKIP_CALLS = {"<init>", "<clinit>"}


class ClassFileError(ValueError):
    pass


class _Reader:
    def __init__(self, data: bytes):
        self.data, self.pos = data, 0

    def take(self, fmt: str):
        size = struct.calcsize(fmt)
        if self.pos + size > len(self.data):
            raise ClassFileError("class 文件截断")
        val = struct.unpack_from(fmt, self.data, self.pos)
        self.pos += size
        return val if len(val) > 1 else val[0]

    def raw(self, n: int) -> bytes:
        out = self.data[self.pos:self.pos + n]
        self.pos += n
        return out


def _read_pool(r: _Reader) -> list:
    count = r.take(">H")
    pool: list = [None] * count
    i = 1
    while i < count:
        tag = r.take(">B")
        if tag == 1:
            pool[i] = ("utf8", r.raw(r.take(">H")).decode("utf-8", "replace"))
        elif tag == 3:
            pool[i] = ("num", r.take(">i"))
        elif tag == 4:
            pool[i] = ("num", r.take(">f"))
        elif tag in (5, 6):
            pool[i] = ("num", r.take(">q" if tag == 5 else ">d"))
            i += 1
        elif tag in (7, 8, 16, 19, 20):
            pool[i] = ({7: "class", 8: "string", 16: "mtype", 19: "module", 20: "package"}[tag], r.take(">H"))
        elif tag in (9, 10, 11):
            pool[i] = ("ref", r.take(">HH"))
        elif tag == 12:
            pool[i] = ("nat", r.take(">HH"))
        elif tag == 15:
            pool[i] = ("mhandle", r.take(">BH"))
        elif tag in (17, 18):
            pool[i] = ("dynamic", r.take(">HH"))
        else:
            raise ClassFileError(f"未知常量池标记 {tag}")
        i += 1
    return pool


def _utf8(pool: list, idx: int) -> str:
    entry = pool[idx] if 0 < idx < len(pool) else None
    return entry[1] if entry and entry[0] == "utf8" else ""


def _const(pool: list, idx: int):
    entry = pool[idx] if 0 < idx < len(pool) else None
    if not entry:
        return None
    kind, val = entry
    if kind == "string":
        return ("str", _utf8(pool, val))
    if kind == "num":
        return ("num", val)
    if kind == "class":
        return ("type", _utf8(pool, val).rsplit("/", 1)[-1])
    return None


def _ref(pool: list, idx: int) -> tuple[str, str]:
    entry = pool[idx] if 0 < idx < len(pool) else None
    if not entry or entry[0] != "ref":
        return "", ""
    cls_idx, nat_idx = entry[1]
    owner = _utf8(pool, pool[cls_idx][1]) if pool[cls_idx] else ""
    nat = pool[nat_idx]
    return owner, _utf8(pool, nat[1][0]) if nat else ""


def _walk(code: bytes):
    pc, n = 0, len(code)
    while pc < n:
        op = code[pc]
        if op in (0xaa, 0xab):
            base = pc + 1 + (4 - (pc + 1) % 4) % 4
            if op == 0xaa:
                low, high = struct.unpack_from(">ii", code, base + 4)
                length = base - pc + 12 + 4 * (high - low + 1)
            else:
                npairs = struct.unpack_from(">i", code, base + 4)[0]
                length = base - pc + 8 + 8 * npairs
        elif op == 0xc4:
            length = 6 if code[pc + 1] == 0x84 else 4
        else:
            length = 1 + OPERAND_LEN.get(op, 0)
        yield pc, op, code[pc + 1:pc + length]
        pc += length


def _tokens(pool: list, op: int, operand: bytes):
    if op == 0x10:
        v = struct.unpack(">b", operand)[0]
        return ("num", v) if v not in (0, 1) else None
    if op == 0x11:
        v = struct.unpack(">h", operand)[0]
        return ("num", v) if v not in (0, 1) else None
    if op == 0x12:
        return _const(pool, operand[0])
    if op in (0x13, 0x14):
        return _const(pool, struct.unpack(">H", operand)[0])
    if 0xb2 <= op <= 0xb5:
        return ("name", _ref(pool, struct.unpack(">H", operand[:2])[0])[1])
    if 0xb6 <= op <= 0xb9:
        owner, name = _ref(pool, struct.unpack(">H", operand[:2])[0])
        if name in SKIP_CALLS or owner in SKIP_CALL_OWNERS:
            return None
        return ("name", name)
    if op in (0xbb, 0xbd, 0xc0, 0xc1):
        return _const(pool, struct.unpack(">H", operand)[0])
    return None


def parse(data: bytes) -> dict:
    """返回 {(方法名, 描述符): {原始行号: set(线索)}}；线索为 ("str"|"num"|"name"|"type", 值)。"""
    r = _Reader(data)
    if r.take(">I") != 0xCAFEBABE:
        raise ClassFileError("不是 class 文件")
    r.take(">HH")
    pool = _read_pool(r)
    r.take(">HHH")
    r.raw(2 * r.take(">H"))
    for _ in range(r.take(">H")):
        r.take(">HHH")
        for _ in range(r.take(">H")):
            r.take(">H")
            r.raw(r.take(">I"))
    methods: dict = {}
    for _ in range(r.take(">H")):
        _, name_idx, desc_idx = r.take(">HHH")
        key = (_utf8(pool, name_idx), _utf8(pool, desc_idx))
        lines: dict[int, set] = defaultdict(set)
        for _ in range(r.take(">H")):
            attr = _utf8(pool, r.take(">H"))
            body = r.raw(r.take(">I"))
            if attr == "Code":
                lines = _code_lines(pool, body)
        methods[key] = dict(lines)
    return methods


def _code_lines(pool: list, body: bytes) -> dict[int, set]:
    r = _Reader(body)
    r.take(">HH")
    code = r.raw(r.take(">I"))
    r.raw(8 * r.take(">H"))
    table: list[tuple[int, int]] = []
    for _ in range(r.take(">H")):
        attr = _utf8(pool, r.take(">H"))
        data = r.raw(r.take(">I"))
        if attr == "LineNumberTable":
            n = struct.unpack_from(">H", data, 0)[0]
            table += [struct.unpack_from(">HH", data, 2 + 4 * i) for i in range(n)]
    table.sort()
    out: dict[int, set] = defaultdict(set)
    if not table:
        return out
    for pc, op, operand in _walk(code):
        line = None
        for start, ln in table:
            if start <= pc:
                line = ln
            else:
                break
        if line is None:
            continue
        bucket = out.setdefault(line, set())
        tok = _tokens(pool, op, operand)
        if tok and tok[1] not in ("", None):
            bucket.add(tok)
    return out
