/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.util.NoSuchElementException;
import java.util.Stack;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.StackLocator;

public final class StackLocatorUtil {
    private static StackLocator stackLocator = null;
    private static volatile boolean errorLogged = false;

    private StackLocatorUtil() {
    }

    @PerformanceSensitive
    public static Class<?> getCallerClass(int n) {
        return stackLocator.getCallerClass(n + 1);
    }

    public static StackTraceElement getStackTraceElement(int n) {
        return stackLocator.getStackTraceElement(n + 1);
    }

    @PerformanceSensitive
    public static Class<?> getCallerClass(String string) {
        return StackLocatorUtil.getCallerClass(string, "");
    }

    @PerformanceSensitive
    public static Class<?> getCallerClass(String string, String string2) {
        return stackLocator.getCallerClass(string, string2);
    }

    @PerformanceSensitive
    public static Class<?> getCallerClass(Class<?> clazz) {
        return stackLocator.getCallerClass(clazz);
    }

    @PerformanceSensitive
    public static Stack<Class<?>> getCurrentStackTrace() {
        return stackLocator.getCurrentStackTrace();
    }

    public static StackTraceElement calcLocation(String string) {
        try {
            return stackLocator.calcLocation(string);
        }
        catch (NoSuchElementException noSuchElementException) {
            if (!errorLogged) {
                errorLogged = true;
                StatusLogger.getLogger().warn("Unable to locate stack trace element for {}", (Object)string, (Object)noSuchElementException);
            }
            return null;
        }
    }

    static {
        stackLocator = StackLocator.getInstance();
    }
}

