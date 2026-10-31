/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.event.SubstituteLoggingEvent;
import org.slf4j.helpers.NOPLoggerFactory;
import org.slf4j.helpers.SubstituteLogger;
import org.slf4j.helpers.SubstituteLoggerFactory;
import org.slf4j.helpers.Util;
import org.slf4j.impl.StaticLoggerBinder;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class LoggerFactory {
    static final String CODES_PREFIX = "http://www.slf4j.org/codes.html";
    static final String NO_STATICLOGGERBINDER_URL = "http://www.slf4j.org/codes.html#StaticLoggerBinder";
    static final String MULTIPLE_BINDINGS_URL = "http://www.slf4j.org/codes.html#multiple_bindings";
    static final String NULL_LF_URL = "http://www.slf4j.org/codes.html#null_LF";
    static final String VERSION_MISMATCH = "http://www.slf4j.org/codes.html#version_mismatch";
    static final String SUBSTITUTE_LOGGER_URL = "http://www.slf4j.org/codes.html#substituteLogger";
    static final String LOGGER_NAME_MISMATCH_URL = "http://www.slf4j.org/codes.html#loggerNameMismatch";
    static final String REPLAY_URL = "http://www.slf4j.org/codes.html#replay";
    static final String UNSUCCESSFUL_INIT_URL = "http://www.slf4j.org/codes.html#unsuccessfulInit";
    static final String UNSUCCESSFUL_INIT_MSG = "org.slf4j.LoggerFactory in failed state. Original exception was thrown EARLIER. See also http://www.slf4j.org/codes.html#unsuccessfulInit";
    static final int UNINITIALIZED = 0;
    static final int ONGOING_INITIALIZATION = 1;
    static final int FAILED_INITIALIZATION = 2;
    static final int SUCCESSFUL_INITIALIZATION = 3;
    static final int NOP_FALLBACK_INITIALIZATION = 4;
    static volatile int INITIALIZATION_STATE = 0;
    static final SubstituteLoggerFactory SUBST_FACTORY = new SubstituteLoggerFactory();
    static final NOPLoggerFactory NOP_FALLBACK_FACTORY = new NOPLoggerFactory();
    static final String DETECT_LOGGER_NAME_MISMATCH_PROPERTY = "slf4j.detectLoggerNameMismatch";
    static final String JAVA_VENDOR_PROPERTY = "java.vendor.url";
    static boolean DETECT_LOGGER_NAME_MISMATCH = Util.safeGetBooleanSystemProperty("slf4j.detectLoggerNameMismatch");
    private static final String[] API_COMPATIBILITY_LIST = new String[]{"1.6", "1.7"};
    private static String STATIC_LOGGER_BINDER_PATH = "org/slf4j/impl/StaticLoggerBinder.class";

    private LoggerFactory() {
    }

    static void reset() {
        INITIALIZATION_STATE = 0;
    }

    private static final void performInitialization() {
        LoggerFactory.bind();
        if (INITIALIZATION_STATE == 3) {
            LoggerFactory.versionSanityCheck();
        }
    }

    private static boolean messageContainsOrgSlf4jImplStaticLoggerBinder(String string) {
        if (string == null) {
            return false;
        }
        if (string.contains("org/slf4j/impl/StaticLoggerBinder")) {
            return true;
        }
        return string.contains("org.slf4j.impl.StaticLoggerBinder");
    }

    private static final void bind() {
        try {
            Set<URL> set = null;
            if (!LoggerFactory.isAndroid()) {
                set = LoggerFactory.findPossibleStaticLoggerBinderPathSet();
                LoggerFactory.reportMultipleBindingAmbiguity(set);
            }
            StaticLoggerBinder.getSingleton();
            INITIALIZATION_STATE = 3;
            LoggerFactory.reportActualBinding(set);
            LoggerFactory.fixSubstituteLoggers();
            LoggerFactory.replayEvents();
            SUBST_FACTORY.clear();
            return;
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            NoClassDefFoundError noClassDefFoundError2 = noClassDefFoundError;
            if (LoggerFactory.messageContainsOrgSlf4jImplStaticLoggerBinder(noClassDefFoundError.getMessage())) {
                INITIALIZATION_STATE = 4;
                Util.report("Failed to load class \"org.slf4j.impl.StaticLoggerBinder\".");
                Util.report("Defaulting to no-operation (NOP) logger implementation");
                Util.report("See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.");
                return;
            }
            LoggerFactory.failedBinding(noClassDefFoundError2);
            throw noClassDefFoundError2;
        }
        catch (NoSuchMethodError noSuchMethodError) {
            NoSuchMethodError noSuchMethodError2 = noSuchMethodError;
            String string = noSuchMethodError.getMessage();
            if (string != null && string.contains("org.slf4j.impl.StaticLoggerBinder.getSingleton()")) {
                INITIALIZATION_STATE = 2;
                Util.report("slf4j-api 1.6.x (or later) is incompatible with this binding.");
                Util.report("Your binding is version 1.5.5 or earlier.");
                Util.report("Upgrade your binding to version 1.6.x.");
            }
            throw noSuchMethodError2;
        }
        catch (Exception exception) {
            Exception exception2 = exception;
            LoggerFactory.failedBinding(exception);
            throw new IllegalStateException("Unexpected initialization failure", exception2);
        }
    }

    private static void fixSubstituteLoggers() {
        SubstituteLoggerFactory substituteLoggerFactory = SUBST_FACTORY;
        synchronized (substituteLoggerFactory) {
            SUBST_FACTORY.postInitialization();
            for (SubstituteLogger substituteLogger : SUBST_FACTORY.getLoggers()) {
                Logger logger = LoggerFactory.getLogger(substituteLogger.getName());
                substituteLogger.setDelegate(logger);
            }
            return;
        }
    }

    static void failedBinding(Throwable throwable) {
        INITIALIZATION_STATE = 2;
        Util.report("Failed to instantiate SLF4J LoggerFactory", throwable);
    }

    private static void replayEvents() {
        LinkedBlockingQueue<SubstituteLoggingEvent> linkedBlockingQueue = SUBST_FACTORY.getEventQueue();
        int n = linkedBlockingQueue.size();
        int n2 = 0;
        ArrayList arrayList = new ArrayList(128);
        while (linkedBlockingQueue.drainTo(arrayList, 128) != 0) {
            for (SubstituteLoggingEvent substituteLoggingEvent : arrayList) {
                LoggerFactory.replaySingleEvent(substituteLoggingEvent);
                if (n2++ != 0) continue;
                LoggerFactory.emitReplayOrSubstituionWarning(substituteLoggingEvent, n);
            }
            arrayList.clear();
        }
    }

    private static void emitReplayOrSubstituionWarning(SubstituteLoggingEvent substituteLoggingEvent, int n) {
        if (substituteLoggingEvent.getLogger().isDelegateEventAware()) {
            LoggerFactory.emitReplayWarning(n);
            return;
        }
        if (!substituteLoggingEvent.getLogger().isDelegateNOP()) {
            LoggerFactory.emitSubstitutionWarning();
        }
    }

    private static void replaySingleEvent(SubstituteLoggingEvent substituteLoggingEvent) {
        if (substituteLoggingEvent == null) {
            return;
        }
        SubstituteLogger substituteLogger = substituteLoggingEvent.getLogger();
        String string = substituteLogger.getName();
        if (substituteLogger.isDelegateNull()) {
            throw new IllegalStateException("Delegate logger cannot be null at this state.");
        }
        if (!substituteLogger.isDelegateNOP()) {
            if (substituteLogger.isDelegateEventAware()) {
                substituteLogger.log(substituteLoggingEvent);
                return;
            }
            Util.report(string);
        }
    }

    private static void emitSubstitutionWarning() {
        Util.report("The following set of substitute loggers may have been accessed");
        Util.report("during the initialization phase. Logging calls during this");
        Util.report("phase were not honored. However, subsequent logging calls to these");
        Util.report("loggers will work as normally expected.");
        Util.report("See also http://www.slf4j.org/codes.html#substituteLogger");
    }

    private static void emitReplayWarning(int n) {
        Util.report("A number (" + n + ") of logging calls during the initialization phase have been intercepted and are");
        Util.report("now being replayed. These are subject to the filtering rules of the underlying logging system.");
        Util.report("See also http://www.slf4j.org/codes.html#replay");
    }

    private static final void versionSanityCheck() {
        try {
            String string = StaticLoggerBinder.REQUESTED_API_VERSION;
            boolean bl = false;
            String[] stringArray = API_COMPATIBILITY_LIST;
            int n = API_COMPATIBILITY_LIST.length;
            for (int i2 = 0; i2 < n; ++i2) {
                String string2 = stringArray[i2];
                if (!string.startsWith(string2)) continue;
                bl = true;
            }
            if (!bl) {
                Util.report("The requested version " + string + " by your slf4j binding is not compatible with " + Arrays.asList(API_COMPATIBILITY_LIST).toString());
                Util.report("See http://www.slf4j.org/codes.html#version_mismatch for further details.");
            }
            return;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            return;
        }
        catch (Throwable throwable) {
            Util.report("Unexpected problem occured during version sanity check", throwable);
            return;
        }
    }

    static Set<URL> findPossibleStaticLoggerBinderPathSet() {
        LinkedHashSet<URL> linkedHashSet = new LinkedHashSet<URL>();
        try {
            Enumeration<URL> enumeration = LoggerFactory.class.getClassLoader();
            enumeration = enumeration == null ? ClassLoader.getSystemResources(STATIC_LOGGER_BINDER_PATH) : ((ClassLoader)((Object)enumeration)).getResources(STATIC_LOGGER_BINDER_PATH);
            while (enumeration.hasMoreElements()) {
                URL uRL = enumeration.nextElement();
                linkedHashSet.add(uRL);
            }
        }
        catch (IOException iOException) {
            Util.report("Error getting resources from path", iOException);
        }
        return linkedHashSet;
    }

    private static boolean isAmbiguousStaticLoggerBinderPathSet(Set<URL> set) {
        return set.size() > 1;
    }

    private static void reportMultipleBindingAmbiguity(Set<URL> object) {
        if (LoggerFactory.isAmbiguousStaticLoggerBinderPathSet(object)) {
            Util.report("Class path contains multiple SLF4J bindings.");
            object = object.iterator();
            while (object.hasNext()) {
                URL uRL = (URL)object.next();
                Util.report("Found binding in [" + uRL + "]");
            }
            Util.report("See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.");
        }
    }

    private static boolean isAndroid() {
        String string = Util.safeGetSystemProperty(JAVA_VENDOR_PROPERTY);
        if (string == null) {
            return false;
        }
        return string.toLowerCase().contains("android");
    }

    private static void reportActualBinding(Set<URL> set) {
        if (set != null && LoggerFactory.isAmbiguousStaticLoggerBinderPathSet(set)) {
            Util.report("Actual binding is of type [" + StaticLoggerBinder.getSingleton().getLoggerFactoryClassStr() + "]");
        }
    }

    public static Logger getLogger(String string) {
        return LoggerFactory.getILoggerFactory().getLogger(string);
    }

    public static Logger getLogger(Class<?> clazz) {
        Class<?> clazz2;
        Logger logger = LoggerFactory.getLogger(clazz.getName());
        if (DETECT_LOGGER_NAME_MISMATCH && (clazz2 = Util.getCallingClass()) != null && LoggerFactory.nonMatchingClasses(clazz, clazz2)) {
            Util.report(String.format("Detected logger name mismatch. Given name: \"%s\"; computed name: \"%s\".", logger.getName(), clazz2.getName()));
            Util.report("See http://www.slf4j.org/codes.html#loggerNameMismatch for an explanation");
        }
        return logger;
    }

    private static boolean nonMatchingClasses(Class<?> clazz, Class<?> clazz2) {
        return !clazz2.isAssignableFrom(clazz);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    public static ILoggerFactory getILoggerFactory() {
        if (INITIALIZATION_STATE == 0) {
            Class<LoggerFactory> clazz = LoggerFactory.class;
            // MONITORENTER : org.slf4j.LoggerFactory.class
            if (INITIALIZATION_STATE == 0) {
                INITIALIZATION_STATE = 1;
                LoggerFactory.performInitialization();
            }
            // MONITOREXIT : clazz
        }
        switch (INITIALIZATION_STATE) {
            case 3: {
                return StaticLoggerBinder.getSingleton().getLoggerFactory();
            }
            case 4: {
                return NOP_FALLBACK_FACTORY;
            }
            case 2: {
                throw new IllegalStateException(UNSUCCESSFUL_INIT_MSG);
            }
            case 1: {
                return SUBST_FACTORY;
            }
        }
        throw new IllegalStateException("Unreachable code");
    }
}

