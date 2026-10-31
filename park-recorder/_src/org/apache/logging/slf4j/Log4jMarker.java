/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import org.apache.logging.log4j.MarkerManager;
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.impl.StaticMarkerBinder;

public class Log4jMarker
implements Marker {
    public static final long serialVersionUID = 1590472L;
    private final IMarkerFactory factory = StaticMarkerBinder.SINGLETON.getMarkerFactory();
    private final org.apache.logging.log4j.Marker marker;

    public Log4jMarker(org.apache.logging.log4j.Marker marker) {
        this.marker = marker;
    }

    @Override
    public void add(Marker marker) {
        if (marker == null) {
            throw new IllegalArgumentException();
        }
        marker = this.factory.getMarker(marker.getName());
        this.marker.addParents(((Log4jMarker)marker).getLog4jMarker());
    }

    @Override
    public boolean contains(Marker marker) {
        if (marker == null) {
            throw new IllegalArgumentException();
        }
        return this.marker.isInstanceOf(marker.getName());
    }

    @Override
    public boolean contains(String string) {
        if (string != null) {
            return this.marker.isInstanceOf(string);
        }
        return false;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (!(object instanceof Log4jMarker)) {
            return false;
        }
        object = (Log4jMarker)object;
        return !(this.marker == null ? ((Log4jMarker)object).marker != null : !this.marker.equals(((Log4jMarker)object).marker));
    }

    public org.apache.logging.log4j.Marker getLog4jMarker() {
        return this.marker;
    }

    @Override
    public String getName() {
        return this.marker.getName();
    }

    @Override
    public boolean hasChildren() {
        return this.marker.hasParents();
    }

    @Override
    public int hashCode() {
        return 31 + (this.marker == null ? 0 : this.marker.hashCode());
    }

    @Override
    public boolean hasReferences() {
        return this.marker.hasParents();
    }

    @Override
    public Iterator<Marker> iterator() {
        org.apache.logging.log4j.Marker[] markerArray = this.marker.getParents();
        ArrayList<Marker> arrayList = new ArrayList<Marker>(markerArray.length);
        for (org.apache.logging.log4j.Marker marker : markerArray) {
            arrayList.add(this.factory.getMarker(marker.getName()));
        }
        return arrayList.iterator();
    }

    @Override
    public boolean remove(Marker marker) {
        if (marker != null) {
            return this.marker.remove(MarkerManager.getMarker(marker.getName()));
        }
        return false;
    }
}

