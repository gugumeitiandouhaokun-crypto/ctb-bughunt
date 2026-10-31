/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

public final class h {
    private String bC;
    public double bD;
    public double bE;
    public double bF;
    public double bG;

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof h)) {
            return false;
        }
        object = (h)object;
        if (Double.compare(this.bD, ((h)object).bD) != 0) {
            return false;
        }
        if (Double.compare(this.bE, ((h)object).bE) != 0) {
            return false;
        }
        if (Double.compare(this.bF, ((h)object).bF) != 0) {
            return false;
        }
        if (Double.compare(this.bG, ((h)object).bG) != 0) {
            return false;
        }
        String string = this.bC;
        object = ((h)object).bC;
        return !(string == null ? object != null : !string.equals(object));
    }

    public final int hashCode() {
        long l = Double.doubleToLongBits(this.bD);
        int n = 59 + (int)(l >>> 32 ^ l);
        long l2 = Double.doubleToLongBits(this.bE);
        n = n * 59 + (int)(l2 >>> 32 ^ l2);
        long l3 = Double.doubleToLongBits(this.bF);
        n = n * 59 + (int)(l3 >>> 32 ^ l3);
        long l4 = Double.doubleToLongBits(this.bG);
        n = n * 59 + (int)(l4 >>> 32 ^ l4);
        String string = this.bC;
        return n * 59 + (string == null ? 43 : string.hashCode());
    }

    public final String toString() {
        return "RevenueRecord(dateStr=" + this.bC + ", tempIncome=" + this.bD + ", rechargeIncome=" + this.bE + ", moncardIncome=" + this.bF + ", balanceIncome=" + this.bG + ")";
    }

    public h() {
    }

    public h(String string) {
        this.bC = string;
        this.bD = 0.0;
        this.bE = 0.0;
        this.bF = 0.0;
        this.bG = 0.0;
    }
}

