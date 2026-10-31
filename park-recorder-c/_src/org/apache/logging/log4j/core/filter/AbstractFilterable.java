/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.filter;

import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LifeCycle2;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.filter.CompositeFilter;
import org.apache.logging.log4j.core.filter.Filterable;

public abstract class AbstractFilterable
extends AbstractLifeCycle
implements Filterable {
    private volatile Filter filter;
    @PluginElement(value="Properties")
    private final Property[] propertyArray;

    public AbstractFilterable() {
        this(null, Property.EMPTY_ARRAY);
    }

    public AbstractFilterable(Filter filter) {
        this(filter, Property.EMPTY_ARRAY);
    }

    public AbstractFilterable(Filter filter, Property[] propertyArray) {
        this.filter = filter;
        this.propertyArray = propertyArray == null ? Property.EMPTY_ARRAY : propertyArray;
    }

    @Override
    public synchronized void addFilter(Filter filterArray) {
        if (filterArray == null) {
            return;
        }
        if (this.filter == null) {
            this.filter = filterArray;
            return;
        }
        if (this.filter instanceof CompositeFilter) {
            this.filter = ((CompositeFilter)this.filter).addFilter((Filter)filterArray);
            return;
        }
        filterArray = new Filter[]{this.filter, filterArray};
        this.filter = CompositeFilter.createFilters(filterArray);
    }

    @Override
    public Filter getFilter() {
        return this.filter;
    }

    @Override
    public boolean hasFilter() {
        return this.filter != null;
    }

    @Override
    public boolean isFiltered(LogEvent logEvent) {
        return this.filter != null && this.filter.filter(logEvent) == Filter.Result.DENY;
    }

    @Override
    public synchronized void removeFilter(Filter object) {
        if (this.filter == null || object == null) {
            return;
        }
        if (this.filter == object || this.filter.equals(object)) {
            this.filter = null;
            return;
        }
        if (this.filter instanceof CompositeFilter) {
            if (((CompositeFilter)(object = ((CompositeFilter)this.filter).removeFilter((Filter)object))).size() > 1) {
                this.filter = object;
                return;
            }
            if (((CompositeFilter)object).size() == 1) {
                object = ((CompositeFilter)object).iterator();
                this.filter = (Filter)object.next();
                return;
            }
            this.filter = null;
        }
    }

    @Override
    public void start() {
        this.setStarting();
        if (this.filter != null) {
            this.filter.start();
        }
        this.setStarted();
    }

    @Override
    public boolean stop(long l, TimeUnit timeUnit) {
        return this.stop(l, timeUnit, true);
    }

    public boolean stop(long l, TimeUnit timeUnit, boolean bl) {
        if (bl) {
            this.setStopping();
        }
        boolean bl2 = true;
        if (this.filter != null) {
            if (this.filter instanceof LifeCycle2) {
                bl2 = ((LifeCycle2)((Object)this.filter)).stop(l, timeUnit);
            } else {
                this.filter.stop();
                bl2 = true;
            }
        }
        if (bl) {
            this.setStopped();
        }
        return bl2;
    }

    public Property[] getPropertyArray() {
        return this.propertyArray;
    }

    public static abstract class Builder<B extends Builder<B>> {
        @PluginElement(value="Filter")
        private Filter filter;
        @PluginElement(value="Properties")
        private Property[] propertyArray;

        public B asBuilder() {
            return (B)this;
        }

        public Filter getFilter() {
            return this.filter;
        }

        public Property[] getPropertyArray() {
            return this.propertyArray;
        }

        public B setFilter(Filter filter) {
            this.filter = filter;
            return this.asBuilder();
        }

        public B setPropertyArray(Property[] propertyArray) {
            this.propertyArray = propertyArray;
            return this.asBuilder();
        }

        @Deprecated
        public B withFilter(Filter filter) {
            return this.setFilter(filter);
        }
    }
}

