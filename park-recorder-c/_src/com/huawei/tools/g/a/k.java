/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

public final class k {
    public String cr;
    public String bh;
    public String bB;
    public String bN;
    public long cs;
    public String bt;
    public long bw;
    public long ct;
    public long cu;
    public long cv;
    public double cw;
    public double cx;
    public double cy;
    public String bq;
    public String bK;
    public boolean cz;
    public boolean cA;

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof k)) {
            return false;
        }
        object = (k)object;
        if (this.cs != ((k)object).cs) {
            return false;
        }
        if (this.bw != ((k)object).bw) {
            return false;
        }
        if (this.ct != ((k)object).ct) {
            return false;
        }
        if (this.cu != ((k)object).cu) {
            return false;
        }
        if (this.cv != ((k)object).cv) {
            return false;
        }
        if (Double.compare(this.cw, ((k)object).cw) != 0) {
            return false;
        }
        if (Double.compare(this.cx, ((k)object).cx) != 0) {
            return false;
        }
        if (Double.compare(this.cy, ((k)object).cy) != 0) {
            return false;
        }
        if (this.cz != ((k)object).cz) {
            return false;
        }
        if (this.cA != ((k)object).cA) {
            return false;
        }
        String string = this.cr;
        String string2 = ((k)object).cr;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bh;
        string2 = ((k)object).bh;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bB;
        string2 = ((k)object).bB;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bN;
        string2 = ((k)object).bN;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bt;
        string2 = ((k)object).bt;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bq;
        string2 = ((k)object).bq;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bK;
        object = ((k)object).bK;
        return !(string == null ? object != null : !string.equals(object));
    }

    public final int hashCode() {
        long l = this.cs;
        int n = 59 + (int)(l >>> 32 ^ l);
        long l2 = this.bw;
        n = n * 59 + (int)(l2 >>> 32 ^ l2);
        long l3 = this.ct;
        n = n * 59 + (int)(l3 >>> 32 ^ l3);
        long l4 = this.cu;
        n = n * 59 + (int)(l4 >>> 32 ^ l4);
        long l5 = this.cv;
        n = n * 59 + (int)(l5 >>> 32 ^ l5);
        long l6 = Double.doubleToLongBits(this.cw);
        n = n * 59 + (int)(l6 >>> 32 ^ l6);
        long l7 = Double.doubleToLongBits(this.cx);
        n = n * 59 + (int)(l7 >>> 32 ^ l7);
        long l8 = Double.doubleToLongBits(this.cy);
        n = ((n * 59 + (int)(l8 >>> 32 ^ l8)) * 59 + (this.cz ? 79 : 97)) * 59 + (this.cA ? 79 : 97);
        String string = this.cr;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bh;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bB;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bN;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bt;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bq;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bK;
        return n * 59 + (string == null ? 43 : string.hashCode());
    }

    public final String toString() {
        return "WashOrder(orderId=" + this.cr + ", plateId=" + this.bh + ", vehicleType=" + this.bB + ", spaceId=" + this.bN + ", queueSeq=" + this.cs + ", status=" + this.bt + ", createTime=" + this.bw + ", washStartTime=" + this.ct + ", washEndTime=" + this.cu + ", calledTime=" + this.cv + ", baseFee=" + this.cw + ", cancelFee=" + this.cx + ", violationFee=" + this.cy + ", memberId=" + this.bq + ", sessionId=" + this.bK + ", countedDaily=" + this.cz + ", feeSettled=" + this.cA + ")";
    }
}

