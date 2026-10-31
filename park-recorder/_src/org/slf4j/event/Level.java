/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j.event;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public enum Level {
    ERROR(40, "ERROR"),
    WARN(30, "WARN"),
    INFO(20, "INFO"),
    DEBUG(10, "DEBUG"),
    TRACE(0, "TRACE");

    private int levelInt;
    private String levelStr;

    private Level(int n2, String string2) {
        this.levelInt = n2;
        this.levelStr = string2;
    }

    public final int toInt() {
        return this.levelInt;
    }

    public final String toString() {
        return this.levelStr;
    }
}

