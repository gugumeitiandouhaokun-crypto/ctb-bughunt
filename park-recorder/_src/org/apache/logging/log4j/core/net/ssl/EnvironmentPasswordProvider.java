/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net.ssl;

import java.util.Objects;
import org.apache.logging.log4j.core.net.ssl.PasswordProvider;

class EnvironmentPasswordProvider
implements PasswordProvider {
    private final String passwordEnvironmentVariable;

    public EnvironmentPasswordProvider(String string) {
        this.passwordEnvironmentVariable = Objects.requireNonNull(string, "passwordEnvironmentVariable");
    }

    @Override
    public char[] getPassword() {
        String string = System.getenv(this.passwordEnvironmentVariable);
        if (string == null) {
            return null;
        }
        return string.toCharArray();
    }
}

