/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.async;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.async.AsyncLoggerConfigDelegate;
import org.apache.logging.log4j.core.async.AsyncQueueFullMessageUtil;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.jmx.RingBufferAdmin;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.spi.AbstractLogger;

@Plugin(name="asyncLogger", category="Core", printObject=true)
public class AsyncLoggerConfig
extends LoggerConfig {
    private static final ThreadLocal<Boolean> ASYNC_LOGGER_ENTERED = new ThreadLocal<Boolean>(){

        @Override
        protected final Boolean initialValue() {
            return Boolean.FALSE;
        }
    };
    private final AsyncLoggerConfigDelegate delegate;

    protected AsyncLoggerConfig(String string, List<AppenderRef> list, Filter filter, Level level, boolean bl, Property[] propertyArray, Configuration configuration, boolean bl2) {
        super(string, list, filter, level, bl, propertyArray, configuration, bl2);
        this.delegate = configuration.getAsyncLoggerConfigDelegate();
        this.delegate.setLogEventFactory(this.getLogEventFactory());
    }

    @Override
    public void log(LogEvent logEvent, LoggerConfig.LoggerConfigPredicate loggerConfigPredicate) {
        if (loggerConfigPredicate == LoggerConfig.LoggerConfigPredicate.ALL && ASYNC_LOGGER_ENTERED.get() == Boolean.FALSE && this.hasAppenders()) {
            ASYNC_LOGGER_ENTERED.set(Boolean.TRUE);
            try {
                super.log(logEvent, LoggerConfig.LoggerConfigPredicate.SYNCHRONOUS_ONLY);
                this.logToAsyncDelegate(logEvent);
                return;
            }
            finally {
                ASYNC_LOGGER_ENTERED.set(Boolean.FALSE);
            }
        }
        super.log(logEvent, loggerConfigPredicate);
    }

    @Override
    public void callAppenders(LogEvent logEvent) {
        super.callAppenders(logEvent);
    }

    private void logToAsyncDelegate(LogEvent logEvent) {
        if (!this.isFiltered(logEvent)) {
            this.populateLazilyInitializedFields(logEvent);
            if (!this.delegate.tryEnqueue(logEvent, this)) {
                this.handleQueueFull(logEvent);
            }
        }
    }

    private void handleQueueFull(LogEvent logEvent) {
        if (AbstractLogger.getRecursionDepth() > 1) {
            AsyncQueueFullMessageUtil.logWarningToStatusLogger();
            this.logToAsyncLoggerConfigsOnCurrentThread(logEvent);
            return;
        }
        this.delegate.getEventRoute(logEvent.getLevel()).logMessage(this, logEvent);
    }

    private void populateLazilyInitializedFields(LogEvent logEvent) {
        logEvent.getSource();
        logEvent.getThreadName();
    }

    void logInBackgroundThread(LogEvent logEvent) {
        this.delegate.enqueueEvent(logEvent, this);
    }

    void logToAsyncLoggerConfigsOnCurrentThread(LogEvent logEvent) {
        this.log(logEvent, LoggerConfig.LoggerConfigPredicate.ASYNCHRONOUS_ONLY);
    }

    private String displayName() {
        if ("".equals(this.getName())) {
            return "root";
        }
        return this.getName();
    }

    @Override
    public void start() {
        LOGGER.trace("AsyncLoggerConfig[{}] starting...", (Object)this.displayName());
        super.start();
    }

    @Override
    public boolean stop(long l, TimeUnit timeUnit) {
        this.setStopping();
        super.stop(l, timeUnit, false);
        LOGGER.trace("AsyncLoggerConfig[{}] stopping...", (Object)this.displayName());
        this.setStopped();
        return true;
    }

    public RingBufferAdmin createRingBufferAdmin(String string) {
        return this.delegate.createRingBufferAdmin(string, this.getName());
    }

    @Deprecated
    public static LoggerConfig createLogger(String string, String string2, String string3, String string4, AppenderRef[] object, Property[] propertyArray, Configuration configuration, Filter filter) {
        Level level;
        if (string3 == null) {
            LOGGER.error("Loggers cannot be configured without a name");
            return null;
        }
        object = Arrays.asList(object);
        try {
            level = Level.toLevel(string2, Level.ERROR);
        }
        catch (Exception exception) {
            LOGGER.error("Invalid Log level specified: {}. Defaulting to Error", (Object)string2);
            level = Level.ERROR;
        }
        string2 = string3.equals("root") ? "" : string3;
        boolean bl = Booleans.parseBoolean(string, true);
        return new AsyncLoggerConfig(string2, (List<AppenderRef>)object, filter, level, bl, propertyArray, configuration, AsyncLoggerConfig.includeLocation(string4));
    }

    @PluginFactory
    public static LoggerConfig createLogger(@PluginAttribute(value="additivity", defaultBoolean=true) boolean bl, @PluginAttribute(value="level") Level level, @Required(message="Loggers cannot be configured without a name") @PluginAttribute(value="name") String string, @PluginAttribute(value="includeLocation") String string2, @PluginElement(value="AppenderRef") AppenderRef[] appenderRefArray, @PluginElement(value="Properties") Property[] propertyArray, @PluginConfiguration Configuration configuration, @PluginElement(value="Filter") Filter filter) {
        string = string.equals("root") ? "" : string;
        return new AsyncLoggerConfig(string, Arrays.asList(appenderRefArray), filter, level, bl, propertyArray, configuration, AsyncLoggerConfig.includeLocation(string2));
    }

    protected static boolean includeLocation(String string) {
        return Boolean.parseBoolean(string);
    }

    @Plugin(name="asyncRoot", category="Core", printObject=true)
    public static class RootLogger
    extends LoggerConfig {
        @Deprecated
        public static LoggerConfig createLogger(String string, String string2, String string3, AppenderRef[] object, Property[] propertyArray, Configuration configuration, Filter filter) {
            Level level;
            object = Arrays.asList(object);
            try {
                level = Level.toLevel(string2, Level.ERROR);
            }
            catch (Exception exception) {
                LOGGER.error("Invalid Log level specified: {}. Defaulting to Error", (Object)string2);
                level = Level.ERROR;
            }
            boolean bl = Booleans.parseBoolean(string, true);
            return new AsyncLoggerConfig("", (List<AppenderRef>)object, filter, level, bl, propertyArray, configuration, AsyncLoggerConfig.includeLocation(string3));
        }

        @PluginFactory
        public static LoggerConfig createLogger(@PluginAttribute(value="additivity") String string, @PluginAttribute(value="level") Level level, @PluginAttribute(value="includeLocation") String string2, @PluginElement(value="AppenderRef") AppenderRef[] object, @PluginElement(value="Properties") Property[] propertyArray, @PluginConfiguration Configuration configuration, @PluginElement(value="Filter") Filter filter) {
            object = Arrays.asList(object);
            level = level == null ? Level.ERROR : level;
            boolean bl = Booleans.parseBoolean(string, true);
            return new AsyncLoggerConfig("", (List<AppenderRef>)object, filter, level, bl, propertyArray, configuration, AsyncLoggerConfig.includeLocation(string2));
        }
    }
}

