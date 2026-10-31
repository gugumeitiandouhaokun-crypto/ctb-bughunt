/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.visitors;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.visitors.AbstractPluginVisitor;
import org.apache.logging.log4j.core.util.NameUtil;
import org.apache.logging.log4j.util.StringBuilders;

public class PluginAttributeVisitor
extends AbstractPluginVisitor<PluginAttribute> {
    public PluginAttributeVisitor() {
        super(PluginAttribute.class);
    }

    @Override
    public Object visit(Configuration object, Node object2, LogEvent object3, StringBuilder stringBuilder) {
        object = ((PluginAttribute)this.annotation).value();
        object2 = PluginAttributeVisitor.removeAttributeValue(((Node)object2).getAttributes(), (String)object, this.aliases);
        object2 = this.substitutor.replace((LogEvent)object3, (String)object2);
        object3 = this.findDefaultValue((LogEvent)object3);
        object2 = this.convert((String)object2, object3);
        object3 = ((PluginAttribute)this.annotation).sensitive() ? NameUtil.md5(object2 + this.getClass().getName()) : object2;
        StringBuilders.appendKeyDqValue(stringBuilder, (String)object, object3);
        return object2;
    }

    private Object findDefaultValue(LogEvent logEvent) {
        if (this.conversionType == Integer.TYPE || this.conversionType == Integer.class) {
            return ((PluginAttribute)this.annotation).defaultInt();
        }
        if (this.conversionType == Long.TYPE || this.conversionType == Long.class) {
            return ((PluginAttribute)this.annotation).defaultLong();
        }
        if (this.conversionType == Boolean.TYPE || this.conversionType == Boolean.class) {
            return ((PluginAttribute)this.annotation).defaultBoolean();
        }
        if (this.conversionType == Float.TYPE || this.conversionType == Float.class) {
            return Float.valueOf(((PluginAttribute)this.annotation).defaultFloat());
        }
        if (this.conversionType == Double.TYPE || this.conversionType == Double.class) {
            return ((PluginAttribute)this.annotation).defaultDouble();
        }
        if (this.conversionType == Byte.TYPE || this.conversionType == Byte.class) {
            return ((PluginAttribute)this.annotation).defaultByte();
        }
        if (this.conversionType == Character.TYPE || this.conversionType == Character.class) {
            return Character.valueOf(((PluginAttribute)this.annotation).defaultChar());
        }
        if (this.conversionType == Short.TYPE || this.conversionType == Short.class) {
            return ((PluginAttribute)this.annotation).defaultShort();
        }
        if (this.conversionType == Class.class) {
            return ((PluginAttribute)this.annotation).defaultClass();
        }
        return this.substitutor.replace(logEvent, ((PluginAttribute)this.annotation).defaultString());
    }
}

