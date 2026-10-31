/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.db;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.logging.log4j.LoggingException;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.db.AbstractDatabaseManager;
import org.apache.logging.log4j.core.config.Property;

public abstract class AbstractDatabaseAppender<T extends AbstractDatabaseManager>
extends AbstractAppender {
    public static final int DEFAULT_RECONNECT_INTERVAL_MILLIS = 5000;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = this.lock.readLock();
    private final Lock writeLock = this.lock.writeLock();
    private T manager;

    @Deprecated
    protected AbstractDatabaseAppender(String string, Filter filter, boolean bl, T t) {
        super(string, filter, null, bl, Property.EMPTY_ARRAY);
        this.manager = t;
    }

    public AbstractDatabaseAppender(String string, Filter filter, Layout<? extends Serializable> layout, boolean bl, Property[] propertyArray, T t) {
        super(string, filter, layout, bl, propertyArray);
        this.manager = t;
    }

    @Deprecated
    protected AbstractDatabaseAppender(String string, Filter filter, Layout<? extends Serializable> layout, boolean bl, T t) {
        super(string, filter, layout, bl, Property.EMPTY_ARRAY);
        this.manager = t;
    }

    @Override
    public final void append(LogEvent logEvent) {
        this.readLock.lock();
        try {
            ((AbstractDatabaseManager)this.getManager()).write(logEvent, this.toSerializable(logEvent));
            return;
        }
        catch (LoggingException loggingException) {
            LOGGER.error("Unable to write to database [{}] for appender [{}].", (Object)((AbstractManager)this.getManager()).getName(), (Object)this.getName(), (Object)loggingException);
            throw loggingException;
        }
        catch (Exception exception) {
            LOGGER.error("Unable to write to database [{}] for appender [{}].", (Object)((AbstractManager)this.getManager()).getName(), (Object)this.getName(), (Object)exception);
            throw new AppenderLoggingException("Unable to write to database in appender: " + exception.getMessage(), exception);
        }
        finally {
            this.readLock.unlock();
        }
    }

    public final Layout<LogEvent> getLayout() {
        return null;
    }

    public final T getManager() {
        return this.manager;
    }

    protected final void replaceManager(T t) {
        this.writeLock.lock();
        try {
            T t2 = this.getManager();
            if (!((AbstractDatabaseManager)t).isRunning()) {
                ((AbstractDatabaseManager)t).startup();
            }
            this.manager = t;
            ((AbstractManager)t2).close();
            return;
        }
        finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public final void start() {
        if (this.getManager() == null) {
            LOGGER.error("No AbstractDatabaseManager set for the appender named [{}].", (Object)this.getName());
        }
        super.start();
        if (this.getManager() != null) {
            ((AbstractDatabaseManager)this.getManager()).startup();
        }
    }

    @Override
    public boolean stop(long l, TimeUnit timeUnit) {
        this.setStopping();
        boolean bl = super.stop(l, timeUnit, false);
        if (this.getManager() != null) {
            bl &= ((AbstractManager)this.getManager()).stop(l, timeUnit);
        }
        this.setStopped();
        return bl;
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractAppender.Builder<B> {
    }
}

