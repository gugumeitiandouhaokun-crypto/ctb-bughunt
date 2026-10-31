/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.lookup;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.AbstractLookup;

@Plugin(name="event", category="Lookup")
public class EventLookup
extends AbstractLookup {
    @Override
    public String lookup(LogEvent logEvent, String string) {
        int n = -1;
        switch (string.hashCode()) {
            case -1997438886: {
                if (!string.equals("Marker")) break;
                n = 0;
                break;
            }
            case 808305621: {
                if (!string.equals("ThreadName")) break;
                n = 1;
                break;
            }
            case 73313124: {
                if (!string.equals("Level")) break;
                n = 2;
                break;
            }
            case 1538269413: {
                if (!string.equals("ThreadId")) break;
                n = 3;
                break;
            }
            case 2059094262: {
                if (!string.equals("Timestamp")) break;
                n = 4;
                break;
            }
            case -809373649: {
                if (!string.equals("Exception")) break;
                n = 5;
                break;
            }
            case -2013470288: {
                if (!string.equals("Logger")) break;
                n = 6;
                break;
            }
            case -1675388953: {
                if (!string.equals("Message")) break;
                n = 7;
            }
        }
        switch (n) {
            case 0: {
                if (logEvent.getMarker() != null) {
                    return logEvent.getMarker().getName();
                }
                return null;
            }
            case 1: {
                return logEvent.getThreadName();
            }
            case 2: {
                return logEvent.getLevel().toString();
            }
            case 3: {
                return Long.toString(logEvent.getThreadId());
            }
            case 4: {
                return Long.toString(logEvent.getTimeMillis());
            }
            case 5: {
                if (logEvent.getThrown() != null) {
                    return logEvent.getThrown().getClass().getSimpleName();
                }
                if (logEvent.getThrownProxy() != null) {
                    return logEvent.getThrownProxy().getName();
                }
                return null;
            }
            case 6: {
                return logEvent.getLoggerName();
            }
            case 7: {
                return logEvent.getMessage().getFormattedMessage();
            }
        }
        return null;
    }
}

