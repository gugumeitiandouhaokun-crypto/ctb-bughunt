/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.composite;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Reconfigurable;
import org.apache.logging.log4j.core.config.composite.DefaultMergeStrategy;
import org.apache.logging.log4j.core.config.composite.MergeStrategy;
import org.apache.logging.log4j.core.config.plugins.util.ResolverUtil;
import org.apache.logging.log4j.core.config.status.StatusConfiguration;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.core.util.Patterns;
import org.apache.logging.log4j.core.util.Source;
import org.apache.logging.log4j.core.util.WatchManager;
import org.apache.logging.log4j.core.util.Watcher;
import org.apache.logging.log4j.util.PropertiesUtil;

public class CompositeConfiguration
extends AbstractConfiguration
implements Reconfigurable {
    public static final String MERGE_STRATEGY_PROPERTY = "log4j.mergeStrategy";
    private static final String[] VERBOSE_CLASSES = new String[]{ResolverUtil.class.getName()};
    private final List<? extends AbstractConfiguration> configurations;
    private MergeStrategy mergeStrategy;

    public CompositeConfiguration(List<? extends AbstractConfiguration> object) {
        super(((AbstractConfiguration)object.get(0)).getLoggerContext(), ConfigurationSource.COMPOSITE_SOURCE);
        this.rootNode = ((AbstractConfiguration)object.get(0)).getRootNode();
        this.configurations = object;
        Iterator<Map.Entry<String, String>> iterator = PropertiesUtil.getProperties().getStringProperty(MERGE_STRATEGY_PROPERTY, DefaultMergeStrategy.class.getName());
        try {
            this.mergeStrategy = (MergeStrategy)Loader.newInstanceOf((String)((Object)iterator));
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException reflectiveOperationException) {
            this.mergeStrategy = new DefaultMergeStrategy();
        }
        object = object.iterator();
        while (object.hasNext()) {
            iterator = (AbstractConfiguration)object.next();
            this.mergeStrategy.mergeRootProperties(this.rootNode, (AbstractConfiguration)((Object)iterator));
        }
        object = new StatusConfiguration().withVerboseClasses(VERBOSE_CLASSES).withStatus(this.getDefaultStatus());
        for (Map.Entry<String, String> entry : this.rootNode.getAttributes().entrySet()) {
            String string = entry.getKey();
            String object2 = this.getStrSubstitutor().replace(entry.getValue());
            if ("status".equalsIgnoreCase(string)) {
                ((StatusConfiguration)object).withStatus(object2.toUpperCase());
                continue;
            }
            if ("dest".equalsIgnoreCase(string)) {
                ((StatusConfiguration)object).withDestination(object2);
                continue;
            }
            if ("shutdownHook".equalsIgnoreCase(string)) {
                this.isShutdownHookEnabled = !"disable".equalsIgnoreCase(object2);
                continue;
            }
            if ("shutdownTimeout".equalsIgnoreCase(string)) {
                this.shutdownTimeoutMillis = Long.parseLong(object2);
                continue;
            }
            if ("verbose".equalsIgnoreCase(string)) {
                ((StatusConfiguration)object).withVerbosity(object2);
                continue;
            }
            if ("packages".equalsIgnoreCase(string)) {
                this.pluginPackages.addAll(Arrays.asList(object2.split(Patterns.COMMA_SEPARATOR)));
                continue;
            }
            if (!"name".equalsIgnoreCase(string)) continue;
            this.setName(object2);
        }
        ((StatusConfiguration)object).initialize();
    }

    @Override
    public void setup() {
        Object object = this.configurations.get(0);
        this.staffChildConfiguration((AbstractConfiguration)object);
        WatchManager watchManager = this.getWatchManager();
        object = ((AbstractConfiguration)object).getWatchManager();
        if (((WatchManager)object).getIntervalSeconds() > 0) {
            watchManager.setIntervalSeconds(((WatchManager)object).getIntervalSeconds());
            for (Map.Entry<Source, Watcher> entry : ((WatchManager)object).getConfigurationWatchers().entrySet()) {
                CompositeConfiguration compositeConfiguration = this;
                watchManager.watch(entry.getKey(), entry.getValue().newWatcher(compositeConfiguration, compositeConfiguration.listeners, entry.getValue().getLastModified()));
            }
        }
        for (AbstractConfiguration abstractConfiguration : this.configurations.subList(1, this.configurations.size())) {
            int n;
            this.staffChildConfiguration(abstractConfiguration);
            Object object2 = abstractConfiguration.getRootNode();
            this.mergeStrategy.mergConfigurations(this.rootNode, (Node)object2, this.getPluginManager());
            if (LOGGER.isEnabled(Level.ALL)) {
                object2 = new StringBuilder();
                this.printNodes("", this.rootNode, (StringBuilder)object2);
                System.out.println(((StringBuilder)object2).toString());
            }
            if ((n = abstractConfiguration.getWatchManager().getIntervalSeconds()) <= 0) continue;
            int n2 = watchManager.getIntervalSeconds();
            if (n2 <= 0 || n < n2) {
                watchManager.setIntervalSeconds(n);
            }
            for (Map.Entry<Source, Watcher> entry : abstractConfiguration.getWatchManager().getConfigurationWatchers().entrySet()) {
                CompositeConfiguration compositeConfiguration = this;
                watchManager.watch(entry.getKey(), entry.getValue().newWatcher(compositeConfiguration, compositeConfiguration.listeners, entry.getValue().getLastModified()));
            }
        }
    }

    @Override
    public Configuration reconfigure() {
        LOGGER.debug("Reconfiguring composite configuration");
        ArrayList<AbstractConfiguration> arrayList = new ArrayList<AbstractConfiguration>();
        ConfigurationFactory configurationFactory = ConfigurationFactory.getInstance();
        for (AbstractConfiguration abstractConfiguration : this.configurations) {
            URI uRI = abstractConfiguration.getConfigurationSource().getURI();
            Configuration configuration = abstractConfiguration;
            if (uRI == null) {
                LOGGER.warn("Unable to determine URI for configuration {}, changes to it will be ignored", (Object)abstractConfiguration.getName());
            } else {
                configuration = configurationFactory.getConfiguration(this.getLoggerContext(), abstractConfiguration.getName(), uRI);
                if (configuration == null) {
                    LOGGER.warn("Unable to reload configuration {}, changes to it will be ignored", (Object)abstractConfiguration.getName());
                }
            }
            arrayList.add((AbstractConfiguration)configuration);
        }
        return new CompositeConfiguration(arrayList);
    }

    private void staffChildConfiguration(AbstractConfiguration abstractConfiguration) {
        abstractConfiguration.setPluginManager(this.pluginManager);
        abstractConfiguration.setScriptManager(this.scriptManager);
        abstractConfiguration.setup();
    }

    private void printNodes(String string, Node object, StringBuilder stringBuilder) {
        stringBuilder.append(string).append(((Node)object).getName()).append(" type: ").append(((Node)object).getType()).append("\n");
        stringBuilder.append(string).append(((Node)object).getAttributes().toString()).append("\n");
        for (Node node : ((Node)object).getChildren()) {
            this.printNodes(string + "  ", node, stringBuilder);
        }
    }

    public String toString() {
        return this.getClass().getName() + "@" + Integer.toHexString(this.hashCode()) + " [configurations=" + this.configurations + ", mergeStrategy=" + this.mergeStrategy + ", rootNode=" + this.rootNode + ", listeners=" + this.listeners + ", pluginPackages=" + this.pluginPackages + ", pluginManager=" + this.pluginManager + ", isShutdownHookEnabled=" + this.isShutdownHookEnabled + ", shutdownTimeoutMillis=" + this.shutdownTimeoutMillis + ", scriptManager=" + this.scriptManager + "]";
    }
}

