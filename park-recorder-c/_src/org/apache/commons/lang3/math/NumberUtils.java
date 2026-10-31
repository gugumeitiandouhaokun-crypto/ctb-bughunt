/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.math;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.Validate;

public class NumberUtils {
    public static final Long LONG_ZERO = 0L;
    public static final Long LONG_ONE = 1L;
    public static final Long LONG_MINUS_ONE = -1L;
    public static final Integer INTEGER_ZERO = 0;
    public static final Integer INTEGER_ONE = 1;
    public static final Integer INTEGER_MINUS_ONE = -1;
    public static final Short SHORT_ZERO = 0;
    public static final Short SHORT_ONE = 1;
    public static final Short SHORT_MINUS_ONE = -1;
    public static final Byte BYTE_ZERO = 0;
    public static final Byte BYTE_ONE = 1;
    public static final Byte BYTE_MINUS_ONE = -1;
    public static final Double DOUBLE_ZERO = 0.0;
    public static final Double DOUBLE_ONE = 1.0;
    public static final Double DOUBLE_MINUS_ONE = -1.0;
    public static final Float FLOAT_ZERO = Float.valueOf(0.0f);
    public static final Float FLOAT_ONE = Float.valueOf(1.0f);
    public static final Float FLOAT_MINUS_ONE = Float.valueOf(-1.0f);

    public static int toInt(String string) {
        return NumberUtils.toInt(string, 0);
    }

    public static int toInt(String string, int n) {
        if (string == null) {
            return n;
        }
        try {
            return Integer.parseInt(string);
        }
        catch (NumberFormatException numberFormatException) {
            return n;
        }
    }

    public static long toLong(String string) {
        return NumberUtils.toLong(string, 0L);
    }

    public static long toLong(String string, long l) {
        if (string == null) {
            return l;
        }
        try {
            return Long.parseLong(string);
        }
        catch (NumberFormatException numberFormatException) {
            return l;
        }
    }

    public static float toFloat(String string) {
        return NumberUtils.toFloat(string, 0.0f);
    }

    public static float toFloat(String string, float f2) {
        if (string == null) {
            return f2;
        }
        try {
            return Float.parseFloat(string);
        }
        catch (NumberFormatException numberFormatException) {
            return f2;
        }
    }

    public static double toDouble(String string) {
        return NumberUtils.toDouble(string, 0.0);
    }

    public static double toDouble(String string, double d2) {
        if (string == null) {
            return d2;
        }
        try {
            return Double.parseDouble(string);
        }
        catch (NumberFormatException numberFormatException) {
            return d2;
        }
    }

    public static byte toByte(String string) {
        return NumberUtils.toByte(string, (byte)0);
    }

    public static byte toByte(String string, byte by) {
        if (string == null) {
            return by;
        }
        try {
            return Byte.parseByte(string);
        }
        catch (NumberFormatException numberFormatException) {
            return by;
        }
    }

    public static short toShort(String string) {
        return NumberUtils.toShort(string, (short)0);
    }

    public static short toShort(String string, short s) {
        if (string == null) {
            return s;
        }
        try {
            return Short.parseShort(string);
        }
        catch (NumberFormatException numberFormatException) {
            return s;
        }
    }

    public static Number createNumber(String string) {
        Object object;
        String string2;
        if (string == null) {
            return null;
        }
        if (StringUtils.isBlank(string)) {
            throw new NumberFormatException("A blank string is not a valid number");
        }
        String[] stringArray = new String[]{"0x", "0X", "-0x", "-0X", "#", "-#"};
        int n = 0;
        for (int i2 = 0; i2 < 6; ++i2) {
            String string3 = stringArray[i2];
            if (!string.startsWith(string3)) continue;
            n = 0 + string3.length();
            break;
        }
        if (n > 0) {
            int n2;
            char c2 = '\u0000';
            for (n2 = n; n2 < string.length() && (c2 = string.charAt(n2)) == '0'; ++n2) {
                ++n;
            }
            n2 = string.length() - n;
            if (n2 > 16 || n2 == 16 && c2 > '7') {
                return NumberUtils.createBigInteger(string);
            }
            if (n2 > 8 || n2 == 8 && c2 > '7') {
                return NumberUtils.createLong(string);
            }
            return NumberUtils.createInteger(string);
        }
        String string4 = string;
        char c3 = string4.charAt(string4.length() - 1);
        n = string.indexOf(46);
        int n3 = string.indexOf(101) + string.indexOf(69) + 1;
        if (n >= 0) {
            if (n3 >= 0) {
                if (n3 < n || n3 > string.length()) {
                    throw new NumberFormatException(string + " is not a valid number.");
                }
                string2 = string.substring(n + 1, n3);
            } else {
                string2 = string.substring(n + 1);
            }
            object = NumberUtils.getMantissa(string, n);
        } else {
            if (n3 >= 0) {
                if (n3 > string.length()) {
                    throw new NumberFormatException(string + " is not a valid number.");
                }
                object = NumberUtils.getMantissa(string, n3);
            } else {
                object = NumberUtils.getMantissa(string);
            }
            string2 = null;
        }
        if (!Character.isDigit(c3) && c3 != '.') {
            String string5 = n3 >= 0 && n3 < string.length() - 1 ? string.substring(n3 + 1, string.length() - 1) : null;
            String string6 = string.substring(0, string.length() - 1);
            boolean bl = NumberUtils.isAllZeros((String)object) && NumberUtils.isAllZeros(string5);
            switch (c3) {
                case 'L': 
                case 'l': {
                    if (string2 == null && string5 == null && (string6.charAt(0) == '-' && NumberUtils.isDigits(string6.substring(1)) || NumberUtils.isDigits(string6))) {
                        try {
                            return NumberUtils.createLong(string6);
                        }
                        catch (NumberFormatException numberFormatException) {
                            return NumberUtils.createBigInteger(string6);
                        }
                    }
                    throw new NumberFormatException(string + " is not a valid number.");
                }
                case 'F': 
                case 'f': {
                    try {
                        Float f2 = NumberUtils.createFloat(string);
                        if (!f2.isInfinite() && (f2.floatValue() != 0.0f || bl)) {
                            return f2;
                        }
                    }
                    catch (NumberFormatException numberFormatException) {}
                }
                case 'D': 
                case 'd': {
                    try {
                        Double d2 = NumberUtils.createDouble(string);
                        if (!d2.isInfinite() && ((double)d2.floatValue() != 0.0 || bl)) {
                            return d2;
                        }
                    }
                    catch (NumberFormatException numberFormatException) {}
                    try {
                        return NumberUtils.createBigDecimal(string6);
                    }
                    catch (NumberFormatException numberFormatException) {}
                }
            }
            throw new NumberFormatException(string + " is not a valid number.");
        }
        String string7 = n3 >= 0 && n3 < string.length() - 1 ? string.substring(n3 + 1, string.length()) : null;
        if (string2 == null && string7 == null) {
            try {
                return NumberUtils.createInteger(string);
            }
            catch (NumberFormatException numberFormatException) {
                try {
                    return NumberUtils.createLong(string);
                }
                catch (NumberFormatException numberFormatException2) {
                    return NumberUtils.createBigInteger(string);
                }
            }
        }
        n = NumberUtils.isAllZeros((String)object) && NumberUtils.isAllZeros(string7) ? 1 : 0;
        try {
            object = NumberUtils.createFloat(string);
            Double d3 = NumberUtils.createDouble(string);
            if (!((Float)object).isInfinite() && (((Float)object).floatValue() != 0.0f || n != 0) && ((Float)object).toString().equals(d3.toString())) {
                return object;
            }
            if (!(d3.isInfinite() || d3 == 0.0 && n == 0)) {
                BigDecimal bigDecimal = NumberUtils.createBigDecimal(string);
                if (bigDecimal.compareTo(BigDecimal.valueOf(d3)) == 0) {
                    return d3;
                }
                return bigDecimal;
            }
        }
        catch (NumberFormatException numberFormatException) {}
        return NumberUtils.createBigDecimal(string);
    }

    private static String getMantissa(String string) {
        String string2 = string;
        return NumberUtils.getMantissa(string2, string2.length());
    }

    private static String getMantissa(String string, int n) {
        char c2 = string.charAt(0);
        if (c2 == '-' || c2 == '+') {
            return string.substring(1, n);
        }
        return string.substring(0, n);
    }

    private static boolean isAllZeros(String string) {
        if (string == null) {
            return true;
        }
        for (int i2 = string.length() - 1; i2 >= 0; --i2) {
            if (string.charAt(i2) == '0') continue;
            return false;
        }
        return string.length() > 0;
    }

    public static Float createFloat(String string) {
        if (string == null) {
            return null;
        }
        return Float.valueOf(string);
    }

    public static Double createDouble(String string) {
        if (string == null) {
            return null;
        }
        return Double.valueOf(string);
    }

    public static Integer createInteger(String string) {
        if (string == null) {
            return null;
        }
        return Integer.decode(string);
    }

    public static Long createLong(String string) {
        if (string == null) {
            return null;
        }
        return Long.decode(string);
    }

    public static BigInteger createBigInteger(String object) {
        if (object == null) {
            return null;
        }
        int n = 0;
        int n2 = 10;
        boolean bl = false;
        if (((String)object).startsWith("-")) {
            bl = true;
            n = 1;
        }
        if (((String)object).startsWith("0x", n) || ((String)object).startsWith("0X", n)) {
            n2 = 16;
            n += 2;
        } else if (((String)object).startsWith("#", n)) {
            n2 = 16;
            ++n;
        } else if (((String)object).startsWith("0", n) && ((String)object).length() > n + 1) {
            n2 = 8;
            ++n;
        }
        object = new BigInteger(((String)object).substring(n), n2);
        if (bl) {
            return ((BigInteger)object).negate();
        }
        return object;
    }

    public static BigDecimal createBigDecimal(String string) {
        if (string == null) {
            return null;
        }
        if (StringUtils.isBlank(string)) {
            throw new NumberFormatException("A blank string is not a valid number");
        }
        if (string.trim().startsWith("--")) {
            throw new NumberFormatException(string + " is not a valid number.");
        }
        return new BigDecimal(string);
    }

    public static long min(long ... lArray) {
        NumberUtils.validateArray(lArray);
        long l = lArray[0];
        for (int i2 = 1; i2 < lArray.length; ++i2) {
            if (lArray[i2] >= l) continue;
            l = lArray[i2];
        }
        return l;
    }

    public static int min(int ... nArray) {
        NumberUtils.validateArray(nArray);
        int n = nArray[0];
        for (int i2 = 1; i2 < nArray.length; ++i2) {
            if (nArray[i2] >= n) continue;
            n = nArray[i2];
        }
        return n;
    }

    public static short min(short ... sArray) {
        NumberUtils.validateArray(sArray);
        short s = sArray[0];
        for (int i2 = 1; i2 < sArray.length; ++i2) {
            if (sArray[i2] >= s) continue;
            s = sArray[i2];
        }
        return s;
    }

    public static byte min(byte ... byArray) {
        NumberUtils.validateArray(byArray);
        byte by = byArray[0];
        for (int i2 = 1; i2 < byArray.length; ++i2) {
            if (byArray[i2] >= by) continue;
            by = byArray[i2];
        }
        return by;
    }

    public static double min(double ... dArray) {
        NumberUtils.validateArray(dArray);
        double d2 = dArray[0];
        for (int i2 = 1; i2 < dArray.length; ++i2) {
            if (Double.isNaN(dArray[i2])) {
                return Double.NaN;
            }
            if (!(dArray[i2] < d2)) continue;
            d2 = dArray[i2];
        }
        return d2;
    }

    public static float min(float ... fArray) {
        NumberUtils.validateArray(fArray);
        float f2 = fArray[0];
        for (int i2 = 1; i2 < fArray.length; ++i2) {
            if (Float.isNaN(fArray[i2])) {
                return Float.NaN;
            }
            if (!(fArray[i2] < f2)) continue;
            f2 = fArray[i2];
        }
        return f2;
    }

    public static long max(long ... lArray) {
        NumberUtils.validateArray(lArray);
        long l = lArray[0];
        for (int i2 = 1; i2 < lArray.length; ++i2) {
            if (lArray[i2] <= l) continue;
            l = lArray[i2];
        }
        return l;
    }

    public static int max(int ... nArray) {
        NumberUtils.validateArray(nArray);
        int n = nArray[0];
        for (int i2 = 1; i2 < nArray.length; ++i2) {
            if (nArray[i2] <= n) continue;
            n = nArray[i2];
        }
        return n;
    }

    public static short max(short ... sArray) {
        NumberUtils.validateArray(sArray);
        short s = sArray[0];
        for (int i2 = 1; i2 < sArray.length; ++i2) {
            if (sArray[i2] <= s) continue;
            s = sArray[i2];
        }
        return s;
    }

    public static byte max(byte ... byArray) {
        NumberUtils.validateArray(byArray);
        byte by = byArray[0];
        for (int i2 = 1; i2 < byArray.length; ++i2) {
            if (byArray[i2] <= by) continue;
            by = byArray[i2];
        }
        return by;
    }

    public static double max(double ... dArray) {
        NumberUtils.validateArray(dArray);
        double d2 = dArray[0];
        for (int i2 = 1; i2 < dArray.length; ++i2) {
            if (Double.isNaN(dArray[i2])) {
                return Double.NaN;
            }
            if (!(dArray[i2] > d2)) continue;
            d2 = dArray[i2];
        }
        return d2;
    }

    public static float max(float ... fArray) {
        NumberUtils.validateArray(fArray);
        float f2 = fArray[0];
        for (int i2 = 1; i2 < fArray.length; ++i2) {
            if (Float.isNaN(fArray[i2])) {
                return Float.NaN;
            }
            if (!(fArray[i2] > f2)) continue;
            f2 = fArray[i2];
        }
        return f2;
    }

    private static void validateArray(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        Validate.isTrue(Array.getLength(object) != 0, "Array cannot be empty.", new Object[0]);
    }

    public static long min(long l, long l2, long l3) {
        if (l2 < l) {
            l = l2;
        }
        if (l3 < l) {
            l = l3;
        }
        return l;
    }

    public static int min(int n, int n2, int n3) {
        if (n2 < n) {
            n = n2;
        }
        if (n3 < n) {
            n = n3;
        }
        return n;
    }

    public static short min(short s, short s2, short s3) {
        if (s2 < s) {
            s = s2;
        }
        if (s3 < s) {
            s = s3;
        }
        return s;
    }

    public static byte min(byte by, byte by2, byte by3) {
        if (by2 < by) {
            by = by2;
        }
        if (by3 < by) {
            by = by3;
        }
        return by;
    }

    public static double min(double d2, double d3, double d4) {
        return Math.min(Math.min(d2, d3), d4);
    }

    public static float min(float f2, float f3, float f4) {
        return Math.min(Math.min(f2, f3), f4);
    }

    public static long max(long l, long l2, long l3) {
        if (l2 > l) {
            l = l2;
        }
        if (l3 > l) {
            l = l3;
        }
        return l;
    }

    public static int max(int n, int n2, int n3) {
        if (n2 > n) {
            n = n2;
        }
        if (n3 > n) {
            n = n3;
        }
        return n;
    }

    public static short max(short s, short s2, short s3) {
        if (s2 > s) {
            s = s2;
        }
        if (s3 > s) {
            s = s3;
        }
        return s;
    }

    public static byte max(byte by, byte by2, byte by3) {
        if (by2 > by) {
            by = by2;
        }
        if (by3 > by) {
            by = by3;
        }
        return by;
    }

    public static double max(double d2, double d3, double d4) {
        return Math.max(Math.max(d2, d3), d4);
    }

    public static float max(float f2, float f3, float f4) {
        return Math.max(Math.max(f2, f3), f4);
    }

    public static boolean isDigits(String string) {
        return StringUtils.isNumeric(string);
    }

    @Deprecated
    public static boolean isNumber(String string) {
        return NumberUtils.isCreatable(string);
    }

    public static boolean isCreatable(String object) {
        boolean bl;
        if (StringUtils.isEmpty((CharSequence)object)) {
            return false;
        }
        char[] cArray = ((String)object).toCharArray();
        object = cArray;
        int n = cArray.length;
        boolean bl2 = false;
        boolean bl3 = false;
        boolean bl4 = false;
        boolean bl5 = false;
        int n2 = object[0] == 45 || object[0] == 43 ? 1 : 0;
        boolean bl6 = bl = n2 == 1 && object[0] == 43;
        if (n > n2 + 1 && object[n2] == 48) {
            if (object[n2 + 1] == 120 || object[n2 + 1] == 88) {
                if ((n2 += 2) == n) {
                    return false;
                }
                while (n2 < ((Object)object).length) {
                    if (!(object[n2] >= 48 && object[n2] <= 57 || object[n2] >= 97 && object[n2] <= 102 || object[n2] >= 65 && object[n2] <= 70)) {
                        return false;
                    }
                    ++n2;
                }
                return true;
            }
            if (Character.isDigit((char)object[n2 + 1])) {
                ++n2;
                while (n2 < ((Object)object).length) {
                    if (object[n2] < 48 || object[n2] > 55) {
                        return false;
                    }
                    ++n2;
                }
                return true;
            }
        }
        --n;
        while (n2 < n || n2 < n + 1 && bl4 && !bl5) {
            if (object[n2] >= 48 && object[n2] <= 57) {
                bl5 = true;
                bl4 = false;
            } else if (object[n2] == 46) {
                if (bl3 || bl2) {
                    return false;
                }
                bl3 = true;
            } else if (object[n2] == 101 || object[n2] == 69) {
                if (bl2) {
                    return false;
                }
                if (!bl5) {
                    return false;
                }
                bl2 = true;
                bl4 = true;
            } else if (object[n2] == 43 || object[n2] == 45) {
                if (!bl4) {
                    return false;
                }
                bl4 = false;
                bl5 = false;
            } else {
                return false;
            }
            ++n2;
        }
        if (n2 < ((Object)object).length) {
            if (object[n2] >= 48 && object[n2] <= 57) {
                return !SystemUtils.IS_JAVA_1_6 || !bl || bl3;
            }
            if (object[n2] == 101 || object[n2] == 69) {
                return false;
            }
            if (object[n2] == 46) {
                if (bl3 || bl2) {
                    return false;
                }
                return bl5;
            }
            if (!(bl4 || object[n2] != 100 && object[n2] != 68 && object[n2] != 102 && object[n2] != 70)) {
                return bl5;
            }
            if (object[n2] == 108 || object[n2] == 76) {
                return bl5 && !bl2 && !bl3;
            }
            return false;
        }
        return !bl4 && bl5;
    }

    public static boolean isParsable(String string) {
        if (StringUtils.isEmpty(string)) {
            return false;
        }
        String string2 = string;
        if (string2.charAt(string2.length() - 1) == '.') {
            return false;
        }
        if (string.charAt(0) == '-') {
            if (string.length() == 1) {
                return false;
            }
            return NumberUtils.withDecimalsParsing(string, 1);
        }
        return NumberUtils.withDecimalsParsing(string, 0);
    }

    private static boolean withDecimalsParsing(String string, int n) {
        int n2 = 0;
        while (n < string.length()) {
            boolean bl = string.charAt(n) == '.';
            if (bl) {
                ++n2;
            }
            if (n2 > 1) {
                return false;
            }
            if (!bl && !Character.isDigit(string.charAt(n))) {
                return false;
            }
            ++n;
        }
        return true;
    }

    public static int compare(int n, int n2) {
        if (n == n2) {
            return 0;
        }
        if (n < n2) {
            return -1;
        }
        return 1;
    }

    public static int compare(long l, long l2) {
        if (l == l2) {
            return 0;
        }
        if (l < l2) {
            return -1;
        }
        return 1;
    }

    public static int compare(short s, short s2) {
        if (s == s2) {
            return 0;
        }
        if (s < s2) {
            return -1;
        }
        return 1;
    }

    public static int compare(byte by, byte by2) {
        return by - by2;
    }
}

