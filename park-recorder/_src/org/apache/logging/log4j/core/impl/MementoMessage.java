/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import java.util.Arrays;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.StringBuilderFormattable;

public final class MementoMessage
implements Message,
StringBuilderFormattable {
    private final String formattedMessage;
    private final String format;
    private final Object[] parameters;

    public MementoMessage(String string, String string2, Object[] objectArray) {
        this.formattedMessage = string;
        this.format = string2;
        this.parameters = objectArray;
    }

    @Override
    public final String getFormattedMessage() {
        return this.formattedMessage;
    }

    @Override
    public final String getFormat() {
        return this.format;
    }

    @Override
    public final Object[] getParameters() {
        return this.parameters;
    }

    @Override
    public final Throwable getThrowable() {
        return null;
    }

    @Override
    public final void formatTo(StringBuilder stringBuilder) {
        stringBuilder.append(this.formattedMessage);
    }

    public final String toString() {
        return "MementoMessage{formattedMessage='" + this.formattedMessage + '\'' + ", format='" + this.format + '\'' + ", parameters=" + Arrays.toString(this.parameters) + '}';
    }
}

