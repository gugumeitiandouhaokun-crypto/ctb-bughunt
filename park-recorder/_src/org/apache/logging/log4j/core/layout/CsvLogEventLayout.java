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
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="CsvLogEventLayout", category="Core", elementType="layout", printObject=true)
public class CsvLogEventLayout
extends AbstractCsvLayout {
    public static CsvLogEventLayout createDefaultLayout() {
        return new CsvLogEventLayout(null, Charset.forName("UTF-8"), CSVFormat.valueOf((String)"Default"), null, null);
    }

    public static CsvLogEventLayout createLayout(CSVFormat cSVFormat) {
        return new CsvLogEventLayout(null, Charset.forName("UTF-8"), cSVFormat, null, null);
    }

    @PluginFactory
    public static CsvLogEventLayout createLayout(@PluginConfiguration Configuration configuration, @PluginAttribute(value="format", defaultString="Default") String string, @PluginAttribute(value="delimiter") Character c2, @PluginAttribute(value="escape") Character c3, @PluginAttribute(value="quote") Character c4, @PluginAttribute(value="quoteMode") QuoteMode quoteMode, @PluginAttribute(value="nullString") String string2, @PluginAttribute(value="recordSeparator") String string3, @PluginAttribute(value="charset", defaultString="UTF-8") Charset charset, @PluginAttribute(value="header") String string4, @PluginAttribute(value="footer") String string5) {
        string = CsvLogEventLayout.createFormat(string, c2, c3, c4, quoteMode, string2, string3);
        return new CsvLogEventLayout(configuration, charset, (CSVFormat)string, string4, string5);
    }

    protected CsvLogEventLayout(Configuration configuration, Charset charset, CSVFormat cSVFormat, String string, String string2) {
        super(configuration, charset, cSVFormat, string, string2);
    }

    @Override
    public String toSerializable(LogEvent logEvent) {
        StringBuilder stringBuilder = CsvLogEventLayout.getStringBuilder();
        CSVFormat cSVFormat = this.getFormat();
        try {
            cSVFormat.print((Object)logEvent.getNanoTime(), (Appendable)stringBuilder, true);
            cSVFormat.print((Object)logEvent.getTimeMillis(), (Appendable)stringBuilder, false);
            cSVFormat.print((Object)logEvent.getLevel(), (Appendable)stringBuilder, false);
            cSVFormat.print((Object)logEvent.getThreadId(), (Appendable)stringBuilder, false);
            cSVFormat.print((Object)logEvent.getThreadName(), (Appendable)stringBuilder, false);
            cSVFormat.print((Object)logEvent.getThreadPriority(), (Appendable)stringBuilder, false);
            cSVFormat.print((Object)logEvent.getMessage().getFormattedMessage(), (Appendable)stringBuilder, false);
            cSVFormat.print((Object)logEvent.getLoggerFqcn(), (Appendable)stringBuilder, false);
            cSVFormat.print((Object)logEvent.getLoggerName(), (Appendable)stringBuilder, false);
            cSVFormat.print((Object)logEvent.getMarker(), (Appendable)stringBuilder, false);
            cSVFormat.print((Object)logEvent.getThrownProxy(), (Appendable)stringBuilder, false);
            cSVFormat.print((Object)logEvent.getSource(), (Appendable)stringBuilder, false);
            cSVFormat.print((Object)logEvent.getContextData(), (Appendable)stringBuilder, false);
            cSVFormat.print((Object)logEvent.getContextStack(), (Appendable)stringBuilder, false);
            cSVFormat.println((Appendable)stringBuilder);
            return stringBuilder.toString();
        }
        catch (IOException iOException) {
            StatusLogger.getLogger().error(logEvent.toString(), (Throwable)iOException);
            return cSVFormat.getCommentMarker() + " " + iOException;
        }
    }
}

