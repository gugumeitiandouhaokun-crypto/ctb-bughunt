/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.text.translate;

import java.io.Writer;
import java.util.Arrays;
import java.util.EnumSet;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;

public class NumericEntityUnescaper
extends CharSequenceTranslator {
    private final EnumSet<OPTION> options;

    public NumericEntityUnescaper(OPTION ... oPTIONArray) {
        if (oPTIONArray.length > 0) {
            this.options = EnumSet.copyOf(Arrays.asList(oPTIONArray));
            return;
        }
        this.options = EnumSet.copyOf(Arrays.asList(OPTION.semiColonRequired));
    }

    public boolean isSet(OPTION oPTION) {
        if (this.options == null) {
            return false;
        }
        return this.options.contains((Object)oPTION);
    }

    @Override
    public int translate(CharSequence charSequence, int n, Writer writer) {
        int n2 = charSequence.length();
        if (charSequence.charAt(n) == '&' && n < n2 - 2 && charSequence.charAt(n + 1) == '#') {
            int n3;
            boolean bl = false;
            int n4 = charSequence.charAt(n += 2);
            if (n4 == 120 || n4 == 88) {
                bl = true;
                if (++n == n2) {
                    return 0;
                }
            }
            for (n4 = n; n4 < n2 && (charSequence.charAt(n4) >= '0' && charSequence.charAt(n4) <= '9' || charSequence.charAt(n4) >= 'a' && charSequence.charAt(n4) <= 'f' || charSequence.charAt(n4) >= 'A' && charSequence.charAt(n4) <= 'F'); ++n4) {
            }
            if ((n2 = n4 != n2 && charSequence.charAt(n4) == ';' ? 1 : 0) == 0) {
                if (this.isSet(OPTION.semiColonRequired)) {
                    return 0;
                }
                if (this.isSet(OPTION.errorIfNoSemiColon)) {
                    throw new IllegalArgumentException("Semi-colon required at end of numeric entity");
                }
            }
            try {
                n3 = bl ? Integer.parseInt(charSequence.subSequence(n, n4).toString(), 16) : Integer.parseInt(charSequence.subSequence(n, n4).toString(), 10);
            }
            catch (NumberFormatException numberFormatException) {
                return 0;
            }
            if (n3 > 65535) {
                char[] cArray = Character.toChars(n3);
                writer.write(cArray[0]);
                writer.write(cArray[1]);
            } else {
                writer.write(n3);
            }
            return 2 + n4 - n + (bl ? 1 : 0) + (n2 != 0 ? 1 : 0);
        }
        return 0;
    }

    public static enum OPTION {
        semiColonRequired,
        semiColonOptional,
        errorIfNoSemiColon;

    }
}

