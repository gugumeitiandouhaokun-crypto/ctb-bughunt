/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.csv.CSVFormat
 *  org.apache.commons.csv.QuoteMode
 */
package org.apache.logging.log4j.core.layout;

import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractCsvLayout;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="CsvParameterLayout", category="Core", elementType="layout", printObject=true)
public class CsvParameterLayout
extends AbstractCsvLayout {
    public static AbstractCsvLayout createDefaultLayout() {
        return new CsvParameterLayout(null, Charset.forName("UTF-8"), CSVFormat.valueOf((String)"Default"), null, null);
    }

    public static AbstractCsvLayout createLayout(CSVFormat cSVFormat) {
        return new CsvParameterLayout(null, Charset.forName("UTF-8"), cSVFormat, null, null);
    }

    @PluginFactory
    public static AbstractCsvLayout createLayout(@PluginConfiguration Configuration configuration, @PluginAttribute(value="format", defaultString="Default") String string, @PluginAttribute(value="delimiter") Character c2, @PluginAttribute(value="escape") Character c3, @PluginAttribute(value="quote") Character c4, @PluginAttribute(value="quoteMode") QuoteMode quoteMode, @PluginAttribute(value="nullString") String string2, @PluginAttribute(value="recordSeparator") String string3, @PluginAttribute(value="charset", defaultString="UTF-8") Charset charset, @PluginAttribute(value="header") String string4, @PluginAttribute(value="footer") String string5) {
        string = CsvParameterLayout.createFormat(string, c2, c3, c4, quoteMode, string2, string3);
        return new CsvParameterLayout(configuration, charset, (CSVFormat)string, string4, string5);
    }

    public CsvParameterLayout(Configuration configuration, Charset charset, CSVFormat cSVFormat, String string, String string2) {
        super(configuration, charset, cSVFormat, string, string2);
    }

    @Override
    public String toSerializable(LogEvent serializable) {
        serializable = serializable.getMessage();
        Object[] objectArray = serializable.getParameters();
        StringBuilder stringBuilder = CsvParameterLayout.getStringBuilder();
        try {
            this.getFormat().printRecord((Appendable)stringBuilder, objectArray);
            return stringBuilder.toString();
        }
        catch (IOException iOException) {
            StatusLogger.getLogger().error((Message)serializable, (Throwable)iOException);
            return this.getFormat().getCommentMarker() + " " + iOException;
        }
    }
}

