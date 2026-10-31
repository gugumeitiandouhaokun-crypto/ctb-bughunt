/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.huawei.tools.a.a
 *  com.huawei.tools.b.a
 *  com.huawei.tools.g.a.c
 *  com.huawei.tools.g.a.f
 *  com.huawei.tools.g.a.j
 *  com.huawei.tools.g.b.a.g
 *  com.huawei.tools.g.b.a.h
 *  com.huawei.tools.h.d
 *  org.apache.commons.lang3.StringUtils
 */
package com.huawei.tools.g.b.a;

import com.huawei.tools.b.a;
import com.huawei.tools.g.a.c;
import com.huawei.tools.g.a.f;
import com.huawei.tools.g.a.j;
import com.huawei.tools.g.b.a.g;
import com.huawei.tools.g.b.a.h;
import com.huawei.tools.h.d;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

final class b {
    private final h cB;
    private static final Pattern cC = Pattern.compile("[a-zA-Z]");
    private static final Pattern cD = Pattern.compile("[0-9]");
    private static final Pattern cE = Pattern.compile("[!@#$%^&*]");

    b(h h2) {
        this.cB = h2;
    }

    static String b(String string, String string2) {
        int n;
        boolean bl;
        if (StringUtils.isBlank((CharSequence)string) || !string.startsWith("1") || !string.matches("\\d{11}")) {
            throw new a("E001:Fail", com.huawei.tools.a.a.k.key);
        }
        Object object = string2;
        if (StringUtils.isBlank((CharSequence)object) || ((String)object).length() < 6 || ((String)object).length() > 8) {
            bl = false;
        } else {
            n = 0;
            if (cC.matcher((CharSequence)object).find()) {
                ++n;
            }
            if (cD.matcher((CharSequence)object).find()) {
                ++n;
            }
            if (cE.matcher((CharSequence)object).find()) {
                ++n;
            }
            bl = n >= 2;
        }
        if (!bl) {
            throw new a("E001:Fail", com.huawei.tools.a.a.l.key);
        }
        for (c c3 : d.df.values()) {
            if (!string.equals(c3.br) || "cancelled".equals(c3.bt)) continue;
            throw new a("E001:Fail", com.huawei.tools.a.a.aa.key);
        }
        if (d.df.values().stream().filter(c2 -> "active".equals(c2.bt)).count() >= 500L) {
            throw new a("E001:Fail", com.huawei.tools.a.a.o.key);
        }
        object = g.o();
        n = ((Random)ThreadLocalRandom.current()).nextInt(1000000);
        String string3 = String.format("%06d", n);
        string = new c((String)object, string, string2);
        v1.bu = string3;
        ((c)string).bv = d.de + 600L;
        ((c)string).bw = d.de;
        d.df.put(object, string);
        return "E000:" + (String)object + "," + string3;
    }

    static String c(String string, String string2) {
        if ((string = (c)d.df.get(string)) == null) {
            return "E001:InvalidPara";
        }
        String string3 = ((c)string).bt;
        if ("active".equals(string3) || "cancelled".equals(string3)) {
            return "E001:InvalidPara";
        }
        if ("inactive".equals(string3)) {
            if (StringUtils.isBlank((CharSequence)string2) || !string2.equals(((c)string).bu)) {
                ((c)string).bt = "expired";
                ((c)string).bx = d.de;
                return "E002:InvalidCode";
            }
            if (d.df.values().stream().filter(c2 -> "active".equals(c2.bt)).count() >= 500L) {
                ((c)string).bt = "expired";
                ((c)string).bx = d.de;
                return "E004:CapacityFull";
            }
            ((c)string).bt = "active";
            ((c)string).bu = null;
            ((c)string).bv = 0L;
            return "E000:Success";
        }
        if (StringUtils.isBlank((CharSequence)string2)) {
            return "E001:InvalidPara";
        }
        if (!string2.equals(((c)string).bu)) {
            return "E002:InvalidCode";
        }
        return "E003:ExpiredCode";
    }

    static String e(String string, String string2) {
        Object object;
        g.G((String)string2);
        c c2 = (c)d.df.get(string);
        if (c2 == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(c2.bt)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (c2.bs.size() >= 3) {
            throw new a("E001:Fail", com.huawei.tools.a.a.r.key);
        }
        if (d.dg.containsKey(string2)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.t.key);
        }
        Long l = (Long)d.do.get(string2);
        if (l != null && d.de - l < 43200L) {
            throw new a("E001:Fail", com.huawei.tools.a.a.u.key);
        }
        l = null;
        for (f f2 : d.di.values()) {
            if (!string2.equals(f2.bh) || f2.bM != 0L) continue;
            l = f2;
            break;
        }
        if (l != null) {
            if (d.dg.containsKey(string2)) {
                throw new a("E001:Fail", com.huawei.tools.a.a.w.key);
            }
            object = g.e((f)l);
            if ("entrying".equals(object) || "exiting".equals(object)) {
                throw new a("E001:Fail", com.huawei.tools.a.a.w.key);
            }
        }
        c2.bs.add(string2);
        d.dg.put(string2, string);
        object = (j)d.dh.get(string2);
        if (object != null && "active".equals(((j)object).bt)) {
            d.dh.remove(string2);
            if (l != null) {
                ((f)l).bO = string;
            }
        }
        return "E000:Success";
    }

    static void f() {
        long l = d.de;
        for (c c2 : d.df.values()) {
            if ("inactive".equals(c2.bt) && c2.bv > 0L && l > c2.bv) {
                c2.bt = "expired";
                c2.bx = c2.bv;
            }
            if (!"expired".equals(c2.bt) || c2.bx <= 0L || l - c2.bx < 86400L) continue;
            c2.bt = "cancelled";
        }
    }

    static String c(String string) {
        c c2 = (c)d.df.get(string);
        if (c2 != null) {
            if ("cancelled".equals(c2.bt)) {
                throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
            }
            String string2 = "member";
            String string3 = c2.br;
            string = c2.bt;
            return "E000:" + string2 + "|" + string3 + "|" + string;
        }
        for (j j2 : d.dh.values()) {
            if (!string.equals(j2.bO)) continue;
            String string4 = string = j2.bt != null ? j2.bt : "active";
            if ("invalid".equals(string)) {
                throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
            }
            return "E000:temp|null|".concat(String.valueOf(string));
        }
        throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
    }

    static String d(String string) {
        if ((string = (c)d.df.get(string)) == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if ("cancelled".equals(((c)string).bt)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        string = String.join((CharSequence)"|", ((c)string).bs);
        return "E000:".concat(String.valueOf(string));
    }

    static String h(String objectArray) {
        if ((objectArray = (c)d.df.get(objectArray)) == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if ("cancelled".equals(objectArray.bt)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(objectArray.bt)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        String string = String.format("%.2f", objectArray.by);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("E000:").append(string);
        objectArray = objectArray.bs.toArray();
        for (int i = 0; i < 3; ++i) {
            stringBuilder.append("|");
            if (i < objectArray.length) {
                String string2 = (String)objectArray[i];
                int n = g.P((String)string2);
                stringBuilder.append(string2).append(":").append(String.format("%03d", n));
                continue;
            }
            stringBuilder.append("Null:000");
        }
        return stringBuilder.toString();
    }
}
