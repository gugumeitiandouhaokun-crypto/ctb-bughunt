/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import java.io.Serializable;
import org.apache.logging.log4j.core.pattern.PlainTextRenderer;
import org.apache.logging.log4j.core.pattern.TextRenderer;

public final class ExtendedClassInfo
implements Serializable {
    private static final long serialVersionUID = 1L;
    private final boolean exact;
    private final String location;
    private final String version;

    public ExtendedClassInfo(boolean bl, String string, String string2) {
        this.exact = bl;
        this.location = string;
        this.version = string2;
    }

    public final boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (!(object instanceof ExtendedClassInfo)) {
            return false;
        }
        object = (ExtendedClassInfo)object;
        if (this.exact != ((ExtendedClassInfo)object).exact) {
            return false;
        }
        if (this.location == null ? ((ExtendedClassInfo)object).location != null : !this.location.equals(((ExtendedClassInfo)object).location)) {
            return false;
        }
        return !(this.version == null ? ((ExtendedClassInfo)object).version != null : !this.version.equals(((ExtendedClassInfo)object).version));
    }

    public final boolean getExact() {
        return this.exact;
    }

    public final String getLocation() {
        return this.location;
    }

    public final String getVersion() {
        return this.version;
    }

    public final int hashCode() {
        int n = 31 + (this.exact ? 1231 : 1237);
        n = 31 * n + (this.location == null ? 0 : this.location.hashCode());
        return 31 * n + (this.version == null ? 0 : this.version.hashCode());
    }

    public final void renderOn(StringBuilder stringBuilder, TextRenderer textRenderer) {
        if (!this.exact) {
            textRenderer.render("~", stringBuilder, "ExtraClassInfo.Inexact");
        }
        textRenderer.render("[", stringBuilder, "ExtraClassInfo.Container");
        textRenderer.render(this.location, stringBuilder, "ExtraClassInfo.Location");
        textRenderer.render(":", stringBuilder, "ExtraClassInfo.ContainerSeparator");
        textRenderer.render(this.version, stringBuilder, "ExtraClassInfo.Version");
        textRenderer.render("]", stringBuilder, "ExtraClassInfo.Container");
    }

    public final String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        this.renderOn(stringBuilder, PlainTextRenderer.getInstance());
        return stringBuilder.toString();
    }
}

