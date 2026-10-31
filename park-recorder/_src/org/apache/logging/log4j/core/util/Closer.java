/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import org.apache.logging.log4j.status.StatusLogger;

public final class Closer {
    private Closer() {
    }

    public static boolean close(AutoCloseable autoCloseable) {
        if (autoCloseable != null) {
            StatusLogger.getLogger().debug("Closing {} {}", (Object)autoCloseable.getClass().getSimpleName(), (Object)autoCloseable);
            autoCloseable.close();
            return true;
        }
        return false;
    }

    public static boolean closeSilently(AutoCloseable autoCloseable) {
        try {
            return Closer.close(autoCloseable);
        }
        catch (Exception exception) {
            return false;
        }
    }
}

