/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class e {
    public String aD;
    public int aY;
    public int aZ;
    public int ba;
    public Map<String, AtomicInteger> bb;
    public Map<String, AtomicInteger> bc;

    public final boolean equals(Object map) {
        if (map == this) {
            return true;
        }
        if (!(map instanceof e)) {
            return false;
        }
        map = (e)((Object)map);
        if (this.aY != ((e)((Object)map)).aY) {
            return false;
        }
        if (this.aZ != ((e)((Object)map)).aZ) {
            return false;
        }
        if (this.ba != ((e)((Object)map)).ba) {
            return false;
        }
        Map<String, AtomicInteger> map2 = this.aD;
        Object object = ((e)((Object)map)).aD;
        if (map2 == null ? object != null : !((Object)map2).equals(object)) {
            return false;
        }
        map2 = this.bb;
        object = ((e)((Object)map)).bb;
        if (map2 == null ? object != null : !((Object)map2).equals(object)) {
            return false;
        }
        map2 = this.bc;
        map = ((e)((Object)map)).bc;
        return !(map2 == null ? map != null : !((Object)map2).equals(map));
    }

    public final int hashCode() {
        int n = ((59 + this.aY) * 59 + this.aZ) * 59 + this.ba;
        Object object = this.aD;
        n = n * 59 + (object == null ? 43 : object.hashCode());
        object = this.bb;
        n = n * 59 + (object == null ? 43 : object.hashCode());
        object = this.bc;
        return n * 59 + (object == null ? 43 : object.hashCode());
    }

    public final String toString() {
        return "ParkingLot(parkingLotId=" + this.aD + ", largeCapacity=" + this.aY + ", mediumCapacity=" + this.aZ + ", smallCapacity=" + this.ba + ", usedCountMap=" + this.bb + ", lockedCountMap=" + this.bc + ")";
    }
}

