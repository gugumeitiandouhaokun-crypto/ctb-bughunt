/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import org.apache.logging.log4j.message.Message;

public final class ObjectArrayMessage
implements Message {
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final long serialVersionUID = -5903272448334166185L;
    private transient Object[] array;
    private transient String arrayString;

    public ObjectArrayMessage(Object ... objectArray) {
        this.array = objectArray == null ? EMPTY_OBJECT_ARRAY : objectArray;
    }

    private boolean equalObjectsOrStrings(Object[] objectArray, Object[] objectArray2) {
        return Arrays.equals(objectArray, objectArray2) || Arrays.toString(objectArray).equals(Arrays.toString(objectArray2));
    }

    public final boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        object = (ObjectArrayMessage)object;
        if (this.array == null) {
            return ((ObjectArrayMessage)object).array == null;
        }
        ObjectArrayMessage objectArrayMessage = this;
        return objectArrayMessage.equalObjectsOrStrings(objectArrayMessage.array, ((ObjectArrayMessage)object).array);
    }

    @Override
    public final String getFormat() {
        return this.getFormattedMessage();
    }

    @Override
    public final String getFormattedMessage() {
        if (this.arrayString == null) {
            this.arrayString = Arrays.toString(this.array);
        }
        return this.arrayString;
    }

    @Override
    public final Object[] getParameters() {
        return this.array;
    }

    @Override
    public final Throwable getThrowable() {
        return null;
    }

    public final int hashCode() {
        return Arrays.hashCode(this.array);
    }

    private void readObject(ObjectInputStream objectInputStream) {
        objectInputStream.defaultReadObject();
        this.array = (Object[])objectInputStream.readObject();
    }

    public final String toString() {
        return this.getFormattedMessage();
    }

    private void writeObject(ObjectOutputStream objectOutputStream) {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeObject(this.array);
    }
}

