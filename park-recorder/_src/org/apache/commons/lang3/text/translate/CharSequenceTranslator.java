/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.text.translate;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import org.apache.commons.lang3.text.translate.AggregateTranslator;

public abstract class CharSequenceTranslator {
    static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public abstract int translate(CharSequence var1, int var2, Writer var3);

    public final String translate(CharSequence charSequence) {
        if (charSequence == null) {
            return null;
        }
        try {
            StringWriter stringWriter = new StringWriter(charSequence.length() * 2);
            this.translate(charSequence, stringWriter);
            return stringWriter.toString();
        }
        catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
    }

    public final void translate(CharSequence charSequence, Writer writer) {
        if (writer == null) {
            throw new IllegalArgumentException("The Writer must not be null");
        }
        if (charSequence == null) {
            return;
        }
        int n = 0;
        int n2 = charSequence.length();
        while (n < n2) {
            int n3;
            int n4 = this.translate(charSequence, n, writer);
            if (n4 == 0) {
                n3 = charSequence.charAt(n);
                writer.write(n3);
                if (!Character.isHighSurrogate((char)n3) || ++n >= n2) continue;
                char c2 = charSequence.charAt(n);
                n4 = c2;
                if (!Character.isLowSurrogate(c2)) continue;
                writer.write(n4);
                ++n;
                continue;
            }
            for (n3 = 0; n3 < n4; ++n3) {
                n += Character.charCount(Character.codePointAt(charSequence, n));
            }
        }
    }

    public final CharSequenceTranslator with(CharSequenceTranslator ... charSequenceTranslatorArray) {
        CharSequenceTranslator[] charSequenceTranslatorArray2 = new CharSequenceTranslator[charSequenceTranslatorArray.length + 1];
        CharSequenceTranslator[] charSequenceTranslatorArray3 = charSequenceTranslatorArray2;
        charSequenceTranslatorArray2[0] = this;
        System.arraycopy(charSequenceTranslatorArray, 0, charSequenceTranslatorArray3, 1, charSequenceTranslatorArray.length);
        return new AggregateTranslator(charSequenceTranslatorArray3);
    }

    public static String hex(int n) {
        return Integer.toHexString(n).toUpperCase(Locale.ENGLISH);
    }
}

