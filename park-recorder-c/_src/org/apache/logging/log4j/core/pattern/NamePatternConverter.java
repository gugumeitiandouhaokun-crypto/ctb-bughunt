/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.NameAbbreviator;
import org.apache.logging.log4j.util.PerformanceSensitive;

@PerformanceSensitive(value={"allocation"})
public abstract class NamePatternConverter
extends LogEventPatternConverter {
    private final NameAbbreviator abbreviator;

    protected NamePatternConverter(String string, String string2, String[] stringArray) {
        super(string, string2);
        if (stringArray != null && stringArray.length > 0) {
            this.abbreviator = NameAbbreviator.getAbbreviator(stringArray[0]);
            return;
        }
        this.abbreviator = NameAbbreviator.getDefaultAbbreviator();
    }

    protected final void abbreviate(String string, StringBuilder stringBuilder) {
        this.abbreviator.abbreviate(string, stringBuilder);
    }
}

