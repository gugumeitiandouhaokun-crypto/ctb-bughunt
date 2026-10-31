/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.text.translate;

import java.io.Writer;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;

public class OctalUnescaper
extends CharSequenceTranslator {
    @Override
    public int translate(CharSequence charSequence, int n, Writer writer) {
        int n2 = charSequence.length() - n - 1;
        StringBuilder stringBuilder = new StringBuilder();
        if (charSequence.charAt(n) == '\\' && n2 > 0 && this.isOctalDigit(charSequence.charAt(n + 1))) {
            int n3 = n + 1;
            int n4 = n + 2;
            n += 3;
            stringBuilder.append(charSequence.charAt(n3));
            if (n2 > 1 && this.isOctalDigit(charSequence.charAt(n4))) {
                stringBuilder.append(charSequence.charAt(n4));
                if (n2 > 2 && this.isZeroToThree(charSequence.charAt(n3)) && this.isOctalDigit(charSequence.charAt(n))) {
                    stringBuilder.append(charSequence.charAt(n));
                }
            }
            writer.write(Integer.parseInt(stringBuilder.toString(), 8));
            return 1 + stringBuilder.length();
        }
        return 0;
    }

    private boolean isOctalDigit(char c2) {
        return c2 >= '0' && c2 <= '7';
    }

    private boolean isZeroToThree(char c2) {
        return c2 >= '0' && c2 <= '3';
    }
}

