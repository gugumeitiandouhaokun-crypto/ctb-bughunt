/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.Date;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.AbstractPatternConverter;
import org.apache.logging.log4j.core.pattern.ArrayPatternConverter;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.NotANumber;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name="IntegerPatternConverter", category="FileConverter")
@ConverterKeys(value={"i", "index"})
@PerformanceSensitive(value={"allocation"})
public final class IntegerPatternConverter
extends AbstractPatternConverter
implements ArrayPatternConverter {
    private static final IntegerPatternConverter INSTANCE = new IntegerPatternConverter();

    private IntegerPatternConverter() {
        super("Integer", "integer");
    }

    public static IntegerPatternConverter newInstance(String[] stringArray) {
        return INSTANCE;
    }

    @Override
    public final void format(StringBuilder stringBuilder, Object ... objectArray) {
        for (int j = 0; j < objectArray.length; ++j) {
            if (objectArray[j] instanceof Integer) {
                this.format(objectArray[j], stringBuilder);
                return;
            }
            if (!(objectArray[j] instanceof NotANumber)) continue;
            stringBuilder.append("\u0000");
            return;
        }
    }

    @Override
    public final void format(Object object, StringBuilder stringBuilder) {
        if (object instanceof Integer) {
            stringBuilder.append((Integer)object);
            return;
        }
        if (object instanceof Date) {
            stringBuilder.append(((Date)object).getTime());
        }
    }
}

