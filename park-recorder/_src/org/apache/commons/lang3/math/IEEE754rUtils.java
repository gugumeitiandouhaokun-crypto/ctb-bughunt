/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.math;

import org.apache.commons.lang3.Validate;

public class IEEE754rUtils {
    public static double min(double ... dArray) {
        if (dArray == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        Validate.isTrue(dArray.length != 0, "Array cannot be empty.", new Object[0]);
        double d2 = dArray[0];
        for (int j = 1; j < dArray.length; ++j) {
            d2 = IEEE754rUtils.min(dArray[j], d2);
        }
        return d2;
    }

    public static float min(float ... fArray) {
        if (fArray == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        Validate.isTrue(fArray.length != 0, "Array cannot be empty.", new Object[0]);
        float f2 = fArray[0];
        for (int j = 1; j < fArray.length; ++j) {
            f2 = IEEE754rUtils.min(fArray[j], f2);
        }
        return f2;
    }

    public static double min(double d2, double d3, double d4) {
        return IEEE754rUtils.min(IEEE754rUtils.min(d2, d3), d4);
    }

    public static double min(double d2, double d3) {
        if (Double.isNaN(d2)) {
            return d3;
        }
        if (Double.isNaN(d3)) {
            return d2;
        }
        return Math.min(d2, d3);
    }

    public static float min(float f2, float f3, float f4) {
        return IEEE754rUtils.min(IEEE754rUtils.min(f2, f3), f4);
    }

    public static float min(float f2, float f3) {
        if (Float.isNaN(f2)) {
            return f3;
        }
        if (Float.isNaN(f3)) {
            return f2;
        }
        return Math.min(f2, f3);
    }

    public static double max(double ... dArray) {
        if (dArray == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        Validate.isTrue(dArray.length != 0, "Array cannot be empty.", new Object[0]);
        double d2 = dArray[0];
        for (int j = 1; j < dArray.length; ++j) {
            d2 = IEEE754rUtils.max(dArray[j], d2);
        }
        return d2;
    }

    public static float max(float ... fArray) {
        if (fArray == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        Validate.isTrue(fArray.length != 0, "Array cannot be empty.", new Object[0]);
        float f2 = fArray[0];
        for (int j = 1; j < fArray.length; ++j) {
            f2 = IEEE754rUtils.max(fArray[j], f2);
        }
        return f2;
    }

    public static double max(double d2, double d3, double d4) {
        return IEEE754rUtils.max(IEEE754rUtils.max(d2, d3), d4);
    }

    public static double max(double d2, double d3) {
        if (Double.isNaN(d2)) {
            return d3;
        }
        if (Double.isNaN(d3)) {
            return d2;
        }
        return Math.max(d2, d3);
    }

    public static float max(float f2, float f3, float f4) {
        return IEEE754rUtils.max(IEEE754rUtils.max(f2, f3), f4);
    }

    public static float max(float f2, float f3) {
        if (Float.isNaN(f2)) {
            return f3;
        }
        if (Float.isNaN(f3)) {
            return f2;
        }
        return Math.max(f2, f3);
    }
}

