/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.h;

import com.huawei.tools.h.a;
import com.huawei.tools.h.b;

public final class c {
    private static final String bZ = com.huawei.tools.a.a.c.key;
    private static final String ca = com.huawei.tools.a.a.e.key;

    public static boolean o() {
        if (a.B(com.huawei.tools.a.a.b.key).equals("tp000") && (b.C(a.B(bZ)) || !b.C(a.B(ca)))) {
            com.huawei.tools.e.a.error(com.huawei.tools.a.a.f.key);
            return true;
        }
        return false;
    }
}

