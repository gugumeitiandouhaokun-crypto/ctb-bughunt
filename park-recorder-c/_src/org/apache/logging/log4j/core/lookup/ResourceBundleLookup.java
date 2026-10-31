/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.lookup;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.AbstractLookup;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="bundle", category="Lookup")
public class ResourceBundleLookup
extends AbstractLookup {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final Marker LOOKUP = MarkerManager.getMarker("LOOKUP");

    @Override
    public String lookup(LogEvent object, String string) {
        if (string == null) {
            return null;
        }
        object = string.split(":");
        if (((String[])object).length != 2) {
            LOGGER.warn(LOOKUP, "Bad ResourceBundle key format [{}]. Expected format is BundleName:KeyName.", (Object)string);
            return null;
        }
        string = object[0];
        object = object[1];
        try {
            return ResourceBundle.getBundle(string).getString((String)object);
        }
        catch (MissingResourceException missingResourceException) {
            LOGGER.warn(LOOKUP, "Error looking up ResourceBundle [{}].", (Object)string, (Object)missingResourceException);
            return null;
        }
    }
}

