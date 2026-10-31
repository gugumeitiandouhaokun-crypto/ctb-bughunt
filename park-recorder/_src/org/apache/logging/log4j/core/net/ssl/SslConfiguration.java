/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net.ssl;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.net.ssl.KeyStoreConfiguration;
import org.apache.logging.log4j.core.net.ssl.KeyStoreConfigurationException;
import org.apache.logging.log4j.core.net.ssl.TrustStoreConfiguration;
import org.apache.logging.log4j.core.net.ssl.TrustStoreConfigurationException;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="Ssl", category="Core", printObject=true)
public class SslConfiguration {
    private static final StatusLogger LOGGER = StatusLogger.getLogger();
    private final KeyStoreConfiguration keyStoreConfig;
    private final TrustStoreConfiguration trustStoreConfig;
    private final SSLContext sslContext;
    private final String protocol;
    private final boolean verifyHostName;

    private SslConfiguration(String string, KeyStoreConfiguration keyStoreConfiguration, TrustStoreConfiguration trustStoreConfiguration, boolean bl) {
        this.keyStoreConfig = keyStoreConfiguration;
        this.trustStoreConfig = trustStoreConfiguration;
        this.protocol = string == null ? "SSL" : string;
        this.sslContext = this.createSslContext();
        this.verifyHostName = bl;
    }

    public void clearSecrets() {
        if (this.keyStoreConfig != null) {
            this.keyStoreConfig.clearSecrets();
        }
        if (this.trustStoreConfig != null) {
            this.trustStoreConfig.clearSecrets();
        }
    }

    public SSLSocketFactory getSslSocketFactory() {
        return this.sslContext.getSocketFactory();
    }

    public SSLServerSocketFactory getSslServerSocketFactory() {
        return this.sslContext.getServerSocketFactory();
    }

    private SSLContext createSslContext() {
        SSLContext sSLContext;
        try {
            sSLContext = this.createSslContextBasedOnConfiguration();
            LOGGER.debug("Creating SSLContext with the given parameters");
        }
        catch (TrustStoreConfigurationException trustStoreConfigurationException) {
            sSLContext = this.createSslContextWithTrustStoreFailure();
        }
        catch (KeyStoreConfigurationException keyStoreConfigurationException) {
            sSLContext = this.createSslContextWithKeyStoreFailure();
        }
        return sSLContext;
    }

    private SSLContext createSslContextWithTrustStoreFailure() {
        SSLContext sSLContext;
        try {
            sSLContext = this.createSslContextWithDefaultTrustManagerFactory();
            LOGGER.debug("Creating SSLContext with default truststore");
        }
        catch (KeyStoreConfigurationException keyStoreConfigurationException) {
            sSLContext = this.createDefaultSslContext();
            LOGGER.debug("Creating SSLContext with default configuration");
        }
        return sSLContext;
    }

    private SSLContext createSslContextWithKeyStoreFailure() {
        SSLContext sSLContext;
        try {
            sSLContext = this.createSslContextWithDefaultKeyManagerFactory();
            LOGGER.debug("Creating SSLContext with default keystore");
        }
        catch (TrustStoreConfigurationException trustStoreConfigurationException) {
            sSLContext = this.createDefaultSslContext();
            LOGGER.debug("Creating SSLContext with default configuration");
        }
        return sSLContext;
    }

    private SSLContext createSslContextBasedOnConfiguration() {
        return this.createSslContext(false, false);
    }

    private SSLContext createSslContextWithDefaultKeyManagerFactory() {
        try {
            return this.createSslContext(true, false);
        }
        catch (KeyStoreConfigurationException keyStoreConfigurationException) {
            LOGGER.debug("Exception occurred while using default keystore. This should be a BUG");
            return null;
        }
    }

    private SSLContext createSslContextWithDefaultTrustManagerFactory() {
        try {
            return this.createSslContext(false, true);
        }
        catch (TrustStoreConfigurationException trustStoreConfigurationException) {
            LOGGER.debug("Exception occurred while using default truststore. This should be a BUG");
            return null;
        }
    }

    private SSLContext createDefaultSslContext() {
        try {
            return SSLContext.getDefault();
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            LOGGER.error("Failed to create an SSLContext with default configuration", (Throwable)noSuchAlgorithmException);
            return null;
        }
    }

    private SSLContext createSslContext(boolean bl, boolean bl2) {
        try {
            KeyManager[] keyManagerArray = null;
            TrustManager[] trustManagerArray = null;
            SSLContext sSLContext = SSLContext.getInstance(this.protocol);
            if (!bl) {
                keyManagerArray = this.loadKeyManagerFactory().getKeyManagers();
            }
            if (!bl2) {
                trustManagerArray = this.loadTrustManagerFactory().getTrustManagers();
            }
            sSLContext.init(keyManagerArray, trustManagerArray, null);
            return sSLContext;
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            LOGGER.error("No Provider supports a TrustManagerFactorySpi implementation for the specified protocol", (Throwable)noSuchAlgorithmException);
            throw new TrustStoreConfigurationException(noSuchAlgorithmException);
        }
        catch (KeyManagementException keyManagementException) {
            LOGGER.error("Failed to initialize the SSLContext", (Throwable)keyManagementException);
            throw new KeyStoreConfigurationException(keyManagementException);
        }
    }

    private TrustManagerFactory loadTrustManagerFactory() {
        if (this.trustStoreConfig == null) {
            throw new TrustStoreConfigurationException(new Exception("The trustStoreConfiguration is null"));
        }
        try {
            return this.trustStoreConfig.initTrustManagerFactory();
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            LOGGER.error("The specified algorithm is not available from the specified provider", (Throwable)noSuchAlgorithmException);
            throw new TrustStoreConfigurationException(noSuchAlgorithmException);
        }
        catch (KeyStoreException keyStoreException) {
            LOGGER.error("Failed to initialize the TrustManagerFactory", (Throwable)keyStoreException);
            throw new TrustStoreConfigurationException(keyStoreException);
        }
    }

    private KeyManagerFactory loadKeyManagerFactory() {
        if (this.keyStoreConfig == null) {
            throw new KeyStoreConfigurationException(new Exception("The keyStoreConfiguration is null"));
        }
        try {
            return this.keyStoreConfig.initKeyManagerFactory();
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            LOGGER.error("The specified algorithm is not available from the specified provider", (Throwable)noSuchAlgorithmException);
            throw new KeyStoreConfigurationException(noSuchAlgorithmException);
        }
        catch (KeyStoreException keyStoreException) {
            LOGGER.error("Failed to initialize the TrustManagerFactory", (Throwable)keyStoreException);
            throw new KeyStoreConfigurationException(keyStoreException);
        }
        catch (UnrecoverableKeyException unrecoverableKeyException) {
            LOGGER.error("The key cannot be recovered (e.g. the given password is wrong)", (Throwable)unrecoverableKeyException);
            throw new KeyStoreConfigurationException(unrecoverableKeyException);
        }
    }

    @PluginFactory
    public static SslConfiguration createSSLConfiguration(@PluginAttribute(value="protocol") String string, @PluginElement(value="KeyStore") KeyStoreConfiguration keyStoreConfiguration, @PluginElement(value="TrustStore") TrustStoreConfiguration trustStoreConfiguration) {
        return new SslConfiguration(string, keyStoreConfiguration, trustStoreConfiguration, false);
    }

    public static SslConfiguration createSSLConfiguration(@PluginAttribute(value="protocol") String string, @PluginElement(value="KeyStore") KeyStoreConfiguration keyStoreConfiguration, @PluginElement(value="TrustStore") TrustStoreConfiguration trustStoreConfiguration, @PluginAttribute(value="verifyHostName") boolean bl) {
        return new SslConfiguration(string, keyStoreConfiguration, trustStoreConfiguration, bl);
    }

    public int hashCode() {
        int n = 31 + (this.keyStoreConfig == null ? 0 : this.keyStoreConfig.hashCode());
        n = 31 * n + (this.protocol == null ? 0 : this.protocol.hashCode());
        n = 31 * n + (this.sslContext == null ? 0 : this.sslContext.hashCode());
        return 31 * n + (this.trustStoreConfig == null ? 0 : this.trustStoreConfig.hashCode());
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (this.getClass() != object.getClass()) {
            return false;
        }
        object = (SslConfiguration)object;
        if (this.keyStoreConfig == null ? ((SslConfiguration)object).keyStoreConfig != null : !this.keyStoreConfig.equals(((SslConfiguration)object).keyStoreConfig)) {
            return false;
        }
        if (this.protocol == null ? ((SslConfiguration)object).protocol != null : !this.protocol.equals(((SslConfiguration)object).protocol)) {
            return false;
        }
        if (this.sslContext == null ? ((SslConfiguration)object).sslContext != null : !this.sslContext.equals(((SslConfiguration)object).sslContext)) {
            return false;
        }
        return !(this.trustStoreConfig == null ? ((SslConfiguration)object).trustStoreConfig != null : !this.trustStoreConfig.equals(((SslConfiguration)object).trustStoreConfig));
    }

    public KeyStoreConfiguration getKeyStoreConfig() {
        return this.keyStoreConfig;
    }

    public TrustStoreConfiguration getTrustStoreConfig() {
        return this.trustStoreConfig;
    }

    public SSLContext getSslContext() {
        return this.sslContext;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public boolean isVerifyHostName() {
        return this.verifyHostName;
    }
}

