/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.StringBuilderFormattable;

public class SimpleMessage
implements CharSequence,
Message,
StringBuilderFormattable {
    private static final long serialVersionUID = -8398002534962715992L;
    private String message;
    private transient CharSequence charSequence;

    public SimpleMessage() {
        this(null);
    }

    public SimpleMessage(String string) {
        this.message = string;
        this.charSequence = string;
    }

    public SimpleMessage(CharSequence charSequence) {
        this.charSequence = charSequence;
    }

    @Override
    public String getFormattedMessage() {
        this.message = this.message == null ? String.valueOf(this.charSequence) : this.message;
        return this.message;
    }

    @Override
    public void formatTo(StringBuilder stringBuilder) {
        stringBuilder.append(this.message != null ? this.message : this.charSequence);
    }

    @Override
    public String getFormat() {
        return this.message;
    }

    @Override
    public Object[] getParameters() {
        return null;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        object = (SimpleMessage)object;
        return !(this.charSequence == null ? ((SimpleMessage)object).charSequence != null : !this.charSequence.equals(((SimpleMessage)object).charSequence));
    }

    public int hashCode() {
        if (this.charSequence != null) {
            return this.charSequence.hashCode();
        }
        return 0;
    }

    @Override
    public String toString() {
        return this.getFormattedMessage();
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }

    @Override
    public int length() {
        if (this.charSequence == null) {
            return 0;
        }
        return this.charSequence.length();
    }

    @Override
    public char charAt(int n) {
        return this.charSequence.charAt(n);
    }

    @Override
    public CharSequence subSequence(int n, int n2) {
        return this.charSequence.subSequence(n, n2);
    }

    private void writeObject(ObjectOutputStream objectOutputStream) {
        this.getFormattedMessage();
        objectOutputStream.defaultWriteObject();
    }

    private void readObject(ObjectInputStream objectInputStream) {
        objectInputStream.defaultReadObject();
        this.charSequence = this.message;
    }
}

