/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.LocationAware;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.NamePatternConverter;

@Plugin(name="ClassNamePatternConverter", category="Converter")
@ConverterKeys(value={"C", "class"})
public final class ClassNamePatternConverter
extends NamePatternConverter
implements LocationAware {
    private static final String NA = "?";

    private ClassNamePatternConverter(String[] stringArray) {
        super("Class Name", "class name", stringArray);
    }

    public static ClassNamePatternConverter newInstance(String[] stringArray) {
        return new ClassNamePatternConverter(stringArray);
    }

    @Override
    public final void format(LogEvent serializable, StringBuilder stringBuilder) {
        if ((serializable = serializable.getSource()) == null) {
            stringBuilder.append(NA);
            return;
        }
        this.abbreviate(((StackTraceElement)serializable).getClassName(), stringBuilder);
    }

    @Override
    public final boolean requiresLocation() {
        return true;
    }
}

