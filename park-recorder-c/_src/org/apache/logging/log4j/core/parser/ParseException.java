/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.parser;

public class ParseException
extends Exception {
    private static final long serialVersionUID = -2739649998196663857L;

    public ParseException(String string) {
        super(string);
    }

    public ParseException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public ParseException(Throwable throwable) {
        super(throwable);
    }
}

