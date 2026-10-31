/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.ConfigurationSource;

public class NullConfiguration
extends AbstractConfiguration {
    public static final String NULL_NAME = "Null";

    public NullConfiguration() {
        super(null, ConfigurationSource.NULL_SOURCE);
        this.setName(NULL_NAME);
        this.getRootLogger().setLevel(Level.OFF);
    }
}

