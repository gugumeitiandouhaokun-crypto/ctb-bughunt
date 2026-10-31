/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.builder.impl;

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.KeyValuePairComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultComponentAndConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultConfigurationBuilder;

class DefaultKeyValuePairComponentBuilder
extends DefaultComponentAndConfigurationBuilder<KeyValuePairComponentBuilder>
implements KeyValuePairComponentBuilder {
    public DefaultKeyValuePairComponentBuilder(DefaultConfigurationBuilder<? extends Configuration> defaultConfigurationBuilder, String string, String string2) {
        super(defaultConfigurationBuilder, "KeyValuePair");
        this.addAttribute("key", string);
        this.addAttribute("value", string2);
    }
}

