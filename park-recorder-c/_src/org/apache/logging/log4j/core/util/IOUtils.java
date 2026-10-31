/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.io.Reader;
import java.io.Writer;
import org.apache.logging.log4j.core.util.StringBuilderWriter;

public class IOUtils {
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final int EOF = -1;

    public static int copy(Reader reader, Writer writer) {
        long l = IOUtils.copyLarge(reader, writer);
        if (l > Integer.MAX_VALUE) {
            return -1;
        }
        return (int)l;
    }

    public static long copyLarge(Reader reader, Writer writer) {
        return IOUtils.copyLarge(reader, writer, new char[4096]);
    }

    public static long copyLarge(Reader reader, Writer writer, char[] cArray) {
        int n;
        long l = 0L;
        while (-1 != (n = reader.read(cArray))) {
            writer.write(cArray, 0, n);
            l += (long)n;
        }
        return l;
    }

    public static String toString(Reader reader) {
        StringBuilderWriter stringBuilderWriter = new StringBuilderWriter();
        IOUtils.copy(reader, stringBuilderWriter);
        return stringBuilderWriter.toString();
    }
}

