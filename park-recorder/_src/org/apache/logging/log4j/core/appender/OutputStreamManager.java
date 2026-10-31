/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.core.layout.ByteBufferDestinationHelper;
import org.apache.logging.log4j.core.util.Constants;

public class OutputStreamManager
extends AbstractManager
implements ByteBufferDestination {
    protected final Layout<?> layout;
    protected ByteBuffer byteBuffer;
    private volatile OutputStream outputStream;
    private boolean skipFooter;

    protected OutputStreamManager(OutputStream outputStream, String string, Layout<?> layout, boolean bl) {
        this(outputStream, string, layout, bl, Constants.ENCODER_BYTE_BUFFER_SIZE);
    }

    public OutputStreamManager(OutputStream outputStream, String string, Layout<?> layout, boolean bl, int n) {
        this(outputStream, string, layout, bl, ByteBuffer.wrap(new byte[n]));
    }

    @Deprecated
    protected OutputStreamManager(OutputStream outputStream, String string, Layout<?> layout, boolean bl, ByteBuffer byteBuffer) {
        super(null, string);
        this.outputStream = outputStream;
        this.layout = layout;
        if (bl) {
            this.writeHeader(outputStream);
        }
        this.byteBuffer = Objects.requireNonNull(byteBuffer, "byteBuffer");
    }

    protected OutputStreamManager(LoggerContext loggerContext, OutputStream outputStream, String string, boolean bl, Layout<? extends Serializable> layout, boolean bl2, ByteBuffer byteBuffer) {
        super(loggerContext, string);
        if (bl && outputStream != null) {
            LOGGER.error("Invalid OutputStreamManager configuration for '{}': You cannot both set the OutputStream and request on-demand.", (Object)string);
        }
        this.layout = layout;
        this.byteBuffer = Objects.requireNonNull(byteBuffer, "byteBuffer");
        this.outputStream = outputStream;
        if (bl2) {
            this.writeHeader(outputStream);
        }
    }

    public static <T> OutputStreamManager getManager(String string, T t, ManagerFactory<? extends OutputStreamManager, T> managerFactory) {
        return AbstractManager.getManager(string, managerFactory, t);
    }

    protected OutputStream createOutputStream() {
        throw new IllegalStateException(this.getClass().getCanonicalName() + " must implement createOutputStream()");
    }

    public void skipFooter(boolean bl) {
        this.skipFooter = bl;
    }

    @Override
    public boolean releaseSub(long l, TimeUnit timeUnit) {
        this.writeFooter();
        return this.closeOutputStream();
    }

    protected void writeHeader(OutputStream outputStream) {
        byte[] byArray;
        if (this.layout != null && outputStream != null && (byArray = this.layout.getHeader()) != null) {
            try {
                outputStream.write(byArray, 0, byArray.length);
                return;
            }
            catch (IOException iOException) {
                this.logError("Unable to write header", iOException);
            }
        }
    }

    protected void writeFooter() {
        if (this.layout == null || this.skipFooter) {
            return;
        }
        byte[] byArray = this.layout.getFooter();
        if (byArray != null) {
            this.write(byArray);
        }
    }

    public boolean isOpen() {
        return this.getCount() > 0;
    }

    public boolean hasOutputStream() {
        return this.outputStream != null;
    }

    public OutputStream getOutputStream() {
        if (this.outputStream == null) {
            this.outputStream = this.createOutputStream();
        }
        return this.outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    protected void write(byte[] byArray) {
        this.write(byArray, 0, byArray.length, false);
    }

    protected void write(byte[] byArray, boolean bl) {
        this.write(byArray, 0, byArray.length, bl);
    }

    @Override
    public void writeBytes(byte[] byArray, int n, int n2) {
        this.write(byArray, n, n2, false);
    }

    protected void write(byte[] byArray, int n, int n2) {
        this.writeBytes(byArray, n, n2);
    }

    protected synchronized void write(byte[] byArray, int n, int n2, boolean bl) {
        if (bl && this.byteBuffer.position() == 0) {
            this.writeToDestination(byArray, n, n2);
            this.flushDestination();
            return;
        }
        if (n2 >= this.byteBuffer.capacity()) {
            this.flush();
            this.writeToDestination(byArray, n, n2);
        } else {
            if (n2 > this.byteBuffer.remaining()) {
                this.flush();
            }
            this.byteBuffer.put(byArray, n, n2);
        }
        if (bl) {
            this.flush();
        }
    }

    protected synchronized void writeToDestination(byte[] byArray, int n, int n2) {
        try {
            this.getOutputStream().write(byArray, n, n2);
            return;
        }
        catch (IOException iOException) {
            throw new AppenderLoggingException("Error writing to stream " + this.getName(), iOException);
        }
    }

    protected synchronized void flushDestination() {
        OutputStream outputStream = this.outputStream;
        if (outputStream != null) {
            try {
                outputStream.flush();
                return;
            }
            catch (IOException iOException) {
                throw new AppenderLoggingException("Error flushing stream " + this.getName(), iOException);
            }
        }
    }

    protected synchronized void flushBuffer(ByteBuffer byteBuffer) {
        byteBuffer.flip();
        if (byteBuffer.remaining() > 0) {
            this.writeToDestination(byteBuffer.array(), byteBuffer.arrayOffset() + byteBuffer.position(), byteBuffer.remaining());
        }
        byteBuffer.clear();
    }

    public synchronized void flush() {
        OutputStreamManager outputStreamManager = this;
        outputStreamManager.flushBuffer(outputStreamManager.byteBuffer);
        this.flushDestination();
    }

    public synchronized boolean closeOutputStream() {
        this.flush();
        OutputStream outputStream = this.outputStream;
        if (outputStream == null || outputStream == System.out || outputStream == System.err) {
            return true;
        }
        try {
            outputStream.close();
            LOGGER.debug("OutputStream closed");
        }
        catch (IOException iOException) {
            this.logError("Unable to close stream", iOException);
            return false;
        }
        return true;
    }

    @Override
    public ByteBuffer getByteBuffer() {
        return this.byteBuffer;
    }

    @Override
    public ByteBuffer drain(ByteBuffer byteBuffer) {
        this.flushBuffer(byteBuffer);
        return byteBuffer;
    }

    @Override
    public void writeBytes(ByteBuffer byteBuffer) {
        if (byteBuffer.remaining() == 0) {
            return;
        }
        OutputStreamManager outputStreamManager = this;
        synchronized (outputStreamManager) {
            ByteBufferDestinationHelper.writeToUnsynchronized(byteBuffer, this);
            return;
        }
    }
}

