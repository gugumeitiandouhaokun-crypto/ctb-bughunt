/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.LocationAware;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

@Plugin(name="MethodLocationPatternConverter", category="Converter")
@ConverterKeys(value={"M", "method"})
public final class MethodLocationPatternConverter
extends LogEventPatternConverter
implements LocationAware {
    private static final MethodLocationPatternConverter INSTANCE = new MethodLocationPatternConverter();

    private MethodLocationPatternConverter() {
        super("Method", "method");
    }

    public static MethodLocationPatternConverter newInstance(String[] stringArray) {
        return INSTANCE;
    }

    @Override
    public final void format(LogEvent serializable, StringBuilder stringBuilder) {
        if ((serializable = serializable.getSource()) != null) {
            stringBuilder.append(((StackTraceElement)serializable).getMethodName());
        }
    }

    @Override
    public final boolean requiresLocation() {
        return true;
    }
}

