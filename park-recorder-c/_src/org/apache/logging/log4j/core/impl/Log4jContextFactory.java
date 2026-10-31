/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.composite.CompositeConfiguration;
import org.apache.logging.log4j.core.impl.ContextAnchor;
import org.apache.logging.log4j.core.selector.ClassLoaderContextSelector;
import org.apache.logging.log4j.core.selector.ContextSelector;
import org.apache.logging.log4j.core.util.Cancellable;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.core.util.ShutdownCallbackRegistry;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

public class Log4jContextFactory
implements ShutdownCallbackRegistry,
LoggerContextFactory {
    private static final StatusLogger LOGGER = StatusLogger.getLogger();
    private static final boolean SHUTDOWN_HOOK_ENABLED = PropertiesUtil.getProperties().getBooleanProperty("log4j.shutdownHookEnabled", true) && !Constants.IS_WEB_APP;
    private final ContextSelector selector;
    private final ShutdownCallbackRegistry shutdownCallbackRegistry;

    public Log4jContextFactory() {
        this(Log4jContextFactory.createContextSelector(), Log4jContextFactory.createShutdownCallbackRegistry());
    }

    public Log4jContextFactory(ContextSelector contextSelector) {
        this(contextSelector, Log4jContextFactory.createShutdownCallbackRegistry());
    }

    public Log4jContextFactory(ShutdownCallbackRegistry shutdownCallbackRegistry) {
        this(Log4jContextFactory.createContextSelector(), shutdownCallbackRegistry);
    }

    public Log4jContextFactory(ContextSelector contextSelector, ShutdownCallbackRegistry shutdownCallbackRegistry) {
        this.selector = Objects.requireNonNull(contextSelector, "No ContextSelector provided");
        this.shutdownCallbackRegistry = Objects.requireNonNull(shutdownCallbackRegistry, "No ShutdownCallbackRegistry provided");
        LOGGER.debug("Using ShutdownCallbackRegistry {}", (Object)shutdownCallbackRegistry.getClass());
        this.initializeShutdownCallbackRegistry();
    }

    private static ContextSelector createContextSelector() {
        try {
            ContextSelector contextSelector = Loader.newCheckedInstanceOfProperty("Log4jContextSelector", ContextSelector.class);
            if (contextSelector != null) {
                return contextSelector;
            }
        }
        catch (Exception exception) {
            LOGGER.error("Unable to create custom ContextSelector. Falling back to default.", (Throwable)exception);
        }
        return new ClassLoaderContextSelector();
    }

    private static ShutdownCallbackRegistry createShutdownCallbackRegistry() {
        try {
            ShutdownCallbackRegistry shutdownCallbackRegistry = Loader.newCheckedInstanceOfProperty("log4j.shutdownCallbackRegistry", ShutdownCallbackRegistry.class);
            if (shutdownCallbackRegistry != null) {
                return shutdownCallbackRegistry;
            }
        }
        catch (Exception exception) {
            LOGGER.error("Unable to create custom ShutdownCallbackRegistry. Falling back to default.", (Throwable)exception);
        }
        return new DefaultShutdownCallbackRegistry();
    }

    private void initializeShutdownCallbackRegistry() {
        if (this.isShutdownHookEnabled() && this.shutdownCallbackRegistry instanceof LifeCycle) {
            try {
                ((LifeCycle)((Object)this.shutdownCallbackRegistry)).start();
                return;
            }
            catch (IllegalStateException illegalStateException) {
                LOGGER.error("Cannot start ShutdownCallbackRegistry, already shutting down.");
                throw illegalStateException;
            }
            catch (RuntimeException runtimeException) {
                LOGGER.error("There was an error starting the ShutdownCallbackRegistry.", (Throwable)runtimeException);
            }
        }
    }

    @Override
    public LoggerContext getContext(String object, ClassLoader classLoader, Object object2, boolean bl) {
        object = this.selector.getContext((String)object, classLoader, bl);
        if (object2 != null && ((LoggerContext)object).getExternalContext() == null) {
            ((LoggerContext)object).setExternalContext(object2);
        }
        if (((AbstractLifeCycle)object).getState() == LifeCycle.State.INITIALIZED) {
            ((LoggerContext)object).start();
        }
        return object;
    }

    public LoggerContext getContext(String object, ClassLoader object2, Object object3, boolean bl, ConfigurationSource configurationSource) {
        object = this.selector.getContext((String)object, (ClassLoader)object2, bl, null);
        if (object3 != null && ((LoggerContext)object).getExternalContext() == null) {
            ((LoggerContext)object).setExternalContext(object3);
        }
        if (((AbstractLifeCycle)object).getState() == LifeCycle.State.INITIALIZED) {
            if (configurationSource != null) {
                ContextAnchor.THREAD_CONTEXT.set((LoggerContext)object);
                object2 = ConfigurationFactory.getInstance().getConfiguration((LoggerContext)object, configurationSource);
                LOGGER.debug("Starting LoggerContext[name={}] from configuration {}", (Object)((LoggerContext)object).getName(), (Object)configurationSource);
                ((LoggerContext)object).start((Configuration)object2);
                ContextAnchor.THREAD_CONTEXT.remove();
            } else {
                ((LoggerContext)object).start();
            }
        }
        return object;
    }

    public LoggerContext getContext(String object, ClassLoader classLoader, Object object2, boolean bl, Configuration configuration) {
        object = this.selector.getContext((String)object, classLoader, bl, null);
        if (object2 != null && ((LoggerContext)object).getExternalContext() == null) {
            ((LoggerContext)object).setExternalContext(object2);
        }
        if (((AbstractLifeCycle)object).getState() == LifeCycle.State.INITIALIZED) {
            ContextAnchor.THREAD_CONTEXT.set((LoggerContext)object);
            try {
                ((LoggerContext)object).start(configuration);
            }
            finally {
                ContextAnchor.THREAD_CONTEXT.remove();
            }
        }
        return object;
    }

    @Override
    public LoggerContext getContext(String object, ClassLoader object2, Object object3, boolean bl, URI uRI, String string) {
        object = this.selector.getContext((String)object, (ClassLoader)object2, bl, uRI);
        if (object3 != null && ((LoggerContext)object).getExternalContext() == null) {
            ((LoggerContext)object).setExternalContext(object3);
        }
        if (string != null) {
            ((LoggerContext)object).setName(string);
        }
        if (((AbstractLifeCycle)object).getState() == LifeCycle.State.INITIALIZED) {
            if (uRI != null || string != null) {
                ContextAnchor.THREAD_CONTEXT.set((LoggerContext)object);
                object2 = ConfigurationFactory.getInstance().getConfiguration((LoggerContext)object, string, uRI);
                LOGGER.debug("Starting LoggerContext[name={}] from configuration at {}", (Object)((LoggerContext)object).getName(), (Object)uRI);
                ((LoggerContext)object).start((Configuration)object2);
                ContextAnchor.THREAD_CONTEXT.remove();
            } else {
                ((LoggerContext)object).start();
            }
        }
        return object;
    }

    public LoggerContext getContext(String object, ClassLoader object2, Object object3, boolean bl, List<URI> list, String string) {
        object = this.selector.getContext((String)object, (ClassLoader)object2, bl, null);
        if (object3 != null && ((LoggerContext)object).getExternalContext() == null) {
            ((LoggerContext)object).setExternalContext(object3);
        }
        if (string != null) {
            ((LoggerContext)object).setName(string);
        }
        if (((AbstractLifeCycle)object).getState() == LifeCycle.State.INITIALIZED) {
            if (list != null && !list.isEmpty()) {
                ContextAnchor.THREAD_CONTEXT.set((LoggerContext)object);
                object2 = new ArrayList(list.size());
                for (URI uRI : list) {
                    Configuration configuration = ConfigurationFactory.getInstance().getConfiguration((LoggerContext)object, string, uRI);
                    if (configuration instanceof AbstractConfiguration) {
                        object2.add((AbstractConfiguration)configuration);
                        continue;
                    }
                    LOGGER.error("Found configuration {}, which is not an AbstractConfiguration and can't be handled by CompositeConfiguration", (Object)uRI);
                }
                object3 = new CompositeConfiguration((List<? extends AbstractConfiguration>)object2);
                LOGGER.debug("Starting LoggerContext[name={}] from configurations at {}", (Object)((LoggerContext)object).getName(), (Object)list);
                ((LoggerContext)object).start((Configuration)object3);
                ContextAnchor.THREAD_CONTEXT.remove();
            } else {
                ((LoggerContext)object).start();
            }
        }
        return object;
    }

    @Override
    public void shutdown(String string, ClassLoader classLoader, boolean bl, boolean bl2) {
        if (this.selector.hasContext(string, classLoader, bl)) {
            this.selector.shutdown(string, classLoader, bl, bl2);
        }
    }

    @Override
    public boolean hasContext(String string, ClassLoader classLoader, boolean bl) {
        return this.selector.hasContext(string, classLoader, bl);
    }

    public ContextSelector getSelector() {
        return this.selector;
    }

    public ShutdownCallbackRegistry getShutdownCallbackRegistry() {
        return this.shutdownCallbackRegistry;
    }

    @Override
    public void removeContext(org.apache.logging.log4j.spi.LoggerContext loggerContext) {
        if (loggerContext instanceof LoggerContext) {
            this.selector.removeContext((LoggerContext)loggerContext);
        }
    }

    @Override
    public Cancellable addShutdownCallback(Runnable runnable) {
        if (this.isShutdownHookEnabled()) {
            return this.shutdownCallbackRegistry.addShutdownCallback(runnable);
        }
        return null;
    }

    public boolean isShutdownHookEnabled() {
        return SHUTDOWN_HOOK_ENABLED;
    }
}

