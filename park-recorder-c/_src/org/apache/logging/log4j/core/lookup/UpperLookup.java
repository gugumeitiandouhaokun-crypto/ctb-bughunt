/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.lookup;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.StrLookup;

@Plugin(name="upper", category="Lookup")
public class UpperLookup
implements StrLookup {
    @Override
    public String lookup(String string) {
        if (string != null) {
            return string.toUpperCase();
        }
        return null;
    }

    @Override
    public String lookup(LogEvent logEvent, String string) {
        return this.lookup(string);
    }
}

