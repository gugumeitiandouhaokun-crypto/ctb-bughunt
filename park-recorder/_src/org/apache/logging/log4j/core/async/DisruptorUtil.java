/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.lmax.disruptor.BlockingWaitStrategy
 *  com.lmax.disruptor.BusySpinWaitStrategy
 *  com.lmax.disruptor.ExceptionHandler
 *  com.lmax.disruptor.SleepingWaitStrategy
 *  com.lmax.disruptor.TimeoutBlockingWaitStrategy
 *  com.lmax.disruptor.WaitStrategy
 *  com.lmax.disruptor.YieldingWaitStrategy
 */
package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.TimeoutBlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.async.AsyncLoggerConfigDefaultExceptionHandler;
import org.apache.logging.log4j.core.async.AsyncLoggerConfigDisruptor;
import org.apache.logging.log4j.core.async.AsyncLoggerDefaultExceptionHandler;
import org.apache.logging.log4j.core.async.RingBufferLogEvent;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.Integers;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

final class DisruptorUtil {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final int RINGBUFFER_MIN_SIZE = 128;
    private static final int RINGBUFFER_DEFAULT_SIZE = 262144;
    private static final int RINGBUFFER_NO_GC_DEFAULT_SIZE = 4096;
    static final boolean ASYNC_LOGGER_SYNCHRONIZE_ENQUEUE_WHEN_QUEUE_FULL = PropertiesUtil.getProperties().getBooleanProperty("AsyncLogger.SynchronizeEnqueueWhenQueueFull", true);
    static final boolean ASYNC_CONFIG_SYNCHRONIZE_ENQUEUE_WHEN_QUEUE_FULL = PropertiesUtil.getProperties().getBooleanProperty("AsyncLoggerConfig.SynchronizeEnqueueWhenQueueFull", true);

    private DisruptorUtil() {
    }

    static long getTimeout(String string, long l) {
        return PropertiesUtil.getProperties().getLongProperty(string, l);
    }

    static WaitStrategy createWaitStrategy(String string) {
        long l = DisruptorUtil.getTimeout(string.startsWith("AsyncLogger.") ? "AsyncLogger.Timeout" : "AsyncLoggerConfig.Timeout", 10L);
        return DisruptorUtil.createWaitStrategy(string, l);
    }

    static WaitStrategy createWaitStrategy(String string, long l) {
        String string2 = PropertiesUtil.getProperties().getStringProperty(string, "TIMEOUT");
        LOGGER.trace("property {}={}", (Object)string, (Object)string2);
        switch (string2.toUpperCase(Locale.ROOT)) {
            case "SLEEP": {
                return new SleepingWaitStrategy();
            }
            case "YIELD": {
                return new YieldingWaitStrategy();
            }
            case "BLOCK": {
                return new BlockingWaitStrategy();
            }
            case "BUSYSPIN": {
                return new BusySpinWaitStrategy();
            }
            case "TIMEOUT": {
                return new TimeoutBlockingWaitStrategy(l, TimeUnit.MILLISECONDS);
            }
        }
        return new TimeoutBlockingWaitStrategy(l, TimeUnit.MILLISECONDS);
    }

    static int calculateRingBufferSize(String string) {
        int n = Constants.ENABLE_THREADLOCALS ? 4096 : 262144;
        string = PropertiesUtil.getProperties().getStringProperty(string, String.valueOf(n));
        try {
            int n2 = Integer.parseInt(string);
            if (n2 < 128) {
                n2 = 128;
                LOGGER.warn("Invalid RingBufferSize {}, using minimum size {}.", (Object)string, (Object)128);
            }
            n = n2;
        }
        catch (Exception exception) {
            LOGGER.warn("Invalid RingBufferSize {}, using default size {}.", (Object)string, (Object)n);
        }
        return Integers.ceilingNextPowerOfTwo(n);
    }

    static ExceptionHandler<RingBufferLogEvent> getAsyncLoggerExceptionHandler() {
        String string = PropertiesUtil.getProperties().getStringProperty("AsyncLogger.ExceptionHandler");
        if (string == null) {
            return new AsyncLoggerDefaultExceptionHandler();
        }
        try {
            return (ExceptionHandler)Loader.loadClass(string).newInstance();
        }
        catch (Exception exception) {
            LOGGER.debug("Invalid AsyncLogger.ExceptionHandler value: error creating {}: ", (Object)string, (Object)exception);
            return new AsyncLoggerDefaultExceptionHandler();
        }
    }

    static ExceptionHandler<AsyncLoggerConfigDisruptor.Log4jEventWrapper> getAsyncLoggerConfigExceptionHandler() {
        String string = PropertiesUtil.getProperties().getStringProperty("AsyncLoggerConfig.ExceptionHandler");
        if (string == null) {
            return new AsyncLoggerConfigDefaultExceptionHandler();
        }
        try {
            return (ExceptionHandler)Loader.loadClass(string).newInstance();
        }
        catch (Exception exception) {
            LOGGER.debug("Invalid AsyncLoggerConfig.ExceptionHandler value: error creating {}: ", (Object)string, (Object)exception);
            return new AsyncLoggerConfigDefaultExceptionHandler();
        }
    }

    public static long getExecutorThreadId(ExecutorService object) {
        object = object.submit(new Callable<Long>(){

            @Override
            public final Long call() {
                return Thread.currentThread().getId();
            }
        });
        try {
            return (Long)object.get();
        }
        catch (Exception exception) {
            throw new IllegalStateException("Could not obtain executor thread Id. Giving up to avoid the risk of application deadlock.", exception);
        }
    }
}

