/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name="NdcPatternConverter", category="Converter")
@ConverterKeys(value={"x", "NDC"})
public final class NdcPatternConverter
extends LogEventPatternConverter {
    private static final NdcPatternConverter INSTANCE = new NdcPatternConverter();

    private NdcPatternConverter() {
        super("NDC", "ndc");
    }

    public static NdcPatternConverter newInstance(String[] stringArray) {
        return INSTANCE;
    }

    @Override
    @PerformanceSensitive(value={"allocation"})
    public final void format(LogEvent logEvent, StringBuilder stringBuilder) {
        stringBuilder.append(logEvent.getContextStack());
    }
}

