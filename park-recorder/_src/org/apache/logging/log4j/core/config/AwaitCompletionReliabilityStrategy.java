/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LocationAwareReliabilityStrategy;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.ReliabilityStrategy;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.Supplier;

public class AwaitCompletionReliabilityStrategy
implements LocationAwareReliabilityStrategy,
ReliabilityStrategy {
    private static final int MAX_RETRIES = 3;
    private final AtomicInteger counter = new AtomicInteger();
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final Lock shutdownLock = new ReentrantLock();
    private final Condition noLogEvents = this.shutdownLock.newCondition();
    private final LoggerConfig loggerConfig;

    public AwaitCompletionReliabilityStrategy(LoggerConfig loggerConfig) {
        this.loggerConfig = Objects.requireNonNull(loggerConfig, "loggerConfig is null");
    }

    @Override
    public void log(Supplier<LoggerConfig> object, String string, String string2, Marker marker, Level level, Message message, Throwable throwable) {
        object = this.getActiveLoggerConfig((Supplier<LoggerConfig>)object);
        try {
            ((LoggerConfig)object).log(string, string2, marker, level, message, throwable);
            return;
        }
        finally {
            ((LoggerConfig)object).getReliabilityStrategy().afterLogEvent();
        }
    }

    @Override
    public void log(Supplier<LoggerConfig> object, String string, String string2, StackTraceElement stackTraceElement, Marker marker, Level level, Message message, Throwable throwable) {
        object = this.getActiveLoggerConfig((Supplier<LoggerConfig>)object);
        try {
            ((LoggerConfig)object).log(string, string2, stackTraceElement, marker, level, message, throwable);
            return;
        }
        finally {
            ((LoggerConfig)object).getReliabilityStrategy().afterLogEvent();
        }
    }

    @Override
    public void log(Supplier<LoggerConfig> object, LogEvent logEvent) {
        object = this.getActiveLoggerConfig((Supplier<LoggerConfig>)object);
        try {
            ((LoggerConfig)object).log(logEvent);
            return;
        }
        finally {
            ((LoggerConfig)object).getReliabilityStrategy().afterLogEvent();
        }
    }

    @Override
    public LoggerConfig getActiveLoggerConfig(Supplier<LoggerConfig> supplier) {
        LoggerConfig loggerConfig = this.loggerConfig;
        if (!this.beforeLogEvent()) {
            loggerConfig = supplier.get();
            if (loggerConfig == this.loggerConfig) {
                return loggerConfig;
            }
            return loggerConfig.getReliabilityStrategy().getActiveLoggerConfig(supplier);
        }
        return loggerConfig;
    }

    private boolean beforeLogEvent() {
        return this.counter.incrementAndGet() > 0;
    }

    @Override
    public void afterLogEvent() {
        if (this.counter.decrementAndGet() == 0 && this.shutdown.get()) {
            this.signalCompletionIfShutdown();
        }
    }

    private void signalCompletionIfShutdown() {
        Lock lock = this.shutdownLock;
        lock.lock();
        try {
            this.noLogEvents.signalAll();
            return;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void beforeStopAppenders() {
        this.waitForCompletion();
    }

    private void waitForCompletion() {
        this.shutdownLock.lock();
        try {
            if (this.shutdown.compareAndSet(false, true)) {
                int n = 0;
                while (!this.counter.compareAndSet(0, Integer.MIN_VALUE)) {
                    if (this.counter.get() < 0) {
                        return;
                    }
                    try {
                        this.noLogEvents.await(n + 1, TimeUnit.SECONDS);
                    }
                    catch (InterruptedException interruptedException) {
                        if (++n <= 3) continue;
                        break;
                    }
                }
            }
            return;
        }
        finally {
            this.shutdownLock.unlock();
        }
    }

    @Override
    public void beforeStopConfiguration(Configuration configuration) {
    }
}

