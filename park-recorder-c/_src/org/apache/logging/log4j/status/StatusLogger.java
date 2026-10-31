/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.status;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.ParameterizedNoReferenceMessageFactory;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.status.StatusData;
import org.apache.logging.log4j.status.StatusListener;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.Strings;

public final class StatusLogger
extends AbstractLogger {
    public static final String MAX_STATUS_ENTRIES = "log4j2.status.entries";
    public static final String DEFAULT_STATUS_LISTENER_LEVEL = "log4j2.StatusLogger.level";
    public static final String STATUS_DATE_FORMAT = "log4j2.StatusLogger.DateFormat";
    private static final long serialVersionUID = 2L;
    private static final String NOT_AVAIL = "?";
    private static final PropertiesUtil PROPS = new PropertiesUtil("log4j2.StatusLogger.properties");
    private static final int MAX_ENTRIES = PROPS.getIntegerProperty("log4j2.status.entries", 200);
    private static final String DEFAULT_STATUS_LEVEL = PROPS.getStringProperty("log4j2.StatusLogger.level");
    private static final StatusLogger STATUS_LOGGER = new StatusLogger(StatusLogger.class.getName(), ParameterizedNoReferenceMessageFactory.INSTANCE);
    private final SimpleLogger logger;
    private final Collection<StatusListener> listeners = new CopyOnWriteArrayList<StatusListener>();
    private final ReadWriteLock listenersLock = new ReentrantReadWriteLock();
    private final Queue<StatusData> messages = new BoundedQueue<StatusData>(MAX_ENTRIES);
    private final Lock msgLock = new ReentrantLock();
    private int listenersLevel;

    private StatusLogger(String string, MessageFactory messageFactory) {
        super(string, messageFactory);
        string = PROPS.getStringProperty(STATUS_DATE_FORMAT, "");
        boolean bl = !Strings.isEmpty(string);
        this.logger = new SimpleLogger("StatusLogger", Level.ERROR, false, true, bl, false, string, messageFactory, PROPS, System.err);
        this.listenersLevel = Level.toLevel(DEFAULT_STATUS_LEVEL, Level.WARN).intLevel();
        if (this.isDebugPropertyEnabled()) {
            this.logger.setLevel(Level.TRACE);
        }
    }

    private boolean isDebugPropertyEnabled() {
        return PropertiesUtil.getProperties().getBooleanProperty("log4j2.debug", false, true);
    }

    public static StatusLogger getLogger() {
        return STATUS_LOGGER;
    }

    public final void setLevel(Level level) {
        this.logger.setLevel(level);
    }

    public final void registerListener(StatusListener object) {
        this.listenersLock.writeLock().lock();
        try {
            this.listeners.add((StatusListener)object);
            object = object.getStatusLevel();
            if (this.listenersLevel < ((Level)object).intLevel()) {
                this.listenersLevel = ((Level)object).intLevel();
            }
            return;
        }
        finally {
            this.listenersLock.writeLock().unlock();
        }
    }

    public final void removeListener(StatusListener statusListener) {
        StatusLogger.closeSilently(statusListener);
        this.listenersLock.writeLock().lock();
        try {
            this.listeners.remove(statusListener);
            int n = Level.toLevel(DEFAULT_STATUS_LEVEL, Level.WARN).intLevel();
            Iterator<StatusListener> iterator = this.listeners.iterator();
            while (iterator.hasNext()) {
                int n2 = iterator.next().getStatusLevel().intLevel();
                if (n >= n2) continue;
                n = n2;
            }
            this.listenersLevel = n;
            return;
        }
        finally {
            this.listenersLock.writeLock().unlock();
        }
    }

    public final void updateListenerLevel(Level level) {
        if (level.intLevel() > this.listenersLevel) {
            this.listenersLevel = level.intLevel();
        }
    }

    public final Iterable<StatusListener> getListeners() {
        return this.listeners;
    }

    public final void reset() {
        this.listenersLock.writeLock().lock();
        try {
            Iterator<StatusListener> iterator = this.listeners.iterator();
            while (iterator.hasNext()) {
                StatusLogger.closeSilently(iterator.next());
            }
            return;
        }
        finally {
            this.listeners.clear();
            this.listenersLock.writeLock().unlock();
            this.clear();
        }
    }

    private static void closeSilently(Closeable closeable) {
        try {
            closeable.close();
            return;
        }
        catch (IOException iOException) {
            return;
        }
    }

    public final List<StatusData> getStatusData() {
        this.msgLock.lock();
        try {
            ArrayList<StatusData> arrayList = new ArrayList<StatusData>(this.messages);
            return arrayList;
        }
        finally {
            this.msgLock.unlock();
        }
    }

    public final void clear() {
        this.msgLock.lock();
        try {
            this.messages.clear();
            return;
        }
        finally {
            this.msgLock.unlock();
        }
    }

    @Override
    public final Level getLevel() {
        return this.logger.getLevel();
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public final void logMessage(String object, Level object22, Marker marker, Message message, Throwable throwable) {
        void var3_6;
        void var5_8;
        void var4_7;
        Serializable serializable = null;
        if (object != null) {
            serializable = this.getStackTraceElement((String)((Object)object), Thread.currentThread().getStackTrace());
        }
        serializable = new StatusData((StackTraceElement)serializable, (Level)object22, (Message)var4_7, (Throwable)var5_8, null);
        this.msgLock.lock();
        try {
            this.messages.add((StatusData)serializable);
        }
        finally {
            this.msgLock.unlock();
        }
        if (!this.isDebugPropertyEnabled() && this.listeners.size() > 0) {
            for (StatusListener statusListener : this.listeners) {
                if (!((StatusData)serializable).getLevel().isMoreSpecificThan(statusListener.getStatusLevel())) continue;
                statusListener.log((StatusData)serializable);
            }
            return;
        }
        this.logger.logMessage((String)((Object)object), (Level)object22, (Marker)var3_6, (Message)var4_7, (Throwable)var5_8);
    }

    private StackTraceElement getStackTraceElement(String string, StackTraceElement[] stackTraceElementArray) {
        if (string == null) {
            return null;
        }
        boolean bl = false;
        for (StackTraceElement stackTraceElement : stackTraceElementArray) {
            String string2 = stackTraceElement.getClassName();
            if (bl && !string.equals(string2)) {
                return stackTraceElement;
            }
            if (string.equals(string2)) {
                bl = true;
                continue;
            }
            if (NOT_AVAIL.equals(string2)) break;
        }
        return null;
    }

    @Override
    public final boolean isEnabled(Level level, Marker marker, String string, Throwable throwable) {
        return this.isEnabled(level, marker);
    }

    @Override
    public final boolean isEnabled(Level level, Marker marker, String string) {
        return this.isEnabled(level, marker);
    }

    @Override
    public final boolean isEnabled(Level level, Marker marker, String string, Object ... objectArray) {
        return this.isEnabled(level, marker);
    }

    @Override
    public final boolean isEnabled(Level level, Marker marker, String string, Object object) {
        return this.isEnabled(level, marker);
    }

    @Override
    public final boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2) {
        return this.isEnabled(level, marker);
    }

    @Override
    public final boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3) {
        return this.isEnabled(level, marker);
    }

    @Override
    public final boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        return this.isEnabled(level, marker);
    }

    @Override
    public final boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        return this.isEnabled(level, marker);
    }

    @Override
    public final boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        return this.isEnabled(level, marker);
    }

    @Override
    public final boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        return this.isEnabled(level, marker);
    }

    @Override
    public final boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        return this.isEnabled(level, marker);
    }

    @Override
    public final boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        return this.isEnabled(level, marker);
    }

    @Override
    public final boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        return this.isEnabled(level, marker);
    }

    @Override
    public final boolean isEnabled(Level level, Marker marker, CharSequence charSequence, Throwable throwable) {
        return this.isEnabled(level, marker);
    }

    @Override
    public final boolean isEnabled(Level level, Marker marker, Object object, Throwable throwable) {
        return this.isEnabled(level, marker);
    }

    @Override
    public final boolean isEnabled(Level level, Marker marker, Message message, Throwable throwable) {
        return this.isEnabled(level, marker);
    }

    @Override
    public final boolean isEnabled(Level level, Marker marker) {
        if (this.isDebugPropertyEnabled()) {
            return true;
        }
        if (this.listeners.size() > 0) {
            return this.listenersLevel >= level.intLevel();
        }
        return this.logger.isEnabled(level, marker);
    }

    class BoundedQueue<E>
    extends ConcurrentLinkedQueue<E> {
        private static final long serialVersionUID = -3945953719763255337L;
        private final int size;

        BoundedQueue(int n) {
            this.size = n;
        }

        @Override
        public boolean add(E e2) {
            super.add(e2);
            while (StatusLogger.this.messages.size() > this.size) {
                StatusLogger.this.messages.poll();
            }
            return this.size > 0;
        }
    }
}

