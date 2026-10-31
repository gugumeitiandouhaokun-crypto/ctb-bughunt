/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.builder.impl;

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.FilterComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultComponentAndConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultConfigurationBuilder;

class DefaultAppenderComponentBuilder
extends DefaultComponentAndConfigurationBuilder<AppenderComponentBuilder>
implements AppenderComponentBuilder {
    public DefaultAppenderComponentBuilder(DefaultConfigurationBuilder<? extends Configuration> defaultConfigurationBuilder, String string, String string2) {
        super(defaultConfigurationBuilder, string, string2);
    }

    @Override
    public AppenderComponentBuilder add(LayoutComponentBuilder layoutComponentBuilder) {
        return (AppenderComponentBuilder)this.addComponent(layoutComponentBuilder);
    }

    @Override
    public AppenderComponentBuilder add(FilterComponentBuilder filterComponentBuilder) {
        return (AppenderComponentBuilder)this.addComponent(filterComponentBuilder);
    }
}

