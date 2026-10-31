/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.huawei.tools.a.a
 *  com.huawei.tools.b.a
 *  com.huawei.tools.g.a.c
 *  com.huawei.tools.g.a.d
 *  com.huawei.tools.g.a.i
 *  com.huawei.tools.g.b.a.g
 *  com.huawei.tools.h.d
 */
package com.huawei.tools.g.b.a;

import com.huawei.tools.g.a.c;
import com.huawei.tools.g.a.i;
import com.huawei.tools.g.b.a.g;
import com.huawei.tools.h.d;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

final class a {
    a() {
    }

    static String b(String string, int n) {
        double d2;
        if ((string = (c)d.df.get(string)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(((c)string).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (n > 0 && n <= 50) {
            d2 = 0.0;
        } else if (n == 100) {
            d2 = 20.0;
        } else if (n == 500) {
            d2 = 120.0;
        } else {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ae.key);
        }
        if (((c)string).by + (double)n + d2 > 5000.0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.af.key);
        }
        ((c)string).by = ((c)string).by + (double)n + d2;
        ((c)string).bz += d2;
        a.a(d.de, "recharge", n);
        return "E000:Success";
    }

    static String l(String string2, String string3) {
        double d2;
        String string4;
        Object object = (c)d.df.get(string2);
        if (object == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(object.bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (object.bs == null || !object.bs.contains(string3)) {
            return "E001:Fail";
        }
        switch (string4 = g.I((String)string3)) {
            case "L": {
                double d3 = 800.0;
                break;
            }
            case "M": {
                double d3 = 600.0;
                break;
            }
            case "S": {
                double d3 = 400.0;
                break;
            }
            default: {
                double d3 = d2 = 400.0;
            }
        }
        if (object.by < d2) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ag.key);
        }
        Object object2 = (List)d.ds.get(string3);
        com.huawei.tools.g.a.d d4 = null;
        if (object2 != null) {
            object2 = object2.iterator();
            while (object2.hasNext()) {
                com.huawei.tools.g.a.d d5 = (com.huawei.tools.g.a.d)object2.next();
                if (!d5.bD || d5.bC <= d.de) continue;
                d4 = d5;
            }
        }
        if (d.dt.getOrDefault(string3, 0) >= 12) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ah.key);
        }
        if (d4 != null && d4.bC >= 31535999L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ah.key);
        }
        double d6 = Math.min(object.bz, d2);
        object.bz -= d6;
        object.by -= d2;
        long l = d.de;
        if (d4 != null) {
            long l2 = d4.bC + 2592000L;
            if (l2 > 31535999L) {
                l2 = 31535999L;
            }
            d4.bC = l2;
        } else {
            long l3 = l;
            long l4 = l3 - l3 % 86400L;
            long l5 = l4 + 2592000L - 1L;
            if (l5 > 31535999L) {
                l5 = 31535999L;
            }
            object = a.e();
            string2 = new com.huawei.tools.g.a.d((String)object, string3, string2, string4, l4, l5);
            d.dr.put(object, string2);
            d.ds.computeIfAbsent(string3, string -> new ArrayList()).add(string2);
        }
        a.a(l, "monthcard", d2);
        d.dt.merge(string3, 1, Integer::sum);
        return "E000:Success";
    }

    static String b(String string2, String string3, int n) {
        Object object = (String)d.dg.get(string2);
        String string4 = (String)d.dg.get(string3);
        if (object == null || string4 == null) {
            return "E001:Fail";
        }
        object = (c)d.df.get(object);
        Object object2 = (c)d.df.get(string4);
        if (object == null || object2 == null) {
            return "E001:Fail";
        }
        if (!"active".equals(((c)object).bt) || !"active".equals(object2.bt)) {
            return "E001:Fail";
        }
        object = g.I((String)string2);
        if (!((String)object).equals(object2 = g.I((String)string3))) {
            return "E001:Fail";
        }
        object = (List)d.ds.get(string2);
        if (object == null || object.isEmpty()) {
            return "E001:Fail";
        }
        int n2 = g.O((String)string2);
        if (n <= 0 || n > n2) {
            return "E001:Fail";
        }
        n2 = (int)((double)n * 0.8);
        long l = d.de;
        long l2 = l - l % 86400L + 86400L;
        long l3 = n;
        for (n = object.size() - 1; n >= 0 && l3 > 0L; --n) {
            long l4;
            com.huawei.tools.g.a.d d2 = (com.huawei.tools.g.a.d)object.get(n);
            if (!d2.bD || d2.bC <= l || (l4 = (d2.bC - l2 + 1L) / 86400L) <= 0L) continue;
            if (l4 <= l3) {
                l3 -= l4;
                d2.bC = l2 - 1L;
                continue;
            }
            long l5 = l4 - l3;
            d2.bC = l2 + l5 * 86400L - 1L;
            l3 = 0L;
        }
        if (n2 > 0) {
            List list = (List)d.ds.get(string3);
            long l6 = (long)n2 * 86400L;
            String string5 = g.I((String)string3);
            com.huawei.tools.g.a.d d3 = null;
            if (list != null) {
                for (com.huawei.tools.g.a.d d4 : list) {
                    if (!d4.bD || d4.bC <= l) continue;
                    d3 = d4;
                }
            }
            if (d3 != null) {
                long l7 = d3.bC + l6;
                if (l7 > 31535999L) {
                    l7 = 31535999L;
                }
                d3.bC = l7;
            } else {
                long l8 = l + l6;
                if (l8 > 31535999L) {
                    l8 = 31535999L;
                }
                String string6 = a.e();
                list = new com.huawei.tools.g.a.d(string6, string3, string4, string5, l, l8);
                d.dr.put(string6, list);
                d.ds.computeIfAbsent(string3, string -> new ArrayList()).add(list);
            }
        }
        return "E000:Success";
    }

    static void a(long l, String string, double d2) {
        String string3;
        String string4 = string3 = g.c((long)l);
        string3 = d.du.computeIfAbsent(string4, string2 -> new i(string4));
        switch (string) {
            case "recharge": {
                ((i)string3).co += d2;
                return;
            }
            case "monthcard": {
                ((i)string3).cp += d2;
                return;
            }
            case "temppay": {
                ((i)string3).cn += d2;
                return;
            }
            case "balpay": {
                ((i)string3).cq += d2;
            }
        }
    }

    private static String e() {
        int n;
        String string;
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        do {
            n = ((Random)threadLocalRandom).nextInt(100000000);
        } while (d.dr.containsKey(string = "MC" + String.format("%08d", n)));
        return string;
    }
}
