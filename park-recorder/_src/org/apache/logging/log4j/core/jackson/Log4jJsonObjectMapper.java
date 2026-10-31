/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.databind.Module
 *  com.fasterxml.jackson.databind.ObjectMapper
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.core.jackson.Log4jJsonModule;

public class Log4jJsonObjectMapper
extends ObjectMapper {
    private static final long serialVersionUID = 1L;

    public Log4jJsonObjectMapper() {
        this(false, true, false, false);
    }

    public Log4jJsonObjectMapper(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        this.registerModule((Module)new Log4jJsonModule(bl, bl2, bl3, bl4));
        this.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }
}

