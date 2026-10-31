/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.layout;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.layout.AbstractJacksonLayout;
import org.apache.logging.log4j.core.layout.JacksonFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.KeyValuePair;

@Plugin(name="YamlLayout", category="Core", elementType="layout", printObject=true)
public final class YamlLayout
extends AbstractJacksonLayout {
    private static final String DEFAULT_FOOTER = "";
    private static final String DEFAULT_HEADER = "";
    static final String CONTENT_TYPE = "application/yaml";

    @Deprecated
    protected YamlLayout(Configuration configuration, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, String string, String string2, Charset charset, boolean bl6) {
        super(configuration, new JacksonFactory.YAML(bl6, false).newWriter(bl, bl2, bl4), charset, bl4, bl3, bl5, null, PatternLayout.newSerializerBuilder().setConfiguration(configuration).setPattern(string).setDefaultPattern("").build(), PatternLayout.newSerializerBuilder().setConfiguration(configuration).setPattern(string2).setDefaultPattern("").build(), false, null);
    }

    private YamlLayout(Configuration configuration, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, String string, String string2, String string3, Charset charset, boolean bl6, boolean bl7, boolean bl8, boolean bl9, KeyValuePair[] keyValuePairArray) {
        super(configuration, new JacksonFactory.YAML(bl6, bl7).newWriter(bl, bl2, bl4, bl9), charset, bl4, bl3, bl5, string, PatternLayout.newSerializerBuilder().setConfiguration(configuration).setPattern(string2).setDefaultPattern("").build(), PatternLayout.newSerializerBuilder().setConfiguration(configuration).setPattern(string3).setDefaultPattern("").build(), bl8, keyValuePairArray);
    }

    @Override
    public final byte[] getHeader() {
        if (!this.complete) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        YamlLayout yamlLayout = this;
        String string = yamlLayout.serializeToString(yamlLayout.getHeaderSerializer());
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
        YamlLayout yamlLayout = this;
        String string = yamlLayout.serializeToString(yamlLayout.getFooterSerializer());
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
        return "application/yaml; charset=" + this.getCharset();
    }

    @Deprecated
    public static AbstractJacksonLayout createLayout(Configuration configuration, boolean bl, boolean bl2, String string, String string2, Charset charset, boolean bl3) {
        return new YamlLayout(configuration, bl, bl2, false, false, true, null, string, string2, charset, bl3, false, false, false, null);
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    public static AbstractJacksonLayout createDefaultLayout() {
        return new YamlLayout(new DefaultConfiguration(), false, false, false, false, false, null, "", "", StandardCharsets.UTF_8, true, false, false, false, null);
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractJacksonLayout.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<YamlLayout> {
        public Builder() {
            this.setCharset(StandardCharsets.UTF_8);
        }

        @Override
        public YamlLayout build() {
            Builder builder = this;
            String string = builder.toStringOrNull(builder.getHeader());
            Builder builder2 = this;
            String string2 = builder2.toStringOrNull(builder2.getFooter());
            return new YamlLayout(this.getConfiguration(), this.isLocationInfo(), this.isProperties(), this.isComplete(), this.isCompact(), this.getEventEol(), this.getEndOfLine(), string, string2, this.getCharset(), this.isIncludeStacktrace(), this.isStacktraceAsString(), this.isIncludeNullDelimiter(), this.isIncludeTimeMillis(), this.getAdditionalFields());
        }
    }
}

