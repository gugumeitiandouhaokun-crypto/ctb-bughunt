/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LocationAwareReliabilityStrategy;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.ReliabilityStrategy;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.Supplier;

public class LockingReliabilityStrategy
implements LocationAwareReliabilityStrategy,
ReliabilityStrategy {
    private final LoggerConfig loggerConfig;
    private final ReadWriteLock reconfigureLock = new ReentrantReadWriteLock();
    private volatile boolean isStopping = false;

    public LockingReliabilityStrategy(LoggerConfig loggerConfig) {
        this.loggerConfig = Objects.requireNonNull(loggerConfig, "loggerConfig was null");
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
        this.reconfigureLock.readLock().lock();
        if (this.isStopping) {
            this.reconfigureLock.readLock().unlock();
            return false;
        }
        return true;
    }

    @Override
    public void afterLogEvent() {
        this.reconfigureLock.readLock().unlock();
    }

    @Override
    public void beforeStopAppenders() {
        this.reconfigureLock.writeLock().lock();
        try {
            this.isStopping = true;
            return;
        }
        finally {
            this.reconfigureLock.writeLock().unlock();
        }
    }

    @Override
    public void beforeStopConfiguration(Configuration configuration) {
    }
}

