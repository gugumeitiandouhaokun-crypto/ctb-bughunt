/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.MarshalledObject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.ContextDataInjector;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.async.RingBufferLogEvent;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.ContextDataFactory;
import org.apache.logging.log4j.core.impl.ContextDataInjectorFactory;
import org.apache.logging.log4j.core.impl.MementoMessage;
import org.apache.logging.log4j.core.impl.MutableLogEvent;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.core.time.MutableInstant;
import org.apache.logging.log4j.core.util.Clock;
import org.apache.logging.log4j.core.util.ClockFactory;
import org.apache.logging.log4j.core.util.DummyNanoClock;
import org.apache.logging.log4j.core.util.NanoClock;
import org.apache.logging.log4j.message.LoggerNameAwareMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ReusableMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.message.TimestampMessage;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.apache.logging.log4j.util.StringMap;

public class Log4jLogEvent
implements LogEvent {
    private static final long serialVersionUID = -8393305700508709443L;
    private static final Clock CLOCK = ClockFactory.getClock();
    private static volatile NanoClock nanoClock = new DummyNanoClock();
    private static final ContextDataInjector CONTEXT_DATA_INJECTOR = ContextDataInjectorFactory.createInjector();
    private final String loggerFqcn;
    private final Marker marker;
    private final Level level;
    private final String loggerName;
    private Message message;
    private final MutableInstant instant = new MutableInstant();
    private final transient Throwable thrown;
    private ThrowableProxy thrownProxy;
    private final StringMap contextData;
    private final ThreadContext.ContextStack contextStack;
    private long threadId;
    private String threadName;
    private int threadPriority;
    private StackTraceElement source;
    private boolean includeLocation;
    private boolean endOfBatch = false;
    private final transient long nanoTime;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Log4jLogEvent() {
        this("", null, "", null, null, null, null, null, null, 0L, null, 0, null, CLOCK, nanoClock.nanoTime());
    }

    @Deprecated
    public Log4jLogEvent(long l) {
        this("", null, "", null, null, null, null, null, null, 0L, null, 0, null, l, 0, nanoClock.nanoTime());
    }

    @Deprecated
    public Log4jLogEvent(String string, Marker marker, String string2, Level level, Message message, Throwable throwable) {
        this(string, marker, string2, level, message, null, throwable);
    }

    public Log4jLogEvent(String string, Marker marker, String string2, Level level, Message message, List<Property> list, Throwable throwable) {
        this(string, marker, string2, level, message, throwable, null, Log4jLogEvent.createContextData(list), ThreadContext.getDepth() == 0 ? null : ThreadContext.cloneStack(), 0L, null, 0, null, CLOCK, nanoClock.nanoTime());
    }

    public Log4jLogEvent(String string, Marker marker, String string2, StackTraceElement stackTraceElement, Level level, Message message, List<Property> list, Throwable throwable) {
        this(string, marker, string2, level, message, throwable, null, Log4jLogEvent.createContextData(list), ThreadContext.getDepth() == 0 ? null : ThreadContext.cloneStack(), 0L, null, 0, stackTraceElement, CLOCK, nanoClock.nanoTime());
    }

    @Deprecated
    public Log4jLogEvent(String string, Marker marker, String string2, Level level, Message message, Throwable throwable, Map<String, String> map, ThreadContext.ContextStack contextStack, String string3, StackTraceElement stackTraceElement, long l) {
        this(string, marker, string2, level, message, throwable, null, Log4jLogEvent.createContextData(map), contextStack, 0L, string3, 0, stackTraceElement, l, 0, nanoClock.nanoTime());
    }

    @Deprecated
    public static Log4jLogEvent createEvent(String string, Marker marker, String string2, Level level, Message message, Throwable throwable, ThrowableProxy throwableProxy, Map<String, String> map, ThreadContext.ContextStack contextStack, String string3, StackTraceElement stackTraceElement, long l) {
        return new Log4jLogEvent(string, marker, string2, level, message, throwable, throwableProxy, Log4jLogEvent.createContextData(map), contextStack, 0L, string3, 0, stackTraceElement, l, 0, nanoClock.nanoTime());
    }

    private Log4jLogEvent(String string, Marker marker, String string2, Level level, Message message, Throwable throwable, ThrowableProxy throwableProxy, StringMap stringMap, ThreadContext.ContextStack contextStack, long l, String string3, int n, StackTraceElement stackTraceElement, long l2, int n2, long l3) {
        this(string, marker, string2, level, message, throwable, throwableProxy, stringMap, contextStack, l, string3, n, stackTraceElement, l3);
        long l4 = message instanceof TimestampMessage ? ((TimestampMessage)((Object)message)).getTimestamp() : l2;
        this.instant.initFromEpochMilli(l4, n2);
    }

    private Log4jLogEvent(String string, Marker marker, String string2, Level level, Message message, Throwable throwable, ThrowableProxy throwableProxy, StringMap stringMap, ThreadContext.ContextStack contextStack, long l, String string3, int n, StackTraceElement stackTraceElement, Clock clock, long l2) {
        this(string, marker, string2, level, message, throwable, throwableProxy, stringMap, contextStack, l, string3, n, stackTraceElement, l2);
        if (message instanceof TimestampMessage) {
            this.instant.initFromEpochMilli(((TimestampMessage)((Object)message)).getTimestamp(), 0);
            return;
        }
        this.instant.initFrom(clock);
    }

    private Log4jLogEvent(String string, Marker marker, String string2, Level level, Message message, Throwable throwable, ThrowableProxy throwableProxy, StringMap stringMap, ThreadContext.ContextStack contextStack, long l, String string3, int n, StackTraceElement stackTraceElement, long l2) {
        this.loggerName = string;
        this.marker = marker;
        this.loggerFqcn = string2;
        this.level = level == null ? Level.OFF : level;
        this.message = message;
        this.thrown = throwable;
        this.thrownProxy = throwableProxy;
        this.contextData = stringMap == null ? ContextDataFactory.createContextData() : stringMap;
        this.contextStack = contextStack == null ? ThreadContext.EMPTY_STACK : contextStack;
        this.threadId = l;
        this.threadName = string3;
        this.threadPriority = n;
        this.source = stackTraceElement;
        if (message instanceof LoggerNameAwareMessage) {
            ((LoggerNameAwareMessage)((Object)message)).setLoggerName(string);
        }
        this.nanoTime = l2;
    }

    private static StringMap createContextData(Map<String, String> object) {
        StringMap stringMap = ContextDataFactory.createContextData();
        if (object != null) {
            for (Map.Entry entry : object.entrySet()) {
                stringMap.putValue((String)entry.getKey(), entry.getValue());
            }
        }
        return stringMap;
    }

    private static StringMap createContextData(List<Property> list) {
        StringMap stringMap = ContextDataFactory.createContextData();
        return CONTEXT_DATA_INJECTOR.injectContextData(list, stringMap);
    }

    public static NanoClock getNanoClock() {
        return nanoClock;
    }

    public static void setNanoClock(NanoClock nanoClock) {
        Log4jLogEvent.nanoClock = Objects.requireNonNull(nanoClock, "NanoClock must be non-null");
        StatusLogger.getLogger().trace("Using {} for nanosecond timestamps.", (Object)nanoClock.getClass().getSimpleName());
    }

    public Builder asBuilder() {
        return new Builder(this);
    }

    @Override
    public Log4jLogEvent toImmutable() {
        if (this.getMessage() instanceof ReusableMessage) {
            this.makeMessageImmutable();
        }
        return this;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    @Override
    public String getLoggerName() {
        return this.loggerName;
    }

    @Override
    public Message getMessage() {
        return this.message;
    }

    public void makeMessageImmutable() {
        this.message = new MementoMessage(this.message.getFormattedMessage(), this.message.getFormat(), this.message.getParameters());
    }

    @Override
    public long getThreadId() {
        if (this.threadId == 0L) {
            this.threadId = Thread.currentThread().getId();
        }
        return this.threadId;
    }

    @Override
    public String getThreadName() {
        if (this.threadName == null) {
            this.threadName = Thread.currentThread().getName();
        }
        return this.threadName;
    }

    @Override
    public int getThreadPriority() {
        if (this.threadPriority == 0) {
            this.threadPriority = Thread.currentThread().getPriority();
        }
        return this.threadPriority;
    }

    @Override
    public long getTimeMillis() {
        return this.instant.getEpochMillisecond();
    }

    @Override
    public Instant getInstant() {
        return this.instant;
    }

    @Override
    public Throwable getThrown() {
        return this.thrown;
    }

    @Override
    public ThrowableProxy getThrownProxy() {
        if (this.thrownProxy == null && this.thrown != null) {
            this.thrownProxy = new ThrowableProxy(this.thrown);
        }
        return this.thrownProxy;
    }

    @Override
    public Marker getMarker() {
        return this.marker;
    }

    @Override
    public String getLoggerFqcn() {
        return this.loggerFqcn;
    }

    @Override
    public ReadOnlyStringMap getContextData() {
        return this.contextData;
    }

    @Override
    public Map<String, String> getContextMap() {
        return this.contextData.toMap();
    }

    @Override
    public ThreadContext.ContextStack getContextStack() {
        return this.contextStack;
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

    protected Object writeReplace() {
        this.getThrownProxy();
        Log4jLogEvent log4jLogEvent = this;
        return new LogEventProxy(log4jLogEvent, log4jLogEvent.includeLocation);
    }

    public static Serializable serialize(LogEvent logEvent, boolean bl) {
        if (logEvent instanceof Log4jLogEvent) {
            logEvent.getThrownProxy();
            return new LogEventProxy((Log4jLogEvent)logEvent, bl);
        }
        return new LogEventProxy(logEvent, bl);
    }

    public static Serializable serialize(Log4jLogEvent log4jLogEvent, boolean bl) {
        log4jLogEvent.getThrownProxy();
        return new LogEventProxy(log4jLogEvent, bl);
    }

    public static boolean canDeserialize(Serializable serializable) {
        return serializable instanceof LogEventProxy;
    }

    public static Log4jLogEvent deserialize(Serializable serializable) {
        Objects.requireNonNull(serializable, "Event cannot be null");
        if (serializable instanceof LogEventProxy) {
            serializable = (LogEventProxy)serializable;
            Log4jLogEvent log4jLogEvent = new Log4jLogEvent(((LogEventProxy)serializable).loggerName, ((LogEventProxy)serializable).marker, ((LogEventProxy)serializable).loggerFQCN, ((LogEventProxy)serializable).level, ((LogEventProxy)serializable).message, ((LogEventProxy)serializable).thrown, ((LogEventProxy)serializable).thrownProxy, ((LogEventProxy)serializable).contextData, ((LogEventProxy)serializable).contextStack, ((LogEventProxy)serializable).threadId, ((LogEventProxy)serializable).threadName, ((LogEventProxy)serializable).threadPriority, ((LogEventProxy)serializable).source, ((LogEventProxy)serializable).timeMillis, ((LogEventProxy)serializable).nanoOfMillisecond, ((LogEventProxy)serializable).nanoTime);
            log4jLogEvent.setEndOfBatch(((LogEventProxy)serializable).isEndOfBatch);
            log4jLogEvent.setIncludeLocation(((LogEventProxy)serializable).isLocationRequired);
            return log4jLogEvent;
        }
        throw new IllegalArgumentException("Event is not a serialized LogEvent: " + serializable.toString());
    }

    private void readObject(ObjectInputStream objectInputStream) {
        throw new InvalidObjectException("Proxy required");
    }

    public static LogEvent createMemento(LogEvent logEvent) {
        return new Builder(logEvent).build();
    }

    public static Log4jLogEvent createMemento(LogEvent logEvent, boolean bl) {
        return Log4jLogEvent.deserialize(Log4jLogEvent.serialize(logEvent, bl));
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        String string = this.loggerName.isEmpty() ? "root" : this.loggerName;
        stringBuilder.append("Logger=").append(string);
        stringBuilder.append(" Level=").append(this.level.name());
        stringBuilder.append(" Message=").append(this.message == null ? null : this.message.getFormattedMessage());
        return stringBuilder.toString();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        object = (Log4jLogEvent)object;
        if (this.endOfBatch != ((Log4jLogEvent)object).endOfBatch) {
            return false;
        }
        if (this.includeLocation != ((Log4jLogEvent)object).includeLocation) {
            return false;
        }
        if (!this.instant.equals(((Log4jLogEvent)object).instant)) {
            return false;
        }
        if (this.nanoTime != ((Log4jLogEvent)object).nanoTime) {
            return false;
        }
        if (this.loggerFqcn != null ? !this.loggerFqcn.equals(((Log4jLogEvent)object).loggerFqcn) : ((Log4jLogEvent)object).loggerFqcn != null) {
            return false;
        }
        if (this.level != null ? !this.level.equals(((Log4jLogEvent)object).level) : ((Log4jLogEvent)object).level != null) {
            return false;
        }
        if (this.source != null ? !this.source.equals(((Log4jLogEvent)object).source) : ((Log4jLogEvent)object).source != null) {
            return false;
        }
        if (this.marker != null ? !this.marker.equals(((Log4jLogEvent)object).marker) : ((Log4jLogEvent)object).marker != null) {
            return false;
        }
        if (this.contextData != null ? !this.contextData.equals(((Log4jLogEvent)object).contextData) : ((Log4jLogEvent)object).contextData != null) {
            return false;
        }
        if (!this.message.equals(((Log4jLogEvent)object).message)) {
            return false;
        }
        if (!this.loggerName.equals(((Log4jLogEvent)object).loggerName)) {
            return false;
        }
        if (this.contextStack != null ? !this.contextStack.equals(((Log4jLogEvent)object).contextStack) : ((Log4jLogEvent)object).contextStack != null) {
            return false;
        }
        if (this.threadId != ((Log4jLogEvent)object).threadId) {
            return false;
        }
        if (this.threadName != null ? !this.threadName.equals(((Log4jLogEvent)object).threadName) : ((Log4jLogEvent)object).threadName != null) {
            return false;
        }
        if (this.threadPriority != ((Log4jLogEvent)object).threadPriority) {
            return false;
        }
        if (this.thrown != null ? !this.thrown.equals(((Log4jLogEvent)object).thrown) : ((Log4jLogEvent)object).thrown != null) {
            return false;
        }
        return !(this.thrownProxy != null ? !this.thrownProxy.equals(((Log4jLogEvent)object).thrownProxy) : ((Log4jLogEvent)object).thrownProxy != null);
    }

    public int hashCode() {
        int n = this.loggerFqcn != null ? this.loggerFqcn.hashCode() : 0;
        n = 31 * n + (this.marker != null ? this.marker.hashCode() : 0);
        n = 31 * n + (this.level != null ? this.level.hashCode() : 0);
        n = 31 * n + this.loggerName.hashCode();
        n = 31 * n + this.message.hashCode();
        n = 31 * n + this.instant.hashCode();
        n = 31 * n + (int)(this.nanoTime ^ this.nanoTime >>> 32);
        n = 31 * n + (this.thrown != null ? this.thrown.hashCode() : 0);
        n = 31 * n + (this.thrownProxy != null ? this.thrownProxy.hashCode() : 0);
        n = 31 * n + (this.contextData != null ? this.contextData.hashCode() : 0);
        n = 31 * n + (this.contextStack != null ? this.contextStack.hashCode() : 0);
        n = 31 * n + (int)(this.threadId ^ this.threadId >>> 32);
        n = 31 * n + (this.threadName != null ? this.threadName.hashCode() : 0);
        n = 31 * n + (this.threadPriority ^ this.threadPriority >>> 32);
        n = 31 * n + (this.source != null ? this.source.hashCode() : 0);
        n = 31 * n + (this.includeLocation ? 1 : 0);
        return 31 * n + (this.endOfBatch ? 1 : 0);
    }

    static /* synthetic */ StringMap access$000(List list) {
        return Log4jLogEvent.createContextData(list);
    }

    static class LogEventProxy
    implements Serializable {
        private static final long serialVersionUID = -8634075037355293699L;
        private final String loggerFQCN;
        private final Marker marker;
        private final Level level;
        private final String loggerName;
        private final transient Message message;
        private MarshalledObject<Message> marshalledMessage;
        private String messageString;
        private final long timeMillis;
        private final int nanoOfMillisecond;
        private final transient Throwable thrown;
        private final ThrowableProxy thrownProxy;
        private final StringMap contextData;
        private final ThreadContext.ContextStack contextStack;
        private final long threadId;
        private final String threadName;
        private final int threadPriority;
        private final StackTraceElement source;
        private final boolean isLocationRequired;
        private final boolean isEndOfBatch;
        private final transient long nanoTime;

        public LogEventProxy(Log4jLogEvent log4jLogEvent, boolean bl) {
            this.loggerFQCN = log4jLogEvent.loggerFqcn;
            this.marker = log4jLogEvent.marker;
            this.level = log4jLogEvent.level;
            this.loggerName = log4jLogEvent.loggerName;
            this.message = log4jLogEvent.message instanceof ReusableMessage ? LogEventProxy.memento((ReusableMessage)log4jLogEvent.message) : log4jLogEvent.message;
            this.timeMillis = log4jLogEvent.instant.getEpochMillisecond();
            this.nanoOfMillisecond = log4jLogEvent.instant.getNanoOfMillisecond();
            this.thrown = log4jLogEvent.thrown;
            this.thrownProxy = log4jLogEvent.thrownProxy;
            this.contextData = log4jLogEvent.contextData;
            this.contextStack = log4jLogEvent.contextStack;
            this.source = bl ? log4jLogEvent.getSource() : null;
            this.threadId = log4jLogEvent.getThreadId();
            this.threadName = log4jLogEvent.getThreadName();
            this.threadPriority = log4jLogEvent.getThreadPriority();
            this.isLocationRequired = bl;
            this.isEndOfBatch = log4jLogEvent.endOfBatch;
            this.nanoTime = log4jLogEvent.nanoTime;
        }

        public LogEventProxy(LogEvent logEvent, boolean bl) {
            this.loggerFQCN = logEvent.getLoggerFqcn();
            this.marker = logEvent.getMarker();
            this.level = logEvent.getLevel();
            this.loggerName = logEvent.getLoggerName();
            Message message = logEvent.getMessage();
            this.message = message instanceof ReusableMessage ? LogEventProxy.memento((ReusableMessage)message) : message;
            this.timeMillis = logEvent.getInstant().getEpochMillisecond();
            this.nanoOfMillisecond = logEvent.getInstant().getNanoOfMillisecond();
            this.thrown = logEvent.getThrown();
            this.thrownProxy = logEvent.getThrownProxy();
            this.contextData = LogEventProxy.memento(logEvent.getContextData());
            this.contextStack = logEvent.getContextStack();
            this.source = bl ? logEvent.getSource() : null;
            this.threadId = logEvent.getThreadId();
            this.threadName = logEvent.getThreadName();
            this.threadPriority = logEvent.getThreadPriority();
            this.isLocationRequired = bl;
            this.isEndOfBatch = logEvent.isEndOfBatch();
            this.nanoTime = logEvent.getNanoTime();
        }

        private static Message memento(ReusableMessage reusableMessage) {
            return reusableMessage.memento();
        }

        private static StringMap memento(ReadOnlyStringMap readOnlyStringMap) {
            StringMap stringMap = ContextDataFactory.createContextData();
            stringMap.putAll(readOnlyStringMap);
            return stringMap;
        }

        private static MarshalledObject<Message> marshall(Message message) {
            try {
                return new MarshalledObject<Message>(message);
            }
            catch (Exception exception) {
                return null;
            }
        }

        private void writeObject(ObjectOutputStream objectOutputStream) {
            this.messageString = this.message.getFormattedMessage();
            this.marshalledMessage = LogEventProxy.marshall(this.message);
            objectOutputStream.defaultWriteObject();
        }

        protected Object readResolve() {
            Log4jLogEvent log4jLogEvent = new Log4jLogEvent(this.loggerName, this.marker, this.loggerFQCN, this.level, this.message(), this.thrown, this.thrownProxy, this.contextData, this.contextStack, this.threadId, this.threadName, this.threadPriority, this.source, this.timeMillis, this.nanoOfMillisecond, this.nanoTime);
            log4jLogEvent.setEndOfBatch(this.isEndOfBatch);
            log4jLogEvent.setIncludeLocation(this.isLocationRequired);
            return log4jLogEvent;
        }

        private Message message() {
            if (this.marshalledMessage != null) {
                try {
                    return this.marshalledMessage.get();
                }
                catch (Exception exception) {}
            }
            return new SimpleMessage(this.messageString);
        }
    }

    public static class Builder
    implements org.apache.logging.log4j.core.util.Builder<LogEvent> {
        private String loggerFqcn;
        private Marker marker;
        private Level level;
        private String loggerName;
        private Message message;
        private Throwable thrown;
        private final MutableInstant instant = new MutableInstant();
        private ThrowableProxy thrownProxy;
        private StringMap contextData = Log4jLogEvent.access$000(null);
        private ThreadContext.ContextStack contextStack = ThreadContext.getImmutableStack();
        private long threadId;
        private String threadName;
        private int threadPriority;
        private StackTraceElement source;
        private boolean includeLocation;
        private boolean endOfBatch = false;
        private long nanoTime;

        public Builder() {
        }

        public Builder(LogEvent logEvent) {
            Objects.requireNonNull(logEvent);
            if (logEvent instanceof RingBufferLogEvent) {
                ((RingBufferLogEvent)logEvent).initializeBuilder(this);
                return;
            }
            if (logEvent instanceof MutableLogEvent) {
                ((MutableLogEvent)logEvent).initializeBuilder(this);
                return;
            }
            this.loggerFqcn = logEvent.getLoggerFqcn();
            this.marker = logEvent.getMarker();
            this.level = logEvent.getLevel();
            this.loggerName = logEvent.getLoggerName();
            this.message = logEvent.getMessage();
            this.instant.initFrom(logEvent.getInstant());
            this.thrown = logEvent.getThrown();
            this.contextStack = logEvent.getContextStack();
            this.includeLocation = logEvent.isIncludeLocation();
            this.endOfBatch = logEvent.isEndOfBatch();
            this.nanoTime = logEvent.getNanoTime();
            if (logEvent instanceof Log4jLogEvent) {
                logEvent = (Log4jLogEvent)logEvent;
                this.contextData = ((Log4jLogEvent)logEvent).contextData;
                this.thrownProxy = ((Log4jLogEvent)logEvent).thrownProxy;
                this.source = ((Log4jLogEvent)logEvent).source;
                this.threadId = ((Log4jLogEvent)logEvent).threadId;
                this.threadName = ((Log4jLogEvent)logEvent).threadName;
                this.threadPriority = ((Log4jLogEvent)logEvent).threadPriority;
                return;
            }
            if (logEvent.getContextData() instanceof StringMap) {
                this.contextData = (StringMap)logEvent.getContextData();
            } else {
                if (this.contextData.isFrozen()) {
                    this.contextData = ContextDataFactory.createContextData();
                } else {
                    this.contextData.clear();
                }
                this.contextData.putAll(logEvent.getContextData());
            }
            this.thrownProxy = logEvent.getThrownProxy();
            this.source = logEvent.getSource();
            this.threadId = logEvent.getThreadId();
            this.threadName = logEvent.getThreadName();
            this.threadPriority = logEvent.getThreadPriority();
        }

        public Builder setLevel(Level level) {
            this.level = level;
            return this;
        }

        public Builder setLoggerFqcn(String string) {
            this.loggerFqcn = string;
            return this;
        }

        public Builder setLoggerName(String string) {
            this.loggerName = string;
            return this;
        }

        public Builder setMarker(Marker marker) {
            this.marker = marker;
            return this;
        }

        public Builder setMessage(Message message) {
            this.message = message;
            return this;
        }

        public Builder setThrown(Throwable throwable) {
            this.thrown = throwable;
            return this;
        }

        public Builder setTimeMillis(long l) {
            this.instant.initFromEpochMilli(l, 0);
            return this;
        }

        public Builder setInstant(Instant instant) {
            this.instant.initFrom(instant);
            return this;
        }

        public Builder setThrownProxy(ThrowableProxy throwableProxy) {
            this.thrownProxy = throwableProxy;
            return this;
        }

        @Deprecated
        public Builder setContextMap(Map<String, String> object) {
            this.contextData = ContextDataFactory.createContextData();
            if (object != null) {
                for (Map.Entry entry : object.entrySet()) {
                    this.contextData.putValue((String)entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        public Builder setContextData(StringMap stringMap) {
            this.contextData = stringMap;
            return this;
        }

        public Builder setContextStack(ThreadContext.ContextStack contextStack) {
            this.contextStack = contextStack;
            return this;
        }

        public Builder setThreadId(long l) {
            this.threadId = l;
            return this;
        }

        public Builder setThreadName(String string) {
            this.threadName = string;
            return this;
        }

        public Builder setThreadPriority(int n) {
            this.threadPriority = n;
            return this;
        }

        public Builder setSource(StackTraceElement stackTraceElement) {
            this.source = stackTraceElement;
            return this;
        }

        public Builder setIncludeLocation(boolean bl) {
            this.includeLocation = bl;
            return this;
        }

        public Builder setEndOfBatch(boolean bl) {
            this.endOfBatch = bl;
            return this;
        }

        public Builder setNanoTime(long l) {
            this.nanoTime = l;
            return this;
        }

        @Override
        public Log4jLogEvent build() {
            this.initTimeFields();
            Log4jLogEvent log4jLogEvent = new Log4jLogEvent(this.loggerName, this.marker, this.loggerFqcn, this.level, this.message, this.thrown, this.thrownProxy, this.contextData, this.contextStack, this.threadId, this.threadName, this.threadPriority, this.source, this.instant.getEpochMillisecond(), this.instant.getNanoOfMillisecond(), this.nanoTime);
            log4jLogEvent.setIncludeLocation(this.includeLocation);
            log4jLogEvent.setEndOfBatch(this.endOfBatch);
            return log4jLogEvent;
        }

        private void initTimeFields() {
            if (this.instant.getEpochMillisecond() == 0L) {
                this.instant.initFrom(CLOCK);
            }
        }
    }
}

