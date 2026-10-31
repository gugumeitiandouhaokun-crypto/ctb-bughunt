/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.b.a;

import com.huawei.tools.g.a.f;
import com.huawei.tools.g.b.a.a;
import com.huawei.tools.g.b.a.g;
import com.huawei.tools.g.b.a.i;
import com.huawei.tools.h.d;
import java.util.Map;

final class c {
    private static final double[][] cF = new double[][]{{4.0, 2.0, 20.0}, {6.0, 3.0, 40.0}, {8.0, 4.0, 60.0}};
    private final a cG;
    i cH;

    c(a a2) {
        this.cG = a2;
    }

    final void a(f f2) {
        double d2;
        double d3;
        if (f2.bY <= 0L) {
            f2.bY = d.de;
        }
        Object object = f2.bh;
        boolean bl = d.dg.containsKey(object);
        double d4 = 0.0;
        if (this.cH != null) {
            d4 = i.s(f2.bK);
        }
        if (f2.bX <= 0L) {
            if (d4 > 0.0 && bl) {
                String string = d.dg.get(object);
                com.huawei.tools.g.a.c c2 = d.df.get(string);
                if (c2 != null) {
                    c2.by -= d4;
                }
                f2.ca = "paid";
                f2.cb = d4;
                f2.cd = d4;
                f2.ce = d.de;
                i.v(f2.bK);
                a.a(d.de, "balpay", d4);
            }
            return;
        }
        if (!bl) {
            return;
        }
        double d5 = f2.cb;
        bl = "paid".equals(f2.ca) && f2.ce > 0L;
        if (bl) {
            f f3 = f2;
            d3 = this.a(f3, f3.ce);
        } else {
            d3 = this.b(f2);
        }
        double d6 = d3 + d4;
        if (d2 <= 0.0) {
            f2.ca = "paid";
            f2.cb = bl ? d5 : 0.0;
            f2.ce = d.de;
            if (d4 > 0.0) {
                i.v(f2.bK);
            }
            return;
        }
        object = d.dg.get(object);
        if ((object = d.df.get(object)) != null) {
            ((com.huawei.tools.g.a.c)object).by -= d6;
        }
        f2.ca = "paid";
        f2.cb = d5 + d6;
        f2.cd += d6;
        f2.ce = d.de;
        if (d4 > 0.0) {
            i.v(f2.bK);
        }
        a.a(d.de, "balpay", d6);
    }

    static void a(double d2) {
        a.a(d.de, "balpay", d2);
    }

    final double s(String string) {
        if (this.cH == null) {
            return 0.0;
        }
        return i.s(string);
    }

    private long[][] t(String string) {
        if (this.cH == null) {
            return new long[0][];
        }
        return i.t(string);
    }

    private long[][] u(String string) {
        if (this.cH == null) {
            return new long[0][];
        }
        return i.u(string);
    }

    final void v(String string) {
        if (this.cH == null) {
            return;
        }
        i.v(string);
    }

    final void w(String string) {
        if (this.cH == null) {
            return;
        }
        i.w(string);
    }

    static void a(String string, double d2) {
        a.a(d.de, string, -d2);
    }

    static String d(String string, String string2, String string3) {
        double d2;
        if (!("day".equals(string) || "month".equals(string) || "year".equals(string))) {
            return "E001:Fail";
        }
        if (!("recharge".equals(string2) || "monthcard".equals(string2) || "temppay".equals(string2) || "balpay".equals(string2))) {
            return "E001:Fail";
        }
        if (string3 == null || !string3.matches("^-?\\d+\\.\\d{2}$")) {
            return "E001:Fail";
        }
        double d3 = Double.parseDouble(string3);
        string3 = g.c(d.de);
        double d4 = 0.0;
        for (Map.Entry<String, com.huawei.tools.g.a.i> entry : d.du.entrySet()) {
            String string4 = entry.getKey();
            boolean bl = false;
            switch (string) {
                case "day": {
                    bl = string4.equals(string3);
                    break;
                }
                case "month": {
                    bl = string4.substring(0, 7).equals(string3.substring(0, 7));
                    break;
                }
                case "year": {
                    bl = string4.substring(0, 4).equals(string3.substring(0, 4));
                }
            }
            if (!bl) continue;
            Object object = entry.getValue();
            switch (string2) {
                case "recharge": {
                    d4 += ((com.huawei.tools.g.a.i)object).co;
                    break;
                }
                case "monthcard": {
                    d4 += ((com.huawei.tools.g.a.i)object).cp;
                    break;
                }
                case "temppay": {
                    d4 += ((com.huawei.tools.g.a.i)object).cn;
                    break;
                }
                case "balpay": {
                    d4 += ((com.huawei.tools.g.a.i)object).cq;
                }
            }
        }
        if (Math.abs((d4 = (double)Math.round(d4 * 100.0) / 100.0) - (d2 = (double)Math.round(d3 * 100.0) / 100.0)) < 0.01) {
            return "E000:Success";
        }
        return "E001:Fail";
    }

    final double b(f f2) {
        long l = f2.bX;
        long l2 = f2.bY;
        if (l <= 0L) {
            return 0.0;
        }
        if (l2 <= 0L) {
            l2 = d.de;
        }
        if (l2 <= l) {
            return 0.0;
        }
        if (l2 - l <= 1800L) {
            return 0.0;
        }
        return this.a(f2, l, l2, 2);
    }

    final double a(f f2, long l) {
        long l2 = f2.bY;
        if (l2 <= 0L) {
            l2 = d.de;
        }
        if (l >= l2) {
            return 0.0;
        }
        return this.a(f2, l, l2, 0);
    }

    private double a(f object, long l, long l2, int n) {
        String string;
        long[][] lArray = this.t(((f)object).bK);
        long[][] lArray2 = this.u(((f)object).bK);
        if (lArray.length == 0 && lArray2.length == 0) {
            return c.b((f)object, l, l2, n);
        }
        String string2 = ((f)object).bB;
        if (string2 == null) {
            string2 = g.I(((f)object).bh);
        }
        if ((string = ((f)object).bZ) == null) {
            string = string2;
        }
        object = ((f)object).bh;
        boolean bl = d.dg.containsKey(object);
        double d2 = 0.0;
        long l3 = l;
        long l4 = 5400L;
        while (l3 < l2) {
            long l5;
            long l6 = l3;
            long l7 = l6 - l6 % 86400L;
            long l8 = l7 + 86400L;
            long l9 = Math.max(l3, l7);
            long l10 = Math.min(l2, l8);
            long l11 = l10 - l9;
            long[][] lArray3 = lArray;
            int n2 = lArray.length;
            for (int i2 = 0; i2 < n2; ++i2) {
                long[] lArray4 = lArray3[i2];
                long l12 = Math.max(lArray4[0], l9);
                l5 = Math.min(lArray4[1], l10);
                if (l5 <= l12) continue;
                l11 -= l5 - l12;
            }
            long l13 = l11;
            if (l4 > 0L) {
                long[][] lArray5 = lArray2;
                int n3 = lArray2.length;
                for (int i3 = 0; i3 < n3; ++i3) {
                    long[] lArray6 = lArray5[i3];
                    l5 = Math.max(lArray6[0], l9);
                    long l14 = Math.min(lArray6[1], l10);
                    if (l14 <= l5) continue;
                    long l15 = l14 - l5;
                    long[][] lArray7 = lArray;
                    int n4 = lArray.length;
                    for (int i4 = 0; i4 < n4; ++i4) {
                        long[] lArray8 = lArray7[i4];
                        long l16 = Math.max(lArray8[0], l5);
                        long l17 = Math.min(lArray8[1], l14);
                        if (l17 <= l16) continue;
                        l15 -= l17 - l16;
                    }
                    if (l15 <= 0L) continue;
                    long l18 = Math.min(l15, l4);
                    l13 -= l18;
                    if ((l4 -= l18) <= 0L) break;
                }
            }
            if (l11 > 0L) {
                String string3 = g.c(l9);
                long l19 = (l11 + 1800L - 1L) / 1800L;
                int n5 = Math.min(n, (int)l19);
                if (g.L(string3)) {
                    double d3 = c.a(l11, string, string2, n5);
                    d2 += d3;
                } else if (g.a((String)object, l9)) {
                    double d4 = c.a(l11, string, string2, n5);
                    d2 += d4;
                } else if (g.M(string3)) {
                    double d5 = l13 > 0L ? c.a(l13, string2, n5) * 0.8 : 0.0;
                    double d6 = c.a(l11, string, string2, n5);
                    if (!bl) {
                        d5 *= 1.5;
                    }
                    d2 += d5 + d6;
                } else {
                    double d7 = l13 > 0L ? c.a(l13, string2, n5) : 0.0;
                    double d8 = c.a(l11, string, string2, n5);
                    if (!bl) {
                        d7 *= 1.5;
                    }
                    d2 += d7 + d8;
                }
            }
            n = 0;
            l3 = l8;
        }
        return (double)Math.round(d2 * 100.0) / 100.0;
    }

    private static double b(f object, long l, long l2, int n) {
        String string;
        String string2 = ((f)object).bB;
        if (string2 == null) {
            string2 = g.I(((f)object).bh);
        }
        if ((string = ((f)object).bZ) == null) {
            string = string2;
        }
        object = ((f)object).bh;
        boolean bl = d.dg.containsKey(object);
        double d2 = 0.0;
        long l3 = l;
        int n2 = n;
        while (l3 < l2) {
            long l4 = l3;
            long l5 = l4 - l4 % 86400L;
            long l6 = l5 + 86400L;
            long l7 = Math.max(l3, l5);
            long l8 = Math.min(l2, l6) - l7;
            if (l8 > 0L) {
                double d3;
                double d4;
                String string3 = g.c(l7);
                long l9 = (l8 + 1800L - 1L) / 1800L;
                n2 = Math.min(n2, (int)l9);
                if (g.L(string3)) {
                    d4 = c.a(l8, string, string2, n2);
                    d2 += d4;
                } else if (g.a((String)object, l7)) {
                    d4 = c.a(l8, string, string2, n2);
                    d2 += d4;
                } else if (g.M(string3)) {
                    d4 = c.a(l8, string2, n2);
                    d3 = c.a(l8, string, string2, n2);
                    d4 *= 0.8;
                    if (!bl) {
                        d4 *= 1.5;
                    }
                    d2 += d4 + d3;
                } else {
                    d4 = c.a(l8, string2, n2);
                    d3 = c.a(l8, string, string2, n2);
                    if (!bl) {
                        d4 *= 1.5;
                    }
                    d2 += d4 + d3;
                }
            }
            n2 = 0;
            l3 = l6;
        }
        return (double)Math.round(d2 * 100.0) / 100.0;
    }

    private static double a(long l, String string, int n) {
        int n2 = c.x(string);
        double d2 = cF[n2][0];
        double d3 = cF[n2][1];
        double d4 = cF[n2][2];
        long l2 = (l + 1800L - 1L) / 1800L;
        double d5 = l2 <= (long)n ? (double)l2 * (d2 / 2.0) : (double)n * (d2 / 2.0) + (double)(l2 - (long)n) * (d3 / 2.0);
        return Math.min(d5, d4);
    }

    private static double a(long l, String string, String string2, int n) {
        int n2;
        if (string.equals(string2)) {
            return 0.0;
        }
        int n3 = c.x(string);
        if (n3 <= (n2 = c.x(string2))) {
            return 0.0;
        }
        double d2 = cF[n3][0];
        double d3 = cF[n3][1];
        double d4 = cF[n2][0];
        double d5 = cF[n2][1];
        long l2 = (l + 1800L - 1L) / 1800L;
        double d6 = l2 <= (long)n ? (double)l2 * ((d2 - d4) / 2.0) : (double)n * ((d2 - d4) / 2.0) + (double)(l2 - (long)n) * ((d3 - d5) / 2.0);
        return Math.min(d6, 15.0);
    }

    private static int x(String string) {
        int n = -1;
        switch (string.hashCode()) {
            case 83: {
                if (!string.equals("S")) break;
                n = 0;
                break;
            }
            case 77: {
                if (!string.equals("M")) break;
                n = 1;
                break;
            }
            case 76: {
                if (!string.equals("L")) break;
                n = 2;
            }
        }
        switch (n) {
            case 0: {
                return 0;
            }
            case 1: {
                return 1;
            }
            case 2: {
                return 2;
            }
        }
        return 0;
    }
}

