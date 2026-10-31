/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.apache.logging.log4j.core.impl.ExtendedStackTraceElement;
import org.apache.logging.log4j.core.impl.ThrowableProxyHelper;
import org.apache.logging.log4j.core.impl.ThrowableProxyRenderer;
import org.apache.logging.log4j.core.pattern.PlainTextRenderer;
import org.apache.logging.log4j.core.pattern.TextRenderer;
import org.apache.logging.log4j.util.StackLocatorUtil;

public class ThrowableProxy
implements Serializable {
    private static final char EOL = '\n';
    private static final String EOL_STR = "\n";
    private static final long serialVersionUID = -2752771578252251910L;
    private final ThrowableProxy causeProxy;
    private int commonElementCount;
    private final ExtendedStackTraceElement[] extendedStackTrace;
    private final String localizedMessage;
    private final String message;
    private final String name;
    private final ThrowableProxy[] suppressedProxies;
    private final transient Throwable throwable;

    private ThrowableProxy() {
        this.throwable = null;
        this.name = null;
        this.extendedStackTrace = null;
        this.causeProxy = null;
        this.message = null;
        this.localizedMessage = null;
        this.suppressedProxies = ThrowableProxyHelper.EMPTY_THROWABLE_PROXY_ARRAY;
    }

    public ThrowableProxy(Throwable throwable) {
        this(throwable, null);
    }

    ThrowableProxy(Throwable throwable, Set<Throwable> set) {
        this.throwable = throwable;
        this.name = throwable.getClass().getName();
        this.message = throwable.getMessage();
        this.localizedMessage = throwable.getLocalizedMessage();
        HashMap<String, ThrowableProxyHelper.CacheEntry> hashMap = new HashMap<String, ThrowableProxyHelper.CacheEntry>();
        Stack<Class<?>> stack = StackLocatorUtil.getCurrentStackTrace();
        this.extendedStackTrace = ThrowableProxyHelper.toExtendedStackTrace(this, stack, hashMap, null, throwable.getStackTrace());
        Throwable throwable2 = throwable.getCause();
        HashSet<Throwable> hashSet = new HashSet<Throwable>(1);
        this.causeProxy = throwable2 == null ? null : new ThrowableProxy(throwable, stack, hashMap, throwable2, set, hashSet);
        this.suppressedProxies = ThrowableProxyHelper.toSuppressedProxies(throwable, set);
    }

    private ThrowableProxy(Throwable throwable, Stack<Class<?>> stack, Map<String, ThrowableProxyHelper.CacheEntry> map, Throwable throwable2, Set<Throwable> set, Set<Throwable> set2) {
        set2.add(throwable2);
        this.throwable = throwable2;
        this.name = throwable2.getClass().getName();
        this.message = this.throwable.getMessage();
        this.localizedMessage = this.throwable.getLocalizedMessage();
        this.extendedStackTrace = ThrowableProxyHelper.toExtendedStackTrace(this, stack, map, throwable.getStackTrace(), throwable2.getStackTrace());
        Throwable throwable3 = throwable2.getCause();
        this.causeProxy = throwable3 == null || set2.contains(throwable3) ? null : new ThrowableProxy(throwable, stack, map, throwable3, set, set2);
        this.suppressedProxies = ThrowableProxyHelper.toSuppressedProxies(throwable2, set);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (this.getClass() != object.getClass()) {
            return false;
        }
        object = (ThrowableProxy)object;
        if (this.causeProxy == null ? ((ThrowableProxy)object).causeProxy != null : !this.causeProxy.equals(((ThrowableProxy)object).causeProxy)) {
            return false;
        }
        if (this.commonElementCount != ((ThrowableProxy)object).commonElementCount) {
            return false;
        }
        if (this.name == null ? ((ThrowableProxy)object).name != null : !this.name.equals(((ThrowableProxy)object).name)) {
            return false;
        }
        if (!Arrays.equals(this.extendedStackTrace, ((ThrowableProxy)object).extendedStackTrace)) {
            return false;
        }
        return Arrays.equals(this.suppressedProxies, ((ThrowableProxy)object).suppressedProxies);
    }

    public void formatWrapper(StringBuilder stringBuilder, ThrowableProxy throwableProxy, String string) {
        this.formatWrapper(stringBuilder, throwableProxy, null, PlainTextRenderer.getInstance(), string);
    }

    public void formatWrapper(StringBuilder stringBuilder, ThrowableProxy throwableProxy, List<String> list, String string) {
        this.formatWrapper(stringBuilder, throwableProxy, list, PlainTextRenderer.getInstance(), string);
    }

    public void formatWrapper(StringBuilder stringBuilder, ThrowableProxy throwableProxy, List<String> list, TextRenderer textRenderer, String string) {
        this.formatWrapper(stringBuilder, throwableProxy, list, textRenderer, string, EOL_STR);
    }

    public void formatWrapper(StringBuilder stringBuilder, ThrowableProxy throwableProxy, List<String> list, TextRenderer textRenderer, String string, String string2) {
        ThrowableProxyRenderer.formatWrapper(stringBuilder, throwableProxy, list, textRenderer, string, string2);
    }

    public ThrowableProxy getCauseProxy() {
        return this.causeProxy;
    }

    public String getCauseStackTraceAsString(String string) {
        return this.getCauseStackTraceAsString(null, PlainTextRenderer.getInstance(), string, EOL_STR);
    }

    public String getCauseStackTraceAsString(List<String> list, String string) {
        return this.getCauseStackTraceAsString(list, PlainTextRenderer.getInstance(), string, EOL_STR);
    }

    public String getCauseStackTraceAsString(List<String> list, TextRenderer textRenderer, String string) {
        return this.getCauseStackTraceAsString(list, textRenderer, string, EOL_STR);
    }

    public String getCauseStackTraceAsString(List<String> list, TextRenderer textRenderer, String string, String string2) {
        StringBuilder stringBuilder = new StringBuilder();
        ThrowableProxyRenderer.formatCauseStackTrace(this, stringBuilder, list, textRenderer, string, string2);
        return stringBuilder.toString();
    }

    public int getCommonElementCount() {
        return this.commonElementCount;
    }

    void setCommonElementCount(int n) {
        this.commonElementCount = n;
    }

    public ExtendedStackTraceElement[] getExtendedStackTrace() {
        return this.extendedStackTrace;
    }

    public String getExtendedStackTraceAsString() {
        return this.getExtendedStackTraceAsString(null, PlainTextRenderer.getInstance(), "", EOL_STR);
    }

    public String getExtendedStackTraceAsString(String string) {
        return this.getExtendedStackTraceAsString(null, PlainTextRenderer.getInstance(), string, EOL_STR);
    }

    public String getExtendedStackTraceAsString(List<String> list, String string) {
        return this.getExtendedStackTraceAsString(list, PlainTextRenderer.getInstance(), string, EOL_STR);
    }

    public String getExtendedStackTraceAsString(List<String> list, TextRenderer textRenderer, String string) {
        return this.getExtendedStackTraceAsString(list, textRenderer, string, EOL_STR);
    }

    public String getExtendedStackTraceAsString(List<String> list, TextRenderer textRenderer, String string, String string2) {
        StringBuilder stringBuilder = new StringBuilder(1024);
        this.formatExtendedStackTraceTo(stringBuilder, list, textRenderer, string, string2);
        return stringBuilder.toString();
    }

    public void formatExtendedStackTraceTo(StringBuilder stringBuilder, List<String> list, TextRenderer textRenderer, String string, String string2) {
        ThrowableProxyRenderer.formatExtendedStackTraceTo(this, stringBuilder, list, textRenderer, string, string2);
    }

    public String getLocalizedMessage() {
        return this.localizedMessage;
    }

    public String getMessage() {
        return this.message;
    }

    public String getName() {
        return this.name;
    }

    public StackTraceElement[] getStackTrace() {
        if (this.throwable == null) {
            return null;
        }
        return this.throwable.getStackTrace();
    }

    public ThrowableProxy[] getSuppressedProxies() {
        return this.suppressedProxies;
    }

    public String getSuppressedStackTrace(String string) {
        ThrowableProxy[] throwableProxyArray = this.getSuppressedProxies();
        if (throwableProxyArray == null || throwableProxyArray.length == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder("Suppressed Stack Trace Elements:\n");
        for (ThrowableProxy throwableProxy : throwableProxyArray) {
            stringBuilder.append(throwableProxy.getExtendedStackTraceAsString(string));
        }
        return stringBuilder.toString();
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    public int hashCode() {
        int n = 31 + (this.causeProxy == null ? 0 : this.causeProxy.hashCode());
        n = 31 * n + this.commonElementCount;
        n = 31 * n + (this.extendedStackTrace == null ? 0 : Arrays.hashCode(this.extendedStackTrace));
        n = 31 * n + (this.suppressedProxies == null ? 0 : Arrays.hashCode(this.suppressedProxies));
        return 31 * n + (this.name == null ? 0 : this.name.hashCode());
    }

    public String toString() {
        String string = this.message;
        if (string != null) {
            return this.name + ": " + string;
        }
        return this.name;
    }
}

