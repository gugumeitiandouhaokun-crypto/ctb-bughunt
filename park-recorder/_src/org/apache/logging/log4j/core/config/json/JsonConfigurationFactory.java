/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.json;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.json.JsonConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.util.Loader;

@Plugin(name="JsonConfigurationFactory", category="ConfigurationFactory")
@Order(value=6)
public class JsonConfigurationFactory
extends ConfigurationFactory {
    private static final String[] SUFFIXES = new String[]{".json", ".jsn"};
    private static final String[] dependencies = new String[]{"com.fasterxml.jackson.databind.ObjectMapper", "com.fasterxml.jackson.databind.JsonNode", "com.fasterxml.jackson.core.JsonParser"};
    private final boolean isActive;

    public JsonConfigurationFactory() {
        String[] stringArray = dependencies;
        int n = dependencies.length;
        for (int j = 0; j < n; ++j) {
            if (Loader.isClassAvailable(stringArray[j])) continue;
            LOGGER.debug("Missing dependencies for Json support, ConfigurationFactory {} is inactive", (Object)this.getClass().getName());
            this.isActive = false;
            return;
        }
        this.isActive = true;
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource configurationSource) {
        if (!this.isActive) {
            return null;
        }
        return new JsonConfiguration(loggerContext, configurationSource);
    }

    @Override
    public String[] getSupportedTypes() {
        return SUFFIXES;
    }
}

