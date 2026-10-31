/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.ContextDataInjector;
import org.apache.logging.log4j.core.impl.ThreadContextDataInjector;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.spi.CopyOnWrite;
import org.apache.logging.log4j.spi.DefaultThreadContextMap;
import org.apache.logging.log4j.spi.ReadOnlyThreadContextMap;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

public class ContextDataInjectorFactory {
    public static ContextDataInjector createInjector() {
        String string = PropertiesUtil.getProperties().getStringProperty("log4j2.ContextDataInjector");
        if (string == null) {
            return ContextDataInjectorFactory.createDefaultInjector();
        }
        try {
            return Loader.loadClass(string).asSubclass(ContextDataInjector.class).newInstance();
        }
        catch (Exception exception) {
            ContextDataInjector contextDataInjector = ContextDataInjectorFactory.createDefaultInjector();
            StatusLogger.getLogger().warn("Could not create ContextDataInjector for '{}', using default {}: {}", (Object)string, (Object)contextDataInjector.getClass().getName(), (Object)exception);
            return contextDataInjector;
        }
    }

    private static ContextDataInjector createDefaultInjector() {
        ReadOnlyThreadContextMap readOnlyThreadContextMap = ThreadContext.getThreadContextMap();
        if (readOnlyThreadContextMap instanceof DefaultThreadContextMap || readOnlyThreadContextMap == null) {
            return new ThreadContextDataInjector.ForDefaultThreadContextMap();
        }
        if (readOnlyThreadContextMap instanceof CopyOnWrite) {
            return new ThreadContextDataInjector.ForCopyOnWriteThreadContextMap();
        }
        return new ThreadContextDataInjector.ForGarbageFreeThreadContextMap();
    }
}

