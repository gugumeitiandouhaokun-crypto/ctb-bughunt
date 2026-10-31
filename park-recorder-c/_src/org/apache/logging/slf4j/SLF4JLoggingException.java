/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.slf4j;

public class SLF4JLoggingException
extends RuntimeException {
    private static final long serialVersionUID = -1618650972455089998L;

    public SLF4JLoggingException(String string) {
        super(string);
    }

    public SLF4JLoggingException(String string, Exception exception) {
        super(string, exception);
    }

    public SLF4JLoggingException(Exception exception) {
        super(exception);
    }
}

