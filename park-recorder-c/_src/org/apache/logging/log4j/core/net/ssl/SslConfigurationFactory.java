/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net.ssl;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.net.ssl.KeyStoreConfiguration;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;
import org.apache.logging.log4j.core.net.ssl.TrustStoreConfiguration;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

public class SslConfigurationFactory {
    private static final Logger LOGGER;
    private static SslConfiguration sslConfiguration;
    private static final String trustStorelocation = "log4j2.trustStoreLocation";
    private static final String trustStorePassword = "log4j2.trustStorePassword";
    private static final String trustStorePasswordFile = "log4j2.trustStorePasswordFile";
    private static final String trustStorePasswordEnvVar = "log4j2.trustStorePasswordEnvironmentVariable";
    private static final String trustStoreKeyStoreType = "log4j2.trustStoreKeyStoreType";
    private static final String trustStoreKeyManagerFactoryAlgorithm = "log4j2.trustStoreKeyManagerFactoryAlgorithm";
    private static final String keyStoreLocation = "log4j2.keyStoreLocation";
    private static final String keyStorePassword = "log4j2.keyStorePassword";
    private static final String keyStorePasswordFile = "log4j2.keyStorePasswordFile";
    private static final String keyStorePasswordEnvVar = "log4j2.keyStorePasswordEnvironmentVariable";
    private static final String keyStoreType = "log4j2.keyStoreType";
    private static final String keyStoreKeyManagerFactoryAlgorithm = "log4j2.keyStoreKeyManagerFactoryAlgorithm";
    private static final String verifyHostName = "log4j2.sslVerifyHostName";

    public static SslConfiguration getSslConfiguration() {
        return sslConfiguration;
    }

    static {
        char[] cArray;
        String string;
        LOGGER = StatusLogger.getLogger();
        sslConfiguration = null;
        PropertiesUtil propertiesUtil = PropertiesUtil.getProperties();
        KeyStoreConfiguration keyStoreConfiguration = null;
        TrustStoreConfiguration trustStoreConfiguration = null;
        String string2 = propertiesUtil.getStringProperty(trustStorelocation);
        if (string2 != null) {
            string = propertiesUtil.getStringProperty(trustStorePassword);
            cArray = null;
            if (string != null) {
                cArray = string.toCharArray();
            }
            try {
                trustStoreConfiguration = TrustStoreConfiguration.createKeyStoreConfiguration(string2, cArray, propertiesUtil.getStringProperty(trustStorePasswordEnvVar), propertiesUtil.getStringProperty(trustStorePasswordFile), propertiesUtil.getStringProperty(trustStoreKeyStoreType), propertiesUtil.getStringProperty(trustStoreKeyManagerFactoryAlgorithm));
            }
            catch (Exception exception) {
                LOGGER.warn("Unable to create trust store configuration due to: {} {}", (Object)exception.getClass().getName(), (Object)exception.getMessage());
            }
        }
        if ((string2 = propertiesUtil.getStringProperty(keyStoreLocation)) != null) {
            string = propertiesUtil.getStringProperty(keyStorePassword);
            cArray = null;
            if (string != null) {
                cArray = string.toCharArray();
            }
            try {
                keyStoreConfiguration = KeyStoreConfiguration.createKeyStoreConfiguration(string2, cArray, propertiesUtil.getStringProperty(keyStorePasswordEnvVar), propertiesUtil.getStringProperty(keyStorePasswordFile), propertiesUtil.getStringProperty(keyStoreType), propertiesUtil.getStringProperty(keyStoreKeyManagerFactoryAlgorithm));
            }
            catch (Exception exception) {
                LOGGER.warn("Unable to create key store configuration due to: {} {}", (Object)exception.getClass().getName(), (Object)exception.getMessage());
            }
        }
        if (trustStoreConfiguration != null || keyStoreConfiguration != null) {
            boolean bl = propertiesUtil.getBooleanProperty(verifyHostName, false);
            sslConfiguration = SslConfiguration.createSSLConfiguration("https", keyStoreConfiguration, trustStoreConfiguration, bl);
        }
    }
}

