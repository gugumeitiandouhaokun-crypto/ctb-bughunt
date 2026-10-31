/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.util.Map;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.LowLevelLogUtil;
import org.apache.logging.log4j.util.PropertySource;

public class EnvironmentPropertySource
implements PropertySource {
    private static final String PREFIX = "LOG4J_";
    private static final int DEFAULT_PRIORITY = -100;

    @Override
    public int getPriority() {
        return -100;
    }

    @Override
    public void forEach(BiConsumer<String, String> biConsumer) {
        Map<String, String> map;
        try {
            map = System.getenv();
        }
        catch (SecurityException securityException) {
            LowLevelLogUtil.logException("The system environment variables are not available to Log4j due to security restrictions: ".concat(String.valueOf(securityException)), securityException);
            return;
        }
        for (Map.Entry entry : map.entrySet()) {
            String string = (String)entry.getKey();
            if (!string.startsWith(PREFIX)) continue;
            biConsumer.accept(string.substring(6), (String)entry.getValue());
        }
    }

    @Override
    public CharSequence getNormalForm(Iterable<? extends CharSequence> object) {
        StringBuilder stringBuilder = new StringBuilder("LOG4J");
        object = object.iterator();
        while (object.hasNext()) {
            CharSequence charSequence = (CharSequence)object.next();
            stringBuilder.append('_');
            for (int i2 = 0; i2 < charSequence.length(); ++i2) {
                stringBuilder.append(Character.toUpperCase(charSequence.charAt(i2)));
            }
        }
        return stringBuilder.toString();
    }
}

