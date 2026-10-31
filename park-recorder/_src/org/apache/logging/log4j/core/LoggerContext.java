/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LifeCycle2;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationListener;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.NullConfiguration;
import org.apache.logging.log4j.core.config.Reconfigurable;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.jmx.Server;
import org.apache.logging.log4j.core.util.Cancellable;
import org.apache.logging.log4j.core.util.ExecutorServices;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.core.util.NetUtils;
import org.apache.logging.log4j.core.util.ShutdownCallbackRegistry;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.spi.LoggerContextShutdownAware;
import org.apache.logging.log4j.spi.LoggerContextShutdownEnabled;
import org.apache.logging.log4j.spi.LoggerRegistry;
import org.apache.logging.log4j.spi.Terminable;
import org.apache.logging.log4j.spi.ThreadContextMapFactory;
import org.apache.logging.log4j.util.PropertiesUtil;

public class LoggerContext
extends AbstractLifeCycle
implements AutoCloseable,
ConfigurationListener,
org.apache.logging.log4j.spi.LoggerContext,
LoggerContextShutdownEnabled,
Terminable {
    public static final String PROPERTY_CONFIG = "config";
    private static final Configuration NULL_CONFIGURATION;
    private final LoggerRegistry<Logger> loggerRegistry = new LoggerRegistry();
    private final CopyOnWriteArrayList<PropertyChangeListener> propertyChangeListeners = new CopyOnWriteArrayList();
    private volatile List<LoggerContextShutdownAware> listeners = null;
    private volatile Configuration configuration = new DefaultConfiguration();
    private static final String EXTERNAL_CONTEXT_KEY = "__EXTERNAL_CONTEXT_KEY__";
    private ConcurrentMap<String, Object> externalMap = new ConcurrentHashMap<String, Object>();
    private String contextName;
    private volatile URI configLocation;
    private Cancellable shutdownCallback;
    private final Lock configLock = new ReentrantLock();

    public LoggerContext(String string) {
        this(string, (Object)null, (URI)null);
    }

    public LoggerContext(String string, Object object) {
        this(string, object, (URI)null);
    }

    public LoggerContext(String string, Object object, URI uRI) {
        this.contextName = string;
        if (object == null) {
            this.externalMap.remove(EXTERNAL_CONTEXT_KEY);
        } else {
            this.externalMap.put(EXTERNAL_CONTEXT_KEY, object);
        }
        this.configLocation = uRI;
    }

    public LoggerContext(String object, Object object2, String string) {
        this.contextName = object;
        if (object2 == null) {
            this.externalMap.remove(EXTERNAL_CONTEXT_KEY);
        } else {
            this.externalMap.put(EXTERNAL_CONTEXT_KEY, object2);
        }
        if (string != null) {
            try {
                object = new File(string).toURI();
            }
            catch (Exception exception) {
                object = null;
            }
            this.configLocation = object;
            return;
        }
        this.configLocation = null;
    }

    @Override
    public void addShutdownListener(LoggerContextShutdownAware loggerContextShutdownAware) {
        if (this.listeners == null) {
            LoggerContext loggerContext = this;
            synchronized (loggerContext) {
                if (this.listeners == null) {
                    this.listeners = Collections.synchronizedList(new ArrayList());
                }
            }
        }
        this.listeners.add(loggerContextShutdownAware);
    }

    @Override
    public List<LoggerContextShutdownAware> getListeners() {
        return this.listeners;
    }

    public static LoggerContext getContext() {
        return (LoggerContext)LogManager.getContext();
    }

    public static LoggerContext getContext(boolean bl) {
        return (LoggerContext)LogManager.getContext(bl);
    }

    public static LoggerContext getContext(ClassLoader classLoader, boolean bl, URI uRI) {
        return (LoggerContext)LogManager.getContext(classLoader, bl, uRI);
    }

    @Override
    public void start() {
        LOGGER.debug("Starting LoggerContext[name={}, {}]...", (Object)this.getName(), (Object)this);
        if (PropertiesUtil.getProperties().getBooleanProperty("log4j.LoggerContext.stacktrace.on.start", false)) {
            LOGGER.debug("Stack trace to locate invoker", (Throwable)new Exception("Not a real error, showing stack trace to locate invoker"));
        }
        if (this.configLock.tryLock()) {
            try {
                if (this.isInitialized() || this.isStopped()) {
                    this.setStarting();
                    this.reconfigure();
                    if (this.configuration.isShutdownHookEnabled()) {
                        this.setUpShutdownHook();
                    }
                    this.setStarted();
                }
            }
            finally {
                this.configLock.unlock();
            }
        }
        LOGGER.debug("LoggerContext[name={}, {}] started OK.", (Object)this.getName(), (Object)this);
    }

    public void start(Configuration configuration) {
        LOGGER.debug("Starting LoggerContext[name={}, {}] with configuration {}...", (Object)this.getName(), (Object)this, (Object)configuration);
        if (this.configLock.tryLock()) {
            try {
                if (this.isInitialized() || this.isStopped()) {
                    if (this.configuration.isShutdownHookEnabled()) {
                        this.setUpShutdownHook();
                    }
                    this.setStarted();
                }
            }
            finally {
                this.configLock.unlock();
            }
        }
        this.setConfiguration(configuration);
        LOGGER.debug("LoggerContext[name={}, {}] started OK with configuration {}.", (Object)this.getName(), (Object)this, (Object)configuration);
    }

    private void setUpShutdownHook() {
        LoggerContextFactory loggerContextFactory;
        if (this.shutdownCallback == null && (loggerContextFactory = LogManager.getFactory()) instanceof ShutdownCallbackRegistry) {
            LOGGER.debug(ShutdownCallbackRegistry.SHUTDOWN_HOOK_MARKER, "Shutdown hook enabled. Registering a new one.");
            try {
                final long l = this.configuration.getShutdownTimeoutMillis();
                this.shutdownCallback = ((ShutdownCallbackRegistry)((Object)loggerContextFactory)).addShutdownCallback(new Runnable(){

                    @Override
                    public void run() {
                        LoggerContext loggerContext = LoggerContext.this;
                        AbstractLifeCycle.LOGGER.debug(ShutdownCallbackRegistry.SHUTDOWN_HOOK_MARKER, "Stopping LoggerContext[name={}, {}]", (Object)loggerContext.getName(), (Object)loggerContext);
                        loggerContext.stop(l, TimeUnit.MILLISECONDS);
                    }

                    public String toString() {
                        return "Shutdown callback for LoggerContext[name=" + LoggerContext.this.getName() + ']';
                    }
                });
                return;
            }
            catch (IllegalStateException illegalStateException) {
                throw new IllegalStateException("Unable to register Log4j shutdown hook because JVM is shutting down.", illegalStateException);
            }
            catch (SecurityException securityException) {
                LOGGER.error(ShutdownCallbackRegistry.SHUTDOWN_HOOK_MARKER, "Unable to register shutdown hook due to security restrictions", (Throwable)securityException);
            }
        }
    }

    @Override
    public void close() {
        this.stop();
    }

    @Override
    public void terminate() {
        this.stop();
    }

    @Override
    public boolean stop(long l, TimeUnit timeUnit) {
        LOGGER.debug("Stopping LoggerContext[name={}, {}]...", (Object)this.getName(), (Object)this);
        this.configLock.lock();
        try {
            if (this.isStopped()) {
                return true;
            }
            this.setStopping();
            try {
                Server.unregisterLoggerContext(this.getName());
            }
            catch (Exception | LinkageError throwable) {
                LOGGER.error("Unable to unregister MBeans", throwable);
            }
            if (this.shutdownCallback != null) {
                this.shutdownCallback.cancel();
                this.shutdownCallback = null;
            }
            Configuration configuration = this.configuration;
            this.configuration = NULL_CONFIGURATION;
            this.updateLoggers();
            if (configuration instanceof LifeCycle2) {
                ((LifeCycle2)((Object)configuration)).stop(l, timeUnit);
            } else {
                configuration.stop();
            }
            this.externalMap.clear();
            LogManager.getFactory().removeContext(this);
        }
        finally {
            this.configLock.unlock();
            this.setStopped();
        }
        if (this.listeners != null) {
            for (LoggerContextShutdownAware loggerContextShutdownAware : this.listeners) {
                try {
                    loggerContextShutdownAware.contextShutdown(this);
                }
                catch (Exception exception) {}
            }
        }
        LOGGER.debug("Stopped LoggerContext[name={}, {}] with status {}", (Object)this.getName(), (Object)this, (Object)Boolean.TRUE);
        return true;
    }

    public String getName() {
        return this.contextName;
    }

    public Logger getRootLogger() {
        return this.getLogger("");
    }

    public void setName(String string) {
        this.contextName = Objects.requireNonNull(string);
    }

    @Override
    public Object getObject(String string) {
        return this.externalMap.get(string);
    }

    @Override
    public Object putObject(String string, Object object) {
        return this.externalMap.put(string, object);
    }

    @Override
    public Object putObjectIfAbsent(String string, Object object) {
        return this.externalMap.putIfAbsent(string, object);
    }

    @Override
    public Object removeObject(String string) {
        return this.externalMap.remove(string);
    }

    @Override
    public boolean removeObject(String string, Object object) {
        return this.externalMap.remove(string, object);
    }

    public void setExternalContext(Object object) {
        if (object != null) {
            this.externalMap.put(EXTERNAL_CONTEXT_KEY, object);
            return;
        }
        this.externalMap.remove(EXTERNAL_CONTEXT_KEY);
    }

    @Override
    public Object getExternalContext() {
        return this.externalMap.get(EXTERNAL_CONTEXT_KEY);
    }

    @Override
    public Logger getLogger(String string) {
        return this.getLogger(string, null);
    }

    public Collection<Logger> getLoggers() {
        return this.loggerRegistry.getLoggers();
    }

    @Override
    public Logger getLogger(String string, MessageFactory messageFactory) {
        Logger logger = this.loggerRegistry.getLogger(string, messageFactory);
        if (logger != null) {
            AbstractLogger.checkMessageFactory(logger, messageFactory);
            return logger;
        }
        LoggerContext loggerContext = this;
        logger = loggerContext.newInstance(loggerContext, string, messageFactory);
        this.loggerRegistry.putIfAbsent(string, messageFactory, logger);
        return this.loggerRegistry.getLogger(string, messageFactory);
    }

    @Override
    public boolean hasLogger(String string) {
        return this.loggerRegistry.hasLogger(string);
    }

    @Override
    public boolean hasLogger(String string, MessageFactory messageFactory) {
        return this.loggerRegistry.hasLogger(string, messageFactory);
    }

    @Override
    public boolean hasLogger(String string, Class<? extends MessageFactory> clazz) {
        return this.loggerRegistry.hasLogger(string, clazz);
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public void addFilter(Filter filter) {
        this.configuration.addFilter(filter);
    }

    public void removeFilter(Filter filter) {
        this.configuration.removeFilter(filter);
    }

    public Configuration setConfiguration(Configuration configuration) {
        if (configuration == null) {
            LOGGER.error("No configuration found for context '{}'.", (Object)this.contextName);
            return this.configuration;
        }
        this.configLock.lock();
        try {
            Configuration configuration2 = this.configuration;
            configuration.addListener(this);
            ConcurrentMap concurrentMap = (ConcurrentMap)configuration.getComponent("ContextProperties");
            try {
                concurrentMap.putIfAbsent("hostName", NetUtils.getLocalHostname());
            }
            catch (Exception exception) {
                LOGGER.debug("Ignoring {}, setting hostName to 'unknown'", (Object)exception.toString());
                concurrentMap.putIfAbsent("hostName", "unknown");
            }
            concurrentMap.putIfAbsent("contextName", this.contextName);
            configuration.start();
            this.configuration = configuration;
            this.updateLoggers();
            if (configuration2 != null) {
                configuration2.removeListener(this);
                configuration2.stop();
            }
            this.firePropertyChangeEvent(new PropertyChangeEvent(this, PROPERTY_CONFIG, configuration2, configuration));
            try {
                Server.reregisterMBeansAfterReconfigure();
            }
            catch (Exception | LinkageError throwable) {
                LOGGER.error("Could not reconfigure JMX", throwable);
            }
            Log4jLogEvent.setNanoClock(this.configuration.getNanoClock());
            Configuration configuration3 = configuration2;
            return configuration3;
        }
        finally {
            this.configLock.unlock();
        }
    }

    private void firePropertyChangeEvent(PropertyChangeEvent propertyChangeEvent) {
        Iterator<PropertyChangeListener> iterator = this.propertyChangeListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().propertyChange(propertyChangeEvent);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.propertyChangeListeners.add(Objects.requireNonNull(propertyChangeListener, "listener"));
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        this.propertyChangeListeners.remove(propertyChangeListener);
    }

    public URI getConfigLocation() {
        return this.configLocation;
    }

    public void setConfigLocation(URI uRI) {
        this.configLocation = uRI;
        this.reconfigure(uRI);
    }

    private void reconfigure(URI object) {
        Object object2 = this.externalMap.get(EXTERNAL_CONTEXT_KEY);
        object2 = ClassLoader.class.isInstance(object2) ? (ClassLoader)object2 : null;
        LOGGER.debug("Reconfiguration started for context[name={}] at URI {} ({}) with optional ClassLoader: {}", (Object)this.contextName, object, (Object)this, object2);
        LoggerContext loggerContext = this;
        Configuration configuration = ConfigurationFactory.getInstance().getConfiguration(loggerContext, loggerContext.contextName, (URI)object, (ClassLoader)object2);
        if (configuration == null) {
            LOGGER.error("Reconfiguration failed: No configuration found for '{}' at '{}' in '{}'", (Object)this.contextName, object, object2);
            return;
        }
        this.setConfiguration(configuration);
        object = this.configuration == null ? "?" : String.valueOf(this.configuration.getConfigurationSource());
        LOGGER.debug("Reconfiguration complete for context[name={}] at URI {} ({}) with optional ClassLoader: {}", (Object)this.contextName, object, (Object)this, object2);
    }

    public void reconfigure() {
        LoggerContext loggerContext = this;
        loggerContext.reconfigure(loggerContext.configLocation);
    }

    public void reconfigure(Configuration object) {
        this.setConfiguration((Configuration)object);
        object = object.getConfigurationSource();
        if (object != null && (object = ((ConfigurationSource)object).getURI()) != null) {
            this.configLocation = object;
        }
    }

    public void updateLoggers() {
        LoggerContext loggerContext = this;
        loggerContext.updateLoggers(loggerContext.configuration);
    }

    public void updateLoggers(Configuration configuration) {
        Configuration configuration2 = this.configuration;
        Iterator<Logger> iterator = this.loggerRegistry.getLoggers().iterator();
        while (iterator.hasNext()) {
            iterator.next().updateConfiguration(configuration);
        }
        this.firePropertyChangeEvent(new PropertyChangeEvent(this, PROPERTY_CONFIG, configuration2, configuration));
    }

    @Override
    public synchronized void onChange(Reconfigurable object) {
        long l = System.currentTimeMillis();
        LOGGER.debug("Reconfiguration started for context {} ({})", (Object)this.contextName, (Object)this);
        this.initApiModule();
        object = object.reconfigure();
        if (object != null) {
            this.setConfiguration((Configuration)object);
            LOGGER.debug("Reconfiguration completed for {} ({}) in {} milliseconds.", (Object)this.contextName, (Object)this, (Object)(System.currentTimeMillis() - l));
            return;
        }
        LOGGER.debug("Reconfiguration failed for {} ({}) in {} milliseconds.", (Object)this.contextName, (Object)this, (Object)(System.currentTimeMillis() - l));
    }

    private void initApiModule() {
        ThreadContextMapFactory.init();
    }

    protected Logger newInstance(LoggerContext loggerContext, String string, MessageFactory messageFactory) {
        return new Logger(loggerContext, string, messageFactory);
    }

    static {
        try {
            Loader.loadClass(ExecutorServices.class.getName());
        }
        catch (Exception exception) {
            LOGGER.error("Failed to preload ExecutorServices class.", (Throwable)exception);
        }
        NULL_CONFIGURATION = new NullConfiguration();
    }
}

