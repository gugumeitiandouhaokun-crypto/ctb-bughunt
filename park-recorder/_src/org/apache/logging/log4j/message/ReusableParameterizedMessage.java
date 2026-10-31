/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import java.util.Arrays;
import org.apache.logging.log4j.message.Clearable;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterConsumer;
import org.apache.logging.log4j.message.ParameterFormatter;
import org.apache.logging.log4j.message.ParameterVisitable;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.ReusableMessage;
import org.apache.logging.log4j.util.Constants;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.StringBuilders;

@PerformanceSensitive(value={"allocation"})
public class ReusableParameterizedMessage
implements Clearable,
ParameterVisitable,
ReusableMessage {
    private static final int MIN_BUILDER_SIZE = 512;
    private static final int MAX_PARMS = 10;
    private static final long serialVersionUID = 7800075879295123856L;
    private transient ThreadLocal<StringBuilder> buffer;
    private String messagePattern;
    private int argCount;
    private int usedCount;
    private final int[] indices = new int[256];
    private transient Object[] varargs;
    private transient Object[] params = new Object[10];
    private transient Throwable throwable;
    transient boolean reserved = false;

    private Object[] getTrimmedParams() {
        if (this.varargs == null) {
            return Arrays.copyOf(this.params, this.argCount);
        }
        return this.varargs;
    }

    private Object[] getParams() {
        if (this.varargs == null) {
            return this.params;
        }
        return this.varargs;
    }

    @Override
    public Object[] swapParameters(Object[] objectArray) {
        Object[] objectArray2;
        if (this.varargs == null) {
            objectArray2 = this.params;
            if (objectArray.length >= 10) {
                this.params = objectArray;
            } else if (this.argCount <= objectArray.length) {
                System.arraycopy(this.params, 0, objectArray, 0, this.argCount);
                for (int j = 0; j < this.argCount; ++j) {
                    this.params[j] = null;
                }
                objectArray2 = objectArray;
            } else {
                this.params = new Object[10];
            }
        } else {
            objectArray2 = this.argCount <= objectArray.length ? objectArray : new Object[this.argCount];
            System.arraycopy(this.varargs, 0, objectArray2, 0, this.argCount);
        }
        return objectArray2;
    }

    @Override
    public short getParameterCount() {
        return (short)this.argCount;
    }

    @Override
    public <S> void forEachParameter(ParameterConsumer<S> parameterConsumer, S s) {
        Object[] objectArray = this.getParams();
        for (int n = 0; n < this.argCount; n = (int)((short)(n + 1))) {
            parameterConsumer.accept(objectArray[n], n, s);
        }
    }

    @Override
    public Message memento() {
        return new ParameterizedMessage(this.messagePattern, this.getTrimmedParams());
    }

    private void init(String string, int n, Object[] objectArray) {
        this.varargs = null;
        this.messagePattern = string;
        this.argCount = n;
        int n2 = ReusableParameterizedMessage.count(string, this.indices);
        this.initThrowable(objectArray, n, n2);
        this.usedCount = Math.min(n2, n);
    }

    private static int count(String string, int[] nArray) {
        try {
            return ParameterFormatter.countArgumentPlaceholders2(string, nArray);
        }
        catch (Exception exception) {
            return ParameterFormatter.countArgumentPlaceholders(string);
        }
    }

    private void initThrowable(Object[] objectArray, int n, int n2) {
        if (n2 < n && objectArray[n - 1] instanceof Throwable) {
            this.throwable = (Throwable)objectArray[n - 1];
            return;
        }
        this.throwable = null;
    }

    ReusableParameterizedMessage set(String string, Object ... objectArray) {
        this.init(string, objectArray == null ? 0 : objectArray.length, objectArray);
        this.varargs = objectArray;
        return this;
    }

    ReusableParameterizedMessage set(String string, Object object) {
        this.params[0] = object;
        this.init(string, 1, this.params);
        return this;
    }

    ReusableParameterizedMessage set(String string, Object object, Object object2) {
        this.params[0] = object;
        this.params[1] = object2;
        this.init(string, 2, this.params);
        return this;
    }

    ReusableParameterizedMessage set(String string, Object object, Object object2, Object object3) {
        this.params[0] = object;
        this.params[1] = object2;
        this.params[2] = object3;
        this.init(string, 3, this.params);
        return this;
    }

    ReusableParameterizedMessage set(String string, Object object, Object object2, Object object3, Object object4) {
        this.params[0] = object;
        this.params[1] = object2;
        this.params[2] = object3;
        this.params[3] = object4;
        this.init(string, 4, this.params);
        return this;
    }

    ReusableParameterizedMessage set(String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.params[0] = object;
        this.params[1] = object2;
        this.params[2] = object3;
        this.params[3] = object4;
        this.params[4] = object5;
        this.init(string, 5, this.params);
        return this;
    }

    ReusableParameterizedMessage set(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.params[0] = object;
        this.params[1] = object2;
        this.params[2] = object3;
        this.params[3] = object4;
        this.params[4] = object5;
        this.params[5] = object6;
        this.init(string, 6, this.params);
        return this;
    }

    ReusableParameterizedMessage set(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.params[0] = object;
        this.params[1] = object2;
        this.params[2] = object3;
        this.params[3] = object4;
        this.params[4] = object5;
        this.params[5] = object6;
        this.params[6] = object7;
        this.init(string, 7, this.params);
        return this;
    }

    ReusableParameterizedMessage set(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.params[0] = object;
        this.params[1] = object2;
        this.params[2] = object3;
        this.params[3] = object4;
        this.params[4] = object5;
        this.params[5] = object6;
        this.params[6] = object7;
        this.params[7] = object8;
        this.init(string, 8, this.params);
        return this;
    }

    ReusableParameterizedMessage set(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.params[0] = object;
        this.params[1] = object2;
        this.params[2] = object3;
        this.params[3] = object4;
        this.params[4] = object5;
        this.params[5] = object6;
        this.params[6] = object7;
        this.params[7] = object8;
        this.params[8] = object9;
        this.init(string, 9, this.params);
        return this;
    }

    ReusableParameterizedMessage set(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.params[0] = object;
        this.params[1] = object2;
        this.params[2] = object3;
        this.params[3] = object4;
        this.params[4] = object5;
        this.params[5] = object6;
        this.params[6] = object7;
        this.params[7] = object8;
        this.params[8] = object9;
        this.params[9] = object10;
        this.init(string, 10, this.params);
        return this;
    }

    @Override
    public String getFormat() {
        return this.messagePattern;
    }

    @Override
    public Object[] getParameters() {
        return this.getTrimmedParams();
    }

    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }

    @Override
    public String getFormattedMessage() {
        StringBuilder stringBuilder = this.getBuffer();
        this.formatTo(stringBuilder);
        String string = stringBuilder.toString();
        StringBuilders.trimToMaxSize(stringBuilder, Constants.MAX_REUSABLE_MESSAGE_SIZE);
        return string;
    }

    private StringBuilder getBuffer() {
        StringBuilder stringBuilder;
        if (this.buffer == null) {
            this.buffer = new ThreadLocal();
        }
        if ((stringBuilder = this.buffer.get()) == null) {
            int n = this.messagePattern == null ? 0 : this.messagePattern.length();
            stringBuilder = new StringBuilder(Math.max(512, n * 2));
            this.buffer.set(stringBuilder);
        }
        stringBuilder.setLength(0);
        return stringBuilder;
    }

    @Override
    public void formatTo(StringBuilder stringBuilder) {
        if (this.indices[0] < 0) {
            ParameterFormatter.formatMessage(stringBuilder, this.messagePattern, this.getParams(), this.argCount);
            return;
        }
        ParameterFormatter.formatMessage2(stringBuilder, this.messagePattern, this.getParams(), this.usedCount, this.indices);
    }

    ReusableParameterizedMessage reserve() {
        this.reserved = true;
        return this;
    }

    public String toString() {
        return "ReusableParameterizedMessage[messagePattern=" + this.getFormat() + ", stringArgs=" + Arrays.toString(this.getParameters()) + ", throwable=" + this.getThrowable() + ']';
    }

    @Override
    public void clear() {
        this.reserved = false;
        this.varargs = null;
        this.messagePattern = null;
        this.throwable = null;
    }
}

