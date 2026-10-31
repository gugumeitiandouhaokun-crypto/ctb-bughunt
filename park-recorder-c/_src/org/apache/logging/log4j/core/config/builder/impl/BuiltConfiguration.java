/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.builder.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Reconfigurable;
import org.apache.logging.log4j.core.config.builder.api.Component;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.config.plugins.util.ResolverUtil;
import org.apache.logging.log4j.core.config.status.StatusConfiguration;
import org.apache.logging.log4j.core.util.Patterns;

public class BuiltConfiguration
extends AbstractConfiguration {
    private static final String[] VERBOSE_CLASSES = new String[]{ResolverUtil.class.getName()};
    private final StatusConfiguration statusConfig = new StatusConfiguration().withVerboseClasses(VERBOSE_CLASSES).withStatus(this.getDefaultStatus());
    protected Component rootComponent;
    private Component loggersComponent;
    private Component appendersComponent;
    private Component filtersComponent;
    private Component propertiesComponent;
    private Component customLevelsComponent;
    private Component scriptsComponent;
    private String contentType = "text";

    /*
     * WARNING - void declaration
     */
    public BuiltConfiguration(LoggerContext object, ConfigurationSource object22, Component component) {
        super((LoggerContext)((Object)object), (ConfigurationSource)object22);
        void var3_5;
        for (Component component2 : var3_5.getComponents()) {
            switch (component2.getPluginType()) {
                case "Scripts": {
                    this.scriptsComponent = component2;
                    break;
                }
                case "Loggers": {
                    this.loggersComponent = component2;
                    break;
                }
                case "Appenders": {
                    this.appendersComponent = component2;
                    break;
                }
                case "Filters": {
                    this.filtersComponent = component2;
                    break;
                }
                case "Properties": {
                    this.propertiesComponent = component2;
                    break;
                }
                case "CustomLevels": {
                    this.customLevelsComponent = component2;
                }
            }
        }
        this.rootComponent = var3_5;
    }

    @Override
    public void setup() {
        List<Node> list = this.rootNode.getChildren();
        if (this.propertiesComponent.getComponents().size() > 0) {
            BuiltConfiguration builtConfiguration = this;
            list.add(builtConfiguration.convertToNode(builtConfiguration.rootNode, this.propertiesComponent));
        }
        if (this.scriptsComponent.getComponents().size() > 0) {
            BuiltConfiguration builtConfiguration = this;
            list.add(builtConfiguration.convertToNode(builtConfiguration.rootNode, this.scriptsComponent));
        }
        if (this.customLevelsComponent.getComponents().size() > 0) {
            BuiltConfiguration builtConfiguration = this;
            list.add(builtConfiguration.convertToNode(builtConfiguration.rootNode, this.customLevelsComponent));
        }
        BuiltConfiguration builtConfiguration = this;
        list.add(builtConfiguration.convertToNode(builtConfiguration.rootNode, this.loggersComponent));
        BuiltConfiguration builtConfiguration2 = this;
        list.add(builtConfiguration2.convertToNode(builtConfiguration2.rootNode, this.appendersComponent));
        if (this.filtersComponent.getComponents().size() > 0) {
            if (this.filtersComponent.getComponents().size() == 1) {
                BuiltConfiguration builtConfiguration3 = this;
                list.add(builtConfiguration3.convertToNode(builtConfiguration3.rootNode, this.filtersComponent.getComponents().get(0)));
            } else {
                BuiltConfiguration builtConfiguration4 = this;
                list.add(builtConfiguration4.convertToNode(builtConfiguration4.rootNode, this.filtersComponent));
            }
        }
        this.rootComponent = null;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String string) {
        this.contentType = string;
    }

    public void createAdvertiser(String string, ConfigurationSource configurationSource) {
        byte[] byArray = null;
        try {
            InputStream inputStream;
            if (configurationSource != null && (inputStream = configurationSource.getInputStream()) != null) {
                byArray = BuiltConfiguration.toByteArray(inputStream);
            }
        }
        catch (IOException iOException) {
            LOGGER.warn("Unable to read configuration source " + configurationSource.toString());
        }
        super.createAdvertiser(string, configurationSource, byArray, this.contentType);
    }

    public StatusConfiguration getStatusConfiguration() {
        return this.statusConfig;
    }

    public void setPluginPackages(String string) {
        this.pluginPackages.addAll(Arrays.asList(string.split(Patterns.COMMA_SEPARATOR)));
    }

    public void setShutdownHook(String string) {
        this.isShutdownHookEnabled = !"disable".equalsIgnoreCase(string);
    }

    public void setShutdownTimeoutMillis(long l) {
        this.shutdownTimeoutMillis = l;
    }

    public void setMonitorInterval(int n) {
        if (this instanceof Reconfigurable && n > 0) {
            BuiltConfiguration builtConfiguration = this;
            builtConfiguration.initializeWatchers((Reconfigurable)((Object)builtConfiguration), this.getConfigurationSource(), n);
        }
    }

    @Override
    public PluginManager getPluginManager() {
        return this.pluginManager;
    }

    protected Node convertToNode(Node node, Component object) {
        Object object2 = ((Component)((Object)object)).getPluginType();
        PluginType<?> object32 = this.pluginManager.getPluginType((String)object2);
        node = new Node(node, (String)object2, object32);
        node.getAttributes().putAll(((Component)((Object)object)).getAttributes());
        node.setValue(((Component)((Object)object)).getValue());
        object2 = node.getChildren();
        for (Component component : ((Component)((Object)object)).getComponents()) {
            object2.add(this.convertToNode(node, component));
        }
        return node;
    }
}

