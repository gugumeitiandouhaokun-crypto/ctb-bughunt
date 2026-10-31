/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

public final class d {
    private String bA;
    private String bh;
    private String bq;
    private String bB;
    public long startTime;
    public long bC;
    public boolean bD;

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof d)) {
            return false;
        }
        object = (d)object;
        if (this.startTime != ((d)object).startTime) {
            return false;
        }
        if (this.bC != ((d)object).bC) {
            return false;
        }
        if (this.bD != ((d)object).bD) {
            return false;
        }
        String string = this.bA;
        String string2 = ((d)object).bA;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bh;
        string2 = ((d)object).bh;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bq;
        string2 = ((d)object).bq;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bB;
        object = ((d)object).bB;
        return !(string == null ? object != null : !string.equals(object));
    }

    public final int hashCode() {
        long l = this.startTime;
        int n = 59 + (int)(l >>> 32 ^ l);
        long l2 = this.bC;
        n = (n * 59 + (int)(l2 >>> 32 ^ l2)) * 59 + (this.bD ? 79 : 97);
        String string = this.bA;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bh;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bq;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bB;
        return n * 59 + (string == null ? 43 : string.hashCode());
    }

    public final String toString() {
        return "MonthlyCard(cardId=" + this.bA + ", plateId=" + this.bh + ", memberId=" + this.bq + ", vehicleType=" + this.bB + ", startTime=" + this.startTime + ", expiryTime=" + this.bC + ", active=" + this.bD + ")";
    }

    public d() {
    }

    public d(String string, String string2, String string3, String string4, long l, long l2) {
        this.bA = string;
        this.bh = string2;
        this.bq = string3;
        this.bB = string4;
        this.startTime = l;
        this.bC = l2;
        this.bD = true;
    }
}

