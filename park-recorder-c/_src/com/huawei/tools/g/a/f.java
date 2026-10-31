/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

public final class f {
    public String bK;
    public String bh;
    public String bj;
    public String bi;
    public long bL;
    public long bM;
    public String bN;
    public String bO;
    public long bP;
    public long bQ;
    public boolean bR;
    public int bS;
    public int bT;
    public String bU;
    public String bV;
    public String bW;
    public long bX;
    public long bY;
    public String bB;
    public String bZ;
    public String ca;
    public double cb;
    public double cd;
    public long ce;
    public double cf;
    public long cg;
    public long ci;
    public long cj;
    public String ck;

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof f)) {
            return false;
        }
        object = (f)object;
        if (this.bL != ((f)object).bL) {
            return false;
        }
        if (this.bM != ((f)object).bM) {
            return false;
        }
        if (this.bP != ((f)object).bP) {
            return false;
        }
        if (this.bQ != ((f)object).bQ) {
            return false;
        }
        if (this.bR != ((f)object).bR) {
            return false;
        }
        if (this.bS != ((f)object).bS) {
            return false;
        }
        if (this.bT != ((f)object).bT) {
            return false;
        }
        if (this.bX != ((f)object).bX) {
            return false;
        }
        if (this.bY != ((f)object).bY) {
            return false;
        }
        if (Double.compare(this.cb, ((f)object).cb) != 0) {
            return false;
        }
        if (Double.compare(this.cd, ((f)object).cd) != 0) {
            return false;
        }
        if (this.ce != ((f)object).ce) {
            return false;
        }
        if (Double.compare(this.cf, ((f)object).cf) != 0) {
            return false;
        }
        if (this.cg != ((f)object).cg) {
            return false;
        }
        if (this.ci != ((f)object).ci) {
            return false;
        }
        if (this.cj != ((f)object).cj) {
            return false;
        }
        String string = this.bK;
        String string2 = ((f)object).bK;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bh;
        string2 = ((f)object).bh;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bj;
        string2 = ((f)object).bj;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bi;
        string2 = ((f)object).bi;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bN;
        string2 = ((f)object).bN;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bO;
        string2 = ((f)object).bO;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bU;
        string2 = ((f)object).bU;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bV;
        string2 = ((f)object).bV;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bW;
        string2 = ((f)object).bW;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bB;
        string2 = ((f)object).bB;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bZ;
        string2 = ((f)object).bZ;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.ca;
        string2 = ((f)object).ca;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.ck;
        object = ((f)object).ck;
        return !(string == null ? object != null : !string.equals(object));
    }

    public final int hashCode() {
        long l = this.bL;
        int n = 59 + (int)(l >>> 32 ^ l);
        long l2 = this.bM;
        n = n * 59 + (int)(l2 >>> 32 ^ l2);
        long l3 = this.bP;
        n = n * 59 + (int)(l3 >>> 32 ^ l3);
        long l4 = this.bQ;
        n = (((n * 59 + (int)(l4 >>> 32 ^ l4)) * 59 + (this.bR ? 79 : 97)) * 59 + this.bS) * 59 + this.bT;
        long l5 = this.bX;
        n = n * 59 + (int)(l5 >>> 32 ^ l5);
        long l6 = this.bY;
        n = n * 59 + (int)(l6 >>> 32 ^ l6);
        long l7 = Double.doubleToLongBits(this.cb);
        n = n * 59 + (int)(l7 >>> 32 ^ l7);
        long l8 = Double.doubleToLongBits(this.cd);
        n = n * 59 + (int)(l8 >>> 32 ^ l8);
        long l9 = this.ce;
        n = n * 59 + (int)(l9 >>> 32 ^ l9);
        long l10 = Double.doubleToLongBits(this.cf);
        n = n * 59 + (int)(l10 >>> 32 ^ l10);
        long l11 = this.cg;
        n = n * 59 + (int)(l11 >>> 32 ^ l11);
        long l12 = this.ci;
        n = n * 59 + (int)(l12 >>> 32 ^ l12);
        long l13 = this.cj;
        n = n * 59 + (int)(l13 >>> 32 ^ l13);
        String string = this.bK;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bh;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bj;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bi;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bN;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bO;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bU;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bV;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bW;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bB;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bZ;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.ca;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.ck;
        return n * 59 + (string == null ? 43 : string.hashCode());
    }

    public final String toString() {
        return "ParkingSession(sessionId=" + this.bK + ", plateId=" + this.bh + ", parkingLotId=" + this.bj + ", gateId=" + this.bi + ", inTime=" + this.bL + ", outTime=" + this.bM + ", spaceId=" + this.bN + ", accountId=" + this.bO + ", lockedTime=" + this.bP + ", exitStartTime=" + this.bQ + ", released=" + this.bR + ", entryQueueOrder=" + this.bS + ", exitQueueOrder=" + this.bT + ", exitGateId=" + this.bU + ", entryGateMode=" + this.bV + ", exitGateMode=" + this.bW + ", occupyTime=" + this.bX + ", releaseTime=" + this.bY + ", vehicleType=" + this.bB + ", occupiedSpaceType=" + this.bZ + ", paymentStatus=" + this.ca + ", paidAmount=" + this.cb + ", balancePaidAmount=" + this.cd + ", paymentTime=" + this.ce + ", bonusBalanceAtEntry=" + this.cf + ", zombieMarkedAt=" + this.cg + ", zombieAccumulatedSeconds=" + this.ci + ", zombieTimerStart=" + this.cj + ", reservationId=" + this.ck + ")";
    }
}

