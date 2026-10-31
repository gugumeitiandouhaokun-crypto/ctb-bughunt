/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

public final class h {
    public String ck;
    public String bh;
    public String bN;
    public String cl;
    public String bq;
    public String bt;
    public long bw;
    public long bC;
    public String bK;

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof h)) {
            return false;
        }
        object = (h)object;
        if (this.bw != ((h)object).bw) {
            return false;
        }
        if (this.bC != ((h)object).bC) {
            return false;
        }
        String string = this.ck;
        String string2 = ((h)object).ck;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bh;
        string2 = ((h)object).bh;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bN;
        string2 = ((h)object).bN;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.cl;
        string2 = ((h)object).cl;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bq;
        string2 = ((h)object).bq;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bt;
        string2 = ((h)object).bt;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bK;
        object = ((h)object).bK;
        return !(string == null ? object != null : !string.equals(object));
    }

    public final int hashCode() {
        long l = this.bw;
        int n = 59 + (int)(l >>> 32 ^ l);
        long l2 = this.bC;
        n = n * 59 + (int)(l2 >>> 32 ^ l2);
        String string = this.ck;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bh;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bN;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.cl;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bq;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bt;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bK;
        return n * 59 + (string == null ? 43 : string.hashCode());
    }

    public final String toString() {
        return "Reservation(reservationId=" + this.ck + ", plateId=" + this.bh + ", spaceId=" + this.bN + ", spaceType=" + this.cl + ", memberId=" + this.bq + ", status=" + this.bt + ", createTime=" + this.bw + ", expiryTime=" + this.bC + ", sessionId=" + this.bK + ")";
    }
}

