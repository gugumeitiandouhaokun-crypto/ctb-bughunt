/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.Module$SetupContext
 *  com.fasterxml.jackson.databind.module.SimpleModule
 *  com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import org.apache.logging.log4j.core.jackson.Initializers;

final class Log4jXmlModule
extends JacksonXmlModule {
    private static final long serialVersionUID = 1L;
    private final boolean includeStacktrace;
    private final boolean stacktraceAsString;

    Log4jXmlModule(boolean bl, boolean bl2) {
        this.includeStacktrace = bl;
        this.stacktraceAsString = bl2;
        new Initializers.SimpleModuleInitializer().initialize((SimpleModule)this, false);
    }

    public final void setupModule(Module.SetupContext setupContext) {
        super.setupModule(setupContext);
        new Initializers.SetupContextInitializer().setupModule(setupContext, this.includeStacktrace, this.stacktraceAsString);
    }
}

