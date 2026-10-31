/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.pattern.AbstractPatternConverter;

public abstract class LogEventPatternConverter
extends AbstractPatternConverter {
    protected LogEventPatternConverter(String string, String string2) {
        super(string, string2);
    }

    public abstract void format(LogEvent var1, StringBuilder var2);

    @Override
    public void format(Object object, StringBuilder stringBuilder) {
        if (object instanceof LogEvent) {
            this.format((LogEvent)object, stringBuilder);
        }
    }

    public boolean handlesThrowable() {
        return false;
    }

    public boolean isVariable() {
        return true;
    }
}

