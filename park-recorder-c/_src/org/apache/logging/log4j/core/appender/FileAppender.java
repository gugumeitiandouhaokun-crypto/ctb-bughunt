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
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.FileManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.filter.AbstractFilterable;
import org.apache.logging.log4j.core.net.Advertiser;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.util.Integers;

@Plugin(name="File", category="Core", elementType="appender", printObject=true)
public final class FileAppender
extends AbstractOutputStreamAppender<FileManager> {
    public static final String PLUGIN_NAME = "File";
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private final String fileName;
    private final Advertiser advertiser;
    private final Object advertisement;

    @Deprecated
    public static <B extends Builder<B>> FileAppender createAppender(String string, String string2, String string3, String string4, String string5, String string6, String string7, String string8, Layout<? extends Serializable> layout, Filter filter, String string9, String string10, Configuration configuration) {
        return ((Builder)((AbstractAppender.Builder)((Builder)((Builder)((Builder)((Builder)((AbstractFilterable.Builder)((Builder)((Builder)((Builder)((AbstractOutputStreamAppender.Builder)((Builder)((Builder)((Builder)FileAppender.newBuilder()).withAdvertise(Boolean.parseBoolean(string9))).withAdvertiseUri(string10)).withAppend(Booleans.parseBoolean(string2, true))).withBufferedIo(Booleans.parseBoolean(string7, true))).withBufferSize(Integers.parseInt(string8, 8192))).setConfiguration(configuration)).withFileName(string)).setFilter(filter)).setIgnoreExceptions(Booleans.parseBoolean(string6, true))).withImmediateFlush(Booleans.parseBoolean(string5, true))).setLayout(layout)).withLocking(Boolean.parseBoolean(string3))).setName(string4)).build();
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    private FileAppender(String string, Layout<? extends Serializable> layout, Filter object, FileManager fileManager, String string2, boolean bl, boolean bl2, Advertiser advertiser, Property[] propertyArray) {
        super(string, layout, (Filter)object, bl, bl2, propertyArray, fileManager);
        if (advertiser != null) {
            object = new HashMap<String, String>(layout.getContentFormat());
            object.putAll(fileManager.getContentFormat());
            object.put("contentType", layout.getContentType());
            object.put("name", string);
            this.advertisement = advertiser.advertise((Map<String, String>)object);
        } else {
            this.advertisement = null;
        }
        this.fileName = string2;
        this.advertiser = advertiser;
    }

    public final String getFileName() {
        return this.fileName;
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

    public static class Builder<B extends Builder<B>>
    extends AbstractOutputStreamAppender.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<FileAppender> {
        @PluginBuilderAttribute
        @Required
        private String fileName;
        @PluginBuilderAttribute
        private boolean append = true;
        @PluginBuilderAttribute
        private boolean locking;
        @PluginBuilderAttribute
        private boolean advertise;
        @PluginBuilderAttribute
        private String advertiseUri;
        @PluginBuilderAttribute
        private boolean createOnDemand;
        @PluginBuilderAttribute
        private String filePermissions;
        @PluginBuilderAttribute
        private String fileOwner;
        @PluginBuilderAttribute
        private String fileGroup;

        @Override
        public FileAppender build() {
            Layout<Serializable> layout;
            FileManager fileManager;
            boolean bl = this.isBufferedIo();
            int n = this.getBufferSize();
            if (this.locking && bl) {
                LOGGER.warn("Locking and buffering are mutually exclusive. No buffering will occur for {}", (Object)this.fileName);
                bl = false;
            }
            if (!bl && n > 0) {
                LOGGER.warn("The bufferSize is set to {} but bufferedIo is false: {}", (Object)n, (Object)bl);
            }
            if ((fileManager = FileManager.getFileManager(this.fileName, this.append, this.locking, bl, this.createOnDemand, this.advertiseUri, layout = this.getOrCreateLayout(), n, this.filePermissions, this.fileOwner, this.fileGroup, this.getConfiguration())) == null) {
                return null;
            }
            return new FileAppender(this.getName(), layout, this.getFilter(), fileManager, this.fileName, this.isIgnoreExceptions(), !bl || this.isImmediateFlush(), this.advertise ? this.getConfiguration().getAdvertiser() : null, this.getPropertyArray());
        }

        public String getAdvertiseUri() {
            return this.advertiseUri;
        }

        public String getFileName() {
            return this.fileName;
        }

        public boolean isAdvertise() {
            return this.advertise;
        }

        public boolean isAppend() {
            return this.append;
        }

        public boolean isCreateOnDemand() {
            return this.createOnDemand;
        }

        public boolean isLocking() {
            return this.locking;
        }

        public String getFilePermissions() {
            return this.filePermissions;
        }

        public String getFileOwner() {
            return this.fileOwner;
        }

        public String getFileGroup() {
            return this.fileGroup;
        }

        public B withAdvertise(boolean bl) {
            this.advertise = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B withAdvertiseUri(String string) {
            this.advertiseUri = string;
            return (B)((Builder)this.asBuilder());
        }

        public B withAppend(boolean bl) {
            this.append = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B withFileName(String string) {
            this.fileName = string;
            return (B)((Builder)this.asBuilder());
        }

        public B withCreateOnDemand(boolean bl) {
            this.createOnDemand = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B withLocking(boolean bl) {
            this.locking = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B withFilePermissions(String string) {
            this.filePermissions = string;
            return (B)((Builder)this.asBuilder());
        }

        public B withFileOwner(String string) {
            this.fileOwner = string;
            return (B)((Builder)this.asBuilder());
        }

        public B withFileGroup(String string) {
            this.fileGroup = string;
            return (B)((Builder)this.asBuilder());
        }
    }
}

