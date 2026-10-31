/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import org.apache.logging.log4j.util.PropertiesUtil;

public final class Strings {
    public static final String EMPTY = "";
    public static final String LINE_SEPARATOR = PropertiesUtil.getProperties().getStringProperty("line.separator", "\n");

    public static String dquote(String string) {
        return "\"" + string + '\"';
    }

    public static boolean isBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    public static boolean isEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    public static boolean isNotBlank(String string) {
        return !Strings.isBlank(string);
    }

    public static boolean isNotEmpty(CharSequence charSequence) {
        return !Strings.isEmpty(charSequence);
    }

    public static String join(Iterable<?> iterable, char c2) {
        if (iterable == null) {
            return null;
        }
        return Strings.join(iterable.iterator(), c2);
    }

    public static String join(Iterator<?> iterator, char c2) {
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return EMPTY;
        }
        Object obj = iterator.next();
        if (!iterator.hasNext()) {
            return Objects.toString(obj, EMPTY);
        }
        StringBuilder stringBuilder = new StringBuilder(256);
        if (obj != null) {
            stringBuilder.append(obj);
        }
        while (iterator.hasNext()) {
            stringBuilder.append(c2);
            obj = iterator.next();
            if (obj == null) continue;
            stringBuilder.append(obj);
        }
        return stringBuilder.toString();
    }

    public static String left(String string, int n) {
        if (string == null) {
            return null;
        }
        if (n < 0) {
            return EMPTY;
        }
        if (string.length() <= n) {
            return string;
        }
        return string.substring(0, n);
    }

    public static String quote(String string) {
        return "'" + string + '\'';
    }

    public static String trimToNull(String string) {
        if (Strings.isEmpty(string = string == null ? null : string.trim())) {
            return null;
        }
        return string;
    }

    private Strings() {
    }

    public static String toRootUpperCase(String string) {
        return string.toUpperCase(Locale.ROOT);
    }
}

