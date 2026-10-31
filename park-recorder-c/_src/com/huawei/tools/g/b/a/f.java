/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.b.a;

import com.huawei.tools.g.a.j;
import com.huawei.tools.g.a.k;
import com.huawei.tools.g.b.a;
import com.huawei.tools.g.b.a.b;
import com.huawei.tools.g.b.a.c;
import com.huawei.tools.g.b.a.d;
import com.huawei.tools.g.b.a.e;
import com.huawei.tools.g.b.a.g;
import com.huawei.tools.g.b.a.h;
import com.huawei.tools.g.b.a.i;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;

public final class f
implements a {
    private final b cL;
    private final e cM;
    private final d cN;
    private final com.huawei.tools.g.b.a.a cG;
    private final c cI;
    private final h cB = new h();
    private final i cH;

    public f() {
        this.cL = new b(this.cB);
        this.cN = new d();
        this.cG = new com.huawei.tools.g.b.a.a();
        this.cI = new c(this.cG);
        this.cI.cH = this.cH = new i();
        this.cM = new e(this.cI, this.cB);
        this.cB.cR = () -> e.l();
        this.cM.cJ = () -> {
            i.t();
            long l = com.huawei.tools.h.d.de;
            HashSet<String> hashSet = new HashSet<String>();
            for (k k2 : com.huawei.tools.h.d.dx.values()) {
                String string;
                if (!"queuing".equals(k2.bt) || hashSet.contains(string = k2.bN)) continue;
                hashSet.add(string);
                i.c(string, l);
            }
            i.d(l);
            i.e(l);
        };
    }

    @Override
    public final String c() {
        com.huawei.tools.h.d.clear();
        e.g();
        com.huawei.tools.e.a.info(com.huawei.tools.a.a.g.key);
        return "E000:Success";
    }

    @Override
    public final String a(String string, int n) {
        string = this.cM.a(string, n);
        b.f();
        return string;
    }

    @Override
    public final String b(String string, String string2) {
        return b.b(string, string2);
    }

    @Override
    public final String c(String string, String string2) {
        return b.c(string, string2);
    }

    @Override
    public final String d(String object, String object2) {
        String string2 = object2;
        object2 = object;
        if ((object = com.huawei.tools.h.d.df.get(object2)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if ("cancelled".equals(((com.huawei.tools.g.a.c)object).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if ("expired".equals(((com.huawei.tools.g.a.c)object).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ac.key);
        }
        if (!"active".equals(((com.huawei.tools.g.a.c)object).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (!string2.equals(((com.huawei.tools.g.a.c)object).password)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.l.key);
        }
        object2 = ((com.huawei.tools.g.a.c)object).bs.iterator();
        while (object2.hasNext()) {
            if (!g.F((String)object2.next())) continue;
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.W.key);
        }
        object2 = ((com.huawei.tools.g.a.c)object).bs.iterator();
        while (object2.hasNext()) {
            if (!h.T(object2.next())) continue;
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.az.key);
        }
        if (((com.huawei.tools.g.a.c)object).by != 0.0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.z.key);
        }
        object2 = ((com.huawei.tools.g.a.c)object).bs.iterator();
        while (object2.hasNext()) {
            if (g.P(object2.next()) <= 0) continue;
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.A.key);
        }
        ((com.huawei.tools.g.a.c)object).bt = "cancelled";
        for (String string2 : ((com.huawei.tools.g.a.c)object).bs) {
            com.huawei.tools.h.d.dg.remove(string2);
            com.huawei.tools.h.d.do.put(string2, com.huawei.tools.h.d.de);
        }
        ((com.huawei.tools.g.a.c)object).bs.clear();
        return "E000:Success";
    }

    @Override
    public final String e(String string, String string2) {
        return b.e(string, string2);
    }

    @Override
    public final String f(String object, String object2) {
        String string = object2;
        object2 = object;
        if ((object = com.huawei.tools.h.d.df.get(object2)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(((com.huawei.tools.g.a.c)object).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (!((com.huawei.tools.g.a.c)object).bs.contains(string)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.v.key);
        }
        if (g.F(string)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.w.key);
        }
        if (h.T(string)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ay.key);
        }
        if (g.P(string) > 0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.x.key);
        }
        ((com.huawei.tools.g.a.c)object).bs.remove(string);
        com.huawei.tools.h.d.dg.remove(string);
        com.huawei.tools.h.d.do.put(string, com.huawei.tools.h.d.de);
        return "E000:Success";
    }

    @Override
    public final String c(String string) {
        return b.c(string);
    }

    @Override
    public final String d(String string) {
        return b.d(string);
    }

    @Override
    public final String g(String object, String string) {
        long l;
        String string2;
        Object object2 = string;
        string = object;
        g.G(string);
        object = com.huawei.tools.h.d.dn.get(object2);
        if (object == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.K.key);
        }
        if (g.F(string)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.X.key);
        }
        boolean bl = com.huawei.tools.h.d.dg.containsKey(string);
        if (!bl && g.p() >= 500) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.F.key);
        }
        Object object3 = ThreadLocalRandom.current();
        while (!com.huawei.tools.h.d.dm.add(string2 = "p" + String.format("%016d", l = Math.abs(((Random)object3).nextLong()) % 10000000000000000L))) {
        }
        long l2 = com.huawei.tools.h.d.de;
        object3 = new com.huawei.tools.g.a.f();
        new com.huawei.tools.g.a.f().bK = string2;
        ((com.huawei.tools.g.a.f)object3).bh = string;
        ((com.huawei.tools.g.a.f)object3).bj = "P1";
        ((com.huawei.tools.g.a.f)object3).bi = object2;
        ((com.huawei.tools.g.a.f)object3).bL = l2;
        ((com.huawei.tools.g.a.f)object3).bM = 0L;
        ((com.huawei.tools.g.a.f)object3).bS = com.huawei.tools.h.d.dp.incrementAndGet();
        ((com.huawei.tools.g.a.f)object3).bV = g.a((com.huawei.tools.g.a.b)object);
        com.huawei.tools.h.d.di.put(string2, (com.huawei.tools.g.a.f)object3);
        if (bl) {
            object = com.huawei.tools.h.d.dg.get(string);
        } else {
            object2 = com.huawei.tools.h.d.dh.get(string);
            if (object2 != null && "invalid".equals(((j)object2).bt)) {
                com.huawei.tools.h.d.dh.remove(string);
            }
            object = g.o();
            object2 = new j();
            new j().bO = object;
            ((j)object2).bh = string;
            ((j)object2).bK = string2;
            ((j)object2).bt = "active";
            com.huawei.tools.h.d.dh.put(string, (j)object2);
        }
        ((com.huawei.tools.g.a.f)object3).bO = object;
        if (bl && (object2 = com.huawei.tools.h.d.df.get(object)) != null) {
            ((com.huawei.tools.g.a.f)object3).cf = ((com.huawei.tools.g.a.c)object2).bz;
        }
        return "E000:" + string2 + "," + (String)object;
    }

    @Override
    public final String h(String string, String string2) {
        return e.h(string, string2);
    }

    @Override
    public final String i(String string, String string2) {
        return e.i(string, string2);
    }

    @Override
    public final String e(String string) {
        return e.e(string);
    }

    @Override
    public final String a(String string, String string2, String string3) {
        return e.a(string, string2, string3);
    }

    @Override
    public final String j(String object, String object2) {
        Object object3;
        String string = object;
        object = object2;
        object2 = string;
        if ((object2 = com.huawei.tools.h.d.di.get(object2)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (((com.huawei.tools.g.a.f)object2).bM > 0L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (g.f((com.huawei.tools.g.a.f)object2)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.Z.key);
        }
        if (((com.huawei.tools.g.a.f)object2).bQ > 0L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        String string2 = ((com.huawei.tools.g.a.f)object2).bh;
        com.huawei.tools.g.a.g g2 = com.huawei.tools.h.d.dk.get(object);
        if (g2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.N.key);
        }
        boolean bl = ((com.huawei.tools.g.a.f)object2).ck != null;
        boolean bl2 = bl && ((String)object).equals(h.S(((com.huawei.tools.g.a.f)object2).ck));
        if (bl2 && "R".equals(g2.bt)) {
            g2.bt = "U";
            g2.bh = string2;
            ((com.huawei.tools.g.a.f)object2).bN = object;
            ((com.huawei.tools.g.a.f)object2).bX = com.huawei.tools.h.d.de;
            ((com.huawei.tools.g.a.f)object2).bY = 0L;
            ((com.huawei.tools.g.a.f)object2).bR = false;
            ((com.huawei.tools.g.a.f)object2).bB = g.I(string2);
            ((com.huawei.tools.g.a.f)object2).bZ = g2.cl;
            ((com.huawei.tools.g.a.f)object2).ca = "unpaid";
            ((com.huawei.tools.g.a.f)object2).cb = 0.0;
            ((com.huawei.tools.g.a.f)object2).cd = 0.0;
            ((com.huawei.tools.g.a.f)object2).ce = 0L;
            com.huawei.tools.g.a.e e2 = com.huawei.tools.h.d.dj.get(g2.bj);
            if (e2 != null) {
                AtomicInteger atomicInteger = e2.bH.get(g2.cl);
                if (atomicInteger != null) {
                    atomicInteger.incrementAndGet();
                }
                if ((object = e2.bJ.get(g2.cl)) != null && ((AtomicInteger)object).get() > 0) {
                    ((AtomicInteger)object).decrementAndGet();
                }
            }
            ((com.huawei.tools.g.a.f)object2).bP = 0L;
            return "E000:Success";
        }
        if (!"F".equals(g2.bt)) {
            if ("R".equals(g2.bt)) {
                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.av.key);
            }
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.O.key);
        }
        if (bl && !bl2) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aw.key);
        }
        String string3 = g.I(string2);
        String string4 = g2.cl;
        String string5 = string3;
        if (!("L".equals(string4) ? true : ("M".equals(string4) ? "M".equals(string5) || "S".equals(string5) : ("S".equals(string4) ? "S".equals(string5) : false)))) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.P.key);
        }
        string5 = ((com.huawei.tools.g.a.f)object2).bZ;
        if (string5 != null && !string5.equals(g2.cl)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.U.key);
        }
        g2.bt = "U";
        g2.bh = string2;
        ((com.huawei.tools.g.a.f)object2).bN = object;
        boolean bl3 = "paid".equals(((com.huawei.tools.g.a.f)object2).ca);
        if (((com.huawei.tools.g.a.f)object2).bX <= 0L) {
            ((com.huawei.tools.g.a.f)object2).bX = com.huawei.tools.h.d.de;
        }
        if (((com.huawei.tools.g.a.f)object2).bZ == null) {
            ((com.huawei.tools.g.a.f)object2).bZ = g2.cl;
        }
        ((com.huawei.tools.g.a.f)object2).bY = 0L;
        ((com.huawei.tools.g.a.f)object2).bR = false;
        ((com.huawei.tools.g.a.f)object2).bB = string3;
        if (!bl3) {
            ((com.huawei.tools.g.a.f)object2).ca = "unpaid";
            ((com.huawei.tools.g.a.f)object2).cb = 0.0;
            ((com.huawei.tools.g.a.f)object2).cd = 0.0;
            ((com.huawei.tools.g.a.f)object2).ce = 0L;
        }
        if ((object3 = com.huawei.tools.h.d.dj.get(g2.bj)) != null && (object3 = ((com.huawei.tools.g.a.e)object3).bH.get(g2.cl)) != null) {
            ((AtomicInteger)object3).incrementAndGet();
        }
        ((com.huawei.tools.g.a.f)object2).bP = 0L;
        e.l();
        return "E000:Success";
    }

    @Override
    public final String b(String object) {
        AtomicInteger atomicInteger;
        Object object2 = object;
        if ((object2 = com.huawei.tools.h.d.di.get(object2)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (((com.huawei.tools.g.a.f)object2).bM > 0L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (g.f((com.huawei.tools.g.a.f)object2)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.Z.key);
        }
        String string = ((com.huawei.tools.g.a.f)object2).bh;
        String string2 = ((com.huawei.tools.g.a.f)object2).bN;
        if (string2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.N.key);
        }
        object = com.huawei.tools.h.d.dk.get(string2);
        if (object == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.N.key);
        }
        if (!string.equals(((com.huawei.tools.g.a.g)object).bh)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.R.key);
        }
        if (((com.huawei.tools.g.a.f)object2).ck != null) {
            ((com.huawei.tools.g.a.g)object).bt = "R";
            ((com.huawei.tools.g.a.g)object).bh = null;
            com.huawei.tools.g.a.e e2 = com.huawei.tools.h.d.dj.get(((com.huawei.tools.g.a.g)object).bj);
            if (e2 != null) {
                AtomicInteger atomicInteger2 = e2.bH.get(((com.huawei.tools.g.a.g)object).cl);
                if (atomicInteger2 != null) {
                    atomicInteger2.decrementAndGet();
                }
                if ((object = e2.bJ.get(((com.huawei.tools.g.a.g)object).cl)) != null) {
                    ((AtomicInteger)object).incrementAndGet();
                }
            }
            ((com.huawei.tools.g.a.f)object2).bN = null;
            if (!g.K(string)) {
                ((com.huawei.tools.g.a.f)object2).bP = com.huawei.tools.h.d.de;
            }
            ((com.huawei.tools.g.a.f)object2).bR = true;
            ((com.huawei.tools.g.a.f)object2).bY = com.huawei.tools.h.d.de;
            return "E000:".concat(String.valueOf(string2));
        }
        ((com.huawei.tools.g.a.g)object).bt = "F";
        ((com.huawei.tools.g.a.g)object).bh = null;
        com.huawei.tools.g.a.e e3 = com.huawei.tools.h.d.dj.get(((com.huawei.tools.g.a.g)object).bj);
        if (e3 != null && (atomicInteger = e3.bH.get(((com.huawei.tools.g.a.g)object).cl)) != null) {
            atomicInteger.decrementAndGet();
        }
        ((com.huawei.tools.g.a.f)object2).bN = null;
        if (!g.K(string)) {
            ((com.huawei.tools.g.a.f)object2).bP = com.huawei.tools.h.d.de;
        }
        ((com.huawei.tools.g.a.f)object2).bR = true;
        ((com.huawei.tools.g.a.f)object2).bY = com.huawei.tools.h.d.de;
        e.l();
        return "E000:".concat(String.valueOf(string2));
    }

    @Override
    public final String k(String string, String string2) {
        return e.k(string, string2);
    }

    @Override
    public final String b(String string, String string2, String string3) {
        return e.b(string, string2, string3);
    }

    @Override
    public final String f(String string) {
        if (string == null || string.isEmpty()) {
            return "E001:Fail";
        }
        if (!com.huawei.tools.h.d.dq.containsKey(string)) {
            com.huawei.tools.g.a.a a2 = new com.huawei.tools.g.a.a(string);
            com.huawei.tools.h.d.dq.put(string, a2);
        }
        return "E000:Success";
    }

    @Override
    public final String g(String string) {
        if (string == null || string.isEmpty()) {
            return "E001:Fail";
        }
        com.huawei.tools.h.d.dq.remove(string);
        return "E000:Success";
    }

    @Override
    public final String b(String string, int n) {
        return com.huawei.tools.g.b.a.a.b(string, n);
    }

    @Override
    public final String a(String object, String object2, int n) {
        object = com.huawei.tools.h.d.df.get(object);
        object2 = com.huawei.tools.h.d.df.get(object2);
        if (object == null || object2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(((com.huawei.tools.g.a.c)object).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (!"active".equals(((com.huawei.tools.g.a.c)object2).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (n <= 0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.J.key);
        }
        double d2 = 0.0;
        if (((com.huawei.tools.g.a.c)object).bs != null) {
            for (String string : ((com.huawei.tools.g.a.c)object).bs) {
                for (com.huawei.tools.g.a.f f2 : com.huawei.tools.h.d.di.values()) {
                    double d3;
                    if (!string.equals(f2.bh) || f2.bM != 0L) continue;
                    double d4 = ((com.huawei.tools.g.a.c)object).bz - f2.cf;
                    if (!(d3 > d2)) continue;
                    d2 = d4;
                }
            }
        }
        if (((com.huawei.tools.g.a.c)object).by - d2 < (double)n) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ag.key);
        }
        if (((com.huawei.tools.g.a.c)object2).by + (double)n > 5000.0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.af.key);
        }
        ((com.huawei.tools.g.a.c)object).by -= (double)n;
        ((com.huawei.tools.g.a.c)object2).by += (double)n;
        return "E000:Success";
    }

    @Override
    public final String l(String string, String string2) {
        return com.huawei.tools.g.b.a.a.l(string, string2);
    }

    @Override
    public final String b(String string, String string2, int n) {
        return com.huawei.tools.g.b.a.a.b(string, string2, n);
    }

    @Override
    public final String c(String object, String string, String string2) {
        double d2;
        block24: {
            double d3;
            double d4;
            block27: {
                double d5;
                block25: {
                    double d6;
                    block26: {
                        double d7;
                        boolean bl;
                        Object object2;
                        Object object3;
                        block23: {
                            object3 = string2;
                            string2 = string;
                            string = object;
                            object = this.cI;
                            if (object3 == null || !((String)object3).matches("^-?\\d+\\.\\d{2}$")) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                            }
                            d5 = Double.parseDouble((String)object3);
                            object3 = com.huawei.tools.h.d.di.get(string);
                            if (object3 == null) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
                            }
                            if (((com.huawei.tools.g.a.f)object3).bM > 0L) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
                            }
                            d4 = ((com.huawei.tools.g.a.f)object3).cb;
                            if ("paid".equals(((com.huawei.tools.g.a.f)object3).ca) && ((com.huawei.tools.g.a.f)object3).ce > 0L) {
                                double d8;
                                if (com.huawei.tools.h.d.de - ((com.huawei.tools.g.a.f)object3).ce > 900L) {
                                    Object object4 = object3;
                                    d8 = ((c)object).a((com.huawei.tools.g.a.f)object4, ((com.huawei.tools.g.a.f)object4).ce);
                                } else {
                                    d8 = 0.0;
                                }
                                d3 = d8;
                            } else {
                                d3 = ((c)object).b((com.huawei.tools.g.a.f)object3) - d4;
                            }
                            d2 = 0.0;
                            if (((c)object).cH != null) {
                                d2 = i.s(((com.huawei.tools.g.a.f)object3).bK);
                                d3 += d2;
                            }
                            if (d3 <= 0.0) {
                                String string3;
                                boolean bl2 = false;
                                object2 = ((com.huawei.tools.g.a.f)object3).bh;
                                if (com.huawei.tools.h.d.dg.containsKey(object2) && (object2 = com.huawei.tools.h.d.df.get(string3 = com.huawei.tools.h.d.dg.get(object2))) != null && ((com.huawei.tools.g.a.c)object2).by < 0.0) {
                                    bl2 = true;
                                }
                                if (!bl2) {
                                    throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                                }
                            }
                            String string4 = ((com.huawei.tools.g.a.f)object3).bh;
                            object2 = ((com.huawei.tools.g.a.f)object3).bO;
                            bl = com.huawei.tools.h.d.dg.containsKey(string4);
                            if (bl) {
                                object2 = com.huawei.tools.h.d.dg.get(string4);
                            }
                            if (!"bal".equals(string2)) break block23;
                            if (!bl) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                            }
                            if ((object2 = com.huawei.tools.h.d.df.get(object2)) == null) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
                            }
                            double d9 = ((com.huawei.tools.g.a.c)object2).bz - ((com.huawei.tools.g.a.f)object3).cf;
                            double d10 = ((com.huawei.tools.g.a.c)object2).by - Math.max(0.0, d9);
                            if (Math.abs(d5 - d3) > 0.01) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                            }
                            if (d10 < d3) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ag.key);
                            }
                            ((com.huawei.tools.g.a.c)object2).by -= d3;
                            ((com.huawei.tools.g.a.f)object3).ca = "paid";
                            ((com.huawei.tools.g.a.f)object3).cb = d4 + d3;
                            ((com.huawei.tools.g.a.f)object3).cd += d3;
                            ((com.huawei.tools.g.a.f)object3).ce = com.huawei.tools.h.d.de;
                            com.huawei.tools.g.b.a.a.a(com.huawei.tools.h.d.de, "balpay", d3);
                            break block24;
                        }
                        if (!bl) break block25;
                        double d11 = (object2 = com.huawei.tools.h.d.df.get(object2)) != null ? ((com.huawei.tools.g.a.c)object2).by : 0.0;
                        double d12 = object2 != null ? ((com.huawei.tools.g.a.c)object2).bz - ((com.huawei.tools.g.a.f)object3).cf : 0.0;
                        d6 = d11 - Math.max(0.0, d12);
                        if (d7 < 0.0) {
                            d6 = 0.0;
                        }
                        if (d6 >= d3 && d11 >= 0.0) {
                            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                        }
                        if (!(d11 < 0.0)) break block26;
                        double d13 = d3 - d11;
                        if (Math.abs(d5 - d13) > 0.01) {
                            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                        }
                        ((com.huawei.tools.g.a.c)object2).by = 0.0;
                        break block27;
                    }
                    double d14 = Math.min(d6, d3);
                    double d15 = d3 - d14;
                    if (Math.abs(d5 - d15) > 0.01) {
                        throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                    }
                    if (d14 > 0.0) {
                        ((com.huawei.tools.g.a.c)object2).by -= d14;
                        ((com.huawei.tools.g.a.f)object3).cd += d14;
                        com.huawei.tools.g.b.a.a.a(com.huawei.tools.h.d.de, "balpay", d14);
                    }
                    ((com.huawei.tools.g.a.f)object3).ca = "paid";
                    ((com.huawei.tools.g.a.f)object3).cb = d4 + d3;
                    ((com.huawei.tools.g.a.f)object3).ce = com.huawei.tools.h.d.de;
                    com.huawei.tools.g.b.a.a.a(com.huawei.tools.h.d.de, "temppay", d5);
                    break block24;
                }
                if (Math.abs(d5 - d3) > 0.01) {
                    throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                }
            }
            ((com.huawei.tools.g.a.f)object3).ca = "paid";
            ((com.huawei.tools.g.a.f)object3).cb = d4 + d3;
            ((com.huawei.tools.g.a.f)object3).ce = com.huawei.tools.h.d.de;
            com.huawei.tools.g.b.a.a.a(com.huawei.tools.h.d.de, "temppay", d3);
        }
        if (d2 > 0.0 && ((c)object).cH != null) {
            i.v(string);
        }
        return "E000:Success";
    }

    @Override
    public final String d(String string, String string2, String string3) {
        return c.d(string, string2, string3);
    }

    @Override
    public final String h(String string) {
        return b.h(string);
    }

    @Override
    public final String m(String string, String string2) {
        return this.cB.m(string, string2);
    }

    @Override
    public final String i(String object) {
        Object object2 = object;
        object = this.cB;
        if ((object2 = com.huawei.tools.h.d.dv.get(object2)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.as.key);
        }
        if (!"locked".equals(((com.huawei.tools.g.a.h)object2).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.at.key);
        }
        if (h.U(((com.huawei.tools.g.a.h)object2).bh)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.au.key);
        }
        h.b((com.huawei.tools.g.a.h)object2);
        ((com.huawei.tools.g.a.h)object2).bt = "canceled";
        ((h)object).q();
        return "E000:Success";
    }

    @Override
    public final String n(String string, String object) {
        String string2;
        String string3 = object;
        object = string;
        if (StringUtils.isBlank((CharSequence)object) || !((String)object).matches("r\\d{8}")) {
            return "E001:Fail";
        }
        if (StringUtils.isBlank(string3)) {
            return "E001:Fail";
        }
        switch (string3) {
            case "locked": {
                string2 = "locked";
                break;
            }
            case "used": {
                string2 = "used";
                break;
            }
            case "expired": {
                string2 = "expired";
                break;
            }
            case "cancel": {
                string2 = "canceled";
                break;
            }
            case "closed": {
                string2 = "closed";
                break;
            }
            default: {
                string2 = string = null;
            }
        }
        if (string2 == null) {
            return "E001:Fail";
        }
        if ((object = com.huawei.tools.h.d.dv.get(object)) == null) {
            return "E001:Fail";
        }
        if (string.equals(((com.huawei.tools.g.a.h)object).bt)) {
            return "E000:Success";
        }
        return com.huawei.tools.a.b.a("E002", "Verify_fail");
    }

    @Override
    public final String j(String string) {
        return h.j(string);
    }

    @Override
    public final String k(String string) {
        String string2;
        Object object;
        String string3 = string;
        if (StringUtils.isBlank(string3)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.I.key);
        }
        i.t();
        long l = com.huawei.tools.h.d.de;
        long l2 = l % 86400L;
        if (l2 < 32400L || l2 > 61200L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aC.key);
        }
        if (g.J(string3)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aM.key);
        }
        com.huawei.tools.g.a.f f2 = i.ac(string3);
        if (f2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aA.key);
        }
        String string4 = g.e(f2);
        if (!"inpark".equals(string4) && !"parked".equals(string4)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aA.key);
        }
        string4 = com.huawei.tools.h.d.dg.get(string3);
        if (string4 != null && (object = com.huawei.tools.h.d.df.get(string4)) != null && ((com.huawei.tools.g.a.c)object).by < 0.0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aN.key);
        }
        if (i.ad(string3) > 0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aB.key);
        }
        if (g.K(string3)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aP.key);
        }
        object = g.I(string3);
        Object object2 = object;
        if ("S".equals(object2)) {
            string = i.a(i.cT, com.huawei.tools.h.d.dy.get());
            if (string != null) {
                i.a(i.cT, string, com.huawei.tools.h.d.dy);
                string2 = string;
            } else {
                string = i.a(i.cU, com.huawei.tools.h.d.dz.get());
                if (string != null) {
                    i.a(i.cU, string, com.huawei.tools.h.d.dz);
                    string2 = string;
                } else {
                    string2 = i.a(i.cV, 0);
                }
            }
        } else if ("M".equals(object2)) {
            string = i.a(i.cU, com.huawei.tools.h.d.dz.get());
            if (string != null) {
                i.a(i.cU, string, com.huawei.tools.h.d.dz);
                string2 = string;
            } else {
                string2 = i.a(i.cV, 0);
            }
        } else {
            string2 = string = "L".equals(object2) ? i.a(i.cV, 0) : null;
        }
        if (string2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aD.key);
        }
        i.t();
        int n = com.huawei.tools.h.d.dA.incrementAndGet();
        String string5 = String.format("%03d", n);
        Object object3 = object;
        long l3 = l % 86400L;
        int n2 = (int)(com.huawei.tools.h.d.de / 86400L);
        Object object4 = LocalDate.of(2026, 1, 1).plusDays(n2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        n2 = (g.N((String)object4) ? false : ((n2 = (n2 + 4) % 7) == 0 || n2 == 6 ? true : i.X((String)object4))) ? 1 : (l3 >= 43200L && l3 < 50400L || l3 >= 57600L && l3 < 64800L ? 1 : 0);
        double[] dArray = i.cS.get(object3);
        object4 = dArray;
        double d2 = dArray == null ? 0.0 : (n2 != 0 ? (double)object4[1] : (double)object4[0]);
        k k2 = new k();
        new k().cr = string5;
        k2.bh = string3;
        k2.bB = object;
        k2.bN = string;
        k2.bt = "queuing";
        k2.bw = l;
        k2.cw = d2;
        k2.bq = string4;
        k2.bK = f2.bK;
        k2.cz = true;
        k2.cs = com.huawei.tools.h.d.dC.incrementAndGet();
        com.huawei.tools.h.d.dx.put(i.b(string5, l), k2);
        i.g(f2);
        if (i.aa(string).size() == 1) {
            i.d(string, l);
        }
        return "E000:".concat(String.valueOf(string5));
    }

    @Override
    public final String l(String object) {
        if (StringUtils.isBlank((CharSequence)object)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aE.key);
        }
        if ((object = i.W((String)object)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aE.key);
        }
        if (!"queuing".equals(((k)object).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aF.key);
        }
        if (((k)object).cv > 0L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aG.key);
        }
        long l = com.huawei.tools.h.d.de;
        double d2 = 0.0;
        if (l - ((k)object).bw > 600L) {
            d2 = 5.0;
        }
        ((k)object).cx = d2;
        ((k)object).bt = "cancel";
        i.a((k)object, com.huawei.tools.h.d.de);
        i.d(((k)object).bN, l);
        return "E000:Success";
    }

    @Override
    public final String m(String object) {
        if (StringUtils.isBlank((CharSequence)object)) {
            return "E001:Fail";
        }
        if ((object = i.W((String)object)) == null) {
            return "E001:Fail";
        }
        return "E000:" + ((k)object).bt;
    }

    @Override
    public final String n(String object) {
        Object object2 = object;
        if (StringUtils.isBlank((CharSequence)object2)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aE.key);
        }
        object = i.W((String)object2);
        if (object == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aE.key);
        }
        if (!"queuing".equals(((k)object).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aH.key);
        }
        Object object3 = ((k)object).bK;
        if (object3 != null && (object3 = com.huawei.tools.h.d.di.get(object3)) != null && ((com.huawei.tools.g.a.f)object3).bN != null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aK.key);
        }
        object3 = ((k)object).bN;
        List<k> list = i.aa((String)object3);
        if (list.isEmpty() || !list.get((int)0).cr.equals(object2)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aI.key);
        }
        if (i.ab((String)object3) != null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aJ.key);
        }
        long l = com.huawei.tools.h.d.de;
        ((k)object).bt = "washing";
        ((k)object).ct = l;
        ((k)object).cv = 0L;
        object2 = com.huawei.tools.h.d.dl.get(object3);
        if (object2 != null) {
            ((com.huawei.tools.g.a.g)object2).bt = "U";
            ((com.huawei.tools.g.a.g)object2).bh = ((k)object).bh;
        }
        return "E000:Success";
    }

    @Override
    public final String o(String string) {
        Object object = string;
        if (StringUtils.isBlank((CharSequence)object)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aE.key);
        }
        if ((object = i.W((String)object)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aE.key);
        }
        if (!"washing".equals(((k)object).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aL.key);
        }
        long l = com.huawei.tools.h.d.de;
        ((k)object).bt = "closed";
        ((k)object).cu = l;
        string = ((k)object).bN;
        com.huawei.tools.g.a.g g2 = com.huawei.tools.h.d.dl.get(string);
        if (g2 != null) {
            g2.bt = "F";
            g2.bh = null;
        }
        i.a((k)object, com.huawei.tools.h.d.de);
        i.d(string, l);
        return "E000:Success";
    }

    @Override
    public final String d() {
        i i2 = this.cH;
        i.t();
        i i3 = i2;
        i3.ag("WS");
        i3.ag("WM");
        i3 = i2;
        i3.t("WL", "WM");
        i3.t("WM", "WS");
        i3 = i2;
        i3.t("WS", "WM");
        i3.t("WM", "WL");
        return "E000:Success";
    }

    @Override
    public final String p(String charSequence) {
        if (StringUtils.isBlank(charSequence)) {
            charSequence = new StringBuilder("E000:");
            for (int i2 = 0; i2 < i.cW.size(); ++i2) {
                if (i2 > 0) {
                    ((StringBuilder)charSequence).append(";");
                }
                ((StringBuilder)charSequence).append(i.af(i.cW.get(i2)));
            }
            return ((StringBuilder)charSequence).toString();
        }
        if (!i.cW.contains(charSequence)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aO.key);
        }
        return "E000:" + i.af((String)charSequence);
    }
}

