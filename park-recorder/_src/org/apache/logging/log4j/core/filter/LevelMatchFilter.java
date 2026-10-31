/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.filter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name="LevelMatchFilter", category="Core", elementType="filter", printObject=true)
@PerformanceSensitive(value={"allocation"})
public final class LevelMatchFilter
extends AbstractFilter {
    public static final String ATTR_MATCH = "match";
    private final Level level;

    private LevelMatchFilter(Level level, Filter.Result result, Filter.Result result2) {
        super(result, result2);
        this.level = level;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object ... objectArray) {
        return this.filter(level);
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, Object object, Throwable throwable) {
        return this.filter(level);
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
        return this.filter(level);
    }

    @Override
    public final Filter.Result filter(LogEvent logEvent) {
        return this.filter(logEvent.getLevel());
    }

    private Filter.Result filter(Level level) {
        if (level == this.level) {
            return this.onMatch;
        }
        return this.onMismatch;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object) {
        return this.filter(level);
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2) {
        return this.filter(level);
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3) {
        return this.filter(level);
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        return this.filter(level);
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        return this.filter(level);
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        return this.filter(level);
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        return this.filter(level);
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        return this.filter(level);
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        return this.filter(level);
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        return this.filter(level);
    }

    @Override
    public final String toString() {
        return this.level.toString();
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder
    extends AbstractFilter.AbstractFilterBuilder<Builder>
    implements org.apache.logging.log4j.core.util.Builder<LevelMatchFilter> {
        @PluginBuilderAttribute
        private Level level = Level.ERROR;

        public Builder setLevel(Level level) {
            this.level = level;
            return this;
        }

        @Override
        public LevelMatchFilter build() {
            return new LevelMatchFilter(this.level, this.getOnMatch(), this.getOnMismatch());
        }
    }
}

