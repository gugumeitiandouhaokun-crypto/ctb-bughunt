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

@Plugin(name="StringMatchFilter", category="Core", elementType="filter", printObject=true)
@PerformanceSensitive(value={"allocation"})
public final class StringMatchFilter
extends AbstractFilter {
    public static final String ATTR_MATCH = "match";
    private final String text;

    private StringMatchFilter(String string, Filter.Result result, Filter.Result result2) {
        super(result, result2);
        this.text = string;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object ... objectArray) {
        return this.filter(logger.getMessageFactory().newMessage(string, objectArray).getFormattedMessage());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, Object object, Throwable throwable) {
        return this.filter(logger.getMessageFactory().newMessage(object).getFormattedMessage());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
        return this.filter(message.getFormattedMessage());
    }

    @Override
    public final Filter.Result filter(LogEvent logEvent) {
        return this.filter(logEvent.getMessage().getFormattedMessage());
    }

    private Filter.Result filter(String string) {
        if (string.contains(this.text)) {
            return this.onMatch;
        }
        return this.onMismatch;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object) {
        return this.filter(logger.getMessageFactory().newMessage(string, object).getFormattedMessage());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2) {
        return this.filter(logger.getMessageFactory().newMessage(string, object, object2).getFormattedMessage());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3) {
        return this.filter(logger.getMessageFactory().newMessage(string, object, object2, object3).getFormattedMessage());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        return this.filter(logger.getMessageFactory().newMessage(string, object, object2, object3, object4).getFormattedMessage());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        return this.filter(logger.getMessageFactory().newMessage(string, object, object2, object3, object4, object5).getFormattedMessage());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        return this.filter(logger.getMessageFactory().newMessage(string, object, object2, object3, object4, object5, object6).getFormattedMessage());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        return this.filter(logger.getMessageFactory().newMessage(string, object, object2, object3, object4, object5, object6, object7).getFormattedMessage());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        return this.filter(logger.getMessageFactory().newMessage(string, object, object2, object3, object4, object5, object6, object7, object8).getFormattedMessage());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        return this.filter(logger.getMessageFactory().newMessage(string, object, object2, object3, object4, object5, object6, object7, object8, object9).getFormattedMessage());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        return this.filter(logger.getMessageFactory().newMessage(string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10).getFormattedMessage());
    }

    @Override
    public final String toString() {
        return this.text;
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder
    extends AbstractFilter.AbstractFilterBuilder<Builder>
    implements org.apache.logging.log4j.core.util.Builder<StringMatchFilter> {
        @PluginBuilderAttribute
        private String text = "";

        public Builder setMatchString(String string) {
            this.text = string;
            return this;
        }

        @Override
        public StringMatchFilter build() {
            return new StringMatchFilter(this.text, this.getOnMatch(), this.getOnMismatch());
        }
    }
}

