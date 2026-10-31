/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

public final class i {
    private String cm;
    public double cn;
    public double co;
    public double cp;
    public double cq;

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof i)) {
            return false;
        }
        object = (i)object;
        if (Double.compare(this.cn, ((i)object).cn) != 0) {
            return false;
        }
        if (Double.compare(this.co, ((i)object).co) != 0) {
            return false;
        }
        if (Double.compare(this.cp, ((i)object).cp) != 0) {
            return false;
        }
        if (Double.compare(this.cq, ((i)object).cq) != 0) {
            return false;
        }
        String string = this.cm;
        object = ((i)object).cm;
        return !(string == null ? object != null : !string.equals(object));
    }

    public final int hashCode() {
        long l = Double.doubleToLongBits(this.cn);
        int n = 59 + (int)(l >>> 32 ^ l);
        long l2 = Double.doubleToLongBits(this.co);
        n = n * 59 + (int)(l2 >>> 32 ^ l2);
        long l3 = Double.doubleToLongBits(this.cp);
        n = n * 59 + (int)(l3 >>> 32 ^ l3);
        long l4 = Double.doubleToLongBits(this.cq);
        n = n * 59 + (int)(l4 >>> 32 ^ l4);
        String string = this.cm;
        return n * 59 + (string == null ? 43 : string.hashCode());
    }

    public final String toString() {
        return "RevenueRecord(dateStr=" + this.cm + ", tempIncome=" + this.cn + ", rechargeIncome=" + this.co + ", moncardIncome=" + this.cp + ", balanceIncome=" + this.cq + ")";
    }

    public i() {
    }

    public i(String string) {
        this.cm = string;
        this.cn = 0.0;
        this.co = 0.0;
        this.cp = 0.0;
        this.cq = 0.0;
    }
}

