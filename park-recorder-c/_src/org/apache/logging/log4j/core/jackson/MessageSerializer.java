/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.JsonGenerator
 *  com.fasterxml.jackson.databind.SerializerProvider
 *  com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.apache.logging.log4j.message.Message;

final class MessageSerializer
extends StdScalarSerializer<Message> {
    private static final long serialVersionUID = 1L;

    MessageSerializer() {
        super(Message.class);
    }

    public final void serialize(Message message, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
        jsonGenerator.writeString(message.getFormattedMessage());
    }
}

