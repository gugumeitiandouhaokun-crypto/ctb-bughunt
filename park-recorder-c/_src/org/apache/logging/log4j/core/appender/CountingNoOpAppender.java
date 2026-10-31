/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name="CountingNoOp", category="Core", elementType="appender", printObject=true)
public class CountingNoOpAppender
extends AbstractAppender {
    private final AtomicLong total = new AtomicLong();

    public CountingNoOpAppender(String string, Layout<?> layout) {
        super(string, null, layout, true, Property.EMPTY_ARRAY);
    }

    private CountingNoOpAppender(String string, Layout<?> layout, Property[] propertyArray) {
        super(string, null, layout, true, propertyArray);
    }

    public long getCount() {
        return this.total.get();
    }

    @Override
    public void append(LogEvent logEvent) {
        this.total.incrementAndGet();
    }

    @PluginFactory
    public static CountingNoOpAppender createAppender(@PluginAttribute(value="name") String string) {
        return new CountingNoOpAppender(Objects.requireNonNull(string), null, Property.EMPTY_ARRAY);
    }
}

