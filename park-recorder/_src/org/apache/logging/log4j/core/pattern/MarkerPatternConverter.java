/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.StringBuilders;

@Plugin(name="MarkerPatternConverter", category="Converter")
@ConverterKeys(value={"marker"})
@PerformanceSensitive(value={"allocation"})
public final class MarkerPatternConverter
extends LogEventPatternConverter {
    private MarkerPatternConverter(String[] stringArray) {
        super("Marker", "marker");
    }

    public static MarkerPatternConverter newInstance(String[] stringArray) {
        return new MarkerPatternConverter(stringArray);
    }

    @Override
    public final void format(LogEvent serializable, StringBuilder stringBuilder) {
        if ((serializable = serializable.getMarker()) != null) {
            StringBuilders.appendValue(stringBuilder, serializable);
        }
    }
}

