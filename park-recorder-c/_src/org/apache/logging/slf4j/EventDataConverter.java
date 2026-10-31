/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.ext.EventData
 */
package org.apache.logging.slf4j;

import java.util.Map;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.StructuredDataMessage;
import org.slf4j.ext.EventData;

public class EventDataConverter {
    public Message convertEvent(String string, Object[] objectArray, Throwable throwable) {
        try {
            EventData eventData = objectArray != null && objectArray[0] instanceof EventData ? (EventData)objectArray[0] : new EventData(string);
            StructuredDataMessage structuredDataMessage = new StructuredDataMessage(eventData.getEventId(), eventData.getMessage(), eventData.getEventType());
            for (Map.Entry entry : eventData.getEventMap().entrySet()) {
                String string2 = (String)entry.getKey();
                if ("EventType".equals(string2) || "EventId".equals(string2) || "EventMessage".equals(string2)) continue;
                structuredDataMessage.put(string2, String.valueOf(entry.getValue()));
            }
            return structuredDataMessage;
        }
        catch (Exception exception) {
            return new ParameterizedMessage(string, objectArray, throwable);
        }
    }
}

