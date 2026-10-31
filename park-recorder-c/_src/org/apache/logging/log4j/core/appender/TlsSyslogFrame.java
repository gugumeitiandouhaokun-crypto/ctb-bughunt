/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.nio.charset.StandardCharsets;

public class TlsSyslogFrame {
    private final String message;
    private final int byteLength;

    public TlsSyslogFrame(String object) {
        this.message = object;
        object = ((String)object).getBytes(StandardCharsets.UTF_8);
        this.byteLength = ((Object)object).length;
    }

    public String getMessage() {
        return this.message;
    }

    public String toString() {
        return Integer.toString(this.byteLength) + ' ' + this.message;
    }

    public int hashCode() {
        return 31 + (this.message == null ? 0 : this.message.hashCode());
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (!(object instanceof TlsSyslogFrame)) {
            return false;
        }
        object = (TlsSyslogFrame)object;
        return !(this.message == null ? ((TlsSyslogFrame)object).message != null : !this.message.equals(((TlsSyslogFrame)object).message));
    }
}

