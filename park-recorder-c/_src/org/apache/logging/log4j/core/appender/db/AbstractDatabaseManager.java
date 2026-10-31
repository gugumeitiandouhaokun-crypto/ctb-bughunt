/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.db;

import java.io.Flushable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.ManagerFactory;

public abstract class AbstractDatabaseManager
extends AbstractManager
implements Flushable {
    private final ArrayList<LogEvent> buffer;
    private final int bufferSize;
    private final Layout<? extends Serializable> layout;
    private boolean running;

    public static <M extends AbstractDatabaseManager, T extends AbstractFactoryData> M getManager(String string, T t, ManagerFactory<M, T> managerFactory) {
        return (M)((AbstractDatabaseManager)AbstractManager.getManager(string, managerFactory, t));
    }

    public AbstractDatabaseManager(String string, int n) {
        this(string, n, null);
    }

    protected AbstractDatabaseManager(String string, int n, Layout<? extends Serializable> layout) {
        super(null, string);
        this.bufferSize = n;
        this.buffer = new ArrayList(n + 1);
        this.layout = layout;
    }

    protected void buffer(LogEvent logEvent) {
        this.buffer.add(logEvent.toImmutable());
        if (this.buffer.size() >= this.bufferSize || logEvent.isEndOfBatch()) {
            this.flush();
        }
    }

    protected abstract boolean commitAndClose();

    protected abstract void connectAndStart();

    @Override
    public final synchronized void flush() {
        if (this.isRunning() && this.isBuffered()) {
            this.connectAndStart();
            try {
                for (LogEvent logEvent : this.buffer) {
                    this.writeInternal(logEvent, this.layout != null ? this.layout.toSerializable(logEvent) : null);
                }
                return;
            }
            finally {
                this.commitAndClose();
                this.buffer.clear();
            }
        }
    }

    protected boolean isBuffered() {
        return this.bufferSize > 0;
    }

    public final boolean isRunning() {
        return this.running;
    }

    @Override
    public final boolean releaseSub(long l, TimeUnit timeUnit) {
        return this.shutdown();
    }

    public final synchronized boolean shutdown() {
        boolean bl = true;
        this.flush();
        if (this.isRunning()) {
            try {
                bl = true & this.shutdownInternal();
            }
            catch (Exception exception) {
                this.logWarn("Caught exception while performing database shutdown operations", exception);
                bl = false;
            }
            finally {
                this.running = false;
            }
        }
        return bl;
    }

    protected abstract boolean shutdownInternal();

    public final synchronized void startup() {
        if (!this.isRunning()) {
            try {
                this.startupInternal();
                this.running = true;
                return;
            }
            catch (Exception exception) {
                this.logError("Could not perform database startup operations", exception);
            }
        }
    }

    protected abstract void startupInternal();

    public final String toString() {
        return this.getName();
    }

    @Deprecated
    public final synchronized void write(LogEvent logEvent) {
        this.write(logEvent, null);
    }

    public final synchronized void write(LogEvent logEvent, Serializable serializable) {
        if (this.isBuffered()) {
            this.buffer(logEvent);
            return;
        }
        this.writeThrough(logEvent, serializable);
    }

    @Deprecated
    protected void writeInternal(LogEvent logEvent) {
        this.writeInternal(logEvent, null);
    }

    protected abstract void writeInternal(LogEvent var1, Serializable var2);

    protected void writeThrough(LogEvent logEvent, Serializable serializable) {
        this.connectAndStart();
        try {
            this.writeInternal(logEvent, serializable);
            return;
        }
        finally {
            this.commitAndClose();
        }
    }

    public static abstract class AbstractFactoryData {
        private final int bufferSize;
        private final Layout<? extends Serializable> layout;

        public AbstractFactoryData(int n, Layout<? extends Serializable> layout) {
            this.bufferSize = n;
            this.layout = layout;
        }

        public int getBufferSize() {
            return this.bufferSize;
        }

        public Layout<? extends Serializable> getLayout() {
            return this.layout;
        }
    }
}

