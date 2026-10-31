/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.huawei.tools.a.a
 *  com.huawei.tools.a.b
 *  com.huawei.tools.b.a
 *  com.huawei.tools.g.a.a
 *  com.huawei.tools.g.a.b
 *  com.huawei.tools.g.a.c
 *  com.huawei.tools.g.a.e
 *  com.huawei.tools.g.a.f
 *  com.huawei.tools.g.a.g
 *  com.huawei.tools.g.a.i
 *  com.huawei.tools.g.b.a.c
 *  com.huawei.tools.g.b.a.g
 *  com.huawei.tools.h.d
 *  org.apache.commons.lang3.StringUtils
 */
package com.huawei.tools.g.b.a;

import com.huawei.tools.g.a.a;
import com.huawei.tools.g.a.b;
import com.huawei.tools.g.a.f;
import com.huawei.tools.g.a.i;
import com.huawei.tools.g.b.a.c;
import com.huawei.tools.g.b.a.g;
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
    private final c bM;
    private static final Set<String> bN = new HashSet<String>(Arrays.asList("2026-01-01", "2026-01-02", "2026-01-03", "2026-02-15", "2026-02-16", "2026-02-17", "2026-02-18", "2026-02-19", "2026-02-20", "2026-02-21", "2026-02-22", "2026-02-23", "2026-04-04", "2026-04-05", "2026-04-06", "2026-05-01", "2026-05-02", "2026-05-03", "2026-05-04", "2026-05-05", "2026-06-19", "2026-06-20", "2026-06-21", "2026-09-25", "2026-09-26", "2026-09-27", "2026-10-01", "2026-10-02", "2026-10-03", "2026-10-04", "2026-10-05", "2026-10-06", "2026-10-07"));
    private static final Set<String> bO = new HashSet<String>(Arrays.asList("2026-01-04", "2026-02-14", "2026-02-28", "2026-05-09", "2026-09-20", "2026-10-10"));

    e(c c2) {
        this.bM = c2;
    }

    static void f() {
        String[] stringArray;
        int n;
        com.huawei.tools.g.a.e e2 = new com.huawei.tools.g.a.e();
        new com.huawei.tools.g.a.e().aD = "P1";
        e2.aY = 10;
        e2.aZ = 200;
        e2.ba = 70;
        e2.bb = new HashMap();
        e2.bc = new HashMap();
        e2.bb.put("L", new AtomicInteger(0));
        e2.bb.put("M", new AtomicInteger(0));
        e2.bb.put("S", new AtomicInteger(0));
        e2.bc.put("L", new AtomicInteger(0));
        e2.bc.put("M", new AtomicInteger(0));
        e2.bc.put("S", new AtomicInteger(0));
        d.ci.put("P1", e2);
        for (n = 1; n <= 10; ++n) {
            stringArray = new com.huawei.tools.g.a.g();
            new com.huawei.tools.g.a.g().bg = String.format("L%03d", n);
            stringArray.aD = "P1";
            stringArray.bB = "L";
            stringArray.aN = "F";
            d.cj.put(stringArray.bg, stringArray);
        }
        for (n = 1; n <= 200; ++n) {
            stringArray = new com.huawei.tools.g.a.g();
            new com.huawei.tools.g.a.g().bg = String.format("M%03d", n);
            stringArray.aD = "P1";
            stringArray.bB = "M";
            stringArray.aN = "F";
            d.cj.put(stringArray.bg, stringArray);
        }
        for (n = 1; n <= 70; ++n) {
            stringArray = new com.huawei.tools.g.a.g();
            new com.huawei.tools.g.a.g().bg = String.format("S%03d", n);
            stringArray.aD = "P1";
            stringArray.bB = "S";
            stringArray.aN = "F";
            d.cj.put(stringArray.bg, stringArray);
        }
        stringArray = new String[]{"G1", "G2", "G3", "G4"};
        for (n = 0; n < 4; ++n) {
            String string = stringArray[n];
            b b2 = new b();
            new b().aC = string;
            b2.aD = "P1";
            b2.aE = "IO";
            b2.aF = "default";
            b2.aG = "IO";
            b2.aH = "default";
            b2.aI = -1L;
            b2.aJ = null;
            d.cl.put(string, b2);
        }
    }

    /*
     * Could not resolve type clashes
     * Unable to fully structure code
     */
    final String a(String var1_1, int var2_2) {
        block40: {
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
            if (d.cb + var3_8 > 31535999L) {
                throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.j.key);
            }
            var5_6 = d.cb;
            var7_15 = d.cb += var3_8;
            var1_1 = this;
            var9_16 = d.cb;
            var11_17 = new HashMap<String, List>();
            for (Object var13_20 : d.cg.values()) {
                if (var13_20.bf > 0L) continue;
                if (var13_20.bl > 0 && var13_20.bj == 0L && var9_16 >= var13_20.be + 5L) {
                    var11_17.computeIfAbsent(var13_20.aC, (Function<String, List>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, r(java.lang.String ), (Ljava/lang/String;)Ljava/util/List;)()).add(var13_20);
                }
                if (var13_20.bm <= 0 || var13_20.bj <= 0L || var9_16 < var13_20.bj + 5L) continue;
                var11_17.computeIfAbsent(var13_20.bn, (Function<String, List>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, q(java.lang.String ), (Ljava/lang/String;)Ljava/util/List;)()).add(var13_20);
            }
            if (var11_17.isEmpty()) break block40;
            for (Object var13_20 : var11_17.entrySet()) {
                var14_23 = (String)var13_20.getKey();
                var15_24 = (List)var13_20.getValue();
                var16_26 = (b)d.cl.get(var14_23);
                if (var16_26 == null) continue;
                var15_24.sort((Comparator)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;Ljava/lang/Object;)I, a(com.huawei.tools.g.a.f com.huawei.tools.g.a.f ), (Lcom/huawei/tools/g/a/f;Lcom/huawei/tools/g/a/f;)I)());
                for (Object var18_30 : var15_24) {
                    block42: {
                        block48: {
                            block45: {
                                block47: {
                                    block46: {
                                        block44: {
                                            block43: {
                                                block41: {
                                                    var2_2 = var18_30.bl > 0 && var18_30.bj == 0L ? 1 : 0;
                                                    var19_31 = var2_2 != 0 ? var18_30.be + 5L : var18_30.bj + 5L;
                                                    var21_34 = var16_26.aI < 0L ? var19_31 : Math.max(var19_31, var16_26.aI + 5L);
                                                    if (var21_34 > var9_16) break;
                                                    if (var2_2 == 0) break block41;
                                                    var2_3 = e.a(var16_26, var18_30.bo, var21_34);
                                                    var3_9 = "O".equals(var2_3) != false || "B".equals(var2_3) != false;
                                                    var23_36 = g.v((String)var18_30.aB);
                                                    var24_39 = e.n("P1", var23_36);
                                                    var25_40 = g.w((String)var18_30.aB);
                                                    if (var3_9 || !var24_39 || var25_40) {
                                                        var3_10 = var18_30;
                                                        e.l(var3_10.aB);
                                                        d.cg.remove(var3_10.bd);
                                                    } else {
                                                        var18_30.bi = var21_34;
                                                        var18_30.bl = 0;
                                                        var18_30.bA = var21_34;
                                                        e.k();
                                                    }
                                                    break block42;
                                                }
                                                var2_3 = e.a(var16_26, var18_30.bp, var21_34);
                                                if (!"I".equals(var2_3) && !"B".equals(var2_3)) break block43;
                                                e.c((f)var18_30);
                                                var18_30.bj = 0L;
                                                var18_30.bm = 0;
                                                var18_30.bn = null;
                                                var18_30.bA = var21_34;
                                                break block42;
                                            }
                                            var3_11 = var18_30;
                                            var2_3 = var1_1;
                                            if (var3_11.bq > 0L) break block44;
                                            var31_44 = var3_11.aB;
                                            if (d.ce.containsKey(var31_44) && (var4_22 /* !! */  = (com.huawei.tools.g.a.c)d.cd.get(var32_46 = (String)d.ce.get(var31_44))) != null && var4_22 /* !! */ .aS < 0.0) ** GOTO lbl-1000
                                            v0 = true;
                                            break block45;
                                        }
                                        if (!"paid".equals(var3_11.bt)) break block46;
                                        v0 = true;
                                        break block45;
                                    }
                                    if (var2_3.bM == null) ** GOTO lbl-1000
                                    var31_45 = c.b((f)var3_11);
                                    if (var31_45 != 0.0) break block47;
                                    var3_11.bt = "paid";
                                    var3_11.bu = 0.0;
                                    var3_11.bv = 0.0;
                                    var3_11.bw = d.cb;
                                    v0 = true;
                                    break block45;
                                }
                                var4_22 /* !! */  = var3_11.aB;
                                if (!d.ce.containsKey(var4_22 /* !! */ )) ** GOTO lbl-1000
                                var4_22 /* !! */  = (String)d.ce.get(var4_22 /* !! */ );
                                if ((var4_22 /* !! */  = (com.huawei.tools.g.a.c)d.cd.get(var4_22 /* !! */ )) != null && var4_22 /* !! */ .aS - Math.max(0.0, var33_47 = var4_22 /* !! */ .aT - var3_11.bx) >= var31_45) {
                                    var4_22 /* !! */ .aS -= var31_45;
                                    var3_11.bt = "paid";
                                    var3_11.bu = var31_45;
                                    var3_11.bv += var31_45;
                                    var3_11.bw = d.cb;
                                    c.a((double)var31_45);
                                    v0 = true;
                                } else lbl-1000:
                                // 4 sources

                                {
                                    v0 = false;
                                }
                            }
                            if (v0) break block48;
                            e.c((f)var18_30);
                            var18_30.bj = 0L;
                            var18_30.bm = 0;
                            var18_30.bn = null;
                            var18_30.bA = var21_34;
                            break block42;
                        }
                        if (!"paid".equals(var18_30.bt) || var18_30.bw <= 0L || d.cb - var18_30.bw <= 900L) ** GOTO lbl166
                        v1 = var18_30;
                        var23_37 = c.a((f)v1, (long)v1.bw);
                        var25_41 = var18_30.bu;
                        var2_3 = var18_30.aB;
                        var2_3 = (var2_3 = (String)d.ce.get(var2_3)) != null ? (com.huawei.tools.g.a.c)d.cd.get(var2_3) : null;
                        var27_42 = 0.0;
                        if (var23_37 <= 0.0) {
                            var3_12 = true;
                        } else if (var2_3 != null) {
                            var29_43 = var2_3.aT - var18_30.bx;
                            var3_12 = var2_3.aS - Math.max(0.0, var29_43) >= var23_37;
                            if (var3_12) {
                                var27_42 = var23_37;
                            }
                        } else {
                            var3_12 = false;
                        }
                        if (!var3_12) {
                            e.c((f)var18_30);
                            var18_30.bj = 0L;
                            var18_30.bm = 0;
                            var18_30.bn = null;
                            var18_30.bA = var21_34;
                        } else {
                            if (var27_42 > 0.0 && var2_3 != null) {
                                var2_3.aS -= var27_42;
                                var18_30.bv += var27_42;
                                c.a((double)var27_42);
                            }
                            var18_30.bu = var25_41 + var23_37;
                            var18_30.bt = "paid";
                            var18_30.bw = d.cb;
lbl166:
                            // 2 sources

                            var35_48 = var21_34;
                            var2_3 = var18_30;
                            var3_13 = var2_3.aB;
                            var2_3.bf = var35_48;
                            var2_3.bm = 0;
                            e.k();
                            var2_3.bz = 0L;
                            var2_3.bA = 0L;
                            var2_3.by = 0L;
                            e.l(var3_13);
                            d.cg.remove(var2_3.bd);
                        }
                    }
                    var16_26.aJ = g.a((b)var16_26);
                    var16_26.aI = var21_34;
                }
                var17_27 = false;
                var18_30 = d.cg.values().iterator();
                while (var18_30.hasNext()) {
                    var2_4 = (f)var18_30.next();
                    if (var2_4.bf > 0L) continue;
                    var19_32 = var2_4.bl > 0 && var2_4.bj == 0L;
                    v2 = var20_33 = var2_4.bm > 0 && var2_4.bj > 0L;
                    if (var19_32 && var14_23.equals(var2_4.aC)) {
                        var17_27 = true;
                        break;
                    }
                    if (!var20_33 || !var14_23.equals(var2_4.bn)) continue;
                    var17_27 = true;
                    break;
                }
                if (var17_27) continue;
                var16_26.aJ = null;
                var16_26.aI = -1L;
            }
        }
        this.g();
        e.h();
        this.i();
        var11_18 = var7_15;
        var9_16 = var5_6;
        var13_21 = var9_16 % 86400L;
        var15_25 = var11_18 % 86400L;
        var17_29 = var9_16 / 86400L;
        var20_33 = var11_18 / 86400L > var17_29;
        var21_35 = var13_21 < 25200L && var15_25 >= 25200L;
        var22_49 = var13_21 < 32400L && var15_25 >= 32400L;
        var2_2 = var13_21 < 61200L && var15_25 >= 61200L ? 1 : 0;
        v3 = var3_14 = var13_21 < 68400L && var15_25 >= 68400L;
        if (var20_33) {
            if (var15_25 >= 25200L && var15_25 < 32400L) {
                var21_35 = true;
            }
            if (var15_25 >= 61200L && var15_25 < 68400L) {
                var2_2 = 1;
            }
        }
        var23_38 = e.a(var11_18);
        if (var21_35 && var23_38) {
            e.n("morning");
        }
        if (var22_49) {
            e.j();
        }
        if (var2_2 != 0 && var23_38) {
            e.n("evening");
        }
        if (var3_14) {
            e.j();
        }
        if (var20_33 && !var23_38) {
            e.j();
        }
        var1_1 = e.b(var7_15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return "E000:".concat(String.valueOf(var1_1));
    }

    static String h(String string, String string2) {
        if ((string = (f)d.cg.get(string)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (((f)string).bj > 0L || ((f)string).bf > 0L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.L.key);
        }
        if (g.e((f)string)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.Z.key);
        }
        if (((f)string).bg != null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.Y.key);
        }
        b b2 = (b)d.cl.get(string2);
        if (b2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.K.key);
        }
        long l = d.cb;
        if (((f)string).bA > 0L) {
            ((f)string).bz = ((f)string).bz + l - ((f)string).bA;
            ((f)string).bA = 0L;
        }
        ((f)string).bj = l;
        ((f)string).bn = string2;
        ((f)string).bm = d.cn.incrementAndGet();
        ((f)string).bp = g.a((b)b2);
        return "E000:Success";
    }

    private static void l(String string) {
        if (!d.ce.containsKey(string) && (string = (i)d.cf.get(string)) != null) {
            ((i)string).aN = "invalid";
        }
    }

    static String i(String string, String string2) {
        if (!("I".equals(string2) || "O".equals(string2) || "IO".equals(string2) || "B".equals(string2))) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.J.key);
        }
        if ((string = (b)d.cl.get(string)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.K.key);
        }
        if (!"morning".equals(((b)string).aF) && !"evening".equals(((b)string).aF)) {
            ((b)string).aE = string2;
            ((b)string).aF = "manual";
            ((b)string).aG = string2;
            ((b)string).aH = "manual";
        }
        return "E000:Success";
    }

    static String e(String string) {
        if ((string = (b)d.cl.get(string)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.K.key);
        }
        String string2 = g.a((b)string);
        string = ((b)string).aF;
        return "E000:" + string2 + "|" + string;
    }

    static String a(String string, String object, String string2) {
        if (StringUtils.isBlank((CharSequence)string) || !string.matches("[LMS]\\d{5,6}")) {
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
            f2 = (f)d.cg.get(object);
        } else {
            for (f f3 : d.cg.values()) {
                if (!string.equals(f3.aB) || f3.bf != 0L) continue;
                f2 = f3;
                break;
            }
        }
        if (f2 == null) {
            if (bl) {
                return com.huawei.tools.a.b.a((String)"E002", (String)"Verify_fail");
            }
            if ("outpark".equals(string2)) {
                return "E000:Success";
            }
            return com.huawei.tools.a.b.a((String)"E002", (String)"Verify_fail");
        }
        if (!string.equals(f2.aB)) {
            return com.huawei.tools.a.b.a((String)"E002", (String)"Verify_fail");
        }
        if (g.d((f)f2).equals(string2)) {
            return "E000:Success";
        }
        return com.huawei.tools.a.b.a((String)"E002", (String)"Verify_fail");
    }

    static String k(String string, String string2) {
        if (StringUtils.isBlank((CharSequence)string) || !string.matches("\\d+")) {
            return "E001:Fail";
        }
        try {
            int n = Integer.parseInt(string);
            ArrayList<String> arrayList = new ArrayList<String>();
            for (com.huawei.tools.g.a.g g2 : d.cj.values()) {
                if (!"U".equals(g2.aN)) continue;
                arrayList.add(g2.bg);
            }
            if (arrayList.size() != n) {
                return com.huawei.tools.a.b.a((String)"E002", (String)"Verify_fail");
            }
            if (StringUtils.isNotBlank((CharSequence)string2)) {
                HashSet hashSet = new HashSet();
                String[] stringArray = string2.split("\\|");
                int n2 = stringArray.length;
                for (int j = 0; j < n2; ++j) {
                    String string3 = stringArray[j].trim();
                    if (string3.isEmpty()) continue;
                    if (!string3.matches("[LMS]\\d{3}")) {
                        return "E001:Fail";
                    }
                    hashSet.add(string3);
                }
                if (!new HashSet(arrayList).equals(hashSet)) {
                    return com.huawei.tools.a.b.a((String)"E002", (String)"Verify_fail");
                }
            }
            return "E000:Success";
        }
        catch (NumberFormatException numberFormatException) {
            return "E001:Fail";
        }
    }

    static String b(String object, String object2, String object3) {
        String string = "\\d+\\|\\d+";
        if (StringUtils.isBlank((CharSequence)object) || !((String)object).matches(string)) {
            return "E001:Fail";
        }
        if (StringUtils.isBlank((CharSequence)object2) || !((String)object2).matches(string)) {
            return "E001:Fail";
        }
        if (StringUtils.isBlank((CharSequence)object3) || !((String)object3).matches(string)) {
            return "E001:Fail";
        }
        string = (com.huawei.tools.g.a.e)d.ci.get("P1");
        if (string == null) {
            return "E001:Fail";
        }
        try {
            object = e.p((String)object);
            object2 = e.p((String)object2);
            object3 = e.p((String)object3);
            int n = ((com.huawei.tools.g.a.e)string).bb.getOrDefault("S", new AtomicInteger(0)).get();
            int n2 = ((com.huawei.tools.g.a.e)string).bc.getOrDefault("S", new AtomicInteger(0)).get();
            int n3 = ((com.huawei.tools.g.a.e)string).bb.getOrDefault("M", new AtomicInteger(0)).get();
            int n4 = ((com.huawei.tools.g.a.e)string).bc.getOrDefault("M", new AtomicInteger(0)).get();
            int n5 = ((com.huawei.tools.g.a.e)string).bb.getOrDefault("L", new AtomicInteger(0)).get();
            int n6 = ((com.huawei.tools.g.a.e)string).bc.getOrDefault("L", new AtomicInteger(0)).get();
            if (n != object[0] || n2 != object[1] || n3 != object2[0] || n4 != object2[1] || n5 != object3[0] || n6 != object3[1]) {
                return com.huawei.tools.a.b.a((String)"E002", (String)"Verify_fail");
            }
            return "E000:Success";
        }
        catch (NumberFormatException numberFormatException) {
            return "E001:Fail";
        }
    }

    private void g() {
        long l = d.cb;
        ArrayList<String> arrayList = new ArrayList<String>();
        for (f object : d.cg.values()) {
            if (object.bf > 0L || object.bj > 0L || object.bl > 0 || object.bi <= 0L || l - object.bi < 900L) continue;
            arrayList.add(object.bd);
        }
        for (String string : arrayList) {
            this.m(string);
        }
    }

    private void m(String string) {
        Object object;
        f f2 = (f)d.cg.get(string);
        if (f2 == null) {
            return;
        }
        String string2 = f2.aB;
        if (this.bM != null) {
            c.a((f)f2);
        }
        if (f2.bg != null && (object = (com.huawei.tools.g.a.g)d.cj.get(f2.bg)) != null && "U".equals(((com.huawei.tools.g.a.g)object).aN)) {
            ((com.huawei.tools.g.a.g)object).aN = "F";
            ((com.huawei.tools.g.a.g)object).aB = null;
            com.huawei.tools.g.a.e e2 = (com.huawei.tools.g.a.e)d.ci.get(((com.huawei.tools.g.a.g)object).aD);
            if (e2 != null && (object = (AtomicInteger)e2.bb.get(((com.huawei.tools.g.a.g)object).bB)) != null) {
                ((AtomicInteger)object).decrementAndGet();
            }
        }
        f2.bf = d.cb;
        e.k();
        f2.bz = 0L;
        f2.bA = 0L;
        f2.by = 0L;
        e.l(string2);
        d.cg.remove(string);
    }

    private static void c(f f2) {
        double d2 = f2.bv;
        double d3 = f2.bu - d2;
        if (d2 > 0.0) {
            String string = f2.aB;
            if ((string = (String)d.ce.get(string)) != null && (string = (com.huawei.tools.g.a.c)d.cd.get(string)) != null) {
                ((com.huawei.tools.g.a.c)string).aS += d2;
            }
            c.a((String)"balpay", (double)d2);
        }
        if (d3 > 0.0) {
            c.a((String)"temppay", (double)d3);
        }
        f2.bt = "unpaid";
        f2.bu = 0.0;
        f2.bv = 0.0;
        f2.bw = 0L;
    }

    private static void h() {
        long l = d.cb;
        for (f f2 : d.cg.values()) {
            long l2;
            if (f2.bf > 0L || f2.bl > 0 || f2.bj > 0L && f2.bm > 0 || f2.by > 0L) continue;
            if (f2.bA == 0L) {
                f2.bA = l;
            }
            if (f2.bz + (l - f2.bA) < 1296000L) continue;
            String string = f2.aB;
            f2.by = l2 = f2.bA + (1296000L - f2.bz);
            if (!d.co.containsKey(string)) {
                a a2 = new a(string);
                d.co.put(string, a2);
            }
            f2.bz = 0L;
            f2.bA = l2;
        }
    }

    private void i() {
        long l = d.cb;
        ArrayList<String> arrayList = new ArrayList<String>();
        for (f object : d.cg.values()) {
            if (object.bf > 0L || object.by <= 0L || object.bl > 0 || object.bj > 0L && object.bm > 0) continue;
            if (object.bA == 0L) {
                object.bA = l;
            }
            if (object.bz + (l - object.bA) < 1296000L) continue;
            arrayList.add(object.bd);
        }
        for (String string : arrayList) {
            this.m(string);
        }
    }

    private static boolean a(long l) {
        LocalDateTime localDateTime = e.b(l);
        String string = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (bO.contains(string)) {
            return true;
        }
        if (bN.contains(string)) {
            return false;
        }
        return localDateTime.getDayOfWeek().getValue() <= 5;
    }

    private static void n(String string) {
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

    private static void e(String string, String string2, String string3) {
        if ((string = (b)d.cl.get(string)) != null) {
            ((b)string).aE = string2;
            ((b)string).aF = string3;
        }
    }

    private static void j() {
        String[] stringArray = new String[]{"G1", "G2", "G3", "G4"};
        for (int j = 0; j < 4; ++j) {
            String string = stringArray[j];
            if ((string = (b)d.cl.get(string)) == null || !"morning".equals(((b)string).aF) && !"evening".equals(((b)string).aF)) continue;
            if (((b)string).aG != null) {
                ((b)string).aE = ((b)string).aG;
                ((b)string).aF = ((b)string).aH != null ? ((b)string).aH : "default";
            } else {
                ((b)string).aE = "IO";
                ((b)string).aF = "default";
            }
            if (((b)string).aJ == null) continue;
            ((b)string).aJ = ((b)string).aE;
        }
    }

    private static String[] o(String string) {
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

    private static boolean n(String string, String stringArray) {
        if ((string = (com.huawei.tools.g.a.e)d.ci.get(string)) == null) {
            return false;
        }
        stringArray = e.o((String)stringArray);
        int n = 0;
        int n2 = 0;
        for (String string2 : stringArray) {
            int n3 = e.a((com.huawei.tools.g.a.e)string, string2);
            int n4 = ((com.huawei.tools.g.a.e)string).bb.getOrDefault(string2, new AtomicInteger(0)).get();
            int n5 = ((com.huawei.tools.g.a.e)string).bc.getOrDefault(string2, new AtomicInteger(0)).get();
            n += (n3 -= n4);
            n2 += n5;
        }
        return n - n2 > 0;
    }

    static void k() {
        com.huawei.tools.g.a.e e2 = (com.huawei.tools.g.a.e)d.ci.get("P1");
        if (e2 == null) {
            return;
        }
        e2.bc.getOrDefault("S", new AtomicInteger(0)).set(0);
        e2.bc.getOrDefault("M", new AtomicInteger(0)).set(0);
        e2.bc.getOrDefault("L", new AtomicInteger(0)).set(0);
        String[] stringArray = new ArrayList();
        for (f f2 : d.cg.values()) {
            if (f2.bf > 0L || f2.bl > 0 || f2.bg != null) continue;
            stringArray.add(f2);
        }
        Iterator<Object> iterator = stringArray.iterator();
        block1: while (iterator.hasNext()) {
            for (String string : e.o(g.v((String)((f)iterator.next()).aB))) {
                int n;
                int n2;
                int n3 = e.a(e2, string);
                if (n3 - (n2 = e2.bb.getOrDefault(string, new AtomicInteger(0)).get()) - (n = e2.bc.getOrDefault(string, new AtomicInteger(0)).get()) <= 0) continue;
                e2.bc.getOrDefault(string, new AtomicInteger(0)).incrementAndGet();
                continue block1;
            }
        }
    }

    private static int a(com.huawei.tools.g.a.e e2, String string) {
        if ("L".equals(string)) {
            return e2.aY;
        }
        if ("M".equals(string)) {
            return e2.aZ;
        }
        if ("S".equals(string)) {
            return e2.ba;
        }
        return 0;
    }

    private static LocalDateTime b(long l) {
        return LocalDateTime.of(2026, 1, 1, 0, 0).plusSeconds(l);
    }

    private static int[] p(String stringArray) {
        stringArray = stringArray.split("\\|");
        return new int[]{Integer.parseInt(stringArray[0].trim()), Integer.parseInt(stringArray[1].trim())};
    }

    private static String a(b b2, String string, long l) {
        long l2 = l - 5L;
        long l3 = l2 % 86400L;
        if (e.a(l2)) {
            if (l3 >= 25200L && l3 < 32400L) {
                return e.o(b2.aC, "morning");
            }
            if (l3 >= 61200L && l3 < 68400L) {
                return e.o(b2.aC, "evening");
            }
        }
        if (b2.aJ == null) {
            return string;
        }
        if ("morning".equals(b2.aF) || "evening".equals(b2.aF)) {
            if (b2.aG != null) {
                return b2.aG;
            }
            return "IO";
        }
        return b2.aJ;
    }

    private static String o(String string, String string2) {
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
        int n = f2.bl > 0 ? f2.bl : f2.bm;
        int n2 = f3.bl > 0 ? f3.bl : f3.bm;
        return Integer.compare(n, n2);
    }

    private static /* synthetic */ List q(String string) {
        return new ArrayList();
    }

    private static /* synthetic */ List r(String string) {
        return new ArrayList();
    }
}
