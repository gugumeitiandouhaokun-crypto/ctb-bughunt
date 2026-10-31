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
import org.apache.logging.log4j.message.ObjectMessage;

final class ObjectMessageSerializer
extends StdScalarSerializer<ObjectMessage> {
    private static final long serialVersionUID = 1L;

    ObjectMessageSerializer() {
        super(ObjectMessage.class);
    }

    public final void serialize(ObjectMessage objectMessage, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
        jsonGenerator.writeObject(objectMessage.getParameter());
    }
}

