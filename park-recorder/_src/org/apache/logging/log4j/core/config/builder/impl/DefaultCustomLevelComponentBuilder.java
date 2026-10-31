/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.builder.impl;

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.CustomLevelComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultComponentAndConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultConfigurationBuilder;

class DefaultCustomLevelComponentBuilder
extends DefaultComponentAndConfigurationBuilder<CustomLevelComponentBuilder>
implements CustomLevelComponentBuilder {
    public DefaultCustomLevelComponentBuilder(DefaultConfigurationBuilder<? extends Configuration> defaultConfigurationBuilder, String string, int n) {
        super(defaultConfigurationBuilder, string, "CustomLevel");
        this.addAttribute("intLevel", n);
    }
}

