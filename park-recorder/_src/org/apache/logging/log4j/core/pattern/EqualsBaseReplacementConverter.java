/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.List;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.apache.logging.log4j.util.PerformanceSensitive;

@PerformanceSensitive(value={"allocation"})
public abstract class EqualsBaseReplacementConverter
extends LogEventPatternConverter {
    private final List<PatternFormatter> formatters;
    private final List<PatternFormatter> substitutionFormatters;
    private final String substitution;
    private final String testString;

    protected EqualsBaseReplacementConverter(String string, String string2, List<PatternFormatter> list, String string3, String string4, PatternParser patternParser) {
        super(string, string2);
        this.testString = string3;
        this.substitution = string4;
        this.formatters = list;
        this.substitutionFormatters = string4.contains("%") ? patternParser.parse(string4) : null;
    }

    @Override
    public void format(LogEvent logEvent, StringBuilder stringBuilder) {
        int n = stringBuilder.length();
        for (int j = 0; j < this.formatters.size(); ++j) {
            this.formatters.get(j).format(logEvent, stringBuilder);
        }
        EqualsBaseReplacementConverter equalsBaseReplacementConverter = this;
        if (equalsBaseReplacementConverter.equals(equalsBaseReplacementConverter.testString, stringBuilder, n, stringBuilder.length() - n)) {
            stringBuilder.setLength(n);
            this.parseSubstitution(logEvent, stringBuilder);
        }
    }

    protected abstract boolean equals(String var1, StringBuilder var2, int var3, int var4);

    void parseSubstitution(LogEvent logEvent, StringBuilder stringBuilder) {
        if (this.substitutionFormatters != null) {
            for (int j = 0; j < this.substitutionFormatters.size(); ++j) {
                this.substitutionFormatters.get(j).format(logEvent, stringBuilder);
            }
            return;
        }
        stringBuilder.append(this.substitution);
    }
}

