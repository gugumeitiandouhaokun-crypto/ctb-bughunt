/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ConfigurationFactoryData;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.util.FileUtils;
import org.apache.logging.log4j.core.util.NullOutputStream;

public class RandomAccessFileManager
extends OutputStreamManager {
    static final int DEFAULT_BUFFER_SIZE = 262144;
    private static final RandomAccessFileManagerFactory FACTORY = new RandomAccessFileManagerFactory();
    private final String advertiseURI;
    private final RandomAccessFile randomAccessFile;
    private final ThreadLocal<Boolean> isEndOfBatch = new ThreadLocal();

    protected RandomAccessFileManager(LoggerContext loggerContext, RandomAccessFile randomAccessFile, String string, OutputStream outputStream, int n, String string2, Layout<? extends Serializable> layout, boolean bl) {
        super(loggerContext, outputStream, string, false, layout, bl, ByteBuffer.wrap(new byte[n]));
        this.randomAccessFile = randomAccessFile;
        this.advertiseURI = string2;
        this.isEndOfBatch.set(Boolean.FALSE);
    }

    public static RandomAccessFileManager getFileManager(String string, boolean bl, boolean bl2, int n, String string2, Layout<? extends Serializable> layout, Configuration configuration) {
        return RandomAccessFileManager.narrow(RandomAccessFileManager.class, RandomAccessFileManager.getManager(string, new FactoryData(bl, bl2, n, string2, layout, configuration), FACTORY));
    }

    public Boolean isEndOfBatch() {
        return this.isEndOfBatch.get();
    }

    public void setEndOfBatch(boolean bl) {
        this.isEndOfBatch.set(bl);
    }

    @Override
    protected void writeToDestination(byte[] byArray, int n, int n2) {
        try {
            this.randomAccessFile.write(byArray, n, n2);
            return;
        }
        catch (IOException iOException) {
            String string = "Error writing to RandomAccessFile " + this.getName();
            throw new AppenderLoggingException(string, iOException);
        }
    }

    @Override
    public synchronized void flush() {
        RandomAccessFileManager randomAccessFileManager = this;
        randomAccessFileManager.flushBuffer(randomAccessFileManager.byteBuffer);
    }

    @Override
    public synchronized boolean closeOutputStream() {
        this.flush();
        try {
            this.randomAccessFile.close();
            return true;
        }
        catch (IOException iOException) {
            this.logError("Unable to close RandomAccessFile", iOException);
            return false;
        }
    }

    public String getFileName() {
        return this.getName();
    }

    public int getBufferSize() {
        return this.byteBuffer.capacity();
    }

    @Override
    public Map<String, String> getContentFormat() {
        HashMap<String, String> hashMap = new HashMap<String, String>(super.getContentFormat());
        hashMap.put("fileURI", this.advertiseURI);
        return hashMap;
    }

    static class RandomAccessFileManagerFactory
    implements ManagerFactory<RandomAccessFileManager, FactoryData> {
        private RandomAccessFileManagerFactory() {
        }

        @Override
        public RandomAccessFileManager createManager(String string, FactoryData factoryData) {
            Object object = new File(string);
            if (!factoryData.append) {
                ((File)object).delete();
            }
            boolean bl = !factoryData.append || !((File)object).exists();
            NullOutputStream nullOutputStream = NullOutputStream.getInstance();
            try {
                FileUtils.makeParentDirs((File)object);
                object = new RandomAccessFile(string, "rw");
                if (factoryData.append) {
                    Object object2 = object;
                    ((RandomAccessFile)object2).seek(((RandomAccessFile)object2).length());
                } else {
                    ((RandomAccessFile)object).setLength(0L);
                }
                return new RandomAccessFileManager(factoryData.getLoggerContext(), (RandomAccessFile)object, string, nullOutputStream, factoryData.bufferSize, factoryData.advertiseURI, factoryData.layout, bl);
            }
            catch (Exception exception) {
                AbstractManager.LOGGER.error("RandomAccessFileManager (" + string + ") " + exception, (Throwable)exception);
                return null;
            }
        }
    }

    static class FactoryData
    extends ConfigurationFactoryData {
        private final boolean append;
        private final boolean immediateFlush;
        private final int bufferSize;
        private final String advertiseURI;
        private final Layout<? extends Serializable> layout;

        public FactoryData(boolean bl, boolean bl2, int n, String string, Layout<? extends Serializable> layout, Configuration configuration) {
            super(configuration);
            this.append = bl;
            this.immediateFlush = bl2;
            this.bufferSize = n;
            this.advertiseURI = string;
            this.layout = layout;
        }
    }
}

