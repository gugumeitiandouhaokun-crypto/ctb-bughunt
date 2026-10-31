/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

public final class JsonUtils {
    private static final char[] HC = "0123456789ABCDEF".toCharArray();
    private static final int[] ESC_CODES;
    private static final ThreadLocal<char[]> _qbufLocal;

    private static char[] getQBuf() {
        char[] cArray = _qbufLocal.get();
        if (cArray == null) {
            char[] cArray2 = new char[6];
            cArray = cArray2;
            cArray2[0] = 92;
            cArray[2] = 48;
            cArray[3] = 48;
            _qbufLocal.set(cArray);
        }
        return cArray;
    }

    public static void quoteAsString(CharSequence charSequence, StringBuilder stringBuilder) {
        char[] cArray = JsonUtils.getQBuf();
        int n = ESC_CODES.length;
        int n2 = 0;
        int n3 = charSequence.length();
        block0: while (n2 < n3) {
            int n4;
            int n5;
            while ((n5 = charSequence.charAt(n2)) >= n || ESC_CODES[n5] == 0) {
                stringBuilder.append((char)n5);
                if (++n2 >= n3) break block0;
            }
            n5 = (n4 = ESC_CODES[n5 = charSequence.charAt(n2++)]) < 0 ? JsonUtils._appendNumeric(n5, cArray) : JsonUtils._appendNamed(n4, cArray);
            stringBuilder.append(cArray, 0, n5);
        }
    }

    private static int _appendNumeric(int n, char[] cArray) {
        cArray[1] = 117;
        cArray[4] = HC[n >> 4];
        cArray[5] = HC[n & 0xF];
        return 6;
    }

    private static int _appendNamed(int n, char[] cArray) {
        cArray[1] = (char)n;
        return 2;
    }

    static {
        int[] nArray = new int[128];
        for (int i2 = 0; i2 < 32; ++i2) {
            nArray[i2] = -1;
        }
        nArray[34] = 34;
        nArray[92] = 92;
        nArray[8] = 98;
        nArray[9] = 116;
        nArray[12] = 102;
        nArray[10] = 110;
        nArray[13] = 114;
        ESC_CODES = nArray;
        _qbufLocal = new ThreadLocal();
    }
}

