/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.text;

import java.util.Map;

public abstract class StrLookup<V> {
    private static final StrLookup<String> NONE_LOOKUP = new MapStrLookup<String>(null);
    private static final StrLookup<String> SYSTEM_PROPERTIES_LOOKUP = new SystemPropertiesStrLookup();

    public static StrLookup<?> noneLookup() {
        return NONE_LOOKUP;
    }

    public static StrLookup<String> systemPropertiesLookup() {
        return SYSTEM_PROPERTIES_LOOKUP;
    }

    public static <V> StrLookup<V> mapLookup(Map<String, V> map) {
        return new MapStrLookup<V>(map);
    }

    protected StrLookup() {
    }

    public abstract String lookup(String var1);

    static class SystemPropertiesStrLookup
    extends StrLookup<String> {
        private SystemPropertiesStrLookup() {
        }

        @Override
        public String lookup(String string) {
            if (string.length() > 0) {
                try {
                    return System.getProperty(string);
                }
                catch (SecurityException securityException) {}
            }
            return null;
        }
    }

    static class MapStrLookup<V>
    extends StrLookup<V> {
        private final Map<String, V> map;

        MapStrLookup(Map<String, V> map) {
            this.map = map;
        }

        @Override
        public String lookup(String string) {
            if (this.map == null) {
                return null;
            }
            if ((string = this.map.get(string)) == null) {
                return null;
            }
            return string.toString();
        }
    }
}

