/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.layout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.layout.internal.ExcludeChecker;
import org.apache.logging.log4j.core.layout.internal.IncludeChecker;
import org.apache.logging.log4j.core.layout.internal.ListChecker;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.net.Severity;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.core.util.NetUtils;
import org.apache.logging.log4j.core.util.Patterns;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.StringBuilderFormattable;
import org.apache.logging.log4j.util.TriConsumer;

@Plugin(name="GelfLayout", category="Core", elementType="layout", printObject=true)
public final class GelfLayout
extends AbstractStringLayout {
    private static final char C = ',';
    private static final int COMPRESSION_THRESHOLD = 1024;
    private static final char Q = '\"';
    private static final String QC = "\",";
    private static final String QU = "\"_";
    private final KeyValuePair[] additionalFields;
    private final int compressionThreshold;
    private final CompressionType compressionType;
    private final String host;
    private final boolean includeStacktrace;
    private final boolean includeThreadContext;
    private final boolean includeNullDelimiter;
    private final PatternLayout layout;
    private final FieldWriter fieldWriter;
    private static final ThreadLocal<StringBuilder> messageStringBuilder = new ThreadLocal();
    private static final ThreadLocal<StringBuilder> timestampStringBuilder = new ThreadLocal();

    @Deprecated
    public GelfLayout(String string, KeyValuePair[] keyValuePairArray, CompressionType compressionType, int n, boolean bl) {
        this(null, string, keyValuePairArray, compressionType, n, bl, true, false, null, null);
    }

    private GelfLayout(Configuration keyValuePairArray, String string, KeyValuePair[] keyValuePairArray2, CompressionType compressionType, int n, boolean bl, boolean bl2, boolean bl3, ListChecker listChecker, PatternLayout patternLayout) {
        super((Configuration)keyValuePairArray, StandardCharsets.UTF_8, null, null);
        this.host = string != null ? string : NetUtils.getLocalHostname();
        KeyValuePair[] keyValuePairArray3 = this.additionalFields = keyValuePairArray2 != null ? keyValuePairArray2 : new KeyValuePair[]{};
        if (keyValuePairArray == null) {
            keyValuePairArray = this.additionalFields;
            int n2 = this.additionalFields.length;
            for (int j = 0; j < n2; ++j) {
                if (!GelfLayout.valueNeedsLookup(keyValuePairArray[j].getValue())) continue;
                throw new IllegalArgumentException("configuration needs to be set when there are additional fields with variables");
            }
        }
        this.compressionType = compressionType;
        this.compressionThreshold = n;
        this.includeStacktrace = bl;
        this.includeThreadContext = bl2;
        this.includeNullDelimiter = bl3;
        if (bl3 && compressionType != CompressionType.OFF) {
            throw new IllegalArgumentException("null delimiter cannot be used with compression");
        }
        this.fieldWriter = new FieldWriter(listChecker);
        this.layout = patternLayout;
    }

    public final String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("host=").append(this.host);
        stringBuilder.append(", compressionType=").append(this.compressionType.toString());
        stringBuilder.append(", compressionThreshold=").append(this.compressionThreshold);
        stringBuilder.append(", includeStackTrace=").append(this.includeStacktrace);
        stringBuilder.append(", includeThreadContext=").append(this.includeThreadContext);
        stringBuilder.append(", includeNullDelimiter=").append(this.includeNullDelimiter);
        String string = this.fieldWriter.getChecker().toString();
        if (string.length() > 0) {
            stringBuilder.append(", ").append(string);
        }
        if (this.layout != null) {
            stringBuilder.append(", PatternLayout{").append(this.layout.toString()).append("}");
        }
        return stringBuilder.toString();
    }

    @Deprecated
    public static GelfLayout createLayout(@PluginAttribute(value="host") String string, @PluginElement(value="AdditionalField") KeyValuePair[] keyValuePairArray, @PluginAttribute(value="compressionType", defaultString="GZIP") CompressionType compressionType, @PluginAttribute(value="compressionThreshold", defaultInt=1024) int n, @PluginAttribute(value="includeStacktrace", defaultBoolean=true) boolean bl) {
        return new GelfLayout(null, string, keyValuePairArray, compressionType, n, bl, true, false, null, null);
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    @Override
    public final Map<String, String> getContentFormat() {
        return Collections.emptyMap();
    }

    @Override
    public final String getContentType() {
        return "application/json; charset=" + this.getCharset();
    }

    @Override
    public final byte[] toByteArray(LogEvent object) {
        object = this.toText((LogEvent)object, GelfLayout.getStringBuilder(), false);
        object = this.getBytes(((StringBuilder)object).toString());
        if (this.compressionType != CompressionType.OFF && ((Object)object).length > this.compressionThreshold) {
            return this.compress((byte[])object);
        }
        return object;
    }

    @Override
    public final void encode(LogEvent serializable, ByteBufferDestination byteBufferDestination) {
        if (this.compressionType != CompressionType.OFF) {
            super.encode((LogEvent)serializable, byteBufferDestination);
            return;
        }
        serializable = this.toText((LogEvent)serializable, GelfLayout.getStringBuilder(), true);
        this.getStringBuilderEncoder().encode((StringBuilder)serializable, byteBufferDestination);
    }

    @Override
    public final boolean requiresLocation() {
        return Objects.nonNull(this.layout) && this.layout.requiresLocation();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private byte[] compress(byte[] byArray) {
        Throwable throwable;
        DeflaterOutputStream deflaterOutputStream;
        Object object;
        block11: {
            try {
                object = new ByteArrayOutputStream(this.compressionThreshold / 8);
                deflaterOutputStream = this.compressionType.createDeflaterOutputStream((OutputStream)object);
                throwable = null;
                if (deflaterOutputStream != null) break block11;
                object = byArray;
                if (deflaterOutputStream == null) return object;
                deflaterOutputStream.close();
                return object;
            }
            catch (IOException iOException) {
                StatusLogger.getLogger().error(iOException);
                return byArray;
            }
        }
        try {
            deflaterOutputStream.write(byArray);
            deflaterOutputStream.finish();
            if (deflaterOutputStream == null) return ((ByteArrayOutputStream)object).toByteArray();
        }
        catch (Throwable throwable2) {
            try {
                object = throwable2;
                throwable = throwable2;
                throw object;
            }
            catch (Throwable throwable3) {
                if (deflaterOutputStream == null) throw throwable3;
                if (throwable != null) {
                    try {
                        deflaterOutputStream.close();
                        throw throwable3;
                    }
                    catch (Throwable throwable4) {
                        throwable.addSuppressed(throwable4);
                    }
                    throw throwable3;
                } else {
                    deflaterOutputStream.close();
                }
                throw throwable3;
            }
        }
        deflaterOutputStream.close();
        return ((ByteArrayOutputStream)object).toByteArray();
    }

    @Override
    public final String toSerializable(LogEvent logEvent) {
        return this.toText(logEvent, GelfLayout.getStringBuilder(), false).toString();
    }

    private StringBuilder toText(LogEvent logEvent, StringBuilder stringBuilder, boolean bl) {
        Object object;
        Object object2;
        stringBuilder.append('{');
        stringBuilder.append("\"version\":\"1.1\",");
        stringBuilder.append("\"host\":\"");
        JsonUtils.quoteAsString(GelfLayout.toNullSafeString(this.host), stringBuilder);
        stringBuilder.append(QC);
        stringBuilder.append("\"timestamp\":").append(GelfLayout.formatTimestamp(logEvent.getTimeMillis())).append(',');
        stringBuilder.append("\"level\":").append(this.formatLevel(logEvent.getLevel())).append(',');
        if (logEvent.getThreadName() != null) {
            stringBuilder.append("\"_thread\":\"");
            JsonUtils.quoteAsString(logEvent.getThreadName(), stringBuilder);
            stringBuilder.append(QC);
        }
        if (logEvent.getLoggerName() != null) {
            stringBuilder.append("\"_logger\":\"");
            JsonUtils.quoteAsString(logEvent.getLoggerName(), stringBuilder);
            stringBuilder.append(QC);
        }
        if (this.additionalFields.length > 0) {
            object2 = this.getConfiguration().getStrSubstitutor();
            object = this.additionalFields;
            int n = this.additionalFields.length;
            for (int j = 0; j < n; ++j) {
                KeyValuePair keyValuePair = object[j];
                stringBuilder.append(QU);
                JsonUtils.quoteAsString(keyValuePair.getKey(), stringBuilder);
                stringBuilder.append("\":\"");
                JsonUtils.quoteAsString(GelfLayout.toNullSafeString(GelfLayout.valueNeedsLookup(keyValuePair.getValue()) ? ((StrSubstitutor)object2).replace(logEvent, keyValuePair.getValue()) : keyValuePair.getValue()), stringBuilder);
                stringBuilder.append(QC);
            }
        }
        if (this.includeThreadContext) {
            logEvent.getContextData().forEach(this.fieldWriter, stringBuilder);
        }
        if (logEvent.getThrown() != null || this.layout != null) {
            stringBuilder.append("\"full_message\":\"");
            if (this.layout != null) {
                object2 = GelfLayout.getMessageStringBuilder();
                this.layout.serialize(logEvent, (StringBuilder)object2);
                JsonUtils.quoteAsString((CharSequence)object2, stringBuilder);
            } else if (this.includeStacktrace) {
                JsonUtils.quoteAsString(GelfLayout.formatThrowable(logEvent.getThrown()), stringBuilder);
            } else {
                JsonUtils.quoteAsString(logEvent.getThrown().toString(), stringBuilder);
            }
            stringBuilder.append(QC);
        }
        stringBuilder.append("\"short_message\":\"");
        object2 = logEvent.getMessage();
        if (object2 instanceof CharSequence) {
            JsonUtils.quoteAsString((CharSequence)object2, stringBuilder);
        } else if (bl && object2 instanceof StringBuilderFormattable) {
            object = GelfLayout.getMessageStringBuilder();
            try {
                ((StringBuilderFormattable)object2).formatTo((StringBuilder)object);
                JsonUtils.quoteAsString((CharSequence)object, stringBuilder);
            }
            finally {
                GelfLayout.trimToMaxSize((StringBuilder)object);
            }
        } else {
            JsonUtils.quoteAsString(GelfLayout.toNullSafeString(object2.getFormattedMessage()), stringBuilder);
        }
        stringBuilder.append('\"');
        stringBuilder.append('}');
        if (this.includeNullDelimiter) {
            stringBuilder.append('\u0000');
        }
        return stringBuilder;
    }

    private static boolean valueNeedsLookup(String string) {
        return string != null && string.contains("${");
    }

    private static StringBuilder getMessageStringBuilder() {
        StringBuilder stringBuilder = messageStringBuilder.get();
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder(1024);
            messageStringBuilder.set(stringBuilder);
        }
        stringBuilder.setLength(0);
        return stringBuilder;
    }

    private static CharSequence toNullSafeString(CharSequence charSequence) {
        if (charSequence == null) {
            return "";
        }
        return charSequence;
    }

    static CharSequence formatTimestamp(long l) {
        if (l < 1000L) {
            return "0";
        }
        StringBuilder stringBuilder = GelfLayout.getTimestampStringBuilder();
        stringBuilder.append(l);
        StringBuilder stringBuilder2 = stringBuilder;
        stringBuilder2.insert(stringBuilder2.length() - 3, '.');
        return stringBuilder;
    }

    private static StringBuilder getTimestampStringBuilder() {
        StringBuilder stringBuilder = timestampStringBuilder.get();
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder(20);
            timestampStringBuilder.set(stringBuilder);
        }
        stringBuilder.setLength(0);
        return stringBuilder;
    }

    private int formatLevel(Level level) {
        return Severity.getSeverity(level).getCode();
    }

    static CharSequence formatThrowable(Throwable throwable) {
        StringWriter stringWriter = new StringWriter(2048);
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.flush();
        return stringWriter.getBuffer();
    }

    static class FieldWriter
    implements TriConsumer<String, Object, StringBuilder> {
        private final ListChecker checker;

        FieldWriter(ListChecker listChecker) {
            this.checker = listChecker;
        }

        @Override
        public void accept(String string, Object object, StringBuilder stringBuilder) {
            if (this.checker.check(string)) {
                stringBuilder.append(GelfLayout.QU);
                JsonUtils.quoteAsString(string, stringBuilder);
                stringBuilder.append("\":\"");
                JsonUtils.quoteAsString(GelfLayout.toNullSafeString(String.valueOf(object)), stringBuilder);
                stringBuilder.append(GelfLayout.QC);
            }
        }

        public ListChecker getChecker() {
            return this.checker;
        }
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractStringLayout.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<GelfLayout> {
        @PluginBuilderAttribute
        private String host;
        @PluginElement(value="AdditionalField")
        private KeyValuePair[] additionalFields;
        @PluginBuilderAttribute
        private CompressionType compressionType = CompressionType.GZIP;
        @PluginBuilderAttribute
        private int compressionThreshold = 1024;
        @PluginBuilderAttribute
        private boolean includeStacktrace = true;
        @PluginBuilderAttribute
        private boolean includeThreadContext = true;
        @PluginBuilderAttribute
        private boolean includeNullDelimiter = false;
        @PluginBuilderAttribute
        private String threadContextIncludes = null;
        @PluginBuilderAttribute
        private String threadContextExcludes = null;
        @PluginBuilderAttribute
        private String messagePattern = null;

        public Builder() {
            this.setCharset(StandardCharsets.UTF_8);
        }

        /*
         * WARNING - void declaration
         */
        @Override
        public GelfLayout build() {
            void var2_7;
            String[] stringArray;
            String[] object;
            Object object2 = null;
            if (this.threadContextExcludes != null && (object = this.threadContextExcludes.split(Patterns.COMMA_SEPARATOR)).length > 0) {
                object2 = new ArrayList(object.length);
                for (String string : object) {
                    object2.add((String)string.trim());
                }
                object2 = new ExcludeChecker((List<String>)object2);
            }
            if (this.threadContextIncludes != null && (stringArray = this.threadContextIncludes.split(Patterns.COMMA_SEPARATOR)).length > 0) {
                object2 = new ArrayList<String>(stringArray.length);
                for (String string : stringArray) {
                    object2.add(string.trim());
                }
                object2 = new IncludeChecker((List<String>)object2);
            }
            if (object2 == null) {
                object2 = ListChecker.NOOP_CHECKER;
            }
            Object var2_5 = null;
            if (this.messagePattern != null) {
                PatternLayout patternLayout = PatternLayout.newBuilder().withPattern(this.messagePattern).withAlwaysWriteExceptions(this.includeStacktrace).withConfiguration(this.getConfiguration()).build();
            }
            return new GelfLayout(this.getConfiguration(), this.host, this.additionalFields, this.compressionType, this.compressionThreshold, this.includeStacktrace, this.includeThreadContext, this.includeNullDelimiter, (ListChecker)object2, (PatternLayout)var2_7);
        }

        public String getHost() {
            return this.host;
        }

        public CompressionType getCompressionType() {
            return this.compressionType;
        }

        public int getCompressionThreshold() {
            return this.compressionThreshold;
        }

        public boolean isIncludeStacktrace() {
            return this.includeStacktrace;
        }

        public boolean isIncludeThreadContext() {
            return this.includeThreadContext;
        }

        public boolean isIncludeNullDelimiter() {
            return this.includeNullDelimiter;
        }

        public KeyValuePair[] getAdditionalFields() {
            return this.additionalFields;
        }

        public B setHost(String string) {
            this.host = string;
            return (B)((Builder)this.asBuilder());
        }

        public B setCompressionType(CompressionType compressionType) {
            this.compressionType = compressionType;
            return (B)((Builder)this.asBuilder());
        }

        public B setCompressionThreshold(int n) {
            this.compressionThreshold = n;
            return (B)((Builder)this.asBuilder());
        }

        public B setIncludeStacktrace(boolean bl) {
            this.includeStacktrace = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B setIncludeThreadContext(boolean bl) {
            this.includeThreadContext = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B setIncludeNullDelimiter(boolean bl) {
            this.includeNullDelimiter = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B setAdditionalFields(KeyValuePair[] keyValuePairArray) {
            this.additionalFields = keyValuePairArray;
            return (B)((Builder)this.asBuilder());
        }

        public B setMessagePattern(String string) {
            this.messagePattern = string;
            return (B)((Builder)this.asBuilder());
        }

        public B setMdcIncludes(String string) {
            this.threadContextIncludes = string;
            return (B)((Builder)this.asBuilder());
        }

        public B setMdcExcludes(String string) {
            this.threadContextExcludes = string;
            return (B)((Builder)this.asBuilder());
        }
    }

    public static enum CompressionType {
        GZIP{

            @Override
            public final DeflaterOutputStream createDeflaterOutputStream(OutputStream outputStream) {
                return new GZIPOutputStream(outputStream);
            }
        }
        ,
        ZLIB{

            @Override
            public final DeflaterOutputStream createDeflaterOutputStream(OutputStream outputStream) {
                return new DeflaterOutputStream(outputStream);
            }
        }
        ,
        OFF{

            @Override
            public final DeflaterOutputStream createDeflaterOutputStream(OutputStream outputStream) {
                return null;
            }
        };


        public abstract DeflaterOutputStream createDeflaterOutputStream(OutputStream var1);
    }
}

