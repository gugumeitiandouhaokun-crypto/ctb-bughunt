/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.huawei.tools.a.a
 *  com.huawei.tools.b.a
 *  com.huawei.tools.g.a.b
 *  com.huawei.tools.g.a.d
 *  com.huawei.tools.g.a.f
 *  com.huawei.tools.g.a.i
 *  com.huawei.tools.h.d
 *  org.apache.commons.lang3.StringUtils
 */
package com.huawei.tools.g.b.a;

import com.huawei.tools.b.a;
import com.huawei.tools.g.a.b;
import com.huawei.tools.g.a.f;
import com.huawei.tools.g.a.i;
import com.huawei.tools.h.d;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.StringUtils;

final class g {
    private static final Set<String> bS = new HashSet<String>(Arrays.asList("2026-02-15", "2026-02-16", "2026-02-17", "2026-02-18", "2026-02-19", "2026-02-20", "2026-02-21", "2026-02-22", "2026-02-23", "2026-10-01", "2026-10-02", "2026-10-03", "2026-10-04", "2026-10-05", "2026-10-06", "2026-10-07", "2026-05-01", "2026-05-02", "2026-05-03", "2026-05-04", "2026-05-05"));
    private static final Set<String> bT = new HashSet<String>(Arrays.asList("2026-01-01", "2026-01-02", "2026-01-03", "2026-04-04", "2026-04-05", "2026-04-06", "2026-06-19", "2026-06-20", "2026-06-21", "2026-09-25", "2026-09-26", "2026-09-27"));

    static String a(b b2) {
        return b2.aE;
    }

    static boolean s(String string) {
        for (f f2 : d.cg.values()) {
            if (!string.equals(f2.aB) || f2.bf != 0L) continue;
            return true;
        }
        return false;
    }

    static String d(f f2) {
        if (f2.bf > 0L) {
            return "outpark";
        }
        if (f2.bj > 0L && f2.bm > 0) {
            return "exiting";
        }
        if (f2.bg != null) {
            return "parked";
        }
        if (f2.bl > 0) {
            return "entrying";
        }
        return "inpark";
    }

    static String t(String string) {
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

    static String l() {
        int n;
        String string;
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        while (d.cd.containsKey(string = "CID" + String.format("%08d", n = ((Random)threadLocalRandom).nextInt(100000000))) || g.u(string)) {
        }
        return string;
    }

    private static boolean u(String string) {
        for (i i2 : d.cf.values()) {
            if (!string.equals(i2.bh)) continue;
            return true;
        }
        return false;
    }

    static int m() {
        int n = 0;
        for (i i2 : d.cf.values()) {
            if (!"active".equals(i2.aN)) continue;
            ++n;
        }
        return n;
    }

    static boolean e(f f2) {
        return f2.bf == 0L && f2.bj == 0L && f2.bg == null && !f2.bk && f2.bl > 0;
    }

    static String v(String string) {
        if (string == null || string.isEmpty()) {
            return "S";
        }
        return String.valueOf(string.charAt(0));
    }

    static boolean w(String string) {
        return d.co.containsKey(string);
    }

    static String c(long l) {
        return LocalDateTime.of(2026, 1, 1, 0, 0).plusSeconds(l).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    static boolean x(String string) {
        return bS.contains(string);
    }

    static boolean y(String string) {
        return bT.contains(string);
    }

    static int z(String iterator) {
        if ((iterator = (List)d.cq.get(iterator)) == null || iterator.isEmpty()) {
            return 0;
        }
        long l = d.cb;
        long l2 = l - l % 86400L + 86400L;
        int n = 0;
        iterator = iterator.iterator();
        while (iterator.hasNext()) {
            long l3;
            com.huawei.tools.g.a.d d2 = (com.huawei.tools.g.a.d)iterator.next();
            if (!d2.aX || d2.aW <= l || (l3 = d2.aW - l2) <= 0L) continue;
            n += (int)((l3 + 1L) / 86400L);
        }
        return n;
    }

    static int A(String iterator) {
        if ((iterator = (List)d.cq.get(iterator)) == null || iterator.isEmpty()) {
            return 0;
        }
        long l = d.cb;
        long l2 = l - l % 86400L;
        int n = 0;
        iterator = iterator.iterator();
        while (iterator.hasNext()) {
            long l3;
            com.huawei.tools.g.a.d d2 = (com.huawei.tools.g.a.d)iterator.next();
            if (!d2.aX || d2.aW <= l || (l3 = d2.aW - l2) <= 0L) continue;
            n += (int)((l3 + 1L) / 86400L);
        }
        return n;
    }

    static boolean a(String iterator, long l) {
        if ((iterator = (List)d.cq.get(iterator)) == null || iterator.isEmpty()) {
            return false;
        }
        iterator = iterator.iterator();
        while (iterator.hasNext()) {
            com.huawei.tools.g.a.d d2 = (com.huawei.tools.g.a.d)iterator.next();
            if (!d2.aX || d2.startTime > l || l >= d2.aW) continue;
            return true;
        }
        return false;
    }
}
