/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Objects;
import javax.net.ssl.HttpsURLConnection;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.HttpManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.net.ssl.LaxHostnameVerifier;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;

public class HttpURLConnectionManager
extends HttpManager {
    private static final Charset CHARSET = Charset.forName("US-ASCII");
    private final URL url;
    private final boolean isHttps;
    private final String method;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;
    private final Property[] headers;
    private final SslConfiguration sslConfiguration;
    private final boolean verifyHostname;

    public HttpURLConnectionManager(Configuration configuration, LoggerContext loggerContext, String string, URL uRL, String string2, int n, int n2, Property[] propertyArray, SslConfiguration sslConfiguration, boolean bl) {
        super(configuration, loggerContext, string);
        this.url = uRL;
        if (!uRL.getProtocol().equalsIgnoreCase("http") && !uRL.getProtocol().equalsIgnoreCase("https")) {
            throw new ConfigurationException("URL must have scheme http or https");
        }
        this.isHttps = this.url.getProtocol().equalsIgnoreCase("https");
        this.method = Objects.requireNonNull(string2, "method");
        this.connectTimeoutMillis = n;
        this.readTimeoutMillis = n2;
        this.headers = propertyArray != null ? propertyArray : new Property[]{};
        this.sslConfiguration = sslConfiguration;
        if (this.sslConfiguration != null && !this.isHttps) {
            throw new ConfigurationException("SSL configuration can only be specified with URL scheme https");
        }
        this.verifyHostname = bl;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void send(Layout<?> object, LogEvent serializable) {
        Object object2;
        Object object3;
        Object object4;
        HttpURLConnection httpURLConnection;
        block33: {
            httpURLConnection = (HttpURLConnection)this.url.openConnection();
            httpURLConnection.setAllowUserInteraction(false);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod(this.method);
            if (this.connectTimeoutMillis > 0) {
                httpURLConnection.setConnectTimeout(this.connectTimeoutMillis);
            }
            if (this.readTimeoutMillis > 0) {
                httpURLConnection.setReadTimeout(this.readTimeoutMillis);
            }
            if (object.getContentType() != null) {
                httpURLConnection.setRequestProperty("Content-Type", object.getContentType());
            }
            Property[] propertyArray = this.headers;
            int n = this.headers.length;
            for (int i2 = 0; i2 < n; ++i2) {
                object4 = propertyArray[i2];
                httpURLConnection.setRequestProperty(((Property)object4).getName(), ((Property)object4).isValueNeedsLookup() ? this.getConfiguration().getStrSubstitutor().replace((LogEvent)serializable, ((Property)object4).getValue()) : ((Property)object4).getValue());
            }
            if (this.sslConfiguration != null) {
                ((HttpsURLConnection)httpURLConnection).setSSLSocketFactory(this.sslConfiguration.getSslSocketFactory());
            }
            if (this.isHttps && !this.verifyHostname) {
                ((HttpsURLConnection)httpURLConnection).setHostnameVerifier(LaxHostnameVerifier.INSTANCE);
            }
            byte[] byArray = object.toByteArray((LogEvent)serializable);
            httpURLConnection.setFixedLengthStreamingMode(byArray.length);
            httpURLConnection.connect();
            object3 = httpURLConnection.getOutputStream();
            object2 = null;
            try {
                ((OutputStream)object3).write(byArray);
                if (object3 == null) break block33;
            }
            catch (Throwable throwable) {
                try {
                    object4 = throwable;
                    object2 = throwable;
                    throw object4;
                }
                catch (Throwable throwable2) {
                    if (object3 == null) throw throwable2;
                    if (object2 == null) {
                        ((OutputStream)object3).close();
                        throw throwable2;
                    }
                    try {
                        ((OutputStream)object3).close();
                        throw throwable2;
                    }
                    catch (Throwable throwable3) {
                        ((Throwable)object2).addSuppressed(throwable3);
                        throw throwable2;
                    }
                }
            }
            ((OutputStream)object3).close();
        }
        object3 = new byte[1024];
        try {
            object2 = httpURLConnection.getInputStream();
            object4 = null;
            try {
                while (-1 != ((InputStream)object2).read((byte[])object3)) {
                }
                if (object2 == null) return;
            }
            catch (Throwable throwable) {
                try {
                    object = throwable;
                    object4 = throwable;
                    throw object;
                }
                catch (Throwable throwable4) {
                    if (object2 == null) throw throwable4;
                    if (object4 == null) {
                        ((InputStream)object2).close();
                        throw throwable4;
                    }
                    try {
                        ((InputStream)object2).close();
                        throw throwable4;
                    }
                    catch (Throwable throwable5) {
                        ((Throwable)object4).addSuppressed(throwable5);
                        throw throwable4;
                    }
                }
            }
            ((InputStream)object2).close();
            return;
        }
        catch (IOException iOException) {
            block34: {
                object4 = new StringBuilder();
                object = httpURLConnection.getErrorStream();
                serializable = null;
                try {
                    ((StringBuilder)object4).append(httpURLConnection.getResponseCode());
                    if (httpURLConnection.getResponseMessage() != null) {
                        ((StringBuilder)object4).append(' ').append(httpURLConnection.getResponseMessage());
                    }
                    if (object != null) {
                        int n;
                        ((StringBuilder)object4).append(" - ");
                        while (-1 != (n = ((InputStream)object).read((byte[])object3))) {
                            ((StringBuilder)object4).append(new String((byte[])object3, 0, n, CHARSET));
                        }
                    }
                    if (object == null) break block34;
                }
                catch (Throwable throwable) {
                    try {
                        Throwable throwable6 = throwable;
                        serializable = throwable;
                        throw throwable6;
                    }
                    catch (Throwable throwable7) {
                        if (object == null) throw throwable7;
                        if (serializable == null) {
                            ((InputStream)object).close();
                            throw throwable7;
                        }
                        try {
                            ((InputStream)object).close();
                            throw throwable7;
                        }
                        catch (Throwable throwable8) {
                            ((Throwable)serializable).addSuppressed(throwable8);
                            throw throwable7;
                        }
                    }
                }
                ((InputStream)object).close();
            }
            if (httpURLConnection.getResponseCode() < 0) throw iOException;
            throw new IOException(((StringBuilder)object4).toString());
        }
    }
}

