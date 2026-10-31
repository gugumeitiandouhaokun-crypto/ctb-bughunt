/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.mom.kafka;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.mom.kafka.KafkaManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.layout.SerializedLayout;

@Plugin(name="Kafka", category="Core", elementType="appender", printObject=true)
public final class KafkaAppender
extends AbstractAppender {
    private final Integer retryCount;
    private final KafkaManager manager;

    @Deprecated
    public static KafkaAppender createAppender(Layout<? extends Serializable> layout, Filter filter, String string, boolean bl, String object, Property[] propertyArray, Configuration configuration, String string2) {
        if (layout == null) {
            AbstractLifeCycle.LOGGER.error("No layout provided for KafkaAppender");
            return null;
        }
        object = KafkaManager.getManager(configuration.getLoggerContext(), string, (String)object, true, propertyArray, string2);
        return new KafkaAppender(string, layout, filter, bl, (KafkaManager)object, null, null);
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    private KafkaAppender(String string, Layout<? extends Serializable> layout, Filter filter, boolean bl, KafkaManager kafkaManager, Property[] propertyArray, Integer n) {
        super(string, filter, layout, bl, propertyArray);
        this.manager = Objects.requireNonNull(kafkaManager, "manager");
        this.retryCount = n;
    }

    @Override
    public final void append(LogEvent logEvent) {
        if (logEvent.getLoggerName() != null && logEvent.getLoggerName().startsWith("org.apache.kafka")) {
            LOGGER.warn("Recursive logging from [{}] for appender [{}].", (Object)logEvent.getLoggerName(), (Object)this.getName());
            return;
        }
        try {
            this.tryAppend(logEvent);
            return;
        }
        catch (Exception exception) {
            if (this.retryCount != null) {
                for (int i2 = 0; i2 < this.retryCount; ++i2) {
                    try {
                        this.tryAppend(logEvent);
                        break;
                    }
                    catch (Exception exception2) {
                        continue;
                    }
                }
            }
            this.error("Unable to write to Kafka in appender [" + this.getName() + "]", logEvent, exception);
            return;
        }
    }

    private void tryAppend(LogEvent object) {
        Layout<? extends Serializable> layout = this.getLayout();
        if (layout instanceof SerializedLayout) {
            byte[] byArray = layout.getHeader();
            layout = (Layout<? extends Serializable>)layout.toByteArray((LogEvent)object);
            object = new byte[byArray.length + ((Object)layout).length];
            System.arraycopy(byArray, 0, object, 0, byArray.length);
            System.arraycopy(layout, 0, object, byArray.length, ((Object)layout).length);
        } else {
            object = layout.toByteArray((LogEvent)object);
        }
        this.manager.send((byte[])object);
    }

    @Override
    public final void start() {
        super.start();
        this.manager.startup();
    }

    @Override
    public final boolean stop(long l, TimeUnit timeUnit) {
        this.setStopping();
        boolean bl = super.stop(l, timeUnit, false) & this.manager.stop(l, timeUnit);
        this.setStopped();
        return bl;
    }

    @Override
    public final String toString() {
        return "KafkaAppender{name=" + this.getName() + ", state=" + (Object)((Object)this.getState()) + ", topic=" + this.manager.getTopic() + '}';
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractAppender.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<KafkaAppender> {
        @PluginAttribute(value="retryCount")
        private String retryCount;
        @PluginAttribute(value="topic")
        private String topic;
        @PluginAttribute(value="key")
        private String key;
        @PluginAttribute(value="syncSend", defaultBoolean=true)
        private boolean syncSend;

        @Override
        public KafkaAppender build() {
            Layout<Serializable> layout = this.getLayout();
            if (layout == null) {
                LOGGER.error("No layout provided for KafkaAppender");
                return null;
            }
            KafkaManager kafkaManager = KafkaManager.getManager(this.getConfiguration().getLoggerContext(), this.getName(), this.topic, this.syncSend, this.getPropertyArray(), this.key);
            return new KafkaAppender(this.getName(), layout, this.getFilter(), this.isIgnoreExceptions(), kafkaManager, this.getPropertyArray(), this.getRetryCount());
        }

        public String getTopic() {
            return this.topic;
        }

        public boolean isSyncSend() {
            return this.syncSend;
        }

        public B setTopic(String string) {
            this.topic = string;
            return (B)((Builder)this.asBuilder());
        }

        public B setSyncSend(boolean bl) {
            this.syncSend = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B setKey(String string) {
            this.key = string;
            return (B)((Builder)this.asBuilder());
        }

        public Integer getRetryCount() {
            Integer n = null;
            try {
                n = Integer.valueOf(this.retryCount);
            }
            catch (NumberFormatException numberFormatException) {}
            return n;
        }
    }
}

