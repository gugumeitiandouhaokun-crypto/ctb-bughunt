/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.layout;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.layout.AbstractJacksonLayout;
import org.apache.logging.log4j.core.layout.JacksonFactory;
import org.apache.logging.log4j.core.util.KeyValuePair;

@Plugin(name="XmlLayout", category="Core", elementType="layout", printObject=true)
public final class XmlLayout
extends AbstractJacksonLayout {
    private static final String ROOT_TAG = "Events";

    @Deprecated
    protected XmlLayout(boolean bl, boolean bl2, boolean bl3, boolean bl4, Charset charset, boolean bl5) {
        this(null, bl, bl2, bl3, bl4, null, charset, bl5, false, false, false, null);
    }

    private XmlLayout(Configuration configuration, boolean bl, boolean bl2, boolean bl3, boolean bl4, String string, Charset charset, boolean bl5, boolean bl6, boolean bl7, boolean bl8, KeyValuePair[] keyValuePairArray) {
        super(configuration, new JacksonFactory.XML(bl5, bl6).newWriter(bl, bl2, bl4, bl8), charset, bl4, bl3, false, string, null, null, bl7, keyValuePairArray);
    }

    @Override
    public final byte[] getHeader() {
        if (!this.complete) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"");
        stringBuilder.append(this.getCharset().name());
        stringBuilder.append("\"?>");
        stringBuilder.append(this.eol);
        stringBuilder.append('<');
        stringBuilder.append(ROOT_TAG);
        stringBuilder.append(" xmlns=\"http://logging.apache.org/log4j/2.0/events\">");
        stringBuilder.append(this.eol);
        return stringBuilder.toString().getBytes(this.getCharset());
    }

    @Override
    public final byte[] getFooter() {
        if (!this.complete) {
            return null;
        }
        return this.getBytes("</Events>" + this.eol);
    }

    @Override
    public final Map<String, String> getContentFormat() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("xsd", "log4j-events.xsd");
        hashMap.put("version", "2.0");
        return hashMap;
    }

    @Override
    public final String getContentType() {
        return "text/xml; charset=" + this.getCharset();
    }

    @Deprecated
    public static XmlLayout createLayout(boolean bl, boolean bl2, boolean bl3, boolean bl4, Charset charset, boolean bl5) {
        return new XmlLayout(null, bl, bl2, bl3, bl4, null, charset, bl5, false, false, false, null);
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    public static XmlLayout createDefaultLayout() {
        return new XmlLayout(null, false, false, false, false, null, StandardCharsets.UTF_8, true, false, false, false, null);
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractJacksonLayout.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<XmlLayout> {
        public Builder() {
            this.setCharset(StandardCharsets.UTF_8);
        }

        @Override
        public XmlLayout build() {
            return new XmlLayout(this.getConfiguration(), this.isLocationInfo(), this.isProperties(), this.isComplete(), this.isCompact(), this.getEndOfLine(), this.getCharset(), this.isIncludeStacktrace(), this.isStacktraceAsString(), this.isIncludeNullDelimiter(), this.isIncludeTimeMillis(), this.getAdditionalFields());
        }
    }
}

