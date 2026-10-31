/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;

public final class FileSize {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final long KB = 1024L;
    private static final long MB = 0x100000L;
    private static final long GB = 0x40000000L;
    private static final Pattern VALUE_PATTERN = Pattern.compile("([0-9]+([\\.,][0-9]+)?)\\s*(|K|M|G)B?", 2);

    private FileSize() {
    }

    public static long parse(String string, long l) {
        Object object = VALUE_PATTERN.matcher(string);
        if (((Matcher)object).matches()) {
            try {
                long l2 = NumberFormat.getNumberInstance(Locale.ROOT).parse(((Matcher)object).group(1)).longValue();
                object = ((Matcher)object).group(3);
                if (((String)object).isEmpty()) {
                    return l2;
                }
                if (((String)object).equalsIgnoreCase("K")) {
                    return l2 * 1024L;
                }
                if (((String)object).equalsIgnoreCase("M")) {
                    return l2 * 0x100000L;
                }
                if (((String)object).equalsIgnoreCase("G")) {
                    return l2 * 0x40000000L;
                }
                LOGGER.error("FileSize units not recognized: ".concat(String.valueOf(string)));
                return l;
            }
            catch (ParseException parseException) {
                LOGGER.error("FileSize unable to parse numeric part: ".concat(String.valueOf(string)), (Throwable)parseException);
                return l;
            }
        }
        LOGGER.error("FileSize unable to parse bytes: ".concat(String.valueOf(string)));
        return l;
    }
}

