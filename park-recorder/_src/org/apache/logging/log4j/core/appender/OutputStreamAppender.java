/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.io.OutputStream;
import java.io.Serializable;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.CloseShieldOutputStream;
import org.apache.logging.log4j.core.util.NullOutputStream;

@Plugin(name="OutputStream", category="Core", elementType="appender", printObject=true)
public final class OutputStreamAppender
extends AbstractOutputStreamAppender<OutputStreamManager> {
    private static OutputStreamManagerFactory factory = new OutputStreamManagerFactory();

    @PluginFactory
    public static OutputStreamAppender createAppender(Layout<? extends Serializable> patternLayout, Filter filter, OutputStream outputStream, String string, boolean bl, boolean bl2) {
        if (string == null) {
            LOGGER.error("No name provided for OutputStreamAppender");
            return null;
        }
        if (patternLayout == null) {
            patternLayout = PatternLayout.createDefaultLayout();
        }
        return new OutputStreamAppender(string, (Layout<? extends Serializable>)patternLayout, filter, OutputStreamAppender.getManager(outputStream, bl, patternLayout), bl2, null);
    }

    private static OutputStreamManager getManager(OutputStream object, boolean bl, Layout<? extends Serializable> layout) {
        OutputStream outputStream = object == null ? NullOutputStream.getInstance() : new CloseShieldOutputStream((OutputStream)object);
        object = object == null ? outputStream : object;
        object = object.getClass().getName() + "@" + Integer.toHexString(object.hashCode()) + '.' + bl;
        return OutputStreamManager.getManager((String)object, new FactoryData(outputStream, (String)object, layout), factory);
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    private OutputStreamAppender(String string, Layout<? extends Serializable> layout, Filter filter, OutputStreamManager outputStreamManager, boolean bl, Property[] propertyArray) {
        super(string, layout, filter, bl, true, propertyArray, outputStreamManager);
    }

    static class OutputStreamManagerFactory
    implements ManagerFactory<OutputStreamManager, FactoryData> {
        private OutputStreamManagerFactory() {
        }

        @Override
        public OutputStreamManager createManager(String string, FactoryData factoryData) {
            return new OutputStreamManager(factoryData.os, factoryData.name, factoryData.layout, true);
        }
    }

    static class FactoryData {
        private final Layout<? extends Serializable> layout;
        private final String name;
        private final OutputStream os;

        public FactoryData(OutputStream outputStream, String string, Layout<? extends Serializable> layout) {
            this.os = outputStream;
            this.name = string;
            this.layout = layout;
        }
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractOutputStreamAppender.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<OutputStreamAppender> {
        private boolean follow = false;
        private final boolean ignoreExceptions = true;
        private OutputStream target;

        @Override
        public OutputStreamAppender build() {
            Layout<String> layout = this.getLayout();
            layout = layout == null ? PatternLayout.createDefaultLayout() : layout;
            return new OutputStreamAppender(this.getName(), layout, this.getFilter(), OutputStreamAppender.getManager(this.target, this.follow, layout), true, this.getPropertyArray());
        }

        public B setFollow(boolean bl) {
            this.follow = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B setTarget(OutputStream outputStream) {
            this.target = outputStream;
            return (B)((Builder)this.asBuilder());
        }
    }
}

