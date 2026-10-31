/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import java.util.List;
import org.apache.logging.log4j.core.impl.ExtendedStackTraceElement;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.pattern.TextRenderer;

class ThrowableProxyRenderer {
    private static final String TAB = "\t";
    private static final String CAUSED_BY_LABEL = "Caused by: ";
    private static final String SUPPRESSED_LABEL = "Suppressed: ";
    private static final String WRAPPED_BY_LABEL = "Wrapped by: ";

    private ThrowableProxyRenderer() {
    }

    static void formatWrapper(StringBuilder stringBuilder, ThrowableProxy throwableProxy, List<String> list, TextRenderer textRenderer, String string, String string2) {
        if ((throwableProxy.getCauseProxy() != null ? throwableProxy.getCauseProxy().getThrowable() : null) != null) {
            ThrowableProxyRenderer.formatWrapper(stringBuilder, throwableProxy.getCauseProxy(), list, textRenderer, string, string2);
            stringBuilder.append(WRAPPED_BY_LABEL);
            ThrowableProxyRenderer.renderSuffix(string, stringBuilder, textRenderer);
        }
        ThrowableProxyRenderer.renderOn(throwableProxy, stringBuilder, textRenderer);
        ThrowableProxyRenderer.renderSuffix(string, stringBuilder, textRenderer);
        textRenderer.render(string2, stringBuilder, "Text");
        ThrowableProxyRenderer.formatElements(stringBuilder, "", throwableProxy.getCommonElementCount(), throwableProxy.getThrowable().getStackTrace(), throwableProxy.getExtendedStackTrace(), list, textRenderer, string, string2);
    }

    private static void formatCause(StringBuilder stringBuilder, String string, ThrowableProxy throwableProxy, List<String> list, TextRenderer textRenderer, String string2, String string3) {
        ThrowableProxyRenderer.formatThrowableProxy(stringBuilder, string, CAUSED_BY_LABEL, throwableProxy, list, textRenderer, string2, string3);
    }

    private static void formatThrowableProxy(StringBuilder stringBuilder, String string, String string2, ThrowableProxy throwableProxy, List<String> list, TextRenderer textRenderer, String string3, String string4) {
        if (throwableProxy == null) {
            return;
        }
        textRenderer.render(string, stringBuilder, "Prefix");
        textRenderer.render(string2, stringBuilder, "CauseLabel");
        ThrowableProxyRenderer.renderOn(throwableProxy, stringBuilder, textRenderer);
        ThrowableProxyRenderer.renderSuffix(string3, stringBuilder, textRenderer);
        textRenderer.render(string4, stringBuilder, "Text");
        ThrowableProxyRenderer.formatElements(stringBuilder, string, throwableProxy.getCommonElementCount(), throwableProxy.getStackTrace(), throwableProxy.getExtendedStackTrace(), list, textRenderer, string3, string4);
        ThrowableProxyRenderer.formatSuppressed(stringBuilder, string + TAB, throwableProxy.getSuppressedProxies(), list, textRenderer, string3, string4);
        ThrowableProxyRenderer.formatCause(stringBuilder, string, throwableProxy.getCauseProxy(), list, textRenderer, string3, string4);
    }

    private static void formatSuppressed(StringBuilder stringBuilder, String string, ThrowableProxy[] throwableProxyArray, List<String> list, TextRenderer textRenderer, String string2, String string3) {
        if (throwableProxyArray == null) {
            return;
        }
        for (ThrowableProxy throwableProxy : throwableProxyArray) {
            ThrowableProxyRenderer.formatThrowableProxy(stringBuilder, string, SUPPRESSED_LABEL, throwableProxy, list, textRenderer, string2, string3);
        }
    }

    private static void formatElements(StringBuilder stringBuilder, String string, int n, StackTraceElement[] stackTraceElementArray, ExtendedStackTraceElement[] extendedStackTraceElementArray, List<String> list, TextRenderer textRenderer, String string2, String string3) {
        if (list == null || list.isEmpty()) {
            ExtendedStackTraceElement[] extendedStackTraceElementArray2 = extendedStackTraceElementArray;
            int n2 = extendedStackTraceElementArray.length;
            for (int j = 0; j < n2; ++j) {
                ThrowableProxyRenderer.formatEntry(extendedStackTraceElementArray2[j], stringBuilder, string, textRenderer, string2, string3);
            }
        } else {
            int n3 = 0;
            for (int j = 0; j < extendedStackTraceElementArray.length; ++j) {
                if (!ThrowableProxyRenderer.ignoreElement(stackTraceElementArray[j], list)) {
                    if (n3 > 0) {
                        ThrowableProxyRenderer.appendSuppressedCount(stringBuilder, string, n3, textRenderer, string2, string3);
                        n3 = 0;
                    }
                    ThrowableProxyRenderer.formatEntry(extendedStackTraceElementArray[j], stringBuilder, string, textRenderer, string2, string3);
                    continue;
                }
                ++n3;
            }
            if (n3 > 0) {
                ThrowableProxyRenderer.appendSuppressedCount(stringBuilder, string, n3, textRenderer, string2, string3);
            }
        }
        if (n != 0) {
            textRenderer.render(string, stringBuilder, "Prefix");
            textRenderer.render("\t... ", stringBuilder, "More");
            textRenderer.render(Integer.toString(n), stringBuilder, "More");
            textRenderer.render(" more", stringBuilder, "More");
            ThrowableProxyRenderer.renderSuffix(string2, stringBuilder, textRenderer);
            textRenderer.render(string3, stringBuilder, "Text");
        }
    }

    private static void renderSuffix(String string, StringBuilder stringBuilder, TextRenderer textRenderer) {
        if (!string.isEmpty()) {
            textRenderer.render(" ", stringBuilder, "Suffix");
            textRenderer.render(string, stringBuilder, "Suffix");
        }
    }

    private static void appendSuppressedCount(StringBuilder stringBuilder, String string, int n, TextRenderer textRenderer, String string2, String string3) {
        textRenderer.render(string, stringBuilder, "Prefix");
        if (n == 1) {
            textRenderer.render("\t... ", stringBuilder, "Suppressed");
        } else {
            textRenderer.render("\t... suppressed ", stringBuilder, "Suppressed");
            textRenderer.render(Integer.toString(n), stringBuilder, "Suppressed");
            textRenderer.render(" lines", stringBuilder, "Suppressed");
        }
        ThrowableProxyRenderer.renderSuffix(string2, stringBuilder, textRenderer);
        textRenderer.render(string3, stringBuilder, "Text");
    }

    private static void formatEntry(ExtendedStackTraceElement extendedStackTraceElement, StringBuilder stringBuilder, String string, TextRenderer textRenderer, String string2, String string3) {
        textRenderer.render(string, stringBuilder, "Prefix");
        textRenderer.render("\tat ", stringBuilder, "At");
        extendedStackTraceElement.renderOn(stringBuilder, textRenderer);
        ThrowableProxyRenderer.renderSuffix(string2, stringBuilder, textRenderer);
        textRenderer.render(string3, stringBuilder, "Text");
    }

    private static boolean ignoreElement(StackTraceElement object, List<String> object2) {
        if (object2 != null) {
            object = ((StackTraceElement)object).getClassName();
            object2 = object2.iterator();
            while (object2.hasNext()) {
                String string = (String)object2.next();
                if (!((String)object).startsWith(string)) continue;
                return true;
            }
        }
        return false;
    }

    static void formatExtendedStackTraceTo(ThrowableProxy throwableProxy, StringBuilder stringBuilder, List<String> list, TextRenderer textRenderer, String string, String string2) {
        textRenderer.render(throwableProxy.getName(), stringBuilder, "Name");
        textRenderer.render(": ", stringBuilder, "NameMessageSeparator");
        textRenderer.render(throwableProxy.getMessage(), stringBuilder, "Message");
        ThrowableProxyRenderer.renderSuffix(string, stringBuilder, textRenderer);
        textRenderer.render(string2, stringBuilder, "Text");
        StackTraceElement[] stackTraceElementArray = throwableProxy.getThrowable() != null ? throwableProxy.getThrowable().getStackTrace() : null;
        ThrowableProxyRenderer.formatElements(stringBuilder, "", 0, stackTraceElementArray, throwableProxy.getExtendedStackTrace(), list, textRenderer, string, string2);
        ThrowableProxyRenderer.formatSuppressed(stringBuilder, TAB, throwableProxy.getSuppressedProxies(), list, textRenderer, string, string2);
        ThrowableProxyRenderer.formatCause(stringBuilder, "", throwableProxy.getCauseProxy(), list, textRenderer, string, string2);
    }

    static void formatCauseStackTrace(ThrowableProxy throwableProxy, StringBuilder stringBuilder, List<String> list, TextRenderer textRenderer, String string, String string2) {
        ThrowableProxy throwableProxy2 = throwableProxy.getCauseProxy();
        if (throwableProxy2 != null) {
            ThrowableProxyRenderer.formatWrapper(stringBuilder, throwableProxy2, list, textRenderer, string, string2);
            stringBuilder.append(WRAPPED_BY_LABEL);
            ThrowableProxyRenderer.renderSuffix(string, stringBuilder, textRenderer);
        }
        ThrowableProxyRenderer.renderOn(throwableProxy, stringBuilder, textRenderer);
        ThrowableProxyRenderer.renderSuffix(string, stringBuilder, textRenderer);
        textRenderer.render(string2, stringBuilder, "Text");
        ThrowableProxyRenderer.formatElements(stringBuilder, "", 0, throwableProxy.getStackTrace(), throwableProxy.getExtendedStackTrace(), list, textRenderer, string, string2);
    }

    private static void renderOn(ThrowableProxy throwableProxy, StringBuilder stringBuilder, TextRenderer textRenderer) {
        String string = throwableProxy.getMessage();
        textRenderer.render(throwableProxy.getName(), stringBuilder, "Name");
        if (string != null) {
            textRenderer.render(": ", stringBuilder, "NameMessageSeparator");
            textRenderer.render(string, stringBuilder, "Message");
        }
    }
}

