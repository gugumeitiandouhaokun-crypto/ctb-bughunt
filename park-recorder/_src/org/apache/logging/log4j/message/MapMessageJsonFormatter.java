/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.util.IndexedStringMap;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.StringBuilderFormattable;
import org.apache.logging.log4j.util.StringBuilders;

enum MapMessageJsonFormatter {

    public static final int MAX_DEPTH = MapMessageJsonFormatter.readMaxDepth();
    private static final char DQUOTE = '\"';
    private static final char RBRACE = ']';
    private static final char LBRACE = '[';
    private static final char COMMA = ',';
    private static final char RCURLY = '}';
    private static final char LCURLY = '{';
    private static final char COLON = ':';

    private static int readMaxDepth() {
        int n = PropertiesUtil.getProperties().getIntegerProperty("log4j2.mapMessage.jsonFormatter.maxDepth", 8);
        if (n < 0) {
            throw new IllegalArgumentException("was expecting a positive maxDepth, found: ".concat(String.valueOf(n)));
        }
        return n;
    }

    static void format(StringBuilder stringBuilder, Object object) {
        MapMessageJsonFormatter.format(stringBuilder, object, 0);
    }

    private static void format(StringBuilder stringBuilder, Object objectArray, int n) {
        if (n >= MAX_DEPTH) {
            throw new IllegalArgumentException("maxDepth has been exceeded");
        }
        if (objectArray == null) {
            stringBuilder.append("null");
            return;
        }
        if (objectArray instanceof IndexedStringMap) {
            objectArray = (IndexedStringMap)objectArray;
            MapMessageJsonFormatter.formatIndexedStringMap(stringBuilder, (IndexedStringMap)objectArray, n);
            return;
        }
        if (objectArray instanceof Map) {
            objectArray = (Map)objectArray;
            MapMessageJsonFormatter.formatMap(stringBuilder, (Map<Object, Object>)objectArray, n);
            return;
        }
        if (objectArray instanceof List) {
            objectArray = (List)objectArray;
            MapMessageJsonFormatter.formatList(stringBuilder, (List<Object>)objectArray, n);
            return;
        }
        if (objectArray instanceof Collection) {
            objectArray = (Collection)objectArray;
            MapMessageJsonFormatter.formatCollection(stringBuilder, (Collection<Object>)objectArray, n);
            return;
        }
        if (objectArray instanceof Number) {
            objectArray = (Number)objectArray;
            MapMessageJsonFormatter.formatNumber(stringBuilder, (Number)objectArray);
            return;
        }
        if (objectArray instanceof Boolean) {
            boolean bl = (Boolean)objectArray;
            MapMessageJsonFormatter.formatBoolean(stringBuilder, bl);
            return;
        }
        if (objectArray instanceof StringBuilderFormattable) {
            objectArray = (StringBuilderFormattable)objectArray;
            MapMessageJsonFormatter.formatFormattable(stringBuilder, (StringBuilderFormattable)objectArray);
            return;
        }
        if (objectArray instanceof char[]) {
            objectArray = (char[])objectArray;
            MapMessageJsonFormatter.formatCharArray(stringBuilder, (char[])objectArray);
            return;
        }
        if (objectArray instanceof boolean[]) {
            objectArray = (boolean[])objectArray;
            MapMessageJsonFormatter.formatBooleanArray(stringBuilder, (boolean[])objectArray);
            return;
        }
        if (objectArray instanceof byte[]) {
            objectArray = (byte[])objectArray;
            MapMessageJsonFormatter.formatByteArray(stringBuilder, (byte[])objectArray);
            return;
        }
        if (objectArray instanceof short[]) {
            objectArray = (short[])objectArray;
            MapMessageJsonFormatter.formatShortArray(stringBuilder, (short[])objectArray);
            return;
        }
        if (objectArray instanceof int[]) {
            objectArray = (int[])objectArray;
            MapMessageJsonFormatter.formatIntArray(stringBuilder, (int[])objectArray);
            return;
        }
        if (objectArray instanceof long[]) {
            objectArray = (long[])objectArray;
            MapMessageJsonFormatter.formatLongArray(stringBuilder, (long[])objectArray);
            return;
        }
        if (objectArray instanceof float[]) {
            objectArray = (float[])objectArray;
            MapMessageJsonFormatter.formatFloatArray(stringBuilder, (float[])objectArray);
            return;
        }
        if (objectArray instanceof double[]) {
            objectArray = (double[])objectArray;
            MapMessageJsonFormatter.formatDoubleArray(stringBuilder, (double[])objectArray);
            return;
        }
        if (objectArray instanceof Object[]) {
            objectArray = objectArray;
            MapMessageJsonFormatter.formatObjectArray(stringBuilder, objectArray, n);
            return;
        }
        MapMessageJsonFormatter.formatString(stringBuilder, objectArray);
    }

    private static void formatIndexedStringMap(StringBuilder stringBuilder, IndexedStringMap indexedStringMap, int n) {
        stringBuilder.append('{');
        ++n;
        for (int j = 0; j < indexedStringMap.size(); ++j) {
            String string = indexedStringMap.getKeyAt(j);
            Object v = indexedStringMap.getValueAt(j);
            if (j > 0) {
                stringBuilder.append(',');
            }
            stringBuilder.append('\"');
            int n2 = stringBuilder.length();
            stringBuilder.append(string);
            StringBuilders.escapeJson(stringBuilder, n2);
            stringBuilder.append('\"').append(':');
            MapMessageJsonFormatter.format(stringBuilder, v, n);
        }
        stringBuilder.append('}');
    }

    private static void formatMap(StringBuilder stringBuilder, Map<Object, Object> map, int n) {
        stringBuilder.append('{');
        boolean[] blArray = new boolean[]{true};
        map.forEach((arg_0, arg_1) -> MapMessageJsonFormatter.lambda$formatMap$0(blArray, stringBuilder, ++n, arg_0, arg_1));
        stringBuilder.append('}');
    }

    private static void formatList(StringBuilder stringBuilder, List<Object> list, int n) {
        stringBuilder.append('[');
        ++n;
        for (int j = 0; j < list.size(); ++j) {
            if (j > 0) {
                stringBuilder.append(',');
            }
            Object object = list.get(j);
            MapMessageJsonFormatter.format(stringBuilder, object, n);
        }
        stringBuilder.append(']');
    }

    private static void formatCollection(StringBuilder stringBuilder, Collection<Object> collection, int n) {
        stringBuilder.append('[');
        boolean[] blArray = new boolean[]{true};
        collection.forEach(arg_0 -> MapMessageJsonFormatter.lambda$formatCollection$1(blArray, stringBuilder, ++n, arg_0));
        stringBuilder.append(']');
    }

    private static void formatNumber(StringBuilder stringBuilder, Number number) {
        double d2;
        if (number instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal)number;
            stringBuilder.append(bigDecimal.toString());
            return;
        }
        if (number instanceof Double) {
            double d3 = (Double)number;
            stringBuilder.append(d3);
            return;
        }
        if (number instanceof Float) {
            float f2 = ((Float)number).floatValue();
            stringBuilder.append(f2);
            return;
        }
        if (number instanceof Byte || number instanceof Short || number instanceof Integer || number instanceof Long) {
            long l = number.longValue();
            stringBuilder.append(l);
            return;
        }
        long l = number.longValue();
        if (Double.compare(l, d2 = number.doubleValue()) == 0) {
            stringBuilder.append(l);
            return;
        }
        stringBuilder.append(d2);
    }

    private static void formatBoolean(StringBuilder stringBuilder, boolean bl) {
        stringBuilder.append(bl);
    }

    private static void formatFormattable(StringBuilder stringBuilder, StringBuilderFormattable stringBuilderFormattable) {
        stringBuilder.append('\"');
        int n = stringBuilder.length();
        stringBuilderFormattable.formatTo(stringBuilder);
        StringBuilders.escapeJson(stringBuilder, n);
        stringBuilder.append('\"');
    }

    private static void formatCharArray(StringBuilder stringBuilder, char[] cArray) {
        stringBuilder.append('[');
        for (int j = 0; j < cArray.length; ++j) {
            if (j > 0) {
                stringBuilder.append(',');
            }
            char c2 = cArray[j];
            stringBuilder.append('\"');
            int n = stringBuilder.length();
            stringBuilder.append(c2);
            StringBuilders.escapeJson(stringBuilder, n);
            stringBuilder.append('\"');
        }
        stringBuilder.append(']');
    }

    private static void formatBooleanArray(StringBuilder stringBuilder, boolean[] blArray) {
        stringBuilder.append('[');
        for (int j = 0; j < blArray.length; ++j) {
            if (j > 0) {
                stringBuilder.append(',');
            }
            boolean bl = blArray[j];
            stringBuilder.append(bl);
        }
        stringBuilder.append(']');
    }

    private static void formatByteArray(StringBuilder stringBuilder, byte[] byArray) {
        stringBuilder.append('[');
        for (int j = 0; j < byArray.length; ++j) {
            if (j > 0) {
                stringBuilder.append(',');
            }
            byte by = byArray[j];
            stringBuilder.append(by);
        }
        stringBuilder.append(']');
    }

    private static void formatShortArray(StringBuilder stringBuilder, short[] sArray) {
        stringBuilder.append('[');
        for (int j = 0; j < sArray.length; ++j) {
            if (j > 0) {
                stringBuilder.append(',');
            }
            short s = sArray[j];
            stringBuilder.append(s);
        }
        stringBuilder.append(']');
    }

    private static void formatIntArray(StringBuilder stringBuilder, int[] nArray) {
        stringBuilder.append('[');
        for (int j = 0; j < nArray.length; ++j) {
            if (j > 0) {
                stringBuilder.append(',');
            }
            int n = nArray[j];
            stringBuilder.append(n);
        }
        stringBuilder.append(']');
    }

    private static void formatLongArray(StringBuilder stringBuilder, long[] lArray) {
        stringBuilder.append('[');
        for (int j = 0; j < lArray.length; ++j) {
            if (j > 0) {
                stringBuilder.append(',');
            }
            long l = lArray[j];
            stringBuilder.append(l);
        }
        stringBuilder.append(']');
    }

    private static void formatFloatArray(StringBuilder stringBuilder, float[] fArray) {
        stringBuilder.append('[');
        for (int j = 0; j < fArray.length; ++j) {
            if (j > 0) {
                stringBuilder.append(',');
            }
            float f2 = fArray[j];
            stringBuilder.append(f2);
        }
        stringBuilder.append(']');
    }

    private static void formatDoubleArray(StringBuilder stringBuilder, double[] dArray) {
        stringBuilder.append('[');
        for (int j = 0; j < dArray.length; ++j) {
            if (j > 0) {
                stringBuilder.append(',');
            }
            double d2 = dArray[j];
            stringBuilder.append(d2);
        }
        stringBuilder.append(']');
    }

    private static void formatObjectArray(StringBuilder stringBuilder, Object[] objectArray, int n) {
        stringBuilder.append('[');
        ++n;
        for (int j = 0; j < objectArray.length; ++j) {
            if (j > 0) {
                stringBuilder.append(',');
            }
            Object object = objectArray[j];
            MapMessageJsonFormatter.format(stringBuilder, object, n);
        }
        stringBuilder.append(']');
    }

    private static void formatString(StringBuilder stringBuilder, Object object) {
        stringBuilder.append('\"');
        int n = stringBuilder.length();
        object = String.valueOf(object);
        stringBuilder.append((String)object);
        StringBuilders.escapeJson(stringBuilder, n);
        stringBuilder.append('\"');
    }

    private static /* synthetic */ void lambda$formatCollection$1(boolean[] blArray, StringBuilder stringBuilder, int n, Object object) {
        if (blArray[0]) {
            blArray[0] = false;
        } else {
            stringBuilder.append(',');
        }
        MapMessageJsonFormatter.format(stringBuilder, object, n);
    }

    private static /* synthetic */ void lambda$formatMap$0(boolean[] object, StringBuilder stringBuilder, int n, Object object2, Object object3) {
        if (object2 == null) {
            throw new IllegalArgumentException("null keys are not allowed");
        }
        if (object[0]) {
            object[0] = false;
        } else {
            stringBuilder.append(',');
        }
        stringBuilder.append('\"');
        object = String.valueOf(object2);
        int n2 = stringBuilder.length();
        stringBuilder.append((String)object);
        StringBuilders.escapeJson(stringBuilder, n2);
        stringBuilder.append('\"').append(':');
        MapMessageJsonFormatter.format(stringBuilder, object3, n);
    }
}

