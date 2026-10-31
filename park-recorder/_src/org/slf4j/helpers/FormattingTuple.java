/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j.helpers;

public class FormattingTuple {
    public static FormattingTuple NULL = new FormattingTuple(null);
    private String message;
    private Throwable throwable;
    private Object[] argArray;

    public FormattingTuple(String string) {
        this(string, null, null);
    }

    public FormattingTuple(String string, Object[] objectArray, Throwable throwable) {
        this.message = string;
        this.throwable = throwable;
        this.argArray = objectArray;
    }

    public String getMessage() {
        return this.message;
    }

    public Object[] getArgArray() {
        return this.argArray;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }
}

