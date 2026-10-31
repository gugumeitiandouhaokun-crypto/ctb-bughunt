/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.lookup;

import java.util.Objects;
import javax.naming.NamingException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.AbstractLookup;
import org.apache.logging.log4j.core.net.JndiManager;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="jndi", category="Lookup")
public class JndiLookup
extends AbstractLookup {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final Marker LOOKUP = MarkerManager.getMarker("LOOKUP");
    static final String CONTAINER_JNDI_RESOURCE_PATH_PREFIX = "java:comp/env/";

    @Override
    public String lookup(LogEvent object, String object2) {
        String string;
        block13: {
            if (object2 == null) {
                return null;
            }
            object = this.convertJndiName((String)object2);
            object2 = JndiManager.getDefaultManager();
            Throwable throwable = null;
            try {
                string = Objects.toString(((JndiManager)object2).lookup((String)object), null);
                if (object2 == null) break block13;
            }
            catch (Throwable throwable2) {
                try {
                    try {
                        Throwable throwable3 = throwable2;
                        throwable = throwable2;
                        throw throwable3;
                    }
                    catch (Throwable throwable4) {
                        if (object2 != null) {
                            if (throwable != null) {
                                try {
                                    ((AbstractManager)object2).close();
                                }
                                catch (Throwable throwable5) {
                                    throwable.addSuppressed(throwable5);
                                }
                            } else {
                                ((AbstractManager)object2).close();
                            }
                        }
                        throw throwable4;
                    }
                }
                catch (NamingException namingException) {
                    LOGGER.warn(LOOKUP, "Error looking up JNDI resource [{}].", object, (Object)namingException);
                    return null;
                }
            }
            ((AbstractManager)object2).close();
        }
        return string;
    }

    private String convertJndiName(String string) {
        if (!string.startsWith(CONTAINER_JNDI_RESOURCE_PATH_PREFIX) && string.indexOf(58) == -1) {
            return CONTAINER_JNDI_RESOURCE_PATH_PREFIX.concat(String.valueOf(string));
        }
        return string;
    }
}

