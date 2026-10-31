/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.h;

public final class f {
    public static int a(byte[] byArray) {
        int n = 0;
        for (int i2 = 0; i2 < 4; ++i2) {
            int n2 = (3 - i2) * 8;
            n += (byArray[i2] & 0xFF) << n2;
        }
        return n;
    }
}

