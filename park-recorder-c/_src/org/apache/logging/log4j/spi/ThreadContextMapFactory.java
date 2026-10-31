/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.spi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.CopyOnWriteSortedArrayThreadContextMap;
import org.apache.logging.log4j.spi.DefaultThreadContextMap;
import org.apache.logging.log4j.spi.GarbageFreeSortedArrayThreadContextMap;
import org.apache.logging.log4j.spi.Provider;
import org.apache.logging.log4j.spi.ThreadContextMap;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Constants;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.ProviderUtil;

public final class ThreadContextMapFactory {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String THREAD_CONTEXT_KEY = "log4j2.threadContextMap";
    private static final String GC_FREE_THREAD_CONTEXT_KEY = "log4j2.garbagefree.threadContextMap";
    private static boolean GcFreeThreadContextKey;
    private static String ThreadContextMapName;

    public static void init() {
        CopyOnWriteSortedArrayThreadContextMap.init();
        GarbageFreeSortedArrayThreadContextMap.init();
        DefaultThreadContextMap.init();
        ThreadContextMapFactory.initPrivate();
    }

    private static void initPrivate() {
        PropertiesUtil propertiesUtil = PropertiesUtil.getProperties();
        ThreadContextMapName = propertiesUtil.getStringProperty(THREAD_CONTEXT_KEY);
        GcFreeThreadContextKey = propertiesUtil.getBooleanProperty(GC_FREE_THREAD_CONTEXT_KEY);
    }

    private ThreadContextMapFactory() {
    }

    public static ThreadContextMap createThreadContextMap() {
        Object object = ProviderUtil.findClassLoader();
        ThreadContextMap threadContextMap = null;
        if (ThreadContextMapName != null) {
            try {
                object = ((ClassLoader)object).loadClass(ThreadContextMapName);
                if (ThreadContextMap.class.isAssignableFrom((Class<?>)object)) {
                    threadContextMap = (ThreadContextMap)((Class)object).newInstance();
                }
            }
            catch (ClassNotFoundException classNotFoundException) {
                LOGGER.error("Unable to locate configured ThreadContextMap {}", (Object)ThreadContextMapName);
            }
            catch (Exception exception) {
                LOGGER.error("Unable to create configured ThreadContextMap {}", (Object)ThreadContextMapName, (Object)exception);
            }
        }
        if (threadContextMap == null && ProviderUtil.hasProviders() && LogManager.getFactory() != null) {
            object = LogManager.getFactory().getClass().getName();
            for (Provider provider : ProviderUtil.getProviders()) {
                Class<? extends ThreadContextMap> clazz;
                if (!((String)object).equals(provider.getClassName()) || (clazz = provider.loadThreadContextMap()) == null) continue;
                try {
                    threadContextMap = clazz.newInstance();
                    break;
                }
                catch (Exception exception) {
                    LOGGER.error("Unable to locate or load configured ThreadContextMap {}", (Object)provider.getThreadContextMap(), (Object)exception);
                    threadContextMap = ThreadContextMapFactory.createDefaultThreadContextMap();
                }
            }
        }
        if (threadContextMap == null) {
            threadContextMap = ThreadContextMapFactory.createDefaultThreadContextMap();
        }
        return threadContextMap;
    }

    private static ThreadContextMap createDefaultThreadContextMap() {
        if (Constants.ENABLE_THREADLOCALS) {
            if (GcFreeThreadContextKey) {
                return new GarbageFreeSortedArrayThreadContextMap();
            }
            return new CopyOnWriteSortedArrayThreadContextMap();
        }
        return new DefaultThreadContextMap(true);
    }

    static {
        ThreadContextMapFactory.initPrivate();
    }
}

