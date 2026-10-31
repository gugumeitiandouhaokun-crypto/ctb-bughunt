/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.b.a;

import com.huawei.tools.g.a.f;
import com.huawei.tools.g.a.h;
import com.huawei.tools.g.b.a.a;
import com.huawei.tools.g.b.a.g;
import com.huawei.tools.h.d;
import java.util.Map;

final class c {
    private static final double[][] bK = new double[][]{{4.0, 2.0, 20.0}, {6.0, 3.0, 40.0}, {8.0, 4.0, 60.0}};
    private final a bL;

    c(a a2) {
        this.bL = a2;
    }

    static void a(f f2) {
        double d2;
        Object object;
        if (f2.bq <= 0L) {
            return;
        }
        if (f2.br <= 0L) {
            f2.br = d.cb;
        }
        if (!d.ce.containsKey(object = f2.aB)) {
            return;
        }
        double d3 = f2.bu;
        boolean bl = "paid".equals(f2.bt) && f2.bw > 0L;
        if (bl) {
            f f3 = f2;
            d2 = c.a(f3, f3.bw);
        } else {
            d2 = c.b(f2);
        }
        if (d2 <= 0.0) {
            f2.bt = "paid";
            f2.bu = bl ? d3 : 0.0;
            f2.bw = d.cb;
            return;
        }
        object = d.ce.get(object);
        if ((object = d.cd.get(object)) != null) {
            ((com.huawei.tools.g.a.c)object).aS -= d2;
        }
        f2.bt = "paid";
        f2.bu = d3 + d2;
        f2.bv += d2;
        f2.bw = d.cb;
        a.a(d.cb, "balpay", d2);
    }

    static void a(double d2) {
        a.a(d.cb, "balpay", d2);
    }

    static void a(String string, double d2) {
        a.a(d.cb, string, -d2);
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
        string3 = g.c(d.cb);
        double d4 = 0.0;
        for (Map.Entry<String, h> entry : d.cs.entrySet()) {
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
                    d4 += ((h)object).bE;
                    break;
                }
                case "monthcard": {
                    d4 += ((h)object).bF;
                    break;
                }
                case "temppay": {
                    d4 += ((h)object).bD;
                    break;
                }
                case "balpay": {
                    d4 += ((h)object).bG;
                }
            }
        }
        if (Math.abs((d4 = (double)Math.round(d4 * 100.0) / 100.0) - (d2 = (double)Math.round(d3 * 100.0) / 100.0)) < 0.01) {
            return "E000:Success";
        }
        return "E001:Fail";
    }

    static double b(f f2) {
        long l = f2.bq;
        long l2 = f2.br;
        if (l <= 0L) {
            return 0.0;
        }
        if (l2 <= 0L) {
            l2 = d.cb;
        }
        if (l2 <= l) {
            return 0.0;
        }
        if (l2 - l <= 1800L) {
            return 0.0;
        }
        return c.a(f2, l, l2, 2);
    }

    static double a(f f2, long l) {
        long l2 = f2.br;
        if (l2 <= 0L) {
            l2 = d.cb;
        }
        if (l >= l2) {
            return 0.0;
        }
        return c.a(f2, l, l2, 0);
    }

    private static double a(f object, long l, long l2, int n) {
        String string;
        String string2 = ((f)object).aV;
        if (string2 == null) {
            string2 = g.v(((f)object).aB);
        }
        if ((string = ((f)object).bs) == null) {
            string = string2;
        }
        object = ((f)object).aB;
        boolean bl = d.ce.containsKey(object);
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
                if (g.x(string3)) {
                    d4 = c.a(l8, string, string2, n2);
                    d2 += d4;
                } else if (g.a((String)object, l7)) {
                    d4 = c.a(l8, string, string2, n2);
                    d2 += d4;
                } else if (g.y(string3)) {
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
        int n2 = c.k(string);
        double d2 = bK[n2][0];
        double d3 = bK[n2][1];
        double d4 = bK[n2][2];
        long l2 = (l + 1800L - 1L) / 1800L;
        double d5 = l2 <= (long)n ? (double)l2 * (d2 / 2.0) : (double)n * (d2 / 2.0) + (double)(l2 - (long)n) * (d3 / 2.0);
        return Math.min(d5, d4);
    }

    private static double a(long l, String string, String string2, int n) {
        int n2;
        if (string.equals(string2)) {
            return 0.0;
        }
        int n3 = c.k(string);
        if (n3 <= (n2 = c.k(string2))) {
            return 0.0;
        }
        double d2 = bK[n3][0];
        double d3 = bK[n3][1];
        double d4 = bK[n2][0];
        double d5 = bK[n2][1];
        long l2 = (l + 1800L - 1L) / 1800L;
        double d6 = l2 <= (long)n ? (double)l2 * ((d2 - d4) / 2.0) : (double)n * ((d2 - d4) / 2.0) + (double)(l2 - (long)n) * ((d3 - d5) / 2.0);
        return Math.min(d6, 15.0);
    }

    private static int k(String string) {
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

