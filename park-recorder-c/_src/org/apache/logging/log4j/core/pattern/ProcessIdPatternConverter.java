/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.util.ProcessIdUtil;

@Plugin(name="ProcessIdPatternConverter", category="Converter")
@ConverterKeys(value={"pid", "processId"})
public final class ProcessIdPatternConverter
extends LogEventPatternConverter {
    private static final String DEFAULT_DEFAULT_VALUE = "???";
    private final String pid;

    private ProcessIdPatternConverter(String ... object) {
        super("Process ID", "pid");
        object = ((String[])object).length > 0 ? object[0] : DEFAULT_DEFAULT_VALUE;
        String string = ProcessIdUtil.getProcessId();
        this.pid = string.equals("-") ? object : string;
    }

    public final String getProcessId() {
        return this.pid;
    }

    public static void main(String[] stringArray) {
        System.out.println(new ProcessIdPatternConverter((String[])new String[0]).pid);
    }

    public static ProcessIdPatternConverter newInstance(String[] stringArray) {
        return new ProcessIdPatternConverter(stringArray);
    }

    @Override
    public final void format(LogEvent logEvent, StringBuilder stringBuilder) {
        stringBuilder.append(this.pid);
    }
}

