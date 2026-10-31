/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

public final class d {
    private String aU;
    private String aB;
    private String aK;
    private String aV;
    public long startTime;
    public long aW;
    public boolean aX;

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
        if (this.aW != ((d)object).aW) {
            return false;
        }
        if (this.aX != ((d)object).aX) {
            return false;
        }
        String string = this.aU;
        String string2 = ((d)object).aU;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aB;
        string2 = ((d)object).aB;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aK;
        string2 = ((d)object).aK;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aV;
        object = ((d)object).aV;
        return !(string == null ? object != null : !string.equals(object));
    }

    public final int hashCode() {
        long l = this.startTime;
        int n = 59 + (int)(l >>> 32 ^ l);
        long l2 = this.aW;
        n = (n * 59 + (int)(l2 >>> 32 ^ l2)) * 59 + (this.aX ? 79 : 97);
        String string = this.aU;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.aB;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.aK;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.aV;
        return n * 59 + (string == null ? 43 : string.hashCode());
    }

    public final String toString() {
        return "MonthlyCard(cardId=" + this.aU + ", plateId=" + this.aB + ", memberId=" + this.aK + ", vehicleType=" + this.aV + ", startTime=" + this.startTime + ", expiryTime=" + this.aW + ", active=" + this.aX + ")";
    }

    public d() {
    }

    public d(String string, String string2, String string3, String string4, long l, long l2) {
        this.aU = string;
        this.aB = string2;
        this.aK = string3;
        this.aV = string4;
        this.startTime = l;
        this.aW = l2;
        this.aX = true;
    }
}

