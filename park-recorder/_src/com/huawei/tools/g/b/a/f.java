/*
 * Decompiled with CFR 0.152.
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

    @Override
    public final String c() {
        com.huawei.tools.h.d.clear();
        e.f();
        com.huawei.tools.e.a.info(com.huawei.tools.a.a.g.key);
        return "E000:Success";
    }

    @Override
    public final String a(String string, int n) {
        string = this.bQ.a(string, n);
        b.e();
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
    public final String d(String string, String string2) {
        return b.d(string, string2);
    }

    @Override
    public final String e(String string, String string2) {
        return b.e(string, string2);
    }

    @Override
    public final String f(String string, String string2) {
        return b.f(string, string2);
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
        g.t(string);
        object = com.huawei.tools.h.d.cl.get(object2);
        if (object == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.K.key);
        }
        if (g.s(string)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.X.key);
        }
        boolean bl = com.huawei.tools.h.d.ce.containsKey(string);
        if (!bl && g.m() >= 500) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.F.key);
        }
        Object object3 = ThreadLocalRandom.current();
        while (!com.huawei.tools.h.d.ck.add(string2 = "p" + String.format("%016d", l = Math.abs(((Random)object3).nextLong()) % 10000000000000000L))) {
        }
        long l2 = com.huawei.tools.h.d.cb;
        object3 = new com.huawei.tools.g.a.f();
        new com.huawei.tools.g.a.f().bd = string2;
        ((com.huawei.tools.g.a.f)object3).aB = string;
        ((com.huawei.tools.g.a.f)object3).aD = "P1";
        ((com.huawei.tools.g.a.f)object3).aC = object2;
        ((com.huawei.tools.g.a.f)object3).be = l2;
        ((com.huawei.tools.g.a.f)object3).bf = 0L;
        ((com.huawei.tools.g.a.f)object3).bl = com.huawei.tools.h.d.cn.incrementAndGet();
        ((com.huawei.tools.g.a.f)object3).bo = g.a((com.huawei.tools.g.a.b)object);
        com.huawei.tools.h.d.cg.put(string2, (com.huawei.tools.g.a.f)object3);
        if (bl) {
            object = com.huawei.tools.h.d.ce.get(string);
        } else {
            object2 = com.huawei.tools.h.d.cf.get(string);
            if (object2 != null && "invalid".equals(((i)object2).aN)) {
                com.huawei.tools.h.d.cf.remove(string);
            }
            object = g.l();
            object2 = new i();
            new i().bh = object;
            ((i)object2).aB = string;
            ((i)object2).bd = string2;
            ((i)object2).aN = "active";
            com.huawei.tools.h.d.cf.put(string, (i)object2);
        }
        ((com.huawei.tools.g.a.f)object3).bh = object;
        if (bl && (object2 = com.huawei.tools.h.d.cd.get(object)) != null) {
            ((com.huawei.tools.g.a.f)object3).bx = ((com.huawei.tools.g.a.c)object2).aT;
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
    public final String j(String string, String object) {
        Object object2;
        String string2 = string;
        string = object;
        object = string2;
        if ((object = com.huawei.tools.h.d.cg.get(object)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (((com.huawei.tools.g.a.f)object).bf > 0L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (g.e((com.huawei.tools.g.a.f)object)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.Z.key);
        }
        if (((com.huawei.tools.g.a.f)object).bj > 0L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        String string3 = ((com.huawei.tools.g.a.f)object).aB;
        com.huawei.tools.g.a.g g2 = com.huawei.tools.h.d.cj.get(string);
        if (g2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.N.key);
        }
        if (!"F".equals(g2.aN)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.O.key);
        }
        String string4 = g.v(string3);
        String string5 = g2.bB;
        String string6 = string4;
        if (!("L".equals(string5) ? true : ("M".equals(string5) ? "M".equals(string6) || "S".equals(string6) : ("S".equals(string5) ? "S".equals(string6) : false)))) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.P.key);
        }
        string6 = ((com.huawei.tools.g.a.f)object).bs;
        if (string6 != null && !string6.equals(g2.bB)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.U.key);
        }
        g2.aN = "U";
        g2.aB = string3;
        ((com.huawei.tools.g.a.f)object).bg = string;
        boolean bl = "paid".equals(((com.huawei.tools.g.a.f)object).bt);
        if (((com.huawei.tools.g.a.f)object).bq <= 0L) {
            ((com.huawei.tools.g.a.f)object).bq = com.huawei.tools.h.d.cb;
        }
        if (((com.huawei.tools.g.a.f)object).bs == null) {
            ((com.huawei.tools.g.a.f)object).bs = g2.bB;
        }
        ((com.huawei.tools.g.a.f)object).br = 0L;
        ((com.huawei.tools.g.a.f)object).bk = false;
        ((com.huawei.tools.g.a.f)object).aV = string4;
        if (!bl) {
            ((com.huawei.tools.g.a.f)object).bt = "unpaid";
            ((com.huawei.tools.g.a.f)object).bu = 0.0;
            ((com.huawei.tools.g.a.f)object).bv = 0.0;
            ((com.huawei.tools.g.a.f)object).bw = 0L;
        }
        if ((object2 = com.huawei.tools.h.d.ci.get(g2.aD)) != null && (object2 = ((com.huawei.tools.g.a.e)object2).bb.get(g2.bB)) != null) {
            ((AtomicInteger)object2).incrementAndGet();
        }
        ((com.huawei.tools.g.a.f)object).bi = 0L;
        e.k();
        return "E000:Success";
    }

    @Override
    public final String b(String object) {
        if ((object = com.huawei.tools.h.d.cg.get(object)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (((com.huawei.tools.g.a.f)object).bf > 0L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (g.e((com.huawei.tools.g.a.f)object)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.Z.key);
        }
        Object object2 = ((com.huawei.tools.g.a.f)object).aB;
        String string = ((com.huawei.tools.g.a.f)object).bg;
        if (string == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.N.key);
        }
        com.huawei.tools.g.a.g g2 = com.huawei.tools.h.d.cj.get(string);
        if (g2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.N.key);
        }
        if (!((String)object2).equals(g2.aB)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.R.key);
        }
        g2.aN = "F";
        g2.aB = null;
        object2 = com.huawei.tools.h.d.ci.get(g2.aD);
        if (object2 != null && (object2 = ((com.huawei.tools.g.a.e)object2).bb.get(g2.bB)) != null) {
            ((AtomicInteger)object2).decrementAndGet();
        }
        ((com.huawei.tools.g.a.f)object).bg = null;
        ((com.huawei.tools.g.a.f)object).bi = com.huawei.tools.h.d.cb;
        ((com.huawei.tools.g.a.f)object).bk = true;
        ((com.huawei.tools.g.a.f)object).br = com.huawei.tools.h.d.cb;
        e.k();
        return "E000:".concat(String.valueOf(string));
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
        if (!com.huawei.tools.h.d.co.containsKey(string)) {
            com.huawei.tools.g.a.a a2 = new com.huawei.tools.g.a.a(string);
            com.huawei.tools.h.d.co.put(string, a2);
        }
        return "E000:Success";
    }

    @Override
    public final String g(String string) {
        if (string == null || string.isEmpty()) {
            return "E001:Fail";
        }
        com.huawei.tools.h.d.co.remove(string);
        return "E000:Success";
    }

    @Override
    public final String b(String string, int n) {
        return com.huawei.tools.g.b.a.a.b(string, n);
    }

    @Override
    public final String a(String object, String object2, int n) {
        object = com.huawei.tools.h.d.cd.get(object);
        object2 = com.huawei.tools.h.d.cd.get(object2);
        if (object == null || object2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(((com.huawei.tools.g.a.c)object).aN)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (!"active".equals(((com.huawei.tools.g.a.c)object2).aN)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (n <= 0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.J.key);
        }
        double d2 = 0.0;
        if (((com.huawei.tools.g.a.c)object).aM != null) {
            for (String string : ((com.huawei.tools.g.a.c)object).aM) {
                for (com.huawei.tools.g.a.f f2 : com.huawei.tools.h.d.cg.values()) {
                    double d3;
                    if (!string.equals(f2.aB) || f2.bf != 0L) continue;
                    double d4 = ((com.huawei.tools.g.a.c)object).aT - f2.bx;
                    if (!(d3 > d2)) continue;
                    d2 = d4;
                }
            }
        }
        if (((com.huawei.tools.g.a.c)object).aS - d2 < (double)n) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ag.key);
        }
        if (((com.huawei.tools.g.a.c)object2).aS + (double)n > 5000.0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.af.key);
        }
        ((com.huawei.tools.g.a.c)object).aS -= (double)n;
        ((com.huawei.tools.g.a.c)object2).aS += (double)n;
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
    public final String c(String string, String object, String string2) {
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
                        Object object2;
                        block21: {
                            object2 = string2;
                            string2 = object;
                            object = string;
                            if (object2 == null || !((String)object2).matches("^-?\\d+\\.\\d{2}$")) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                            }
                            d4 = Double.parseDouble((String)object2);
                            if ((object = com.huawei.tools.h.d.cg.get(object)) == null) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
                            }
                            if (((com.huawei.tools.g.a.f)object).bf > 0L) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
                            }
                            d3 = ((com.huawei.tools.g.a.f)object).bu;
                            if ("paid".equals(((com.huawei.tools.g.a.f)object).bt) && ((com.huawei.tools.g.a.f)object).bw > 0L) {
                                double d7;
                                if (com.huawei.tools.h.d.cb - ((com.huawei.tools.g.a.f)object).bw > 900L) {
                                    Object object3 = object;
                                    d7 = c.a((com.huawei.tools.g.a.f)object3, ((com.huawei.tools.g.a.f)object3).bw);
                                } else {
                                    d7 = 0.0;
                                }
                                d2 = d7;
                            } else {
                                d2 = c.b((com.huawei.tools.g.a.f)object) - d3;
                            }
                            if (d2 <= 0.0) {
                                String string3;
                                boolean bl2 = false;
                                object2 = ((com.huawei.tools.g.a.f)object).aB;
                                if (com.huawei.tools.h.d.ce.containsKey(object2) && (object2 = com.huawei.tools.h.d.cd.get(string3 = com.huawei.tools.h.d.ce.get(object2))) != null && ((com.huawei.tools.g.a.c)object2).aS < 0.0) {
                                    bl2 = true;
                                }
                                if (!bl2) {
                                    throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                                }
                            }
                            string = ((com.huawei.tools.g.a.f)object).aB;
                            object2 = ((com.huawei.tools.g.a.f)object).bh;
                            bl = com.huawei.tools.h.d.ce.containsKey(string);
                            if (bl) {
                                object2 = com.huawei.tools.h.d.ce.get(string);
                            }
                            if (!"bal".equals(string2)) break block21;
                            if (!bl) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                            }
                            if ((object2 = com.huawei.tools.h.d.cd.get(object2)) == null) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
                            }
                            double d8 = ((com.huawei.tools.g.a.c)object2).aT - ((com.huawei.tools.g.a.f)object).bx;
                            double d9 = ((com.huawei.tools.g.a.c)object2).aS - Math.max(0.0, d8);
                            if (Math.abs(d4 - d2) > 0.01) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                            }
                            if (d9 < d2) {
                                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ag.key);
                            }
                            ((com.huawei.tools.g.a.c)object2).aS -= d2;
                            ((com.huawei.tools.g.a.f)object).bt = "paid";
                            ((com.huawei.tools.g.a.f)object).bu = d3 + d2;
                            ((com.huawei.tools.g.a.f)object).bv += d2;
                            ((com.huawei.tools.g.a.f)object).bw = com.huawei.tools.h.d.cb;
                            com.huawei.tools.g.b.a.a.a(com.huawei.tools.h.d.cb, "balpay", d2);
                            break block22;
                        }
                        if (!bl) break block23;
                        double d10 = (object2 = com.huawei.tools.h.d.cd.get(object2)) != null ? ((com.huawei.tools.g.a.c)object2).aS : 0.0;
                        double d11 = object2 != null ? ((com.huawei.tools.g.a.c)object2).aT - ((com.huawei.tools.g.a.f)object).bx : 0.0;
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
                        ((com.huawei.tools.g.a.c)object2).aS = 0.0;
                        break block25;
                    }
                    double d13 = Math.min(d5, d2);
                    double d14 = d2 - d13;
                    if (Math.abs(d4 - d14) > 0.01) {
                        throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                    }
                    if (d13 > 0.0) {
                        ((com.huawei.tools.g.a.c)object2).aS -= d13;
                        ((com.huawei.tools.g.a.f)object).bv += d13;
                        com.huawei.tools.g.b.a.a.a(com.huawei.tools.h.d.cb, "balpay", d13);
                    }
                    ((com.huawei.tools.g.a.f)object).bt = "paid";
                    ((com.huawei.tools.g.a.f)object).bu = d3 + d2;
                    ((com.huawei.tools.g.a.f)object).bw = com.huawei.tools.h.d.cb;
                    com.huawei.tools.g.b.a.a.a(com.huawei.tools.h.d.cb, "temppay", d4);
                    break block22;
                }
                if (Math.abs(d4 - d2) > 0.01) {
                    throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.aj.key);
                }
            }
            ((com.huawei.tools.g.a.f)object).bt = "paid";
            ((com.huawei.tools.g.a.f)object).bu = d3 + d2;
            ((com.huawei.tools.g.a.f)object).bw = com.huawei.tools.h.d.cb;
            com.huawei.tools.g.b.a.a.a(com.huawei.tools.h.d.cb, "temppay", d2);
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
}

