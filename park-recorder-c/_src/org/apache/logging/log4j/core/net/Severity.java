/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net;

import org.apache.logging.log4j.Level;

public enum Severity {
    EMERG(0),
    ALERT(1),
    CRITICAL(2),
    ERROR(3),
    WARNING(4),
    NOTICE(5),
    INFO(6),
    DEBUG(7);

    private final int code;

    private Severity(int n2) {
        this.code = n2;
    }

    public final int getCode() {
        return this.code;
    }

    public final boolean isEqual(String string) {
        return this.name().equalsIgnoreCase(string);
    }

    public static Severity getSeverity(Level level) {
        switch (level.getStandardLevel()) {
            case ALL: {
                return DEBUG;
            }
            case TRACE: {
                return DEBUG;
            }
            case DEBUG: {
                return DEBUG;
            }
            case INFO: {
                return INFO;
            }
            case WARN: {
                return WARNING;
            }
            case ERROR: {
                return ERROR;
            }
            case FATAL: {
                return ALERT;
            }
            case OFF: {
                return EMERG;
            }
        }
        return DEBUG;
    }
}

