/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.JsonGenerator
 *  com.fasterxml.jackson.databind.SerializerProvider
 *  com.fasterxml.jackson.databind.ser.std.StdSerializer
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.util.Map;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.TriConsumer;

public class ContextDataSerializer
extends StdSerializer<ReadOnlyStringMap> {
    private static final long serialVersionUID = 1L;
    private static final TriConsumer<String, Object, JsonGenerator> WRITE_STRING_FIELD_INTO = new TriConsumer<String, Object, JsonGenerator>(){

        @Override
        public final void accept(String string, Object object, JsonGenerator jsonGenerator) {
            try {
                jsonGenerator.writeStringField(string, String.valueOf(object));
                return;
            }
            catch (Exception exception) {
                throw new IllegalStateException("Problem with key ".concat(String.valueOf(string)), exception);
            }
        }
    };

    protected ContextDataSerializer() {
        super(Map.class, false);
    }

    public void serialize(ReadOnlyStringMap readOnlyStringMap, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
        jsonGenerator.writeStartObject();
        readOnlyStringMap.forEach(WRITE_STRING_FIELD_INTO, jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}

