/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rewrite;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="MapRewritePolicy", category="Core", elementType="rewritePolicy", printObject=true)
public final class MapRewritePolicy
implements RewritePolicy {
    protected static final Logger LOGGER = StatusLogger.getLogger();
    private final Map<String, Object> map;
    private final Mode mode;

    private MapRewritePolicy(Map<String, Object> map, Mode mode) {
        this.map = map;
        this.mode = mode;
    }

    @Override
    public final LogEvent rewrite(LogEvent logEvent) {
        Message message = logEvent.getMessage();
        if (message == null || !(message instanceof MapMessage)) {
            return logEvent;
        }
        message = (MapMessage)message;
        HashMap<String, Object> hashMap = new HashMap<String, Object>(((MapMessage)message).getData());
        switch (this.mode) {
            case Add: {
                hashMap.putAll(this.map);
                break;
            }
            default: {
                for (Map.Entry<String, Object> entry : this.map.entrySet()) {
                    if (!hashMap.containsKey(entry.getKey())) continue;
                    hashMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        Iterator<Map.Entry<String, Object>> iterator = ((MapMessage)message).newInstance(hashMap);
        return new Log4jLogEvent.Builder(logEvent).setMessage((Message)((Object)iterator)).build();
    }

    public final String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mode=").append((Object)this.mode);
        stringBuilder.append(" {");
        boolean bl = true;
        for (Map.Entry<String, Object> entry : this.map.entrySet()) {
            if (!bl) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(entry.getKey()).append('=').append(entry.getValue());
            bl = false;
        }
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    @PluginFactory
    public static MapRewritePolicy createPolicy(@PluginAttribute(value="mode") String object, @PluginElement(value="KeyValuePair") KeyValuePair[] keyValuePairArray) {
        Object object2 = object = object == null ? Mode.Add : Mode.valueOf(object);
        if (keyValuePairArray == null || keyValuePairArray.length == 0) {
            LOGGER.error("keys and values must be specified for the MapRewritePolicy");
            return null;
        }
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        for (KeyValuePair keyValuePair : keyValuePairArray) {
            String string = keyValuePair.getKey();
            if (string == null) {
                LOGGER.error("A null key is not valid in MapRewritePolicy");
                continue;
            }
            if (keyValuePair.getValue() == null) {
                LOGGER.error("A null value for key " + string + " is not allowed in MapRewritePolicy");
                continue;
            }
            hashMap.put(keyValuePair.getKey(), keyValuePair.getValue());
        }
        if (hashMap.isEmpty()) {
            LOGGER.error("MapRewritePolicy is not configured with any valid key value pairs");
            return null;
        }
        return new MapRewritePolicy(hashMap, (Mode)((Object)object));
    }

    public static enum Mode {
        Add,
        Update;

    }
}

