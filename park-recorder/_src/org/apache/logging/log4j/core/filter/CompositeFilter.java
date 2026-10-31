/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LifeCycle2;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.util.ObjectArrayIterator;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name="filters", category="Core", printObject=true)
@PerformanceSensitive(value={"allocation"})
public final class CompositeFilter
extends AbstractLifeCycle
implements Iterable<Filter>,
Filter {
    private static final Filter[] EMPTY_FILTERS = new Filter[0];
    private final Filter[] filters;

    private CompositeFilter() {
        this.filters = EMPTY_FILTERS;
    }

    private CompositeFilter(Filter[] filterArray) {
        this.filters = filterArray == null ? EMPTY_FILTERS : filterArray;
    }

    public final CompositeFilter addFilter(Filter filterArray) {
        if (filterArray == null) {
            return this;
        }
        if (filterArray instanceof CompositeFilter) {
            int n = this.filters.length + ((CompositeFilter)filterArray).size();
            Filter[] filterArray2 = Arrays.copyOf(this.filters, n);
            int n2 = this.filters.length;
            filterArray = ((CompositeFilter)filterArray).filters;
            int n3 = ((CompositeFilter)filterArray).filters.length;
            for (int j = 0; j < n3; ++j) {
                Filter filter;
                filterArray2[n2] = filter = filterArray[j];
            }
            return new CompositeFilter(filterArray2);
        }
        Filter[] filterArray3 = Arrays.copyOf(this.filters, this.filters.length + 1);
        Filter[] filterArray4 = filterArray3;
        filterArray3[this.filters.length] = filterArray;
        return new CompositeFilter(filterArray4);
    }

    public final CompositeFilter removeFilter(Filter filterArray) {
        if (filterArray == null) {
            return this;
        }
        ArrayList<Filter> arrayList = new ArrayList<Filter>(Arrays.asList(this.filters));
        if (filterArray instanceof CompositeFilter) {
            filterArray = ((CompositeFilter)filterArray).filters;
            int n = ((CompositeFilter)filterArray).filters.length;
            for (int j = 0; j < n; ++j) {
                Filter filter = filterArray[j];
                arrayList.remove(filter);
            }
        } else {
            arrayList.remove(filterArray);
        }
        return new CompositeFilter(arrayList.toArray(new Filter[this.filters.length - 1]));
    }

    @Override
    public final Iterator<Filter> iterator() {
        return new ObjectArrayIterator<Filter>(this.filters);
    }

    @Deprecated
    public final List<Filter> getFilters() {
        return Arrays.asList(this.filters);
    }

    public final Filter[] getFiltersArray() {
        return this.filters;
    }

    public final boolean isEmpty() {
        return this.filters.length == 0;
    }

    public final int size() {
        return this.filters.length;
    }

    @Override
    public final void start() {
        this.setStarting();
        Filter[] filterArray = this.filters;
        int n = this.filters.length;
        for (int j = 0; j < n; ++j) {
            filterArray[j].start();
        }
        this.setStarted();
    }

    @Override
    public final boolean stop(long l, TimeUnit timeUnit) {
        this.setStopping();
        Filter[] filterArray = this.filters;
        int n = this.filters.length;
        for (int j = 0; j < n; ++j) {
            Filter filter = filterArray[j];
            if (filter instanceof LifeCycle2) {
                ((LifeCycle2)((Object)filter)).stop(l, timeUnit);
                continue;
            }
            filter.stop();
        }
        this.setStopped();
        return true;
    }

    @Override
    public final Filter.Result getOnMismatch() {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public final Filter.Result getOnMatch() {
        return Filter.Result.NEUTRAL;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object ... objectArray) {
        Filter.Result result = Filter.Result.NEUTRAL;
        for (int j = 0; j < this.filters.length; ++j) {
            result = this.filters[j].filter(logger, level, marker, string, objectArray);
            if (result != Filter.Result.ACCEPT && result != Filter.Result.DENY) continue;
            return result;
        }
        return result;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object) {
        Filter.Result result = Filter.Result.NEUTRAL;
        for (int j = 0; j < this.filters.length; ++j) {
            result = this.filters[j].filter(logger, level, marker, string, object);
            if (result != Filter.Result.ACCEPT && result != Filter.Result.DENY) continue;
            return result;
        }
        return result;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2) {
        Filter.Result result = Filter.Result.NEUTRAL;
        for (int j = 0; j < this.filters.length; ++j) {
            result = this.filters[j].filter(logger, level, marker, string, object, object2);
            if (result != Filter.Result.ACCEPT && result != Filter.Result.DENY) continue;
            return result;
        }
        return result;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3) {
        Filter.Result result = Filter.Result.NEUTRAL;
        for (int j = 0; j < this.filters.length; ++j) {
            result = this.filters[j].filter(logger, level, marker, string, object, object2, object3);
            if (result != Filter.Result.ACCEPT && result != Filter.Result.DENY) continue;
            return result;
        }
        return result;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        Filter.Result result = Filter.Result.NEUTRAL;
        for (int j = 0; j < this.filters.length; ++j) {
            result = this.filters[j].filter(logger, level, marker, string, object, object2, object3, object4);
            if (result != Filter.Result.ACCEPT && result != Filter.Result.DENY) continue;
            return result;
        }
        return result;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        Filter.Result result = Filter.Result.NEUTRAL;
        for (int j = 0; j < this.filters.length; ++j) {
            result = this.filters[j].filter(logger, level, marker, string, object, object2, object3, object4, object5);
            if (result != Filter.Result.ACCEPT && result != Filter.Result.DENY) continue;
            return result;
        }
        return result;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Filter.Result result = Filter.Result.NEUTRAL;
        for (int j = 0; j < this.filters.length; ++j) {
            result = this.filters[j].filter(logger, level, marker, string, object, object2, object3, object4, object5, object6);
            if (result != Filter.Result.ACCEPT && result != Filter.Result.DENY) continue;
            return result;
        }
        return result;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        Filter.Result result = Filter.Result.NEUTRAL;
        for (int j = 0; j < this.filters.length; ++j) {
            result = this.filters[j].filter(logger, level, marker, string, object, object2, object3, object4, object5, object6, object7);
            if (result != Filter.Result.ACCEPT && result != Filter.Result.DENY) continue;
            return result;
        }
        return result;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        Filter.Result result = Filter.Result.NEUTRAL;
        for (int j = 0; j < this.filters.length; ++j) {
            result = this.filters[j].filter(logger, level, marker, string, object, object2, object3, object4, object5, object6, object7, object8);
            if (result != Filter.Result.ACCEPT && result != Filter.Result.DENY) continue;
            return result;
        }
        return result;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        Filter.Result result = Filter.Result.NEUTRAL;
        for (int j = 0; j < this.filters.length; ++j) {
            result = this.filters[j].filter(logger, level, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9);
            if (result != Filter.Result.ACCEPT && result != Filter.Result.DENY) continue;
            return result;
        }
        return result;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        Filter.Result result = Filter.Result.NEUTRAL;
        for (int j = 0; j < this.filters.length; ++j) {
            result = this.filters[j].filter(logger, level, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
            if (result != Filter.Result.ACCEPT && result != Filter.Result.DENY) continue;
            return result;
        }
        return result;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, Object object, Throwable throwable) {
        Filter.Result result = Filter.Result.NEUTRAL;
        for (int j = 0; j < this.filters.length; ++j) {
            result = this.filters[j].filter(logger, level, marker, object, throwable);
            if (result != Filter.Result.ACCEPT && result != Filter.Result.DENY) continue;
            return result;
        }
        return result;
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
        Filter.Result result = Filter.Result.NEUTRAL;
        for (int j = 0; j < this.filters.length; ++j) {
            result = this.filters[j].filter(logger, level, marker, message, throwable);
            if (result != Filter.Result.ACCEPT && result != Filter.Result.DENY) continue;
            return result;
        }
        return result;
    }

    @Override
    public final Filter.Result filter(LogEvent logEvent) {
        Filter.Result result = Filter.Result.NEUTRAL;
        for (int j = 0; j < this.filters.length; ++j) {
            result = this.filters[j].filter(logEvent);
            if (result != Filter.Result.ACCEPT && result != Filter.Result.DENY) continue;
            return result;
        }
        return result;
    }

    public final String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int j = 0; j < this.filters.length; ++j) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append('{');
            } else {
                stringBuilder.append(", ");
            }
            stringBuilder.append(this.filters[j].toString());
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.append('}');
        }
        return stringBuilder.toString();
    }

    @PluginFactory
    public static CompositeFilter createFilters(@PluginElement(value="Filters") Filter[] filterArray) {
        return new CompositeFilter(filterArray);
    }
}

