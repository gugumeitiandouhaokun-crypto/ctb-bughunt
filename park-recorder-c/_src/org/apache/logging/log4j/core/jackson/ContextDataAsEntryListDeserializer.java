/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.JsonParser
 *  com.fasterxml.jackson.core.type.TypeReference
 *  com.fasterxml.jackson.databind.DeserializationContext
 *  com.fasterxml.jackson.databind.deser.std.StdDeserializer
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.core.impl.ContextDataFactory;
import org.apache.logging.log4j.core.jackson.MapEntry;
import org.apache.logging.log4j.util.StringMap;

public class ContextDataAsEntryListDeserializer
extends StdDeserializer<StringMap> {
    private static final long serialVersionUID = 1L;

    ContextDataAsEntryListDeserializer() {
        super(Map.class);
    }

    public StringMap deserialize(JsonParser object, DeserializationContext object2) {
        object = (List)object.readValueAs((TypeReference)new TypeReference<List<MapEntry>>(){});
        new ContextDataFactory();
        object2 = ContextDataFactory.createContextData();
        object = object.iterator();
        while (object.hasNext()) {
            MapEntry mapEntry = (MapEntry)object.next();
            object2.putValue(mapEntry.getKey(), mapEntry.getValue());
        }
        return object2;
    }
}

