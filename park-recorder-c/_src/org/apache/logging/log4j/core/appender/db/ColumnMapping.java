/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.db;

import java.util.Date;
import java.util.Locale;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.spi.ThreadContextMap;
import org.apache.logging.log4j.spi.ThreadContextStack;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.ReadOnlyStringMap;

@Plugin(name="ColumnMapping", category="Core", printObject=true)
public class ColumnMapping {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final StringLayout layout;
    private final String literalValue;
    private final String name;
    private final String nameKey;
    private final String parameter;
    private final String source;
    private final Class<?> type;

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    public static String toKey(String string) {
        return string.toUpperCase(Locale.ROOT);
    }

    private ColumnMapping(String string, String string2, StringLayout stringLayout, String string3, String string4, Class<?> clazz) {
        this.name = string;
        this.nameKey = ColumnMapping.toKey(string);
        this.source = string2;
        this.layout = stringLayout;
        this.literalValue = string3;
        this.parameter = string4;
        this.type = clazz;
    }

    public StringLayout getLayout() {
        return this.layout;
    }

    public String getLiteralValue() {
        return this.literalValue;
    }

    public String getName() {
        return this.name;
    }

    public String getNameKey() {
        return this.nameKey;
    }

    public String getParameter() {
        return this.parameter;
    }

    public String getSource() {
        return this.source;
    }

    public Class<?> getType() {
        return this.type;
    }

    public String toString() {
        return "ColumnMapping [name=" + this.name + ", source=" + this.source + ", literalValue=" + this.literalValue + ", parameter=" + this.parameter + ", type=" + this.type + ", layout=" + this.layout + "]";
    }

    public static class Builder
    implements org.apache.logging.log4j.core.util.Builder<ColumnMapping> {
        @PluginConfiguration
        private Configuration configuration;
        @PluginElement(value="Layout")
        private StringLayout layout;
        @PluginBuilderAttribute
        private String literal;
        @PluginBuilderAttribute
        @Required(message="No column name provided")
        private String name;
        @PluginBuilderAttribute
        private String parameter;
        @PluginBuilderAttribute
        private String pattern;
        @PluginBuilderAttribute
        private String source;
        @PluginBuilderAttribute
        @Required(message="No conversion type provided")
        private Class<?> type = String.class;

        @Override
        public ColumnMapping build() {
            if (this.pattern != null) {
                this.layout = PatternLayout.newBuilder().withPattern(this.pattern).withConfiguration(this.configuration).withAlwaysWriteExceptions(false).build();
            }
            if (!(this.layout == null || this.literal == null || Date.class.isAssignableFrom(this.type) || ReadOnlyStringMap.class.isAssignableFrom(this.type) || ThreadContextMap.class.isAssignableFrom(this.type) || ThreadContextStack.class.isAssignableFrom(this.type))) {
                LOGGER.error("No 'layout' or 'literal' value specified and type ({}) is not compatible with ThreadContextMap, ThreadContextStack, or java.util.Date for the mapping", (Object)this.type, (Object)this);
                return null;
            }
            if (this.literal != null && this.parameter != null) {
                LOGGER.error("Only one of 'literal' or 'parameter' can be set on the column mapping {}", (Object)this);
                return null;
            }
            return new ColumnMapping(this.name, this.source, this.layout, this.literal, this.parameter, this.type);
        }

        public Builder setConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder setLayout(StringLayout stringLayout) {
            this.layout = stringLayout;
            return this;
        }

        public Builder setLiteral(String string) {
            this.literal = string;
            return this;
        }

        public Builder setName(String string) {
            this.name = string;
            return this;
        }

        public Builder setParameter(String string) {
            this.parameter = string;
            return this;
        }

        public Builder setPattern(String string) {
            this.pattern = string;
            return this;
        }

        public Builder setSource(String string) {
            this.source = string;
            return this;
        }

        public Builder setType(Class<?> clazz) {
            this.type = clazz;
            return this;
        }

        public String toString() {
            return "Builder [name=" + this.name + ", source=" + this.source + ", literal=" + this.literal + ", parameter=" + this.parameter + ", pattern=" + this.pattern + ", type=" + this.type + ", layout=" + this.layout + "]";
        }
    }
}

