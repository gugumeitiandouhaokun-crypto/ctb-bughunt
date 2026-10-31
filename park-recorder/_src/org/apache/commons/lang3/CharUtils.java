/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import org.apache.commons.lang3.StringUtils;

public class CharUtils {
    private static final String[] CHAR_STRING_ARRAY = new String[128];
    private static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    public static final char LF = '\n';
    public static final char CR = '\r';

    @Deprecated
    public static Character toCharacterObject(char c2) {
        return Character.valueOf(c2);
    }

    public static Character toCharacterObject(String string) {
        if (StringUtils.isEmpty(string)) {
            return null;
        }
        return Character.valueOf(string.charAt(0));
    }

    public static char toChar(Character c2) {
        if (c2 == null) {
            throw new IllegalArgumentException("The Character must not be null");
        }
        return c2.charValue();
    }

    public static char toChar(Character c2, char c3) {
        if (c2 == null) {
            return c3;
        }
        return c2.charValue();
    }

    public static char toChar(String string) {
        if (StringUtils.isEmpty(string)) {
            throw new IllegalArgumentException("The String must not be empty");
        }
        return string.charAt(0);
    }

    public static char toChar(String string, char c2) {
        if (StringUtils.isEmpty(string)) {
            return c2;
        }
        return string.charAt(0);
    }

    public static int toIntValue(char c2) {
        if (!CharUtils.isAsciiNumeric(c2)) {
            throw new IllegalArgumentException("The character " + c2 + " is not in the range '0' - '9'");
        }
        return c2 - 48;
    }

    public static int toIntValue(char c2, int n) {
        if (!CharUtils.isAsciiNumeric(c2)) {
            return n;
        }
        return c2 - 48;
    }

    public static int toIntValue(Character c2) {
        if (c2 == null) {
            throw new IllegalArgumentException("The character must not be null");
        }
        return CharUtils.toIntValue(c2.charValue());
    }

    public static int toIntValue(Character c2, int n) {
        if (c2 == null) {
            return n;
        }
        return CharUtils.toIntValue(c2.charValue(), n);
    }

    public static String toString(char c2) {
        if (c2 < '\u0080') {
            return CHAR_STRING_ARRAY[c2];
        }
        return new String(new char[]{c2});
    }

    public static String toString(Character c2) {
        if (c2 == null) {
            return null;
        }
        return CharUtils.toString(c2.charValue());
    }

    public static String unicodeEscaped(char c2) {
        StringBuilder stringBuilder = new StringBuilder(6);
        stringBuilder.append("\\u");
        stringBuilder.append(HEX_DIGITS[c2 >> 12 & 0xF]);
        stringBuilder.append(HEX_DIGITS[c2 >> 8 & 0xF]);
        stringBuilder.append(HEX_DIGITS[c2 >> 4 & 0xF]);
        stringBuilder.append(HEX_DIGITS[c2 & 0xF]);
        return stringBuilder.toString();
    }

    public static String unicodeEscaped(Character c2) {
        if (c2 == null) {
            return null;
        }
        return CharUtils.unicodeEscaped(c2.charValue());
    }

    public static boolean isAscii(char c2) {
        return c2 < '\u0080';
    }

    public static boolean isAsciiPrintable(char c2) {
        return c2 >= ' ' && c2 < '\u007f';
    }

    public static boolean isAsciiControl(char c2) {
        return c2 < ' ' || c2 == '\u007f';
    }

    public static boolean isAsciiAlpha(char c2) {
        return CharUtils.isAsciiAlphaUpper(c2) || CharUtils.isAsciiAlphaLower(c2);
    }

    public static boolean isAsciiAlphaUpper(char c2) {
        return c2 >= 'A' && c2 <= 'Z';
    }

    public static boolean isAsciiAlphaLower(char c2) {
        return c2 >= 'a' && c2 <= 'z';
    }

    public static boolean isAsciiNumeric(char c2) {
        return c2 >= '0' && c2 <= '9';
    }

    public static boolean isAsciiAlphanumeric(char c2) {
        return CharUtils.isAsciiAlpha(c2) || CharUtils.isAsciiNumeric(c2);
    }

    public static int compare(char c2, char c3) {
        return c2 - c3;
    }

    static {
        for (char c2 = '\u0000'; c2 < CHAR_STRING_ARRAY.length; c2 = (char)(c2 + '\u0001')) {
            char c3 = c2;
            CharUtils.CHAR_STRING_ARRAY[c3] = String.valueOf(c3);
        }
    }
}

