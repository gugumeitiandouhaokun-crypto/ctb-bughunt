/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.LocationAware;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

@Plugin(name="FullLocationPatternConverter", category="Converter")
@ConverterKeys(value={"l", "location"})
public final class FullLocationPatternConverter
extends LogEventPatternConverter
implements LocationAware {
    private static final FullLocationPatternConverter INSTANCE = new FullLocationPatternConverter();

    private FullLocationPatternConverter() {
        super("Full Location", "fullLocation");
    }

    public static FullLocationPatternConverter newInstance(String[] stringArray) {
        return INSTANCE;
    }

    @Override
    public final void format(LogEvent serializable, StringBuilder stringBuilder) {
        if ((serializable = serializable.getSource()) != null) {
            stringBuilder.append(((StackTraceElement)serializable).toString());
        }
    }

    @Override
    public final boolean requiresLocation() {
        return true;
    }
}

