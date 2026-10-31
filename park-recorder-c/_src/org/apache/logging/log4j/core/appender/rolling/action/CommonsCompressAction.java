/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.compress.compressors.CompressorException
 *  org.apache.commons.compress.compressors.CompressorStreamFactory
 *  org.apache.commons.compress.utils.IOUtils
 */
package org.apache.logging.log4j.core.appender.rolling.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Objects;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.core.appender.rolling.action.AbstractAction;

public final class CommonsCompressAction
extends AbstractAction {
    private static final int BUF_SIZE = 8192;
    private final String name;
    private final File source;
    private final File destination;
    private final boolean deleteSource;

    public CommonsCompressAction(String string, File file, File file2, boolean bl) {
        Objects.requireNonNull(file, "source");
        Objects.requireNonNull(file2, "destination");
        this.name = string;
        this.source = file;
        this.destination = file2;
        this.deleteSource = bl;
    }

    @Override
    public final boolean execute() {
        return CommonsCompressAction.execute(this.name, this.source, this.destination, this.deleteSource);
    }

    public static boolean execute(String object, File file, File object2, boolean bl) {
        if (!file.exists()) {
            return false;
        }
        LOGGER.debug("Starting {} compression of {}", object, (Object)file.getPath());
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            Throwable throwable = null;
            try {
                object2 = new BufferedOutputStream((OutputStream)new CompressorStreamFactory().createCompressorOutputStream((String)object, (OutputStream)new FileOutputStream((File)object2)));
                Throwable throwable2 = null;
                try {
                    IOUtils.copy((InputStream)fileInputStream, (OutputStream)object2, (int)8192);
                    LOGGER.debug("Finished {} compression of {}", object, (Object)file.getPath());
                }
                catch (Throwable throwable3) {
                    try {
                        object = throwable3;
                        throwable2 = throwable3;
                        throw object;
                    }
                    catch (Throwable throwable4) {
                        if (throwable2 != null) {
                            try {
                                ((FilterOutputStream)object2).close();
                            }
                            catch (Throwable throwable5) {
                                throwable2.addSuppressed(throwable5);
                            }
                        } else {
                            ((FilterOutputStream)object2).close();
                        }
                        throw throwable4;
                    }
                }
                ((FilterOutputStream)object2).close();
            }
            catch (Throwable throwable6) {
                try {
                    object2 = throwable6;
                    throwable = throwable6;
                    throw object2;
                }
                catch (Throwable throwable7) {
                    if (throwable != null) {
                        try {
                            fileInputStream.close();
                        }
                        catch (Throwable throwable8) {
                            throwable.addSuppressed(throwable8);
                        }
                    } else {
                        fileInputStream.close();
                    }
                    throw throwable7;
                }
            }
            fileInputStream.close();
        }
        catch (CompressorException compressorException) {
            throw new IOException(compressorException);
        }
        if (bl) {
            try {
                if (Files.deleteIfExists(file.toPath())) {
                    LOGGER.debug("Deleted {}", (Object)file.toString());
                } else {
                    LOGGER.warn("Unable to delete {} after {} compression. File did not exist", (Object)file.toString(), object);
                }
            }
            catch (Exception exception) {
                LOGGER.warn("Unable to delete {} after {} compression, {}", (Object)file.toString(), object, (Object)exception.getMessage());
            }
        }
        return true;
    }

    @Override
    protected final void reportException(Exception exception) {
        LOGGER.warn("Exception during " + this.name + " compression of '" + this.source.toString() + "'.", (Throwable)exception);
    }

    public final String toString() {
        return CommonsCompressAction.class.getSimpleName() + '[' + this.source + " to " + this.destination + ", deleteSource=" + this.deleteSource + ']';
    }

    public final String getName() {
        return this.name;
    }

    public final File getSource() {
        return this.source;
    }

    public final File getDestination() {
        return this.destination;
    }

    public final boolean isDeleteSource() {
        return this.deleteSource;
    }
}

