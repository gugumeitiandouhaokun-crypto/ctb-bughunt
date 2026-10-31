/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.huawei.tools.a.a
 *  com.huawei.tools.b.a
 *  com.huawei.tools.g.a.c
 *  com.huawei.tools.g.a.f
 *  com.huawei.tools.g.a.g
 *  com.huawei.tools.g.a.h
 *  com.huawei.tools.g.b.a.g
 *  com.huawei.tools.h.d
 *  org.apache.commons.lang3.StringUtils
 */
package com.huawei.tools.g.b.a;

import com.huawei.tools.b.a;
import com.huawei.tools.g.a.c;
import com.huawei.tools.g.a.f;
import com.huawei.tools.g.b.a.g;
import com.huawei.tools.h.d;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.StringUtils;

final class h {
    Runnable cR;

    h() {
    }

    final void q() {
        if (this.cR != null) {
            this.cR.run();
        }
    }

    final String m(String string2, String string3) {
        String string4;
        int n;
        char c2;
        Object object;
        if (StringUtils.isBlank((CharSequence)string2) || !((object = string2) == null || object.length() < 6 || object.length() > 7 ? false : ((c2 = object.charAt(0)) != 'L' && c2 != 'M' && c2 != 'S' ? false : object.substring(1).matches("\\d+")))) {
            throw new a("E001:Fail", com.huawei.tools.a.a.I.key);
        }
        object = string2;
        if (!(object != null && object.length() == 7)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.ak.key);
        }
        if (g.J((String)string2)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.ad.key);
        }
        String string5 = (String)d.dg.get(string2);
        if (string5 == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.am.key);
        }
        c c3 = (c)d.df.get(string5);
        if (c3 == null || !"active".equals(c3.bt)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.al.key);
        }
        if (g.F((String)string2)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.ax.key);
        }
        c3 = (com.huawei.tools.g.a.g)d.dk.get(string3);
        if (c3 == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.an.key);
        }
        if (!"F".equals(c3.bt)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.ao.key);
        }
        String string6 = c3.cl;
        object = g.I((String)string2);
        if (!("L".equals(string6) ? true : ("M".equals(string6) ? "M".equals(object) || "S".equals(object) : ("S".equals(string6) ? "S".equals(object) : false)))) {
            throw new a("E001:Fail", com.huawei.tools.a.a.ap.key);
        }
        if (h.T(string2)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.aq.key);
        }
        object = string2;
        long l = d.de / 86400L;
        int n2 = 0;
        if ((object = (List)d.dw.get(object)) == null) {
            n = 0;
        } else {
            for (String string7 : object) {
                if ((string7 = (com.huawei.tools.g.a.h)d.dv.get(string7)) == null || ((com.huawei.tools.g.a.h)string7).bw / 86400L != l) continue;
                ++n2;
            }
            n = n2;
        }
        if (n >= 3) {
            throw new a("E001:Fail", com.huawei.tools.a.a.ar.key);
        }
        long l2 = d.de;
        while (d.dv.containsKey(string4 = "r" + String.format("%08d", ThreadLocalRandom.current().nextInt(100000000)))) {
        }
        object = new com.huawei.tools.g.a.h();
        new com.huawei.tools.g.a.h().ck = string4;
        object.bh = string2;
        object.bN = string3;
        object.cl = c3.cl;
        object.bq = string5;
        object.bt = "locked";
        object.bw = l2;
        object.bC = l2 + 1800L;
        c3.bt = "R";
        g.r((String)c3.bj, (String)c3.cl);
        d.dv.put(string4, object);
        d.dw.computeIfAbsent(string2, string -> new ArrayList()).add(string4);
        this.q();
        return "E000:".concat(String.valueOf(string4));
    }

    static String j(String string) {
        Object object3;
        if (StringUtils.isBlank((CharSequence)string)) {
            return "E001:Fail";
        }
        Object object2 = (c)d.df.get(string);
        if (object2 == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if ("cancelled".equals(((c)object2).bt)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(((c)object2).bt)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        object2 = new ArrayList();
        for (Object object3 : d.dv.values()) {
            if (!string.equals(((com.huawei.tools.g.a.h)object3).bq)) continue;
            object2.add(object3);
        }
        if (object2.isEmpty()) {
            return "E001:Fail";
        }
        object2.sort((h2, h3) -> Long.compare(h2.bw, h3.bw));
        StringBuilder stringBuilder = new StringBuilder();
        object3 = object2.iterator();
        while (object3.hasNext()) {
            string = (com.huawei.tools.g.a.h)object3.next();
            if (stringBuilder.length() > 0) {
                stringBuilder.append(";");
            }
            long l = ((com.huawei.tools.g.a.h)string).bw;
            stringBuilder.append(((com.huawei.tools.g.a.h)string).ck).append("|").append(((com.huawei.tools.g.a.h)string).bh).append("|").append(((com.huawei.tools.g.a.h)string).bN).append("|").append(LocalDateTime.of(2026, 1, 1, 0, 0).plusSeconds(l).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        return "E000:" + stringBuilder.toString();
    }

    final void r() {
        long l = d.de;
        ArrayList<com.huawei.tools.g.a.h> arrayList = new ArrayList<com.huawei.tools.g.a.h>();
        for (com.huawei.tools.g.a.h h2 : d.dv.values()) {
            if (!"locked".equals(h2.bt) || h.U(h2.bh) || l <= h2.bC) continue;
            arrayList.add(h2);
        }
        for (com.huawei.tools.g.a.h h2 : arrayList) {
            h.b(h2);
            h2.bt = "expired";
        }
        if (!arrayList.isEmpty()) {
            this.q();
        }
    }

    final void s() {
        long l = d.de;
        boolean bl = false;
        for (com.huawei.tools.g.a.h h2 : d.dv.values()) {
            long l2 = h2.bw / 86400L;
            if (l / 86400L <= l2) continue;
            if ("locked".equals(h2.bt)) {
                if (h.U(h2.bh)) continue;
                h.b(h2);
                h2.bt = "closed";
                bl = true;
                continue;
            }
            if (!"expired".equals(h2.bt) && !"canceled".equals(h2.bt)) continue;
            h2.bt = "closed";
        }
        if (bl) {
            this.q();
        }
    }

    static com.huawei.tools.g.a.h R(String string) {
        for (com.huawei.tools.g.a.h h2 : d.dv.values()) {
            if (!string.equals(h2.bh) || !"locked".equals(h2.bt)) continue;
            return h2;
        }
        return null;
    }

    static void a(com.huawei.tools.g.a.h h2) {
        if (h2 == null) {
            return;
        }
        if (!"used".equals(h2.bt)) {
            return;
        }
        h2.bt = "closed";
    }

    static String S(String string) {
        if ((string = (com.huawei.tools.g.a.h)d.dv.get(string)) != null) {
            return ((com.huawei.tools.g.a.h)string).bN;
        }
        return null;
    }

    static boolean T(String string) {
        for (com.huawei.tools.g.a.h h2 : d.dv.values()) {
            if (!string.equals(h2.bh) || !"locked".equals(h2.bt) && !"used".equals(h2.bt)) continue;
            return true;
        }
        return false;
    }

    static boolean U(String string) {
        for (f f2 : d.di.values()) {
            if (!string.equals(f2.bh) || !g.f((f)f2)) continue;
            return true;
        }
        return false;
    }

    static void b(com.huawei.tools.g.a.h h2) {
        h2 = (com.huawei.tools.g.a.g)d.dk.get(h2.bN);
        if (h2 != null && "R".equals(h2.bt)) {
            h2.bt = "F";
            h2.bh = null;
            g.s((String)h2.bj, (String)h2.cl);
        }
    }
}
