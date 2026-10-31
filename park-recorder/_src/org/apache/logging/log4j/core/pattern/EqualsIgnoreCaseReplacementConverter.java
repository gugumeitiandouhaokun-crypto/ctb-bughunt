/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.List;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.EqualsBaseReplacementConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.StringBuilders;

@Plugin(name="equalsIgnoreCase", category="Converter")
@ConverterKeys(value={"equalsIgnoreCase"})
@PerformanceSensitive(value={"allocation"})
public final class EqualsIgnoreCaseReplacementConverter
extends EqualsBaseReplacementConverter {
    public static EqualsIgnoreCaseReplacementConverter newInstance(Configuration object, String[] stringArray) {
        if (stringArray.length != 3) {
            LOGGER.error("Incorrect number of options on equalsIgnoreCase. Expected 3 received " + stringArray.length);
            return null;
        }
        if (stringArray[0] == null) {
            LOGGER.error("No pattern supplied on equalsIgnoreCase");
            return null;
        }
        if (stringArray[1] == null) {
            LOGGER.error("No test string supplied on equalsIgnoreCase");
            return null;
        }
        if (stringArray[2] == null) {
            LOGGER.error("No substitution supplied on equalsIgnoreCase");
            return null;
        }
        String string = stringArray[1];
        object = PatternLayout.createPatternParser((Configuration)object);
        List<PatternFormatter> list = ((PatternParser)object).parse(stringArray[0]);
        return new EqualsIgnoreCaseReplacementConverter(list, string, stringArray[2], (PatternParser)object);
    }

    private EqualsIgnoreCaseReplacementConverter(List<PatternFormatter> list, String string, String string2, PatternParser patternParser) {
        super("equalsIgnoreCase", "equalsIgnoreCase", list, string, string2, patternParser);
    }

    @Override
    protected final boolean equals(String string, StringBuilder stringBuilder, int n, int n2) {
        return StringBuilders.equalsIgnoreCase(string, 0, string.length(), stringBuilder, n, n2);
    }
}

