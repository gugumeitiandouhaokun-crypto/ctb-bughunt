/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;
import org.apache.logging.log4j.core.appender.rolling.action.AbstractAction;

public final class GzCompressAction
extends AbstractAction {
    private static final int BUF_SIZE = 8192;
    private final File source;
    private final File destination;
    private final boolean deleteSource;
    private final int compressionLevel;

    public GzCompressAction(File file, File file2, boolean bl, int n) {
        Objects.requireNonNull(file, "source");
        Objects.requireNonNull(file2, "destination");
        this.source = file;
        this.destination = file2;
        this.deleteSource = bl;
        this.compressionLevel = n;
    }

    @Deprecated
    public GzCompressAction(File file, File file2, boolean bl) {
        this(file, file2, bl, -1);
    }

    @Override
    public final boolean execute() {
        return GzCompressAction.execute(this.source, this.destination, this.deleteSource, this.compressionLevel);
    }

    @Deprecated
    public static boolean execute(File file, File file2, boolean bl) {
        return GzCompressAction.execute(file, file2, bl, -1);
    }

    public static boolean execute(File file, File object, boolean bl, int n) {
        if (file.exists()) {
            FileInputStream fileInputStream = new FileInputStream(file);
            Throwable throwable = null;
            try {
                object = new FileOutputStream((File)object);
                Throwable throwable2 = null;
                try {
                    ConfigurableLevelGZIPOutputStream configurableLevelGZIPOutputStream = new ConfigurableLevelGZIPOutputStream((OutputStream)object, 8192, n);
                    Throwable throwable3 = null;
                    try {
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(configurableLevelGZIPOutputStream, 8192);
                        Throwable throwable4 = null;
                        try {
                            int n2;
                            byte[] byArray = new byte[8192];
                            while ((n2 = fileInputStream.read(byArray)) != -1) {
                                ((OutputStream)bufferedOutputStream).write(byArray, 0, n2);
                            }
                        }
                        catch (Throwable throwable5) {
                            try {
                                Throwable throwable6 = throwable5;
                                throwable4 = throwable5;
                                throw throwable6;
                            }
                            catch (Throwable throwable7) {
                                if (throwable4 != null) {
                                    try {
                                        ((OutputStream)bufferedOutputStream).close();
                                    }
                                    catch (Throwable throwable8) {
                                        throwable4.addSuppressed(throwable8);
                                    }
                                } else {
                                    ((OutputStream)bufferedOutputStream).close();
                                }
                                throw throwable7;
                            }
                        }
                        ((OutputStream)bufferedOutputStream).close();
                    }
                    catch (Throwable throwable9) {
                        try {
                            Throwable throwable10 = throwable9;
                            throwable3 = throwable9;
                            throw throwable10;
                        }
                        catch (Throwable throwable11) {
                            if (throwable3 != null) {
                                try {
                                    ((OutputStream)configurableLevelGZIPOutputStream).close();
                                }
                                catch (Throwable throwable12) {
                                    throwable3.addSuppressed(throwable12);
                                }
                            } else {
                                ((OutputStream)configurableLevelGZIPOutputStream).close();
                            }
                            throw throwable11;
                        }
                    }
                    ((OutputStream)configurableLevelGZIPOutputStream).close();
                }
                catch (Throwable throwable13) {
                    try {
                        Throwable throwable14 = throwable13;
                        throwable2 = throwable13;
                        throw throwable14;
                    }
                    catch (Throwable throwable15) {
                        if (throwable2 != null) {
                            try {
                                ((OutputStream)object).close();
                            }
                            catch (Throwable throwable16) {
                                throwable2.addSuppressed(throwable16);
                            }
                        } else {
                            ((OutputStream)object).close();
                        }
                        throw throwable15;
                    }
                }
                ((OutputStream)object).close();
            }
            catch (Throwable throwable17) {
                try {
                    object = throwable17;
                    throwable = throwable17;
                    throw object;
                }
                catch (Throwable throwable18) {
                    if (throwable != null) {
                        try {
                            fileInputStream.close();
                        }
                        catch (Throwable throwable19) {
                            throwable.addSuppressed(throwable19);
                        }
                    } else {
                        fileInputStream.close();
                    }
                    throw throwable18;
                }
            }
            fileInputStream.close();
            if (bl && !file.delete()) {
                LOGGER.warn("Unable to delete {}.", (Object)file);
            }
            return true;
        }
        return false;
    }

    @Override
    protected final void reportException(Exception exception) {
        LOGGER.warn("Exception during compression of '" + this.source.toString() + "'.", (Throwable)exception);
    }

    public final String toString() {
        return GzCompressAction.class.getSimpleName() + '[' + this.source + " to " + this.destination + ", deleteSource=" + this.deleteSource + ']';
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

    static final class ConfigurableLevelGZIPOutputStream
    extends GZIPOutputStream {
        ConfigurableLevelGZIPOutputStream(OutputStream outputStream, int n, int n2) {
            super(outputStream, n);
            this.def.setLevel(n2);
        }
    }
}

