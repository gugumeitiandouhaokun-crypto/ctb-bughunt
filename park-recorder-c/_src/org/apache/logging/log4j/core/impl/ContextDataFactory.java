/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import java.lang.reflect.Constructor;
import java.util.Map;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.util.IndexedStringMap;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringMap;

public class ContextDataFactory {
    private static final String CLASS_NAME = PropertiesUtil.getProperties().getStringProperty("log4j2.ContextData");
    private static final Class<? extends StringMap> CACHED_CLASS = ContextDataFactory.createCachedClass(CLASS_NAME);
    private static final Constructor<?> DEFAULT_CONSTRUCTOR = ContextDataFactory.createDefaultConstructor(CACHED_CLASS);
    private static final Constructor<?> INITIAL_CAPACITY_CONSTRUCTOR = ContextDataFactory.createInitialCapacityConstructor(CACHED_CLASS);
    private static final StringMap EMPTY_STRING_MAP = ContextDataFactory.createContextData(0);

    private static Class<? extends StringMap> createCachedClass(String string) {
        if (string == null) {
            return null;
        }
        try {
            return Loader.loadClass(string).asSubclass(IndexedStringMap.class);
        }
        catch (Exception exception) {
            return null;
        }
    }

    private static Constructor<?> createDefaultConstructor(Class<? extends StringMap> clazz) {
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.getConstructor(new Class[0]);
        }
        catch (IllegalAccessError | NoSuchMethodException throwable) {
            return null;
        }
    }

    private static Constructor<?> createInitialCapacityConstructor(Class<? extends StringMap> clazz) {
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.getConstructor(Integer.TYPE);
        }
        catch (IllegalAccessError | NoSuchMethodException throwable) {
            return null;
        }
    }

    public static StringMap createContextData() {
        if (DEFAULT_CONSTRUCTOR == null) {
            return new SortedArrayStringMap();
        }
        try {
            return (IndexedStringMap)DEFAULT_CONSTRUCTOR.newInstance(new Object[0]);
        }
        catch (Throwable throwable) {
            return new SortedArrayStringMap();
        }
    }

    public static StringMap createContextData(int n) {
        if (INITIAL_CAPACITY_CONSTRUCTOR == null) {
            return new SortedArrayStringMap(n);
        }
        try {
            return (IndexedStringMap)INITIAL_CAPACITY_CONSTRUCTOR.newInstance(n);
        }
        catch (Throwable throwable) {
            return new SortedArrayStringMap(n);
        }
    }

    public static StringMap createContextData(Map<String, String> object) {
        StringMap stringMap = ContextDataFactory.createContextData(object.size());
        for (Map.Entry entry : object.entrySet()) {
            stringMap.putValue((String)entry.getKey(), entry.getValue());
        }
        return stringMap;
    }

    public static StringMap createContextData(ReadOnlyStringMap readOnlyStringMap) {
        StringMap stringMap = ContextDataFactory.createContextData(readOnlyStringMap.size());
        stringMap.putAll(readOnlyStringMap);
        return stringMap;
    }

    public static StringMap emptyFrozenContextData() {
        return EMPTY_STRING_MAP;
    }

    static {
        EMPTY_STRING_MAP.freeze();
    }
}

