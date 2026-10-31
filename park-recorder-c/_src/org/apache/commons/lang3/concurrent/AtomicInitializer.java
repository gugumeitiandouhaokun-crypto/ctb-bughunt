/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.concurrent;

import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.concurrent.ConcurrentInitializer;

public abstract class AtomicInitializer<T>
implements ConcurrentInitializer<T> {
    private final AtomicReference<T> reference = new AtomicReference();

    @Override
    public T get() {
        T t = this.reference.get();
        if (t == null && !this.reference.compareAndSet(null, t = this.initialize())) {
            t = this.reference.get();
        }
        return t;
    }

    protected abstract T initialize();
}

