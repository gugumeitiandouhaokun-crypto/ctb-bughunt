/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.h;

public final class f {
    public static int a(byte[] byArray) {
        int n = 0;
        for (int j = 0; j < 4; ++j) {
            int n2 = (3 - j) * 8;
            n += (byArray[j] & 0xFF) << n2;
        }
        return n;
    }
}

