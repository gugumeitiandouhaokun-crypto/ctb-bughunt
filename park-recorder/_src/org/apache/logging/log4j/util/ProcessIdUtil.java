/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

public class ProcessIdUtil {
    public static final String DEFAULT_PROCESSID = "-";

    public static String getProcessId() {
        try {
            Object object = Class.forName("java.lang.management.ManagementFactory").getDeclaredMethod("getRuntimeMXBean", new Class[0]);
            Method method = Class.forName("java.lang.management.RuntimeMXBean").getDeclaredMethod("getName", new Class[0]);
            object = ((Method)object).invoke(null, new Object[0]);
            return ((String)method.invoke(object, new Object[0])).split("@")[0];
        }
        catch (Exception exception) {
            try {
                return new File("/proc/self").getCanonicalFile().getName();
            }
            catch (IOException iOException) {
                return DEFAULT_PROCESSID;
            }
        }
    }
}

