/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.lang.reflect.Array;

public final class CyclicBuffer<T> {
    private final T[] ring;
    private int first = 0;
    private int last = 0;
    private int numElems = 0;
    private final Class<T> clazz;

    public CyclicBuffer(Class<T> clazz, int n) {
        if (n < 0) {
            throw new IllegalArgumentException("The maxSize argument (" + n + ") cannot be negative.");
        }
        this.ring = this.makeArray(clazz, n);
        this.clazz = clazz;
    }

    private T[] makeArray(Class<T> clazz, int n) {
        return (Object[])Array.newInstance(clazz, n);
    }

    public final synchronized void add(T t) {
        if (this.ring.length > 0) {
            this.ring[this.last] = t;
            if (++this.last == this.ring.length) {
                this.last = 0;
            }
            if (this.numElems < this.ring.length) {
                ++this.numElems;
                return;
            }
            if (++this.first == this.ring.length) {
                this.first = 0;
            }
        }
    }

    public final synchronized T[] removeAll() {
        CyclicBuffer cyclicBuffer = this;
        T[] TArray = cyclicBuffer.makeArray(cyclicBuffer.clazz, this.numElems);
        int n = 0;
        while (this.numElems > 0) {
            --this.numElems;
            TArray[n++] = this.ring[this.first];
            this.ring[this.first] = null;
            if (++this.first != this.ring.length) continue;
            this.first = 0;
        }
        return TArray;
    }

    public final boolean isEmpty() {
        return this.numElems == 0;
    }
}

