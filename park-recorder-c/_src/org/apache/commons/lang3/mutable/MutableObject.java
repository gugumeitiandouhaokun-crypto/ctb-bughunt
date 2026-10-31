/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.mutable;

import java.io.Serializable;
import org.apache.commons.lang3.mutable.Mutable;

public class MutableObject<T>
implements Serializable,
Mutable<T> {
    private static final long serialVersionUID = 86241875189L;
    private T value;

    public MutableObject() {
    }

    public MutableObject(T t) {
        this.value = t;
    }

    @Override
    public T getValue() {
        return this.value;
    }

    @Override
    public void setValue(T t) {
        this.value = t;
    }

    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (this.getClass() == object.getClass()) {
            object = (MutableObject)object;
            return this.value.equals(((MutableObject)object).value);
        }
        return false;
    }

    public int hashCode() {
        if (this.value == null) {
            return 0;
        }
        return this.value.hashCode();
    }

    public String toString() {
        if (this.value == null) {
            return "null";
        }
        return this.value.toString();
    }
}

