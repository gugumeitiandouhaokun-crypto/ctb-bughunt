/*
 * Decompiled with CFR 0.152.
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
            if (bH.matcher((CharSequence)object2).find()) {
                ++n;
            }
            if (bI.matcher((CharSequence)object2).find()) {
                ++n;
            }
            if (bJ.matcher((CharSequence)object2).find()) {
                ++n;
            }
            bl = n >= 2;
        }
        if (!bl) {
            throw new a("E001:Fail", com.huawei.tools.a.a.l.key);
        }
        for (c c3 : d.cd.values()) {
            if (!((String)object).equals(c3.aL) || "cancelled".equals(c3.aN)) continue;
            throw new a("E001:Fail", com.huawei.tools.a.a.aa.key);
        }
        if (d.cd.values().stream().filter(c2 -> "active".equals(c2.aN)).count() >= 500L) {
            throw new a("E001:Fail", com.huawei.tools.a.a.o.key);
        }
        object2 = g.l();
        n = ((Random)ThreadLocalRandom.current()).nextInt(1000000);
        String string2 = String.format("%06d", n);
        object = new c((String)object2, (String)object, string);
        v1.aO = string2;
        ((c)object).aP = d.cb + 600L;
        ((c)object).aQ = d.cb;
        d.cd.put((String)object2, (c)object);
        return "E000:" + (String)object2 + "," + string2;
    }

    static String c(String object, String string) {
        if ((object = d.cd.get(object)) == null) {
            return "E001:InvalidPara";
        }
        String string2 = ((c)object).aN;
        if ("active".equals(string2) || "cancelled".equals(string2)) {
            return "E001:InvalidPara";
        }
        if ("inactive".equals(string2)) {
            if (StringUtils.isBlank(string) || !string.equals(((c)object).aO)) {
                ((c)object).aN = "expired";
                ((c)object).aR = d.cb;
                return "E002:InvalidCode";
            }
            if (d.cd.values().stream().filter(c2 -> "active".equals(c2.aN)).count() >= 500L) {
                ((c)object).aN = "expired";
                ((c)object).aR = d.cb;
                return "E004:CapacityFull";
            }
            ((c)object).aN = "active";
            ((c)object).aO = null;
            ((c)object).aP = 0L;
            return "E000:Success";
        }
        if (StringUtils.isBlank(string)) {
            return "E001:InvalidPara";
        }
        if (!string.equals(((c)object).aO)) {
            return "E002:InvalidCode";
        }
        return "E003:ExpiredCode";
    }

    static String d(String object, String iterator) {
        if ((object = d.cd.get(object)) == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if ("cancelled".equals(((c)object).aN)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if ("expired".equals(((c)object).aN)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.ac.key);
        }
        if (!"active".equals(((c)object).aN)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (!((String)((Object)iterator)).equals(((c)object).password)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.l.key);
        }
        iterator = ((c)object).aM.iterator();
        while (iterator.hasNext()) {
            if (!g.s((String)iterator.next())) continue;
            throw new a("E001:Fail", com.huawei.tools.a.a.W.key);
        }
        if (((c)object).aS != 0.0) {
            throw new a("E001:Fail", com.huawei.tools.a.a.z.key);
        }
        iterator = ((c)object).aM.iterator();
        while (iterator.hasNext()) {
            if (g.A(iterator.next()) <= 0) continue;
            throw new a("E001:Fail", com.huawei.tools.a.a.A.key);
        }
        ((c)object).aN = "cancelled";
        for (String string : ((c)object).aM) {
            d.ce.remove(string);
            d.cm.put(string, d.cb);
        }
        ((c)object).aM.clear();
        return "E000:Success";
    }

    static String e(String string, String string2) {
        Object object;
        g.t(string2);
        c c2 = d.cd.get(string);
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
        Object object2 = d.cm.get(string2);
        if (object2 != null && d.cb - (Long)object2 < 43200L) {
            throw new a("E001:Fail", com.huawei.tools.a.a.u.key);
        }
        object2 = null;
        for (f f2 : d.cg.values()) {
            if (!string2.equals(f2.aB) || f2.bf != 0L) continue;
            object2 = f2;
            break;
        }
        if (object2 != null) {
            if (d.ce.containsKey(string2)) {
                throw new a("E001:Fail", com.huawei.tools.a.a.w.key);
            }
            object = g.d((f)object2);
            if ("entrying".equals(object) || "exiting".equals(object)) {
                throw new a("E001:Fail", com.huawei.tools.a.a.w.key);
            }
        }
        c2.aM.add(string2);
        d.ce.put(string2, string);
        object = d.cf.get(string2);
        if (object != null && "active".equals(((i)object).aN)) {
            d.cf.remove(string2);
            if (object2 != null) {
                ((f)object2).bh = string;
            }
        }
        return "E000:Success";
    }

    static String f(String object, String string) {
        if ((object = d.cd.get(object)) == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if (!"active".equals(((c)object).aN)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.q.key);
        }
        if (!((c)object).aM.contains(string)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.v.key);
        }
        if (g.s(string)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.w.key);
        }
        if (g.A(string) > 0) {
            throw new a("E001:Fail", com.huawei.tools.a.a.x.key);
        }
        ((c)object).aM.remove(string);
        d.ce.remove(string);
        d.cm.put(string, d.cb);
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
        c c2 = d.cd.get(string);
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

    static String d(String object) {
        if ((object = d.cd.get(object)) == null) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        if ("cancelled".equals(((c)object).aN)) {
            throw new a("E001:Fail", com.huawei.tools.a.a.G.key);
        }
        object = String.join((CharSequence)"|", ((c)object).aM);
        return "E000:".concat(String.valueOf(object));
    }

    static String h(String objectArray) {
        if ((objectArray = d.cd.get(objectArray)) == null) {
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
                int n = g.A(string2);
                stringBuilder.append(string2).append(":").append(String.format("%03d", n));
                continue;
            }
            stringBuilder.append("Null:000");
        }
        return stringBuilder.toString();
    }
}

