/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.util.Stack;

final class PrivateSecurityManagerStackTraceUtil {
    private static final PrivateSecurityManager SECURITY_MANAGER;

    private PrivateSecurityManagerStackTraceUtil() {
    }

    static boolean isEnabled() {
        return SECURITY_MANAGER != null;
    }

    static Stack<Class<?>> getCurrentStackTrace() {
        Class<?>[] classArray = SECURITY_MANAGER.getClassContext();
        Stack stack = new Stack();
        stack.ensureCapacity(classArray.length);
        for (Class<?> clazz : classArray) {
            stack.push(clazz);
        }
        return stack;
    }

    static {
        SecurityManager securityManager;
        try {
            securityManager = System.getSecurityManager();
            if (securityManager != null) {
                securityManager.checkPermission(new RuntimePermission("createSecurityManager"));
            }
            securityManager = new PrivateSecurityManager();
        }
        catch (SecurityException securityException) {
            securityManager = null;
        }
        SECURITY_MANAGER = securityManager;
    }

    static final class PrivateSecurityManager
    extends SecurityManager {
        private PrivateSecurityManager() {
        }

        protected final Class<?>[] getClassContext() {
            return super.getClassContext();
        }
    }
}

