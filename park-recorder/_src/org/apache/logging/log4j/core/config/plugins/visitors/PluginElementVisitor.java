/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.visitors;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.config.plugins.visitors.AbstractPluginVisitor;

public class PluginElementVisitor
extends AbstractPluginVisitor<PluginElement> {
    public PluginElementVisitor() {
        super(PluginElement.class);
    }

    @Override
    public Object visit(Configuration object, Node node, LogEvent object2, StringBuilder stringBuilder) {
        object = ((PluginElement)this.annotation).value();
        if (this.conversionType.isArray()) {
            PluginElementVisitor pluginElementVisitor = this;
            pluginElementVisitor.setConversionType(pluginElementVisitor.conversionType.getComponentType());
            object2 = new ArrayList<PluginType<Object>>();
            ArrayList<Node> arrayList = new ArrayList<Node>();
            stringBuilder.append("={");
            boolean bl = true;
            for (Node node2 : node.getChildren()) {
                PluginType<Object> pluginType = node2.getType();
                if (!((String)object).equalsIgnoreCase(pluginType.getElementName()) && !this.conversionType.isAssignableFrom(pluginType.getPluginClass())) continue;
                if (!bl) {
                    stringBuilder.append(", ");
                }
                bl = false;
                arrayList.add(node2);
                pluginType = node2.getObject();
                if (pluginType == null) {
                    LOGGER.error("Null object returned for {} in {}.", (Object)node2.getName(), (Object)node.getName());
                    continue;
                }
                if (pluginType.getClass().isArray()) {
                    stringBuilder.append(Arrays.toString((Object[])pluginType)).append('}');
                    node.getChildren().removeAll(arrayList);
                    return pluginType;
                }
                stringBuilder.append(node2.toString());
                object2.add(pluginType);
            }
            stringBuilder.append('}');
            if (!object2.isEmpty() && !this.conversionType.isAssignableFrom(object2.get(0).getClass())) {
                LOGGER.error("Attempted to assign attribute {} to list of type {} which is incompatible with {}.", object, (Object)object2.get(0).getClass(), (Object)this.conversionType);
                return null;
            }
            node.getChildren().removeAll(arrayList);
            Object[] objectArray = (Object[])Array.newInstance(this.conversionType, object2.size());
            for (int j = 0; j < objectArray.length; ++j) {
                objectArray[j] = object2.get(j);
            }
            return objectArray;
        }
        object2 = this.findNamedNode((String)object, node.getChildren());
        if (object2 == null) {
            stringBuilder.append((String)object).append("=null");
            return null;
        }
        stringBuilder.append(((Node)object2).getName()).append('(').append(((Node)object2).toString()).append(')');
        node.getChildren().remove(object2);
        return ((Node)object2).getObject();
    }

    private Node findNamedNode(String string, Iterable<Node> object) {
        object = object.iterator();
        while (object.hasNext()) {
            Node node = (Node)object.next();
            PluginType<?> pluginType = node.getType();
            if (!string.equalsIgnoreCase(pluginType.getElementName()) && !this.conversionType.isAssignableFrom(pluginType.getPluginClass())) continue;
            return node;
        }
        return null;
    }
}

