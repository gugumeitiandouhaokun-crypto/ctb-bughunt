/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.h;

import com.huawei.tools.h.a;
import com.huawei.tools.h.b;

public final class c {
    private static final String dc = com.huawei.tools.a.a.c.key;
    private static final String dd = com.huawei.tools.a.a.e.key;

    public static boolean v() {
        if (a.aj(com.huawei.tools.a.a.b.key).equals("tp000") && (b.ak(a.aj(dc)) || !b.ak(a.aj(dd)))) {
            com.huawei.tools.e.a.error(com.huawei.tools.a.a.f.key);
            return true;
        }
        return false;
    }
}

