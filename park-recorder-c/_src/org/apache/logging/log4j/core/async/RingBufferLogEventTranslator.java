/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.lmax.disruptor.EventTranslator
 */
package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.EventTranslator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.ContextDataInjector;
import org.apache.logging.log4j.core.async.AsyncLogger;
import org.apache.logging.log4j.core.async.RingBufferLogEvent;
import org.apache.logging.log4j.core.impl.ContextDataInjectorFactory;
import org.apache.logging.log4j.core.util.Clock;
import org.apache.logging.log4j.core.util.NanoClock;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.StringMap;

public class RingBufferLogEventTranslator
implements EventTranslator<RingBufferLogEvent> {
    private final ContextDataInjector injector = ContextDataInjectorFactory.createInjector();
    private AsyncLogger asyncLogger;
    String loggerName;
    protected Marker marker;
    protected String fqcn;
    protected Level level;
    protected Message message;
    protected Throwable thrown;
    private ThreadContext.ContextStack contextStack;
    private long threadId = Thread.currentThread().getId();
    private String threadName = Thread.currentThread().getName();
    private int threadPriority = Thread.currentThread().getPriority();
    private StackTraceElement location;
    private Clock clock;
    private NanoClock nanoClock;

    public void translateTo(RingBufferLogEvent ringBufferLogEvent, long l) {
        ringBufferLogEvent.setValues(this.asyncLogger, this.loggerName, this.marker, this.fqcn, this.level, this.message, this.thrown, this.injector.injectContextData(null, (StringMap)ringBufferLogEvent.getContextData()), this.contextStack, this.threadId, this.threadName, this.threadPriority, this.location, this.clock, this.nanoClock);
        this.clear();
    }

    void clear() {
        this.setBasicValues(null, null, null, null, null, null, null, null, null, null, null);
    }

    public void setBasicValues(AsyncLogger asyncLogger, String string, Marker marker, String string2, Level level, Message message, Throwable throwable, ThreadContext.ContextStack contextStack, StackTraceElement stackTraceElement, Clock clock, NanoClock nanoClock) {
        this.asyncLogger = asyncLogger;
        this.loggerName = string;
        this.marker = marker;
        this.fqcn = string2;
        this.level = level;
        this.message = message;
        this.thrown = throwable;
        this.contextStack = contextStack;
        this.location = stackTraceElement;
        this.clock = clock;
        this.nanoClock = nanoClock;
    }

    public void updateThreadValues() {
        Thread thread = Thread.currentThread();
        this.threadId = thread.getId();
        this.threadName = thread.getName();
        this.threadPriority = thread.getPriority();
    }
}

