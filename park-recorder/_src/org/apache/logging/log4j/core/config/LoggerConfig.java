/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.async.AsyncLoggerConfig;
import org.apache.logging.log4j.core.async.AsyncLoggerContext;
import org.apache.logging.log4j.core.async.AsyncLoggerContextSelector;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.AppenderControlArraySet;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultReliabilityStrategy;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.ReliabilityStrategy;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.filter.AbstractFilterable;
import org.apache.logging.log4j.core.impl.DefaultLogEventFactory;
import org.apache.logging.log4j.core.impl.LocationAware;
import org.apache.logging.log4j.core.impl.LocationAwareLogEventFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.impl.LogEventFactory;
import org.apache.logging.log4j.core.impl.ReusableLogEventFactory;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.apache.logging.log4j.util.Strings;

@Plugin(name="logger", category="Core", printObject=true)
public class LoggerConfig
extends AbstractFilterable
implements LocationAware {
    public static final String ROOT = "root";
    private static LogEventFactory LOG_EVENT_FACTORY = null;
    private List<AppenderRef> appenderRefs = new ArrayList<AppenderRef>();
    private final AppenderControlArraySet appenders = new AppenderControlArraySet();
    private final String name;
    private LogEventFactory logEventFactory = LOG_EVENT_FACTORY;
    private Level level;
    private boolean additive = true;
    private boolean includeLocation = true;
    private LoggerConfig parent;
    private Map<Property, Boolean> propertiesMap;
    private final List<Property> properties;
    private final boolean propertiesRequireLookup;
    private final Configuration config;
    private final ReliabilityStrategy reliabilityStrategy;

    public LoggerConfig() {
        this.level = Level.ERROR;
        this.name = "";
        this.properties = null;
        this.propertiesRequireLookup = false;
        this.config = null;
        this.reliabilityStrategy = new DefaultReliabilityStrategy(this);
    }

    public LoggerConfig(String string, Level level, boolean bl) {
        this.name = string;
        this.level = level;
        this.additive = bl;
        this.properties = null;
        this.propertiesRequireLookup = false;
        this.config = null;
        this.reliabilityStrategy = new DefaultReliabilityStrategy(this);
    }

    public LoggerConfig(String string, List<AppenderRef> list, Filter filter, Level level, boolean bl, Property[] propertyArray, Configuration configuration, boolean bl2) {
        super(filter);
        this.name = string;
        this.appenderRefs = list;
        this.level = level;
        this.additive = bl;
        this.includeLocation = bl2;
        this.config = configuration;
        this.properties = propertyArray != null && propertyArray.length > 0 ? Collections.unmodifiableList(Arrays.asList(Arrays.copyOf(propertyArray, propertyArray.length))) : null;
        this.propertiesRequireLookup = LoggerConfig.containsPropertyRequiringLookup(propertyArray);
        this.reliabilityStrategy = configuration.getReliabilityStrategy(this);
    }

    private static boolean containsPropertyRequiringLookup(Property[] propertyArray) {
        if (propertyArray == null) {
            return false;
        }
        for (int j = 0; j < propertyArray.length; ++j) {
            if (!propertyArray[j].isValueNeedsLookup()) continue;
            return true;
        }
        return false;
    }

    @Override
    public Filter getFilter() {
        return super.getFilter();
    }

    public String getName() {
        return this.name;
    }

    public void setParent(LoggerConfig loggerConfig) {
        this.parent = loggerConfig;
    }

    public LoggerConfig getParent() {
        return this.parent;
    }

    public void addAppender(Appender appender, Level level, Filter filter) {
        this.appenders.add(new AppenderControl(appender, level, filter));
    }

    public void removeAppender(String string) {
        AppenderControl appenderControl;
        while ((appenderControl = this.appenders.remove(string)) != null) {
            this.cleanupFilter(appenderControl);
        }
    }

    public Map<String, Appender> getAppenders() {
        return this.appenders.asMap();
    }

    protected void clearAppenders() {
        do {
            for (AppenderControl appenderControl : this.appenders.clear()) {
                this.cleanupFilter(appenderControl);
            }
        } while (!this.appenders.isEmpty());
    }

    private void cleanupFilter(AppenderControl appenderControl) {
        Filter filter = appenderControl.getFilter();
        if (filter != null) {
            appenderControl.removeFilter(filter);
            filter.stop();
        }
    }

    public List<AppenderRef> getAppenderRefs() {
        return this.appenderRefs;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        if (this.level == null) {
            if (this.parent == null) {
                return Level.ERROR;
            }
            return this.parent.getLevel();
        }
        return this.level;
    }

    public LogEventFactory getLogEventFactory() {
        return this.logEventFactory;
    }

    public void setLogEventFactory(LogEventFactory logEventFactory) {
        this.logEventFactory = logEventFactory;
    }

    public boolean isAdditive() {
        return this.additive;
    }

    public void setAdditive(boolean bl) {
        this.additive = bl;
    }

    public boolean isIncludeLocation() {
        return this.includeLocation;
    }

    @Deprecated
    public Map<Property, Boolean> getProperties() {
        if (this.properties == null) {
            return null;
        }
        if (this.propertiesMap == null) {
            HashMap<Property, Boolean> hashMap = new HashMap<Property, Boolean>(this.properties.size() * 2);
            for (int j = 0; j < this.properties.size(); ++j) {
                hashMap.put(this.properties.get(j), this.properties.get(j).isValueNeedsLookup());
            }
            this.propertiesMap = Collections.unmodifiableMap(hashMap);
        }
        return this.propertiesMap;
    }

    public List<Property> getPropertyList() {
        return this.properties;
    }

    public boolean isPropertiesRequireLookup() {
        return this.propertiesRequireLookup;
    }

    @PerformanceSensitive(value={"allocation"})
    public void log(String string, String string2, Marker marker, Level level, Message message, Throwable throwable) {
        LogEvent logEvent;
        List<Property> list = null;
        if (!this.propertiesRequireLookup) {
            list = this.properties;
        } else if (this.properties != null) {
            list = new ArrayList<Property>(this.properties.size());
            logEvent = Log4jLogEvent.newBuilder().setMessage(message).setMarker(marker).setLevel(level).setLoggerName(string).setLoggerFqcn(string2).setThrown(throwable).build();
            for (int j = 0; j < this.properties.size(); ++j) {
                Property property = this.properties.get(j);
                String string3 = property.isValueNeedsLookup() ? this.config.getStrSubstitutor().replace(logEvent, property.getValue()) : property.getValue();
                list.add(Property.createProperty(property.getName(), string3));
            }
        }
        logEvent = this.logEventFactory instanceof LocationAwareLogEventFactory ? ((LocationAwareLogEventFactory)((Object)this.logEventFactory)).createEvent(string, marker, string2, this.requiresLocation() ? StackLocatorUtil.calcLocation(string2) : null, level, message, list, throwable) : this.logEventFactory.createEvent(string, marker, string2, level, message, list, throwable);
        try {
            this.log(logEvent, LoggerConfigPredicate.ALL);
            return;
        }
        finally {
            ReusableLogEventFactory.release(logEvent);
        }
    }

    @PerformanceSensitive(value={"allocation"})
    public void log(String string, String string2, StackTraceElement stackTraceElement, Marker marker, Level level, Message message, Throwable throwable) {
        LogEvent logEvent;
        List<Property> list = null;
        if (!this.propertiesRequireLookup) {
            list = this.properties;
        } else if (this.properties != null) {
            list = new ArrayList<Property>(this.properties.size());
            logEvent = Log4jLogEvent.newBuilder().setMessage(message).setMarker(marker).setLevel(level).setLoggerName(string).setLoggerFqcn(string2).setThrown(throwable).build();
            for (int j = 0; j < this.properties.size(); ++j) {
                Property property = this.properties.get(j);
                String string3 = property.isValueNeedsLookup() ? this.config.getStrSubstitutor().replace(logEvent, property.getValue()) : property.getValue();
                list.add(Property.createProperty(property.getName(), string3));
            }
        }
        logEvent = this.logEventFactory instanceof LocationAwareLogEventFactory ? ((LocationAwareLogEventFactory)((Object)this.logEventFactory)).createEvent(string, marker, string2, stackTraceElement, level, message, list, throwable) : this.logEventFactory.createEvent(string, marker, string2, level, message, list, throwable);
        try {
            this.log(logEvent, LoggerConfigPredicate.ALL);
            return;
        }
        finally {
            ReusableLogEventFactory.release(logEvent);
        }
    }

    public void log(LogEvent logEvent) {
        this.log(logEvent, LoggerConfigPredicate.ALL);
    }

    public void log(LogEvent logEvent, LoggerConfigPredicate loggerConfigPredicate) {
        if (!this.isFiltered(logEvent)) {
            this.processLogEvent(logEvent, loggerConfigPredicate);
        }
    }

    public ReliabilityStrategy getReliabilityStrategy() {
        return this.reliabilityStrategy;
    }

    private void processLogEvent(LogEvent logEvent, LoggerConfigPredicate loggerConfigPredicate) {
        logEvent.setIncludeLocation(this.isIncludeLocation());
        if (loggerConfigPredicate.allow(this)) {
            this.callAppenders(logEvent);
        }
        this.logParent(logEvent, loggerConfigPredicate);
    }

    @Override
    public boolean requiresLocation() {
        if (!this.includeLocation) {
            return false;
        }
        AppenderControl[] appenderControlArray = this.appenders.get();
        LoggerConfig loggerConfig = this;
        while (loggerConfig != null) {
            AppenderControl[] appenderControlArray2 = appenderControlArray;
            int n = appenderControlArray.length;
            for (int j = 0; j < n; ++j) {
                Appender appender = appenderControlArray2[j].getAppender();
                if (!(appender instanceof LocationAware) || !((LocationAware)((Object)appender)).requiresLocation()) continue;
                return true;
            }
            if (!loggerConfig.additive) break;
            loggerConfig = loggerConfig.parent;
            if (loggerConfig == null) continue;
            appenderControlArray = loggerConfig.appenders.get();
        }
        return false;
    }

    private void logParent(LogEvent logEvent, LoggerConfigPredicate loggerConfigPredicate) {
        if (this.additive && this.parent != null) {
            this.parent.log(logEvent, loggerConfigPredicate);
        }
    }

    @PerformanceSensitive(value={"allocation"})
    public void callAppenders(LogEvent logEvent) {
        AppenderControl[] appenderControlArray = this.appenders.get();
        for (int j = 0; j < appenderControlArray.length; ++j) {
            appenderControlArray[j].callAppender(logEvent);
        }
    }

    public String toString() {
        if (Strings.isEmpty(this.name)) {
            return ROOT;
        }
        return this.name;
    }

    @Deprecated
    public static LoggerConfig createLogger(String string, Level level, @PluginAttribute(value="name") String string2, String string3, AppenderRef[] object, Property[] propertyArray, @PluginConfiguration Configuration configuration, Filter filter) {
        if (string2 == null) {
            LOGGER.error("Loggers cannot be configured without a name");
            return null;
        }
        object = Arrays.asList(object);
        string2 = string2.equals(ROOT) ? "" : string2;
        boolean bl = Booleans.parseBoolean(string, true);
        return new LoggerConfig(string2, (List<AppenderRef>)object, filter, level, bl, propertyArray, configuration, LoggerConfig.includeLocation(string3, configuration));
    }

    @PluginFactory
    public static LoggerConfig createLogger(@PluginAttribute(value="additivity", defaultBoolean=true) boolean bl, @PluginAttribute(value="level") Level level, @Required(message="Loggers cannot be configured without a name") @PluginAttribute(value="name") String string, @PluginAttribute(value="includeLocation") String string2, @PluginElement(value="AppenderRef") AppenderRef[] appenderRefArray, @PluginElement(value="Properties") Property[] propertyArray, @PluginConfiguration Configuration configuration, @PluginElement(value="Filter") Filter filter) {
        string = string.equals(ROOT) ? "" : string;
        return new LoggerConfig(string, Arrays.asList(appenderRefArray), filter, level, bl, propertyArray, configuration, LoggerConfig.includeLocation(string2, configuration));
    }

    @Deprecated
    protected static boolean includeLocation(String string) {
        return LoggerConfig.includeLocation(string, null);
    }

    protected static boolean includeLocation(String object, Configuration configuration) {
        if (object == null) {
            object = null;
            if (configuration != null) {
                object = configuration.getLoggerContext();
            }
            if (object != null) {
                return !(object instanceof AsyncLoggerContext);
            }
            return !AsyncLoggerContextSelector.isSelected();
        }
        return Boolean.parseBoolean((String)object);
    }

    protected final boolean hasAppenders() {
        return !this.appenders.isEmpty();
    }

    static {
        String string = PropertiesUtil.getProperties().getStringProperty("Log4jLogEventFactory");
        if (string != null) {
            try {
                Class<?> clazz = Loader.loadClass(string);
                if (clazz != null && LogEventFactory.class.isAssignableFrom(clazz)) {
                    LOG_EVENT_FACTORY = (LogEventFactory)clazz.newInstance();
                }
            }
            catch (Exception exception) {
                LOGGER.error("Unable to create LogEventFactory {}", (Object)string, (Object)exception);
            }
        }
        if (LOG_EVENT_FACTORY == null) {
            LOG_EVENT_FACTORY = Constants.ENABLE_THREADLOCALS ? new ReusableLogEventFactory() : new DefaultLogEventFactory();
        }
    }

    public static enum LoggerConfigPredicate {
        ALL{

            @Override
            final boolean allow(LoggerConfig loggerConfig) {
                return true;
            }
        }
        ,
        ASYNCHRONOUS_ONLY{

            @Override
            final boolean allow(LoggerConfig loggerConfig) {
                return loggerConfig instanceof AsyncLoggerConfig;
            }
        }
        ,
        SYNCHRONOUS_ONLY{

            @Override
            final boolean allow(LoggerConfig loggerConfig) {
                return !ASYNCHRONOUS_ONLY.allow(loggerConfig);
            }
        };


        abstract boolean allow(LoggerConfig var1);
    }

    @Plugin(name="root", category="Core", printObject=true)
    public static class RootLogger
    extends LoggerConfig {
        @PluginFactory
        public static LoggerConfig createLogger(@PluginAttribute(value="additivity") String string, @PluginAttribute(value="level") Level level, @PluginAttribute(value="includeLocation") String string2, @PluginElement(value="AppenderRef") AppenderRef[] object, @PluginElement(value="Properties") Property[] propertyArray, @PluginConfiguration Configuration configuration, @PluginElement(value="Filter") Filter filter) {
            object = Arrays.asList(object);
            level = level == null ? Level.ERROR : level;
            boolean bl = Booleans.parseBoolean(string, true);
            return new LoggerConfig("", (List<AppenderRef>)object, filter, level, bl, propertyArray, configuration, RootLogger.includeLocation(string2, configuration));
        }
    }
}

