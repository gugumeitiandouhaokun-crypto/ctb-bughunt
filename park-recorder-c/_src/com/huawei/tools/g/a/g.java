/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

public final class g {
    public String bN;
    public String bj;
    public String cl;
    public String bt;
    public String bh;

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof g)) {
            return false;
        }
        object = (g)object;
        String string = this.bN;
        String string2 = ((g)object).bN;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bj;
        string2 = ((g)object).bj;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.cl;
        string2 = ((g)object).cl;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bt;
        string2 = ((g)object).bt;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bh;
        object = ((g)object).bh;
        return !(string == null ? object != null : !string.equals(object));
    }

    public final int hashCode() {
        String string = this.bN;
        int n = 59 + (string == null ? 43 : string.hashCode());
        String string2 = this.bj;
        n = n * 59 + (string2 == null ? 43 : string2.hashCode());
        string2 = this.cl;
        n = n * 59 + (string2 == null ? 43 : string2.hashCode());
        string2 = this.bt;
        n = n * 59 + (string2 == null ? 43 : string2.hashCode());
        string2 = this.bh;
        return n * 59 + (string2 == null ? 43 : string2.hashCode());
    }

    public final String toString() {
        return "ParkingSpace(spaceId=" + this.bN + ", parkingLotId=" + this.bj + ", spaceType=" + this.cl + ", status=" + this.bt + ", plateId=" + this.bh + ")";
    }
}

