/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import java.io.Serializable;
import org.apache.logging.log4j.core.impl.ExtendedClassInfo;
import org.apache.logging.log4j.core.pattern.PlainTextRenderer;
import org.apache.logging.log4j.core.pattern.TextRenderer;

public final class ExtendedStackTraceElement
implements Serializable {
    private static final long serialVersionUID = -2171069569241280505L;
    private final ExtendedClassInfo extraClassInfo;
    private final StackTraceElement stackTraceElement;

    public ExtendedStackTraceElement(StackTraceElement stackTraceElement, ExtendedClassInfo extendedClassInfo) {
        this.stackTraceElement = stackTraceElement;
        this.extraClassInfo = extendedClassInfo;
    }

    public ExtendedStackTraceElement(String string, String string2, String string3, int n, boolean bl, String string4, String string5) {
        this(new StackTraceElement(string, string2, string3, n), new ExtendedClassInfo(bl, string4, string5));
    }

    public final boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (!(object instanceof ExtendedStackTraceElement)) {
            return false;
        }
        object = (ExtendedStackTraceElement)object;
        if (this.extraClassInfo == null ? ((ExtendedStackTraceElement)object).extraClassInfo != null : !this.extraClassInfo.equals(((ExtendedStackTraceElement)object).extraClassInfo)) {
            return false;
        }
        return !(this.stackTraceElement == null ? ((ExtendedStackTraceElement)object).stackTraceElement != null : !this.stackTraceElement.equals(((ExtendedStackTraceElement)object).stackTraceElement));
    }

    public final String getClassName() {
        return this.stackTraceElement.getClassName();
    }

    public final boolean getExact() {
        return this.extraClassInfo.getExact();
    }

    public final ExtendedClassInfo getExtraClassInfo() {
        return this.extraClassInfo;
    }

    public final String getFileName() {
        return this.stackTraceElement.getFileName();
    }

    public final int getLineNumber() {
        return this.stackTraceElement.getLineNumber();
    }

    public final String getLocation() {
        return this.extraClassInfo.getLocation();
    }

    public final String getMethodName() {
        return this.stackTraceElement.getMethodName();
    }

    public final StackTraceElement getStackTraceElement() {
        return this.stackTraceElement;
    }

    public final String getVersion() {
        return this.extraClassInfo.getVersion();
    }

    public final int hashCode() {
        int n = 31 + (this.extraClassInfo == null ? 0 : this.extraClassInfo.hashCode());
        return 31 * n + (this.stackTraceElement == null ? 0 : this.stackTraceElement.hashCode());
    }

    public final boolean isNativeMethod() {
        return this.stackTraceElement.isNativeMethod();
    }

    final void renderOn(StringBuilder stringBuilder, TextRenderer textRenderer) {
        ExtendedStackTraceElement extendedStackTraceElement = this;
        extendedStackTraceElement.render(extendedStackTraceElement.stackTraceElement, stringBuilder, textRenderer);
        textRenderer.render(" ", stringBuilder, "Text");
        this.extraClassInfo.renderOn(stringBuilder, textRenderer);
    }

    private void render(StackTraceElement stackTraceElement, StringBuilder stringBuilder, TextRenderer textRenderer) {
        String string = stackTraceElement.getFileName();
        int n = stackTraceElement.getLineNumber();
        textRenderer.render(this.getClassName(), stringBuilder, "StackTraceElement.ClassName");
        textRenderer.render(".", stringBuilder, "StackTraceElement.ClassMethodSeparator");
        textRenderer.render(stackTraceElement.getMethodName(), stringBuilder, "StackTraceElement.MethodName");
        if (stackTraceElement.isNativeMethod()) {
            textRenderer.render("(Native Method)", stringBuilder, "StackTraceElement.NativeMethod");
            return;
        }
        if (string != null && n >= 0) {
            textRenderer.render("(", stringBuilder, "StackTraceElement.Container");
            textRenderer.render(string, stringBuilder, "StackTraceElement.FileName");
            textRenderer.render(":", stringBuilder, "StackTraceElement.ContainerSeparator");
            textRenderer.render(Integer.toString(n), stringBuilder, "StackTraceElement.LineNumber");
            textRenderer.render(")", stringBuilder, "StackTraceElement.Container");
            return;
        }
        if (string != null) {
            textRenderer.render("(", stringBuilder, "StackTraceElement.Container");
            textRenderer.render(string, stringBuilder, "StackTraceElement.FileName");
            textRenderer.render(")", stringBuilder, "StackTraceElement.Container");
            return;
        }
        textRenderer.render("(", stringBuilder, "StackTraceElement.Container");
        textRenderer.render("Unknown Source", stringBuilder, "StackTraceElement.UnknownSource");
        textRenderer.render(")", stringBuilder, "StackTraceElement.Container");
    }

    public final String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        this.renderOn(stringBuilder, PlainTextRenderer.getInstance());
        return stringBuilder.toString();
    }
}

