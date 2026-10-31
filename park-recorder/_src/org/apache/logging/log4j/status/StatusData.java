/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.status;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.message.Message;

public class StatusData
implements Serializable {
    private static final long serialVersionUID = -4341916115118014017L;
    private final long timestamp = System.currentTimeMillis();
    private final StackTraceElement caller;
    private final Level level;
    private final Message msg;
    private String threadName;
    private final Throwable throwable;

    public StatusData(StackTraceElement stackTraceElement, Level level, Message message, Throwable throwable, String string) {
        this.caller = stackTraceElement;
        this.level = level;
        this.msg = message;
        this.throwable = throwable;
        this.threadName = string;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public StackTraceElement getStackTraceElement() {
        return this.caller;
    }

    public Level getLevel() {
        return this.level;
    }

    public Message getMessage() {
        return this.msg;
    }

    public String getThreadName() {
        if (this.threadName == null) {
            this.threadName = Thread.currentThread().getName();
        }
        return this.threadName;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    public String getFormattedStatus() {
        StringBuilder stringBuilder = new StringBuilder();
        Object object = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        stringBuilder.append(object.format(new Date(this.timestamp)));
        stringBuilder.append(' ');
        stringBuilder.append(this.getThreadName());
        stringBuilder.append(' ');
        stringBuilder.append(this.level.toString());
        stringBuilder.append(' ');
        stringBuilder.append(this.msg.getFormattedMessage());
        object = this.msg.getParameters();
        if (this.throwable == null && object != null && object[((Object[])object).length - 1] instanceof Throwable) {
            Object[] objectArray = object;
            object = (Throwable)objectArray[objectArray.length - 1];
        } else {
            object = this.throwable;
        }
        if (object != null) {
            stringBuilder.append(' ');
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            object.printStackTrace(new PrintStream(byteArrayOutputStream));
            stringBuilder.append(byteArrayOutputStream.toString());
        }
        return stringBuilder.toString();
    }
}

