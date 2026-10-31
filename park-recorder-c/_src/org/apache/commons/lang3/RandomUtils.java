/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.util.Random;
import org.apache.commons.lang3.Validate;

public class RandomUtils {
    private static final Random RANDOM = new Random();

    public static boolean nextBoolean() {
        return RANDOM.nextBoolean();
    }

    public static byte[] nextBytes(int n) {
        Validate.isTrue(n >= 0, "Count cannot be negative.", new Object[0]);
        byte[] byArray = new byte[n];
        RANDOM.nextBytes(byArray);
        return byArray;
    }

    public static int nextInt(int n, int n2) {
        Validate.isTrue(n2 >= n, "Start value must be smaller or equal to end value.", new Object[0]);
        Validate.isTrue(n >= 0, "Both range values must be non-negative.", new Object[0]);
        if (n == n2) {
            return n;
        }
        return n + RANDOM.nextInt(n2 - n);
    }

    public static int nextInt() {
        return RandomUtils.nextInt(0, Integer.MAX_VALUE);
    }

    public static long nextLong(long l, long l2) {
        Validate.isTrue(l2 >= l, "Start value must be smaller or equal to end value.", new Object[0]);
        Validate.isTrue(l >= 0L, "Both range values must be non-negative.", new Object[0]);
        if (l == l2) {
            return l;
        }
        return (long)RandomUtils.nextDouble(l, l2);
    }

    public static long nextLong() {
        return RandomUtils.nextLong(0L, Long.MAX_VALUE);
    }

    public static double nextDouble(double d2, double d3) {
        Validate.isTrue(d3 >= d2, "Start value must be smaller or equal to end value.", new Object[0]);
        Validate.isTrue(d2 >= 0.0, "Both range values must be non-negative.", new Object[0]);
        if (d2 == d3) {
            return d2;
        }
        return d2 + (d3 - d2) * RANDOM.nextDouble();
    }

    public static double nextDouble() {
        return RandomUtils.nextDouble(0.0, Double.MAX_VALUE);
    }

    public static float nextFloat(float f2, float f3) {
        Validate.isTrue(f3 >= f2, "Start value must be smaller or equal to end value.", new Object[0]);
        Validate.isTrue(f2 >= 0.0f, "Both range values must be non-negative.", new Object[0]);
        if (f2 == f3) {
            return f2;
        }
        return f2 + (f3 - f2) * RANDOM.nextFloat();
    }

    public static float nextFloat() {
        return RandomUtils.nextFloat(0.0f, Float.MAX_VALUE);
    }
}

