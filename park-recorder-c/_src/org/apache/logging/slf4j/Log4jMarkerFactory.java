/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.slf4j.Log4jMarker;
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;

public class Log4jMarkerFactory
implements IMarkerFactory {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final ConcurrentMap<String, Marker> markerMap = new ConcurrentHashMap<String, Marker>();

    @Override
    public Marker getMarker(String string) {
        if (string == null) {
            throw new IllegalArgumentException("Marker name must not be null");
        }
        Serializable serializable = (Marker)this.markerMap.get(string);
        if (serializable != null) {
            return serializable;
        }
        serializable = MarkerManager.getMarker(string);
        return this.addMarkerIfAbsent(string, (org.apache.logging.log4j.Marker)serializable);
    }

    private Marker addMarkerIfAbsent(String object, org.apache.logging.log4j.Marker serializable) {
        if ((object = this.markerMap.putIfAbsent((String)object, (Marker)(serializable = new Log4jMarker((org.apache.logging.log4j.Marker)serializable)))) == null) {
            return serializable;
        }
        return object;
    }

    public Marker getMarker(Marker marker) {
        if (marker == null) {
            throw new IllegalArgumentException("Marker must not be null");
        }
        Marker marker2 = (Marker)this.markerMap.get(marker.getName());
        if (marker2 != null) {
            return marker2;
        }
        return this.addMarkerIfAbsent(marker.getName(), Log4jMarkerFactory.convertMarker(marker));
    }

    private static org.apache.logging.log4j.Marker convertMarker(Marker marker) {
        if (marker == null) {
            throw new IllegalArgumentException("Marker must not be null");
        }
        return Log4jMarkerFactory.convertMarker(marker, new ArrayList<Marker>());
    }

    private static org.apache.logging.log4j.Marker convertMarker(Marker object, Collection<Marker> collection) {
        org.apache.logging.log4j.Marker marker = MarkerManager.getMarker(object.getName());
        if (object.hasReferences()) {
            object = object.iterator();
            while (object.hasNext()) {
                Marker marker2 = (Marker)object.next();
                if (collection.contains(marker2)) {
                    LOGGER.warn("Found a cycle in Marker [{}]. Cycle will be broken.", (Object)marker2.getName());
                    continue;
                }
                collection.add(marker2);
                marker.addParents(Log4jMarkerFactory.convertMarker(marker2, collection));
            }
        }
        return marker;
    }

    @Override
    public boolean exists(String string) {
        return this.markerMap.containsKey(string);
    }

    @Override
    public boolean detachMarker(String string) {
        return false;
    }

    @Override
    public Marker getDetachedMarker(String string) {
        LOGGER.warn("Log4j does not support detached Markers. Returned Marker [{}] will be unchanged.", (Object)string);
        return this.getMarker(string);
    }
}

