/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.Objects;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.message.MapMessage;

@Plugin(name="MapPatternConverter", category="Converter")
@ConverterKeys(value={"K", "map", "MAP"})
public final class MapPatternConverter
extends LogEventPatternConverter {
    private static final String JAVA_UNQUOTED = MapMessage.MapFormat.JAVA_UNQUOTED.name();
    private final String key;
    private final String[] format;

    private MapPatternConverter(String[] stringArray, String ... stringArray2) {
        super(stringArray != null && stringArray.length > 0 ? "MAP{" + stringArray[0] + '}' : "MAP", "map");
        this.key = stringArray != null && stringArray.length > 0 ? stringArray[0] : null;
        this.format = stringArray2;
    }

    public static MapPatternConverter newInstance(String[] stringArray) {
        return new MapPatternConverter(stringArray, JAVA_UNQUOTED);
    }

    public static MapPatternConverter newInstance(String[] stringArray, MapMessage.MapFormat mapFormat) {
        return new MapPatternConverter(stringArray, Objects.toString((Object)mapFormat, JAVA_UNQUOTED));
    }

    @Override
    public final void format(LogEvent object, StringBuilder stringBuilder) {
        if (!(object.getMessage() instanceof MapMessage)) {
            return;
        }
        object = (MapMessage)object.getMessage();
        if (this.key == null) {
            ((MapMessage)object).formatTo(this.format, stringBuilder);
            return;
        }
        if ((object = ((MapMessage)object).get(this.key)) != null) {
            stringBuilder.append((String)object);
        }
    }
}

