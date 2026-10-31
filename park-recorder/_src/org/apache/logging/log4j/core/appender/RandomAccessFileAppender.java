/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.RandomAccessFileManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.filter.AbstractFilterable;
import org.apache.logging.log4j.core.net.Advertiser;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.util.Integers;

@Plugin(name="RandomAccessFile", category="Core", elementType="appender", printObject=true)
public final class RandomAccessFileAppender
extends AbstractOutputStreamAppender<RandomAccessFileManager> {
    private final String fileName;
    private Object advertisement;
    private final Advertiser advertiser;

    private RandomAccessFileAppender(String string, Layout<? extends Serializable> layout, Filter object, RandomAccessFileManager randomAccessFileManager, String string2, boolean bl, boolean bl2, Advertiser advertiser, Property[] propertyArray) {
        super(string, layout, (Filter)object, bl, bl2, propertyArray, randomAccessFileManager);
        if (advertiser != null) {
            object = new HashMap<String, String>(layout.getContentFormat());
            object.putAll(randomAccessFileManager.getContentFormat());
            object.put("contentType", layout.getContentType());
            object.put("name", string);
            this.advertisement = advertiser.advertise((Map<String, String>)object);
        }
        this.fileName = string2;
        this.advertiser = advertiser;
    }

    @Override
    public final boolean stop(long l, TimeUnit timeUnit) {
        this.setStopping();
        super.stop(l, timeUnit, false);
        if (this.advertiser != null) {
            this.advertiser.unadvertise(this.advertisement);
        }
        this.setStopped();
        return true;
    }

    @Override
    public final void append(LogEvent logEvent) {
        ((RandomAccessFileManager)this.getManager()).setEndOfBatch(logEvent.isEndOfBatch());
        super.append(logEvent);
    }

    public final String getFileName() {
        return this.fileName;
    }

    public final int getBufferSize() {
        return ((RandomAccessFileManager)this.getManager()).getBufferSize();
    }

    @Deprecated
    public static <B extends Builder<B>> RandomAccessFileAppender createAppender(String string, String string2, String string3, String string4, String string5, String string6, Layout<? extends Serializable> layout, Filter filter, String string7, String string8, Configuration configuration) {
        boolean bl = Booleans.parseBoolean(string2, true);
        boolean bl2 = Booleans.parseBoolean(string4, true);
        boolean bl3 = Booleans.parseBoolean(string6, true);
        boolean bl4 = Boolean.parseBoolean(string7);
        int n = Integers.parseInt(string5, 262144);
        return ((Builder)((Builder)((Builder)((Builder)((Builder)((AbstractFilterable.Builder)((Builder)((Builder)((AbstractOutputStreamAppender.Builder)((Builder)((Builder)((Builder)RandomAccessFileAppender.newBuilder()).setAdvertise(bl4)).setAdvertiseURI(string8)).setAppend(bl)).withBufferSize(n)).setConfiguration(configuration)).setFileName(string)).setFilter(filter)).setIgnoreExceptions(bl3)).withImmediateFlush(bl2)).setLayout(layout)).setName(string3)).build();
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractOutputStreamAppender.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<RandomAccessFileAppender> {
        @PluginBuilderAttribute(value="fileName")
        private String fileName;
        @PluginBuilderAttribute(value="append")
        private boolean append = true;
        @PluginBuilderAttribute(value="advertise")
        private boolean advertise;
        @PluginBuilderAttribute(value="advertiseURI")
        private String advertiseURI;

        @Override
        public RandomAccessFileAppender build() {
            String string = this.getName();
            if (string == null) {
                LOGGER.error("No name provided for FileAppender");
                return null;
            }
            if (this.fileName == null) {
                LOGGER.error("No filename provided for FileAppender with name ".concat(String.valueOf(string)));
                return null;
            }
            Layout<Serializable> layout = this.getOrCreateLayout();
            boolean bl = this.isImmediateFlush();
            RandomAccessFileManager randomAccessFileManager = RandomAccessFileManager.getFileManager(this.fileName, this.append, bl, this.getBufferSize(), this.advertiseURI, layout, null);
            if (randomAccessFileManager == null) {
                return null;
            }
            return new RandomAccessFileAppender(string, layout, this.getFilter(), randomAccessFileManager, this.fileName, this.isIgnoreExceptions(), bl, this.advertise ? this.getConfiguration().getAdvertiser() : null, this.getPropertyArray());
        }

        public B setFileName(String string) {
            this.fileName = string;
            return (B)((Builder)this.asBuilder());
        }

        public B setAppend(boolean bl) {
            this.append = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B setAdvertise(boolean bl) {
            this.advertise = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B setAdvertiseURI(String string) {
            this.advertiseURI = string;
            return (B)((Builder)this.asBuilder());
        }
    }
}

