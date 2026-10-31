/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name="appenders", category="Core")
public final class AppendersPlugin {
    private AppendersPlugin() {
    }

    @PluginFactory
    public static ConcurrentMap<String, Appender> createAppenders(@PluginElement(value="Appenders") Appender[] appenderArray) {
        ConcurrentHashMap<String, Appender> concurrentHashMap = new ConcurrentHashMap<String, Appender>(appenderArray.length);
        for (Appender appender : appenderArray) {
            concurrentHashMap.put(appender.getName(), appender);
        }
        return concurrentHashMap;
    }
}

