/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.lookup;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.StructuredDataMessage;

@Plugin(name="sd", category="Lookup")
public class StructuredDataLookup
implements StrLookup {
    @Override
    public String lookup(String string) {
        return null;
    }

    @Override
    public String lookup(LogEvent serializable, String string) {
        if (serializable == null || !(serializable.getMessage() instanceof StructuredDataMessage)) {
            return null;
        }
        serializable = (StructuredDataMessage)serializable.getMessage();
        if (string.equalsIgnoreCase("id")) {
            return ((StructuredDataMessage)serializable).getId().getName();
        }
        if (string.equalsIgnoreCase("type")) {
            return ((StructuredDataMessage)serializable).getType();
        }
        return ((MapMessage)serializable).get(string);
    }
}

