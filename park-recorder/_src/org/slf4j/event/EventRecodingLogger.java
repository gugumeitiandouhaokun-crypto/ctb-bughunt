/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j.event;

import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.event.SubstituteLoggingEvent;
import org.slf4j.helpers.SubstituteLogger;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class EventRecodingLogger
implements Logger {
    String name;
    SubstituteLogger logger;
    Queue<SubstituteLoggingEvent> eventQueue;

    public EventRecodingLogger(SubstituteLogger substituteLogger, Queue<SubstituteLoggingEvent> queue) {
        this.logger = substituteLogger;
        this.name = substituteLogger.getName();
        this.eventQueue = queue;
    }

    @Override
    public String getName() {
        return this.name;
    }

    private void recordEvent(Level level, String string, Object[] objectArray, Throwable throwable) {
        this.recordEvent(level, null, string, objectArray, throwable);
    }

    private void recordEvent(Level level, Marker marker, String string, Object[] objectArray, Throwable throwable) {
        SubstituteLoggingEvent substituteLoggingEvent = new SubstituteLoggingEvent();
        substituteLoggingEvent.setTimeStamp(System.currentTimeMillis());
        substituteLoggingEvent.setLevel(level);
        substituteLoggingEvent.setLogger(this.logger);
        substituteLoggingEvent.setLoggerName(this.name);
        substituteLoggingEvent.setMarker(marker);
        substituteLoggingEvent.setMessage(string);
        substituteLoggingEvent.setArgumentArray(objectArray);
        substituteLoggingEvent.setThrowable(throwable);
        substituteLoggingEvent.setThreadName(Thread.currentThread().getName());
        this.eventQueue.add(substituteLoggingEvent);
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public void trace(String string) {
        this.recordEvent(Level.TRACE, string, null, null);
    }

    @Override
    public void trace(String string, Object object) {
        this.recordEvent(Level.TRACE, string, new Object[]{object}, null);
    }

    @Override
    public void trace(String string, Object object, Object object2) {
        this.recordEvent(Level.TRACE, string, new Object[]{object, object2}, null);
    }

    @Override
    public void trace(String string, Object ... objectArray) {
        this.recordEvent(Level.TRACE, string, objectArray, null);
    }

    @Override
    public void trace(String string, Throwable throwable) {
        this.recordEvent(Level.TRACE, string, null, throwable);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return true;
    }

    @Override
    public void trace(Marker marker, String string) {
        this.recordEvent(Level.TRACE, marker, string, null, null);
    }

    @Override
    public void trace(Marker marker, String string, Object object) {
        this.recordEvent(Level.TRACE, marker, string, new Object[]{object}, null);
    }

    @Override
    public void trace(Marker marker, String string, Object object, Object object2) {
        this.recordEvent(Level.TRACE, marker, string, new Object[]{object, object2}, null);
    }

    @Override
    public void trace(Marker marker, String string, Object ... objectArray) {
        this.recordEvent(Level.TRACE, marker, string, objectArray, null);
    }

    @Override
    public void trace(Marker marker, String string, Throwable throwable) {
        this.recordEvent(Level.TRACE, marker, string, null, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public void debug(String string) {
        this.recordEvent(Level.TRACE, string, null, null);
    }

    @Override
    public void debug(String string, Object object) {
        this.recordEvent(Level.DEBUG, string, new Object[]{object}, null);
    }

    @Override
    public void debug(String string, Object object, Object object2) {
        this.recordEvent(Level.DEBUG, string, new Object[]{object, object2}, null);
    }

    @Override
    public void debug(String string, Object ... objectArray) {
        this.recordEvent(Level.DEBUG, string, objectArray, null);
    }

    @Override
    public void debug(String string, Throwable throwable) {
        this.recordEvent(Level.DEBUG, string, null, throwable);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return true;
    }

    @Override
    public void debug(Marker marker, String string) {
        this.recordEvent(Level.DEBUG, marker, string, null, null);
    }

    @Override
    public void debug(Marker marker, String string, Object object) {
        this.recordEvent(Level.DEBUG, marker, string, new Object[]{object}, null);
    }

    @Override
    public void debug(Marker marker, String string, Object object, Object object2) {
        this.recordEvent(Level.DEBUG, marker, string, new Object[]{object, object2}, null);
    }

    @Override
    public void debug(Marker marker, String string, Object ... objectArray) {
        this.recordEvent(Level.DEBUG, marker, string, objectArray, null);
    }

    @Override
    public void debug(Marker marker, String string, Throwable throwable) {
        this.recordEvent(Level.DEBUG, marker, string, null, throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String string) {
        this.recordEvent(Level.INFO, string, null, null);
    }

    @Override
    public void info(String string, Object object) {
        this.recordEvent(Level.INFO, string, new Object[]{object}, null);
    }

    @Override
    public void info(String string, Object object, Object object2) {
        this.recordEvent(Level.INFO, string, new Object[]{object, object2}, null);
    }

    @Override
    public void info(String string, Object ... objectArray) {
        this.recordEvent(Level.INFO, string, objectArray, null);
    }

    @Override
    public void info(String string, Throwable throwable) {
        this.recordEvent(Level.INFO, string, null, throwable);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return true;
    }

    @Override
    public void info(Marker marker, String string) {
        this.recordEvent(Level.INFO, marker, string, null, null);
    }

    @Override
    public void info(Marker marker, String string, Object object) {
        this.recordEvent(Level.INFO, marker, string, new Object[]{object}, null);
    }

    @Override
    public void info(Marker marker, String string, Object object, Object object2) {
        this.recordEvent(Level.INFO, marker, string, new Object[]{object, object2}, null);
    }

    @Override
    public void info(Marker marker, String string, Object ... objectArray) {
        this.recordEvent(Level.INFO, marker, string, objectArray, null);
    }

    @Override
    public void info(Marker marker, String string, Throwable throwable) {
        this.recordEvent(Level.INFO, marker, string, null, throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String string) {
        this.recordEvent(Level.WARN, string, null, null);
    }

    @Override
    public void warn(String string, Object object) {
        this.recordEvent(Level.WARN, string, new Object[]{object}, null);
    }

    @Override
    public void warn(String string, Object object, Object object2) {
        this.recordEvent(Level.WARN, string, new Object[]{object, object2}, null);
    }

    @Override
    public void warn(String string, Object ... objectArray) {
        this.recordEvent(Level.WARN, string, objectArray, null);
    }

    @Override
    public void warn(String string, Throwable throwable) {
        this.recordEvent(Level.WARN, string, null, throwable);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return true;
    }

    @Override
    public void warn(Marker marker, String string) {
        this.recordEvent(Level.WARN, string, null, null);
    }

    @Override
    public void warn(Marker marker, String string, Object object) {
        this.recordEvent(Level.WARN, string, new Object[]{object}, null);
    }

    @Override
    public void warn(Marker marker, String string, Object object, Object object2) {
        this.recordEvent(Level.WARN, marker, string, new Object[]{object, object2}, null);
    }

    @Override
    public void warn(Marker marker, String string, Object ... objectArray) {
        this.recordEvent(Level.WARN, marker, string, objectArray, null);
    }

    @Override
    public void warn(Marker marker, String string, Throwable throwable) {
        this.recordEvent(Level.WARN, marker, string, null, throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(String string) {
        this.recordEvent(Level.ERROR, string, null, null);
    }

    @Override
    public void error(String string, Object object) {
        this.recordEvent(Level.ERROR, string, new Object[]{object}, null);
    }

    @Override
    public void error(String string, Object object, Object object2) {
        this.recordEvent(Level.ERROR, string, new Object[]{object, object2}, null);
    }

    @Override
    public void error(String string, Object ... objectArray) {
        this.recordEvent(Level.ERROR, string, objectArray, null);
    }

    @Override
    public void error(String string, Throwable throwable) {
        this.recordEvent(Level.ERROR, string, null, throwable);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return true;
    }

    @Override
    public void error(Marker marker, String string) {
        this.recordEvent(Level.ERROR, marker, string, null, null);
    }

    @Override
    public void error(Marker marker, String string, Object object) {
        this.recordEvent(Level.ERROR, marker, string, new Object[]{object}, null);
    }

    @Override
    public void error(Marker marker, String string, Object object, Object object2) {
        this.recordEvent(Level.ERROR, marker, string, new Object[]{object, object2}, null);
    }

    @Override
    public void error(Marker marker, String string, Object ... objectArray) {
        this.recordEvent(Level.ERROR, marker, string, objectArray, null);
    }

    @Override
    public void error(Marker marker, String string, Throwable throwable) {
        this.recordEvent(Level.ERROR, marker, string, null, throwable);
    }
}

