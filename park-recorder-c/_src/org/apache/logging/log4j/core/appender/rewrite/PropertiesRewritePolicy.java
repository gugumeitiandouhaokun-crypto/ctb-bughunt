/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rewrite;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.ContextDataFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.StringMap;

@Plugin(name="PropertiesRewritePolicy", category="Core", elementType="rewritePolicy", printObject=true)
public final class PropertiesRewritePolicy
implements RewritePolicy {
    protected static final Logger LOGGER = StatusLogger.getLogger();
    private final Map<Property, Boolean> properties;
    private final Configuration config;

    private PropertiesRewritePolicy(Configuration object, List<Property> object2) {
        this.config = object;
        this.properties = new HashMap<Property, Boolean>(object2.size());
        object = object2.iterator();
        while (object.hasNext()) {
            object2 = (Property)object.next();
            Boolean bl = ((Property)object2).getValue().contains("${");
            this.properties.put((Property)object2, bl);
        }
    }

    @Override
    public final LogEvent rewrite(LogEvent logEvent) {
        StringMap stringMap = ContextDataFactory.createContextData(logEvent.getContextData());
        for (Map.Entry<Property, Boolean> entry : this.properties.entrySet()) {
            Property property = entry.getKey();
            stringMap.putValue(property.getName(), entry.getValue() != false ? this.config.getStrSubstitutor().replace(property.getValue()) : property.getValue());
        }
        return new Log4jLogEvent.Builder(logEvent).setContextData(stringMap).build();
    }

    public final String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" {");
        boolean bl = true;
        for (Map.Entry<Property, Boolean> entry : this.properties.entrySet()) {
            if (!bl) {
                stringBuilder.append(", ");
            }
            Property property = entry.getKey();
            stringBuilder.append(property.getName()).append('=').append(property.getValue());
            bl = false;
        }
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    @PluginFactory
    public static PropertiesRewritePolicy createPolicy(@PluginConfiguration Configuration configuration, @PluginElement(value="Properties") Property[] object) {
        if (object == null || ((Property[])object).length == 0) {
            LOGGER.error("Properties must be specified for the PropertiesRewritePolicy");
            return null;
        }
        object = Arrays.asList(object);
        return new PropertiesRewritePolicy(configuration, (List<Property>)object);
    }
}

