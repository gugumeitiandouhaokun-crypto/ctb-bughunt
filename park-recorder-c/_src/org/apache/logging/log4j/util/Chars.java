/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

public final class Chars {
    public static final char CR = '\r';
    public static final char DQUOTE = '\"';
    public static final char EQ = '=';
    public static final char LF = '\n';
    public static final char NUL = '\u0000';
    public static final char QUOTE = '\'';
    public static final char SPACE = ' ';
    public static final char TAB = '\t';

    public static char getUpperCaseHex(int n) {
        if (n < 0 || n >= 16) {
            return '\u0000';
        }
        if (n < 10) {
            return Chars.getNumericalDigit(n);
        }
        return Chars.getUpperCaseAlphaDigit(n);
    }

    public static char getLowerCaseHex(int n) {
        if (n < 0 || n >= 16) {
            return '\u0000';
        }
        if (n < 10) {
            return Chars.getNumericalDigit(n);
        }
        return Chars.getLowerCaseAlphaDigit(n);
    }

    private static char getNumericalDigit(int n) {
        return (char)(48 + n);
    }

    private static char getUpperCaseAlphaDigit(int n) {
        return (char)(65 + n - 10);
    }

    private static char getLowerCaseAlphaDigit(int n) {
        return (char)(97 + n - 10);
    }

    private Chars() {
    }
}

