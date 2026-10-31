/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.h;

import com.huawei.tools.g.a.a;
import com.huawei.tools.g.a.b;
import com.huawei.tools.g.a.c;
import com.huawei.tools.g.a.e;
import com.huawei.tools.g.a.f;
import com.huawei.tools.g.a.g;
import com.huawei.tools.g.a.h;
import com.huawei.tools.g.a.i;
import com.huawei.tools.g.a.j;
import com.huawei.tools.g.a.k;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class d {
    public static long de;
    public static final Map<String, c> df;
    public static final Map<String, String> dg;
    public static final Map<String, j> dh;
    public static final Map<String, f> di;
    public static final Map<String, e> dj;
    public static final Map<String, g> dk;
    public static final Map<String, g> dl;
    public static final Set<String> dm;
    public static final Map<String, b> dn;
    public static final Map<String, Long> do;
    public static AtomicInteger dp;
    public static final Map<String, a> dq;
    public static final Map<String, com.huawei.tools.g.a.d> dr;
    public static final Map<String, List<com.huawei.tools.g.a.d>> ds;
    public static final Map<String, Integer> dt;
    public static final Map<String, i> du;
    public static final Map<String, h> dv;
    public static final Map<String, List<String>> dw;
    public static final Map<String, k> dx;
    public static AtomicInteger dy;
    public static AtomicInteger dz;
    public static AtomicInteger dA;
    public static int dB;
    public static AtomicLong dC;

    public static void clear() {
        de = 0L;
        df.clear();
        dg.clear();
        dh.clear();
        di.clear();
        dm.clear();
        dj.clear();
        dk.clear();
        dl.clear();
        dn.clear();
        do.clear();
        dp.set(0);
        dq.clear();
        dr.clear();
        ds.clear();
        dt.clear();
        du.clear();
        dv.clear();
        dw.clear();
        dx.clear();
        dy.set(0);
        dz.set(0);
        dA.set(0);
        dB = 0;
        dC.set(0L);
    }

    static {
        df = new ConcurrentHashMap<String, c>();
        dg = new ConcurrentHashMap<String, String>();
        dh = new ConcurrentHashMap<String, j>();
        di = new ConcurrentHashMap<String, f>();
        dj = new ConcurrentHashMap<String, e>();
        dk = new ConcurrentHashMap<String, g>();
        dl = new ConcurrentHashMap<String, g>();
        dm = ConcurrentHashMap.newKeySet();
        dn = new ConcurrentHashMap<String, b>();
        do = new ConcurrentHashMap<String, Long>();
        dp = new AtomicInteger(0);
        dq = new ConcurrentHashMap<String, a>();
        dr = new ConcurrentHashMap<String, com.huawei.tools.g.a.d>();
        ds = new ConcurrentHashMap<String, List<com.huawei.tools.g.a.d>>();
        dt = new ConcurrentHashMap<String, Integer>();
        du = new ConcurrentHashMap<String, i>();
        dv = new ConcurrentHashMap<String, h>();
        dw = new ConcurrentHashMap<String, List<String>>();
        dx = new ConcurrentHashMap<String, k>();
        dy = new AtomicInteger(0);
        dz = new AtomicInteger(0);
        dA = new AtomicInteger(0);
        dB = 0;
        dC = new AtomicLong(0L);
    }
}

