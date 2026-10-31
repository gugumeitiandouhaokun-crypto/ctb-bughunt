/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class BasicThreadFactory
implements ThreadFactory {
    private final AtomicLong threadCounter;
    private final ThreadFactory wrappedFactory;
    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
    private final String namingPattern;
    private final Integer priority;
    private final Boolean daemonFlag;

    private BasicThreadFactory(Builder builder) {
        this.wrappedFactory = builder.wrappedFactory == null ? Executors.defaultThreadFactory() : builder.wrappedFactory;
        this.namingPattern = builder.namingPattern;
        this.priority = builder.priority;
        this.daemonFlag = builder.daemonFlag;
        this.uncaughtExceptionHandler = builder.exceptionHandler;
        this.threadCounter = new AtomicLong();
    }

    public final ThreadFactory getWrappedFactory() {
        return this.wrappedFactory;
    }

    public final String getNamingPattern() {
        return this.namingPattern;
    }

    public final Boolean getDaemonFlag() {
        return this.daemonFlag;
    }

    public final Integer getPriority() {
        return this.priority;
    }

    public final Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return this.uncaughtExceptionHandler;
    }

    public long getThreadCount() {
        return this.threadCounter.get();
    }

    @Override
    public Thread newThread(Runnable runnable) {
        runnable = this.getWrappedFactory().newThread(runnable);
        this.initializeThread((Thread)runnable);
        return runnable;
    }

    private void initializeThread(Thread thread) {
        if (this.getNamingPattern() != null) {
            Long l = this.threadCounter.incrementAndGet();
            thread.setName(String.format(this.getNamingPattern(), l));
        }
        if (this.getUncaughtExceptionHandler() != null) {
            thread.setUncaughtExceptionHandler(this.getUncaughtExceptionHandler());
        }
        if (this.getPriority() != null) {
            thread.setPriority(this.getPriority());
        }
        if (this.getDaemonFlag() != null) {
            thread.setDaemon(this.getDaemonFlag());
        }
    }

    public static class Builder
    implements org.apache.commons.lang3.builder.Builder<BasicThreadFactory> {
        private ThreadFactory wrappedFactory;
        private Thread.UncaughtExceptionHandler exceptionHandler;
        private String namingPattern;
        private Integer priority;
        private Boolean daemonFlag;

        public Builder wrappedFactory(ThreadFactory threadFactory) {
            if (threadFactory == null) {
                throw new NullPointerException("Wrapped ThreadFactory must not be null!");
            }
            this.wrappedFactory = threadFactory;
            return this;
        }

        public Builder namingPattern(String string) {
            if (string == null) {
                throw new NullPointerException("Naming pattern must not be null!");
            }
            this.namingPattern = string;
            return this;
        }

        public Builder daemon(boolean bl) {
            this.daemonFlag = bl;
            return this;
        }

        public Builder priority(int n) {
            this.priority = n;
            return this;
        }

        public Builder uncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
            if (uncaughtExceptionHandler == null) {
                throw new NullPointerException("Uncaught exception handler must not be null!");
            }
            this.exceptionHandler = uncaughtExceptionHandler;
            return this;
        }

        public void reset() {
            this.wrappedFactory = null;
            this.exceptionHandler = null;
            this.namingPattern = null;
            this.priority = null;
            this.daemonFlag = null;
        }

        @Override
        public BasicThreadFactory build() {
            BasicThreadFactory basicThreadFactory = new BasicThreadFactory(this);
            this.reset();
            return basicThreadFactory;
        }
    }
}

