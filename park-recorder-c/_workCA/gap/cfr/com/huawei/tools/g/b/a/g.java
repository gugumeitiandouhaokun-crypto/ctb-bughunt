/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.huawei.tools.a.a
 *  com.huawei.tools.b.a
 *  com.huawei.tools.g.a.b
 *  com.huawei.tools.g.a.d
 *  com.huawei.tools.g.a.e
 *  com.huawei.tools.g.a.f
 *  com.huawei.tools.g.a.j
 *  com.huawei.tools.g.a.k
 *  com.huawei.tools.h.d
 *  org.apache.commons.lang3.StringUtils
 */
package com.huawei.tools.g.b.a;

import com.huawei.tools.b.a;
import com.huawei.tools.g.a.b;
import com.huawei.tools.g.a.e;
import com.huawei.tools.g.a.f;
import com.huawei.tools.g.a.j;
import com.huawei.tools.g.a.k;
import com.huawei.tools.h.d;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;

final class g {
    private static final Set<String> cO = new HashSet<String>(Arrays.asList("2026-02-15", "2026-02-16", "2026-02-17", "2026-02-18", "2026-02-19", "2026-02-20", "2026-02-21", "2026-02-22", "2026-02-23", "2026-10-01", "2026-10-02", "2026-10-03", "2026-10-04", "2026-10-05", "2026-10-06", "2026-10-07", "2026-05-01", "2026-05-02", "2026-05-03", "2026-05-04", "2026-05-05"));
    private static final Set<String> cP = new HashSet<String>(Arrays.asList("2026-01-01", "2026-01-02", "2026-01-03", "2026-04-04", "2026-04-05", "2026-04-06", "2026-06-19", "2026-06-20", "2026-06-21", "2026-09-25", "2026-09-26", "2026-09-27"));
    private static final Set<String> cQ = new HashSet<String>(Arrays.asList("2026-01-04", "2026-02-14", "2026-02-28", "2026-05-09", "2026-09-20", "2026-10-10"));

    static String a(b b2) {
        return b2.bk;
    }

    static boolean F(String string) {
        for (f f2 : d.di.values()) {
            if (!string.equals(f2.bh) || f2.bM != 0L) continue;
            return true;
        }
        return false;
    }

    static String e(f f2) {
        if (f2.bM > 0L) {
            return "outpark";
        }
        if (f2.bQ > 0L && f2.bT > 0) {
            return "exiting";
        }
        if (f2.bN != null) {
            return "parked";
        }
        if (f2.bS > 0) {
            return "entrying";
        }
        return "inpark";
    }

    static String G(String string) {
        if (StringUtils.isBlank((CharSequence)string) || string.length() < 6 || string.length() > 7) {
            throw new a("E001:Fail", com.huawei.tools.a.a.I.key);
        }
        char c = string.charAt(0);
        if (c != 'L' && c != 'M' && c != 'S') {
            throw new a("E001:Fail", com.huawei.tools.a.a.I.key);
        }
        if (!string.substring(1).matches("\\d+")) {
            throw new a("E001:Fail", com.huawei.tools.a.a.I.key);
        }
        return String.valueOf(c);
    }

    static String o() {
        int n;
        String string;
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        while (d.df.containsKey(string = "CID" + String.format("%08d", n = ((Random)threadLocalRandom).nextInt(100000000))) || g.H(string)) {
        }
        return string;
    }

    private static boolean H(String string) {
        for (j j2 : d.dh.values()) {
            if (!string.equals(j2.bO)) continue;
            return true;
        }
        return false;
    }

    static int p() {
        int n = 0;
        for (j j2 : d.dh.values()) {
            if (!"active".equals(j2.bt)) continue;
            ++n;
        }
        return n;
    }

    static boolean f(f f2) {
        return f2.bM == 0L && f2.bQ == 0L && f2.bN == null && !f2.bR && f2.bS > 0;
    }

    static String I(String string) {
        if (string == null || string.isEmpty()) {
            return "S";
        }
        return String.valueOf(string.charAt(0));
    }

    static boolean J(String string) {
        return d.dq.containsKey(string);
    }

    static void r(String string2, String string3) {
        if ((string2 = (e)d.dj.get(string2)) != null && ((e)string2).bJ != null) {
            ((e)string2).bJ.computeIfAbsent(string3, string -> new AtomicInteger(0)).incrementAndGet();
        }
    }

    static void s(String object, String string) {
        if ((object = (e)d.dj.get(object)) != null && ((e)object).bJ != null && (object = (AtomicInteger)((e)object).bJ.get(string)) != null && ((AtomicInteger)object).get() > 0) {
            ((AtomicInteger)object).decrementAndGet();
        }
    }

    static boolean K(String string) {
        for (k k2 : d.dx.values()) {
            if (!string.equals(k2.bh) || !"queuing".equals(k2.bt) && !"washing".equals(k2.bt)) continue;
            return true;
        }
        return false;
    }

    static String c(long l) {
        return LocalDateTime.of(2026, 1, 1, 0, 0).plusSeconds(l).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    static boolean L(String string) {
        return cO.contains(string);
    }

    static boolean M(String string) {
        return cP.contains(string);
    }

    static boolean N(String string) {
        return cQ.contains(string);
    }

    static int O(String iterator) {
        if ((iterator = (List)d.ds.get(iterator)) == null || iterator.isEmpty()) {
            return 0;
        }
        long l = d.de;
        long l2 = l - l % 86400L + 86400L;
        int n = 0;
        iterator = iterator.iterator();
        while (iterator.hasNext()) {
            long l3;
            com.huawei.tools.g.a.d d2 = (com.huawei.tools.g.a.d)iterator.next();
            if (!d2.bD || d2.bC <= l || (l3 = d2.bC - l2) <= 0L) continue;
            n += (int)((l3 + 1L) / 86400L);
        }
        return n;
    }

    static int P(String iterator) {
        if ((iterator = (List)d.ds.get(iterator)) == null || iterator.isEmpty()) {
            return 0;
        }
        long l = d.de;
        long l2 = l - l % 86400L;
        int n = 0;
        iterator = iterator.iterator();
        while (iterator.hasNext()) {
            long l3;
            com.huawei.tools.g.a.d d2 = (com.huawei.tools.g.a.d)iterator.next();
            if (!d2.bD || d2.bC <= l || (l3 = d2.bC - l2) <= 0L) continue;
            n += (int)((l3 + 1L) / 86400L);
        }
        return n;
    }

    static boolean a(String iterator, long l) {
        if ((iterator = (List)d.ds.get(iterator)) == null || iterator.isEmpty()) {
            return false;
        }
        iterator = iterator.iterator();
        while (iterator.hasNext()) {
            com.huawei.tools.g.a.d d2 = (com.huawei.tools.g.a.d)iterator.next();
            if (!d2.bD || d2.startTime > l || l >= d2.bC) continue;
            return true;
        }
        return false;
    }
}
