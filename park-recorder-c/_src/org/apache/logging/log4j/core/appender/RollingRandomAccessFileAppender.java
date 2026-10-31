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
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.DirectFileRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.DirectWriteRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.RollingRandomAccessFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.filter.AbstractFilterable;
import org.apache.logging.log4j.core.net.Advertiser;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.util.Integers;

@Plugin(name="RollingRandomAccessFile", category="Core", elementType="appender", printObject=true)
public final class RollingRandomAccessFileAppender
extends AbstractOutputStreamAppender<RollingRandomAccessFileManager> {
    private final String fileName;
    private final String filePattern;
    private final Object advertisement;
    private final Advertiser advertiser;

    private RollingRandomAccessFileAppender(String string, Layout<? extends Serializable> layout, Filter object, RollingRandomAccessFileManager rollingRandomAccessFileManager, String string2, String string3, boolean bl, boolean bl2, int n, Advertiser advertiser, Property[] propertyArray) {
        super(string, layout, (Filter)object, bl, bl2, propertyArray, rollingRandomAccessFileManager);
        if (advertiser != null) {
            object = new HashMap<String, String>(layout.getContentFormat());
            object.put((String)"contentType", (String)layout.getContentType());
            object.put("name", string);
            this.advertisement = advertiser.advertise((Map<String, String>)object);
        } else {
            this.advertisement = null;
        }
        this.fileName = string2;
        this.filePattern = string3;
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
        RollingRandomAccessFileManager rollingRandomAccessFileManager = (RollingRandomAccessFileManager)this.getManager();
        rollingRandomAccessFileManager.checkRollover(logEvent);
        rollingRandomAccessFileManager.setEndOfBatch(logEvent.isEndOfBatch());
        super.append(logEvent);
    }

    public final String getFileName() {
        return this.fileName;
    }

    public final String getFilePattern() {
        return this.filePattern;
    }

    public final int getBufferSize() {
        return ((RollingRandomAccessFileManager)this.getManager()).getBufferSize();
    }

    @Deprecated
    public static <B extends Builder<B>> RollingRandomAccessFileAppender createAppender(String string, String string2, String string3, String string4, String string5, String string6, TriggeringPolicy triggeringPolicy, RolloverStrategy rolloverStrategy, Layout<? extends Serializable> layout, Filter filter, String string7, String string8, String string9, Configuration configuration) {
        boolean bl = Booleans.parseBoolean(string3, true);
        boolean bl2 = Booleans.parseBoolean(string7, true);
        boolean bl3 = Booleans.parseBoolean(string5, true);
        boolean bl4 = Boolean.parseBoolean(string8);
        int n = Integers.parseInt(string6, 262144);
        return ((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((Builder)((AbstractFilterable.Builder)((Builder)((Builder)((Builder)((AbstractOutputStreamAppender.Builder)((Builder)((Builder)((Builder)RollingRandomAccessFileAppender.newBuilder()).withAdvertise(bl4)).withAdvertiseURI(string9)).withAppend(bl)).withBufferSize(n)).setConfiguration(configuration)).withFileName(string)).withFilePattern(string2)).setFilter(filter)).setIgnoreExceptions(bl2)).withImmediateFlush(bl3)).setLayout(layout)).setName(string4)).withPolicy(triggeringPolicy)).withStrategy(rolloverStrategy)).build();
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractOutputStreamAppender.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<RollingRandomAccessFileAppender> {
        @PluginBuilderAttribute(value="fileName")
        private String fileName;
        @PluginBuilderAttribute(value="filePattern")
        private String filePattern;
        @PluginBuilderAttribute(value="append")
        private boolean append = true;
        @PluginElement(value="Policy")
        private TriggeringPolicy policy;
        @PluginElement(value="Strategy")
        private RolloverStrategy strategy;
        @PluginBuilderAttribute(value="advertise")
        private boolean advertise;
        @PluginBuilderAttribute(value="advertiseURI")
        private String advertiseURI;
        @PluginBuilderAttribute
        private String filePermissions;
        @PluginBuilderAttribute
        private String fileOwner;
        @PluginBuilderAttribute
        private String fileGroup;

        public Builder() {
            this.withBufferSize(262144);
            this.setIgnoreExceptions(true);
            this.withImmediateFlush(true);
        }

        @Override
        public RollingRandomAccessFileAppender build() {
            int n;
            String string = this.getName();
            if (string == null) {
                LOGGER.error("No name provided for FileAppender");
                return null;
            }
            if (this.strategy == null) {
                this.strategy = this.fileName != null ? DefaultRolloverStrategy.newBuilder().withCompressionLevelStr("-1").withConfig(this.getConfiguration()).build() : DirectWriteRolloverStrategy.newBuilder().withCompressionLevelStr("-1").withConfig(this.getConfiguration()).build();
            } else if (this.fileName == null && !(this.strategy instanceof DirectFileRolloverStrategy)) {
                LOGGER.error("RollingFileAppender '{}': When no file name is provided a DirectFileRolloverStrategy must be configured");
                return null;
            }
            if (this.filePattern == null) {
                LOGGER.error("No filename pattern provided for FileAppender with name ".concat(String.valueOf(string)));
                return null;
            }
            if (this.policy == null) {
                LOGGER.error("A TriggeringPolicy must be provided");
                return null;
            }
            Layout<Serializable> layout = this.getOrCreateLayout();
            boolean bl = this.isImmediateFlush();
            RollingRandomAccessFileManager rollingRandomAccessFileManager = RollingRandomAccessFileManager.getRollingRandomAccessFileManager(this.fileName, this.filePattern, this.append, bl, n = this.getBufferSize(), this.policy, this.strategy, this.advertiseURI, layout, this.filePermissions, this.fileOwner, this.fileGroup, this.getConfiguration());
            if (rollingRandomAccessFileManager == null) {
                return null;
            }
            rollingRandomAccessFileManager.initialize();
            return new RollingRandomAccessFileAppender(string, layout, this.getFilter(), rollingRandomAccessFileManager, this.fileName, this.filePattern, this.isIgnoreExceptions(), bl, n, this.advertise ? this.getConfiguration().getAdvertiser() : null, this.getPropertyArray());
        }

        public B withFileName(String string) {
            this.fileName = string;
            return (B)((Builder)this.asBuilder());
        }

        public B withFilePattern(String string) {
            this.filePattern = string;
            return (B)((Builder)this.asBuilder());
        }

        public B withAppend(boolean bl) {
            this.append = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B withPolicy(TriggeringPolicy triggeringPolicy) {
            this.policy = triggeringPolicy;
            return (B)((Builder)this.asBuilder());
        }

        public B withStrategy(RolloverStrategy rolloverStrategy) {
            this.strategy = rolloverStrategy;
            return (B)((Builder)this.asBuilder());
        }

        public B withAdvertise(boolean bl) {
            this.advertise = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B withAdvertiseURI(String string) {
            this.advertiseURI = string;
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

