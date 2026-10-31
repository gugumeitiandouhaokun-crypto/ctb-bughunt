/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.apache.logging.log4j.core.impl.ExtendedClassInfo;
import org.apache.logging.log4j.core.impl.ExtendedStackTraceElement;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;

class ThrowableProxyHelper {
    static final ThrowableProxy[] EMPTY_THROWABLE_PROXY_ARRAY = new ThrowableProxy[0];

    private ThrowableProxyHelper() {
    }

    static ExtendedStackTraceElement[] toExtendedStackTrace(ThrowableProxy throwableProxy, Stack<Class<?>> stack, Map<String, CacheEntry> map, StackTraceElement[] object, StackTraceElement[] stackTraceElementArray) {
        int n;
        if (object != null) {
            int n2;
            int n3 = ((StackTraceElement[])object).length - 1;
            for (n2 = stackTraceElementArray.length - 1; n3 >= 0 && n2 >= 0 && object[n3].equals(stackTraceElementArray[n2]); --n3, --n2) {
            }
            throwableProxy.setCommonElementCount(stackTraceElementArray.length - 1 - n2);
            n = n2 + 1;
        } else {
            throwableProxy.setCommonElementCount(0);
            n = stackTraceElementArray.length;
        }
        ExtendedStackTraceElement[] extendedStackTraceElementArray = new ExtendedStackTraceElement[n];
        Class<?> clazz = stack.isEmpty() ? null : stack.peek();
        object = null;
        --n;
        while (n >= 0) {
            ExtendedClassInfo extendedClassInfo;
            CacheEntry cacheEntry;
            StackTraceElement stackTraceElement = stackTraceElementArray[n];
            String string = stackTraceElement.getClassName();
            if (clazz != null && string.equals(clazz.getName())) {
                cacheEntry = ThrowableProxyHelper.toCacheEntry(clazz, true);
                extendedClassInfo = cacheEntry.element;
                object = cacheEntry.loader;
                stack.pop();
                clazz = stack.isEmpty() ? null : stack.peek();
            } else {
                cacheEntry = map.get(string);
                if (cacheEntry != null) {
                    extendedClassInfo = cacheEntry.element;
                    if (cacheEntry.loader != null) {
                        object = cacheEntry.loader;
                    }
                } else {
                    cacheEntry = ThrowableProxyHelper.toCacheEntry(ThrowableProxyHelper.loadClass((ClassLoader)object, string), false);
                    extendedClassInfo = cacheEntry.element;
                    map.put(string, cacheEntry);
                    if (cacheEntry.loader != null) {
                        object = cacheEntry.loader;
                    }
                }
            }
            extendedStackTraceElementArray[n] = new ExtendedStackTraceElement(stackTraceElement, extendedClassInfo);
            --n;
        }
        return extendedStackTraceElementArray;
    }

    static ThrowableProxy[] toSuppressedProxies(Throwable throwableArray, Set<Throwable> set) {
        try {
            throwableArray = throwableArray.getSuppressed();
            if (throwableArray == null || throwableArray.length == 0) {
                return EMPTY_THROWABLE_PROXY_ARRAY;
            }
            ArrayList<ThrowableProxy> arrayList = new ArrayList<ThrowableProxy>(throwableArray.length);
            if (set == null) {
                set = new HashSet<Throwable>(throwableArray.length);
            }
            for (int j = 0; j < throwableArray.length; ++j) {
                Throwable throwable = throwableArray[j];
                if (!set.add(throwable)) continue;
                arrayList.add(new ThrowableProxy(throwable, set));
            }
            ArrayList<ThrowableProxy> arrayList2 = arrayList;
            return arrayList2.toArray(new ThrowableProxy[arrayList2.size()]);
        }
        catch (Exception exception) {
            StatusLogger.getLogger().error(exception);
            return null;
        }
    }

    private static CacheEntry toCacheEntry(Class<?> clazz, boolean bl) {
        String string = "?";
        Object object = "?";
        Object object2 = null;
        if (clazz != null) {
            try {
                object2 = clazz.getProtectionDomain().getCodeSource();
                if (object2 != null && (object2 = ((CodeSource)object2).getLocation()) != null) {
                    int n = ((String)(object2 = ((URL)object2).toString().replace('\\', '/'))).lastIndexOf("/");
                    if (n >= 0 && n == ((String)object2).length() - 1) {
                        n = ((String)object2).lastIndexOf("/", n - 1);
                        string = ((String)object2).substring(n + 1);
                    } else {
                        string = ((String)object2).substring(n + 1);
                    }
                }
            }
            catch (Exception exception) {}
            object2 = clazz.getPackage();
            if (object2 != null && (object2 = ((Package)object2).getImplementationVersion()) != null) {
                object = object2;
            }
            try {
                object2 = clazz.getClassLoader();
            }
            catch (SecurityException securityException) {
                object2 = null;
            }
        }
        return new CacheEntry(new ExtendedClassInfo(bl, string, (String)object), (ClassLoader)object2);
    }

    private static Class<?> loadClass(ClassLoader clazz, String string) {
        if (clazz != null) {
            try {
                clazz = ((ClassLoader)((Object)clazz)).loadClass(string);
                if (clazz != null) {
                    return clazz;
                }
            }
            catch (Throwable throwable) {}
        }
        try {
            clazz = LoaderUtil.loadClass(string);
        }
        catch (ClassNotFoundException | NoClassDefFoundError throwable) {
            return ThrowableProxyHelper.loadClass(string);
        }
        catch (SecurityException securityException) {
            return null;
        }
        return clazz;
    }

    private static Class<?> loadClass(String string) {
        try {
            return Loader.loadClass(string, ThrowableProxyHelper.class.getClassLoader());
        }
        catch (ClassNotFoundException | NoClassDefFoundError | SecurityException throwable) {
            return null;
        }
    }

    static final class CacheEntry {
        private final ExtendedClassInfo element;
        private final ClassLoader loader;

        private CacheEntry(ExtendedClassInfo extendedClassInfo, ClassLoader classLoader) {
            this.element = extendedClassInfo;
            this.loader = classLoader;
        }
    }
}

