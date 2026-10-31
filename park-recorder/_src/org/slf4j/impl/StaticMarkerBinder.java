/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j.impl;

import org.apache.logging.slf4j.Log4jMarkerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.spi.MarkerFactoryBinder;

public class StaticMarkerBinder
implements MarkerFactoryBinder {
    public static final StaticMarkerBinder SINGLETON = new StaticMarkerBinder();
    private final IMarkerFactory markerFactory = new Log4jMarkerFactory();

    @Override
    public IMarkerFactory getMarkerFactory() {
        return this.markerFactory;
    }

    @Override
    public String getMarkerFactoryClassStr() {
        return Log4jMarkerFactory.class.getName();
    }
}

