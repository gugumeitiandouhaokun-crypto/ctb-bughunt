/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

public final class a {
    private String bh;

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof a)) {
            return false;
        }
        object = (a)object;
        String string = this.bh;
        object = ((a)object).bh;
        return !(string == null ? object != null : !string.equals(object));
    }

    public final int hashCode() {
        String string = this.bh;
        return 59 + (string == null ? 43 : string.hashCode());
    }

    public final String toString() {
        return "BlacklistEntry(plateId=" + this.bh + ")";
    }

    public a() {
    }

    public a(String string) {
        this.bh = string;
    }
}

