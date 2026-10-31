/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.selector;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.ContextAnchor;
import org.apache.logging.log4j.core.selector.ContextSelector;
import org.apache.logging.log4j.spi.LoggerContextShutdownAware;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.StackLocatorUtil;

public class ClassLoaderContextSelector
implements ContextSelector,
LoggerContextShutdownAware {
    private static final AtomicReference<LoggerContext> DEFAULT_CONTEXT = new AtomicReference();
    public static final StatusLogger LOGGER = StatusLogger.getLogger();
    public static final ConcurrentMap<String, AtomicReference<WeakReference<LoggerContext>>> CONTEXT_MAP = new ConcurrentHashMap<String, AtomicReference<WeakReference<LoggerContext>>>();

    @Override
    public void shutdown(String object, ClassLoader classLoader, boolean bl, boolean bl2) {
        LoggerContext loggerContext = null;
        if (bl) {
            loggerContext = ContextAnchor.THREAD_CONTEXT.get();
        } else if (classLoader != null) {
            loggerContext = this.findContext(classLoader);
        } else {
            if ((object = StackLocatorUtil.getCallerClass((String)object)) != null) {
                loggerContext = this.findContext(((Class)object).getClassLoader());
            }
            if (loggerContext == null) {
                loggerContext = ContextAnchor.THREAD_CONTEXT.get();
            }
        }
        if (loggerContext != null) {
            loggerContext.stop(50L, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void contextShutdown(org.apache.logging.log4j.spi.LoggerContext loggerContext) {
        if (loggerContext instanceof LoggerContext) {
            this.removeContext((LoggerContext)loggerContext);
        }
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public boolean hasContext(String var1_1, ClassLoader var2_2, boolean var3_3) {
        if (var3_3) ** GOTO lbl-1000
        if (var2_2 != null) {
            var1_1 = this.findContext(var2_2);
        } else if ((var1_1 = StackLocatorUtil.getCallerClass((String)var1_1)) != null) {
            var1_1 = this.findContext(var1_1.getClassLoader());
        } else lbl-1000:
        // 2 sources

        {
            var1_1 = ContextAnchor.THREAD_CONTEXT.get();
        }
        return var1_1 != null && var1_1.isStarted() != false;
    }

    private LoggerContext findContext(ClassLoader object) {
        object = object != null ? object : ClassLoader.getSystemClassLoader();
        object = this.toContextMapKey((ClassLoader)object);
        if ((object = (AtomicReference)CONTEXT_MAP.get(object)) != null) {
            return (LoggerContext)((WeakReference)((AtomicReference)object).get()).get();
        }
        return null;
    }

    @Override
    public LoggerContext getContext(String string, ClassLoader classLoader, boolean bl) {
        return this.getContext(string, classLoader, bl, null);
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
        if (classLoader != null) {
            return this.locateContext(classLoader, uRI);
        }
        if ((object = StackLocatorUtil.getCallerClass((String)object)) != null) {
            return this.locateContext(((Class)object).getClassLoader(), uRI);
        }
        object = ContextAnchor.THREAD_CONTEXT.get();
        if (object != null) {
            return object;
        }
        return this.getDefault();
    }

    @Override
    public void removeContext(LoggerContext loggerContext) {
        for (Map.Entry entry : CONTEXT_MAP.entrySet()) {
            if ((LoggerContext)((WeakReference)((AtomicReference)entry.getValue()).get()).get() != loggerContext) continue;
            CONTEXT_MAP.remove(entry.getKey());
        }
    }

    @Override
    public List<LoggerContext> getLoggerContexts() {
        ArrayList<LoggerContext> arrayList = new ArrayList<LoggerContext>();
        Iterator iterator = CONTEXT_MAP.values().iterator();
        while (iterator.hasNext()) {
            LoggerContext loggerContext = (LoggerContext)((WeakReference)((AtomicReference)iterator.next()).get()).get();
            if (loggerContext == null) continue;
            arrayList.add(loggerContext);
        }
        return Collections.unmodifiableList(arrayList);
    }

    private LoggerContext locateContext(ClassLoader object, URI uRI) {
        String string = this.toContextMapKey((ClassLoader)(object = object != null ? object : ClassLoader.getSystemClassLoader()));
        Object object2 = (AtomicReference)CONTEXT_MAP.get(string);
        if (object2 == null) {
            LoggerContext loggerContext;
            if (uRI == null) {
                for (object = ((ClassLoader)object).getParent(); object != null; object = ((ClassLoader)object).getParent()) {
                    object2 = (AtomicReference)CONTEXT_MAP.get(this.toContextMapKey((ClassLoader)object));
                    if (object2 == null || (object2 = (LoggerContext)((WeakReference)((AtomicReference)object2).get()).get()) == null) continue;
                    return object2;
                }
            }
            if ((loggerContext = (LoggerContext)((WeakReference)CONTEXT_MAP.computeIfAbsent(string, arg_0 -> ClassLoaderContextSelector.lambda$locateContext$0((LoggerContext)(object = this.createContext(string, uRI)), arg_0)).get()).get()) == object) {
                ((LoggerContext)object).addShutdownListener(this);
            }
            return loggerContext;
        }
        object = (WeakReference)((AtomicReference)object2).get();
        LoggerContext loggerContext = (LoggerContext)((Reference)object).get();
        if (loggerContext != null) {
            if (loggerContext.getConfigLocation() == null && uRI != null) {
                LOGGER.debug("Setting configuration to {}", (Object)uRI);
                loggerContext.setConfigLocation(uRI);
            } else if (loggerContext.getConfigLocation() != null && uRI != null && !loggerContext.getConfigLocation().equals(uRI)) {
                LOGGER.warn("locateContext called with URI {}. Existing LoggerContext has URI {}", (Object)uRI, (Object)loggerContext.getConfigLocation());
            }
            return loggerContext;
        }
        loggerContext = this.createContext(string, uRI);
        ((AtomicReference)object2).compareAndSet(object, new WeakReference<LoggerContext>(loggerContext));
        return loggerContext;
    }

    protected LoggerContext createContext(String string, URI uRI) {
        return new LoggerContext(string, null, uRI);
    }

    protected String toContextMapKey(ClassLoader classLoader) {
        return Integer.toHexString(System.identityHashCode(classLoader));
    }

    protected LoggerContext getDefault() {
        LoggerContext loggerContext = DEFAULT_CONTEXT.get();
        if (loggerContext != null) {
            return loggerContext;
        }
        ClassLoaderContextSelector classLoaderContextSelector = this;
        DEFAULT_CONTEXT.compareAndSet(null, classLoaderContextSelector.createContext(classLoaderContextSelector.defaultContextName(), null));
        return DEFAULT_CONTEXT.get();
    }

    protected String defaultContextName() {
        return "Default";
    }

    private static /* synthetic */ AtomicReference lambda$locateContext$0(LoggerContext loggerContext, String string) {
        return new AtomicReference<WeakReference<LoggerContext>>(new WeakReference<LoggerContext>(loggerContext));
    }
}

