/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.layout;

import java.nio.ByteBuffer;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;

public final class ByteBufferDestinationHelper {
    private ByteBufferDestinationHelper() {
    }

    public static void writeToUnsynchronized(ByteBuffer byteBuffer, ByteBufferDestination byteBufferDestination) {
        ByteBuffer byteBuffer2 = byteBufferDestination.getByteBuffer();
        while (byteBuffer.remaining() > byteBuffer2.remaining()) {
            int n = byteBuffer.limit();
            ByteBuffer byteBuffer3 = byteBuffer;
            byteBuffer3.limit(Math.min(byteBuffer3.limit(), byteBuffer.position() + byteBuffer2.remaining()));
            byteBuffer2.put(byteBuffer);
            byteBuffer.limit(n);
            byteBuffer2 = byteBufferDestination.drain(byteBuffer2);
        }
        byteBuffer2.put(byteBuffer);
    }

    public static void writeToUnsynchronized(byte[] byArray, int n, int n2, ByteBufferDestination byteBufferDestination) {
        ByteBuffer byteBuffer = byteBufferDestination.getByteBuffer();
        while (n2 > byteBuffer.remaining()) {
            int n3 = byteBuffer.remaining();
            byteBuffer.put(byArray, n, n3);
            n += n3;
            n2 -= n3;
            byteBuffer = byteBufferDestination.drain(byteBuffer);
        }
        byteBuffer.put(byArray, n, n2);
    }
}

