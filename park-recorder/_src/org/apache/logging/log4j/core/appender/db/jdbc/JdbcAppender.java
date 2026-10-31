/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.db.jdbc;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.db.AbstractDatabaseAppender;
import org.apache.logging.log4j.core.appender.db.ColumnMapping;
import org.apache.logging.log4j.core.appender.db.jdbc.ColumnConfig;
import org.apache.logging.log4j.core.appender.db.jdbc.ConnectionSource;
import org.apache.logging.log4j.core.appender.db.jdbc.JdbcDatabaseManager;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.util.Assert;
import org.apache.logging.log4j.core.util.Booleans;

@Plugin(name="JDBC", category="Core", elementType="appender", printObject=true)
public final class JdbcAppender
extends AbstractDatabaseAppender<JdbcDatabaseManager> {
    private final String description = this.getName() + "{ manager=" + this.getManager() + " }";

    @Deprecated
    public static <B extends Builder<B>> JdbcAppender createAppender(String string, String string2, Filter filter, ConnectionSource connectionSource, String string3, String string4, ColumnConfig[] columnConfigArray) {
        Assert.requireNonEmpty(string, "Name cannot be empty");
        Objects.requireNonNull(connectionSource, "ConnectionSource cannot be null");
        Assert.requireNonEmpty(string4, "Table name cannot be empty");
        Assert.requireNonEmpty(columnConfigArray, "ColumnConfigs cannot be empty");
        int n = AbstractAppender.parseInt(string3, 0);
        boolean bl = Booleans.parseBoolean(string2, true);
        return ((Builder)((Builder)((Builder)((AbstractAppender.Builder)((Builder)((Builder)((Builder)((Builder)JdbcAppender.newBuilder()).setBufferSize(n)).setColumnConfigs(columnConfigArray)).setConnectionSource(connectionSource)).setTableName(string4)).setName(string)).setIgnoreExceptions(bl)).setFilter(filter)).build();
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    private JdbcAppender(String string, Filter filter, Layout<? extends Serializable> layout, boolean bl, Property[] propertyArray, JdbcDatabaseManager jdbcDatabaseManager) {
        super(string, filter, layout, bl, propertyArray, jdbcDatabaseManager);
    }

    @Override
    public final String toString() {
        return this.description;
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractDatabaseAppender.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<JdbcAppender> {
        @PluginElement(value="ConnectionSource")
        @Required(message="No ConnectionSource provided")
        private ConnectionSource connectionSource;
        @PluginBuilderAttribute
        private boolean immediateFail;
        @PluginBuilderAttribute
        private int bufferSize;
        @PluginBuilderAttribute
        @Required(message="No table name provided")
        private String tableName;
        @PluginElement(value="ColumnConfigs")
        private ColumnConfig[] columnConfigs;
        @PluginElement(value="ColumnMappings")
        private ColumnMapping[] columnMappings;
        @PluginBuilderAttribute
        private boolean truncateStrings = true;
        @PluginBuilderAttribute
        private long reconnectIntervalMillis = 5000L;

        @Override
        public JdbcAppender build() {
            if (Assert.isEmpty(this.columnConfigs) && Assert.isEmpty(this.columnMappings)) {
                LOGGER.error("Cannot create JdbcAppender without any columns.");
                return null;
            }
            JdbcDatabaseManager jdbcDatabaseManager = JdbcDatabaseManager.getManager("JdbcManager{name=" + this.getName() + ", bufferSize=" + this.bufferSize + ", tableName=" + this.tableName + ", columnConfigs=" + Arrays.toString(this.columnConfigs) + ", columnMappings=" + Arrays.toString(this.columnMappings) + '}', this.bufferSize, this.getLayout(), this.connectionSource, this.tableName, this.columnConfigs, this.columnMappings, this.immediateFail, this.reconnectIntervalMillis, this.truncateStrings);
            if (jdbcDatabaseManager == null) {
                return null;
            }
            return new JdbcAppender(this.getName(), this.getFilter(), this.getLayout(), this.isIgnoreExceptions(), this.getPropertyArray(), jdbcDatabaseManager);
        }

        public long getReconnectIntervalMillis() {
            return this.reconnectIntervalMillis;
        }

        public boolean isImmediateFail() {
            return this.immediateFail;
        }

        public B setBufferSize(int n) {
            this.bufferSize = n;
            return (B)((Builder)this.asBuilder());
        }

        public B setColumnConfigs(ColumnConfig ... columnConfigArray) {
            this.columnConfigs = columnConfigArray;
            return (B)((Builder)this.asBuilder());
        }

        public B setColumnMappings(ColumnMapping ... columnMappingArray) {
            this.columnMappings = columnMappingArray;
            return (B)((Builder)this.asBuilder());
        }

        public B setConnectionSource(ConnectionSource connectionSource) {
            this.connectionSource = connectionSource;
            return (B)((Builder)this.asBuilder());
        }

        public void setImmediateFail(boolean bl) {
            this.immediateFail = bl;
        }

        public void setReconnectIntervalMillis(long l) {
            this.reconnectIntervalMillis = l;
        }

        public B setTableName(String string) {
            this.tableName = string;
            return (B)((Builder)this.asBuilder());
        }

        public B setTruncateStrings(boolean bl) {
            this.truncateStrings = bl;
            return (B)((Builder)this.asBuilder());
        }
    }
}

