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
import org.apache.logging.log4j.core.jackson.MapEntry;

public class ListOfMapEntrySerializer
extends StdSerializer<Map<String, String>> {
    private static final long serialVersionUID = 1L;

    protected ListOfMapEntrySerializer() {
        super(Map.class, false);
    }

    public void serialize(Map<String, String> iterator, JsonGenerator jsonGenerator, SerializerProvider mapEntryArray) {
        iterator = iterator.entrySet();
        mapEntryArray = new MapEntry[iterator.size()];
        int n = 0;
        iterator = iterator.iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            mapEntryArray[n++] = new MapEntry((String)entry.getKey(), (String)entry.getValue());
        }
        jsonGenerator.writeObject((Object)mapEntryArray);
    }
}

