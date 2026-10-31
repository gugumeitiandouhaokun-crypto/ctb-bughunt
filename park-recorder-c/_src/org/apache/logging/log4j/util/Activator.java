/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.AdaptPermission
 *  org.osgi.framework.AdminPermission
 *  org.osgi.framework.Bundle
 *  org.osgi.framework.BundleActivator
 *  org.osgi.framework.BundleContext
 *  org.osgi.framework.BundleEvent
 *  org.osgi.framework.BundleListener
 *  org.osgi.framework.InvalidSyntaxException
 *  org.osgi.framework.ServiceReference
 *  org.osgi.framework.SynchronousBundleListener
 *  org.osgi.framework.wiring.BundleWiring
 */
package org.apache.logging.log4j.util;

import java.net.URL;
import java.security.Permission;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.spi.Provider;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.ProviderUtil;
import org.osgi.framework.AdaptPermission;
import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.wiring.BundleWiring;

public class Activator
implements BundleActivator,
SynchronousBundleListener {
    private static final SecurityManager SECURITY_MANAGER = System.getSecurityManager();
    private static final Logger LOGGER = StatusLogger.getLogger();
    private boolean lockingProviderUtil;

    private static void checkPermission(Permission permission) {
        if (SECURITY_MANAGER != null) {
            SECURITY_MANAGER.checkPermission(permission);
        }
    }

    private void loadProvider(Bundle bundle) {
        if (bundle.getState() == 1) {
            return;
        }
        try {
            Activator.checkPermission((Permission)new AdminPermission(bundle, "resource"));
            Activator.checkPermission((Permission)new AdaptPermission(BundleWiring.class.getName(), bundle, "adapt"));
            BundleContext bundleContext = bundle.getBundleContext();
            if (bundleContext == null) {
                LOGGER.debug("Bundle {} has no context (state={}), skipping loading provider", (Object)bundle.getSymbolicName(), (Object)this.toStateString(bundle.getState()));
                return;
            }
            this.loadProvider(bundleContext, (BundleWiring)bundle.adapt(BundleWiring.class));
            return;
        }
        catch (SecurityException securityException) {
            LOGGER.debug("Cannot access bundle [{}] contents. Ignoring.", (Object)bundle.getSymbolicName(), (Object)securityException);
            return;
        }
        catch (Exception exception) {
            LOGGER.warn("Problem checking bundle {} for Log4j 2 provider.", (Object)bundle.getSymbolicName(), (Object)exception);
            return;
        }
    }

    private String toStateString(int n) {
        switch (n) {
            case 1: {
                return "UNINSTALLED";
            }
            case 2: {
                return "INSTALLED";
            }
            case 4: {
                return "RESOLVED";
            }
            case 8: {
                return "STARTING";
            }
            case 16: {
                return "STOPPING";
            }
            case 32: {
                return "ACTIVE";
            }
        }
        return Integer.toString(n);
    }

    private void loadProvider(BundleContext bundleContext, BundleWiring bundleWiring) {
        Object object;
        try {
            Object object2 = bundleContext.getServiceReferences(Provider.class, "(APIVersion>=2.6.0)");
            object = null;
            object2 = object2.iterator();
            while (object2.hasNext()) {
                Object object3 = (ServiceReference)object2.next();
                object3 = (Provider)bundleContext.getService((ServiceReference)object3);
                if (object != null && ((Provider)object3).getPriority() <= ((Provider)object).getPriority()) continue;
                object = object3;
            }
            if (object != null) {
                ProviderUtil.addProvider(object);
            }
        }
        catch (InvalidSyntaxException invalidSyntaxException) {
            LOGGER.error("Invalid service filter: (APIVersion>=2.6.0)", (Throwable)invalidSyntaxException);
        }
        object = bundleWiring.findEntries("META-INF", "log4j-provider.properties", 0).iterator();
        while (object.hasNext()) {
            ProviderUtil.loadProvider((URL)object.next(), bundleWiring.getClassLoader());
        }
    }

    public void start(BundleContext bundleContext) {
        ProviderUtil.STARTUP_LOCK.lock();
        this.lockingProviderUtil = true;
        for (Bundle[] bundleArray : ((BundleWiring)bundleContext.getBundle().adapt(BundleWiring.class)).getRequiredWires(LoggerContextFactory.class.getName())) {
            this.loadProvider(bundleContext, bundleArray.getProviderWiring());
        }
        bundleContext.addBundleListener((BundleListener)this);
        for (Bundle bundle : bundleContext.getBundles()) {
            this.loadProvider(bundle);
        }
        this.unlockIfReady();
    }

    private void unlockIfReady() {
        if (this.lockingProviderUtil && !ProviderUtil.PROVIDERS.isEmpty()) {
            ProviderUtil.STARTUP_LOCK.unlock();
            this.lockingProviderUtil = false;
        }
    }

    public void stop(BundleContext bundleContext) {
        bundleContext.removeBundleListener((BundleListener)this);
        this.unlockIfReady();
    }

    public void bundleChanged(BundleEvent bundleEvent) {
        switch (bundleEvent.getType()) {
            case 2: {
                this.loadProvider(bundleEvent.getBundle());
                this.unlockIfReady();
            }
        }
    }
}

