/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.ContextDataInjector;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.impl.ContextDataInjectorFactory;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.ReadOnlyStringMap;

@Plugin(name="DynamicThresholdFilter", category="Core", elementType="filter", printObject=true)
@PerformanceSensitive(value={"allocation"})
public final class DynamicThresholdFilter
extends AbstractFilter {
    private Level defaultThreshold = Level.ERROR;
    private final String key;
    private final ContextDataInjector injector = ContextDataInjectorFactory.createInjector();
    private Map<String, Level> levelMap = new HashMap<String, Level>();

    /*
     * WARNING - void declaration
     */
    @PluginFactory
    public static DynamicThresholdFilter createFilter(@PluginAttribute(value="key") String string, @PluginElement(value="Pairs") KeyValuePair[] object, @PluginAttribute(value="defaultThreshold") Level level, @PluginAttribute(value="onMatch") Filter.Result result, @PluginAttribute(value="onMismatch") Filter.Result result2) {
        void var4_5;
        void var3_4;
        void var2_3;
        HashMap<String, Level> hashMap = new HashMap<String, Level>();
        for (Object object2 : object) {
            hashMap.put(((KeyValuePair)object2).getKey(), Level.toLevel(((KeyValuePair)object2).getValue()));
        }
        Level level2 = var2_3 == null ? Level.ERROR : var2_3;
        return new DynamicThresholdFilter(string, hashMap, level2, (Filter.Result)var3_4, (Filter.Result)var4_5);
    }

    private DynamicThresholdFilter(String string, Map<String, Level> map, Level level, Filter.Result result, Filter.Result result2) {
        super(result, result2);
        Objects.requireNonNull(string, "key cannot be null");
        this.key = string;
        this.levelMap = map;
        this.defaultThreshold = level;
    }

    public final boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!super.equalsImpl(object)) {
            return false;
        }
        if (this.getClass() != object.getClass()) {
            return false;
        }
        object = (DynamicThresholdFilter)object;
        if (this.defaultThreshold == null ? ((DynamicThresholdFilter)object).defaultThreshold != null : !this.defaultThreshold.equals(((DynamicThresholdFilter)object).defaultThreshold)) {
            return false;
        }
        if (this.key == null ? ((DynamicThresholdFilter)object).key != null : !this.key.equals(((DynamicThresholdFilter)object).key)) {
            return false;
        }
        return !(this.levelMap == null ? ((DynamicThresholdFilter)object).levelMap != null : !this.levelMap.equals(((DynamicThresholdFilter)object).levelMap));
    }

    private Filter.Result filter(Level level, ReadOnlyStringMap object) {
        if ((object = (String)object.getValue(this.key)) != null) {
            if ((object = this.levelMap.get(object)) == null) {
                object = this.defaultThreshold;
            }
            if (level.isMoreSpecificThan((Level)object)) {
                return this.onMatch;
            }
            return this.onMismatch;
        }
        return Filter.Result.NEUTRAL;
    }

    @Override
    public final Filter.Result filter(LogEvent logEvent) {
        return this.filter(logEvent.getLevel(), logEvent.getContextData());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
        return this.filter(level, this.currentContextData());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, Object object, Throwable throwable) {
        return this.filter(level, this.currentContextData());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object ... objectArray) {
        return this.filter(level, this.currentContextData());
    }

    private ReadOnlyStringMap currentContextData() {
        return this.injector.rawContextData();
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object) {
        return this.filter(level, this.currentContextData());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2) {
        return this.filter(level, this.currentContextData());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3) {
        return this.filter(level, this.currentContextData());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        return this.filter(level, this.currentContextData());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        return this.filter(level, this.currentContextData());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        return this.filter(level, this.currentContextData());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        return this.filter(level, this.currentContextData());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        return this.filter(level, this.currentContextData());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        return this.filter(level, this.currentContextData());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        return this.filter(level, this.currentContextData());
    }

    public final String getKey() {
        return this.key;
    }

    public final Map<String, Level> getLevelMap() {
        return this.levelMap;
    }

    public final int hashCode() {
        int n = super.hashCodeImpl();
        n = 31 * n + (this.defaultThreshold == null ? 0 : this.defaultThreshold.hashCode());
        n = 31 * n + (this.key == null ? 0 : this.key.hashCode());
        return 31 * n + (this.levelMap == null ? 0 : this.levelMap.hashCode());
    }

    @Override
    public final String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("key=").append(this.key);
        stringBuilder.append(", default=").append(this.defaultThreshold);
        if (this.levelMap.size() > 0) {
            stringBuilder.append('{');
            for (Map.Entry<String, Level> entry : this.levelMap.entrySet()) {
                stringBuilder.append(entry.getKey()).append('=').append(entry.getValue());
            }
            stringBuilder.append('}');
        }
        return stringBuilder.toString();
    }
}

