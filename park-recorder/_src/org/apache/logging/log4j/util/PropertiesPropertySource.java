/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.PropertySource;

public class PropertiesPropertySource
implements PropertySource {
    private static final String PREFIX = "log4j2.";
    private final Properties properties;

    public PropertiesPropertySource(Properties properties) {
        this.properties = properties;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void forEach(BiConsumer<String, String> biConsumer) {
        for (Map.Entry entry : this.properties.entrySet()) {
            biConsumer.accept((String)entry.getKey(), (String)entry.getValue());
        }
    }

    @Override
    public CharSequence getNormalForm(Iterable<? extends CharSequence> iterable) {
        return PREFIX + PropertySource.Util.joinAsCamelCase(iterable);
    }
}

