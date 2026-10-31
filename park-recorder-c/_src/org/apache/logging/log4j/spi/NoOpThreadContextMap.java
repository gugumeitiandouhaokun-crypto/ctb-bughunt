/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.spi;

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.spi.ThreadContextMap;

public class NoOpThreadContextMap
implements ThreadContextMap {
    @Override
    public void clear() {
    }

    @Override
    public boolean containsKey(String string) {
        return false;
    }

    @Override
    public String get(String string) {
        return null;
    }

    @Override
    public Map<String, String> getCopy() {
        return new HashMap<String, String>();
    }

    @Override
    public Map<String, String> getImmutableMapOrNull() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void put(String string, String string2) {
    }

    @Override
    public void remove(String string) {
    }
}

