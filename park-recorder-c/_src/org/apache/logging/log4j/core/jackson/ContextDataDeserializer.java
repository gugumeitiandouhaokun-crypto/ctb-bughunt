/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.JsonParser
 *  com.fasterxml.jackson.core.JsonToken
 *  com.fasterxml.jackson.databind.DeserializationContext
 *  com.fasterxml.jackson.databind.deser.std.StdDeserializer
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.util.Map;
import org.apache.logging.log4j.core.impl.ContextDataFactory;
import org.apache.logging.log4j.util.StringMap;

public class ContextDataDeserializer
extends StdDeserializer<StringMap> {
    private static final long serialVersionUID = 1L;

    ContextDataDeserializer() {
        super(Map.class);
    }

    public StringMap deserialize(JsonParser jsonParser, DeserializationContext object) {
        object = ContextDataFactory.createContextData();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String string = jsonParser.getCurrentName();
            jsonParser.nextToken();
            object.putValue(string, jsonParser.getText());
        }
        return object;
    }
}

