/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LifeCycle2;
import org.apache.logging.log4j.core.util.Cancellable;
import org.apache.logging.log4j.core.util.ShutdownCallbackRegistry;
import org.apache.logging.log4j.status.StatusLogger;

public class DefaultShutdownCallbackRegistry
implements Runnable,
LifeCycle2,
ShutdownCallbackRegistry {
    protected static final Logger LOGGER = StatusLogger.getLogger();
    private final AtomicReference<LifeCycle.State> state = new AtomicReference<LifeCycle.State>(LifeCycle.State.INITIALIZED);
    private final ThreadFactory threadFactory;
    private final Collection<Cancellable> hooks = new CopyOnWriteArrayList<Cancellable>();
    private Reference<Thread> shutdownHookRef;

    public DefaultShutdownCallbackRegistry() {
        this(Executors.defaultThreadFactory());
    }

    protected DefaultShutdownCallbackRegistry(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Override
    public void run() {
        if (this.state.compareAndSet(LifeCycle.State.STARTED, LifeCycle.State.STOPPING)) {
            for (Runnable throwable2 : this.hooks) {
                try {
                    throwable2.run();
                }
                catch (Throwable throwable) {
                    try {
                        LOGGER.error(SHUTDOWN_HOOK_MARKER, "Caught exception executing shutdown hook {}", (Object)throwable2, (Object)throwable);
                    }
                    catch (Throwable throwable3) {
                        System.err.println("Caught exception " + throwable3.getClass() + " logging exception " + throwable.getClass());
                        throwable.printStackTrace();
                    }
                }
            }
            this.state.set(LifeCycle.State.STOPPED);
        }
    }

    @Override
    public Cancellable addShutdownCallback(Runnable runnable) {
        if (this.isStarted()) {
            runnable = new RegisteredCancellable(runnable, this.hooks);
            this.hooks.add((Cancellable)runnable);
            return runnable;
        }
        throw new IllegalStateException("Cannot add new shutdown hook as this is not started. Current state: " + this.state.get().name());
    }

    @Override
    public void initialize() {
    }

    @Override
    public void start() {
        if (this.state.compareAndSet(LifeCycle.State.INITIALIZED, LifeCycle.State.STARTING)) {
            try {
                DefaultShutdownCallbackRegistry defaultShutdownCallbackRegistry = this;
                defaultShutdownCallbackRegistry.addShutdownHook(defaultShutdownCallbackRegistry.threadFactory.newThread(this));
                this.state.set(LifeCycle.State.STARTED);
                return;
            }
            catch (IllegalStateException illegalStateException) {
                this.state.set(LifeCycle.State.STOPPED);
                throw illegalStateException;
            }
            catch (Exception exception) {
                LOGGER.catching(exception);
                this.state.set(LifeCycle.State.STOPPED);
            }
        }
    }

    private void addShutdownHook(Thread thread) {
        this.shutdownHookRef = new WeakReference<Thread>(thread);
        Runtime.getRuntime().addShutdownHook(thread);
    }

    @Override
    public void stop() {
        this.stop(0L, AbstractLifeCycle.DEFAULT_STOP_TIMEUNIT);
    }

    @Override
    public boolean stop(long l, TimeUnit timeUnit) {
        if (this.state.compareAndSet(LifeCycle.State.STARTED, LifeCycle.State.STOPPING)) {
            try {
                this.removeShutdownHook();
            }
            finally {
                this.state.set(LifeCycle.State.STOPPED);
            }
        }
        return true;
    }

    private void removeShutdownHook() {
        Thread thread = this.shutdownHookRef.get();
        if (thread != null) {
            Runtime.getRuntime().removeShutdownHook(thread);
            this.shutdownHookRef.enqueue();
        }
    }

    @Override
    public LifeCycle.State getState() {
        return this.state.get();
    }

    @Override
    public boolean isStarted() {
        return this.state.get() == LifeCycle.State.STARTED;
    }

    @Override
    public boolean isStopped() {
        return this.state.get() == LifeCycle.State.STOPPED;
    }

    static class RegisteredCancellable
    implements Cancellable {
        private final Reference<Runnable> hook;
        private Collection<Cancellable> registered;

        RegisteredCancellable(Runnable runnable, Collection<Cancellable> collection) {
            this.registered = collection;
            this.hook = new SoftReference<Runnable>(runnable);
        }

        @Override
        public void cancel() {
            this.hook.clear();
            this.registered.remove(this);
            this.registered = null;
        }

        @Override
        public void run() {
            Runnable runnable = this.hook.get();
            if (runnable != null) {
                runnable.run();
                this.hook.clear();
            }
        }

        public String toString() {
            return String.valueOf(this.hook.get());
        }
    }
}

