/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CronScheduledFuture<V>
implements ScheduledFuture<V> {
    private volatile FutureData futureData;

    public CronScheduledFuture(ScheduledFuture<V> scheduledFuture, Date date) {
        this.futureData = new FutureData(scheduledFuture, date);
    }

    public Date getFireTime() {
        return this.futureData.runDate;
    }

    void reset(ScheduledFuture<?> scheduledFuture, Date date) {
        this.futureData = new FutureData(scheduledFuture, date);
    }

    @Override
    public long getDelay(TimeUnit timeUnit) {
        return this.futureData.scheduledFuture.getDelay(timeUnit);
    }

    @Override
    public int compareTo(Delayed delayed) {
        return this.futureData.scheduledFuture.compareTo(delayed);
    }

    @Override
    public boolean cancel(boolean bl) {
        return this.futureData.scheduledFuture.cancel(bl);
    }

    @Override
    public boolean isCancelled() {
        return this.futureData.scheduledFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return this.futureData.scheduledFuture.isDone();
    }

    @Override
    public V get() {
        return this.futureData.scheduledFuture.get();
    }

    @Override
    public V get(long l, TimeUnit timeUnit) {
        return this.futureData.scheduledFuture.get(l, timeUnit);
    }

    class FutureData {
        private final ScheduledFuture<?> scheduledFuture;
        private final Date runDate;

        FutureData(ScheduledFuture<?> scheduledFuture, Date date) {
            this.scheduledFuture = scheduledFuture;
            this.runDate = date;
        }
    }
}

