/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

public final class f {
    public String bd;
    public String aB;
    public String aD;
    public String aC;
    public long be;
    public long bf;
    public String bg;
    public String bh;
    public long bi;
    public long bj;
    public boolean bk;
    public int bl;
    public int bm;
    public String bn;
    public String bo;
    public String bp;
    public long bq;
    public long br;
    public String aV;
    public String bs;
    public String bt;
    public double bu;
    public double bv;
    public long bw;
    public double bx;
    public long by;
    public long bz;
    public long bA;

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof f)) {
            return false;
        }
        object = (f)object;
        if (this.be != ((f)object).be) {
            return false;
        }
        if (this.bf != ((f)object).bf) {
            return false;
        }
        if (this.bi != ((f)object).bi) {
            return false;
        }
        if (this.bj != ((f)object).bj) {
            return false;
        }
        if (this.bk != ((f)object).bk) {
            return false;
        }
        if (this.bl != ((f)object).bl) {
            return false;
        }
        if (this.bm != ((f)object).bm) {
            return false;
        }
        if (this.bq != ((f)object).bq) {
            return false;
        }
        if (this.br != ((f)object).br) {
            return false;
        }
        if (Double.compare(this.bu, ((f)object).bu) != 0) {
            return false;
        }
        if (Double.compare(this.bv, ((f)object).bv) != 0) {
            return false;
        }
        if (this.bw != ((f)object).bw) {
            return false;
        }
        if (Double.compare(this.bx, ((f)object).bx) != 0) {
            return false;
        }
        if (this.by != ((f)object).by) {
            return false;
        }
        if (this.bz != ((f)object).bz) {
            return false;
        }
        if (this.bA != ((f)object).bA) {
            return false;
        }
        String string = this.bd;
        String string2 = ((f)object).bd;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aB;
        string2 = ((f)object).aB;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aD;
        string2 = ((f)object).aD;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aC;
        string2 = ((f)object).aC;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bg;
        string2 = ((f)object).bg;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bh;
        string2 = ((f)object).bh;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bn;
        string2 = ((f)object).bn;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bo;
        string2 = ((f)object).bo;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bp;
        string2 = ((f)object).bp;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aV;
        string2 = ((f)object).aV;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bs;
        string2 = ((f)object).bs;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bt;
        object = ((f)object).bt;
        return !(string == null ? object != null : !string.equals(object));
    }

    public final int hashCode() {
        long l = this.be;
        int n = 59 + (int)(l >>> 32 ^ l);
        long l2 = this.bf;
        n = n * 59 + (int)(l2 >>> 32 ^ l2);
        long l3 = this.bi;
        n = n * 59 + (int)(l3 >>> 32 ^ l3);
        long l4 = this.bj;
        n = (((n * 59 + (int)(l4 >>> 32 ^ l4)) * 59 + (this.bk ? 79 : 97)) * 59 + this.bl) * 59 + this.bm;
        long l5 = this.bq;
        n = n * 59 + (int)(l5 >>> 32 ^ l5);
        long l6 = this.br;
        n = n * 59 + (int)(l6 >>> 32 ^ l6);
        long l7 = Double.doubleToLongBits(this.bu);
        n = n * 59 + (int)(l7 >>> 32 ^ l7);
        long l8 = Double.doubleToLongBits(this.bv);
        n = n * 59 + (int)(l8 >>> 32 ^ l8);
        long l9 = this.bw;
        n = n * 59 + (int)(l9 >>> 32 ^ l9);
        long l10 = Double.doubleToLongBits(this.bx);
        n = n * 59 + (int)(l10 >>> 32 ^ l10);
        long l11 = this.by;
        n = n * 59 + (int)(l11 >>> 32 ^ l11);
        long l12 = this.bz;
        n = n * 59 + (int)(l12 >>> 32 ^ l12);
        long l13 = this.bA;
        n = n * 59 + (int)(l13 >>> 32 ^ l13);
        String string = this.bd;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.aB;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.aD;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.aC;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bg;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bh;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bn;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bo;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bp;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.aV;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bs;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bt;
        return n * 59 + (string == null ? 43 : string.hashCode());
    }

    public final String toString() {
        return "ParkingSession(sessionId=" + this.bd + ", plateId=" + this.aB + ", parkingLotId=" + this.aD + ", gateId=" + this.aC + ", inTime=" + this.be + ", outTime=" + this.bf + ", spaceId=" + this.bg + ", accountId=" + this.bh + ", lockedTime=" + this.bi + ", exitStartTime=" + this.bj + ", released=" + this.bk + ", entryQueueOrder=" + this.bl + ", exitQueueOrder=" + this.bm + ", exitGateId=" + this.bn + ", entryGateMode=" + this.bo + ", exitGateMode=" + this.bp + ", occupyTime=" + this.bq + ", releaseTime=" + this.br + ", vehicleType=" + this.aV + ", occupiedSpaceType=" + this.bs + ", paymentStatus=" + this.bt + ", paidAmount=" + this.bu + ", balancePaidAmount=" + this.bv + ", paymentTime=" + this.bw + ", bonusBalanceAtEntry=" + this.bx + ", zombieMarkedAt=" + this.by + ", zombieAccumulatedSeconds=" + this.bz + ", zombieTimerStart=" + this.bA + ")";
    }
}

