/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net;

public enum Protocol {
    TCP,
    SSL,
    UDP;


    public final boolean isEqual(String string) {
        return this.name().equalsIgnoreCase(string);
    }
}

