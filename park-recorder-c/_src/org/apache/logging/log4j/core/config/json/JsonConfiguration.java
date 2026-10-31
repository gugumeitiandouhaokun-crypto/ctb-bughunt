/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.JsonParser$Feature
 *  com.fasterxml.jackson.databind.JsonNode
 *  com.fasterxml.jackson.databind.ObjectMapper
 */
package org.apache.logging.log4j.core.config.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Reconfigurable;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.config.plugins.util.ResolverUtil;
import org.apache.logging.log4j.core.config.status.StatusConfiguration;
import org.apache.logging.log4j.core.util.Patterns;

public class JsonConfiguration
extends AbstractConfiguration
implements Reconfigurable {
    private static final String[] VERBOSE_CLASSES = new String[]{ResolverUtil.class.getName()};
    private final List<Status> status = new ArrayList<Status>();
    private JsonNode root;

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public JsonConfiguration(LoggerContext object, ConfigurationSource configurationSource) {
        super((LoggerContext)object, configurationSource);
        configurationSource.getFile();
        try {
            Object object2;
            block21: {
                InputStream inputStream = configurationSource.getInputStream();
                object2 = null;
                try {
                    object = JsonConfiguration.toByteArray(inputStream);
                    if (inputStream == null) break block21;
                }
                catch (Throwable throwable) {
                    try {
                        Throwable throwable2 = throwable;
                        object2 = throwable;
                        throw throwable2;
                    }
                    catch (Throwable throwable3) {
                        if (inputStream == null) throw throwable3;
                        if (object2 == null) {
                            inputStream.close();
                            throw throwable3;
                        }
                        try {
                            inputStream.close();
                            throw throwable3;
                        }
                        catch (Throwable throwable4) {
                            ((Throwable)object2).addSuppressed(throwable4);
                            throw throwable3;
                        }
                    }
                }
                inputStream.close();
            }
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream((byte[])object);
            this.root = this.getObjectMapper().readTree((InputStream)byteArrayInputStream);
            if (this.root.size() == 1) {
                object2 = this.root.iterator();
                while (object2.hasNext()) {
                    JsonNode jsonNode;
                    this.root = jsonNode = (JsonNode)object2.next();
                }
            }
            JsonConfiguration jsonConfiguration = this;
            jsonConfiguration.processAttributes(jsonConfiguration.rootNode, this.root);
            object2 = new StatusConfiguration().withVerboseClasses(VERBOSE_CLASSES).withStatus(this.getDefaultStatus());
            int n = 0;
            Iterator<Map.Entry<String, String>> iterator = this.rootNode.getAttributes().entrySet().iterator();
            while (true) {
                if (!iterator.hasNext()) {
                    JsonConfiguration jsonConfiguration2 = this;
                    jsonConfiguration2.initializeWatchers(jsonConfiguration2, configurationSource, n);
                    ((StatusConfiguration)object2).initialize();
                    if (this.getName() != null) return;
                    this.setName(configurationSource.getLocation());
                    return;
                }
                Map.Entry<String, String> entry = iterator.next();
                String string = entry.getKey();
                String string2 = this.getStrSubstitutor().replace(entry.getValue());
                if ("status".equalsIgnoreCase(string)) {
                    ((StatusConfiguration)object2).withStatus(string2);
                    continue;
                }
                if ("dest".equalsIgnoreCase(string)) {
                    ((StatusConfiguration)object2).withDestination(string2);
                    continue;
                }
                if ("shutdownHook".equalsIgnoreCase(string)) {
                    this.isShutdownHookEnabled = !"disable".equalsIgnoreCase(string2);
                    continue;
                }
                if ("shutdownTimeout".equalsIgnoreCase(string)) {
                    this.shutdownTimeoutMillis = Long.parseLong(string2);
                    continue;
                }
                if ("verbose".equalsIgnoreCase(entry.getKey())) {
                    ((StatusConfiguration)object2).withVerbosity(string2);
                    continue;
                }
                if ("packages".equalsIgnoreCase(string)) {
                    this.pluginPackages.addAll(Arrays.asList(string2.split(Patterns.COMMA_SEPARATOR)));
                    continue;
                }
                if ("name".equalsIgnoreCase(string)) {
                    this.setName(string2);
                    continue;
                }
                if ("monitorInterval".equalsIgnoreCase(string)) {
                    n = Integer.parseInt(string2);
                    continue;
                }
                if (!"advertiser".equalsIgnoreCase(string)) continue;
                this.createAdvertiser(string2, configurationSource, (byte[])object, "application/json");
            }
        }
        catch (Exception exception) {
            LOGGER.error("Error parsing " + configurationSource.getLocation(), (Throwable)exception);
            return;
        }
    }

    protected ObjectMapper getObjectMapper() {
        return new ObjectMapper().configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    @Override
    public void setup() {
        Iterator iterator = this.root.fields();
        List<Node> list = this.rootNode.getChildren();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            JsonNode object = (JsonNode)entry.getValue();
            if (object.isObject()) {
                LOGGER.debug("Processing node for object {}", entry.getKey());
                list.add(this.constructNode((String)entry.getKey(), this.rootNode, object));
                continue;
            }
            if (!object.isArray()) continue;
            LOGGER.error("Arrays are not supported at the root configuration.");
        }
        LOGGER.debug("Completed parsing configuration");
        if (this.status.size() > 0) {
            for (Status status : this.status) {
                LOGGER.error("Error processing element {}: {}", (Object)status.name, (Object)status.errorType);
            }
        }
    }

    @Override
    public Configuration reconfigure() {
        try {
            ConfigurationSource configurationSource = this.getConfigurationSource().resetInputStream();
            if (configurationSource == null) {
                return null;
            }
            return new JsonConfiguration(this.getLoggerContext(), configurationSource);
        }
        catch (IOException iOException) {
            LOGGER.error("Cannot locate file {}", (Object)this.getConfigurationSource(), (Object)iOException);
            return null;
        }
    }

    private Node constructNode(String string, Node node, JsonNode object) {
        Object object2;
        Object object3;
        PluginType<?> pluginType = this.pluginManager.getPluginType(string);
        node = new Node(node, string, pluginType);
        this.processAttributes(node, (JsonNode)object);
        object = object.fields();
        List<Node> list = node.getChildren();
        while (object.hasNext()) {
            object3 = (Map.Entry)object.next();
            object2 = (JsonNode)object3.getValue();
            if (object2.isArray() || object2.isObject()) {
                if (pluginType == null) {
                    this.status.add(new Status(string, (JsonNode)object2, ErrorType.CLASS_NOT_FOUND));
                }
                if (object2.isArray()) {
                    LOGGER.debug("Processing node for array {}", object3.getKey());
                    for (int i2 = 0; i2 < object2.size(); ++i2) {
                        Object object4 = this.getType(object2.get(i2), (String)object3.getKey());
                        Object object5 = this.pluginManager.getPluginType((String)object4);
                        object5 = new Node(node, (String)object3.getKey(), (PluginType<?>)object5);
                        this.processAttributes((Node)object5, object2.get(i2));
                        if (((String)object4).equals(object3.getKey())) {
                            LOGGER.debug("Processing {}[{}]", object3.getKey(), (Object)i2);
                        } else {
                            LOGGER.debug("Processing {} {}[{}]", object4, object3.getKey(), (Object)i2);
                        }
                        object4 = object2.get(i2).fields();
                        List<Node> list2 = ((Node)object5).getChildren();
                        while (object4.hasNext()) {
                            Object object6 = (Map.Entry)object4.next();
                            if (((JsonNode)object6.getValue()).isObject()) {
                                LOGGER.debug("Processing node for object {}", object6.getKey());
                                list2.add(this.constructNode((String)object6.getKey(), (Node)object5, (JsonNode)object6.getValue()));
                                continue;
                            }
                            if (!((JsonNode)object6.getValue()).isArray()) continue;
                            JsonNode jsonNode = (JsonNode)object6.getValue();
                            object6 = (String)object6.getKey();
                            LOGGER.debug("Processing array for object {}", object6);
                            for (int i3 = 0; i3 < jsonNode.size(); ++i3) {
                                list2.add(this.constructNode((String)object6, (Node)object5, jsonNode.get(i3)));
                            }
                        }
                        list.add((Node)object5);
                    }
                    continue;
                }
                LOGGER.debug("Processing node for object {}", object3.getKey());
                list.add(this.constructNode((String)object3.getKey(), node, (JsonNode)object2));
                continue;
            }
            LOGGER.debug("Node {} is of type {}", object3.getKey(), (Object)object2.getNodeType());
        }
        object3 = pluginType == null ? "null" : pluginType.getElementName() + ':' + pluginType.getPluginClass();
        object2 = node.getParent() == null ? "null" : (node.getParent().getName() == null ? "root" : node.getParent().getName());
        LOGGER.debug("Returning {} with parent {} of type {}", (Object)node.getName(), object2, object3);
        return node;
    }

    private String getType(JsonNode object, String string) {
        object = object.fields();
        while (object.hasNext()) {
            Map.Entry entry = (Map.Entry)object.next();
            if (!((String)entry.getKey()).equalsIgnoreCase("type") || !(entry = (JsonNode)entry.getValue()).isValueNode()) continue;
            return entry.asText();
        }
        return string;
    }

    private void processAttributes(Node object, JsonNode object2) {
        object = ((Node)object).getAttributes();
        object2 = object2.fields();
        while (object2.hasNext()) {
            JsonNode jsonNode;
            Map.Entry entry = (Map.Entry)object2.next();
            if (((String)entry.getKey()).equalsIgnoreCase("type") || !(jsonNode = (JsonNode)entry.getValue()).isValueNode()) continue;
            object.put(entry.getKey(), jsonNode.asText());
        }
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[location=" + this.getConfigurationSource() + "]";
    }

    static class Status {
        private final JsonNode node;
        private final String name;
        private final ErrorType errorType;

        public Status(String string, JsonNode jsonNode, ErrorType errorType) {
            this.name = string;
            this.node = jsonNode;
            this.errorType = errorType;
        }

        public String toString() {
            return "Status [name=" + this.name + ", errorType=" + (Object)((Object)this.errorType) + ", node=" + this.node + "]";
        }
    }

    static enum ErrorType {
        CLASS_NOT_FOUND;

    }
}

