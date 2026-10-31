/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.tuple;

import org.apache.commons.lang3.tuple.Pair;

public final class ImmutablePair<L, R>
extends Pair<L, R> {
    private static final long serialVersionUID = 4954918890077093841L;
    public final L left;
    public final R right;

    public static <L, R> ImmutablePair<L, R> of(L l, R r) {
        return new ImmutablePair<L, R>(l, r);
    }

    public ImmutablePair(L l, R r) {
        this.left = l;
        this.right = r;
    }

    @Override
    public final L getLeft() {
        return this.left;
    }

    @Override
    public final R getRight() {
        return this.right;
    }

    @Override
    public final R setValue(R r) {
        throw new UnsupportedOperationException();
    }
}

