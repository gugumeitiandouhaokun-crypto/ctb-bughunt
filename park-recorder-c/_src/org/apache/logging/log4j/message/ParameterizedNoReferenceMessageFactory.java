/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import org.apache.logging.log4j.message.AbstractMessageFactory;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;

public final class ParameterizedNoReferenceMessageFactory
extends AbstractMessageFactory {
    private static final long serialVersionUID = 5027639245636870500L;
    public static final ParameterizedNoReferenceMessageFactory INSTANCE = new ParameterizedNoReferenceMessageFactory();

    @Override
    public final Message newMessage(String object, Object ... objectArray) {
        if (objectArray == null) {
            return new SimpleMessage((String)object);
        }
        object = new ParameterizedMessage((String)object, objectArray);
        return new StatusMessage(((ParameterizedMessage)object).getFormattedMessage(), ((ParameterizedMessage)object).getThrowable());
    }

    static class StatusMessage
    implements Message {
        private static final long serialVersionUID = 4199272162767841280L;
        private final String formattedMessage;
        private final Throwable throwable;

        public StatusMessage(String string, Throwable throwable) {
            this.formattedMessage = string;
            this.throwable = throwable;
        }

        @Override
        public String getFormattedMessage() {
            return this.formattedMessage;
        }

        @Override
        public String getFormat() {
            return this.formattedMessage;
        }

        @Override
        public Object[] getParameters() {
            return null;
        }

        @Override
        public Throwable getThrowable() {
            return this.throwable;
        }
    }
}

