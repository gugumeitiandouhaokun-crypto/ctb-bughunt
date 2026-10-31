/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j.helpers;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.Util;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class MessageFormatter {
    static final char DELIM_START = '{';
    static final char DELIM_STOP = '}';
    static final String DELIM_STR = "{}";
    private static final char ESCAPE_CHAR = '\\';

    public static final FormattingTuple format(String string, Object object) {
        return MessageFormatter.arrayFormat(string, new Object[]{object});
    }

    public static final FormattingTuple format(String string, Object object, Object object2) {
        return MessageFormatter.arrayFormat(string, new Object[]{object, object2});
    }

    static final Throwable getThrowableCandidate(Object[] object) {
        if (object == null || ((Object[])object).length == 0) {
            return null;
        }
        if ((object = object[((Object[])object).length - 1]) instanceof Throwable) {
            return (Throwable)object;
        }
        return null;
    }

    public static final FormattingTuple arrayFormat(String string, Object[] objectArray) {
        Throwable throwable = MessageFormatter.getThrowableCandidate(objectArray);
        Object[] objectArray2 = objectArray;
        if (throwable != null) {
            objectArray2 = MessageFormatter.trimmedCopy(objectArray);
        }
        return MessageFormatter.arrayFormat(string, objectArray2, throwable);
    }

    private static Object[] trimmedCopy(Object[] objectArray) {
        if (objectArray == null || objectArray.length == 0) {
            throw new IllegalStateException("non-sensical empty or null argument array");
        }
        int n = objectArray.length - 1;
        Object[] objectArray2 = new Object[n];
        System.arraycopy(objectArray, 0, objectArray2, 0, n);
        return objectArray2;
    }

    public static final FormattingTuple arrayFormat(String string, Object[] objectArray, Throwable throwable) {
        if (string == null) {
            return new FormattingTuple(null, objectArray, throwable);
        }
        if (objectArray == null) {
            return new FormattingTuple(string);
        }
        int n = 0;
        StringBuilder stringBuilder = new StringBuilder(string.length() + 50);
        for (int j = 0; j < objectArray.length; ++j) {
            int n2 = string.indexOf(DELIM_STR, n);
            if (n2 == -1) {
                if (n == 0) {
                    return new FormattingTuple(string, objectArray, throwable);
                }
                stringBuilder.append(string, n, string.length());
                return new FormattingTuple(stringBuilder.toString(), objectArray, throwable);
            }
            if (MessageFormatter.isEscapedDelimeter(string, n2)) {
                if (!MessageFormatter.isDoubleEscaped(string, n2)) {
                    --j;
                    stringBuilder.append(string, n, n2 - 1);
                    stringBuilder.append('{');
                    n = n2 + 1;
                    continue;
                }
                stringBuilder.append(string, n, n2 - 1);
                MessageFormatter.deeplyAppendParameter(stringBuilder, objectArray[j], new HashMap<Object[], Object>());
                n = n2 + 2;
                continue;
            }
            stringBuilder.append(string, n, n2);
            MessageFormatter.deeplyAppendParameter(stringBuilder, objectArray[j], new HashMap<Object[], Object>());
            n = n2 + 2;
        }
        stringBuilder.append(string, n, string.length());
        return new FormattingTuple(stringBuilder.toString(), objectArray, throwable);
    }

    static final boolean isEscapedDelimeter(String string, int n) {
        if (n == 0) {
            return false;
        }
        return string.charAt(n - 1) == '\\';
    }

    static final boolean isDoubleEscaped(String string, int n) {
        return n >= 2 && string.charAt(n - 2) == '\\';
    }

    private static void deeplyAppendParameter(StringBuilder stringBuilder, Object object, Map<Object[], Object> map) {
        if (object == null) {
            stringBuilder.append("null");
            return;
        }
        if (!object.getClass().isArray()) {
            MessageFormatter.safeObjectAppend(stringBuilder, object);
            return;
        }
        if (object instanceof boolean[]) {
            MessageFormatter.booleanArrayAppend(stringBuilder, (boolean[])object);
            return;
        }
        if (object instanceof byte[]) {
            MessageFormatter.byteArrayAppend(stringBuilder, (byte[])object);
            return;
        }
        if (object instanceof char[]) {
            MessageFormatter.charArrayAppend(stringBuilder, (char[])object);
            return;
        }
        if (object instanceof short[]) {
            MessageFormatter.shortArrayAppend(stringBuilder, (short[])object);
            return;
        }
        if (object instanceof int[]) {
            MessageFormatter.intArrayAppend(stringBuilder, (int[])object);
            return;
        }
        if (object instanceof long[]) {
            MessageFormatter.longArrayAppend(stringBuilder, (long[])object);
            return;
        }
        if (object instanceof float[]) {
            MessageFormatter.floatArrayAppend(stringBuilder, (float[])object);
            return;
        }
        if (object instanceof double[]) {
            MessageFormatter.doubleArrayAppend(stringBuilder, (double[])object);
            return;
        }
        MessageFormatter.objectArrayAppend(stringBuilder, (Object[])object, map);
    }

    private static void safeObjectAppend(StringBuilder stringBuilder, Object object) {
        try {
            String string = object.toString();
            stringBuilder.append(string);
            return;
        }
        catch (Throwable throwable) {
            Util.report("SLF4J: Failed toString() invocation on an object of type [" + object.getClass().getName() + "]", throwable);
            stringBuilder.append("[FAILED toString()]");
            return;
        }
    }

    private static void objectArrayAppend(StringBuilder stringBuilder, Object[] objectArray, Map<Object[], Object> map) {
        stringBuilder.append('[');
        if (!map.containsKey(objectArray)) {
            map.put(objectArray, null);
            int n = objectArray.length;
            for (int j = 0; j < n; ++j) {
                MessageFormatter.deeplyAppendParameter(stringBuilder, objectArray[j], map);
                if (j == n - 1) continue;
                stringBuilder.append(", ");
            }
            map.remove(objectArray);
        } else {
            stringBuilder.append("...");
        }
        stringBuilder.append(']');
    }

    private static void booleanArrayAppend(StringBuilder stringBuilder, boolean[] blArray) {
        stringBuilder.append('[');
        int n = blArray.length;
        for (int j = 0; j < n; ++j) {
            stringBuilder.append(blArray[j]);
            if (j == n - 1) continue;
            stringBuilder.append(", ");
        }
        stringBuilder.append(']');
    }

    private static void byteArrayAppend(StringBuilder stringBuilder, byte[] byArray) {
        stringBuilder.append('[');
        int n = byArray.length;
        for (int j = 0; j < n; ++j) {
            stringBuilder.append(byArray[j]);
            if (j == n - 1) continue;
            stringBuilder.append(", ");
        }
        stringBuilder.append(']');
    }

    private static void charArrayAppend(StringBuilder stringBuilder, char[] cArray) {
        stringBuilder.append('[');
        int n = cArray.length;
        for (int j = 0; j < n; ++j) {
            stringBuilder.append(cArray[j]);
            if (j == n - 1) continue;
            stringBuilder.append(", ");
        }
        stringBuilder.append(']');
    }

    private static void shortArrayAppend(StringBuilder stringBuilder, short[] sArray) {
        stringBuilder.append('[');
        int n = sArray.length;
        for (int j = 0; j < n; ++j) {
            stringBuilder.append(sArray[j]);
            if (j == n - 1) continue;
            stringBuilder.append(", ");
        }
        stringBuilder.append(']');
    }

    private static void intArrayAppend(StringBuilder stringBuilder, int[] nArray) {
        stringBuilder.append('[');
        int n = nArray.length;
        for (int j = 0; j < n; ++j) {
            stringBuilder.append(nArray[j]);
            if (j == n - 1) continue;
            stringBuilder.append(", ");
        }
        stringBuilder.append(']');
    }

    private static void longArrayAppend(StringBuilder stringBuilder, long[] lArray) {
        stringBuilder.append('[');
        int n = lArray.length;
        for (int j = 0; j < n; ++j) {
            stringBuilder.append(lArray[j]);
            if (j == n - 1) continue;
            stringBuilder.append(", ");
        }
        stringBuilder.append(']');
    }

    private static void floatArrayAppend(StringBuilder stringBuilder, float[] fArray) {
        stringBuilder.append('[');
        int n = fArray.length;
        for (int j = 0; j < n; ++j) {
            stringBuilder.append(fArray[j]);
            if (j == n - 1) continue;
            stringBuilder.append(", ");
        }
        stringBuilder.append(']');
    }

    private static void doubleArrayAppend(StringBuilder stringBuilder, double[] dArray) {
        stringBuilder.append('[');
        int n = dArray.length;
        for (int j = 0; j < n; ++j) {
            stringBuilder.append(dArray[j]);
            if (j == n - 1) continue;
            stringBuilder.append(", ");
        }
        stringBuilder.append(']');
    }
}

