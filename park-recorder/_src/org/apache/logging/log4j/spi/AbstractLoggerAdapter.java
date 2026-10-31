/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.spi;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.LoggerAdapter;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.spi.LoggerContextShutdownAware;
import org.apache.logging.log4j.spi.LoggerContextShutdownEnabled;
import org.apache.logging.log4j.util.LoaderUtil;

public abstract class AbstractLoggerAdapter<L>
implements LoggerAdapter<L>,
LoggerContextShutdownAware {
    protected final Map<LoggerContext, ConcurrentMap<String, L>> registry = new ConcurrentHashMap<LoggerContext, ConcurrentMap<String, L>>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    @Override
    public L getLogger(String string) {
        LoggerContext loggerContext = this.getContext();
        ConcurrentMap<String, L> concurrentMap = this.getLoggersInContext(loggerContext);
        Object v = concurrentMap.get(string);
        if (v != null) {
            return (L)v;
        }
        concurrentMap.putIfAbsent(string, this.newLogger(string, loggerContext));
        return (L)concurrentMap.get(string);
    }

    @Override
    public void contextShutdown(LoggerContext loggerContext) {
        this.registry.remove(loggerContext);
    }

    public ConcurrentMap<String, L> getLoggersInContext(LoggerContext object) {
        ConcurrentMap<String, L> concurrentMap;
        this.lock.readLock().lock();
        try {
            concurrentMap = this.registry.get(object);
        }
        finally {
            this.lock.readLock().unlock();
        }
        if (concurrentMap != null) {
            return concurrentMap;
        }
        this.lock.writeLock().lock();
        try {
            concurrentMap = this.registry.get(object);
            if (concurrentMap == null) {
                concurrentMap = new ConcurrentHashMap<String, L>();
                this.registry.put((LoggerContext)object, concurrentMap);
                if (object instanceof LoggerContextShutdownEnabled) {
                    ((LoggerContextShutdownEnabled)object).addShutdownListener(this);
                }
            }
            object = concurrentMap;
            return object;
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    public Set<LoggerContext> getLoggerContexts() {
        return new HashSet<LoggerContext>(this.registry.keySet());
    }

    protected abstract L newLogger(String var1, LoggerContext var2);

    protected abstract LoggerContext getContext();

    protected LoggerContext getContext(Class<?> clazz) {
        ClassLoader classLoader = null;
        if (clazz != null) {
            classLoader = clazz.getClassLoader();
        }
        if (classLoader == null) {
            classLoader = LoaderUtil.getThreadContextClassLoader();
        }
        return LogManager.getContext(classLoader, false);
    }

    @Override
    public void close() {
        this.lock.writeLock().lock();
        try {
            this.registry.clear();
            return;
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }
}

