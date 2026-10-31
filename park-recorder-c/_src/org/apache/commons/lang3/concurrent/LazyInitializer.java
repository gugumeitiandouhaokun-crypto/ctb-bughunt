/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.concurrent;

import org.apache.commons.lang3.concurrent.ConcurrentInitializer;

public abstract class LazyInitializer<T>
implements ConcurrentInitializer<T> {
    private volatile T object;

    @Override
    public T get() {
        T t = this.object;
        if (t == null) {
            LazyInitializer lazyInitializer = this;
            synchronized (lazyInitializer) {
                t = this.object;
                if (t == null) {
                    this.object = t = this.initialize();
                }
            }
        }
        return t;
    }

    protected abstract T initialize();
}

