/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.lmax.disruptor.EventFactory
 *  com.lmax.disruptor.EventHandler
 *  com.lmax.disruptor.EventTranslator
 *  com.lmax.disruptor.EventTranslatorVararg
 *  com.lmax.disruptor.RingBuffer
 *  com.lmax.disruptor.TimeoutException
 *  com.lmax.disruptor.WaitStrategy
 *  com.lmax.disruptor.dsl.Disruptor
 *  com.lmax.disruptor.dsl.ProducerType
 */
package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.EventTranslatorVararg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.async.AsyncLogger;
import org.apache.logging.log4j.core.async.AsyncQueueFullPolicy;
import org.apache.logging.log4j.core.async.AsyncQueueFullPolicyFactory;
import org.apache.logging.log4j.core.async.DiscardingAsyncQueueFullPolicy;
import org.apache.logging.log4j.core.async.DisruptorUtil;
import org.apache.logging.log4j.core.async.EventRoute;
import org.apache.logging.log4j.core.async.RingBufferLogEvent;
import org.apache.logging.log4j.core.async.RingBufferLogEventHandler;
import org.apache.logging.log4j.core.async.RingBufferLogEventTranslator;
import org.apache.logging.log4j.core.jmx.RingBufferAdmin;
import org.apache.logging.log4j.core.util.Log4jThreadFactory;
import org.apache.logging.log4j.core.util.Throwables;
import org.apache.logging.log4j.message.Message;

class AsyncLoggerDisruptor
extends AbstractLifeCycle {
    private static final int SLEEP_MILLIS_BETWEEN_DRAIN_ATTEMPTS = 50;
    private static final int MAX_DRAIN_ATTEMPTS_BEFORE_SHUTDOWN = 200;
    private final Object queueFullEnqueueLock = new Object();
    private volatile Disruptor<RingBufferLogEvent> disruptor;
    private String contextName;
    private boolean useThreadLocalTranslator = true;
    private long backgroundThreadId;
    private AsyncQueueFullPolicy asyncQueueFullPolicy;
    private int ringBufferSize;

    AsyncLoggerDisruptor(String string) {
        this.contextName = string;
    }

    public String getContextName() {
        return this.contextName;
    }

    public void setContextName(String string) {
        this.contextName = string;
    }

    Disruptor<RingBufferLogEvent> getDisruptor() {
        return this.disruptor;
    }

    @Override
    public synchronized void start() {
        if (this.disruptor != null) {
            LOGGER.trace("[{}] AsyncLoggerDisruptor not starting new disruptor for this context, using existing object.", (Object)this.contextName);
            return;
        }
        LOGGER.trace("[{}] AsyncLoggerDisruptor creating new disruptor for this context.", (Object)this.contextName);
        this.ringBufferSize = DisruptorUtil.calculateRingBufferSize("AsyncLogger.RingBufferSize");
        WaitStrategy waitStrategy = DisruptorUtil.createWaitStrategy("AsyncLogger.WaitStrategy");
        Log4jThreadFactory log4jThreadFactory = new Log4jThreadFactory("AsyncLogger[" + this.contextName + "]", true, 5){

            @Override
            public Thread newThread(Runnable runnable) {
                runnable = super.newThread(runnable);
                AsyncLoggerDisruptor.this.backgroundThreadId = ((Thread)runnable).getId();
                return runnable;
            }
        };
        this.asyncQueueFullPolicy = AsyncQueueFullPolicyFactory.create();
        this.disruptor = new Disruptor((EventFactory)RingBufferLogEvent.FACTORY, this.ringBufferSize, (ThreadFactory)log4jThreadFactory, ProducerType.MULTI, waitStrategy);
        log4jThreadFactory = DisruptorUtil.getAsyncLoggerExceptionHandler();
        this.disruptor.setDefaultExceptionHandler(log4jThreadFactory);
        RingBufferLogEventHandler[] ringBufferLogEventHandlerArray = new RingBufferLogEventHandler[]{new RingBufferLogEventHandler()};
        this.disruptor.handleEventsWith((EventHandler[])ringBufferLogEventHandlerArray);
        LOGGER.debug("[{}] Starting AsyncLogger disruptor for this context with ringbufferSize={}, waitStrategy={}, exceptionHandler={}...", (Object)this.contextName, (Object)this.disruptor.getRingBuffer().getBufferSize(), (Object)waitStrategy.getClass().getSimpleName(), (Object)log4jThreadFactory);
        this.disruptor.start();
        LOGGER.trace("[{}] AsyncLoggers use a {} translator", (Object)this.contextName, (Object)(this.useThreadLocalTranslator ? "threadlocal" : "vararg"));
        super.start();
    }

    @Override
    public boolean stop(long l, TimeUnit timeUnit) {
        Disruptor<RingBufferLogEvent> disruptor = this.getDisruptor();
        if (disruptor == null) {
            LOGGER.trace("[{}] AsyncLoggerDisruptor: disruptor for this context already shut down.", (Object)this.contextName);
            return true;
        }
        this.setStopping();
        LOGGER.debug("[{}] AsyncLoggerDisruptor: shutting down disruptor for this context.", (Object)this.contextName);
        this.disruptor = null;
        for (int j = 0; AsyncLoggerDisruptor.hasBacklog(disruptor) && j < 200; ++j) {
            try {
                Thread.sleep(50L);
                continue;
            }
            catch (InterruptedException interruptedException) {}
        }
        try {
            disruptor.shutdown(l, timeUnit);
        }
        catch (TimeoutException timeoutException) {
            LOGGER.warn("[{}] AsyncLoggerDisruptor: shutdown timed out after {} {}", (Object)this.contextName, (Object)l, (Object)timeUnit);
            disruptor.halt();
        }
        LOGGER.trace("[{}] AsyncLoggerDisruptor: disruptor has been shut down.", (Object)this.contextName);
        if (DiscardingAsyncQueueFullPolicy.getDiscardCount(this.asyncQueueFullPolicy) > 0L) {
            LOGGER.trace("AsyncLoggerDisruptor: {} discarded {} events.", (Object)this.asyncQueueFullPolicy, (Object)DiscardingAsyncQueueFullPolicy.getDiscardCount(this.asyncQueueFullPolicy));
        }
        this.setStopped();
        return true;
    }

    private static boolean hasBacklog(Disruptor<?> disruptor) {
        RingBuffer ringBuffer = disruptor.getRingBuffer();
        return !ringBuffer.hasAvailableCapacity(ringBuffer.getBufferSize());
    }

    public RingBufferAdmin createRingBufferAdmin(String string) {
        return RingBufferAdmin.forAsyncLogger(this.disruptor == null ? null : this.disruptor.getRingBuffer(), string);
    }

    EventRoute getEventRoute(Level level) {
        if (this.remainingDisruptorCapacity() < 0) {
            return EventRoute.DISCARD;
        }
        return this.asyncQueueFullPolicy.getRoute(this.backgroundThreadId, level);
    }

    private int remainingDisruptorCapacity() {
        Disruptor<RingBufferLogEvent> disruptor = this.disruptor;
        if (this.hasLog4jBeenShutDown(disruptor)) {
            return -1;
        }
        return (int)disruptor.getRingBuffer().remainingCapacity();
    }

    private boolean hasLog4jBeenShutDown(Disruptor<RingBufferLogEvent> disruptor) {
        if (disruptor == null) {
            LOGGER.warn("Ignoring log event after log4j was shut down");
            return true;
        }
        return false;
    }

    boolean tryPublish(RingBufferLogEventTranslator ringBufferLogEventTranslator) {
        try {
            return this.disruptor.getRingBuffer().tryPublishEvent((EventTranslator)ringBufferLogEventTranslator);
        }
        catch (NullPointerException nullPointerException) {
            this.logWarningOnNpeFromDisruptorPublish(ringBufferLogEventTranslator);
            return false;
        }
    }

    void enqueueLogMessageWhenQueueFull(RingBufferLogEventTranslator ringBufferLogEventTranslator) {
        try {
            if (this.synchronizeEnqueueWhenQueueFull()) {
                Object object = this.queueFullEnqueueLock;
                synchronized (object) {
                    this.disruptor.publishEvent((EventTranslator)ringBufferLogEventTranslator);
                    return;
                }
            }
            this.disruptor.publishEvent((EventTranslator)ringBufferLogEventTranslator);
            return;
        }
        catch (NullPointerException nullPointerException) {
            this.logWarningOnNpeFromDisruptorPublish(ringBufferLogEventTranslator);
            return;
        }
    }

    void enqueueLogMessageWhenQueueFull(EventTranslatorVararg<RingBufferLogEvent> eventTranslatorVararg, AsyncLogger asyncLogger, StackTraceElement stackTraceElement, String string, Level level, Marker marker, Message message, Throwable throwable) {
        try {
            if (this.synchronizeEnqueueWhenQueueFull()) {
                Object object = this.queueFullEnqueueLock;
                synchronized (object) {
                    this.disruptor.getRingBuffer().publishEvent(eventTranslatorVararg, new Object[]{asyncLogger, stackTraceElement, string, level, marker, message, throwable});
                    return;
                }
            }
            this.disruptor.getRingBuffer().publishEvent(eventTranslatorVararg, new Object[]{asyncLogger, stackTraceElement, string, level, marker, message, throwable});
            return;
        }
        catch (NullPointerException nullPointerException) {
            this.logWarningOnNpeFromDisruptorPublish(level, string, message, throwable);
            return;
        }
    }

    private boolean synchronizeEnqueueWhenQueueFull() {
        return DisruptorUtil.ASYNC_LOGGER_SYNCHRONIZE_ENQUEUE_WHEN_QUEUE_FULL && this.backgroundThreadId != Thread.currentThread().getId();
    }

    private void logWarningOnNpeFromDisruptorPublish(RingBufferLogEventTranslator ringBufferLogEventTranslator) {
        this.logWarningOnNpeFromDisruptorPublish(ringBufferLogEventTranslator.level, ringBufferLogEventTranslator.loggerName, ringBufferLogEventTranslator.message, ringBufferLogEventTranslator.thrown);
    }

    private void logWarningOnNpeFromDisruptorPublish(Level level, String string, Message message, Throwable throwable) {
        LOGGER.warn("[{}] Ignoring log event after log4j was shut down: {} [{}] {}{}", (Object)this.contextName, (Object)level, (Object)string, (Object)message.getFormattedMessage(), throwable == null ? "" : Throwables.toStringList(throwable));
    }

    public boolean isUseThreadLocals() {
        return this.useThreadLocalTranslator;
    }

    public void setUseThreadLocals(boolean bl) {
        this.useThreadLocalTranslator = bl;
        LOGGER.trace("[{}] AsyncLoggers have been modified to use a {} translator", (Object)this.contextName, (Object)(this.useThreadLocalTranslator ? "threadlocal" : "vararg"));
    }
}

