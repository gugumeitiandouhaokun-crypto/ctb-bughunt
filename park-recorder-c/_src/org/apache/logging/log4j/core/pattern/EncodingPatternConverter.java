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
import org.apache.logging.log4j.util.EnglishEnums;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.StringBuilders;

@Plugin(name="encode", category="Converter")
@ConverterKeys(value={"enc", "encode"})
@PerformanceSensitive(value={"allocation"})
public final class EncodingPatternConverter
extends LogEventPatternConverter {
    private final List<PatternFormatter> formatters;
    private final EscapeFormat escapeFormat;

    private EncodingPatternConverter(List<PatternFormatter> list, EscapeFormat escapeFormat) {
        super("encode", "encode");
        this.formatters = list;
        this.escapeFormat = escapeFormat;
    }

    public static EncodingPatternConverter newInstance(Configuration object, String[] stringArray) {
        if (stringArray.length > 2 || stringArray.length == 0) {
            LOGGER.error("Incorrect number of options on escape. Expected 1 or 2, but received {}", (Object)stringArray.length);
            return null;
        }
        if (stringArray[0] == null) {
            LOGGER.error("No pattern supplied on escape");
            return null;
        }
        EscapeFormat escapeFormat = stringArray.length < 2 ? EscapeFormat.HTML : EnglishEnums.valueOf(EscapeFormat.class, stringArray[1], EscapeFormat.HTML);
        object = PatternLayout.createPatternParser((Configuration)object).parse(stringArray[0]);
        return new EncodingPatternConverter((List<PatternFormatter>)object, escapeFormat);
    }

    @Override
    public final void format(LogEvent logEvent, StringBuilder stringBuilder) {
        int n = stringBuilder.length();
        for (int i2 = 0; i2 < this.formatters.size(); ++i2) {
            this.formatters.get(i2).format(logEvent, stringBuilder);
        }
        this.escapeFormat.escape(stringBuilder, n);
    }

    static enum EscapeFormat {
        HTML{

            @Override
            final void escape(StringBuilder stringBuilder, int n) {
                block10: for (int i2 = stringBuilder.length() - 1; i2 >= n; --i2) {
                    switch (stringBuilder.charAt(i2)) {
                        case '\r': {
                            stringBuilder.setCharAt(i2, '\\');
                            stringBuilder.insert(i2 + 1, 'r');
                            continue block10;
                        }
                        case '\n': {
                            stringBuilder.setCharAt(i2, '\\');
                            stringBuilder.insert(i2 + 1, 'n');
                            continue block10;
                        }
                        case '&': {
                            stringBuilder.setCharAt(i2, '&');
                            stringBuilder.insert(i2 + 1, "amp;");
                            continue block10;
                        }
                        case '<': {
                            stringBuilder.setCharAt(i2, '&');
                            stringBuilder.insert(i2 + 1, "lt;");
                            continue block10;
                        }
                        case '>': {
                            stringBuilder.setCharAt(i2, '&');
                            stringBuilder.insert(i2 + 1, "gt;");
                            continue block10;
                        }
                        case '\"': {
                            stringBuilder.setCharAt(i2, '&');
                            stringBuilder.insert(i2 + 1, "quot;");
                            continue block10;
                        }
                        case '\'': {
                            stringBuilder.setCharAt(i2, '&');
                            stringBuilder.insert(i2 + 1, "apos;");
                            continue block10;
                        }
                        case '/': {
                            stringBuilder.setCharAt(i2, '&');
                            stringBuilder.insert(i2 + 1, "#x2F;");
                        }
                    }
                }
            }
        }
        ,
        JSON{

            @Override
            final void escape(StringBuilder stringBuilder, int n) {
                StringBuilders.escapeJson(stringBuilder, n);
            }
        }
        ,
        CRLF{

            @Override
            final void escape(StringBuilder stringBuilder, int n) {
                block4: for (int i2 = stringBuilder.length() - 1; i2 >= n; --i2) {
                    switch (stringBuilder.charAt(i2)) {
                        case '\r': {
                            stringBuilder.setCharAt(i2, '\\');
                            stringBuilder.insert(i2 + 1, 'r');
                            continue block4;
                        }
                        case '\n': {
                            stringBuilder.setCharAt(i2, '\\');
                            stringBuilder.insert(i2 + 1, 'n');
                        }
                    }
                }
            }
        }
        ,
        XML{

            @Override
            final void escape(StringBuilder stringBuilder, int n) {
                StringBuilders.escapeXml(stringBuilder, n);
            }
        };


        abstract void escape(StringBuilder var1, int var2);
    }
}

