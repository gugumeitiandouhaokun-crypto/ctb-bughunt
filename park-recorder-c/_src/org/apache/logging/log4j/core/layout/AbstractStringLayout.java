/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.layout;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.impl.DefaultLogEventFactory;
import org.apache.logging.log4j.core.impl.LocationAware;
import org.apache.logging.log4j.core.layout.AbstractLayout;
import org.apache.logging.log4j.core.layout.Encoder;
import org.apache.logging.log4j.core.layout.StringBuilderEncoder;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.StringEncoder;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.StringBuilders;

public abstract class AbstractStringLayout
extends AbstractLayout<String>
implements StringLayout,
LocationAware {
    protected static final int DEFAULT_STRING_BUILDER_SIZE = 1024;
    protected static final int MAX_STRING_BUILDER_SIZE = Math.max(1024, AbstractStringLayout.size("log4j.layoutStringBuilder.maxSize", 2048));
    private static final ThreadLocal<StringBuilder> threadLocal = new ThreadLocal();
    private Encoder<StringBuilder> textEncoder;
    private transient Charset charset;
    private final String charsetName;
    private final Serializer footerSerializer;
    private final Serializer headerSerializer;
    private final boolean useCustomEncoding;

    @Override
    public boolean requiresLocation() {
        return false;
    }

    protected static StringBuilder getStringBuilder() {
        if (AbstractLogger.getRecursionDepth() > 1) {
            return new StringBuilder(1024);
        }
        StringBuilder stringBuilder = threadLocal.get();
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder(1024);
            threadLocal.set(stringBuilder);
        }
        AbstractStringLayout.trimToMaxSize(stringBuilder);
        stringBuilder.setLength(0);
        return stringBuilder;
    }

    private static boolean isPreJava8() {
        return org.apache.logging.log4j.util.Constants.JAVA_MAJOR_VERSION < 8;
    }

    private static int size(String string, int n) {
        return PropertiesUtil.getProperties().getIntegerProperty(string, n);
    }

    protected static void trimToMaxSize(StringBuilder stringBuilder) {
        StringBuilders.trimToMaxSize(stringBuilder, MAX_STRING_BUILDER_SIZE);
    }

    protected AbstractStringLayout(Charset charset) {
        this(charset, null, null);
    }

    protected AbstractStringLayout(Charset charset, byte[] byArray, byte[] byArray2) {
        super(null, byArray, byArray2);
        this.headerSerializer = null;
        this.footerSerializer = null;
        this.charset = charset == null ? StandardCharsets.UTF_8 : charset;
        this.charsetName = this.charset.name();
        this.useCustomEncoding = AbstractStringLayout.isPreJava8() && (StandardCharsets.ISO_8859_1.equals(charset) || StandardCharsets.US_ASCII.equals(charset));
        this.textEncoder = Constants.ENABLE_DIRECT_ENCODERS ? new StringBuilderEncoder(this.charset) : null;
    }

    protected AbstractStringLayout(Configuration configuration, Charset charset, Serializer serializer, Serializer serializer2) {
        super(configuration, null, null);
        this.headerSerializer = serializer;
        this.footerSerializer = serializer2;
        this.charset = charset == null ? StandardCharsets.UTF_8 : charset;
        this.charsetName = this.charset.name();
        this.useCustomEncoding = AbstractStringLayout.isPreJava8() && (StandardCharsets.ISO_8859_1.equals(charset) || StandardCharsets.US_ASCII.equals(charset));
        this.textEncoder = Constants.ENABLE_DIRECT_ENCODERS ? new StringBuilderEncoder(this.charset) : null;
    }

    protected byte[] getBytes(String string) {
        if (this.useCustomEncoding) {
            return StringEncoder.encodeSingleByteChars(string);
        }
        try {
            return string.getBytes(this.charsetName);
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            return string.getBytes(this.charset);
        }
    }

    @Override
    public Charset getCharset() {
        return this.charset;
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Override
    public byte[] getFooter() {
        AbstractStringLayout abstractStringLayout = this;
        return abstractStringLayout.serializeToBytes(abstractStringLayout.footerSerializer, super.getFooter());
    }

    public Serializer getFooterSerializer() {
        return this.footerSerializer;
    }

    @Override
    public byte[] getHeader() {
        AbstractStringLayout abstractStringLayout = this;
        return abstractStringLayout.serializeToBytes(abstractStringLayout.headerSerializer, super.getHeader());
    }

    public Serializer getHeaderSerializer() {
        return this.headerSerializer;
    }

    private DefaultLogEventFactory getLogEventFactory() {
        return DefaultLogEventFactory.getInstance();
    }

    protected Encoder<StringBuilder> getStringBuilderEncoder() {
        if (this.textEncoder == null) {
            this.textEncoder = new StringBuilderEncoder(this.getCharset());
        }
        return this.textEncoder;
    }

    protected byte[] serializeToBytes(Serializer object, byte[] byArray) {
        if ((object = this.serializeToString((Serializer)object)) == null) {
            return byArray;
        }
        return StringEncoder.toBytes((String)object, this.getCharset());
    }

    protected String serializeToString(Serializer serializer) {
        if (serializer == null) {
            return null;
        }
        Object object = this.getConfiguration().getRootLogger();
        object = this.getLogEventFactory().createEvent(((LoggerConfig)object).getName(), null, "", ((LoggerConfig)object).getLevel(), null, null, null);
        return serializer.toSerializable((LogEvent)object);
    }

    @Override
    public byte[] toByteArray(LogEvent logEvent) {
        AbstractStringLayout abstractStringLayout = this;
        return abstractStringLayout.getBytes((String)abstractStringLayout.toSerializable(logEvent));
    }

    public static interface Serializer2 {
        public StringBuilder toSerializable(LogEvent var1, StringBuilder var2);
    }

    public static interface Serializer {
        public String toSerializable(LogEvent var1);

        default public StringBuilder toSerializable(LogEvent logEvent, StringBuilder stringBuilder) {
            stringBuilder.append(this.toSerializable(logEvent));
            return stringBuilder;
        }
    }

    public static abstract class Builder<B extends Builder<B>>
    extends AbstractLayout.Builder<B> {
        @PluginBuilderAttribute(value="charset")
        private Charset charset;
        @PluginElement(value="footerSerializer")
        private Serializer footerSerializer;
        @PluginElement(value="headerSerializer")
        private Serializer headerSerializer;

        public Charset getCharset() {
            return this.charset;
        }

        public Serializer getFooterSerializer() {
            return this.footerSerializer;
        }

        public Serializer getHeaderSerializer() {
            return this.headerSerializer;
        }

        public B setCharset(Charset charset) {
            this.charset = charset;
            return (B)((Builder)this.asBuilder());
        }

        public B setFooterSerializer(Serializer serializer) {
            this.footerSerializer = serializer;
            return (B)((Builder)this.asBuilder());
        }

        public B setHeaderSerializer(Serializer serializer) {
            this.headerSerializer = serializer;
            return (B)((Builder)this.asBuilder());
        }
    }
}

