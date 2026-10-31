/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.util.NetUtils;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

public final class Configurator {
    private static final String FQCN = Configurator.class.getName();
    private static final Logger LOGGER = StatusLogger.getLogger();

    private static Log4jContextFactory getFactory() {
        LoggerContextFactory loggerContextFactory = LogManager.getFactory();
        if (loggerContextFactory instanceof Log4jContextFactory) {
            return (Log4jContextFactory)loggerContextFactory;
        }
        if (loggerContextFactory != null) {
            LOGGER.error("LogManager returned an instance of {} which does not implement {}. Unable to initialize Log4j.", (Object)loggerContextFactory.getClass().getName(), (Object)Log4jContextFactory.class.getName());
            return null;
        }
        LOGGER.fatal("LogManager did not return a LoggerContextFactory. This indicates something has gone terribly wrong!");
        return null;
    }

    public static LoggerContext initialize(ClassLoader classLoader, ConfigurationSource configurationSource) {
        return Configurator.initialize(classLoader, configurationSource, null);
    }

    public static LoggerContext initialize(ClassLoader classLoader, ConfigurationSource configurationSource, Object object) {
        try {
            Log4jContextFactory log4jContextFactory = Configurator.getFactory();
            if (log4jContextFactory == null) {
                return null;
            }
            return log4jContextFactory.getContext(FQCN, classLoader, object, false, configurationSource);
        }
        catch (Exception exception) {
            LOGGER.error("There was a problem obtaining a LoggerContext using the configuration source [{}]", (Object)configurationSource, (Object)exception);
            return null;
        }
    }

    public static LoggerContext initialize(String string, ClassLoader classLoader, String string2) {
        return Configurator.initialize(string, classLoader, string2, null);
    }

    public static LoggerContext initialize(String string, ClassLoader classLoader, String stringArray, Object object) {
        if (Strings.isBlank((String)stringArray)) {
            return Configurator.initialize(string, classLoader, null, object);
        }
        if (stringArray.contains(",")) {
            stringArray = stringArray.split(",");
            String string2 = null;
            ArrayList<URI> arrayList = new ArrayList<URI>(stringArray.length);
            for (String string3 : stringArray) {
                URI object2 = NetUtils.toURI(string2 != null ? string2 + ":" + string3.trim() : string3.trim());
                if (string2 == null && object2.getScheme() != null) {
                    string2 = object2.getScheme();
                }
                arrayList.add(object2);
            }
            return Configurator.initialize(string, classLoader, arrayList, object);
        }
        return Configurator.initialize(string, classLoader, NetUtils.toURI((String)stringArray), object);
    }

    public static LoggerContext initialize(String string, ClassLoader classLoader, URI uRI) {
        return Configurator.initialize(string, classLoader, uRI, null);
    }

    public static LoggerContext initialize(String string, ClassLoader classLoader, URI uRI, Object object) {
        try {
            Log4jContextFactory log4jContextFactory = Configurator.getFactory();
            if (log4jContextFactory == null) {
                return null;
            }
            return log4jContextFactory.getContext(FQCN, classLoader, object, false, uRI, string);
        }
        catch (Exception exception) {
            LOGGER.error("There was a problem initializing the LoggerContext [{}] using configuration at [{}].", (Object)string, (Object)uRI, (Object)exception);
            return null;
        }
    }

    public static LoggerContext initialize(String string, ClassLoader classLoader, List<URI> list, Object object) {
        try {
            Log4jContextFactory log4jContextFactory = Configurator.getFactory();
            if (log4jContextFactory == null) {
                return null;
            }
            return log4jContextFactory.getContext(FQCN, classLoader, object, false, list, string);
        }
        catch (Exception exception) {
            LOGGER.error("There was a problem initializing the LoggerContext [{}] using configurations at [{}].", (Object)string, (Object)list, (Object)exception);
            return null;
        }
    }

    public static LoggerContext initialize(String string, String string2) {
        return Configurator.initialize(string, null, string2);
    }

    public static LoggerContext initialize(Configuration configuration) {
        return Configurator.initialize(null, configuration, null);
    }

    public static LoggerContext initialize(ClassLoader classLoader, Configuration configuration) {
        return Configurator.initialize(classLoader, configuration, null);
    }

    public static LoggerContext initialize(ClassLoader classLoader, Configuration configuration, Object object) {
        try {
            Log4jContextFactory log4jContextFactory = Configurator.getFactory();
            if (log4jContextFactory == null) {
                return null;
            }
            return log4jContextFactory.getContext(FQCN, classLoader, object, false, configuration);
        }
        catch (Exception exception) {
            LOGGER.error("There was a problem initializing the LoggerContext using configuration {}", (Object)configuration.getName(), (Object)exception);
            return null;
        }
    }

    public static void reconfigure(Configuration configuration) {
        try {
            Log4jContextFactory log4jContextFactory = Configurator.getFactory();
            if (log4jContextFactory != null) {
                log4jContextFactory.getContext(FQCN, null, null, false).reconfigure(configuration);
            }
            return;
        }
        catch (Exception exception) {
            LOGGER.error("There was a problem initializing the LoggerContext using configuration {}", (Object)configuration.getName(), (Object)exception);
            return;
        }
    }

    public static void reconfigure() {
        try {
            Log4jContextFactory log4jContextFactory = Configurator.getFactory();
            if (log4jContextFactory != null) {
                log4jContextFactory.getSelector().getContext(FQCN, null, false).reconfigure();
                return;
            }
            LOGGER.warn("Unable to reconfigure - Log4j has not been initialized.");
            return;
        }
        catch (Exception exception) {
            LOGGER.error("Error encountered trying to reconfigure logging", (Throwable)exception);
            return;
        }
    }

    public static void reconfigure(URI uRI) {
        try {
            Log4jContextFactory log4jContextFactory = Configurator.getFactory();
            if (log4jContextFactory != null) {
                log4jContextFactory.getSelector().getContext(FQCN, null, false).setConfigLocation(uRI);
                return;
            }
            LOGGER.warn("Unable to reconfigure - Log4j has not been initialized.");
            return;
        }
        catch (Exception exception) {
            LOGGER.error("Error encountered trying to reconfigure logging", (Throwable)exception);
            return;
        }
    }

    public static void setAllLevels(String string, Level level) {
        LoggerContext loggerContext = LoggerContext.getContext(false);
        Configuration configuration = loggerContext.getConfiguration();
        boolean bl = Configurator.setLevel(string, level, configuration);
        for (Map.Entry entry : configuration.getLoggers().entrySet()) {
            if (!((String)entry.getKey()).startsWith(string)) continue;
            bl |= Configurator.setLevel((LoggerConfig)entry.getValue(), level);
        }
        if (bl) {
            loggerContext.updateLoggers();
        }
    }

    private static boolean setLevel(LoggerConfig loggerConfig, Level level) {
        boolean bl = !loggerConfig.getLevel().equals(level);
        if (bl) {
            loggerConfig.setLevel(level);
        }
        return bl;
    }

    public static void setLevel(Map<String, Level> object) {
        LoggerContext loggerContext = LoggerContext.getContext(false);
        Configuration configuration = loggerContext.getConfiguration();
        boolean bl = false;
        for (Map.Entry entry : object.entrySet()) {
            String string = (String)entry.getKey();
            Level object2 = (Level)entry.getValue();
            bl |= Configurator.setLevel(string, object2, configuration);
        }
        if (bl) {
            loggerContext.updateLoggers();
        }
    }

    public static void setLevel(String string, Level level) {
        LoggerContext loggerContext = LoggerContext.getContext(false);
        if (Strings.isEmpty(string)) {
            Configurator.setRootLevel(level);
            return;
        }
        if (Configurator.setLevel(string, level, loggerContext.getConfiguration())) {
            loggerContext.updateLoggers();
        }
    }

    private static boolean setLevel(String string, Level level, Configuration configuration) {
        boolean bl;
        LoggerConfig loggerConfig = configuration.getLoggerConfig(string);
        if (!string.equals(loggerConfig.getName())) {
            loggerConfig = new LoggerConfig(string, level, true);
            configuration.addLogger(string, loggerConfig);
            loggerConfig.setLevel(level);
            bl = true;
        } else {
            bl = Configurator.setLevel(loggerConfig, level);
        }
        return bl;
    }

    public static void setRootLevel(Level level) {
        LoggerContext loggerContext = LoggerContext.getContext(false);
        LoggerConfig loggerConfig = loggerContext.getConfiguration().getRootLogger();
        if (!loggerConfig.getLevel().equals(level)) {
            loggerConfig.setLevel(level);
            loggerContext.updateLoggers();
        }
    }

    public static void shutdown(LoggerContext loggerContext) {
        if (loggerContext != null) {
            loggerContext.stop();
        }
    }

    public static boolean shutdown(LoggerContext loggerContext, long l, TimeUnit timeUnit) {
        if (loggerContext != null) {
            return loggerContext.stop(l, timeUnit);
        }
        return true;
    }

    private Configurator() {
    }
}

