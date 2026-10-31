/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.builder.impl;

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.AppenderRefComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.FilterComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultComponentAndConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultConfigurationBuilder;

class DefaultLoggerComponentBuilder
extends DefaultComponentAndConfigurationBuilder<LoggerComponentBuilder>
implements LoggerComponentBuilder {
    public DefaultLoggerComponentBuilder(DefaultConfigurationBuilder<? extends Configuration> defaultConfigurationBuilder, String string, String string2) {
        super(defaultConfigurationBuilder, string, "Logger");
        if (string2 != null) {
            this.addAttribute("level", string2);
        }
    }

    public DefaultLoggerComponentBuilder(DefaultConfigurationBuilder<? extends Configuration> defaultConfigurationBuilder, String string, String string2, boolean bl) {
        super(defaultConfigurationBuilder, string, "Logger");
        if (string2 != null) {
            this.addAttribute("level", string2);
        }
        this.addAttribute("includeLocation", bl);
    }

    public DefaultLoggerComponentBuilder(DefaultConfigurationBuilder<? extends Configuration> defaultConfigurationBuilder, String string, String string2, String string3) {
        super(defaultConfigurationBuilder, string, string3);
        if (string2 != null) {
            this.addAttribute("level", string2);
        }
    }

    public DefaultLoggerComponentBuilder(DefaultConfigurationBuilder<? extends Configuration> defaultConfigurationBuilder, String string, String string2, String string3, boolean bl) {
        super(defaultConfigurationBuilder, string, string3);
        if (string2 != null) {
            this.addAttribute("level", string2);
        }
        this.addAttribute("includeLocation", bl);
    }

    @Override
    public LoggerComponentBuilder add(AppenderRefComponentBuilder appenderRefComponentBuilder) {
        return (LoggerComponentBuilder)this.addComponent(appenderRefComponentBuilder);
    }

    @Override
    public LoggerComponentBuilder add(FilterComponentBuilder filterComponentBuilder) {
        return (LoggerComponentBuilder)this.addComponent(filterComponentBuilder);
    }
}

