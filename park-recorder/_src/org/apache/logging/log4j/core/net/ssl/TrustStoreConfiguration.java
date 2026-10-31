/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net.ssl;

import java.util.Arrays;
import javax.net.ssl.TrustManagerFactory;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.net.ssl.AbstractKeyStoreConfiguration;
import org.apache.logging.log4j.core.net.ssl.EnvironmentPasswordProvider;
import org.apache.logging.log4j.core.net.ssl.FilePasswordProvider;
import org.apache.logging.log4j.core.net.ssl.MemoryPasswordProvider;
import org.apache.logging.log4j.core.net.ssl.PasswordProvider;
import org.apache.logging.log4j.core.net.ssl.StoreConfigurationException;

@Plugin(name="TrustStore", category="Core", printObject=true)
public class TrustStoreConfiguration
extends AbstractKeyStoreConfiguration {
    private final String trustManagerFactoryAlgorithm;

    public TrustStoreConfiguration(String string, PasswordProvider passwordProvider, String string2, String string3) {
        super(string, passwordProvider, string2);
        this.trustManagerFactoryAlgorithm = string3 == null ? TrustManagerFactory.getDefaultAlgorithm() : string3;
    }

    @Deprecated
    public TrustStoreConfiguration(String string, char[] cArray, String string2, String string3) {
        this(string, new MemoryPasswordProvider(cArray), string2, string3);
        if (cArray != null) {
            Arrays.fill(cArray, '\u0000');
        }
    }

    @Deprecated
    public TrustStoreConfiguration(String string, String string2, String string3, String string4) {
        this(string, new MemoryPasswordProvider(string2 == null ? null : string2.toCharArray()), string3, string4);
    }

    @PluginFactory
    public static TrustStoreConfiguration createKeyStoreConfiguration(@PluginAttribute(value="location") String string, @PluginAttribute(value="password", sensitive=true) char[] cArray, @PluginAttribute(value="passwordEnvironmentVariable") String object, @PluginAttribute(value="passwordFile") String string2, @PluginAttribute(value="type") String string3, @PluginAttribute(value="trustManagerFactoryAlgorithm") String string4) {
        if (cArray != null && object != null && string2 != null) {
            throw new IllegalStateException("You MUST set only one of 'password', 'passwordEnvironmentVariable' or 'passwordFile'.");
        }
        try {
            Object object2 = string2 != null ? new FilePasswordProvider(string2) : (object = object != null ? new EnvironmentPasswordProvider((String)object) : new MemoryPasswordProvider(cArray));
            if (cArray != null) {
                Arrays.fill(cArray, '\u0000');
            }
            return new TrustStoreConfiguration(string, (PasswordProvider)object, string3, string4);
        }
        catch (Exception exception) {
            throw new StoreConfigurationException("Could not configure TrustStore", exception);
        }
    }

    @Deprecated
    public static TrustStoreConfiguration createKeyStoreConfiguration(String string, char[] cArray, String string2, String string3) {
        return TrustStoreConfiguration.createKeyStoreConfiguration(string, cArray, null, null, string2, string3);
    }

    @Deprecated
    public static TrustStoreConfiguration createKeyStoreConfiguration(String string, String string2, String string3, String string4) {
        return TrustStoreConfiguration.createKeyStoreConfiguration(string, string2 == null ? null : string2.toCharArray(), null, null, string3, string4);
    }

    public TrustManagerFactory initTrustManagerFactory() {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(this.trustManagerFactoryAlgorithm);
        trustManagerFactory.init(this.getKeyStore());
        return trustManagerFactory;
    }

    @Override
    public int hashCode() {
        int n = super.hashCode();
        return 31 * n + (this.trustManagerFactoryAlgorithm == null ? 0 : this.trustManagerFactoryAlgorithm.hashCode());
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
        object = (TrustStoreConfiguration)object;
        return !(this.trustManagerFactoryAlgorithm == null ? ((TrustStoreConfiguration)object).trustManagerFactoryAlgorithm != null : !this.trustManagerFactoryAlgorithm.equals(((TrustStoreConfiguration)object).trustManagerFactoryAlgorithm));
    }

    public String getTrustManagerFactoryAlgorithm() {
        return this.trustManagerFactoryAlgorithm;
    }
}

