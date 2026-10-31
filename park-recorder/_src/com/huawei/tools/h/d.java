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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class d {
    public static long cb;
    public static final Map<String, c> cd;
    public static final Map<String, String> ce;
    public static final Map<String, i> cf;
    public static final Map<String, f> cg;
    public static final Map<String, e> ci;
    public static final Map<String, g> cj;
    public static final Set<String> ck;
    public static final Map<String, b> cl;
    public static final Map<String, Long> cm;
    public static AtomicInteger cn;
    public static final Map<String, a> co;
    public static final Map<String, com.huawei.tools.g.a.d> cp;
    public static final Map<String, List<com.huawei.tools.g.a.d>> cq;
    public static final Map<String, Integer> cr;
    public static final Map<String, h> cs;

    public static void clear() {
        cb = 0L;
        cd.clear();
        ce.clear();
        cf.clear();
        cg.clear();
        ck.clear();
        ci.clear();
        cj.clear();
        cl.clear();
        cm.clear();
        cn.set(0);
        co.clear();
        cp.clear();
        cq.clear();
        cr.clear();
        cs.clear();
    }

    static {
        cd = new ConcurrentHashMap<String, c>();
        ce = new ConcurrentHashMap<String, String>();
        cf = new ConcurrentHashMap<String, i>();
        cg = new ConcurrentHashMap<String, f>();
        ci = new ConcurrentHashMap<String, e>();
        cj = new ConcurrentHashMap<String, g>();
        ck = ConcurrentHashMap.newKeySet();
        cl = new ConcurrentHashMap<String, b>();
        cm = new ConcurrentHashMap<String, Long>();
        cn = new AtomicInteger(0);
        co = new ConcurrentHashMap<String, a>();
        cp = new ConcurrentHashMap<String, com.huawei.tools.g.a.d>();
        cq = new ConcurrentHashMap<String, List<com.huawei.tools.g.a.d>>();
        cr = new ConcurrentHashMap<String, Integer>();
        cs = new ConcurrentHashMap<String, h>();
    }
}

