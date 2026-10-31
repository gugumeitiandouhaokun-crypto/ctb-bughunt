/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.Bundle
 *  org.osgi.framework.BundleReference
 *  org.osgi.framework.FrameworkUtil
 */
package org.apache.logging.log4j.core.osgi;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.ContextAnchor;
import org.apache.logging.log4j.core.selector.ClassLoaderContextSelector;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;
import org.osgi.framework.FrameworkUtil;

public class BundleContextSelector
extends ClassLoaderContextSelector {
    @Override
    public void shutdown(String bundleArray, ClassLoader classLoader, boolean n, boolean bl) {
        LoggerContext loggerContext = null;
        Bundle bundle = null;
        if (n != 0) {
            loggerContext = ContextAnchor.THREAD_CONTEXT.get();
            ContextAnchor.THREAD_CONTEXT.remove();
        }
        if (loggerContext == null && classLoader instanceof BundleReference) {
            bundle = ((BundleReference)classLoader).getBundle();
            loggerContext = this.getLoggerContext(bundle);
            this.removeLoggerContext(loggerContext);
        }
        if (loggerContext == null && (bundleArray = StackLocatorUtil.getCallerClass((String)bundleArray)) != null) {
            bundle = FrameworkUtil.getBundle((Class)bundleArray);
            loggerContext = this.getLoggerContext(FrameworkUtil.getBundle((Class)bundleArray));
            this.removeLoggerContext(loggerContext);
        }
        if (loggerContext == null) {
            loggerContext = ContextAnchor.THREAD_CONTEXT.get();
            ContextAnchor.THREAD_CONTEXT.remove();
        }
        if (loggerContext != null) {
            loggerContext.stop(50L, TimeUnit.MILLISECONDS);
        }
        if (bundle != null && bl) {
            for (Bundle bundle2 : bundle.getBundleContext().getBundles()) {
                loggerContext = this.getLoggerContext(bundle2);
                if (loggerContext == null) continue;
                loggerContext.stop(50L, TimeUnit.MILLISECONDS);
            }
        }
    }

    private LoggerContext getLoggerContext(Bundle object) {
        object = Objects.requireNonNull(object, "No Bundle provided").getSymbolicName();
        if ((object = (AtomicReference)CONTEXT_MAP.get(object)) != null && ((AtomicReference)object).get() != null) {
            return (LoggerContext)((WeakReference)((AtomicReference)object).get()).get();
        }
        return null;
    }

    private void removeLoggerContext(LoggerContext loggerContext) {
        CONTEXT_MAP.remove(loggerContext.getName());
    }

    @Override
    public boolean hasContext(String object, ClassLoader classLoader, boolean bl) {
        if (bl && ContextAnchor.THREAD_CONTEXT.get() != null) {
            return ContextAnchor.THREAD_CONTEXT.get().isStarted();
        }
        if (classLoader instanceof BundleReference) {
            return BundleContextSelector.hasContext(((BundleReference)classLoader).getBundle());
        }
        if ((object = StackLocatorUtil.getCallerClass((String)object)) != null) {
            return BundleContextSelector.hasContext(FrameworkUtil.getBundle((Class)object));
        }
        return ContextAnchor.THREAD_CONTEXT.get() != null && ContextAnchor.THREAD_CONTEXT.get().isStarted();
    }

    @Override
    public LoggerContext getContext(String object, ClassLoader classLoader, boolean bl, URI uRI) {
        if (bl) {
            object = ContextAnchor.THREAD_CONTEXT.get();
            if (object != null) {
                return object;
            }
            return this.getDefault();
        }
        if (classLoader instanceof BundleReference) {
            return BundleContextSelector.locateContext(((BundleReference)classLoader).getBundle(), uRI);
        }
        if ((object = StackLocatorUtil.getCallerClass((String)object)) != null) {
            return BundleContextSelector.locateContext(FrameworkUtil.getBundle(object), uRI);
        }
        object = ContextAnchor.THREAD_CONTEXT.get();
        if (object == null) {
            return this.getDefault();
        }
        return object;
    }

    private static boolean hasContext(Bundle object) {
        object = Objects.requireNonNull(object, "No Bundle provided").getSymbolicName();
        return (object = (AtomicReference)CONTEXT_MAP.get(object)) != null && ((AtomicReference)object).get() != null && ((WeakReference)((AtomicReference)object).get()).get() != null && ((LoggerContext)((WeakReference)((AtomicReference)object).get()).get()).isStarted();
    }

    private static LoggerContext locateContext(Bundle object, URI uRI) {
        String string = Objects.requireNonNull(object, "No Bundle provided").getSymbolicName();
        AtomicReference atomicReference = (AtomicReference)CONTEXT_MAP.get(string);
        if (atomicReference == null) {
            LoggerContext loggerContext = new LoggerContext(string, object, uRI);
            CONTEXT_MAP.putIfAbsent(string, new AtomicReference<WeakReference<LoggerContext>>(new WeakReference<LoggerContext>(loggerContext)));
            return (LoggerContext)((WeakReference)((AtomicReference)CONTEXT_MAP.get(string)).get()).get();
        }
        WeakReference weakReference = (WeakReference)atomicReference.get();
        LoggerContext loggerContext = (LoggerContext)weakReference.get();
        if (loggerContext == null) {
            object = new LoggerContext(string, object, uRI);
            atomicReference.compareAndSet(weakReference, new WeakReference<Bundle>((Bundle)object));
            return (LoggerContext)((WeakReference)atomicReference.get()).get();
        }
        object = loggerContext.getConfigLocation();
        if (object == null && uRI != null) {
            LOGGER.debug("Setting bundle ({}) configuration to {}", (Object)string, (Object)uRI);
            loggerContext.setConfigLocation(uRI);
        } else if (object != null && uRI != null && !uRI.equals(object)) {
            LOGGER.warn("locateContext called with URI [{}], but existing LoggerContext has URI [{}]", (Object)uRI, object);
        }
        return loggerContext;
    }
}

