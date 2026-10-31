/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.ContextDataInjector;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.ContextDataFactory;
import org.apache.logging.log4j.core.impl.JdkMapAdapterStringMap;
import org.apache.logging.log4j.core.util.ContextDataProvider;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.StringMap;

public class ThreadContextDataInjector {
    private static Logger LOGGER = StatusLogger.getLogger();
    public static Collection<ContextDataProvider> contextDataProviders = new ConcurrentLinkedDeque<ContextDataProvider>();

    public static void copyProperties(List<Property> list, StringMap stringMap) {
        if (list != null) {
            for (int i2 = 0; i2 < list.size(); ++i2) {
                Property property = list.get(i2);
                stringMap.putValue(property.getName(), property.getValue());
            }
        }
    }

    private static List<ContextDataProvider> getProviders() {
        ArrayList<ContextDataProvider> arrayList = new ArrayList<ContextDataProvider>(contextDataProviders);
        for (ClassLoader classLoader : LoaderUtil.getClassLoaders()) {
            try {
                for (ContextDataProvider contextDataProvider : ServiceLoader.load(ContextDataProvider.class, classLoader)) {
                    if (!arrayList.stream().noneMatch(contextDataProvider2 -> contextDataProvider2.getClass().isAssignableFrom(contextDataProvider.getClass()))) continue;
                    arrayList.add(contextDataProvider);
                }
            }
            catch (Throwable throwable) {
                LOGGER.debug("Unable to access Context Data Providers {}", (Object)throwable.getMessage());
            }
        }
        return arrayList;
    }

    static /* synthetic */ List access$000() {
        return ThreadContextDataInjector.getProviders();
    }

    public static class ForCopyOnWriteThreadContextMap
    implements ContextDataInjector {
        private final List<ContextDataProvider> providers = ThreadContextDataInjector.access$000();

        @Override
        public StringMap injectContextData(List<Property> stringMapArray, StringMap stringMap) {
            if (this.providers.size() == 1 && (stringMapArray == null || stringMapArray.isEmpty())) {
                return this.providers.get(0).supplyStringMap();
            }
            int n = stringMapArray == null ? 0 : stringMapArray.size();
            StringMap[] stringMapArray2 = new StringMap[this.providers.size()];
            for (int i2 = 0; i2 < this.providers.size(); ++i2) {
                stringMapArray2[i2] = this.providers.get(i2).supplyStringMap();
                n += stringMapArray2[i2].size();
            }
            StringMap stringMap2 = ContextDataFactory.createContextData(n);
            ThreadContextDataInjector.copyProperties(stringMapArray, stringMap2);
            stringMapArray = stringMapArray2;
            n = stringMapArray2.length;
            for (int i3 = 0; i3 < n; ++i3) {
                StringMap stringMap3 = stringMapArray[i3];
                stringMap2.putAll(stringMap3);
            }
            return stringMap2;
        }

        @Override
        public ReadOnlyStringMap rawContextData() {
            return ThreadContext.getThreadContextMap().getReadOnlyContextData();
        }
    }

    public static class ForGarbageFreeThreadContextMap
    implements ContextDataInjector {
        private final List<ContextDataProvider> providers = ThreadContextDataInjector.access$000();

        @Override
        public StringMap injectContextData(List<Property> list, StringMap stringMap) {
            ThreadContextDataInjector.copyProperties(list, stringMap);
            for (int i2 = 0; i2 < this.providers.size(); ++i2) {
                stringMap.putAll(this.providers.get(i2).supplyStringMap());
            }
            return stringMap;
        }

        @Override
        public ReadOnlyStringMap rawContextData() {
            return ThreadContext.getThreadContextMap().getReadOnlyContextData();
        }
    }

    public static class ForDefaultThreadContextMap
    implements ContextDataInjector {
        private final List<ContextDataProvider> providers = ThreadContextDataInjector.access$000();

        @Override
        public StringMap injectContextData(List<Property> list, StringMap object) {
            if (this.providers.size() == 1) {
                object = this.providers.get(0).supplyContextData();
            } else {
                object = new HashMap();
                for (ContextDataProvider contextDataProvider : this.providers) {
                    object.putAll(contextDataProvider.supplyContextData());
                }
            }
            if (list == null || list.isEmpty()) {
                if (object.isEmpty()) {
                    return ContextDataFactory.emptyFrozenContextData();
                }
                return ForDefaultThreadContextMap.frozenStringMap((Map<String, String>)object);
            }
            JdkMapAdapterStringMap jdkMapAdapterStringMap = new JdkMapAdapterStringMap(new HashMap<String, String>((Map<String, String>)object));
            for (int i2 = 0; i2 < list.size(); ++i2) {
                Property property = list.get(i2);
                if (object.containsKey(property.getName())) continue;
                jdkMapAdapterStringMap.putValue(property.getName(), property.getValue());
            }
            jdkMapAdapterStringMap.freeze();
            return jdkMapAdapterStringMap;
        }

        private static JdkMapAdapterStringMap frozenStringMap(Map<String, String> object) {
            object = new JdkMapAdapterStringMap((Map<String, String>)object);
            ((JdkMapAdapterStringMap)object).freeze();
            return object;
        }

        @Override
        public ReadOnlyStringMap rawContextData() {
            Map<String, String> map = ThreadContext.getThreadContextMap();
            if (map instanceof ReadOnlyStringMap) {
                return (ReadOnlyStringMap)((Object)map);
            }
            map = ThreadContext.getImmutableContext();
            if (map.isEmpty()) {
                return ContextDataFactory.emptyFrozenContextData();
            }
            return new JdkMapAdapterStringMap(map);
        }
    }
}

