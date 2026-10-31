/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.core.util.Closer;

public final class Throwables {
    private Throwables() {
    }

    public static Throwable getRootCause(Throwable throwable) {
        Throwable throwable2 = throwable;
        while ((throwable = throwable2.getCause()) != null) {
            throwable2 = throwable;
        }
        return throwable2;
    }

    public static List<String> toStringList(Throwable serializable) {
        Closeable closeable = new StringWriter();
        Object object = new PrintWriter((Writer)closeable);
        try {
            ((Throwable)serializable).printStackTrace((PrintWriter)object);
        }
        catch (RuntimeException runtimeException) {}
        ((PrintWriter)object).flush();
        serializable = new ArrayList();
        closeable = new LineNumberReader(new StringReader(((StringWriter)closeable).toString()));
        try {
            object = ((LineNumberReader)closeable).readLine();
            while (object != null) {
                serializable.add(object);
                object = ((LineNumberReader)closeable).readLine();
            }
        }
        catch (IOException iOException) {
            object = iOException;
            if (iOException instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            serializable.add(((Throwable)object).toString());
        }
        finally {
            Closer.closeSilently(closeable);
        }
        return serializable;
    }

    public static void rethrow(Throwable throwable) {
        Throwables.rethrow0(throwable);
    }

    private static <T extends Throwable> void rethrow0(Throwable throwable) {
        throw throwable;
    }
}

