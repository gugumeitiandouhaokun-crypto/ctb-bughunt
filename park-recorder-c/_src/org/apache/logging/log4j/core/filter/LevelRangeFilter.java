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
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name="LevelRangeFilter", category="Core", elementType="filter", printObject=true)
@PerformanceSensitive(value={"allocation"})
public final class LevelRangeFilter
extends AbstractFilter {
    private final Level maxLevel;
    private final Level minLevel;

    @PluginFactory
    public static LevelRangeFilter createFilter(@PluginAttribute(value="minLevel") Level level, @PluginAttribute(value="maxLevel") Level level2, @PluginAttribute(value="onMatch") Filter.Result result, @PluginAttribute(value="onMismatch") Filter.Result result2) {
        level = level == null ? Level.ERROR : level;
        level2 = level2 == null ? Level.ERROR : level2;
        result = result == null ? Filter.Result.NEUTRAL : result;
        result2 = result2 == null ? Filter.Result.DENY : result2;
        return new LevelRangeFilter(level, level2, result, result2);
    }

    private LevelRangeFilter(Level level, Level level2, Filter.Result result, Filter.Result result2) {
        super(result, result2);
        this.minLevel = level;
        this.maxLevel = level2;
    }

    private Filter.Result filter(Level level) {
        if (level.isInRange(this.minLevel, this.maxLevel)) {
            return this.onMatch;
        }
        return this.onMismatch;
    }

    @Override
    public final Filter.Result filter(LogEvent logEvent) {
        return this.filter(logEvent.getLevel());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
        return this.filter(level);
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, Object object, Throwable throwable) {
        return this.filter(level);
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object ... objectArray) {
        return this.filter(level);
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

    public final Level getMinLevel() {
        return this.minLevel;
    }

    @Override
    public final String toString() {
        return this.minLevel.toString();
    }
}

