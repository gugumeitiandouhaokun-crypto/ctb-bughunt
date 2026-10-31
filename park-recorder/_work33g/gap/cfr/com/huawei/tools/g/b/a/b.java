/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.huawei.tools.a.a
 *  com.huawei.tools.b.a
 *  com.huawei.tools.g.a.c
 *  com.huawei.tools.g.a.f
 *  com.huawei.tools.g.a.i
 *  com.huawei.tools.g.b.a.g
 *  com.huawei.tools.h.d
 *  org.apache.commons.lang3.StringUtils
 */
package com.huawei.tools.g.b.a;

import com.huawei.tools.b.a;
import com.huawei.tools.g.a.c;
import com.huawei.tools.g.a.f;
import com.huawei.tools.g.a.i;
import com.huawei.tools.g.b.a.g;
import com.huawei.tools.h.d;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

final class b {
    private static final Pattern bH = Pattern.compile("[a-zA-Z]");
    private static final Pattern bI = Pattern.compile("[0-9]");
    private static final Pattern bJ = Pattern.compile("[!@#$%^&*]");

    b() {
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
            if (bH.matcher((CharSequence)object).find()) {
                ++n;
            }
            if (bI.matcher((CharSequence)object).find()) {
                ++n;
            }
            if (bJ.matcher((CharSequence)object).find()) {
                ++n;
            }
            bl = n >= 2;
        }
        if (!bl) {
            throw new a("E001:Fail", com.huawei.tools.a.a.l.key);
        }
        for (c c3 : d.cd.values()) {
            if (!string.equals(c3.aL) || "cancelled".equals(c3.aN)) continue;
            throw new a("E001:Fail", com.huawei.tools.a.a.aa.key);
        }
        if (d.cd.values().stream().filter(c2 -> "active".equals(c2.aN)).count() >= 500L) {
            throw new a("E001:Fail", com.huawei.tools.a.a.o.key);
        }
        object = g.l();
        n = ((Random)ThreadLocalRandom.current()).nextInt(1000000);
        String string3 = String.format("%06d", n);
        string = new c((String)object, string, string2);
        v1.aO = string3;
        ((c)string).aP = d.cb + 600L;
        ((c)string).aQ = d.cb;
        d.cd.put(object, string);
        return "E000:" + (String)object + "," + string3;
    }

    static String c(String string, String string2) {
        if ((string = (c)d.cd.get(string)) == null) {
            return "E001:InvalidPara";
        }
        String string3 = ((c)string).aN;
        if ("active".equals(string3) || "cancelled".equals(string3)) {
            return "E001:InvalidPara";
        }
        if ("inactive".equals(string3)) {
            if (StringUtils.isBlank((CharSequence)string2) || !string2.equals(((c)string).aO)) {
                ((c)string).aN = "expired";
                ((c)string).aR = d.cb;
                return "E002:InvalidCode";
            }
            if (d.cd.values().stream().filter(c2 -> "active".equals(c2.aN)).count() >= 500L) {
                ((c)string).aN = "expired";
                ((c)string).aR = d.cb;
                return "E004:CapacityFull";
            }
            ((c)string).aN = "active";
            ((c)string).aO = null;
            ((c)string).aP = 0L;
            return "E000:Success";
        }
        if (StringUtils.isBlank((CharSequence)string2)) {
            return "E001:InvalidPara";
        }
        if (!string2.equals(((c)string).aO)) {
            return "E002:InvalidCode";
        }
        return "E003:ExpiredCode";
    }

    static String d(String string, String iterator) {
        if ((string = (c)d.cd.get(string)) == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if ("cancelled".equals(((c)string).aN)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if ("expired".equals(((c)string).aN)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.ac.key);
        }
        if (!"active".equals(((c)string).aN)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (!((String)((Object)iterator)).equals(((c)string).password)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.l.key);
        }
        iterator = ((c)string).aM.iterator();
        while (iterator.hasNext()) {
            if (!g.s((String)((String)iterator.next()))) continue;
            throw new a("E001:Fail", com.huawei.tools.a.a.W.key);
        }
        if (((c)string).aS != 0.0) {
            throw new a("E001:Fail", com.huawei.tools.a.a.z.key);
        }
        iterator = ((c)string).aM.iterator();
        while (iterator.hasNext()) {
            if (g.A((String)((String)iterator.next())) <= 0) continue;
            throw new a("E001:Fail", com.huawei.tools.a.a.A.key);
        }
        ((c)string).aN = "cancelled";
        for (String string2 : ((c)string).aM) {
            d.ce.remove(string2);
            d.cm.put(string2, d.cb);
        }
        ((c)string).aM.clear();
        return "E000:Success";
    }

    static String e(String string, String string2) {
        Object object;
        g.t((String)string2);
        c c2 = (c)d.cd.get(string);
        if (c2 == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(c2.aN)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (c2.aM.size() >= 3) {
            throw new a("E001:Fail", com.huawei.tools.a.a.r.key);
        }
        if (d.ce.containsKey(string2)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.t.key);
        }
        Long l = (Long)d.cm.get(string2);
        if (l != null && d.cb - l < 43200L) {
            throw new a("E001:Fail", com.huawei.tools.a.a.u.key);
        }
        l = null;
        for (f f2 : d.cg.values()) {
            if (!string2.equals(f2.aB) || f2.bf != 0L) continue;
            l = f2;
            break;
        }
        if (l != null) {
            if (d.ce.containsKey(string2)) {
                throw new a("E001:Fail", com.huawei.tools.a.a.w.key);
            }
            object = g.d((f)l);
            if ("entrying".equals(object) || "exiting".equals(object)) {
                throw new a("E001:Fail", com.huawei.tools.a.a.w.key);
            }
        }
        c2.aM.add(string2);
        d.ce.put(string2, string);
        object = (i)d.cf.get(string2);
        if (object != null && "active".equals(((i)object).aN)) {
            d.cf.remove(string2);
            if (l != null) {
                ((f)l).bh = string;
            }
        }
        return "E000:Success";
    }

    static String f(String string, String string2) {
        if ((string = (c)d.cd.get(string)) == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(((c)string).aN)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (!((c)string).aM.contains(string2)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.v.key);
        }
        if (g.s((String)string2)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.w.key);
        }
        if (g.A((String)string2) > 0) {
            throw new a("E001:Fail", com.huawei.tools.a.a.x.key);
        }
        ((c)string).aM.remove(string2);
        d.ce.remove(string2);
        d.cm.put(string2, d.cb);
        return "E000:Success";
    }

    static void e() {
        long l = d.cb;
        for (c c2 : d.cd.values()) {
            if ("inactive".equals(c2.aN) && c2.aP > 0L && l > c2.aP) {
                c2.aN = "expired";
                c2.aR = c2.aP;
            }
            if (!"expired".equals(c2.aN) || c2.aR <= 0L || l - c2.aR < 86400L) continue;
            c2.aN = "cancelled";
        }
    }

    static String c(String string) {
        c c2 = (c)d.cd.get(string);
        if (c2 != null) {
            if ("cancelled".equals(c2.aN)) {
                throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
            }
            String string2 = "member";
            String string3 = c2.aL;
            string = c2.aN;
            return "E000:" + string2 + "|" + string3 + "|" + string;
        }
        for (i i2 : d.cf.values()) {
            if (!string.equals(i2.bh)) continue;
            String string4 = string = i2.aN != null ? i2.aN : "active";
            if ("invalid".equals(string)) {
                throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
            }
            return "E000:temp|null|".concat(String.valueOf(string));
        }
        throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
    }

    static String d(String string) {
        if ((string = (c)d.cd.get(string)) == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if ("cancelled".equals(((c)string).aN)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        string = String.join((CharSequence)"|", ((c)string).aM);
        return "E000:".concat(String.valueOf(string));
    }

    static String h(String objectArray) {
        if ((objectArray = (c)d.cd.get(objectArray)) == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if ("cancelled".equals(objectArray.aN)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(objectArray.aN)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        String string = String.format("%.2f", objectArray.aS);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("E000:").append(string);
        objectArray = objectArray.aM.toArray();
        for (int j = 0; j < 3; ++j) {
            stringBuilder.append("|");
            if (j < objectArray.length) {
                String string2 = (String)objectArray[j];
                int n = g.A((String)string2);
                stringBuilder.append(string2).append(":").append(String.format("%03d", n));
                continue;
            }
            stringBuilder.append("Null:000");
        }
        return stringBuilder.toString();
    }
}
