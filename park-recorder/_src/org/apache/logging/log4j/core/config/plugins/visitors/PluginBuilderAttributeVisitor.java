/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.visitors;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.visitors.AbstractPluginVisitor;
import org.apache.logging.log4j.core.util.NameUtil;
import org.apache.logging.log4j.util.StringBuilders;

public class PluginBuilderAttributeVisitor
extends AbstractPluginVisitor<PluginBuilderAttribute> {
    public PluginBuilderAttributeVisitor() {
        super(PluginBuilderAttribute.class);
    }

    @Override
    public Object visit(Configuration object, Node object2, LogEvent object3, StringBuilder stringBuilder) {
        object = ((PluginBuilderAttribute)this.annotation).value();
        object = ((String)object).isEmpty() ? this.member.getName() : object;
        object2 = PluginBuilderAttributeVisitor.removeAttributeValue(((Node)object2).getAttributes(), (String)object, this.aliases);
        object2 = this.substitutor.replace((LogEvent)object3, (String)object2);
        object2 = this.convert((String)object2, null);
        object3 = ((PluginBuilderAttribute)this.annotation).sensitive() ? NameUtil.md5(object2 + this.getClass().getName()) : object2;
        StringBuilders.appendKeyDqValue(stringBuilder, (String)object, object3);
        return object2;
    }
}

