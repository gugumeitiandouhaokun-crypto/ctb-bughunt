/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.io.Writer;

public class CloseShieldWriter
extends Writer {
    private final Writer delegate;

    public CloseShieldWriter(Writer writer) {
        this.delegate = writer;
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
        this.delegate.flush();
    }

    @Override
    public void write(char[] cArray, int n, int n2) {
        this.delegate.write(cArray, n, n2);
    }
}

