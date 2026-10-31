/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.lmax.disruptor.EventFactory
 *  com.lmax.disruptor.EventHandler
 *  com.lmax.disruptor.EventTranslatorTwoArg
 *  com.lmax.disruptor.RingBuffer
 *  com.lmax.disruptor.Sequence
 *  com.lmax.disruptor.SequenceReportingEventHandler
 *  com.lmax.disruptor.TimeoutException
 *  com.lmax.disruptor.WaitStrategy
 *  com.lmax.disruptor.dsl.Disruptor
 *  com.lmax.disruptor.dsl.ProducerType
 */
package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorTwoArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceReportingEventHandler;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.async.AsyncLoggerConfig;
import org.apache.logging.log4j.core.async.AsyncLoggerConfigDelegate;
import org.apache.logging.log4j.core.async.AsyncQueueFullPolicy;
import org.apache.logging.log4j.core.async.AsyncQueueFullPolicyFactory;
import org.apache.logging.log4j.core.async.DiscardingAsyncQueueFullPolicy;
import org.apache.logging.log4j.core.async.DisruptorUtil;
import org.apache.logging.log4j.core.async.EventRoute;
import org.apache.logging.log4j.core.async.InternalAsyncUtil;
import org.apache.logging.log4j.core.async.RingBufferLogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.impl.LogEventFactory;
import org.apache.logging.log4j.core.impl.MutableLogEvent;
import org.apache.logging.log4j.core.impl.ReusableLogEventFactory;
import org.apache.logging.log4j.core.jmx.RingBufferAdmin;
import org.apache.logging.log4j.core.util.Log4jThreadFactory;
import org.apache.logging.log4j.core.util.Throwables;
import org.apache.logging.log4j.message.ReusableMessage;

public class AsyncLoggerConfigDisruptor
extends AbstractLifeCycle
implements AsyncLoggerConfigDelegate {
    private static final int MAX_DRAIN_ATTEMPTS_BEFORE_SHUTDOWN = 200;
    private static final int SLEEP_MILLIS_BETWEEN_DRAIN_ATTEMPTS = 50;
    private static final EventFactory<Log4jEventWrapper> FACTORY = new EventFactory<Log4jEventWrapper>(){

        public final Log4jEventWrapper newInstance() {
            return new Log4jEventWrapper();
        }
    };
    private static final EventFactory<Log4jEventWrapper> MUTABLE_FACTORY = new EventFactory<Log4jEventWrapper>(){

        public final Log4jEventWrapper newInstance() {
            return new Log4jEventWrapper(new MutableLogEvent());
        }
    };
    private static final EventTranslatorTwoArg<Log4jEventWrapper, LogEvent, AsyncLoggerConfig> TRANSLATOR = new EventTranslatorTwoArg<Log4jEventWrapper, LogEvent, AsyncLoggerConfig>(){

        public final void translateTo(Log4jEventWrapper log4jEventWrapper, long l, LogEvent logEvent, AsyncLoggerConfig asyncLoggerConfig) {
            log4jEventWrapper.event = logEvent;
            log4jEventWrapper.loggerConfig = asyncLoggerConfig;
        }
    };
    private static final EventTranslatorTwoArg<Log4jEventWrapper, LogEvent, AsyncLoggerConfig> MUTABLE_TRANSLATOR = new EventTranslatorTwoArg<Log4jEventWrapper, LogEvent, AsyncLoggerConfig>(){

        public final void translateTo(Log4jEventWrapper log4jEventWrapper, long l, LogEvent logEvent, AsyncLoggerConfig asyncLoggerConfig) {
            ((MutableLogEvent)log4jEventWrapper.event).initFrom(logEvent);
            log4jEventWrapper.loggerConfig = asyncLoggerConfig;
        }
    };
    private int ringBufferSize;
    private AsyncQueueFullPolicy asyncQueueFullPolicy;
    private Boolean mutable = Boolean.FALSE;
    private volatile Disruptor<Log4jEventWrapper> disruptor;
    private long backgroundThreadId;
    private EventFactory<Log4jEventWrapper> factory;
    private EventTranslatorTwoArg<Log4jEventWrapper, LogEvent, AsyncLoggerConfig> translator;
    private volatile boolean alreadyLoggedWarning = false;
    private final Object queueFullEnqueueLock = new Object();

    @Override
    public void setLogEventFactory(LogEventFactory logEventFactory) {
        this.mutable = this.mutable != false || logEventFactory instanceof ReusableLogEventFactory;
    }

    @Override
    public synchronized void start() {
        if (this.disruptor != null) {
            LOGGER.trace("AsyncLoggerConfigDisruptor not starting new disruptor for this configuration, using existing object.");
            return;
        }
        LOGGER.trace("AsyncLoggerConfigDisruptor creating new disruptor for this configuration.");
        this.ringBufferSize = DisruptorUtil.calculateRingBufferSize("AsyncLoggerConfig.RingBufferSize");
        WaitStrategy waitStrategy = DisruptorUtil.createWaitStrategy("AsyncLoggerConfig.WaitStrategy");
        Log4jThreadFactory log4jThreadFactory = new Log4jThreadFactory("AsyncLoggerConfig", true, 5){

            @Override
            public Thread newThread(Runnable runnable) {
                runnable = super.newThread(runnable);
                AsyncLoggerConfigDisruptor.this.backgroundThreadId = ((Thread)runnable).getId();
                return runnable;
            }
        };
        this.asyncQueueFullPolicy = AsyncQueueFullPolicyFactory.create();
        this.translator = this.mutable != false ? MUTABLE_TRANSLATOR : TRANSLATOR;
        this.factory = this.mutable != false ? MUTABLE_FACTORY : FACTORY;
        this.disruptor = new Disruptor(this.factory, this.ringBufferSize, (ThreadFactory)log4jThreadFactory, ProducerType.MULTI, waitStrategy);
        log4jThreadFactory = DisruptorUtil.getAsyncLoggerConfigExceptionHandler();
        this.disruptor.setDefaultExceptionHandler(log4jThreadFactory);
        Log4jEventWrapperHandler[] log4jEventWrapperHandlerArray = new Log4jEventWrapperHandler[]{new Log4jEventWrapperHandler()};
        this.disruptor.handleEventsWith((EventHandler[])log4jEventWrapperHandlerArray);
        LOGGER.debug("Starting AsyncLoggerConfig disruptor for this configuration with ringbufferSize={}, waitStrategy={}, exceptionHandler={}...", (Object)this.disruptor.getRingBuffer().getBufferSize(), (Object)waitStrategy.getClass().getSimpleName(), (Object)log4jThreadFactory);
        this.disruptor.start();
        super.start();
    }

    @Override
    public boolean stop(long l, TimeUnit timeUnit) {
        Disruptor<Log4jEventWrapper> disruptor = this.disruptor;
        if (disruptor == null) {
            LOGGER.trace("AsyncLoggerConfigDisruptor: disruptor for this configuration already shut down.");
            return true;
        }
        this.setStopping();
        LOGGER.trace("AsyncLoggerConfigDisruptor: shutting down disruptor for this configuration.");
        this.disruptor = null;
        for (int i2 = 0; AsyncLoggerConfigDisruptor.hasBacklog(disruptor) && i2 < 200; ++i2) {
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
            LOGGER.warn("AsyncLoggerConfigDisruptor: shutdown timed out after {} {}", (Object)l, (Object)timeUnit);
            disruptor.halt();
        }
        LOGGER.trace("AsyncLoggerConfigDisruptor: disruptor has been shut down.");
        if (DiscardingAsyncQueueFullPolicy.getDiscardCount(this.asyncQueueFullPolicy) > 0L) {
            LOGGER.trace("AsyncLoggerConfigDisruptor: {} discarded {} events.", (Object)this.asyncQueueFullPolicy, (Object)DiscardingAsyncQueueFullPolicy.getDiscardCount(this.asyncQueueFullPolicy));
        }
        this.setStopped();
        return true;
    }

    private static boolean hasBacklog(Disruptor<?> disruptor) {
        RingBuffer ringBuffer = disruptor.getRingBuffer();
        return !ringBuffer.hasAvailableCapacity(ringBuffer.getBufferSize());
    }

    @Override
    public EventRoute getEventRoute(Level level) {
        if (this.remainingDisruptorCapacity() < 0) {
            return EventRoute.DISCARD;
        }
        return this.asyncQueueFullPolicy.getRoute(this.backgroundThreadId, level);
    }

    private int remainingDisruptorCapacity() {
        Disruptor<Log4jEventWrapper> disruptor = this.disruptor;
        if (this.hasLog4jBeenShutDown(disruptor)) {
            return -1;
        }
        return (int)disruptor.getRingBuffer().remainingCapacity();
    }

    private boolean hasLog4jBeenShutDown(Disruptor<Log4jEventWrapper> disruptor) {
        if (disruptor == null) {
            LOGGER.warn("Ignoring log event after log4j was shut down");
            return true;
        }
        return false;
    }

    @Override
    public void enqueueEvent(LogEvent logEvent, AsyncLoggerConfig asyncLoggerConfig) {
        try {
            LogEvent logEvent2 = this.prepareEvent(logEvent);
            this.enqueue(logEvent2, asyncLoggerConfig);
            return;
        }
        catch (NullPointerException nullPointerException) {
            LOGGER.warn("Ignoring log event after log4j was shut down: {} [{}] {}", (Object)logEvent.getLevel(), (Object)logEvent.getLoggerName(), (Object)(logEvent.getMessage().getFormattedMessage() + (logEvent.getThrown() == null ? "" : Throwables.toStringList(logEvent.getThrown()))));
            return;
        }
    }

    private LogEvent prepareEvent(LogEvent logEvent) {
        if ((logEvent = this.ensureImmutable(logEvent)).getMessage() instanceof ReusableMessage) {
            if (logEvent instanceof Log4jLogEvent) {
                ((Log4jLogEvent)logEvent).makeMessageImmutable();
            } else if (logEvent instanceof MutableLogEvent) {
                if (this.translator != MUTABLE_TRANSLATOR) {
                    logEvent = ((MutableLogEvent)logEvent).createMemento();
                }
            } else {
                this.showWarningAboutCustomLogEventWithReusableMessage(logEvent);
            }
        } else {
            InternalAsyncUtil.makeMessageImmutable(logEvent.getMessage());
        }
        return logEvent;
    }

    private void showWarningAboutCustomLogEventWithReusableMessage(LogEvent logEvent) {
        if (!this.alreadyLoggedWarning) {
            LOGGER.warn("Custom log event of type {} contains a mutable message of type {}. AsyncLoggerConfig does not know how to make an immutable copy of this message. This may result in ConcurrentModificationExceptions or incorrect log messages if the application modifies objects in the message while the background thread is writing it to the appenders.", (Object)logEvent.getClass().getName(), (Object)logEvent.getMessage().getClass().getName());
            this.alreadyLoggedWarning = true;
        }
    }

    private void enqueue(LogEvent logEvent, AsyncLoggerConfig asyncLoggerConfig) {
        if (this.synchronizeEnqueueWhenQueueFull()) {
            Object object = this.queueFullEnqueueLock;
            synchronized (object) {
                this.disruptor.getRingBuffer().publishEvent(this.translator, (Object)logEvent, (Object)asyncLoggerConfig);
                return;
            }
        }
        this.disruptor.getRingBuffer().publishEvent(this.translator, (Object)logEvent, (Object)asyncLoggerConfig);
    }

    private boolean synchronizeEnqueueWhenQueueFull() {
        return DisruptorUtil.ASYNC_CONFIG_SYNCHRONIZE_ENQUEUE_WHEN_QUEUE_FULL && this.backgroundThreadId != Thread.currentThread().getId();
    }

    @Override
    public boolean tryEnqueue(LogEvent logEvent, AsyncLoggerConfig asyncLoggerConfig) {
        logEvent = this.prepareEvent(logEvent);
        return this.disruptor.getRingBuffer().tryPublishEvent(this.translator, (Object)logEvent, (Object)asyncLoggerConfig);
    }

    private LogEvent ensureImmutable(LogEvent logEvent) {
        LogEvent logEvent2 = logEvent;
        if (logEvent instanceof RingBufferLogEvent) {
            logEvent2 = ((RingBufferLogEvent)logEvent).createMemento();
        }
        return logEvent2;
    }

    @Override
    public RingBufferAdmin createRingBufferAdmin(String string, String string2) {
        return RingBufferAdmin.forAsyncLoggerConfig(this.disruptor.getRingBuffer(), string, string2);
    }

    static class Log4jEventWrapperHandler
    implements SequenceReportingEventHandler<Log4jEventWrapper> {
        private static final int NOTIFY_PROGRESS_THRESHOLD = 50;
        private Sequence sequenceCallback;
        private int counter;

        private Log4jEventWrapperHandler() {
        }

        public void setSequenceCallback(Sequence sequence) {
            this.sequenceCallback = sequence;
        }

        public void onEvent(Log4jEventWrapper log4jEventWrapper, long l, boolean bl) {
            log4jEventWrapper.event.setEndOfBatch(bl);
            log4jEventWrapper.loggerConfig.logToAsyncLoggerConfigsOnCurrentThread(log4jEventWrapper.event);
            log4jEventWrapper.clear();
            this.notifyIntermediateProgress(l);
        }

        private void notifyIntermediateProgress(long l) {
            if (++this.counter > 50) {
                this.sequenceCallback.set(l);
                this.counter = 0;
            }
        }
    }

    public static class Log4jEventWrapper {
        private AsyncLoggerConfig loggerConfig;
        private LogEvent event;

        public Log4jEventWrapper() {
        }

        public Log4jEventWrapper(MutableLogEvent mutableLogEvent) {
            this.event = mutableLogEvent;
        }

        public void clear() {
            this.loggerConfig = null;
            if (this.event instanceof MutableLogEvent) {
                ((MutableLogEvent)this.event).clear();
                return;
            }
            this.event = null;
        }

        public String toString() {
            return String.valueOf(this.event);
        }
    }
}

