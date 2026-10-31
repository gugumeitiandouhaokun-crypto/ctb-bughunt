/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.async;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Constants;
import org.apache.logging.log4j.util.PropertiesUtil;

public enum ThreadNameCachingStrategy {
    CACHED{

        @Override
        public final String getThreadName() {
            String string = (String)THREADLOCAL_NAME.get();
            if (string == null) {
                string = Thread.currentThread().getName();
                THREADLOCAL_NAME.set(string);
            }
            return string;
        }
    }
    ,
    UNCACHED{

        @Override
        public final String getThreadName() {
            return Thread.currentThread().getName();
        }
    };

    private static final StatusLogger LOGGER;
    private static final ThreadLocal<String> THREADLOCAL_NAME;
    static final ThreadNameCachingStrategy DEFAULT_STRATEGY;

    abstract String getThreadName();

    public static ThreadNameCachingStrategy create() {
        String string = PropertiesUtil.getProperties().getStringProperty("AsyncLogger.ThreadNameStrategy");
        try {
            ThreadNameCachingStrategy threadNameCachingStrategy = string != null ? ThreadNameCachingStrategy.valueOf(string) : DEFAULT_STRATEGY;
            LOGGER.debug("AsyncLogger.ThreadNameStrategy={} (user specified {}, default is {})", (Object)threadNameCachingStrategy.name(), (Object)string, (Object)DEFAULT_STRATEGY.name());
            return threadNameCachingStrategy;
        }
        catch (Exception exception) {
            LOGGER.debug("Using AsyncLogger.ThreadNameStrategy.{}: '{}' not valid: {}", (Object)DEFAULT_STRATEGY.name(), (Object)string, (Object)exception.toString());
            return DEFAULT_STRATEGY;
        }
    }

    static boolean isAllocatingThreadGetName() {
        if (Constants.JAVA_MAJOR_VERSION == 8) {
            try {
                Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)_(\\d+)").matcher(System.getProperty("java.version"));
                if (matcher.matches()) {
                    return Integer.parseInt(matcher.group(3)) == 0 && Integer.parseInt(matcher.group(4)) < 102;
                }
                return true;
            }
            catch (Exception exception) {
                return true;
            }
        }
        return Constants.JAVA_MAJOR_VERSION < 8;
    }

    static {
        LOGGER = StatusLogger.getLogger();
        THREADLOCAL_NAME = new ThreadLocal();
        DEFAULT_STRATEGY = ThreadNameCachingStrategy.isAllocatingThreadGetName() ? CACHED : UNCACHED;
    }
}

