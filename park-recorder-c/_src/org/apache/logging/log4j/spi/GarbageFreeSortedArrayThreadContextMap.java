/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.spi.ObjectThreadContextMap;
import org.apache.logging.log4j.spi.ReadOnlyThreadContextMap;
import org.apache.logging.log4j.spi.ThreadContextMap;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringMap;

class GarbageFreeSortedArrayThreadContextMap
implements ObjectThreadContextMap,
ReadOnlyThreadContextMap {
    public static final String INHERITABLE_MAP = "isThreadContextMapInheritable";
    protected static final int DEFAULT_INITIAL_CAPACITY = 16;
    protected static final String PROPERTY_NAME_INITIAL_CAPACITY = "log4j2.ThreadContext.initial.capacity";
    protected final ThreadLocal<StringMap> localMap = this.createThreadLocalMap();
    private static volatile int initialCapacity;
    private static volatile boolean inheritableMap;

    static void init() {
        PropertiesUtil propertiesUtil = PropertiesUtil.getProperties();
        initialCapacity = propertiesUtil.getIntegerProperty(PROPERTY_NAME_INITIAL_CAPACITY, 16);
        inheritableMap = propertiesUtil.getBooleanProperty(INHERITABLE_MAP);
    }

    private ThreadLocal<StringMap> createThreadLocalMap() {
        if (inheritableMap) {
            return new InheritableThreadLocal<StringMap>(){

                @Override
                protected StringMap childValue(StringMap stringMap) {
                    if (stringMap != null) {
                        return GarbageFreeSortedArrayThreadContextMap.this.createStringMap(stringMap);
                    }
                    return null;
                }
            };
        }
        return new ThreadLocal<StringMap>();
    }

    protected StringMap createStringMap() {
        return new SortedArrayStringMap(initialCapacity);
    }

    protected StringMap createStringMap(ReadOnlyStringMap readOnlyStringMap) {
        return new SortedArrayStringMap(readOnlyStringMap);
    }

    private StringMap getThreadLocalMap() {
        StringMap stringMap = this.localMap.get();
        if (stringMap == null) {
            stringMap = this.createStringMap();
            this.localMap.set(stringMap);
        }
        return stringMap;
    }

    @Override
    public void put(String string, String string2) {
        this.getThreadLocalMap().putValue(string, string2);
    }

    public void putValue(String string, Object object) {
        this.getThreadLocalMap().putValue(string, object);
    }

    @Override
    public void putAll(Map<String, String> object) {
        if (object == null || object.isEmpty()) {
            return;
        }
        StringMap stringMap = this.getThreadLocalMap();
        for (Map.Entry entry : object.entrySet()) {
            stringMap.putValue((String)entry.getKey(), entry.getValue());
        }
    }

    @Override
    public <V> void putAllValues(Map<String, V> object) {
        if (object == null || object.isEmpty()) {
            return;
        }
        StringMap stringMap = this.getThreadLocalMap();
        for (Map.Entry entry : object.entrySet()) {
            stringMap.putValue((String)entry.getKey(), entry.getValue());
        }
    }

    @Override
    public String get(String string) {
        return (String)this.getValue(string);
    }

    @Override
    public <V> V getValue(String string) {
        StringMap stringMap = this.localMap.get();
        if (stringMap == null) {
            return null;
        }
        return stringMap.getValue(string);
    }

    @Override
    public void remove(String string) {
        StringMap stringMap = this.localMap.get();
        if (stringMap != null) {
            stringMap.remove(string);
        }
    }

    @Override
    public void removeAll(Iterable<String> object) {
        StringMap stringMap = this.localMap.get();
        if (stringMap != null) {
            object = object.iterator();
            while (object.hasNext()) {
                String string = (String)object.next();
                stringMap.remove(string);
            }
        }
    }

    @Override
    public void clear() {
        StringMap stringMap = this.localMap.get();
        if (stringMap != null) {
            stringMap.clear();
        }
    }

    @Override
    public boolean containsKey(String string) {
        StringMap stringMap = this.localMap.get();
        return stringMap != null && stringMap.containsKey(string);
    }

    @Override
    public Map<String, String> getCopy() {
        StringMap stringMap = this.localMap.get();
        if (stringMap == null) {
            return new HashMap<String, String>();
        }
        return stringMap.toMap();
    }

    @Override
    public StringMap getReadOnlyContextData() {
        StringMap stringMap = this.localMap.get();
        if (stringMap == null) {
            stringMap = this.createStringMap();
            this.localMap.set(stringMap);
        }
        return stringMap;
    }

    @Override
    public Map<String, String> getImmutableMapOrNull() {
        StringMap stringMap = this.localMap.get();
        if (stringMap == null) {
            return null;
        }
        return Collections.unmodifiableMap(stringMap.toMap());
    }

    @Override
    public boolean isEmpty() {
        StringMap stringMap = this.localMap.get();
        return stringMap == null || stringMap.size() == 0;
    }

    public String toString() {
        StringMap stringMap = this.localMap.get();
        if (stringMap == null) {
            return "{}";
        }
        return stringMap.toString();
    }

    public int hashCode() {
        StringMap stringMap = this.localMap.get();
        return 31 + (stringMap == null ? 0 : stringMap.hashCode());
    }

    public boolean equals(Object map) {
        if (this == map) {
            return true;
        }
        if (map == null) {
            return false;
        }
        if (!(map instanceof ThreadContextMap)) {
            return false;
        }
        map = (ThreadContextMap)((Object)map);
        Map<String, String> map2 = this.getImmutableMapOrNull();
        map = map.getImmutableMapOrNull();
        return !(map2 == null ? map != null : !map2.equals(map));
    }

    static {
        GarbageFreeSortedArrayThreadContextMap.init();
    }
}

