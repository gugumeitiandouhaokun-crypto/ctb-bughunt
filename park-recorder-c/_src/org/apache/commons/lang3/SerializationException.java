/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

public class SerializationException
extends RuntimeException {
    private static final long serialVersionUID = 4029025366392702726L;

    public SerializationException() {
    }

    public SerializationException(String string) {
        super(string);
    }

    public SerializationException(Throwable throwable) {
        super(throwable);
    }

    public SerializationException(String string, Throwable throwable) {
        super(string, throwable);
    }
}

