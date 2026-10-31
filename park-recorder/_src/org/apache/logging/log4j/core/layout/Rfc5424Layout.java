/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.layout;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LoggingException;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.TlsSyslogFrame;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.LoggerFields;
import org.apache.logging.log4j.core.layout.internal.ExcludeChecker;
import org.apache.logging.log4j.core.layout.internal.IncludeChecker;
import org.apache.logging.log4j.core.layout.internal.ListChecker;
import org.apache.logging.log4j.core.net.Facility;
import org.apache.logging.log4j.core.net.Priority;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.apache.logging.log4j.core.pattern.ThrowablePatternConverter;
import org.apache.logging.log4j.core.util.NetUtils;
import org.apache.logging.log4j.core.util.Patterns;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageCollectionMessage;
import org.apache.logging.log4j.message.StructuredDataCollectionMessage;
import org.apache.logging.log4j.message.StructuredDataId;
import org.apache.logging.log4j.message.StructuredDataMessage;
import org.apache.logging.log4j.util.ProcessIdUtil;
import org.apache.logging.log4j.util.StringBuilders;
import org.apache.logging.log4j.util.Strings;

@Plugin(name="Rfc5424Layout", category="Core", elementType="layout", printObject=true)
public final class Rfc5424Layout
extends AbstractStringLayout {
    public static final int DEFAULT_ENTERPRISE_NUMBER = 18060;
    public static final String DEFAULT_ID = "Audit";
    public static final Pattern NEWLINE_PATTERN = Pattern.compile("\\r?\\n");
    public static final Pattern PARAM_VALUE_ESCAPE_PATTERN = Pattern.compile("[\\\"\\]\\\\]");
    public static final String DEFAULT_MDCID = "mdc";
    private static final String LF = "\n";
    private static final int TWO_DIGITS = 10;
    private static final int THREE_DIGITS = 100;
    private static final int MILLIS_PER_MINUTE = 60000;
    private static final int MINUTES_PER_HOUR = 60;
    private static final String COMPONENT_KEY = "RFC5424-Converter";
    private final Facility facility;
    private final String defaultId;
    private final int enterpriseNumber;
    private final boolean includeMdc;
    private final String mdcId;
    private final StructuredDataId mdcSdId;
    private final String localHostName;
    private final String appName;
    private final String messageId;
    private final String configName;
    private final String mdcPrefix;
    private final String eventPrefix;
    private final List<String> mdcExcludes;
    private final List<String> mdcIncludes;
    private final List<String> mdcRequired;
    private final ListChecker listChecker;
    private final boolean includeNewLine;
    private final String escapeNewLine;
    private final boolean useTlsMessageFormat;
    private long lastTimestamp = -1L;
    private String timestamppStr;
    private final List<PatternFormatter> exceptionFormatters;
    private final Map<String, FieldFormatter> fieldFormatters;
    private final String procId;

    /*
     * WARNING - void declaration
     */
    private Rfc5424Layout(Configuration configuration, Facility object, String object2, int n, boolean n2, boolean bl, String string, String string2, String string3, String string4, String string5, String string6, String string7, String string8, String string9, Charset object3, String string10, boolean bl2, LoggerFields[] loggerFieldsArray) {
        super((Charset)((Object)patternParser));
        void var19_29;
        void var15_25;
        void var14_24;
        void var13_23;
        void var18_28;
        void var12_22;
        void var11_21;
        void var10_20;
        void var9_19;
        void var8_18;
        void var7_17;
        void var6_13;
        int n3;
        int n4;
        void var17_27;
        PatternParser patternParser;
        patternParser = Rfc5424Layout.createPatternParser(configuration, ThrowablePatternConverter.class);
        this.exceptionFormatters = var17_27 == null ? null : patternParser.parse((String)var17_27);
        this.facility = object;
        this.defaultId = object2 == null ? DEFAULT_ID : object2;
        this.enterpriseNumber = n4;
        this.includeMdc = n3;
        this.includeNewLine = var6_13;
        String string11 = this.escapeNewLine = var7_17 == null ? null : Matcher.quoteReplacement((String)var7_17);
        this.mdcId = var8_18 != null ? var8_18 : (object2 == null ? DEFAULT_MDCID : object2);
        this.mdcSdId = new StructuredDataId(this.mdcId, this.enterpriseNumber, null, null);
        this.mdcPrefix = var9_19;
        this.eventPrefix = var10_20;
        this.appName = var11_21;
        this.messageId = var12_22;
        this.useTlsMessageFormat = var18_28;
        this.localHostName = NetUtils.getLocalHostname();
        object = null;
        if (var13_23 != null) {
            String[] stringArray = var13_23.split(Patterns.COMMA_SEPARATOR);
            if (stringArray.length > 0) {
                this.mdcExcludes = new ArrayList<String>(stringArray.length);
                for (String string12 : stringArray) {
                    this.mdcExcludes.add(string12.trim());
                }
                object = new ExcludeChecker(this.mdcExcludes);
            } else {
                this.mdcExcludes = null;
            }
        } else {
            this.mdcExcludes = null;
        }
        if (var14_24 != null) {
            String[] stringArray = var14_24.split(Patterns.COMMA_SEPARATOR);
            if (stringArray.length > 0) {
                this.mdcIncludes = new ArrayList<String>(stringArray.length);
                for (String string13 : stringArray) {
                    this.mdcIncludes.add(string13.trim());
                }
                object = new IncludeChecker(this.mdcIncludes);
            } else {
                this.mdcIncludes = null;
            }
        } else {
            this.mdcIncludes = null;
        }
        if (var15_25 != null) {
            String[] stringArray = var15_25.split(Patterns.COMMA_SEPARATOR);
            if (stringArray.length > 0) {
                this.mdcRequired = new ArrayList<String>(stringArray.length);
                for (String string14 : stringArray) {
                    this.mdcRequired.add(string14.trim());
                }
            } else {
                this.mdcRequired = null;
            }
        } else {
            this.mdcRequired = null;
        }
        this.listChecker = object != null ? object : ListChecker.NOOP_CHECKER;
        String string15 = configuration == null ? null : configuration.getName();
        this.configName = Strings.isNotEmpty(string15) ? string15 : null;
        this.fieldFormatters = this.createFieldFormatters((LoggerFields[])var19_29, configuration);
        this.procId = ProcessIdUtil.getProcessId();
    }

    private Map<String, FieldFormatter> createFieldFormatters(LoggerFields[] loggerFieldsArray, Configuration configuration) {
        HashMap<String, FieldFormatter> hashMap = new HashMap<String, FieldFormatter>(loggerFieldsArray == null ? 0 : loggerFieldsArray.length);
        if (loggerFieldsArray != null) {
            for (LoggerFields loggerFields : loggerFieldsArray) {
                StructuredDataId structuredDataId = loggerFields.getSdId() == null ? this.mdcSdId : loggerFields.getSdId();
                HashMap<String, List<PatternFormatter>> hashMap2 = new HashMap<String, List<PatternFormatter>>();
                Map<String, String> map = loggerFields.getMap();
                if (map.isEmpty()) continue;
                PatternParser patternParser = Rfc5424Layout.createPatternParser(configuration, null);
                for (Map.Entry entry : map.entrySet()) {
                    List<PatternFormatter> list = patternParser.parse((String)entry.getValue());
                    hashMap2.put((String)entry.getKey(), list);
                }
                map = new FieldFormatter(hashMap2, loggerFields.getDiscardIfAllFieldsAreEmpty());
                hashMap.put(structuredDataId.toString(), (FieldFormatter)((Object)map));
            }
        }
        if (hashMap.size() > 0) {
            return hashMap;
        }
        return null;
    }

    private static PatternParser createPatternParser(Configuration configuration, Class<? extends PatternConverter> object) {
        if (configuration == null) {
            return new PatternParser(configuration, "Converter", LogEventPatternConverter.class, (Class<?>)object);
        }
        object = (PatternParser)configuration.getComponent(COMPONENT_KEY);
        if (object == null) {
            object = new PatternParser(configuration, "Converter", ThrowablePatternConverter.class);
            configuration.addComponent(COMPONENT_KEY, object);
            object = (PatternParser)configuration.getComponent(COMPONENT_KEY);
        }
        return object;
    }

    @Override
    public final Map<String, String> getContentFormat() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("structured", "true");
        hashMap.put("formatType", "RFC5424");
        return hashMap;
    }

    @Override
    public final String toSerializable(LogEvent logEvent) {
        StringBuilder stringBuilder = Rfc5424Layout.getStringBuilder();
        this.appendPriority(stringBuilder, logEvent.getLevel());
        this.appendTimestamp(stringBuilder, logEvent.getTimeMillis());
        this.appendSpace(stringBuilder);
        this.appendHostName(stringBuilder);
        this.appendSpace(stringBuilder);
        this.appendAppName(stringBuilder);
        this.appendSpace(stringBuilder);
        this.appendProcessId(stringBuilder);
        this.appendSpace(stringBuilder);
        this.appendMessageId(stringBuilder, logEvent.getMessage());
        this.appendSpace(stringBuilder);
        this.appendStructuredElements(stringBuilder, logEvent);
        this.appendMessage(stringBuilder, logEvent);
        if (this.useTlsMessageFormat) {
            return new TlsSyslogFrame(stringBuilder.toString()).toString();
        }
        return stringBuilder.toString();
    }

    private void appendPriority(StringBuilder stringBuilder, Level level) {
        stringBuilder.append('<');
        stringBuilder.append(Priority.getPriority(this.facility, level));
        stringBuilder.append(">1 ");
    }

    private void appendTimestamp(StringBuilder stringBuilder, long l) {
        stringBuilder.append(this.computeTimeStampString(l));
    }

    private void appendSpace(StringBuilder stringBuilder) {
        stringBuilder.append(' ');
    }

    private void appendHostName(StringBuilder stringBuilder) {
        stringBuilder.append(this.localHostName);
    }

    private void appendAppName(StringBuilder stringBuilder) {
        if (this.appName != null) {
            stringBuilder.append(this.appName);
            return;
        }
        if (this.configName != null) {
            stringBuilder.append(this.configName);
            return;
        }
        stringBuilder.append('-');
    }

    private void appendProcessId(StringBuilder stringBuilder) {
        stringBuilder.append(this.getProcId());
    }

    private void appendMessageId(StringBuilder stringBuilder, Message object) {
        if ((object = object instanceof StructuredDataMessage ? ((StructuredDataMessage)object).getType() : null) != null) {
            stringBuilder.append((String)object);
            return;
        }
        if (this.messageId != null) {
            stringBuilder.append(this.messageId);
            return;
        }
        stringBuilder.append('-');
    }

    private void appendMessage(StringBuilder stringBuilder, LogEvent logEvent) {
        Object object = logEvent.getMessage();
        object = object instanceof StructuredDataMessage || object instanceof MessageCollectionMessage ? object.getFormat() : object.getFormattedMessage();
        if (object != null && ((String)object).length() > 0) {
            stringBuilder.append(' ').append(this.escapeNewlines((String)object, this.escapeNewLine));
        }
        if (this.exceptionFormatters != null && logEvent.getThrown() != null) {
            object = new StringBuilder(LF);
            Iterator<PatternFormatter> iterator = this.exceptionFormatters.iterator();
            while (iterator.hasNext()) {
                iterator.next().format(logEvent, (StringBuilder)object);
            }
            stringBuilder.append(this.escapeNewlines(((StringBuilder)object).toString(), this.escapeNewLine));
        }
        if (this.includeNewLine) {
            stringBuilder.append(LF);
        }
    }

    private void appendStructuredElements(StringBuilder stringBuilder, LogEvent logEvent) {
        Object object;
        Message message = logEvent.getMessage();
        boolean bl = message instanceof StructuredDataMessage || message instanceof StructuredDataCollectionMessage;
        if (!bl && this.fieldFormatters != null && this.fieldFormatters.isEmpty() && !this.includeMdc) {
            stringBuilder.append('-');
            return;
        }
        HashMap<String, StructuredDataElement> hashMap = new HashMap<String, StructuredDataElement>();
        Map<String, String> map = logEvent.getContextData().toMap();
        if (this.mdcRequired != null) {
            this.checkRequired(map);
        }
        if (this.fieldFormatters != null) {
            for (Map.Entry<String, FieldFormatter> entry : this.fieldFormatters.entrySet()) {
                object = entry.getKey();
                StructuredDataElement object2 = entry.getValue().format(logEvent);
                hashMap.put((String)object, object2);
            }
        }
        if (this.includeMdc && map.size() > 0) {
            String string = this.mdcSdId.toString();
            StructuredDataElement structuredDataElement = (StructuredDataElement)hashMap.get(string);
            if (structuredDataElement != null) {
                structuredDataElement.union(map);
                hashMap.put(string, structuredDataElement);
            } else {
                object = new StructuredDataElement(map, this.mdcPrefix, false);
                hashMap.put(string, (StructuredDataElement)object);
            }
        }
        if (bl) {
            if (message instanceof MessageCollectionMessage) {
                for (StructuredDataMessage structuredDataMessage : (StructuredDataCollectionMessage)message) {
                    this.addStructuredData(hashMap, structuredDataMessage);
                }
            } else {
                this.addStructuredData(hashMap, (StructuredDataMessage)message);
            }
        }
        if (hashMap.isEmpty()) {
            stringBuilder.append('-');
            return;
        }
        for (Map.Entry entry : hashMap.entrySet()) {
            this.formatStructuredElement((String)entry.getKey(), (StructuredDataElement)entry.getValue(), stringBuilder, this.listChecker);
        }
    }

    private void addStructuredData(Map<String, StructuredDataElement> map, StructuredDataMessage object) {
        Map<String, String> map2 = ((MapMessage)object).getData();
        String string = this.getId((StructuredDataId)(object = ((StructuredDataMessage)object).getId()));
        if (map.containsKey(string)) {
            object = map.get(((StructuredDataId)object).toString());
            ((StructuredDataElement)object).union(map2);
            map.put(string, (StructuredDataElement)object);
            return;
        }
        object = new StructuredDataElement(map2, this.eventPrefix, false);
        map.put(string, (StructuredDataElement)object);
    }

    private String escapeNewlines(String string, String string2) {
        if (string2 == null) {
            return string;
        }
        return NEWLINE_PATTERN.matcher(string).replaceAll(string2);
    }

    protected final String getProcId() {
        return this.procId;
    }

    protected final List<String> getMdcExcludes() {
        return this.mdcExcludes;
    }

    protected final List<String> getMdcIncludes() {
        return this.mdcIncludes;
    }

    private String computeTimeStampString(long l) {
        long l2;
        Object object = this;
        synchronized (object) {
            l2 = this.lastTimestamp;
            if (l == this.lastTimestamp) {
                return this.timestamppStr;
            }
        }
        object = new StringBuilder();
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(l);
        ((StringBuilder)object).append(Integer.toString(gregorianCalendar.get(1)));
        ((StringBuilder)object).append('-');
        this.pad(gregorianCalendar.get(2) + 1, 10, (StringBuilder)object);
        ((StringBuilder)object).append('-');
        this.pad(gregorianCalendar.get(5), 10, (StringBuilder)object);
        ((StringBuilder)object).append('T');
        this.pad(gregorianCalendar.get(11), 10, (StringBuilder)object);
        ((StringBuilder)object).append(':');
        this.pad(gregorianCalendar.get(12), 10, (StringBuilder)object);
        ((StringBuilder)object).append(':');
        this.pad(gregorianCalendar.get(13), 10, (StringBuilder)object);
        ((StringBuilder)object).append('.');
        this.pad(gregorianCalendar.get(14), 100, (StringBuilder)object);
        int n = (gregorianCalendar.get(15) + gregorianCalendar.get(16)) / 60000;
        if (n == 0) {
            ((StringBuilder)object).append('Z');
        } else {
            if (n < 0) {
                n = -n;
                ((StringBuilder)object).append('-');
            } else {
                ((StringBuilder)object).append('+');
            }
            int n2 = n / 60;
            this.pad(n2, 10, (StringBuilder)object);
            ((StringBuilder)object).append(':');
            this.pad(n -= n2 * 60, 10, (StringBuilder)object);
        }
        Rfc5424Layout rfc5424Layout = this;
        synchronized (rfc5424Layout) {
            if (l2 == this.lastTimestamp) {
                this.lastTimestamp = l;
                this.timestamppStr = ((StringBuilder)object).toString();
            }
        }
        return ((StringBuilder)object).toString();
    }

    private void pad(int n, int n2, StringBuilder stringBuilder) {
        while (n2 > 1) {
            if (n < n2) {
                stringBuilder.append('0');
            }
            n2 /= 10;
        }
        stringBuilder.append(Integer.toString(n));
    }

    private void formatStructuredElement(String string, StructuredDataElement structuredDataElement, StringBuilder stringBuilder, ListChecker listChecker) {
        if (string == null && this.defaultId == null || structuredDataElement.discard()) {
            return;
        }
        stringBuilder.append('[');
        stringBuilder.append(string);
        if (!this.mdcSdId.toString().equals(string)) {
            this.appendMap(structuredDataElement.getPrefix(), structuredDataElement.getFields(), stringBuilder, ListChecker.NOOP_CHECKER);
        } else {
            this.appendMap(structuredDataElement.getPrefix(), structuredDataElement.getFields(), stringBuilder, listChecker);
        }
        stringBuilder.append(']');
    }

    private String getId(StructuredDataId structuredDataId) {
        int n;
        StringBuilder stringBuilder = new StringBuilder();
        if (structuredDataId == null || structuredDataId.getName() == null) {
            stringBuilder.append(this.defaultId);
        } else {
            stringBuilder.append(structuredDataId.getName());
        }
        if ((n = structuredDataId != null ? structuredDataId.getEnterpriseNumber() : this.enterpriseNumber) < 0) {
            n = this.enterpriseNumber;
        }
        if (n >= 0) {
            stringBuilder.append('@').append(n);
        }
        return stringBuilder.toString();
    }

    private void checkRequired(Map<String, String> map) {
        for (String string : this.mdcRequired) {
            if (map.get(string) != null) continue;
            throw new LoggingException("Required key " + string + " is missing from the " + this.mdcId);
        }
    }

    private void appendMap(String string, Map<String, String> object, StringBuilder stringBuilder, ListChecker listChecker) {
        for (Map.Entry entry : new TreeMap(object).entrySet()) {
            if (!listChecker.check((String)entry.getKey()) || entry.getValue() == null) continue;
            stringBuilder.append(' ');
            if (string != null) {
                stringBuilder.append(string);
            }
            Rfc5424Layout rfc5424Layout = this;
            String string2 = rfc5424Layout.escapeNewlines(rfc5424Layout.escapeSDParams((String)entry.getKey()), this.escapeNewLine);
            Rfc5424Layout rfc5424Layout2 = this;
            String string3 = rfc5424Layout2.escapeNewlines(rfc5424Layout2.escapeSDParams((String)entry.getValue()), this.escapeNewLine);
            StringBuilders.appendKeyDqValue(stringBuilder, string2, string3);
        }
    }

    private String escapeSDParams(String string) {
        return PARAM_VALUE_ESCAPE_PATTERN.matcher(string).replaceAll("\\\\$0");
    }

    public final String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("facility=").append(this.facility.name());
        stringBuilder.append(" appName=").append(this.appName);
        stringBuilder.append(" defaultId=").append(this.defaultId);
        stringBuilder.append(" enterpriseNumber=").append(this.enterpriseNumber);
        stringBuilder.append(" newLine=").append(this.includeNewLine);
        stringBuilder.append(" includeMDC=").append(this.includeMdc);
        stringBuilder.append(" messageId=").append(this.messageId);
        return stringBuilder.toString();
    }

    @PluginFactory
    public static Rfc5424Layout createLayout(@PluginAttribute(value="facility", defaultString="LOCAL0") Facility facility, @PluginAttribute(value="id") String string, @PluginAttribute(value="enterpriseNumber", defaultInt=18060) int n, @PluginAttribute(value="includeMDC", defaultBoolean=true) boolean bl, @PluginAttribute(value="mdcId", defaultString="mdc") String string2, @PluginAttribute(value="mdcPrefix") String string3, @PluginAttribute(value="eventPrefix") String string4, @PluginAttribute(value="newLine") boolean bl2, @PluginAttribute(value="newLineEscape") String string5, @PluginAttribute(value="appName") String string6, @PluginAttribute(value="messageId") String string7, @PluginAttribute(value="mdcExcludes") String string8, @PluginAttribute(value="mdcIncludes") String string9, @PluginAttribute(value="mdcRequired") String string10, @PluginAttribute(value="exceptionPattern") String string11, @PluginAttribute(value="useTlsMessageFormat") boolean bl3, @PluginElement(value="LoggerFields") LoggerFields[] loggerFieldsArray, @PluginConfiguration Configuration configuration) {
        if (string9 != null && string8 != null) {
            LOGGER.error("mdcIncludes and mdcExcludes are mutually exclusive. Includes wil be ignored");
            string9 = null;
        }
        return new Rfc5424Layout(configuration, facility, string, n, bl, bl2, string5, string2, string3, string4, string6, string7, string8, string9, string10, StandardCharsets.UTF_8, string11, bl3, loggerFieldsArray);
    }

    public final Facility getFacility() {
        return this.facility;
    }

    class StructuredDataElement {
        private final Map<String, String> fields;
        private final boolean discardIfEmpty;
        private final String prefix;

        public StructuredDataElement(Map<String, String> map, String string, boolean bl) {
            this.discardIfEmpty = bl;
            this.fields = map;
            this.prefix = string;
        }

        boolean discard() {
            if (!this.discardIfEmpty) {
                return false;
            }
            boolean bl = false;
            Iterator<Map.Entry<String, String>> iterator = this.fields.entrySet().iterator();
            while (iterator.hasNext()) {
                if (!Strings.isNotEmpty(iterator.next().getValue())) continue;
                bl = true;
                break;
            }
            return !bl;
        }

        void union(Map<String, String> map) {
            this.fields.putAll(map);
        }

        Map<String, String> getFields() {
            return this.fields;
        }

        String getPrefix() {
            return this.prefix;
        }
    }

    class FieldFormatter {
        private final Map<String, List<PatternFormatter>> delegateMap;
        private final boolean discardIfEmpty;

        public FieldFormatter(Map<String, List<PatternFormatter>> map, boolean bl) {
            this.discardIfEmpty = bl;
            this.delegateMap = map;
        }

        public StructuredDataElement format(LogEvent logEvent) {
            HashMap<String, String> hashMap = new HashMap<String, String>(this.delegateMap.size());
            for (Map.Entry<String, List<PatternFormatter>> entry : this.delegateMap.entrySet()) {
                StringBuilder stringBuilder = new StringBuilder();
                Iterator<PatternFormatter> iterator = entry.getValue().iterator();
                while (iterator.hasNext()) {
                    iterator.next().format(logEvent, stringBuilder);
                }
                hashMap.put(entry.getKey(), stringBuilder.toString());
            }
            return new StructuredDataElement(hashMap, Rfc5424Layout.this.eventPrefix, this.discardIfEmpty);
        }
    }
}

