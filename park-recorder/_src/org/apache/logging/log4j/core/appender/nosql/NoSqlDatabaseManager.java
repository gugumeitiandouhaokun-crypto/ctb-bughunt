/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.nosql;

import java.io.Serializable;
import java.util.Date;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.db.AbstractDatabaseManager;
import org.apache.logging.log4j.core.appender.nosql.NoSqlConnection;
import org.apache.logging.log4j.core.appender.nosql.NoSqlObject;
import org.apache.logging.log4j.core.appender.nosql.NoSqlProvider;
import org.apache.logging.log4j.core.util.Closer;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.util.BiConsumer;

public final class NoSqlDatabaseManager<W>
extends AbstractDatabaseManager {
    private static final NoSQLDatabaseManagerFactory FACTORY = new NoSQLDatabaseManagerFactory();
    private final NoSqlProvider<NoSqlConnection<W, ? extends NoSqlObject<W>>> provider;
    private NoSqlConnection<W, ? extends NoSqlObject<W>> connection;

    private NoSqlDatabaseManager(String string, int n, NoSqlProvider<NoSqlConnection<W, ? extends NoSqlObject<W>>> noSqlProvider) {
        super(string, n);
        this.provider = noSqlProvider;
    }

    @Override
    public final void startupInternal() {
    }

    @Override
    public final boolean shutdownInternal() {
        return Closer.closeSilently(this.connection);
    }

    @Override
    public final void connectAndStart() {
        try {
            this.connection = this.provider.getConnection();
            return;
        }
        catch (Exception exception) {
            throw new AppenderLoggingException("Failed to get connection from NoSQL connection provider.", exception);
        }
    }

    @Override
    public final void writeInternal(LogEvent logEvent, Serializable serializable) {
        if (!this.isRunning() || this.connection == null || this.connection.isClosed()) {
            throw new AppenderLoggingException("Cannot write logging event; NoSQL manager not connected to the database.");
        }
        NoSqlObject<W> noSqlObject = this.connection.createObject();
        if (serializable instanceof MapMessage) {
            this.setFields((MapMessage)serializable, noSqlObject);
        } else {
            this.setFields(logEvent, noSqlObject);
        }
        this.connection.insertObject(noSqlObject);
    }

    private void setFields(MapMessage<?, ?> mapMessage, final NoSqlObject<W> noSqlObject) {
        mapMessage.forEach(new BiConsumer<String, Object>(){

            @Override
            public void accept(String string, Object object) {
                noSqlObject.set(string, object);
            }
        });
    }

    private void setFields(LogEvent logEvent, NoSqlObject<W> noSqlObject) {
        NoSqlObject<W> noSqlObject2;
        Object object;
        noSqlObject.set("level", logEvent.getLevel());
        noSqlObject.set("loggerName", logEvent.getLoggerName());
        noSqlObject.set("message", logEvent.getMessage() == null ? null : logEvent.getMessage().getFormattedMessage());
        Serializable serializable = logEvent.getSource();
        if (serializable == null) {
            noSqlObject.set("source", (Object)null);
        } else {
            noSqlObject.set("source", this.convertStackTraceElement((StackTraceElement)serializable));
        }
        serializable = logEvent.getMarker();
        if (serializable == null) {
            noSqlObject.set("marker", (Object)null);
        } else {
            noSqlObject.set("marker", this.buildMarkerEntity((Marker)serializable));
        }
        noSqlObject.set("threadId", logEvent.getThreadId());
        noSqlObject.set("threadName", logEvent.getThreadName());
        noSqlObject.set("threadPriority", logEvent.getThreadPriority());
        noSqlObject.set("millis", logEvent.getTimeMillis());
        noSqlObject.set("date", new Date(logEvent.getTimeMillis()));
        serializable = logEvent.getThrown();
        if (serializable == null) {
            noSqlObject.set("thrown", (Object)null);
        } else {
            object = this.connection.createObject();
            noSqlObject2 = object;
            object.set("type", serializable.getClass().getName());
            noSqlObject2.set("message", ((Throwable)serializable).getMessage());
            noSqlObject2.set("stackTrace", this.convertStackTrace(((Throwable)serializable).getStackTrace()));
            while (((Throwable)serializable).getCause() != null) {
                serializable = ((Throwable)serializable).getCause();
                NoSqlObject<W> noSqlObject3 = this.connection.createObject();
                noSqlObject3.set("type", serializable.getClass().getName());
                noSqlObject3.set("message", ((Throwable)serializable).getMessage());
                noSqlObject3.set("stackTrace", this.convertStackTrace(((Throwable)serializable).getStackTrace()));
                noSqlObject2.set("cause", noSqlObject3);
                noSqlObject2 = noSqlObject3;
            }
            noSqlObject.set("thrown", (NoSqlObject<W>)object);
        }
        object = logEvent.getContextData();
        if (object == null) {
            noSqlObject.set("contextMap", (Object)null);
        } else {
            noSqlObject2 = this.connection.createObject();
            object.forEach(new BiConsumer<String, String>(){

                @Override
                public void accept(String string, String string2) {
                    noSqlObject2.set(string, string2);
                }
            });
            noSqlObject.set("contextMap", noSqlObject2);
        }
        noSqlObject2 = logEvent.getContextStack();
        if (noSqlObject2 == null) {
            noSqlObject.set("contextStack", (Object)null);
            return;
        }
        noSqlObject.set("contextStack", noSqlObject2.asList().toArray());
    }

    private NoSqlObject<W> buildMarkerEntity(Marker markerArray) {
        NoSqlObject<W> noSqlObject = this.connection.createObject();
        noSqlObject.set("name", markerArray.getName());
        markerArray = markerArray.getParents();
        if (markerArray != null) {
            NoSqlObject[] noSqlObjectArray = new NoSqlObject[markerArray.length];
            for (int j = 0; j < markerArray.length; ++j) {
                noSqlObjectArray[j] = this.buildMarkerEntity(markerArray[j]);
            }
            noSqlObject.set("parents", noSqlObjectArray);
        }
        return noSqlObject;
    }

    @Override
    public final boolean commitAndClose() {
        return true;
    }

    private NoSqlObject<W>[] convertStackTrace(StackTraceElement[] stackTraceElementArray) {
        NoSqlObject[] noSqlObjectArray = this.connection.createList(stackTraceElementArray.length);
        for (int j = 0; j < stackTraceElementArray.length; ++j) {
            noSqlObjectArray[j] = this.convertStackTraceElement(stackTraceElementArray[j]);
        }
        return noSqlObjectArray;
    }

    private NoSqlObject<W> convertStackTraceElement(StackTraceElement stackTraceElement) {
        NoSqlObject<W> noSqlObject = this.connection.createObject();
        noSqlObject.set("className", stackTraceElement.getClassName());
        noSqlObject.set("methodName", stackTraceElement.getMethodName());
        noSqlObject.set("fileName", stackTraceElement.getFileName());
        noSqlObject.set("lineNumber", stackTraceElement.getLineNumber());
        return noSqlObject;
    }

    public static NoSqlDatabaseManager<?> getNoSqlDatabaseManager(String string, int n, NoSqlProvider<?> noSqlProvider) {
        return (NoSqlDatabaseManager)AbstractDatabaseManager.getManager(string, new FactoryData(n, noSqlProvider), FACTORY);
    }

    static final class NoSQLDatabaseManagerFactory
    implements ManagerFactory<NoSqlDatabaseManager<?>, FactoryData> {
        private NoSQLDatabaseManagerFactory() {
        }

        @Override
        public final NoSqlDatabaseManager<?> createManager(String string, FactoryData factoryData) {
            return new NoSqlDatabaseManager(string, factoryData.getBufferSize(), factoryData.provider);
        }
    }

    static final class FactoryData
    extends AbstractDatabaseManager.AbstractFactoryData {
        private final NoSqlProvider<?> provider;

        protected FactoryData(int n, NoSqlProvider<?> noSqlProvider) {
            super(n, null);
            this.provider = noSqlProvider;
        }
    }
}

