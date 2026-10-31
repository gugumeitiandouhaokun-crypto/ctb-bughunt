/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.c;

import com.huawei.tools.a.b;
import com.huawei.tools.g.b.a.f;

public final class a {
    static String[] an;

    public static String a(String string) {
        f f2 = new f();
        try {
            String string2;
            String string3 = string2 = string.replaceAll(" ", "");
            int n = string2.indexOf("(");
            int n2 = string3.indexOf(")");
            an = n == -1 || n2 == -1 ? new String[]{} : ((string3 = string3.substring(n + 1, n2)).trim().isEmpty() ? new String[]{} : (string3.endsWith(",") ? string3.split(",", -1) : string3.split(",")));
            string3 = string2;
            String string4 = string3.replaceAll(" ", "");
            n2 = string3.indexOf("(");
            string2 = n2 >= 0 ? string4.substring(0, n2).toLowerCase() : "";
            string3 = string;
            com.huawei.tools.d.a.info(String.format("[LOG][CASE][CMD] %s", string3));
            switch (string2) {
                case "init": {
                    a.a(0);
                    Thread.sleep(20L);
                    return f2.c();
                }
                case "register_account": {
                    a.a(2);
                    Thread.sleep(20L);
                    return f2.b(an[0], an[1]);
                }
                case "activate_account": {
                    a.a(2);
                    Thread.sleep(20L);
                    return f2.c(an[0], an[1]);
                }
                case "unregister_account": {
                    a.a(2);
                    Thread.sleep(20L);
                    return f2.d(an[0], an[1]);
                }
                case "bind_plate_id": {
                    a.a(2);
                    Thread.sleep(20L);
                    return f2.e(an[0], an[1]);
                }
                case "unbind_plate_id": {
                    a.a(2);
                    Thread.sleep(20L);
                    return f2.f(an[0], an[1]);
                }
                case "car_entry": {
                    a.a(2);
                    Thread.sleep(20L);
                    return f2.g(an[0], an[1]);
                }
                case "car_exit": {
                    a.a(2);
                    Thread.sleep(20L);
                    return f2.h(an[0], an[1]);
                }
                case "change_gate": {
                    a.a(2);
                    Thread.sleep(20L);
                    return f2.i(an[0], an[1]);
                }
                case "occupy_park": {
                    a.a(2);
                    Thread.sleep(20L);
                    return f2.j(an[0], an[1]);
                }
                case "release_park": {
                    a.a(1);
                    Thread.sleep(20L);
                    return f2.b(an[0]);
                }
                case "time_pass": {
                    a.a(2);
                    Thread.sleep(20L);
                    return f2.a(an[0], Integer.parseInt(an[1]));
                }
                case "query_account": {
                    a.a(1);
                    Thread.sleep(20L);
                    return f2.c(an[0]);
                }
                case "query_bindcar_info": {
                    a.a(1);
                    Thread.sleep(20L);
                    return f2.d(an[0]);
                }
                case "query_gate_status": {
                    a.a(1);
                    Thread.sleep(20L);
                    return f2.e(an[0]);
                }
                case "check_car_status": {
                    a.a(3);
                    Thread.sleep(20L);
                    return f2.a(an[0], an[1], an[2]);
                }
                case "add_blacklist": {
                    a.a(1);
                    Thread.sleep(20L);
                    return f2.f(an[0]);
                }
                case "del_blacklist": {
                    a.a(1);
                    Thread.sleep(20L);
                    return f2.g(an[0]);
                }
                case "recharge_account": {
                    a.a(2);
                    Thread.sleep(20L);
                    return f2.b(an[0], Integer.parseInt(an[1]));
                }
                case "transfer_account": {
                    a.a(3);
                    Thread.sleep(20L);
                    return f2.a(an[0], an[1], Integer.parseInt(an[2]));
                }
                case "purchase_moncard": {
                    a.a(2);
                    Thread.sleep(20L);
                    return f2.l(an[0], an[1]);
                }
                case "transfer_moncard": {
                    a.a(3);
                    Thread.sleep(20L);
                    return f2.b(an[0], an[1], Integer.parseInt(an[2]));
                }
                case "pay_parkcost": {
                    a.a(3);
                    Thread.sleep(20L);
                    return f2.c(an[0], an[1], an[2]);
                }
                case "check_occupied_spaces": {
                    a.a(2);
                    Thread.sleep(20L);
                    return f2.k(an[0], an[1]);
                }
                case "check_park_statistics": {
                    a.a(3);
                    Thread.sleep(20L);
                    return f2.b(an[0], an[1], an[2]);
                }
                case "check_revenue": {
                    a.a(3);
                    Thread.sleep(20L);
                    return f2.d(an[0], an[1], an[2]);
                }
                case "query_account_asset": {
                    a.a(1);
                    Thread.sleep(20L);
                    return f2.h(an[0]);
                }
            }
            return b.a("E999", "Unknown Command or params error!");
        }
        catch (com.huawei.tools.b.a a2) {
            return "E001:Fail";
        }
        catch (Exception exception) {
            return b.a("E999", "Unknown Command or params error!");
        }
    }

    private static void a(int n) {
        if (an == null && n == 0) {
            return;
        }
        boolean bl = false;
        String[] stringArray = an;
        int n2 = an.length;
        for (int j = 0; j < n2; ++j) {
            if (!stringArray[j].equals("")) continue;
            bl = true;
            break;
        }
        if (an != null && an.length == n && !bl) {
            return;
        }
        throw new Exception("\u53c2\u6570\u6570\u91cf\u5f02\u5e38\uff01\uff01");
    }
}

