/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.JsonParser
 *  com.fasterxml.jackson.core.JsonToken
 *  com.fasterxml.jackson.databind.DeserializationContext
 *  com.fasterxml.jackson.databind.JsonMappingException
 *  com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

public final class Log4jStackTraceElementDeserializer
extends StdScalarDeserializer<StackTraceElement> {
    private static final long serialVersionUID = 1L;

    public Log4jStackTraceElementDeserializer() {
        super(StackTraceElement.class);
    }

    public final StackTraceElement deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        JsonToken jsonToken = jsonParser.getCurrentToken();
        if (jsonToken == JsonToken.START_OBJECT) {
            String string = null;
            String string2 = null;
            String string3 = null;
            int n = -1;
            while ((jsonToken = jsonParser.nextValue()) != JsonToken.END_OBJECT) {
                String string4 = jsonParser.getCurrentName();
                if ("class".equals(string4)) {
                    string = jsonParser.getText();
                    continue;
                }
                if ("file".equals(string4)) {
                    string3 = jsonParser.getText();
                    continue;
                }
                if ("line".equals(string4)) {
                    if (jsonToken.isNumeric()) {
                        n = jsonParser.getIntValue();
                        continue;
                    }
                    try {
                        n = Integer.parseInt(jsonParser.getText().trim());
                        continue;
                    }
                    catch (NumberFormatException numberFormatException) {
                        throw JsonMappingException.from((JsonParser)jsonParser, (String)("Non-numeric token (" + jsonToken + ") for property 'line'"), (Throwable)numberFormatException);
                    }
                }
                if ("method".equals(string4)) {
                    string2 = jsonParser.getText();
                    continue;
                }
                if ("nativeMethod".equals(string4)) continue;
                this.handleUnknownProperty(jsonParser, deserializationContext, this._valueClass, string4);
            }
            return new StackTraceElement(string, string2, string3, n);
        }
        throw deserializationContext.mappingException(this._valueClass, jsonToken);
    }
}

