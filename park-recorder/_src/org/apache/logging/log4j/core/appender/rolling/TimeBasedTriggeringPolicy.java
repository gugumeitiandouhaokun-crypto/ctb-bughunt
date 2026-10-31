/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rolling.AbstractTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.util.Integers;

@Plugin(name="TimeBasedTriggeringPolicy", category="Core", printObject=true)
public final class TimeBasedTriggeringPolicy
extends AbstractTriggeringPolicy {
    private long nextRolloverMillis;
    private final int interval;
    private final boolean modulate;
    private final long maxRandomDelayMillis;
    private RollingFileManager manager;

    private TimeBasedTriggeringPolicy(int n, boolean bl, long l) {
        this.interval = n;
        this.modulate = bl;
        this.maxRandomDelayMillis = l;
    }

    public final int getInterval() {
        return this.interval;
    }

    public final long getNextRolloverMillis() {
        return this.nextRolloverMillis;
    }

    @Override
    public final void initialize(RollingFileManager rollingFileManager) {
        this.manager = rollingFileManager;
        long l = rollingFileManager.getFileTime();
        if (l == 0L) {
            l = System.currentTimeMillis();
        }
        rollingFileManager.getPatternProcessor().getNextTime(l, this.interval, this.modulate);
        rollingFileManager.getPatternProcessor().setTimeBased(true);
        this.nextRolloverMillis = ThreadLocalRandom.current().nextLong(0L, 1L + this.maxRandomDelayMillis) + rollingFileManager.getPatternProcessor().getNextTime(l, this.interval, this.modulate);
    }

    @Override
    public final boolean isTriggeringEvent(LogEvent logEvent) {
        long l = logEvent.getTimeMillis();
        if (l >= this.nextRolloverMillis) {
            this.nextRolloverMillis = ThreadLocalRandom.current().nextLong(0L, 1L + this.maxRandomDelayMillis) + this.manager.getPatternProcessor().getNextTime(l, this.interval, this.modulate);
            this.manager.getPatternProcessor().setCurrentFileTime(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    @Deprecated
    public static TimeBasedTriggeringPolicy createPolicy(@PluginAttribute(value="interval") String string, @PluginAttribute(value="modulate") String string2) {
        return TimeBasedTriggeringPolicy.newBuilder().withInterval(Integers.parseInt(string, 1)).withModulate(Boolean.parseBoolean(string2)).build();
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    public final String toString() {
        return "TimeBasedTriggeringPolicy(nextRolloverMillis=" + this.nextRolloverMillis + ", interval=" + this.interval + ", modulate=" + this.modulate + ")";
    }

    public static class Builder
    implements org.apache.logging.log4j.core.util.Builder<TimeBasedTriggeringPolicy> {
        @PluginBuilderAttribute
        private int interval = 1;
        @PluginBuilderAttribute
        private boolean modulate = false;
        @PluginBuilderAttribute
        private int maxRandomDelay = 0;

        @Override
        public TimeBasedTriggeringPolicy build() {
            long l = TimeUnit.SECONDS.toMillis(this.maxRandomDelay);
            return new TimeBasedTriggeringPolicy(this.interval, this.modulate, l);
        }

        public int getInterval() {
            return this.interval;
        }

        public boolean isModulate() {
            return this.modulate;
        }

        public int getMaxRandomDelay() {
            return this.maxRandomDelay;
        }

        public Builder withInterval(int n) {
            this.interval = n;
            return this;
        }

        public Builder withModulate(boolean bl) {
            this.modulate = bl;
            return this;
        }

        public Builder withMaxRandomDelay(int n) {
            this.maxRandomDelay = n;
            return this;
        }
    }
}

