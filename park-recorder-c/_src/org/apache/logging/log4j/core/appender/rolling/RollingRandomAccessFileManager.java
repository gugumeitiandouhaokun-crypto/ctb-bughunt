/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ConfigurationFactoryData;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.rolling.DirectWriteRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.PatternProcessor;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.util.FileUtils;
import org.apache.logging.log4j.core.util.NullOutputStream;

public class RollingRandomAccessFileManager
extends RollingFileManager {
    public static final int DEFAULT_BUFFER_SIZE = 262144;
    private static final RollingRandomAccessFileManagerFactory FACTORY = new RollingRandomAccessFileManagerFactory();
    private RandomAccessFile randomAccessFile;
    private final ThreadLocal<Boolean> isEndOfBatch = new ThreadLocal();

    @Deprecated
    public RollingRandomAccessFileManager(LoggerContext loggerContext, RandomAccessFile randomAccessFile, String string, String string2, OutputStream outputStream, boolean bl, boolean bl2, int n, long l, long l2, TriggeringPolicy triggeringPolicy, RolloverStrategy rolloverStrategy, String string3, Layout<? extends Serializable> layout, boolean bl3) {
        this(loggerContext, randomAccessFile, string, string2, outputStream, bl, bl2, n, l, l2, triggeringPolicy, rolloverStrategy, string3, layout, null, null, null, bl3);
    }

    public RollingRandomAccessFileManager(LoggerContext loggerContext, RandomAccessFile randomAccessFile, String string, String string2, OutputStream outputStream, boolean bl, boolean bl2, int n, long l, long l2, TriggeringPolicy triggeringPolicy, RolloverStrategy rolloverStrategy, String string3, Layout<? extends Serializable> layout, String string4, String string5, String string6, boolean bl3) {
        super(loggerContext, string, string2, outputStream, bl, false, l, l2, triggeringPolicy, rolloverStrategy, string3, layout, string4, string5, string6, bl3, ByteBuffer.wrap(new byte[n]));
        this.randomAccessFile = randomAccessFile;
        this.isEndOfBatch.set(Boolean.FALSE);
        this.writeHeader();
    }

    private void writeHeader() {
        if (this.layout == null) {
            return;
        }
        byte[] byArray = this.layout.getHeader();
        if (byArray == null) {
            return;
        }
        try {
            if (this.randomAccessFile != null && this.randomAccessFile.length() == 0L) {
                this.randomAccessFile.write(byArray, 0, byArray.length);
            }
            return;
        }
        catch (IOException iOException) {
            this.logError("Unable to write header", iOException);
            return;
        }
    }

    public static RollingRandomAccessFileManager getRollingRandomAccessFileManager(String string, String string2, boolean bl, boolean bl2, int n, TriggeringPolicy triggeringPolicy, RolloverStrategy rolloverStrategy, String string3, Layout<? extends Serializable> layout, String string4, String string5, String string6, Configuration configuration) {
        if (rolloverStrategy instanceof DirectWriteRolloverStrategy && string != null) {
            LOGGER.error("The fileName attribute must not be specified with the DirectWriteRolloverStrategy");
            return null;
        }
        String string7 = string == null ? string2 : string;
        return RollingRandomAccessFileManager.narrow(RollingRandomAccessFileManager.class, RollingRandomAccessFileManager.getManager(string7, new FactoryData(string, string2, bl, bl2, n, triggeringPolicy, rolloverStrategy, string3, layout, string4, string5, string6, configuration), FACTORY));
    }

    public Boolean isEndOfBatch() {
        return this.isEndOfBatch.get();
    }

    public void setEndOfBatch(boolean bl) {
        this.isEndOfBatch.set(bl);
    }

    @Override
    public synchronized void write(byte[] byArray, int n, int n2, boolean bl) {
        super.write(byArray, n, n2, bl);
    }

    @Override
    public synchronized void writeToDestination(byte[] object, int n, int n2) {
        try {
            if (this.randomAccessFile == null) {
                String string = this.getFileName();
                FileUtils.makeParentDirs(new File(string));
                this.createFileAfterRollover(string);
            }
            this.randomAccessFile.write((byte[])object, n, n2);
            this.size += (long)n2;
            return;
        }
        catch (IOException iOException) {
            object = "Error writing to RandomAccessFile " + this.getName();
            throw new AppenderLoggingException((String)object, iOException);
        }
    }

    @Override
    protected void createFileAfterRollover() {
        RollingRandomAccessFileManager rollingRandomAccessFileManager = this;
        rollingRandomAccessFileManager.createFileAfterRollover(rollingRandomAccessFileManager.getFileName());
    }

    private void createFileAfterRollover(String string) {
        this.randomAccessFile = new RandomAccessFile(string, "rw");
        if (this.isAppend()) {
            this.randomAccessFile.seek(this.randomAccessFile.length());
        }
        this.writeHeader();
    }

    @Override
    public synchronized void flush() {
        RollingRandomAccessFileManager rollingRandomAccessFileManager = this;
        rollingRandomAccessFileManager.flushBuffer(rollingRandomAccessFileManager.byteBuffer);
    }

    @Override
    public synchronized boolean closeOutputStream() {
        this.flush();
        if (this.randomAccessFile != null) {
            try {
                this.randomAccessFile.close();
                return true;
            }
            catch (IOException iOException) {
                this.logError("Unable to close RandomAccessFile", iOException);
                return false;
            }
        }
        return true;
    }

    @Override
    public int getBufferSize() {
        return this.byteBuffer.capacity();
    }

    @Override
    public void updateData(Object object) {
        object = (FactoryData)object;
        this.setRolloverStrategy(((FactoryData)object).getRolloverStrategy());
        this.setPatternProcessor(new PatternProcessor(((FactoryData)object).getPattern(), this.getPatternProcessor()));
        this.setTriggeringPolicy(((FactoryData)object).getTriggeringPolicy());
    }

    static class FactoryData
    extends ConfigurationFactoryData {
        private final String fileName;
        private final String pattern;
        private final boolean append;
        private final boolean immediateFlush;
        private final int bufferSize;
        private final TriggeringPolicy policy;
        private final RolloverStrategy strategy;
        private final String advertiseURI;
        private final Layout<? extends Serializable> layout;
        private final String filePermissions;
        private final String fileOwner;
        private final String fileGroup;

        public FactoryData(String string, String string2, boolean bl, boolean bl2, int n, TriggeringPolicy triggeringPolicy, RolloverStrategy rolloverStrategy, String string3, Layout<? extends Serializable> layout, String string4, String string5, String string6, Configuration configuration) {
            super(configuration);
            this.fileName = string;
            this.pattern = string2;
            this.append = bl;
            this.immediateFlush = bl2;
            this.bufferSize = n;
            this.policy = triggeringPolicy;
            this.strategy = rolloverStrategy;
            this.advertiseURI = string3;
            this.layout = layout;
            this.filePermissions = string4;
            this.fileOwner = string5;
            this.fileGroup = string6;
        }

        public String getPattern() {
            return this.pattern;
        }

        public TriggeringPolicy getTriggeringPolicy() {
            return this.policy;
        }

        public RolloverStrategy getRolloverStrategy() {
            return this.strategy;
        }
    }

    static class RollingRandomAccessFileManagerFactory
    implements ManagerFactory<RollingRandomAccessFileManager, FactoryData> {
        private RollingRandomAccessFileManagerFactory() {
        }

        @Override
        public RollingRandomAccessFileManager createManager(String string, FactoryData factoryData) {
            File file = null;
            long l = 0L;
            long l2 = System.currentTimeMillis();
            RandomAccessFile randomAccessFile = null;
            if (factoryData.fileName != null) {
                file = new File(string);
                if (!factoryData.append) {
                    file.delete();
                }
                long l3 = l = factoryData.append ? file.length() : 0L;
                if (file.exists()) {
                    l2 = file.lastModified();
                }
                try {
                    FileUtils.makeParentDirs(file);
                    randomAccessFile = new RandomAccessFile(string, "rw");
                    if (factoryData.append) {
                        long l4 = randomAccessFile.length();
                        LOGGER.trace("RandomAccessFile {} seek to {}", (Object)string, (Object)l4);
                        randomAccessFile.seek(l4);
                    } else {
                        LOGGER.trace("RandomAccessFile {} set length to 0", (Object)string);
                        randomAccessFile.setLength(0L);
                    }
                }
                catch (IOException iOException) {
                    LOGGER.error("Cannot access RandomAccessFile ".concat(String.valueOf(iOException)), (Throwable)iOException);
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        }
                        catch (IOException iOException2) {
                            LOGGER.error("Cannot close RandomAccessFile {}", (Object)string, (Object)iOException2);
                        }
                    }
                    return null;
                }
            }
            boolean bl = !factoryData.append || file == null || !file.exists();
            RollingRandomAccessFileManager rollingRandomAccessFileManager = new RollingRandomAccessFileManager(factoryData.getLoggerContext(), randomAccessFile, string, factoryData.pattern, NullOutputStream.getInstance(), factoryData.append, factoryData.immediateFlush, factoryData.bufferSize, l, l2, factoryData.policy, factoryData.strategy, factoryData.advertiseURI, factoryData.layout, factoryData.filePermissions, factoryData.fileOwner, factoryData.fileGroup, bl);
            if (rollingRandomAccessFileManager.isAttributeViewEnabled()) {
                rollingRandomAccessFileManager.defineAttributeView(file.toPath());
            }
            return rollingRandomAccessFileManager;
        }
    }
}

