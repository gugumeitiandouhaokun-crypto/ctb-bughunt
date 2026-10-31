/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.huawei.tools.a.a
 *  com.huawei.tools.b.a
 *  com.huawei.tools.e.a
 *  com.huawei.tools.g.a.a
 *  com.huawei.tools.g.a.b
 *  com.huawei.tools.g.a.c
 *  com.huawei.tools.g.a.e
 *  com.huawei.tools.g.a.f
 *  com.huawei.tools.g.a.g
 *  com.huawei.tools.g.a.i
 *  com.huawei.tools.g.b.a
 *  com.huawei.tools.g.b.a.a
 *  com.huawei.tools.g.b.a.b
 *  com.huawei.tools.g.b.a.c
 *  com.huawei.tools.g.b.a.d
 *  com.huawei.tools.g.b.a.e
 *  com.huawei.tools.g.b.a.g
 *  com.huawei.tools.h.d
 */
package com.huawei.tools.g.b.a;

import com.huawei.tools.g.a.i;
import com.huawei.tools.g.b.a;
import com.huawei.tools.g.b.a.b;
import com.huawei.tools.g.b.a.c;
import com.huawei.tools.g.b.a.d;
import com.huawei.tools.g.b.a.e;
import com.huawei.tools.g.b.a.g;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public final class f
implements a {
    private final b bP = new b();
    private final e bQ;
    private final d bR = new d();
    private final com.huawei.tools.g.b.a.a bL = new com.huawei.tools.g.b.a.a();
    private final c bM = new c(this.bL);

    public f() {
        this.bQ = new e(this.bM);
    }

    public final String c() {
        com.huawei.tools.h.d.clear();
        e.f();
        com.huawei.tools.e.a.info((String)com.huawei.tools.a.a.g.key);
        return "E000:Success";
    }

    public final String a(String string, int n) {
        string = this.bQ.a(string, n);
        b.e();
        return string;
    }

    public final String b(String string, String string2) {
        return b.b((String)string, (String)string2);
    }

    public final String c(String string, String string2) {
        return b.c((String)string, (String)string2);
    }

    public final String d(String string, String string2) {
        return b.d((String)string, (String)string2);
    }

    public final String e(String string, String string2) {
        return b.e((String)string, (String)string2);
    }

    public final String f(String string, String string2) {
        return b.f((String)string, (String)string2);
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
        g.t((String)string2);
        string = (com.huawei.tools.g.a.b)com.huawei.tools.h.d.cl.get(string4);
        if (string == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.K.key);
        }
        if (g.s((String)string2)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.X.key);
        }
        boolean bl = com.huawei.tools.h.d.ce.containsKey(string2);
        if (!bl && g.m() >= 500) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.F.key);
        }
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        while (!com.huawei.tools.h.d.ck.add(string3 = "p" + String.format("%016d", l = Math.abs(((Random)threadLocalRandom).nextLong()) % 10000000000000000L))) {
        }
        long l2 = com.huawei.tools.h.d.cb;
        threadLocalRandom = new com.huawei.tools.g.a.f();
        new com.huawei.tools.g.a.f().bd = string3;
        ((com.huawei.tools.g.a.f)threadLocalRandom).aB = string2;
        ((com.huawei.tools.g.a.f)threadLocalRandom).aD = "P1";
        ((com.huawei.tools.g.a.f)threadLocalRandom).aC = string4;
        ((com.huawei.tools.g.a.f)threadLocalRandom).be = l2;
        ((com.huawei.tools.g.a.f)threadLocalRandom).bf = 0L;
        ((com.huawei.tools.g.a.f)threadLocalRandom).bl = com.huawei.tools.h.d.cn.incrementAndGet();
        ((com.huawei.tools.g.a.f)threadLocalRandom).bo = g.a((com.huawei.tools.g.a.b)string);
        com.huawei.tools.h.d.cg.put(string3, threadLocalRandom);
        if (bl) {
            string = (String)com.huawei.tools.h.d.ce.get(string2);
        } else {
            string4 = (i)com.huawei.tools.h.d.cf.get(string2);
            if (string4 != null && "invalid".equals(((i)string4).aN)) {
                com.huawei.tools.h.d.cf.remove(string2);
            }
            string = g.l();
            string4 = new i();
            new i().bh = string;
            ((i)string4).aB = string2;
            ((i)string4).bd = string3;
            ((i)string4).aN = "active";
            com.huawei.tools.h.d.cf.put(string2, string4);
        }
        ((com.huawei.tools.g.a.f)threadLocalRandom).bh = string;
        if (bl && (string4 = (com.huawei.tools.g.a.c)com.huawei.tools.h.d.cd.get(string)) != null) {
            ((com.huawei.tools.g.a.f)threadLocalRandom).bx = ((com.huawei.tools.g.a.c)string4).aT;
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

    public final String j(String string, String string2) {
        Object object;
        String string3 = string;
        string = string2;
        string2 = string3;
        if ((string2 = (com.huawei.tools.g.a.f)com.huawei.tools.h.d.cg.get(string2)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (((com.huawei.tools.g.a.f)string2).bf > 0L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (g.e((com.huawei.tools.g.a.f)string2)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.Z.key);
        }
        if (((com.huawei.tools.g.a.f)string2).bj > 0L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        String string4 = ((com.huawei.tools.g.a.f)string2).aB;
        com.huawei.tools.g.a.g g2 = (com.huawei.tools.g.a.g)com.huawei.tools.h.d.cj.get(string);
        if (g2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.N.key);
        }
        if (!"F".equals(g2.aN)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.O.key);
        }
        String string5 = g.v((String)string4);
        String string6 = g2.bB;
        String string7 = string5;
        if (!("L".equals(string6) ? true : ("M".equals(string6) ? "M".equals(string7) || "S".equals(string7) : ("S".equals(string6) ? "S".equals(string7) : false)))) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.P.key);
        }
        string7 = ((com.huawei.tools.g.a.f)string2).bs;
        if (string7 != null && !string7.equals(g2.bB)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.U.key);
        }
        g2.aN = "U";
        g2.aB = string4;
        ((com.huawei.tools.g.a.f)string2).bg = string;
        boolean bl = "paid".equals(((com.huawei.tools.g.a.f)string2).bt);
        if (((com.huawei.tools.g.a.f)string2).bq <= 0L) {
            ((com.huawei.tools.g.a.f)string2).bq = com.huawei.tools.h.d.cb;
        }
        if (((com.huawei.tools.g.a.f)string2).bs == null) {
            ((com.huawei.tools.g.a.f)string2).bs = g2.bB;
        }
        ((com.huawei.tools.g.a.f)string2).br = 0L;
        ((com.huawei.tools.g.a.f)string2).bk = false;
        ((com.huawei.tools.g.a.f)string2).aV = string5;
        if (!bl) {
            ((com.huawei.tools.g.a.f)string2).bt = "unpaid";
            ((com.huawei.tools.g.a.f)string2).bu = 0.0;
            ((com.huawei.tools.g.a.f)string2).bv = 0.0;
            ((com.huawei.tools.g.a.f)string2).bw = 0L;
        }
        if ((object = (com.huawei.tools.g.a.e)com.huawei.tools.h.d.ci.get(g2.aD)) != null && (object = (AtomicInteger)((com.huawei.tools.g.a.e)object).bb.get(g2.bB)) != null) {
            ((AtomicInteger)object).incrementAndGet();
        }
        ((com.huawei.tools.g.a.f)string2).bi = 0L;
        e.k();
        return "E000:Success";
    }

    public final String b(String string) {
        if ((string = (com.huawei.tools.g.a.f)com.huawei.tools.h.d.cg.get(string)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (((com.huawei.tools.g.a.f)string).bf > 0L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (g.e((com.huawei.tools.g.a.f)string)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.Z.key);
        }
        Object object = ((com.huawei.tools.g.a.f)string).aB;
        String string2 = ((com.huawei.tools.g.a.f)string).bg;
        if (string2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.N.key);
        }
        com.huawei.tools.g.a.g g2 = (com.huawei.tools.g.a.g)com.huawei.tools.h.d.cj.get(string2);
        if (g2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.N.key);
        }
        if (!((String)object).equals(g2.aB)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.R.key);
        }
        g2.aN = "F";
        g2.aB = null;
        object = (com.huawei.tools.g.a.e)com.huawei.tools.h.d.ci.get(g2.aD);
        if (object != null && (object = (AtomicInteger)((com.huawei.tools.g.a.e)object).bb.get(g2.bB)) != null) {
            ((AtomicInteger)object).decrementAndGet();
        }
        ((com.huawei.tools.g.a.f)string).bg = null;
        ((com.huawei.tools.g.a.f)string).bi = com.huawei.tools.h.d.cb;
        ((com.huawei.tools.g.a.f)string).bk = true;
        ((com.huawei.tools.g.a.f)string).br = com.huawei.tools.h.d.cb;
        e.k();
        return "E000:".concat(String.valueOf(string2));
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
        if (!com.huawei.tools.h.d.co.containsKey(string)) {
            com.huawei.tools.g.a.a a2 = new com.huawei.tools.g.a.a(string);
            com.huawei.tools.h.d.co.put(string, a2);
        }
        return "E000:Success";
    }

    public final String g(String string) {
        if (string == null || string.isEmpty()) {
            return "E001:Fail";
        }
        com.huawei.tools.h.d.co.remove(string);
        return "E000:Success";
    }

    public final String b(String string, int n) {
        return com.huawei.tools.g.b.a.a.b((String)string, (int)n);
    }

    public final String a(String string, String string2, int n) {
        string = (com.huawei.tools.g.a.c)com.huawei.tools.h.d.cd.get(string);
        string2 = (com.huawei.tools.g.a.c)com.huawei.tools.h.d.cd.get(string2);
        if (string == null || string2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(((com.huawei.tools.g.a.c)string).aN)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (!"active".equals(((com.huawei.tools.g.a.c)string2).aN)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (n <= 0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.J.key);
        }
        double d2 = 0.0;
        if (((com.huawei.tools.g.a.c)string).aM != null) {
            for (String string3 : ((com.huawei.tools.g.a.c)string).aM) {
                for (com.huawei.tools.g.a.f f2 : com.huawei.tools.h.d.cg.values()) {
                    double d3;
                    if (!string3.equals(f2.aB) || f2.bf != 0L) continue;
                    double d4 = ((com.huawei.tools.g.a.c)string).aT - f2.bx;
                    if (!(d3 > d2)) continue;
                    d2 = d4;
                }
            }
        }
        if (((com.huawei.tools.g.a.c)string).aS - d2 < (double)n) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ag.key);
        }
        if (((com.huawei.tools.g.a.c)string2).aS + (double)n > 5000.0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.af.key);
        }
        ((com.huawei.tools.g.a.c)string).aS -= (double)n;
        ((com.huawei.tools.g.a.c)string2).aS += (double)n;
        return "E000:Success";
    }

    public final String l(String string, String string2) {
        return com.huawei.tools.g.b.a.a.l((String)string, (String)string2);
    }

    public final String b(String string, String string2, int n) {
        return com.huawei.tools.g.b.a.a.b((String)string, (String)string2, (int)n);
    }

    public final String c(String string, String string2, String string3) {
        block22: {
            double d2;
            double d3;
            block25: {
                double d4;
                block23: {
                    double d5;
                    block24: {
                        double d6;
                        boolean bl;
                        String string4;
                        block21: {
                            string4 = string3;
                            string3 = string2;
                            string2 = string;
                            if (string4 == null || !string4.matches("^-?\\d+\\.\\d{2}$")) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                            }
                            d4 = Double.parseDouble(string4);
                            if ((string2 = (com.huawei.tools.g.a.f)com.huawei.tools.h.d.cg.get(string2)) == null) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
                            }
                            if (((com.huawei.tools.g.a.f)string2).bf > 0L) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
                            }
                            d3 = ((com.huawei.tools.g.a.f)string2).bu;
                            if ("paid".equals(((com.huawei.tools.g.a.f)string2).bt) && ((com.huawei.tools.g.a.f)string2).bw > 0L) {
                                double d7;
                                if (com.huawei.tools.h.d.cb - ((com.huawei.tools.g.a.f)string2).bw > 900L) {
                                    String string5 = string2;
                                    d7 = c.a((com.huawei.tools.g.a.f)string5, (long)((com.huawei.tools.g.a.f)string5).bw);
                                } else {
                                    d7 = 0.0;
                                }
                                d2 = d7;
                            } else {
                                d2 = c.b((com.huawei.tools.g.a.f)string2) - d3;
                            }
                            if (d2 <= 0.0) {
                                String string6;
                                boolean bl2 = false;
                                string4 = ((com.huawei.tools.g.a.f)string2).aB;
                                if (com.huawei.tools.h.d.ce.containsKey(string4) && (string4 = (com.huawei.tools.g.a.c)com.huawei.tools.h.d.cd.get(string6 = (String)com.huawei.tools.h.d.ce.get(string4))) != null && ((com.huawei.tools.g.a.c)string4).aS < 0.0) {
                                    bl2 = true;
                                }
                                if (!bl2) {
                                    throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                                }
                            }
                            string = ((com.huawei.tools.g.a.f)string2).aB;
                            string4 = ((com.huawei.tools.g.a.f)string2).bh;
                            bl = com.huawei.tools.h.d.ce.containsKey(string);
                            if (bl) {
                                string4 = (String)com.huawei.tools.h.d.ce.get(string);
                            }
                            if (!"bal".equals(string3)) break block21;
                            if (!bl) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                            }
                            if ((string4 = (com.huawei.tools.g.a.c)com.huawei.tools.h.d.cd.get(string4)) == null) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
                            }
                            double d8 = ((com.huawei.tools.g.a.c)string4).aT - ((com.huawei.tools.g.a.f)string2).bx;
                            double d9 = ((com.huawei.tools.g.a.c)string4).aS - Math.max(0.0, d8);
                            if (Math.abs(d4 - d2) > 0.01) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                            }
                            if (d9 < d2) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ag.key);
                            }
                            ((com.huawei.tools.g.a.c)string4).aS -= d2;
                            ((com.huawei.tools.g.a.f)string2).bt = "paid";
                            ((com.huawei.tools.g.a.f)string2).bu = d3 + d2;
                            ((com.huawei.tools.g.a.f)string2).bv += d2;
                            ((com.huawei.tools.g.a.f)string2).bw = com.huawei.tools.h.d.cb;
                            com.huawei.tools.g.b.a.a.a((long)com.huawei.tools.h.d.cb, (String)"balpay", (double)d2);
                            break block22;
                        }
                        if (!bl) break block23;
                        double d10 = (string4 = (com.huawei.tools.g.a.c)com.huawei.tools.h.d.cd.get(string4)) != null ? ((com.huawei.tools.g.a.c)string4).aS : 0.0;
                        double d11 = string4 != null ? ((com.huawei.tools.g.a.c)string4).aT - ((com.huawei.tools.g.a.f)string2).bx : 0.0;
                        d5 = d10 - Math.max(0.0, d11);
                        if (d6 < 0.0) {
                            d5 = 0.0;
                        }
                        if (d5 >= d2 && d10 >= 0.0) {
                            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                        }
                        if (!(d10 < 0.0)) break block24;
                        double d12 = d2 - d10;
                        if (Math.abs(d4 - d12) > 0.01) {
                            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                        }
                        ((com.huawei.tools.g.a.c)string4).aS = 0.0;
                        break block25;
                    }
                    double d13 = Math.min(d5, d2);
                    double d14 = d2 - d13;
                    if (Math.abs(d4 - d14) > 0.01) {
                        throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                    }
                    if (d13 > 0.0) {
                        ((com.huawei.tools.g.a.c)string4).aS -= d13;
                        ((com.huawei.tools.g.a.f)string2).bv += d13;
                        com.huawei.tools.g.b.a.a.a((long)com.huawei.tools.h.d.cb, (String)"balpay", (double)d13);
                    }
                    ((com.huawei.tools.g.a.f)string2).bt = "paid";
                    ((com.huawei.tools.g.a.f)string2).bu = d3 + d2;
                    ((com.huawei.tools.g.a.f)string2).bw = com.huawei.tools.h.d.cb;
                    com.huawei.tools.g.b.a.a.a((long)com.huawei.tools.h.d.cb, (String)"temppay", (double)d4);
                    break block22;
                }
                if (Math.abs(d4 - d2) > 0.01) {
                    throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                }
            }
            ((com.huawei.tools.g.a.f)string2).bt = "paid";
            ((com.huawei.tools.g.a.f)string2).bu = d3 + d2;
            ((com.huawei.tools.g.a.f)string2).bw = com.huawei.tools.h.d.cb;
            com.huawei.tools.g.b.a.a.a((long)com.huawei.tools.h.d.cb, (String)"temppay", (double)d2);
        }
        return "E000:Success";
    }

    public final String d(String string, String string2, String string3) {
        return c.d((String)string, (String)string2, (String)string3);
    }

    public final String h(String string) {
        return b.h((String)string);
    }
}
