/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.core.time.PreciseClock;
import org.apache.logging.log4j.core.util.CachedClock;
import org.apache.logging.log4j.core.util.Clock;
import org.apache.logging.log4j.core.util.CoarseCachedClock;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.core.util.SystemClock;
import org.apache.logging.log4j.core.util.SystemMillisClock;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.Supplier;

public final class ClockFactory {
    public static final String PROPERTY_NAME = "log4j.Clock";
    private static final StatusLogger LOGGER = StatusLogger.getLogger();

    private ClockFactory() {
    }

    public static Clock getClock() {
        return ClockFactory.createClock();
    }

    private static Map<String, Supplier<Clock>> aliases() {
        HashMap<String, Supplier<Clock>> hashMap = new HashMap<String, Supplier<Clock>>();
        hashMap.put("SystemClock", new Supplier<Clock>(){

            @Override
            public final Clock get() {
                return new SystemClock();
            }
        });
        hashMap.put("SystemMillisClock", new Supplier<Clock>(){

            @Override
            public final Clock get() {
                return new SystemMillisClock();
            }
        });
        hashMap.put("CachedClock", new Supplier<Clock>(){

            @Override
            public final Clock get() {
                return CachedClock.instance();
            }
        });
        hashMap.put("CoarseCachedClock", new Supplier<Clock>(){

            @Override
            public final Clock get() {
                return CoarseCachedClock.instance();
            }
        });
        hashMap.put("org.apache.logging.log4j.core.util.CachedClock", new Supplier<Clock>(){

            @Override
            public final Clock get() {
                return CachedClock.instance();
            }
        });
        hashMap.put("org.apache.logging.log4j.core.util.CoarseCachedClock", new Supplier<Clock>(){

            @Override
            public final Clock get() {
                return CoarseCachedClock.instance();
            }
        });
        return hashMap;
    }

    private static Clock createClock() {
        String string = PropertiesUtil.getProperties().getStringProperty(PROPERTY_NAME);
        if (string == null) {
            LOGGER.trace("Using default SystemClock for timestamps.");
            return ClockFactory.logSupportedPrecision(new SystemClock());
        }
        Supplier<Clock> supplier = ClockFactory.aliases().get(string);
        if (supplier != null) {
            LOGGER.trace("Using specified {} for timestamps.", (Object)string);
            return ClockFactory.logSupportedPrecision(supplier.get());
        }
        try {
            supplier = Loader.newCheckedInstanceOf(string, Clock.class);
            LOGGER.trace("Using {} for timestamps.", (Object)supplier.getClass().getName());
            return ClockFactory.logSupportedPrecision((Clock)((Object)supplier));
        }
        catch (Exception exception) {
            LOGGER.error("Could not create {}: {}, using default SystemClock for timestamps.", (Object)string, (Object)exception);
            return ClockFactory.logSupportedPrecision(new SystemClock());
        }
    }

    private static Clock logSupportedPrecision(Clock clock) {
        String string = clock instanceof PreciseClock ? "supports" : "does not support";
        LOGGER.debug("{} {} precise timestamps.", (Object)clock.getClass().getName(), (Object)string);
        return clock;
    }
}

