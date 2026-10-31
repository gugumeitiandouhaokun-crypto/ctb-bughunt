/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util.datetime;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.core.util.datetime.FastDateFormat;

public class FixedDateFormat {
    private static final char NONE = '\u0000';
    private final FixedFormat fixedFormat;
    private final TimeZone timeZone;
    private final int length;
    private final int secondFractionDigits;
    private final FastDateFormat fastDateFormat;
    private final char timeSeparatorChar;
    private final char millisSeparatorChar;
    private final int timeSeparatorLength;
    private final int millisSeparatorLength;
    private final FixedTimeZoneFormat fixedTimeZoneFormat;
    private volatile long midnightToday = 0L;
    private volatile long midnightTomorrow = 0L;
    private final int[] dstOffsets = new int[25];
    private char[] cachedDate;
    private int dateLength;
    static int[] TABLE = new int[]{100000, 10000, 1000, 100, 10, 1};

    FixedDateFormat(FixedFormat fixedFormat, TimeZone timeZone) {
        this(fixedFormat, timeZone, fixedFormat.getSecondFractionDigits());
    }

    FixedDateFormat(FixedFormat fixedFormat, TimeZone timeZone, int n) {
        this.fixedFormat = Objects.requireNonNull(fixedFormat);
        this.timeZone = Objects.requireNonNull(timeZone);
        this.timeSeparatorChar = fixedFormat.timeSeparatorChar;
        this.timeSeparatorLength = fixedFormat.timeSeparatorLength;
        this.millisSeparatorChar = fixedFormat.millisSeparatorChar;
        this.millisSeparatorLength = fixedFormat.millisSeparatorLength;
        this.fixedTimeZoneFormat = fixedFormat.fixedTimeZoneFormat;
        this.length = fixedFormat.getLength();
        this.secondFractionDigits = Math.max(1, Math.min(9, n));
        this.fastDateFormat = fixedFormat.getFastDateFormat(timeZone);
    }

    public static FixedDateFormat createIfSupported(String ... object) {
        if (object == null || ((Object)object).length == 0 || object[0] == null) {
            return new FixedDateFormat(FixedFormat.DEFAULT, TimeZone.getDefault());
        }
        TimeZone timeZone = ((Object)object).length > 1 ? (object[1] != null ? TimeZone.getTimeZone((String)object[1]) : TimeZone.getDefault()) : TimeZone.getDefault();
        Object object2 = object[0];
        FixedFormat fixedFormat = FixedFormat.lookupIgnoringNanos((String)object2);
        if (fixedFormat != null) {
            int[] nArray = FixedFormat.nanoRange((String)object2);
            int n = nArray[0];
            int n2 = nArray[1] - n;
            return new FixedDateFormat(fixedFormat, timeZone, n2);
        }
        FixedFormat fixedFormat2 = FixedFormat.lookup((String)object2);
        if (fixedFormat2 == null) {
            return null;
        }
        return new FixedDateFormat(fixedFormat2, timeZone);
    }

    public static FixedDateFormat create(FixedFormat fixedFormat) {
        return new FixedDateFormat(fixedFormat, TimeZone.getDefault());
    }

    public static FixedDateFormat create(FixedFormat fixedFormat, TimeZone timeZone) {
        return new FixedDateFormat(fixedFormat, timeZone != null ? timeZone : TimeZone.getDefault());
    }

    public String getFormat() {
        return this.fixedFormat.getPattern();
    }

    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    public long millisSinceMidnight(long l) {
        if (l >= this.midnightTomorrow || l < this.midnightToday) {
            this.updateMidnightMillis(l);
        }
        return l - this.midnightToday;
    }

    private void updateMidnightMillis(long l) {
        if (l >= this.midnightTomorrow || l < this.midnightToday) {
            FixedDateFormat fixedDateFormat = this;
            synchronized (fixedDateFormat) {
                this.updateCachedDate(l);
                this.midnightToday = this.calcMidnightMillis(l, 0);
                this.midnightTomorrow = this.calcMidnightMillis(l, 1);
                this.updateDaylightSavingTime();
                return;
            }
        }
    }

    private long calcMidnightMillis(long l, int n) {
        Calendar calendar = Calendar.getInstance(this.timeZone);
        calendar.setTimeInMillis(l);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        calendar.add(5, n);
        return calendar.getTimeInMillis();
    }

    private void updateDaylightSavingTime() {
        Arrays.fill(this.dstOffsets, 0);
        int n = (int)TimeUnit.HOURS.toMillis(1L);
        if (this.timeZone.getOffset(this.midnightToday) != this.timeZone.getOffset(this.midnightToday + (long)(23 * n))) {
            int n2;
            for (n2 = 0; n2 < this.dstOffsets.length; ++n2) {
                long l = this.midnightToday + (long)(n2 * n);
                this.dstOffsets[n2] = this.timeZone.getOffset(l) - this.timeZone.getRawOffset();
            }
            if (this.dstOffsets[0] > this.dstOffsets[23]) {
                n2 = this.dstOffsets.length - 1;
                while (n2 >= 0) {
                    int n3 = n2--;
                    this.dstOffsets[n3] = this.dstOffsets[n3] - this.dstOffsets[0];
                }
            }
        }
    }

    private void updateCachedDate(long l) {
        if (this.fastDateFormat != null) {
            StringBuilder stringBuilder = this.fastDateFormat.format(l, new StringBuilder());
            this.cachedDate = stringBuilder.toString().toCharArray();
            this.dateLength = stringBuilder.length();
        }
    }

    public String formatInstant(Instant instant) {
        char[] cArray = new char[this.length << 1];
        int n = this.formatInstant(instant, cArray, 0);
        return new String(cArray, 0, n);
    }

    public int formatInstant(Instant instant, char[] cArray, int n) {
        long l = instant.getEpochMillisecond();
        int n2 = this.format(l, cArray, n) - this.digitsLessThanThree();
        int n3 = this.formatNanoOfMillisecond(instant.getNanoOfMillisecond(), cArray, n + n2);
        return this.writeTimeZone(l, cArray, n3);
    }

    private int digitsLessThanThree() {
        return Math.max(0, FixedFormat.MILLI_FRACTION_DIGITS - this.secondFractionDigits);
    }

    public String format(long l) {
        char[] cArray = new char[this.length << 1];
        int n = this.format(l, cArray, 0);
        return new String(cArray, 0, n);
    }

    public int format(long l, char[] cArray, int n) {
        int n2 = (int)this.millisSinceMidnight(l);
        this.writeDate(cArray, n);
        return this.writeTime(n2, cArray, n + this.dateLength) - n;
    }

    private void writeDate(char[] cArray, int n) {
        if (this.cachedDate != null) {
            System.arraycopy(this.cachedDate, 0, cArray, n, this.dateLength);
        }
    }

    private int writeTime(int n, char[] cArray, int n2) {
        int n3 = n / 3600000;
        int n4 = n3 + this.daylightSavingTime(n3) / 3600000;
        n -= 3600000 * n3;
        n3 = n / 60000;
        int n5 = (n -= 60000 * n3) / 1000;
        n -= 1000 * n5;
        int n6 = n4 / 10;
        cArray[n2++] = (char)(n6 + 48);
        cArray[n2++] = (char)(n4 - 10 * n6 + 48);
        cArray[n2] = this.timeSeparatorChar;
        n2 += this.timeSeparatorLength;
        n6 = n3 / 10;
        cArray[n2++] = (char)(n6 + 48);
        cArray[n2++] = (char)(n3 - 10 * n6 + 48);
        cArray[n2] = this.timeSeparatorChar;
        n2 += this.timeSeparatorLength;
        n6 = n5 / 10;
        cArray[n2++] = (char)(n6 + 48);
        cArray[n2++] = (char)(n5 - 10 * n6 + 48);
        cArray[n2] = this.millisSeparatorChar;
        n2 += this.millisSeparatorLength;
        n6 = n / 100;
        cArray[n2++] = (char)(n6 + 48);
        n -= 100 * n6;
        n6 = n / 10;
        cArray[n2++] = (char)(n6 + 48);
        cArray[n2++] = (char)((n -= 10 * n6) + 48);
        return n2;
    }

    private int writeTimeZone(long l, char[] cArray, int n) {
        if (this.fixedTimeZoneFormat != null) {
            n = this.fixedTimeZoneFormat.write(this.timeZone.getOffset(l), cArray, n);
        }
        return n;
    }

    private int formatNanoOfMillisecond(int n, char[] cArray, int n2) {
        int n3 = n;
        for (int i2 = 0; i2 < this.secondFractionDigits - FixedFormat.MILLI_FRACTION_DIGITS; ++i2) {
            int n4 = TABLE[i2];
            n = n3 / n4;
            cArray[n2++] = (char)(n + 48);
            n3 -= n4 * n;
        }
        return n2;
    }

    private int daylightSavingTime(int n) {
        if (n > 23) {
            return this.dstOffsets[23];
        }
        return this.dstOffsets[n];
    }

    public static enum FixedTimeZoneFormat {
        HH('\u0000', false, 3),
        HHMM('\u0000', true, 5),
        HHCMM(':', true, 6);

        private final char timeSeparatorChar;
        private final int timeSeparatorCharLen;
        private final boolean useMinutes;
        private final int length;

        private FixedTimeZoneFormat() {
            this('\u0000', true, 4);
        }

        private FixedTimeZoneFormat(char c2, boolean bl, int n2) {
            this.timeSeparatorChar = c2;
            this.timeSeparatorCharLen = c2 != '\u0000' ? 1 : 0;
            this.useMinutes = bl;
            this.length = n2;
        }

        public final int getLength() {
            return this.length;
        }

        private int write(int n, char[] cArray, int n2) {
            cArray[n2++] = n < 0 ? 45 : 43;
            n = Math.abs(n);
            int n3 = n / 3600000;
            n -= 3600000 * n3;
            int n4 = n3 / 10;
            cArray[n2++] = (char)(n4 + 48);
            cArray[n2++] = (char)(n3 - 10 * n4 + 48);
            if (this.useMinutes) {
                cArray[n2] = this.timeSeparatorChar;
                n2 += this.timeSeparatorCharLen;
                n4 = (n /= 60000) / 10;
                cArray[n2++] = (char)(n4 + 48);
                cArray[n2++] = (char)(n - 10 * n4 + 48);
            }
            return n2;
        }
    }

    public static enum FixedFormat {
        ABSOLUTE("HH:mm:ss,SSS", null, 0, ':', 1, ',', 1, 3, null),
        ABSOLUTE_MICROS("HH:mm:ss,nnnnnn", null, 0, ':', 1, ',', 1, 6, null),
        ABSOLUTE_NANOS("HH:mm:ss,nnnnnnnnn", null, 0, ':', 1, ',', 1, 9, null),
        ABSOLUTE_PERIOD("HH:mm:ss.SSS", null, 0, ':', 1, '.', 1, 3, null),
        COMPACT("yyyyMMddHHmmssSSS", "yyyyMMdd", 0, ' ', 0, ' ', 0, 3, null),
        DATE("dd MMM yyyy HH:mm:ss,SSS", "dd MMM yyyy ", 0, ':', 1, ',', 1, 3, null),
        DATE_PERIOD("dd MMM yyyy HH:mm:ss.SSS", "dd MMM yyyy ", 0, ':', 1, '.', 1, 3, null),
        DEFAULT("yyyy-MM-dd HH:mm:ss,SSS", "yyyy-MM-dd ", 0, ':', 1, ',', 1, 3, null),
        DEFAULT_MICROS("yyyy-MM-dd HH:mm:ss,nnnnnn", "yyyy-MM-dd ", 0, ':', 1, ',', 1, 6, null),
        DEFAULT_NANOS("yyyy-MM-dd HH:mm:ss,nnnnnnnnn", "yyyy-MM-dd ", 0, ':', 1, ',', 1, 9, null),
        DEFAULT_PERIOD("yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd ", 0, ':', 1, '.', 1, 3, null),
        ISO8601_BASIC("yyyyMMdd'T'HHmmss,SSS", "yyyyMMdd'T'", 2, ' ', 0, ',', 1, 3, null),
        ISO8601_BASIC_PERIOD("yyyyMMdd'T'HHmmss.SSS", "yyyyMMdd'T'", 2, ' ', 0, '.', 1, 3, null),
        ISO8601("yyyy-MM-dd'T'HH:mm:ss,SSS", "yyyy-MM-dd'T'", 2, ':', 1, ',', 1, 3, null),
        ISO8601_OFFSET_DATE_TIME_HH("yyyy-MM-dd'T'HH:mm:ss,SSSX", "yyyy-MM-dd'T'", 2, ':', 1, ',', 1, 3, FixedTimeZoneFormat.HH),
        ISO8601_OFFSET_DATE_TIME_HHMM("yyyy-MM-dd'T'HH:mm:ss,SSSXX", "yyyy-MM-dd'T'", 2, ':', 1, ',', 1, 3, FixedTimeZoneFormat.HHMM),
        ISO8601_OFFSET_DATE_TIME_HHCMM("yyyy-MM-dd'T'HH:mm:ss,SSSXXX", "yyyy-MM-dd'T'", 2, ':', 1, ',', 1, 3, FixedTimeZoneFormat.HHCMM),
        ISO8601_PERIOD("yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'", 2, ':', 1, '.', 1, 3, null),
        ISO8601_PERIOD_MICROS("yyyy-MM-dd'T'HH:mm:ss.nnnnnn", "yyyy-MM-dd'T'", 2, ':', 1, '.', 1, 6, null);

        private static final String DEFAULT_SECOND_FRACTION_PATTERN = "SSS";
        private static final int MILLI_FRACTION_DIGITS;
        private static final char SECOND_FRACTION_PATTERN = 'n';
        private final String pattern;
        private final String datePattern;
        private final int escapeCount;
        private final char timeSeparatorChar;
        private final int timeSeparatorLength;
        private final char millisSeparatorChar;
        private final int millisSeparatorLength;
        private final int secondFractionDigits;
        private final FixedTimeZoneFormat fixedTimeZoneFormat;
        private static final int[] EMPTY_RANGE;

        private FixedFormat(String string2, String string3, int n2, char c2, int n3, char c3, int n4, int n5, FixedTimeZoneFormat fixedTimeZoneFormat) {
            this.timeSeparatorChar = c2;
            this.timeSeparatorLength = n3;
            this.millisSeparatorChar = c3;
            this.millisSeparatorLength = n4;
            this.pattern = Objects.requireNonNull(string2);
            this.datePattern = string3;
            this.escapeCount = n2;
            this.secondFractionDigits = n5;
            this.fixedTimeZoneFormat = fixedTimeZoneFormat;
        }

        public final String getPattern() {
            return this.pattern;
        }

        public final String getDatePattern() {
            return this.datePattern;
        }

        public static FixedFormat lookup(String string) {
            for (FixedFormat fixedFormat : FixedFormat.values()) {
                if (!fixedFormat.name().equals(string) && !fixedFormat.getPattern().equals(string)) continue;
                return fixedFormat;
            }
            return null;
        }

        static FixedFormat lookupIgnoringNanos(String string) {
            int[] nArray = FixedFormat.nanoRange(string);
            int n = nArray[0];
            int n2 = nArray[1];
            if (n > 0) {
                string = string.substring(0, n) + DEFAULT_SECOND_FRACTION_PATTERN + string.substring(n2, string.length());
                for (FixedFormat fixedFormat : FixedFormat.values()) {
                    if (!fixedFormat.getPattern().equals(string)) continue;
                    return fixedFormat;
                }
            }
            return null;
        }

        private static int[] nanoRange(String string) {
            int n = string.indexOf(110);
            int n2 = -1;
            if (n >= 0) {
                n2 = string.indexOf(90, n);
                n2 = n2 < 0 ? string.indexOf(88, n) : n2;
                n2 = n2 < 0 ? string.length() : n2;
                for (int i2 = n + 1; i2 < n2; ++i2) {
                    if (string.charAt(i2) == 'n') continue;
                    return EMPTY_RANGE;
                }
            }
            return new int[]{n, n2};
        }

        public final int getLength() {
            return this.pattern.length() - this.escapeCount;
        }

        public final int getDatePatternLength() {
            if (this.getDatePattern() == null) {
                return 0;
            }
            return this.getDatePattern().length() - this.escapeCount;
        }

        public final FastDateFormat getFastDateFormat() {
            return this.getFastDateFormat(null);
        }

        public final FastDateFormat getFastDateFormat(TimeZone timeZone) {
            if (this.getDatePattern() == null) {
                return null;
            }
            return FastDateFormat.getInstance(this.getDatePattern(), timeZone);
        }

        public final int getSecondFractionDigits() {
            return this.secondFractionDigits;
        }

        public final FixedTimeZoneFormat getFixedTimeZoneFormat() {
            return this.fixedTimeZoneFormat;
        }

        static {
            MILLI_FRACTION_DIGITS = 3;
            EMPTY_RANGE = new int[]{-1, -1};
        }
    }
}

