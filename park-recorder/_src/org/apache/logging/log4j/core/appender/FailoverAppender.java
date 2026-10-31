/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LoggingException;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAliases;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.util.Booleans;

@Plugin(name="Failover", category="Core", elementType="appender", printObject=true)
public final class FailoverAppender
extends AbstractAppender {
    private static final int DEFAULT_INTERVAL_SECONDS = 60;
    private final String primaryRef;
    private final String[] failovers;
    private final Configuration config;
    private AppenderControl primary;
    private final List<AppenderControl> failoverAppenders = new ArrayList<AppenderControl>();
    private final long intervalNanos;
    private volatile long nextCheckNanos = 0L;

    private FailoverAppender(String string, Filter filter, String string2, String[] stringArray, int n, Configuration configuration, boolean bl, Property[] propertyArray) {
        super(string, filter, null, bl, propertyArray);
        this.primaryRef = string2;
        this.failovers = stringArray;
        this.config = configuration;
        this.intervalNanos = TimeUnit.MILLISECONDS.toNanos(n);
    }

    @Override
    public final void start() {
        Map<String, Appender> map = this.config.getAppenders();
        int n = 0;
        String[] stringArray = map.get(this.primaryRef);
        if (stringArray != null) {
            this.primary = new AppenderControl((Appender)stringArray, null, null);
        } else {
            LOGGER.error("Unable to locate primary Appender " + this.primaryRef);
            ++n;
        }
        stringArray = this.failovers;
        int n2 = this.failovers.length;
        for (int j = 0; j < n2; ++j) {
            String string = stringArray[j];
            Appender appender = map.get(string);
            if (appender != null) {
                this.failoverAppenders.add(new AppenderControl(appender, null, null));
                continue;
            }
            LOGGER.error("Failover appender " + string + " is not configured");
        }
        if (this.failoverAppenders.isEmpty()) {
            LOGGER.error("No failover appenders are available");
            ++n;
        }
        if (n == 0) {
            super.start();
        }
    }

    @Override
    public final void append(LogEvent logEvent) {
        if (!this.isStarted()) {
            this.error("FailoverAppender " + this.getName() + " did not start successfully");
            return;
        }
        long l = this.nextCheckNanos;
        if (l == 0L || System.nanoTime() - l > 0L) {
            this.callAppender(logEvent);
            return;
        }
        this.failover(logEvent, null);
    }

    private void callAppender(LogEvent logEvent) {
        try {
            this.primary.callAppender(logEvent);
            this.nextCheckNanos = 0L;
            return;
        }
        catch (Exception exception) {
            this.nextCheckNanos = System.nanoTime() + this.intervalNanos;
            this.failover(logEvent, exception);
            return;
        }
    }

    private void failover(LogEvent logEvent, Exception exception) {
        exception = exception != null ? (exception instanceof LoggingException ? (LoggingException)exception : new LoggingException(exception)) : null;
        boolean bl = false;
        Exception exception2 = null;
        for (AppenderControl appenderControl : this.failoverAppenders) {
            try {
                appenderControl.callAppender(logEvent);
                bl = true;
                break;
            }
            catch (Exception exception3) {
                if (exception2 != null) continue;
                exception2 = exception3;
            }
        }
        if (!bl && !this.ignoreExceptions()) {
            if (exception != null) {
                throw exception;
            }
            throw new LoggingException("Unable to write to failover appenders", exception2);
        }
    }

    @Override
    public final String toString() {
        StringBuilder stringBuilder = new StringBuilder(this.getName());
        stringBuilder.append(" primary=").append(this.primary).append(", failover={");
        boolean bl = true;
        String[] stringArray = this.failovers;
        int n = this.failovers.length;
        for (int j = 0; j < n; ++j) {
            String string = stringArray[j];
            if (!bl) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(string);
            bl = false;
        }
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    @PluginFactory
    public static FailoverAppender createAppender(@PluginAttribute(value="name") String string, @PluginAttribute(value="primary") String string2, @PluginElement(value="Failovers") String[] stringArray, @PluginAliases(value={"retryInterval"}) @PluginAttribute(value="retryIntervalSeconds") String string3, @PluginConfiguration Configuration configuration, @PluginElement(value="Filter") Filter filter, @PluginAttribute(value="ignoreExceptions") String string4) {
        int n;
        if (string == null) {
            LOGGER.error("A name for the Appender must be specified");
            return null;
        }
        if (string2 == null) {
            LOGGER.error("A primary Appender must be specified");
            return null;
        }
        if (stringArray == null || stringArray.length == 0) {
            LOGGER.error("At least one failover Appender must be specified");
            return null;
        }
        int n2 = FailoverAppender.parseInt(string3, 60);
        if (n2 >= 0) {
            n = n2 * 1000;
        } else {
            LOGGER.warn("Interval " + string3 + " is less than zero. Using default");
            n = 60000;
        }
        boolean bl = Booleans.parseBoolean(string4, true);
        return new FailoverAppender(string, filter, string2, stringArray, n, configuration, bl, null);
    }
}

