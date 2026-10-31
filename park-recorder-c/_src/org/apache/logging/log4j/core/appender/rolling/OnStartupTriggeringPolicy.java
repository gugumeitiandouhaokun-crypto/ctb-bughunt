/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rolling.AbstractTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="OnStartupTriggeringPolicy", category="Core", printObject=true)
public class OnStartupTriggeringPolicy
extends AbstractTriggeringPolicy {
    private static final long JVM_START_TIME = OnStartupTriggeringPolicy.initStartTime();
    private final long minSize;

    private OnStartupTriggeringPolicy(long l) {
        this.minSize = l;
    }

    private static long initStartTime() {
        try {
            Object object = Loader.loadSystemClass("java.lang.management.ManagementFactory").getMethod("getRuntimeMXBean", new Class[0]).invoke(null, new Object[0]);
            return (Long)Loader.loadSystemClass("java.lang.management.RuntimeMXBean").getMethod("getStartTime", new Class[0]).invoke(object, new Object[0]);
        }
        catch (Throwable throwable) {
            StatusLogger.getLogger().error("Unable to call ManagementFactory.getRuntimeMXBean().getStartTime(), using system time for OnStartupTriggeringPolicy", throwable);
            return System.currentTimeMillis();
        }
    }

    @Override
    public void initialize(RollingFileManager rollingFileManager) {
        if (rollingFileManager.getFileTime() < JVM_START_TIME && rollingFileManager.getFileSize() >= this.minSize) {
            StatusLogger.getLogger().debug("Initiating rollover at startup");
            if (this.minSize == 0L) {
                rollingFileManager.setRenameEmptyFiles(true);
            }
            rollingFileManager.skipFooter(true);
            rollingFileManager.rollover();
            rollingFileManager.skipFooter(false);
        }
    }

    @Override
    public boolean isTriggeringEvent(LogEvent logEvent) {
        return false;
    }

    public String toString() {
        return "OnStartupTriggeringPolicy";
    }

    @PluginFactory
    public static OnStartupTriggeringPolicy createPolicy(@PluginAttribute(value="minSize", defaultLong=1L) long l) {
        return new OnStartupTriggeringPolicy(l);
    }
}

