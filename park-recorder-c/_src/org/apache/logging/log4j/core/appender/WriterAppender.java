/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.io.Writer;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AbstractWriterAppender;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.WriterManager;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.CloseShieldWriter;

@Plugin(name="Writer", category="Core", elementType="appender", printObject=true)
public final class WriterAppender
extends AbstractWriterAppender<WriterManager> {
    private static WriterManagerFactory factory = new WriterManagerFactory();

    @PluginFactory
    public static WriterAppender createAppender(StringLayout stringLayout, Filter filter, Writer writer, String string, boolean bl, boolean bl2) {
        if (string == null) {
            LOGGER.error("No name provided for WriterAppender");
            return null;
        }
        if (stringLayout == null) {
            stringLayout = PatternLayout.createDefaultLayout();
        }
        return new WriterAppender(string, stringLayout, filter, WriterAppender.getManager(writer, bl, stringLayout), bl2, null);
    }

    private static WriterManager getManager(Writer object, boolean bl, StringLayout stringLayout) {
        CloseShieldWriter closeShieldWriter = new CloseShieldWriter((Writer)object);
        object = object.getClass().getName() + "@" + Integer.toHexString(object.hashCode()) + '.' + bl;
        return WriterManager.getManager((String)object, new FactoryData(closeShieldWriter, (String)object, stringLayout), factory);
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    private WriterAppender(String string, StringLayout stringLayout, Filter filter, WriterManager writerManager, boolean bl, Property[] propertyArray) {
        super(string, stringLayout, filter, bl, true, propertyArray, writerManager);
    }

    static class WriterManagerFactory
    implements ManagerFactory<WriterManager, FactoryData> {
        private WriterManagerFactory() {
        }

        @Override
        public WriterManager createManager(String string, FactoryData factoryData) {
            return new WriterManager(factoryData.writer, factoryData.name, factoryData.layout, true);
        }
    }

    static class FactoryData {
        private final StringLayout layout;
        private final String name;
        private final Writer writer;

        public FactoryData(Writer writer, String string, StringLayout stringLayout) {
            this.writer = writer;
            this.name = string;
            this.layout = stringLayout;
        }
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractAppender.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<WriterAppender> {
        private boolean follow = false;
        private Writer target;

        @Override
        public WriterAppender build() {
            StringLayout stringLayout = (StringLayout)this.getLayout();
            stringLayout = stringLayout != null ? stringLayout : PatternLayout.createDefaultLayout();
            return new WriterAppender(this.getName(), stringLayout, this.getFilter(), WriterAppender.getManager(this.target, this.follow, stringLayout), this.isIgnoreExceptions(), this.getPropertyArray());
        }

        public B setFollow(boolean bl) {
            this.follow = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B setTarget(Writer writer) {
            this.target = writer;
            return (B)((Builder)this.asBuilder());
        }
    }
}

