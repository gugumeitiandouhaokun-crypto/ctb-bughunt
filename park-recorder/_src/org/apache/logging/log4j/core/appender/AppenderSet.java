/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginNode;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="AppenderSet", category="Core", printObject=true, deferChildren=true)
public class AppenderSet {
    private static final StatusLogger LOGGER = StatusLogger.getLogger();
    private final Configuration configuration;
    private final Map<String, Node> nodeMap;

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    private AppenderSet(Configuration configuration, Map<String, Node> map) {
        this.configuration = configuration;
        this.nodeMap = map;
    }

    public Appender createAppender(String object, String string) {
        Node node = this.nodeMap.get(object);
        if (node == null) {
            LOGGER.error("No node named {} in {}", object, (Object)this);
            return null;
        }
        node.getAttributes().put("name", string);
        if (node.getType().getElementName().equals("appender")) {
            object = new Node(node);
            this.configuration.createConfiguration((Node)object, null);
            if (((Node)object).getObject() instanceof Appender) {
                object = (Appender)((Node)object).getObject();
                object.start();
                return object;
            }
            LOGGER.error("Unable to create Appender of type " + node.getName());
            return null;
        }
        LOGGER.error("No Appender was configured for name {} ".concat(String.valueOf(object)));
        return null;
    }

    public static class Builder
    implements org.apache.logging.log4j.core.util.Builder<AppenderSet> {
        @PluginNode
        private Node node;
        @PluginConfiguration
        @Required
        private Configuration configuration;

        @Override
        public AppenderSet build() {
            if (this.configuration == null) {
                LOGGER.error("Configuration is missing from AppenderSet {}", (Object)this);
                return null;
            }
            if (this.node == null) {
                LOGGER.error("No node in AppenderSet {}", (Object)this);
                return null;
            }
            List<Node> list = this.node.getChildren();
            if (list == null) {
                LOGGER.error("No children node in AppenderSet {}", (Object)this);
                return null;
            }
            HashMap<String, Node> hashMap = new HashMap<String, Node>(list.size());
            for (Node node : list) {
                String string = node.getAttributes().get("name");
                if (string == null) {
                    LOGGER.error("The attribute 'name' is missing from from the node {} in AppenderSet {}", (Object)node, (Object)list);
                    continue;
                }
                hashMap.put(string, node);
            }
            return new AppenderSet(this.configuration, hashMap);
        }

        public Node getNode() {
            return this.node;
        }

        public Configuration getConfiguration() {
            return this.configuration;
        }

        public Builder withNode(Node node) {
            this.node = node;
            return this;
        }

        public Builder withConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public String toString() {
            return this.getClass().getName() + " [node=" + this.node + ", configuration=" + this.configuration + "]";
        }
    }
}

