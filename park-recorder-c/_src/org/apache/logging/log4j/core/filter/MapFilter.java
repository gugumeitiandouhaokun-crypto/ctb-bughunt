/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.IndexedReadOnlyStringMap;
import org.apache.logging.log4j.util.IndexedStringMap;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.SortedArrayStringMap;

@Plugin(name="MapFilter", category="Core", elementType="filter", printObject=true)
@PerformanceSensitive(value={"allocation"})
public class MapFilter
extends AbstractFilter {
    private final IndexedStringMap map;
    private final boolean isAnd;

    protected MapFilter(Map<String, List<String>> object, boolean bl, Filter.Result result, Filter.Result result2) {
        super(result, result2);
        this.isAnd = bl;
        Objects.requireNonNull(object, "map cannot be null");
        this.map = new SortedArrayStringMap(object.size());
        for (Map.Entry entry : object.entrySet()) {
            this.map.putValue((String)entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
        if (message instanceof MapMessage) {
            if (this.filter((MapMessage)message)) {
                return this.onMatch;
            }
            return this.onMismatch;
        }
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result filter(LogEvent serializable) {
        if ((serializable = serializable.getMessage()) instanceof MapMessage) {
            if (this.filter((MapMessage)serializable)) {
                return this.onMatch;
            }
            return this.onMismatch;
        }
        return Filter.Result.NEUTRAL;
    }

    protected boolean filter(MapMessage<?, ?> mapMessage) {
        boolean bl = false;
        for (int i2 = 0; i2 < this.map.size(); ++i2) {
            String string = mapMessage.get(this.map.getKeyAt(i2));
            boolean bl2 = bl = string != null && ((List)this.map.getValueAt(i2)).contains(string);
            if (!this.isAnd && bl || this.isAnd && !bl) break;
        }
        return bl;
    }

    protected boolean filter(Map<String, String> map) {
        boolean bl = false;
        for (int i2 = 0; i2 < this.map.size(); ++i2) {
            String string = map.get(this.map.getKeyAt(i2));
            boolean bl2 = bl = string != null && ((List)this.map.getValueAt(i2)).contains(string);
            if (!this.isAnd && bl || this.isAnd && !bl) break;
        }
        return bl;
    }

    protected boolean filter(ReadOnlyStringMap readOnlyStringMap) {
        boolean bl = false;
        for (int i2 = 0; i2 < this.map.size(); ++i2) {
            String string = (String)readOnlyStringMap.getValue(this.map.getKeyAt(i2));
            boolean bl2 = bl = string != null && ((List)this.map.getValueAt(i2)).contains(string);
            if (!this.isAnd && bl || this.isAnd && !bl) break;
        }
        return bl;
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object) {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2) {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3) {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("isAnd=").append(this.isAnd);
        if (this.map.size() > 0) {
            stringBuilder.append(", {");
            for (int i2 = 0; i2 < this.map.size(); ++i2) {
                Object object;
                if (i2 > 0) {
                    stringBuilder.append(", ");
                }
                object = (object = (List)this.map.getValueAt(i2)).size() > 1 ? (String)object.get(0) : object.toString();
                stringBuilder.append(this.map.getKeyAt(i2)).append('=').append((String)object);
            }
            stringBuilder.append('}');
        }
        return stringBuilder.toString();
    }

    protected boolean isAnd() {
        return this.isAnd;
    }

    @Deprecated
    protected Map<String, List<String>> getMap() {
        final HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>(this.map.size());
        this.map.forEach(new BiConsumer<String, List<String>>(){

            @Override
            public void accept(String string, List<String> list) {
                hashMap.put(string, list);
            }
        });
        return hashMap;
    }

    protected IndexedReadOnlyStringMap getStringMap() {
        return this.map;
    }

    @PluginFactory
    public static MapFilter createFilter(@PluginElement(value="Pairs") KeyValuePair[] keyValuePairArray, @PluginAttribute(value="operator") String string, @PluginAttribute(value="onMatch") Filter.Result result, @PluginAttribute(value="onMismatch") Filter.Result result2) {
        if (keyValuePairArray == null || keyValuePairArray.length == 0) {
            LOGGER.error("keys and values must be specified for the MapFilter");
            return null;
        }
        HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();
        for (KeyValuePair keyValuePair : keyValuePairArray) {
            Object object = keyValuePair.getKey();
            if (object == null) {
                LOGGER.error("A null key is not valid in MapFilter");
                continue;
            }
            String string2 = keyValuePair.getValue();
            if (string2 == null) {
                LOGGER.error("A null value for key " + (String)object + " is not allowed in MapFilter");
                continue;
            }
            object = (List)hashMap.get(keyValuePair.getKey());
            if (object != null) {
                object.add(string2);
                continue;
            }
            object = new ArrayList<String>();
            object.add(string2);
            hashMap.put(keyValuePair.getKey(), (List<String>)object);
        }
        if (hashMap.isEmpty()) {
            LOGGER.error("MapFilter is not configured with any valid key value pairs");
            return null;
        }
        boolean bl = string == null || !string.equalsIgnoreCase("or");
        return new MapFilter(hashMap, bl, result, result2);
    }
}

