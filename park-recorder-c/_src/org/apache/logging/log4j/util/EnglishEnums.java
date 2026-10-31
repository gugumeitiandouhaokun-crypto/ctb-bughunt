/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.util.Locale;

public final class EnglishEnums {
    private EnglishEnums() {
    }

    public static <T extends Enum<T>> T valueOf(Class<T> clazz, String string) {
        return EnglishEnums.valueOf(clazz, string, null);
    }

    public static <T extends Enum<T>> T valueOf(Class<T> clazz, String string, T t) {
        if (string == null) {
            return t;
        }
        return Enum.valueOf(clazz, string.toUpperCase(Locale.ENGLISH));
    }
}

