/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.spi;

import java.net.URI;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.spi.Terminable;

public interface LoggerContextFactory {
    default public void shutdown(String object, ClassLoader classLoader, boolean bl, boolean bl2) {
        if (this.hasContext((String)object, classLoader, bl) && (object = this.getContext((String)object, classLoader, null, bl)) instanceof Terminable) {
            ((Terminable)object).terminate();
        }
    }

    default public boolean hasContext(String string, ClassLoader classLoader, boolean bl) {
        return false;
    }

    public LoggerContext getContext(String var1, ClassLoader var2, Object var3, boolean var4);

    public LoggerContext getContext(String var1, ClassLoader var2, Object var3, boolean var4, URI var5, String var6);

    public void removeContext(LoggerContext var1);
}

