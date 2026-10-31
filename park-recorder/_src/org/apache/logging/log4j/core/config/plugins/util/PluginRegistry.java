/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.util;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAliases;
import org.apache.logging.log4j.core.config.plugins.processor.PluginCache;
import org.apache.logging.log4j.core.config.plugins.processor.PluginEntry;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.config.plugins.util.ResolverUtil;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

public class PluginRegistry {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static volatile PluginRegistry INSTANCE;
    private static final Object INSTANCE_LOCK;
    private final AtomicReference<Map<String, List<PluginType<?>>>> pluginsByCategoryRef = new AtomicReference();
    private final ConcurrentMap<Long, Map<String, List<PluginType<?>>>> pluginsByCategoryByBundleId = new ConcurrentHashMap();
    private final ConcurrentMap<String, Map<String, List<PluginType<?>>>> pluginsByCategoryByPackage = new ConcurrentHashMap();

    private PluginRegistry() {
    }

    public static PluginRegistry getInstance() {
        PluginRegistry pluginRegistry = INSTANCE;
        if (pluginRegistry == null) {
            Object object = INSTANCE_LOCK;
            synchronized (object) {
                pluginRegistry = INSTANCE;
                if (pluginRegistry == null) {
                    INSTANCE = pluginRegistry = new PluginRegistry();
                }
            }
        }
        return pluginRegistry;
    }

    public void clear() {
        this.pluginsByCategoryRef.set(null);
        this.pluginsByCategoryByPackage.clear();
        this.pluginsByCategoryByBundleId.clear();
    }

    public Map<Long, Map<String, List<PluginType<?>>>> getPluginsByCategoryByBundleId() {
        return this.pluginsByCategoryByBundleId;
    }

    public Map<String, List<PluginType<?>>> loadFromMainClassLoader() {
        Map<String, List<PluginType<?>>> map = this.pluginsByCategoryRef.get();
        if (map != null) {
            return map;
        }
        map = this.decodeCacheFiles(Loader.getClassLoader());
        if (this.pluginsByCategoryRef.compareAndSet(null, map)) {
            return map;
        }
        return this.pluginsByCategoryRef.get();
    }

    public void clearBundlePlugins(long l) {
        this.pluginsByCategoryByBundleId.remove(l);
    }

    public Map<String, List<PluginType<?>>> loadFromBundle(long l, ClassLoader object) {
        Map map = (Map)this.pluginsByCategoryByBundleId.get(l);
        if (map != null) {
            return map;
        }
        object = this.decodeCacheFiles((ClassLoader)object);
        map = (Map)this.pluginsByCategoryByBundleId.putIfAbsent(l, (Map<String, List<PluginType<?>>>)object);
        if (map != null) {
            return map;
        }
        return object;
    }

    private Map<String, List<PluginType<?>>> decodeCacheFiles(ClassLoader classLoader) {
        Object object;
        long l = System.nanoTime();
        PluginCache pluginCache = new PluginCache();
        try {
            object = classLoader.getResources("META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat");
            if (object == null) {
                LOGGER.info("Plugin preloads not available from class loader {}", (Object)classLoader);
            } else {
                pluginCache.loadCacheFiles((Enumeration<URL>)object);
            }
        }
        catch (IOException iOException) {
            LOGGER.warn("Unable to preload plugins", (Throwable)iOException);
        }
        object = new HashMap();
        int n = 0;
        for (Map.Entry entry : pluginCache.getAllCategories().entrySet()) {
            PluginType pluginType = (String)entry.getKey();
            ArrayList arrayList = new ArrayList(((Map)entry.getValue()).size());
            object.put(pluginType, arrayList);
            Iterator object2 = ((Map)entry.getValue()).entrySet().iterator();
            while (object2.hasNext()) {
                pluginType = (PluginEntry)object2.next().getValue();
                String string = ((PluginEntry)((Object)pluginType)).getClassName();
                try {
                    Class<?> classNotFoundException = classLoader.loadClass(string);
                    pluginType = new PluginType((PluginEntry)((Object)pluginType), classNotFoundException, ((PluginEntry)((Object)pluginType)).getName());
                    arrayList.add(pluginType);
                    ++n;
                }
                catch (ClassNotFoundException linkageError) {
                    LOGGER.info("Plugin [{}] could not be loaded due to missing classes.", (Object)string, (Object)linkageError);
                }
                catch (LinkageError linkageError) {
                    LOGGER.info("Plugin [{}] could not be loaded due to linkage error.", (Object)string, (Object)linkageError);
                }
            }
        }
        int n2 = n;
        LOGGER.debug(() -> {
            long l2 = System.nanoTime();
            StringBuilder stringBuilder = new StringBuilder("Took ");
            DecimalFormat decimalFormat = new DecimalFormat("#0.000000");
            stringBuilder.append(decimalFormat.format((double)(l2 - l) * 1.0E-9));
            stringBuilder.append(" seconds to load ").append(n2);
            stringBuilder.append(" plugins from ").append(classLoader);
            return stringBuilder.toString();
        });
        return object;
    }

    public Map<String, List<PluginType<?>>> loadFromPackage(String string) {
        if (Strings.isBlank(string)) {
            return Collections.emptyMap();
        }
        Object object = (Map)this.pluginsByCategoryByPackage.get(string);
        if (object != null) {
            return object;
        }
        long l = System.nanoTime();
        object = new ResolverUtil();
        Object object2 = Loader.getClassLoader();
        if (object2 != null) {
            ((ResolverUtil)object).setClassLoader((ClassLoader)object2);
        }
        ((ResolverUtil)object).findInPackage(new PluginTest(), string);
        object2 = new HashMap();
        for (Class<?> clazz : ((ResolverUtil)object).getClasses()) {
            Plugin plugin = clazz.getAnnotation(Plugin.class);
            String string2 = plugin.category().toLowerCase();
            ArrayList arrayList = (ArrayList)object2.get(string2);
            if (arrayList == null) {
                arrayList = new ArrayList();
                object2.put(string2, arrayList);
            }
            PluginEntry pluginEntry = new PluginEntry();
            String string3 = plugin.elementType().equals("") ? plugin.name() : plugin.elementType();
            pluginEntry.setKey(plugin.name().toLowerCase());
            pluginEntry.setName(plugin.name());
            pluginEntry.setCategory(plugin.category());
            pluginEntry.setClassName(clazz.getName());
            pluginEntry.setPrintable(plugin.printObject());
            pluginEntry.setDefer(plugin.deferChildren());
            PluginType pluginType = new PluginType(pluginEntry, clazz, string3);
            arrayList.add(pluginType);
            PluginAliases pluginAliases = clazz.getAnnotation(PluginAliases.class);
            if (pluginAliases == null) continue;
            for (String string4 : pluginAliases.value()) {
                PluginEntry pluginEntry2 = new PluginEntry();
                String string5 = plugin.elementType().equals("") ? string4.trim() : plugin.elementType();
                pluginEntry2.setKey(string4.trim().toLowerCase());
                pluginEntry2.setName(plugin.name());
                pluginEntry2.setCategory(plugin.category());
                pluginEntry2.setClassName(clazz.getName());
                pluginEntry2.setPrintable(plugin.printObject());
                pluginEntry2.setDefer(plugin.deferChildren());
                PluginType pluginType2 = new PluginType(pluginEntry2, clazz, string5);
                arrayList.add(pluginType2);
            }
        }
        LOGGER.debug(() -> PluginRegistry.lambda$loadFromPackage$1(l, (ResolverUtil)object, string));
        object = (Map)this.pluginsByCategoryByPackage.putIfAbsent(string, (Map<String, List<PluginType<?>>>)object2);
        if (object != null) {
            return object;
        }
        return object2;
    }

    private static /* synthetic */ Object lambda$loadFromPackage$1(long l, ResolverUtil resolverUtil, String string) {
        long l2 = System.nanoTime();
        StringBuilder stringBuilder = new StringBuilder("Took ");
        DecimalFormat decimalFormat = new DecimalFormat("#0.000000");
        stringBuilder.append(decimalFormat.format((double)(l2 - l) * 1.0E-9));
        stringBuilder.append(" seconds to load ").append(resolverUtil.getClasses().size());
        stringBuilder.append(" plugins from package ").append(string);
        return stringBuilder.toString();
    }

    static {
        INSTANCE_LOCK = new Object();
    }

    public static class PluginTest
    implements ResolverUtil.Test {
        @Override
        public boolean matches(Class<?> clazz) {
            return clazz != null && clazz.isAnnotationPresent(Plugin.class);
        }

        public String toString() {
            return "annotated with @" + Plugin.class.getSimpleName();
        }

        @Override
        public boolean matches(URI uRI) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean doesMatchClass() {
            return true;
        }

        @Override
        public boolean doesMatchResource() {
            return false;
        }
    }
}

