/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ConfigurationFactoryData;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.FileUtils;

public class FileManager
extends OutputStreamManager {
    private static final FileManagerFactory FACTORY = new FileManagerFactory();
    private final boolean isAppend;
    private final boolean createOnDemand;
    private final boolean isLocking;
    private final String advertiseURI;
    private final int bufferSize;
    private final Set<PosixFilePermission> filePermissions;
    private final String fileOwner;
    private final String fileGroup;
    private final boolean attributeViewEnabled;

    @Deprecated
    protected FileManager(String string, OutputStream outputStream, boolean bl, boolean bl2, String string2, Layout<? extends Serializable> layout, int n, boolean bl3) {
        this(string, outputStream, bl, bl2, string2, layout, bl3, ByteBuffer.wrap(new byte[n]));
    }

    @Deprecated
    public FileManager(String string, OutputStream outputStream, boolean bl, boolean bl2, String string2, Layout<? extends Serializable> layout, boolean bl3, ByteBuffer byteBuffer) {
        super(outputStream, string, layout, bl3, byteBuffer);
        this.isAppend = bl;
        this.createOnDemand = false;
        this.isLocking = bl2;
        this.advertiseURI = string2;
        this.bufferSize = byteBuffer.capacity();
        this.filePermissions = null;
        this.fileOwner = null;
        this.fileGroup = null;
        this.attributeViewEnabled = false;
    }

    @Deprecated
    public FileManager(LoggerContext loggerContext, String string, OutputStream outputStream, boolean bl, boolean bl2, boolean bl3, String string2, Layout<? extends Serializable> layout, boolean bl4, ByteBuffer byteBuffer) {
        super(loggerContext, outputStream, string, bl3, layout, bl4, byteBuffer);
        this.isAppend = bl;
        this.createOnDemand = bl3;
        this.isLocking = bl2;
        this.advertiseURI = string2;
        this.bufferSize = byteBuffer.capacity();
        this.filePermissions = null;
        this.fileOwner = null;
        this.fileGroup = null;
        this.attributeViewEnabled = false;
    }

    public FileManager(LoggerContext object, String string, OutputStream outputStream, boolean bl, boolean bl2, boolean bl3, String string2, Layout<? extends Serializable> layout, String string3, String string4, String string5, boolean bl4, ByteBuffer byteBuffer) {
        super((LoggerContext)object, outputStream, string, bl3, layout, bl4, byteBuffer);
        this.isAppend = bl;
        this.createOnDemand = bl3;
        this.isLocking = bl2;
        this.advertiseURI = string2;
        this.bufferSize = byteBuffer.capacity();
        object = FileSystems.getDefault().supportedFileAttributeViews();
        if (object.contains("posix")) {
            this.filePermissions = string3 != null ? PosixFilePermissions.fromString(string3) : null;
            this.fileGroup = string5;
        } else {
            this.filePermissions = null;
            this.fileGroup = null;
            if (string3 != null) {
                LOGGER.warn("Posix file attribute permissions defined but it is not supported by this files system.");
            }
            if (string5 != null) {
                LOGGER.warn("Posix file attribute group defined but it is not supported by this files system.");
            }
        }
        if (object.contains("owner")) {
            this.fileOwner = string4;
        } else {
            this.fileOwner = null;
            if (string4 != null) {
                LOGGER.warn("Owner file attribute defined but it is not supported by this files system.");
            }
        }
        this.attributeViewEnabled = this.filePermissions != null || this.fileOwner != null || this.fileGroup != null;
    }

    public static FileManager getFileManager(String string, boolean bl, boolean bl2, boolean bl3, boolean bl4, String string2, Layout<? extends Serializable> layout, int n, String string3, String string4, String string5, Configuration configuration) {
        if (bl2 && bl3) {
            bl2 = false;
        }
        return FileManager.narrow(FileManager.class, FileManager.getManager(string, new FactoryData(bl, bl2, bl3, n, bl4, string2, layout, string3, string4, string5, configuration), FACTORY));
    }

    @Override
    protected OutputStream createOutputStream() {
        String string = this.getFileName();
        LOGGER.debug("Now writing to {} at {}", (Object)string, (Object)new Date());
        File file = new File(string);
        FileOutputStream fileOutputStream = new FileOutputStream(file, this.isAppend);
        if (file.exists() && file.length() == 0L) {
            try {
                FileTime fileTime = FileTime.fromMillis(System.currentTimeMillis());
                Files.setAttribute(file.toPath(), "creationTime", fileTime, new LinkOption[0]);
            }
            catch (Exception exception) {
                LOGGER.warn("Unable to set current file tiem for {}", (Object)string);
            }
            this.writeHeader(fileOutputStream);
        }
        this.defineAttributeView(Paths.get(string, new String[0]));
        return fileOutputStream;
    }

    public void defineAttributeView(Path path) {
        if (this.attributeViewEnabled) {
            try {
                path.toFile().createNewFile();
                FileUtils.defineFilePosixAttributeView(path, this.filePermissions, this.fileOwner, this.fileGroup);
                return;
            }
            catch (Exception exception) {
                LOGGER.error("Could not define attribute view on path \"{}\" got {}", (Object)path, (Object)exception.getMessage(), (Object)exception);
            }
        }
    }

    /*
     * WARNING - void declaration
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public synchronized void write(byte[] object, int n, int n2, boolean bl) {
        void var4_7;
        void var3_6;
        void var2_4;
        if (this.isLocking) {
            try {
                FileLock fileLock = ((FileOutputStream)this.getOutputStream()).getChannel().lock(0L, Long.MAX_VALUE, false);
                Throwable throwable = null;
                try {
                    super.write((byte[])object, (int)var2_4, (int)var3_6, (boolean)var4_7);
                    if (fileLock == null) return;
                }
                catch (Throwable throwable2) {
                    try {
                        Throwable throwable3 = throwable2;
                        throwable = throwable2;
                        throw throwable3;
                    }
                    catch (Throwable throwable4) {
                        if (fileLock == null) throw throwable4;
                        if (throwable != null) {
                            try {
                                fileLock.close();
                                throw throwable4;
                            }
                            catch (Throwable throwable5) {
                                throwable.addSuppressed(throwable5);
                            }
                            throw throwable4;
                        } else {
                            fileLock.close();
                        }
                        throw throwable4;
                    }
                }
                fileLock.close();
                return;
            }
            catch (IOException iOException) {
                throw new AppenderLoggingException("Unable to obtain lock on " + this.getName(), iOException);
            }
        }
        super.write((byte[])object, (int)var2_4, (int)var3_6, (boolean)var4_7);
    }

    /*
     * WARNING - void declaration
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public synchronized void writeToDestination(byte[] object, int n, int n2) {
        void var3_6;
        void var2_4;
        if (this.isLocking) {
            try {
                FileLock fileLock = ((FileOutputStream)this.getOutputStream()).getChannel().lock(0L, Long.MAX_VALUE, false);
                Throwable throwable = null;
                try {
                    super.writeToDestination((byte[])object, (int)var2_4, (int)var3_6);
                    if (fileLock == null) return;
                }
                catch (Throwable throwable2) {
                    try {
                        Throwable throwable3 = throwable2;
                        throwable = throwable2;
                        throw throwable3;
                    }
                    catch (Throwable throwable4) {
                        if (fileLock == null) throw throwable4;
                        if (throwable != null) {
                            try {
                                fileLock.close();
                                throw throwable4;
                            }
                            catch (Throwable throwable5) {
                                throwable.addSuppressed(throwable5);
                            }
                            throw throwable4;
                        } else {
                            fileLock.close();
                        }
                        throw throwable4;
                    }
                }
                fileLock.close();
                return;
            }
            catch (IOException iOException) {
                throw new AppenderLoggingException("Unable to obtain lock on " + this.getName(), iOException);
            }
        }
        super.writeToDestination((byte[])object, (int)var2_4, (int)var3_6);
    }

    public String getFileName() {
        return this.getName();
    }

    public boolean isAppend() {
        return this.isAppend;
    }

    public boolean isCreateOnDemand() {
        return this.createOnDemand;
    }

    public boolean isLocking() {
        return this.isLocking;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public Set<PosixFilePermission> getFilePermissions() {
        return this.filePermissions;
    }

    public String getFileOwner() {
        return this.fileOwner;
    }

    public String getFileGroup() {
        return this.fileGroup;
    }

    public boolean isAttributeViewEnabled() {
        return this.attributeViewEnabled;
    }

    @Override
    public Map<String, String> getContentFormat() {
        HashMap<String, String> hashMap = new HashMap<String, String>(super.getContentFormat());
        hashMap.put("fileURI", this.advertiseURI);
        return hashMap;
    }

    static class FileManagerFactory
    implements ManagerFactory<FileManager, FactoryData> {
        private FileManagerFactory() {
        }

        @Override
        public FileManager createManager(String string, FactoryData object) {
            File file = new File(string);
            try {
                FileUtils.makeParentDirs(file);
                boolean bl = !((FactoryData)object).append || !file.exists();
                ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[((FactoryData)object).bufferedIo ? ((FactoryData)object).bufferSize : Constants.ENCODER_BYTE_BUFFER_SIZE]);
                FileOutputStream fileOutputStream = ((FactoryData)object).createOnDemand ? null : new FileOutputStream(file, ((FactoryData)object).append);
                object = new FileManager(((ConfigurationFactoryData)object).getLoggerContext(), string, fileOutputStream, ((FactoryData)object).append, ((FactoryData)object).locking, ((FactoryData)object).createOnDemand, ((FactoryData)object).advertiseURI, ((FactoryData)object).layout, ((FactoryData)object).filePermissions, ((FactoryData)object).fileOwner, ((FactoryData)object).fileGroup, bl, byteBuffer);
                if (fileOutputStream != null && ((FileManager)object).attributeViewEnabled) {
                    ((FileManager)object).defineAttributeView(file.toPath());
                }
                return object;
            }
            catch (IOException iOException) {
                AbstractManager.LOGGER.error("FileManager (" + string + ") " + iOException, (Throwable)iOException);
                return null;
            }
        }
    }

    static class FactoryData
    extends ConfigurationFactoryData {
        private final boolean append;
        private final boolean locking;
        private final boolean bufferedIo;
        private final int bufferSize;
        private final boolean createOnDemand;
        private final String advertiseURI;
        private final Layout<? extends Serializable> layout;
        private final String filePermissions;
        private final String fileOwner;
        private final String fileGroup;

        public FactoryData(boolean bl, boolean bl2, boolean bl3, int n, boolean bl4, String string, Layout<? extends Serializable> layout, String string2, String string3, String string4, Configuration configuration) {
            super(configuration);
            this.append = bl;
            this.locking = bl2;
            this.bufferedIo = bl3;
            this.bufferSize = n;
            this.createOnDemand = bl4;
            this.advertiseURI = string;
            this.layout = layout;
            this.filePermissions = string2;
            this.fileOwner = string3;
            this.fileGroup = string4;
        }
    }
}

