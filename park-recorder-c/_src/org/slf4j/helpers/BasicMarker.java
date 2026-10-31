/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j.helpers;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Marker;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class BasicMarker
implements Marker {
    private static final long serialVersionUID = -2849567615646933777L;
    private final String name;
    private List<Marker> referenceList = new CopyOnWriteArrayList<Marker>();
    private static String OPEN = "[ ";
    private static String CLOSE = " ]";
    private static String SEP = ", ";

    BasicMarker(String string) {
        if (string == null) {
            throw new IllegalArgumentException("A marker name cannot be null");
        }
        this.name = string;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void add(Marker marker) {
        if (marker == null) {
            throw new IllegalArgumentException("A null value cannot be added to a Marker as reference.");
        }
        if (this.contains(marker)) {
            return;
        }
        if (marker.contains(this)) {
            return;
        }
        this.referenceList.add(marker);
    }

    @Override
    public boolean hasReferences() {
        return this.referenceList.size() > 0;
    }

    @Override
    public boolean hasChildren() {
        return this.hasReferences();
    }

    @Override
    public Iterator<Marker> iterator() {
        return this.referenceList.iterator();
    }

    @Override
    public boolean remove(Marker marker) {
        return this.referenceList.remove(marker);
    }

    @Override
    public boolean contains(Marker marker) {
        if (marker == null) {
            throw new IllegalArgumentException("Other cannot be null");
        }
        if (this.equals(marker)) {
            return true;
        }
        if (this.hasReferences()) {
            Iterator<Marker> iterator = this.referenceList.iterator();
            while (iterator.hasNext()) {
                if (!iterator.next().contains(marker)) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(String string) {
        if (string == null) {
            throw new IllegalArgumentException("Other cannot be null");
        }
        if (this.name.equals(string)) {
            return true;
        }
        if (this.hasReferences()) {
            Iterator<Marker> iterator = this.referenceList.iterator();
            while (iterator.hasNext()) {
                if (!iterator.next().contains(string)) continue;
                return true;
            }
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
        if (!(object instanceof Marker)) {
            return false;
        }
        object = (Marker)object;
        return this.name.equals(object.getName());
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    public String toString() {
        if (!this.hasReferences()) {
            return this.getName();
        }
        Iterator<Marker> iterator = this.iterator();
        StringBuilder stringBuilder = new StringBuilder(this.getName());
        stringBuilder.append(' ').append(OPEN);
        while (iterator.hasNext()) {
            Marker marker = iterator.next();
            stringBuilder.append(marker.getName());
            if (!iterator.hasNext()) continue;
            stringBuilder.append(SEP);
        }
        stringBuilder.append(CLOSE);
        return stringBuilder.toString();
    }
}

