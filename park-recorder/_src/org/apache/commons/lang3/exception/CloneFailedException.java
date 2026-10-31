/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.exception;

public class CloneFailedException
extends RuntimeException {
    private static final long serialVersionUID = 20091223L;

    public CloneFailedException(String string) {
        super(string);
    }

    public CloneFailedException(Throwable throwable) {
        super(throwable);
    }

    public CloneFailedException(String string, Throwable throwable) {
        super(string, throwable);
    }
}

