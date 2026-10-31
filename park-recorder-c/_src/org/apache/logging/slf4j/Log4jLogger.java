/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.slf4j;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.slf4j.EventDataConverter;
import org.apache.logging.slf4j.Log4jMarker;
import org.apache.logging.slf4j.Log4jMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.impl.StaticMarkerBinder;
import org.slf4j.spi.LocationAwareLogger;

public class Log4jLogger
implements Serializable,
LocationAwareLogger {
    public static final String FQCN = Log4jLogger.class.getName();
    private static final long serialVersionUID = 7869000638091304316L;
    private static final Marker EVENT_MARKER = MarkerFactory.getMarker("EVENT");
    private static final EventDataConverter CONVERTER = Log4jLogger.createConverter();
    private final boolean eventLogger;
    private transient ExtendedLogger logger;
    private final String name;

    public Log4jLogger(ExtendedLogger extendedLogger, String string) {
        this.logger = extendedLogger;
        this.eventLogger = "EventLogger".equals(string);
        this.name = string;
    }

    @Override
    public void trace(String string) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, null, string);
    }

    @Override
    public void trace(String string, Object object) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, (org.apache.logging.log4j.Marker)null, string, object);
    }

    @Override
    public void trace(String string, Object object, Object object2) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, (org.apache.logging.log4j.Marker)null, string, object, object2);
    }

    @Override
    public void trace(String string, Object ... objectArray) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, (org.apache.logging.log4j.Marker)null, string, objectArray);
    }

    @Override
    public void trace(String string, Throwable throwable) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, (org.apache.logging.log4j.Marker)null, string, throwable);
    }

    @Override
    public boolean isTraceEnabled() {
        return this.logger.isEnabled(Level.TRACE, null, null);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return this.logger.isEnabled(Level.TRACE, Log4jLogger.getMarker(marker), null);
    }

    @Override
    public void trace(Marker marker, String string) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, Log4jLogger.getMarker(marker), string);
    }

    @Override
    public void trace(Marker marker, String string, Object object) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, Log4jLogger.getMarker(marker), string, object);
    }

    @Override
    public void trace(Marker marker, String string, Object object, Object object2) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, Log4jLogger.getMarker(marker), string, object, object2);
    }

    @Override
    public void trace(Marker marker, String string, Object ... objectArray) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, Log4jLogger.getMarker(marker), string, objectArray);
    }

    @Override
    public void trace(Marker marker, String string, Throwable throwable) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, Log4jLogger.getMarker(marker), string, throwable);
    }

    @Override
    public void debug(String string) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, null, string);
    }

    @Override
    public void debug(String string, Object object) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, (org.apache.logging.log4j.Marker)null, string, object);
    }

    @Override
    public void debug(String string, Object object, Object object2) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, (org.apache.logging.log4j.Marker)null, string, object, object2);
    }

    @Override
    public void debug(String string, Object ... objectArray) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, (org.apache.logging.log4j.Marker)null, string, objectArray);
    }

    @Override
    public void debug(String string, Throwable throwable) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, (org.apache.logging.log4j.Marker)null, string, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return this.logger.isEnabled(Level.DEBUG, null, null);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return this.logger.isEnabled(Level.DEBUG, Log4jLogger.getMarker(marker), null);
    }

    @Override
    public void debug(Marker marker, String string) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, Log4jLogger.getMarker(marker), string);
    }

    @Override
    public void debug(Marker marker, String string, Object object) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, Log4jLogger.getMarker(marker), string, object);
    }

    @Override
    public void debug(Marker marker, String string, Object object, Object object2) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, Log4jLogger.getMarker(marker), string, object, object2);
    }

    @Override
    public void debug(Marker marker, String string, Object ... objectArray) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, Log4jLogger.getMarker(marker), string, objectArray);
    }

    @Override
    public void debug(Marker marker, String string, Throwable throwable) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, Log4jLogger.getMarker(marker), string, throwable);
    }

    @Override
    public void info(String string) {
        this.logger.logIfEnabled(FQCN, Level.INFO, null, string);
    }

    @Override
    public void info(String string, Object object) {
        this.logger.logIfEnabled(FQCN, Level.INFO, (org.apache.logging.log4j.Marker)null, string, object);
    }

    @Override
    public void info(String string, Object object, Object object2) {
        this.logger.logIfEnabled(FQCN, Level.INFO, (org.apache.logging.log4j.Marker)null, string, object, object2);
    }

    @Override
    public void info(String string, Object ... objectArray) {
        this.logger.logIfEnabled(FQCN, Level.INFO, (org.apache.logging.log4j.Marker)null, string, objectArray);
    }

    @Override
    public void info(String string, Throwable throwable) {
        this.logger.logIfEnabled(FQCN, Level.INFO, (org.apache.logging.log4j.Marker)null, string, throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return this.logger.isEnabled(Level.INFO, null, null);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return this.logger.isEnabled(Level.INFO, Log4jLogger.getMarker(marker), null);
    }

    @Override
    public void info(Marker marker, String string) {
        this.logger.logIfEnabled(FQCN, Level.INFO, Log4jLogger.getMarker(marker), string);
    }

    @Override
    public void info(Marker marker, String string, Object object) {
        this.logger.logIfEnabled(FQCN, Level.INFO, Log4jLogger.getMarker(marker), string, object);
    }

    @Override
    public void info(Marker marker, String string, Object object, Object object2) {
        this.logger.logIfEnabled(FQCN, Level.INFO, Log4jLogger.getMarker(marker), string, object, object2);
    }

    @Override
    public void info(Marker marker, String string, Object ... objectArray) {
        this.logger.logIfEnabled(FQCN, Level.INFO, Log4jLogger.getMarker(marker), string, objectArray);
    }

    @Override
    public void info(Marker marker, String string, Throwable throwable) {
        this.logger.logIfEnabled(FQCN, Level.INFO, Log4jLogger.getMarker(marker), string, throwable);
    }

    @Override
    public void warn(String string) {
        this.logger.logIfEnabled(FQCN, Level.WARN, null, string);
    }

    @Override
    public void warn(String string, Object object) {
        this.logger.logIfEnabled(FQCN, Level.WARN, (org.apache.logging.log4j.Marker)null, string, object);
    }

    @Override
    public void warn(String string, Object object, Object object2) {
        this.logger.logIfEnabled(FQCN, Level.WARN, (org.apache.logging.log4j.Marker)null, string, object, object2);
    }

    @Override
    public void warn(String string, Object ... objectArray) {
        this.logger.logIfEnabled(FQCN, Level.WARN, (org.apache.logging.log4j.Marker)null, string, objectArray);
    }

    @Override
    public void warn(String string, Throwable throwable) {
        this.logger.logIfEnabled(FQCN, Level.WARN, (org.apache.logging.log4j.Marker)null, string, throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return this.logger.isEnabled(Level.WARN, null, null);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return this.logger.isEnabled(Level.WARN, Log4jLogger.getMarker(marker), null);
    }

    @Override
    public void warn(Marker marker, String string) {
        this.logger.logIfEnabled(FQCN, Level.WARN, Log4jLogger.getMarker(marker), string);
    }

    @Override
    public void warn(Marker marker, String string, Object object) {
        this.logger.logIfEnabled(FQCN, Level.WARN, Log4jLogger.getMarker(marker), string, object);
    }

    @Override
    public void warn(Marker marker, String string, Object object, Object object2) {
        this.logger.logIfEnabled(FQCN, Level.WARN, Log4jLogger.getMarker(marker), string, object, object2);
    }

    @Override
    public void warn(Marker marker, String string, Object ... objectArray) {
        this.logger.logIfEnabled(FQCN, Level.WARN, Log4jLogger.getMarker(marker), string, objectArray);
    }

    @Override
    public void warn(Marker marker, String string, Throwable throwable) {
        this.logger.logIfEnabled(FQCN, Level.WARN, Log4jLogger.getMarker(marker), string, throwable);
    }

    @Override
    public void error(String string) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, null, string);
    }

    @Override
    public void error(String string, Object object) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, (org.apache.logging.log4j.Marker)null, string, object);
    }

    @Override
    public void error(String string, Object object, Object object2) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, (org.apache.logging.log4j.Marker)null, string, object, object2);
    }

    @Override
    public void error(String string, Object ... objectArray) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, (org.apache.logging.log4j.Marker)null, string, objectArray);
    }

    @Override
    public void error(String string, Throwable throwable) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, (org.apache.logging.log4j.Marker)null, string, throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return this.logger.isEnabled(Level.ERROR, null, null);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return this.logger.isEnabled(Level.ERROR, Log4jLogger.getMarker(marker), null);
    }

    @Override
    public void error(Marker marker, String string) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, Log4jLogger.getMarker(marker), string);
    }

    @Override
    public void error(Marker marker, String string, Object object) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, Log4jLogger.getMarker(marker), string, object);
    }

    @Override
    public void error(Marker marker, String string, Object object, Object object2) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, Log4jLogger.getMarker(marker), string, object, object2);
    }

    @Override
    public void error(Marker marker, String string, Object ... objectArray) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, Log4jLogger.getMarker(marker), string, objectArray);
    }

    @Override
    public void error(Marker marker, String string, Throwable throwable) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, Log4jLogger.getMarker(marker), string, throwable);
    }

    @Override
    public void log(Marker serializable, String string, int n, String string2, Object[] objectArray, Throwable throwable) {
        org.apache.logging.log4j.Marker marker;
        Level level = Log4jLogger.getLevel(n);
        if (!this.logger.isEnabled(level, marker = Log4jLogger.getMarker((Marker)serializable), string2, objectArray)) {
            return;
        }
        if (CONVERTER != null && this.eventLogger && serializable != null && serializable.contains(EVENT_MARKER)) {
            serializable = CONVERTER.convertEvent(string2, objectArray, throwable);
        } else if (objectArray == null) {
            serializable = new SimpleMessage(string2);
        } else {
            serializable = new ParameterizedMessage(string2, objectArray, throwable);
            if (throwable != null) {
                throwable = serializable.getThrowable();
            }
        }
        this.logger.logMessage(string, level, marker, (Message)serializable, throwable);
    }

    private static org.apache.logging.log4j.Marker getMarker(Marker marker) {
        if (marker == null) {
            return null;
        }
        if (marker instanceof Log4jMarker) {
            return ((Log4jMarker)marker).getLog4jMarker();
        }
        return ((Log4jMarker)((Log4jMarkerFactory)StaticMarkerBinder.SINGLETON.getMarkerFactory()).getMarker(marker)).getLog4jMarker();
    }

    @Override
    public String getName() {
        return this.name;
    }

    private void readObject(ObjectInputStream objectInputStream) {
        objectInputStream.defaultReadObject();
        this.logger = LogManager.getContext().getLogger(this.name);
    }

    private void writeObject(ObjectOutputStream objectOutputStream) {
        objectOutputStream.defaultWriteObject();
    }

    private static EventDataConverter createConverter() {
        try {
            LoaderUtil.loadClass("org.slf4j.ext.EventData");
            return new EventDataConverter();
        }
        catch (ClassNotFoundException classNotFoundException) {
            return null;
        }
    }

    private static Level getLevel(int n) {
        switch (n) {
            case 0: {
                return Level.TRACE;
            }
            case 10: {
                return Level.DEBUG;
            }
            case 20: {
                return Level.INFO;
            }
            case 30: {
                return Level.WARN;
            }
            case 40: {
                return Level.ERROR;
            }
        }
        return Level.ERROR;
    }
}

