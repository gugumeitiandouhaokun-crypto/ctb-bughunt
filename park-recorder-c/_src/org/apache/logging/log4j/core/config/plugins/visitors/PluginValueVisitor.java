/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.visitors;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.PluginValue;
import org.apache.logging.log4j.core.config.plugins.visitors.AbstractPluginVisitor;
import org.apache.logging.log4j.util.StringBuilders;
import org.apache.logging.log4j.util.Strings;

public class PluginValueVisitor
extends AbstractPluginVisitor<PluginValue> {
    public PluginValueVisitor() {
        super(PluginValue.class);
    }

    @Override
    public Object visit(Configuration object, Node object2, LogEvent logEvent, StringBuilder stringBuilder) {
        object = ((PluginValue)this.annotation).value();
        String string = ((Node)object2).getValue();
        String string2 = ((Node)object2).getAttributes().get(object);
        if (Strings.isNotEmpty(string)) {
            if (Strings.isNotEmpty(string2)) {
                LOGGER.error("Configuration contains {} with both attribute value ({}) AND element value ({}). Please specify only one value. Using the element value.", (Object)((Node)object2).getName(), (Object)string2, (Object)string);
            }
            object2 = string;
        } else {
            object2 = PluginValueVisitor.removeAttributeValue(((Node)object2).getAttributes(), (String)object, new String[0]);
        }
        object2 = this.substitutor.replace(logEvent, (String)object2);
        StringBuilders.appendKeyDqValue(stringBuilder, (String)object, object2);
        return object2;
    }
}

