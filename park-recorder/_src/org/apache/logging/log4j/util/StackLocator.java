/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.lang.reflect.Method;
import java.util.Stack;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.PrivateSecurityManagerStackTraceUtil;

public final class StackLocator {
    static final int JDK_7u25_OFFSET;
    private static final Method GET_CALLER_CLASS;
    private static final StackLocator INSTANCE;

    public static StackLocator getInstance() {
        return INSTANCE;
    }

    private StackLocator() {
    }

    @PerformanceSensitive
    public final Class<?> getCallerClass(int n) {
        if (n < 0) {
            throw new IndexOutOfBoundsException(Integer.toString(n));
        }
        if (GET_CALLER_CLASS == null) {
            return null;
        }
        try {
            return (Class)GET_CALLER_CLASS.invoke(null, n + 1 + JDK_7u25_OFFSET);
        }
        catch (Exception exception) {
            return null;
        }
    }

    @PerformanceSensitive
    public final Class<?> getCallerClass(String string, String string2) {
        Class<?> clazz;
        boolean bl = false;
        int n = 2;
        while (null != (clazz = this.getCallerClass(n))) {
            if (string.equals(clazz.getName())) {
                bl = true;
            } else if (bl && clazz.getName().startsWith(string2)) {
                return clazz;
            }
            ++n;
        }
        return null;
    }

    @PerformanceSensitive
    public final Class<?> getCallerClass(Class<?> clazz) {
        Class<?> clazz2;
        boolean bl = false;
        int n = 2;
        while (null != (clazz2 = this.getCallerClass(n))) {
            if (clazz.equals(clazz2)) {
                bl = true;
            } else if (bl) {
                return clazz2;
            }
            ++n;
        }
        return Object.class;
    }

    @PerformanceSensitive
    public final Stack<Class<?>> getCurrentStackTrace() {
        Class<?> clazz;
        if (PrivateSecurityManagerStackTraceUtil.isEnabled()) {
            return PrivateSecurityManagerStackTraceUtil.getCurrentStackTrace();
        }
        Stack stack = new Stack();
        int n = 1;
        while (null != (clazz = this.getCallerClass(n))) {
            stack.push(clazz);
            ++n;
        }
        return stack;
    }

    public final StackTraceElement calcLocation(String string) {
        if (string == null) {
            return null;
        }
        StackTraceElement[] stackTraceElementArray = new Throwable().getStackTrace();
        boolean bl = false;
        for (int j = 0; j < stackTraceElementArray.length; ++j) {
            String string2 = stackTraceElementArray[j].getClassName();
            if (string.equals(string2)) {
                bl = true;
                continue;
            }
            if (!bl || string.equals(string2)) continue;
            return stackTraceElementArray[j];
        }
        return null;
    }

    public final StackTraceElement getStackTraceElement(int n) {
        StackTraceElement[] stackTraceElementArray = new Throwable().getStackTrace();
        int n2 = 0;
        for (StackTraceElement stackTraceElement : stackTraceElementArray) {
            if (!this.isValid(stackTraceElement)) continue;
            if (n2 == n) {
                return stackTraceElement;
            }
            ++n2;
        }
        throw new IndexOutOfBoundsException(Integer.toString(n));
    }

    private boolean isValid(StackTraceElement object) {
        if (((StackTraceElement)object).isNativeMethod()) {
            return false;
        }
        String string = ((StackTraceElement)object).getClassName();
        if (string.startsWith("sun.reflect.")) {
            return false;
        }
        object = ((StackTraceElement)object).getMethodName();
        if (string.startsWith("java.lang.reflect.") && (((String)object).equals("invoke") || ((String)object).equals("newInstance"))) {
            return false;
        }
        if (string.startsWith("jdk.internal.reflect.")) {
            return false;
        }
        if (string.equals("java.lang.Class") && ((String)object).equals("newInstance")) {
            return false;
        }
        return !string.equals("java.lang.invoke.MethodHandle") || !((String)object).startsWith("invoke");
    }

    static {
        Method method;
        int n = 0;
        try {
            Class<?> clazz = LoaderUtil.loadClass("sun.reflect.Reflection");
            method = clazz.getDeclaredMethod("getCallerClass", Integer.TYPE);
            Object object = method.invoke(null, 0);
            method.invoke(null, 0);
            if (object == null || object != clazz) {
                method = null;
                n = -1;
            } else if (method.invoke(null, 1) == clazz) {
                System.out.println("WARNING: Java 1.7.0_25 is in use which has a broken implementation of Reflection.getCallerClass().  Please consider upgrading to Java 1.7.0_40 or later.");
                n = 1;
            }
        }
        catch (Exception | LinkageError throwable) {
            System.out.println("WARNING: sun.reflect.Reflection.getCallerClass is not supported. This will impact performance.");
            method = null;
            n = -1;
        }
        GET_CALLER_CLASS = method;
        JDK_7u25_OFFSET = n;
        INSTANCE = new StackLocator();
    }
}

