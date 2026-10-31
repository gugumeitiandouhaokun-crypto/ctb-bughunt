/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.lmax.disruptor.ExceptionHandler
 */
package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.ExceptionHandler;

abstract class AbstractAsyncExceptionHandler<T>
implements ExceptionHandler<T> {
    AbstractAsyncExceptionHandler() {
    }

    public void handleEventException(Throwable throwable, long l, T t) {
        try {
            System.err.print("AsyncLogger error handling event seq=");
            System.err.print(l);
            System.err.print(", value='");
            try {
                System.err.print(t);
            }
            catch (Throwable throwable2) {
                System.err.print("ERROR calling toString() on ");
                System.err.print(t.getClass().getName());
                System.err.print(": ");
                System.err.print(throwable2.getClass().getName());
                System.err.print(": ");
                System.err.print(throwable2.getMessage());
            }
            System.err.print("': ");
            System.err.print(throwable.getClass().getName());
            System.err.print(": ");
            System.err.println(throwable.getMessage());
            throwable.printStackTrace();
            return;
        }
        catch (Throwable throwable3) {
            return;
        }
    }

    public void handleOnStartException(Throwable throwable) {
        System.err.println("AsyncLogger error starting:");
        throwable.printStackTrace();
    }

    public void handleOnShutdownException(Throwable throwable) {
        System.err.println("AsyncLogger error shutting down:");
        throwable.printStackTrace();
    }
}

