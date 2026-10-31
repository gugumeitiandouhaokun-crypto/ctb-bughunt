/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.huawei.tools.a.a
 *  com.huawei.tools.a.b
 *  com.huawei.tools.b.a
 *  com.huawei.tools.e.a
 *  com.huawei.tools.g.a.a
 *  com.huawei.tools.g.a.b
 *  com.huawei.tools.g.a.c
 *  com.huawei.tools.g.a.e
 *  com.huawei.tools.g.a.f
 *  com.huawei.tools.g.a.g
 *  com.huawei.tools.g.a.h
 *  com.huawei.tools.g.a.j
 *  com.huawei.tools.g.a.k
 *  com.huawei.tools.g.b.a
 *  com.huawei.tools.g.b.a.a
 *  com.huawei.tools.g.b.a.b
 *  com.huawei.tools.g.b.a.c
 *  com.huawei.tools.g.b.a.d
 *  com.huawei.tools.g.b.a.e
 *  com.huawei.tools.g.b.a.g
 *  com.huawei.tools.g.b.a.h
 *  com.huawei.tools.g.b.a.i
 *  com.huawei.tools.h.d
 *  org.apache.commons.lang3.StringUtils
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
            HashSet<k> hashSet = new HashSet<k>();
            for (Object object : com.huawei.tools.h.d.dx.values()) {
                if (!"queuing".equals(object.bt) || hashSet.contains(object = object.bN)) continue;
                hashSet.add((k)object);
                i.c((String)object, (long)l);
            }
            i.d((long)l);
            i.e((long)l);
        };
    }

    public final String c() {
        com.huawei.tools.h.d.clear();
        e.g();
        com.huawei.tools.e.a.info((String)com.huawei.tools.a.a.g.key);
        return "E000:Success";
    }

    public final String a(String string, int n) {
        string = this.cM.a(string, n);
        b.f();
        return string;
    }

    public final String b(String string, String string2) {
        return b.b((String)string, (String)string2);
    }

    public final String c(String string, String string2) {
        return b.c((String)string, (String)string2);
    }

    public final String d(String string, String object) {
        String string22 = object;
        object = string;
        if ((string = (com.huawei.tools.g.a.c)com.huawei.tools.h.d.df.get(object)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if ("cancelled".equals(((com.huawei.tools.g.a.c)string).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if ("expired".equals(((com.huawei.tools.g.a.c)string).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ac.key);
        }
        if (!"active".equals(((com.huawei.tools.g.a.c)string).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (!string22.equals(((com.huawei.tools.g.a.c)string).password)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.l.key);
        }
        object = ((com.huawei.tools.g.a.c)string).bs.iterator();
        while (object.hasNext()) {
            if (!g.F((String)((String)object.next()))) continue;
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.W.key);
        }
        object = ((com.huawei.tools.g.a.c)string).bs.iterator();
        while (object.hasNext()) {
            if (!h.T((String)((String)object.next()))) continue;
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.az.key);
        }
        if (((com.huawei.tools.g.a.c)string).by != 0.0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.z.key);
        }
        object = ((com.huawei.tools.g.a.c)string).bs.iterator();
        while (object.hasNext()) {
            if (g.P((String)((String)object.next())) <= 0) continue;
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.A.key);
        }
        ((com.huawei.tools.g.a.c)string).bt = "cancelled";
        for (String string22 : ((com.huawei.tools.g.a.c)string).bs) {
            com.huawei.tools.h.d.dg.remove(string22);
            com.huawei.tools.h.d.do.put(string22, com.huawei.tools.h.d.de);
        }
        ((com.huawei.tools.g.a.c)string).bs.clear();
        return "E000:Success";
    }

    public final String e(String string, String string2) {
        return b.e((String)string, (String)string2);
    }

    public final String f(String string, String string2) {
        String string3 = string2;
        string2 = string;
        if ((string = (com.huawei.tools.g.a.c)com.huawei.tools.h.d.df.get(string2)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(((com.huawei.tools.g.a.c)string).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (!((com.huawei.tools.g.a.c)string).bs.contains(string3)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.v.key);
        }
        if (g.F((String)string3)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.w.key);
        }
        if (h.T((String)string3)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ay.key);
        }
        if (g.P((String)string3) > 0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.x.key);
        }
        ((com.huawei.tools.g.a.c)string).bs.remove(string3);
        com.huawei.tools.h.d.dg.remove(string3);
        com.huawei.tools.h.d.do.put(string3, com.huawei.tools.h.d.de);
        return "E000:Success";
    }

    public final String c(String string) {
        return b.c((String)string);
    }

    public final String d(String string) {
        return b.d((String)string);
    }

    public final String g(String string, String string2) {
        long l;
        String string3;
        String string4 = string2;
        string2 = string;
        g.G((String)string2);
        string = (com.huawei.tools.g.a.b)com.huawei.tools.h.d.dn.get(string4);
        if (string == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.K.key);
        }
        if (g.F((String)string2)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.X.key);
        }
        boolean bl = com.huawei.tools.h.d.dg.containsKey(string2);
        if (!bl && g.p() >= 500) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.F.key);
        }
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        while (!com.huawei.tools.h.d.dm.add(string3 = "p" + String.format("%016d", l = Math.abs(((Random)threadLocalRandom).nextLong()) % 10000000000000000L))) {
        }
        long l2 = com.huawei.tools.h.d.de;
        threadLocalRandom = new com.huawei.tools.g.a.f();
        new com.huawei.tools.g.a.f().bK = string3;
        ((com.huawei.tools.g.a.f)threadLocalRandom).bh = string2;
        ((com.huawei.tools.g.a.f)threadLocalRandom).bj = "P1";
        ((com.huawei.tools.g.a.f)threadLocalRandom).bi = string4;
        ((com.huawei.tools.g.a.f)threadLocalRandom).bL = l2;
        ((com.huawei.tools.g.a.f)threadLocalRandom).bM = 0L;
        ((com.huawei.tools.g.a.f)threadLocalRandom).bS = com.huawei.tools.h.d.dp.incrementAndGet();
        ((com.huawei.tools.g.a.f)threadLocalRandom).bV = g.a((com.huawei.tools.g.a.b)string);
        com.huawei.tools.h.d.di.put(string3, threadLocalRandom);
        if (bl) {
            string = (String)com.huawei.tools.h.d.dg.get(string2);
        } else {
            string4 = (j)com.huawei.tools.h.d.dh.get(string2);
            if (string4 != null && "invalid".equals(((j)string4).bt)) {
                com.huawei.tools.h.d.dh.remove(string2);
            }
            string = g.o();
            string4 = new j();
            new j().bO = string;
            ((j)string4).bh = string2;
            ((j)string4).bK = string3;
            ((j)string4).bt = "active";
            com.huawei.tools.h.d.dh.put(string2, string4);
        }
        ((com.huawei.tools.g.a.f)threadLocalRandom).bO = string;
        if (bl && (string4 = (com.huawei.tools.g.a.c)com.huawei.tools.h.d.df.get(string)) != null) {
            ((com.huawei.tools.g.a.f)threadLocalRandom).cf = ((com.huawei.tools.g.a.c)string4).bz;
        }
        return "E000:" + string3 + "," + string;
    }

    public final String h(String string, String string2) {
        return e.h((String)string, (String)string2);
    }

    public final String i(String string, String string2) {
        return e.i((String)string, (String)string2);
    }

    public final String e(String string) {
        return e.e((String)string);
    }

    public final String a(String string, String string2, String string3) {
        return e.a((String)string, (String)string2, (String)string3);
    }

    public final String j(String object, String string) {
        Object object2;
        String string2 = object;
        object = string;
        string = string2;
        if ((string = (com.huawei.tools.g.a.f)com.huawei.tools.h.d.di.get(string)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (((com.huawei.tools.g.a.f)string).bM > 0L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (g.f((com.huawei.tools.g.a.f)string)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.Z.key);
        }
        if (((com.huawei.tools.g.a.f)string).bQ > 0L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        String string3 = ((com.huawei.tools.g.a.f)string).bh;
        com.huawei.tools.g.a.g g2 = (com.huawei.tools.g.a.g)com.huawei.tools.h.d.dk.get(object);
        if (g2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.N.key);
        }
        boolean bl = ((com.huawei.tools.g.a.f)string).ck != null;
        boolean bl2 = bl && ((String)object).equals(h.S((String)((com.huawei.tools.g.a.f)string).ck));
        if (bl2 && "R".equals(g2.bt)) {
            g2.bt = "U";
            g2.bh = string3;
            ((com.huawei.tools.g.a.f)string).bN = object;
            ((com.huawei.tools.g.a.f)string).bX = com.huawei.tools.h.d.de;
            ((com.huawei.tools.g.a.f)string).bY = 0L;
            ((com.huawei.tools.g.a.f)string).bR = false;
            ((com.huawei.tools.g.a.f)string).bB = g.I((String)string3);
            ((com.huawei.tools.g.a.f)string).bZ = g2.cl;
            ((com.huawei.tools.g.a.f)string).ca = "unpaid";
            ((com.huawei.tools.g.a.f)string).cb = 0.0;
            ((com.huawei.tools.g.a.f)string).cd = 0.0;
            ((com.huawei.tools.g.a.f)string).ce = 0L;
            com.huawei.tools.g.a.e e2 = (com.huawei.tools.g.a.e)com.huawei.tools.h.d.dj.get(g2.bj);
            if (e2 != null) {
                AtomicInteger atomicInteger = (AtomicInteger)e2.bH.get(g2.cl);
                if (atomicInteger != null) {
                    atomicInteger.incrementAndGet();
                }
                if ((object = (AtomicInteger)e2.bJ.get(g2.cl)) != null && ((AtomicInteger)object).get() > 0) {
                    ((AtomicInteger)object).decrementAndGet();
                }
            }
            ((com.huawei.tools.g.a.f)string).bP = 0L;
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
        String string4 = g.I((String)string3);
        String string5 = g2.cl;
        String string6 = string4;
        if (!("L".equals(string5) ? true : ("M".equals(string5) ? "M".equals(string6) || "S".equals(string6) : ("S".equals(string5) ? "S".equals(string6) : false)))) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.P.key);
        }
        string6 = ((com.huawei.tools.g.a.f)string).bZ;
        if (string6 != null && !string6.equals(g2.cl)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.U.key);
        }
        g2.bt = "U";
        g2.bh = string3;
        ((com.huawei.tools.g.a.f)string).bN = object;
        boolean bl3 = "paid".equals(((com.huawei.tools.g.a.f)string).ca);
        if (((com.huawei.tools.g.a.f)string).bX <= 0L) {
            ((com.huawei.tools.g.a.f)string).bX = com.huawei.tools.h.d.de;
        }
        if (((com.huawei.tools.g.a.f)string).bZ == null) {
            ((com.huawei.tools.g.a.f)string).bZ = g2.cl;
        }
        ((com.huawei.tools.g.a.f)string).bY = 0L;
        ((com.huawei.tools.g.a.f)string).bR = false;
        ((com.huawei.tools.g.a.f)string).bB = string4;
        if (!bl3) {
            ((com.huawei.tools.g.a.f)string).ca = "unpaid";
            ((com.huawei.tools.g.a.f)string).cb = 0.0;
            ((com.huawei.tools.g.a.f)string).cd = 0.0;
            ((com.huawei.tools.g.a.f)string).ce = 0L;
        }
        if ((object2 = (com.huawei.tools.g.a.e)com.huawei.tools.h.d.dj.get(g2.bj)) != null && (object2 = (AtomicInteger)((com.huawei.tools.g.a.e)object2).bH.get(g2.cl)) != null) {
            ((AtomicInteger)object2).incrementAndGet();
        }
        ((com.huawei.tools.g.a.f)string).bP = 0L;
        e.l();
        return "E000:Success";
    }

    public final String b(String object) {
        AtomicInteger atomicInteger;
        String string = object;
        if ((string = (com.huawei.tools.g.a.f)com.huawei.tools.h.d.di.get(string)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (((com.huawei.tools.g.a.f)string).bM > 0L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (g.f((com.huawei.tools.g.a.f)string)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.Z.key);
        }
        String string2 = ((com.huawei.tools.g.a.f)string).bh;
        String string3 = ((com.huawei.tools.g.a.f)string).bN;
        if (string3 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.N.key);
        }
        object = (com.huawei.tools.g.a.g)com.huawei.tools.h.d.dk.get(string3);
        if (object == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.N.key);
        }
        if (!string2.equals(((com.huawei.tools.g.a.g)object).bh)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.R.key);
        }
        if (((com.huawei.tools.g.a.f)string).ck != null) {
            ((com.huawei.tools.g.a.g)object).bt = "R";
            ((com.huawei.tools.g.a.g)object).bh = null;
            com.huawei.tools.g.a.e e2 = (com.huawei.tools.g.a.e)com.huawei.tools.h.d.dj.get(((com.huawei.tools.g.a.g)object).bj);
            if (e2 != null) {
                AtomicInteger atomicInteger2 = (AtomicInteger)e2.bH.get(((com.huawei.tools.g.a.g)object).cl);
                if (atomicInteger2 != null) {
                    atomicInteger2.decrementAndGet();
                }
                if ((object = (AtomicInteger)e2.bJ.get(((com.huawei.tools.g.a.g)object).cl)) != null) {
                    ((AtomicInteger)object).incrementAndGet();
                }
            }
            ((com.huawei.tools.g.a.f)string).bN = null;
            if (!g.K((String)string2)) {
                ((com.huawei.tools.g.a.f)string).bP = com.huawei.tools.h.d.de;
            }
            ((com.huawei.tools.g.a.f)string).bR = true;
            ((com.huawei.tools.g.a.f)string).bY = com.huawei.tools.h.d.de;
            return "E000:".concat(String.valueOf(string3));
        }
        ((com.huawei.tools.g.a.g)object).bt = "F";
        ((com.huawei.tools.g.a.g)object).bh = null;
        com.huawei.tools.g.a.e e3 = (com.huawei.tools.g.a.e)com.huawei.tools.h.d.dj.get(((com.huawei.tools.g.a.g)object).bj);
        if (e3 != null && (atomicInteger = (AtomicInteger)e3.bH.get(((com.huawei.tools.g.a.g)object).cl)) != null) {
            atomicInteger.decrementAndGet();
        }
        ((com.huawei.tools.g.a.f)string).bN = null;
        if (!g.K((String)string2)) {
            ((com.huawei.tools.g.a.f)string).bP = com.huawei.tools.h.d.de;
        }
        ((com.huawei.tools.g.a.f)string).bR = true;
        ((com.huawei.tools.g.a.f)string).bY = com.huawei.tools.h.d.de;
        e.l();
        return "E000:".concat(String.valueOf(string3));
    }

    public final String k(String string, String string2) {
        return e.k((String)string, (String)string2);
    }

    public final String b(String string, String string2, String string3) {
        return e.b((String)string, (String)string2, (String)string3);
    }

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

    public final String g(String string) {
        if (string == null || string.isEmpty()) {
            return "E001:Fail";
        }
        com.huawei.tools.h.d.dq.remove(string);
        return "E000:Success";
    }

    public final String b(String string, int n) {
        return com.huawei.tools.g.b.a.a.b((String)string, (int)n);
    }

    public final String a(String string, String string2, int n) {
        string = (com.huawei.tools.g.a.c)com.huawei.tools.h.d.df.get(string);
        string2 = (com.huawei.tools.g.a.c)com.huawei.tools.h.d.df.get(string2);
        if (string == null || string2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(((com.huawei.tools.g.a.c)string).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (!"active".equals(((com.huawei.tools.g.a.c)string2).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (n <= 0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.J.key);
        }
        double d2 = 0.0;
        if (((com.huawei.tools.g.a.c)string).bs != null) {
            for (String string3 : ((com.huawei.tools.g.a.c)string).bs) {
                for (com.huawei.tools.g.a.f f2 : com.huawei.tools.h.d.di.values()) {
                    double d3;
                    if (!string3.equals(f2.bh) || f2.bM != 0L) continue;
                    double d4 = ((com.huawei.tools.g.a.c)string).bz - f2.cf;
                    if (!(d3 > d2)) continue;
                    d2 = d4;
                }
            }
        }
        if (((com.huawei.tools.g.a.c)string).by - d2 < (double)n) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ag.key);
        }
        if (((com.huawei.tools.g.a.c)string2).by + (double)n > 5000.0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.af.key);
        }
        ((com.huawei.tools.g.a.c)string).by -= (double)n;
        ((com.huawei.tools.g.a.c)string2).by += (double)n;
        return "E000:Success";
    }

    public final String l(String string, String string2) {
        return com.huawei.tools.g.b.a.a.l((String)string, (String)string2);
    }

    public final String b(String string, String string2, int n) {
        return com.huawei.tools.g.b.a.a.b((String)string, (String)string2, (int)n);
    }

    public final String c(String string, String string2, String string3) {
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
                        String string4;
                        String string5;
                        block23: {
                            string5 = string3;
                            string3 = string2;
                            string2 = string;
                            string = this.cI;
                            if (string5 == null || !string5.matches("^-?\\d+\\.\\d{2}$")) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                            }
                            d5 = Double.parseDouble(string5);
                            string5 = (com.huawei.tools.g.a.f)com.huawei.tools.h.d.di.get(string2);
                            if (string5 == null) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
                            }
                            if (((com.huawei.tools.g.a.f)string5).bM > 0L) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
                            }
                            d4 = ((com.huawei.tools.g.a.f)string5).cb;
                            if ("paid".equals(((com.huawei.tools.g.a.f)string5).ca) && ((com.huawei.tools.g.a.f)string5).ce > 0L) {
                                double d8;
                                if (com.huawei.tools.h.d.de - ((com.huawei.tools.g.a.f)string5).ce > 900L) {
                                    String string6 = string5;
                                    d8 = string.a((com.huawei.tools.g.a.f)string6, ((com.huawei.tools.g.a.f)string6).ce);
                                } else {
                                    d8 = 0.0;
                                }
                                d3 = d8;
                            } else {
                                d3 = string.b((com.huawei.tools.g.a.f)string5) - d4;
                            }
                            d2 = 0.0;
                            if (((c)string).cH != null) {
                                d2 = i.s((String)((com.huawei.tools.g.a.f)string5).bK);
                                d3 += d2;
                            }
                            if (d3 <= 0.0) {
                                String string7;
                                boolean bl2 = false;
                                string4 = ((com.huawei.tools.g.a.f)string5).bh;
                                if (com.huawei.tools.h.d.dg.containsKey(string4) && (string4 = (com.huawei.tools.g.a.c)com.huawei.tools.h.d.df.get(string7 = (String)com.huawei.tools.h.d.dg.get(string4))) != null && ((com.huawei.tools.g.a.c)string4).by < 0.0) {
                                    bl2 = true;
                                }
                                if (!bl2) {
                                    throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                                }
                            }
                            String string8 = ((com.huawei.tools.g.a.f)string5).bh;
                            string4 = ((com.huawei.tools.g.a.f)string5).bO;
                            bl = com.huawei.tools.h.d.dg.containsKey(string8);
                            if (bl) {
                                string4 = (String)com.huawei.tools.h.d.dg.get(string8);
                            }
                            if (!"bal".equals(string3)) break block23;
                            if (!bl) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                            }
                            if ((string4 = (com.huawei.tools.g.a.c)com.huawei.tools.h.d.df.get(string4)) == null) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
                            }
                            double d9 = ((com.huawei.tools.g.a.c)string4).bz - ((com.huawei.tools.g.a.f)string5).cf;
                            double d10 = ((com.huawei.tools.g.a.c)string4).by - Math.max(0.0, d9);
                            if (Math.abs(d5 - d3) > 0.01) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                            }
                            if (d10 < d3) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ag.key);
                            }
                            ((com.huawei.tools.g.a.c)string4).by -= d3;
                            ((com.huawei.tools.g.a.f)string5).ca = "paid";
                            ((com.huawei.tools.g.a.f)string5).cb = d4 + d3;
                            ((com.huawei.tools.g.a.f)string5).cd += d3;
                            ((com.huawei.tools.g.a.f)string5).ce = com.huawei.tools.h.d.de;
                            com.huawei.tools.g.b.a.a.a((long)com.huawei.tools.h.d.de, (String)"balpay", (double)d3);
                            break block24;
                        }
                        if (!bl) break block25;
                        double d11 = (string4 = (com.huawei.tools.g.a.c)com.huawei.tools.h.d.df.get(string4)) != null ? ((com.huawei.tools.g.a.c)string4).by : 0.0;
                        double d12 = string4 != null ? ((com.huawei.tools.g.a.c)string4).bz - ((com.huawei.tools.g.a.f)string5).cf : 0.0;
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
                        ((com.huawei.tools.g.a.c)string4).by = 0.0;
                        break block27;
                    }
                    double d14 = Math.min(d6, d3);
                    double d15 = d3 - d14;
                    if (Math.abs(d5 - d15) > 0.01) {
                        throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                    }
                    if (d14 > 0.0) {
                        ((com.huawei.tools.g.a.c)string4).by -= d14;
                        ((com.huawei.tools.g.a.f)string5).cd += d14;
                        com.huawei.tools.g.b.a.a.a((long)com.huawei.tools.h.d.de, (String)"balpay", (double)d14);
                    }
                    ((com.huawei.tools.g.a.f)string5).ca = "paid";
                    ((com.huawei.tools.g.a.f)string5).cb = d4 + d3;
                    ((com.huawei.tools.g.a.f)string5).ce = com.huawei.tools.h.d.de;
                    com.huawei.tools.g.b.a.a.a((long)com.huawei.tools.h.d.de, (String)"temppay", (double)d5);
                    break block24;
                }
                if (Math.abs(d5 - d3) > 0.01) {
                    throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                }
            }
            ((com.huawei.tools.g.a.f)string5).ca = "paid";
            ((com.huawei.tools.g.a.f)string5).cb = d4 + d3;
            ((com.huawei.tools.g.a.f)string5).ce = com.huawei.tools.h.d.de;
            com.huawei.tools.g.b.a.a.a((long)com.huawei.tools.h.d.de, (String)"temppay", (double)d3);
        }
        if (d2 > 0.0 && ((c)string).cH != null) {
            i.v((String)string2);
        }
        return "E000:Success";
    }

    public final String d(String string, String string2, String string3) {
        return c.d((String)string, (String)string2, (String)string3);
    }

    public final String h(String string) {
        return b.h((String)string);
    }

    public final String m(String string, String string2) {
        return this.cB.m(string, string2);
    }

    public final String i(String string) {
        String string2 = string;
        string = this.cB;
        if ((string2 = (com.huawei.tools.g.a.h)com.huawei.tools.h.d.dv.get(string2)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.as.key);
        }
        if (!"locked".equals(((com.huawei.tools.g.a.h)string2).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.at.key);
        }
        if (h.U((String)((com.huawei.tools.g.a.h)string2).bh)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.au.key);
        }
        h.b((com.huawei.tools.g.a.h)string2);
        ((com.huawei.tools.g.a.h)string2).bt = "canceled";
        string.q();
        return "E000:Success";
    }

    public final String n(String string, String string2) {
        String string3;
        String string4 = string2;
        string2 = string;
        if (StringUtils.isBlank((CharSequence)string2) || !string2.matches("r\\d{8}")) {
            return "E001:Fail";
        }
        if (StringUtils.isBlank((CharSequence)string4)) {
            return "E001:Fail";
        }
        switch (string4) {
            case "locked": {
                string3 = "locked";
                break;
            }
            case "used": {
                string3 = "used";
                break;
            }
            case "expired": {
                string3 = "expired";
                break;
            }
            case "cancel": {
                string3 = "canceled";
                break;
            }
            case "closed": {
                string3 = "closed";
                break;
            }
            default: {
                string3 = string = null;
            }
        }
        if (string3 == null) {
            return "E001:Fail";
        }
        if ((string2 = (com.huawei.tools.g.a.h)com.huawei.tools.h.d.dv.get(string2)) == null) {
            return "E001:Fail";
        }
        if (string.equals(((com.huawei.tools.g.a.h)string2).bt)) {
            return "E000:Success";
        }
        return com.huawei.tools.a.b.a((String)"E002", (String)"Verify_fail");
    }

    public final String j(String string) {
        return h.j((String)string);
    }

    public final String k(String string) {
        String string2;
        Object object;
        String string3 = string;
        if (StringUtils.isBlank((CharSequence)string3)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.I.key);
        }
        i.t();
        long l = com.huawei.tools.h.d.de;
        long l2 = l % 86400L;
        if (l2 < 32400L || l2 > 61200L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aC.key);
        }
        if (g.J((String)string3)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aM.key);
        }
        com.huawei.tools.g.a.f f2 = i.ac((String)string3);
        if (f2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aA.key);
        }
        String string4 = g.e((com.huawei.tools.g.a.f)f2);
        if (!"inpark".equals(string4) && !"parked".equals(string4)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aA.key);
        }
        string4 = (String)com.huawei.tools.h.d.dg.get(string3);
        if (string4 != null && (object = (com.huawei.tools.g.a.c)com.huawei.tools.h.d.df.get(string4)) != null && object.by < 0.0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aN.key);
        }
        if (i.ad((String)string3) > 0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aB.key);
        }
        if (g.K((String)string3)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aP.key);
        }
        object = g.I((String)string3);
        String string5 = object;
        if ("S".equals(string5)) {
            string = i.a((String[])i.cT, (int)com.huawei.tools.h.d.dy.get());
            if (string != null) {
                i.a((String[])i.cT, (String)string, (AtomicInteger)com.huawei.tools.h.d.dy);
                string2 = string;
            } else {
                string = i.a((String[])i.cU, (int)com.huawei.tools.h.d.dz.get());
                if (string != null) {
                    i.a((String[])i.cU, (String)string, (AtomicInteger)com.huawei.tools.h.d.dz);
                    string2 = string;
                } else {
                    string2 = i.a((String[])i.cV, (int)0);
                }
            }
        } else if ("M".equals(string5)) {
            string = i.a((String[])i.cU, (int)com.huawei.tools.h.d.dz.get());
            if (string != null) {
                i.a((String[])i.cU, (String)string, (AtomicInteger)com.huawei.tools.h.d.dz);
                string2 = string;
            } else {
                string2 = i.a((String[])i.cV, (int)0);
            }
        } else {
            string2 = string = "L".equals(string5) ? i.a((String[])i.cV, (int)0) : null;
        }
        if (string2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aD.key);
        }
        i.t();
        int n = com.huawei.tools.h.d.dA.incrementAndGet();
        String string6 = String.format("%03d", n);
        Object object2 = object;
        long l3 = l % 86400L;
        int n2 = (int)(com.huawei.tools.h.d.de / 86400L);
        Object object3 = LocalDate.of(2026, 1, 1).plusDays(n2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        n2 = (g.N((String)object3) ? false : ((n2 = (n2 + 4) % 7) == 0 || n2 == 6 ? true : i.X((String)object3))) ? 1 : (l3 >= 43200L && l3 < 50400L || l3 >= 57600L && l3 < 64800L ? 1 : 0);
        double[] dArray = (double[])i.cS.get(object2);
        object3 = dArray;
        double d2 = dArray == null ? 0.0 : (n2 != 0 ? (double)object3[1] : (double)object3[0]);
        k k2 = new k();
        new k().cr = string6;
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
        com.huawei.tools.h.d.dx.put(i.b((String)string6, (long)l), k2);
        i.g((com.huawei.tools.g.a.f)f2);
        if (i.aa((String)string).size() == 1) {
            i.d((String)string, (long)l);
        }
        return "E000:".concat(String.valueOf(string6));
    }

    public final String l(String string) {
        if (StringUtils.isBlank((CharSequence)string)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aE.key);
        }
        if ((string = i.W((String)string)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aE.key);
        }
        if (!"queuing".equals(((k)string).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aF.key);
        }
        if (((k)string).cv > 0L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aG.key);
        }
        long l = com.huawei.tools.h.d.de;
        double d2 = 0.0;
        if (l - ((k)string).bw > 600L) {
            d2 = 5.0;
        }
        ((k)string).cx = d2;
        ((k)string).bt = "cancel";
        i.a((k)string, (long)com.huawei.tools.h.d.de);
        i.d((String)((k)string).bN, (long)l);
        return "E000:Success";
    }

    public final String m(String string) {
        if (StringUtils.isBlank((CharSequence)string)) {
            return "E001:Fail";
        }
        if ((string = i.W((String)string)) == null) {
            return "E001:Fail";
        }
        return "E000:" + ((k)string).bt;
    }

    public final String n(String string) {
        String string2 = string;
        if (StringUtils.isBlank((CharSequence)string2)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aE.key);
        }
        string = i.W((String)string2);
        if (string == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aE.key);
        }
        if (!"queuing".equals(((k)string).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aH.key);
        }
        String string3 = ((k)string).bK;
        if (string3 != null && (string3 = (com.huawei.tools.g.a.f)com.huawei.tools.h.d.di.get(string3)) != null && ((com.huawei.tools.g.a.f)string3).bN != null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aK.key);
        }
        string3 = ((k)string).bN;
        List list = i.aa((String)string3);
        if (list.isEmpty() || !((k)list.get((int)0)).cr.equals(string2)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aI.key);
        }
        if (i.ab((String)string3) != null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aJ.key);
        }
        long l = com.huawei.tools.h.d.de;
        ((k)string).bt = "washing";
        ((k)string).ct = l;
        ((k)string).cv = 0L;
        string2 = (com.huawei.tools.g.a.g)com.huawei.tools.h.d.dl.get(string3);
        if (string2 != null) {
            ((com.huawei.tools.g.a.g)string2).bt = "U";
            ((com.huawei.tools.g.a.g)string2).bh = ((k)string).bh;
        }
        return "E000:Success";
    }

    public final String o(String string) {
        String string2 = string;
        if (StringUtils.isBlank((CharSequence)string2)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aE.key);
        }
        if ((string2 = i.W((String)string2)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aE.key);
        }
        if (!"washing".equals(((k)string2).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aL.key);
        }
        long l = com.huawei.tools.h.d.de;
        ((k)string2).bt = "closed";
        ((k)string2).cu = l;
        string = ((k)string2).bN;
        com.huawei.tools.g.a.g g2 = (com.huawei.tools.g.a.g)com.huawei.tools.h.d.dl.get(string);
        if (g2 != null) {
            g2.bt = "F";
            g2.bh = null;
        }
        i.a((k)string2, (long)com.huawei.tools.h.d.de);
        i.d((String)string, (long)l);
        return "E000:Success";
    }

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

    public final String p(String charSequence) {
        if (StringUtils.isBlank((CharSequence)charSequence)) {
            charSequence = new StringBuilder("E000:");
            for (int i2 = 0; i2 < i.cW.size(); ++i2) {
                if (i2 > 0) {
                    ((StringBuilder)charSequence).append(";");
                }
                ((StringBuilder)charSequence).append(i.af((String)((String)i.cW.get(i2))));
            }
            return ((StringBuilder)charSequence).toString();
        }
        if (!i.cW.contains(charSequence)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aO.key);
        }
        return "E000:" + i.af((String)charSequence);
    }
}
