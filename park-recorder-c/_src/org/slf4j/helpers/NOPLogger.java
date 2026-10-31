/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j.helpers;

import org.slf4j.helpers.MarkerIgnoringBase;

public class NOPLogger
extends MarkerIgnoringBase {
    private static final long serialVersionUID = -517220405410904473L;
    public static final NOPLogger NOP_LOGGER = new NOPLogger();

    protected NOPLogger() {
    }

    public String getName() {
        return "NOP";
    }

    public final boolean isTraceEnabled() {
        return false;
    }

    public final void trace(String string) {
    }

    public final void trace(String string, Object object) {
    }

    public final void trace(String string, Object object, Object object2) {
    }

    public final void trace(String string, Object ... objectArray) {
    }

    public final void trace(String string, Throwable throwable) {
    }

    public final boolean isDebugEnabled() {
        return false;
    }

    public final void debug(String string) {
    }

    public final void debug(String string, Object object) {
    }

    public final void debug(String string, Object object, Object object2) {
    }

    public final void debug(String string, Object ... objectArray) {
    }

    public final void debug(String string, Throwable throwable) {
    }

    public final boolean isInfoEnabled() {
        return false;
    }

    public final void info(String string) {
    }

    public final void info(String string, Object object) {
    }

    public final void info(String string, Object object, Object object2) {
    }

    public final void info(String string, Object ... objectArray) {
    }

    public final void info(String string, Throwable throwable) {
    }

    public final boolean isWarnEnabled() {
        return false;
    }

    public final void warn(String string) {
    }

    public final void warn(String string, Object object) {
    }

    public final void warn(String string, Object object, Object object2) {
    }

    public final void warn(String string, Object ... objectArray) {
    }

    public final void warn(String string, Throwable throwable) {
    }

    public final boolean isErrorEnabled() {
        return false;
    }

    public final void error(String string) {
    }

    public final void error(String string, Object object) {
    }

    public final void error(String string, Object object, Object object2) {
    }

    public final void error(String string, Object ... objectArray) {
    }

    public final void error(String string, Throwable throwable) {
    }
}

