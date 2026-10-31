/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.e;

import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

public final class a {
    private static final String FQCN = com.huawei.tools.d.a.class.getName();
    private static final a ap = new a(0);

    private static LocationAwareLogger a() {
        return (LocationAwareLogger)LoggerFactory.getLogger(ap.b().getName());
    }

    public static void info(String string) {
        string = com.huawei.tools.h.a.B(string);
        a.a().log(null, FQCN, 20, string, null, null);
    }

    public static void error(String string) {
        string = com.huawei.tools.h.a.B(string);
        a.a().log(null, FQCN, 40, string, null, null);
    }

    static final class a
    extends SecurityManager {
        private a() {
        }

        final Class<?> b() {
            return super.getClassContext()[3];
        }

        /* synthetic */ a(byte by) {
            this();
        }
    }
}

