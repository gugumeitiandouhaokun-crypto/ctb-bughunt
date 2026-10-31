/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.apache.logging.log4j.message.AsynchronouslyFormattable;
import org.apache.logging.log4j.message.MapMessageJsonFormatter;
import org.apache.logging.log4j.message.ParameterFormatter;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.EnglishEnums;
import org.apache.logging.log4j.util.IndexedReadOnlyStringMap;
import org.apache.logging.log4j.util.IndexedStringMap;
import org.apache.logging.log4j.util.MultiFormatStringBuilderFormattable;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringBuilders;
import org.apache.logging.log4j.util.TriConsumer;

@AsynchronouslyFormattable
@PerformanceSensitive(value={"allocation"})
public class MapMessage<M extends MapMessage<M, V>, V>
implements MultiFormatStringBuilderFormattable {
    private static final long serialVersionUID = -5031471831131487120L;
    private final IndexedStringMap data;

    public MapMessage() {
        this.data = new SortedArrayStringMap();
    }

    public MapMessage(int n) {
        this.data = new SortedArrayStringMap(n);
    }

    public MapMessage(Map<String, V> map) {
        this.data = new SortedArrayStringMap(map);
    }

    @Override
    public String[] getFormats() {
        return MapFormat.names();
    }

    @Override
    public Object[] getParameters() {
        Object[] objectArray = new Object[this.data.size()];
        for (int i2 = 0; i2 < this.data.size(); ++i2) {
            objectArray[i2] = this.data.getValueAt(i2);
        }
        return objectArray;
    }

    @Override
    public String getFormat() {
        return "";
    }

    public Map<String, V> getData() {
        TreeMap treeMap = new TreeMap();
        for (int i2 = 0; i2 < this.data.size(); ++i2) {
            treeMap.put(this.data.getKeyAt(i2), this.data.getValueAt(i2));
        }
        return Collections.unmodifiableMap(treeMap);
    }

    public IndexedReadOnlyStringMap getIndexedReadOnlyStringMap() {
        return this.data;
    }

    public void clear() {
        this.data.clear();
    }

    public boolean containsKey(String string) {
        return this.data.containsKey(string);
    }

    public void put(String string, String string2) {
        if (string2 == null) {
            throw new IllegalArgumentException("No value provided for key ".concat(String.valueOf(string)));
        }
        string = this.toKey(string);
        this.validate(string, string2);
        this.data.putValue(string, string2);
    }

    public void putAll(Map<String, String> object) {
        for (Map.Entry entry : object.entrySet()) {
            this.data.putValue((String)entry.getKey(), entry.getValue());
        }
    }

    public String get(String string) {
        return ParameterFormatter.deepToString(this.data.getValue(string));
    }

    public String remove(String string) {
        String string2 = this.get(string);
        this.data.remove(string);
        return string2;
    }

    public String asString() {
        return this.format(null, new StringBuilder()).toString();
    }

    public String asString(String string) {
        try {
            return this.format(EnglishEnums.valueOf(MapFormat.class, string), new StringBuilder()).toString();
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return this.asString();
        }
    }

    public <CV> void forEach(BiConsumer<String, ? super CV> biConsumer) {
        this.data.forEach(biConsumer);
    }

    public <CV, S> void forEach(TriConsumer<String, ? super CV, S> triConsumer, S s) {
        this.data.forEach(triConsumer, s);
    }

    /*
     * Enabled aggressive block sorting
     */
    private StringBuilder format(MapFormat mapFormat, StringBuilder stringBuilder) {
        if (mapFormat != null) {
            switch (mapFormat) {
                case XML: {
                    this.asXml(stringBuilder);
                    return stringBuilder;
                }
                case JSON: {
                    this.asJson(stringBuilder);
                    return stringBuilder;
                }
                case JAVA: {
                    this.asJava(stringBuilder);
                    return stringBuilder;
                }
                case JAVA_UNQUOTED: {
                    this.asJavaUnquoted(stringBuilder);
                    return stringBuilder;
                }
            }
        }
        this.appendMap(stringBuilder);
        return stringBuilder;
    }

    public void asXml(StringBuilder stringBuilder) {
        stringBuilder.append("<Map>\n");
        for (int i2 = 0; i2 < this.data.size(); ++i2) {
            stringBuilder.append("  <Entry key=\"").append(this.data.getKeyAt(i2)).append("\">");
            int n = stringBuilder.length();
            ParameterFormatter.recursiveDeepToString(this.data.getValueAt(i2), stringBuilder, null);
            StringBuilders.escapeXml(stringBuilder, n);
            stringBuilder.append("</Entry>\n");
        }
        stringBuilder.append("</Map>");
    }

    @Override
    public String getFormattedMessage() {
        return this.asString();
    }

    @Override
    public String getFormattedMessage(String[] stringArray) {
        MapMessage mapMessage = this;
        return mapMessage.format(mapMessage.getFormat(stringArray), new StringBuilder()).toString();
    }

    private MapFormat getFormat(String[] stringArray) {
        if (stringArray == null || stringArray.length == 0) {
            return null;
        }
        for (int i2 = 0; i2 < stringArray.length; ++i2) {
            MapFormat mapFormat = MapFormat.lookupIgnoreCase(stringArray[i2]);
            if (mapFormat == null) continue;
            return mapFormat;
        }
        return null;
    }

    protected void appendMap(StringBuilder stringBuilder) {
        for (int i2 = 0; i2 < this.data.size(); ++i2) {
            if (i2 > 0) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(this.data.getKeyAt(i2)).append('=').append('\"');
            ParameterFormatter.recursiveDeepToString(this.data.getValueAt(i2), stringBuilder, null);
            stringBuilder.append('\"');
        }
    }

    protected void asJson(StringBuilder stringBuilder) {
        MapMessageJsonFormatter.format(stringBuilder, this.data);
    }

    protected void asJavaUnquoted(StringBuilder stringBuilder) {
        this.asJava(stringBuilder, false);
    }

    protected void asJava(StringBuilder stringBuilder) {
        this.asJava(stringBuilder, true);
    }

    private void asJava(StringBuilder stringBuilder, boolean bl) {
        stringBuilder.append('{');
        for (int i2 = 0; i2 < this.data.size(); ++i2) {
            if (i2 > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(this.data.getKeyAt(i2)).append('=');
            if (bl) {
                stringBuilder.append('\"');
            }
            ParameterFormatter.recursiveDeepToString(this.data.getValueAt(i2), stringBuilder, null);
            if (!bl) continue;
            stringBuilder.append('\"');
        }
        stringBuilder.append('}');
    }

    public M newInstance(Map<String, V> map) {
        return (M)new MapMessage<M, V>(map);
    }

    public String toString() {
        return this.asString();
    }

    @Override
    public void formatTo(StringBuilder stringBuilder) {
        this.format(null, stringBuilder);
    }

    @Override
    public void formatTo(String[] stringArray, StringBuilder stringBuilder) {
        MapMessage mapMessage = this;
        mapMessage.format(mapMessage.getFormat(stringArray), stringBuilder);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        object = (MapMessage)object;
        return this.data.equals(((MapMessage)object).data);
    }

    public int hashCode() {
        return this.data.hashCode();
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }

    protected void validate(String string, boolean bl) {
    }

    protected void validate(String string, byte by) {
    }

    protected void validate(String string, char c2) {
    }

    protected void validate(String string, double d2) {
    }

    protected void validate(String string, float f2) {
    }

    protected void validate(String string, int n) {
    }

    protected void validate(String string, long l) {
    }

    protected void validate(String string, Object object) {
    }

    protected void validate(String string, short s) {
    }

    protected void validate(String string, String string2) {
    }

    protected String toKey(String string) {
        return string;
    }

    public M with(String string, boolean bl) {
        string = this.toKey(string);
        this.validate(string, bl);
        this.data.putValue(string, bl);
        return (M)this;
    }

    public M with(String string, byte by) {
        string = this.toKey(string);
        this.validate(string, by);
        this.data.putValue(string, by);
        return (M)this;
    }

    public M with(String string, char c2) {
        string = this.toKey(string);
        this.validate(string, c2);
        this.data.putValue(string, Character.valueOf(c2));
        return (M)this;
    }

    public M with(String string, double d2) {
        string = this.toKey(string);
        this.validate(string, d2);
        this.data.putValue(string, d2);
        return (M)this;
    }

    public M with(String string, float f2) {
        string = this.toKey(string);
        this.validate(string, f2);
        this.data.putValue(string, Float.valueOf(f2));
        return (M)this;
    }

    public M with(String string, int n) {
        string = this.toKey(string);
        this.validate(string, n);
        this.data.putValue(string, n);
        return (M)this;
    }

    public M with(String string, long l) {
        string = this.toKey(string);
        this.validate(string, l);
        this.data.putValue(string, l);
        return (M)this;
    }

    public M with(String string, Object object) {
        string = this.toKey(string);
        this.validate(string, object);
        this.data.putValue(string, object);
        return (M)this;
    }

    public M with(String string, short s) {
        string = this.toKey(string);
        this.validate(string, s);
        this.data.putValue(string, s);
        return (M)this;
    }

    public M with(String string, String string2) {
        string = this.toKey(string);
        this.put(string, string2);
        return (M)this;
    }

    public static enum MapFormat {
        XML,
        JSON,
        JAVA,
        JAVA_UNQUOTED;


        public static MapFormat lookupIgnoreCase(String string) {
            if (XML.name().equalsIgnoreCase(string)) {
                return XML;
            }
            if (JSON.name().equalsIgnoreCase(string)) {
                return JSON;
            }
            if (JAVA.name().equalsIgnoreCase(string)) {
                return JAVA;
            }
            if (JAVA_UNQUOTED.name().equalsIgnoreCase(string)) {
                return JAVA_UNQUOTED;
            }
            return null;
        }

        public static String[] names() {
            return new String[]{XML.name(), JSON.name(), JAVA.name(), JAVA_UNQUOTED.name()};
        }
    }
}

