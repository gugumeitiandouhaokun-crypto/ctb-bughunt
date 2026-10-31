/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.Supplier;

public interface LogBuilder {
    public static final LogBuilder NOOP = new LogBuilder(){};

    default public LogBuilder withMarker(Marker marker) {
        return this;
    }

    default public LogBuilder withThrowable(Throwable throwable) {
        return this;
    }

    default public LogBuilder withLocation() {
        return this;
    }

    default public LogBuilder withLocation(StackTraceElement stackTraceElement) {
        return this;
    }

    default public void log(CharSequence charSequence) {
    }

    default public void log(String string) {
    }

    default public void log(String string, Object ... objectArray) {
    }

    default public void log(String string, Supplier<?> ... supplierArray) {
    }

    default public void log(Message message) {
    }

    default public void log(Supplier<Message> supplier) {
    }

    default public void log(Object object) {
    }

    default public void log(String string, Object object) {
    }

    default public void log(String string, Object object, Object object2) {
    }

    default public void log(String string, Object object, Object object2, Object object3) {
    }

    default public void log(String string, Object object, Object object2, Object object3, Object object4) {
    }

    default public void log(String string, Object object, Object object2, Object object3, Object object4, Object object5) {
    }

    default public void log(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
    }

    default public void log(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
    }

    default public void log(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
    }

    default public void log(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
    }

    default public void log(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
    }
}

