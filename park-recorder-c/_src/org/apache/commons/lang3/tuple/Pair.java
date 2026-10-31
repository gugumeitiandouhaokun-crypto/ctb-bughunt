/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.tuple;

import java.io.Serializable;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;

public abstract class Pair<L, R>
implements Serializable,
Comparable<Pair<L, R>>,
Map.Entry<L, R> {
    private static final long serialVersionUID = 4954918890077093841L;

    public static <L, R> Pair<L, R> of(L l, R r) {
        return new ImmutablePair<L, R>(l, r);
    }

    public abstract L getLeft();

    public abstract R getRight();

    @Override
    public final L getKey() {
        return this.getLeft();
    }

    @Override
    public R getValue() {
        return this.getRight();
    }

    @Override
    public int compareTo(Pair<L, R> pair) {
        return new CompareToBuilder().append(this.getLeft(), pair.getLeft()).append(this.getRight(), pair.getRight()).toComparison();
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Map.Entry) {
            object = (Map.Entry)object;
            return ObjectUtils.equals(this.getKey(), object.getKey()) && ObjectUtils.equals(this.getValue(), object.getValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (this.getKey() == null ? 0 : this.getKey().hashCode()) ^ (this.getValue() == null ? 0 : this.getValue().hashCode());
    }

    public String toString() {
        return "(" + this.getLeft() + ',' + this.getRight() + ')';
    }

    public String toString(String string) {
        return String.format(string, this.getLeft(), this.getRight());
    }
}

