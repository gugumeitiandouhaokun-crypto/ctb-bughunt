/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.util.PluginRegistry;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

public class PluginManager {
    private static final CopyOnWriteArrayList<String> PACKAGES = new CopyOnWriteArrayList();
    private static final String LOG4J_PACKAGES = "org.apache.logging.log4j.core";
    private static final Logger LOGGER = StatusLogger.getLogger();
    private Map<String, PluginType<?>> plugins = new HashMap();
    private final String category;

    public PluginManager(String string) {
        this.category = string;
    }

    @Deprecated
    public static void main(String[] stringArray) {
        System.err.println("ERROR: this tool is superseded by the annotation processor included in log4j-core.");
        System.err.println("If the annotation processor does not work for you, please see the manual page:");
        System.err.println("http://logging.apache.org/log4j/2.x/manual/configuration.html#ConfigurationSyntax");
        System.exit(-1);
    }

    public static void addPackage(String string) {
        if (Strings.isBlank(string)) {
            return;
        }
        PACKAGES.addIfAbsent(string);
    }

    public static void addPackages(Collection<String> object) {
        object = object.iterator();
        while (object.hasNext()) {
            String string = (String)object.next();
            if (!Strings.isNotBlank(string)) continue;
            PACKAGES.addIfAbsent(string);
        }
    }

    public PluginType<?> getPluginType(String string) {
        return this.plugins.get(string.toLowerCase());
    }

    public Map<String, PluginType<?>> getPlugins() {
        return this.plugins;
    }

    public void collectPlugins() {
        this.collectPlugins(null);
    }

    public void collectPlugins(List<String> list) {
        String string = this.category.toLowerCase();
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        Object object = PluginRegistry.getInstance().loadFromMainClassLoader();
        if (object.isEmpty()) {
            object = PluginRegistry.getInstance().loadFromPackage(LOG4J_PACKAGES);
        }
        PluginManager.mergeByName(linkedHashMap, object.get(string));
        for (Map object2 : PluginRegistry.getInstance().getPluginsByCategoryByBundleId().values()) {
            PluginManager.mergeByName(linkedHashMap, (List)object2.get(string));
        }
        for (String string2 : PACKAGES) {
            PluginManager.mergeByName(linkedHashMap, PluginRegistry.getInstance().loadFromPackage(string2).get(string));
        }
        if (list != null) {
            for (String string3 : list) {
                PluginManager.mergeByName(linkedHashMap, PluginRegistry.getInstance().loadFromPackage(string3).get(string));
            }
        }
        LOGGER.debug("PluginManager '{}' found {} plugins", (Object)this.category, (Object)linkedHashMap.size());
        this.plugins = linkedHashMap;
    }

    private static void mergeByName(Map<String, PluginType<?>> map, List<PluginType<?>> object) {
        if (object == null) {
            return;
        }
        object = object.iterator();
        while (object.hasNext()) {
            PluginType pluginType = (PluginType)object.next();
            String string = pluginType.getKey();
            PluginType<?> pluginType2 = map.get(string);
            if (pluginType2 == null) {
                map.put(string, pluginType);
                continue;
            }
            if (pluginType2.getPluginClass().equals(pluginType.getPluginClass())) continue;
            LOGGER.warn("Plugin [{}] is already mapped to {}, ignoring {}", (Object)string, (Object)pluginType2.getPluginClass(), (Object)pluginType.getPluginClass());
        }
    }
}

