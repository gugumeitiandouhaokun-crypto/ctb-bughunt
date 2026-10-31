/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.logging.log4j.core.appender.rolling.action.AbstractAction;

public final class ZipCompressAction
extends AbstractAction {
    private static final int BUF_SIZE = 8192;
    private final File source;
    private final File destination;
    private final boolean deleteSource;
    private final int level;

    public ZipCompressAction(File file, File file2, boolean bl, int n) {
        Objects.requireNonNull(file, "source");
        Objects.requireNonNull(file2, "destination");
        this.source = file;
        this.destination = file2;
        this.deleteSource = bl;
        this.level = n;
    }

    @Override
    public final boolean execute() {
        return ZipCompressAction.execute(this.source, this.destination, this.deleteSource, this.level);
    }

    public static boolean execute(File file, File object, boolean bl, int n) {
        if (file.exists()) {
            FileInputStream fileInputStream = new FileInputStream(file);
            Throwable throwable = null;
            try {
                object = new ZipOutputStream(new FileOutputStream((File)object));
                Throwable throwable2 = null;
                try {
                    int n2;
                    ((ZipOutputStream)object).setLevel(n);
                    Object object2 = new ZipEntry(file.getName());
                    ((ZipOutputStream)object).putNextEntry((ZipEntry)object2);
                    object2 = new byte[8192];
                    while ((n2 = fileInputStream.read((byte[])object2)) != -1) {
                        ((ZipOutputStream)object).write((byte[])object2, 0, n2);
                    }
                }
                catch (Throwable throwable3) {
                    try {
                        Throwable throwable4 = throwable3;
                        throwable2 = throwable3;
                        throw throwable4;
                    }
                    catch (Throwable throwable5) {
                        if (throwable2 != null) {
                            try {
                                ((ZipOutputStream)object).close();
                            }
                            catch (Throwable throwable6) {
                                throwable2.addSuppressed(throwable6);
                            }
                        } else {
                            ((ZipOutputStream)object).close();
                        }
                        throw throwable5;
                    }
                }
                ((ZipOutputStream)object).close();
            }
            catch (Throwable throwable7) {
                try {
                    object = throwable7;
                    throwable = throwable7;
                    throw object;
                }
                catch (Throwable throwable8) {
                    if (throwable != null) {
                        try {
                            fileInputStream.close();
                        }
                        catch (Throwable throwable9) {
                            throwable.addSuppressed(throwable9);
                        }
                    } else {
                        fileInputStream.close();
                    }
                    throw throwable8;
                }
            }
            fileInputStream.close();
            if (bl && !file.delete()) {
                LOGGER.warn("Unable to delete " + file.toString() + '.');
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
        return ZipCompressAction.class.getSimpleName() + '[' + this.source + " to " + this.destination + ", level=" + this.level + ", deleteSource=" + this.deleteSource + ']';
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

    public final int getLevel() {
        return this.level;
    }
}

