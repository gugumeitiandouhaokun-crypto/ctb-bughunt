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
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name="DenyAllFilter", category="Core", elementType="filter", printObject=true)
@PerformanceSensitive(value={"allocation"})
public final class DenyAllFilter
extends AbstractFilter {
    private DenyAllFilter(Filter.Result result, Filter.Result result2) {
        super(result, result2);
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object ... objectArray) {
        return Filter.Result.DENY;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, Object object, Throwable throwable) {
        return Filter.Result.DENY;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
        return Filter.Result.DENY;
    }

    @Override
    public final Filter.Result filter(LogEvent logEvent) {
        return Filter.Result.DENY;
    }

    private Filter.Result filter(Marker marker) {
        return Filter.Result.DENY;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object) {
        return Filter.Result.DENY;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2) {
        return Filter.Result.DENY;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3) {
        return this.filter(marker);
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        return Filter.Result.DENY;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        return Filter.Result.DENY;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        return Filter.Result.DENY;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        return Filter.Result.DENY;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        return Filter.Result.DENY;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        return Filter.Result.DENY;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        return Filter.Result.DENY;
    }

    @Override
    public final String toString() {
        return "DenyAll";
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder
    extends AbstractFilter.AbstractFilterBuilder<Builder>
    implements org.apache.logging.log4j.core.util.Builder<DenyAllFilter> {
        @Override
        public DenyAllFilter build() {
            return new DenyAllFilter(this.getOnMatch(), this.getOnMismatch());
        }
    }
}

