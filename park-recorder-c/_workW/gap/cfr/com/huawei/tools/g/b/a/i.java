/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.huawei.tools.g.a.f
 *  com.huawei.tools.g.a.g
 *  com.huawei.tools.g.a.k
 *  com.huawei.tools.g.b.a.g
 *  com.huawei.tools.h.d
 *  org.apache.commons.lang3.StringUtils
 */
package com.huawei.tools.g.b.a;

import com.huawei.tools.g.a.f;
import com.huawei.tools.g.a.k;
import com.huawei.tools.g.b.a.g;
import com.huawei.tools.h.d;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;

final class i {
    static final Map<String, double[]> cS = new LinkedHashMap<String, double[]>();
    static final String[] cT;
    static final String[] cU;
    static final String[] cV;
    static final List<String> cW;

    i() {
    }

    static double s(String string) {
        double d2 = 0.0;
        for (k k2 : d.dx.values()) {
            if (!string.equals(k2.bK) || k2.cA) continue;
            if ("expired".equals(k2.bt)) {
                d2 += k2.cx + k2.cy;
                continue;
            }
            d2 += k2.cw + k2.cx + k2.cy;
        }
        return d2;
    }

    static void v(String string) {
        for (k k2 : d.dx.values()) {
            if (!string.equals(k2.bK) || k2.cA) continue;
            k2.cA = true;
        }
    }

    static void w(String string) {
        for (k k2 : d.dx.values()) {
            if (!string.equals(k2.bK) || !k2.cA) continue;
            k2.cA = false;
        }
    }

    static long[][] t(String string) {
        ArrayList<long[]> arrayList = new ArrayList<long[]>();
        for (k k2 : d.dx.values()) {
            if (!string.equals(k2.bK) || k2.ct <= 0L || k2.cu <= 0L || k2.cu <= k2.ct) continue;
            arrayList.add(new long[]{k2.ct, k2.cu});
        }
        arrayList.sort((lArray, lArray2) -> Long.compare(lArray[0], lArray2[0]));
        return (long[][])arrayList.toArray((T[])new long[0][]);
    }

    static long[][] u(String string) {
        ArrayList<long[]> arrayList = new ArrayList<long[]>();
        for (k k2 : d.dx.values()) {
            if (!string.equals(k2.bK) || k2.ct <= 0L || k2.cu <= 0L) continue;
            long l = k2.ct - 3600L;
            long l2 = k2.cu + 1800L;
            arrayList.add(new long[]{l, l2});
        }
        arrayList.sort((lArray, lArray2) -> Long.compare(lArray[0], lArray2[0]));
        return (long[][])arrayList.toArray((T[])new long[0][]);
    }

    static void t() {
        int n = (int)(d.de / 86400L);
        if (n != d.dB) {
            d.dB = n;
            d.dA.set(0);
            d.dy.set(0);
            d.dz.set(0);
        }
    }

    static String b(String string, long l) {
        return (int)(l / 86400L) + "-" + string;
    }

    static k W(String string) {
        return (k)d.dx.get(i.b(string, d.de));
    }

    static boolean X(String string) {
        return g.L((String)string) || g.M((String)string);
    }

    static String a(String[] stringArray, int n) {
        for (int j = 0; j < stringArray.length; ++j) {
            int n2 = (n + j) % stringArray.length;
            String string = stringArray[n2];
            if (!i.Y(string)) continue;
            return string;
        }
        return null;
    }

    static void a(String[] stringArray, String string, AtomicInteger atomicInteger) {
        for (int j = 0; j < stringArray.length; ++j) {
            if (!stringArray[j].equals(string)) continue;
            atomicInteger.set((j + 1) % stringArray.length);
            return;
        }
    }

    private static boolean Y(String string) {
        if (i.aa(string).size() >= 6) {
            return false;
        }
        return i.Z(string) < 20;
    }

    private static int Z(String string) {
        int n = (int)(d.de / 86400L);
        int n2 = 0;
        for (Object object : d.dx.values()) {
            if (!string.equals(object.bN) || (int)(object.bw / 86400L) != n || !"queuing".equals(object = object.bt) && !"washing".equals(object) && !"closed".equals(object)) continue;
            ++n2;
        }
        return n2;
    }

    static List<k> aa(String string) {
        ArrayList<k> arrayList = new ArrayList<k>();
        for (k k4 : d.dx.values()) {
            if (!"queuing".equals(k4.bt) || !string.equals(k4.bN)) continue;
            arrayList.add(k4);
        }
        arrayList.sort((k2, k3) -> {
            if (k2.cs != k3.cs) {
                return Long.compare(k2.cs, k3.cs);
            }
            return k2.cr.compareTo(k3.cr);
        });
        return arrayList;
    }

    static k ab(String string) {
        for (k k2 : d.dx.values()) {
            if (!"washing".equals(k2.bt) || !string.equals(k2.bN)) continue;
            return k2;
        }
        return null;
    }

    static void c(String string, long l) {
        while (i.ab(string) == null) {
            k k2 = i.aa(string);
            if (k2.isEmpty()) {
                return;
            }
            k2 = k2.get(0);
            if (k2.cv == 0L) {
                return;
            }
            if (l - k2.cv <= 300L) {
                return;
            }
            long l2 = k2.cv + 300L;
            k2.bt = "expired";
            k2.cy = 10.0;
            i.a(k2, d.de);
            i.d(string, l2);
        }
        return;
    }

    static void d(String object, long l) {
        if (l % 86400L >= 63000L) {
            return;
        }
        if (i.ab((String)object) != null) {
            return;
        }
        if ((object = i.aa((String)object)).isEmpty()) {
            return;
        }
        object = (k)object.get(0);
        if (((k)object).cv == 0L) {
            ((k)object).cv = l;
        }
    }

    static void d(long l) {
        for (k k2 : d.dx.values()) {
            long l2;
            if (!"queuing".equals(k2.bt) || l < (l2 = k2.bw - k2.bw % 86400L) + 63000L) continue;
            k2.bt = "expired";
            k2.cy = 0.0;
            k2.cz = false;
            i.a(k2, d.de);
        }
    }

    static void e(long l) {
        for (k k2 : d.dx.values()) {
            long l2;
            if ("closed".equals(k2.bt) || l < (l2 = k2.bw - k2.bw % 86400L) + 64800L) continue;
            boolean bl = "queuing".equals(k2.bt) || "washing".equals(k2.bt);
            k2.bt = "closed";
            long l3 = l2 + 64800L;
            if (k2.cu == 0L) {
                k2.cu = l3;
            }
            String string = k2.bN;
            if ((string = (com.huawei.tools.g.a.g)d.dl.get(string)) != null && "U".equals(((com.huawei.tools.g.a.g)string).bt)) {
                ((com.huawei.tools.g.a.g)string).bt = "F";
                ((com.huawei.tools.g.a.g)string).bh = null;
            }
            if (!bl) continue;
            i.a(k2, l3);
        }
    }

    static f ac(String string) {
        for (f f2 : d.di.values()) {
            if (!string.equals(f2.bh) || f2.bM != 0L) continue;
            return f2;
        }
        return null;
    }

    static int ad(String string) {
        int n = (int)(d.de / 86400L);
        int n2 = 0;
        for (k k2 : d.dx.values()) {
            if (!string.equals(k2.bh) || (int)(k2.bw / 86400L) != n || !k2.cz) continue;
            ++n2;
        }
        return n2;
    }

    static void g(f f2) {
        if (f2 == null) {
            return;
        }
        f2.bP = 0L;
        if (f2.cj > 0L) {
            long l = d.de - f2.cj;
            f2.ci += l;
            f2.cj = 0L;
        }
    }

    static void a(k k2, long l) {
        if (k2 == null) {
            return;
        }
        String string = k2.bK;
        if (StringUtils.isBlank((CharSequence)string)) {
            return;
        }
        if ((string = (f)d.di.get(string)) == null || ((f)string).bM > 0L) {
            return;
        }
        if (g.K((String)k2.bh)) {
            return;
        }
        if (((f)string).bN == null) {
            ((f)string).bP = l;
        }
        if (((f)string).cj == 0L && ((f)string).cg == 0L) {
            ((f)string).cj = l;
        }
    }

    private static String[] ae(String string) {
        int n = -1;
        switch (string.hashCode()) {
            case 2780: {
                if (!string.equals("WS")) break;
                n = 0;
                break;
            }
            case 2774: {
                if (!string.equals("WM")) break;
                n = 1;
                break;
            }
            case 2773: {
                if (!string.equals("WL")) break;
                n = 2;
            }
        }
        switch (n) {
            case 0: {
                return cT;
            }
            case 1: {
                return cU;
            }
            case 2: {
                return cV;
            }
        }
        return new String[0];
    }

    static String af(String string) {
        Object object = i.ab(string);
        object = object != null ? object.cr : "null";
        List<k> list = i.aa(string);
        Object[] objectArray = new String[6];
        Arrays.fill(objectArray, "null");
        for (int j = 0; j < list.size() && j < 6; ++j) {
            objectArray[j] = list.get((int)j).cr;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(string).append("|").append((String)object).append("|[");
        for (int j = 0; j < 6; ++j) {
            if (j > 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append((String)objectArray[j]);
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    final void ag(String stringArray) {
        String string3;
        if ((stringArray = i.ae((String)stringArray)).length <= 1) {
            return;
        }
        HashMap<String, Long> hashMap = new HashMap<String, Long>();
        String[] stringArray2 = stringArray;
        int n = stringArray.length;
        for (int j = 0; j < n; ++j) {
            long l;
            string3 = stringArray2[j];
            List<k> list = i.aa(string3);
            if (list.isEmpty()) {
                l = 0L;
            } else {
                List<k> list2 = list;
                l = list2.get((int)(list2.size() - 1)).cs;
            }
            hashMap.put(string3, l);
        }
        boolean bl = true;
        while (bl) {
            boolean bl2;
            int n2;
            bl = false;
            String[] stringArray3 = new ArrayList<String>(Arrays.asList(stringArray));
            stringArray3.sort((string, string2) -> {
                int n;
                int n2 = i.aa(string).size();
                if (n2 != (n = i.aa(string2).size())) {
                    return n - n2;
                }
                return string.compareTo((String)string2);
            });
            Object object = null;
            string3 = null;
            int n3 = -1;
            int n4 = Integer.MAX_VALUE;
            for (String string4 : stringArray3) {
                n2 = i.aa(string4).size();
                if (n2 > n3) {
                    n3 = n2;
                    object = string4;
                    continue;
                }
                if (n2 != n3 || string4.compareTo((String)object) <= 0) continue;
                object = string4;
            }
            stringArray3 = stringArray;
            int n5 = stringArray.length;
            for (n2 = 0; n2 < n5; ++n2) {
                String string5 = stringArray3[n2];
                int n6 = i.aa(string5).size();
                if (i.Y(string5) && n6 < n4) {
                    n4 = n6;
                    string3 = string5;
                    continue;
                }
                if (!i.Y(string5) || n6 != n4 || string5.compareTo(string3) >= 0) continue;
                string3 = string5;
            }
            if (object == null || string3 == null || !(n3 - n4 >= 2 || n3 - n4 == 1 && ((String)object).compareTo(string3) > 0)) continue;
            long l = (Long)hashMap.get(string3);
            if ((object = i.aa((String)object)).isEmpty()) {
                bl2 = false;
            } else if (!i.Y(string3)) {
                bl2 = false;
            } else {
                Object object2 = object;
                i.a((k)object2.get(object2.size() - 1), string3, l);
                bl2 = true;
            }
            if (!bl2) continue;
            bl = true;
        }
    }

    final void t(String string, String string2) {
        String string3;
        int n = i.v(string, string2) ? 1 : 2;
        HashMap<String, Long> hashMap = new HashMap<String, Long>();
        for (String object : i.ae(string2)) {
            long l;
            List<k> list = i.aa(object);
            if (list.isEmpty()) {
                l = 0L;
            } else {
                List<k> list2 = list;
                l = list2.get((int)(list2.size() - 1)).cs;
            }
            hashMap.put(object, l);
        }
        while (!(i.w(string, string2) < (double)n) && (string3 = i.ai(string2)) != null) {
            List<String> list = this.ah(string);
            boolean bl = false;
            Iterator<String> iterator = list.iterator();
            while (iterator.hasNext()) {
                if (!i.a((String)iterator.next(), string3, string2, (Long)hashMap.get(string3))) continue;
                bl = true;
                break;
            }
            if (bl) continue;
        }
    }

    private List<String> ah(String stringArray) {
        stringArray = i.ae((String)stringArray);
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String string3 : stringArray) {
            if (i.aa(string3).isEmpty()) continue;
            arrayList.add(string3);
        }
        arrayList.sort((string, string2) -> {
            int n;
            int n2 = i.aa(string).size();
            if (n2 != (n = i.aa(string2).size())) {
                return n - n2;
            }
            return string2.compareTo((String)string);
        });
        return arrayList;
    }

    private static boolean a(String object, String string, String string2, long l) {
        if ((object = i.aa((String)object)).isEmpty()) {
            return false;
        }
        if (!i.Y(string)) {
            return false;
        }
        Object object2 = object;
        object = (k)object2.get(object2.size() - 1);
        if (!i.u(((k)object).bB, string2)) {
            return false;
        }
        i.a((k)object, string, l);
        return true;
    }

    private static boolean u(String string, String string2) {
        if ("S".equals(string)) {
            return "WS".equals(string2) || "WM".equals(string2) || "WL".equals(string2);
        }
        if ("M".equals(string)) {
            return "WM".equals(string2) || "WL".equals(string2);
        }
        if ("L".equals(string)) {
            return "WL".equals(string2);
        }
        return false;
    }

    private static void a(k k2, String string, long l) {
        for (k k3 : d.dx.values()) {
            if (!"queuing".equals(k3.bt) || !string.equals(k3.bN) || k3 == k2 || k3.cs <= l) continue;
            ++k3.cs;
        }
        k2.bN = string;
        k2.cs = l + 1L;
    }

    private static boolean v(String string, String string2) {
        if ("WL".equals(string) && "WM".equals(string2)) {
            return true;
        }
        return "WM".equals(string) && "WS".equals(string2);
    }

    private static double w(String object, String stringArray) {
        String string;
        object = i.ae((String)object);
        stringArray = i.ae((String)stringArray);
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String string2 : object) {
            if (i.Z(string2) - i.aa(string2).size() >= 20) continue;
            arrayList.add(string2);
        }
        object = new ArrayList();
        String[] stringArray2 = stringArray;
        int n = stringArray.length;
        for (int j = 0; j < n; ++j) {
            string = stringArray2[j];
            if (i.aa(string).size() >= 6 || i.Z(string) >= 20) continue;
            object.add(string);
        }
        if (arrayList.isEmpty() || object.isEmpty()) {
            return 0.0;
        }
        int n2 = 0;
        for (String string3 : arrayList) {
            n2 += i.aa(string3).size();
        }
        int n3 = 0;
        Iterator iterator = object.iterator();
        while (iterator.hasNext()) {
            string = (String)iterator.next();
            n3 += i.aa(string).size();
        }
        double d2 = (double)n2 / (double)arrayList.size();
        double d3 = (double)n3 / (double)object.size();
        return Math.floor((d2 - d3) * 100.0) / 100.0;
    }

    private static String ai(String stringArray) {
        stringArray = i.ae((String)stringArray);
        String string = null;
        int n = Integer.MAX_VALUE;
        for (String string2 : stringArray) {
            int n2;
            if (!i.Y(string2) || (n2 = i.aa(string2).size()) >= n && (n2 != n || string != null && string2.compareTo(string) >= 0)) continue;
            n = n2;
            string = string2;
        }
        return string;
    }

    static {
        cS.put("S", new double[]{45.0, 50.0});
        cS.put("M", new double[]{54.0, 60.0});
        cS.put("L", new double[]{72.0, 80.0});
        cT = new String[]{"WS1", "WS2", "WS3"};
        cU = new String[]{"WM1", "WM2"};
        cV = new String[]{"WL1"};
        cW = Arrays.asList("WS1", "WS2", "WS3", "WM1", "WM2", "WL1");
    }
}
