/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import org.apache.logging.log4j.core.util.Closer;
import org.apache.logging.log4j.core.util.FileUtils;
import org.apache.logging.log4j.core.util.NullOutputStream;

public class MemoryMappedFileManager
extends OutputStreamManager {
    static final int DEFAULT_REGION_LENGTH = 0x2000000;
    private static final int MAX_REMAP_COUNT = 10;
    private static final MemoryMappedFileManagerFactory FACTORY = new MemoryMappedFileManagerFactory();
    private static final double NANOS_PER_MILLISEC = 1000000.0;
    private final boolean immediateFlush;
    private final int regionLength;
    private final String advertiseURI;
    private final RandomAccessFile randomAccessFile;
    private final ThreadLocal<Boolean> isEndOfBatch = new ThreadLocal();
    private MappedByteBuffer mappedBuffer;
    private long mappingOffset;

    protected MemoryMappedFileManager(RandomAccessFile randomAccessFile, String string, OutputStream outputStream, boolean bl, long l, int n, String string2, Layout<? extends Serializable> layout, boolean bl2) {
        super(outputStream, string, layout, bl2, ByteBuffer.wrap(new byte[0]));
        this.immediateFlush = bl;
        this.randomAccessFile = Objects.requireNonNull(randomAccessFile, "RandomAccessFile");
        this.regionLength = n;
        this.advertiseURI = string2;
        this.isEndOfBatch.set(Boolean.FALSE);
        this.mappedBuffer = MemoryMappedFileManager.mmap(this.randomAccessFile.getChannel(), this.getFileName(), l, n);
        this.byteBuffer = this.mappedBuffer;
        this.mappingOffset = l;
    }

    public static MemoryMappedFileManager getFileManager(String string, boolean bl, boolean bl2, int n, String string2, Layout<? extends Serializable> layout) {
        return MemoryMappedFileManager.narrow(MemoryMappedFileManager.class, MemoryMappedFileManager.getManager(string, new FactoryData(bl, bl2, n, string2, layout), FACTORY));
    }

    public Boolean isEndOfBatch() {
        return this.isEndOfBatch.get();
    }

    public void setEndOfBatch(boolean bl) {
        this.isEndOfBatch.set(bl);
    }

    @Override
    protected synchronized void write(byte[] byArray, int n, int n2, boolean bl) {
        while (n2 > this.mappedBuffer.remaining()) {
            int n3 = this.mappedBuffer.remaining();
            this.mappedBuffer.put(byArray, n, n3);
            n += n3;
            n2 -= n3;
            this.remap();
        }
        this.mappedBuffer.put(byArray, n, n2);
    }

    private synchronized void remap() {
        long l = this.mappingOffset + (long)this.mappedBuffer.position();
        int n = this.mappedBuffer.remaining() + this.regionLength;
        try {
            MemoryMappedFileManager.unsafeUnmap(this.mappedBuffer);
            long l2 = this.randomAccessFile.length() + (long)this.regionLength;
            LOGGER.debug("{} {} extending {} by {} bytes to {}", (Object)this.getClass().getSimpleName(), (Object)this.getName(), (Object)this.getFileName(), (Object)this.regionLength, (Object)l2);
            long l3 = System.nanoTime();
            this.randomAccessFile.setLength(l2);
            float f2 = (float)((double)(System.nanoTime() - l3) / 1000000.0);
            LOGGER.debug("{} {} extended {} OK in {} millis", (Object)this.getClass().getSimpleName(), (Object)this.getName(), (Object)this.getFileName(), (Object)Float.valueOf(f2));
            this.mappedBuffer = MemoryMappedFileManager.mmap(this.randomAccessFile.getChannel(), this.getFileName(), l, n);
            this.byteBuffer = this.mappedBuffer;
            this.mappingOffset = l;
            return;
        }
        catch (Exception exception) {
            this.logError("Unable to remap", exception);
            return;
        }
    }

    @Override
    public synchronized void flush() {
        this.mappedBuffer.force();
    }

    @Override
    public synchronized boolean closeOutputStream() {
        long l = this.mappedBuffer.position();
        long l2 = this.mappingOffset + l;
        try {
            MemoryMappedFileManager.unsafeUnmap(this.mappedBuffer);
        }
        catch (Exception exception) {
            this.logError("Unable to unmap MappedBuffer", exception);
        }
        try {
            LOGGER.debug("MMapAppender closing. Setting {} length to {} (offset {} + position {})", (Object)this.getFileName(), (Object)l2, (Object)this.mappingOffset, (Object)l);
            this.randomAccessFile.setLength(l2);
            this.randomAccessFile.close();
            return true;
        }
        catch (IOException iOException) {
            this.logError("Unable to close MemoryMappedFile", iOException);
            return false;
        }
    }

    public static MappedByteBuffer mmap(FileChannel fileChannel, String string, long l, int n) {
        int n2 = 1;
        while (true) {
            try {
                LOGGER.debug("MMapAppender remapping {} start={}, size={}", (Object)string, (Object)l, (Object)n);
                long l2 = System.nanoTime();
                MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, l, n);
                mappedByteBuffer.order(ByteOrder.nativeOrder());
                float f2 = (float)((double)(System.nanoTime() - l2) / 1000000.0);
                LOGGER.debug("MMapAppender remapped {} OK in {} millis", (Object)string, (Object)Float.valueOf(f2));
                return mappedByteBuffer;
            }
            catch (IOException iOException) {
                IOException iOException2 = iOException;
                if (iOException.getMessage() == null || !iOException2.getMessage().endsWith("user-mapped section open")) {
                    throw iOException2;
                }
                LOGGER.debug("Remap attempt {}/{} failed. Retrying...", (Object)n2, (Object)10, (Object)iOException2);
                if (n2 < 10) {
                    Thread.yield();
                } else {
                    try {
                        Thread.sleep(1L);
                    }
                    catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        throw iOException2;
                    }
                }
                ++n2;
                continue;
            }
            break;
        }
    }

    private static void unsafeUnmap(final MappedByteBuffer mappedByteBuffer) {
        LOGGER.debug("MMapAppender unmapping old buffer...");
        long l = System.nanoTime();
        AccessController.doPrivileged(new PrivilegedExceptionAction<Object>(){

            @Override
            public final Object run() {
                Object object = mappedByteBuffer.getClass().getMethod("cleaner", new Class[0]);
                ((AccessibleObject)object).setAccessible(true);
                object = ((Method)object).invoke(mappedByteBuffer, new Object[0]);
                object.getClass().getMethod("clean", new Class[0]).invoke(object, new Object[0]);
                return null;
            }
        });
        float f2 = (float)((double)(System.nanoTime() - l) / 1000000.0);
        LOGGER.debug("MMapAppender unmapped buffer OK in {} millis", (Object)Float.valueOf(f2));
    }

    public String getFileName() {
        return this.getName();
    }

    public int getRegionLength() {
        return this.regionLength;
    }

    public boolean isImmediateFlush() {
        return this.immediateFlush;
    }

    @Override
    public Map<String, String> getContentFormat() {
        HashMap<String, String> hashMap = new HashMap<String, String>(super.getContentFormat());
        hashMap.put("fileURI", this.advertiseURI);
        return hashMap;
    }

    @Override
    protected void flushBuffer(ByteBuffer byteBuffer) {
    }

    @Override
    public ByteBuffer getByteBuffer() {
        return this.mappedBuffer;
    }

    @Override
    public ByteBuffer drain(ByteBuffer byteBuffer) {
        this.remap();
        return this.mappedBuffer;
    }

    static class MemoryMappedFileManagerFactory
    implements ManagerFactory<MemoryMappedFileManager, FactoryData> {
        private MemoryMappedFileManagerFactory() {
        }

        @Override
        public MemoryMappedFileManager createManager(String string, FactoryData factoryData) {
            File file = new File(string);
            if (!factoryData.append) {
                file.delete();
            }
            boolean bl = !factoryData.append || !file.exists();
            NullOutputStream nullOutputStream = NullOutputStream.getInstance();
            RandomAccessFile randomAccessFile = null;
            try {
                FileUtils.makeParentDirs(file);
                randomAccessFile = new RandomAccessFile(string, "rw");
                long l = factoryData.append ? randomAccessFile.length() : 0L;
                randomAccessFile.setLength(l + (long)factoryData.regionLength);
                return new MemoryMappedFileManager(randomAccessFile, string, nullOutputStream, factoryData.immediateFlush, l, factoryData.regionLength, factoryData.advertiseURI, factoryData.layout, bl);
            }
            catch (Exception exception) {
                AbstractManager.LOGGER.error("MemoryMappedFileManager (" + string + ") " + exception, (Throwable)exception);
                Closer.closeSilently(randomAccessFile);
                return null;
            }
        }
    }

    static class FactoryData {
        private final boolean append;
        private final boolean immediateFlush;
        private final int regionLength;
        private final String advertiseURI;
        private final Layout<? extends Serializable> layout;

        public FactoryData(boolean bl, boolean bl2, int n, String string, Layout<? extends Serializable> layout) {
            this.append = bl;
            this.immediateFlush = bl2;
            this.regionLength = n;
            this.advertiseURI = string;
            this.layout = layout;
        }
    }
}

