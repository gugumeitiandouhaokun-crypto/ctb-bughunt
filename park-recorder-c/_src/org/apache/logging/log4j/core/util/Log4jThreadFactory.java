/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.core.util.Log4jThread;

public class Log4jThreadFactory
implements ThreadFactory {
    private static final String PREFIX = "TF-";
    private static final AtomicInteger FACTORY_NUMBER = new AtomicInteger(1);
    private static final AtomicInteger THREAD_NUMBER = new AtomicInteger(1);
    private final boolean daemon;
    private final ThreadGroup group;
    private final int priority;
    private final String threadNamePrefix;

    public static Log4jThreadFactory createDaemonThreadFactory(String string) {
        return new Log4jThreadFactory(string, true, 5);
    }

    public static Log4jThreadFactory createThreadFactory(String string) {
        return new Log4jThreadFactory(string, false, 5);
    }

    public Log4jThreadFactory(String object, boolean bl, int n) {
        this.threadNamePrefix = PREFIX + FACTORY_NUMBER.getAndIncrement() + "-" + (String)object + "-";
        this.daemon = bl;
        this.priority = n;
        object = System.getSecurityManager();
        this.group = object != null ? ((SecurityManager)object).getThreadGroup() : Thread.currentThread().getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable runnable) {
        if (((Thread)(runnable = new Log4jThread(this.group, runnable, this.threadNamePrefix + THREAD_NUMBER.getAndIncrement(), 0L))).isDaemon() != this.daemon) {
            ((Thread)runnable).setDaemon(this.daemon);
        }
        if (((Thread)runnable).getPriority() != this.priority) {
            ((Thread)runnable).setPriority(this.priority);
        }
        return runnable;
    }
}

