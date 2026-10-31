/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.nosql;

import java.io.Serializable;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.db.AbstractDatabaseAppender;
import org.apache.logging.log4j.core.appender.nosql.NoSqlDatabaseManager;
import org.apache.logging.log4j.core.appender.nosql.NoSqlProvider;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.util.Booleans;

@Plugin(name="NoSql", category="Core", elementType="appender", printObject=true)
public final class NoSqlAppender
extends AbstractDatabaseAppender<NoSqlDatabaseManager<?>> {
    private final String description = this.getName() + "{ manager=" + this.getManager() + " }";

    @Deprecated
    public static NoSqlAppender createAppender(String string, String string2, Filter filter, String string3, NoSqlProvider<?> noSqlProvider) {
        if (noSqlProvider == null) {
            LOGGER.error("NoSQL provider not specified for appender [{}].", (Object)string);
            return null;
        }
        int n = AbstractAppender.parseInt(string3, 0);
        boolean bl = Booleans.parseBoolean(string2, true);
        NoSqlDatabaseManager<?> noSqlDatabaseManager = NoSqlDatabaseManager.getNoSqlDatabaseManager("noSqlManager{ description=" + string + ", bufferSize=" + n + ", provider=" + noSqlProvider + " }", n, noSqlProvider);
        if (noSqlDatabaseManager == null) {
            return null;
        }
        return new NoSqlAppender(string, filter, null, bl, null, noSqlDatabaseManager);
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    private NoSqlAppender(String string, Filter filter, Layout<? extends Serializable> layout, boolean bl, Property[] propertyArray, NoSqlDatabaseManager<?> noSqlDatabaseManager) {
        super(string, filter, layout, bl, propertyArray, noSqlDatabaseManager);
    }

    @Override
    public final String toString() {
        return this.description;
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractAppender.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<NoSqlAppender> {
        @PluginBuilderAttribute(value="bufferSize")
        private int bufferSize;
        @PluginElement(value="NoSqlProvider")
        private NoSqlProvider<?> provider;

        @Override
        public NoSqlAppender build() {
            String string = this.getName();
            if (this.provider == null) {
                LOGGER.error("NoSQL provider not specified for appender [{}].", (Object)string);
                return null;
            }
            NoSqlDatabaseManager<?> noSqlDatabaseManager = NoSqlDatabaseManager.getNoSqlDatabaseManager("noSqlManager{ description=" + string + ", bufferSize=" + this.bufferSize + ", provider=" + this.provider + " }", this.bufferSize, this.provider);
            if (noSqlDatabaseManager == null) {
                return null;
            }
            return new NoSqlAppender(string, this.getFilter(), this.getLayout(), this.isIgnoreExceptions(), this.getPropertyArray(), noSqlDatabaseManager);
        }

        public B setBufferSize(int n) {
            this.bufferSize = n;
            return (B)((Builder)this.asBuilder());
        }

        public B setProvider(NoSqlProvider<?> noSqlProvider) {
            this.provider = noSqlProvider;
            return (B)((Builder)this.asBuilder());
        }
    }
}

