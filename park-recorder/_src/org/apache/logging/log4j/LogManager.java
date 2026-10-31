/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.StringFormatterMessageFactory;
import org.apache.logging.log4j.simple.SimpleLoggerContextFactory;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.spi.Provider;
import org.apache.logging.log4j.spi.Terminable;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.ProviderUtil;
import org.apache.logging.log4j.util.StackLocatorUtil;

public class LogManager {
    public static final String FACTORY_PROPERTY_NAME = "log4j2.loggerContextFactory";
    public static final String ROOT_LOGGER_NAME = "";
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String FQCN = LogManager.class.getName();
    private static volatile LoggerContextFactory factory;

    protected LogManager() {
    }

    public static boolean exists(String string) {
        return LogManager.getContext().hasLogger(string);
    }

    public static LoggerContext getContext() {
        try {
            return factory.getContext(FQCN, null, null, true);
        }
        catch (IllegalStateException illegalStateException) {
            LOGGER.warn(illegalStateException.getMessage() + " Using SimpleLogger");
            return new SimpleLoggerContextFactory().getContext(FQCN, null, null, true);
        }
    }

    public static LoggerContext getContext(boolean bl) {
        try {
            return factory.getContext(FQCN, null, null, bl, null, null);
        }
        catch (IllegalStateException illegalStateException) {
            LOGGER.warn(illegalStateException.getMessage() + " Using SimpleLogger");
            return new SimpleLoggerContextFactory().getContext(FQCN, null, null, bl, null, null);
        }
    }

    public static LoggerContext getContext(ClassLoader classLoader, boolean bl) {
        try {
            return factory.getContext(FQCN, classLoader, null, bl);
        }
        catch (IllegalStateException illegalStateException) {
            LOGGER.warn(illegalStateException.getMessage() + " Using SimpleLogger");
            return new SimpleLoggerContextFactory().getContext(FQCN, classLoader, null, bl);
        }
    }

    public static LoggerContext getContext(ClassLoader classLoader, boolean bl, Object object) {
        try {
            return factory.getContext(FQCN, classLoader, object, bl);
        }
        catch (IllegalStateException illegalStateException) {
            LOGGER.warn(illegalStateException.getMessage() + " Using SimpleLogger");
            return new SimpleLoggerContextFactory().getContext(FQCN, classLoader, object, bl);
        }
    }

    public static LoggerContext getContext(ClassLoader classLoader, boolean bl, URI uRI) {
        try {
            return factory.getContext(FQCN, classLoader, null, bl, uRI, null);
        }
        catch (IllegalStateException illegalStateException) {
            LOGGER.warn(illegalStateException.getMessage() + " Using SimpleLogger");
            return new SimpleLoggerContextFactory().getContext(FQCN, classLoader, null, bl, uRI, null);
        }
    }

    public static LoggerContext getContext(ClassLoader classLoader, boolean bl, Object object, URI uRI) {
        try {
            return factory.getContext(FQCN, classLoader, object, bl, uRI, null);
        }
        catch (IllegalStateException illegalStateException) {
            LOGGER.warn(illegalStateException.getMessage() + " Using SimpleLogger");
            return new SimpleLoggerContextFactory().getContext(FQCN, classLoader, object, bl, uRI, null);
        }
    }

    public static LoggerContext getContext(ClassLoader classLoader, boolean bl, Object object, URI uRI, String string) {
        try {
            return factory.getContext(FQCN, classLoader, object, bl, uRI, string);
        }
        catch (IllegalStateException illegalStateException) {
            LOGGER.warn(illegalStateException.getMessage() + " Using SimpleLogger");
            return new SimpleLoggerContextFactory().getContext(FQCN, classLoader, object, bl, uRI, string);
        }
    }

    protected static LoggerContext getContext(String string, boolean bl) {
        try {
            return factory.getContext(string, null, null, bl);
        }
        catch (IllegalStateException illegalStateException) {
            LOGGER.warn(illegalStateException.getMessage() + " Using SimpleLogger");
            return new SimpleLoggerContextFactory().getContext(string, null, null, bl);
        }
    }

    protected static LoggerContext getContext(String string, ClassLoader classLoader, boolean bl) {
        try {
            return factory.getContext(string, classLoader, null, bl);
        }
        catch (IllegalStateException illegalStateException) {
            LOGGER.warn(illegalStateException.getMessage() + " Using SimpleLogger");
            return new SimpleLoggerContextFactory().getContext(string, classLoader, null, bl);
        }
    }

    protected static LoggerContext getContext(String string, ClassLoader classLoader, boolean bl, URI uRI, String string2) {
        try {
            return factory.getContext(string, classLoader, null, bl, uRI, string2);
        }
        catch (IllegalStateException illegalStateException) {
            LOGGER.warn(illegalStateException.getMessage() + " Using SimpleLogger");
            return new SimpleLoggerContextFactory().getContext(string, classLoader, null, bl);
        }
    }

    public static void shutdown() {
        LogManager.shutdown(false);
    }

    public static void shutdown(boolean bl) {
        factory.shutdown(FQCN, null, bl, false);
    }

    public static void shutdown(boolean bl, boolean bl2) {
        factory.shutdown(FQCN, null, bl, bl2);
    }

    public static void shutdown(LoggerContext loggerContext) {
        if (loggerContext != null && loggerContext instanceof Terminable) {
            ((Terminable)((Object)loggerContext)).terminate();
        }
    }

    private static String toLoggerName(Class<?> clazz) {
        String string = clazz.getCanonicalName();
        if (string != null) {
            return string;
        }
        return clazz.getName();
    }

    public static LoggerContextFactory getFactory() {
        return factory;
    }

    public static void setFactory(LoggerContextFactory loggerContextFactory) {
        factory = loggerContextFactory;
    }

    public static Logger getFormatterLogger() {
        return LogManager.getFormatterLogger(StackLocatorUtil.getCallerClass(2));
    }

    public static Logger getFormatterLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz != null ? clazz : StackLocatorUtil.getCallerClass(2), (MessageFactory)StringFormatterMessageFactory.INSTANCE);
    }

    public static Logger getFormatterLogger(Object object) {
        return LogManager.getLogger(object != null ? object.getClass() : StackLocatorUtil.getCallerClass(2), (MessageFactory)StringFormatterMessageFactory.INSTANCE);
    }

    public static Logger getFormatterLogger(String string) {
        if (string == null) {
            return LogManager.getFormatterLogger(StackLocatorUtil.getCallerClass(2));
        }
        return LogManager.getLogger(string, (MessageFactory)StringFormatterMessageFactory.INSTANCE);
    }

    private static Class<?> callerClass(Class<?> clazz) {
        if (clazz != null) {
            return clazz;
        }
        clazz = StackLocatorUtil.getCallerClass(3);
        if (clazz == null) {
            throw new UnsupportedOperationException("No class provided, and an appropriate one cannot be found.");
        }
        return clazz;
    }

    public static Logger getLogger() {
        return LogManager.getLogger(StackLocatorUtil.getCallerClass(2));
    }

    public static Logger getLogger(Class<?> clazz) {
        clazz = LogManager.callerClass(clazz);
        return LogManager.getContext(clazz.getClassLoader(), false).getLogger(LogManager.toLoggerName(clazz));
    }

    public static Logger getLogger(Class<?> clazz, MessageFactory messageFactory) {
        clazz = LogManager.callerClass(clazz);
        return LogManager.getContext(clazz.getClassLoader(), false).getLogger(LogManager.toLoggerName(clazz), messageFactory);
    }

    public static Logger getLogger(MessageFactory messageFactory) {
        return LogManager.getLogger(StackLocatorUtil.getCallerClass(2), messageFactory);
    }

    public static Logger getLogger(Object object) {
        return LogManager.getLogger(object != null ? object.getClass() : StackLocatorUtil.getCallerClass(2));
    }

    public static Logger getLogger(Object object, MessageFactory messageFactory) {
        return LogManager.getLogger(object != null ? object.getClass() : StackLocatorUtil.getCallerClass(2), messageFactory);
    }

    public static Logger getLogger(String string) {
        if (string != null) {
            return LogManager.getContext(false).getLogger(string);
        }
        return LogManager.getLogger(StackLocatorUtil.getCallerClass(2));
    }

    public static Logger getLogger(String string, MessageFactory messageFactory) {
        if (string != null) {
            return LogManager.getContext(false).getLogger(string, messageFactory);
        }
        return LogManager.getLogger(StackLocatorUtil.getCallerClass(2), messageFactory);
    }

    protected static Logger getLogger(String string, String string2) {
        return factory.getContext(string, null, null, false).getLogger(string2);
    }

    public static Logger getRootLogger() {
        return LogManager.getLogger(ROOT_LOGGER_NAME);
    }

    static {
        Object object = PropertiesUtil.getProperties().getStringProperty(FACTORY_PROPERTY_NAME);
        if (object != null) {
            try {
                factory = LoaderUtil.newCheckedInstanceOf((String)object, LoggerContextFactory.class);
            }
            catch (ClassNotFoundException classNotFoundException) {
                LOGGER.error("Unable to locate configured LoggerContextFactory {}", object);
            }
            catch (Exception exception) {
                LOGGER.error("Unable to create configured LoggerContextFactory {}", object, (Object)exception);
            }
        }
        if (factory == null) {
            TreeMap<Integer, LoggerContextFactory> treeMap = new TreeMap<Integer, LoggerContextFactory>();
            if (ProviderUtil.hasProviders()) {
                for (Provider object2 : ProviderUtil.getProviders()) {
                    Class<? extends LoggerContextFactory> clazz = object2.loadLoggerContextFactory();
                    if (clazz == null) continue;
                    try {
                        treeMap.put(object2.getPriority(), clazz.newInstance());
                    }
                    catch (Exception exception) {
                        LOGGER.error("Unable to create class {} specified in provider URL {}", (Object)clazz.getName(), (Object)object2.getUrl(), (Object)exception);
                    }
                }
                if (treeMap.isEmpty()) {
                    LOGGER.error("Log4j2 could not find a logging implementation. Please add log4j-core to the classpath. Using SimpleLogger to log to the console...");
                    factory = new SimpleLoggerContextFactory();
                    return;
                }
                if (treeMap.size() == 1) {
                    TreeMap<Integer, LoggerContextFactory> treeMap2 = treeMap;
                    factory = (LoggerContextFactory)treeMap2.get(treeMap2.lastKey());
                    return;
                }
                object = new StringBuilder("Multiple logging implementations found: \n");
                for (Map.Entry entry : treeMap.entrySet()) {
                    ((StringBuilder)object).append("Factory: ").append(((LoggerContextFactory)entry.getValue()).getClass().getName());
                    ((StringBuilder)object).append(", Weighting: ").append(entry.getKey()).append('\n');
                }
                TreeMap<Integer, LoggerContextFactory> treeMap3 = treeMap;
                factory = (LoggerContextFactory)treeMap3.get(treeMap3.lastKey());
                ((StringBuilder)object).append("Using factory: ").append(factory.getClass().getName());
                LOGGER.warn(((StringBuilder)object).toString());
                return;
            }
            LOGGER.error("Log4j2 could not find a logging implementation. Please add log4j-core to the classpath. Using SimpleLogger to log to the console...");
            factory = new SimpleLoggerContextFactory();
        }
    }
}

