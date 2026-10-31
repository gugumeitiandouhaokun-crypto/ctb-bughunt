/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.JsonParser
 *  com.fasterxml.jackson.databind.DeserializationContext
 *  com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import org.apache.logging.log4j.message.SimpleMessage;

public final class SimpleMessageDeserializer
extends StdScalarDeserializer<SimpleMessage> {
    private static final long serialVersionUID = 1L;

    SimpleMessageDeserializer() {
        super(SimpleMessage.class);
    }

    public final SimpleMessage deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        return new SimpleMessage(jsonParser.getValueAsString());
    }
}

