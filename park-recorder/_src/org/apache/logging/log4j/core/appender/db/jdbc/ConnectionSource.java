/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.db.jdbc;

import java.sql.Connection;
import org.apache.logging.log4j.core.LifeCycle;

public interface ConnectionSource
extends LifeCycle {
    public Connection getConnection();

    public String toString();
}

