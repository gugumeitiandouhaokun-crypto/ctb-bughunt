/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFileWatcher;
import org.apache.logging.log4j.core.config.ConfigurationListener;
import org.apache.logging.log4j.core.config.Reconfigurable;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.util.Source;
import org.apache.logging.log4j.core.util.Throwables;
import org.apache.logging.log4j.core.util.Watcher;
import org.apache.logging.log4j.status.StatusLogger;

public class WatcherFactory {
    private static Logger LOGGER = StatusLogger.getLogger();
    private static PluginManager pluginManager = new PluginManager("Watcher");
    private static volatile WatcherFactory factory = null;
    private final Map<String, PluginType<?>> plugins;

    private WatcherFactory(List<String> list) {
        pluginManager.collectPlugins(list);
        this.plugins = pluginManager.getPlugins();
    }

    public static WatcherFactory getInstance(List<String> list) {
        if (factory == null) {
            PluginManager pluginManager = WatcherFactory.pluginManager;
            synchronized (pluginManager) {
                if (factory == null) {
                    factory = new WatcherFactory(list);
                }
            }
        }
        return factory;
    }

    public Watcher newWatcher(Source object, Configuration configuration, Reconfigurable reconfigurable, List<ConfigurationListener> list, long l) {
        if (((Source)object).getFile() != null) {
            return new ConfigurationFileWatcher(configuration, reconfigurable, list, l);
        }
        PluginType<?> pluginType = this.plugins.get(object = ((Source)object).getURI().getScheme());
        if (pluginType != null) {
            return WatcherFactory.instantiate((String)object, pluginType.getPluginClass(), configuration, reconfigurable, list, l);
        }
        LOGGER.info("No Watcher plugin is available for protocol '{}'", object);
        return null;
    }

    public static <T extends Watcher> T instantiate(String string, Class<T> clazz, Configuration configuration, Reconfigurable reconfigurable, List<ConfigurationListener> list, long l) {
        Objects.requireNonNull(clazz, "No class provided");
        try {
            return (T)((Watcher)clazz.getConstructor(Configuration.class, Reconfigurable.class, List.class, Long.TYPE).newInstance(configuration, reconfigurable, list, l));
        }
        catch (NoSuchMethodException noSuchMethodException) {
            throw new IllegalArgumentException("No valid constructor for Watcher plugin ".concat(String.valueOf(string)), noSuchMethodException);
        }
        catch (InstantiationException | LinkageError throwable) {
            throw new IllegalArgumentException(throwable);
        }
        catch (IllegalAccessException illegalAccessException) {
            throw new IllegalStateException(illegalAccessException);
        }
        catch (InvocationTargetException invocationTargetException) {
            Throwables.rethrow(invocationTargetException.getCause());
            throw new InternalError("Unreachable");
        }
    }
}

