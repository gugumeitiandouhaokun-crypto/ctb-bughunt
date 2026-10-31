/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import org.apache.logging.log4j.core.config.Order;

public class OrderComparator
implements Serializable,
Comparator<Class<?>> {
    private static final long serialVersionUID = 1L;
    private static final Comparator<Class<?>> INSTANCE = new OrderComparator();

    public static Comparator<Class<?>> getInstance() {
        return INSTANCE;
    }

    @Override
    public int compare(Class<?> object, Class<?> object2) {
        object = Objects.requireNonNull(object, "lhs").getAnnotation(Order.class);
        object2 = Objects.requireNonNull(object2, "rhs").getAnnotation(Order.class);
        if (object == null && object2 == null) {
            return 0;
        }
        if (object2 == null) {
            return -1;
        }
        if (object == null) {
            return 1;
        }
        return Integer.signum(object2.value() - object.value());
    }
}

