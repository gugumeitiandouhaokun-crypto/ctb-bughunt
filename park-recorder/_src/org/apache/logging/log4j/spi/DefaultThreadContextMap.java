/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.spi.ThreadContextMap;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.TriConsumer;

public class DefaultThreadContextMap
implements ThreadContextMap,
ReadOnlyStringMap {
    private static final long serialVersionUID = 8218007901108944053L;
    public static final String INHERITABLE_MAP = "isThreadContextMapInheritable";
    private final boolean useMap;
    private final ThreadLocal<Map<String, String>> localMap;
    private static boolean inheritableMap;

    static ThreadLocal<Map<String, String>> createThreadLocalMap(final boolean bl) {
        if (inheritableMap) {
            return new InheritableThreadLocal<Map<String, String>>(){

                @Override
                protected final Map<String, String> childValue(Map<String, String> map) {
                    if (map != null && bl) {
                        return Collections.unmodifiableMap(new HashMap<String, String>(map));
                    }
                    return null;
                }
            };
        }
        return new ThreadLocal<Map<String, String>>();
    }

    static void init() {
        inheritableMap = PropertiesUtil.getProperties().getBooleanProperty(INHERITABLE_MAP);
    }

    public DefaultThreadContextMap() {
        this(true);
    }

    public DefaultThreadContextMap(boolean bl) {
        this.useMap = bl;
        this.localMap = DefaultThreadContextMap.createThreadLocalMap(bl);
    }

    @Override
    public void put(String string, String string2) {
        if (!this.useMap) {
            return;
        }
        Map<String, String> map = this.localMap.get();
        map = map == null ? new HashMap<String, String>(1) : new HashMap<String, String>(map);
        map.put(string, string2);
        this.localMap.set(Collections.unmodifiableMap(map));
    }

    public void putAll(Map<String, String> object) {
        if (!this.useMap) {
            return;
        }
        Map<String, String> map = this.localMap.get();
        map = map == null ? new HashMap<String, String>(object.size()) : new HashMap<String, String>(map);
        for (Map.Entry entry : object.entrySet()) {
            map.put((String)entry.getKey(), (String)entry.getValue());
        }
        this.localMap.set(Collections.unmodifiableMap(map));
    }

    @Override
    public String get(String string) {
        Map<String, String> map = this.localMap.get();
        if (map == null) {
            return null;
        }
        return map.get(string);
    }

    @Override
    public void remove(String string) {
        Map<String, String> map = this.localMap.get();
        if (map != null) {
            map = new HashMap<String, String>(map);
            map.remove(string);
            this.localMap.set(Collections.unmodifiableMap(map));
        }
    }

    public void removeAll(Iterable<String> object) {
        Map<String, String> map = this.localMap.get();
        if (map != null) {
            map = new HashMap<String, String>(map);
            object = object.iterator();
            while (object.hasNext()) {
                String string = (String)object.next();
                map.remove(string);
            }
            this.localMap.set(Collections.unmodifiableMap(map));
        }
    }

    @Override
    public void clear() {
        this.localMap.remove();
    }

    @Override
    public Map<String, String> toMap() {
        return this.getCopy();
    }

    @Override
    public boolean containsKey(String string) {
        Map<String, String> map = this.localMap.get();
        return map != null && map.containsKey(string);
    }

    @Override
    public <V> void forEach(BiConsumer<String, ? super V> biConsumer) {
        Map<String, String> map = this.localMap.get();
        if (map == null) {
            return;
        }
        for (Map.Entry entry : map.entrySet()) {
            Object v = entry.getValue();
            biConsumer.accept((String)entry.getKey(), v);
        }
    }

    @Override
    public <V, S> void forEach(TriConsumer<String, ? super V, S> triConsumer, S s) {
        Map<String, String> map = this.localMap.get();
        if (map == null) {
            return;
        }
        for (Map.Entry entry : map.entrySet()) {
            Object v = entry.getValue();
            triConsumer.accept((String)entry.getKey(), v, s);
        }
    }

    @Override
    public <V> V getValue(String string) {
        Map<String, String> map = this.localMap.get();
        if (map == null) {
            return null;
        }
        return (V)map.get(string);
    }

    @Override
    public Map<String, String> getCopy() {
        Map<String, String> map = this.localMap.get();
        if (map == null) {
            return new HashMap<String, String>();
        }
        return new HashMap<String, String>(map);
    }

    @Override
    public Map<String, String> getImmutableMapOrNull() {
        return this.localMap.get();
    }

    @Override
    public boolean isEmpty() {
        Map<String, String> map = this.localMap.get();
        return map == null || map.size() == 0;
    }

    @Override
    public int size() {
        Map<String, String> map = this.localMap.get();
        if (map == null) {
            return 0;
        }
        return map.size();
    }

    public String toString() {
        Map<String, String> map = this.localMap.get();
        if (map == null) {
            return "{}";
        }
        return map.toString();
    }

    public int hashCode() {
        Map<String, String> map = this.localMap.get();
        int n = 31 + (map == null ? 0 : map.hashCode());
        return 31 * n + Boolean.valueOf(this.useMap).hashCode();
    }

    public boolean equals(Object map) {
        Object object;
        if (this == map) {
            return true;
        }
        if (map == null) {
            return false;
        }
        if (map instanceof DefaultThreadContextMap) {
            object = (DefaultThreadContextMap)((Object)map);
            if (this.useMap != ((DefaultThreadContextMap)object).useMap) {
                return false;
            }
        }
        if (!(map instanceof ThreadContextMap)) {
            return false;
        }
        object = (ThreadContextMap)((Object)map);
        map = this.localMap.get();
        object = object.getImmutableMapOrNull();
        return !(map == null ? object != null : !map.equals(object));
    }

    static {
        DefaultThreadContextMap.init();
    }
}

