/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.NetUtils;
import org.apache.logging.log4j.core.util.Patterns;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

public final class UuidUtil {
    public static final String UUID_SEQUENCE = "org.apache.logging.log4j.uuidSequence";
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String ASSIGNED_SEQUENCES = "org.apache.logging.log4j.assignedSequences";
    private static final AtomicInteger COUNT = new AtomicInteger(0);
    private static final long TYPE1 = 4096L;
    private static final byte VARIANT = -128;
    private static final int SEQUENCE_MASK = 16383;
    private static final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 122192928000000000L;
    private static final long INITIAL_UUID_SEQNO = PropertiesUtil.getProperties().getLongProperty("org.apache.logging.log4j.uuidSequence", 0L);
    private static final long LOW_MASK = 0xFFFFFFFFL;
    private static final long MID_MASK = 0xFFFF00000000L;
    private static final long HIGH_MASK = 0xFFF000000000000L;
    private static final int NODE_SIZE = 8;
    private static final int SHIFT_2 = 16;
    private static final int SHIFT_4 = 32;
    private static final int SHIFT_6 = 48;
    private static final int HUNDRED_NANOS_PER_MILLI = 10000;
    private static final long LEAST = UuidUtil.initialize(NetUtils.getMacAddress());

    private UuidUtil() {
    }

    static long initialize(byte[] object) {
        int n;
        long[] lArray;
        SecureRandom secureRandom = new SecureRandom();
        if (object == null || ((byte[])object).length == 0) {
            object = new byte[6];
            ((Random)secureRandom).nextBytes((byte[])object);
        }
        int n2 = ((byte[])object).length >= 6 ? 6 : ((byte[])object).length;
        int n3 = ((byte[])object).length >= 6 ? ((byte[])object).length - 6 : 0;
        byte[] byArray = new byte[8];
        Object[] objectArray = byArray;
        byArray[0] = -128;
        objectArray[1] = 0;
        for (int i2 = 2; i2 < 8; ++i2) {
            objectArray[i2] = 0;
        }
        System.arraycopy(object, n3, objectArray, 2, n2);
        ByteBuffer byteBuffer = ByteBuffer.wrap(objectArray);
        long l = INITIAL_UUID_SEQNO;
        String string = PropertiesUtil.getProperties().getStringProperty(ASSIGNED_SEQUENCES);
        object = string;
        if (string == null) {
            lArray = new long[]{};
        } else {
            String[] stringArray = ((String)object).split(Patterns.COMMA_SEPARATOR);
            lArray = new long[stringArray.length];
            int n4 = 0;
            String[] stringArray2 = stringArray;
            n = stringArray.length;
            for (n3 = 0; n3 < n; ++n3) {
                String string2 = stringArray2[n3];
                lArray[n4] = Long.parseLong(string2);
                ++n4;
            }
        }
        if (l == 0L) {
            l = secureRandom.nextLong();
        }
        l &= 0x3FFFL;
        do {
            n3 = 0;
            objectArray = lArray;
            int n5 = lArray.length;
            for (n = 0; n < n5; ++n) {
                if (objectArray[n] != l) continue;
                n3 = 1;
                break;
            }
            if (n3 == 0) continue;
            l = l + 1L & 0x3FFFL;
        } while (n3 != 0);
        object = object == null ? Long.toString(l) : (String)object + ',' + Long.toString(l);
        System.setProperty(ASSIGNED_SEQUENCES, (String)object);
        return byteBuffer.getLong() | l << 48;
    }

    public static UUID getTimeBasedUuid() {
        long l = System.currentTimeMillis() * 10000L + 122192928000000000L + (long)(COUNT.incrementAndGet() % 10000);
        long l2 = (l & 0xFFFFFFFFL) << 32;
        long l3 = (l & 0xFFFF00000000L) >> 16;
        long l4 = (l & 0xFFF000000000000L) >> 48;
        long l5 = l2 | l3 | 0x1000L | l4;
        return new UUID(l5, LEAST);
    }
}

