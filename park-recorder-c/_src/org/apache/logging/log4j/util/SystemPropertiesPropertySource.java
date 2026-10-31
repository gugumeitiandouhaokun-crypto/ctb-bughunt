/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.util.Objects;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.PropertySource;

public class SystemPropertiesPropertySource
implements PropertySource {
    private static final int DEFAULT_PRIORITY = 100;
    private static final String PREFIX = "log4j2.";

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void forEach(BiConsumer<String, String> biConsumer) {
        Object[] objectArray;
        try {
            objectArray = System.getProperties();
        }
        catch (SecurityException securityException) {
            return;
        }
        Object[] objectArray2 = objectArray;
        synchronized (objectArray) {
            Object[] objectArray3 = objectArray.keySet().toArray();
            // ** MonitorExit[var4_3] (shouldn't be in output)
            objectArray2 = objectArray3;
            int n = objectArray3.length;
            for (int i2 = 0; i2 < n; ++i2) {
                String string = Objects.toString(objectArray2[i2], null);
                biConsumer.accept(string, objectArray.getProperty(string));
            }
            return;
        }
    }

    @Override
    public CharSequence getNormalForm(Iterable<? extends CharSequence> iterable) {
        return PREFIX + PropertySource.Util.joinAsCamelCase(iterable);
    }
}

