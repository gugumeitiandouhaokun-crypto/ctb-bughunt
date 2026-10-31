/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.message.MessageCollectionMessage;
import org.apache.logging.log4j.message.StructuredDataMessage;
import org.apache.logging.log4j.util.StringBuilderFormattable;

public class StructuredDataCollectionMessage
implements MessageCollectionMessage<StructuredDataMessage>,
StringBuilderFormattable {
    private static final long serialVersionUID = 5725337076388822924L;
    private final List<StructuredDataMessage> structuredDataMessageList;

    public StructuredDataCollectionMessage(List<StructuredDataMessage> list) {
        this.structuredDataMessageList = list;
    }

    @Override
    public Iterator<StructuredDataMessage> iterator() {
        return this.structuredDataMessageList.iterator();
    }

    @Override
    public String getFormattedMessage() {
        StringBuilder stringBuilder = new StringBuilder();
        this.formatTo(stringBuilder);
        return stringBuilder.toString();
    }

    @Override
    public String getFormat() {
        StringBuilder stringBuilder = new StringBuilder();
        for (StructuredDataMessage structuredDataMessage : this.structuredDataMessageList) {
            if (structuredDataMessage.getFormat() == null) continue;
            if (stringBuilder.length() > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(structuredDataMessage.getFormat());
        }
        return stringBuilder.toString();
    }

    @Override
    public void formatTo(StringBuilder stringBuilder) {
        Iterator<StructuredDataMessage> iterator = this.structuredDataMessageList.iterator();
        while (iterator.hasNext()) {
            iterator.next().formatTo(stringBuilder);
        }
    }

    @Override
    public Object[] getParameters() {
        Object object;
        Object[] objectArray = new ArrayList();
        int n = 0;
        Object[] objectArray2 = this.structuredDataMessageList.iterator();
        while (objectArray2.hasNext()) {
            object = objectArray2.next().getParameters();
            if (object == null) continue;
            objectArray.add(object);
            n += ((Object)object).length;
        }
        objectArray2 = new Object[n];
        int n2 = 0;
        object = objectArray.iterator();
        while (object.hasNext()) {
            for (Object object2 : (Object[])object.next()) {
                objectArray2[n2++] = object2;
            }
        }
        return objectArray2;
    }

    @Override
    public Throwable getThrowable() {
        Iterator<StructuredDataMessage> iterator = this.structuredDataMessageList.iterator();
        while (iterator.hasNext()) {
            Throwable throwable = iterator.next().getThrowable();
            if (throwable == null) continue;
            return throwable;
        }
        return null;
    }
}

