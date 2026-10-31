/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j.event;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.SubstituteLogger;

public class SubstituteLoggingEvent
implements LoggingEvent {
    Level level;
    Marker marker;
    String loggerName;
    SubstituteLogger logger;
    String threadName;
    String message;
    Object[] argArray;
    long timeStamp;
    Throwable throwable;

    public Level getLevel() {
        return this.level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Marker getMarker() {
        return this.marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String getLoggerName() {
        return this.loggerName;
    }

    public void setLoggerName(String string) {
        this.loggerName = string;
    }

    public SubstituteLogger getLogger() {
        return this.logger;
    }

    public void setLogger(SubstituteLogger substituteLogger) {
        this.logger = substituteLogger;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String string) {
        this.message = string;
    }

    public Object[] getArgumentArray() {
        return this.argArray;
    }

    public void setArgumentArray(Object[] objectArray) {
        this.argArray = objectArray;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(long l) {
        this.timeStamp = l;
    }

    public String getThreadName() {
        return this.threadName;
    }

    public void setThreadName(String string) {
        this.threadName = string;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}

