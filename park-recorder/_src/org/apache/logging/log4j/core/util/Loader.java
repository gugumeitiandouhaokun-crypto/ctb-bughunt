/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.io.InputStream;
import java.net.URL;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.PropertiesUtil;

public final class Loader {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String TSTR = "Caught Exception while in Loader.getResource. This may be innocuous.";

    private Loader() {
    }

    public static ClassLoader getClassLoader() {
        return Loader.getClassLoader(Loader.class, null);
    }

    public static ClassLoader getThreadContextClassLoader() {
        return LoaderUtil.getThreadContextClassLoader();
    }

    public static ClassLoader getClassLoader(Class<?> object, Class<?> object2) {
        ClassLoader classLoader = Loader.getThreadContextClassLoader();
        object = object == null ? null : ((Class)object).getClassLoader();
        Object object3 = object2 = object2 == null ? null : ((Class)object2).getClassLoader();
        if (Loader.isChild(classLoader, (ClassLoader)object)) {
            if (Loader.isChild(classLoader, (ClassLoader)object2)) {
                return classLoader;
            }
            return object2;
        }
        if (Loader.isChild((ClassLoader)object, (ClassLoader)object2)) {
            return object;
        }
        return object2;
    }

    public static URL getResource(String string, ClassLoader classLoader) {
        try {
            Object object = Loader.getThreadContextClassLoader();
            if (object != null) {
                LOGGER.trace("Trying to find [{}] using context class loader {}.", (Object)string, object);
                object = ((ClassLoader)object).getResource(string);
                if (object != null) {
                    return object;
                }
            }
            if ((object = Loader.class.getClassLoader()) != null) {
                LOGGER.trace("Trying to find [{}] using {} class loader.", (Object)string, object);
                object = ((ClassLoader)object).getResource(string);
                if (object != null) {
                    return object;
                }
            }
            if (classLoader != null) {
                LOGGER.trace("Trying to find [{}] using {} class loader.", (Object)string, (Object)classLoader);
                object = classLoader.getResource(string);
                if (object != null) {
                    return object;
                }
            }
        }
        catch (Throwable throwable) {
            LOGGER.warn(TSTR, throwable);
        }
        LOGGER.trace("Trying to find [{}] using ClassLoader.getSystemResource().", (Object)string);
        return ClassLoader.getSystemResource(string);
    }

    public static InputStream getResourceAsStream(String string, ClassLoader classLoader) {
        try {
            Object object = Loader.getThreadContextClassLoader();
            if (object != null) {
                LOGGER.trace("Trying to find [{}] using context class loader {}.", (Object)string, object);
                object = ((ClassLoader)object).getResourceAsStream(string);
                if (object != null) {
                    return object;
                }
            }
            if ((object = Loader.class.getClassLoader()) != null) {
                LOGGER.trace("Trying to find [{}] using {} class loader.", (Object)string, object);
                object = ((ClassLoader)object).getResourceAsStream(string);
                if (object != null) {
                    return object;
                }
            }
            if (classLoader != null) {
                LOGGER.trace("Trying to find [{}] using {} class loader.", (Object)string, (Object)classLoader);
                object = classLoader.getResourceAsStream(string);
                if (object != null) {
                    return object;
                }
            }
        }
        catch (Throwable throwable) {
            LOGGER.warn(TSTR, throwable);
        }
        LOGGER.trace("Trying to find [{}] using ClassLoader.getSystemResource().", (Object)string);
        return ClassLoader.getSystemResourceAsStream(string);
    }

    private static boolean isChild(ClassLoader classLoader, ClassLoader classLoader2) {
        if (classLoader != null && classLoader2 != null) {
            for (classLoader = classLoader.getParent(); classLoader != null && classLoader != classLoader2; classLoader = classLoader.getParent()) {
            }
            return classLoader != null;
        }
        return classLoader != null;
    }

    public static Class<?> initializeClass(String string, ClassLoader classLoader) {
        return Class.forName(string, true, classLoader);
    }

    public static Class<?> loadClass(String string, ClassLoader classLoader) {
        if (classLoader != null) {
            return classLoader.loadClass(string);
        }
        return null;
    }

    public static Class<?> loadSystemClass(String string) {
        try {
            return Class.forName(string, true, ClassLoader.getSystemClassLoader());
        }
        catch (Throwable throwable) {
            LOGGER.trace("Couldn't use SystemClassLoader. Trying Class.forName({}).", (Object)string, (Object)throwable);
            return Class.forName(string);
        }
    }

    public static <T> T newInstanceOf(String string) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(Loader.getClassLoader());
            string = LoaderUtil.newInstanceOf(string);
            return (T)string;
        }
        finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    public static <T> T newCheckedInstanceOf(String string, Class<T> clazz) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(Loader.getClassLoader());
            string = LoaderUtil.newCheckedInstanceOf(string, clazz);
            return (T)string;
        }
        finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    public static <T> T newCheckedInstanceOfProperty(String string, Class<T> clazz) {
        PropertiesUtil.getProperties().getStringProperty(string);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(Loader.getClassLoader());
            string = LoaderUtil.newCheckedInstanceOfProperty(string, clazz);
            return (T)string;
        }
        finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    public static boolean isClassAvailable(String string) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(Loader.getClassLoader());
            boolean bl = LoaderUtil.isClassAvailable(string);
            return bl;
        }
        finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    public static boolean isJansiAvailable() {
        return Loader.isClassAvailable("org.fusesource.jansi.AnsiRenderer");
    }

    public static Class<?> loadClass(String object) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(Loader.getClassLoader());
            object = LoaderUtil.loadClass((String)object);
            return object;
        }
        finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }
}

