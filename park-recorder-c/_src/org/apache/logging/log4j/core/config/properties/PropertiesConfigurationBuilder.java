/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.properties;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.AppenderRefComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.FilterComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.FilterableComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LoggableComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ScriptComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ScriptFileComponentBuilder;
import org.apache.logging.log4j.core.config.properties.PropertiesConfiguration;
import org.apache.logging.log4j.core.util.Builder;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.Strings;

public class PropertiesConfigurationBuilder
extends ConfigurationBuilderFactory
implements Builder<PropertiesConfiguration> {
    private static final String ADVERTISER_KEY = "advertiser";
    private static final String STATUS_KEY = "status";
    private static final String SHUTDOWN_HOOK = "shutdownHook";
    private static final String SHUTDOWN_TIMEOUT = "shutdownTimeout";
    private static final String VERBOSE = "verbose";
    private static final String DEST = "dest";
    private static final String PACKAGES = "packages";
    private static final String CONFIG_NAME = "name";
    private static final String MONITOR_INTERVAL = "monitorInterval";
    private static final String CONFIG_TYPE = "type";
    private final ConfigurationBuilder<PropertiesConfiguration> builder = PropertiesConfigurationBuilder.newConfigurationBuilder(PropertiesConfiguration.class);
    private LoggerContext loggerContext;
    private Properties rootProperties;

    public PropertiesConfigurationBuilder setRootProperties(Properties properties) {
        this.rootProperties = properties;
        return this;
    }

    public PropertiesConfigurationBuilder setConfigurationSource(ConfigurationSource configurationSource) {
        this.builder.setConfigurationSource(configurationSource);
        return this;
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public PropertiesConfiguration build() {
        Properties properties;
        String string;
        String string22;
        for (String object42 : this.rootProperties.stringPropertyNames()) {
            if (object42.contains(".")) continue;
            this.builder.addRootProperty(object42, this.rootProperties.getProperty(object42));
        }
        this.builder.setStatusLevel(Level.toLevel(this.rootProperties.getProperty(STATUS_KEY), Level.ERROR)).setShutdownHook(this.rootProperties.getProperty(SHUTDOWN_HOOK)).setShutdownTimeout(Long.parseLong(this.rootProperties.getProperty(SHUTDOWN_TIMEOUT, "0")), TimeUnit.MILLISECONDS).setVerbosity(this.rootProperties.getProperty(VERBOSE)).setDestination(this.rootProperties.getProperty(DEST)).setPackages(this.rootProperties.getProperty(PACKAGES)).setConfigurationName(this.rootProperties.getProperty(CONFIG_NAME)).setMonitorInterval(this.rootProperties.getProperty(MONITOR_INTERVAL, "0")).setAdvertiser(this.rootProperties.getProperty(ADVERTISER_KEY));
        Object object5 = PropertiesUtil.extractSubset(this.rootProperties, "property");
        for (String string3 : ((Properties)object5).stringPropertyNames()) {
            this.builder.addProperty(string3, ((Properties)object5).getProperty(string3));
        }
        Iterator<Map.Entry<String, Properties>> iterator = PropertiesUtil.partitionOnCommonPrefixes(PropertiesUtil.extractSubset(this.rootProperties, "script")).entrySet().iterator();
        while (iterator.hasNext()) {
            Properties properties2 = iterator.next().getValue();
            object5 = (String)properties2.remove(CONFIG_TYPE);
            if (object5 == null) {
                throw new ConfigurationException("No type provided for script - must be Script or ScriptFile");
            }
            if (((String)object5).equalsIgnoreCase("script")) {
                this.builder.add(this.createScript(properties2));
                continue;
            }
            this.builder.add(this.createScriptFile(properties2));
        }
        Properties properties3 = PropertiesUtil.extractSubset(this.rootProperties, "customLevel");
        if (properties3.size() > 0) {
            object5 = properties3.stringPropertyNames().iterator();
            while (object5.hasNext()) {
                String string4 = (String)object5.next();
                this.builder.add(this.builder.newCustomLevel(string4, Integer.parseInt(properties3.getProperty(string4))));
            }
        }
        if ((object5 = this.rootProperties.getProperty("filters")) != null) {
            void var3_20;
            object5 = ((String)object5).split(",");
            int n = ((String[])object5).length;
            boolean bl = false;
            while (var3_20 < n) {
                string22 = ((String)object5[var3_20]).trim();
                this.builder.add(this.createFilter(string22, PropertiesUtil.extractSubset(this.rootProperties, "filter.".concat(String.valueOf(string22)))));
                ++var3_20;
            }
        } else {
            object5 = PropertiesUtil.partitionOnCommonPrefixes(PropertiesUtil.extractSubset(this.rootProperties, "filter")).entrySet().iterator();
            while (object5.hasNext()) {
                Map.Entry entry = (Map.Entry)object5.next();
                this.builder.add(this.createFilter(((String)entry.getKey()).trim(), (Properties)entry.getValue()));
            }
        }
        if ((string = this.rootProperties.getProperty("appenders")) != null) {
            for (String string22 : string.split(",")) {
                object5 = string22.trim();
                this.builder.add(this.createAppender(string22.trim(), PropertiesUtil.extractSubset(this.rootProperties, "appender.".concat(String.valueOf(object5)))));
            }
        } else {
            for (Map.Entry<String, Properties> entry : PropertiesUtil.partitionOnCommonPrefixes(PropertiesUtil.extractSubset(this.rootProperties, "appender")).entrySet()) {
                this.builder.add(this.createAppender(entry.getKey().trim(), entry.getValue()));
            }
        }
        if ((object5 = this.rootProperties.getProperty("loggers")) != null) {
            String[] stringArray = ((String)object5).split(",");
            int n = stringArray.length;
            for (int i2 = 0; i2 < n; ++i2) {
                object5 = stringArray[i2].trim();
                if (((String)object5).equals("root")) continue;
                this.builder.add(this.createLogger((String)object5, PropertiesUtil.extractSubset(this.rootProperties, "logger.".concat(String.valueOf(object5)))));
            }
        } else {
            for (Map.Entry<String, Properties> entry : PropertiesUtil.partitionOnCommonPrefixes(PropertiesUtil.extractSubset(this.rootProperties, "logger")).entrySet()) {
                string22 = entry.getKey().trim();
                if (string22.equals("root")) continue;
                this.builder.add(this.createLogger(string22, entry.getValue()));
            }
        }
        if ((properties = PropertiesUtil.extractSubset(this.rootProperties, "rootLogger")).size() > 0) {
            this.builder.add(this.createRootLogger(properties));
        }
        this.builder.setLoggerContext(this.loggerContext);
        return this.builder.build(false);
    }

    private ScriptComponentBuilder createScript(Properties properties) {
        String string = (String)properties.remove(CONFIG_NAME);
        String string2 = (String)properties.remove("language");
        String string3 = (String)properties.remove("text");
        return PropertiesConfigurationBuilder.processRemainingProperties(this.builder.newScript(string, string2, string3), properties);
    }

    private ScriptFileComponentBuilder createScriptFile(Properties properties) {
        String string = (String)properties.remove(CONFIG_NAME);
        String string2 = (String)properties.remove("path");
        return PropertiesConfigurationBuilder.processRemainingProperties(this.builder.newScriptFile(string, string2), properties);
    }

    private AppenderComponentBuilder createAppender(String object, Properties properties) {
        String string = (String)properties.remove(CONFIG_NAME);
        if (Strings.isEmpty(string)) {
            throw new ConfigurationException("No name attribute provided for Appender ".concat(String.valueOf(object)));
        }
        Object object2 = (String)properties.remove(CONFIG_TYPE);
        if (Strings.isEmpty((CharSequence)object2)) {
            throw new ConfigurationException("No type attribute provided for Appender ".concat(String.valueOf(object)));
        }
        object = this.builder.newAppender(string, (String)object2);
        this.addFiltersToComponent(object, properties);
        object2 = PropertiesUtil.extractSubset(properties, "layout");
        if (((Hashtable)object2).size() > 0) {
            object.add(this.createLayout(string, (Properties)object2));
        }
        return (AppenderComponentBuilder)PropertiesConfigurationBuilder.processRemainingProperties(object, properties);
    }

    private FilterComponentBuilder createFilter(String string, Properties properties) {
        String string2 = (String)properties.remove(CONFIG_TYPE);
        if (Strings.isEmpty(string2)) {
            throw new ConfigurationException("No type attribute provided for Appender ".concat(String.valueOf(string)));
        }
        string = (String)properties.remove("onMatch");
        String string3 = (String)properties.remove("onMismatch");
        return PropertiesConfigurationBuilder.processRemainingProperties(this.builder.newFilter(string2, string, string3), properties);
    }

    private AppenderRefComponentBuilder createAppenderRef(String object, Properties properties) {
        String string = (String)properties.remove("ref");
        if (Strings.isEmpty(string)) {
            throw new ConfigurationException("No ref attribute provided for AppenderRef ".concat(String.valueOf(object)));
        }
        object = this.builder.newAppenderRef(string);
        string = Strings.trimToNull((String)properties.remove("level"));
        if (!Strings.isEmpty(string)) {
            object.addAttribute("level", string);
        }
        return (AppenderRefComponentBuilder)this.addFiltersToComponent(object, properties);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private LoggerComponentBuilder createLogger(String object, Properties object2) {
        String string = (String)((Hashtable)object2).remove(CONFIG_NAME);
        String string2 = (String)((Hashtable)object2).remove("includeLocation");
        if (Strings.isEmpty(string)) {
            throw new ConfigurationException("No name attribute provided for Logger ".concat(String.valueOf(object)));
        }
        object = Strings.trimToNull((String)((Hashtable)object2).remove("level"));
        String string3 = (String)((Hashtable)object2).remove(CONFIG_TYPE);
        if (string3 != null) {
            if (!string3.equalsIgnoreCase("asyncLogger")) throw new ConfigurationException("Unknown Logger type " + string3 + " for Logger " + string);
            if (string2 != null) {
                boolean bl = Boolean.parseBoolean(string2);
                object = this.builder.newAsyncLogger(string, (String)object, bl);
            } else {
                object = this.builder.newAsyncLogger(string, (String)object);
            }
        } else if (string2 != null) {
            boolean bl = Boolean.parseBoolean(string2);
            object = this.builder.newLogger(string, (String)object, bl);
        } else {
            object = this.builder.newLogger(string, (String)object);
        }
        this.addLoggersToComponent(object, (Properties)object2);
        this.addFiltersToComponent(object, (Properties)object2);
        object2 = (String)((Hashtable)object2).remove("additivity");
        if (Strings.isEmpty((CharSequence)object2)) return object;
        object.addAttribute("additivity", (String)object2);
        return object;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private RootLoggerComponentBuilder createRootLogger(Properties properties) {
        Object object = Strings.trimToNull((String)properties.remove("level"));
        String string = (String)properties.remove(CONFIG_TYPE);
        String string2 = (String)properties.remove("includeLocation");
        if (string != null) {
            if (!string.equalsIgnoreCase("asyncRoot")) throw new ConfigurationException("Unknown Logger type for root logger".concat(String.valueOf(string)));
            if (string2 != null) {
                boolean bl = Boolean.parseBoolean(string2);
                object = this.builder.newAsyncRootLogger((String)object, bl);
            } else {
                object = this.builder.newAsyncRootLogger((String)object);
            }
        } else if (string2 != null) {
            boolean bl = Boolean.parseBoolean(string2);
            object = this.builder.newRootLogger((String)object, bl);
        } else {
            object = this.builder.newRootLogger((String)object);
        }
        this.addLoggersToComponent(object, properties);
        return (RootLoggerComponentBuilder)this.addFiltersToComponent(object, properties);
    }

    private LayoutComponentBuilder createLayout(String string, Properties properties) {
        String string2 = (String)properties.remove(CONFIG_TYPE);
        if (Strings.isEmpty(string2)) {
            throw new ConfigurationException("No type attribute provided for Layout on Appender ".concat(String.valueOf(string)));
        }
        return PropertiesConfigurationBuilder.processRemainingProperties(this.builder.newLayout(string2), properties);
    }

    private static <B extends ComponentBuilder<B>> ComponentBuilder<B> createComponent(ComponentBuilder<?> componentBuilder, String string, Properties properties) {
        String string2 = (String)properties.remove(CONFIG_NAME);
        String string3 = (String)properties.remove(CONFIG_TYPE);
        if (Strings.isEmpty(string3)) {
            throw new ConfigurationException("No type attribute provided for component ".concat(String.valueOf(string)));
        }
        return PropertiesConfigurationBuilder.processRemainingProperties(componentBuilder.getBuilder().newComponent(string2, string3), properties);
    }

    private static <B extends ComponentBuilder<?>> B processRemainingProperties(B b2, Properties properties) {
        while (properties.size() > 0) {
            String string = properties.stringPropertyNames().iterator().next();
            int n = string.indexOf(46);
            if (n > 0) {
                string = string.substring(0, n);
                Properties properties2 = PropertiesUtil.extractSubset(properties, string);
                B b3 = b2;
                b3.addComponent(PropertiesConfigurationBuilder.createComponent(b3, string, properties2));
                continue;
            }
            b2.addAttribute(string, properties.getProperty(string));
            properties.remove(string);
        }
        return b2;
    }

    private <B extends FilterableComponentBuilder<? extends ComponentBuilder<?>>> B addFiltersToComponent(B b2, Properties object) {
        for (Map.Entry<String, Properties> entry : PropertiesUtil.partitionOnCommonPrefixes(PropertiesUtil.extractSubset((Properties)((Object)object), "filter")).entrySet()) {
            b2.add(this.createFilter(entry.getKey().trim(), entry.getValue()));
        }
        return b2;
    }

    private <B extends LoggableComponentBuilder<? extends ComponentBuilder<?>>> B addLoggersToComponent(B b2, Properties object) {
        for (Map.Entry<String, Properties> entry : PropertiesUtil.partitionOnCommonPrefixes(PropertiesUtil.extractSubset((Properties)((Object)object), "appenderRef")).entrySet()) {
            b2.add(this.createAppenderRef(entry.getKey().trim(), entry.getValue()));
        }
        return b2;
    }

    public PropertiesConfigurationBuilder setLoggerContext(LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
        return this;
    }

    public LoggerContext getLoggerContext() {
        return this.loggerContext;
    }
}

