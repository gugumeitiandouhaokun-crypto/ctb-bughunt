/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Locale;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.status.StatusLogger;

public class MessageFormatMessage
implements Message {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final long serialVersionUID = 1L;
    private static final int HASHVAL = 31;
    private String messagePattern;
    private transient Object[] parameters;
    private String[] serializedParameters;
    private transient String formattedMessage;
    private transient Throwable throwable;
    private final Locale locale;

    public MessageFormatMessage(Locale locale, String string, Object ... objectArray) {
        this.locale = locale;
        this.messagePattern = string;
        this.parameters = objectArray;
        int n = objectArray == null ? 0 : objectArray.length;
        if (n > 0 && objectArray[n - 1] instanceof Throwable) {
            this.throwable = (Throwable)objectArray[n - 1];
        }
    }

    public MessageFormatMessage(String string, Object ... objectArray) {
        this(Locale.getDefault(Locale.Category.FORMAT), string, objectArray);
    }

    @Override
    public String getFormattedMessage() {
        if (this.formattedMessage == null) {
            this.formattedMessage = this.formatMessage(this.messagePattern, this.parameters);
        }
        return this.formattedMessage;
    }

    @Override
    public String getFormat() {
        return this.messagePattern;
    }

    @Override
    public Object[] getParameters() {
        if (this.parameters != null) {
            return this.parameters;
        }
        return this.serializedParameters;
    }

    protected String formatMessage(String string, Object ... objectArray) {
        try {
            return new MessageFormat(string, this.locale).format(objectArray);
        }
        catch (IllegalFormatException illegalFormatException) {
            LOGGER.error("Unable to format msg: ".concat(String.valueOf(string)), (Throwable)illegalFormatException);
            return string;
        }
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        object = (MessageFormatMessage)object;
        if (this.messagePattern != null ? !this.messagePattern.equals(((MessageFormatMessage)object).messagePattern) : ((MessageFormatMessage)object).messagePattern != null) {
            return false;
        }
        return Arrays.equals(this.serializedParameters, ((MessageFormatMessage)object).serializedParameters);
    }

    public int hashCode() {
        int n = this.messagePattern != null ? this.messagePattern.hashCode() : 0;
        return 31 * n + (this.serializedParameters != null ? Arrays.hashCode(this.serializedParameters) : 0);
    }

    public String toString() {
        return this.getFormattedMessage();
    }

    private void writeObject(ObjectOutputStream objectOutputStream) {
        this.getFormattedMessage();
        objectOutputStream.writeUTF(this.formattedMessage);
        objectOutputStream.writeUTF(this.messagePattern);
        int n = this.parameters == null ? 0 : this.parameters.length;
        objectOutputStream.writeInt(n);
        this.serializedParameters = new String[n];
        if (n > 0) {
            for (int j = 0; j < n; ++j) {
                this.serializedParameters[j] = String.valueOf(this.parameters[j]);
                objectOutputStream.writeUTF(this.serializedParameters[j]);
            }
        }
    }

    private void readObject(ObjectInputStream objectInputStream) {
        this.parameters = null;
        this.throwable = null;
        this.formattedMessage = objectInputStream.readUTF();
        this.messagePattern = objectInputStream.readUTF();
        int n = objectInputStream.readInt();
        this.serializedParameters = new String[n];
        for (int j = 0; j < n; ++j) {
            this.serializedParameters[j] = objectInputStream.readUTF();
        }
    }

    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }
}

