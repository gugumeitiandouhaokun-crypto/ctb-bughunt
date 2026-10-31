/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.StringBuilders;
import org.apache.logging.log4j.util.TriConsumer;

@Plugin(name="MdcPatternConverter", category="Converter")
@ConverterKeys(value={"X", "mdc", "MDC"})
@PerformanceSensitive(value={"allocation"})
public final class MdcPatternConverter
extends LogEventPatternConverter {
    private final String key;
    private final String[] keys;
    private final boolean full;
    private static final TriConsumer<String, Object, StringBuilder> WRITE_KEY_VALUES_INTO = new TriConsumer<String, Object, StringBuilder>(){

        @Override
        public final void accept(String string, Object object, StringBuilder stringBuilder) {
            stringBuilder.append(string).append('=');
            StringBuilders.appendValue(stringBuilder, object);
            stringBuilder.append(", ");
        }
    };

    private MdcPatternConverter(String[] stringArray) {
        super(stringArray != null && stringArray.length > 0 ? "MDC{" + stringArray[0] + '}' : "MDC", "mdc");
        if (stringArray != null && stringArray.length > 0) {
            this.full = false;
            if (stringArray[0].indexOf(44) > 0) {
                this.keys = stringArray[0].split(",");
                for (int i2 = 0; i2 < this.keys.length; ++i2) {
                    this.keys[i2] = this.keys[i2].trim();
                }
                this.key = null;
                return;
            }
            this.keys = null;
            this.key = stringArray[0];
            return;
        }
        this.full = true;
        this.key = null;
        this.keys = null;
    }

    public static MdcPatternConverter newInstance(String[] stringArray) {
        return new MdcPatternConverter(stringArray);
    }

    @Override
    public final void format(LogEvent serializable, StringBuilder stringBuilder) {
        serializable = serializable.getContextData();
        if (this.full) {
            if (serializable == null || serializable.size() == 0) {
                stringBuilder.append("{}");
                return;
            }
            MdcPatternConverter.appendFully((ReadOnlyStringMap)serializable, stringBuilder);
            return;
        }
        if (this.keys != null) {
            if (serializable == null || serializable.size() == 0) {
                stringBuilder.append("{}");
                return;
            }
            MdcPatternConverter.appendSelectedKeys(this.keys, (ReadOnlyStringMap)serializable, stringBuilder);
            return;
        }
        if (serializable != null && (serializable = serializable.getValue(this.key)) != null) {
            StringBuilders.appendValue(stringBuilder, serializable);
        }
    }

    private static void appendFully(ReadOnlyStringMap readOnlyStringMap, StringBuilder stringBuilder) {
        stringBuilder.append("{");
        int n = stringBuilder.length();
        readOnlyStringMap.forEach(WRITE_KEY_VALUES_INTO, stringBuilder);
        int n2 = stringBuilder.length();
        if (n2 > n) {
            stringBuilder.setCharAt(n2 - 2, '}');
            stringBuilder.deleteCharAt(n2 - 1);
            return;
        }
        stringBuilder.append('}');
    }

    private static void appendSelectedKeys(String[] stringArray, ReadOnlyStringMap readOnlyStringMap, StringBuilder stringBuilder) {
        int n = stringBuilder.length();
        stringBuilder.append('{');
        for (int i2 = 0; i2 < stringArray.length; ++i2) {
            String string = stringArray[i2];
            Object v = readOnlyStringMap.getValue(string);
            if (v == null) continue;
            if (stringBuilder.length() - n > 1) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(string).append('=');
            StringBuilders.appendValue(stringBuilder, v);
        }
        stringBuilder.append('}');
    }
}

