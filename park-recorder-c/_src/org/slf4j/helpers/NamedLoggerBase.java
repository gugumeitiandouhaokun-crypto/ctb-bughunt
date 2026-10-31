/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j.helpers;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class NamedLoggerBase
implements Serializable,
Logger {
    private static final long serialVersionUID = 7535258609338176893L;
    protected String name;

    NamedLoggerBase() {
    }

    public String getName() {
        return this.name;
    }

    protected Object readResolve() {
        return LoggerFactory.getLogger(this.getName());
    }
}

