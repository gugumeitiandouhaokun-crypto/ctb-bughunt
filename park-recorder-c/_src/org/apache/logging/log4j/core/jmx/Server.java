/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.jmx;

import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.async.AsyncLoggerConfig;
import org.apache.logging.log4j.core.async.AsyncLoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.jmx.AppenderAdmin;
import org.apache.logging.log4j.core.jmx.AsyncAppenderAdmin;
import org.apache.logging.log4j.core.jmx.ContextSelectorAdmin;
import org.apache.logging.log4j.core.jmx.LoggerConfigAdmin;
import org.apache.logging.log4j.core.jmx.LoggerContextAdmin;
import org.apache.logging.log4j.core.jmx.RingBufferAdmin;
import org.apache.logging.log4j.core.jmx.StatusLoggerAdmin;
import org.apache.logging.log4j.core.selector.ContextSelector;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.Log4jThreadFactory;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

public final class Server {
    private static final String CONTEXT_NAME_ALL = "*";
    public static final String DOMAIN = "org.apache.logging.log4j2";
    private static final String PROPERTY_DISABLE_JMX = "log4j2.disable.jmx";
    private static final String PROPERTY_ASYNC_NOTIF = "log4j2.jmx.notify.async";
    private static final String THREAD_NAME_PREFIX = "jmx.notif";
    private static final StatusLogger LOGGER = StatusLogger.getLogger();
    static final Executor executor = Server.isJmxDisabled() ? null : Server.createExecutor();

    private Server() {
    }

    private static ExecutorService createExecutor() {
        boolean bl;
        boolean bl2 = bl = !Constants.IS_WEB_APP;
        if (PropertiesUtil.getProperties().getBooleanProperty(PROPERTY_ASYNC_NOTIF, bl)) {
            return Executors.newFixedThreadPool(1, Log4jThreadFactory.createDaemonThreadFactory(THREAD_NAME_PREFIX));
        }
        return null;
    }

    /*
     * Enabled aggressive block sorting
     */
    public static String escape(String string) {
        StringBuilder stringBuilder = new StringBuilder(string.length() * 2);
        boolean bl = false;
        block6: for (int i2 = 0; i2 < string.length(); ++i2) {
            char c2 = string.charAt(i2);
            switch (c2) {
                case '\"': 
                case '*': 
                case '?': 
                case '\\': {
                    stringBuilder.append('\\');
                    bl = true;
                    break;
                }
                case ',': 
                case ':': 
                case '=': {
                    bl = true;
                    break;
                }
                case '\r': {
                    continue block6;
                }
                case '\n': {
                    stringBuilder.append("\\n");
                    bl = true;
                    continue block6;
                }
            }
            stringBuilder.append(c2);
        }
        if (bl) {
            stringBuilder.insert(0, '\"');
            stringBuilder.append('\"');
        }
        return stringBuilder.toString();
    }

    private static boolean isJmxDisabled() {
        return PropertiesUtil.getProperties().getBooleanProperty(PROPERTY_DISABLE_JMX);
    }

    public static void reregisterMBeansAfterReconfigure() {
        if (Server.isJmxDisabled()) {
            LOGGER.debug("JMX disabled for Log4j2. Not registering MBeans.");
            return;
        }
        Server.reregisterMBeansAfterReconfigure(ManagementFactory.getPlatformMBeanServer());
    }

    public static void reregisterMBeansAfterReconfigure(MBeanServer mBeanServer) {
        if (Server.isJmxDisabled()) {
            LOGGER.debug("JMX disabled for Log4j2. Not registering MBeans.");
            return;
        }
        try {
            ContextSelector contextSelector = Server.getContextSelector();
            if (contextSelector == null) {
                LOGGER.debug("Could not register MBeans: no ContextSelector found.");
                return;
            }
            LOGGER.trace("Reregistering MBeans after reconfigure. Selector={}", (Object)contextSelector);
            List<LoggerContext> list = contextSelector.getLoggerContexts();
            int n = 0;
            for (LoggerContext loggerContext : list) {
                LOGGER.trace("Reregistering context ({}/{}): '{}' {}", (Object)(++n), (Object)list.size(), (Object)loggerContext.getName(), (Object)loggerContext);
                Server.unregisterLoggerContext(loggerContext.getName(), mBeanServer);
                Object object = new LoggerContextAdmin(loggerContext, executor);
                LoggerContextAdmin loggerContextAdmin = object;
                Server.register(mBeanServer, loggerContextAdmin, loggerContextAdmin.getObjectName());
                if (loggerContext instanceof AsyncLoggerContext && ((RingBufferAdmin)(object = ((AsyncLoggerContext)loggerContext).createRingBufferAdmin())).getBufferSize() > 0L) {
                    Object object2 = object;
                    Server.register(mBeanServer, object2, ((RingBufferAdmin)object2).getObjectName());
                }
                Server.registerStatusLogger(loggerContext.getName(), mBeanServer, executor);
                Server.registerContextSelector(loggerContext.getName(), contextSelector, mBeanServer, executor);
                Server.registerLoggerConfigs(loggerContext, mBeanServer, executor);
                Server.registerAppenders(loggerContext, mBeanServer, executor);
            }
            return;
        }
        catch (Exception exception) {
            LOGGER.error("Could not register mbeans", (Throwable)exception);
            return;
        }
    }

    public static void unregisterMBeans() {
        if (Server.isJmxDisabled()) {
            LOGGER.debug("JMX disabled for Log4j2. Not unregistering MBeans.");
            return;
        }
        Server.unregisterMBeans(ManagementFactory.getPlatformMBeanServer());
    }

    public static void unregisterMBeans(MBeanServer mBeanServer) {
        if (mBeanServer != null) {
            Server.unregisterStatusLogger(CONTEXT_NAME_ALL, mBeanServer);
            Server.unregisterContextSelector(CONTEXT_NAME_ALL, mBeanServer);
            Server.unregisterContexts(mBeanServer);
            Server.unregisterLoggerConfigs(CONTEXT_NAME_ALL, mBeanServer);
            Server.unregisterAsyncLoggerRingBufferAdmins(CONTEXT_NAME_ALL, mBeanServer);
            Server.unregisterAsyncLoggerConfigRingBufferAdmins(CONTEXT_NAME_ALL, mBeanServer);
            Server.unregisterAppenders(CONTEXT_NAME_ALL, mBeanServer);
            Server.unregisterAsyncAppenders(CONTEXT_NAME_ALL, mBeanServer);
        }
    }

    private static ContextSelector getContextSelector() {
        LoggerContextFactory loggerContextFactory = LogManager.getFactory();
        if (loggerContextFactory instanceof Log4jContextFactory) {
            return ((Log4jContextFactory)loggerContextFactory).getSelector();
        }
        return null;
    }

    public static void unregisterLoggerContext(String string) {
        if (Server.isJmxDisabled()) {
            LOGGER.debug("JMX disabled for Log4j2. Not unregistering MBeans.");
            return;
        }
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        Server.unregisterLoggerContext(string, mBeanServer);
    }

    public static void unregisterLoggerContext(String string, MBeanServer mBeanServer) {
        Server.unregisterAllMatching(String.format("org.apache.logging.log4j2:type=%s", Server.escape(string), CONTEXT_NAME_ALL), mBeanServer);
        Server.unregisterStatusLogger(string, mBeanServer);
        Server.unregisterContextSelector(string, mBeanServer);
        Server.unregisterLoggerConfigs(string, mBeanServer);
        Server.unregisterAppenders(string, mBeanServer);
        Server.unregisterAsyncAppenders(string, mBeanServer);
        Server.unregisterAsyncLoggerRingBufferAdmins(string, mBeanServer);
        Server.unregisterAsyncLoggerConfigRingBufferAdmins(string, mBeanServer);
    }

    private static void registerStatusLogger(String object, MBeanServer mBeanServer, Executor executor) {
        Object object2 = object = new StatusLoggerAdmin((String)object, executor);
        Server.register(mBeanServer, object2, ((StatusLoggerAdmin)object2).getObjectName());
    }

    private static void registerContextSelector(String object, ContextSelector contextSelector, MBeanServer mBeanServer, Executor executor) {
        Object object2 = object = new ContextSelectorAdmin((String)object, contextSelector);
        Server.register(mBeanServer, object2, ((ContextSelectorAdmin)object2).getObjectName());
    }

    private static void unregisterStatusLogger(String string, MBeanServer mBeanServer) {
        Server.unregisterAllMatching(String.format("org.apache.logging.log4j2:type=%s,component=StatusLogger", Server.escape(string), CONTEXT_NAME_ALL), mBeanServer);
    }

    private static void unregisterContextSelector(String string, MBeanServer mBeanServer) {
        Server.unregisterAllMatching(String.format("org.apache.logging.log4j2:type=%s,component=ContextSelector", Server.escape(string), CONTEXT_NAME_ALL), mBeanServer);
    }

    private static void unregisterLoggerConfigs(String string, MBeanServer mBeanServer) {
        Server.unregisterAllMatching(String.format("org.apache.logging.log4j2:type=%s,component=Loggers,name=%s", Server.escape(string), CONTEXT_NAME_ALL), mBeanServer);
    }

    private static void unregisterContexts(MBeanServer mBeanServer) {
        Server.unregisterAllMatching(String.format("org.apache.logging.log4j2:type=%s", CONTEXT_NAME_ALL), mBeanServer);
    }

    private static void unregisterAppenders(String string, MBeanServer mBeanServer) {
        Server.unregisterAllMatching(String.format("org.apache.logging.log4j2:type=%s,component=Appenders,name=%s", Server.escape(string), CONTEXT_NAME_ALL), mBeanServer);
    }

    private static void unregisterAsyncAppenders(String string, MBeanServer mBeanServer) {
        Server.unregisterAllMatching(String.format("org.apache.logging.log4j2:type=%s,component=AsyncAppenders,name=%s", Server.escape(string), CONTEXT_NAME_ALL), mBeanServer);
    }

    private static void unregisterAsyncLoggerRingBufferAdmins(String string, MBeanServer mBeanServer) {
        Server.unregisterAllMatching(String.format("org.apache.logging.log4j2:type=%s,component=AsyncLoggerRingBuffer", Server.escape(string)), mBeanServer);
    }

    private static void unregisterAsyncLoggerConfigRingBufferAdmins(String string, MBeanServer mBeanServer) {
        Server.unregisterAllMatching(String.format("org.apache.logging.log4j2:type=%s,component=Loggers,name=%s,subtype=RingBuffer", Server.escape(string), CONTEXT_NAME_ALL), mBeanServer);
    }

    private static void unregisterAllMatching(String string, MBeanServer mBeanServer) {
        try {
            Iterator iterator = new ObjectName(string);
            iterator = mBeanServer.queryNames((ObjectName)((Object)iterator), null);
            if (iterator == null || iterator.isEmpty()) {
                LOGGER.trace("Unregistering but no MBeans found matching '{}'", (Object)string);
            } else {
                LOGGER.trace("Unregistering {} MBeans: {}", (Object)iterator.size(), (Object)iterator);
            }
            if (iterator != null) {
                iterator = iterator.iterator();
                while (iterator.hasNext()) {
                    ObjectName objectName = (ObjectName)iterator.next();
                    mBeanServer.unregisterMBean(objectName);
                }
            }
            return;
        }
        catch (InstanceNotFoundException instanceNotFoundException) {
            LOGGER.debug("Could not unregister MBeans for " + string + ". Ignoring " + instanceNotFoundException);
            return;
        }
        catch (Exception exception) {
            LOGGER.error("Could not unregister MBeans for ".concat(String.valueOf(string)), (Throwable)exception);
            return;
        }
    }

    private static void registerLoggerConfigs(LoggerContext loggerContext, MBeanServer mBeanServer, Executor object) {
        object = loggerContext.getConfiguration().getLoggers();
        for (String string : object.keySet()) {
            RingBufferAdmin ringBufferAdmin;
            LoggerConfigAdmin loggerConfigAdmin;
            LoggerConfig loggerConfig = object.get(string);
            LoggerConfigAdmin loggerConfigAdmin2 = loggerConfigAdmin = new LoggerConfigAdmin(loggerContext, loggerConfig);
            Server.register(mBeanServer, loggerConfigAdmin2, loggerConfigAdmin2.getObjectName());
            if (!(loggerConfig instanceof AsyncLoggerConfig)) continue;
            RingBufferAdmin ringBufferAdmin2 = ringBufferAdmin = ((AsyncLoggerConfig)loggerConfig).createRingBufferAdmin(loggerContext.getName());
            Server.register(mBeanServer, ringBufferAdmin2, ringBufferAdmin2.getObjectName());
        }
    }

    private static void registerAppenders(LoggerContext loggerContext, MBeanServer mBeanServer, Executor object) {
        object = loggerContext.getConfiguration().getAppenders();
        for (String string : object.keySet()) {
            AppenderAdmin appenderAdmin;
            Appender appender = object.get(string);
            if (appender instanceof AsyncAppender) {
                AsyncAppenderAdmin asyncAppenderAdmin;
                AsyncAppender asyncAppender = (AsyncAppender)appender;
                AsyncAppenderAdmin asyncAppenderAdmin2 = asyncAppenderAdmin = new AsyncAppenderAdmin(loggerContext.getName(), asyncAppender);
                Server.register(mBeanServer, asyncAppenderAdmin2, asyncAppenderAdmin2.getObjectName());
                continue;
            }
            AppenderAdmin appenderAdmin2 = appenderAdmin = new AppenderAdmin(loggerContext.getName(), appender);
            Server.register(mBeanServer, appenderAdmin2, appenderAdmin2.getObjectName());
        }
    }

    private static void register(MBeanServer mBeanServer, Object object, ObjectName objectName) {
        LOGGER.debug("Registering MBean {}", (Object)objectName);
        mBeanServer.registerMBean(object, objectName);
    }
}

