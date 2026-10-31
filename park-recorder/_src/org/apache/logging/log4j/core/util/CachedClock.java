/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.util.concurrent.locks.LockSupport;
import org.apache.logging.log4j.core.util.Clock;
import org.apache.logging.log4j.core.util.Log4jThread;

public final class CachedClock
implements Clock {
    private static final int UPDATE_THRESHOLD = 1000;
    private static volatile CachedClock instance;
    private static final Object INSTANCE_LOCK;
    private volatile long millis = System.currentTimeMillis();
    private short count = 0;

    private CachedClock() {
        Log4jThread log4jThread = new Log4jThread(new Runnable(){

            @Override
            public void run() {
                while (true) {
                    long l = System.currentTimeMillis();
                    CachedClock.this.millis = l;
                    LockSupport.parkNanos(1000000L);
                }
            }
        }, "CachedClock Updater Thread");
        log4jThread.setDaemon(true);
        log4jThread.start();
    }

    public static CachedClock instance() {
        CachedClock cachedClock = instance;
        if (cachedClock == null) {
            Object object = INSTANCE_LOCK;
            synchronized (object) {
                cachedClock = instance;
                if (cachedClock == null) {
                    instance = cachedClock = new CachedClock();
                }
            }
        }
        return cachedClock;
    }

    @Override
    public final long currentTimeMillis() {
        this.count = (short)(this.count + 1);
        if (this.count > 1000) {
            this.millis = System.currentTimeMillis();
            this.count = 0;
        }
        return this.millis;
    }

    static {
        INSTANCE_LOCK = new Object();
    }
}

