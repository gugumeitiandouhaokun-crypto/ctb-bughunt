/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.db.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.db.jdbc.AbstractConnectionSource;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.status.StatusLogger;

public class AbstractDriverManagerConnectionSource
extends AbstractConnectionSource {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final String actualConnectionString;
    private final String connectionString;
    private final String driverClassName;
    private final char[] password;
    private final Property[] properties;
    private final char[] userName;

    public static Logger getLogger() {
        return LOGGER;
    }

    public AbstractDriverManagerConnectionSource(String string, String string2, String string3, char[] cArray, char[] cArray2, Property[] propertyArray) {
        this.driverClassName = string;
        this.connectionString = string2;
        this.actualConnectionString = string3;
        this.userName = cArray;
        this.password = cArray2;
        this.properties = propertyArray;
    }

    public String getActualConnectionString() {
        return this.actualConnectionString;
    }

    @Override
    public Connection getConnection() {
        Connection connection;
        this.loadDriver();
        String string = this.getActualConnectionString();
        LOGGER.debug("{} getting connection for '{}'", (Object)this.getClass().getSimpleName(), (Object)string);
        if (this.properties != null && this.properties.length > 0) {
            if (this.userName != null || this.password != null) {
                throw new SQLException("Either set the userName and password, or set the Properties, but not both.");
            }
            AbstractDriverManagerConnectionSource abstractDriverManagerConnectionSource = this;
            connection = DriverManager.getConnection(string, abstractDriverManagerConnectionSource.toProperties(abstractDriverManagerConnectionSource.properties));
        } else {
            AbstractDriverManagerConnectionSource abstractDriverManagerConnectionSource = this;
            AbstractDriverManagerConnectionSource abstractDriverManagerConnectionSource2 = this;
            connection = DriverManager.getConnection(string, abstractDriverManagerConnectionSource.toString(abstractDriverManagerConnectionSource.userName), abstractDriverManagerConnectionSource2.toString(abstractDriverManagerConnectionSource2.password));
        }
        Connection connection2 = connection;
        LOGGER.debug("{} acquired connection for '{}': {} ({}@{})", (Object)this.getClass().getSimpleName(), (Object)string, (Object)connection2, (Object)connection2.getClass().getName(), (Object)Integer.toHexString(connection.hashCode()));
        return connection;
    }

    public String getConnectionString() {
        return this.connectionString;
    }

    public String getDriverClassName() {
        return this.driverClassName;
    }

    public char[] getPassword() {
        return this.password;
    }

    public Property[] getProperties() {
        return this.properties;
    }

    public char[] getUserName() {
        return this.userName;
    }

    protected void loadDriver() {
        AbstractDriverManagerConnectionSource abstractDriverManagerConnectionSource = this;
        abstractDriverManagerConnectionSource.loadDriver(abstractDriverManagerConnectionSource.driverClassName);
    }

    protected void loadDriver(String string) {
        if (string != null) {
            LOGGER.debug("Loading driver class {}", (Object)string);
            try {
                Class.forName(string);
                return;
            }
            catch (Exception exception) {
                throw new SQLException(String.format("The %s could not load the JDBC driver %s: %s", this.getClass().getSimpleName(), string, exception.toString()), exception);
            }
        }
    }

    protected Properties toProperties(Property[] propertyArray) {
        Properties properties = new Properties();
        for (Property property : propertyArray) {
            properties.setProperty(property.getName(), property.getValue());
        }
        return properties;
    }

    @Override
    public String toString() {
        return this.connectionString;
    }

    protected String toString(char[] cArray) {
        if (cArray == null) {
            return null;
        }
        return String.valueOf(cArray);
    }

    public static class Builder<B extends Builder<B>> {
        @PluginBuilderAttribute
        @Required
        protected String connectionString;
        @PluginBuilderAttribute
        protected String driverClassName;
        @PluginBuilderAttribute
        protected char[] password;
        @PluginElement(value="Properties")
        protected Property[] properties;
        @PluginBuilderAttribute
        protected char[] userName;

        protected B asBuilder() {
            return (B)this;
        }

        public String getConnectionString() {
            return this.connectionString;
        }

        public String getDriverClassName() {
            return this.driverClassName;
        }

        public char[] getPassword() {
            return this.password;
        }

        public Property[] getProperties() {
            return this.properties;
        }

        public char[] getUserName() {
            return this.userName;
        }

        public B setConnectionString(String string) {
            this.connectionString = string;
            return this.asBuilder();
        }

        public B setDriverClassName(String string) {
            this.driverClassName = string;
            return this.asBuilder();
        }

        public B setPassword(char[] cArray) {
            this.password = cArray;
            return this.asBuilder();
        }

        public B setProperties(Property[] propertyArray) {
            this.properties = propertyArray;
            return this.asBuilder();
        }

        public B setUserName(char[] cArray) {
            this.userName = cArray;
            return this.asBuilder();
        }
    }
}

