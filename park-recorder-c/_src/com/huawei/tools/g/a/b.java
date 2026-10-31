/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

public final class b {
    public String bi;
    public String bj;
    public String bk;
    public String bl;
    public String bm;
    public String bn;
    public long bo;
    public String bp;

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof b)) {
            return false;
        }
        object = (b)object;
        if (this.bo != ((b)object).bo) {
            return false;
        }
        String string = this.bi;
        String string2 = ((b)object).bi;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bj;
        string2 = ((b)object).bj;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bk;
        string2 = ((b)object).bk;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bl;
        string2 = ((b)object).bl;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bm;
        string2 = ((b)object).bm;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bn;
        string2 = ((b)object).bn;
        if (string == null ? string2 != null : !string.equals(string2)) {
            return false;
        }
        string = this.bp;
        object = ((b)object).bp;
        return !(string == null ? object != null : !string.equals(object));
    }

    public final int hashCode() {
        long l = this.bo;
        int n = 59 + (int)(l >>> 32 ^ l);
        String string = this.bi;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bj;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bk;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bl;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bm;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bn;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.bp;
        return n * 59 + (string == null ? 43 : string.hashCode());
    }

    public final String toString() {
        return "Gate(gateId=" + this.bi + ", parkingLotId=" + this.bj + ", mode=" + this.bk + ", configMode=" + this.bl + ", preTidalMode=" + this.bm + ", preTidalConfigMode=" + this.bn + ", lastAccessTime=" + this.bo + ", lastProcessedMode=" + this.bp + ")";
    }
}

