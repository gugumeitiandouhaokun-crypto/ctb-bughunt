/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.properties.PropertiesConfiguration;
import org.apache.logging.log4j.core.config.properties.PropertiesConfigurationBuilder;

@Plugin(name="PropertiesConfigurationFactory", category="ConfigurationFactory")
@Order(value=8)
public class PropertiesConfigurationFactory
extends ConfigurationFactory {
    @Override
    public String[] getSupportedTypes() {
        return new String[]{".properties"};
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public PropertiesConfiguration getConfiguration(LoggerContext object, ConfigurationSource configurationSource) {
        Properties properties = new Properties();
        try {
            InputStream inputStream = configurationSource.getInputStream();
            Throwable throwable = null;
            try {
                properties.load(inputStream);
                if (inputStream == null) return new PropertiesConfigurationBuilder().setConfigurationSource(configurationSource).setRootProperties(properties).setLoggerContext((LoggerContext)object).build();
            }
            catch (Throwable throwable2) {
                try {
                    object = throwable2;
                    throwable = throwable2;
                    throw object;
                }
                catch (Throwable throwable3) {
                    if (inputStream == null) throw throwable3;
                    if (throwable != null) {
                        try {
                            inputStream.close();
                            throw throwable3;
                        }
                        catch (Throwable throwable4) {
                            throwable.addSuppressed(throwable4);
                        }
                        throw throwable3;
                    } else {
                        inputStream.close();
                    }
                    throw throwable3;
                }
            }
            inputStream.close();
            return new PropertiesConfigurationBuilder().setConfigurationSource(configurationSource).setRootProperties(properties).setLoggerContext((LoggerContext)object).build();
        }
        catch (IOException iOException) {
            throw new ConfigurationException("Unable to load " + configurationSource.toString(), iOException);
        }
    }
}

