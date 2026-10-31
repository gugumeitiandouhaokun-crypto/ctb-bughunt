/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.spi;

import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.AbstractLogger;

@Deprecated
public class LoggerContextKey {
    public static String create(String string) {
        return LoggerContextKey.create(string, AbstractLogger.DEFAULT_MESSAGE_FACTORY_CLASS);
    }

    public static String create(String string, MessageFactory object) {
        object = object != null ? object.getClass() : AbstractLogger.DEFAULT_MESSAGE_FACTORY_CLASS;
        return LoggerContextKey.create(string, (Class<? extends MessageFactory>)object);
    }

    public static String create(String string, Class<? extends MessageFactory> clazz) {
        clazz = clazz != null ? clazz : AbstractLogger.DEFAULT_MESSAGE_FACTORY_CLASS;
        return string + "." + clazz.getName();
    }
}

