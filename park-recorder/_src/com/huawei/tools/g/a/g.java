/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

public final class g {
    public String bg;
    public String aD;
    public String bB;
    public String aN;
    public String aB;

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof g)) {
            return false;
        }
        object = (g)object;
        String string = this.bg;
        String string2 = ((g)object).bg;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aD;
        string2 = ((g)object).aD;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bB;
        string2 = ((g)object).bB;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aN;
        string2 = ((g)object).aN;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aB;
        object = ((g)object).aB;
        return !(string == null ? object != null : !string.equals(object));
    }

    public final int hashCode() {
        String string = this.bg;
        int n = 59 + (string == null ? 43 : string.hashCode());
        String string2 = this.aD;
        n = n * 59 + (string2 == null ? 43 : string2.hashCode());
        string2 = this.bB;
        n = n * 59 + (string2 == null ? 43 : string2.hashCode());
        string2 = this.aN;
        n = n * 59 + (string2 == null ? 43 : string2.hashCode());
        string2 = this.aB;
        return n * 59 + (string2 == null ? 43 : string2.hashCode());
    }

    public final String toString() {
        return "ParkingSpace(spaceId=" + this.bg + ", parkingLotId=" + this.aD + ", spaceType=" + this.bB + ", status=" + this.aN + ", plateId=" + this.aB + ")";
    }
}

