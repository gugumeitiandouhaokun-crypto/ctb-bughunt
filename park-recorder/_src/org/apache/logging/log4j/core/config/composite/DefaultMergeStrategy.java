/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.composite;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.composite.MergeStrategy;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.filter.CompositeFilter;

public class DefaultMergeStrategy
implements MergeStrategy {
    private static final String APPENDERS = "appenders";
    private static final String PROPERTIES = "properties";
    private static final String LOGGERS = "loggers";
    private static final String SCRIPTS = "scripts";
    private static final String FILTERS = "filters";
    private static final String STATUS = "status";
    private static final String NAME = "name";
    private static final String REF = "ref";

    @Override
    public void mergeRootProperties(Node node, AbstractConfiguration object) {
        for (Map.Entry<String, String> entry : ((AbstractConfiguration)((Object)object)).getRootNode().getAttributes().entrySet()) {
            int n = 0;
            for (Map.Entry<String, String> entry2 : node.getAttributes().entrySet()) {
                if (!entry2.getKey().equalsIgnoreCase(entry.getKey())) continue;
                if (entry.getKey().equalsIgnoreCase(STATUS)) {
                    Level level = Level.getLevel(entry2.getValue().toUpperCase());
                    Level level2 = Level.getLevel(entry.getValue().toUpperCase());
                    if (level != null && level2 != null) {
                        if (level2.isLessSpecificThan(level)) {
                            entry2.setValue(entry.getValue());
                        }
                    } else if (level2 != null) {
                        entry2.setValue(entry.getValue());
                    }
                } else if (entry.getKey().equalsIgnoreCase("monitorInterval")) {
                    n = Integer.parseInt(entry.getValue());
                    int n2 = Integer.parseInt(entry2.getValue());
                    if (n2 == 0 || n < n2) {
                        entry2.setValue(entry.getValue());
                    }
                } else {
                    entry2.setValue(entry.getValue());
                }
                n = 1;
            }
            if (n != 0) continue;
            node.getAttributes().put(entry.getKey(), entry.getValue());
        }
    }

    /*
     * Could not resolve type clashes
     * Unable to fully structure code
     */
    @Override
    public void mergConfigurations(Node var1_1, Node var2_2, PluginManager var3_3) {
        for (Node var4_4 : var2_2.getChildren()) {
            var5_5 = this.isFilterNode(var4_4);
            var6_6 = false;
            block11: for (Node var8_9 : var1_1.getChildren()) {
                if (var5_5) {
                    if (!this.isFilterNode(var8_9)) continue;
                    this.updateFilterNode(var1_1, var8_9, var4_4, var3_3);
                    var6_6 = true;
                    break;
                }
                if (!var8_9.getName().equalsIgnoreCase(var4_4.getName())) continue;
                var6_7 = var8_9.getName().toLowerCase();
                var9_10 = -1;
                switch (var6_7.hashCode()) {
                    case -926053069: {
                        if (!var6_7.equals("properties")) break;
                        var9_10 = 0;
                        break;
                    }
                    case 1926514952: {
                        if (!var6_7.equals("scripts")) break;
                        var9_10 = 1;
                        break;
                    }
                    case 2009213964: {
                        if (!var6_7.equals("appenders")) break;
                        var9_10 = 2;
                        break;
                    }
                    case 342277347: {
                        if (!var6_7.equals("loggers")) break;
                        var9_10 = 3;
                    }
                }
                switch (var9_10) {
                    case 0: 
                    case 1: 
                    case 2: {
                        for (Node var9_11 : var4_4.getChildren()) {
                            for (Node var11_14 : var8_9.getChildren()) {
                                if (!Objects.equals(var11_14.getAttributes().get("name"), var9_11.getAttributes().get("name"))) continue;
                                var8_9.getChildren().remove(var11_14);
                                break;
                            }
                            var8_9.getChildren().add(var9_11);
                        }
                        var6_6 = true;
                        continue block11;
                    }
                    case 3: {
                        var6_7 = new HashMap<String, Iterator<Node>>();
                        for (Iterator<Node> var10_13 : var8_9.getChildren()) {
                            var6_7.put(var10_13.getName(), var10_13);
                        }
                        for (Iterator<Node> var10_13 : var4_4.getChildren()) {
                            var11_14 = this.getLoggerNode(var8_9, var10_13.getAttributes().get("name"));
                            var6_7 = new Node(var8_9, var10_13.getName(), var10_13.getType());
                            if (var11_14 == null) ** GOTO lbl91
                            var11_14.getAttributes().putAll(var10_13.getAttributes());
                            var10_13 = var10_13.getChildren().iterator();
                            while (var10_13.hasNext()) {
                                var12_15 = var10_13.next();
                                if (this.isFilterNode(var12_15)) {
                                    var13_16 = false;
                                    for (Node var15_19 : var11_14.getChildren()) {
                                        if (!this.isFilterNode(var15_19)) continue;
                                        this.updateFilterNode((Node)var6_7, var15_19, var12_15, var3_3);
                                        var13_16 = true;
                                        break;
                                    }
                                    if (var13_16) continue;
                                    var14_18 = new Node((Node)var6_7, var12_15.getName(), var12_15.getType());
                                    var14_18.getAttributes().putAll(var12_15.getAttributes());
                                    var14_18.getChildren().addAll(var12_15.getChildren());
                                    var11_14.getChildren().add((Node)var14_18);
                                    continue;
                                }
                                var13_17 = new Node((Node)var6_7, var12_15.getName(), var12_15.getType());
                                var13_17.getAttributes().putAll(var12_15.getAttributes());
                                var13_17.getChildren().addAll(var12_15.getChildren());
                                if (!var13_17.getName().equalsIgnoreCase("AppenderRef")) ** GOTO lbl82
                                for (Node var15_19 : var11_14.getChildren()) {
                                    if (!this.isSameReference(var15_19, var13_17)) continue;
                                    var11_14.getChildren().remove(var15_19);
                                    ** GOTO lbl87
                                }
                                ** GOTO lbl87
lbl82:
                                // 2 sources

                                for (Node var15_19 : var11_14.getChildren()) {
                                    if (!this.isSameName(var15_19, var13_17)) continue;
                                    var11_14.getChildren().remove(var15_19);
                                    break;
                                }
lbl87:
                                // 4 sources

                                var11_14.getChildren().add(var13_17);
                            }
                            continue;
lbl91:
                            // 1 sources

                            var6_7.getAttributes().putAll(var10_13.getAttributes());
                            var6_7.getChildren().addAll(var10_13.getChildren());
                            var8_9.getChildren().add((Node)var6_7);
                        }
                        var6_6 = true;
                        continue block11;
                    }
                }
                var8_9.getChildren().addAll(var4_4.getChildren());
                var6_6 = true;
            }
            if (var6_6) continue;
            if (var4_4.getName().equalsIgnoreCase("Properties")) {
                var1_1.getChildren().add(0, var4_4);
                continue;
            }
            var1_1.getChildren().add(var4_4);
        }
    }

    private Node getLoggerNode(Node object, String string) {
        for (Node node : ((Node)object).getChildren()) {
            String string2 = node.getAttributes().get(NAME);
            if (string == null && string2 == null) {
                return node;
            }
            if (string2 == null || !string2.equals(string)) continue;
            return node;
        }
        return null;
    }

    private void updateFilterNode(Node object, Node node, Node object2, PluginManager object3) {
        if (CompositeFilter.class.isAssignableFrom(node.getType().getPluginClass())) {
            object3 = new Node(node, ((Node)object2).getName(), ((Node)object2).getType());
            ((Node)object3).getChildren().addAll(((Node)object2).getChildren());
            ((Node)object3).getAttributes().putAll(((Node)object2).getAttributes());
            node.getChildren().add((Node)object3);
            return;
        }
        object3 = ((PluginManager)object3).getPluginType(FILTERS);
        object3 = new Node(node, FILTERS, (PluginType<?>)object3);
        Node node2 = new Node((Node)object3, ((Node)object2).getName(), ((Node)object2).getType());
        node2.getAttributes().putAll(((Node)object2).getAttributes());
        object2 = ((Node)object3).getChildren();
        object2.add((Node)node);
        object2.add(node2);
        object = ((Node)object).getChildren();
        object.remove(node);
        object.add(object3);
    }

    private boolean isFilterNode(Node node) {
        return Filter.class.isAssignableFrom(node.getType().getPluginClass());
    }

    private boolean isSameName(Node object, Node node) {
        return (object = ((Node)object).getAttributes().get(NAME)) != null && ((String)object).toLowerCase().equals(node.getAttributes().get(NAME).toLowerCase());
    }

    private boolean isSameReference(Node object, Node node) {
        return (object = ((Node)object).getAttributes().get(REF)) != null && ((String)object).toLowerCase().equals(node.getAttributes().get(REF).toLowerCase());
    }
}

