/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j.helpers;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.NamedLoggerBase;

public abstract class MarkerIgnoringBase
extends NamedLoggerBase
implements Logger {
    private static final long serialVersionUID = 9044267456635152283L;

    public boolean isTraceEnabled(Marker marker) {
        return this.isTraceEnabled();
    }

    public void trace(Marker marker, String string) {
        this.trace(string);
    }

    public void trace(Marker marker, String string, Object object) {
        this.trace(string, object);
    }

    public void trace(Marker marker, String string, Object object, Object object2) {
        this.trace(string, object, object2);
    }

    public void trace(Marker marker, String string, Object ... objectArray) {
        this.trace(string, objectArray);
    }

    public void trace(Marker marker, String string, Throwable throwable) {
        this.trace(string, throwable);
    }

    public boolean isDebugEnabled(Marker marker) {
        return this.isDebugEnabled();
    }

    public void debug(Marker marker, String string) {
        this.debug(string);
    }

    public void debug(Marker marker, String string, Object object) {
        this.debug(string, object);
    }

    public void debug(Marker marker, String string, Object object, Object object2) {
        this.debug(string, object, object2);
    }

    public void debug(Marker marker, String string, Object ... objectArray) {
        this.debug(string, objectArray);
    }

    public void debug(Marker marker, String string, Throwable throwable) {
        this.debug(string, throwable);
    }

    public boolean isInfoEnabled(Marker marker) {
        return this.isInfoEnabled();
    }

    public void info(Marker marker, String string) {
        this.info(string);
    }

    public void info(Marker marker, String string, Object object) {
        this.info(string, object);
    }

    public void info(Marker marker, String string, Object object, Object object2) {
        this.info(string, object, object2);
    }

    public void info(Marker marker, String string, Object ... objectArray) {
        this.info(string, objectArray);
    }

    public void info(Marker marker, String string, Throwable throwable) {
        this.info(string, throwable);
    }

    public boolean isWarnEnabled(Marker marker) {
        return this.isWarnEnabled();
    }

    public void warn(Marker marker, String string) {
        this.warn(string);
    }

    public void warn(Marker marker, String string, Object object) {
        this.warn(string, object);
    }

    public void warn(Marker marker, String string, Object object, Object object2) {
        this.warn(string, object, object2);
    }

    public void warn(Marker marker, String string, Object ... objectArray) {
        this.warn(string, objectArray);
    }

    public void warn(Marker marker, String string, Throwable throwable) {
        this.warn(string, throwable);
    }

    public boolean isErrorEnabled(Marker marker) {
        return this.isErrorEnabled();
    }

    public void error(Marker marker, String string) {
        this.error(string);
    }

    public void error(Marker marker, String string, Object object) {
        this.error(string, object);
    }

    public void error(Marker marker, String string, Object object, Object object2) {
        this.error(string, object, object2);
    }

    public void error(Marker marker, String string, Object ... objectArray) {
        this.error(string, objectArray);
    }

    public void error(Marker marker, String string, Throwable throwable) {
        this.error(string, throwable);
    }

    public String toString() {
        return this.getClass().getName() + "(" + this.getName() + ")";
    }
}

