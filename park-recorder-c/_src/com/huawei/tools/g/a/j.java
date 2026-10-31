/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

public final class j {
    public String bO;
    public String bh;
    public String bK;
    public String bt;

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof j)) {
            return false;
        }
        object = (j)object;
        String string = this.bO;
        String string2 = ((j)object).bO;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bh;
        string2 = ((j)object).bh;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bK;
        string2 = ((j)object).bK;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bt;
        object = ((j)object).bt;
        return !(string == null ? object != null : !string.equals(object));
    }

    public final int hashCode() {
        String string = this.bO;
        int n = 59 + (string == null ? 43 : string.hashCode());
        String string2 = this.bh;
        n = n * 59 + (string2 == null ? 43 : string2.hashCode());
        string2 = this.bK;
        n = n * 59 + (string2 == null ? 43 : string2.hashCode());
        string2 = this.bt;
        return n * 59 + (string2 == null ? 43 : string2.hashCode());
    }

    public final String toString() {
        return "TempAccount(accountId=" + this.bO + ", plateId=" + this.bh + ", sessionId=" + this.bK + ", status=" + this.bt + ")";
    }
}

