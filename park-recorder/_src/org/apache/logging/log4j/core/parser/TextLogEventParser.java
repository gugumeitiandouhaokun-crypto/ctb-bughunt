/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.parser;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.parser.LogEventParser;

public interface TextLogEventParser
extends LogEventParser {
    public LogEvent parseFrom(String var1);
}

