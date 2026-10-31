/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.convert;

public class HexConverter {
    public static byte[] parseHexBinary(String string) {
        int n = string.length();
        byte[] byArray = new byte[n / 2];
        for (int j = 0; j < n; j += 2) {
            byArray[j / 2] = (byte)((Character.digit(string.charAt(j), 16) << 4) + Character.digit(string.charAt(j + 1), 16));
        }
        return byArray;
    }
}

