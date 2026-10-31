/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.b.a;

import com.huawei.tools.g.a.a;
import com.huawei.tools.g.a.b;
import com.huawei.tools.g.a.f;
import com.huawei.tools.g.a.j;
import com.huawei.tools.g.b.a.c;
import com.huawei.tools.g.b.a.g;
import com.huawei.tools.g.b.a.h;
import com.huawei.tools.h.d;
import java.lang.invoke.LambdaMetafactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

final class e {
    private final c cI;
    private final h cB;
    Runnable cJ;
    private static final Set<String> cK = new HashSet<String>(Arrays.asList("2026-01-01", "2026-01-02", "2026-01-03", "2026-02-15", "2026-02-16", "2026-02-17", "2026-02-18", "2026-02-19", "2026-02-20", "2026-02-21", "2026-02-22", "2026-02-23", "2026-04-04", "2026-04-05", "2026-04-06", "2026-05-01", "2026-05-02", "2026-05-03", "2026-05-04", "2026-05-05", "2026-06-19", "2026-06-20", "2026-06-21", "2026-09-25", "2026-09-26", "2026-09-27", "2026-10-01", "2026-10-02", "2026-10-03", "2026-10-04", "2026-10-05", "2026-10-06", "2026-10-07"));

    e(c c2, h h2) {
        this.cI = c2;
        this.cB = h2;
    }

    static void g() {
        com.huawei.tools.g.a.g g2;
        int n;
        com.huawei.tools.g.a.e e2 = new com.huawei.tools.g.a.e();
        new com.huawei.tools.g.a.e().bj = "P1";
        e2.bE = 10;
        e2.bF = 200;
        e2.bG = 70;
        e2.bH = new HashMap<String, AtomicInteger>();
        e2.bI = new HashMap<String, AtomicInteger>();
        e2.bJ = new HashMap<String, AtomicInteger>();
        e2.bH.put("L", new AtomicInteger(0));
        e2.bH.put("M", new AtomicInteger(0));
        e2.bH.put("S", new AtomicInteger(0));
        e2.bI.put("L", new AtomicInteger(0));
        e2.bI.put("M", new AtomicInteger(0));
        e2.bI.put("S", new AtomicInteger(0));
        e2.bJ.put("L", new AtomicInteger(0));
        e2.bJ.put("M", new AtomicInteger(0));
        e2.bJ.put("S", new AtomicInteger(0));
        d.dj.put("P1", e2);
        for (n = 1; n <= 10; ++n) {
            g2 = new com.huawei.tools.g.a.g();
            new com.huawei.tools.g.a.g().bN = String.format("L%03d", n);
            g2.bj = "P1";
            g2.cl = "L";
            g2.bt = "F";
            d.dk.put(g2.bN, g2);
        }
        for (n = 1; n <= 200; ++n) {
            g2 = new com.huawei.tools.g.a.g();
            new com.huawei.tools.g.a.g().bN = String.format("M%03d", n);
            g2.bj = "P1";
            g2.cl = "M";
            g2.bt = "F";
            d.dk.put(g2.bN, g2);
        }
        for (n = 1; n <= 70; ++n) {
            g2 = new com.huawei.tools.g.a.g();
            new com.huawei.tools.g.a.g().bN = String.format("S%03d", n);
            g2.bj = "P1";
            g2.cl = "S";
            g2.bt = "F";
            d.dk.put(g2.bN, g2);
        }
        for (n = 1; n <= 3; ++n) {
            g2 = new com.huawei.tools.g.a.g();
            new com.huawei.tools.g.a.g().bN = "WS".concat(String.valueOf(n));
            g2.bj = "P1";
            g2.cl = "S";
            g2.bt = "F";
            d.dl.put(g2.bN, g2);
        }
        for (n = 1; n <= 2; ++n) {
            g2 = new com.huawei.tools.g.a.g();
            new com.huawei.tools.g.a.g().bN = "WM".concat(String.valueOf(n));
            g2.bj = "P1";
            g2.cl = "M";
            g2.bt = "F";
            d.dl.put(g2.bN, g2);
        }
        String[] stringArray = new com.huawei.tools.g.a.g();
        new com.huawei.tools.g.a.g().bN = "WL1";
        stringArray.bj = "P1";
        stringArray.cl = "L";
        stringArray.bt = "F";
        d.dl.put(stringArray.bN, (com.huawei.tools.g.a.g)stringArray);
        stringArray = new String[]{"G1", "G2", "G3", "G4"};
        for (int i2 = 0; i2 < 4; ++i2) {
            String string = stringArray[i2];
            b b2 = new b();
            new b().bi = string;
            b2.bj = "P1";
            b2.bk = "IO";
            b2.bl = "default";
            b2.bm = "IO";
            b2.bn = "default";
            b2.bo = -1L;
            b2.bp = null;
            d.dn.put(string, b2);
        }
    }

    /*
     * Could not resolve type clashes
     * Unable to fully structure code
     */
    final String a(String var1_1, int var2_2) {
        block54: {
            if (var2_2 <= 0 || var2_2 > 10000) {
                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.j.key);
            }
            var5_5 = var1_1;
            var6_7 = -1;
            switch (var5_5.hashCode()) {
                case 99228: {
                    if (!var5_5.equals("day")) break;
                    var6_7 = 0;
                    break;
                }
                case 3208676: {
                    if (!var5_5.equals("hour")) break;
                    var6_7 = 1;
                    break;
                }
                case 108114: {
                    if (!var5_5.equals("min")) break;
                    var6_7 = 2;
                    break;
                }
                case 113745: {
                    if (!var5_5.equals("sec")) break;
                    var6_7 = 3;
                }
            }
            switch (var6_7) {
                case 0: {
                    var3_8 = (long)var2_2 * 24L * 60L * 60L;
                    break;
                }
                case 1: {
                    var3_8 = (long)var2_2 * 60L * 60L;
                    break;
                }
                case 2: {
                    var3_8 = (long)var2_2 * 60L;
                    break;
                }
                case 3: {
                    var3_8 = var2_2;
                    break;
                }
                default: {
                    throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.h.key);
                }
            }
            if (d.de + var3_8 > 31535999L) {
                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.j.key);
            }
            var5_6 = d.de;
            var7_16 = d.de += var3_8;
            if (this.cJ != null) {
                this.cJ.run();
            }
            var1_1 = this;
            var9_17 = d.de;
            var11_18 = new HashMap<String, List>();
            for (Object var13_21 : d.di.values()) {
                if (var13_21.bM > 0L) continue;
                if (var13_21.bS > 0 && var13_21.bQ == 0L && var9_17 >= var13_21.bL + 5L) {
                    var11_18.computeIfAbsent(var13_21.bi, (Function<String, List>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, E(java.lang.String ), (Ljava/lang/String;)Ljava/util/List;)()).add(var13_21);
                }
                if (var13_21.bT <= 0 || var13_21.bQ <= 0L || var9_17 < var13_21.bQ + 5L) continue;
                var11_18.computeIfAbsent(var13_21.bU, (Function<String, List>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, D(java.lang.String ), (Ljava/lang/String;)Ljava/util/List;)()).add(var13_21);
            }
            if (var11_18.isEmpty()) break block54;
            for (Object var13_21 : var11_18.entrySet()) {
                var14_24 = (String)var13_21.getKey();
                var15_25 = (List)var13_21.getValue();
                var16_27 = d.dn.get(var14_24);
                if (var16_27 == null) continue;
                var15_25.sort((Comparator)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;Ljava/lang/Object;)I, a(com.huawei.tools.g.a.f com.huawei.tools.g.a.f ), (Lcom/huawei/tools/g/a/f;Lcom/huawei/tools/g/a/f;)I)());
                for (Object var18_31 : var15_25) {
                    block56: {
                        block61: {
                            block59: {
                                block60: {
                                    block58: {
                                        block57: {
                                            block55: {
                                                var2_2 = var18_31.bS > 0 && var18_31.bQ == 0L ? 1 : 0;
                                                var19_32 = var2_2 != 0 ? var18_31.bL + 5L : var18_31.bQ + 5L;
                                                var21_35 = var16_27.bo < 0L ? var19_32 : Math.max(var19_32, var16_27.bo + 5L);
                                                if (var21_35 > var9_17) break;
                                                if (var2_2 == 0) break block55;
                                                var2_3 = e.a(var16_27, var18_31.bV, var21_35);
                                                var3_9 = "O".equals(var2_3) != false || "B".equals(var2_3) != false;
                                                var23_37 = g.I(var18_31.bh);
                                                var24_40 = h.R(var18_31.bh);
                                                var25_41 = var24_40 != null || e.p("P1", var23_37) != false;
                                                var26_43 = g.J(var18_31.bh);
                                                if (var3_9 || !var25_41 || var26_43) {
                                                    var3_10 = var18_31;
                                                    var31_46 = var3_10.bh;
                                                    if (var3_10.ck != null) {
                                                        var3_10.ck = null;
                                                    }
                                                    e.y(var31_46);
                                                    d.di.remove(var3_10.bK);
                                                } else {
                                                    if (var24_40 != null) {
                                                        var3_11 = var18_31.bK;
                                                        var2_3 = var24_40;
                                                        var24_40.bt = "used";
                                                        var2_3.bK = var3_11;
                                                        var18_31.ck = var24_40.ck;
                                                    }
                                                    var18_31.bP = var21_35;
                                                    var18_31.bS = 0;
                                                    var18_31.cj = var21_35;
                                                    e.l();
                                                }
                                                break block56;
                                            }
                                            var2_3 = e.a(var16_27, var18_31.bW, var21_35);
                                            if (!"I".equals(var2_3) && !"B".equals(var2_3)) break block57;
                                            super.d((f)var18_31);
                                            var18_31.bQ = 0L;
                                            var18_31.bT = 0;
                                            var18_31.bU = null;
                                            var18_31.cj = var21_35;
                                            break block56;
                                        }
                                        var3_12 = var18_31;
                                        var2_3 = var1_1;
                                        if (var3_12.bX > 0L) break block58;
                                        var31_46 = var3_12.bh;
                                        if (d.dg.containsKey(var31_46) && (var33_51 = d.df.get(var32_49 = d.dg.get(var31_46))) != null && var33_51.by < 0.0) ** GOTO lbl-1000
                                        v0 = var32_50 = var2_3.cI != null ? var2_3.cI.s(var3_12.bK) : 0.0;
                                        if (v0 > 0.0) {
                                            var34_54 = var3_12.bh;
                                            if (d.dg.containsKey(var34_54) && (var36_58 = d.df.get(var35_55 = d.dg.get(var34_54))) != null && var36_58.by - Math.max(0.0, var37_60 = var36_58.bz - var3_12.cf) >= var32_50) {
                                                var36_58.by -= var32_50;
                                                var3_12.ca = "paid";
                                                var3_12.cb = var32_50;
                                                var3_12.cd += var32_50;
                                                var3_12.ce = d.de;
                                                var2_3.cI.v(var3_12.bK);
                                                c.a(var32_50);
                                                v1 = true;
                                            } else {
                                                v1 = false;
                                            }
                                        } else {
                                            v1 = true;
                                        }
                                        break block59;
                                    }
                                    if (!"paid".equals(var3_12.ca)) break block60;
                                    v2 = var31_47 = var2_3.cI != null ? var2_3.cI.s(var3_12.bK) : 0.0;
                                    if (v2 > 0.0) {
                                        var33_52 = var3_12.bh;
                                        if (d.dg.containsKey(var33_52) && (var35_56 = d.df.get(var34_54 = d.dg.get(var33_52))) != null && var35_56.by - Math.max(0.0, var36_59 = var35_56.bz - var3_12.cf) >= var31_47) {
                                            var35_56.by -= var31_47;
                                            var3_12.cb += var31_47;
                                            var3_12.cd += var31_47;
                                            var2_3.cI.v(var3_12.bK);
                                            c.a(var31_47);
                                            v1 = true;
                                        } else {
                                            v1 = false;
                                        }
                                    } else {
                                        v1 = true;
                                    }
                                    break block59;
                                }
                                if (var2_3.cI == null) ** GOTO lbl-1000
                                var31_48 = var2_3.cI.b((f)var3_12);
                                var35_57 = var31_48 + (var33_53 = var2_3.cI.s(var3_12.bK));
                                if (var35_57 == 0.0) {
                                    var3_12.ca = "paid";
                                    var3_12.cb = 0.0;
                                    var3_12.cd = 0.0;
                                    var3_12.ce = d.de;
                                    var2_3.cI.v(var3_12.bK);
                                    v1 = true;
                                } else {
                                    var37_61 = var3_12.bh;
                                    if (d.dg.containsKey(var37_61) && (var4_23 = d.df.get(var38_62 = d.dg.get(var37_61))) != null && var4_23.by - Math.max(0.0, var39_63 = var4_23.bz - var3_12.cf) >= var35_57) {
                                        var4_23.by -= var35_57;
                                        var3_12.ca = "paid";
                                        var3_12.cb = var35_57;
                                        var3_12.cd += var35_57;
                                        var3_12.ce = d.de;
                                        var2_3.cI.v(var3_12.bK);
                                        c.a(var35_57);
                                        v1 = true;
                                    } else lbl-1000:
                                    // 3 sources

                                    {
                                        v1 = false;
                                    }
                                }
                            }
                            if (v1) break block61;
                            super.d((f)var18_31);
                            var18_31.bQ = 0L;
                            var18_31.bT = 0;
                            var18_31.bU = null;
                            var18_31.cj = var21_35;
                            break block56;
                        }
                        if (!"paid".equals(var18_31.ca) || var18_31.ce <= 0L || d.de - var18_31.ce <= 900L) ** GOTO lbl206
                        v3 = var18_31;
                        var23_38 = var1_1.cI.a((f)v3, v3.ce);
                        var25_42 = var18_31.cb;
                        var2_3 = var18_31.bh;
                        var2_3 = (var2_3 = d.dg.get(var2_3)) != null ? d.df.get(var2_3) : null;
                        var27_44 = 0.0;
                        if (var23_38 <= 0.0) {
                            var3_13 = true;
                        } else if (var2_3 != null) {
                            var29_45 = var2_3.bz - var18_31.cf;
                            var3_13 = var2_3.by - Math.max(0.0, var29_45) >= var23_38;
                            if (var3_13) {
                                var27_44 = var23_38;
                            }
                        } else {
                            var3_13 = false;
                        }
                        if (!var3_13) {
                            super.d((f)var18_31);
                            var18_31.bQ = 0L;
                            var18_31.bT = 0;
                            var18_31.bU = null;
                            var18_31.cj = var21_35;
                        } else {
                            if (var27_44 > 0.0 && var2_3 != null) {
                                var2_3.by -= var27_44;
                                var18_31.cd += var27_44;
                                c.a(var27_44);
                            }
                            var18_31.cb = var25_42 + var23_38;
                            var18_31.ca = "paid";
                            var18_31.ce = d.de;
lbl206:
                            // 2 sources

                            var41_64 = var21_35;
                            var2_3 = var18_31;
                            var3_14 = var2_3.bh;
                            var2_3.bM = var41_64;
                            var2_3.bT = 0;
                            if (var2_3.ck != null) {
                                e.c((f)var2_3);
                                h.a(d.dv.get(var2_3.ck));
                            }
                            e.l();
                            var2_3.ci = 0L;
                            var2_3.cj = 0L;
                            var2_3.cg = 0L;
                            e.y(var3_14);
                            d.di.remove(var2_3.bK);
                        }
                    }
                    var16_27.bp = g.a(var16_27);
                    var16_27.bo = var21_35;
                }
                var17_28 = false;
                var18_31 = d.di.values().iterator();
                while (var18_31.hasNext()) {
                    var2_4 = (f)var18_31.next();
                    if (var2_4.bM > 0L) continue;
                    var19_33 = var2_4.bS > 0 && var2_4.bQ == 0L;
                    v4 = var20_34 = var2_4.bT > 0 && var2_4.bQ > 0L;
                    if (var19_33 && var14_24.equals(var2_4.bi)) {
                        var17_28 = true;
                        break;
                    }
                    if (!var20_34 || !var14_24.equals(var2_4.bU)) continue;
                    var17_28 = true;
                    break;
                }
                if (var17_28) continue;
                var16_27.bp = null;
                var16_27.bo = -1L;
            }
        }
        this.h();
        e.i();
        this.j();
        this.cB.r();
        this.cB.s();
        var11_19 = var7_16;
        var9_17 = var5_6;
        var13_22 = var9_17 % 86400L;
        var15_26 = var11_19 % 86400L;
        var17_30 = var9_17 / 86400L;
        var20_34 = var11_19 / 86400L > var17_30;
        var21_36 = var13_22 < 25200L && var15_26 >= 25200L;
        var22_65 = var13_22 < 32400L && var15_26 >= 32400L;
        var2_2 = var13_22 < 61200L && var15_26 >= 61200L ? 1 : 0;
        v5 = var3_15 = var13_22 < 68400L && var15_26 >= 68400L;
        if (var20_34) {
            if (var15_26 >= 25200L && var15_26 < 32400L) {
                var21_36 = true;
            }
            if (var15_26 >= 61200L && var15_26 < 68400L) {
                var2_2 = 1;
            }
        }
        var23_39 = e.a(var11_19);
        if (var21_36 && var23_39) {
            e.A("morning");
        }
        if (var22_65) {
            e.k();
        }
        if (var2_2 != 0 && var23_39) {
            e.A("evening");
        }
        if (var3_15) {
            e.k();
        }
        if (var20_34 && !var23_39) {
            e.k();
        }
        var1_1 = e.b(var7_16).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return "E000:".concat(String.valueOf(var1_1));
    }

    static String h(String object, String string) {
        if ((object = d.di.get(object)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (((f)object).bQ > 0L || ((f)object).bM > 0L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (g.f((f)object)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.Z.key);
        }
        if (((f)object).bN != null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.Y.key);
        }
        b b2 = d.dn.get(string);
        if (b2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.K.key);
        }
        long l = d.de;
        if (((f)object).cj > 0L) {
            ((f)object).ci = ((f)object).ci + l - ((f)object).cj;
            ((f)object).cj = 0L;
        }
        ((f)object).bQ = l;
        ((f)object).bU = string;
        ((f)object).bT = d.dp.incrementAndGet();
        ((f)object).bW = g.a(b2);
        return "E000:Success";
    }

    private static void y(String object) {
        if (!d.dg.containsKey(object) && (object = d.dh.get(object)) != null) {
            ((j)object).bt = "invalid";
        }
    }

    private static void c(f object) {
        Object object2 = ((f)object).bN;
        if (object2 == null && ((f)object).ck != null) {
            object2 = h.S(((f)object).ck);
        }
        if (object2 == null) {
            return;
        }
        object = d.dk.get(object2);
        if (object == null) {
            return;
        }
        if ("U".equals(((com.huawei.tools.g.a.g)object).bt)) {
            ((com.huawei.tools.g.a.g)object).bt = "F";
            ((com.huawei.tools.g.a.g)object).bh = null;
            object2 = d.dj.get(((com.huawei.tools.g.a.g)object).bj);
            if (object2 != null && (object = ((com.huawei.tools.g.a.e)object2).bH.get(((com.huawei.tools.g.a.g)object).cl)) != null) {
                ((AtomicInteger)object).decrementAndGet();
            }
            return;
        }
        if ("R".equals(((com.huawei.tools.g.a.g)object).bt)) {
            ((com.huawei.tools.g.a.g)object).bt = "F";
            ((com.huawei.tools.g.a.g)object).bh = null;
            g.s(((com.huawei.tools.g.a.g)object).bj, ((com.huawei.tools.g.a.g)object).cl);
        }
    }

    static String i(String object, String string) {
        if (!("I".equals(string) || "O".equals(string) || "IO".equals(string) || "B".equals(string))) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.J.key);
        }
        if ((object = d.dn.get(object)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.K.key);
        }
        if (!"morning".equals(((b)object).bl) && !"evening".equals(((b)object).bl)) {
            ((b)object).bk = string;
            ((b)object).bl = "manual";
            ((b)object).bm = string;
            ((b)object).bn = "manual";
        }
        return "E000:Success";
    }

    static String e(String object) {
        if ((object = d.dn.get(object)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.K.key);
        }
        String string = g.a((b)object);
        object = ((b)object).bl;
        return "E000:" + string + "|" + (String)object;
    }

    static String a(String string, String object, String string2) {
        if (StringUtils.isBlank(string) || !string.matches("[LMS]\\d{5,6}")) {
            return "E001:Fail";
        }
        if (!(object == null || "null".equals(object) || ((String)object).isEmpty() || ((String)object).matches("p\\d{16}"))) {
            return "E001:Fail";
        }
        if (!("outpark".equals(string2) || "entrying".equals(string2) || "inpark".equals(string2) || "parked".equals(string2) || "exiting".equals(string2))) {
            return "E001:Fail";
        }
        f f2 = null;
        boolean bl = object != null && !"null".equals(object) && !((String)object).isEmpty();
        if (bl) {
            f2 = d.di.get(object);
        } else {
            for (f f3 : d.di.values()) {
                if (!string.equals(f3.bh) || f3.bM != 0L) continue;
                f2 = f3;
                break;
            }
        }
        if (f2 == null) {
            if (bl) {
                return com.huawei.tools.a.b.a("E002", "Verify_fail");
            }
            if ("outpark".equals(string2)) {
                return "E000:Success";
            }
            return com.huawei.tools.a.b.a("E002", "Verify_fail");
        }
        if (!string.equals(f2.bh)) {
            return com.huawei.tools.a.b.a("E002", "Verify_fail");
        }
        if (g.e(f2).equals(string2)) {
            return "E000:Success";
        }
        return com.huawei.tools.a.b.a("E002", "Verify_fail");
    }

    static String k(String string, String string2) {
        if (StringUtils.isBlank(string) || !string.matches("\\d+")) {
            return "E001:Fail";
        }
        try {
            int n = Integer.parseInt(string);
            ArrayList<String> arrayList = new ArrayList<String>();
            for (com.huawei.tools.g.a.g g2 : d.dk.values()) {
                if (!"U".equals(g2.bt)) continue;
                arrayList.add(g2.bN);
            }
            if (arrayList.size() != n) {
                return com.huawei.tools.a.b.a("E002", "Verify_fail");
            }
            if (StringUtils.isNotBlank(string2)) {
                HashSet hashSet = new HashSet();
                String[] stringArray = string2.split("\\|");
                int n2 = stringArray.length;
                for (int i2 = 0; i2 < n2; ++i2) {
                    String string3 = stringArray[i2].trim();
                    if (string3.isEmpty()) continue;
                    if (!string3.matches("[LMS]\\d{3}")) {
                        return "E001:Fail";
                    }
                    hashSet.add(string3);
                }
                if (!new HashSet(arrayList).equals(hashSet)) {
                    return com.huawei.tools.a.b.a("E002", "Verify_fail");
                }
            }
            return "E000:Success";
        }
        catch (NumberFormatException numberFormatException) {
            return "E001:Fail";
        }
    }

    static String b(String object, String object2, String object3) {
        Object object4 = "\\d+\\|\\d+\\|\\d+";
        if (StringUtils.isBlank((CharSequence)object) || !((String)object).matches((String)object4)) {
            return "E001:Fail";
        }
        if (StringUtils.isBlank((CharSequence)object2) || !((String)object2).matches((String)object4)) {
            return "E001:Fail";
        }
        if (StringUtils.isBlank((CharSequence)object3) || !((String)object3).matches((String)object4)) {
            return "E001:Fail";
        }
        object4 = d.dj.get("P1");
        if (object4 == null) {
            return "E001:Fail";
        }
        try {
            int n;
            object = e.C((String)object);
            object2 = e.C((String)object2);
            object3 = e.C((String)object3);
            int n2 = ((com.huawei.tools.g.a.e)object4).bH.getOrDefault("S", new AtomicInteger(0)).get();
            int n3 = ((com.huawei.tools.g.a.e)object4).bI.getOrDefault("S", new AtomicInteger(0)).get();
            int n4 = ((com.huawei.tools.g.a.e)object4).bJ != null ? ((com.huawei.tools.g.a.e)object4).bJ.getOrDefault("S", new AtomicInteger(0)).get() : 0;
            int n5 = ((com.huawei.tools.g.a.e)object4).bH.getOrDefault("M", new AtomicInteger(0)).get();
            int n6 = ((com.huawei.tools.g.a.e)object4).bI.getOrDefault("M", new AtomicInteger(0)).get();
            int n7 = ((com.huawei.tools.g.a.e)object4).bJ != null ? ((com.huawei.tools.g.a.e)object4).bJ.getOrDefault("M", new AtomicInteger(0)).get() : 0;
            int n8 = ((com.huawei.tools.g.a.e)object4).bH.getOrDefault("L", new AtomicInteger(0)).get();
            int n9 = ((com.huawei.tools.g.a.e)object4).bI.getOrDefault("L", new AtomicInteger(0)).get();
            int n10 = n = ((com.huawei.tools.g.a.e)object4).bJ != null ? ((com.huawei.tools.g.a.e)object4).bJ.getOrDefault("L", new AtomicInteger(0)).get() : 0;
            if (n2 != object[0] || n3 != object[1] || n4 != object[2] || n5 != object2[0] || n6 != object2[1] || n7 != object2[2] || n8 != object3[0] || n9 != object3[1] || n != object3[2]) {
                return com.huawei.tools.a.b.a("E002", "Verify_fail");
            }
            return "E000:Success";
        }
        catch (NumberFormatException numberFormatException) {
            return "E001:Fail";
        }
    }

    private void h() {
        long l = d.de;
        ArrayList<String> arrayList = new ArrayList<String>();
        for (f object : d.di.values()) {
            if (object.bM > 0L || object.bQ > 0L || object.bS > 0 || object.bP <= 0L || l - object.bP < 900L) continue;
            arrayList.add(object.bK);
        }
        for (String string : arrayList) {
            this.z(string);
        }
    }

    private void z(String string) {
        Object object;
        f f2 = d.di.get(string);
        if (f2 == null) {
            return;
        }
        String string2 = f2.bh;
        if (this.cI != null) {
            this.cI.a(f2);
        }
        if (f2.ck != null) {
            e.c(f2);
            h.a(d.dv.get(f2.ck));
        }
        if (f2.bN != null && (object = d.dk.get(f2.bN)) != null && "U".equals(((com.huawei.tools.g.a.g)object).bt)) {
            ((com.huawei.tools.g.a.g)object).bt = "F";
            ((com.huawei.tools.g.a.g)object).bh = null;
            com.huawei.tools.g.a.e e2 = d.dj.get(((com.huawei.tools.g.a.g)object).bj);
            if (e2 != null && (object = e2.bH.get(((com.huawei.tools.g.a.g)object).cl)) != null) {
                ((AtomicInteger)object).decrementAndGet();
            }
        }
        f2.bM = d.de;
        e.l();
        f2.ci = 0L;
        f2.cj = 0L;
        f2.cg = 0L;
        e.y(string2);
        d.di.remove(string);
    }

    private void d(f f2) {
        double d2 = f2.cd;
        double d3 = f2.cb - d2;
        if (d2 > 0.0) {
            Object object = f2.bh;
            if ((object = d.dg.get(object)) != null && (object = d.df.get(object)) != null) {
                ((com.huawei.tools.g.a.c)object).by += d2;
            }
            c.a("balpay", d2);
        }
        if (d3 > 0.0) {
            c.a("temppay", d3);
        }
        if (this.cI != null) {
            this.cI.w(f2.bK);
        }
        f2.ca = "unpaid";
        f2.cb = 0.0;
        f2.cd = 0.0;
        f2.ce = 0L;
    }

    private static void i() {
        long l = d.de;
        for (f f2 : d.di.values()) {
            long l2;
            if (f2.bM > 0L || f2.bS > 0 || f2.bQ > 0L && f2.bT > 0 || f2.cg > 0L || g.K(f2.bh)) continue;
            if (f2.cj == 0L) {
                f2.cj = l;
            }
            if (f2.ci + (l - f2.cj) < 1296000L) continue;
            String string = f2.bh;
            f2.cg = l2 = f2.cj + (1296000L - f2.ci);
            if (!d.dq.containsKey(string)) {
                a a2 = new a(string);
                d.dq.put(string, a2);
            }
            f2.ci = 0L;
            f2.cj = l2;
        }
    }

    private void j() {
        long l = d.de;
        ArrayList<String> arrayList = new ArrayList<String>();
        for (f object : d.di.values()) {
            if (object.bM > 0L || object.cg <= 0L || object.bS > 0 || object.bQ > 0L && object.bT > 0 || g.K(object.bh)) continue;
            if (object.cj == 0L) {
                object.cj = l;
            }
            if (object.ci + (l - object.cj) < 1296000L) continue;
            arrayList.add(object.bK);
        }
        for (String string : arrayList) {
            this.z(string);
        }
    }

    private static boolean a(long l) {
        LocalDateTime localDateTime = e.b(l);
        String string = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (g.N(string)) {
            return true;
        }
        if (cK.contains(string)) {
            return false;
        }
        return localDateTime.getDayOfWeek().getValue() <= 5;
    }

    private static void A(String string) {
        if ("morning".equals(string)) {
            e.e("G1", "I", string);
            e.e("G2", "I", string);
            e.e("G3", "I", string);
            e.e("G4", "O", string);
            return;
        }
        if ("evening".equals(string)) {
            e.e("G1", "O", string);
            e.e("G2", "O", string);
            e.e("G3", "O", string);
            e.e("G4", "I", string);
        }
    }

    private static void e(String object, String string, String string2) {
        if ((object = d.dn.get(object)) != null) {
            ((b)object).bk = string;
            ((b)object).bl = string2;
        }
    }

    private static void k() {
        String[] stringArray = new String[]{"G1", "G2", "G3", "G4"};
        for (int i2 = 0; i2 < 4; ++i2) {
            Object object = stringArray[i2];
            if ((object = d.dn.get(object)) == null || !"morning".equals(((b)object).bl) && !"evening".equals(((b)object).bl)) continue;
            if (((b)object).bm != null) {
                ((b)object).bk = ((b)object).bm;
                ((b)object).bl = ((b)object).bn != null ? ((b)object).bn : "default";
            } else {
                ((b)object).bk = "IO";
                ((b)object).bl = "default";
            }
            if (((b)object).bp == null) continue;
            ((b)object).bp = ((b)object).bk;
        }
    }

    private static String[] B(String string) {
        if ("S".equals(string)) {
            return new String[]{"S", "M", "L"};
        }
        if ("M".equals(string)) {
            return new String[]{"M", "L"};
        }
        if ("L".equals(string)) {
            return new String[]{"L"};
        }
        return new String[0];
    }

    private static boolean p(String object, String stringArray) {
        if ((object = d.dj.get(object)) == null) {
            return false;
        }
        stringArray = e.B((String)stringArray);
        int n = 0;
        int n2 = 0;
        int n3 = 0;
        for (String string : stringArray) {
            int n4 = e.a((com.huawei.tools.g.a.e)object, string);
            int n5 = ((com.huawei.tools.g.a.e)object).bH.getOrDefault(string, new AtomicInteger(0)).get();
            int n6 = ((com.huawei.tools.g.a.e)object).bI.getOrDefault(string, new AtomicInteger(0)).get();
            int n7 = ((com.huawei.tools.g.a.e)object).bJ != null ? ((com.huawei.tools.g.a.e)object).bJ.getOrDefault(string, new AtomicInteger(0)).get() : 0;
            n += (n4 -= n5);
            n2 += n6;
            n3 += n7;
        }
        return n - n2 - n3 > 0;
    }

    static void l() {
        com.huawei.tools.g.a.e e2 = d.dj.get("P1");
        if (e2 == null) {
            return;
        }
        e2.bI.getOrDefault("S", new AtomicInteger(0)).set(0);
        e2.bI.getOrDefault("M", new AtomicInteger(0)).set(0);
        e2.bI.getOrDefault("L", new AtomicInteger(0)).set(0);
        String[] stringArray = new ArrayList();
        for (f f2 : d.di.values()) {
            if (f2.bM > 0L || f2.bS > 0 || f2.bN != null || f2.ck != null) continue;
            stringArray.add(f2);
        }
        Iterator<f> iterator = stringArray.iterator();
        block1: while (iterator.hasNext()) {
            for (String string : e.B(g.I(iterator.next().bh))) {
                int n;
                int n2 = e.a(e2, string);
                int n3 = e2.bH.getOrDefault(string, new AtomicInteger(0)).get();
                int n4 = e2.bI.getOrDefault(string, new AtomicInteger(0)).get();
                int n5 = n = e2.bJ != null ? e2.bJ.getOrDefault(string, new AtomicInteger(0)).get() : 0;
                if (n2 - n3 - n4 - n <= 0) continue;
                e2.bI.getOrDefault(string, new AtomicInteger(0)).incrementAndGet();
                continue block1;
            }
        }
    }

    private static int a(com.huawei.tools.g.a.e e2, String string) {
        if ("L".equals(string)) {
            return e2.bE;
        }
        if ("M".equals(string)) {
            return e2.bF;
        }
        if ("S".equals(string)) {
            return e2.bG;
        }
        return 0;
    }

    private static LocalDateTime b(long l) {
        return LocalDateTime.of(2026, 1, 1, 0, 0).plusSeconds(l);
    }

    private static int[] C(String stringArray) {
        stringArray = stringArray.split("\\|");
        return new int[]{Integer.parseInt(stringArray[0].trim()), Integer.parseInt(stringArray[1].trim()), Integer.parseInt(stringArray[2].trim())};
    }

    private static String a(b b2, String string, long l) {
        long l2 = l - 5L;
        long l3 = l2 % 86400L;
        if (e.a(l2)) {
            if (l3 >= 25200L && l3 < 32400L) {
                return e.q(b2.bi, "morning");
            }
            if (l3 >= 61200L && l3 < 68400L) {
                return e.q(b2.bi, "evening");
            }
        }
        if (b2.bp == null) {
            return string;
        }
        if ("morning".equals(b2.bl) || "evening".equals(b2.bl)) {
            if (b2.bm != null) {
                return b2.bm;
            }
            return "IO";
        }
        return b2.bp;
    }

    private static String q(String string, String string2) {
        if ("morning".equals(string2)) {
            if ("G4".equals(string)) {
                return "O";
            }
            return "I";
        }
        if ("G4".equals(string)) {
            return "I";
        }
        return "O";
    }

    private static /* synthetic */ int a(f f2, f f3) {
        int n = f2.bS > 0 ? f2.bS : f2.bT;
        int n2 = f3.bS > 0 ? f3.bS : f3.bT;
        return Integer.compare(n, n2);
    }

    private static /* synthetic */ List D(String string) {
        return new ArrayList();
    }

    private static /* synthetic */ List E(String string) {
        return new ArrayList();
    }
}

