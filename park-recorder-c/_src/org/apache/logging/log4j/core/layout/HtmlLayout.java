/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.layout;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.util.Transform;
import org.apache.logging.log4j.util.Strings;

@Plugin(name="HtmlLayout", category="Core", elementType="layout", printObject=true)
public final class HtmlLayout
extends AbstractStringLayout {
    public static final String DEFAULT_FONT_FAMILY = "arial,sans-serif";
    private static final String TRACE_PREFIX = "<br />&nbsp;&nbsp;&nbsp;&nbsp;";
    private static final String REGEXP = Strings.LINE_SEPARATOR.equals("\n") ? "\n" : Strings.LINE_SEPARATOR + "|\n";
    private static final String DEFAULT_TITLE = "Log4j Log Messages";
    private static final String DEFAULT_CONTENT_TYPE = "text/html";
    private final long jvmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
    private final boolean locationInfo;
    private final String title;
    private final String contentType;
    private final String font;
    private final String fontSize;
    private final String headerSize;

    private HtmlLayout(boolean bl, String string, String string2, Charset charset, String string3, String string4, String string5) {
        super(charset);
        this.locationInfo = bl;
        this.title = string;
        this.contentType = this.addCharsetToContentType(string2);
        this.font = string3;
        this.fontSize = string4;
        this.headerSize = string5;
    }

    public final String getTitle() {
        return this.title;
    }

    public final boolean isLocationInfo() {
        return this.locationInfo;
    }

    @Override
    public final boolean requiresLocation() {
        return this.locationInfo;
    }

    private String addCharsetToContentType(String string) {
        if (string == null) {
            return "text/html; charset=" + this.getCharset();
        }
        if (string.contains("charset")) {
            return string;
        }
        return string + "; charset=" + this.getCharset();
    }

    @Override
    public final String toSerializable(LogEvent logEvent) {
        StringBuilder stringBuilder = HtmlLayout.getStringBuilder();
        stringBuilder.append(Strings.LINE_SEPARATOR).append("<tr>").append(Strings.LINE_SEPARATOR);
        stringBuilder.append("<td>");
        stringBuilder.append(logEvent.getTimeMillis() - this.jvmStartTime);
        stringBuilder.append("</td>").append(Strings.LINE_SEPARATOR);
        Object object = Transform.escapeHtmlTags(logEvent.getThreadName());
        stringBuilder.append("<td title=\"").append((String)object).append(" thread\">");
        stringBuilder.append((String)object);
        stringBuilder.append("</td>").append(Strings.LINE_SEPARATOR);
        stringBuilder.append("<td title=\"Level\">");
        if (logEvent.getLevel().equals(Level.DEBUG)) {
            stringBuilder.append("<font color=\"#339933\">");
            stringBuilder.append(Transform.escapeHtmlTags(String.valueOf(logEvent.getLevel())));
            stringBuilder.append("</font>");
        } else if (logEvent.getLevel().isMoreSpecificThan(Level.WARN)) {
            stringBuilder.append("<font color=\"#993300\"><strong>");
            stringBuilder.append(Transform.escapeHtmlTags(String.valueOf(logEvent.getLevel())));
            stringBuilder.append("</strong></font>");
        } else {
            stringBuilder.append(Transform.escapeHtmlTags(String.valueOf(logEvent.getLevel())));
        }
        stringBuilder.append("</td>").append(Strings.LINE_SEPARATOR);
        object = Transform.escapeHtmlTags(logEvent.getLoggerName());
        if (Strings.isEmpty((CharSequence)object)) {
            object = "root";
        }
        stringBuilder.append("<td title=\"").append((String)object).append(" logger\">");
        stringBuilder.append((String)object);
        stringBuilder.append("</td>").append(Strings.LINE_SEPARATOR);
        if (this.locationInfo) {
            object = logEvent.getSource();
            stringBuilder.append("<td>");
            stringBuilder.append(Transform.escapeHtmlTags(((StackTraceElement)object).getFileName()));
            stringBuilder.append(':');
            stringBuilder.append(((StackTraceElement)object).getLineNumber());
            stringBuilder.append("</td>").append(Strings.LINE_SEPARATOR);
        }
        stringBuilder.append("<td title=\"Message\">");
        stringBuilder.append(Transform.escapeHtmlTags(logEvent.getMessage().getFormattedMessage()).replaceAll(REGEXP, "<br />"));
        stringBuilder.append("</td>").append(Strings.LINE_SEPARATOR);
        stringBuilder.append("</tr>").append(Strings.LINE_SEPARATOR);
        if (logEvent.getContextStack() != null && !logEvent.getContextStack().isEmpty()) {
            stringBuilder.append("<tr><td bgcolor=\"#EEEEEE\" style=\"font-size : ").append(this.fontSize);
            stringBuilder.append(";\" colspan=\"6\" ");
            stringBuilder.append("title=\"Nested Diagnostic Context\">");
            stringBuilder.append("NDC: ").append(Transform.escapeHtmlTags(logEvent.getContextStack().toString()));
            stringBuilder.append("</td></tr>").append(Strings.LINE_SEPARATOR);
        }
        if (logEvent.getContextData() != null && !logEvent.getContextData().isEmpty()) {
            stringBuilder.append("<tr><td bgcolor=\"#EEEEEE\" style=\"font-size : ").append(this.fontSize);
            stringBuilder.append(";\" colspan=\"6\" ");
            stringBuilder.append("title=\"Mapped Diagnostic Context\">");
            stringBuilder.append("MDC: ").append(Transform.escapeHtmlTags(logEvent.getContextData().toMap().toString()));
            stringBuilder.append("</td></tr>").append(Strings.LINE_SEPARATOR);
        }
        if ((object = logEvent.getThrown()) != null) {
            stringBuilder.append("<tr><td bgcolor=\"#993300\" style=\"color:White; font-size : ").append(this.fontSize);
            stringBuilder.append(";\" colspan=\"6\">");
            this.appendThrowableAsHtml((Throwable)object, stringBuilder);
            stringBuilder.append("</td></tr>").append(Strings.LINE_SEPARATOR);
        }
        return stringBuilder.toString();
    }

    @Override
    public final String getContentType() {
        return this.contentType;
    }

    private void appendThrowableAsHtml(Throwable iterator, StringBuilder stringBuilder) {
        Object object = new StringWriter();
        Object object2 = new PrintWriter((Writer)object);
        try {
            ((Throwable)((Object)iterator)).printStackTrace((PrintWriter)object2);
        }
        catch (RuntimeException runtimeException) {}
        ((PrintWriter)object2).flush();
        iterator = new LineNumberReader(new StringReader(((StringWriter)object).toString()));
        object = new ArrayList();
        try {
            object2 = ((LineNumberReader)((Object)iterator)).readLine();
            while (object2 != null) {
                ((ArrayList)object).add(object2);
                object2 = ((LineNumberReader)((Object)iterator)).readLine();
            }
        }
        catch (IOException iOException) {
            object2 = iOException;
            if (iOException instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            ((ArrayList)object).add(((Throwable)object2).toString());
        }
        boolean bl = true;
        iterator = ((ArrayList)object).iterator();
        while (iterator.hasNext()) {
            object = (String)iterator.next();
            if (!bl) {
                stringBuilder.append(TRACE_PREFIX);
            } else {
                bl = false;
            }
            stringBuilder.append(Transform.escapeHtmlTags((String)object));
            stringBuilder.append(Strings.LINE_SEPARATOR);
        }
    }

    private StringBuilder appendLs(StringBuilder stringBuilder, String string) {
        stringBuilder.append(string).append(Strings.LINE_SEPARATOR);
        return stringBuilder;
    }

    private StringBuilder append(StringBuilder stringBuilder, String string) {
        stringBuilder.append(string);
        return stringBuilder;
    }

    @Override
    public final byte[] getHeader() {
        StringBuilder stringBuilder = new StringBuilder();
        this.append(stringBuilder, "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" ");
        this.appendLs(stringBuilder, "\"http://www.w3.org/TR/html4/loose.dtd\">");
        this.appendLs(stringBuilder, "<html>");
        this.appendLs(stringBuilder, "<head>");
        this.append(stringBuilder, "<meta charset=\"");
        this.append(stringBuilder, this.getCharset().toString());
        this.appendLs(stringBuilder, "\"/>");
        this.append(stringBuilder, "<title>").append(this.title);
        this.appendLs(stringBuilder, "</title>");
        this.appendLs(stringBuilder, "<style type=\"text/css\">");
        this.appendLs(stringBuilder, "<!--");
        this.append(stringBuilder, "body, table {font-family:").append(this.font).append("; font-size: ");
        this.appendLs(stringBuilder, this.headerSize).append(";}");
        this.appendLs(stringBuilder, "th {background: #336699; color: #FFFFFF; text-align: left;}");
        this.appendLs(stringBuilder, "-->");
        this.appendLs(stringBuilder, "</style>");
        this.appendLs(stringBuilder, "</head>");
        this.appendLs(stringBuilder, "<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\">");
        this.appendLs(stringBuilder, "<hr size=\"1\" noshade=\"noshade\">");
        this.appendLs(stringBuilder, "Log session start time " + new Date() + "<br>");
        this.appendLs(stringBuilder, "<br>");
        this.appendLs(stringBuilder, "<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">");
        this.appendLs(stringBuilder, "<tr>");
        this.appendLs(stringBuilder, "<th>Time</th>");
        this.appendLs(stringBuilder, "<th>Thread</th>");
        this.appendLs(stringBuilder, "<th>Level</th>");
        this.appendLs(stringBuilder, "<th>Logger</th>");
        if (this.locationInfo) {
            this.appendLs(stringBuilder, "<th>File:Line</th>");
        }
        this.appendLs(stringBuilder, "<th>Message</th>");
        this.appendLs(stringBuilder, "</tr>");
        return stringBuilder.toString().getBytes(this.getCharset());
    }

    @Override
    public final byte[] getFooter() {
        StringBuilder stringBuilder = new StringBuilder();
        this.appendLs(stringBuilder, "</table>");
        this.appendLs(stringBuilder, "<br>");
        this.appendLs(stringBuilder, "</body></html>");
        return this.getBytes(stringBuilder.toString());
    }

    @PluginFactory
    public static HtmlLayout createLayout(@PluginAttribute(value="locationInfo") boolean bl, @PluginAttribute(value="title", defaultString="Log4j Log Messages") String string, @PluginAttribute(value="contentType") String string2, @PluginAttribute(value="charset", defaultString="UTF-8") Charset charset, @PluginAttribute(value="fontSize") String string3, @PluginAttribute(value="fontName", defaultString="arial,sans-serif") String string4) {
        Object object = FontSize.getFontSize(string3);
        string3 = object.getFontSize();
        object = object.larger().getFontSize();
        if (string2 == null) {
            string2 = "text/html; charset=".concat(String.valueOf(charset));
        }
        return new HtmlLayout(bl, string, string2, charset, string4, string3, (String)object);
    }

    public static HtmlLayout createDefaultLayout() {
        return HtmlLayout.newBuilder().build();
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder
    implements org.apache.logging.log4j.core.util.Builder<HtmlLayout> {
        @PluginBuilderAttribute
        private boolean locationInfo = false;
        @PluginBuilderAttribute
        private String title = "Log4j Log Messages";
        @PluginBuilderAttribute
        private String contentType = null;
        @PluginBuilderAttribute
        private Charset charset = StandardCharsets.UTF_8;
        @PluginBuilderAttribute
        private FontSize fontSize = FontSize.SMALL;
        @PluginBuilderAttribute
        private String fontName = "arial,sans-serif";

        private Builder() {
        }

        public Builder withLocationInfo(boolean bl) {
            this.locationInfo = bl;
            return this;
        }

        public Builder withTitle(String string) {
            this.title = string;
            return this;
        }

        public Builder withContentType(String string) {
            this.contentType = string;
            return this;
        }

        public Builder withCharset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Builder withFontSize(FontSize fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        public Builder withFontName(String string) {
            this.fontName = string;
            return this;
        }

        @Override
        public HtmlLayout build() {
            if (this.contentType == null) {
                this.contentType = "text/html; charset=" + this.charset;
            }
            return new HtmlLayout(this.locationInfo, this.title, this.contentType, this.charset, this.fontName, this.fontSize.getFontSize(), this.fontSize.larger().getFontSize());
        }
    }

    public static enum FontSize {
        SMALLER("smaller"),
        XXSMALL("xx-small"),
        XSMALL("x-small"),
        SMALL("small"),
        MEDIUM("medium"),
        LARGE("large"),
        XLARGE("x-large"),
        XXLARGE("xx-large"),
        LARGER("larger");

        private final String size;

        private FontSize(String string2) {
            this.size = string2;
        }

        public final String getFontSize() {
            return this.size;
        }

        public static FontSize getFontSize(String string) {
            for (FontSize fontSize : FontSize.values()) {
                if (!fontSize.size.equals(string)) continue;
                return fontSize;
            }
            return SMALL;
        }

        public final FontSize larger() {
            if (this.ordinal() < XXLARGE.ordinal()) {
                return FontSize.values()[this.ordinal() + 1];
            }
            return this;
        }
    }
}

