/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.io.Serializable;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.ValidPort;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.HtmlLayout;
import org.apache.logging.log4j.core.net.SmtpManager;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;
import org.apache.logging.log4j.core.util.Booleans;

@Plugin(name="SMTP", category="Core", elementType="appender", printObject=true)
public final class SmtpAppender
extends AbstractAppender {
    private static final int DEFAULT_BUFFER_SIZE = 512;
    private final SmtpManager manager;

    private SmtpAppender(String string, Filter filter, Layout<? extends Serializable> layout, SmtpManager smtpManager, boolean bl, Property[] propertyArray) {
        super(string, filter, layout, bl, propertyArray);
        this.manager = smtpManager;
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    public static SmtpAppender createAppender(@PluginConfiguration Configuration object, @PluginAttribute(value="name") @Required String string, @PluginAttribute(value="to") String string2, @PluginAttribute(value="cc") String string3, @PluginAttribute(value="bcc") String string4, @PluginAttribute(value="from") String string5, @PluginAttribute(value="replyTo") String string6, @PluginAttribute(value="subject") String string7, @PluginAttribute(value="smtpProtocol") String string8, @PluginAttribute(value="smtpHost") String string9, @PluginAttribute(value="smtpPort", defaultString="0") @ValidPort String string10, @PluginAttribute(value="smtpUsername") String string11, @PluginAttribute(value="smtpPassword", sensitive=true) String string12, @PluginAttribute(value="smtpDebug") String string13, @PluginAttribute(value="bufferSize") String string14, @PluginElement(value="Layout") Layout<? extends Serializable> htmlLayout, @PluginElement(value="Filter") Filter filter, @PluginAttribute(value="ignoreExceptions") String string15) {
        int n;
        if (string == null) {
            LOGGER.error("No name provided for SmtpAppender");
            return null;
        }
        boolean bl = Booleans.parseBoolean(string15, true);
        int n2 = AbstractAppender.parseInt(string10, 0);
        boolean bl2 = Boolean.parseBoolean(string13);
        int n3 = n = string14 == null ? 512 : Integer.parseInt(string14);
        if (htmlLayout == null) {
            htmlLayout = HtmlLayout.createDefaultLayout();
        }
        if (filter == null) {
            filter = ThresholdFilter.createFilter(null, null, null);
        }
        if ((object = SmtpManager.getSmtpManager((Configuration)(object != null ? object : new DefaultConfiguration()), string2, string3, string4, string5, string6, string7, string8, string9, n2, string11, string12, bl2, filter.toString(), n, null)) == null) {
            return null;
        }
        return new SmtpAppender(string, filter, htmlLayout, (SmtpManager)object, bl, null);
    }

    @Override
    public final boolean isFiltered(LogEvent logEvent) {
        boolean bl = super.isFiltered(logEvent);
        if (bl) {
            this.manager.add(logEvent);
        }
        return bl;
    }

    @Override
    public final void append(LogEvent logEvent) {
        this.manager.sendEvents(this.getLayout(), logEvent);
    }

    public static class Builder
    extends AbstractAppender.Builder<Builder>
    implements org.apache.logging.log4j.core.util.Builder<SmtpAppender> {
        @PluginBuilderAttribute
        private String to;
        @PluginBuilderAttribute
        private String cc;
        @PluginBuilderAttribute
        private String bcc;
        @PluginBuilderAttribute
        private String from;
        @PluginBuilderAttribute
        private String replyTo;
        @PluginBuilderAttribute
        private String subject;
        @PluginBuilderAttribute
        private String smtpProtocol = "smtp";
        @PluginBuilderAttribute
        private String smtpHost;
        @PluginBuilderAttribute
        @ValidPort
        private int smtpPort;
        @PluginBuilderAttribute
        private String smtpUsername;
        @PluginBuilderAttribute(sensitive=true)
        private String smtpPassword;
        @PluginBuilderAttribute
        private boolean smtpDebug;
        @PluginBuilderAttribute
        private int bufferSize = 512;
        @PluginElement(value="SSL")
        private SslConfiguration sslConfiguration;

        public Builder setTo(String string) {
            this.to = string;
            return this;
        }

        public Builder setCc(String string) {
            this.cc = string;
            return this;
        }

        public Builder setBcc(String string) {
            this.bcc = string;
            return this;
        }

        public Builder setFrom(String string) {
            this.from = string;
            return this;
        }

        public Builder setReplyTo(String string) {
            this.replyTo = string;
            return this;
        }

        public Builder setSubject(String string) {
            this.subject = string;
            return this;
        }

        public Builder setSmtpProtocol(String string) {
            this.smtpProtocol = string;
            return this;
        }

        public Builder setSmtpHost(String string) {
            this.smtpHost = string;
            return this;
        }

        public Builder setSmtpPort(int n) {
            this.smtpPort = n;
            return this;
        }

        public Builder setSmtpUsername(String string) {
            this.smtpUsername = string;
            return this;
        }

        public Builder setSmtpPassword(String string) {
            this.smtpPassword = string;
            return this;
        }

        public Builder setSmtpDebug(boolean bl) {
            this.smtpDebug = bl;
            return this;
        }

        public Builder setBufferSize(int n) {
            this.bufferSize = n;
            return this;
        }

        public Builder setSslConfiguration(SslConfiguration sslConfiguration) {
            this.sslConfiguration = sslConfiguration;
            return this;
        }

        @Override
        public Builder setLayout(Layout<? extends Serializable> layout) {
            return (Builder)super.setLayout(layout);
        }

        @Override
        public Builder setFilter(Filter filter) {
            return (Builder)super.setFilter(filter);
        }

        @Override
        public SmtpAppender build() {
            if (this.getLayout() == null) {
                this.setLayout((Layout)HtmlLayout.createDefaultLayout());
            }
            if (this.getFilter() == null) {
                this.setFilter(ThresholdFilter.createFilter(null, null, null));
            }
            SmtpManager smtpManager = SmtpManager.getSmtpManager(this.getConfiguration(), this.to, this.cc, this.bcc, this.from, this.replyTo, this.subject, this.smtpProtocol, this.smtpHost, this.smtpPort, this.smtpUsername, this.smtpPassword, this.smtpDebug, this.getFilter().toString(), this.bufferSize, this.sslConfiguration);
            return new SmtpAppender(this.getName(), this.getFilter(), this.getLayout(), smtpManager, this.isIgnoreExceptions(), this.getPropertyArray());
        }
    }
}

