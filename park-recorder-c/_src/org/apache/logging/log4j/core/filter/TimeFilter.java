/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.filter;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.util.Clock;
import org.apache.logging.log4j.core.util.ClockFactory;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name="TimeFilter", category="Core", elementType="filter", printObject=true)
@PerformanceSensitive(value={"allocation"})
public final class TimeFilter
extends AbstractFilter {
    private static final Clock CLOCK = ClockFactory.getClock();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final long HOUR_MS = 3600000L;
    private static final long DAY_MS = 86400000L;
    private volatile long start;
    private final LocalTime startTime;
    private volatile long end;
    private final LocalTime endTime;
    private final long duration;
    private final ZoneId timeZone;

    TimeFilter(LocalTime localTime, LocalTime localTime2, ZoneId zoneId, Filter.Result result, Filter.Result result2, LocalDate localDate) {
        super(result, result2);
        this.startTime = localTime;
        this.endTime = localTime2;
        this.timeZone = zoneId;
        this.start = ZonedDateTime.of(localDate, this.startTime, zoneId).withEarlierOffsetAtOverlap().toInstant().toEpochMilli();
        long l = ZonedDateTime.of(localDate, this.endTime, zoneId).withEarlierOffsetAtOverlap().toInstant().toEpochMilli();
        if (localTime2.isBefore(localTime)) {
            l += 86400000L;
        }
        this.duration = this.startTime.isBefore(this.endTime) ? Duration.between(this.startTime, this.endTime).toMillis() : Duration.between(this.startTime, this.endTime).plusHours(24L).toMillis();
        long l2 = l - this.start - this.duration;
        if (l2 != 0L) {
            l -= l2;
        }
        this.end = l;
    }

    private TimeFilter(LocalTime localTime, LocalTime localTime2, ZoneId zoneId, Filter.Result result, Filter.Result result2) {
        this(localTime, localTime2, zoneId, result, result2, LocalDate.now(zoneId));
    }

    private synchronized void adjustTimes(long l) {
        long l2;
        if (l <= this.end) {
            return;
        }
        LocalDate localDate = Instant.ofEpochMilli(l).atZone(this.timeZone).toLocalDate();
        this.start = ZonedDateTime.of(localDate, this.startTime, this.timeZone).withEarlierOffsetAtOverlap().toInstant().toEpochMilli();
        long l3 = ZonedDateTime.of(localDate, this.endTime, this.timeZone).withEarlierOffsetAtOverlap().toInstant().toEpochMilli();
        if (this.endTime.isBefore(this.startTime)) {
            l3 += 86400000L;
        }
        if ((l2 = l3 - this.start - this.duration) != 0L) {
            l3 -= l2;
        }
        this.end = l3;
    }

    final Filter.Result filter(long l) {
        if (l > this.end) {
            this.adjustTimes(l);
        }
        if (l >= this.start && l <= this.end) {
            return this.onMatch;
        }
        return this.onMismatch;
    }

    @Override
    public final Filter.Result filter(LogEvent logEvent) {
        return this.filter(logEvent.getTimeMillis());
    }

    private Filter.Result filter() {
        return this.filter(CLOCK.currentTimeMillis());
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
        return this.filter();
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, Object object, Throwable throwable) {
        return this.filter();
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object ... objectArray) {
        return this.filter();
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object) {
        return this.filter();
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2) {
        return this.filter();
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3) {
        return this.filter();
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        return this.filter();
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        return this.filter();
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        return this.filter();
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        return this.filter();
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        return this.filter();
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        return this.filter();
    }

    @Override
    public final Filter.Result filter(Logger logger, Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        return this.filter();
    }

    @Override
    public final String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("start=").append(this.start);
        stringBuilder.append(", end=").append(this.end);
        stringBuilder.append(", timezone=").append(this.timeZone.toString());
        return stringBuilder.toString();
    }

    @PluginFactory
    public static TimeFilter createFilter(@PluginAttribute(value="start") String object, @PluginAttribute(value="end") String object2, @PluginAttribute(value="timezone") String object3, @PluginAttribute(value="onMatch") Filter.Result result, @PluginAttribute(value="onMismatch") Filter.Result result2) {
        object = TimeFilter.parseTimestamp((String)object, LocalTime.MIN);
        object2 = TimeFilter.parseTimestamp((String)object2, LocalTime.MAX);
        object3 = object3 == null ? ZoneId.systemDefault() : ZoneId.of((String)object3);
        result = result == null ? Filter.Result.NEUTRAL : result;
        result2 = result2 == null ? Filter.Result.DENY : result2;
        return new TimeFilter((LocalTime)object, (LocalTime)object2, (ZoneId)object3, result, result2);
    }

    private static LocalTime parseTimestamp(String string, LocalTime localTime) {
        if (string == null) {
            return localTime;
        }
        try {
            return LocalTime.parse(string, FORMATTER);
        }
        catch (Exception exception) {
            LOGGER.warn("Error parsing TimeFilter timestamp value {}", (Object)string, (Object)exception);
            return localTime;
        }
    }
}

