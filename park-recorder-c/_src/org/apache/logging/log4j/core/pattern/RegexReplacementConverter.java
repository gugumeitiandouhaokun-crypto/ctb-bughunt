/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;

@Plugin(name="replace", category="Converter")
@ConverterKeys(value={"replace"})
public final class RegexReplacementConverter
extends LogEventPatternConverter {
    private final Pattern pattern;
    private final String substitution;
    private final List<PatternFormatter> formatters;

    private RegexReplacementConverter(List<PatternFormatter> list, Pattern pattern, String string) {
        super("replace", "replace");
        this.pattern = pattern;
        this.substitution = string;
        this.formatters = list;
    }

    public static RegexReplacementConverter newInstance(Configuration object, String[] stringArray) {
        if (stringArray.length != 3) {
            LOGGER.error("Incorrect number of options on replace. Expected 3 received " + stringArray.length);
            return null;
        }
        if (stringArray[0] == null) {
            LOGGER.error("No pattern supplied on replace");
            return null;
        }
        if (stringArray[1] == null) {
            LOGGER.error("No regular expression supplied on replace");
            return null;
        }
        if (stringArray[2] == null) {
            LOGGER.error("No substitution supplied on replace");
            return null;
        }
        Pattern pattern = Pattern.compile(stringArray[1]);
        object = PatternLayout.createPatternParser((Configuration)object).parse(stringArray[0]);
        return new RegexReplacementConverter((List<PatternFormatter>)object, pattern, stringArray[2]);
    }

    @Override
    public final void format(LogEvent logEvent, StringBuilder stringBuilder) {
        StringBuilder stringBuilder2 = new StringBuilder();
        Iterator<PatternFormatter> iterator = this.formatters.iterator();
        while (iterator.hasNext()) {
            iterator.next().format(logEvent, stringBuilder2);
        }
        stringBuilder.append(this.pattern.matcher(stringBuilder2.toString()).replaceAll(this.substitution));
    }
}

