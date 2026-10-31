/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.a;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class e {
    public String bj;
    public int bE;
    public int bF;
    public int bG;
    public Map<String, AtomicInteger> bH;
    public Map<String, AtomicInteger> bI;
    public Map<String, AtomicInteger> bJ;

    public final boolean equals(Object map) {
        if (map == this) {
            return true;
        }
        if (!(map instanceof e)) {
            return false;
        }
        map = (e)((Object)map);
        if (this.bE != ((e)((Object)map)).bE) {
            return false;
        }
        if (this.bF != ((e)((Object)map)).bF) {
            return false;
        }
        if (this.bG != ((e)((Object)map)).bG) {
            return false;
        }
        Map<String, AtomicInteger> map2 = this.bj;
        Map<String, AtomicInteger> map3 = ((e)((Object)map)).bj;
        if (map2 == null ? map3 != null : !((Object)map2).equals(map3)) {
            return false;
        }
        map2 = this.bH;
        map3 = ((e)((Object)map)).bH;
        if (map2 == null ? map3 != null : !((Object)map2).equals(map3)) {
            return false;
        }
        map2 = this.bI;
        map3 = ((e)((Object)map)).bI;
        if (map2 == null ? map3 != null : !((Object)map2).equals(map3)) {
            return false;
        }
        map2 = this.bJ;
        map = ((e)((Object)map)).bJ;
        return !(map2 == null ? map != null : !((Object)map2).equals(map));
    }

    public final int hashCode() {
        int n = ((59 + this.bE) * 59 + this.bF) * 59 + this.bG;
        Object object = this.bj;
        n = n * 59 + (object == null ? 43 : object.hashCode());
        object = this.bH;
        n = n * 59 + (object == null ? 43 : object.hashCode());
        object = this.bI;
        n = n * 59 + (object == null ? 43 : object.hashCode());
        object = this.bJ;
        return n * 59 + (object == null ? 43 : object.hashCode());
    }

    public final String toString() {
        return "ParkingLot(parkingLotId=" + this.bj + ", largeCapacity=" + this.bE + ", mediumCapacity=" + this.bF + ", smallCapacity=" + this.bG + ", usedCountMap=" + this.bH + ", lockedCountMap=" + this.bI + ", reservedCountMap=" + this.bJ + ")";
    }
}

