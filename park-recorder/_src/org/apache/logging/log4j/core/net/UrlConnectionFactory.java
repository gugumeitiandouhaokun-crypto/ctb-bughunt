/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import javax.net.ssl.HttpsURLConnection;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.net.ssl.LaxHostnameVerifier;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;
import org.apache.logging.log4j.core.net.ssl.SslConfigurationFactory;

public class UrlConnectionFactory {
    private static int DEFAULT_TIMEOUT = 60000;
    private static int connectTimeoutMillis = 60000;
    private static int readTimeoutMillis = DEFAULT_TIMEOUT;
    private static final String JSON = "application/json";
    private static final String XML = "application/xml";
    private static final String PROPERTIES = "text/x-java-properties";
    private static final String TEXT = "text/plain";
    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    public static HttpURLConnection createConnection(URL uRL, long l, SslConfiguration sslConfiguration) {
        HttpURLConnection httpURLConnection = (HttpURLConnection)uRL.openConnection();
        Object object = ConfigurationFactory.getAuthorizationProvider();
        if (object != null) {
            object.addAuthorization(httpURLConnection);
        }
        httpURLConnection.setAllowUserInteraction(false);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setRequestMethod("GET");
        if (connectTimeoutMillis > 0) {
            httpURLConnection.setConnectTimeout(connectTimeoutMillis);
        }
        if (readTimeoutMillis > 0) {
            httpURLConnection.setReadTimeout(readTimeoutMillis);
        }
        String[] stringArray = uRL.getFile().split("\\.");
        object = stringArray[stringArray.length - 1].trim();
        object = UrlConnectionFactory.isXml((String)object) ? XML : (UrlConnectionFactory.isJson((String)object) ? JSON : (UrlConnectionFactory.isProperties((String)object) ? PROPERTIES : TEXT));
        httpURLConnection.setRequestProperty("Content-Type", (String)object);
        if (l > 0L) {
            httpURLConnection.setIfModifiedSince(l);
        }
        if (uRL.getProtocol().equals(HTTPS) && sslConfiguration != null) {
            ((HttpsURLConnection)httpURLConnection).setSSLSocketFactory(sslConfiguration.getSslSocketFactory());
            if (!sslConfiguration.isVerifyHostName()) {
                ((HttpsURLConnection)httpURLConnection).setHostnameVerifier(LaxHostnameVerifier.INSTANCE);
            }
        }
        return httpURLConnection;
    }

    public static URLConnection createConnection(URL object) {
        object = ((URL)object).getProtocol().equals(HTTPS) || ((URL)object).getProtocol().equals(HTTP) ? UrlConnectionFactory.createConnection((URL)object, 0L, SslConfigurationFactory.getSslConfiguration()) : ((URL)object).openConnection();
        return object;
    }

    private static boolean isXml(String string) {
        return string.equalsIgnoreCase("xml");
    }

    private static boolean isJson(String string) {
        return string.equalsIgnoreCase("json") || string.equalsIgnoreCase("jsn");
    }

    private static boolean isProperties(String string) {
        return string.equalsIgnoreCase("properties");
    }
}

