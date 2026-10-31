/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

public class ConfigurationException
extends RuntimeException {
    private static final long serialVersionUID = -2413951820300775294L;

    public ConfigurationException(String string) {
        super(string);
    }

    public ConfigurationException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public ConfigurationException(Throwable throwable) {
        super(throwable);
    }
}

