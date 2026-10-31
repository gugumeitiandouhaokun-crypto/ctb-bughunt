/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.spi;

import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.ExtendedLogger;

public interface LoggerContext {
    public Object getExternalContext();

    default public Object getObject(String string) {
        return null;
    }

    default public Object putObject(String string, Object object) {
        return null;
    }

    default public Object putObjectIfAbsent(String string, Object object) {
        return null;
    }

    default public Object removeObject(String string) {
        return null;
    }

    default public boolean removeObject(String string, Object object) {
        return false;
    }

    public ExtendedLogger getLogger(String var1);

    public ExtendedLogger getLogger(String var1, MessageFactory var2);

    public boolean hasLogger(String var1);

    public boolean hasLogger(String var1, MessageFactory var2);

    public boolean hasLogger(String var1, Class<? extends MessageFactory> var2);
}

