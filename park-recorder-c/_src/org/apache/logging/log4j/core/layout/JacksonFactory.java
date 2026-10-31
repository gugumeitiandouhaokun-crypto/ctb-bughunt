/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.PrettyPrinter
 *  com.fasterxml.jackson.core.util.DefaultPrettyPrinter
 *  com.fasterxml.jackson.core.util.MinimalPrettyPrinter
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  com.fasterxml.jackson.databind.ObjectWriter
 *  com.fasterxml.jackson.databind.ser.FilterProvider
 *  com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
 *  com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
 *  com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter
 *  org.codehaus.stax2.XMLStreamWriter2
 */
package org.apache.logging.log4j.core.layout;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;
import java.util.HashSet;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.jackson.Log4jJsonObjectMapper;
import org.apache.logging.log4j.core.jackson.Log4jXmlObjectMapper;
import org.apache.logging.log4j.core.jackson.Log4jYamlObjectMapper;
import org.codehaus.stax2.XMLStreamWriter2;

abstract class JacksonFactory {
    JacksonFactory() {
    }

    protected abstract String getPropertyNameForTimeMillis();

    protected abstract String getPropertyNameForInstant();

    protected abstract String getPropertNameForContextMap();

    protected abstract String getPropertNameForSource();

    protected abstract String getPropertNameForNanoTime();

    protected abstract PrettyPrinter newCompactPrinter();

    protected abstract ObjectMapper newObjectMapper();

    protected abstract PrettyPrinter newPrettyPrinter();

    ObjectWriter newWriter(boolean bl, boolean bl2, boolean bl3) {
        return this.newWriter(bl, bl2, bl3, false);
    }

    ObjectWriter newWriter(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();
        HashSet<String> hashSet = new HashSet<String>(3);
        if (!bl) {
            hashSet.add(this.getPropertNameForSource());
        }
        if (!bl2) {
            hashSet.add(this.getPropertNameForContextMap());
        }
        if (bl4) {
            hashSet.add(this.getPropertyNameForInstant());
        } else {
            hashSet.add(this.getPropertyNameForTimeMillis());
        }
        hashSet.add(this.getPropertNameForNanoTime());
        simpleFilterProvider.addFilter(Log4jLogEvent.class.getName(), SimpleBeanPropertyFilter.serializeAllExcept(hashSet));
        return this.newObjectMapper().writer(bl3 ? this.newCompactPrinter() : this.newPrettyPrinter()).with((FilterProvider)simpleFilterProvider);
    }

    static class Log4jXmlPrettyPrinter
    extends DefaultXmlPrettyPrinter {
        private static final long serialVersionUID = 1L;

        Log4jXmlPrettyPrinter(int n) {
            this._nesting = n;
        }

        public void writePrologLinefeed(XMLStreamWriter2 xMLStreamWriter2) {
        }

        public DefaultXmlPrettyPrinter createInstance() {
            return new Log4jXmlPrettyPrinter(1);
        }
    }

    static class YAML
    extends JacksonFactory {
        private final boolean includeStacktrace;
        private final boolean stacktraceAsString;

        public YAML(boolean bl, boolean bl2) {
            this.includeStacktrace = bl;
            this.stacktraceAsString = bl2;
        }

        @Override
        protected String getPropertyNameForTimeMillis() {
            return "timeMillis";
        }

        @Override
        protected String getPropertyNameForInstant() {
            return "instant";
        }

        @Override
        protected String getPropertNameForContextMap() {
            return "contextMap";
        }

        @Override
        protected String getPropertNameForSource() {
            return "source";
        }

        @Override
        protected String getPropertNameForNanoTime() {
            return "nanoTime";
        }

        @Override
        protected PrettyPrinter newCompactPrinter() {
            return new MinimalPrettyPrinter();
        }

        @Override
        protected ObjectMapper newObjectMapper() {
            return new Log4jYamlObjectMapper(false, this.includeStacktrace, this.stacktraceAsString);
        }

        @Override
        protected PrettyPrinter newPrettyPrinter() {
            return new DefaultPrettyPrinter();
        }
    }

    static class XML
    extends JacksonFactory {
        static final int DEFAULT_INDENT = 1;
        private final boolean includeStacktrace;
        private final boolean stacktraceAsString;

        public XML(boolean bl, boolean bl2) {
            this.includeStacktrace = bl;
            this.stacktraceAsString = bl2;
        }

        @Override
        protected String getPropertyNameForTimeMillis() {
            return "TimeMillis";
        }

        @Override
        protected String getPropertyNameForInstant() {
            return "Instant";
        }

        @Override
        protected String getPropertNameForContextMap() {
            return "ContextMap";
        }

        @Override
        protected String getPropertNameForSource() {
            return "Source";
        }

        @Override
        protected String getPropertNameForNanoTime() {
            return "nanoTime";
        }

        @Override
        protected PrettyPrinter newCompactPrinter() {
            return null;
        }

        @Override
        protected ObjectMapper newObjectMapper() {
            return new Log4jXmlObjectMapper(this.includeStacktrace, this.stacktraceAsString);
        }

        @Override
        protected PrettyPrinter newPrettyPrinter() {
            return new Log4jXmlPrettyPrinter(1);
        }
    }

    static class JSON
    extends JacksonFactory {
        private final boolean encodeThreadContextAsList;
        private final boolean includeStacktrace;
        private final boolean stacktraceAsString;
        private final boolean objectMessageAsJsonObject;

        public JSON(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
            this.encodeThreadContextAsList = bl;
            this.includeStacktrace = bl2;
            this.stacktraceAsString = bl3;
            this.objectMessageAsJsonObject = bl4;
        }

        @Override
        protected String getPropertNameForContextMap() {
            return "contextMap";
        }

        @Override
        protected String getPropertyNameForTimeMillis() {
            return "timeMillis";
        }

        @Override
        protected String getPropertyNameForInstant() {
            return "instant";
        }

        @Override
        protected String getPropertNameForSource() {
            return "source";
        }

        @Override
        protected String getPropertNameForNanoTime() {
            return "nanoTime";
        }

        @Override
        protected PrettyPrinter newCompactPrinter() {
            return new MinimalPrettyPrinter();
        }

        @Override
        protected ObjectMapper newObjectMapper() {
            return new Log4jJsonObjectMapper(this.encodeThreadContextAsList, this.includeStacktrace, this.stacktraceAsString, this.objectMessageAsJsonObject);
        }

        @Override
        protected PrettyPrinter newPrettyPrinter() {
            return new DefaultPrettyPrinter();
        }
    }
}

