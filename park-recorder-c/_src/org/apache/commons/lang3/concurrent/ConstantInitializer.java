/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.concurrent;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.concurrent.ConcurrentInitializer;

public class ConstantInitializer<T>
implements ConcurrentInitializer<T> {
    private static final String FMT_TO_STRING = "ConstantInitializer@%d [ object = %s ]";
    private final T object;

    public ConstantInitializer(T t) {
        this.object = t;
    }

    public final T getObject() {
        return this.object;
    }

    @Override
    public T get() {
        return this.getObject();
    }

    public int hashCode() {
        if (this.getObject() != null) {
            return this.getObject().hashCode();
        }
        return 0;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ConstantInitializer)) {
            return false;
        }
        object = (ConstantInitializer)object;
        return ObjectUtils.equals(this.getObject(), ((ConstantInitializer)object).getObject());
    }

    public String toString() {
        return String.format(FMT_TO_STRING, System.identityHashCode(this), String.valueOf(this.getObject()));
    }
}

