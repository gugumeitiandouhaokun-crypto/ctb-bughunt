/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.b;

public final class a
extends Exception {
    private String code;
    private String aS;

    public a() {
    }

    public a(String string, String string2) {
        this.code = string;
        this.aS = com.huawei.tools.h.a.aj(string2);
        com.huawei.tools.d.a.trace(String.format("%s", this.aS));
    }

    @Override
    public final String toString() {
        return "BusinessException(code=" + this.code + ", msg=" + this.aS + ")";
    }

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof a)) {
            return false;
        }
        Object object2 = (a)object;
        if (!super.equals(object)) {
            return false;
        }
        object = this.code;
        String string = ((a)object2).code;
        if (object == null ? string != null : !object.equals(string)) {
            return false;
        }
        object = this.aS;
        object2 = ((a)object2).aS;
        return !(object == null ? object2 != null : !object.equals(object2));
    }

    public final int hashCode() {
        int n = super.hashCode();
        String string = this.code;
        n = n * 59 + (string == null ? 43 : string.hashCode());
        string = this.aS;
        return n * 59 + (string == null ? 43 : string.hashCode());
    }
}

