/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.StringBuilderFormattable;

public final class MarkerManager {
    private static final ConcurrentMap<String, Marker> MARKERS = new ConcurrentHashMap<String, Marker>();

    private MarkerManager() {
    }

    public static void clear() {
        MARKERS.clear();
    }

    public static boolean exists(String string) {
        return MARKERS.containsKey(string);
    }

    public static Marker getMarker(String string) {
        Marker marker = (Marker)MARKERS.get(string);
        if (marker == null) {
            MARKERS.putIfAbsent(string, new Log4jMarker(string));
            marker = (Marker)MARKERS.get(string);
        }
        return marker;
    }

    @Deprecated
    public static Marker getMarker(String string, String string2) {
        Marker marker = (Marker)MARKERS.get(string2);
        if (marker == null) {
            throw new IllegalArgumentException("Parent Marker " + string2 + " has not been defined");
        }
        return MarkerManager.getMarker(string, marker);
    }

    @Deprecated
    public static Marker getMarker(String string, Marker marker) {
        return MarkerManager.getMarker(string).addParents(marker);
    }

    private static void requireNonNull(Object object, String string) {
        if (object == null) {
            throw new IllegalArgumentException(string);
        }
    }

    public static class Log4jMarker
    implements Marker,
    StringBuilderFormattable {
        private static final long serialVersionUID = 100L;
        private final String name;
        private volatile Marker[] parents;

        private Log4jMarker() {
            this.name = null;
            this.parents = null;
        }

        public Log4jMarker(String string) {
            MarkerManager.requireNonNull(string, "Marker name cannot be null.");
            this.name = string;
            this.parents = null;
        }

        @Override
        public synchronized Marker addParents(Marker ... markerArray) {
            int n;
            MarkerManager.requireNonNull(markerArray, "A parent marker must be specified");
            Marker[] markerArray2 = this.parents;
            int n2 = 0;
            int n3 = markerArray.length;
            if (markerArray2 != null) {
                Marker[] markerArray3 = markerArray;
                n = markerArray.length;
                for (int j = 0; j < n; ++j) {
                    Marker marker = markerArray3[j];
                    if (Log4jMarker.contains(marker, markerArray2) || marker.isInstanceOf(this)) continue;
                    ++n2;
                }
                if (n2 == 0) {
                    return this;
                }
                n3 = markerArray2.length + n2;
            }
            Marker[] markerArray4 = new Marker[n3];
            if (markerArray2 != null) {
                System.arraycopy(markerArray2, 0, markerArray4, 0, markerArray2.length);
            }
            n = markerArray2 == null ? 0 : markerArray2.length;
            Marker[] markerArray5 = markerArray;
            int n4 = markerArray.length;
            for (int j = 0; j < n4; ++j) {
                Marker marker = markerArray5[j];
                if (markerArray2 != null && (Log4jMarker.contains(marker, markerArray2) || marker.isInstanceOf(this))) continue;
                markerArray4[n++] = marker;
            }
            this.parents = markerArray4;
            return this;
        }

        @Override
        public synchronized boolean remove(Marker marker) {
            MarkerManager.requireNonNull(marker, "A parent marker must be specified");
            Marker[] markerArray = this.parents;
            if (this.parents == null) {
                return false;
            }
            int n = markerArray.length;
            if (n == 1) {
                if (markerArray[0].equals(marker)) {
                    this.parents = null;
                    return true;
                }
                return false;
            }
            int n2 = 0;
            Marker[] markerArray2 = new Marker[n - 1];
            for (int j = 0; j < n; ++j) {
                Marker marker2 = markerArray[j];
                if (marker2.equals(marker)) continue;
                if (n2 == n - 1) {
                    return false;
                }
                markerArray2[n2++] = marker2;
            }
            this.parents = markerArray2;
            return true;
        }

        @Override
        public Marker setParents(Marker ... markerArray) {
            if (markerArray == null || markerArray.length == 0) {
                this.parents = null;
            } else {
                Marker[] markerArray2 = new Marker[markerArray.length];
                System.arraycopy(markerArray, 0, markerArray2, 0, markerArray.length);
                this.parents = markerArray2;
            }
            return this;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public Marker[] getParents() {
            if (this.parents == null) {
                return null;
            }
            return Arrays.copyOf(this.parents, this.parents.length);
        }

        @Override
        public boolean hasParents() {
            return this.parents != null;
        }

        @Override
        @PerformanceSensitive(value={"allocation", "unrolled"})
        public boolean isInstanceOf(Marker marker) {
            MarkerManager.requireNonNull(marker, "A marker parameter is required");
            if (this == marker) {
                return true;
            }
            Marker[] markerArray = this.parents;
            if (this.parents != null) {
                int n = markerArray.length;
                if (n == 1) {
                    return Log4jMarker.checkParent(markerArray[0], marker);
                }
                if (n == 2) {
                    return Log4jMarker.checkParent(markerArray[0], marker) || Log4jMarker.checkParent(markerArray[1], marker);
                }
                for (int j = 0; j < n; ++j) {
                    if (!Log4jMarker.checkParent(markerArray[j], marker)) continue;
                    return true;
                }
            }
            return false;
        }

        @Override
        @PerformanceSensitive(value={"allocation", "unrolled"})
        public boolean isInstanceOf(String object) {
            MarkerManager.requireNonNull(object, "A marker name is required");
            if (((String)object).equals(this.getName())) {
                return true;
            }
            object = (Marker)MARKERS.get(object);
            if (object == null) {
                return false;
            }
            Marker[] markerArray = this.parents;
            if (this.parents != null) {
                int n = markerArray.length;
                if (n == 1) {
                    return Log4jMarker.checkParent(markerArray[0], (Marker)object);
                }
                if (n == 2) {
                    return Log4jMarker.checkParent(markerArray[0], (Marker)object) || Log4jMarker.checkParent(markerArray[1], (Marker)object);
                }
                for (int j = 0; j < n; ++j) {
                    if (!Log4jMarker.checkParent(markerArray[j], (Marker)object)) continue;
                    return true;
                }
            }
            return false;
        }

        @PerformanceSensitive(value={"allocation", "unrolled"})
        private static boolean checkParent(Marker object, Marker marker) {
            block4: {
                int n;
                while (true) {
                    if (object == marker) {
                        return true;
                    }
                    if ((object = object instanceof Log4jMarker ? ((Log4jMarker)object).parents : object.getParents()) == null) break block4;
                    n = ((Marker[])object).length;
                    if (n != 1) break;
                    object = object[0];
                }
                if (n == 2) {
                    return Log4jMarker.checkParent(object[0], marker) || Log4jMarker.checkParent(object[1], marker);
                }
                for (int j = 0; j < n; ++j) {
                    if (!Log4jMarker.checkParent(object[j], marker)) continue;
                    return true;
                }
            }
            return false;
        }

        @PerformanceSensitive(value={"allocation"})
        private static boolean contains(Marker marker, Marker ... markerArray) {
            int n = markerArray.length;
            for (int j = 0; j < n; ++j) {
                if (markerArray[j] != marker) continue;
                return true;
            }
            return false;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || !(object instanceof Marker)) {
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
            StringBuilder stringBuilder = new StringBuilder();
            this.formatTo(stringBuilder);
            return stringBuilder.toString();
        }

        @Override
        public void formatTo(StringBuilder stringBuilder) {
            stringBuilder.append(this.name);
            Marker[] markerArray = this.parents;
            if (this.parents != null) {
                Log4jMarker.addParentInfo(stringBuilder, markerArray);
            }
        }

        @PerformanceSensitive(value={"allocation"})
        private static void addParentInfo(StringBuilder stringBuilder, Marker ... markerArray) {
            stringBuilder.append("[ ");
            boolean bl = true;
            for (Marker marker : markerArray) {
                if (!bl) {
                    stringBuilder.append(", ");
                }
                bl = false;
                stringBuilder.append(marker.getName());
                Marker[] markerArray2 = marker instanceof Log4jMarker ? ((Log4jMarker)marker).parents : marker.getParents();
                if (markerArray2 == null) continue;
                Log4jMarker.addParentInfo(stringBuilder, markerArray2);
            }
            stringBuilder.append(" ]");
        }
    }
}

