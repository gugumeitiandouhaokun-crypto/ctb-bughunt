/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

import java.util.LinkedHashSet;
import java.util.Set;

public final class c {
    private String bq;
    public String br;
    public String password;
    public Set<String> bs;
    public String bt;
    public String bu;
    public long bv;
    public long bw;
    public long bx;
    public double by;
    public double bz;

    public c(String string, String string2, String string3) {
        this.bq = string;
        this.br = string2;
        this.password = string3;
        this.bs = new LinkedHashSet<String>();
        this.bt = "inactive";
        this.bu = null;
        this.bv = 0L;
        this.bw = 0L;
        this.bx = 0L;
        this.by = 0.0;
        this.bz = 0.0;
    }

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof c)) {
            return false;
        }
        object = (c)object;
        if (this.bv != ((c)object).bv) {
            return false;
        }
        if (this.bw != ((c)object).bw) {
            return false;
        }
        if (this.bx != ((c)object).bx) {
            return false;
        }
        if (Double.compare(this.by, ((c)object).by) != 0) {
            return false;
        }
        if (Double.compare(this.bz, ((c)object).bz) != 0) {
            return false;
        }
        Object object2 = this.bq;
        Object object3 = ((c)object).bq;
        if (object2 == null ? object3 != null : !object2.equals(object3)) {
            return false;
        }
        object2 = this.br;
        object3 = ((c)object).br;
        if (object2 == null ? object3 != null : !object2.equals(object3)) {
            return false;
        }
        object2 = this.password;
        object3 = ((c)object).password;
        if (object2 == null ? object3 != null : !object2.equals(object3)) {
            return false;
        }
        object2 = this.bs;
        object3 = ((c)object).bs;
        if (object2 == null ? object3 != null : !object2.equals(object3)) {
            return false;
        }
        object2 = this.bt;
        object3 = ((c)object).bt;
        if (object2 == null ? object3 != null : !object2.equals(object3)) {
            return false;
        }
        object2 = this.bu;
        object = ((c)object).bu;
        return !(object2 == null ? object != null : !object2.equals(object));
    }

    public final int hashCode() {
        long l = this.bv;
        int n = 59 + (int)(l >>> 32 ^ l);
        long l2 = this.bw;
        n = n * 59 + (int)(l2 >>> 32 ^ l2);
        long l3 = this.bx;
        n = n * 59 + (int)(l3 >>> 32 ^ l3);
        long l4 = Double.doubleToLongBits(this.by);
        n = n * 59 + (int)(l4 >>> 32 ^ l4);
        long l5 = Double.doubleToLongBits(this.bz);
        n = n * 59 + (int)(l5 >>> 32 ^ l5);
        Object object = this.bq;
        n = n * 59 + (object == null ? 43 : object.hashCode());
        object = this.br;
        n = n * 59 + (object == null ? 43 : object.hashCode());
        object = this.password;
        n = n * 59 + (object == null ? 43 : object.hashCode());
        object = this.bs;
        n = n * 59 + (object == null ? 43 : object.hashCode());
        object = this.bt;
        n = n * 59 + (object == null ? 43 : object.hashCode());
        object = this.bu;
        return n * 59 + (object == null ? 43 : object.hashCode());
    }

    public final String toString() {
        return "MemberAccount(memberId=" + this.bq + ", phone=" + this.br + ", password=" + this.password + ", plateIds=" + this.bs + ", status=" + this.bt + ", verifyCode=" + this.bu + ", verifyCodeExpiry=" + this.bv + ", createTime=" + this.bw + ", expiredTime=" + this.bx + ", balance=" + this.by + ", bonusBalance=" + this.bz + ")";
    }

    public c() {
    }
}

