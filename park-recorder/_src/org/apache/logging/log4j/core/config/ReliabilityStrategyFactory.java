/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import org.apache.logging.log4j.core.config.AwaitCompletionReliabilityStrategy;
import org.apache.logging.log4j.core.config.AwaitUnconditionallyReliabilityStrategy;
import org.apache.logging.log4j.core.config.LockingReliabilityStrategy;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.ReliabilityStrategy;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

public final class ReliabilityStrategyFactory {
    private ReliabilityStrategyFactory() {
    }

    public static ReliabilityStrategy getReliabilityStrategy(LoggerConfig loggerConfig) {
        String string = PropertiesUtil.getProperties().getStringProperty("log4j.ReliabilityStrategy", "AwaitCompletion");
        if ("AwaitCompletion".equals(string)) {
            return new AwaitCompletionReliabilityStrategy(loggerConfig);
        }
        if ("AwaitUnconditionally".equals(string)) {
            return new AwaitUnconditionallyReliabilityStrategy(loggerConfig);
        }
        if ("Locking".equals(string)) {
            return new LockingReliabilityStrategy(loggerConfig);
        }
        try {
            return Loader.loadClass(string).asSubclass(ReliabilityStrategy.class).getConstructor(LoggerConfig.class).newInstance(loggerConfig);
        }
        catch (Exception exception) {
            StatusLogger.getLogger().warn("Could not create ReliabilityStrategy for '{}', using default AwaitCompletionReliabilityStrategy: {}", (Object)string, (Object)exception);
            return new AwaitCompletionReliabilityStrategy(loggerConfig);
        }
    }
}

