/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.concurrent;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.ConcurrentInitializer;
import org.apache.commons.lang3.concurrent.ConcurrentRuntimeException;

public class ConcurrentUtils {
    private ConcurrentUtils() {
    }

    public static ConcurrentException extractCause(ExecutionException executionException) {
        if (executionException == null || executionException.getCause() == null) {
            return null;
        }
        ConcurrentUtils.throwCause(executionException);
        return new ConcurrentException(executionException.getMessage(), executionException.getCause());
    }

    public static ConcurrentRuntimeException extractCauseUnchecked(ExecutionException executionException) {
        if (executionException == null || executionException.getCause() == null) {
            return null;
        }
        ConcurrentUtils.throwCause(executionException);
        return new ConcurrentRuntimeException(executionException.getMessage(), executionException.getCause());
    }

    public static void handleCause(ExecutionException exception) {
        if ((exception = ConcurrentUtils.extractCause(exception)) != null) {
            throw exception;
        }
    }

    public static void handleCauseUnchecked(ExecutionException exception) {
        if ((exception = ConcurrentUtils.extractCauseUnchecked(exception)) != null) {
            throw exception;
        }
    }

    static Throwable checkedException(Throwable throwable) {
        Validate.isTrue(throwable != null && !(throwable instanceof RuntimeException) && !(throwable instanceof Error), "Not a checked exception: ".concat(String.valueOf(throwable)), new Object[0]);
        return throwable;
    }

    private static void throwCause(ExecutionException executionException) {
        if (executionException.getCause() instanceof RuntimeException) {
            throw (RuntimeException)executionException.getCause();
        }
        if (executionException.getCause() instanceof Error) {
            throw (Error)executionException.getCause();
        }
    }

    public static <T> T initialize(ConcurrentInitializer<T> concurrentInitializer) {
        if (concurrentInitializer != null) {
            return concurrentInitializer.get();
        }
        return null;
    }

    public static <T> T initializeUnchecked(ConcurrentInitializer<T> concurrentInitializer) {
        try {
            return ConcurrentUtils.initialize(concurrentInitializer);
        }
        catch (ConcurrentException concurrentException) {
            throw new ConcurrentRuntimeException(concurrentException.getCause());
        }
    }

    public static <K, V> V putIfAbsent(ConcurrentMap<K, V> concurrentMap, K k, V v) {
        if (concurrentMap == null) {
            return null;
        }
        if ((concurrentMap = concurrentMap.putIfAbsent(k, v)) != null) {
            return (V)concurrentMap;
        }
        return v;
    }

    public static <K, V> V createIfAbsent(ConcurrentMap<K, V> concurrentMap, K k, ConcurrentInitializer<V> concurrentInitializer) {
        if (concurrentMap == null || concurrentInitializer == null) {
            return null;
        }
        Object v = concurrentMap.get(k);
        if (v == null) {
            return ConcurrentUtils.putIfAbsent(concurrentMap, k, concurrentInitializer.get());
        }
        return v;
    }

    public static <K, V> V createIfAbsentUnchecked(ConcurrentMap<K, V> concurrentMap, K k, ConcurrentInitializer<V> concurrentInitializer) {
        try {
            return ConcurrentUtils.createIfAbsent(concurrentMap, k, concurrentInitializer);
        }
        catch (ConcurrentException concurrentException) {
            throw new ConcurrentRuntimeException(concurrentException.getCause());
        }
    }

    public static <T> Future<T> constantFuture(T t) {
        return new ConstantFuture<T>(t);
    }

    static final class ConstantFuture<T>
    implements Future<T> {
        private final T value;

        ConstantFuture(T t) {
            this.value = t;
        }

        @Override
        public final boolean isDone() {
            return true;
        }

        @Override
        public final T get() {
            return this.value;
        }

        @Override
        public final T get(long l, TimeUnit timeUnit) {
            return this.value;
        }

        @Override
        public final boolean isCancelled() {
            return false;
        }

        @Override
        public final boolean cancel(boolean bl) {
            return false;
        }
    }
}

