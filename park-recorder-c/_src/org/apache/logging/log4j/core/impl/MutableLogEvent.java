/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.async.InternalAsyncUtil;
import org.apache.logging.log4j.core.impl.ContextDataFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.impl.MementoMessage;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.core.time.MutableInstant;
import org.apache.logging.log4j.core.util.Clock;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.NanoClock;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterConsumer;
import org.apache.logging.log4j.message.ParameterVisitable;
import org.apache.logging.log4j.message.ReusableMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.message.TimestampMessage;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.apache.logging.log4j.util.StringBuilders;
import org.apache.logging.log4j.util.StringMap;

public class MutableLogEvent
implements LogEvent,
ParameterVisitable,
ReusableMessage {
    private static final Message EMPTY = new SimpleMessage("");
    private int threadPriority;
    private long threadId;
    private final MutableInstant instant = new MutableInstant();
    private long nanoTime;
    private short parameterCount;
    private boolean includeLocation;
    private boolean endOfBatch = false;
    private Level level;
    private String threadName;
    private String loggerName;
    private Message message;
    private String messageFormat;
    private StringBuilder messageText;
    private Object[] parameters;
    private Throwable thrown;
    private ThrowableProxy thrownProxy;
    private StringMap contextData = ContextDataFactory.createContextData();
    private Marker marker;
    private String loggerFqcn;
    private StackTraceElement source;
    private ThreadContext.ContextStack contextStack;
    transient boolean reserved = false;

    public MutableLogEvent() {
        this(null, null);
    }

    public MutableLogEvent(StringBuilder stringBuilder, Object[] objectArray) {
        this.messageText = stringBuilder;
        this.parameters = objectArray;
    }

    @Override
    public Log4jLogEvent toImmutable() {
        return this.createMemento();
    }

    public void initFrom(LogEvent logEvent) {
        this.loggerFqcn = logEvent.getLoggerFqcn();
        this.marker = logEvent.getMarker();
        this.level = logEvent.getLevel();
        this.loggerName = logEvent.getLoggerName();
        this.thrown = logEvent.getThrown();
        this.thrownProxy = logEvent.getThrownProxy();
        this.instant.initFrom(logEvent.getInstant());
        this.contextData.putAll(logEvent.getContextData());
        this.contextStack = logEvent.getContextStack();
        this.source = logEvent.isIncludeLocation() ? logEvent.getSource() : null;
        this.threadId = logEvent.getThreadId();
        this.threadName = logEvent.getThreadName();
        this.threadPriority = logEvent.getThreadPriority();
        this.endOfBatch = logEvent.isEndOfBatch();
        this.includeLocation = logEvent.isIncludeLocation();
        this.nanoTime = logEvent.getNanoTime();
        this.setMessage(logEvent.getMessage());
    }

    public void clear() {
        this.loggerFqcn = null;
        this.marker = null;
        this.level = null;
        this.loggerName = null;
        this.message = null;
        this.messageFormat = null;
        this.thrown = null;
        this.thrownProxy = null;
        this.source = null;
        if (this.contextData != null) {
            if (this.contextData.isFrozen()) {
                this.contextData = null;
            } else {
                this.contextData.clear();
            }
        }
        this.contextStack = null;
        StringBuilders.trimToMaxSize(this.messageText, Constants.MAX_REUSABLE_MESSAGE_SIZE);
        if (this.parameters != null) {
            Arrays.fill(this.parameters, null);
        }
    }

    @Override
    public String getLoggerFqcn() {
        return this.loggerFqcn;
    }

    public void setLoggerFqcn(String string) {
        this.loggerFqcn = string;
    }

    @Override
    public Marker getMarker() {
        return this.marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    @Override
    public Level getLevel() {
        if (this.level == null) {
            this.level = Level.OFF;
        }
        return this.level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public String getLoggerName() {
        return this.loggerName;
    }

    public void setLoggerName(String string) {
        this.loggerName = string;
    }

    @Override
    public Message getMessage() {
        if (this.message == null) {
            if (this.messageText == null) {
                return EMPTY;
            }
            return this;
        }
        return this.message;
    }

    public void setMessage(Message message) {
        if (message instanceof ReusableMessage) {
            ReusableMessage reusableMessage = (ReusableMessage)message;
            reusableMessage.formatTo(this.getMessageTextForWriting());
            this.messageFormat = message.getFormat();
            this.parameters = reusableMessage.swapParameters(this.parameters == null ? new Object[10] : this.parameters);
            this.parameterCount = reusableMessage.getParameterCount();
            return;
        }
        this.message = InternalAsyncUtil.makeMessageImmutable(message);
    }

    private StringBuilder getMessageTextForWriting() {
        if (this.messageText == null) {
            this.messageText = new StringBuilder(Constants.INITIAL_REUSABLE_MESSAGE_SIZE);
        }
        this.messageText.setLength(0);
        return this.messageText;
    }

    @Override
    public String getFormattedMessage() {
        return this.messageText.toString();
    }

    @Override
    public String getFormat() {
        return this.messageFormat;
    }

    @Override
    public Object[] getParameters() {
        if (this.parameters == null) {
            return null;
        }
        return Arrays.copyOf(this.parameters, (int)this.parameterCount);
    }

    @Override
    public <S> void forEachParameter(ParameterConsumer<S> parameterConsumer, S s) {
        if (this.parameters != null) {
            for (short s2 = 0; s2 < this.parameterCount; s2 = (short)(s2 + 1)) {
                parameterConsumer.accept(this.parameters[s2], s2, s);
            }
        }
    }

    @Override
    public Throwable getThrowable() {
        return this.getThrown();
    }

    @Override
    public void formatTo(StringBuilder stringBuilder) {
        stringBuilder.append((CharSequence)this.messageText);
    }

    @Override
    public Object[] swapParameters(Object[] objectArray) {
        Object[] objectArray2 = this.parameters;
        this.parameters = objectArray;
        return objectArray2;
    }

    @Override
    public short getParameterCount() {
        return this.parameterCount;
    }

    @Override
    public Message memento() {
        if (this.message == null) {
            this.message = new MementoMessage(String.valueOf(this.messageText), this.messageFormat, this.getParameters());
        }
        return this.message;
    }

    @Override
    public Throwable getThrown() {
        return this.thrown;
    }

    public void setThrown(Throwable throwable) {
        this.thrown = throwable;
    }

    void initTime(Clock clock, NanoClock nanoClock) {
        if (this.message instanceof TimestampMessage) {
            this.instant.initFromEpochMilli(((TimestampMessage)((Object)this.message)).getTimestamp(), 0);
        } else {
            this.instant.initFrom(clock);
        }
        this.nanoTime = nanoClock.nanoTime();
    }

    @Override
    public long getTimeMillis() {
        return this.instant.getEpochMillisecond();
    }

    public void setTimeMillis(long l) {
        this.instant.initFromEpochMilli(l, 0);
    }

    @Override
    public Instant getInstant() {
        return this.instant;
    }

    @Override
    public ThrowableProxy getThrownProxy() {
        if (this.thrownProxy == null && this.thrown != null) {
            this.thrownProxy = new ThrowableProxy(this.thrown);
        }
        return this.thrownProxy;
    }

    public void setSource(StackTraceElement stackTraceElement) {
        this.source = stackTraceElement;
    }

    @Override
    public StackTraceElement getSource() {
        if (this.source != null) {
            return this.source;
        }
        if (this.loggerFqcn == null || !this.includeLocation) {
            return null;
        }
        this.source = StackLocatorUtil.calcLocation(this.loggerFqcn);
        return this.source;
    }

    @Override
    public ReadOnlyStringMap getContextData() {
        return this.contextData;
    }

    @Override
    public Map<String, String> getContextMap() {
        return this.contextData.toMap();
    }

    public void setContextData(StringMap stringMap) {
        this.contextData = stringMap;
    }

    @Override
    public ThreadContext.ContextStack getContextStack() {
        return this.contextStack;
    }

    public void setContextStack(ThreadContext.ContextStack contextStack) {
        this.contextStack = contextStack;
    }

    @Override
    public long getThreadId() {
        return this.threadId;
    }

    public void setThreadId(long l) {
        this.threadId = l;
    }

    @Override
    public String getThreadName() {
        return this.threadName;
    }

    public void setThreadName(String string) {
        this.threadName = string;
    }

    @Override
    public int getThreadPriority() {
        return this.threadPriority;
    }

    public void setThreadPriority(int n) {
        this.threadPriority = n;
    }

    @Override
    public boolean isIncludeLocation() {
        return this.includeLocation;
    }

    @Override
    public void setIncludeLocation(boolean bl) {
        this.includeLocation = bl;
    }

    @Override
    public boolean isEndOfBatch() {
        return this.endOfBatch;
    }

    @Override
    public void setEndOfBatch(boolean bl) {
        this.endOfBatch = bl;
    }

    @Override
    public long getNanoTime() {
        return this.nanoTime;
    }

    public void setNanoTime(long l) {
        this.nanoTime = l;
    }

    protected Object writeReplace() {
        MutableLogEvent mutableLogEvent = this;
        return new Log4jLogEvent.LogEventProxy(mutableLogEvent, mutableLogEvent.includeLocation);
    }

    private void readObject(ObjectInputStream objectInputStream) {
        throw new InvalidObjectException("Proxy required");
    }

    public Log4jLogEvent createMemento() {
        MutableLogEvent mutableLogEvent = this;
        return Log4jLogEvent.deserialize(Log4jLogEvent.serialize(mutableLogEvent, mutableLogEvent.includeLocation));
    }

    public void initializeBuilder(Log4jLogEvent.Builder builder) {
        builder.setContextData(this.contextData).setContextStack(this.contextStack).setEndOfBatch(this.endOfBatch).setIncludeLocation(this.includeLocation).setLevel(this.getLevel()).setLoggerFqcn(this.loggerFqcn).setLoggerName(this.loggerName).setMarker(this.marker).setMessage(this.memento()).setNanoTime(this.nanoTime).setSource(this.source).setThreadId(this.threadId).setThreadName(this.threadName).setThreadPriority(this.threadPriority).setThrown(this.getThrown()).setThrownProxy(this.thrownProxy).setInstant(this.instant);
    }
}

