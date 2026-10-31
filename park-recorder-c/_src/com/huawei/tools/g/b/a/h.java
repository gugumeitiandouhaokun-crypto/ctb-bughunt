/*
 * Decompiled with CFR 0.152.
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
import java.util.Iterator;
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
        if (StringUtils.isBlank(string2) || !((object = string2) == null || ((String)object).length() < 6 || ((String)object).length() > 7 ? false : ((c2 = ((String)object).charAt(0)) != 'L' && c2 != 'M' && c2 != 'S' ? false : ((String)object).substring(1).matches("\\d+")))) {
            throw new a("E001:Fail", com.huawei.tools.a.a.I.key);
        }
        object = string2;
        if (!(object != null && ((String)object).length() == 7)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.ak.key);
        }
        if (g.J(string2)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.ad.key);
        }
        String string5 = d.dg.get(string2);
        if (string5 == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.am.key);
        }
        Object object2 = d.df.get(string5);
        if (object2 == null || !"active".equals(((c)object2).bt)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.al.key);
        }
        if (g.F(string2)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.ax.key);
        }
        object2 = d.dk.get(string3);
        if (object2 == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.an.key);
        }
        if (!"F".equals(((com.huawei.tools.g.a.g)object2).bt)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.ao.key);
        }
        String string6 = ((com.huawei.tools.g.a.g)object2).cl;
        object = g.I(string2);
        if (!("L".equals(string6) ? true : ("M".equals(string6) ? "M".equals(object) || "S".equals(object) : ("S".equals(string6) ? "S".equals(object) : false)))) {
            throw new a("E001:Fail", com.huawei.tools.a.a.ap.key);
        }
        if (h.T(string2)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.aq.key);
        }
        object = string2;
        long l = d.de / 86400L;
        int n2 = 0;
        if ((object = d.dw.get(object)) == null) {
            n = 0;
        } else {
            object = object.iterator();
            while (object.hasNext()) {
                Object object3 = (String)object.next();
                if ((object3 = d.dv.get(object3)) == null || ((com.huawei.tools.g.a.h)object3).bw / 86400L != l) continue;
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
        ((com.huawei.tools.g.a.h)object).bh = string2;
        ((com.huawei.tools.g.a.h)object).bN = string3;
        ((com.huawei.tools.g.a.h)object).cl = ((com.huawei.tools.g.a.g)object2).cl;
        ((com.huawei.tools.g.a.h)object).bq = string5;
        ((com.huawei.tools.g.a.h)object).bt = "locked";
        ((com.huawei.tools.g.a.h)object).bw = l2;
        ((com.huawei.tools.g.a.h)object).bC = l2 + 1800L;
        ((com.huawei.tools.g.a.g)object2).bt = "R";
        g.r(((com.huawei.tools.g.a.g)object2).bj, ((com.huawei.tools.g.a.g)object2).cl);
        d.dv.put(string4, (com.huawei.tools.g.a.h)object);
        d.dw.computeIfAbsent(string2, string -> new ArrayList()).add(string4);
        this.q();
        return "E000:".concat(String.valueOf(string4));
    }

    static String j(String object) {
        if (StringUtils.isBlank((CharSequence)object)) {
            return "E001:Fail";
        }
        Object object3 = d.df.get(object);
        if (object3 == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if ("cancelled".equals(((c)object3).bt)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(((c)object3).bt)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        object3 = new ArrayList();
        for (com.huawei.tools.g.a.h object22 : d.dv.values()) {
            if (!((String)object).equals(object22.bq)) continue;
            object3.add(object22);
        }
        if (object3.isEmpty()) {
            return "E001:Fail";
        }
        object3.sort((h2, h3) -> Long.compare(h2.bw, h3.bw));
        StringBuilder stringBuilder = new StringBuilder();
        Iterator iterator = object3.iterator();
        while (iterator.hasNext()) {
            object = (com.huawei.tools.g.a.h)iterator.next();
            if (stringBuilder.length() > 0) {
                stringBuilder.append(";");
            }
            long l = ((com.huawei.tools.g.a.h)object).bw;
            stringBuilder.append(((com.huawei.tools.g.a.h)object).ck).append("|").append(((com.huawei.tools.g.a.h)object).bh).append("|").append(((com.huawei.tools.g.a.h)object).bN).append("|").append(LocalDateTime.of(2026, 1, 1, 0, 0).plusSeconds(l).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
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

    static String S(String object) {
        if ((object = d.dv.get(object)) != null) {
            return ((com.huawei.tools.g.a.h)object).bN;
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
            if (!string.equals(f2.bh) || !g.f(f2)) continue;
            return true;
        }
        return false;
    }

    static void b(com.huawei.tools.g.a.h object) {
        object = d.dk.get(((com.huawei.tools.g.a.h)object).bN);
        if (object != null && "R".equals(((com.huawei.tools.g.a.g)object).bt)) {
            ((com.huawei.tools.g.a.g)object).bt = "F";
            ((com.huawei.tools.g.a.g)object).bh = null;
            g.s(((com.huawei.tools.g.a.g)object).bj, ((com.huawei.tools.g.a.g)object).cl);
        }
    }
}

