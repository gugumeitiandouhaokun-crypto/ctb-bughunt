/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.layout;

import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.layout.AbstractJacksonLayout;
import org.apache.logging.log4j.core.layout.JacksonFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.KeyValuePair;

@Plugin(name="JsonLayout", category="Core", elementType="layout", printObject=true)
public final class JsonLayout
extends AbstractJacksonLayout {
    private static final String DEFAULT_FOOTER = "]";
    private static final String DEFAULT_HEADER = "[";
    static final String CONTENT_TYPE = "application/json";

    @Deprecated
    protected JsonLayout(Configuration configuration, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, String string, String string2, String string3, Charset charset, boolean bl7) {
        super(configuration, new JacksonFactory.JSON(bl3, bl7, false, false).newWriter(bl, bl2, bl5), charset, bl5, bl4, bl6, string, PatternLayout.newSerializerBuilder().setConfiguration(configuration).setPattern(string2).setDefaultPattern(DEFAULT_HEADER).build(), PatternLayout.newSerializerBuilder().setConfiguration(configuration).setPattern(string3).setDefaultPattern(DEFAULT_FOOTER).build(), false, null);
    }

    private JsonLayout(Configuration configuration, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, String string, String string2, String string3, Charset charset, boolean bl7, boolean bl8, boolean bl9, boolean bl10, KeyValuePair[] keyValuePairArray, boolean bl11) {
        super(configuration, new JacksonFactory.JSON(bl3, bl7, bl8, bl11).newWriter(bl, bl2, bl5, bl10), charset, bl5, bl4, bl6, string, PatternLayout.newSerializerBuilder().setConfiguration(configuration).setPattern(string2).setDefaultPattern(DEFAULT_HEADER).build(), PatternLayout.newSerializerBuilder().setConfiguration(configuration).setPattern(string3).setDefaultPattern(DEFAULT_FOOTER).build(), bl9, keyValuePairArray);
    }

    @Override
    public final byte[] getHeader() {
        if (!this.complete) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        JsonLayout jsonLayout = this;
        String string = jsonLayout.serializeToString(jsonLayout.getHeaderSerializer());
        if (string != null) {
            stringBuilder.append(string);
        }
        stringBuilder.append(this.eol);
        return this.getBytes(stringBuilder.toString());
    }

    @Override
    public final byte[] getFooter() {
        if (!this.complete) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.eol);
        JsonLayout jsonLayout = this;
        String string = jsonLayout.serializeToString(jsonLayout.getFooterSerializer());
        if (string != null) {
            stringBuilder.append(string);
        }
        stringBuilder.append(this.eol);
        return this.getBytes(stringBuilder.toString());
    }

    @Override
    public final Map<String, String> getContentFormat() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("version", "2.0");
        return hashMap;
    }

    @Override
    public final String getContentType() {
        return "application/json; charset=" + this.getCharset();
    }

    @Deprecated
    public static JsonLayout createLayout(Configuration configuration, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, String string, String string2, Charset charset, boolean bl7) {
        bl3 = bl2 && bl3;
        return new JsonLayout(configuration, bl, bl2, bl3, bl4, bl5, bl6, null, string, string2, charset, bl7, false, false, false, null, false);
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    public static JsonLayout createDefaultLayout() {
        return new JsonLayout(new DefaultConfiguration(), false, false, false, false, false, false, null, DEFAULT_HEADER, DEFAULT_FOOTER, StandardCharsets.UTF_8, true, false, false, false, null, false);
    }

    @Override
    public final void toSerializable(LogEvent logEvent, Writer writer) {
        if (this.complete && this.eventCount > 0L) {
            writer.append(", ");
        }
        super.toSerializable(logEvent, writer);
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractJacksonLayout.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<JsonLayout> {
        @PluginBuilderAttribute
        private boolean propertiesAsList;
        @PluginBuilderAttribute
        private boolean objectMessageAsJsonObject;
        @PluginElement(value="AdditionalField")
        private KeyValuePair[] additionalFields;

        public Builder() {
            this.setCharset(StandardCharsets.UTF_8);
        }

        @Override
        public JsonLayout build() {
            boolean bl = this.isProperties() && this.propertiesAsList;
            Builder builder = this;
            String string = builder.toStringOrNull(builder.getHeader());
            Builder builder2 = this;
            String string2 = builder2.toStringOrNull(builder2.getFooter());
            return new JsonLayout(this.getConfiguration(), this.isLocationInfo(), this.isProperties(), bl, this.isComplete(), this.isCompact(), this.getEventEol(), this.getEndOfLine(), string, string2, this.getCharset(), this.isIncludeStacktrace(), this.isStacktraceAsString(), this.isIncludeNullDelimiter(), this.isIncludeTimeMillis(), this.getAdditionalFields(), this.getObjectMessageAsJsonObject());
        }

        public boolean isPropertiesAsList() {
            return this.propertiesAsList;
        }

        public B setPropertiesAsList(boolean bl) {
            this.propertiesAsList = bl;
            return (B)((Builder)this.asBuilder());
        }

        public boolean getObjectMessageAsJsonObject() {
            return this.objectMessageAsJsonObject;
        }

        public B setObjectMessageAsJsonObject(boolean bl) {
            this.objectMessageAsJsonObject = bl;
            return (B)((Builder)this.asBuilder());
        }

        @Override
        public KeyValuePair[] getAdditionalFields() {
            return this.additionalFields;
        }

        @Override
        public B setAdditionalFields(KeyValuePair[] keyValuePairArray) {
            this.additionalFields = keyValuePairArray;
            return (B)((Builder)this.asBuilder());
        }
    }
}

