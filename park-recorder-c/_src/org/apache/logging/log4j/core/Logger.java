/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LocationAwareReliabilityStrategy;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.ReliabilityStrategy;
import org.apache.logging.log4j.core.filter.CompositeFilter;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.util.Supplier;

public class Logger
extends AbstractLogger
implements Supplier<LoggerConfig> {
    private static final long serialVersionUID = 1L;
    protected volatile PrivateConfig privateConfig;
    private final LoggerContext context;

    public Logger(LoggerContext loggerContext, String string, MessageFactory messageFactory) {
        super(string, messageFactory);
        this.context = loggerContext;
        this.privateConfig = new PrivateConfig(loggerContext.getConfiguration(), this);
    }

    protected Object writeReplace() {
        return new LoggerProxy(this.getName(), (MessageFactory)this.getMessageFactory());
    }

    public Logger getParent() {
        Object MF;
        Object object = this.privateConfig.loggerConfig.getName().equals(this.getName()) ? this.privateConfig.loggerConfig.getParent() : this.privateConfig.loggerConfig;
        if (object == null) {
            return null;
        }
        if (this.context.hasLogger((String)(object = ((LoggerConfig)object).getName()), (MessageFactory)(MF = this.getMessageFactory()))) {
            return this.context.getLogger((String)object, (MessageFactory)MF);
        }
        return new Logger(this.context, (String)object, (MessageFactory)MF);
    }

    public LoggerContext getContext() {
        return this.context;
    }

    public synchronized void setLevel(Level serializable) {
        if (serializable == this.getLevel()) {
            return;
        }
        serializable = serializable != null ? serializable : ((serializable = this.getParent()) != null ? ((Logger)serializable).getLevel() : this.privateConfig.loggerConfigLevel);
        Logger logger = this;
        this.privateConfig = logger.new PrivateConfig(logger.privateConfig, (Level)serializable);
    }

    @Override
    public LoggerConfig get() {
        return this.privateConfig.loggerConfig;
    }

    @Override
    public boolean requiresLocation() {
        return this.privateConfig.requiresLocation;
    }

    @Override
    public void logMessage(String string, Level level, Marker marker, Message message, Throwable throwable) {
        message = message == null ? new SimpleMessage("") : message;
        Logger logger = this;
        this.privateConfig.loggerConfig.getReliabilityStrategy().log(logger, logger.getName(), string, marker, level, message, throwable);
    }

    @Override
    public void log(Level level, Marker marker, String string, StackTraceElement stackTraceElement, Message message, Throwable throwable) {
        ReliabilityStrategy reliabilityStrategy = this.privateConfig.loggerConfig.getReliabilityStrategy();
        if (reliabilityStrategy instanceof LocationAwareReliabilityStrategy) {
            Logger logger = this;
            ((LocationAwareReliabilityStrategy)((Object)reliabilityStrategy)).log(logger, logger.getName(), string, stackTraceElement, marker, level, message, throwable);
            return;
        }
        Logger logger = this;
        reliabilityStrategy.log(logger, logger.getName(), string, marker, level, message, throwable);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Throwable throwable) {
        return this.privateConfig.filter(level, marker, string, throwable);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string) {
        return this.privateConfig.filter(level, marker, string);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object ... objectArray) {
        return this.privateConfig.filter(level, marker, string, objectArray);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object) {
        return this.privateConfig.filter(level, marker, string, object);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2) {
        return this.privateConfig.filter(level, marker, string, object, object2);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3) {
        return this.privateConfig.filter(level, marker, string, object, object2, object3);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        return this.privateConfig.filter(level, marker, string, object, object2, object3, object4);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        return this.privateConfig.filter(level, marker, string, object, object2, object3, object4, object5);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        return this.privateConfig.filter(level, marker, string, object, object2, object3, object4, object5, object6);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        return this.privateConfig.filter(level, marker, string, object, object2, object3, object4, object5, object6, object7);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        return this.privateConfig.filter(level, marker, string, object, object2, object3, object4, object5, object6, object7, object8);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        return this.privateConfig.filter(level, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        return this.privateConfig.filter(level, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, CharSequence charSequence, Throwable throwable) {
        return this.privateConfig.filter(level, marker, charSequence, throwable);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Object object, Throwable throwable) {
        return this.privateConfig.filter(level, marker, object, throwable);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Message message, Throwable throwable) {
        return this.privateConfig.filter(level, marker, message, throwable);
    }

    public void addAppender(Appender appender) {
        this.privateConfig.config.addLoggerAppender(this, appender);
    }

    public void removeAppender(Appender appender) {
        this.privateConfig.loggerConfig.removeAppender(appender.getName());
    }

    public Map<String, Appender> getAppenders() {
        return this.privateConfig.loggerConfig.getAppenders();
    }

    public Iterator<Filter> getFilters() {
        Filter filter = this.privateConfig.loggerConfig.getFilter();
        if (filter == null) {
            return new ArrayList().iterator();
        }
        if (filter instanceof CompositeFilter) {
            return ((CompositeFilter)filter).iterator();
        }
        ArrayList<Filter> arrayList = new ArrayList<Filter>();
        arrayList.add(filter);
        return arrayList.iterator();
    }

    @Override
    public Level getLevel() {
        return this.privateConfig.loggerConfigLevel;
    }

    public int filterCount() {
        Filter filter = this.privateConfig.loggerConfig.getFilter();
        if (filter == null) {
            return 0;
        }
        if (filter instanceof CompositeFilter) {
            return ((CompositeFilter)filter).size();
        }
        return 1;
    }

    public void addFilter(Filter filter) {
        this.privateConfig.config.addLoggerFilter(this, filter);
    }

    public boolean isAdditive() {
        return this.privateConfig.loggerConfig.isAdditive();
    }

    public void setAdditive(boolean bl) {
        this.privateConfig.config.setLoggerAdditive(this, bl);
    }

    public void updateConfiguration(Configuration configuration) {
        this.privateConfig = new PrivateConfig(configuration, this);
    }

    public String toString() {
        String string = this.getName() + ':' + this.getLevel();
        if (this.context == null) {
            return string;
        }
        String string2 = this.context.getName();
        if (string2 == null) {
            return string;
        }
        return string + " in " + string2;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        object = (Logger)object;
        return this.getName().equals(((AbstractLogger)object).getName());
    }

    public int hashCode() {
        return this.getName().hashCode();
    }

    protected static class LoggerProxy
    implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String name;
        private final MessageFactory messageFactory;

        public LoggerProxy(String string, MessageFactory messageFactory) {
            this.name = string;
            this.messageFactory = messageFactory;
        }

        protected Object readResolve() {
            return new Logger(LoggerContext.getContext(), this.name, this.messageFactory);
        }
    }

    public class PrivateConfig {
        public final LoggerConfig loggerConfig;
        public final Configuration config;
        private final Level loggerConfigLevel;
        private final int intLevel;
        private final Logger logger;
        private final boolean requiresLocation;

        public PrivateConfig(Configuration configuration, Logger logger2) {
            this.config = configuration;
            this.loggerConfig = configuration.getLoggerConfig(Logger.this.getName());
            this.loggerConfigLevel = this.loggerConfig.getLevel();
            this.intLevel = this.loggerConfigLevel.intLevel();
            this.logger = logger2;
            this.requiresLocation = this.loggerConfig.requiresLocation();
        }

        public PrivateConfig(PrivateConfig privateConfig, Level level) {
            this.config = privateConfig.config;
            this.loggerConfig = privateConfig.loggerConfig;
            this.loggerConfigLevel = level;
            this.intLevel = this.loggerConfigLevel.intLevel();
            this.logger = privateConfig.logger;
            this.requiresLocation = this.loggerConfig.requiresLocation();
        }

        public PrivateConfig(PrivateConfig privateConfig, LoggerConfig loggerConfig) {
            this.config = privateConfig.config;
            this.loggerConfig = loggerConfig;
            this.loggerConfigLevel = loggerConfig.getLevel();
            this.intLevel = this.loggerConfigLevel.intLevel();
            this.logger = privateConfig.logger;
            this.requiresLocation = this.loggerConfig.requiresLocation();
        }

        public void logEvent(LogEvent logEvent) {
            this.loggerConfig.log(logEvent);
        }

        boolean filter(Level level, Marker object, String string) {
            Filter filter = this.config.getFilter();
            if (filter != null && (object = filter.filter(this.logger, level, (Marker)object, string, new Object[0])) != Filter.Result.NEUTRAL) {
                return object == Filter.Result.ACCEPT;
            }
            return level != null && this.intLevel >= level.intLevel();
        }

        boolean filter(Level level, Marker object, String string, Throwable throwable) {
            Filter filter = this.config.getFilter();
            if (filter != null && (object = filter.filter(this.logger, level, (Marker)object, (Object)string, throwable)) != Filter.Result.NEUTRAL) {
                return object == Filter.Result.ACCEPT;
            }
            return level != null && this.intLevel >= level.intLevel();
        }

        boolean filter(Level level, Marker object, String string, Object ... objectArray) {
            Filter filter = this.config.getFilter();
            if (filter != null && (object = filter.filter(this.logger, level, (Marker)object, string, objectArray)) != Filter.Result.NEUTRAL) {
                return object == Filter.Result.ACCEPT;
            }
            return level != null && this.intLevel >= level.intLevel();
        }

        boolean filter(Level level, Marker object, String string, Object object2) {
            Filter filter = this.config.getFilter();
            if (filter != null && (object = filter.filter(this.logger, level, (Marker)object, string, object2)) != Filter.Result.NEUTRAL) {
                return object == Filter.Result.ACCEPT;
            }
            return level != null && this.intLevel >= level.intLevel();
        }

        boolean filter(Level level, Marker object, String string, Object object2, Object object3) {
            Filter filter = this.config.getFilter();
            if (filter != null && (object = filter.filter(this.logger, level, (Marker)object, string, object2, object3)) != Filter.Result.NEUTRAL) {
                return object == Filter.Result.ACCEPT;
            }
            return level != null && this.intLevel >= level.intLevel();
        }

        boolean filter(Level level, Marker object, String string, Object object2, Object object3, Object object4) {
            Filter filter = this.config.getFilter();
            if (filter != null && (object = filter.filter(this.logger, level, (Marker)object, string, object2, object3, object4)) != Filter.Result.NEUTRAL) {
                return object == Filter.Result.ACCEPT;
            }
            return level != null && this.intLevel >= level.intLevel();
        }

        boolean filter(Level level, Marker object, String string, Object object2, Object object3, Object object4, Object object5) {
            Filter filter = this.config.getFilter();
            if (filter != null && (object = filter.filter(this.logger, level, (Marker)object, string, object2, object3, object4, object5)) != Filter.Result.NEUTRAL) {
                return object == Filter.Result.ACCEPT;
            }
            return level != null && this.intLevel >= level.intLevel();
        }

        boolean filter(Level level, Marker object, String string, Object object2, Object object3, Object object4, Object object5, Object object6) {
            Filter filter = this.config.getFilter();
            if (filter != null && (object = filter.filter(this.logger, level, (Marker)object, string, object2, object3, object4, object5, object6)) != Filter.Result.NEUTRAL) {
                return object == Filter.Result.ACCEPT;
            }
            return level != null && this.intLevel >= level.intLevel();
        }

        boolean filter(Level level, Marker object, String string, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
            Filter filter = this.config.getFilter();
            if (filter != null && (object = filter.filter(this.logger, level, (Marker)object, string, object2, object3, object4, object5, object6, object7)) != Filter.Result.NEUTRAL) {
                return object == Filter.Result.ACCEPT;
            }
            return level != null && this.intLevel >= level.intLevel();
        }

        boolean filter(Level level, Marker object, String string, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
            Filter filter = this.config.getFilter();
            if (filter != null && (object = filter.filter(this.logger, level, (Marker)object, string, object2, object3, object4, object5, object6, object7, object8)) != Filter.Result.NEUTRAL) {
                return object == Filter.Result.ACCEPT;
            }
            return level != null && this.intLevel >= level.intLevel();
        }

        boolean filter(Level level, Marker object, String string, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
            Filter filter = this.config.getFilter();
            if (filter != null && (object = filter.filter(this.logger, level, (Marker)object, string, object2, object3, object4, object5, object6, object7, object8, object9)) != Filter.Result.NEUTRAL) {
                return object == Filter.Result.ACCEPT;
            }
            return level != null && this.intLevel >= level.intLevel();
        }

        boolean filter(Level level, Marker object, String string, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
            Filter filter = this.config.getFilter();
            if (filter != null && (object = filter.filter(this.logger, level, (Marker)object, string, object2, object3, object4, object5, object6, object7, object8, object9, object10)) != Filter.Result.NEUTRAL) {
                return object == Filter.Result.ACCEPT;
            }
            return level != null && this.intLevel >= level.intLevel();
        }

        boolean filter(Level level, Marker object, String string, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11) {
            Filter filter = this.config.getFilter();
            if (filter != null && (object = filter.filter(this.logger, level, (Marker)object, string, object2, object3, object4, object5, object6, object7, object8, object9, object10, object11)) != Filter.Result.NEUTRAL) {
                return object == Filter.Result.ACCEPT;
            }
            return level != null && this.intLevel >= level.intLevel();
        }

        boolean filter(Level level, Marker object, CharSequence charSequence, Throwable throwable) {
            Filter filter = this.config.getFilter();
            if (filter != null && (object = filter.filter(this.logger, level, (Marker)object, charSequence, throwable)) != Filter.Result.NEUTRAL) {
                return object == Filter.Result.ACCEPT;
            }
            return level != null && this.intLevel >= level.intLevel();
        }

        boolean filter(Level level, Marker object, Object object2, Throwable throwable) {
            Filter filter = this.config.getFilter();
            if (filter != null && (object = filter.filter(this.logger, level, (Marker)object, object2, throwable)) != Filter.Result.NEUTRAL) {
                return object == Filter.Result.ACCEPT;
            }
            return level != null && this.intLevel >= level.intLevel();
        }

        boolean filter(Level level, Marker object, Message message, Throwable throwable) {
            Filter filter = this.config.getFilter();
            if (filter != null && (object = filter.filter(this.logger, level, (Marker)object, message, throwable)) != Filter.Result.NEUTRAL) {
                return object == Filter.Result.ACCEPT;
            }
            return level != null && this.intLevel >= level.intLevel();
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("PrivateConfig [loggerConfig=");
            stringBuilder.append(this.loggerConfig);
            stringBuilder.append(", config=");
            stringBuilder.append(this.config);
            stringBuilder.append(", loggerConfigLevel=");
            stringBuilder.append(this.loggerConfigLevel);
            stringBuilder.append(", intLevel=");
            stringBuilder.append(this.intLevel);
            stringBuilder.append(", logger=");
            stringBuilder.append(this.logger);
            stringBuilder.append("]");
            return stringBuilder.toString();
        }
    }
}

