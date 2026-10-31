/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ArrayPatternConverter;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.core.time.MutableInstant;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.datetime.FastDateFormat;
import org.apache.logging.log4j.core.util.datetime.FixedDateFormat;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name="DatePatternConverter", category="Converter")
@ConverterKeys(value={"d", "date"})
@PerformanceSensitive(value={"allocation"})
public final class DatePatternConverter
extends LogEventPatternConverter
implements ArrayPatternConverter {
    private static final String UNIX_FORMAT = "UNIX";
    private static final String UNIX_MILLIS_FORMAT = "UNIX_MILLIS";
    private final String[] options;
    private final ThreadLocal<MutableInstant> threadLocalMutableInstant = new ThreadLocal();
    private final ThreadLocal<Formatter> threadLocalFormatter = new ThreadLocal();
    private final AtomicReference<CachedTime> cachedTime;
    private final Formatter formatter;

    private DatePatternConverter(String[] stringArray) {
        super("Date", "date");
        this.options = stringArray == null ? null : Arrays.copyOf(stringArray, stringArray.length);
        this.formatter = this.createFormatter(stringArray);
        this.cachedTime = new AtomicReference<CachedTime>(this.fromEpochMillis(System.currentTimeMillis()));
    }

    private CachedTime fromEpochMillis(long l) {
        MutableInstant mutableInstant = new MutableInstant();
        mutableInstant.initFromEpochMilli(l, 0);
        return new CachedTime(mutableInstant);
    }

    private Formatter createFormatter(String[] stringArray) {
        FixedDateFormat fixedDateFormat = FixedDateFormat.createIfSupported(stringArray);
        if (fixedDateFormat != null) {
            return DatePatternConverter.createFixedFormatter(fixedDateFormat);
        }
        return DatePatternConverter.createNonFixedFormatter(stringArray);
    }

    public static DatePatternConverter newInstance(String[] stringArray) {
        return new DatePatternConverter(stringArray);
    }

    private static Formatter createFixedFormatter(FixedDateFormat fixedDateFormat) {
        return new FixedFormatter(fixedDateFormat);
    }

    private static Formatter createNonFixedFormatter(String[] object) {
        Objects.requireNonNull(object);
        if (((String[])object).length == 0) {
            throw new IllegalArgumentException("Options array must have at least one element");
        }
        Objects.requireNonNull(object[0]);
        String string = object[0];
        if (UNIX_FORMAT.equals(string)) {
            return new UnixFormatter();
        }
        if (UNIX_MILLIS_FORMAT.equals(string)) {
            return new UnixMillisFormatter();
        }
        Object object2 = FixedDateFormat.FixedFormat.lookup(string);
        string = object2 == null ? string : object2.getPattern();
        object2 = null;
        if (((String[])object).length > 1 && object[1] != null) {
            object2 = TimeZone.getTimeZone(object[1]);
        }
        try {
            object = FastDateFormat.getInstance(string, (TimeZone)object2);
            return new PatternFormatter((FastDateFormat)object);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            LOGGER.warn("Could not instantiate FastDateFormat with pattern ".concat(String.valueOf(string)), (Throwable)illegalArgumentException);
            return DatePatternConverter.createFixedFormatter(FixedDateFormat.create(FixedDateFormat.FixedFormat.DEFAULT, (TimeZone)object2));
        }
    }

    public final void format(Date date, StringBuilder stringBuilder) {
        this.format(date.getTime(), stringBuilder);
    }

    @Override
    public final void format(LogEvent logEvent, StringBuilder stringBuilder) {
        this.format(logEvent.getInstant(), stringBuilder);
    }

    public final void format(long l, StringBuilder stringBuilder) {
        MutableInstant mutableInstant = this.getMutableInstant();
        mutableInstant.initFromEpochMilli(l, 0);
        this.format(mutableInstant, stringBuilder);
    }

    private MutableInstant getMutableInstant() {
        if (Constants.ENABLE_THREADLOCALS) {
            MutableInstant mutableInstant = this.threadLocalMutableInstant.get();
            if (mutableInstant == null) {
                mutableInstant = new MutableInstant();
                this.threadLocalMutableInstant.set(mutableInstant);
            }
            return mutableInstant;
        }
        return new MutableInstant();
    }

    public final void format(Instant instant, StringBuilder stringBuilder) {
        if (Constants.ENABLE_THREADLOCALS) {
            this.formatWithoutAllocation(instant, stringBuilder);
            return;
        }
        this.formatWithoutThreadLocals(instant, stringBuilder);
    }

    private void formatWithoutAllocation(Instant instant, StringBuilder stringBuilder) {
        this.getThreadLocalFormatter().formatToBuffer(instant, stringBuilder);
    }

    private Formatter getThreadLocalFormatter() {
        Formatter formatter = this.threadLocalFormatter.get();
        if (formatter == null) {
            DatePatternConverter datePatternConverter = this;
            formatter = datePatternConverter.createFormatter(datePatternConverter.options);
            this.threadLocalFormatter.set(formatter);
        }
        return formatter;
    }

    private void formatWithoutThreadLocals(Instant object, StringBuilder stringBuilder) {
        Object object2 = this.cachedTime.get();
        if (object.getEpochSecond() != ((CachedTime)object2).epochSecond || object.getNanoOfSecond() != ((CachedTime)object2).nanoOfSecond) {
            object2 = this.cachedTime.compareAndSet((CachedTime)object2, (CachedTime)(object = new CachedTime((Instant)object))) ? object : this.cachedTime.get();
        }
        stringBuilder.append(((CachedTime)object2).formatted);
    }

    @Override
    public final void format(Object object, StringBuilder stringBuilder) {
        if (object instanceof Date) {
            this.format((Date)object, stringBuilder);
        }
        super.format(object, stringBuilder);
    }

    @Override
    public final void format(StringBuilder stringBuilder, Object ... objectArray) {
        for (Object object : objectArray) {
            if (!(object instanceof Date)) continue;
            this.format(object, stringBuilder);
            return;
        }
    }

    public final String getPattern() {
        return this.formatter.toPattern();
    }

    final class CachedTime {
        public long epochSecond;
        public int nanoOfSecond;
        public String formatted;

        public CachedTime(Instant instant) {
            this.epochSecond = instant.getEpochSecond();
            this.nanoOfSecond = instant.getNanoOfSecond();
            this.formatted = DatePatternConverter.this.formatter.format(instant);
        }
    }

    static final class UnixMillisFormatter
    extends Formatter {
        private UnixMillisFormatter() {
        }

        @Override
        final String format(Instant instant) {
            return Long.toString(instant.getEpochMillisecond());
        }

        @Override
        final void formatToBuffer(Instant instant, StringBuilder stringBuilder) {
            stringBuilder.append(instant.getEpochMillisecond());
        }
    }

    static final class UnixFormatter
    extends Formatter {
        private UnixFormatter() {
        }

        @Override
        final String format(Instant instant) {
            return Long.toString(instant.getEpochSecond());
        }

        @Override
        final void formatToBuffer(Instant instant, StringBuilder stringBuilder) {
            stringBuilder.append(instant.getEpochSecond());
        }
    }

    static final class FixedFormatter
    extends Formatter {
        private final FixedDateFormat fixedDateFormat;
        private final char[] cachedBuffer = new char[70];
        private int length = 0;

        FixedFormatter(FixedDateFormat fixedDateFormat) {
            this.fixedDateFormat = fixedDateFormat;
        }

        @Override
        final String format(Instant instant) {
            return this.fixedDateFormat.formatInstant(instant);
        }

        @Override
        final void formatToBuffer(Instant instant, StringBuilder stringBuilder) {
            long l = instant.getEpochSecond();
            int n = instant.getNanoOfSecond();
            if (this.previousTime != l || this.nanos != n) {
                this.length = this.fixedDateFormat.formatInstant(instant, this.cachedBuffer, 0);
                this.previousTime = l;
                this.nanos = n;
            }
            stringBuilder.append(this.cachedBuffer, 0, this.length);
        }

        @Override
        public final String toPattern() {
            return this.fixedDateFormat.getFormat();
        }
    }

    static final class PatternFormatter
    extends Formatter {
        private final FastDateFormat fastDateFormat;
        private final StringBuilder cachedBuffer = new StringBuilder(64);

        PatternFormatter(FastDateFormat fastDateFormat) {
            this.fastDateFormat = fastDateFormat;
        }

        @Override
        final String format(Instant instant) {
            return this.fastDateFormat.format(instant.getEpochMillisecond());
        }

        @Override
        final void formatToBuffer(Instant instant, StringBuilder stringBuilder) {
            long l = instant.getEpochMillisecond();
            if (this.previousTime != l) {
                this.cachedBuffer.setLength(0);
                this.fastDateFormat.format(l, this.cachedBuffer);
            }
            stringBuilder.append((CharSequence)this.cachedBuffer);
        }

        @Override
        public final String toPattern() {
            return this.fastDateFormat.getPattern();
        }
    }

    static abstract class Formatter {
        long previousTime;
        int nanos;

        private Formatter() {
        }

        abstract String format(Instant var1);

        abstract void formatToBuffer(Instant var1, StringBuilder var2);

        public String toPattern() {
            return null;
        }
    }
}

