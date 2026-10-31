/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.layout;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.core.layout.ByteBufferDestinationHelper;

public class TextEncoderHelper {
    private TextEncoderHelper() {
    }

    static void encodeTextFallBack(Charset object, StringBuilder stringBuilder, ByteBufferDestination byteBufferDestination) {
        object = stringBuilder.toString().getBytes((Charset)object);
        byteBufferDestination.writeBytes((byte[])object, 0, ((Object)object).length);
    }

    static void encodeText(CharsetEncoder charsetEncoder, CharBuffer charBuffer, ByteBuffer byteBuffer, StringBuilder object, ByteBufferDestination byteBufferDestination) {
        charsetEncoder.reset();
        if (((StringBuilder)object).length() > charBuffer.capacity()) {
            TextEncoderHelper.encodeChunkedText(charsetEncoder, charBuffer, byteBuffer, (StringBuilder)object, byteBufferDestination);
            return;
        }
        charBuffer.clear();
        ((StringBuilder)object).getChars(0, ((StringBuilder)object).length(), charBuffer.array(), charBuffer.arrayOffset());
        charBuffer.limit(((StringBuilder)object).length());
        object = charsetEncoder.encode(charBuffer, byteBuffer, true);
        TextEncoderHelper.writeEncodedText(charsetEncoder, charBuffer, byteBuffer, byteBufferDestination, (CoderResult)object);
    }

    private static void writeEncodedText(CharsetEncoder charsetEncoder, CharBuffer object, ByteBuffer byteBuffer, ByteBufferDestination byteBufferDestination, CoderResult coderResult) {
        if (!coderResult.isUnderflow()) {
            TextEncoderHelper.writeChunkedEncodedText(charsetEncoder, (CharBuffer)object, byteBufferDestination, byteBuffer, coderResult);
            return;
        }
        if (!charsetEncoder.flush(byteBuffer).isUnderflow()) {
            object = byteBufferDestination;
            synchronized (object) {
                TextEncoderHelper.flushRemainingBytes(charsetEncoder, byteBufferDestination, byteBuffer);
                return;
            }
        }
        if (byteBuffer != byteBufferDestination.getByteBuffer()) {
            byteBuffer.flip();
            byteBufferDestination.writeBytes(byteBuffer);
            byteBuffer.clear();
        }
    }

    private static void writeChunkedEncodedText(CharsetEncoder charsetEncoder, CharBuffer charBuffer, ByteBufferDestination byteBufferDestination, ByteBuffer byteBuffer, CoderResult coderResult) {
        ByteBufferDestination byteBufferDestination2 = byteBufferDestination;
        synchronized (byteBufferDestination2) {
            byteBuffer = TextEncoderHelper.writeAndEncodeAsMuchAsPossible(charsetEncoder, charBuffer, true, byteBufferDestination, byteBuffer, coderResult);
            TextEncoderHelper.flushRemainingBytes(charsetEncoder, byteBufferDestination, byteBuffer);
            return;
        }
    }

    private static void encodeChunkedText(CharsetEncoder charsetEncoder, CharBuffer charBuffer, ByteBuffer byteBuffer, StringBuilder stringBuilder, ByteBufferDestination byteBufferDestination) {
        int n = 0;
        CoderResult coderResult = CoderResult.UNDERFLOW;
        boolean bl = false;
        while (!bl && coderResult.isUnderflow()) {
            charBuffer.clear();
            int n2 = TextEncoderHelper.copy(stringBuilder, n, charBuffer);
            bl = (n += n2) >= stringBuilder.length();
            charBuffer.flip();
            coderResult = charsetEncoder.encode(charBuffer, byteBuffer, bl);
        }
        if (bl) {
            TextEncoderHelper.writeEncodedText(charsetEncoder, charBuffer, byteBuffer, byteBufferDestination, coderResult);
            return;
        }
        ByteBufferDestination byteBufferDestination2 = byteBufferDestination;
        synchronized (byteBufferDestination2) {
            byteBuffer = TextEncoderHelper.writeAndEncodeAsMuchAsPossible(charsetEncoder, charBuffer, bl, byteBufferDestination, byteBuffer, coderResult);
            while (!bl) {
                coderResult = CoderResult.UNDERFLOW;
                while (!bl && coderResult.isUnderflow()) {
                    charBuffer.clear();
                    int n3 = TextEncoderHelper.copy(stringBuilder, n, charBuffer);
                    bl = (n += n3) >= stringBuilder.length();
                    charBuffer.flip();
                    coderResult = charsetEncoder.encode(charBuffer, byteBuffer, bl);
                }
                byteBuffer = TextEncoderHelper.writeAndEncodeAsMuchAsPossible(charsetEncoder, charBuffer, bl, byteBufferDestination, byteBuffer, coderResult);
            }
            TextEncoderHelper.flushRemainingBytes(charsetEncoder, byteBufferDestination, byteBuffer);
            return;
        }
    }

    @Deprecated
    public static void encodeText(CharsetEncoder charsetEncoder, CharBuffer charBuffer, ByteBufferDestination byteBufferDestination) {
        charsetEncoder.reset();
        ByteBufferDestination byteBufferDestination2 = byteBufferDestination;
        synchronized (byteBufferDestination2) {
            ByteBuffer byteBuffer = byteBufferDestination.getByteBuffer();
            byteBuffer = TextEncoderHelper.encodeAsMuchAsPossible(charsetEncoder, charBuffer, true, byteBufferDestination, byteBuffer);
            TextEncoderHelper.flushRemainingBytes(charsetEncoder, byteBufferDestination, byteBuffer);
            return;
        }
    }

    private static ByteBuffer writeAndEncodeAsMuchAsPossible(CharsetEncoder charsetEncoder, CharBuffer charBuffer, boolean bl, ByteBufferDestination byteBufferDestination, ByteBuffer byteBuffer, CoderResult coderResult) {
        while (true) {
            byteBuffer = TextEncoderHelper.drainIfByteBufferFull(byteBufferDestination, byteBuffer, coderResult);
            if (!coderResult.isOverflow()) break;
            coderResult = charsetEncoder.encode(charBuffer, byteBuffer, bl);
        }
        if (!coderResult.isUnderflow()) {
            TextEncoderHelper.throwException(coderResult);
        }
        return byteBuffer;
    }

    private static void throwException(CoderResult coderResult) {
        try {
            coderResult.throwException();
            return;
        }
        catch (CharacterCodingException characterCodingException) {
            throw new IllegalStateException(characterCodingException);
        }
    }

    private static ByteBuffer encodeAsMuchAsPossible(CharsetEncoder charsetEncoder, CharBuffer charBuffer, boolean bl, ByteBufferDestination byteBufferDestination, ByteBuffer byteBuffer) {
        CoderResult coderResult;
        do {
            coderResult = charsetEncoder.encode(charBuffer, byteBuffer, bl);
            byteBuffer = TextEncoderHelper.drainIfByteBufferFull(byteBufferDestination, byteBuffer, coderResult);
        } while (coderResult.isOverflow());
        if (!coderResult.isUnderflow()) {
            TextEncoderHelper.throwException(coderResult);
        }
        return byteBuffer;
    }

    private static ByteBuffer drainIfByteBufferFull(ByteBufferDestination byteBufferDestination, ByteBuffer byteBuffer, CoderResult object) {
        if (((CoderResult)object).isOverflow()) {
            object = byteBufferDestination;
            synchronized (object) {
                ByteBuffer byteBuffer2 = byteBufferDestination.getByteBuffer();
                if (byteBuffer2 != byteBuffer) {
                    byteBuffer.flip();
                    ByteBufferDestinationHelper.writeToUnsynchronized(byteBuffer, byteBufferDestination);
                    byteBuffer.clear();
                    return byteBufferDestination.getByteBuffer();
                }
                return byteBufferDestination.drain(byteBuffer2);
            }
        }
        return byteBuffer;
    }

    private static void flushRemainingBytes(CharsetEncoder charsetEncoder, ByteBufferDestination byteBufferDestination, ByteBuffer byteBuffer) {
        CoderResult coderResult;
        do {
            coderResult = charsetEncoder.flush(byteBuffer);
            byteBuffer = TextEncoderHelper.drainIfByteBufferFull(byteBufferDestination, byteBuffer, coderResult);
        } while (coderResult.isOverflow());
        if (!coderResult.isUnderflow()) {
            TextEncoderHelper.throwException(coderResult);
        }
        if (byteBuffer.remaining() > 0 && byteBuffer != byteBufferDestination.getByteBuffer()) {
            byteBuffer.flip();
            ByteBufferDestinationHelper.writeToUnsynchronized(byteBuffer, byteBufferDestination);
            byteBuffer.clear();
        }
    }

    static int copy(StringBuilder stringBuilder, int n, CharBuffer charBuffer) {
        int n2 = Math.min(stringBuilder.length() - n, charBuffer.remaining());
        char[] cArray = charBuffer.array();
        int n3 = charBuffer.position();
        int n4 = n;
        stringBuilder.getChars(n4, n4 + n2, cArray, charBuffer.arrayOffset() + n3);
        charBuffer.position(n3 + n2);
        return n2;
    }
}

