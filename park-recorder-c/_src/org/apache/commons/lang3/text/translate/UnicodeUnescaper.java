/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.text.translate;

import java.io.Writer;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;

public class UnicodeUnescaper
extends CharSequenceTranslator {
    @Override
    public int translate(CharSequence charSequence, int n, Writer writer) {
        if (charSequence.charAt(n) == '\\' && n + 1 < charSequence.length() && charSequence.charAt(n + 1) == 'u') {
            int n2 = 2;
            while (n + n2 < charSequence.length() && charSequence.charAt(n + n2) == 'u') {
                ++n2;
            }
            if (n + n2 < charSequence.length() && charSequence.charAt(n + n2) == '+') {
                ++n2;
            }
            if (n + n2 + 4 <= charSequence.length()) {
                charSequence = charSequence.subSequence(n + n2, n + n2 + 4);
                try {
                    n = Integer.parseInt(charSequence.toString(), 16);
                    writer.write((char)n);
                }
                catch (NumberFormatException numberFormatException) {
                    throw new IllegalArgumentException("Unable to parse unicode value: ".concat(String.valueOf(charSequence)), numberFormatException);
                }
                return n2 + 4;
            }
            throw new IllegalArgumentException("Less than 4 hex digits in unicode value: '" + charSequence.subSequence(n, charSequence.length()) + "' due to end of CharSequence");
        }
        return 0;
    }
}

