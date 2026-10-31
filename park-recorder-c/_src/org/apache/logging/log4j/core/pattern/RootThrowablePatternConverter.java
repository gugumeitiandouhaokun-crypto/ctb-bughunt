/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.ThrowablePatternConverter;
import org.apache.logging.log4j.util.Strings;

@Plugin(name="RootThrowablePatternConverter", category="Converter")
@ConverterKeys(value={"rEx", "rThrowable", "rException"})
public final class RootThrowablePatternConverter
extends ThrowablePatternConverter {
    private RootThrowablePatternConverter(Configuration configuration, String[] stringArray) {
        super("RootThrowable", "throwable", stringArray, configuration);
    }

    public static RootThrowablePatternConverter newInstance(Configuration configuration, String[] stringArray) {
        return new RootThrowablePatternConverter(configuration, stringArray);
    }

    @Override
    public final void format(LogEvent stringArray, StringBuilder stringBuilder) {
        ThrowableProxy throwableProxy = stringArray.getThrownProxy();
        if (stringArray.getThrown() != null && this.options.anyLines()) {
            if (throwableProxy == null) {
                super.format((LogEvent)stringArray, stringBuilder);
                return;
            }
            stringArray = throwableProxy.getCauseStackTraceAsString(this.options.getIgnorePackages(), this.options.getTextRenderer(), this.getSuffix((LogEvent)stringArray), this.options.getSeparator());
            int n = stringBuilder.length();
            if (n > 0 && !Character.isWhitespace(stringBuilder.charAt(n - 1))) {
                stringBuilder.append(' ');
            }
            if (!this.options.allLines() || !Strings.LINE_SEPARATOR.equals(this.options.getSeparator())) {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringArray = stringArray.split(Strings.LINE_SEPARATOR);
                int n2 = this.options.minLines(stringArray.length) - 1;
                for (int i2 = 0; i2 <= n2; ++i2) {
                    stringBuilder2.append(stringArray[i2]);
                    if (i2 >= n2) continue;
                    stringBuilder2.append(this.options.getSeparator());
                }
                stringBuilder.append(stringBuilder2.toString());
                return;
            }
            stringBuilder.append((String)stringArray);
        }
    }
}

