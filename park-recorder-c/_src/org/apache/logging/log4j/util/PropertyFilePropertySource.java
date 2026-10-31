/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.LowLevelLogUtil;
import org.apache.logging.log4j.util.PropertiesPropertySource;

public class PropertyFilePropertySource
extends PropertiesPropertySource {
    public PropertyFilePropertySource(String string) {
        super(PropertyFilePropertySource.loadPropertiesFile(string));
    }

    private static Properties loadPropertiesFile(String object) {
        Properties properties = new Properties();
        for (URL uRL : LoaderUtil.findResources((String)object)) {
            try {
                InputStream inputStream = uRL.openStream();
                Throwable throwable = null;
                try {
                    properties.load(inputStream);
                    if (inputStream == null) continue;
                }
                catch (Throwable throwable2) {
                    try {
                        Throwable throwable3 = throwable2;
                        throwable = throwable2;
                        throw throwable3;
                    }
                    catch (Throwable throwable4) {
                        if (inputStream != null) {
                            if (throwable != null) {
                                try {
                                    inputStream.close();
                                }
                                catch (Throwable throwable5) {
                                    throwable.addSuppressed(throwable5);
                                }
                            } else {
                                inputStream.close();
                            }
                        }
                        throw throwable4;
                    }
                }
                inputStream.close();
            }
            catch (IOException iOException) {
                LowLevelLogUtil.logException("Unable to read ".concat(String.valueOf(uRL)), iOException);
            }
        }
        return properties;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}

