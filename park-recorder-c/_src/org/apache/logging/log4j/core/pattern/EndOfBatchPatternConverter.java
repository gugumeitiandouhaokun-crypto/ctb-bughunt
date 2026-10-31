/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name="EndOfBatchPatternConverter", category="Converter")
@ConverterKeys(value={"endOfBatch"})
@PerformanceSensitive(value={"allocation"})
public final class EndOfBatchPatternConverter
extends LogEventPatternConverter {
    private static final EndOfBatchPatternConverter INSTANCE = new EndOfBatchPatternConverter();

    private EndOfBatchPatternConverter() {
        super("LoggerFqcn", "loggerFqcn");
    }

    public static EndOfBatchPatternConverter newInstance(String[] stringArray) {
        return INSTANCE;
    }

    @Override
    public final void format(LogEvent logEvent, StringBuilder stringBuilder) {
        stringBuilder.append(logEvent.isEndOfBatch());
    }
}

