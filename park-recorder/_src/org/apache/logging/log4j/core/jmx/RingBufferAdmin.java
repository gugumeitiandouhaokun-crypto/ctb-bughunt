/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.lmax.disruptor.RingBuffer
 */
package org.apache.logging.log4j.core.jmx;

import com.lmax.disruptor.RingBuffer;
import javax.management.ObjectName;
import org.apache.logging.log4j.core.jmx.RingBufferAdminMBean;
import org.apache.logging.log4j.core.jmx.Server;

public class RingBufferAdmin
implements RingBufferAdminMBean {
    private final RingBuffer<?> ringBuffer;
    private final ObjectName objectName;

    public static RingBufferAdmin forAsyncLogger(RingBuffer<?> ringBuffer, String string) {
        string = Server.escape(string);
        string = String.format("org.apache.logging.log4j2:type=%s,component=AsyncLoggerRingBuffer", string);
        return new RingBufferAdmin(ringBuffer, string);
    }

    public static RingBufferAdmin forAsyncLoggerConfig(RingBuffer<?> ringBuffer, String string, String string2) {
        string = Server.escape(string);
        string2 = Server.escape(string2);
        string = String.format("org.apache.logging.log4j2:type=%s,component=Loggers,name=%s,subtype=RingBuffer", string, string2);
        return new RingBufferAdmin(ringBuffer, string);
    }

    protected RingBufferAdmin(RingBuffer<?> ringBuffer, String string) {
        this.ringBuffer = ringBuffer;
        try {
            this.objectName = new ObjectName(string);
            return;
        }
        catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Override
    public long getBufferSize() {
        if (this.ringBuffer == null) {
            return 0L;
        }
        return this.ringBuffer.getBufferSize();
    }

    @Override
    public long getRemainingCapacity() {
        if (this.ringBuffer == null) {
            return 0L;
        }
        return this.ringBuffer.remainingCapacity();
    }

    public ObjectName getObjectName() {
        return this.objectName;
    }
}

