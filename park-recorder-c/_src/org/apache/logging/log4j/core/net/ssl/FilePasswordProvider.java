/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net.ssl;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.apache.logging.log4j.core.net.ssl.PasswordProvider;

class FilePasswordProvider
implements PasswordProvider {
    private final Path passwordPath;

    public FilePasswordProvider(String string) {
        this.passwordPath = Paths.get(string, new String[0]);
        if (!Files.exists(this.passwordPath, new LinkOption[0])) {
            throw new NoSuchFileException("PasswordFile '" + string + "' does not exist");
        }
    }

    @Override
    public char[] getPassword() {
        byte[] byArray = null;
        try {
            byArray = Files.readAllBytes(this.passwordPath);
            Object object = ByteBuffer.wrap(byArray);
            object = Charset.defaultCharset().decode((ByteBuffer)object);
            char[] cArray = new char[((Buffer)object).limit()];
            ((CharBuffer)object).get(cArray, 0, cArray.length);
            ((Buffer)object).rewind();
            ((CharBuffer)object).put(new char[cArray.length]);
            object = cArray;
            return object;
        }
        catch (IOException iOException) {
            throw new IllegalStateException("Could not read password from " + this.passwordPath + ": " + iOException, iOException);
        }
        finally {
            if (byArray != null) {
                Arrays.fill(byArray, (byte)0);
            }
        }
    }
}

