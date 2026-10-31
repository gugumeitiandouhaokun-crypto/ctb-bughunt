/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.io.Serializable;
import java.text.DecimalFormat;
import org.apache.logging.log4j.util.StringBuilderFormattable;

public class Timer
implements Serializable,
StringBuilderFormattable {
    private static final long serialVersionUID = 9175191792439630013L;
    private final String name;
    private Status status;
    private long elapsedTime;
    private final int iterations;
    private static long NANO_PER_SECOND = 1000000000L;
    private static long NANO_PER_MINUTE = 60000000000L;
    private static long NANO_PER_HOUR = 3600000000000L;
    private ThreadLocal<Long> startTime = new ThreadLocal<Long>(){

        @Override
        protected Long initialValue() {
            return 0L;
        }
    };

    public Timer(String string) {
        this(string, 0);
    }

    public Timer(String string, int n) {
        this.name = string;
        this.status = Status.Stopped;
        this.iterations = n > 0 ? n : 0;
    }

    public synchronized void start() {
        this.startTime.set(System.nanoTime());
        this.elapsedTime = 0L;
        this.status = Status.Started;
    }

    public synchronized void startOrResume() {
        if (this.status == Status.Stopped) {
            this.start();
            return;
        }
        this.resume();
    }

    public synchronized String stop() {
        this.elapsedTime += System.nanoTime() - this.startTime.get();
        this.startTime.set(0L);
        this.status = Status.Stopped;
        return this.toString();
    }

    public synchronized void pause() {
        this.elapsedTime += System.nanoTime() - this.startTime.get();
        this.startTime.set(0L);
        this.status = Status.Paused;
    }

    public synchronized void resume() {
        this.startTime.set(System.nanoTime());
        this.status = Status.Started;
    }

    public String getName() {
        return this.name;
    }

    public long getElapsedTime() {
        return this.elapsedTime / 1000000L;
    }

    public long getElapsedNanoTime() {
        return this.elapsedTime;
    }

    public Status getStatus() {
        return this.status;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        this.formatTo(stringBuilder);
        return stringBuilder.toString();
    }

    @Override
    public void formatTo(StringBuilder stringBuilder) {
        stringBuilder.append("Timer ").append(this.name);
        switch (this.status) {
            case Started: {
                stringBuilder.append(" started");
                return;
            }
            case Paused: {
                stringBuilder.append(" paused");
                return;
            }
            case Stopped: {
                long l = this.elapsedTime;
                long l2 = l / NANO_PER_HOUR;
                long l3 = (l %= NANO_PER_HOUR) / NANO_PER_MINUTE;
                long l4 = (l %= NANO_PER_MINUTE) / NANO_PER_SECOND;
                l %= NANO_PER_SECOND;
                String string = "";
                if (l2 > 0L) {
                    string = string + l2 + " hours ";
                }
                if (l3 > 0L || l2 > 0L) {
                    string = string + l3 + " minutes ";
                }
                DecimalFormat decimalFormat = new DecimalFormat("#0");
                string = string + decimalFormat.format(l4) + '.';
                decimalFormat = new DecimalFormat("000000000");
                string = string + decimalFormat.format(l) + " seconds";
                stringBuilder.append(" stopped. Elapsed time: ").append(string);
                if (this.iterations <= 0) break;
                l = this.elapsedTime / (long)this.iterations;
                long l5 = l / NANO_PER_HOUR;
                l3 = (l %= NANO_PER_HOUR) / NANO_PER_MINUTE;
                l4 = (l %= NANO_PER_MINUTE) / NANO_PER_SECOND;
                l %= NANO_PER_SECOND;
                string = "";
                if (l5 > 0L) {
                    string = string + l5 + " hours ";
                }
                if (l3 > 0L || l5 > 0L) {
                    string = string + l3 + " minutes ";
                }
                DecimalFormat decimalFormat2 = new DecimalFormat("#0");
                string = string + decimalFormat2.format(l4) + '.';
                decimalFormat2 = new DecimalFormat("000000000");
                string = string + decimalFormat2.format(l) + " seconds";
                stringBuilder.append(" Average per iteration: ").append(string);
                return;
            }
            default: {
                stringBuilder.append(' ').append((Object)this.status);
            }
        }
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Timer)) {
            return false;
        }
        object = (Timer)object;
        if (this.elapsedTime != ((Timer)object).elapsedTime) {
            return false;
        }
        if (this.startTime != ((Timer)object).startTime) {
            return false;
        }
        if (this.name != null ? !this.name.equals(((Timer)object).name) : ((Timer)object).name != null) {
            return false;
        }
        return !(this.status != null ? !this.status.equals((Object)((Timer)object).status) : ((Timer)object).status != null);
    }

    public int hashCode() {
        long l;
        int n = this.name != null ? this.name.hashCode() : 0;
        n = 29 * n + (this.status != null ? this.status.hashCode() : 0);
        long l2 = l = this.startTime.get().longValue();
        n = 29 * n + (int)(l2 ^ l2 >>> 32);
        return 29 * n + (int)(this.elapsedTime ^ this.elapsedTime >>> 32);
    }

    public static enum Status {
        Started,
        Stopped,
        Paused;

    }
}

