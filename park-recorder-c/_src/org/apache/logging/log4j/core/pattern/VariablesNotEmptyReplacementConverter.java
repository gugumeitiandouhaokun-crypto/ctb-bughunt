/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.List;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name="notEmpty", category="Converter")
@ConverterKeys(value={"notEmpty", "varsNotEmpty", "variablesNotEmpty"})
@PerformanceSensitive(value={"allocation"})
public final class VariablesNotEmptyReplacementConverter
extends LogEventPatternConverter {
    private final List<PatternFormatter> formatters;

    private VariablesNotEmptyReplacementConverter(List<PatternFormatter> list) {
        super("notEmpty", "notEmpty");
        this.formatters = list;
    }

    public static VariablesNotEmptyReplacementConverter newInstance(Configuration object, String[] stringArray) {
        if (stringArray.length != 1) {
            LOGGER.error("Incorrect number of options on varsNotEmpty. Expected 1 received " + stringArray.length);
            return null;
        }
        if (stringArray[0] == null) {
            LOGGER.error("No pattern supplied on varsNotEmpty");
            return null;
        }
        object = PatternLayout.createPatternParser((Configuration)object).parse(stringArray[0]);
        return new VariablesNotEmptyReplacementConverter((List<PatternFormatter>)object);
    }

    @Override
    public final void format(LogEvent logEvent, StringBuilder stringBuilder) {
        int n = stringBuilder.length();
        boolean bl = true;
        boolean bl2 = false;
        for (int i2 = 0; i2 < this.formatters.size(); ++i2) {
            PatternFormatter patternFormatter = this.formatters.get(i2);
            int n2 = stringBuilder.length();
            patternFormatter.format(logEvent, stringBuilder);
            if (!patternFormatter.getConverter().isVariable()) continue;
            bl2 = true;
            bl = bl && stringBuilder.length() == n2;
        }
        if (!bl2 || bl) {
            stringBuilder.setLength(n);
        }
    }
}

