/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.time;

import java.io.Serializable;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.core.time.PreciseClock;
import org.apache.logging.log4j.core.util.Clock;
import org.apache.logging.log4j.util.PerformanceSensitive;

@PerformanceSensitive(value={"allocation"})
public class MutableInstant
implements Serializable,
Instant {
    private static final int MILLIS_PER_SECOND = 1000;
    private static final int NANOS_PER_MILLI = 1000000;
    private static final int NANOS_PER_SECOND = 1000000000;
    private long epochSecond;
    private int nanoOfSecond;

    @Override
    public long getEpochSecond() {
        return this.epochSecond;
    }

    @Override
    public int getNanoOfSecond() {
        return this.nanoOfSecond;
    }

    @Override
    public long getEpochMillisecond() {
        int n = this.nanoOfSecond / 1000000;
        return this.epochSecond * 1000L + (long)n;
    }

    @Override
    public int getNanoOfMillisecond() {
        int n = this.nanoOfSecond / 1000000;
        return this.nanoOfSecond - n * 1000000;
    }

    public void initFrom(Instant instant) {
        this.epochSecond = instant.getEpochSecond();
        this.nanoOfSecond = instant.getNanoOfSecond();
    }

    public void initFromEpochMilli(long l, int n) {
        this.validateNanoOfMillisecond(n);
        this.epochSecond = l / 1000L;
        this.nanoOfSecond = (int)(l - this.epochSecond * 1000L) * 1000000 + n;
    }

    private void validateNanoOfMillisecond(int n) {
        if (n < 0 || n >= 1000000) {
            throw new IllegalArgumentException("Invalid nanoOfMillisecond ".concat(String.valueOf(n)));
        }
    }

    public void initFrom(Clock clock) {
        if (clock instanceof PreciseClock) {
            ((PreciseClock)clock).init(this);
            return;
        }
        this.initFromEpochMilli(clock.currentTimeMillis(), 0);
    }

    public void initFromEpochSecond(long l, int n) {
        this.validateNanoOfSecond(n);
        this.epochSecond = l;
        this.nanoOfSecond = n;
    }

    private void validateNanoOfSecond(int n) {
        if (n < 0 || n >= 1000000000) {
            throw new IllegalArgumentException("Invalid nanoOfSecond ".concat(String.valueOf(n)));
        }
    }

    public static void instantToMillisAndNanos(long l, int n, long[] lArray) {
        int n2 = n / 1000000;
        lArray[0] = l * 1000L + (long)n2;
        lArray[1] = n - n2 * 1000000;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof MutableInstant)) {
            return false;
        }
        object = (MutableInstant)object;
        return this.epochSecond == ((MutableInstant)object).epochSecond && this.nanoOfSecond == ((MutableInstant)object).nanoOfSecond;
    }

    public int hashCode() {
        int n = 527 + (int)(this.epochSecond ^ this.epochSecond >>> 32);
        return 31 * n + this.nanoOfSecond;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(64);
        this.formatTo(stringBuilder);
        return stringBuilder.toString();
    }

    @Override
    public void formatTo(StringBuilder stringBuilder) {
        stringBuilder.append("MutableInstant[epochSecond=").append(this.epochSecond).append(", nano=").append(this.nanoOfSecond).append("]");
    }
}

