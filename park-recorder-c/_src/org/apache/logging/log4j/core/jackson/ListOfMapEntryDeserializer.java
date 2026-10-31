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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.core.jackson.MapEntry;

public class ListOfMapEntryDeserializer
extends StdDeserializer<Map<String, String>> {
    private static final long serialVersionUID = 1L;

    ListOfMapEntryDeserializer() {
        super(Map.class);
    }

    public Map<String, String> deserialize(JsonParser object, DeserializationContext object2) {
        object = (List)object.readValueAs((TypeReference)new TypeReference<List<MapEntry>>(){});
        object2 = new HashMap(object.size());
        object = object.iterator();
        while (object.hasNext()) {
            MapEntry mapEntry = (MapEntry)object.next();
            ((HashMap)object2).put(mapEntry.getKey(), mapEntry.getValue());
        }
        return object2;
    }
}

