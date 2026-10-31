/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net.ssl;

public class StoreConfigurationException
extends Exception {
    private static final long serialVersionUID = 1L;

    public StoreConfigurationException(Exception exception) {
        super(exception);
    }

    public StoreConfigurationException(String string) {
        super(string);
    }

    public StoreConfigurationException(String string, Exception exception) {
        super(string, exception);
    }
}

