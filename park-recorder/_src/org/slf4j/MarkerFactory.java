/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j;

import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.Util;
import org.slf4j.impl.StaticMarkerBinder;

public class MarkerFactory {
    static IMarkerFactory MARKER_FACTORY;

    private MarkerFactory() {
    }

    private static IMarkerFactory bwCompatibleGetMarkerFactoryFromBinder() {
        try {
            return StaticMarkerBinder.getSingleton().getMarkerFactory();
        }
        catch (NoSuchMethodError noSuchMethodError) {
            return StaticMarkerBinder.SINGLETON.getMarkerFactory();
        }
    }

    public static Marker getMarker(String string) {
        return MARKER_FACTORY.getMarker(string);
    }

    public static Marker getDetachedMarker(String string) {
        return MARKER_FACTORY.getDetachedMarker(string);
    }

    public static IMarkerFactory getIMarkerFactory() {
        return MARKER_FACTORY;
    }

    static {
        try {
            MARKER_FACTORY = MarkerFactory.bwCompatibleGetMarkerFactoryFromBinder();
            return;
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            MARKER_FACTORY = new BasicMarkerFactory();
            return;
        }
        catch (Exception exception) {
            Util.report("Unexpected failure while binding MarkerFactory", exception);
            return;
        }
    }
}

