/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j.helpers;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class Util {
    private static ClassContextSecurityManager SECURITY_MANAGER;
    private static boolean SECURITY_MANAGER_CREATION_ALREADY_ATTEMPTED;

    private Util() {
    }

    public static String safeGetSystemProperty(String string) {
        if (string == null) {
            throw new IllegalArgumentException("null input");
        }
        String string2 = null;
        try {
            string2 = System.getProperty(string);
        }
        catch (SecurityException securityException) {}
        return string2;
    }

    public static boolean safeGetBooleanSystemProperty(String string) {
        if ((string = Util.safeGetSystemProperty(string)) == null) {
            return false;
        }
        return string.equalsIgnoreCase("true");
    }

    private static ClassContextSecurityManager getSecurityManager() {
        if (SECURITY_MANAGER != null) {
            return SECURITY_MANAGER;
        }
        if (SECURITY_MANAGER_CREATION_ALREADY_ATTEMPTED) {
            return null;
        }
        SECURITY_MANAGER = Util.safeCreateSecurityManager();
        SECURITY_MANAGER_CREATION_ALREADY_ATTEMPTED = true;
        return SECURITY_MANAGER;
    }

    private static ClassContextSecurityManager safeCreateSecurityManager() {
        try {
            return new ClassContextSecurityManager();
        }
        catch (SecurityException securityException) {
            return null;
        }
    }

    public static Class<?> getCallingClass() {
        int n;
        Class<?>[] classArray = Util.getSecurityManager();
        if (classArray == null) {
            return null;
        }
        classArray = classArray.getClassContext();
        String string = Util.class.getName();
        for (n = 0; n < classArray.length && !string.equals(classArray[n].getName()); ++n) {
        }
        if (n >= classArray.length || n + 2 >= classArray.length) {
            throw new IllegalStateException("Failed to find org.slf4j.helpers.Util or its caller in the stack; this should not happen");
        }
        return classArray[n + 2];
    }

    public static final void report(String string, Throwable throwable) {
        System.err.println(string);
        System.err.println("Reported exception:");
        throwable.printStackTrace();
    }

    public static final void report(String string) {
        System.err.println("SLF4J: ".concat(String.valueOf(string)));
    }

    static {
        SECURITY_MANAGER_CREATION_ALREADY_ATTEMPTED = false;
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static final class ClassContextSecurityManager
    extends SecurityManager {
        private ClassContextSecurityManager() {
        }

        protected final Class<?>[] getClassContext() {
            return super.getClassContext();
        }
    }
}

