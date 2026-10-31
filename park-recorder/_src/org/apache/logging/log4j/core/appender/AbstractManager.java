/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.status.StatusLogger;

public abstract class AbstractManager
implements AutoCloseable {
    public static final Logger LOGGER = StatusLogger.getLogger();
    private static final Map<String, AbstractManager> MAP = new HashMap<String, AbstractManager>();
    private static final Lock LOCK = new ReentrantLock();
    protected int count;
    private final String name;
    private final LoggerContext loggerContext;

    public AbstractManager(LoggerContext loggerContext, String string) {
        this.loggerContext = loggerContext;
        this.name = string;
        LOGGER.debug("Starting {} {}", (Object)this.getClass().getSimpleName(), (Object)string);
    }

    @Override
    public void close() {
        this.stop(0L, AbstractLifeCycle.DEFAULT_STOP_TIMEUNIT);
    }

    public boolean stop(long l, TimeUnit timeUnit) {
        boolean bl = true;
        LOCK.lock();
        try {
            --this.count;
            if (this.count <= 0) {
                MAP.remove(this.name);
                LOGGER.debug("Shutting down {} {}", (Object)this.getClass().getSimpleName(), (Object)this.getName());
                bl = this.releaseSub(l, timeUnit);
                LOGGER.debug("Shut down {} {}, all resources released: {}", (Object)this.getClass().getSimpleName(), (Object)this.getName(), (Object)bl);
            }
        }
        finally {
            LOCK.unlock();
        }
        return bl;
    }

    public static <M extends AbstractManager, T> M getManager(String object, ManagerFactory<M, T> managerFactory, T t) {
        LOCK.lock();
        try {
            AbstractManager abstractManager = MAP.get(object);
            if (abstractManager == null) {
                abstractManager = (AbstractManager)managerFactory.createManager((String)object, t);
                if (abstractManager == null) {
                    throw new IllegalStateException("ManagerFactory [" + managerFactory + "] unable to create manager for [" + (String)object + "] with data [" + t + "]");
                }
                MAP.put((String)object, abstractManager);
            } else {
                abstractManager.updateData(t);
            }
            ++abstractManager.count;
            object = abstractManager;
            return (M)object;
        }
        finally {
            LOCK.unlock();
        }
    }

    public void updateData(Object object) {
    }

    public static boolean hasManager(String string) {
        LOCK.lock();
        try {
            boolean bl = MAP.containsKey(string);
            return bl;
        }
        finally {
            LOCK.unlock();
        }
    }

    public static <M extends AbstractManager> M narrow(Class<M> clazz, AbstractManager abstractManager) {
        if (clazz.isAssignableFrom(abstractManager.getClass())) {
            return (M)abstractManager;
        }
        throw new ConfigurationException("Configuration has multiple incompatible Appenders pointing to the same resource '" + abstractManager.getName() + "'");
    }

    public static StatusLogger logger() {
        return StatusLogger.getLogger();
    }

    protected boolean releaseSub(long l, TimeUnit timeUnit) {
        return true;
    }

    protected int getCount() {
        return this.count;
    }

    public LoggerContext getLoggerContext() {
        return this.loggerContext;
    }

    @Deprecated
    public void release() {
        this.close();
    }

    public String getName() {
        return this.name;
    }

    public Map<String, String> getContentFormat() {
        return new HashMap<String, String>();
    }

    protected void log(Level level, String object, Throwable throwable) {
        object = LOGGER.getMessageFactory().newMessage("{} {} {}: {}", this.getClass().getSimpleName(), this.getName(), object, throwable);
        LOGGER.log(level, (Message)object, throwable);
    }

    protected void logDebug(String string, Throwable throwable) {
        this.log(Level.DEBUG, string, throwable);
    }

    protected void logError(String string, Throwable throwable) {
        this.log(Level.ERROR, string, throwable);
    }

    protected void logWarn(String string, Throwable throwable) {
        this.log(Level.WARN, string, throwable);
    }
}

