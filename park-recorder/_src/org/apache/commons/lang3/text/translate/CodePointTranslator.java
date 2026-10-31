/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.text.translate;

import java.io.Writer;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;

public abstract class CodePointTranslator
extends CharSequenceTranslator {
    @Override
    public final int translate(CharSequence charSequence, int n, Writer writer) {
        int n2 = Character.codePointAt(charSequence, n);
        if (this.translate(n2, writer)) {
            return 1;
        }
        return 0;
    }

    public abstract boolean translate(int var1, Writer var2);
}

