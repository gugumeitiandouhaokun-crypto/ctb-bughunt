/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.LocationAware;
import org.apache.logging.log4j.core.pattern.FormattingInfo;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

public class PatternFormatter {
    private final LogEventPatternConverter converter;
    private final FormattingInfo field;
    private final boolean skipFormattingInfo;

    public PatternFormatter(LogEventPatternConverter logEventPatternConverter, FormattingInfo formattingInfo) {
        this.converter = logEventPatternConverter;
        this.field = formattingInfo;
        this.skipFormattingInfo = formattingInfo == FormattingInfo.getDefault();
    }

    public void format(LogEvent logEvent, StringBuilder stringBuilder) {
        if (this.skipFormattingInfo) {
            this.converter.format(logEvent, stringBuilder);
            return;
        }
        this.formatWithInfo(logEvent, stringBuilder);
    }

    private void formatWithInfo(LogEvent logEvent, StringBuilder stringBuilder) {
        int n = stringBuilder.length();
        this.converter.format(logEvent, stringBuilder);
        this.field.format(n, stringBuilder);
    }

    public LogEventPatternConverter getConverter() {
        return this.converter;
    }

    public FormattingInfo getFormattingInfo() {
        return this.field;
    }

    public boolean handlesThrowable() {
        return this.converter.handlesThrowable();
    }

    public boolean requiresLocation() {
        return this.converter instanceof LocationAware && ((LocationAware)((Object)this.converter)).requiresLocation();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.toString());
        stringBuilder.append("[converter=");
        stringBuilder.append(this.converter);
        stringBuilder.append(", field=");
        stringBuilder.append(this.field);
        stringBuilder.append(']');
        return stringBuilder.toString();
    }
}

