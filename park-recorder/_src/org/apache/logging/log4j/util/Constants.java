/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.PropertiesUtil;

public final class Constants {
    public static final boolean IS_WEB_APP = PropertiesUtil.getProperties().getBooleanProperty("log4j2.is.webapp", Constants.isClassAvailable("javax.servlet.Servlet"));
    public static final boolean ENABLE_THREADLOCALS = !IS_WEB_APP && PropertiesUtil.getProperties().getBooleanProperty("log4j2.enable.threadlocals", true);
    public static final int JAVA_MAJOR_VERSION = Constants.getMajorVersion();
    public static final int MAX_REUSABLE_MESSAGE_SIZE = Constants.size("log4j.maxReusableMsgSize", 518);
    public static final String LOG4J2_DEBUG = "log4j2.debug";

    private static int size(String string, int n) {
        return PropertiesUtil.getProperties().getIntegerProperty(string, n);
    }

    private static boolean isClassAvailable(String string) {
        try {
            return LoaderUtil.loadClass(string) != null;
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    private Constants() {
    }

    private static int getMajorVersion() {
        return Constants.getMajorVersion(System.getProperty("java.version"));
    }

    static int getMajorVersion(String stringArray) {
        stringArray = stringArray.split("-|\\.");
        try {
            int n = Integer.parseInt(stringArray[0]);
            if (n != 1) {
                return n;
            }
            return Integer.parseInt(stringArray[1]);
        }
        catch (Exception exception) {
            return 0;
        }
    }
}

