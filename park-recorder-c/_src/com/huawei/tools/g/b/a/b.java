/*
 * Decompiled with CFR 0.152.
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

    static String b(String object, String string) {
        int n;
        boolean bl;
        if (StringUtils.isBlank((CharSequence)object) || !((String)object).startsWith("1") || !((String)object).matches("\\d{11}")) {
            throw new a("E001:Fail", com.huawei.tools.a.a.k.key);
        }
        Object object2 = string;
        if (StringUtils.isBlank((CharSequence)object2) || ((String)object2).length() < 6 || ((String)object2).length() > 8) {
            bl = false;
        } else {
            n = 0;
            if (cC.matcher((CharSequence)object2).find()) {
                ++n;
            }
            if (cD.matcher((CharSequence)object2).find()) {
                ++n;
            }
            if (cE.matcher((CharSequence)object2).find()) {
                ++n;
            }
            bl = n >= 2;
        }
        if (!bl) {
            throw new a("E001:Fail", com.huawei.tools.a.a.l.key);
        }
        for (c c3 : d.df.values()) {
            if (!((String)object).equals(c3.br) || "cancelled".equals(c3.bt)) continue;
            throw new a("E001:Fail", com.huawei.tools.a.a.aa.key);
        }
        if (d.df.values().stream().filter(c2 -> "active".equals(c2.bt)).count() >= 500L) {
            throw new a("E001:Fail", com.huawei.tools.a.a.o.key);
        }
        object2 = g.o();
        n = ((Random)ThreadLocalRandom.current()).nextInt(1000000);
        String string2 = String.format("%06d", n);
        object = new c((String)object2, (String)object, string);
        v1.bu = string2;
        ((c)object).bv = d.de + 600L;
        ((c)object).bw = d.de;
        d.df.put((String)object2, (c)object);
        return "E000:" + (String)object2 + "," + string2;
    }

    static String c(String object, String string) {
        if ((object = d.df.get(object)) == null) {
            return "E001:InvalidPara";
        }
        String string2 = ((c)object).bt;
        if ("active".equals(string2) || "cancelled".equals(string2)) {
            return "E001:InvalidPara";
        }
        if ("inactive".equals(string2)) {
            if (StringUtils.isBlank(string) || !string.equals(((c)object).bu)) {
                ((c)object).bt = "expired";
                ((c)object).bx = d.de;
                return "E002:InvalidCode";
            }
            if (d.df.values().stream().filter(c2 -> "active".equals(c2.bt)).count() >= 500L) {
                ((c)object).bt = "expired";
                ((c)object).bx = d.de;
                return "E004:CapacityFull";
            }
            ((c)object).bt = "active";
            ((c)object).bu = null;
            ((c)object).bv = 0L;
            return "E000:Success";
        }
        if (StringUtils.isBlank(string)) {
            return "E001:InvalidPara";
        }
        if (!string.equals(((c)object).bu)) {
            return "E002:InvalidCode";
        }
        return "E003:ExpiredCode";
    }

    static String e(String string, String string2) {
        Object object;
        g.G(string2);
        c c2 = d.df.get(string);
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
        Object object2 = d.do.get(string2);
        if (object2 != null && d.de - (Long)object2 < 43200L) {
            throw new a("E001:Fail", com.huawei.tools.a.a.u.key);
        }
        object2 = null;
        for (f f2 : d.di.values()) {
            if (!string2.equals(f2.bh) || f2.bM != 0L) continue;
            object2 = f2;
            break;
        }
        if (object2 != null) {
            if (d.dg.containsKey(string2)) {
                throw new a("E001:Fail", com.huawei.tools.a.a.w.key);
            }
            object = g.e((f)object2);
            if ("entrying".equals(object) || "exiting".equals(object)) {
                throw new a("E001:Fail", com.huawei.tools.a.a.w.key);
            }
        }
        c2.bs.add(string2);
        d.dg.put(string2, string);
        object = d.dh.get(string2);
        if (object != null && "active".equals(((j)object).bt)) {
            d.dh.remove(string2);
            if (object2 != null) {
                ((f)object2).bO = string;
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
        c c2 = d.df.get(string);
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

    static String d(String object) {
        if ((object = d.df.get(object)) == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if ("cancelled".equals(((c)object).bt)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        object = String.join((CharSequence)"|", ((c)object).bs);
        return "E000:".concat(String.valueOf(object));
    }

    static String h(String objectArray) {
        if ((objectArray = d.df.get(objectArray)) == null) {
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
        for (int i2 = 0; i2 < 3; ++i2) {
            stringBuilder.append("|");
            if (i2 < objectArray.length) {
                String string2 = (String)objectArray[i2];
                int n = g.P(string2);
                stringBuilder.append(string2).append(":").append(String.format("%03d", n));
                continue;
            }
            stringBuilder.append("Null:000");
        }
        return stringBuilder.toString();
    }
}

