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

@Plugin(name="ExtendedThrowablePatternConverter", category="Converter")
@ConverterKeys(value={"xEx", "xThrowable", "xException"})
public final class ExtendedThrowablePatternConverter
extends ThrowablePatternConverter {
    private ExtendedThrowablePatternConverter(Configuration configuration, String[] stringArray) {
        super("ExtendedThrowable", "throwable", stringArray, configuration);
    }

    public static ExtendedThrowablePatternConverter newInstance(Configuration configuration, String[] stringArray) {
        return new ExtendedThrowablePatternConverter(configuration, stringArray);
    }

    @Override
    public final void format(LogEvent logEvent, StringBuilder stringBuilder) {
        ThrowableProxy throwableProxy = logEvent.getThrownProxy();
        if ((logEvent.getThrown() != null || throwableProxy != null) && this.options.anyLines()) {
            if (throwableProxy == null) {
                super.format(logEvent, stringBuilder);
                return;
            }
            int n = stringBuilder.length();
            if (n > 0 && !Character.isWhitespace(stringBuilder.charAt(n - 1))) {
                stringBuilder.append(' ');
            }
            throwableProxy.formatExtendedStackTraceTo(stringBuilder, this.options.getIgnorePackages(), this.options.getTextRenderer(), this.getSuffix(logEvent), this.options.getSeparator());
        }
    }
}

