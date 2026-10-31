/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.LocationAware;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

@Plugin(name="LineLocationPatternConverter", category="Converter")
@ConverterKeys(value={"L", "line"})
public final class LineLocationPatternConverter
extends LogEventPatternConverter
implements LocationAware {
    private static final LineLocationPatternConverter INSTANCE = new LineLocationPatternConverter();

    private LineLocationPatternConverter() {
        super("Line", "line");
    }

    public static LineLocationPatternConverter newInstance(String[] stringArray) {
        return INSTANCE;
    }

    @Override
    public final void format(LogEvent serializable, StringBuilder stringBuilder) {
        if ((serializable = serializable.getSource()) != null) {
            stringBuilder.append(((StackTraceElement)serializable).getLineNumber());
        }
    }

    @Override
    public final boolean requiresLocation() {
        return true;
    }
}

