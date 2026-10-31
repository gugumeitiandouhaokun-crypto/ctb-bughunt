/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.logging.log4j.core.AbstractLogEvent;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.async.ArrayBlockingQueueFactory;
import org.apache.logging.log4j.core.async.AsyncQueueFullMessageUtil;
import org.apache.logging.log4j.core.async.AsyncQueueFullPolicy;
import org.apache.logging.log4j.core.async.AsyncQueueFullPolicyFactory;
import org.apache.logging.log4j.core.async.BlockingQueueFactory;
import org.apache.logging.log4j.core.async.DiscardingAsyncQueueFullPolicy;
import org.apache.logging.log4j.core.async.InternalAsyncUtil;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAliases;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.filter.AbstractFilterable;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.util.Log4jThread;
import org.apache.logging.log4j.spi.AbstractLogger;

@Plugin(name="Async", category="Core", elementType="appender", printObject=true)
public final class AsyncAppender
extends AbstractAppender {
    private static final int DEFAULT_QUEUE_SIZE = 1024;
    private static final LogEvent SHUTDOWN_LOG_EVENT = new AbstractLogEvent(){
        private static final long serialVersionUID = -1761035149477086330L;
    };
    private static final AtomicLong THREAD_SEQUENCE = new AtomicLong(1L);
    private final BlockingQueue<LogEvent> queue;
    private final int queueSize;
    private final boolean blocking;
    private final long shutdownTimeout;
    private final Configuration config;
    private final AppenderRef[] appenderRefs;
    private final String errorRef;
    private final boolean includeLocation;
    private AppenderControl errorAppender;
    private AsyncThread thread;
    private AsyncQueueFullPolicy asyncQueueFullPolicy;

    private AsyncAppender(String string, Filter filter, AppenderRef[] appenderRefArray, String string2, int n, boolean bl, boolean bl2, long l, Configuration configuration, boolean bl3, BlockingQueueFactory<LogEvent> blockingQueueFactory, Property[] propertyArray) {
        super(string, filter, null, bl2, propertyArray);
        this.queue = blockingQueueFactory.create(n);
        this.queueSize = n;
        this.blocking = bl;
        this.shutdownTimeout = l;
        this.config = configuration;
        this.appenderRefs = appenderRefArray;
        this.errorRef = string2;
        this.includeLocation = bl3;
    }

    @Override
    public final void start() {
        Map<String, Appender> map = this.config.getAppenders();
        ArrayList<AppenderControl> arrayList = new ArrayList<AppenderControl>();
        Object object = this.appenderRefs;
        int n = this.appenderRefs.length;
        for (int i2 = 0; i2 < n; ++i2) {
            AppenderRef appenderRef = object[i2];
            Appender appender = map.get(appenderRef.getRef());
            if (appender != null) {
                arrayList.add(new AppenderControl(appender, appenderRef.getLevel(), appenderRef.getFilter()));
                continue;
            }
            LOGGER.error("No appender named {} was configured", (Object)appenderRef);
        }
        if (this.errorRef != null) {
            object = map.get(this.errorRef);
            if (object != null) {
                this.errorAppender = new AppenderControl((Appender)object, null, null);
            } else {
                LOGGER.error("Unable to set up error Appender. No appender named {} was configured", (Object)this.errorRef);
            }
        }
        if (arrayList.size() > 0) {
            this.thread = new AsyncThread(arrayList, this.queue);
            this.thread.setName("AsyncAppender-" + this.getName());
        } else if (this.errorRef == null) {
            throw new ConfigurationException("No appenders are available for AsyncAppender " + this.getName());
        }
        this.asyncQueueFullPolicy = AsyncQueueFullPolicyFactory.create();
        this.thread.start();
        super.start();
    }

    @Override
    public final boolean stop(long l, TimeUnit timeUnit) {
        this.setStopping();
        super.stop(l, timeUnit, false);
        LOGGER.trace("AsyncAppender stopping. Queue still has {} events.", (Object)this.queue.size());
        this.thread.shutdown();
        try {
            this.thread.join(this.shutdownTimeout);
        }
        catch (InterruptedException interruptedException) {
            LOGGER.warn("Interrupted while stopping AsyncAppender {}", (Object)this.getName());
        }
        LOGGER.trace("AsyncAppender stopped. Queue has {} events.", (Object)this.queue.size());
        if (DiscardingAsyncQueueFullPolicy.getDiscardCount(this.asyncQueueFullPolicy) > 0L) {
            LOGGER.trace("AsyncAppender: {} discarded {} events.", (Object)this.asyncQueueFullPolicy, (Object)DiscardingAsyncQueueFullPolicy.getDiscardCount(this.asyncQueueFullPolicy));
        }
        this.setStopped();
        return true;
    }

    @Override
    public final void append(LogEvent logEvent) {
        if (!this.isStarted()) {
            throw new IllegalStateException("AsyncAppender " + this.getName() + " is not active");
        }
        Log4jLogEvent log4jLogEvent = Log4jLogEvent.createMemento(logEvent, this.includeLocation);
        InternalAsyncUtil.makeMessageImmutable(logEvent.getMessage());
        if (!this.transfer(log4jLogEvent)) {
            if (this.blocking) {
                if (AbstractLogger.getRecursionDepth() > 1) {
                    AsyncQueueFullMessageUtil.logWarningToStatusLogger();
                    this.logMessageInCurrentThread(logEvent);
                    return;
                }
                this.asyncQueueFullPolicy.getRoute(this.thread.getId(), log4jLogEvent.getLevel()).logMessage(this, (LogEvent)log4jLogEvent);
                return;
            }
            this.error("Appender " + this.getName() + " is unable to write primary appenders. queue is full");
            this.logToErrorAppenderIfNecessary(false, log4jLogEvent);
        }
    }

    private boolean transfer(LogEvent logEvent) {
        if (this.queue instanceof TransferQueue) {
            return ((TransferQueue)this.queue).tryTransfer(logEvent);
        }
        return this.queue.offer(logEvent);
    }

    public final void logMessageInCurrentThread(LogEvent logEvent) {
        logEvent.setEndOfBatch(this.queue.isEmpty());
        boolean bl = this.thread.callAppenders(logEvent);
        this.logToErrorAppenderIfNecessary(bl, logEvent);
    }

    public final void logMessageInBackgroundThread(LogEvent logEvent) {
        try {
            this.queue.put(logEvent);
            return;
        }
        catch (InterruptedException interruptedException) {
            boolean bl = this.handleInterruptedException(logEvent);
            this.logToErrorAppenderIfNecessary(bl, logEvent);
            return;
        }
    }

    private boolean handleInterruptedException(LogEvent logEvent) {
        boolean bl = this.queue.offer(logEvent);
        if (!bl) {
            LOGGER.warn("Interrupted while waiting for a free slot in the AsyncAppender LogEvent-queue {}", (Object)this.getName());
        }
        Thread.currentThread().interrupt();
        return bl;
    }

    private void logToErrorAppenderIfNecessary(boolean bl, LogEvent logEvent) {
        if (!bl && this.errorAppender != null) {
            this.errorAppender.callAppender(logEvent);
        }
    }

    @Deprecated
    public static AsyncAppender createAppender(AppenderRef[] appenderRefArray, String string, boolean bl, long l, int n, String string2, boolean bl2, Filter filter, Configuration configuration, boolean bl3) {
        if (string2 == null) {
            LOGGER.error("No name provided for AsyncAppender");
            return null;
        }
        if (appenderRefArray == null) {
            LOGGER.error("No appender references provided to AsyncAppender {}", (Object)string2);
        }
        return new AsyncAppender(string2, filter, appenderRefArray, string, n, bl, bl3, l, configuration, bl2, new ArrayBlockingQueueFactory<LogEvent>(), null);
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    public final String[] getAppenderRefStrings() {
        String[] stringArray = new String[this.appenderRefs.length];
        for (int i2 = 0; i2 < stringArray.length; ++i2) {
            stringArray[i2] = this.appenderRefs[i2].getRef();
        }
        return stringArray;
    }

    public final boolean isIncludeLocation() {
        return this.includeLocation;
    }

    public final boolean isBlocking() {
        return this.blocking;
    }

    public final String getErrorRef() {
        return this.errorRef;
    }

    public final int getQueueCapacity() {
        return this.queueSize;
    }

    public final int getQueueRemainingCapacity() {
        return this.queue.remainingCapacity();
    }

    public final int getQueueSize() {
        return this.queue.size();
    }

    class AsyncThread
    extends Log4jThread {
        private volatile boolean shutdown;
        private final List<AppenderControl> appenders;
        private final BlockingQueue<LogEvent> queue;

        public AsyncThread(List<AppenderControl> list, BlockingQueue<LogEvent> blockingQueue) {
            super("AsyncAppender-" + THREAD_SEQUENCE.getAndIncrement());
            this.shutdown = false;
            this.appenders = list;
            this.queue = blockingQueue;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            while (!this.shutdown) {
                LogEvent logEvent;
                try {
                    logEvent = this.queue.take();
                    if (logEvent == SHUTDOWN_LOG_EVENT) {
                        this.shutdown = true;
                        continue;
                    }
                }
                catch (InterruptedException interruptedException) {
                    break;
                }
                logEvent.setEndOfBatch(this.queue.isEmpty());
                if (this.callAppenders(logEvent) || AsyncAppender.this.errorAppender == null) continue;
                try {
                    AsyncAppender.this.errorAppender.callAppender(logEvent);
                }
                catch (Exception exception) {}
            }
            LOGGER.trace("AsyncAppender.AsyncThread shutting down. Processing remaining {} queue events.", (Object)this.queue.size());
            int n = 0;
            int n2 = 0;
            while (!this.queue.isEmpty()) {
                try {
                    LogEvent logEvent = this.queue.take();
                    if (logEvent instanceof Log4jLogEvent) {
                        logEvent = (Log4jLogEvent)logEvent;
                        ((Log4jLogEvent)logEvent).setEndOfBatch(this.queue.isEmpty());
                        this.callAppenders(logEvent);
                        ++n;
                        continue;
                    }
                    ++n2;
                    LOGGER.trace("Ignoring event of class {}", (Object)logEvent.getClass().getName());
                }
                catch (InterruptedException interruptedException) {}
            }
            LOGGER.trace("AsyncAppender.AsyncThread stopped. Queue has {} events remaining. Processed {} and ignored {} events since shutdown started.", (Object)this.queue.size(), (Object)n, (Object)n2);
        }

        boolean callAppenders(LogEvent logEvent) {
            boolean bl = false;
            for (AppenderControl appenderControl : this.appenders) {
                try {
                    appenderControl.callAppender(logEvent);
                    bl = true;
                }
                catch (Exception exception) {}
            }
            return bl;
        }

        public void shutdown() {
            this.shutdown = true;
            if (this.queue.isEmpty()) {
                this.queue.offer(SHUTDOWN_LOG_EVENT);
            }
            if (this.getState() == Thread.State.TIMED_WAITING || this.getState() == Thread.State.WAITING) {
                this.interrupt();
            }
        }
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractFilterable.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<AsyncAppender> {
        @PluginElement(value="AppenderRef")
        @Required(message="No appender references provided to AsyncAppender")
        private AppenderRef[] appenderRefs;
        @PluginBuilderAttribute
        @PluginAliases(value={"error-ref"})
        private String errorRef;
        @PluginBuilderAttribute
        private boolean blocking = true;
        @PluginBuilderAttribute
        private long shutdownTimeout = 0L;
        @PluginBuilderAttribute
        private int bufferSize = 1024;
        @PluginBuilderAttribute
        @Required(message="No name provided for AsyncAppender")
        private String name;
        @PluginBuilderAttribute
        private boolean includeLocation = false;
        @PluginConfiguration
        private Configuration configuration;
        @PluginBuilderAttribute
        private boolean ignoreExceptions = true;
        @PluginElement(value="BlockingQueueFactory")
        private BlockingQueueFactory<LogEvent> blockingQueueFactory = new ArrayBlockingQueueFactory<LogEvent>();

        public Builder setAppenderRefs(AppenderRef[] appenderRefArray) {
            this.appenderRefs = appenderRefArray;
            return this;
        }

        public Builder setErrorRef(String string) {
            this.errorRef = string;
            return this;
        }

        public Builder setBlocking(boolean bl) {
            this.blocking = bl;
            return this;
        }

        public Builder setShutdownTimeout(long l) {
            this.shutdownTimeout = l;
            return this;
        }

        public Builder setBufferSize(int n) {
            this.bufferSize = n;
            return this;
        }

        public Builder setName(String string) {
            this.name = string;
            return this;
        }

        public Builder setIncludeLocation(boolean bl) {
            this.includeLocation = bl;
            return this;
        }

        public Builder setConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder setIgnoreExceptions(boolean bl) {
            this.ignoreExceptions = bl;
            return this;
        }

        public Builder setBlockingQueueFactory(BlockingQueueFactory<LogEvent> blockingQueueFactory) {
            this.blockingQueueFactory = blockingQueueFactory;
            return this;
        }

        @Override
        public AsyncAppender build() {
            return new AsyncAppender(this.name, this.getFilter(), this.appenderRefs, this.errorRef, this.bufferSize, this.blocking, this.ignoreExceptions, this.shutdownTimeout, this.configuration, this.includeLocation, this.blockingQueueFactory, this.getPropertyArray());
        }
    }
}

