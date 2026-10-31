/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="failovers", category="Core")
public final class FailoversPlugin {
    private static final Logger LOGGER = StatusLogger.getLogger();

    private FailoversPlugin() {
    }

    @PluginFactory
    public static String[] createFailovers(AppenderRef ... appenderRefArray) {
        if (appenderRefArray == null) {
            LOGGER.error("failovers must contain an appender reference");
            return null;
        }
        String[] stringArray = new String[appenderRefArray.length];
        for (int i2 = 0; i2 < appenderRefArray.length; ++i2) {
            stringArray[i2] = appenderRefArray[i2].getRef();
        }
        return stringArray;
    }
}

