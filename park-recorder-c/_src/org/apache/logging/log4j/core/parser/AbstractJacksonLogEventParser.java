/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.DeserializationFeature
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  com.fasterxml.jackson.databind.ObjectReader
 */
package org.apache.logging.log4j.core.parser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.parser.ParseException;
import org.apache.logging.log4j.core.parser.TextLogEventParser;

class AbstractJacksonLogEventParser
implements TextLogEventParser {
    private final ObjectReader objectReader;

    AbstractJacksonLogEventParser(ObjectMapper objectMapper) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectReader = objectMapper.readerFor(Log4jLogEvent.class);
    }

    @Override
    public LogEvent parseFrom(String string) {
        try {
            return (LogEvent)this.objectReader.readValue(string);
        }
        catch (IOException iOException) {
            throw new ParseException(iOException);
        }
    }

    @Override
    public LogEvent parseFrom(byte[] byArray) {
        try {
            return (LogEvent)this.objectReader.readValue(byArray);
        }
        catch (IOException iOException) {
            throw new ParseException(iOException);
        }
    }

    @Override
    public LogEvent parseFrom(byte[] byArray, int n, int n2) {
        try {
            return (LogEvent)this.objectReader.readValue(byArray, n, n2);
        }
        catch (IOException iOException) {
            throw new ParseException(iOException);
        }
    }
}

