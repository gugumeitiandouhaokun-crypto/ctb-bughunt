/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j.impl;

import org.apache.logging.slf4j.Log4jLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

public final class StaticLoggerBinder
implements LoggerFactoryBinder {
    public static String REQUESTED_API_VERSION = "1.6";
    private static final String LOGGER_FACTORY_CLASS_STR = Log4jLoggerFactory.class.getName();
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
    private final ILoggerFactory loggerFactory = new Log4jLoggerFactory();

    private StaticLoggerBinder() {
    }

    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    @Override
    public final ILoggerFactory getLoggerFactory() {
        return this.loggerFactory;
    }

    @Override
    public final String getLoggerFactoryClassStr() {
        return LOGGER_FACTORY_CLASS_STR;
    }
}

