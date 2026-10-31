/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.time.internal;

import org.apache.logging.log4j.core.time.MutableInstant;
import org.apache.logging.log4j.core.time.PreciseClock;

public class FixedPreciseClock
implements PreciseClock {
    private final long currentTimeMillis;
    private final int nanosOfMillisecond;

    public FixedPreciseClock() {
        this(0L);
    }

    public FixedPreciseClock(long l) {
        this(l, 0);
    }

    public FixedPreciseClock(long l, int n) {
        this.currentTimeMillis = l;
        this.nanosOfMillisecond = n;
    }

    @Override
    public void init(MutableInstant mutableInstant) {
        mutableInstant.initFromEpochMilli(this.currentTimeMillis, this.nanosOfMillisecond);
    }

    @Override
    public long currentTimeMillis() {
        return this.currentTimeMillis;
    }
}

