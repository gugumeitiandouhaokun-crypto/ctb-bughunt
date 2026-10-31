/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.builder.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.AppenderRefComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.Component;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.CustomLevelComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.FilterComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.KeyValuePairComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.PropertyComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ScriptComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ScriptFileComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.builder.impl.DefaultAppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultAppenderRefComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultCustomLevelComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultFilterComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultKeyValuePairComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultLayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultPropertyComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultRootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultScriptComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.DefaultScriptFileComponentBuilder;
import org.apache.logging.log4j.core.util.Throwables;

public class DefaultConfigurationBuilder<T extends BuiltConfiguration>
implements ConfigurationBuilder<T> {
    private static final String INDENT = "  ";
    private static final String EOL = System.lineSeparator();
    private final Component root = new Component();
    private Component loggers;
    private Component appenders;
    private Component filters;
    private Component properties;
    private Component customLevels;
    private Component scripts;
    private final Class<T> clazz;
    private ConfigurationSource source;
    private int monitorInterval;
    private Level level;
    private String verbosity;
    private String destination;
    private String packages;
    private String shutdownFlag;
    private long shutdownTimeoutMillis;
    private String advertiser;
    private LoggerContext loggerContext;
    private String name;

    public DefaultConfigurationBuilder() {
        this(BuiltConfiguration.class);
        this.root.addAttribute("name", "Built");
    }

    public DefaultConfigurationBuilder(Class<T> object) {
        if (object == null) {
            throw new IllegalArgumentException("A Configuration class must be provided");
        }
        this.clazz = object;
        object = this.root.getComponents();
        this.properties = new Component("Properties");
        object.add(this.properties);
        this.scripts = new Component("Scripts");
        object.add(this.scripts);
        this.customLevels = new Component("CustomLevels");
        object.add(this.customLevels);
        this.filters = new Component("Filters");
        object.add(this.filters);
        this.appenders = new Component("Appenders");
        object.add(this.appenders);
        this.loggers = new Component("Loggers");
        object.add(this.loggers);
    }

    protected ConfigurationBuilder<T> add(Component component, ComponentBuilder<?> componentBuilder) {
        component.getComponents().add((Component)componentBuilder.build());
        return this;
    }

    @Override
    public ConfigurationBuilder<T> add(AppenderComponentBuilder appenderComponentBuilder) {
        DefaultConfigurationBuilder defaultConfigurationBuilder = this;
        return defaultConfigurationBuilder.add(defaultConfigurationBuilder.appenders, appenderComponentBuilder);
    }

    @Override
    public ConfigurationBuilder<T> add(CustomLevelComponentBuilder customLevelComponentBuilder) {
        DefaultConfigurationBuilder defaultConfigurationBuilder = this;
        return defaultConfigurationBuilder.add(defaultConfigurationBuilder.customLevels, customLevelComponentBuilder);
    }

    @Override
    public ConfigurationBuilder<T> add(FilterComponentBuilder filterComponentBuilder) {
        DefaultConfigurationBuilder defaultConfigurationBuilder = this;
        return defaultConfigurationBuilder.add(defaultConfigurationBuilder.filters, filterComponentBuilder);
    }

    @Override
    public ConfigurationBuilder<T> add(ScriptComponentBuilder scriptComponentBuilder) {
        DefaultConfigurationBuilder defaultConfigurationBuilder = this;
        return defaultConfigurationBuilder.add(defaultConfigurationBuilder.scripts, scriptComponentBuilder);
    }

    @Override
    public ConfigurationBuilder<T> add(ScriptFileComponentBuilder scriptFileComponentBuilder) {
        DefaultConfigurationBuilder defaultConfigurationBuilder = this;
        return defaultConfigurationBuilder.add(defaultConfigurationBuilder.scripts, scriptFileComponentBuilder);
    }

    @Override
    public ConfigurationBuilder<T> add(LoggerComponentBuilder loggerComponentBuilder) {
        DefaultConfigurationBuilder defaultConfigurationBuilder = this;
        return defaultConfigurationBuilder.add(defaultConfigurationBuilder.loggers, loggerComponentBuilder);
    }

    @Override
    public ConfigurationBuilder<T> add(RootLoggerComponentBuilder rootLoggerComponentBuilder) {
        Iterator<Component> iterator = this.loggers.getComponents().iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().getPluginType().equals("root")) continue;
            throw new ConfigurationException("Root Logger was previously defined");
        }
        DefaultConfigurationBuilder defaultConfigurationBuilder = this;
        return defaultConfigurationBuilder.add(defaultConfigurationBuilder.loggers, rootLoggerComponentBuilder);
    }

    @Override
    public ConfigurationBuilder<T> addProperty(String string, String string2) {
        this.properties.addComponent((Component)this.newComponent(string, "Property", string2).build());
        return this;
    }

    @Override
    public T build() {
        return (T)this.build(true);
    }

    @Override
    public T build(boolean bl) {
        BuiltConfiguration builtConfiguration;
        try {
            if (this.source == null) {
                this.source = ConfigurationSource.NULL_SOURCE;
            }
            builtConfiguration = (BuiltConfiguration)this.clazz.getConstructor(LoggerContext.class, ConfigurationSource.class, Component.class).newInstance(this.loggerContext, this.source, this.root);
            builtConfiguration.getRootNode().getAttributes().putAll(this.root.getAttributes());
            if (this.name != null) {
                builtConfiguration.setName(this.name);
            }
            if (this.level != null) {
                builtConfiguration.getStatusConfiguration().withStatus(this.level);
            }
            if (this.verbosity != null) {
                builtConfiguration.getStatusConfiguration().withVerbosity(this.verbosity);
            }
            if (this.destination != null) {
                builtConfiguration.getStatusConfiguration().withDestination(this.destination);
            }
            if (this.packages != null) {
                builtConfiguration.setPluginPackages(this.packages);
            }
            if (this.shutdownFlag != null) {
                builtConfiguration.setShutdownHook(this.shutdownFlag);
            }
            if (this.shutdownTimeoutMillis > 0L) {
                builtConfiguration.setShutdownTimeoutMillis(this.shutdownTimeoutMillis);
            }
            if (this.advertiser != null) {
                builtConfiguration.createAdvertiser(this.advertiser, this.source);
            }
            builtConfiguration.setMonitorInterval(this.monitorInterval);
        }
        catch (Exception exception) {
            throw new IllegalArgumentException("Invalid Configuration class specified", exception);
        }
        builtConfiguration.getStatusConfiguration().initialize();
        if (bl) {
            builtConfiguration.initialize();
        }
        return (T)builtConfiguration;
    }

    @Override
    public void writeXmlConfiguration(OutputStream object) {
        try {
            object = XMLOutputFactory.newInstance().createXMLStreamWriter((OutputStream)object);
            this.writeXmlConfiguration((XMLStreamWriter)object);
            object.close();
            return;
        }
        catch (XMLStreamException xMLStreamException) {
            object = xMLStreamException;
            if (xMLStreamException.getNestedException() instanceof IOException) {
                throw (IOException)((XMLStreamException)object).getNestedException();
            }
            Throwables.rethrow((Throwable)object);
            return;
        }
    }

    @Override
    public String toXmlConfiguration() {
        StringWriter stringWriter = new StringWriter();
        try {
            XMLStreamWriter xMLStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
            this.writeXmlConfiguration(xMLStreamWriter);
            xMLStreamWriter.close();
        }
        catch (XMLStreamException xMLStreamException) {
            Throwables.rethrow(xMLStreamException);
        }
        return stringWriter.toString();
    }

    private void writeXmlConfiguration(XMLStreamWriter xMLStreamWriter) {
        xMLStreamWriter.writeStartDocument();
        xMLStreamWriter.writeCharacters(EOL);
        xMLStreamWriter.writeStartElement("Configuration");
        if (this.name != null) {
            xMLStreamWriter.writeAttribute("name", this.name);
        }
        if (this.level != null) {
            xMLStreamWriter.writeAttribute("status", this.level.name());
        }
        if (this.verbosity != null) {
            xMLStreamWriter.writeAttribute("verbose", this.verbosity);
        }
        if (this.destination != null) {
            xMLStreamWriter.writeAttribute("dest", this.destination);
        }
        if (this.packages != null) {
            xMLStreamWriter.writeAttribute("packages", this.packages);
        }
        if (this.shutdownFlag != null) {
            xMLStreamWriter.writeAttribute("shutdownHook", this.shutdownFlag);
        }
        if (this.shutdownTimeoutMillis > 0L) {
            xMLStreamWriter.writeAttribute("shutdownTimeout", String.valueOf(this.shutdownTimeoutMillis));
        }
        if (this.advertiser != null) {
            xMLStreamWriter.writeAttribute("advertiser", this.advertiser);
        }
        if (this.monitorInterval > 0) {
            xMLStreamWriter.writeAttribute("monitorInterval", String.valueOf(this.monitorInterval));
        }
        xMLStreamWriter.writeCharacters(EOL);
        this.writeXmlSection(xMLStreamWriter, this.properties);
        this.writeXmlSection(xMLStreamWriter, this.scripts);
        this.writeXmlSection(xMLStreamWriter, this.customLevels);
        if (this.filters.getComponents().size() == 1) {
            this.writeXmlComponent(xMLStreamWriter, this.filters.getComponents().get(0), 1);
        } else if (this.filters.getComponents().size() > 1) {
            this.writeXmlSection(xMLStreamWriter, this.filters);
        }
        this.writeXmlSection(xMLStreamWriter, this.appenders);
        this.writeXmlSection(xMLStreamWriter, this.loggers);
        xMLStreamWriter.writeEndElement();
        xMLStreamWriter.writeCharacters(EOL);
        xMLStreamWriter.writeEndDocument();
    }

    private void writeXmlSection(XMLStreamWriter xMLStreamWriter, Component component) {
        if (!component.getAttributes().isEmpty() || !component.getComponents().isEmpty() || component.getValue() != null) {
            this.writeXmlComponent(xMLStreamWriter, component, 1);
        }
    }

    private void writeXmlComponent(XMLStreamWriter xMLStreamWriter, Component component, int n) {
        if (!component.getComponents().isEmpty() || component.getValue() != null) {
            this.writeXmlIndent(xMLStreamWriter, n);
            xMLStreamWriter.writeStartElement(component.getPluginType());
            this.writeXmlAttributes(xMLStreamWriter, component);
            if (!component.getComponents().isEmpty()) {
                xMLStreamWriter.writeCharacters(EOL);
            }
            for (Component component2 : component.getComponents()) {
                this.writeXmlComponent(xMLStreamWriter, component2, n + 1);
            }
            if (component.getValue() != null) {
                xMLStreamWriter.writeCharacters(component.getValue());
            }
            if (!component.getComponents().isEmpty()) {
                this.writeXmlIndent(xMLStreamWriter, n);
            }
            xMLStreamWriter.writeEndElement();
        } else {
            this.writeXmlIndent(xMLStreamWriter, n);
            xMLStreamWriter.writeEmptyElement(component.getPluginType());
            this.writeXmlAttributes(xMLStreamWriter, component);
        }
        xMLStreamWriter.writeCharacters(EOL);
    }

    private void writeXmlIndent(XMLStreamWriter xMLStreamWriter, int n) {
        for (int j = 0; j < n; ++j) {
            xMLStreamWriter.writeCharacters(INDENT);
        }
    }

    private void writeXmlAttributes(XMLStreamWriter xMLStreamWriter, Component object) {
        for (Map.Entry<String, String> entry : ((Component)((Object)object)).getAttributes().entrySet()) {
            xMLStreamWriter.writeAttribute(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public ScriptComponentBuilder newScript(String string, String string2, String string3) {
        return new DefaultScriptComponentBuilder(this, string, string2, string3);
    }

    @Override
    public ScriptFileComponentBuilder newScriptFile(String string) {
        String string2 = string;
        return new DefaultScriptFileComponentBuilder(this, string2, string2);
    }

    @Override
    public ScriptFileComponentBuilder newScriptFile(String string, String string2) {
        return new DefaultScriptFileComponentBuilder(this, string, string2);
    }

    @Override
    public AppenderComponentBuilder newAppender(String string, String string2) {
        return new DefaultAppenderComponentBuilder(this, string, string2);
    }

    @Override
    public AppenderRefComponentBuilder newAppenderRef(String string) {
        return new DefaultAppenderRefComponentBuilder(this, string);
    }

    @Override
    public LoggerComponentBuilder newAsyncLogger(String string) {
        return new DefaultLoggerComponentBuilder((DefaultConfigurationBuilder<? extends Configuration>)this, string, null, "AsyncLogger");
    }

    @Override
    public LoggerComponentBuilder newAsyncLogger(String string, boolean bl) {
        return new DefaultLoggerComponentBuilder(this, string, null, "AsyncLogger", bl);
    }

    @Override
    public LoggerComponentBuilder newAsyncLogger(String string, Level level) {
        return new DefaultLoggerComponentBuilder((DefaultConfigurationBuilder<? extends Configuration>)this, string, level.toString(), "AsyncLogger");
    }

    @Override
    public LoggerComponentBuilder newAsyncLogger(String string, Level level, boolean bl) {
        return new DefaultLoggerComponentBuilder(this, string, level.toString(), "AsyncLogger", bl);
    }

    @Override
    public LoggerComponentBuilder newAsyncLogger(String string, String string2) {
        return new DefaultLoggerComponentBuilder((DefaultConfigurationBuilder<? extends Configuration>)this, string, string2, "AsyncLogger");
    }

    @Override
    public LoggerComponentBuilder newAsyncLogger(String string, String string2, boolean bl) {
        return new DefaultLoggerComponentBuilder(this, string, string2, "AsyncLogger", bl);
    }

    @Override
    public RootLoggerComponentBuilder newAsyncRootLogger() {
        return new DefaultRootLoggerComponentBuilder(this, "AsyncRoot");
    }

    @Override
    public RootLoggerComponentBuilder newAsyncRootLogger(boolean bl) {
        return new DefaultRootLoggerComponentBuilder((DefaultConfigurationBuilder<? extends Configuration>)this, null, "AsyncRoot", bl);
    }

    @Override
    public RootLoggerComponentBuilder newAsyncRootLogger(Level level) {
        return new DefaultRootLoggerComponentBuilder((DefaultConfigurationBuilder<? extends Configuration>)this, level.toString(), "AsyncRoot");
    }

    @Override
    public RootLoggerComponentBuilder newAsyncRootLogger(Level level, boolean bl) {
        return new DefaultRootLoggerComponentBuilder((DefaultConfigurationBuilder<? extends Configuration>)this, level.toString(), "AsyncRoot", bl);
    }

    @Override
    public RootLoggerComponentBuilder newAsyncRootLogger(String string) {
        return new DefaultRootLoggerComponentBuilder((DefaultConfigurationBuilder<? extends Configuration>)this, string, "AsyncRoot");
    }

    @Override
    public RootLoggerComponentBuilder newAsyncRootLogger(String string, boolean bl) {
        return new DefaultRootLoggerComponentBuilder((DefaultConfigurationBuilder<? extends Configuration>)this, string, "AsyncRoot", bl);
    }

    @Override
    public <B extends ComponentBuilder<B>> ComponentBuilder<B> newComponent(String string) {
        return new DefaultComponentBuilder(this, string);
    }

    @Override
    public <B extends ComponentBuilder<B>> ComponentBuilder<B> newComponent(String string, String string2) {
        return new DefaultComponentBuilder(this, string, string2);
    }

    @Override
    public <B extends ComponentBuilder<B>> ComponentBuilder<B> newComponent(String string, String string2, String string3) {
        return new DefaultComponentBuilder(this, string, string2, string3);
    }

    @Override
    public PropertyComponentBuilder newProperty(String string, String string2) {
        return new DefaultPropertyComponentBuilder(this, string, string2);
    }

    @Override
    public KeyValuePairComponentBuilder newKeyValuePair(String string, String string2) {
        return new DefaultKeyValuePairComponentBuilder(this, string, string2);
    }

    @Override
    public CustomLevelComponentBuilder newCustomLevel(String string, int n) {
        return new DefaultCustomLevelComponentBuilder((DefaultConfigurationBuilder<? extends Configuration>)this, string, n);
    }

    @Override
    public FilterComponentBuilder newFilter(String string, Filter.Result result, Filter.Result result2) {
        return new DefaultFilterComponentBuilder(this, string, result.name(), result2.name());
    }

    @Override
    public FilterComponentBuilder newFilter(String string, String string2, String string3) {
        return new DefaultFilterComponentBuilder(this, string, string2, string3);
    }

    @Override
    public LayoutComponentBuilder newLayout(String string) {
        return new DefaultLayoutComponentBuilder(this, string);
    }

    @Override
    public LoggerComponentBuilder newLogger(String string) {
        return new DefaultLoggerComponentBuilder(this, string, null);
    }

    @Override
    public LoggerComponentBuilder newLogger(String string, boolean bl) {
        return new DefaultLoggerComponentBuilder((DefaultConfigurationBuilder<? extends Configuration>)this, string, null, bl);
    }

    @Override
    public LoggerComponentBuilder newLogger(String string, Level level) {
        return new DefaultLoggerComponentBuilder(this, string, level.toString());
    }

    @Override
    public LoggerComponentBuilder newLogger(String string, Level level, boolean bl) {
        return new DefaultLoggerComponentBuilder((DefaultConfigurationBuilder<? extends Configuration>)this, string, level.toString(), bl);
    }

    @Override
    public LoggerComponentBuilder newLogger(String string, String string2) {
        return new DefaultLoggerComponentBuilder(this, string, string2);
    }

    @Override
    public LoggerComponentBuilder newLogger(String string, String string2, boolean bl) {
        return new DefaultLoggerComponentBuilder((DefaultConfigurationBuilder<? extends Configuration>)this, string, string2, bl);
    }

    @Override
    public RootLoggerComponentBuilder newRootLogger() {
        return new DefaultRootLoggerComponentBuilder(this, null);
    }

    @Override
    public RootLoggerComponentBuilder newRootLogger(boolean bl) {
        return new DefaultRootLoggerComponentBuilder((DefaultConfigurationBuilder<? extends Configuration>)this, null, bl);
    }

    @Override
    public RootLoggerComponentBuilder newRootLogger(Level level) {
        return new DefaultRootLoggerComponentBuilder(this, level.toString());
    }

    @Override
    public RootLoggerComponentBuilder newRootLogger(Level level, boolean bl) {
        return new DefaultRootLoggerComponentBuilder((DefaultConfigurationBuilder<? extends Configuration>)this, level.toString(), bl);
    }

    @Override
    public RootLoggerComponentBuilder newRootLogger(String string) {
        return new DefaultRootLoggerComponentBuilder(this, string);
    }

    @Override
    public RootLoggerComponentBuilder newRootLogger(String string, boolean bl) {
        return new DefaultRootLoggerComponentBuilder((DefaultConfigurationBuilder<? extends Configuration>)this, string, bl);
    }

    @Override
    public ConfigurationBuilder<T> setAdvertiser(String string) {
        this.advertiser = string;
        return this;
    }

    @Override
    public ConfigurationBuilder<T> setConfigurationName(String string) {
        this.name = string;
        return this;
    }

    @Override
    public ConfigurationBuilder<T> setConfigurationSource(ConfigurationSource configurationSource) {
        this.source = configurationSource;
        return this;
    }

    @Override
    public ConfigurationBuilder<T> setMonitorInterval(String string) {
        this.monitorInterval = Integer.parseInt(string);
        return this;
    }

    @Override
    public ConfigurationBuilder<T> setPackages(String string) {
        this.packages = string;
        return this;
    }

    @Override
    public ConfigurationBuilder<T> setShutdownHook(String string) {
        this.shutdownFlag = string;
        return this;
    }

    @Override
    public ConfigurationBuilder<T> setShutdownTimeout(long l, TimeUnit timeUnit) {
        this.shutdownTimeoutMillis = timeUnit.toMillis(l);
        return this;
    }

    @Override
    public ConfigurationBuilder<T> setStatusLevel(Level level) {
        this.level = level;
        return this;
    }

    @Override
    public ConfigurationBuilder<T> setVerbosity(String string) {
        this.verbosity = string;
        return this;
    }

    @Override
    public ConfigurationBuilder<T> setDestination(String string) {
        this.destination = string;
        return this;
    }

    @Override
    public void setLoggerContext(LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
    }

    @Override
    public ConfigurationBuilder<T> addRootProperty(String string, String string2) {
        this.root.getAttributes().put(string, string2);
        return this;
    }
}

