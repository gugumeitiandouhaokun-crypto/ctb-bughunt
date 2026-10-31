/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.g.b.a;

import com.huawei.tools.g.a.c;
import com.huawei.tools.g.a.i;
import com.huawei.tools.g.b.a.g;
import com.huawei.tools.h.d;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

final class a {
    a() {
    }

    static String b(String object, int n) {
        double d2;
        if ((object = d.df.get(object)) == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(((c)object).bt)) {
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
        if (((c)object).by + (double)n + d2 > 5000.0) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.af.key);
        }
        ((c)object).by = ((c)object).by + (double)n + d2;
        ((c)object).bz += d2;
        a.a(d.de, "recharge", n);
        return "E000:Success";
    }

    static String l(String object, String string2) {
        double d2;
        String string3;
        Object object2 = d.df.get(object);
        if (object2 == null) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(((c)object2).bt)) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (((c)object2).bs == null || !((c)object2).bs.contains(string2)) {
            return "E001:Fail";
        }
        switch (string3 = g.I(string2)) {
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
        if (((c)object2).by < d2) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ag.key);
        }
        Object object3 = d.ds.get(string2);
        com.huawei.tools.g.a.d d4 = null;
        if (object3 != null) {
            object3 = object3.iterator();
            while (object3.hasNext()) {
                com.huawei.tools.g.a.d d5 = (com.huawei.tools.g.a.d)object3.next();
                if (!d5.bD || d5.bC <= d.de) continue;
                d4 = d5;
            }
        }
        if (d.dt.getOrDefault(string2, 0) >= 12) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ah.key);
        }
        if (d4 != null && d4.bC >= 31535999L) {
            throw new com.huawei.tools.b.a("E001:Fail", com.huawei.tools.a.a.ah.key);
        }
        double d6 = Math.min(((c)object2).bz, d2);
        ((c)object2).bz -= d6;
        ((c)object2).by -= d2;
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
            object2 = a.e();
            object = new com.huawei.tools.g.a.d((String)object2, string2, (String)object, string3, l4, l5);
            d.dr.put((String)object2, (com.huawei.tools.g.a.d)object);
            d.ds.computeIfAbsent(string2, string -> new ArrayList()).add(object);
        }
        a.a(l, "monthcard", d2);
        d.dt.merge(string2, 1, Integer::sum);
        return "E000:Success";
    }

    static String b(String string2, String string3, int n) {
        List<com.huawei.tools.g.a.d> list = d.dg.get(string2);
        String string4 = d.dg.get(string3);
        if (list == null || string4 == null) {
            return "E001:Fail";
        }
        list = d.df.get(list);
        Object object = d.df.get(string4);
        if (list == null || object == null) {
            return "E001:Fail";
        }
        if (!"active".equals(((c)((Object)list)).bt) || !"active".equals(((c)object).bt)) {
            return "E001:Fail";
        }
        list = g.I(string2);
        if (!((String)((Object)list)).equals(object = g.I(string3))) {
            return "E001:Fail";
        }
        list = d.ds.get(string2);
        if (list == null || list.isEmpty()) {
            return "E001:Fail";
        }
        int n2 = g.O(string2);
        if (n <= 0 || n > n2) {
            return "E001:Fail";
        }
        n2 = (int)((double)n * 0.8);
        long l = d.de;
        long l2 = l - l % 86400L + 86400L;
        long l3 = n;
        for (n = list.size() - 1; n >= 0 && l3 > 0L; --n) {
            long l4;
            com.huawei.tools.g.a.d d2 = list.get(n);
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
            Object object2 = d.ds.get(string3);
            long l6 = (long)n2 * 86400L;
            String string5 = g.I(string3);
            com.huawei.tools.g.a.d d3 = null;
            if (object2 != null) {
                Iterator<com.huawei.tools.g.a.d> iterator = object2.iterator();
                while (iterator.hasNext()) {
                    com.huawei.tools.g.a.d d4 = iterator.next();
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
                object2 = new com.huawei.tools.g.a.d(string6, string3, string4, string5, l, l8);
                d.dr.put(string6, (com.huawei.tools.g.a.d)object2);
                d.ds.computeIfAbsent(string3, string -> new ArrayList()).add(object2);
            }
        }
        return "E000:Success";
    }

    static void a(long l, String string, double d2) {
        Object object = g.c(l);
        String string3 = object;
        object = d.du.computeIfAbsent(string3, string2 -> new i(string3));
        switch (string) {
            case "recharge": {
                ((i)object).co += d2;
                return;
            }
            case "monthcard": {
                ((i)object).cp += d2;
                return;
            }
            case "temppay": {
                ((i)object).cn += d2;
                return;
            }
            case "balpay": {
                ((i)object).cq += d2;
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

