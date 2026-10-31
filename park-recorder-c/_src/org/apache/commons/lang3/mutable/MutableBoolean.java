/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.mutable;

import java.io.Serializable;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.mutable.Mutable;

public class MutableBoolean
implements Serializable,
Comparable<MutableBoolean>,
Mutable<Boolean> {
    private static final long serialVersionUID = -4830728138360036487L;
    private boolean value;

    public MutableBoolean() {
    }

    public MutableBoolean(boolean bl) {
        this.value = bl;
    }

    public MutableBoolean(Boolean bl) {
        this.value = bl;
    }

    @Override
    public Boolean getValue() {
        return this.value;
    }

    @Override
    public void setValue(boolean bl) {
        this.value = bl;
    }

    public void setFalse() {
        this.value = false;
    }

    public void setTrue() {
        this.value = true;
    }

    @Override
    public void setValue(Boolean bl) {
        this.value = bl;
    }

    public boolean isTrue() {
        return this.value;
    }

    public boolean isFalse() {
        return !this.value;
    }

    public boolean booleanValue() {
        return this.value;
    }

    public Boolean toBoolean() {
        return this.booleanValue();
    }

    public boolean equals(Object object) {
        if (object instanceof MutableBoolean) {
            return this.value == ((MutableBoolean)object).booleanValue();
        }
        return false;
    }

    public int hashCode() {
        if (this.value) {
            return Boolean.TRUE.hashCode();
        }
        return Boolean.FALSE.hashCode();
    }

    @Override
    public int compareTo(MutableBoolean mutableBoolean) {
        return BooleanUtils.compare(this.value, mutableBoolean.value);
    }

    public String toString() {
        return String.valueOf(this.value);
    }
}

