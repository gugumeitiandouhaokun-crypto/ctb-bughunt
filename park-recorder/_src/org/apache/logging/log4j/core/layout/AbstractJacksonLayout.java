/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonAnyGetter
 *  com.fasterxml.jackson.annotation.JsonIgnore
 *  com.fasterxml.jackson.annotation.JsonRootName
 *  com.fasterxml.jackson.annotation.JsonUnwrapped
 *  com.fasterxml.jackson.databind.ObjectWriter
 *  com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
 */
package org.apache.logging.log4j.core.layout;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.core.util.StringBuilderWriter;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.ReadOnlyStringMap;

abstract class AbstractJacksonLayout
extends AbstractStringLayout {
    protected static final String DEFAULT_EOL = "\r\n";
    protected static final String COMPACT_EOL = "";
    protected final String eol;
    protected final ObjectWriter objectWriter;
    protected final boolean compact;
    protected final boolean complete;
    protected final boolean includeNullDelimiter;
    protected final ResolvableKeyValuePair[] additionalFields;

    @Deprecated
    protected AbstractJacksonLayout(Configuration configuration, ObjectWriter objectWriter, Charset charset, boolean bl, boolean bl2, boolean bl3, AbstractStringLayout.Serializer serializer, AbstractStringLayout.Serializer serializer2) {
        this(configuration, objectWriter, charset, bl, bl2, bl3, serializer, serializer2, false);
    }

    @Deprecated
    protected AbstractJacksonLayout(Configuration configuration, ObjectWriter objectWriter, Charset charset, boolean bl, boolean bl2, boolean bl3, AbstractStringLayout.Serializer serializer, AbstractStringLayout.Serializer serializer2, boolean bl4) {
        this(configuration, objectWriter, charset, bl, bl2, bl3, null, serializer, serializer2, bl4, null);
    }

    protected AbstractJacksonLayout(Configuration configuration, ObjectWriter objectWriter, Charset charset, boolean bl, boolean bl2, boolean bl3, String string, AbstractStringLayout.Serializer serializer, AbstractStringLayout.Serializer serializer2, boolean bl4, KeyValuePair[] keyValuePairArray) {
        super(configuration, charset, serializer, serializer2);
        this.objectWriter = objectWriter;
        this.compact = bl;
        this.complete = bl2;
        this.eol = string != null ? string : (bl && !bl3 ? COMPACT_EOL : DEFAULT_EOL);
        this.includeNullDelimiter = bl4;
        this.additionalFields = AbstractJacksonLayout.prepareAdditionalFields(configuration, keyValuePairArray);
    }

    protected static boolean valueNeedsLookup(String string) {
        return string != null && string.contains("${");
    }

    private static ResolvableKeyValuePair[] prepareAdditionalFields(Configuration configuration, KeyValuePair[] keyValuePairArray) {
        if (keyValuePairArray == null || keyValuePairArray.length == 0) {
            return new ResolvableKeyValuePair[0];
        }
        ResolvableKeyValuePair[] resolvableKeyValuePairArray = new ResolvableKeyValuePair[keyValuePairArray.length];
        for (int j = 0; j < keyValuePairArray.length; ++j) {
            ResolvableKeyValuePair resolvableKeyValuePair = resolvableKeyValuePairArray[j] = new ResolvableKeyValuePair(keyValuePairArray[j]);
            if (configuration != null || !resolvableKeyValuePair.valueNeedsLookup) continue;
            throw new IllegalArgumentException("configuration needs to be set when there are additional fields with variables");
        }
        return resolvableKeyValuePairArray;
    }

    @Override
    public String toSerializable(LogEvent logEvent) {
        StringBuilderWriter stringBuilderWriter = new StringBuilderWriter();
        try {
            this.toSerializable(logEvent, stringBuilderWriter);
            return stringBuilderWriter.toString();
        }
        catch (IOException iOException) {
            LOGGER.error(iOException);
            return COMPACT_EOL;
        }
    }

    private static LogEvent convertMutableToLog4jEvent(LogEvent logEvent) {
        if (logEvent instanceof Log4jLogEvent) {
            return logEvent;
        }
        return Log4jLogEvent.createMemento(logEvent);
    }

    protected Object wrapLogEvent(LogEvent logEvent) {
        if (this.additionalFields.length > 0) {
            Map<String, String> map = this.resolveAdditionalFields(logEvent);
            return new LogEventWithAdditionalFields(logEvent, map);
        }
        if (logEvent instanceof Message) {
            return new ReadOnlyLogEventWrapper(logEvent);
        }
        return logEvent;
    }

    private Map<String, String> resolveAdditionalFields(LogEvent logEvent) {
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<String, String>(this.additionalFields.length);
        StrSubstitutor strSubstitutor = this.configuration.getStrSubstitutor();
        ResolvableKeyValuePair[] resolvableKeyValuePairArray = this.additionalFields;
        int n = this.additionalFields.length;
        for (int j = 0; j < n; ++j) {
            ResolvableKeyValuePair resolvableKeyValuePair = resolvableKeyValuePairArray[j];
            if (resolvableKeyValuePair.valueNeedsLookup) {
                linkedHashMap.put(resolvableKeyValuePair.key, strSubstitutor.replace(logEvent, resolvableKeyValuePair.value));
                continue;
            }
            linkedHashMap.put(resolvableKeyValuePair.key, resolvableKeyValuePair.value);
        }
        return linkedHashMap;
    }

    public void toSerializable(LogEvent logEvent, Writer writer) {
        this.objectWriter.writeValue(writer, this.wrapLogEvent(AbstractJacksonLayout.convertMutableToLog4jEvent(logEvent)));
        writer.write(this.eol);
        if (this.includeNullDelimiter) {
            writer.write(0);
        }
        this.markEvent();
    }

    static class ReadOnlyLogEventWrapper
    implements LogEvent {
        @JsonIgnore
        private final LogEvent event;

        public ReadOnlyLogEventWrapper(LogEvent logEvent) {
            this.event = logEvent;
        }

        @Override
        public LogEvent toImmutable() {
            return this.event.toImmutable();
        }

        @Override
        public Map<String, String> getContextMap() {
            return this.event.getContextMap();
        }

        @Override
        public ReadOnlyStringMap getContextData() {
            return this.event.getContextData();
        }

        @Override
        public ThreadContext.ContextStack getContextStack() {
            return this.event.getContextStack();
        }

        @Override
        public String getLoggerFqcn() {
            return this.event.getLoggerFqcn();
        }

        @Override
        public Level getLevel() {
            return this.event.getLevel();
        }

        @Override
        public String getLoggerName() {
            return this.event.getLoggerName();
        }

        @Override
        public Marker getMarker() {
            return this.event.getMarker();
        }

        @Override
        public Message getMessage() {
            return this.event.getMessage();
        }

        @Override
        public long getTimeMillis() {
            return this.event.getTimeMillis();
        }

        @Override
        public Instant getInstant() {
            return this.event.getInstant();
        }

        @Override
        public StackTraceElement getSource() {
            return this.event.getSource();
        }

        @Override
        public String getThreadName() {
            return this.event.getThreadName();
        }

        @Override
        public long getThreadId() {
            return this.event.getThreadId();
        }

        @Override
        public int getThreadPriority() {
            return this.event.getThreadPriority();
        }

        @Override
        public Throwable getThrown() {
            return this.event.getThrown();
        }

        @Override
        public ThrowableProxy getThrownProxy() {
            return this.event.getThrownProxy();
        }

        @Override
        public boolean isEndOfBatch() {
            return this.event.isEndOfBatch();
        }

        @Override
        public boolean isIncludeLocation() {
            return this.event.isIncludeLocation();
        }

        @Override
        public void setEndOfBatch(boolean bl) {
        }

        @Override
        public void setIncludeLocation(boolean bl) {
        }

        @Override
        public long getNanoTime() {
            return this.event.getNanoTime();
        }
    }

    protected static class ResolvableKeyValuePair {
        final String key;
        final String value;
        final boolean valueNeedsLookup;

        ResolvableKeyValuePair(KeyValuePair keyValuePair) {
            this.key = keyValuePair.getKey();
            this.value = keyValuePair.getValue();
            this.valueNeedsLookup = AbstractJacksonLayout.valueNeedsLookup(this.value);
        }
    }

    @JsonRootName(value="Event")
    @JacksonXmlRootElement(namespace="http://logging.apache.org/log4j/2.0/events", localName="Event")
    public static class LogEventWithAdditionalFields {
        private final Object logEvent;
        private final Map<String, String> additionalFields;

        public LogEventWithAdditionalFields(Object object, Map<String, String> map) {
            this.logEvent = object;
            this.additionalFields = map;
        }

        @JsonUnwrapped
        public Object getLogEvent() {
            return this.logEvent;
        }

        @JsonAnyGetter
        public Map<String, String> getAdditionalFields() {
            return this.additionalFields;
        }
    }

    public static abstract class Builder<B extends Builder<B>>
    extends AbstractStringLayout.Builder<B> {
        @PluginBuilderAttribute
        private boolean eventEol;
        @PluginBuilderAttribute
        private String endOfLine;
        @PluginBuilderAttribute
        private boolean compact;
        @PluginBuilderAttribute
        private boolean complete;
        @PluginBuilderAttribute
        private boolean locationInfo;
        @PluginBuilderAttribute
        private boolean properties;
        @PluginBuilderAttribute
        private boolean includeStacktrace = true;
        @PluginBuilderAttribute
        private boolean stacktraceAsString = false;
        @PluginBuilderAttribute
        private boolean includeNullDelimiter = false;
        @PluginBuilderAttribute
        private boolean includeTimeMillis = false;
        @PluginElement(value="AdditionalField")
        private KeyValuePair[] additionalFields;

        protected String toStringOrNull(byte[] byArray) {
            if (byArray == null) {
                return null;
            }
            return new String(byArray, Charset.defaultCharset());
        }

        public boolean getEventEol() {
            return this.eventEol;
        }

        public String getEndOfLine() {
            return this.endOfLine;
        }

        public boolean isCompact() {
            return this.compact;
        }

        public boolean isComplete() {
            return this.complete;
        }

        public boolean isLocationInfo() {
            return this.locationInfo;
        }

        public boolean isProperties() {
            return this.properties;
        }

        public boolean isIncludeStacktrace() {
            return this.includeStacktrace;
        }

        public boolean isStacktraceAsString() {
            return this.stacktraceAsString;
        }

        public boolean isIncludeNullDelimiter() {
            return this.includeNullDelimiter;
        }

        public boolean isIncludeTimeMillis() {
            return this.includeTimeMillis;
        }

        public KeyValuePair[] getAdditionalFields() {
            return this.additionalFields;
        }

        public B setEventEol(boolean bl) {
            this.eventEol = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B setEndOfLine(String string) {
            this.endOfLine = string;
            return (B)((Builder)this.asBuilder());
        }

        public B setCompact(boolean bl) {
            this.compact = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B setComplete(boolean bl) {
            this.complete = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B setLocationInfo(boolean bl) {
            this.locationInfo = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B setProperties(boolean bl) {
            this.properties = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B setIncludeStacktrace(boolean bl) {
            this.includeStacktrace = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B setStacktraceAsString(boolean bl) {
            this.stacktraceAsString = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B setIncludeNullDelimiter(boolean bl) {
            this.includeNullDelimiter = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B setIncludeTimeMillis(boolean bl) {
            this.includeTimeMillis = bl;
            return (B)((Builder)this.asBuilder());
        }

        public B setAdditionalFields(KeyValuePair[] keyValuePairArray) {
            this.additionalFields = keyValuePairArray;
            return (B)((Builder)this.asBuilder());
        }
    }
}

