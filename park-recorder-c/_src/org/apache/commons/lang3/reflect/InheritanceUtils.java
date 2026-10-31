/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.reflect;

import org.apache.commons.lang3.BooleanUtils;

public class InheritanceUtils {
    public static int distance(Class<?> clazz, Class<?> clazz2) {
        if (clazz == null || clazz2 == null) {
            return -1;
        }
        if (clazz.equals(clazz2)) {
            return 0;
        }
        int n = BooleanUtils.toInteger(clazz2.equals(clazz = clazz.getSuperclass()));
        if (n == 1) {
            return n;
        }
        if ((n += InheritanceUtils.distance(clazz, clazz2)) > 0) {
            return n + 1;
        }
        return -1;
    }
}

