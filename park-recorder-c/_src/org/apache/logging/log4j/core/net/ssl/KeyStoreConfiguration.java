/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net.ssl;

import java.util.Arrays;
import javax.net.ssl.KeyManagerFactory;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.net.ssl.AbstractKeyStoreConfiguration;
import org.apache.logging.log4j.core.net.ssl.EnvironmentPasswordProvider;
import org.apache.logging.log4j.core.net.ssl.FilePasswordProvider;
import org.apache.logging.log4j.core.net.ssl.MemoryPasswordProvider;
import org.apache.logging.log4j.core.net.ssl.PasswordProvider;
import org.apache.logging.log4j.core.net.ssl.StoreConfigurationException;

@Plugin(name="KeyStore", category="Core", printObject=true)
public class KeyStoreConfiguration
extends AbstractKeyStoreConfiguration {
    private final String keyManagerFactoryAlgorithm;

    public KeyStoreConfiguration(String string, PasswordProvider passwordProvider, String string2, String string3) {
        super(string, passwordProvider, string2);
        this.keyManagerFactoryAlgorithm = string3 == null ? KeyManagerFactory.getDefaultAlgorithm() : string3;
    }

    @Deprecated
    public KeyStoreConfiguration(String string, char[] cArray, String string2, String string3) {
        this(string, new MemoryPasswordProvider(cArray), string2, string3);
        if (cArray != null) {
            Arrays.fill(cArray, '\u0000');
        }
    }

    @Deprecated
    public KeyStoreConfiguration(String string, String string2, String string3, String string4) {
        this(string, new MemoryPasswordProvider(string2 == null ? null : string2.toCharArray()), string3, string4);
    }

    @PluginFactory
    public static KeyStoreConfiguration createKeyStoreConfiguration(@PluginAttribute(value="location") String string, @PluginAttribute(value="password", sensitive=true) char[] cArray, @PluginAttribute(value="passwordEnvironmentVariable") String object, @PluginAttribute(value="passwordFile") String string2, @PluginAttribute(value="type") String string3, @PluginAttribute(value="keyManagerFactoryAlgorithm") String string4) {
        if (cArray != null && object != null && string2 != null) {
            throw new StoreConfigurationException("You MUST set only one of 'password', 'passwordEnvironmentVariable' or 'passwordFile'.");
        }
        try {
            Object object2 = string2 != null ? new FilePasswordProvider(string2) : (object = object != null ? new EnvironmentPasswordProvider((String)object) : new MemoryPasswordProvider(cArray));
            if (cArray != null) {
                Arrays.fill(cArray, '\u0000');
            }
            return new KeyStoreConfiguration(string, (PasswordProvider)object, string3, string4);
        }
        catch (Exception exception) {
            throw new StoreConfigurationException("Could not configure KeyStore", exception);
        }
    }

    @Deprecated
    public static KeyStoreConfiguration createKeyStoreConfiguration(String string, char[] cArray, String string2, String string3) {
        return KeyStoreConfiguration.createKeyStoreConfiguration(string, cArray, null, null, string2, string3);
    }

    @Deprecated
    public static KeyStoreConfiguration createKeyStoreConfiguration(String string, String string2, String string3, String string4) {
        return KeyStoreConfiguration.createKeyStoreConfiguration(string, string2 == null ? null : string2.toCharArray(), string3, string4);
    }

    public KeyManagerFactory initKeyManagerFactory() {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(this.keyManagerFactoryAlgorithm);
        char[] cArray = this.getPasswordAsCharArray();
        try {
            keyManagerFactory.init(this.getKeyStore(), cArray);
        }
        finally {
            if (cArray != null) {
                Arrays.fill(cArray, '\u0000');
            }
        }
        return keyManagerFactory;
    }

    @Override
    public int hashCode() {
        int n = super.hashCode();
        return 31 * n + (this.keyManagerFactoryAlgorithm == null ? 0 : this.keyManagerFactoryAlgorithm.hashCode());
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
        object = (KeyStoreConfiguration)object;
        return !(this.keyManagerFactoryAlgorithm == null ? ((KeyStoreConfiguration)object).keyManagerFactoryAlgorithm != null : !this.keyManagerFactoryAlgorithm.equals(((KeyStoreConfiguration)object).keyManagerFactoryAlgorithm));
    }

    public String getKeyManagerFactoryAlgorithm() {
        return this.keyManagerFactoryAlgorithm;
    }
}

