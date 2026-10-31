/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import org.apache.logging.log4j.message.Clearable;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;
import org.apache.logging.log4j.message.ParameterConsumer;
import org.apache.logging.log4j.message.ParameterVisitable;
import org.apache.logging.log4j.message.ReusableMessage;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.StringBuilders;

@PerformanceSensitive(value={"allocation"})
public class ReusableObjectMessage
implements Clearable,
ParameterVisitable,
ReusableMessage {
    private static final long serialVersionUID = 6922476812535519960L;
    private transient Object obj;

    public void set(Object object) {
        this.obj = object;
    }

    @Override
    public String getFormattedMessage() {
        return String.valueOf(this.obj);
    }

    @Override
    public void formatTo(StringBuilder stringBuilder) {
        StringBuilders.appendValue(stringBuilder, this.obj);
    }

    @Override
    public String getFormat() {
        if (this.obj instanceof String) {
            return (String)this.obj;
        }
        return null;
    }

    public Object getParameter() {
        return this.obj;
    }

    @Override
    public Object[] getParameters() {
        return new Object[]{this.obj};
    }

    public String toString() {
        return this.getFormattedMessage();
    }

    @Override
    public Throwable getThrowable() {
        if (this.obj instanceof Throwable) {
            return (Throwable)this.obj;
        }
        return null;
    }

    @Override
    public Object[] swapParameters(Object[] objectArray) {
        if (objectArray.length == 0) {
            Object[] objectArray2 = new Object[10];
            objectArray = objectArray2;
            objectArray2[0] = this.obj;
            return objectArray;
        }
        objectArray[0] = this.obj;
        return objectArray;
    }

    @Override
    public short getParameterCount() {
        return 1;
    }

    @Override
    public <S> void forEachParameter(ParameterConsumer<S> parameterConsumer, S s) {
        parameterConsumer.accept(this.obj, 0, s);
    }

    @Override
    public Message memento() {
        return new ObjectMessage(this.obj);
    }

    @Override
    public void clear() {
        this.obj = null;
    }
}

