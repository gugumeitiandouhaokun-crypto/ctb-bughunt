/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.lookup;

import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.AbstractLookup;

@Plugin(name="marker", category="Lookup")
public class MarkerLookup
extends AbstractLookup {
    static final String MARKER = "marker";

    @Override
    public String lookup(LogEvent serializable, String string) {
        if ((serializable = serializable == null ? null : serializable.getMarker()) == null) {
            return null;
        }
        return serializable.getName();
    }

    @Override
    public String lookup(String string) {
        if (MarkerManager.exists(string)) {
            return string;
        }
        return null;
    }
}

