/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.io.OutputStream;

public class CloseShieldOutputStream
extends OutputStream {
    private final OutputStream delegate;

    public CloseShieldOutputStream(OutputStream outputStream) {
        this.delegate = outputStream;
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
        this.delegate.flush();
    }

    @Override
    public void write(byte[] byArray) {
        this.delegate.write(byArray);
    }

    @Override
    public void write(byte[] byArray, int n, int n2) {
        this.delegate.write(byArray, n, n2);
    }

    @Override
    public void write(int n) {
        this.delegate.write(n);
    }
}

