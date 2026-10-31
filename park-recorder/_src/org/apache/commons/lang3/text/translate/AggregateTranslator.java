/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.text.translate;

import java.io.Writer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;

public class AggregateTranslator
extends CharSequenceTranslator {
    private final CharSequenceTranslator[] translators;

    public AggregateTranslator(CharSequenceTranslator ... charSequenceTranslatorArray) {
        this.translators = ArrayUtils.clone(charSequenceTranslatorArray);
    }

    @Override
    public int translate(CharSequence charSequence, int n, Writer writer) {
        CharSequenceTranslator[] charSequenceTranslatorArray = this.translators;
        int n2 = this.translators.length;
        for (int j = 0; j < n2; ++j) {
            int n3 = charSequenceTranslatorArray[j].translate(charSequence, n, writer);
            if (n3 == 0) continue;
            return n3;
        }
        return 0;
    }
}

