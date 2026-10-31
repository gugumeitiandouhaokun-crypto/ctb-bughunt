/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.AnsiConverter;
import org.apache.logging.log4j.core.pattern.AnsiEscape;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.util.Patterns;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name="style", category="Converter")
@ConverterKeys(value={"style"})
@PerformanceSensitive(value={"allocation"})
public final class StyleConverter
extends LogEventPatternConverter
implements AnsiConverter {
    private final List<PatternFormatter> patternFormatters;
    private final boolean noAnsi;
    private final String style;
    private final String defaultStyle;

    private StyleConverter(List<PatternFormatter> list, String string, boolean bl) {
        super("style", "style");
        this.patternFormatters = list;
        this.style = string;
        this.defaultStyle = AnsiEscape.getDefaultStyle();
        this.noAnsi = bl;
    }

    public static StyleConverter newInstance(Configuration object, String[] stringArray) {
        if (stringArray == null) {
            return null;
        }
        if (stringArray.length < 2) {
            LOGGER.error("Incorrect number of options on style. Expected at least 1, received " + stringArray.length);
            return null;
        }
        if (stringArray[0] == null) {
            LOGGER.error("No pattern supplied for style converter");
            return null;
        }
        if (stringArray[1] == null) {
            LOGGER.error("No style attributes supplied for style converter");
            return null;
        }
        object = PatternLayout.createPatternParser((Configuration)object).parse(stringArray[0]);
        String string = AnsiEscape.createSequence(stringArray[1].split(Patterns.COMMA_SEPARATOR));
        boolean bl = Arrays.toString(stringArray).contains("disableAnsi=true");
        boolean bl2 = Arrays.toString(stringArray).contains("noConsoleNoAnsi=true");
        bl2 = bl || bl2 && System.console() == null;
        return new StyleConverter((List<PatternFormatter>)object, string, bl2);
    }

    @Override
    public final void format(LogEvent logEvent, StringBuilder stringBuilder) {
        int n = 0;
        int n2 = 0;
        if (!this.noAnsi) {
            n = stringBuilder.length();
            stringBuilder.append(this.style);
            n2 = stringBuilder.length();
        }
        int n3 = this.patternFormatters.size();
        for (int i2 = 0; i2 < n3; ++i2) {
            this.patternFormatters.get(i2).format(logEvent, stringBuilder);
        }
        if (!this.noAnsi) {
            if (stringBuilder.length() == n2) {
                stringBuilder.setLength(n);
                return;
            }
            stringBuilder.append(this.defaultStyle);
        }
    }

    @Override
    public final boolean handlesThrowable() {
        Iterator<PatternFormatter> iterator = this.patternFormatters.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().handlesThrowable()) continue;
            return true;
        }
        return false;
    }

    public final String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.toString());
        stringBuilder.append("[style=");
        stringBuilder.append(this.style);
        stringBuilder.append(", defaultStyle=");
        stringBuilder.append(this.defaultStyle);
        stringBuilder.append(", patternFormatters=");
        stringBuilder.append(this.patternFormatters);
        stringBuilder.append(", noAnsi=");
        stringBuilder.append(this.noAnsi);
        stringBuilder.append(']');
        return stringBuilder.toString();
    }
}

