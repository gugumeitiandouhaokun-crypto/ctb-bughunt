/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

@Plugin(name="FileLocationPatternConverter", category="Converter")
@ConverterKeys(value={"F", "file"})
public final class FileLocationPatternConverter
extends LogEventPatternConverter {
    private static final FileLocationPatternConverter INSTANCE = new FileLocationPatternConverter();

    private FileLocationPatternConverter() {
        super("File Location", "file");
    }

    public static FileLocationPatternConverter newInstance(String[] stringArray) {
        return INSTANCE;
    }

    @Override
    public final void format(LogEvent serializable, StringBuilder stringBuilder) {
        if ((serializable = serializable.getSource()) != null) {
            stringBuilder.append(((StackTraceElement)serializable).getFileName());
        }
    }
}

