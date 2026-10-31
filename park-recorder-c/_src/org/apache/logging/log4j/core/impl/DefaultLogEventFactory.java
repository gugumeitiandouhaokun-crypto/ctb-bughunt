/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.LocationAwareLogEventFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.impl.LogEventFactory;
import org.apache.logging.log4j.message.Message;

public class DefaultLogEventFactory
implements LocationAwareLogEventFactory,
LogEventFactory {
    private static final DefaultLogEventFactory instance = new DefaultLogEventFactory();

    public static DefaultLogEventFactory getInstance() {
        return instance;
    }

    @Override
    public LogEvent createEvent(String string, Marker marker, String string2, Level level, Message message, List<Property> list, Throwable throwable) {
        return new Log4jLogEvent(string, marker, string2, level, message, list, throwable);
    }

    @Override
    public LogEvent createEvent(String string, Marker marker, String string2, StackTraceElement stackTraceElement, Level level, Message message, List<Property> list, Throwable throwable) {
        return new Log4jLogEvent(string, marker, string2, stackTraceElement, level, message, list, throwable);
    }
}

