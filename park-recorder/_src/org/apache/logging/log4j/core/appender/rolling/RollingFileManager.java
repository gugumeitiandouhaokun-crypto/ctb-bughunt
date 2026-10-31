/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LifeCycle2;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConfigurationFactoryData;
import org.apache.logging.log4j.core.appender.FileManager;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.rolling.DirectFileRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.DirectWriteRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.FileExtension;
import org.apache.logging.log4j.core.appender.rolling.PatternProcessor;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.action.AbstractAction;
import org.apache.logging.log4j.core.appender.rolling.action.Action;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.FileUtils;
import org.apache.logging.log4j.core.util.Log4jThreadFactory;

public class RollingFileManager
extends FileManager {
    private static RollingFileManagerFactory factory = new RollingFileManagerFactory();
    private static final int MAX_TRIES = 3;
    private static final int MIN_DURATION = 100;
    private static final FileTime EPOCH = FileTime.fromMillis(0L);
    protected long size;
    private long initialTime;
    private volatile PatternProcessor patternProcessor;
    private final Semaphore semaphore = new Semaphore(1);
    private final Log4jThreadFactory threadFactory = Log4jThreadFactory.createThreadFactory("RollingFileManager");
    private volatile TriggeringPolicy triggeringPolicy;
    private volatile RolloverStrategy rolloverStrategy;
    private volatile boolean renameEmptyFiles = false;
    private volatile boolean initialized = false;
    private volatile String fileName;
    private final boolean directWrite;
    private final ExecutorService asyncExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0L, TimeUnit.MILLISECONDS, (BlockingQueue<Runnable>)new EmptyQueue(), this.threadFactory);
    private static final AtomicReferenceFieldUpdater<RollingFileManager, TriggeringPolicy> triggeringPolicyUpdater = AtomicReferenceFieldUpdater.newUpdater(RollingFileManager.class, TriggeringPolicy.class, "triggeringPolicy");
    private static final AtomicReferenceFieldUpdater<RollingFileManager, RolloverStrategy> rolloverStrategyUpdater = AtomicReferenceFieldUpdater.newUpdater(RollingFileManager.class, RolloverStrategy.class, "rolloverStrategy");
    private static final AtomicReferenceFieldUpdater<RollingFileManager, PatternProcessor> patternProcessorUpdater = AtomicReferenceFieldUpdater.newUpdater(RollingFileManager.class, PatternProcessor.class, "patternProcessor");

    @Deprecated
    protected RollingFileManager(String string, String string2, OutputStream outputStream, boolean bl, long l, long l2, TriggeringPolicy triggeringPolicy, RolloverStrategy rolloverStrategy, String string3, Layout<? extends Serializable> layout, int n, boolean bl2) {
        this(string, string2, outputStream, bl, l, l2, triggeringPolicy, rolloverStrategy, string3, layout, bl2, ByteBuffer.wrap(new byte[Constants.ENCODER_BYTE_BUFFER_SIZE]));
    }

    @Deprecated
    protected RollingFileManager(String string, String string2, OutputStream outputStream, boolean bl, long l, long l2, TriggeringPolicy triggeringPolicy, RolloverStrategy rolloverStrategy, String string3, Layout<? extends Serializable> layout, boolean bl2, ByteBuffer byteBuffer) {
        super(string != null ? string : string2, outputStream, bl, false, string3, layout, bl2, byteBuffer);
        this.size = l;
        this.initialTime = l2;
        this.triggeringPolicy = triggeringPolicy;
        this.rolloverStrategy = rolloverStrategy;
        this.patternProcessor = new PatternProcessor(string2);
        this.patternProcessor.setPrevFileTime(l2);
        this.fileName = string;
        this.directWrite = rolloverStrategy instanceof DirectWriteRolloverStrategy;
    }

    @Deprecated
    protected RollingFileManager(LoggerContext loggerContext, String string, String string2, OutputStream outputStream, boolean bl, boolean bl2, long l, long l2, TriggeringPolicy triggeringPolicy, RolloverStrategy rolloverStrategy, String string3, Layout<? extends Serializable> layout, boolean bl3, ByteBuffer byteBuffer) {
        super(loggerContext, string != null ? string : string2, outputStream, bl, false, bl2, string3, layout, bl3, byteBuffer);
        this.size = l;
        this.initialTime = l2;
        this.triggeringPolicy = triggeringPolicy;
        this.rolloverStrategy = rolloverStrategy;
        this.patternProcessor = new PatternProcessor(string2);
        this.patternProcessor.setPrevFileTime(l2);
        this.fileName = string;
        this.directWrite = rolloverStrategy instanceof DirectWriteRolloverStrategy;
    }

    protected RollingFileManager(LoggerContext loggerContext, String string, String string2, OutputStream outputStream, boolean bl, boolean bl2, long l, long l2, TriggeringPolicy triggeringPolicy, RolloverStrategy rolloverStrategy, String string3, Layout<? extends Serializable> layout, String string4, String string5, String string6, boolean bl3, ByteBuffer byteBuffer) {
        super(loggerContext, string != null ? string : string2, outputStream, bl, false, bl2, string3, layout, string4, string5, string6, bl3, byteBuffer);
        this.size = l;
        this.initialTime = l2;
        this.patternProcessor = new PatternProcessor(string2);
        this.patternProcessor.setPrevFileTime(l2);
        this.triggeringPolicy = triggeringPolicy;
        this.rolloverStrategy = rolloverStrategy;
        this.fileName = string;
        this.directWrite = rolloverStrategy instanceof DirectFileRolloverStrategy;
    }

    public void initialize() {
        if (!this.initialized) {
            LOGGER.debug("Initializing triggering policy {}", (Object)this.triggeringPolicy);
            this.initialized = true;
            this.triggeringPolicy.initialize(this);
            if (this.triggeringPolicy instanceof LifeCycle) {
                ((LifeCycle)((Object)this.triggeringPolicy)).start();
            }
            if (this.directWrite) {
                File file = new File(this.getFileName());
                if (file.exists()) {
                    this.size = file.length();
                    return;
                }
                ((DirectFileRolloverStrategy)((Object)this.rolloverStrategy)).clearCurrentFileName();
            }
        }
    }

    public static RollingFileManager getFileManager(String string, String string2, boolean bl, boolean bl2, TriggeringPolicy triggeringPolicy, RolloverStrategy rolloverStrategy, String string3, Layout<? extends Serializable> layout, int n, boolean bl3, boolean bl4, String string4, String string5, String string6, Configuration configuration) {
        if (rolloverStrategy instanceof DirectWriteRolloverStrategy && string != null) {
            LOGGER.error("The fileName attribute must not be specified with the DirectWriteRolloverStrategy");
            return null;
        }
        String string7 = string == null ? string2 : string;
        return RollingFileManager.narrow(RollingFileManager.class, RollingFileManager.getManager(string7, new FactoryData(string, string2, bl, bl2, triggeringPolicy, rolloverStrategy, string3, layout, n, bl3, bl4, string4, string5, string6, configuration), factory));
    }

    @Override
    public String getFileName() {
        if (this.directWrite) {
            this.fileName = ((DirectFileRolloverStrategy)((Object)this.rolloverStrategy)).getCurrentFileName(this);
        }
        return this.fileName;
    }

    public boolean isDirectWrite() {
        return this.directWrite;
    }

    public FileExtension getFileExtension() {
        return this.patternProcessor.getFileExtension();
    }

    @Override
    public synchronized void write(byte[] byArray, int n, int n2, boolean bl) {
        super.write(byArray, n, n2, bl);
    }

    @Override
    public synchronized void writeToDestination(byte[] byArray, int n, int n2) {
        this.size += (long)n2;
        super.writeToDestination(byArray, n, n2);
    }

    public boolean isRenameEmptyFiles() {
        return this.renameEmptyFiles;
    }

    public void setRenameEmptyFiles(boolean bl) {
        this.renameEmptyFiles = bl;
    }

    public long getFileSize() {
        return this.size + (long)this.byteBuffer.position();
    }

    public long getFileTime() {
        return this.initialTime;
    }

    public synchronized void checkRollover(LogEvent logEvent) {
        if (this.triggeringPolicy.isTriggeringEvent(logEvent)) {
            this.rollover();
        }
    }

    @Override
    public boolean releaseSub(long l, TimeUnit timeUnit) {
        boolean bl;
        block13: {
            LOGGER.debug("Shutting down RollingFileManager {}", (Object)this.getName());
            bl = true;
            if (this.triggeringPolicy instanceof LifeCycle2) {
                bl = true & ((LifeCycle2)((Object)this.triggeringPolicy)).stop(l, timeUnit);
            } else if (this.triggeringPolicy instanceof LifeCycle) {
                ((LifeCycle)((Object)this.triggeringPolicy)).stop();
                bl = true;
            }
            bl = super.releaseSub(l, timeUnit) && bl;
            this.asyncExecutor.shutdown();
            try {
                long l2 = timeUnit.toMillis(l);
                long l3 = 100L < l2 ? l2 : 100L;
                for (int j = 1; j <= 3 && !this.asyncExecutor.isTerminated(); ++j) {
                    this.asyncExecutor.awaitTermination(l3 * (long)j, TimeUnit.MILLISECONDS);
                }
                if (this.asyncExecutor.isTerminated()) {
                    LOGGER.debug("All asynchronous threads have terminated");
                    break block13;
                }
                this.asyncExecutor.shutdownNow();
                try {
                    this.asyncExecutor.awaitTermination(l, timeUnit);
                    if (this.asyncExecutor.isTerminated()) {
                        LOGGER.debug("All asynchronous threads have terminated");
                        break block13;
                    }
                    LOGGER.debug("RollingFileManager shutting down but some asynchronous services may not have completed");
                }
                catch (InterruptedException interruptedException) {
                    LOGGER.warn("RollingFileManager stopped but some asynchronous services may not have completed.");
                }
            }
            catch (InterruptedException interruptedException) {
                this.asyncExecutor.shutdownNow();
                try {
                    this.asyncExecutor.awaitTermination(l, timeUnit);
                    if (this.asyncExecutor.isTerminated()) {
                        LOGGER.debug("All asynchronous threads have terminated");
                    }
                }
                catch (InterruptedException interruptedException2) {
                    LOGGER.warn("RollingFileManager stopped but some asynchronous services may not have completed.");
                }
                Thread.currentThread().interrupt();
            }
        }
        LOGGER.debug("RollingFileManager shutdown completed with status {}", (Object)bl);
        return bl;
    }

    public synchronized void rollover(Date date, Date date2) {
        this.getPatternProcessor().setPrevFileTime(date.getTime());
        this.getPatternProcessor().setCurrentFileTime(date2.getTime());
        this.rollover();
    }

    public synchronized void rollover() {
        if (!this.hasOutputStream() && !this.isCreateOnDemand()) {
            return;
        }
        RollingFileManager rollingFileManager = this;
        if (rollingFileManager.rollover(rollingFileManager.rolloverStrategy)) {
            try {
                this.size = 0L;
                this.initialTime = System.currentTimeMillis();
                this.createFileAfterRollover();
                return;
            }
            catch (IOException iOException) {
                this.logError("Failed to create file after rollover", iOException);
            }
        }
    }

    protected void createFileAfterRollover() {
        RollingFileManager rollingFileManager = this;
        rollingFileManager.setOutputStream(rollingFileManager.createOutputStream());
    }

    public PatternProcessor getPatternProcessor() {
        return this.patternProcessor;
    }

    public void setTriggeringPolicy(TriggeringPolicy triggeringPolicy) {
        RollingFileManager rollingFileManager;
        boolean bl;
        triggeringPolicy.initialize(this);
        TriggeringPolicy triggeringPolicy2 = this.triggeringPolicy;
        int n = 0;
        do {
            rollingFileManager = this;
        } while (!(bl = triggeringPolicyUpdater.compareAndSet(rollingFileManager, rollingFileManager.triggeringPolicy, triggeringPolicy)) && ++n < 3);
        if (bl) {
            if (triggeringPolicy instanceof LifeCycle) {
                ((LifeCycle)((Object)triggeringPolicy)).start();
            }
            if (triggeringPolicy2 instanceof LifeCycle) {
                ((LifeCycle)((Object)triggeringPolicy2)).stop();
                return;
            }
        } else if (triggeringPolicy instanceof LifeCycle) {
            ((LifeCycle)((Object)triggeringPolicy)).stop();
        }
    }

    public void setRolloverStrategy(RolloverStrategy rolloverStrategy) {
        RollingFileManager rollingFileManager = this;
        rolloverStrategyUpdater.compareAndSet(rollingFileManager, rollingFileManager.rolloverStrategy, rolloverStrategy);
    }

    public void setPatternProcessor(PatternProcessor patternProcessor) {
        RollingFileManager rollingFileManager = this;
        patternProcessorUpdater.compareAndSet(rollingFileManager, rollingFileManager.patternProcessor, patternProcessor);
    }

    public <T extends TriggeringPolicy> T getTriggeringPolicy() {
        return (T)this.triggeringPolicy;
    }

    public RolloverStrategy getRolloverStrategy() {
        return this.rolloverStrategy;
    }

    private boolean rollover(RolloverStrategy object) {
        boolean bl;
        try {
            this.semaphore.acquire();
            bl = true;
        }
        catch (InterruptedException interruptedException) {
            this.logError("Thread interrupted while attempting to check rollover", interruptedException);
            return false;
        }
        boolean bl2 = true;
        try {
            object = object.rollover(this);
            if (object != null) {
                this.writeFooter();
                this.closeOutputStream();
                if (object.getSynchronous() != null) {
                    LOGGER.debug("RollingFileManager executing synchronous {}", (Object)object.getSynchronous());
                    try {
                        bl2 = object.getSynchronous().execute();
                    }
                    catch (Exception exception) {
                        bl2 = false;
                        this.logError("Caught error in synchronous task", exception);
                    }
                }
                if (bl2 && object.getAsynchronous() != null) {
                    LOGGER.debug("RollingFileManager executing async {}", (Object)object.getAsynchronous());
                    this.asyncExecutor.execute(new AsyncAction(object.getAsynchronous(), this));
                    bl = false;
                }
                if (bl) {
                    this.semaphore.release();
                }
                return true;
            }
            this.semaphore.release();
            return false;
        }
        catch (Throwable throwable) {
            this.semaphore.release();
            throw throwable;
        }
    }

    @Override
    public void updateData(Object object) {
        object = (FactoryData)object;
        this.setRolloverStrategy(((FactoryData)object).getRolloverStrategy());
        this.setPatternProcessor(new PatternProcessor(((FactoryData)object).getPattern(), this.getPatternProcessor()));
        this.setTriggeringPolicy(((FactoryData)object).getTriggeringPolicy());
    }

    private static long initialFileTime(File file) {
        Comparable<Path> comparable = file.toPath();
        if (Files.exists((Path)comparable, new LinkOption[0])) {
            try {
                comparable = Files.readAttributes((Path)comparable, BasicFileAttributes.class, new LinkOption[0]).creationTime();
                if (((FileTime)comparable).compareTo(EPOCH) > 0) {
                    LOGGER.debug("Returning file creation time for {}", (Object)file.getAbsolutePath());
                    return ((FileTime)comparable).toMillis();
                }
                LOGGER.info("Unable to obtain file creation time for " + file.getAbsolutePath());
            }
            catch (Exception exception) {
                LOGGER.info("Unable to calculate file creation time for " + file.getAbsolutePath() + ": " + exception.getMessage());
            }
        }
        return file.lastModified();
    }

    static class EmptyQueue
    extends ArrayBlockingQueue<Runnable> {
        private static final long serialVersionUID = 1L;

        EmptyQueue() {
            super(1);
        }

        @Override
        public int remainingCapacity() {
            return 0;
        }

        @Override
        public boolean add(Runnable runnable) {
            throw new IllegalStateException("Queue is full");
        }

        @Override
        public void put(Runnable runnable) {
            throw new InterruptedException("Unable to insert into queue");
        }

        @Override
        public boolean offer(Runnable runnable, long l, TimeUnit timeUnit) {
            Thread.sleep(timeUnit.toMillis(l));
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends Runnable> collection) {
            if (collection.size() > 0) {
                throw new IllegalArgumentException("Too many items in collection");
            }
            return false;
        }
    }

    static class RollingFileManagerFactory
    implements ManagerFactory<RollingFileManager, FactoryData> {
        private RollingFileManagerFactory() {
        }

        @Override
        public RollingFileManager createManager(String string, FactoryData object) {
            long l = 0L;
            File file = null;
            if (((FactoryData)object).fileName != null) {
                file = new File(((FactoryData)object).fileName);
                try {
                    FileUtils.makeParentDirs(file);
                    boolean bl = ((FactoryData)object).createOnDemand ? false : file.createNewFile();
                    LOGGER.trace("New file '{}' created = {}", (Object)string, (Object)bl);
                }
                catch (IOException iOException) {
                    LOGGER.error("Unable to create file ".concat(String.valueOf(string)), (Throwable)iOException);
                    return null;
                }
                l = ((FactoryData)object).append ? file.length() : 0L;
            }
            try {
                ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[((FactoryData)object).bufferedIO ? ((FactoryData)object).bufferSize : Constants.ENCODER_BYTE_BUFFER_SIZE]);
                FileOutputStream fileOutputStream = ((FactoryData)object).createOnDemand || ((FactoryData)object).fileName == null ? null : new FileOutputStream(((FactoryData)object).fileName, ((FactoryData)object).append);
                long l2 = file == null || !file.exists() ? 0L : RollingFileManager.initialFileTime(file);
                boolean bl = file != null && file.exists() && file.length() == 0L;
                object = new RollingFileManager(((ConfigurationFactoryData)object).getLoggerContext(), ((FactoryData)object).fileName, ((FactoryData)object).pattern, fileOutputStream, ((FactoryData)object).append, ((FactoryData)object).createOnDemand, l, l2, ((FactoryData)object).policy, ((FactoryData)object).strategy, ((FactoryData)object).advertiseURI, ((FactoryData)object).layout, ((FactoryData)object).filePermissions, ((FactoryData)object).fileOwner, ((FactoryData)object).fileGroup, bl, byteBuffer);
                if (fileOutputStream != null && ((FileManager)object).isAttributeViewEnabled()) {
                    ((RollingFileManager)object).defineAttributeView(file.toPath());
                }
                return object;
            }
            catch (IOException iOException) {
                LOGGER.error("RollingFileManager (" + string + ") " + iOException, (Throwable)iOException);
                return null;
            }
        }
    }

    static class FactoryData
    extends ConfigurationFactoryData {
        private final String fileName;
        private final String pattern;
        private final boolean append;
        private final boolean bufferedIO;
        private final int bufferSize;
        private final boolean immediateFlush;
        private final boolean createOnDemand;
        private final TriggeringPolicy policy;
        private final RolloverStrategy strategy;
        private final String advertiseURI;
        private final Layout<? extends Serializable> layout;
        private final String filePermissions;
        private final String fileOwner;
        private final String fileGroup;

        public FactoryData(String string, String string2, boolean bl, boolean bl2, TriggeringPolicy triggeringPolicy, RolloverStrategy rolloverStrategy, String string3, Layout<? extends Serializable> layout, int n, boolean bl3, boolean bl4, String string4, String string5, String string6, Configuration configuration) {
            super(configuration);
            this.fileName = string;
            this.pattern = string2;
            this.append = bl;
            this.bufferedIO = bl2;
            this.bufferSize = n;
            this.policy = triggeringPolicy;
            this.strategy = rolloverStrategy;
            this.advertiseURI = string3;
            this.layout = layout;
            this.immediateFlush = bl3;
            this.createOnDemand = bl4;
            this.filePermissions = string4;
            this.fileOwner = string5;
            this.fileGroup = string6;
        }

        public TriggeringPolicy getTriggeringPolicy() {
            return this.policy;
        }

        public RolloverStrategy getRolloverStrategy() {
            return this.strategy;
        }

        public String getPattern() {
            return this.pattern;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(super.toString());
            stringBuilder.append("[pattern=");
            stringBuilder.append(this.pattern);
            stringBuilder.append(", append=");
            stringBuilder.append(this.append);
            stringBuilder.append(", bufferedIO=");
            stringBuilder.append(this.bufferedIO);
            stringBuilder.append(", bufferSize=");
            stringBuilder.append(this.bufferSize);
            stringBuilder.append(", policy=");
            stringBuilder.append(this.policy);
            stringBuilder.append(", strategy=");
            stringBuilder.append(this.strategy);
            stringBuilder.append(", advertiseURI=");
            stringBuilder.append(this.advertiseURI);
            stringBuilder.append(", layout=");
            stringBuilder.append(this.layout);
            stringBuilder.append(", filePermissions=");
            stringBuilder.append(this.filePermissions);
            stringBuilder.append(", fileOwner=");
            stringBuilder.append(this.fileOwner);
            stringBuilder.append("]");
            return stringBuilder.toString();
        }
    }

    static class AsyncAction
    extends AbstractAction {
        private final Action action;
        private final RollingFileManager manager;

        public AsyncAction(Action action, RollingFileManager rollingFileManager) {
            this.action = action;
            this.manager = rollingFileManager;
        }

        @Override
        public boolean execute() {
            try {
                boolean bl = this.action.execute();
                return bl;
            }
            finally {
                this.manager.semaphore.release();
            }
        }

        @Override
        public void close() {
            this.action.close();
        }

        @Override
        public boolean isComplete() {
            return this.action.isComplete();
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(super.toString());
            stringBuilder.append("[action=");
            stringBuilder.append(this.action);
            stringBuilder.append(", manager=");
            stringBuilder.append(this.manager);
            stringBuilder.append(", isComplete()=");
            stringBuilder.append(this.isComplete());
            stringBuilder.append(", isInterrupted()=");
            stringBuilder.append(this.isInterrupted());
            stringBuilder.append("]");
            return stringBuilder.toString();
        }
    }
}

