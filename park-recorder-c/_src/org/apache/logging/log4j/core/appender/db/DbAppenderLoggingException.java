/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.db;

import org.apache.logging.log4j.core.appender.AppenderLoggingException;

public class DbAppenderLoggingException
extends AppenderLoggingException {
    private static final long serialVersionUID = 1L;

    public DbAppenderLoggingException(String string, Object ... objectArray) {
        super(string, objectArray);
    }

    public DbAppenderLoggingException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public DbAppenderLoggingException(Throwable throwable, String string, Object ... objectArray) {
        super(throwable, string, objectArray);
    }
}

