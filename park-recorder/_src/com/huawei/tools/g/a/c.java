/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

import java.util.LinkedHashSet;
import java.util.Set;

public final class c {
    private String aK;
    public String aL;
    public String password;
    public Set<String> aM;
    public String aN;
    public String aO;
    public long aP;
    public long aQ;
    public long aR;
    public double aS;
    public double aT;

    public c(String string, String string2, String string3) {
        this.aK = string;
        this.aL = string2;
        this.password = string3;
        this.aM = new LinkedHashSet<String>();
        this.aN = "inactive";
        this.aO = null;
        this.aP = 0L;
        this.aQ = 0L;
        this.aR = 0L;
        this.aS = 0.0;
        this.aT = 0.0;
    }

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof c)) {
            return false;
        }
        object = (c)object;
        if (this.aP != ((c)object).aP) {
            return false;
        }
        if (this.aQ != ((c)object).aQ) {
            return false;
        }
        if (this.aR != ((c)object).aR) {
            return false;
        }
        if (Double.compare(this.aS, ((c)object).aS) != 0) {
            return false;
        }
        if (Double.compare(this.aT, ((c)object).aT) != 0) {
            return false;
        }
        Object object2 = this.aK;
        Object object3 = ((c)object).aK;
        if (object2 == null ? object3 != null : !object2.equals(object3)) {
            return false;
        }
        object2 = this.aL;
        object3 = ((c)object).aL;
        if (object2 == null ? object3 != null : !object2.equals(object3)) {
            return false;
        }
        object2 = this.password;
        object3 = ((c)object).password;
        if (object2 == null ? object3 != null : !object2.equals(object3)) {
            return false;
        }
        object2 = this.aM;
        object3 = ((c)object).aM;
        if (object2 == null ? object3 != null : !object2.equals(object3)) {
            return false;
        }
        object2 = this.aN;
        object3 = ((c)object).aN;
        if (object2 == null ? object3 != null : !object2.equals(object3)) {
            return false;
        }
        object2 = this.aO;
        object = ((c)object).aO;
        return !(object2 == null ? object != null : !object2.equals(object));
    }

    public final int hashCode() {
        long l = this.aP;
        int n = 59 + (int)(l >>> 32 ^ l);
        long l2 = this.aQ;
        n = n * 59 + (int)(l2 >>> 32 ^ l2);
        long l3 = this.aR;
        n = n * 59 + (int)(l3 >>> 32 ^ l3);
        long l4 = Double.doubleToLongBits(this.aS);
        n = n * 59 + (int)(l4 >>> 32 ^ l4);
        long l5 = Double.doubleToLongBits(this.aT);
        n = n * 59 + (int)(l5 >>> 32 ^ l5);
        Object object = this.aK;
        n = n * 59 + (object == null ? 43 : object.hashCode());
        object = this.aL;
        n = n * 59 + (object == null ? 43 : object.hashCode());
        object = this.password;
        n = n * 59 + (object == null ? 43 : object.hashCode());
        object = this.aM;
        n = n * 59 + (object == null ? 43 : object.hashCode());
        object = this.aN;
        n = n * 59 + (object == null ? 43 : object.hashCode());
        object = this.aO;
        return n * 59 + (object == null ? 43 : object.hashCode());
    }

    public final String toString() {
        return "MemberAccount(memberId=" + this.aK + ", phone=" + this.aL + ", password=" + this.password + ", plateIds=" + this.aM + ", status=" + this.aN + ", verifyCode=" + this.aO + ", verifyCodeExpiry=" + this.aP + ", createTime=" + this.aQ + ", expiredTime=" + this.aR + ", balance=" + this.aS + ", bonusBalance=" + this.aT + ")";
    }

    public c() {
    }
}

