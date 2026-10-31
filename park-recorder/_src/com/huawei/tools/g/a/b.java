/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

public final class b {
    public String aC;
    public String aD;
    public String aE;
    public String aF;
    public String aG;
    public String aH;
    public long aI;
    public String aJ;

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof b)) {
            return false;
        }
        object = (b)object;
        if (this.aI != ((b)object).aI) {
            return false;
        }
        String string = this.aC;
        String string2 = ((b)object).aC;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aD;
        string2 = ((b)object).aD;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aE;
        string2 = ((b)object).aE;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aF;
        string2 = ((b)object).aF;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aG;
        string2 = ((b)object).aG;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aH;
        string2 = ((b)object).aH;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.aJ;
        object = ((b)object).aJ;
        return !(string == null ? object != null : !string.equals(object));
    }

    public final int hashCode() {
        long l = this.aI;
        int n = 59 + (int)(l >>> 32 ^ l);
        String string = this.aC;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.aD;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.aE;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.aF;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.aG;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.aH;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.aJ;
        return n * 59 + (string == null ? 43 : string.hashCode());
    }

    public final String toString() {
        return "Gate(gateId=" + this.aC + ", parkingLotId=" + this.aD + ", mode=" + this.aE + ", configMode=" + this.aF + ", preTidalMode=" + this.aG + ", preTidalConfigMode=" + this.aH + ", lastAccessTime=" + this.aI + ", lastProcessedMode=" + this.aJ + ")";
    }
}

