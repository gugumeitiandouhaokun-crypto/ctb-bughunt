/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.io.Serializable;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.HttpManager;
import org.apache.logging.log4j.core.appender.HttpURLConnectionManager;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;

@Plugin(name="Http", category="Core", elementType="appender", printObject=true)
public final class HttpAppender
extends AbstractAppender {
    private final HttpManager manager;

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    private HttpAppender(String string, Layout<? extends Serializable> layout, Filter filter, boolean bl, HttpManager httpManager, Property[] propertyArray) {
        super(string, filter, layout, bl, propertyArray);
        Objects.requireNonNull(layout, "layout");
        this.manager = Objects.requireNonNull(httpManager, "manager");
    }

    @Override
    public final void start() {
        super.start();
        this.manager.startup();
    }

    @Override
    public final void append(LogEvent logEvent) {
        try {
            this.manager.send(this.getLayout(), logEvent);
            return;
        }
        catch (Exception exception) {
            this.error("Unable to send HTTP in appender [" + this.getName() + "]", logEvent, exception);
            return;
        }
    }

    @Override
    public final boolean stop(long l, TimeUnit timeUnit) {
        this.setStopping();
        boolean bl = super.stop(l, timeUnit, false) & this.manager.stop(l, timeUnit);
        this.setStopped();
        return bl;
    }

    @Override
    public final String toString() {
        return "HttpAppender{name=" + this.getName() + ", state=" + (Object)((Object)this.getState()) + '}';
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractAppender.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<HttpAppender> {
        @PluginBuilderAttribute
        @Required(message="No URL provided for HttpAppender")
        private URL url;
        @PluginBuilderAttribute
        private String method = "POST";
        @PluginBuilderAttribute
        private int connectTimeoutMillis = 0;
        @PluginBuilderAttribute
        private int readTimeoutMillis = 0;
        @PluginElement(value="Headers")
        private Property[] headers;
        @PluginElement(value="SslConfiguration")
        private SslConfiguration sslConfiguration;
        @PluginBuilderAttribute
        private boolean verifyHostname = true;

        @Override
        public HttpAppender build() {
            HttpURLConnectionManager httpURLConnectionManager = new HttpURLConnectionManager(this.getConfiguration(), this.getConfiguration().getLoggerContext(), this.getName(), this.url, this.method, this.connectTimeoutMillis, this.readTimeoutMillis, this.headers, this.sslConfiguration, this.verifyHostname);
            return new HttpAppender(this.getName(), this.getLayout(), this.getFilter(), this.isIgnoreExceptions(), httpURLConnectionManager, this.getPropertyArray());
        }

        public URL getUrl() {
            return this.url;
        }

        public String getMethod() {
            return this.method;
        }

        public int getConnectTimeoutMillis() {
            return this.connectTimeoutMillis;
        }

        public int getReadTimeoutMillis() {
            return this.readTimeoutMillis;
        }

        public Property[] getHeaders() {
            return this.headers;
        }

        public SslConfiguration getSslConfiguration() {
            return this.sslConfiguration;
        }

        public boolean isVerifyHostname() {
            return this.verifyHostname;
        }

        public B setUrl(URL uRL) {
            this.url = uRL;
            return (B)((Builder)this.asBuilder());
        }

        public B setMethod(String string) {
            this.method = string;
            return (B)((Builder)this.asBuilder());
        }

        public B setConnectTimeoutMillis(int n) {
            this.connectTimeoutMillis = n;
            return (B)((Builder)this.asBuilder());
        }

        public B setReadTimeoutMillis(int n) {
            this.readTimeoutMillis = n;
            return (B)((Builder)this.asBuilder());
        }

        public B setHeaders(Property[] propertyArray) {
            this.headers = propertyArray;
            return (B)((Builder)this.asBuilder());
        }

        public B setSslConfiguration(SslConfiguration sslConfiguration) {
            this.sslConfiguration = sslConfiguration;
            return (B)((Builder)this.asBuilder());
        }

        public B setVerifyHostname(boolean bl) {
            this.verifyHostname = bl;
            return (B)((Builder)this.asBuilder());
        }
    }
}

