/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import org.apache.logging.log4j.message.AsynchronouslyFormattable;
import org.apache.logging.log4j.message.BasicThreadInformation;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ThreadInformation;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.StringBuilderFormattable;

@AsynchronouslyFormattable
public class ThreadDumpMessage
implements Message,
StringBuilderFormattable {
    private static final long serialVersionUID = -1103400781608841088L;
    private static ThreadInfoFactory FACTORY;
    private volatile Map<ThreadInformation, StackTraceElement[]> threads;
    private final String title;
    private String formattedMessage;

    public ThreadDumpMessage(String string) {
        this.title = string == null ? "" : string;
        this.threads = ThreadDumpMessage.getFactory().createThreadInfo();
    }

    private ThreadDumpMessage(String string, String string2) {
        this.formattedMessage = string;
        this.title = string2 == null ? "" : string2;
    }

    private static ThreadInfoFactory getFactory() {
        if (FACTORY == null) {
            FACTORY = ThreadDumpMessage.initFactory(ThreadDumpMessage.class.getClassLoader());
        }
        return FACTORY;
    }

    private static ThreadInfoFactory initFactory(ClassLoader iterator) {
        iterator = ServiceLoader.load(ThreadInfoFactory.class, (ClassLoader)((Object)iterator));
        ThreadInfoFactory threadInfoFactory = null;
        try {
            iterator = ((ServiceLoader)((Object)iterator)).iterator();
            while (threadInfoFactory == null && iterator.hasNext()) {
                threadInfoFactory = (ThreadInfoFactory)iterator.next();
            }
        }
        catch (Exception | LinkageError | ServiceConfigurationError throwable) {
            StatusLogger.getLogger().info("ThreadDumpMessage uses BasicThreadInfoFactory: could not load extended ThreadInfoFactory: {}", (Object)throwable.toString());
            threadInfoFactory = null;
        }
        if (threadInfoFactory == null) {
            return new BasicThreadInfoFactory();
        }
        return threadInfoFactory;
    }

    public String toString() {
        return this.getFormattedMessage();
    }

    @Override
    public String getFormattedMessage() {
        if (this.formattedMessage != null) {
            return this.formattedMessage;
        }
        StringBuilder stringBuilder = new StringBuilder(255);
        this.formatTo(stringBuilder);
        return stringBuilder.toString();
    }

    @Override
    public void formatTo(StringBuilder stringBuilder) {
        stringBuilder.append(this.title);
        if (this.title.length() > 0) {
            stringBuilder.append('\n');
        }
        for (Map.Entry<ThreadInformation, StackTraceElement[]> entry : this.threads.entrySet()) {
            ThreadInformation threadInformation = entry.getKey();
            threadInformation.printThreadInfo(stringBuilder);
            threadInformation.printStack(stringBuilder, entry.getValue());
            stringBuilder.append('\n');
        }
    }

    @Override
    public String getFormat() {
        if (this.title == null) {
            return "";
        }
        return this.title;
    }

    @Override
    public Object[] getParameters() {
        return null;
    }

    protected Object writeReplace() {
        return new ThreadDumpMessageProxy(this);
    }

    private void readObject(ObjectInputStream objectInputStream) {
        throw new InvalidObjectException("Proxy required");
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }

    static class BasicThreadInfoFactory
    implements ThreadInfoFactory {
        private BasicThreadInfoFactory() {
        }

        @Override
        public Map<ThreadInformation, StackTraceElement[]> createThreadInfo() {
            Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
            HashMap<ThreadInformation, StackTraceElement[]> hashMap = new HashMap<ThreadInformation, StackTraceElement[]>(map.size());
            for (Map.Entry entry : map.entrySet()) {
                hashMap.put(new BasicThreadInformation((Thread)entry.getKey()), (StackTraceElement[])entry.getValue());
            }
            return hashMap;
        }
    }

    public static interface ThreadInfoFactory {
        public Map<ThreadInformation, StackTraceElement[]> createThreadInfo();
    }

    static class ThreadDumpMessageProxy
    implements Serializable {
        private static final long serialVersionUID = -3476620450287648269L;
        private final String formattedMsg;
        private final String title;

        ThreadDumpMessageProxy(ThreadDumpMessage threadDumpMessage) {
            this.formattedMsg = threadDumpMessage.getFormattedMessage();
            this.title = threadDumpMessage.title;
        }

        protected Object readResolve() {
            return new ThreadDumpMessage(this.formattedMsg, this.title);
        }
    }
}

