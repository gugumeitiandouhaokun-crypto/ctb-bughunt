/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net.ssl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.net.ssl.MemoryPasswordProvider;
import org.apache.logging.log4j.core.net.ssl.PasswordProvider;
import org.apache.logging.log4j.core.net.ssl.StoreConfiguration;
import org.apache.logging.log4j.core.net.ssl.StoreConfigurationException;
import org.apache.logging.log4j.core.util.NetUtils;

public class AbstractKeyStoreConfiguration
extends StoreConfiguration<KeyStore> {
    private final KeyStore keyStore;
    private final String keyStoreType;

    public AbstractKeyStoreConfiguration(String string, PasswordProvider passwordProvider, String string2) {
        super(string, passwordProvider);
        this.keyStoreType = string2 == null ? "JKS" : string2;
        this.keyStore = this.load();
    }

    @Deprecated
    public AbstractKeyStoreConfiguration(String string, char[] cArray, String string2) {
        this(string, new MemoryPasswordProvider(cArray), string2);
    }

    @Deprecated
    public AbstractKeyStoreConfiguration(String string, String string2, String string3) {
        this(string, new MemoryPasswordProvider(string2 == null ? null : string2.toCharArray()), string3);
    }

    @Override
    protected KeyStore load() {
        KeyStore keyStore;
        block22: {
            String string = this.getLocation();
            LOGGER.debug("Loading keystore from location {}", (Object)string);
            if (string == null) {
                throw new IOException("The location is null");
            }
            InputStream inputStream = this.openInputStream(string);
            Throwable throwable = null;
            try {
                keyStore = KeyStore.getInstance(this.keyStoreType);
                char[] cArray = this.getPasswordAsCharArray();
                try {
                    keyStore.load(inputStream, cArray);
                }
                finally {
                    if (cArray != null) {
                        Arrays.fill(cArray, '\u0000');
                    }
                }
                LOGGER.debug("KeyStore successfully loaded from location {}", (Object)string);
                if (inputStream == null) break block22;
            }
            catch (Throwable throwable2) {
                try {
                    try {
                        Throwable throwable3 = throwable2;
                        throwable = throwable2;
                        throw throwable3;
                    }
                    catch (Throwable throwable4) {
                        if (inputStream != null) {
                            if (throwable != null) {
                                try {
                                    inputStream.close();
                                }
                                catch (Throwable throwable5) {
                                    throwable.addSuppressed(throwable5);
                                }
                            } else {
                                inputStream.close();
                            }
                        }
                        throw throwable4;
                    }
                }
                catch (CertificateException certificateException) {
                    LOGGER.error("No Provider supports a KeyStoreSpi implementation for the specified type {} for location {}", (Object)this.keyStoreType, (Object)string, (Object)certificateException);
                    throw new StoreConfigurationException(string, certificateException);
                }
                catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                    LOGGER.error("The algorithm used to check the integrity of the keystore cannot be found for location {}", (Object)string, (Object)noSuchAlgorithmException);
                    throw new StoreConfigurationException(string, noSuchAlgorithmException);
                }
                catch (KeyStoreException keyStoreException) {
                    LOGGER.error("KeyStoreException for location {}", (Object)string, (Object)keyStoreException);
                    throw new StoreConfigurationException(string, keyStoreException);
                }
                catch (FileNotFoundException fileNotFoundException) {
                    LOGGER.error("The keystore file {} is not found", (Object)string, (Object)fileNotFoundException);
                    throw new StoreConfigurationException(string, fileNotFoundException);
                }
                catch (IOException iOException) {
                    LOGGER.error("Something is wrong with the format of the keystore or the given password for location", (Object)string, (Object)iOException);
                    throw new StoreConfigurationException(string, iOException);
                }
            }
            inputStream.close();
        }
        return keyStore;
    }

    private InputStream openInputStream(String string) {
        return ConfigurationSource.fromUri(NetUtils.toURI(string)).getInputStream();
    }

    public KeyStore getKeyStore() {
        return this.keyStore;
    }

    @Override
    public int hashCode() {
        int n = super.hashCode();
        n = 31 * n + (this.keyStore == null ? 0 : this.keyStore.hashCode());
        return 31 * n + (this.keyStoreType == null ? 0 : this.keyStoreType.hashCode());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!super.equals(object)) {
            return false;
        }
        if (this.getClass() != object.getClass()) {
            return false;
        }
        object = (AbstractKeyStoreConfiguration)object;
        if (this.keyStore == null ? ((AbstractKeyStoreConfiguration)object).keyStore != null : !this.keyStore.equals(((AbstractKeyStoreConfiguration)object).keyStore)) {
            return false;
        }
        return !(this.keyStoreType == null ? ((AbstractKeyStoreConfiguration)object).keyStoreType != null : !this.keyStoreType.equals(((AbstractKeyStoreConfiguration)object).keyStoreType));
    }

    public String getKeyStoreType() {
        return this.keyStoreType;
    }
}

