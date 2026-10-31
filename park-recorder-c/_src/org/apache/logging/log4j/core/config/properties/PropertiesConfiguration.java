/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.properties;

import java.io.IOException;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Reconfigurable;
import org.apache.logging.log4j.core.config.builder.api.Component;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.properties.PropertiesConfigurationFactory;

public class PropertiesConfiguration
extends BuiltConfiguration
implements Reconfigurable {
    public PropertiesConfiguration(LoggerContext loggerContext, ConfigurationSource configurationSource, Component component) {
        super(loggerContext, configurationSource, component);
    }

    @Override
    public Configuration reconfigure() {
        try {
            Object object = this.getConfigurationSource().resetInputStream();
            if (object == null) {
                return null;
            }
            object = new PropertiesConfigurationFactory().getConfiguration(this.getLoggerContext(), (ConfigurationSource)object);
            if (object == null || ((AbstractLifeCycle)object).getState() != LifeCycle.State.INITIALIZING) {
                return null;
            }
            return object;
        }
        catch (IOException iOException) {
            LOGGER.error("Cannot locate file {}: {}", (Object)this.getConfigurationSource(), (Object)iOException);
            return null;
        }
    }
}

