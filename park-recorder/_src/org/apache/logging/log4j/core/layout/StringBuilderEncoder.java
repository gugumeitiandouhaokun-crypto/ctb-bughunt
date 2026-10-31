/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.layout;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Objects;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.core.layout.Encoder;
import org.apache.logging.log4j.core.layout.TextEncoderHelper;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.status.StatusLogger;

public class StringBuilderEncoder
implements Encoder<StringBuilder> {
    private static final int DEFAULT_BYTE_BUFFER_SIZE = 8192;
    private final ThreadLocal<Object[]> threadLocal = new ThreadLocal();
    private final Charset charset;
    private final int charBufferSize;
    private final int byteBufferSize;

    public StringBuilderEncoder(Charset charset) {
        this(charset, Constants.ENCODER_CHAR_BUFFER_SIZE, 8192);
    }

    public StringBuilderEncoder(Charset charset, int n, int n2) {
        this.charBufferSize = n;
        this.byteBufferSize = n2;
        this.charset = Objects.requireNonNull(charset, "charset");
    }

    @Override
    public void encode(StringBuilder stringBuilder, ByteBufferDestination byteBufferDestination) {
        try {
            Object object = this.getThreadLocalState();
            CharsetEncoder charsetEncoder = (CharsetEncoder)object[0];
            CharBuffer charBuffer = (CharBuffer)object[1];
            object = (ByteBuffer)object[2];
            TextEncoderHelper.encodeText(charsetEncoder, charBuffer, (ByteBuffer)object, stringBuilder, byteBufferDestination);
            return;
        }
        catch (Exception exception) {
            this.logEncodeTextException(exception, stringBuilder, byteBufferDestination);
            TextEncoderHelper.encodeTextFallBack(this.charset, stringBuilder, byteBufferDestination);
            return;
        }
    }

    private Object[] getThreadLocalState() {
        Object[] objectArray = this.threadLocal.get();
        if (objectArray == null) {
            objectArray = new Object[]{this.charset.newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE), CharBuffer.allocate(this.charBufferSize), ByteBuffer.allocate(this.byteBufferSize)};
            this.threadLocal.set(objectArray);
        } else {
            ((CharsetEncoder)objectArray[0]).reset();
            ((CharBuffer)objectArray[1]).clear();
            ((ByteBuffer)objectArray[2]).clear();
        }
        return objectArray;
    }

    private void logEncodeTextException(Exception exception, StringBuilder stringBuilder, ByteBufferDestination byteBufferDestination) {
        Exception exception2 = exception;
        StatusLogger.getLogger().error("Recovering from StringBuilderEncoder.encode('{}') error: {}", (Object)stringBuilder, (Object)exception2, (Object)exception2);
    }
}

