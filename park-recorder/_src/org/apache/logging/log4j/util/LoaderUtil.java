/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Objects;
import org.apache.logging.log4j.util.LowLevelLogUtil;
import org.apache.logging.log4j.util.PropertiesUtil;

public final class LoaderUtil {
    public static final String IGNORE_TCCL_PROPERTY = "log4j.ignoreTCL";
    private static final SecurityManager SECURITY_MANAGER = System.getSecurityManager();
    private static Boolean ignoreTCCL;
    private static final boolean GET_CLASS_LOADER_DISABLED;
    private static final PrivilegedAction<ClassLoader> TCCL_GETTER;

    private LoaderUtil() {
    }

    public static ClassLoader getThreadContextClassLoader() {
        if (GET_CLASS_LOADER_DISABLED) {
            return LoaderUtil.class.getClassLoader();
        }
        if (SECURITY_MANAGER == null) {
            return TCCL_GETTER.run();
        }
        return AccessController.doPrivileged(TCCL_GETTER);
    }

    public static ClassLoader[] getClassLoaders() {
        LinkedHashSet<ClassLoader> linkedHashSet = new LinkedHashSet<ClassLoader>();
        ClassLoader classLoader = LoaderUtil.getThreadContextClassLoader();
        if (classLoader != null) {
            linkedHashSet.add(classLoader);
        }
        LoaderUtil.accumulateClassLoaders(LoaderUtil.class.getClassLoader(), linkedHashSet);
        LoaderUtil.accumulateClassLoaders(classLoader == null ? null : classLoader.getParent(), linkedHashSet);
        classLoader = ClassLoader.getSystemClassLoader();
        if (classLoader != null) {
            linkedHashSet.add(classLoader);
        }
        LinkedHashSet<ClassLoader> linkedHashSet2 = linkedHashSet;
        return linkedHashSet2.toArray(new ClassLoader[linkedHashSet2.size()]);
    }

    private static void accumulateClassLoaders(ClassLoader classLoader, Collection<ClassLoader> collection) {
        while (classLoader != null && collection.add(classLoader)) {
            classLoader = classLoader.getParent();
        }
    }

    public static boolean isClassAvailable(String string) {
        try {
            return LoaderUtil.loadClass(string) != null;
        }
        catch (ClassNotFoundException | LinkageError throwable) {
            return false;
        }
        catch (Throwable throwable) {
            LowLevelLogUtil.logException("Unknown error checking for existence of class: ".concat(String.valueOf(string)), throwable);
            return false;
        }
    }

    public static Class<?> loadClass(String string) {
        if (LoaderUtil.isIgnoreTccl()) {
            return Class.forName(string);
        }
        try {
            ClassLoader classLoader = LoaderUtil.getThreadContextClassLoader();
            if (classLoader != null) {
                return classLoader.loadClass(string);
            }
        }
        catch (Throwable throwable) {}
        return Class.forName(string);
    }

    public static <T> T newInstanceOf(Class<T> clazz) {
        try {
            return clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (NoSuchMethodException noSuchMethodException) {
            return clazz.newInstance();
        }
    }

    public static <T> T newInstanceOf(String string) {
        return (T)LoaderUtil.newInstanceOf(LoaderUtil.loadClass(string));
    }

    public static <T> T newCheckedInstanceOf(String string, Class<T> clazz) {
        return clazz.cast(LoaderUtil.newInstanceOf(string));
    }

    public static <T> T newCheckedInstanceOfProperty(String string, Class<T> clazz) {
        string = PropertiesUtil.getProperties().getStringProperty(string);
        if (string == null) {
            return null;
        }
        return LoaderUtil.newCheckedInstanceOf(string, clazz);
    }

    private static boolean isIgnoreTccl() {
        if (ignoreTCCL == null) {
            String string = PropertiesUtil.getProperties().getStringProperty(IGNORE_TCCL_PROPERTY, null);
            ignoreTCCL = string != null && !"false".equalsIgnoreCase(string.trim());
        }
        return ignoreTCCL;
    }

    public static Collection<URL> findResources(String iterator) {
        iterator = LoaderUtil.findUrlResources((String)((Object)iterator));
        LinkedHashSet<URL> linkedHashSet = new LinkedHashSet<URL>(iterator.size());
        iterator = iterator.iterator();
        while (iterator.hasNext()) {
            UrlResource urlResource = (UrlResource)iterator.next();
            linkedHashSet.add(urlResource.getUrl());
        }
        return linkedHashSet;
    }

    static Collection<UrlResource> findUrlResources(String string) {
        ClassLoader[] classLoaderArray = new ClassLoader[]{LoaderUtil.getThreadContextClassLoader(), LoaderUtil.class.getClassLoader(), GET_CLASS_LOADER_DISABLED ? null : ClassLoader.getSystemClassLoader()};
        LinkedHashSet<UrlResource> linkedHashSet = new LinkedHashSet<UrlResource>();
        for (int j = 0; j < 3; ++j) {
            ClassLoader classLoader = classLoaderArray[j];
            if (classLoader == null) continue;
            try {
                Enumeration<URL> enumeration = classLoader.getResources(string);
                while (enumeration.hasMoreElements()) {
                    linkedHashSet.add(new UrlResource(classLoader, enumeration.nextElement()));
                }
                continue;
            }
            catch (IOException iOException) {
                LowLevelLogUtil.logException(iOException);
            }
        }
        return linkedHashSet;
    }

    static {
        TCCL_GETTER = new ThreadContextClassLoaderGetter();
        if (SECURITY_MANAGER != null) {
            boolean bl;
            try {
                SECURITY_MANAGER.checkPermission(new RuntimePermission("getClassLoader"));
                bl = false;
            }
            catch (SecurityException securityException) {
                bl = true;
            }
            GET_CLASS_LOADER_DISABLED = bl;
        } else {
            GET_CLASS_LOADER_DISABLED = false;
        }
    }

    static class UrlResource {
        private final ClassLoader classLoader;
        private final URL url;

        UrlResource(ClassLoader classLoader, URL uRL) {
            this.classLoader = classLoader;
            this.url = uRL;
        }

        public ClassLoader getClassLoader() {
            return this.classLoader;
        }

        public URL getUrl() {
            return this.url;
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            object = (UrlResource)object;
            if (this.classLoader != null ? !this.classLoader.equals(((UrlResource)object).classLoader) : ((UrlResource)object).classLoader != null) {
                return false;
            }
            return !(this.url != null ? !this.url.equals(((UrlResource)object).url) : ((UrlResource)object).url != null);
        }

        public int hashCode() {
            return Objects.hashCode(this.classLoader) + Objects.hashCode(this.url);
        }
    }

    static class ThreadContextClassLoaderGetter
    implements PrivilegedAction<ClassLoader> {
        private ThreadContextClassLoaderGetter() {
        }

        @Override
        public ClassLoader run() {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                return classLoader;
            }
            classLoader = LoaderUtil.class.getClassLoader();
            if (classLoader == null && !GET_CLASS_LOADER_DISABLED) {
                return ClassLoader.getSystemClassLoader();
            }
            return classLoader;
        }
    }
}

