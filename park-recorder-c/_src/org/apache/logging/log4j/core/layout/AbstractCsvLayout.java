/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.csv.CSVFormat
 *  org.apache.commons.csv.QuoteMode
 */
package org.apache.logging.log4j.core.layout;

import java.nio.charset.Charset;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;

public abstract class AbstractCsvLayout
extends AbstractStringLayout {
    protected static final String DEFAULT_CHARSET = "UTF-8";
    protected static final String DEFAULT_FORMAT = "Default";
    private static final String CONTENT_TYPE = "text/csv";
    private final CSVFormat format;

    protected static CSVFormat createFormat(String string, Character c2, Character c3, Character c4, QuoteMode quoteMode, String string2, String string3) {
        string = CSVFormat.valueOf((String)string);
        if (AbstractCsvLayout.isNotNul(c2)) {
            string = string.withDelimiter(c2.charValue());
        }
        if (AbstractCsvLayout.isNotNul(c3)) {
            string = string.withEscape(c3);
        }
        if (AbstractCsvLayout.isNotNul(c4)) {
            string = string.withQuote(c4);
        }
        if (quoteMode != null) {
            string = string.withQuoteMode(quoteMode);
        }
        if (string2 != null) {
            string = string.withNullString(string2);
        }
        if (string3 != null) {
            string = string.withRecordSeparator(string3);
        }
        return string;
    }

    private static boolean isNotNul(Character c2) {
        return c2 != null && c2.charValue() != '\u0000';
    }

    protected AbstractCsvLayout(Configuration configuration, Charset charset, CSVFormat cSVFormat, String string, String string2) {
        super(configuration, charset, PatternLayout.newSerializerBuilder().setConfiguration(configuration).setPattern(string).build(), PatternLayout.newSerializerBuilder().setConfiguration(configuration).setPattern(string2).build());
        this.format = cSVFormat;
    }

    @Override
    public String getContentType() {
        return "text/csv; charset=" + this.getCharset();
    }

    public CSVFormat getFormat() {
        return this.format;
    }
}

