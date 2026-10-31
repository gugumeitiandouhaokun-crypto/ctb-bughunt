/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.lmax.disruptor.EventTranslatorVararg
 *  com.lmax.disruptor.RingBuffer
 *  com.lmax.disruptor.dsl.Disruptor
 */
package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.EventTranslatorVararg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.ContextDataInjector;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.async.AsyncLoggerDisruptor;
import org.apache.logging.log4j.core.async.AsyncQueueFullMessageUtil;
import org.apache.logging.log4j.core.async.EventRoute;
import org.apache.logging.log4j.core.async.InternalAsyncUtil;
import org.apache.logging.log4j.core.async.RingBufferLogEvent;
import org.apache.logging.log4j.core.async.RingBufferLogEventTranslator;
import org.apache.logging.log4j.core.async.ThreadNameCachingStrategy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.ContextDataFactory;
import org.apache.logging.log4j.core.impl.ContextDataInjectorFactory;
import org.apache.logging.log4j.core.util.Clock;
import org.apache.logging.log4j.core.util.ClockFactory;
import org.apache.logging.log4j.core.util.NanoClock;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.ReusableMessage;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.apache.logging.log4j.util.StringMap;

public class AsyncLogger
extends Logger
implements EventTranslatorVararg<RingBufferLogEvent> {
    private static final StatusLogger LOGGER = StatusLogger.getLogger();
    private static final Clock CLOCK = ClockFactory.getClock();
    private static final ContextDataInjector CONTEXT_DATA_INJECTOR = ContextDataInjectorFactory.createInjector();
    private static final ThreadNameCachingStrategy THREAD_NAME_CACHING_STRATEGY = ThreadNameCachingStrategy.create();
    private final ThreadLocal<RingBufferLogEventTranslator> threadLocalTranslator = new ThreadLocal();
    private final AsyncLoggerDisruptor loggerDisruptor;
    private volatile boolean includeLocation;
    private volatile NanoClock nanoClock;
    private final TranslatorType threadLocalTranslatorType = new TranslatorType(){

        @Override
        void log(String string, StackTraceElement stackTraceElement, Level level, Marker marker, Message message, Throwable throwable) {
            AsyncLogger.this.logWithThreadLocalTranslator(string, stackTraceElement, level, marker, message, throwable);
        }

        @Override
        void log(String string, Level level, Marker marker, Message message, Throwable throwable) {
            AsyncLogger.this.logWithThreadLocalTranslator(string, level, marker, message, throwable);
        }
    };
    private final TranslatorType varargTranslatorType = new TranslatorType(){

        @Override
        void log(String string, StackTraceElement stackTraceElement, Level level, Marker marker, Message message, Throwable throwable) {
            AsyncLogger.this.logWithVarargTranslator(string, stackTraceElement, level, marker, message, throwable);
        }

        @Override
        void log(String string, Level level, Marker marker, Message message, Throwable throwable) {
            AsyncLogger.this.logWithVarargTranslator(string, level, marker, message, throwable);
        }
    };

    public AsyncLogger(LoggerContext loggerContext, String string, MessageFactory messageFactory, AsyncLoggerDisruptor asyncLoggerDisruptor) {
        super(loggerContext, string, messageFactory);
        this.loggerDisruptor = asyncLoggerDisruptor;
        this.includeLocation = this.privateConfig.loggerConfig.isIncludeLocation();
        this.nanoClock = loggerContext.getConfiguration().getNanoClock();
    }

    @Override
    public void updateConfiguration(Configuration configuration) {
        this.nanoClock = configuration.getNanoClock();
        this.includeLocation = configuration.getLoggerConfig(this.name).isIncludeLocation();
        super.updateConfiguration(configuration);
    }

    NanoClock getNanoClock() {
        return this.nanoClock;
    }

    private RingBufferLogEventTranslator getCachedTranslator() {
        RingBufferLogEventTranslator ringBufferLogEventTranslator = this.threadLocalTranslator.get();
        if (ringBufferLogEventTranslator == null) {
            ringBufferLogEventTranslator = new RingBufferLogEventTranslator();
            this.threadLocalTranslator.set(ringBufferLogEventTranslator);
        }
        return ringBufferLogEventTranslator;
    }

    @Override
    public void logMessage(String string, Level level, Marker marker, Message message, Throwable throwable) {
        this.getTranslatorType().log(string, level, marker, message, throwable);
    }

    @Override
    public void log(Level level, Marker marker, String string, StackTraceElement stackTraceElement, Message message, Throwable throwable) {
        this.getTranslatorType().log(string, stackTraceElement, level, marker, message, throwable);
    }

    private TranslatorType getTranslatorType() {
        if (this.loggerDisruptor.isUseThreadLocals()) {
            return this.threadLocalTranslatorType;
        }
        return this.varargTranslatorType;
    }

    private boolean isReused(Message message) {
        return message instanceof ReusableMessage;
    }

    private void logWithThreadLocalTranslator(String string, Level level, Marker marker, Message message, Throwable throwable) {
        RingBufferLogEventTranslator ringBufferLogEventTranslator = this.getCachedTranslator();
        this.initTranslator(ringBufferLogEventTranslator, string, level, marker, message, throwable);
        this.initTranslatorThreadValues(ringBufferLogEventTranslator);
        this.publish(ringBufferLogEventTranslator);
    }

    private void logWithThreadLocalTranslator(String string, StackTraceElement stackTraceElement, Level level, Marker marker, Message message, Throwable throwable) {
        RingBufferLogEventTranslator ringBufferLogEventTranslator = this.getCachedTranslator();
        this.initTranslator(ringBufferLogEventTranslator, string, stackTraceElement, level, marker, message, throwable);
        this.initTranslatorThreadValues(ringBufferLogEventTranslator);
        this.publish(ringBufferLogEventTranslator);
    }

    private void publish(RingBufferLogEventTranslator ringBufferLogEventTranslator) {
        if (!this.loggerDisruptor.tryPublish(ringBufferLogEventTranslator)) {
            this.handleRingBufferFull(ringBufferLogEventTranslator);
        }
    }

    private void handleRingBufferFull(RingBufferLogEventTranslator ringBufferLogEventTranslator) {
        if (AbstractLogger.getRecursionDepth() > 1) {
            AsyncQueueFullMessageUtil.logWarningToStatusLogger();
            this.logMessageInCurrentThread(ringBufferLogEventTranslator.fqcn, ringBufferLogEventTranslator.level, ringBufferLogEventTranslator.marker, ringBufferLogEventTranslator.message, ringBufferLogEventTranslator.thrown);
            ringBufferLogEventTranslator.clear();
            return;
        }
        EventRoute eventRoute = this.loggerDisruptor.getEventRoute(ringBufferLogEventTranslator.level);
        switch (eventRoute) {
            case ENQUEUE: {
                this.loggerDisruptor.enqueueLogMessageWhenQueueFull(ringBufferLogEventTranslator);
                return;
            }
            case SYNCHRONOUS: {
                this.logMessageInCurrentThread(ringBufferLogEventTranslator.fqcn, ringBufferLogEventTranslator.level, ringBufferLogEventTranslator.marker, ringBufferLogEventTranslator.message, ringBufferLogEventTranslator.thrown);
                ringBufferLogEventTranslator.clear();
                return;
            }
            case DISCARD: {
                ringBufferLogEventTranslator.clear();
                return;
            }
        }
        throw new IllegalStateException("Unknown EventRoute ".concat(String.valueOf((Object)eventRoute)));
    }

    private void initTranslator(RingBufferLogEventTranslator ringBufferLogEventTranslator, String string, StackTraceElement stackTraceElement, Level level, Marker marker, Message message, Throwable throwable) {
        AsyncLogger asyncLogger = this;
        ringBufferLogEventTranslator.setBasicValues(asyncLogger, asyncLogger.name, marker, string, level, message, throwable, ThreadContext.getImmutableStack(), stackTraceElement, CLOCK, this.nanoClock);
    }

    private void initTranslator(RingBufferLogEventTranslator ringBufferLogEventTranslator, String string, Level level, Marker marker, Message message, Throwable throwable) {
        AsyncLogger asyncLogger = this;
        ringBufferLogEventTranslator.setBasicValues(asyncLogger, asyncLogger.name, marker, string, level, message, throwable, ThreadContext.getImmutableStack(), this.calcLocationIfRequested(string), CLOCK, this.nanoClock);
    }

    private void initTranslatorThreadValues(RingBufferLogEventTranslator ringBufferLogEventTranslator) {
        if (THREAD_NAME_CACHING_STRATEGY == ThreadNameCachingStrategy.UNCACHED) {
            ringBufferLogEventTranslator.updateThreadValues();
        }
    }

    private StackTraceElement calcLocationIfRequested(String string) {
        if (this.includeLocation) {
            return StackLocatorUtil.calcLocation(string);
        }
        return null;
    }

    private void logWithVarargTranslator(String string, Level level, Marker marker, Message message, Throwable throwable) {
        Object object = this.loggerDisruptor.getDisruptor();
        if (object == null) {
            LOGGER.error("Ignoring log event after Log4j has been shut down.");
            return;
        }
        if (!this.isReused(message)) {
            InternalAsyncUtil.makeMessageImmutable(message);
        }
        RingBuffer ringBuffer = object.getRingBuffer();
        Object[] objectArray = new Object[7];
        objectArray[0] = this;
        object = this.calcLocationIfRequested(string);
        objectArray[1] = object;
        objectArray[2] = string;
        objectArray[3] = level;
        objectArray[4] = marker;
        objectArray[5] = message;
        objectArray[6] = throwable;
        if (!ringBuffer.tryPublishEvent((EventTranslatorVararg)this, objectArray)) {
            this.handleRingBufferFull((StackTraceElement)object, string, level, marker, message, throwable);
        }
    }

    private void logWithVarargTranslator(String string, StackTraceElement stackTraceElement, Level level, Marker marker, Message message, Throwable throwable) {
        Disruptor<RingBufferLogEvent> disruptor = this.loggerDisruptor.getDisruptor();
        if (disruptor == null) {
            LOGGER.error("Ignoring log event after Log4j has been shut down.");
            return;
        }
        if (!this.isReused(message)) {
            InternalAsyncUtil.makeMessageImmutable(message);
        }
        if (!disruptor.getRingBuffer().tryPublishEvent((EventTranslatorVararg)this, new Object[]{this, stackTraceElement, string, level, marker, message, throwable})) {
            this.handleRingBufferFull(stackTraceElement, string, level, marker, message, throwable);
        }
    }

    public void translateTo(RingBufferLogEvent ringBufferLogEvent, long l, Object ... object) {
        AsyncLogger asyncLogger = (AsyncLogger)object[0];
        StackTraceElement stackTraceElement = (StackTraceElement)object[1];
        String string = (String)object[2];
        Level level = (Level)object[3];
        Marker marker = (Marker)object[4];
        Message message = (Message)object[5];
        object = (Throwable)object[6];
        ThreadContext.ContextStack contextStack = ThreadContext.getImmutableStack();
        Thread thread = Thread.currentThread();
        String string2 = THREAD_NAME_CACHING_STRATEGY.getThreadName();
        AsyncLogger asyncLogger2 = asyncLogger;
        ringBufferLogEvent.setValues(asyncLogger2, asyncLogger2.getName(), marker, string, level, message, (Throwable)object, CONTEXT_DATA_INJECTOR.injectContextData(null, (StringMap)ringBufferLogEvent.getContextData()), contextStack, thread.getId(), string2, thread.getPriority(), stackTraceElement, CLOCK, this.nanoClock);
    }

    void logMessageInCurrentThread(String string, Level level, Marker marker, Message message, Throwable throwable) {
        AsyncLogger asyncLogger = this;
        this.privateConfig.loggerConfig.getReliabilityStrategy().log(asyncLogger, asyncLogger.getName(), string, marker, level, message, throwable);
    }

    private void handleRingBufferFull(StackTraceElement stackTraceElement, String string, Level level, Marker marker, Message message, Throwable throwable) {
        if (AbstractLogger.getRecursionDepth() > 1) {
            AsyncQueueFullMessageUtil.logWarningToStatusLogger();
            this.logMessageInCurrentThread(string, level, marker, message, throwable);
            return;
        }
        EventRoute eventRoute = this.loggerDisruptor.getEventRoute(level);
        switch (eventRoute) {
            case ENQUEUE: {
                AsyncLogger asyncLogger = this;
                this.loggerDisruptor.enqueueLogMessageWhenQueueFull(asyncLogger, asyncLogger, stackTraceElement, string, level, marker, message, throwable);
                return;
            }
            case SYNCHRONOUS: {
                this.logMessageInCurrentThread(string, level, marker, message, throwable);
                return;
            }
            case DISCARD: {
                return;
            }
        }
        throw new IllegalStateException("Unknown EventRoute ".concat(String.valueOf((Object)eventRoute)));
    }

    public void actualAsyncLog(RingBufferLogEvent ringBufferLogEvent) {
        LoggerConfig loggerConfig = this.privateConfig.loggerConfig;
        List<Property> list = loggerConfig.getPropertyList();
        if (list != null) {
            this.onPropertiesPresent(ringBufferLogEvent, list);
        }
        loggerConfig.getReliabilityStrategy().log(this, ringBufferLogEvent);
    }

    private void onPropertiesPresent(RingBufferLogEvent ringBufferLogEvent, List<Property> list) {
        StringMap stringMap = AsyncLogger.getContextData(ringBufferLogEvent);
        int n = list.size();
        for (int j = 0; j < n; ++j) {
            Property property = list.get(j);
            if (stringMap.getValue(property.getName()) != null) continue;
            String string = property.isValueNeedsLookup() ? this.privateConfig.config.getStrSubstitutor().replace((LogEvent)ringBufferLogEvent, property.getValue()) : property.getValue();
            stringMap.putValue(property.getName(), string);
        }
        ringBufferLogEvent.setContextData(stringMap);
    }

    private static StringMap getContextData(RingBufferLogEvent serializable) {
        if ((serializable = (StringMap)serializable.getContextData()).isFrozen()) {
            StringMap stringMap = ContextDataFactory.createContextData();
            stringMap.putAll((ReadOnlyStringMap)serializable);
            return stringMap;
        }
        return serializable;
    }

    abstract class TranslatorType {
        TranslatorType() {
        }

        abstract void log(String var1, StackTraceElement var2, Level var3, Marker var4, Message var5, Throwable var6);

        abstract void log(String var1, Level var2, Marker var3, Message var4, Throwable var5);
    }
}

