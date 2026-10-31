/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.util.Map;
import org.apache.logging.log4j.util.Chars;
import org.apache.logging.log4j.util.StringBuilderFormattable;

public final class StringBuilders {
    private StringBuilders() {
    }

    public static StringBuilder appendDqValue(StringBuilder stringBuilder, Object object) {
        return stringBuilder.append('\"').append(object).append('\"');
    }

    public static StringBuilder appendKeyDqValue(StringBuilder stringBuilder, Map.Entry<String, String> entry) {
        return StringBuilders.appendKeyDqValue(stringBuilder, entry.getKey(), entry.getValue());
    }

    public static StringBuilder appendKeyDqValue(StringBuilder stringBuilder, String string, Object object) {
        return stringBuilder.append(string).append('=').append('\"').append(object).append('\"');
    }

    public static void appendValue(StringBuilder stringBuilder, Object object) {
        if (!StringBuilders.appendSpecificTypes(stringBuilder, object)) {
            stringBuilder.append(object);
        }
    }

    public static boolean appendSpecificTypes(StringBuilder stringBuilder, Object object) {
        if (object == null || object instanceof String) {
            stringBuilder.append((String)object);
        } else if (object instanceof StringBuilderFormattable) {
            ((StringBuilderFormattable)object).formatTo(stringBuilder);
        } else if (object instanceof CharSequence) {
            stringBuilder.append((CharSequence)object);
        } else if (object instanceof Integer) {
            stringBuilder.append((Integer)object);
        } else if (object instanceof Long) {
            stringBuilder.append((Long)object);
        } else if (object instanceof Double) {
            stringBuilder.append((Double)object);
        } else if (object instanceof Boolean) {
            stringBuilder.append((Boolean)object);
        } else if (object instanceof Character) {
            stringBuilder.append(((Character)object).charValue());
        } else if (object instanceof Short) {
            stringBuilder.append(((Short)object).shortValue());
        } else if (object instanceof Float) {
            stringBuilder.append(((Float)object).floatValue());
        } else if (object instanceof Byte) {
            stringBuilder.append(((Byte)object).byteValue());
        } else {
            return false;
        }
        return true;
    }

    public static boolean equals(CharSequence charSequence, int n, int n2, CharSequence charSequence2, int n3, int n4) {
        if (n2 == n4) {
            for (n2 = 0; n2 < n4; ++n2) {
                if (charSequence.charAt(n2 + n) == charSequence2.charAt(n2 + n3)) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    public static boolean equalsIgnoreCase(CharSequence charSequence, int n, int n2, CharSequence charSequence2, int n3, int n4) {
        if (n2 == n4) {
            for (n2 = 0; n2 < n4; ++n2) {
                if (Character.toLowerCase(charSequence.charAt(n2 + n)) == Character.toLowerCase(charSequence2.charAt(n2 + n3))) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    public static void trimToMaxSize(StringBuilder stringBuilder, int n) {
        if (stringBuilder != null && stringBuilder.capacity() > n) {
            stringBuilder.setLength(n);
            stringBuilder.trimToSize();
        }
    }

    public static void escapeJson(StringBuilder stringBuilder, int n) {
        int n2;
        char c2 = '\u0000';
        while (n < stringBuilder.length()) {
            n2 = stringBuilder.charAt(n);
            switch (n2) {
                case 8: 
                case 9: 
                case 10: 
                case 12: 
                case 13: 
                case 34: 
                case 92: {
                    ++c2;
                    break;
                }
                default: {
                    if (!Character.isISOControl((char)n2)) break;
                    c2 += 5;
                }
            }
            ++n;
        }
        StringBuilder stringBuilder2 = stringBuilder;
        stringBuilder2.setLength(stringBuilder2.length() + c2);
        n2 = stringBuilder.length() - 1;
        block12: for (n = stringBuilder.length() - 1; n2 > n; --n) {
            char c3 = stringBuilder.charAt(n);
            c2 = c3;
            switch (c3) {
                case '\b': {
                    n2 = StringBuilders.escapeAndDecrement(stringBuilder, n2, 'b');
                    continue block12;
                }
                case '\t': {
                    n2 = StringBuilders.escapeAndDecrement(stringBuilder, n2, 't');
                    continue block12;
                }
                case '\f': {
                    n2 = StringBuilders.escapeAndDecrement(stringBuilder, n2, 'f');
                    continue block12;
                }
                case '\n': {
                    n2 = StringBuilders.escapeAndDecrement(stringBuilder, n2, 'n');
                    continue block12;
                }
                case '\r': {
                    n2 = StringBuilders.escapeAndDecrement(stringBuilder, n2, 'r');
                    continue block12;
                }
                case '\"': 
                case '\\': {
                    n2 = StringBuilders.escapeAndDecrement(stringBuilder, n2, c2);
                    continue block12;
                }
                default: {
                    if (Character.isISOControl(c2)) {
                        stringBuilder.setCharAt(n2--, Chars.getUpperCaseHex(c2 & 0xF));
                        stringBuilder.setCharAt(n2--, Chars.getUpperCaseHex((c2 & 0xF0) >> 4));
                        stringBuilder.setCharAt(n2--, '0');
                        stringBuilder.setCharAt(n2--, '0');
                        stringBuilder.setCharAt(n2--, 'u');
                        stringBuilder.setCharAt(n2--, '\\');
                        continue block12;
                    }
                    stringBuilder.setCharAt(n2, c2);
                    --n2;
                }
            }
        }
    }

    private static int escapeAndDecrement(StringBuilder stringBuilder, int n, char c2) {
        stringBuilder.setCharAt(n--, c2);
        stringBuilder.setCharAt(n--, '\\');
        return n;
    }

    public static void escapeXml(StringBuilder stringBuilder, int n) {
        char c2 = '\u0000';
        while (n < stringBuilder.length()) {
            switch (stringBuilder.charAt(n)) {
                case '&': {
                    c2 += 4;
                    break;
                }
                case '<': 
                case '>': {
                    c2 += 3;
                    break;
                }
                case '\"': 
                case '\'': {
                    c2 += 5;
                }
            }
            ++n;
        }
        StringBuilder stringBuilder2 = stringBuilder;
        stringBuilder2.setLength(stringBuilder2.length() + c2);
        int n2 = stringBuilder.length() - 1;
        block13: for (n = stringBuilder.length() - 1; n2 > n; --n) {
            char c3 = stringBuilder.charAt(n);
            c2 = c3;
            switch (c3) {
                case '&': {
                    stringBuilder.setCharAt(n2--, ';');
                    stringBuilder.setCharAt(n2--, 'p');
                    stringBuilder.setCharAt(n2--, 'm');
                    stringBuilder.setCharAt(n2--, 'a');
                    stringBuilder.setCharAt(n2--, '&');
                    continue block13;
                }
                case '<': {
                    stringBuilder.setCharAt(n2--, ';');
                    stringBuilder.setCharAt(n2--, 't');
                    stringBuilder.setCharAt(n2--, 'l');
                    stringBuilder.setCharAt(n2--, '&');
                    continue block13;
                }
                case '>': {
                    stringBuilder.setCharAt(n2--, ';');
                    stringBuilder.setCharAt(n2--, 't');
                    stringBuilder.setCharAt(n2--, 'g');
                    stringBuilder.setCharAt(n2--, '&');
                    continue block13;
                }
                case '\"': {
                    stringBuilder.setCharAt(n2--, ';');
                    stringBuilder.setCharAt(n2--, 't');
                    stringBuilder.setCharAt(n2--, 'o');
                    stringBuilder.setCharAt(n2--, 'u');
                    stringBuilder.setCharAt(n2--, 'q');
                    stringBuilder.setCharAt(n2--, '&');
                    continue block13;
                }
                case '\'': {
                    stringBuilder.setCharAt(n2--, ';');
                    stringBuilder.setCharAt(n2--, 's');
                    stringBuilder.setCharAt(n2--, 'o');
                    stringBuilder.setCharAt(n2--, 'p');
                    stringBuilder.setCharAt(n2--, 'a');
                    stringBuilder.setCharAt(n2--, '&');
                    continue block13;
                }
                default: {
                    stringBuilder.setCharAt(n2--, c2);
                }
            }
        }
    }
}

