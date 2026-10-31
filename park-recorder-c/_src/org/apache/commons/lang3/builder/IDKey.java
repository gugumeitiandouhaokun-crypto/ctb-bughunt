/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.builder;

final class IDKey {
    private final Object value;
    private final int id;

    public IDKey(Object object) {
        this.id = System.identityHashCode(object);
        this.value = object;
    }

    public final int hashCode() {
        return this.id;
    }

    public final boolean equals(Object object) {
        if (!(object instanceof IDKey)) {
            return false;
        }
        object = (IDKey)object;
        if (this.id != ((IDKey)object).id) {
            return false;
        }
        return this.value == ((IDKey)object).value;
    }
}

