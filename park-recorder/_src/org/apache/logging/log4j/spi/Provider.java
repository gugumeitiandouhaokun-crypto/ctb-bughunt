/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.spi;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Properties;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.spi.ThreadContextMap;
import org.apache.logging.log4j.status.StatusLogger;

public class Provider {
    public static final String FACTORY_PRIORITY = "FactoryPriority";
    public static final String THREAD_CONTEXT_MAP = "ThreadContextMap";
    public static final String LOGGER_CONTEXT_FACTORY = "LoggerContextFactory";
    private static final Integer DEFAULT_PRIORITY = -1;
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final Integer priority;
    private final String className;
    private final Class<? extends LoggerContextFactory> loggerContextFactoryClass;
    private final String threadContextMap;
    private final Class<? extends ThreadContextMap> threadContextMapClass;
    private final String versions;
    private final URL url;
    private final WeakReference<ClassLoader> classLoader;

    public Provider(Properties properties, URL object, ClassLoader classLoader) {
        this.url = object;
        this.classLoader = new WeakReference<ClassLoader>(classLoader);
        object = properties.getProperty(FACTORY_PRIORITY);
        this.priority = object == null ? DEFAULT_PRIORITY : Integer.valueOf((String)object);
        this.className = properties.getProperty(LOGGER_CONTEXT_FACTORY);
        this.threadContextMap = properties.getProperty(THREAD_CONTEXT_MAP);
        this.loggerContextFactoryClass = null;
        this.threadContextMapClass = null;
        this.versions = null;
    }

    public Provider(Integer n, String string, Class<? extends LoggerContextFactory> clazz) {
        this(n, string, clazz, null);
    }

    public Provider(Integer n, String string, Class<? extends LoggerContextFactory> clazz, Class<? extends ThreadContextMap> clazz2) {
        this.url = null;
        this.classLoader = null;
        this.priority = n;
        this.loggerContextFactoryClass = clazz;
        this.threadContextMapClass = clazz2;
        this.className = null;
        this.threadContextMap = null;
        this.versions = string;
    }

    public String getVersions() {
        return this.versions;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public String getClassName() {
        if (this.loggerContextFactoryClass != null) {
            return this.loggerContextFactoryClass.getName();
        }
        return this.className;
    }

    public Class<? extends LoggerContextFactory> loadLoggerContextFactory() {
        if (this.loggerContextFactoryClass != null) {
            return this.loggerContextFactoryClass;
        }
        if (this.className == null) {
            return null;
        }
        Object object = (ClassLoader)this.classLoader.get();
        if (object == null) {
            return null;
        }
        try {
            object = ((ClassLoader)object).loadClass(this.className);
            if (LoggerContextFactory.class.isAssignableFrom((Class<?>)object)) {
                return ((Class)object).asSubclass(LoggerContextFactory.class);
            }
        }
        catch (Exception exception) {
            LOGGER.error("Unable to create class {} specified in {}", (Object)this.className, (Object)this.url.toString(), (Object)exception);
        }
        return null;
    }

    public String getThreadContextMap() {
        if (this.threadContextMapClass != null) {
            return this.threadContextMapClass.getName();
        }
        return this.threadContextMap;
    }

    public Class<? extends ThreadContextMap> loadThreadContextMap() {
        if (this.threadContextMapClass != null) {
            return this.threadContextMapClass;
        }
        if (this.threadContextMap == null) {
            return null;
        }
        Object object = (ClassLoader)this.classLoader.get();
        if (object == null) {
            return null;
        }
        try {
            object = ((ClassLoader)object).loadClass(this.threadContextMap);
            if (ThreadContextMap.class.isAssignableFrom((Class<?>)object)) {
                return ((Class)object).asSubclass(ThreadContextMap.class);
            }
        }
        catch (Exception exception) {
            LOGGER.error("Unable to create class {} specified in {}", (Object)this.threadContextMap, (Object)this.url.toString(), (Object)exception);
        }
        return null;
    }

    public URL getUrl() {
        return this.url;
    }

    public String toString() {
        ClassLoader classLoader;
        StringBuilder stringBuilder = new StringBuilder("Provider[");
        if (!DEFAULT_PRIORITY.equals(this.priority)) {
            stringBuilder.append("priority=").append(this.priority).append(", ");
        }
        if (this.threadContextMap != null) {
            stringBuilder.append("threadContextMap=").append(this.threadContextMap).append(", ");
        } else if (this.threadContextMapClass != null) {
            stringBuilder.append("threadContextMapClass=").append(this.threadContextMapClass.getName());
        }
        if (this.className != null) {
            stringBuilder.append("className=").append(this.className).append(", ");
        } else if (this.loggerContextFactoryClass != null) {
            stringBuilder.append("class=").append(this.loggerContextFactoryClass.getName());
        }
        if (this.url != null) {
            stringBuilder.append("url=").append(this.url);
        }
        if (this.classLoader == null || (classLoader = (ClassLoader)this.classLoader.get()) == null) {
            stringBuilder.append(", classLoader=null(not reachable)");
        } else {
            stringBuilder.append(", classLoader=").append(classLoader);
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        object = (Provider)object;
        if (this.priority != null ? !this.priority.equals(((Provider)object).priority) : ((Provider)object).priority != null) {
            return false;
        }
        if (this.className != null ? !this.className.equals(((Provider)object).className) : ((Provider)object).className != null) {
            return false;
        }
        if (this.loggerContextFactoryClass != null ? !this.loggerContextFactoryClass.equals(((Provider)object).loggerContextFactoryClass) : ((Provider)object).loggerContextFactoryClass != null) {
            return false;
        }
        if (this.versions != null) {
            return this.versions.equals(((Provider)object).versions);
        }
        return ((Provider)object).versions == null;
    }

    public int hashCode() {
        int n = this.priority != null ? this.priority.hashCode() : 0;
        n = 31 * n + (this.className != null ? this.className.hashCode() : 0);
        n = 31 * n + (this.loggerContextFactoryClass != null ? this.loggerContextFactoryClass.hashCode() : 0);
        return 31 * n + (this.versions != null ? this.versions.hashCode() : 0);
    }
}

