/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.tuple;

import java.io.Serializable;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.tuple.ImmutableTriple;

public abstract class Triple<L, M, R>
implements Serializable,
Comparable<Triple<L, M, R>> {
    private static final long serialVersionUID = 1L;

    public static <L, M, R> Triple<L, M, R> of(L l, M m, R r) {
        return new ImmutableTriple<L, M, R>(l, m, r);
    }

    public abstract L getLeft();

    public abstract M getMiddle();

    public abstract R getRight();

    @Override
    public int compareTo(Triple<L, M, R> triple) {
        return new CompareToBuilder().append(this.getLeft(), triple.getLeft()).append(this.getMiddle(), triple.getMiddle()).append(this.getRight(), triple.getRight()).toComparison();
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Triple) {
            object = (Triple)object;
            return ObjectUtils.equals(this.getLeft(), ((Triple)object).getLeft()) && ObjectUtils.equals(this.getMiddle(), ((Triple)object).getMiddle()) && ObjectUtils.equals(this.getRight(), ((Triple)object).getRight());
        }
        return false;
    }

    public int hashCode() {
        return (this.getLeft() == null ? 0 : this.getLeft().hashCode()) ^ (this.getMiddle() == null ? 0 : this.getMiddle().hashCode()) ^ (this.getRight() == null ? 0 : this.getRight().hashCode());
    }

    public String toString() {
        return "(" + this.getLeft() + "," + this.getMiddle() + "," + this.getRight() + ")";
    }

    public String toString(String string) {
        return String.format(string, this.getLeft(), this.getMiddle(), this.getRight());
    }
}

