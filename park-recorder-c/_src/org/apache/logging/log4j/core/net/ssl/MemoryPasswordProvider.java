/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net.ssl;

import java.util.Arrays;
import org.apache.logging.log4j.core.net.ssl.PasswordProvider;

class MemoryPasswordProvider
implements PasswordProvider {
    private final char[] password;

    public MemoryPasswordProvider(char[] cArray) {
        if (cArray != null) {
            this.password = (char[])cArray.clone();
            return;
        }
        this.password = null;
    }

    @Override
    public char[] getPassword() {
        if (this.password == null) {
            return null;
        }
        return (char[])this.password.clone();
    }

    public void clearSecrets() {
        Arrays.fill(this.password, '\u0000');
    }
}

