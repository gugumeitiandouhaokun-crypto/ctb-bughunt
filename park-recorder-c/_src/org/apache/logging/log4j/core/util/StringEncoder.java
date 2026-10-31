/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class StringEncoder {
    private StringEncoder() {
    }

    public static byte[] toBytes(String string, Charset charset) {
        if (string != null) {
            if (StandardCharsets.ISO_8859_1.equals(charset)) {
                return StringEncoder.encodeSingleByteChars(string);
            }
            charset = charset != null ? charset : Charset.defaultCharset();
            try {
                return string.getBytes(charset.name());
            }
            catch (UnsupportedEncodingException unsupportedEncodingException) {
                return string.getBytes(charset);
            }
        }
        return null;
    }

    public static byte[] encodeSingleByteChars(CharSequence charSequence) {
        int n = charSequence.length();
        byte[] byArray = new byte[n];
        StringEncoder.encodeString(charSequence, 0, n, byArray);
        return byArray;
    }

    public static int encodeIsoChars(CharSequence charSequence, int n, byte[] byArray, int n2, int n3) {
        char c2;
        int n4;
        for (n4 = 0; n4 < n3 && (c2 = charSequence.charAt(n++)) <= '\u00ff'; ++n4) {
            byArray[n2++] = (byte)c2;
        }
        return n4;
    }

    public static int encodeString(CharSequence charSequence, int n, int n2, byte[] byArray) {
        int n3 = 0;
        int n4 = Math.min(n2, byArray.length);
        int n5 = n + n4;
        while (n < n5) {
            int n6 = StringEncoder.encodeIsoChars(charSequence, n, byArray, n3, n4);
            n += n6;
            n3 += n6;
            if (n6 == n4) continue;
            if (Character.isHighSurrogate(charSequence.charAt(n++)) && n < n5 && Character.isLowSurrogate(charSequence.charAt(n))) {
                if (n2 > byArray.length) {
                    ++n5;
                    --n2;
                }
                ++n;
            }
            byArray[n3++] = 63;
            n4 = Math.min(n5 - n, byArray.length - n3);
        }
        return n3;
    }
}

