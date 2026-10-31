/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name="MarkerNamePatternConverter", category="Converter")
@ConverterKeys(value={"markerSimpleName"})
@PerformanceSensitive(value={"allocation"})
public final class MarkerSimpleNamePatternConverter
extends LogEventPatternConverter {
    private MarkerSimpleNamePatternConverter(String[] stringArray) {
        super("MarkerSimpleName", "markerSimpleName");
    }

    public static MarkerSimpleNamePatternConverter newInstance(String[] stringArray) {
        return new MarkerSimpleNamePatternConverter(stringArray);
    }

    @Override
    public final void format(LogEvent serializable, StringBuilder stringBuilder) {
        if ((serializable = serializable.getMarker()) != null) {
            stringBuilder.append(serializable.getName());
        }
    }
}

