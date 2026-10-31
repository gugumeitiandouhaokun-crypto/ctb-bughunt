/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import org.apache.logging.log4j.util.Strings;

public final class Integers {
    private static final int BITS_PER_INT = 32;

    private Integers() {
    }

    public static int parseInt(String string, int n) {
        if (Strings.isEmpty(string)) {
            return n;
        }
        return Integer.parseInt(string);
    }

    public static int parseInt(String string) {
        return Integers.parseInt(string, 0);
    }

    public static int ceilingNextPowerOfTwo(int n) {
        return 1 << 32 - Integer.numberOfLeadingZeros(n - 1);
    }
}

