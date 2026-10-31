/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.ThrowableFormatOptions;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.util.StringBuilderWriter;
import org.apache.logging.log4j.util.Strings;

@Plugin(name="ThrowablePatternConverter", category="Converter")
@ConverterKeys(value={"ex", "throwable", "exception"})
public class ThrowablePatternConverter
extends LogEventPatternConverter {
    protected final List<PatternFormatter> formatters;
    private String rawOption;
    private final boolean subShortOption;
    private final boolean nonStandardLineSeparator;
    protected final ThrowableFormatOptions options;

    @Deprecated
    protected ThrowablePatternConverter(String string, String string2, String[] stringArray) {
        this(string, string2, stringArray, null);
    }

    protected ThrowablePatternConverter(String object, String string, String[] object2, Configuration object3) {
        super((String)object, string);
        this.options = ThrowableFormatOptions.newInstance((String[])object2);
        if (object2 != null && ((String[])object2).length > 0) {
            this.rawOption = object2[0];
        }
        if (this.options.getSuffix() != null) {
            object = PatternLayout.createPatternParser((Configuration)object3).parse(this.options.getSuffix());
            boolean bl = false;
            object2 = object.iterator();
            while (object2.hasNext()) {
                if (!((PatternFormatter)object2.next()).handlesThrowable()) continue;
                bl = true;
            }
            if (!bl) {
                this.formatters = object;
            } else {
                object2 = new ArrayList();
                object3 = object.iterator();
                while (object3.hasNext()) {
                    object = (PatternFormatter)object3.next();
                    if (((PatternFormatter)object).handlesThrowable()) continue;
                    object2.add(object);
                }
                this.formatters = object2;
            }
        } else {
            this.formatters = Collections.emptyList();
        }
        this.subShortOption = "short.message".equalsIgnoreCase(this.rawOption) || "short.localizedMessage".equalsIgnoreCase(this.rawOption) || "short.fileName".equalsIgnoreCase(this.rawOption) || "short.lineNumber".equalsIgnoreCase(this.rawOption) || "short.methodName".equalsIgnoreCase(this.rawOption) || "short.className".equalsIgnoreCase(this.rawOption);
        this.nonStandardLineSeparator = !Strings.LINE_SEPARATOR.equals(this.options.getSeparator());
    }

    public static ThrowablePatternConverter newInstance(Configuration configuration, String[] stringArray) {
        return new ThrowablePatternConverter("Throwable", "throwable", stringArray, configuration);
    }

    @Override
    public void format(LogEvent logEvent, StringBuilder stringBuilder) {
        Throwable throwable = logEvent.getThrown();
        if (this.subShortOption) {
            this.formatSubShortOption(throwable, this.getSuffix(logEvent), stringBuilder);
            return;
        }
        if (throwable != null && this.options.anyLines()) {
            this.formatOption(throwable, this.getSuffix(logEvent), stringBuilder);
        }
    }

    private void formatSubShortOption(Throwable throwable, String string, StringBuilder stringBuilder) {
        Object object;
        StackTraceElement stackTraceElement = null;
        if (throwable != null && (object = throwable.getStackTrace()) != null && ((StackTraceElement[])object).length > 0) {
            stackTraceElement = object[0];
        }
        if (throwable != null && stackTraceElement != null) {
            object = "";
            if ("short.className".equalsIgnoreCase(this.rawOption)) {
                object = stackTraceElement.getClassName();
            } else if ("short.methodName".equalsIgnoreCase(this.rawOption)) {
                object = stackTraceElement.getMethodName();
            } else if ("short.lineNumber".equalsIgnoreCase(this.rawOption)) {
                object = String.valueOf(stackTraceElement.getLineNumber());
            } else if ("short.message".equalsIgnoreCase(this.rawOption)) {
                object = throwable.getMessage();
            } else if ("short.localizedMessage".equalsIgnoreCase(this.rawOption)) {
                object = throwable.getLocalizedMessage();
            } else if ("short.fileName".equalsIgnoreCase(this.rawOption)) {
                object = stackTraceElement.getFileName();
            }
            int n = stringBuilder.length();
            if (n > 0 && !Character.isWhitespace(stringBuilder.charAt(n - 1))) {
                stringBuilder.append(' ');
            }
            stringBuilder.append((String)object);
            if (Strings.isNotBlank(string)) {
                stringBuilder.append(' ');
                stringBuilder.append(string);
            }
        }
    }

    private void formatOption(Throwable stringArray, String string, StringBuilder stringBuilder) {
        int n = stringBuilder.length();
        if (n > 0 && !Character.isWhitespace(stringBuilder.charAt(n - 1))) {
            stringBuilder.append(' ');
        }
        if (!this.options.allLines() || this.nonStandardLineSeparator || Strings.isNotBlank(string)) {
            StringWriter stringWriter = new StringWriter();
            stringArray.printStackTrace(new PrintWriter(stringWriter));
            stringArray = stringWriter.toString().split(Strings.LINE_SEPARATOR);
            int n2 = this.options.minLines(stringArray.length) - 1;
            boolean bl = Strings.isNotBlank(string);
            for (int j = 0; j <= n2; ++j) {
                stringBuilder.append(stringArray[j]);
                if (bl) {
                    stringBuilder.append(' ');
                    stringBuilder.append(string);
                }
                if (j >= n2) continue;
                stringBuilder.append(this.options.getSeparator());
            }
            return;
        }
        stringArray.printStackTrace(new PrintWriter(new StringBuilderWriter(stringBuilder)));
    }

    @Override
    public boolean handlesThrowable() {
        return true;
    }

    protected String getSuffix(LogEvent logEvent) {
        if (this.formatters.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        int n = this.formatters.size();
        for (int j = 0; j < n; ++j) {
            this.formatters.get(j).format(logEvent, stringBuilder);
        }
        return stringBuilder.toString();
    }

    public ThrowableFormatOptions getOptions() {
        return this.options;
    }
}

