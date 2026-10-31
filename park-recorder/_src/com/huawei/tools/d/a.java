/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.d;

import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

public final class a {
    private static final String FQCN = a.class.getName();
    private static final a ao = new a(0);

    private a() {
    }

    private static LocationAwareLogger a() {
        return (LocationAwareLogger)LoggerFactory.getLogger(ao.b().getName());
    }

    public static void trace(String string) {
        a.a().log(null, FQCN, 0, string, null, null);
    }

    public static void info(String string) {
        a.a().log(null, FQCN, 20, string, null, null);
    }

    public static void error(String string) {
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

