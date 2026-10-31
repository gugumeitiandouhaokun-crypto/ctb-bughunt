/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net.ssl;

import java.util.Arrays;
import java.util.Objects;
import org.apache.logging.log4j.core.net.ssl.MemoryPasswordProvider;
import org.apache.logging.log4j.core.net.ssl.PasswordProvider;
import org.apache.logging.log4j.status.StatusLogger;

public class StoreConfiguration<T> {
    protected static final StatusLogger LOGGER = StatusLogger.getLogger();
    private String location;
    private PasswordProvider passwordProvider;

    public StoreConfiguration(String string, PasswordProvider passwordProvider) {
        this.location = string;
        this.passwordProvider = Objects.requireNonNull(passwordProvider, "passwordProvider");
    }

    @Deprecated
    public StoreConfiguration(String string, char[] cArray) {
        this(string, new MemoryPasswordProvider(cArray));
    }

    @Deprecated
    public StoreConfiguration(String string, String string2) {
        this(string, new MemoryPasswordProvider(string2 == null ? null : string2.toCharArray()));
    }

    public void clearSecrets() {
        this.location = null;
        this.passwordProvider = null;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String string) {
        this.location = string;
    }

    @Deprecated
    public String getPassword() {
        return String.valueOf(this.passwordProvider.getPassword());
    }

    public char[] getPasswordAsCharArray() {
        return this.passwordProvider.getPassword();
    }

    public void setPassword(char[] cArray) {
        this.passwordProvider = new MemoryPasswordProvider(cArray);
    }

    @Deprecated
    public void setPassword(String string) {
        this.passwordProvider = new MemoryPasswordProvider(string == null ? null : string.toCharArray());
    }

    protected T load() {
        return null;
    }

    public int hashCode() {
        int n = 31 + (this.location == null ? 0 : this.location.hashCode());
        return 31 * n + Arrays.hashCode(this.passwordProvider.getPassword());
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
        object = (StoreConfiguration)object;
        if (this.location == null ? ((StoreConfiguration)object).location != null : !this.location.equals(((StoreConfiguration)object).location)) {
            return false;
        }
        return Arrays.equals(this.passwordProvider.getPassword(), ((StoreConfiguration)object).passwordProvider.getPassword());
    }
}

