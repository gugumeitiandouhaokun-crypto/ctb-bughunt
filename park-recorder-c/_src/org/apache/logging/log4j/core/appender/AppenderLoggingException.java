/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import org.apache.logging.log4j.LoggingException;

public class AppenderLoggingException
extends LoggingException {
    private static final long serialVersionUID = 6545990597472958303L;

    public AppenderLoggingException(String string) {
        super(string);
    }

    public AppenderLoggingException(String string, Object ... objectArray) {
        super(String.format(string, objectArray));
    }

    public AppenderLoggingException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public AppenderLoggingException(Throwable throwable) {
        super(throwable);
    }

    public AppenderLoggingException(Throwable throwable, String string, Object ... objectArray) {
        super(String.format(string, objectArray), throwable);
    }
}

