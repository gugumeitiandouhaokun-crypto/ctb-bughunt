/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.convert;

public class HexConverter {
    public static byte[] parseHexBinary(String string) {
        int n = string.length();
        byte[] byArray = new byte[n / 2];
        for (int i2 = 0; i2 < n; i2 += 2) {
            byArray[i2 / 2] = (byte)((Character.digit(string.charAt(i2), 16) << 4) + Character.digit(string.charAt(i2 + 1), 16));
        }
        return byArray;
    }
}

