/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.db.jdbc;

import org.apache.logging.log4j.core.appender.db.jdbc.AbstractDriverManagerConnectionSource;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;

@Plugin(name="DriverManager", category="Core", elementType="connectionSource", printObject=true)
public class DriverManagerConnectionSource
extends AbstractDriverManagerConnectionSource {
    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    public DriverManagerConnectionSource(String string, String string2, String string3, char[] cArray, char[] cArray2, Property[] propertyArray) {
        super(string, string2, string3, cArray, cArray2, propertyArray);
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractDriverManagerConnectionSource.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<DriverManagerConnectionSource> {
        @Override
        public DriverManagerConnectionSource build() {
            return new DriverManagerConnectionSource(this.getDriverClassName(), this.getConnectionString(), this.getConnectionString(), this.getUserName(), this.getPassword(), this.getProperties());
        }
    }
}

