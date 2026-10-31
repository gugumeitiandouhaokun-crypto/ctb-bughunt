/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.db.jdbc;

import java.sql.Connection;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.db.jdbc.AbstractConnectionSource;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

@Plugin(name="DataSource", category="Core", elementType="connectionSource", printObject=true)
public final class DataSourceConnectionSource
extends AbstractConnectionSource {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final DataSource dataSource;
    private final String description;

    private DataSourceConnectionSource(String string, DataSource dataSource) {
        this.dataSource = dataSource;
        this.description = "dataSource{ name=" + string + ", value=" + dataSource + " }";
    }

    @Override
    public final Connection getConnection() {
        return this.dataSource.getConnection();
    }

    @Override
    public final String toString() {
        return this.description;
    }

    @PluginFactory
    public static DataSourceConnectionSource createConnectionSource(@PluginAttribute(value="jndiName") String string) {
        if (Strings.isEmpty(string)) {
            LOGGER.error("No JNDI name provided.");
            return null;
        }
        try {
            DataSource dataSource = (DataSource)new InitialContext().lookup(string);
            if (dataSource == null) {
                LOGGER.error("No data source found with JNDI name [" + string + "].");
                return null;
            }
            return new DataSourceConnectionSource(string, dataSource);
        }
        catch (NamingException namingException) {
            LOGGER.error(namingException.getMessage(), (Throwable)namingException);
            return null;
        }
    }
}

