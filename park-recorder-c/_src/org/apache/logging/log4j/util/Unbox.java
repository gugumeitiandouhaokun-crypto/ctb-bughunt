/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Constants;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.PropertiesUtil;

@PerformanceSensitive(value={"allocation"})
public class Unbox {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final int BITS_PER_INT = 32;
    private static final int RINGBUFFER_MIN_SIZE = 32;
    private static final int RINGBUFFER_SIZE = Unbox.calculateRingBufferSize("log4j.unbox.ringbuffer.size");
    private static final int MASK = RINGBUFFER_SIZE - 1;
    private static ThreadLocal<State> threadLocalState = new ThreadLocal();
    private static WebSafeState webSafeState = new WebSafeState();

    private Unbox() {
    }

    private static int calculateRingBufferSize(String string) {
        String string2 = PropertiesUtil.getProperties().getStringProperty(string, "32");
        try {
            int n = Integer.parseInt(string2);
            if (n < 32) {
                n = 32;
                LOGGER.warn("Invalid {} {}, using minimum size {}.", (Object)string, (Object)string2, (Object)32);
            }
            return Unbox.ceilingNextPowerOfTwo(n);
        }
        catch (Exception exception) {
            LOGGER.warn("Invalid {} {}, using default size {}.", (Object)string, (Object)string2, (Object)32);
            return 32;
        }
    }

    private static int ceilingNextPowerOfTwo(int n) {
        return 1 << 32 - Integer.numberOfLeadingZeros(n - 1);
    }

    @PerformanceSensitive(value={"allocation"})
    public static StringBuilder box(float f2) {
        return Unbox.getSB().append(f2);
    }

    @PerformanceSensitive(value={"allocation"})
    public static StringBuilder box(double d2) {
        return Unbox.getSB().append(d2);
    }

    @PerformanceSensitive(value={"allocation"})
    public static StringBuilder box(short s) {
        return Unbox.getSB().append(s);
    }

    @PerformanceSensitive(value={"allocation"})
    public static StringBuilder box(int n) {
        return Unbox.getSB().append(n);
    }

    @PerformanceSensitive(value={"allocation"})
    public static StringBuilder box(char c2) {
        return Unbox.getSB().append(c2);
    }

    @PerformanceSensitive(value={"allocation"})
    public static StringBuilder box(long l) {
        return Unbox.getSB().append(l);
    }

    @PerformanceSensitive(value={"allocation"})
    public static StringBuilder box(byte by) {
        return Unbox.getSB().append(by);
    }

    @PerformanceSensitive(value={"allocation"})
    public static StringBuilder box(boolean bl) {
        return Unbox.getSB().append(bl);
    }

    private static State getState() {
        State state = threadLocalState.get();
        if (state == null) {
            state = new State();
            threadLocalState.set(state);
        }
        return state;
    }

    private static StringBuilder getSB() {
        if (Constants.ENABLE_THREADLOCALS) {
            return Unbox.getState().getStringBuilder();
        }
        return webSafeState.getStringBuilder();
    }

    static int getRingbufferSize() {
        return RINGBUFFER_SIZE;
    }

    static class State {
        private final StringBuilder[] ringBuffer = new StringBuilder[Unbox.access$000()];
        private int current;

        State() {
            for (int i2 = 0; i2 < this.ringBuffer.length; ++i2) {
                this.ringBuffer[i2] = new StringBuilder(21);
            }
        }

        public StringBuilder getStringBuilder() {
            StringBuilder stringBuilder = this.ringBuffer[MASK & this.current++];
            stringBuilder.setLength(0);
            return stringBuilder;
        }

        public boolean isBoxedPrimitive(StringBuilder stringBuilder) {
            for (int i2 = 0; i2 < this.ringBuffer.length; ++i2) {
                if (stringBuilder != this.ringBuffer[i2]) continue;
                return true;
            }
            return false;
        }
    }

    static class WebSafeState {
        private final ThreadLocal<StringBuilder[]> ringBuffer = new ThreadLocal();
        private final ThreadLocal<int[]> current = new ThreadLocal();

        private WebSafeState() {
        }

        public StringBuilder getStringBuilder() {
            Object object = this.ringBuffer.get();
            if (object == null) {
                object = new StringBuilder[RINGBUFFER_SIZE];
                for (int i2 = 0; i2 < ((StringBuilder[])object).length; ++i2) {
                    object[i2] = new StringBuilder(21);
                }
                this.ringBuffer.set((StringBuilder[])object);
                this.current.set(new int[1]);
            }
            int[] nArray = this.current.get();
            int n = nArray[0];
            nArray[0] = n + 1;
            object = object[MASK & n];
            object.setLength(0);
            return object;
        }

        public boolean isBoxedPrimitive(StringBuilder stringBuilder) {
            StringBuilder[] stringBuilderArray = this.ringBuffer.get();
            if (stringBuilderArray == null) {
                return false;
            }
            for (int i2 = 0; i2 < stringBuilderArray.length; ++i2) {
                if (stringBuilder != stringBuilderArray[i2]) continue;
                return true;
            }
            return false;
        }
    }
}

