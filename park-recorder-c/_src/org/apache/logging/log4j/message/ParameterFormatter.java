/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.util.StringBuilders;

final class ParameterFormatter {
    static final String RECURSION_PREFIX = "[...";
    static final String RECURSION_SUFFIX = "...]";
    static final String ERROR_PREFIX = "[!!!";
    static final String ERROR_SEPARATOR = "=>";
    static final String ERROR_MSG_SEPARATOR = ":";
    static final String ERROR_SUFFIX = "!!!]";
    private static final char DELIM_START = '{';
    private static final char DELIM_STOP = '}';
    private static final char ESCAPE_CHAR = '\\';
    private static ThreadLocal<SimpleDateFormat> threadLocalSimpleDateFormat = new ThreadLocal();

    private ParameterFormatter() {
    }

    static int countArgumentPlaceholders(String string) {
        if (string == null) {
            return 0;
        }
        int n = string.length();
        int n2 = 0;
        boolean bl = false;
        for (int i2 = 0; i2 < n - 1; ++i2) {
            char c2 = string.charAt(i2);
            if (c2 == '\\') {
                bl = !bl;
                continue;
            }
            if (c2 == '{') {
                if (!bl && string.charAt(i2 + 1) == '}') {
                    ++n2;
                    ++i2;
                }
                bl = false;
                continue;
            }
            bl = false;
        }
        return n2;
    }

    static int countArgumentPlaceholders2(String string, int[] nArray) {
        if (string == null) {
            return 0;
        }
        int n = string.length();
        int n2 = 0;
        boolean bl = false;
        for (int i2 = 0; i2 < n - 1; ++i2) {
            char c2 = string.charAt(i2);
            if (c2 == '\\') {
                bl = !bl;
                nArray[0] = -1;
                ++n2;
                continue;
            }
            if (c2 == '{') {
                if (!bl && string.charAt(i2 + 1) == '}') {
                    nArray[n2] = i2++;
                    ++n2;
                }
                bl = false;
                continue;
            }
            bl = false;
        }
        return n2;
    }

    static int countArgumentPlaceholders3(char[] cArray, int n, int[] nArray) {
        int n2 = 0;
        boolean bl = false;
        for (int i2 = 0; i2 < n - 1; ++i2) {
            char c2 = cArray[i2];
            if (c2 == '\\') {
                bl = !bl;
                continue;
            }
            if (c2 == '{') {
                if (!bl && cArray[i2 + 1] == '}') {
                    nArray[n2] = i2++;
                    ++n2;
                }
                bl = false;
                continue;
            }
            bl = false;
        }
        return n2;
    }

    static String format(String string, Object[] objectArray) {
        StringBuilder stringBuilder = new StringBuilder();
        int n = objectArray == null ? 0 : objectArray.length;
        ParameterFormatter.formatMessage(stringBuilder, string, objectArray, n);
        return stringBuilder.toString();
    }

    static void formatMessage2(StringBuilder stringBuilder, String string, Object[] objectArray, int n, int[] nArray) {
        if (string == null || objectArray == null || n == 0) {
            stringBuilder.append(string);
            return;
        }
        int n2 = 0;
        for (int i2 = 0; i2 < n; ++i2) {
            stringBuilder.append(string, n2, nArray[i2]);
            n2 = nArray[i2] + 2;
            ParameterFormatter.recursiveDeepToString(objectArray[i2], stringBuilder, null);
        }
        stringBuilder.append(string, n2, string.length());
    }

    static void formatMessage3(StringBuilder stringBuilder, char[] cArray, int n, Object[] objectArray, int n2, int[] nArray) {
        if (cArray == null) {
            return;
        }
        if (objectArray == null || n2 == 0) {
            stringBuilder.append(cArray);
            return;
        }
        int n3 = 0;
        for (int i2 = 0; i2 < n2; ++i2) {
            stringBuilder.append(cArray, n3, nArray[i2]);
            n3 = nArray[i2] + 2;
            ParameterFormatter.recursiveDeepToString(objectArray[i2], stringBuilder, null);
        }
        stringBuilder.append(cArray, n3, n);
    }

    static void formatMessage(StringBuilder stringBuilder, String string, Object[] objectArray, int n) {
        int n2;
        if (string == null || objectArray == null || n == 0) {
            stringBuilder.append(string);
            return;
        }
        int n3 = 0;
        int n4 = 0;
        int n5 = string.length();
        for (n2 = 0; n2 < n5 - 1; ++n2) {
            char c2 = string.charAt(n2);
            if (c2 == '\\') {
                ++n3;
                continue;
            }
            if (ParameterFormatter.isDelimPair(c2, string, n2)) {
                ++n2;
                ParameterFormatter.writeEscapedEscapeChars(n3, stringBuilder);
                if (ParameterFormatter.isOdd(n3)) {
                    ParameterFormatter.writeDelimPair(stringBuilder);
                } else {
                    ParameterFormatter.writeArgOrDelimPair(objectArray, n, n4, stringBuilder);
                    ++n4;
                }
            } else {
                ParameterFormatter.handleLiteralChar(stringBuilder, n3, c2);
            }
            n3 = 0;
        }
        ParameterFormatter.handleRemainingCharIfAny(string, n5, stringBuilder, n3, n2);
    }

    private static boolean isDelimPair(char c2, String string, int n) {
        return c2 == '{' && string.charAt(n + 1) == '}';
    }

    private static void handleRemainingCharIfAny(String string, int n, StringBuilder stringBuilder, int n2, int n3) {
        if (n3 == n - 1) {
            char c2 = string.charAt(n3);
            ParameterFormatter.handleLastChar(stringBuilder, n2, c2);
        }
    }

    private static void handleLastChar(StringBuilder stringBuilder, int n, char c2) {
        if (c2 == '\\') {
            ParameterFormatter.writeUnescapedEscapeChars(n + 1, stringBuilder);
            return;
        }
        ParameterFormatter.handleLiteralChar(stringBuilder, n, c2);
    }

    private static void handleLiteralChar(StringBuilder stringBuilder, int n, char c2) {
        ParameterFormatter.writeUnescapedEscapeChars(n, stringBuilder);
        stringBuilder.append(c2);
    }

    private static void writeDelimPair(StringBuilder stringBuilder) {
        stringBuilder.append('{');
        stringBuilder.append('}');
    }

    private static boolean isOdd(int n) {
        return (n & 1) == 1;
    }

    private static void writeEscapedEscapeChars(int n, StringBuilder stringBuilder) {
        ParameterFormatter.writeUnescapedEscapeChars(n >> 1, stringBuilder);
    }

    private static void writeUnescapedEscapeChars(int n, StringBuilder stringBuilder) {
        while (n > 0) {
            stringBuilder.append('\\');
            --n;
        }
    }

    private static void writeArgOrDelimPair(Object[] objectArray, int n, int n2, StringBuilder stringBuilder) {
        if (n2 < n) {
            ParameterFormatter.recursiveDeepToString(objectArray[n2], stringBuilder, null);
            return;
        }
        ParameterFormatter.writeDelimPair(stringBuilder);
    }

    static String deepToString(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof String) {
            return (String)object;
        }
        if (object instanceof Integer) {
            return Integer.toString((Integer)object);
        }
        if (object instanceof Long) {
            return Long.toString((Long)object);
        }
        if (object instanceof Double) {
            return Double.toString((Double)object);
        }
        if (object instanceof Boolean) {
            return Boolean.toString((Boolean)object);
        }
        if (object instanceof Character) {
            return Character.toString(((Character)object).charValue());
        }
        if (object instanceof Short) {
            return Short.toString((Short)object);
        }
        if (object instanceof Float) {
            return Float.toString(((Float)object).floatValue());
        }
        if (object instanceof Byte) {
            return Byte.toString((Byte)object);
        }
        StringBuilder stringBuilder = new StringBuilder();
        ParameterFormatter.recursiveDeepToString(object, stringBuilder, null);
        return stringBuilder.toString();
    }

    static void recursiveDeepToString(Object object, StringBuilder stringBuilder, Set<String> set) {
        if (ParameterFormatter.appendSpecialTypes(object, stringBuilder)) {
            return;
        }
        if (ParameterFormatter.isMaybeRecursive(object)) {
            ParameterFormatter.appendPotentiallyRecursiveValue(object, stringBuilder, set);
            return;
        }
        ParameterFormatter.tryObjectToString(object, stringBuilder);
    }

    private static boolean appendSpecialTypes(Object object, StringBuilder stringBuilder) {
        return StringBuilders.appendSpecificTypes(stringBuilder, object) || ParameterFormatter.appendDate(object, stringBuilder);
    }

    private static boolean appendDate(Object object, StringBuilder stringBuilder) {
        if (!(object instanceof Date)) {
            return false;
        }
        object = (Date)object;
        SimpleDateFormat simpleDateFormat = ParameterFormatter.getSimpleDateFormat();
        stringBuilder.append(simpleDateFormat.format((Date)object));
        return true;
    }

    private static SimpleDateFormat getSimpleDateFormat() {
        SimpleDateFormat simpleDateFormat = threadLocalSimpleDateFormat.get();
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            threadLocalSimpleDateFormat.set(simpleDateFormat);
        }
        return simpleDateFormat;
    }

    private static boolean isMaybeRecursive(Object object) {
        return object.getClass().isArray() || object instanceof Map || object instanceof Collection;
    }

    private static void appendPotentiallyRecursiveValue(Object object, StringBuilder stringBuilder, Set<String> set) {
        Class<?> clazz = object.getClass();
        if (clazz.isArray()) {
            ParameterFormatter.appendArray(object, stringBuilder, set, clazz);
            return;
        }
        if (object instanceof Map) {
            ParameterFormatter.appendMap(object, stringBuilder, set);
            return;
        }
        if (object instanceof Collection) {
            ParameterFormatter.appendCollection(object, stringBuilder, set);
        }
    }

    private static void appendArray(Object objectArray, StringBuilder stringBuilder, Set<String> set, Class<?> object) {
        if (object == byte[].class) {
            stringBuilder.append(Arrays.toString((byte[])objectArray));
            return;
        }
        if (object == short[].class) {
            stringBuilder.append(Arrays.toString((short[])objectArray));
            return;
        }
        if (object == int[].class) {
            stringBuilder.append(Arrays.toString((int[])objectArray));
            return;
        }
        if (object == long[].class) {
            stringBuilder.append(Arrays.toString((long[])objectArray));
            return;
        }
        if (object == float[].class) {
            stringBuilder.append(Arrays.toString((float[])objectArray));
            return;
        }
        if (object == double[].class) {
            stringBuilder.append(Arrays.toString((double[])objectArray));
            return;
        }
        if (object == boolean[].class) {
            stringBuilder.append(Arrays.toString((boolean[])objectArray));
            return;
        }
        if (object == char[].class) {
            stringBuilder.append(Arrays.toString((char[])objectArray));
            return;
        }
        if (set == null) {
            set = new HashSet<String>();
        }
        if (set.contains(object = ParameterFormatter.identityToString(objectArray))) {
            stringBuilder.append(RECURSION_PREFIX).append((String)object).append(RECURSION_SUFFIX);
            return;
        }
        set.add((String)object);
        objectArray = objectArray;
        stringBuilder.append('[');
        boolean bl = true;
        for (Object object2 : objectArray) {
            if (bl) {
                bl = false;
            } else {
                stringBuilder.append(", ");
            }
            ParameterFormatter.recursiveDeepToString(object2, stringBuilder, new HashSet<String>(set));
        }
        stringBuilder.append(']');
    }

    private static void appendMap(Object iterator, StringBuilder stringBuilder, Set<String> set) {
        String string;
        if (set == null) {
            set = new HashSet<String>();
        }
        if (set.contains(string = ParameterFormatter.identityToString(iterator))) {
            stringBuilder.append(RECURSION_PREFIX).append(string).append(RECURSION_SUFFIX);
            return;
        }
        set.add(string);
        iterator = (Map)((Object)iterator);
        stringBuilder.append('{');
        boolean bl = true;
        for (Map.Entry entry : iterator.entrySet()) {
            if (bl) {
                bl = false;
            } else {
                stringBuilder.append(", ");
            }
            Object k2 = entry.getKey();
            entry = entry.getValue();
            ParameterFormatter.recursiveDeepToString(k2, stringBuilder, new HashSet<String>(set));
            stringBuilder.append('=');
            ParameterFormatter.recursiveDeepToString(entry, stringBuilder, new HashSet<String>(set));
        }
        stringBuilder.append('}');
    }

    private static void appendCollection(Object iterator, StringBuilder stringBuilder, Set<String> set) {
        String string;
        if (set == null) {
            set = new HashSet<String>();
        }
        if (set.contains(string = ParameterFormatter.identityToString(iterator))) {
            stringBuilder.append(RECURSION_PREFIX).append(string).append(RECURSION_SUFFIX);
            return;
        }
        set.add(string);
        iterator = (Collection)((Object)iterator);
        stringBuilder.append('[');
        boolean bl = true;
        iterator = iterator.iterator();
        while (iterator.hasNext()) {
            Object e2 = iterator.next();
            if (bl) {
                bl = false;
            } else {
                stringBuilder.append(", ");
            }
            ParameterFormatter.recursiveDeepToString(e2, stringBuilder, new HashSet<String>(set));
        }
        stringBuilder.append(']');
    }

    private static void tryObjectToString(Object object, StringBuilder stringBuilder) {
        try {
            stringBuilder.append(object.toString());
            return;
        }
        catch (Throwable throwable) {
            ParameterFormatter.handleErrorInObjectToString(object, stringBuilder, throwable);
            return;
        }
    }

    private static void handleErrorInObjectToString(Object object, StringBuilder stringBuilder, Throwable object2) {
        stringBuilder.append(ERROR_PREFIX);
        stringBuilder.append(ParameterFormatter.identityToString(object));
        stringBuilder.append(ERROR_SEPARATOR);
        object = ((Throwable)object2).getMessage();
        object2 = object2.getClass().getName();
        stringBuilder.append((String)object2);
        if (!((String)object2).equals(object)) {
            stringBuilder.append(ERROR_MSG_SEPARATOR);
            stringBuilder.append((String)object);
        }
        stringBuilder.append(ERROR_SUFFIX);
    }

    static String identityToString(Object object) {
        if (object == null) {
            return null;
        }
        return object.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(object));
    }
}

