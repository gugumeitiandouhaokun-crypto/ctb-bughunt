/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.lookup;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.AbstractConfigurationAwareLookup;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="log4j", category="Lookup")
public class Log4jLookup
extends AbstractConfigurationAwareLookup {
    public static final String KEY_CONFIG_LOCATION = "configLocation";
    public static final String KEY_CONFIG_PARENT_LOCATION = "configParentLocation";
    private static final Logger LOGGER = StatusLogger.getLogger();

    private static String asPath(URI uRI) {
        if (uRI.getScheme() == null || uRI.getScheme().equals("file")) {
            return uRI.getPath();
        }
        return uRI.toString();
    }

    private static URI getParent(URI object) {
        int n = ((String)(object = ((URI)object).toString())).lastIndexOf(47);
        if (n >= 0) {
            return new URI(((String)object).substring(0, n));
        }
        return new URI("../");
    }

    @Override
    public String lookup(LogEvent object, String string) {
        if (this.configuration != null) {
            object = this.configuration.getConfigurationSource();
            File file = ((ConfigurationSource)object).getFile();
            if (file != null) {
                switch (string) {
                    case "configLocation": {
                        return file.getAbsolutePath();
                    }
                    case "configParentLocation": {
                        return file.getParentFile().getAbsolutePath();
                    }
                }
                return null;
            }
            if ((object = ((ConfigurationSource)object).getURL()) != null) {
                try {
                    int n = -1;
                    switch (string.hashCode()) {
                        case -1277483753: {
                            if (!string.equals(KEY_CONFIG_LOCATION)) break;
                            n = 0;
                            break;
                        }
                        case -1024117151: {
                            if (!string.equals(KEY_CONFIG_PARENT_LOCATION)) break;
                            n = 1;
                        }
                    }
                    switch (n) {
                        case 0: {
                            return Log4jLookup.asPath(((URL)object).toURI());
                        }
                        case 1: {
                            return Log4jLookup.asPath(Log4jLookup.getParent(((URL)object).toURI()));
                        }
                    }
                    return null;
                }
                catch (URISyntaxException uRISyntaxException) {
                    LOGGER.error(uRISyntaxException);
                    return null;
                }
            }
        }
        return null;
    }
}

