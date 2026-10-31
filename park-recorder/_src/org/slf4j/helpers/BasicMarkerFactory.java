/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j.helpers;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarker;

public class BasicMarkerFactory
implements IMarkerFactory {
    private final ConcurrentMap<String, Marker> markerMap = new ConcurrentHashMap<String, Marker>();

    public Marker getMarker(String object) {
        if (object == null) {
            throw new IllegalArgumentException("Marker name cannot be null");
        }
        Object object2 = (Marker)this.markerMap.get(object);
        if (object2 == null && (object = this.markerMap.putIfAbsent((String)object, (Marker)(object2 = new BasicMarker((String)object)))) != null) {
            object2 = object;
        }
        return object2;
    }

    public boolean exists(String string) {
        if (string == null) {
            return false;
        }
        return this.markerMap.containsKey(string);
    }

    public boolean detachMarker(String string) {
        if (string == null) {
            return false;
        }
        return this.markerMap.remove(string) != null;
    }

    public Marker getDetachedMarker(String string) {
        return new BasicMarker(string);
    }
}

