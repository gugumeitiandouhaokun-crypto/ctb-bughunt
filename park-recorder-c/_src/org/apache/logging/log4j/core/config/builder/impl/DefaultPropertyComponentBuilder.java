/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.builder.impl;

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.PropertyComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultComponentAndConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultConfigurationBuilder;

class DefaultPropertyComponentBuilder
extends DefaultComponentAndConfigurationBuilder<PropertyComponentBuilder>
implements PropertyComponentBuilder {
    public DefaultPropertyComponentBuilder(DefaultConfigurationBuilder<? extends Configuration> defaultConfigurationBuilder, String string, String string2) {
        super(defaultConfigurationBuilder, string, "Property", string2);
    }
}

