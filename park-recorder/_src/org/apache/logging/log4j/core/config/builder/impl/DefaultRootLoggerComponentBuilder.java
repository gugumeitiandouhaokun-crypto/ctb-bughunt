/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.builder.impl;

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.AppenderRefComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.FilterComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultComponentAndConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultConfigurationBuilder;

class DefaultRootLoggerComponentBuilder
extends DefaultComponentAndConfigurationBuilder<RootLoggerComponentBuilder>
implements RootLoggerComponentBuilder {
    public DefaultRootLoggerComponentBuilder(DefaultConfigurationBuilder<? extends Configuration> defaultConfigurationBuilder, String string) {
        super(defaultConfigurationBuilder, "", "Root");
        if (string != null) {
            this.addAttribute("level", string);
        }
    }

    public DefaultRootLoggerComponentBuilder(DefaultConfigurationBuilder<? extends Configuration> defaultConfigurationBuilder, String string, boolean bl) {
        super(defaultConfigurationBuilder, "", "Root");
        if (string != null) {
            this.addAttribute("level", string);
        }
        this.addAttribute("includeLocation", bl);
    }

    public DefaultRootLoggerComponentBuilder(DefaultConfigurationBuilder<? extends Configuration> defaultConfigurationBuilder, String string, String string2) {
        super(defaultConfigurationBuilder, "", string2);
        if (string != null) {
            this.addAttribute("level", string);
        }
    }

    public DefaultRootLoggerComponentBuilder(DefaultConfigurationBuilder<? extends Configuration> defaultConfigurationBuilder, String string, String string2, boolean bl) {
        super(defaultConfigurationBuilder, "", string2);
        if (string != null) {
            this.addAttribute("level", string);
        }
        this.addAttribute("includeLocation", bl);
    }

    @Override
    public RootLoggerComponentBuilder add(AppenderRefComponentBuilder appenderRefComponentBuilder) {
        return (RootLoggerComponentBuilder)this.addComponent(appenderRefComponentBuilder);
    }

    @Override
    public RootLoggerComponentBuilder add(FilterComponentBuilder filterComponentBuilder) {
        return (RootLoggerComponentBuilder)this.addComponent(filterComponentBuilder);
    }
}

