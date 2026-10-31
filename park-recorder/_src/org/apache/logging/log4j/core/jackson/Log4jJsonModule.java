/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.Version
 *  com.fasterxml.jackson.databind.Module$SetupContext
 *  com.fasterxml.jackson.databind.module.SimpleModule
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.logging.log4j.core.jackson.Initializers;

class Log4jJsonModule
extends SimpleModule {
    private static final long serialVersionUID = 1L;
    private final boolean encodeThreadContextAsList;
    private final boolean includeStacktrace;
    private final boolean stacktraceAsString;
    private final boolean objectMessageAsJsonObject;

    Log4jJsonModule(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        super(Log4jJsonModule.class.getName(), new Version(2, 0, 0, null, null, null));
        this.encodeThreadContextAsList = bl;
        this.includeStacktrace = bl2;
        this.stacktraceAsString = bl3;
        this.objectMessageAsJsonObject = bl4;
        new Initializers.SimpleModuleInitializer().initialize(this, bl4);
    }

    public void setupModule(Module.SetupContext setupContext) {
        super.setupModule(setupContext);
        if (this.encodeThreadContextAsList) {
            new Initializers.SetupContextInitializer().setupModule(setupContext, this.includeStacktrace, this.stacktraceAsString);
            return;
        }
        new Initializers.SetupContextJsonInitializer().setupModule(setupContext, this.includeStacktrace, this.stacktraceAsString);
    }
}

