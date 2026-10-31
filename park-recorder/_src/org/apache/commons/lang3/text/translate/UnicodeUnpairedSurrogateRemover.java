/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.text.translate;

import java.io.Writer;
import org.apache.commons.lang3.text.translate.CodePointTranslator;

public class UnicodeUnpairedSurrogateRemover
extends CodePointTranslator {
    @Override
    public boolean translate(int n, Writer writer) {
        return n >= 55296 && n <= 57343;
    }
}

