/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

public final class i {
    public String bh;
    public String aB;
    public String bd;
    public String aN;

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof i)) {
            return false;
        }
        object = (i)object;
        String string = this.bh;
        String string2 = ((i)object).bh;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aB;
        string2 = ((i)object).aB;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bd;
        string2 = ((i)object).bd;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aN;
        object = ((i)object).aN;
        return !(string == null ? object != null : !string.equals(object));
    }

    public final int hashCode() {
        String string = this.bh;
        int n = 59 + (string == null ? 43 : string.hashCode());
        String string2 = this.aB;
        n = n * 59 + (string2 == null ? 43 : string2.hashCode());
        string2 = this.bd;
        n = n * 59 + (string2 == null ? 43 : string2.hashCode());
        string2 = this.aN;
        return n * 59 + (string2 == null ? 43 : string2.hashCode());
    }

    public final String toString() {
        return "TempAccount(accountId=" + this.bh + ", plateId=" + this.aB + ", sessionId=" + this.bd + ", status=" + this.aN + ")";
    }
}

