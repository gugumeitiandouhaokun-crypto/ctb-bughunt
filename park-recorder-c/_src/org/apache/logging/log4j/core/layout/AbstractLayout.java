/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.layout;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.status.StatusLogger;

public abstract class AbstractLayout<T extends Serializable>
implements Layout<T> {
    protected static final Logger LOGGER = StatusLogger.getLogger();
    protected final Configuration configuration;
    protected long eventCount;
    protected final byte[] footer;
    protected final byte[] header;

    @Deprecated
    public AbstractLayout(byte[] byArray, byte[] byArray2) {
        this(null, byArray, byArray2);
    }

    public AbstractLayout(Configuration configuration, byte[] byArray, byte[] byArray2) {
        this.configuration = configuration;
        this.header = byArray;
        this.footer = byArray2;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public Map<String, String> getContentFormat() {
        return new HashMap<String, String>();
    }

    @Override
    public byte[] getFooter() {
        return this.footer;
    }

    @Override
    public byte[] getHeader() {
        return this.header;
    }

    protected void markEvent() {
        ++this.eventCount;
    }

    @Override
    public void encode(LogEvent object, ByteBufferDestination byteBufferDestination) {
        object = this.toByteArray((LogEvent)object);
        byteBufferDestination.writeBytes((byte[])object, 0, ((Object)object).length);
    }

    public static abstract class Builder<B extends Builder<B>> {
        @PluginConfiguration
        private Configuration configuration;
        @PluginBuilderAttribute
        private byte[] footer;
        @PluginBuilderAttribute
        private byte[] header;

        public B asBuilder() {
            return (B)this;
        }

        public Configuration getConfiguration() {
            return this.configuration;
        }

        public byte[] getFooter() {
            return this.footer;
        }

        public byte[] getHeader() {
            return this.header;
        }

        public B setConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this.asBuilder();
        }

        public B setFooter(byte[] byArray) {
            this.footer = byArray;
            return this.asBuilder();
        }

        public B setHeader(byte[] byArray) {
            this.header = byArray;
            return this.asBuilder();
        }
    }
}

