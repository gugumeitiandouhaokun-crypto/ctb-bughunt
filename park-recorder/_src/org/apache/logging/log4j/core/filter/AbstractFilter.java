/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.filter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.message.Message;

public abstract class AbstractFilter
extends AbstractLifeCycle
implements Filter {
    protected final Filter.Result onMatch;
    protected final Filter.Result onMismatch;

    protected AbstractFilter() {
        this(null, null);
    }

    protected AbstractFilter(Filter.Result result, Filter.Result result2) {
        this.onMatch = result == null ? Filter.Result.NEUTRAL : result;
        this.onMismatch = result2 == null ? Filter.Result.DENY : result2;
    }

    @Override
    public boolean equalsImpl(Object object) {
        if (this == object) {
            return true;
        }
        if (!super.equalsImpl(object)) {
            return false;
        }
        if (this.getClass() != object.getClass()) {
            return false;
        }
        object = (AbstractFilter)object;
        if (this.onMatch != ((AbstractFilter)object).onMatch) {
            return false;
        }
        return this.onMismatch == ((AbstractFilter)object).onMismatch;
    }

    @Override
    public Filter.Result filter(LogEvent logEvent) {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, Object object, Throwable throwable) {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object ... objectArray) {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object) {
        return this.filter(logger, level, marker, string, new Object[]{object});
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2) {
        return this.filter(logger, level, marker, string, new Object[]{object, object2});
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3) {
        return this.filter(logger, level, marker, string, new Object[]{object, object2, object3});
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        return this.filter(logger, level, marker, string, new Object[]{object, object2, object3, object4});
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        return this.filter(logger, level, marker, string, new Object[]{object, object2, object3, object4, object5});
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        return this.filter(logger, level, marker, string, new Object[]{object, object2, object3, object4, object5, object6});
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        return this.filter(logger, level, marker, string, new Object[]{object, object2, object3, object4, object5, object6, object7});
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        return this.filter(logger, level, marker, string, new Object[]{object, object2, object3, object4, object5, object6, object7, object8});
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        return this.filter(logger, level, marker, string, new Object[]{object, object2, object3, object4, object5, object6, object7, object8, object9});
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        return this.filter(logger, level, marker, string, new Object[]{object, object2, object3, object4, object5, object6, object7, object8, object9, object10});
    }

    @Override
    public final Filter.Result getOnMatch() {
        return this.onMatch;
    }

    @Override
    public final Filter.Result getOnMismatch() {
        return this.onMismatch;
    }

    @Override
    public int hashCodeImpl() {
        int n = super.hashCodeImpl();
        n = 31 * n + (this.onMatch == null ? 0 : this.onMatch.hashCode());
        return 31 * n + (this.onMismatch == null ? 0 : this.onMismatch.hashCode());
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }

    public static abstract class AbstractFilterBuilder<B extends AbstractFilterBuilder<B>> {
        public static final String ATTR_ON_MISMATCH = "onMismatch";
        public static final String ATTR_ON_MATCH = "onMatch";
        @PluginBuilderAttribute(value="onMatch")
        private Filter.Result onMatch = Filter.Result.NEUTRAL;
        @PluginBuilderAttribute(value="onMismatch")
        private Filter.Result onMismatch = Filter.Result.DENY;

        public Filter.Result getOnMatch() {
            return this.onMatch;
        }

        public Filter.Result getOnMismatch() {
            return this.onMismatch;
        }

        public B setOnMatch(Filter.Result result) {
            this.onMatch = result;
            return this.asBuilder();
        }

        public B setOnMismatch(Filter.Result result) {
            this.onMismatch = result;
            return this.asBuilder();
        }

        public B asBuilder() {
            return (B)this;
        }
    }
}

