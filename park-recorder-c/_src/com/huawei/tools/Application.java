/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools;

import com.huawei.tools.d.a;
import com.huawei.tools.f.a.c;
import com.huawei.tools.f.b.a;
import com.huawei.tools.f.b.b;
import com.huawei.tools.h.e;
import java.net.SocketException;
import org.apache.commons.lang3.StringUtils;

public class Application {
    public static void main(String[] object) {
        int n = 6000;
        if (((String[])object).length != 0) {
            a.trace(String.format("server start success, args[0] = %s", object[0]));
            object = e.z(object[0], "udpPort");
            if (StringUtils.isBlank((CharSequence)object)) {
                a.error("get upd port error,  please config config.properties path");
                return;
            }
            n = Integer.parseInt((String)object);
        }
        try {
            if (com.huawei.tools.h.c.v()) {
                return;
            }
            object = new com.huawei.tools.f.b.a(n);
            Object object2 = new String[]{""};
            a.a a2 = () -> Application.lambda$main$0((String[])object2);
            object2 = new com.huawei.tools.a((String[])object2);
            object.bb = new b((com.huawei.tools.f.b.a)object, "receive_thread", (c)object2, a2);
            object.bb.start();
            return;
        }
        catch (SocketException socketException) {
            a.error("Unable to initialise the server..." + socketException.getMessage());
            System.exit(1);
            return;
        }
    }

    private static /* synthetic */ String lambda$main$0(String[] stringArray) {
        return stringArray[0];
    }
}

