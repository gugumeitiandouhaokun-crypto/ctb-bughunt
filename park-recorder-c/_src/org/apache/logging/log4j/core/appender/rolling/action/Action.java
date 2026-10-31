/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling.action;

public interface Action
extends Runnable {
    public boolean execute();

    public void close();

    public boolean isComplete();
}

