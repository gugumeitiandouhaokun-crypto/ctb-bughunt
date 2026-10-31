/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.internal;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LambdaUtil;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.apache.logging.log4j.util.Supplier;

public class DefaultLogBuilder
implements LogBuilder {
    private static Message EMPTY_MESSAGE = new SimpleMessage("");
    private static final String FQCN = DefaultLogBuilder.class.getName();
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final Logger logger;
    private Level level;
    private Marker marker;
    private Throwable throwable;
    private StackTraceElement location;
    private volatile boolean inUse;
    private long threadId;

    public DefaultLogBuilder(Logger logger, Level level) {
        this.logger = logger;
        this.level = level;
        this.threadId = Thread.currentThread().getId();
        this.inUse = true;
    }

    public DefaultLogBuilder(Logger logger) {
        this.logger = logger;
        this.inUse = false;
        this.threadId = Thread.currentThread().getId();
    }

    public LogBuilder reset(Level level) {
        this.inUse = true;
        this.level = level;
        this.marker = null;
        this.throwable = null;
        this.location = null;
        return this;
    }

    @Override
    public LogBuilder withMarker(Marker marker) {
        this.marker = marker;
        return this;
    }

    @Override
    public LogBuilder withThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    @Override
    public LogBuilder withLocation() {
        this.location = StackLocatorUtil.getStackTraceElement(2);
        return this;
    }

    @Override
    public LogBuilder withLocation(StackTraceElement stackTraceElement) {
        this.location = stackTraceElement;
        return this;
    }

    public boolean isInUse() {
        return this.inUse;
    }

    @Override
    public void log(Message message) {
        if (this.isValid()) {
            this.logMessage(message);
        }
    }

    @Override
    public void log(CharSequence charSequence) {
        if (this.isValid()) {
            DefaultLogBuilder defaultLogBuilder = this;
            defaultLogBuilder.logMessage(defaultLogBuilder.logger.getMessageFactory().newMessage(charSequence));
        }
    }

    @Override
    public void log(String string) {
        if (this.isValid()) {
            DefaultLogBuilder defaultLogBuilder = this;
            defaultLogBuilder.logMessage(defaultLogBuilder.logger.getMessageFactory().newMessage(string));
        }
    }

    @Override
    public void log(String string, Object ... objectArray) {
        if (this.isValid()) {
            DefaultLogBuilder defaultLogBuilder = this;
            defaultLogBuilder.logMessage(defaultLogBuilder.logger.getMessageFactory().newMessage(string, objectArray));
        }
    }

    @Override
    public void log(String string, Supplier<?> ... supplierArray) {
        if (this.isValid()) {
            DefaultLogBuilder defaultLogBuilder = this;
            defaultLogBuilder.logMessage(defaultLogBuilder.logger.getMessageFactory().newMessage(string, LambdaUtil.getAll(supplierArray)));
        }
    }

    @Override
    public void log(Supplier<Message> supplier) {
        if (this.isValid()) {
            this.logMessage(supplier.get());
        }
    }

    @Override
    public void log(Object object) {
        if (this.isValid()) {
            DefaultLogBuilder defaultLogBuilder = this;
            defaultLogBuilder.logMessage(defaultLogBuilder.logger.getMessageFactory().newMessage(object));
        }
    }

    @Override
    public void log(String string, Object object) {
        if (this.isValid()) {
            DefaultLogBuilder defaultLogBuilder = this;
            defaultLogBuilder.logMessage(defaultLogBuilder.logger.getMessageFactory().newMessage(string, object));
        }
    }

    @Override
    public void log(String string, Object object, Object object2) {
        if (this.isValid()) {
            DefaultLogBuilder defaultLogBuilder = this;
            defaultLogBuilder.logMessage(defaultLogBuilder.logger.getMessageFactory().newMessage(string, object, object2));
        }
    }

    @Override
    public void log(String string, Object object, Object object2, Object object3) {
        if (this.isValid()) {
            DefaultLogBuilder defaultLogBuilder = this;
            defaultLogBuilder.logMessage(defaultLogBuilder.logger.getMessageFactory().newMessage(string, object, object2, object3));
        }
    }

    @Override
    public void log(String string, Object object, Object object2, Object object3, Object object4) {
        if (this.isValid()) {
            DefaultLogBuilder defaultLogBuilder = this;
            defaultLogBuilder.logMessage(defaultLogBuilder.logger.getMessageFactory().newMessage(string, object, object2, object3, object4));
        }
    }

    @Override
    public void log(String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        if (this.isValid()) {
            DefaultLogBuilder defaultLogBuilder = this;
            defaultLogBuilder.logMessage(defaultLogBuilder.logger.getMessageFactory().newMessage(string, object, object2, object3, object4, object5));
        }
    }

    @Override
    public void log(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        if (this.isValid()) {
            DefaultLogBuilder defaultLogBuilder = this;
            defaultLogBuilder.logMessage(defaultLogBuilder.logger.getMessageFactory().newMessage(string, object, object2, object3, object4, object5, object6));
        }
    }

    @Override
    public void log(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        if (this.isValid()) {
            DefaultLogBuilder defaultLogBuilder = this;
            defaultLogBuilder.logMessage(defaultLogBuilder.logger.getMessageFactory().newMessage(string, object, object2, object3, object4, object5, object6, object7));
        }
    }

    @Override
    public void log(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        if (this.isValid()) {
            DefaultLogBuilder defaultLogBuilder = this;
            defaultLogBuilder.logMessage(defaultLogBuilder.logger.getMessageFactory().newMessage(string, object, object2, object3, object4, object5, object6, object7, object8));
        }
    }

    @Override
    public void log(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        if (this.isValid()) {
            DefaultLogBuilder defaultLogBuilder = this;
            defaultLogBuilder.logMessage(defaultLogBuilder.logger.getMessageFactory().newMessage(string, object, object2, object3, object4, object5, object6, object7, object8, object9));
        }
    }

    @Override
    public void log(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        if (this.isValid()) {
            DefaultLogBuilder defaultLogBuilder = this;
            defaultLogBuilder.logMessage(defaultLogBuilder.logger.getMessageFactory().newMessage(string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10));
        }
    }

    private void logMessage(Message message) {
        try {
            this.logger.logMessage(this.level, this.marker, FQCN, this.location, message, this.throwable);
            return;
        }
        finally {
            this.inUse = false;
        }
    }

    private boolean isValid() {
        if (!this.inUse) {
            LOGGER.warn("Attempt to reuse LogBuilder was ignored. {}", (Object)StackLocatorUtil.getCallerClass(2));
            return false;
        }
        if (this.threadId != Thread.currentThread().getId()) {
            LOGGER.warn("LogBuilder can only be used on the owning thread. {}", (Object)StackLocatorUtil.getCallerClass(2));
            return false;
        }
        return true;
    }
}

