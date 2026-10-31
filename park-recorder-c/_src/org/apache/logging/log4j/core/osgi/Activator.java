/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.Bundle
 *  org.osgi.framework.BundleActivator
 *  org.osgi.framework.BundleContext
 *  org.osgi.framework.BundleEvent
 *  org.osgi.framework.BundleListener
 *  org.osgi.framework.InvalidSyntaxException
 *  org.osgi.framework.ServiceRegistration
 *  org.osgi.framework.SynchronousBundleListener
 *  org.osgi.framework.wiring.BundleWiring
 */
package org.apache.logging.log4j.core.osgi;

import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.util.PluginRegistry;
import org.apache.logging.log4j.core.impl.Log4jProvider;
import org.apache.logging.log4j.core.impl.ThreadContextDataInjector;
import org.apache.logging.log4j.core.impl.ThreadContextDataProvider;
import org.apache.logging.log4j.core.osgi.BundleContextSelector;
import org.apache.logging.log4j.core.util.ContextDataProvider;
import org.apache.logging.log4j.spi.Provider;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.wiring.BundleWiring;

public final class Activator
implements BundleActivator,
SynchronousBundleListener {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final AtomicReference<BundleContext> contextRef = new AtomicReference();
    ServiceRegistration provideRegistration = null;
    ServiceRegistration contextDataRegistration = null;

    public final void start(BundleContext bundleContext) {
        Log4jProvider log4jProvider = new Log4jProvider();
        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        hashtable.put("APIVersion", "2.60");
        ThreadContextDataProvider threadContextDataProvider = new ThreadContextDataProvider();
        this.provideRegistration = bundleContext.registerService(Provider.class.getName(), (Object)log4jProvider, hashtable);
        this.contextDataRegistration = bundleContext.registerService(ContextDataProvider.class.getName(), (Object)threadContextDataProvider, null);
        Activator.loadContextProviders(bundleContext);
        if (PropertiesUtil.getProperties().getStringProperty("Log4jContextSelector") == null) {
            System.setProperty("Log4jContextSelector", BundleContextSelector.class.getName());
        }
        if (this.contextRef.compareAndSet(null, bundleContext)) {
            bundleContext.addBundleListener((BundleListener)this);
            Activator.scanInstalledBundlesForPlugins(bundleContext);
        }
    }

    private static void scanInstalledBundlesForPlugins(BundleContext bundleArray) {
        bundleArray = bundleArray.getBundles();
        int n = bundleArray.length;
        for (int i2 = 0; i2 < n; ++i2) {
            Activator.scanBundleForPlugins(bundleArray[i2]);
        }
    }

    private static void scanBundleForPlugins(Bundle bundle) {
        long l = bundle.getBundleId();
        if (bundle.getState() == 32 && l != 0L) {
            LOGGER.trace("Scanning bundle [{}, id=%d] for plugins.", (Object)bundle.getSymbolicName(), (Object)l);
            PluginRegistry.getInstance().loadFromBundle(l, ((BundleWiring)bundle.adapt(BundleWiring.class)).getClassLoader());
        }
    }

    private static void loadContextProviders(BundleContext bundleContext) {
        try {
            for (Object object : bundleContext.getServiceReferences(ContextDataProvider.class, null)) {
                object = (ContextDataProvider)bundleContext.getService(object);
                ThreadContextDataInjector.contextDataProviders.add((ContextDataProvider)object);
            }
            return;
        }
        catch (InvalidSyntaxException invalidSyntaxException) {
            LOGGER.error("Error accessing context data provider", (Throwable)invalidSyntaxException);
            return;
        }
    }

    private static void stopBundlePlugins(Bundle bundle) {
        LOGGER.trace("Stopping bundle [{}] plugins.", (Object)bundle.getSymbolicName());
        PluginRegistry.getInstance().clearBundlePlugins(bundle.getBundleId());
    }

    public final void stop(BundleContext bundleContext) {
        this.provideRegistration.unregister();
        this.contextDataRegistration.unregister();
        this.contextRef.compareAndSet(bundleContext, null);
        LogManager.shutdown();
    }

    public final void bundleChanged(BundleEvent bundleEvent) {
        switch (bundleEvent.getType()) {
            case 2: {
                Activator.scanBundleForPlugins(bundleEvent.getBundle());
                return;
            }
            case 256: {
                Activator.stopBundlePlugins(bundleEvent.getBundle());
            }
        }
    }
}

