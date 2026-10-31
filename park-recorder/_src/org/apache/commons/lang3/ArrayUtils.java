/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.mutable.MutableInt;

public class ArrayUtils {
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final long[] EMPTY_LONG_ARRAY = new long[0];
    public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];
    public static final short[] EMPTY_SHORT_ARRAY = new short[0];
    public static final Short[] EMPTY_SHORT_OBJECT_ARRAY = new Short[0];
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY = new Byte[0];
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];
    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    public static final Float[] EMPTY_FLOAT_OBJECT_ARRAY = new Float[0];
    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
    public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY = new Boolean[0];
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];
    public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = new Character[0];
    public static final int INDEX_NOT_FOUND = -1;

    public static String toString(Object object) {
        return ArrayUtils.toString(object, "{}");
    }

    public static String toString(Object object, String string) {
        if (object == null) {
            return string;
        }
        return new ToStringBuilder(object, ToStringStyle.SIMPLE_STYLE).append(object).toString();
    }

    public static int hashCode(Object object) {
        return new HashCodeBuilder().append(object).toHashCode();
    }

    @Deprecated
    public static boolean isEquals(Object object, Object object2) {
        return new EqualsBuilder().append(object, object2).isEquals();
    }

    public static Map<Object, Object> toMap(Object[] objectArray) {
        if (objectArray == null) {
            return null;
        }
        HashMap<Object, Object> hashMap = new HashMap<Object, Object>((int)((double)objectArray.length * 1.5));
        for (int j = 0; j < objectArray.length; ++j) {
            Object[] objectArray2;
            Object object = objectArray[j];
            if (object instanceof Map.Entry) {
                objectArray2 = (Object[])object;
                hashMap.put(objectArray2.getKey(), objectArray2.getValue());
                continue;
            }
            if (object instanceof Object[]) {
                objectArray2 = (Object[])object;
                if (objectArray2.length < 2) {
                    throw new IllegalArgumentException("Array element " + j + ", '" + object + "', has a length less than 2");
                }
                hashMap.put(objectArray2[0], objectArray2[1]);
                continue;
            }
            throw new IllegalArgumentException("Array element " + j + ", '" + object + "', is neither of type Map.Entry nor an Array");
        }
        return hashMap;
    }

    public static <T> T[] toArray(T ... TArray) {
        return TArray;
    }

    public static <T> T[] clone(T[] TArray) {
        if (TArray == null) {
            return null;
        }
        return (Object[])TArray.clone();
    }

    public static long[] clone(long[] lArray) {
        if (lArray == null) {
            return null;
        }
        return (long[])lArray.clone();
    }

    public static int[] clone(int[] nArray) {
        if (nArray == null) {
            return null;
        }
        return (int[])nArray.clone();
    }

    public static short[] clone(short[] sArray) {
        if (sArray == null) {
            return null;
        }
        return (short[])sArray.clone();
    }

    public static char[] clone(char[] cArray) {
        if (cArray == null) {
            return null;
        }
        return (char[])cArray.clone();
    }

    public static byte[] clone(byte[] byArray) {
        if (byArray == null) {
            return null;
        }
        return (byte[])byArray.clone();
    }

    public static double[] clone(double[] dArray) {
        if (dArray == null) {
            return null;
        }
        return (double[])dArray.clone();
    }

    public static float[] clone(float[] fArray) {
        if (fArray == null) {
            return null;
        }
        return (float[])fArray.clone();
    }

    public static boolean[] clone(boolean[] blArray) {
        if (blArray == null) {
            return null;
        }
        return (boolean[])blArray.clone();
    }

    public static <T> T[] nullToEmpty(T[] TArray, Class<T[]> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("The type must not be null");
        }
        if (TArray == null) {
            Class<T[]> clazz2 = clazz;
            return clazz2.cast(Array.newInstance(clazz2.getComponentType(), 0));
        }
        return TArray;
    }

    public static Object[] nullToEmpty(Object[] objectArray) {
        if (ArrayUtils.isEmpty(objectArray)) {
            return EMPTY_OBJECT_ARRAY;
        }
        return objectArray;
    }

    public static Class<?>[] nullToEmpty(Class<?>[] classArray) {
        if (ArrayUtils.isEmpty(classArray)) {
            return EMPTY_CLASS_ARRAY;
        }
        return classArray;
    }

    public static String[] nullToEmpty(String[] stringArray) {
        if (ArrayUtils.isEmpty(stringArray)) {
            return EMPTY_STRING_ARRAY;
        }
        return stringArray;
    }

    public static long[] nullToEmpty(long[] lArray) {
        if (ArrayUtils.isEmpty(lArray)) {
            return EMPTY_LONG_ARRAY;
        }
        return lArray;
    }

    public static int[] nullToEmpty(int[] nArray) {
        if (ArrayUtils.isEmpty(nArray)) {
            return EMPTY_INT_ARRAY;
        }
        return nArray;
    }

    public static short[] nullToEmpty(short[] sArray) {
        if (ArrayUtils.isEmpty(sArray)) {
            return EMPTY_SHORT_ARRAY;
        }
        return sArray;
    }

    public static char[] nullToEmpty(char[] cArray) {
        if (ArrayUtils.isEmpty(cArray)) {
            return EMPTY_CHAR_ARRAY;
        }
        return cArray;
    }

    public static byte[] nullToEmpty(byte[] byArray) {
        if (ArrayUtils.isEmpty(byArray)) {
            return EMPTY_BYTE_ARRAY;
        }
        return byArray;
    }

    public static double[] nullToEmpty(double[] dArray) {
        if (ArrayUtils.isEmpty(dArray)) {
            return EMPTY_DOUBLE_ARRAY;
        }
        return dArray;
    }

    public static float[] nullToEmpty(float[] fArray) {
        if (ArrayUtils.isEmpty(fArray)) {
            return EMPTY_FLOAT_ARRAY;
        }
        return fArray;
    }

    public static boolean[] nullToEmpty(boolean[] blArray) {
        if (ArrayUtils.isEmpty(blArray)) {
            return EMPTY_BOOLEAN_ARRAY;
        }
        return blArray;
    }

    public static Long[] nullToEmpty(Long[] longArray) {
        if (ArrayUtils.isEmpty((Object[])longArray)) {
            return EMPTY_LONG_OBJECT_ARRAY;
        }
        return longArray;
    }

    public static Integer[] nullToEmpty(Integer[] integerArray) {
        if (ArrayUtils.isEmpty((Object[])integerArray)) {
            return EMPTY_INTEGER_OBJECT_ARRAY;
        }
        return integerArray;
    }

    public static Short[] nullToEmpty(Short[] shortArray) {
        if (ArrayUtils.isEmpty((Object[])shortArray)) {
            return EMPTY_SHORT_OBJECT_ARRAY;
        }
        return shortArray;
    }

    public static Character[] nullToEmpty(Character[] characterArray) {
        if (ArrayUtils.isEmpty((Object[])characterArray)) {
            return EMPTY_CHARACTER_OBJECT_ARRAY;
        }
        return characterArray;
    }

    public static Byte[] nullToEmpty(Byte[] byteArray) {
        if (ArrayUtils.isEmpty((Object[])byteArray)) {
            return EMPTY_BYTE_OBJECT_ARRAY;
        }
        return byteArray;
    }

    public static Double[] nullToEmpty(Double[] doubleArray) {
        if (ArrayUtils.isEmpty((Object[])doubleArray)) {
            return EMPTY_DOUBLE_OBJECT_ARRAY;
        }
        return doubleArray;
    }

    public static Float[] nullToEmpty(Float[] floatArray) {
        if (ArrayUtils.isEmpty((Object[])floatArray)) {
            return EMPTY_FLOAT_OBJECT_ARRAY;
        }
        return floatArray;
    }

    public static Boolean[] nullToEmpty(Boolean[] booleanArray) {
        if (ArrayUtils.isEmpty((Object[])booleanArray)) {
            return EMPTY_BOOLEAN_OBJECT_ARRAY;
        }
        return booleanArray;
    }

    public static <T> T[] subarray(T[] TArray, int n, int n2) {
        if (TArray == null) {
            return null;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 > TArray.length) {
            n2 = TArray.length;
        }
        Object[] objectArray = TArray.getClass().getComponentType();
        if ((n2 -= n) <= 0) {
            return (Object[])Array.newInstance(objectArray, 0);
        }
        objectArray = (Object[])Array.newInstance(objectArray, n2);
        System.arraycopy(TArray, n, objectArray, 0, n2);
        return objectArray;
    }

    public static long[] subarray(long[] lArray, int n, int n2) {
        if (lArray == null) {
            return null;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 > lArray.length) {
            n2 = lArray.length;
        }
        if ((n2 -= n) <= 0) {
            return EMPTY_LONG_ARRAY;
        }
        long[] lArray2 = new long[n2];
        System.arraycopy(lArray, n, lArray2, 0, n2);
        return lArray2;
    }

    public static int[] subarray(int[] nArray, int n, int n2) {
        if (nArray == null) {
            return null;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 > nArray.length) {
            n2 = nArray.length;
        }
        if ((n2 -= n) <= 0) {
            return EMPTY_INT_ARRAY;
        }
        int[] nArray2 = new int[n2];
        System.arraycopy(nArray, n, nArray2, 0, n2);
        return nArray2;
    }

    public static short[] subarray(short[] sArray, int n, int n2) {
        if (sArray == null) {
            return null;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 > sArray.length) {
            n2 = sArray.length;
        }
        if ((n2 -= n) <= 0) {
            return EMPTY_SHORT_ARRAY;
        }
        short[] sArray2 = new short[n2];
        System.arraycopy(sArray, n, sArray2, 0, n2);
        return sArray2;
    }

    public static char[] subarray(char[] cArray, int n, int n2) {
        if (cArray == null) {
            return null;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 > cArray.length) {
            n2 = cArray.length;
        }
        if ((n2 -= n) <= 0) {
            return EMPTY_CHAR_ARRAY;
        }
        char[] cArray2 = new char[n2];
        System.arraycopy(cArray, n, cArray2, 0, n2);
        return cArray2;
    }

    public static byte[] subarray(byte[] byArray, int n, int n2) {
        if (byArray == null) {
            return null;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 > byArray.length) {
            n2 = byArray.length;
        }
        if ((n2 -= n) <= 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] byArray2 = new byte[n2];
        System.arraycopy(byArray, n, byArray2, 0, n2);
        return byArray2;
    }

    public static double[] subarray(double[] dArray, int n, int n2) {
        if (dArray == null) {
            return null;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 > dArray.length) {
            n2 = dArray.length;
        }
        if ((n2 -= n) <= 0) {
            return EMPTY_DOUBLE_ARRAY;
        }
        double[] dArray2 = new double[n2];
        System.arraycopy(dArray, n, dArray2, 0, n2);
        return dArray2;
    }

    public static float[] subarray(float[] fArray, int n, int n2) {
        if (fArray == null) {
            return null;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 > fArray.length) {
            n2 = fArray.length;
        }
        if ((n2 -= n) <= 0) {
            return EMPTY_FLOAT_ARRAY;
        }
        float[] fArray2 = new float[n2];
        System.arraycopy(fArray, n, fArray2, 0, n2);
        return fArray2;
    }

    public static boolean[] subarray(boolean[] blArray, int n, int n2) {
        if (blArray == null) {
            return null;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 > blArray.length) {
            n2 = blArray.length;
        }
        if ((n2 -= n) <= 0) {
            return EMPTY_BOOLEAN_ARRAY;
        }
        boolean[] blArray2 = new boolean[n2];
        System.arraycopy(blArray, n, blArray2, 0, n2);
        return blArray2;
    }

    public static boolean isSameLength(Object[] objectArray, Object[] objectArray2) {
        return ArrayUtils.getLength(objectArray) == ArrayUtils.getLength(objectArray2);
    }

    public static boolean isSameLength(long[] lArray, long[] lArray2) {
        return ArrayUtils.getLength(lArray) == ArrayUtils.getLength(lArray2);
    }

    public static boolean isSameLength(int[] nArray, int[] nArray2) {
        return ArrayUtils.getLength(nArray) == ArrayUtils.getLength(nArray2);
    }

    public static boolean isSameLength(short[] sArray, short[] sArray2) {
        return ArrayUtils.getLength(sArray) == ArrayUtils.getLength(sArray2);
    }

    public static boolean isSameLength(char[] cArray, char[] cArray2) {
        return ArrayUtils.getLength(cArray) == ArrayUtils.getLength(cArray2);
    }

    public static boolean isSameLength(byte[] byArray, byte[] byArray2) {
        return ArrayUtils.getLength(byArray) == ArrayUtils.getLength(byArray2);
    }

    public static boolean isSameLength(double[] dArray, double[] dArray2) {
        return ArrayUtils.getLength(dArray) == ArrayUtils.getLength(dArray2);
    }

    public static boolean isSameLength(float[] fArray, float[] fArray2) {
        return ArrayUtils.getLength(fArray) == ArrayUtils.getLength(fArray2);
    }

    public static boolean isSameLength(boolean[] blArray, boolean[] blArray2) {
        return ArrayUtils.getLength(blArray) == ArrayUtils.getLength(blArray2);
    }

    public static int getLength(Object object) {
        if (object == null) {
            return 0;
        }
        return Array.getLength(object);
    }

    public static boolean isSameType(Object object, Object object2) {
        if (object == null || object2 == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        return object.getClass().getName().equals(object2.getClass().getName());
    }

    public static void reverse(Object[] objectArray) {
        if (objectArray == null) {
            return;
        }
        ArrayUtils.reverse(objectArray, 0, objectArray.length);
    }

    public static void reverse(long[] lArray) {
        if (lArray == null) {
            return;
        }
        ArrayUtils.reverse(lArray, 0, lArray.length);
    }

    public static void reverse(int[] nArray) {
        if (nArray == null) {
            return;
        }
        ArrayUtils.reverse(nArray, 0, nArray.length);
    }

    public static void reverse(short[] sArray) {
        if (sArray == null) {
            return;
        }
        ArrayUtils.reverse(sArray, 0, sArray.length);
    }

    public static void reverse(char[] cArray) {
        if (cArray == null) {
            return;
        }
        ArrayUtils.reverse(cArray, 0, cArray.length);
    }

    public static void reverse(byte[] byArray) {
        if (byArray == null) {
            return;
        }
        ArrayUtils.reverse(byArray, 0, byArray.length);
    }

    public static void reverse(double[] dArray) {
        if (dArray == null) {
            return;
        }
        ArrayUtils.reverse(dArray, 0, dArray.length);
    }

    public static void reverse(float[] fArray) {
        if (fArray == null) {
            return;
        }
        ArrayUtils.reverse(fArray, 0, fArray.length);
    }

    public static void reverse(boolean[] blArray) {
        if (blArray == null) {
            return;
        }
        ArrayUtils.reverse(blArray, 0, blArray.length);
    }

    public static void reverse(boolean[] blArray, int n, int n2) {
        if (blArray == null) {
            return;
        }
        n = n < 0 ? 0 : n;
        for (n2 = Math.min(blArray.length, n2) - 1; n2 > n; --n2, ++n) {
            boolean bl = blArray[n2];
            blArray[n2] = blArray[n];
            blArray[n] = bl;
        }
    }

    public static void reverse(byte[] byArray, int n, int n2) {
        if (byArray == null) {
            return;
        }
        n = n < 0 ? 0 : n;
        for (n2 = Math.min(byArray.length, n2) - 1; n2 > n; --n2, ++n) {
            byte by = byArray[n2];
            byArray[n2] = byArray[n];
            byArray[n] = by;
        }
    }

    public static void reverse(char[] cArray, int n, int n2) {
        if (cArray == null) {
            return;
        }
        n = n < 0 ? 0 : n;
        for (n2 = Math.min(cArray.length, n2) - 1; n2 > n; --n2, ++n) {
            char c2 = cArray[n2];
            cArray[n2] = cArray[n];
            cArray[n] = c2;
        }
    }

    public static void reverse(double[] dArray, int n, int n2) {
        if (dArray == null) {
            return;
        }
        n = n < 0 ? 0 : n;
        for (n2 = Math.min(dArray.length, n2) - 1; n2 > n; --n2, ++n) {
            double d2 = dArray[n2];
            dArray[n2] = dArray[n];
            dArray[n] = d2;
        }
    }

    public static void reverse(float[] fArray, int n, int n2) {
        if (fArray == null) {
            return;
        }
        n = n < 0 ? 0 : n;
        for (n2 = Math.min(fArray.length, n2) - 1; n2 > n; --n2, ++n) {
            float f2 = fArray[n2];
            fArray[n2] = fArray[n];
            fArray[n] = f2;
        }
    }

    public static void reverse(int[] nArray, int n, int n2) {
        if (nArray == null) {
            return;
        }
        n = n < 0 ? 0 : n;
        for (n2 = Math.min(nArray.length, n2) - 1; n2 > n; --n2, ++n) {
            int n3 = nArray[n2];
            nArray[n2] = nArray[n];
            nArray[n] = n3;
        }
    }

    public static void reverse(long[] lArray, int n, int n2) {
        if (lArray == null) {
            return;
        }
        n = n < 0 ? 0 : n;
        for (n2 = Math.min(lArray.length, n2) - 1; n2 > n; --n2, ++n) {
            long l = lArray[n2];
            lArray[n2] = lArray[n];
            lArray[n] = l;
        }
    }

    public static void reverse(Object[] objectArray, int n, int n2) {
        if (objectArray == null) {
            return;
        }
        n = n < 0 ? 0 : n;
        for (n2 = Math.min(objectArray.length, n2) - 1; n2 > n; --n2, ++n) {
            Object object = objectArray[n2];
            objectArray[n2] = objectArray[n];
            objectArray[n] = object;
        }
    }

    public static void reverse(short[] sArray, int n, int n2) {
        if (sArray == null) {
            return;
        }
        n = n < 0 ? 0 : n;
        for (n2 = Math.min(sArray.length, n2) - 1; n2 > n; --n2, ++n) {
            short s = sArray[n2];
            sArray[n2] = sArray[n];
            sArray[n] = s;
        }
    }

    public static void swap(Object[] objectArray, int n, int n2) {
        if (objectArray == null || objectArray.length == 0) {
            return;
        }
        ArrayUtils.swap(objectArray, n, n2, 1);
    }

    public static void swap(long[] lArray, int n, int n2) {
        if (lArray == null || lArray.length == 0) {
            return;
        }
        ArrayUtils.swap(lArray, n, n2, 1);
    }

    public static void swap(int[] nArray, int n, int n2) {
        if (nArray == null || nArray.length == 0) {
            return;
        }
        ArrayUtils.swap(nArray, n, n2, 1);
    }

    public static void swap(short[] sArray, int n, int n2) {
        if (sArray == null || sArray.length == 0) {
            return;
        }
        ArrayUtils.swap(sArray, n, n2, 1);
    }

    public static void swap(char[] cArray, int n, int n2) {
        if (cArray == null || cArray.length == 0) {
            return;
        }
        ArrayUtils.swap(cArray, n, n2, 1);
    }

    public static void swap(byte[] byArray, int n, int n2) {
        if (byArray == null || byArray.length == 0) {
            return;
        }
        ArrayUtils.swap(byArray, n, n2, 1);
    }

    public static void swap(double[] dArray, int n, int n2) {
        if (dArray == null || dArray.length == 0) {
            return;
        }
        ArrayUtils.swap(dArray, n, n2, 1);
    }

    public static void swap(float[] fArray, int n, int n2) {
        if (fArray == null || fArray.length == 0) {
            return;
        }
        ArrayUtils.swap(fArray, n, n2, 1);
    }

    public static void swap(boolean[] blArray, int n, int n2) {
        if (blArray == null || blArray.length == 0) {
            return;
        }
        ArrayUtils.swap(blArray, n, n2, 1);
    }

    public static void swap(boolean[] blArray, int n, int n2, int n3) {
        if (blArray == null || blArray.length == 0 || n >= blArray.length || n2 >= blArray.length) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 < 0) {
            n2 = 0;
        }
        n3 = Math.min(Math.min(n3, blArray.length - n), blArray.length - n2);
        int n4 = 0;
        while (n4 < n3) {
            boolean bl = blArray[n];
            blArray[n] = blArray[n2];
            blArray[n2] = bl;
            ++n4;
            ++n;
            ++n2;
        }
    }

    public static void swap(byte[] byArray, int n, int n2, int n3) {
        if (byArray == null || byArray.length == 0 || n >= byArray.length || n2 >= byArray.length) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 < 0) {
            n2 = 0;
        }
        n3 = Math.min(Math.min(n3, byArray.length - n), byArray.length - n2);
        int n4 = 0;
        while (n4 < n3) {
            byte by = byArray[n];
            byArray[n] = byArray[n2];
            byArray[n2] = by;
            ++n4;
            ++n;
            ++n2;
        }
    }

    public static void swap(char[] cArray, int n, int n2, int n3) {
        if (cArray == null || cArray.length == 0 || n >= cArray.length || n2 >= cArray.length) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 < 0) {
            n2 = 0;
        }
        n3 = Math.min(Math.min(n3, cArray.length - n), cArray.length - n2);
        int n4 = 0;
        while (n4 < n3) {
            char c2 = cArray[n];
            cArray[n] = cArray[n2];
            cArray[n2] = c2;
            ++n4;
            ++n;
            ++n2;
        }
    }

    public static void swap(double[] dArray, int n, int n2, int n3) {
        if (dArray == null || dArray.length == 0 || n >= dArray.length || n2 >= dArray.length) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 < 0) {
            n2 = 0;
        }
        n3 = Math.min(Math.min(n3, dArray.length - n), dArray.length - n2);
        int n4 = 0;
        while (n4 < n3) {
            double d2 = dArray[n];
            dArray[n] = dArray[n2];
            dArray[n2] = d2;
            ++n4;
            ++n;
            ++n2;
        }
    }

    public static void swap(float[] fArray, int n, int n2, int n3) {
        if (fArray == null || fArray.length == 0 || n >= fArray.length || n2 >= fArray.length) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 < 0) {
            n2 = 0;
        }
        n3 = Math.min(Math.min(n3, fArray.length - n), fArray.length - n2);
        int n4 = 0;
        while (n4 < n3) {
            float f2 = fArray[n];
            fArray[n] = fArray[n2];
            fArray[n2] = f2;
            ++n4;
            ++n;
            ++n2;
        }
    }

    public static void swap(int[] nArray, int n, int n2, int n3) {
        if (nArray == null || nArray.length == 0 || n >= nArray.length || n2 >= nArray.length) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 < 0) {
            n2 = 0;
        }
        n3 = Math.min(Math.min(n3, nArray.length - n), nArray.length - n2);
        int n4 = 0;
        while (n4 < n3) {
            int n5 = nArray[n];
            nArray[n] = nArray[n2];
            nArray[n2] = n5;
            ++n4;
            ++n;
            ++n2;
        }
    }

    public static void swap(long[] lArray, int n, int n2, int n3) {
        if (lArray == null || lArray.length == 0 || n >= lArray.length || n2 >= lArray.length) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 < 0) {
            n2 = 0;
        }
        n3 = Math.min(Math.min(n3, lArray.length - n), lArray.length - n2);
        int n4 = 0;
        while (n4 < n3) {
            long l = lArray[n];
            lArray[n] = lArray[n2];
            lArray[n2] = l;
            ++n4;
            ++n;
            ++n2;
        }
    }

    public static void swap(Object[] objectArray, int n, int n2, int n3) {
        if (objectArray == null || objectArray.length == 0 || n >= objectArray.length || n2 >= objectArray.length) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 < 0) {
            n2 = 0;
        }
        n3 = Math.min(Math.min(n3, objectArray.length - n), objectArray.length - n2);
        int n4 = 0;
        while (n4 < n3) {
            Object object = objectArray[n];
            objectArray[n] = objectArray[n2];
            objectArray[n2] = object;
            ++n4;
            ++n;
            ++n2;
        }
    }

    public static void swap(short[] sArray, int n, int n2, int n3) {
        if (sArray == null || sArray.length == 0 || n >= sArray.length || n2 >= sArray.length) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 < 0) {
            n2 = 0;
        }
        if (n == n2) {
            return;
        }
        n3 = Math.min(Math.min(n3, sArray.length - n), sArray.length - n2);
        int n4 = 0;
        while (n4 < n3) {
            short s = sArray[n];
            sArray[n] = sArray[n2];
            sArray[n2] = s;
            ++n4;
            ++n;
            ++n2;
        }
    }

    public static void shift(Object[] objectArray, int n) {
        if (objectArray == null) {
            return;
        }
        ArrayUtils.shift(objectArray, 0, objectArray.length, n);
    }

    public static void shift(long[] lArray, int n) {
        if (lArray == null) {
            return;
        }
        ArrayUtils.shift(lArray, 0, lArray.length, n);
    }

    public static void shift(int[] nArray, int n) {
        if (nArray == null) {
            return;
        }
        ArrayUtils.shift(nArray, 0, nArray.length, n);
    }

    public static void shift(short[] sArray, int n) {
        if (sArray == null) {
            return;
        }
        ArrayUtils.shift(sArray, 0, sArray.length, n);
    }

    public static void shift(char[] cArray, int n) {
        if (cArray == null) {
            return;
        }
        ArrayUtils.shift(cArray, 0, cArray.length, n);
    }

    public static void shift(byte[] byArray, int n) {
        if (byArray == null) {
            return;
        }
        ArrayUtils.shift(byArray, 0, byArray.length, n);
    }

    public static void shift(double[] dArray, int n) {
        if (dArray == null) {
            return;
        }
        ArrayUtils.shift(dArray, 0, dArray.length, n);
    }

    public static void shift(float[] fArray, int n) {
        if (fArray == null) {
            return;
        }
        ArrayUtils.shift(fArray, 0, fArray.length, n);
    }

    public static void shift(boolean[] blArray, int n) {
        if (blArray == null) {
            return;
        }
        ArrayUtils.shift(blArray, 0, blArray.length, n);
    }

    public static void shift(boolean[] blArray, int n, int n2, int n3) {
        if (blArray == null) {
            return;
        }
        if (n >= blArray.length - 1 || n2 <= 0) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 >= blArray.length) {
            n2 = blArray.length;
        }
        if ((n2 -= n) <= 1) {
            return;
        }
        if ((n3 %= n2) < 0) {
            n3 += n2;
        }
        while (n2 > 1 && n3 > 0) {
            int n4 = n2 - n3;
            if (n3 > n4) {
                int n5 = n;
                ArrayUtils.swap(blArray, n5, n5 + n2 - n4, n4);
                n2 = n3;
                n3 -= n4;
                continue;
            }
            if (n3 < n4) {
                int n6 = n;
                ArrayUtils.swap(blArray, n6, n6 + n4, n3);
                n += n3;
                n2 = n4;
                continue;
            }
            int n7 = n;
            ArrayUtils.swap(blArray, n7, n7 + n4, n3);
            return;
        }
    }

    public static void shift(byte[] byArray, int n, int n2, int n3) {
        if (byArray == null) {
            return;
        }
        if (n >= byArray.length - 1 || n2 <= 0) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 >= byArray.length) {
            n2 = byArray.length;
        }
        if ((n2 -= n) <= 1) {
            return;
        }
        if ((n3 %= n2) < 0) {
            n3 += n2;
        }
        while (n2 > 1 && n3 > 0) {
            int n4 = n2 - n3;
            if (n3 > n4) {
                int n5 = n;
                ArrayUtils.swap(byArray, n5, n5 + n2 - n4, n4);
                n2 = n3;
                n3 -= n4;
                continue;
            }
            if (n3 < n4) {
                int n6 = n;
                ArrayUtils.swap(byArray, n6, n6 + n4, n3);
                n += n3;
                n2 = n4;
                continue;
            }
            int n7 = n;
            ArrayUtils.swap(byArray, n7, n7 + n4, n3);
            return;
        }
    }

    public static void shift(char[] cArray, int n, int n2, int n3) {
        if (cArray == null) {
            return;
        }
        if (n >= cArray.length - 1 || n2 <= 0) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 >= cArray.length) {
            n2 = cArray.length;
        }
        if ((n2 -= n) <= 1) {
            return;
        }
        if ((n3 %= n2) < 0) {
            n3 += n2;
        }
        while (n2 > 1 && n3 > 0) {
            int n4 = n2 - n3;
            if (n3 > n4) {
                int n5 = n;
                ArrayUtils.swap(cArray, n5, n5 + n2 - n4, n4);
                n2 = n3;
                n3 -= n4;
                continue;
            }
            if (n3 < n4) {
                int n6 = n;
                ArrayUtils.swap(cArray, n6, n6 + n4, n3);
                n += n3;
                n2 = n4;
                continue;
            }
            int n7 = n;
            ArrayUtils.swap(cArray, n7, n7 + n4, n3);
            return;
        }
    }

    public static void shift(double[] dArray, int n, int n2, int n3) {
        if (dArray == null) {
            return;
        }
        if (n >= dArray.length - 1 || n2 <= 0) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 >= dArray.length) {
            n2 = dArray.length;
        }
        if ((n2 -= n) <= 1) {
            return;
        }
        if ((n3 %= n2) < 0) {
            n3 += n2;
        }
        while (n2 > 1 && n3 > 0) {
            int n4 = n2 - n3;
            if (n3 > n4) {
                int n5 = n;
                ArrayUtils.swap(dArray, n5, n5 + n2 - n4, n4);
                n2 = n3;
                n3 -= n4;
                continue;
            }
            if (n3 < n4) {
                int n6 = n;
                ArrayUtils.swap(dArray, n6, n6 + n4, n3);
                n += n3;
                n2 = n4;
                continue;
            }
            int n7 = n;
            ArrayUtils.swap(dArray, n7, n7 + n4, n3);
            return;
        }
    }

    public static void shift(float[] fArray, int n, int n2, int n3) {
        if (fArray == null) {
            return;
        }
        if (n >= fArray.length - 1 || n2 <= 0) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 >= fArray.length) {
            n2 = fArray.length;
        }
        if ((n2 -= n) <= 1) {
            return;
        }
        if ((n3 %= n2) < 0) {
            n3 += n2;
        }
        while (n2 > 1 && n3 > 0) {
            int n4 = n2 - n3;
            if (n3 > n4) {
                int n5 = n;
                ArrayUtils.swap(fArray, n5, n5 + n2 - n4, n4);
                n2 = n3;
                n3 -= n4;
                continue;
            }
            if (n3 < n4) {
                int n6 = n;
                ArrayUtils.swap(fArray, n6, n6 + n4, n3);
                n += n3;
                n2 = n4;
                continue;
            }
            int n7 = n;
            ArrayUtils.swap(fArray, n7, n7 + n4, n3);
            return;
        }
    }

    public static void shift(int[] nArray, int n, int n2, int n3) {
        if (nArray == null) {
            return;
        }
        if (n >= nArray.length - 1 || n2 <= 0) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 >= nArray.length) {
            n2 = nArray.length;
        }
        if ((n2 -= n) <= 1) {
            return;
        }
        if ((n3 %= n2) < 0) {
            n3 += n2;
        }
        while (n2 > 1 && n3 > 0) {
            int n4 = n2 - n3;
            if (n3 > n4) {
                int n5 = n;
                ArrayUtils.swap(nArray, n5, n5 + n2 - n4, n4);
                n2 = n3;
                n3 -= n4;
                continue;
            }
            if (n3 < n4) {
                int n6 = n;
                ArrayUtils.swap(nArray, n6, n6 + n4, n3);
                n += n3;
                n2 = n4;
                continue;
            }
            int n7 = n;
            ArrayUtils.swap(nArray, n7, n7 + n4, n3);
            return;
        }
    }

    public static void shift(long[] lArray, int n, int n2, int n3) {
        if (lArray == null) {
            return;
        }
        if (n >= lArray.length - 1 || n2 <= 0) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 >= lArray.length) {
            n2 = lArray.length;
        }
        if ((n2 -= n) <= 1) {
            return;
        }
        if ((n3 %= n2) < 0) {
            n3 += n2;
        }
        while (n2 > 1 && n3 > 0) {
            int n4 = n2 - n3;
            if (n3 > n4) {
                int n5 = n;
                ArrayUtils.swap(lArray, n5, n5 + n2 - n4, n4);
                n2 = n3;
                n3 -= n4;
                continue;
            }
            if (n3 < n4) {
                int n6 = n;
                ArrayUtils.swap(lArray, n6, n6 + n4, n3);
                n += n3;
                n2 = n4;
                continue;
            }
            int n7 = n;
            ArrayUtils.swap(lArray, n7, n7 + n4, n3);
            return;
        }
    }

    public static void shift(Object[] objectArray, int n, int n2, int n3) {
        if (objectArray == null) {
            return;
        }
        if (n >= objectArray.length - 1 || n2 <= 0) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 >= objectArray.length) {
            n2 = objectArray.length;
        }
        if ((n2 -= n) <= 1) {
            return;
        }
        if ((n3 %= n2) < 0) {
            n3 += n2;
        }
        while (n2 > 1 && n3 > 0) {
            int n4 = n2 - n3;
            if (n3 > n4) {
                int n5 = n;
                ArrayUtils.swap(objectArray, n5, n5 + n2 - n4, n4);
                n2 = n3;
                n3 -= n4;
                continue;
            }
            if (n3 < n4) {
                int n6 = n;
                ArrayUtils.swap(objectArray, n6, n6 + n4, n3);
                n += n3;
                n2 = n4;
                continue;
            }
            int n7 = n;
            ArrayUtils.swap(objectArray, n7, n7 + n4, n3);
            return;
        }
    }

    public static void shift(short[] sArray, int n, int n2, int n3) {
        if (sArray == null) {
            return;
        }
        if (n >= sArray.length - 1 || n2 <= 0) {
            return;
        }
        if (n < 0) {
            n = 0;
        }
        if (n2 >= sArray.length) {
            n2 = sArray.length;
        }
        if ((n2 -= n) <= 1) {
            return;
        }
        if ((n3 %= n2) < 0) {
            n3 += n2;
        }
        while (n2 > 1 && n3 > 0) {
            int n4 = n2 - n3;
            if (n3 > n4) {
                int n5 = n;
                ArrayUtils.swap(sArray, n5, n5 + n2 - n4, n4);
                n2 = n3;
                n3 -= n4;
                continue;
            }
            if (n3 < n4) {
                int n6 = n;
                ArrayUtils.swap(sArray, n6, n6 + n4, n3);
                n += n3;
                n2 = n4;
                continue;
            }
            int n7 = n;
            ArrayUtils.swap(sArray, n7, n7 + n4, n3);
            return;
        }
    }

    public static int indexOf(Object[] objectArray, Object object) {
        return ArrayUtils.indexOf(objectArray, object, 0);
    }

    public static int indexOf(Object[] objectArray, Object object, int n) {
        if (objectArray == null) {
            return -1;
        }
        if (n < 0) {
            n = 0;
        }
        if (object == null) {
            while (n < objectArray.length) {
                if (objectArray[n] == null) {
                    return n;
                }
                ++n;
            }
        } else {
            while (n < objectArray.length) {
                if (object.equals(objectArray[n])) {
                    return n;
                }
                ++n;
            }
        }
        return -1;
    }

    public static int lastIndexOf(Object[] objectArray, Object object) {
        return ArrayUtils.lastIndexOf(objectArray, object, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(Object[] objectArray, Object object, int n) {
        block8: {
            block7: {
                if (objectArray == null) {
                    return -1;
                }
                if (n < 0) {
                    return -1;
                }
                if (n >= objectArray.length) {
                    n = objectArray.length - 1;
                }
                if (object != null) break block7;
                while (n >= 0) {
                    if (objectArray[n] == null) {
                        return n;
                    }
                    --n;
                }
                break block8;
            }
            if (!objectArray.getClass().getComponentType().isInstance(object)) break block8;
            while (n >= 0) {
                if (object.equals(objectArray[n])) {
                    return n;
                }
                --n;
            }
        }
        return -1;
    }

    public static boolean contains(Object[] objectArray, Object object) {
        return ArrayUtils.indexOf(objectArray, object) != -1;
    }

    public static int indexOf(long[] lArray, long l) {
        return ArrayUtils.indexOf(lArray, l, 0);
    }

    public static int indexOf(long[] lArray, long l, int n) {
        if (lArray == null) {
            return -1;
        }
        if (n < 0) {
            n = 0;
        }
        while (n < lArray.length) {
            if (l == lArray[n]) {
                return n;
            }
            ++n;
        }
        return -1;
    }

    public static int lastIndexOf(long[] lArray, long l) {
        return ArrayUtils.lastIndexOf(lArray, l, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(long[] lArray, long l, int n) {
        if (lArray == null) {
            return -1;
        }
        if (n < 0) {
            return -1;
        }
        if (n >= lArray.length) {
            n = lArray.length - 1;
        }
        while (n >= 0) {
            if (l == lArray[n]) {
                return n;
            }
            --n;
        }
        return -1;
    }

    public static boolean contains(long[] lArray, long l) {
        return ArrayUtils.indexOf(lArray, l) != -1;
    }

    public static int indexOf(int[] nArray, int n) {
        return ArrayUtils.indexOf(nArray, n, 0);
    }

    public static int indexOf(int[] nArray, int n, int n2) {
        if (nArray == null) {
            return -1;
        }
        if (n2 < 0) {
            n2 = 0;
        }
        while (n2 < nArray.length) {
            if (n == nArray[n2]) {
                return n2;
            }
            ++n2;
        }
        return -1;
    }

    public static int lastIndexOf(int[] nArray, int n) {
        return ArrayUtils.lastIndexOf(nArray, n, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(int[] nArray, int n, int n2) {
        if (nArray == null) {
            return -1;
        }
        if (n2 < 0) {
            return -1;
        }
        if (n2 >= nArray.length) {
            n2 = nArray.length - 1;
        }
        while (n2 >= 0) {
            if (n == nArray[n2]) {
                return n2;
            }
            --n2;
        }
        return -1;
    }

    public static boolean contains(int[] nArray, int n) {
        return ArrayUtils.indexOf(nArray, n) != -1;
    }

    public static int indexOf(short[] sArray, short s) {
        return ArrayUtils.indexOf(sArray, s, 0);
    }

    public static int indexOf(short[] sArray, short s, int n) {
        if (sArray == null) {
            return -1;
        }
        if (n < 0) {
            n = 0;
        }
        while (n < sArray.length) {
            if (s == sArray[n]) {
                return n;
            }
            ++n;
        }
        return -1;
    }

    public static int lastIndexOf(short[] sArray, short s) {
        return ArrayUtils.lastIndexOf(sArray, s, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(short[] sArray, short s, int n) {
        if (sArray == null) {
            return -1;
        }
        if (n < 0) {
            return -1;
        }
        if (n >= sArray.length) {
            n = sArray.length - 1;
        }
        while (n >= 0) {
            if (s == sArray[n]) {
                return n;
            }
            --n;
        }
        return -1;
    }

    public static boolean contains(short[] sArray, short s) {
        return ArrayUtils.indexOf(sArray, s) != -1;
    }

    public static int indexOf(char[] cArray, char c2) {
        return ArrayUtils.indexOf(cArray, c2, 0);
    }

    public static int indexOf(char[] cArray, char c2, int n) {
        if (cArray == null) {
            return -1;
        }
        if (n < 0) {
            n = 0;
        }
        while (n < cArray.length) {
            if (c2 == cArray[n]) {
                return n;
            }
            ++n;
        }
        return -1;
    }

    public static int lastIndexOf(char[] cArray, char c2) {
        return ArrayUtils.lastIndexOf(cArray, c2, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(char[] cArray, char c2, int n) {
        if (cArray == null) {
            return -1;
        }
        if (n < 0) {
            return -1;
        }
        if (n >= cArray.length) {
            n = cArray.length - 1;
        }
        while (n >= 0) {
            if (c2 == cArray[n]) {
                return n;
            }
            --n;
        }
        return -1;
    }

    public static boolean contains(char[] cArray, char c2) {
        return ArrayUtils.indexOf(cArray, c2) != -1;
    }

    public static int indexOf(byte[] byArray, byte by) {
        return ArrayUtils.indexOf(byArray, by, 0);
    }

    public static int indexOf(byte[] byArray, byte by, int n) {
        if (byArray == null) {
            return -1;
        }
        if (n < 0) {
            n = 0;
        }
        while (n < byArray.length) {
            if (by == byArray[n]) {
                return n;
            }
            ++n;
        }
        return -1;
    }

    public static int lastIndexOf(byte[] byArray, byte by) {
        return ArrayUtils.lastIndexOf(byArray, by, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(byte[] byArray, byte by, int n) {
        if (byArray == null) {
            return -1;
        }
        if (n < 0) {
            return -1;
        }
        if (n >= byArray.length) {
            n = byArray.length - 1;
        }
        while (n >= 0) {
            if (by == byArray[n]) {
                return n;
            }
            --n;
        }
        return -1;
    }

    public static boolean contains(byte[] byArray, byte by) {
        return ArrayUtils.indexOf(byArray, by) != -1;
    }

    public static int indexOf(double[] dArray, double d2) {
        return ArrayUtils.indexOf(dArray, d2, 0);
    }

    public static int indexOf(double[] dArray, double d2, double d3) {
        return ArrayUtils.indexOf(dArray, d2, 0, d3);
    }

    public static int indexOf(double[] dArray, double d2, int n) {
        if (ArrayUtils.isEmpty(dArray)) {
            return -1;
        }
        if (n < 0) {
            n = 0;
        }
        while (n < dArray.length) {
            if (d2 == dArray[n]) {
                return n;
            }
            ++n;
        }
        return -1;
    }

    public static int indexOf(double[] dArray, double d2, int n, double d3) {
        if (ArrayUtils.isEmpty(dArray)) {
            return -1;
        }
        if (n < 0) {
            n = 0;
        }
        double d4 = d2 - d3;
        double d5 = d2 + d3;
        for (int j = n; j < dArray.length; ++j) {
            if (!(dArray[j] >= d4) || !(dArray[j] <= d5)) continue;
            return j;
        }
        return -1;
    }

    public static int lastIndexOf(double[] dArray, double d2) {
        return ArrayUtils.lastIndexOf(dArray, d2, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(double[] dArray, double d2, double d3) {
        return ArrayUtils.lastIndexOf(dArray, d2, Integer.MAX_VALUE, d3);
    }

    public static int lastIndexOf(double[] dArray, double d2, int n) {
        if (ArrayUtils.isEmpty(dArray)) {
            return -1;
        }
        if (n < 0) {
            return -1;
        }
        if (n >= dArray.length) {
            n = dArray.length - 1;
        }
        while (n >= 0) {
            if (d2 == dArray[n]) {
                return n;
            }
            --n;
        }
        return -1;
    }

    public static int lastIndexOf(double[] dArray, double d2, int n, double d3) {
        if (ArrayUtils.isEmpty(dArray)) {
            return -1;
        }
        if (n < 0) {
            return -1;
        }
        if (n >= dArray.length) {
            n = dArray.length - 1;
        }
        double d4 = d2 - d3;
        double d5 = d2 + d3;
        for (int j = n; j >= 0; --j) {
            if (!(dArray[j] >= d4) || !(dArray[j] <= d5)) continue;
            return j;
        }
        return -1;
    }

    public static boolean contains(double[] dArray, double d2) {
        return ArrayUtils.indexOf(dArray, d2) != -1;
    }

    public static boolean contains(double[] dArray, double d2, double d3) {
        return ArrayUtils.indexOf(dArray, d2, 0, d3) != -1;
    }

    public static int indexOf(float[] fArray, float f2) {
        return ArrayUtils.indexOf(fArray, f2, 0);
    }

    public static int indexOf(float[] fArray, float f2, int n) {
        if (ArrayUtils.isEmpty(fArray)) {
            return -1;
        }
        if (n < 0) {
            n = 0;
        }
        while (n < fArray.length) {
            if (f2 == fArray[n]) {
                return n;
            }
            ++n;
        }
        return -1;
    }

    public static int lastIndexOf(float[] fArray, float f2) {
        return ArrayUtils.lastIndexOf(fArray, f2, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(float[] fArray, float f2, int n) {
        if (ArrayUtils.isEmpty(fArray)) {
            return -1;
        }
        if (n < 0) {
            return -1;
        }
        if (n >= fArray.length) {
            n = fArray.length - 1;
        }
        while (n >= 0) {
            if (f2 == fArray[n]) {
                return n;
            }
            --n;
        }
        return -1;
    }

    public static boolean contains(float[] fArray, float f2) {
        return ArrayUtils.indexOf(fArray, f2) != -1;
    }

    public static int indexOf(boolean[] blArray, boolean bl) {
        return ArrayUtils.indexOf(blArray, bl, 0);
    }

    public static int indexOf(boolean[] blArray, boolean bl, int n) {
        if (ArrayUtils.isEmpty(blArray)) {
            return -1;
        }
        if (n < 0) {
            n = 0;
        }
        while (n < blArray.length) {
            if (bl == blArray[n]) {
                return n;
            }
            ++n;
        }
        return -1;
    }

    public static int lastIndexOf(boolean[] blArray, boolean bl) {
        return ArrayUtils.lastIndexOf(blArray, bl, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(boolean[] blArray, boolean bl, int n) {
        if (ArrayUtils.isEmpty(blArray)) {
            return -1;
        }
        if (n < 0) {
            return -1;
        }
        if (n >= blArray.length) {
            n = blArray.length - 1;
        }
        while (n >= 0) {
            if (bl == blArray[n]) {
                return n;
            }
            --n;
        }
        return -1;
    }

    public static boolean contains(boolean[] blArray, boolean bl) {
        return ArrayUtils.indexOf(blArray, bl) != -1;
    }

    public static char[] toPrimitive(Character[] characterArray) {
        if (characterArray == null) {
            return null;
        }
        if (characterArray.length == 0) {
            return EMPTY_CHAR_ARRAY;
        }
        char[] cArray = new char[characterArray.length];
        for (int j = 0; j < characterArray.length; ++j) {
            cArray[j] = characterArray[j].charValue();
        }
        return cArray;
    }

    public static char[] toPrimitive(Character[] characterArray, char c2) {
        if (characterArray == null) {
            return null;
        }
        if (characterArray.length == 0) {
            return EMPTY_CHAR_ARRAY;
        }
        char[] cArray = new char[characterArray.length];
        for (int j = 0; j < characterArray.length; ++j) {
            Character c3 = characterArray[j];
            cArray[j] = c3 == null ? c2 : c3.charValue();
        }
        return cArray;
    }

    public static Character[] toObject(char[] cArray) {
        if (cArray == null) {
            return null;
        }
        if (cArray.length == 0) {
            return EMPTY_CHARACTER_OBJECT_ARRAY;
        }
        Character[] characterArray = new Character[cArray.length];
        for (int j = 0; j < cArray.length; ++j) {
            characterArray[j] = Character.valueOf(cArray[j]);
        }
        return characterArray;
    }

    public static long[] toPrimitive(Long[] longArray) {
        if (longArray == null) {
            return null;
        }
        if (longArray.length == 0) {
            return EMPTY_LONG_ARRAY;
        }
        long[] lArray = new long[longArray.length];
        for (int j = 0; j < longArray.length; ++j) {
            lArray[j] = longArray[j];
        }
        return lArray;
    }

    public static long[] toPrimitive(Long[] longArray, long l) {
        if (longArray == null) {
            return null;
        }
        if (longArray.length == 0) {
            return EMPTY_LONG_ARRAY;
        }
        long[] lArray = new long[longArray.length];
        for (int j = 0; j < longArray.length; ++j) {
            Long l2 = longArray[j];
            lArray[j] = l2 == null ? l : l2;
        }
        return lArray;
    }

    public static Long[] toObject(long[] lArray) {
        if (lArray == null) {
            return null;
        }
        if (lArray.length == 0) {
            return EMPTY_LONG_OBJECT_ARRAY;
        }
        Long[] longArray = new Long[lArray.length];
        for (int j = 0; j < lArray.length; ++j) {
            longArray[j] = lArray[j];
        }
        return longArray;
    }

    public static int[] toPrimitive(Integer[] integerArray) {
        if (integerArray == null) {
            return null;
        }
        if (integerArray.length == 0) {
            return EMPTY_INT_ARRAY;
        }
        int[] nArray = new int[integerArray.length];
        for (int j = 0; j < integerArray.length; ++j) {
            nArray[j] = integerArray[j];
        }
        return nArray;
    }

    public static int[] toPrimitive(Integer[] integerArray, int n) {
        if (integerArray == null) {
            return null;
        }
        if (integerArray.length == 0) {
            return EMPTY_INT_ARRAY;
        }
        int[] nArray = new int[integerArray.length];
        for (int j = 0; j < integerArray.length; ++j) {
            Integer n2 = integerArray[j];
            nArray[j] = n2 == null ? n : n2;
        }
        return nArray;
    }

    public static Integer[] toObject(int[] nArray) {
        if (nArray == null) {
            return null;
        }
        if (nArray.length == 0) {
            return EMPTY_INTEGER_OBJECT_ARRAY;
        }
        Integer[] integerArray = new Integer[nArray.length];
        for (int j = 0; j < nArray.length; ++j) {
            integerArray[j] = nArray[j];
        }
        return integerArray;
    }

    public static short[] toPrimitive(Short[] shortArray) {
        if (shortArray == null) {
            return null;
        }
        if (shortArray.length == 0) {
            return EMPTY_SHORT_ARRAY;
        }
        short[] sArray = new short[shortArray.length];
        for (int j = 0; j < shortArray.length; ++j) {
            sArray[j] = shortArray[j];
        }
        return sArray;
    }

    public static short[] toPrimitive(Short[] shortArray, short s) {
        if (shortArray == null) {
            return null;
        }
        if (shortArray.length == 0) {
            return EMPTY_SHORT_ARRAY;
        }
        short[] sArray = new short[shortArray.length];
        for (int j = 0; j < shortArray.length; ++j) {
            Short s2 = shortArray[j];
            sArray[j] = s2 == null ? s : s2;
        }
        return sArray;
    }

    public static Short[] toObject(short[] sArray) {
        if (sArray == null) {
            return null;
        }
        if (sArray.length == 0) {
            return EMPTY_SHORT_OBJECT_ARRAY;
        }
        Short[] shortArray = new Short[sArray.length];
        for (int j = 0; j < sArray.length; ++j) {
            shortArray[j] = sArray[j];
        }
        return shortArray;
    }

    public static byte[] toPrimitive(Byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        if (byteArray.length == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] byArray = new byte[byteArray.length];
        for (int j = 0; j < byteArray.length; ++j) {
            byArray[j] = byteArray[j];
        }
        return byArray;
    }

    public static byte[] toPrimitive(Byte[] byteArray, byte by) {
        if (byteArray == null) {
            return null;
        }
        if (byteArray.length == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] byArray = new byte[byteArray.length];
        for (int j = 0; j < byteArray.length; ++j) {
            Byte by2 = byteArray[j];
            byArray[j] = by2 == null ? by : by2;
        }
        return byArray;
    }

    public static Byte[] toObject(byte[] byArray) {
        if (byArray == null) {
            return null;
        }
        if (byArray.length == 0) {
            return EMPTY_BYTE_OBJECT_ARRAY;
        }
        Byte[] byteArray = new Byte[byArray.length];
        for (int j = 0; j < byArray.length; ++j) {
            byteArray[j] = byArray[j];
        }
        return byteArray;
    }

    public static double[] toPrimitive(Double[] doubleArray) {
        if (doubleArray == null) {
            return null;
        }
        if (doubleArray.length == 0) {
            return EMPTY_DOUBLE_ARRAY;
        }
        double[] dArray = new double[doubleArray.length];
        for (int j = 0; j < doubleArray.length; ++j) {
            dArray[j] = doubleArray[j];
        }
        return dArray;
    }

    public static double[] toPrimitive(Double[] doubleArray, double d2) {
        if (doubleArray == null) {
            return null;
        }
        if (doubleArray.length == 0) {
            return EMPTY_DOUBLE_ARRAY;
        }
        double[] dArray = new double[doubleArray.length];
        for (int j = 0; j < doubleArray.length; ++j) {
            Double d3 = doubleArray[j];
            dArray[j] = d3 == null ? d2 : d3;
        }
        return dArray;
    }

    public static Double[] toObject(double[] dArray) {
        if (dArray == null) {
            return null;
        }
        if (dArray.length == 0) {
            return EMPTY_DOUBLE_OBJECT_ARRAY;
        }
        Double[] doubleArray = new Double[dArray.length];
        for (int j = 0; j < dArray.length; ++j) {
            doubleArray[j] = dArray[j];
        }
        return doubleArray;
    }

    public static float[] toPrimitive(Float[] floatArray) {
        if (floatArray == null) {
            return null;
        }
        if (floatArray.length == 0) {
            return EMPTY_FLOAT_ARRAY;
        }
        float[] fArray = new float[floatArray.length];
        for (int j = 0; j < floatArray.length; ++j) {
            fArray[j] = floatArray[j].floatValue();
        }
        return fArray;
    }

    public static float[] toPrimitive(Float[] floatArray, float f2) {
        if (floatArray == null) {
            return null;
        }
        if (floatArray.length == 0) {
            return EMPTY_FLOAT_ARRAY;
        }
        float[] fArray = new float[floatArray.length];
        for (int j = 0; j < floatArray.length; ++j) {
            Float f3 = floatArray[j];
            fArray[j] = f3 == null ? f2 : f3.floatValue();
        }
        return fArray;
    }

    public static Float[] toObject(float[] fArray) {
        if (fArray == null) {
            return null;
        }
        if (fArray.length == 0) {
            return EMPTY_FLOAT_OBJECT_ARRAY;
        }
        Float[] floatArray = new Float[fArray.length];
        for (int j = 0; j < fArray.length; ++j) {
            floatArray[j] = Float.valueOf(fArray[j]);
        }
        return floatArray;
    }

    public static Object toPrimitive(Object object) {
        if (object == null) {
            return null;
        }
        Class<?> clazz = ClassUtils.wrapperToPrimitive(object.getClass().getComponentType());
        if (Integer.TYPE.equals(clazz)) {
            return ArrayUtils.toPrimitive((Integer[])object);
        }
        if (Long.TYPE.equals(clazz)) {
            return ArrayUtils.toPrimitive((Long[])object);
        }
        if (Short.TYPE.equals(clazz)) {
            return ArrayUtils.toPrimitive((Short[])object);
        }
        if (Double.TYPE.equals(clazz)) {
            return ArrayUtils.toPrimitive((Double[])object);
        }
        if (Float.TYPE.equals(clazz)) {
            return ArrayUtils.toPrimitive((Float[])object);
        }
        return object;
    }

    public static boolean[] toPrimitive(Boolean[] booleanArray) {
        if (booleanArray == null) {
            return null;
        }
        if (booleanArray.length == 0) {
            return EMPTY_BOOLEAN_ARRAY;
        }
        boolean[] blArray = new boolean[booleanArray.length];
        for (int j = 0; j < booleanArray.length; ++j) {
            blArray[j] = booleanArray[j];
        }
        return blArray;
    }

    public static boolean[] toPrimitive(Boolean[] booleanArray, boolean bl) {
        if (booleanArray == null) {
            return null;
        }
        if (booleanArray.length == 0) {
            return EMPTY_BOOLEAN_ARRAY;
        }
        boolean[] blArray = new boolean[booleanArray.length];
        for (int j = 0; j < booleanArray.length; ++j) {
            Boolean bl2 = booleanArray[j];
            blArray[j] = bl2 == null ? bl : bl2;
        }
        return blArray;
    }

    public static Boolean[] toObject(boolean[] blArray) {
        if (blArray == null) {
            return null;
        }
        if (blArray.length == 0) {
            return EMPTY_BOOLEAN_OBJECT_ARRAY;
        }
        Boolean[] booleanArray = new Boolean[blArray.length];
        for (int j = 0; j < blArray.length; ++j) {
            booleanArray[j] = blArray[j] ? Boolean.TRUE : Boolean.FALSE;
        }
        return booleanArray;
    }

    public static boolean isEmpty(Object[] objectArray) {
        return ArrayUtils.getLength(objectArray) == 0;
    }

    public static boolean isEmpty(long[] lArray) {
        return ArrayUtils.getLength(lArray) == 0;
    }

    public static boolean isEmpty(int[] nArray) {
        return ArrayUtils.getLength(nArray) == 0;
    }

    public static boolean isEmpty(short[] sArray) {
        return ArrayUtils.getLength(sArray) == 0;
    }

    public static boolean isEmpty(char[] cArray) {
        return ArrayUtils.getLength(cArray) == 0;
    }

    public static boolean isEmpty(byte[] byArray) {
        return ArrayUtils.getLength(byArray) == 0;
    }

    public static boolean isEmpty(double[] dArray) {
        return ArrayUtils.getLength(dArray) == 0;
    }

    public static boolean isEmpty(float[] fArray) {
        return ArrayUtils.getLength(fArray) == 0;
    }

    public static boolean isEmpty(boolean[] blArray) {
        return ArrayUtils.getLength(blArray) == 0;
    }

    public static <T> boolean isNotEmpty(T[] TArray) {
        return !ArrayUtils.isEmpty(TArray);
    }

    public static boolean isNotEmpty(long[] lArray) {
        return !ArrayUtils.isEmpty(lArray);
    }

    public static boolean isNotEmpty(int[] nArray) {
        return !ArrayUtils.isEmpty(nArray);
    }

    public static boolean isNotEmpty(short[] sArray) {
        return !ArrayUtils.isEmpty(sArray);
    }

    public static boolean isNotEmpty(char[] cArray) {
        return !ArrayUtils.isEmpty(cArray);
    }

    public static boolean isNotEmpty(byte[] byArray) {
        return !ArrayUtils.isEmpty(byArray);
    }

    public static boolean isNotEmpty(double[] dArray) {
        return !ArrayUtils.isEmpty(dArray);
    }

    public static boolean isNotEmpty(float[] fArray) {
        return !ArrayUtils.isEmpty(fArray);
    }

    public static boolean isNotEmpty(boolean[] blArray) {
        return !ArrayUtils.isEmpty(blArray);
    }

    public static <T> T[] addAll(T[] TArray, T ... object) {
        if (TArray == null) {
            return ArrayUtils.clone(object);
        }
        if (object == null) {
            return ArrayUtils.clone(TArray);
        }
        Class<?> clazz = TArray.getClass().getComponentType();
        Object[] objectArray = (Object[])Array.newInstance(clazz, TArray.length + ((T[])object).length);
        System.arraycopy(TArray, 0, objectArray, 0, TArray.length);
        try {
            System.arraycopy(object, 0, objectArray, TArray.length, ((T[])object).length);
        }
        catch (ArrayStoreException arrayStoreException) {
            object = object.getClass().getComponentType();
            if (!clazz.isAssignableFrom((Class<?>)object)) {
                throw new IllegalArgumentException("Cannot store " + ((Class)object).getName() + " in an array of " + clazz.getName(), arrayStoreException);
            }
            throw arrayStoreException;
        }
        return objectArray;
    }

    public static boolean[] addAll(boolean[] blArray, boolean ... blArray2) {
        if (blArray == null) {
            return ArrayUtils.clone(blArray2);
        }
        if (blArray2 == null) {
            return ArrayUtils.clone(blArray);
        }
        boolean[] blArray3 = new boolean[blArray.length + blArray2.length];
        System.arraycopy(blArray, 0, blArray3, 0, blArray.length);
        System.arraycopy(blArray2, 0, blArray3, blArray.length, blArray2.length);
        return blArray3;
    }

    public static char[] addAll(char[] cArray, char ... cArray2) {
        if (cArray == null) {
            return ArrayUtils.clone(cArray2);
        }
        if (cArray2 == null) {
            return ArrayUtils.clone(cArray);
        }
        char[] cArray3 = new char[cArray.length + cArray2.length];
        System.arraycopy(cArray, 0, cArray3, 0, cArray.length);
        System.arraycopy(cArray2, 0, cArray3, cArray.length, cArray2.length);
        return cArray3;
    }

    public static byte[] addAll(byte[] byArray, byte ... byArray2) {
        if (byArray == null) {
            return ArrayUtils.clone(byArray2);
        }
        if (byArray2 == null) {
            return ArrayUtils.clone(byArray);
        }
        byte[] byArray3 = new byte[byArray.length + byArray2.length];
        System.arraycopy(byArray, 0, byArray3, 0, byArray.length);
        System.arraycopy(byArray2, 0, byArray3, byArray.length, byArray2.length);
        return byArray3;
    }

    public static short[] addAll(short[] sArray, short ... sArray2) {
        if (sArray == null) {
            return ArrayUtils.clone(sArray2);
        }
        if (sArray2 == null) {
            return ArrayUtils.clone(sArray);
        }
        short[] sArray3 = new short[sArray.length + sArray2.length];
        System.arraycopy(sArray, 0, sArray3, 0, sArray.length);
        System.arraycopy(sArray2, 0, sArray3, sArray.length, sArray2.length);
        return sArray3;
    }

    public static int[] addAll(int[] nArray, int ... nArray2) {
        if (nArray == null) {
            return ArrayUtils.clone(nArray2);
        }
        if (nArray2 == null) {
            return ArrayUtils.clone(nArray);
        }
        int[] nArray3 = new int[nArray.length + nArray2.length];
        System.arraycopy(nArray, 0, nArray3, 0, nArray.length);
        System.arraycopy(nArray2, 0, nArray3, nArray.length, nArray2.length);
        return nArray3;
    }

    public static long[] addAll(long[] lArray, long ... lArray2) {
        if (lArray == null) {
            return ArrayUtils.clone(lArray2);
        }
        if (lArray2 == null) {
            return ArrayUtils.clone(lArray);
        }
        long[] lArray3 = new long[lArray.length + lArray2.length];
        System.arraycopy(lArray, 0, lArray3, 0, lArray.length);
        System.arraycopy(lArray2, 0, lArray3, lArray.length, lArray2.length);
        return lArray3;
    }

    public static float[] addAll(float[] fArray, float ... fArray2) {
        if (fArray == null) {
            return ArrayUtils.clone(fArray2);
        }
        if (fArray2 == null) {
            return ArrayUtils.clone(fArray);
        }
        float[] fArray3 = new float[fArray.length + fArray2.length];
        System.arraycopy(fArray, 0, fArray3, 0, fArray.length);
        System.arraycopy(fArray2, 0, fArray3, fArray.length, fArray2.length);
        return fArray3;
    }

    public static double[] addAll(double[] dArray, double ... dArray2) {
        if (dArray == null) {
            return ArrayUtils.clone(dArray2);
        }
        if (dArray2 == null) {
            return ArrayUtils.clone(dArray);
        }
        double[] dArray3 = new double[dArray.length + dArray2.length];
        System.arraycopy(dArray, 0, dArray3, 0, dArray.length);
        System.arraycopy(dArray2, 0, dArray3, dArray.length, dArray2.length);
        return dArray3;
    }

    public static <T> T[] add(T[] objectArray, T t) {
        Class<?> clazz;
        if (objectArray != null) {
            clazz = objectArray.getClass().getComponentType();
        } else if (t != null) {
            clazz = t.getClass();
        } else {
            throw new IllegalArgumentException("Arguments cannot both be null");
        }
        Object[] objectArray2 = (Object[])ArrayUtils.copyArrayGrow1(objectArray, clazz);
        objectArray = objectArray2;
        objectArray2[objectArray2.length - 1] = t;
        return objectArray;
    }

    public static boolean[] add(boolean[] blArray, boolean bl) {
        boolean[] blArray2 = (boolean[])ArrayUtils.copyArrayGrow1(blArray, Boolean.TYPE);
        blArray = blArray2;
        blArray2[blArray2.length - 1] = bl;
        return blArray;
    }

    public static byte[] add(byte[] byArray, byte by) {
        byte[] byArray2 = (byte[])ArrayUtils.copyArrayGrow1(byArray, Byte.TYPE);
        byArray = byArray2;
        byArray2[byArray2.length - 1] = by;
        return byArray;
    }

    public static char[] add(char[] cArray, char c2) {
        char[] cArray2 = (char[])ArrayUtils.copyArrayGrow1(cArray, Character.TYPE);
        cArray = cArray2;
        cArray2[cArray2.length - 1] = c2;
        return cArray;
    }

    public static double[] add(double[] dArray, double d2) {
        double[] dArray2 = (double[])ArrayUtils.copyArrayGrow1(dArray, Double.TYPE);
        dArray = dArray2;
        dArray2[dArray2.length - 1] = d2;
        return dArray;
    }

    public static float[] add(float[] fArray, float f2) {
        float[] fArray2 = (float[])ArrayUtils.copyArrayGrow1(fArray, Float.TYPE);
        fArray = fArray2;
        fArray2[fArray2.length - 1] = f2;
        return fArray;
    }

    public static int[] add(int[] nArray, int n) {
        int[] nArray2 = (int[])ArrayUtils.copyArrayGrow1(nArray, Integer.TYPE);
        nArray = nArray2;
        nArray2[nArray2.length - 1] = n;
        return nArray;
    }

    public static long[] add(long[] lArray, long l) {
        long[] lArray2 = (long[])ArrayUtils.copyArrayGrow1(lArray, Long.TYPE);
        lArray = lArray2;
        lArray2[lArray2.length - 1] = l;
        return lArray;
    }

    public static short[] add(short[] sArray, short s) {
        short[] sArray2 = (short[])ArrayUtils.copyArrayGrow1(sArray, Short.TYPE);
        sArray = sArray2;
        sArray2[sArray2.length - 1] = s;
        return sArray;
    }

    private static Object copyArrayGrow1(Object object, Class<?> clazz) {
        if (object != null) {
            int n = Array.getLength(object);
            Object object2 = Array.newInstance(object.getClass().getComponentType(), n + 1);
            System.arraycopy(object, 0, object2, 0, n);
            return object2;
        }
        return Array.newInstance(clazz, 1);
    }

    public static <T> T[] add(T[] TArray, int n, T t) {
        Class<?> clazz;
        if (TArray != null) {
            clazz = TArray.getClass().getComponentType();
        } else if (t != null) {
            clazz = t.getClass();
        } else {
            throw new IllegalArgumentException("Array and element cannot both be null");
        }
        return (Object[])ArrayUtils.add(TArray, n, t, clazz);
    }

    public static boolean[] add(boolean[] blArray, int n, boolean bl) {
        return (boolean[])ArrayUtils.add(blArray, n, bl, Boolean.TYPE);
    }

    public static char[] add(char[] cArray, int n, char c2) {
        return (char[])ArrayUtils.add(cArray, n, Character.valueOf(c2), Character.TYPE);
    }

    public static byte[] add(byte[] byArray, int n, byte by) {
        return (byte[])ArrayUtils.add(byArray, n, by, Byte.TYPE);
    }

    public static short[] add(short[] sArray, int n, short s) {
        return (short[])ArrayUtils.add(sArray, n, s, Short.TYPE);
    }

    public static int[] add(int[] nArray, int n, int n2) {
        return (int[])ArrayUtils.add(nArray, n, n2, Integer.TYPE);
    }

    public static long[] add(long[] lArray, int n, long l) {
        return (long[])ArrayUtils.add(lArray, n, l, Long.TYPE);
    }

    public static float[] add(float[] fArray, int n, float f2) {
        return (float[])ArrayUtils.add(fArray, n, Float.valueOf(f2), Float.TYPE);
    }

    public static double[] add(double[] dArray, int n, double d2) {
        return (double[])ArrayUtils.add(dArray, n, d2, Double.TYPE);
    }

    private static Object add(Object object, int n, Object object2, Class<?> object3) {
        if (object == null) {
            if (n != 0) {
                throw new IndexOutOfBoundsException("Index: " + n + ", Length: 0");
            }
            Object object4 = Array.newInstance(object3, 1);
            Array.set(object4, 0, object2);
            return object4;
        }
        int n2 = Array.getLength(object);
        if (n > n2 || n < 0) {
            throw new IndexOutOfBoundsException("Index: " + n + ", Length: " + n2);
        }
        object3 = Array.newInstance(object3, n2 + 1);
        System.arraycopy(object, 0, object3, 0, n);
        Array.set(object3, n, object2);
        if (n < n2) {
            System.arraycopy(object, n, object3, n + 1, n2 - n);
        }
        return object3;
    }

    public static <T> T[] remove(T[] TArray, int n) {
        return (Object[])ArrayUtils.remove(TArray, n);
    }

    public static <T> T[] removeElement(T[] TArray, Object object) {
        int n = ArrayUtils.indexOf(TArray, object);
        if (n == -1) {
            return ArrayUtils.clone(TArray);
        }
        return ArrayUtils.remove(TArray, n);
    }

    public static boolean[] remove(boolean[] blArray, int n) {
        return (boolean[])ArrayUtils.remove((Object)blArray, n);
    }

    public static boolean[] removeElement(boolean[] blArray, boolean n) {
        if ((n = ArrayUtils.indexOf(blArray, n != 0)) == -1) {
            return ArrayUtils.clone(blArray);
        }
        return ArrayUtils.remove(blArray, n);
    }

    public static byte[] remove(byte[] byArray, int n) {
        return (byte[])ArrayUtils.remove((Object)byArray, n);
    }

    public static byte[] removeElement(byte[] byArray, byte by) {
        int n = ArrayUtils.indexOf(byArray, by);
        by = (byte)n;
        if (n == -1) {
            return ArrayUtils.clone(byArray);
        }
        return ArrayUtils.remove(byArray, (int)by);
    }

    public static char[] remove(char[] cArray, int n) {
        return (char[])ArrayUtils.remove((Object)cArray, n);
    }

    public static char[] removeElement(char[] cArray, char c2) {
        int n = ArrayUtils.indexOf(cArray, c2);
        c2 = (char)n;
        if (n == -1) {
            return ArrayUtils.clone(cArray);
        }
        return ArrayUtils.remove(cArray, (int)c2);
    }

    public static double[] remove(double[] dArray, int n) {
        return (double[])ArrayUtils.remove((Object)dArray, n);
    }

    public static double[] removeElement(double[] dArray, double d2) {
        int n = ArrayUtils.indexOf(dArray, d2);
        if (n == -1) {
            return ArrayUtils.clone(dArray);
        }
        return ArrayUtils.remove(dArray, n);
    }

    public static float[] remove(float[] fArray, int n) {
        return (float[])ArrayUtils.remove((Object)fArray, n);
    }

    public static float[] removeElement(float[] fArray, float f2) {
        int n = ArrayUtils.indexOf(fArray, f2);
        if (n == -1) {
            return ArrayUtils.clone(fArray);
        }
        return ArrayUtils.remove(fArray, n);
    }

    public static int[] remove(int[] nArray, int n) {
        return (int[])ArrayUtils.remove((Object)nArray, n);
    }

    public static int[] removeElement(int[] nArray, int n) {
        if ((n = ArrayUtils.indexOf(nArray, n)) == -1) {
            return ArrayUtils.clone(nArray);
        }
        return ArrayUtils.remove(nArray, n);
    }

    public static long[] remove(long[] lArray, int n) {
        return (long[])ArrayUtils.remove((Object)lArray, n);
    }

    public static long[] removeElement(long[] lArray, long l) {
        int n = ArrayUtils.indexOf(lArray, l);
        if (n == -1) {
            return ArrayUtils.clone(lArray);
        }
        return ArrayUtils.remove(lArray, n);
    }

    public static short[] remove(short[] sArray, int n) {
        return (short[])ArrayUtils.remove((Object)sArray, n);
    }

    public static short[] removeElement(short[] sArray, short s) {
        int n = ArrayUtils.indexOf(sArray, s);
        s = (short)n;
        if (n == -1) {
            return ArrayUtils.clone(sArray);
        }
        return ArrayUtils.remove(sArray, (int)s);
    }

    private static Object remove(Object object, int n) {
        int n2 = ArrayUtils.getLength(object);
        if (n < 0 || n >= n2) {
            throw new IndexOutOfBoundsException("Index: " + n + ", Length: " + n2);
        }
        Object object2 = Array.newInstance(object.getClass().getComponentType(), n2 - 1);
        System.arraycopy(object, 0, object2, 0, n);
        if (n < n2 - 1) {
            System.arraycopy(object, n + 1, object2, n, n2 - n - 1);
        }
        return object2;
    }

    public static <T> T[] removeAll(T[] TArray, int ... nArray) {
        return (Object[])ArrayUtils.removeAll(TArray, nArray);
    }

    public static <T> T[] removeElements(T[] TArray, T ... object) {
        if (ArrayUtils.isEmpty(TArray) || ArrayUtils.isEmpty((Object[])object)) {
            return ArrayUtils.clone(TArray);
        }
        HashMap<T, MutableInt> hashMap = new HashMap<T, MutableInt>(((T[])object).length);
        for (Object object2 : object) {
            MutableInt mutableInt = (MutableInt)hashMap.get(object2);
            if (mutableInt == null) {
                hashMap.put(object2, new MutableInt(1));
                continue;
            }
            mutableInt.increment();
        }
        object = new BitSet();
        for (int j = 0; j < TArray.length; ++j) {
            Object object2;
            T t = TArray[j];
            object2 = (MutableInt)hashMap.get(t);
            if (object2 == null) continue;
            if (((MutableInt)object2).decrementAndGet() == 0) {
                hashMap.remove(t);
            }
            ((BitSet)object).set(j);
        }
        return (Object[])ArrayUtils.removeAll(TArray, object);
    }

    public static byte[] removeAll(byte[] byArray, int ... nArray) {
        return (byte[])ArrayUtils.removeAll((Object)byArray, nArray);
    }

    public static byte[] removeElements(byte[] byArray, byte ... object) {
        Number number;
        int n;
        if (ArrayUtils.isEmpty(byArray) || ArrayUtils.isEmpty((byte[])object)) {
            return ArrayUtils.clone(byArray);
        }
        HashMap<Byte, MutableInt> hashMap = new HashMap<Byte, MutableInt>(((byte[])object).length);
        int n2 = ((byte[])object).length;
        for (n = 0; n < n2; ++n) {
            number = object[n];
            MutableInt mutableInt = (MutableInt)hashMap.get(number);
            if (mutableInt == null) {
                hashMap.put((Byte)number, new MutableInt(1));
                continue;
            }
            mutableInt.increment();
        }
        object = new BitSet();
        for (n2 = 0; n2 < byArray.length; ++n2) {
            n = byArray[n2];
            number = (MutableInt)hashMap.get((byte)n);
            if (number == null) continue;
            if (((MutableInt)number).decrementAndGet() == 0) {
                hashMap.remove((byte)n);
            }
            ((BitSet)object).set(n2);
        }
        return (byte[])ArrayUtils.removeAll((Object)byArray, (BitSet)object);
    }

    public static short[] removeAll(short[] sArray, int ... nArray) {
        return (short[])ArrayUtils.removeAll((Object)sArray, nArray);
    }

    public static short[] removeElements(short[] sArray, short ... object) {
        Number number;
        int n;
        if (ArrayUtils.isEmpty(sArray) || ArrayUtils.isEmpty((short[])object)) {
            return ArrayUtils.clone(sArray);
        }
        HashMap<Short, MutableInt> hashMap = new HashMap<Short, MutableInt>(((short[])object).length);
        int n2 = ((short[])object).length;
        for (n = 0; n < n2; ++n) {
            number = object[n];
            MutableInt mutableInt = (MutableInt)hashMap.get(number);
            if (mutableInt == null) {
                hashMap.put((Short)number, new MutableInt(1));
                continue;
            }
            mutableInt.increment();
        }
        object = new BitSet();
        for (n2 = 0; n2 < sArray.length; ++n2) {
            n = sArray[n2];
            number = (MutableInt)hashMap.get((short)n);
            if (number == null) continue;
            if (((MutableInt)number).decrementAndGet() == 0) {
                hashMap.remove((short)n);
            }
            ((BitSet)object).set(n2);
        }
        return (short[])ArrayUtils.removeAll((Object)sArray, (BitSet)object);
    }

    public static int[] removeAll(int[] nArray, int ... nArray2) {
        return (int[])ArrayUtils.removeAll((Object)nArray, nArray2);
    }

    public static int[] removeElements(int[] nArray, int ... object) {
        Number number;
        int n;
        if (ArrayUtils.isEmpty(nArray) || ArrayUtils.isEmpty((int[])object)) {
            return ArrayUtils.clone(nArray);
        }
        HashMap<Integer, MutableInt> hashMap = new HashMap<Integer, MutableInt>(((int[])object).length);
        int n2 = ((int[])object).length;
        for (n = 0; n < n2; ++n) {
            number = object[n];
            MutableInt mutableInt = (MutableInt)hashMap.get(number);
            if (mutableInt == null) {
                hashMap.put((Integer)number, new MutableInt(1));
                continue;
            }
            mutableInt.increment();
        }
        object = new BitSet();
        for (n2 = 0; n2 < nArray.length; ++n2) {
            n = nArray[n2];
            number = (MutableInt)hashMap.get(n);
            if (number == null) continue;
            if (((MutableInt)number).decrementAndGet() == 0) {
                hashMap.remove(n);
            }
            ((BitSet)object).set(n2);
        }
        return (int[])ArrayUtils.removeAll((Object)nArray, (BitSet)object);
    }

    public static char[] removeAll(char[] cArray, int ... nArray) {
        return (char[])ArrayUtils.removeAll((Object)cArray, nArray);
    }

    public static char[] removeElements(char[] cArray, char ... object) {
        Comparable<Character> comparable;
        int n;
        if (ArrayUtils.isEmpty(cArray) || ArrayUtils.isEmpty((char[])object)) {
            return ArrayUtils.clone(cArray);
        }
        HashMap<Character, MutableInt> hashMap = new HashMap<Character, MutableInt>(((char[])object).length);
        int n2 = ((char[])object).length;
        for (n = 0; n < n2; ++n) {
            comparable = Character.valueOf(object[n]);
            MutableInt mutableInt = (MutableInt)hashMap.get(comparable);
            if (mutableInt == null) {
                hashMap.put((Character)comparable, new MutableInt(1));
                continue;
            }
            mutableInt.increment();
        }
        object = new BitSet();
        for (n2 = 0; n2 < cArray.length; ++n2) {
            n = cArray[n2];
            comparable = (MutableInt)hashMap.get(Character.valueOf((char)n));
            if (comparable == null) continue;
            if (((MutableInt)comparable).decrementAndGet() == 0) {
                hashMap.remove(Character.valueOf((char)n));
            }
            ((BitSet)object).set(n2);
        }
        return (char[])ArrayUtils.removeAll((Object)cArray, (BitSet)object);
    }

    public static long[] removeAll(long[] lArray, int ... nArray) {
        return (long[])ArrayUtils.removeAll((Object)lArray, nArray);
    }

    public static long[] removeElements(long[] lArray, long ... object) {
        Number number;
        if (ArrayUtils.isEmpty(lArray) || ArrayUtils.isEmpty((long[])object)) {
            return ArrayUtils.clone(lArray);
        }
        HashMap<Long, MutableInt> hashMap = new HashMap<Long, MutableInt>(((long[])object).length);
        int n = ((long[])object).length;
        for (int j = 0; j < n; ++j) {
            number = object[j];
            MutableInt mutableInt = (MutableInt)hashMap.get(number);
            if (mutableInt == null) {
                hashMap.put((Long)number, new MutableInt(1));
                continue;
            }
            mutableInt.increment();
        }
        object = new BitSet();
        for (n = 0; n < lArray.length; ++n) {
            long l = lArray[n];
            number = (MutableInt)hashMap.get(l);
            if (number == null) continue;
            if (((MutableInt)number).decrementAndGet() == 0) {
                hashMap.remove(l);
            }
            ((BitSet)object).set(n);
        }
        return (long[])ArrayUtils.removeAll((Object)lArray, (BitSet)object);
    }

    public static float[] removeAll(float[] fArray, int ... nArray) {
        return (float[])ArrayUtils.removeAll((Object)fArray, nArray);
    }

    public static float[] removeElements(float[] fArray, float ... object) {
        Number number;
        if (ArrayUtils.isEmpty(fArray) || ArrayUtils.isEmpty((float[])object)) {
            return ArrayUtils.clone(fArray);
        }
        HashMap<Float, MutableInt> hashMap = new HashMap<Float, MutableInt>(((float[])object).length);
        int n = ((float[])object).length;
        for (int j = 0; j < n; ++j) {
            number = Float.valueOf(object[j]);
            MutableInt mutableInt = (MutableInt)hashMap.get(number);
            if (mutableInt == null) {
                hashMap.put((Float)number, new MutableInt(1));
                continue;
            }
            mutableInt.increment();
        }
        object = new BitSet();
        for (n = 0; n < fArray.length; ++n) {
            float f2 = fArray[n];
            number = (MutableInt)hashMap.get(Float.valueOf(f2));
            if (number == null) continue;
            if (((MutableInt)number).decrementAndGet() == 0) {
                hashMap.remove(Float.valueOf(f2));
            }
            ((BitSet)object).set(n);
        }
        return (float[])ArrayUtils.removeAll((Object)fArray, (BitSet)object);
    }

    public static double[] removeAll(double[] dArray, int ... nArray) {
        return (double[])ArrayUtils.removeAll((Object)dArray, nArray);
    }

    public static double[] removeElements(double[] dArray, double ... object) {
        Number number;
        if (ArrayUtils.isEmpty(dArray) || ArrayUtils.isEmpty((double[])object)) {
            return ArrayUtils.clone(dArray);
        }
        HashMap<Double, MutableInt> hashMap = new HashMap<Double, MutableInt>(((double[])object).length);
        int n = ((double[])object).length;
        for (int j = 0; j < n; ++j) {
            number = object[j];
            MutableInt mutableInt = (MutableInt)hashMap.get(number);
            if (mutableInt == null) {
                hashMap.put((Double)number, new MutableInt(1));
                continue;
            }
            mutableInt.increment();
        }
        object = new BitSet();
        for (n = 0; n < dArray.length; ++n) {
            double d2 = dArray[n];
            number = (MutableInt)hashMap.get(d2);
            if (number == null) continue;
            if (((MutableInt)number).decrementAndGet() == 0) {
                hashMap.remove(d2);
            }
            ((BitSet)object).set(n);
        }
        return (double[])ArrayUtils.removeAll((Object)dArray, (BitSet)object);
    }

    public static boolean[] removeAll(boolean[] blArray, int ... nArray) {
        return (boolean[])ArrayUtils.removeAll((Object)blArray, nArray);
    }

    public static boolean[] removeElements(boolean[] blArray, boolean ... object) {
        Comparable<Boolean> comparable;
        int n;
        if (ArrayUtils.isEmpty(blArray) || ArrayUtils.isEmpty((boolean[])object)) {
            return ArrayUtils.clone(blArray);
        }
        HashMap<Boolean, MutableInt> hashMap = new HashMap<Boolean, MutableInt>(2);
        int n2 = ((boolean[])object).length;
        for (n = 0; n < n2; ++n) {
            comparable = object[n];
            MutableInt mutableInt = (MutableInt)hashMap.get(comparable);
            if (mutableInt == null) {
                hashMap.put((Boolean)comparable, new MutableInt(1));
                continue;
            }
            mutableInt.increment();
        }
        object = new BitSet();
        for (n2 = 0; n2 < blArray.length; ++n2) {
            n = blArray[n2];
            comparable = (MutableInt)hashMap.get(n != 0);
            if (comparable == null) continue;
            if (((MutableInt)comparable).decrementAndGet() == 0) {
                hashMap.remove(n != 0);
            }
            ((BitSet)object).set(n2);
        }
        return (boolean[])ArrayUtils.removeAll((Object)blArray, (BitSet)object);
    }

    static Object removeAll(Object object, int ... nArray) {
        int n;
        int n2;
        int n3 = ArrayUtils.getLength(object);
        int n4 = 0;
        nArray = ArrayUtils.clone(nArray);
        Arrays.sort(nArray);
        if (ArrayUtils.isNotEmpty(nArray)) {
            int n5 = nArray.length;
            n2 = n3;
            while (--n5 >= 0) {
                n = nArray[n5];
                if (n < 0 || n >= n3) {
                    throw new IndexOutOfBoundsException("Index: " + n + ", Length: " + n3);
                }
                if (n >= n2) continue;
                ++n4;
                n2 = n;
            }
        }
        Object object2 = Array.newInstance(object.getClass().getComponentType(), n3 - n4);
        if (n4 < n3) {
            n2 = n3;
            n = n3 - n4;
            for (n3 = nArray.length - 1; n3 >= 0; --n3) {
                n4 = nArray[n3];
                if (n2 - n4 > 1) {
                    n2 = n2 - n4 - 1;
                    System.arraycopy(object, n4 + 1, object2, n -= n2, n2);
                }
                n2 = n4;
            }
            if (n2 > 0) {
                System.arraycopy(object, 0, object2, 0, n2);
            }
        }
        return object2;
    }

    static Object removeAll(Object object, BitSet bitSet) {
        int n;
        int n2;
        int n3 = ArrayUtils.getLength(object);
        int n4 = bitSet.cardinality();
        Object object2 = Array.newInstance(object.getClass().getComponentType(), n3 - n4);
        int n5 = 0;
        int n6 = 0;
        while ((n2 = bitSet.nextSetBit(n5)) != -1) {
            n = n2 - n5;
            if (n > 0) {
                System.arraycopy(object, n5, object2, n6, n);
                n6 += n;
            }
            n5 = bitSet.nextClearBit(n2);
        }
        n = n3 - n5;
        if (n > 0) {
            System.arraycopy(object, n5, object2, n6, n);
        }
        return object2;
    }

    public static <T extends Comparable<? super T>> boolean isSorted(T[] TArray) {
        return ArrayUtils.isSorted(TArray, new Comparator<T>(){

            @Override
            public final int compare(T t, T t2) {
                return t.compareTo(t2);
            }
        });
    }

    public static <T> boolean isSorted(T[] TArray, Comparator<T> comparator) {
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator should not be null.");
        }
        if (TArray == null || TArray.length < 2) {
            return true;
        }
        T t = TArray[0];
        int n = TArray.length;
        for (int j = 1; j < n; ++j) {
            T t2 = TArray[j];
            if (comparator.compare(t, t2) > 0) {
                return false;
            }
            t = t2;
        }
        return true;
    }

    public static boolean isSorted(int[] nArray) {
        if (nArray == null || nArray.length < 2) {
            return true;
        }
        int n = nArray[0];
        int n2 = nArray.length;
        for (int j = 1; j < n2; ++j) {
            int n3 = nArray[j];
            if (NumberUtils.compare(n, n3) > 0) {
                return false;
            }
            n = n3;
        }
        return true;
    }

    public static boolean isSorted(long[] lArray) {
        if (lArray == null || lArray.length < 2) {
            return true;
        }
        long l = lArray[0];
        int n = lArray.length;
        for (int j = 1; j < n; ++j) {
            long l2 = lArray[j];
            if (NumberUtils.compare(l, l2) > 0) {
                return false;
            }
            l = l2;
        }
        return true;
    }

    public static boolean isSorted(short[] sArray) {
        if (sArray == null || sArray.length < 2) {
            return true;
        }
        short s = sArray[0];
        int n = sArray.length;
        for (int j = 1; j < n; ++j) {
            short s2 = sArray[j];
            if (NumberUtils.compare(s, s2) > 0) {
                return false;
            }
            s = s2;
        }
        return true;
    }

    public static boolean isSorted(double[] dArray) {
        if (dArray == null || dArray.length < 2) {
            return true;
        }
        double d2 = dArray[0];
        int n = dArray.length;
        for (int j = 1; j < n; ++j) {
            double d3 = dArray[j];
            if (Double.compare(d2, d3) > 0) {
                return false;
            }
            d2 = d3;
        }
        return true;
    }

    public static boolean isSorted(float[] fArray) {
        if (fArray == null || fArray.length < 2) {
            return true;
        }
        float f2 = fArray[0];
        int n = fArray.length;
        for (int j = 1; j < n; ++j) {
            float f3 = fArray[j];
            if (Float.compare(f2, f3) > 0) {
                return false;
            }
            f2 = f3;
        }
        return true;
    }

    public static boolean isSorted(byte[] byArray) {
        if (byArray == null || byArray.length < 2) {
            return true;
        }
        byte by = byArray[0];
        int n = byArray.length;
        for (int j = 1; j < n; ++j) {
            byte by2 = byArray[j];
            if (NumberUtils.compare(by, by2) > 0) {
                return false;
            }
            by = by2;
        }
        return true;
    }

    public static boolean isSorted(char[] cArray) {
        if (cArray == null || cArray.length < 2) {
            return true;
        }
        char c2 = cArray[0];
        int n = cArray.length;
        for (int j = 1; j < n; ++j) {
            char c3 = cArray[j];
            if (CharUtils.compare(c2, c3) > 0) {
                return false;
            }
            c2 = c3;
        }
        return true;
    }

    public static boolean isSorted(boolean[] blArray) {
        if (blArray == null || blArray.length < 2) {
            return true;
        }
        boolean bl = blArray[0];
        int n = blArray.length;
        for (int j = 1; j < n; ++j) {
            boolean bl2 = blArray[j];
            if (BooleanUtils.compare(bl, bl2) > 0) {
                return false;
            }
            bl = bl2;
        }
        return true;
    }

    public static boolean[] removeAllOccurences(boolean[] blArray, boolean bl) {
        int n = ArrayUtils.indexOf(blArray, bl);
        if (n == -1) {
            return ArrayUtils.clone(blArray);
        }
        int[] nArray = new int[blArray.length - n];
        int[] nArray2 = nArray;
        nArray[0] = n;
        int n2 = 1;
        while ((n = ArrayUtils.indexOf(blArray, bl, nArray2[n2 - 1] + 1)) != -1) {
            nArray2[n2++] = n;
        }
        return ArrayUtils.removeAll(blArray, Arrays.copyOf(nArray2, n2));
    }

    public static char[] removeAllOccurences(char[] cArray, char c2) {
        int n = ArrayUtils.indexOf(cArray, c2);
        if (n == -1) {
            return ArrayUtils.clone(cArray);
        }
        int[] nArray = new int[cArray.length - n];
        int[] nArray2 = nArray;
        nArray[0] = n;
        int n2 = 1;
        while ((n = ArrayUtils.indexOf(cArray, c2, nArray2[n2 - 1] + 1)) != -1) {
            nArray2[n2++] = n;
        }
        return ArrayUtils.removeAll(cArray, Arrays.copyOf(nArray2, n2));
    }

    public static byte[] removeAllOccurences(byte[] byArray, byte by) {
        int n = ArrayUtils.indexOf(byArray, by);
        if (n == -1) {
            return ArrayUtils.clone(byArray);
        }
        int[] nArray = new int[byArray.length - n];
        int[] nArray2 = nArray;
        nArray[0] = n;
        int n2 = 1;
        while ((n = ArrayUtils.indexOf(byArray, by, nArray2[n2 - 1] + 1)) != -1) {
            nArray2[n2++] = n;
        }
        return ArrayUtils.removeAll(byArray, Arrays.copyOf(nArray2, n2));
    }

    public static short[] removeAllOccurences(short[] sArray, short s) {
        int n = ArrayUtils.indexOf(sArray, s);
        if (n == -1) {
            return ArrayUtils.clone(sArray);
        }
        int[] nArray = new int[sArray.length - n];
        int[] nArray2 = nArray;
        nArray[0] = n;
        int n2 = 1;
        while ((n = ArrayUtils.indexOf(sArray, s, nArray2[n2 - 1] + 1)) != -1) {
            nArray2[n2++] = n;
        }
        return ArrayUtils.removeAll(sArray, Arrays.copyOf(nArray2, n2));
    }

    public static int[] removeAllOccurences(int[] nArray, int n) {
        int n2 = ArrayUtils.indexOf(nArray, n);
        if (n2 == -1) {
            return ArrayUtils.clone(nArray);
        }
        int[] nArray2 = new int[nArray.length - n2];
        int[] nArray3 = nArray2;
        nArray2[0] = n2;
        int n3 = 1;
        while ((n2 = ArrayUtils.indexOf(nArray, n, nArray3[n3 - 1] + 1)) != -1) {
            nArray3[n3++] = n2;
        }
        return ArrayUtils.removeAll(nArray, Arrays.copyOf(nArray3, n3));
    }

    public static long[] removeAllOccurences(long[] lArray, long l) {
        int n = ArrayUtils.indexOf(lArray, l);
        if (n == -1) {
            return ArrayUtils.clone(lArray);
        }
        int[] nArray = new int[lArray.length - n];
        int[] nArray2 = nArray;
        nArray[0] = n;
        int n2 = 1;
        while ((n = ArrayUtils.indexOf(lArray, l, nArray2[n2 - 1] + 1)) != -1) {
            nArray2[n2++] = n;
        }
        return ArrayUtils.removeAll(lArray, Arrays.copyOf(nArray2, n2));
    }

    public static float[] removeAllOccurences(float[] fArray, float f2) {
        int n = ArrayUtils.indexOf(fArray, f2);
        if (n == -1) {
            return ArrayUtils.clone(fArray);
        }
        int[] nArray = new int[fArray.length - n];
        int[] nArray2 = nArray;
        nArray[0] = n;
        int n2 = 1;
        while ((n = ArrayUtils.indexOf(fArray, f2, nArray2[n2 - 1] + 1)) != -1) {
            nArray2[n2++] = n;
        }
        return ArrayUtils.removeAll(fArray, Arrays.copyOf(nArray2, n2));
    }

    public static double[] removeAllOccurences(double[] dArray, double d2) {
        int n = ArrayUtils.indexOf(dArray, d2);
        if (n == -1) {
            return ArrayUtils.clone(dArray);
        }
        int[] nArray = new int[dArray.length - n];
        int[] nArray2 = nArray;
        nArray[0] = n;
        int n2 = 1;
        while ((n = ArrayUtils.indexOf(dArray, d2, nArray2[n2 - 1] + 1)) != -1) {
            nArray2[n2++] = n;
        }
        return ArrayUtils.removeAll(dArray, Arrays.copyOf(nArray2, n2));
    }

    public static <T> T[] removeAllOccurences(T[] TArray, T t) {
        int n = ArrayUtils.indexOf(TArray, t);
        if (n == -1) {
            return ArrayUtils.clone(TArray);
        }
        int[] nArray = new int[TArray.length - n];
        int[] nArray2 = nArray;
        nArray[0] = n;
        int n2 = 1;
        while ((n = ArrayUtils.indexOf(TArray, t, nArray2[n2 - 1] + 1)) != -1) {
            nArray2[n2++] = n;
        }
        return ArrayUtils.removeAll(TArray, Arrays.copyOf(nArray2, n2));
    }
}

