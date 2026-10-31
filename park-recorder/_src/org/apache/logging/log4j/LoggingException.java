/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j;

public class LoggingException
extends RuntimeException {
    private static final long serialVersionUID = 6366395965071580537L;

    public LoggingException(String string) {
        super(string);
    }

    public LoggingException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public LoggingException(Throwable throwable) {
        super(throwable);
    }
}

