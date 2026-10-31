/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.layout;

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.impl.LocationAware;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.layout.PatternMatch;
import org.apache.logging.log4j.core.layout.PatternSelector;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="LevelPatternSelector", category="Core", elementType="patternSelector", printObject=true)
public class LevelPatternSelector
implements LocationAware,
PatternSelector {
    private final Map<String, PatternFormatter[]> formatterMap = new HashMap<String, PatternFormatter[]>();
    private final Map<String, String> patternMap = new HashMap<String, String>();
    private final PatternFormatter[] defaultFormatters;
    private final String defaultPattern;
    private static Logger LOGGER = StatusLogger.getLogger();
    private final boolean requiresLocation;

    @Deprecated
    public LevelPatternSelector(PatternMatch[] patternMatchArray, String string, boolean bl, boolean bl2, Configuration configuration) {
        this(patternMatchArray, string, bl, false, bl2, configuration);
    }

    private LevelPatternSelector(PatternMatch[] object, String string, boolean bl, boolean bl2, boolean bl3, Configuration object2) {
        boolean bl4 = false;
        object2 = PatternLayout.createPatternParser((Configuration)object2);
        for (PatternMatch patternMatch : object) {
            try {
                PatternFormatter[] patternFormatterArray = ((PatternParser)object2).parse(patternMatch.getPattern(), bl, bl2, bl3).toArray(new PatternFormatter[0]);
                this.formatterMap.put(patternMatch.getKey(), patternFormatterArray);
                for (int j = 0; !bl4 && j < patternFormatterArray.length; ++j) {
                    bl4 = patternFormatterArray[j].requiresLocation();
                }
                this.patternMap.put(patternMatch.getKey(), patternMatch.getPattern());
            }
            catch (RuntimeException runtimeException) {
                throw new IllegalArgumentException("Cannot parse pattern '" + patternMatch.getPattern() + "'", runtimeException);
            }
        }
        try {
            object = ((PatternParser)object2).parse(string, bl, bl2, bl3);
            this.defaultFormatters = object.toArray(new PatternFormatter[0]);
            this.defaultPattern = string;
            for (int j = 0; !bl4 && j < this.defaultFormatters.length; ++j) {
                bl4 = this.defaultFormatters[j].requiresLocation();
            }
        }
        catch (RuntimeException runtimeException) {
            throw new IllegalArgumentException("Cannot parse pattern '" + string + "'", runtimeException);
        }
        this.requiresLocation = bl4;
    }

    @Override
    public boolean requiresLocation() {
        return this.requiresLocation;
    }

    @Override
    public PatternFormatter[] getFormatters(LogEvent serializable) {
        if ((serializable = serializable.getLevel()) == null) {
            return this.defaultFormatters;
        }
        for (String string : this.formatterMap.keySet()) {
            if (!((Level)serializable).name().equalsIgnoreCase(string)) continue;
            return this.formatterMap.get(string);
        }
        return this.defaultFormatters;
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    @Deprecated
    public static LevelPatternSelector createSelector(PatternMatch[] patternMatchArray, String string, boolean bl, boolean bl2, Configuration configuration) {
        Builder builder = LevelPatternSelector.newBuilder();
        builder.setProperties(patternMatchArray);
        builder.setDefaultPattern(string);
        builder.setAlwaysWriteExceptions(bl);
        builder.setNoConsoleNoAnsi(bl2);
        builder.setConfiguration(configuration);
        return builder.build();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean bl = true;
        for (Map.Entry<String, String> entry : this.patternMap.entrySet()) {
            if (!bl) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("key=\"").append(entry.getKey()).append("\", pattern=\"").append(entry.getValue()).append("\"");
            bl = false;
        }
        if (!bl) {
            stringBuilder.append(", ");
        }
        stringBuilder.append("default=\"").append(this.defaultPattern).append("\"");
        return stringBuilder.toString();
    }

    public static class Builder
    implements org.apache.logging.log4j.core.util.Builder<LevelPatternSelector> {
        @PluginElement(value="PatternMatch")
        private PatternMatch[] properties;
        @PluginBuilderAttribute(value="defaultPattern")
        private String defaultPattern;
        @PluginBuilderAttribute(value="alwaysWriteExceptions")
        private boolean alwaysWriteExceptions = true;
        @PluginBuilderAttribute(value="disableAnsi")
        private boolean disableAnsi;
        @PluginBuilderAttribute(value="noConsoleNoAnsi")
        private boolean noConsoleNoAnsi;
        @PluginConfiguration
        private Configuration configuration;

        @Override
        public LevelPatternSelector build() {
            if (this.defaultPattern == null) {
                this.defaultPattern = "%m%n";
            }
            if (this.properties == null || this.properties.length == 0) {
                LOGGER.warn("No marker patterns were provided with PatternMatch");
                return null;
            }
            return new LevelPatternSelector(this.properties, this.defaultPattern, this.alwaysWriteExceptions, this.disableAnsi, this.noConsoleNoAnsi, this.configuration);
        }

        public Builder setProperties(PatternMatch[] patternMatchArray) {
            this.properties = patternMatchArray;
            return this;
        }

        public Builder setDefaultPattern(String string) {
            this.defaultPattern = string;
            return this;
        }

        public Builder setAlwaysWriteExceptions(boolean bl) {
            this.alwaysWriteExceptions = bl;
            return this;
        }

        public Builder setDisableAnsi(boolean bl) {
            this.disableAnsi = bl;
            return this;
        }

        public Builder setNoConsoleNoAnsi(boolean bl) {
            this.noConsoleNoAnsi = bl;
            return this;
        }

        public Builder setConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }
    }
}

