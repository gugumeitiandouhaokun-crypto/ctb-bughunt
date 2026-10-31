/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class BooleanUtils {
    public static Boolean negate(Boolean bl) {
        if (bl == null) {
            return null;
        }
        if (bl.booleanValue()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public static boolean isTrue(Boolean bl) {
        return Boolean.TRUE.equals(bl);
    }

    public static boolean isNotTrue(Boolean bl) {
        return !BooleanUtils.isTrue(bl);
    }

    public static boolean isFalse(Boolean bl) {
        return Boolean.FALSE.equals(bl);
    }

    public static boolean isNotFalse(Boolean bl) {
        return !BooleanUtils.isFalse(bl);
    }

    public static boolean toBoolean(Boolean bl) {
        return bl != null && bl != false;
    }

    public static boolean toBooleanDefaultIfNull(Boolean bl, boolean bl2) {
        if (bl == null) {
            return bl2;
        }
        return bl;
    }

    public static boolean toBoolean(int n) {
        return n != 0;
    }

    public static Boolean toBooleanObject(int n) {
        if (n == 0) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public static Boolean toBooleanObject(Integer n) {
        if (n == null) {
            return null;
        }
        if (n == 0) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public static boolean toBoolean(int n, int n2, int n3) {
        if (n == n2) {
            return true;
        }
        if (n == n3) {
            return false;
        }
        throw new IllegalArgumentException("The Integer did not match either specified value");
    }

    public static boolean toBoolean(Integer n, Integer n2, Integer n3) {
        if (n == null) {
            if (n2 == null) {
                return true;
            }
            if (n3 == null) {
                return false;
            }
        } else {
            if (n.equals(n2)) {
                return true;
            }
            if (n.equals(n3)) {
                return false;
            }
        }
        throw new IllegalArgumentException("The Integer did not match either specified value");
    }

    public static Boolean toBooleanObject(int n, int n2, int n3, int n4) {
        if (n == n2) {
            return Boolean.TRUE;
        }
        if (n == n3) {
            return Boolean.FALSE;
        }
        if (n == n4) {
            return null;
        }
        throw new IllegalArgumentException("The Integer did not match any specified value");
    }

    public static Boolean toBooleanObject(Integer n, Integer n2, Integer n3, Integer n4) {
        if (n == null) {
            if (n2 == null) {
                return Boolean.TRUE;
            }
            if (n3 == null) {
                return Boolean.FALSE;
            }
            if (n4 == null) {
                return null;
            }
        } else {
            if (n.equals(n2)) {
                return Boolean.TRUE;
            }
            if (n.equals(n3)) {
                return Boolean.FALSE;
            }
            if (n.equals(n4)) {
                return null;
            }
        }
        throw new IllegalArgumentException("The Integer did not match any specified value");
    }

    public static int toInteger(boolean bl) {
        if (bl) {
            return 1;
        }
        return 0;
    }

    public static Integer toIntegerObject(boolean bl) {
        if (bl) {
            return NumberUtils.INTEGER_ONE;
        }
        return NumberUtils.INTEGER_ZERO;
    }

    public static Integer toIntegerObject(Boolean bl) {
        if (bl == null) {
            return null;
        }
        if (bl.booleanValue()) {
            return NumberUtils.INTEGER_ONE;
        }
        return NumberUtils.INTEGER_ZERO;
    }

    public static int toInteger(boolean bl, int n, int n2) {
        if (bl) {
            return n;
        }
        return n2;
    }

    public static int toInteger(Boolean bl, int n, int n2, int n3) {
        if (bl == null) {
            return n3;
        }
        if (bl.booleanValue()) {
            return n;
        }
        return n2;
    }

    public static Integer toIntegerObject(boolean bl, Integer n, Integer n2) {
        if (bl) {
            return n;
        }
        return n2;
    }

    public static Integer toIntegerObject(Boolean bl, Integer n, Integer n2, Integer n3) {
        if (bl == null) {
            return n3;
        }
        if (bl.booleanValue()) {
            return n;
        }
        return n2;
    }

    public static Boolean toBooleanObject(String string) {
        if (string == "true") {
            return Boolean.TRUE;
        }
        if (string == null) {
            return null;
        }
        switch (string.length()) {
            case 1: {
                char c2 = string.charAt(0);
                if (c2 == 'y' || c2 == 'Y' || c2 == 't' || c2 == 'T') {
                    return Boolean.TRUE;
                }
                if (c2 != 'n' && c2 != 'N' && c2 != 'f' && c2 != 'F') break;
                return Boolean.FALSE;
            }
            case 2: {
                char c3 = string.charAt(0);
                char c4 = string.charAt(1);
                if (!(c3 != 'o' && c3 != 'O' || c4 != 'n' && c4 != 'N')) {
                    return Boolean.TRUE;
                }
                if (c3 != 'n' && c3 != 'N' || c4 != 'o' && c4 != 'O') break;
                return Boolean.FALSE;
            }
            case 3: {
                char c5 = string.charAt(0);
                char c6 = string.charAt(1);
                char c7 = string.charAt(2);
                if (!(c5 != 'y' && c5 != 'Y' || c6 != 'e' && c6 != 'E' || c7 != 's' && c7 != 'S')) {
                    return Boolean.TRUE;
                }
                if (c5 != 'o' && c5 != 'O' || c6 != 'f' && c6 != 'F' || c7 != 'f' && c7 != 'F') break;
                return Boolean.FALSE;
            }
            case 4: {
                char c8 = string.charAt(0);
                char c9 = string.charAt(1);
                char c10 = string.charAt(2);
                char c11 = string.charAt(3);
                if (c8 != 't' && c8 != 'T' || c9 != 'r' && c9 != 'R' || c10 != 'u' && c10 != 'U' || c11 != 'e' && c11 != 'E') break;
                return Boolean.TRUE;
            }
            case 5: {
                char c12 = string.charAt(0);
                char c13 = string.charAt(1);
                char c14 = string.charAt(2);
                char c15 = string.charAt(3);
                char c16 = string.charAt(4);
                if (c12 != 'f' && c12 != 'F' || c13 != 'a' && c13 != 'A' || c14 != 'l' && c14 != 'L' || c15 != 's' && c15 != 'S' || c16 != 'e' && c16 != 'E') break;
                return Boolean.FALSE;
            }
        }
        return null;
    }

    public static Boolean toBooleanObject(String string, String string2, String string3, String string4) {
        if (string == null) {
            if (string2 == null) {
                return Boolean.TRUE;
            }
            if (string3 == null) {
                return Boolean.FALSE;
            }
            if (string4 == null) {
                return null;
            }
        } else {
            if (string.equals(string2)) {
                return Boolean.TRUE;
            }
            if (string.equals(string3)) {
                return Boolean.FALSE;
            }
            if (string.equals(string4)) {
                return null;
            }
        }
        throw new IllegalArgumentException("The String did not match any specified value");
    }

    public static boolean toBoolean(String string) {
        return BooleanUtils.toBooleanObject(string) == Boolean.TRUE;
    }

    public static boolean toBoolean(String string, String string2, String string3) {
        if (string == string2) {
            return true;
        }
        if (string == string3) {
            return false;
        }
        if (string != null) {
            if (string.equals(string2)) {
                return true;
            }
            if (string.equals(string3)) {
                return false;
            }
        }
        throw new IllegalArgumentException("The String did not match either specified value");
    }

    public static String toStringTrueFalse(Boolean bl) {
        return BooleanUtils.toString(bl, "true", "false", null);
    }

    public static String toStringOnOff(Boolean bl) {
        return BooleanUtils.toString(bl, "on", "off", null);
    }

    public static String toStringYesNo(Boolean bl) {
        return BooleanUtils.toString(bl, "yes", "no", null);
    }

    public static String toString(Boolean bl, String string, String string2, String string3) {
        if (bl == null) {
            return string3;
        }
        if (bl.booleanValue()) {
            return string;
        }
        return string2;
    }

    public static String toStringTrueFalse(boolean bl) {
        return BooleanUtils.toString(bl, "true", "false");
    }

    public static String toStringOnOff(boolean bl) {
        return BooleanUtils.toString(bl, "on", "off");
    }

    public static String toStringYesNo(boolean bl) {
        return BooleanUtils.toString(bl, "yes", "no");
    }

    public static String toString(boolean bl, String string, String string2) {
        if (bl) {
            return string;
        }
        return string2;
    }

    public static boolean and(boolean ... blArray) {
        if (blArray == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        if (blArray.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        int n = blArray.length;
        for (int j = 0; j < n; ++j) {
            if (blArray[j]) continue;
            return false;
        }
        return true;
    }

    public static Boolean and(Boolean ... booleanArray) {
        if (booleanArray == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        if (booleanArray.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        try {
            if (BooleanUtils.and(ArrayUtils.toPrimitive(booleanArray))) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
        catch (NullPointerException nullPointerException) {
            throw new IllegalArgumentException("The array must not contain any null elements");
        }
    }

    public static boolean or(boolean ... blArray) {
        if (blArray == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        if (blArray.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        int n = blArray.length;
        for (int j = 0; j < n; ++j) {
            if (!blArray[j]) continue;
            return true;
        }
        return false;
    }

    public static Boolean or(Boolean ... booleanArray) {
        if (booleanArray == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        if (booleanArray.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        try {
            if (BooleanUtils.or(ArrayUtils.toPrimitive(booleanArray))) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
        catch (NullPointerException nullPointerException) {
            throw new IllegalArgumentException("The array must not contain any null elements");
        }
    }

    public static boolean xor(boolean ... blArray) {
        if (blArray == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        if (blArray.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        boolean bl = false;
        for (boolean bl2 : blArray) {
            bl ^= bl2;
        }
        return bl;
    }

    public static Boolean xor(Boolean ... booleanArray) {
        if (booleanArray == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        if (booleanArray.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        try {
            if (BooleanUtils.xor(ArrayUtils.toPrimitive(booleanArray))) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
        catch (NullPointerException nullPointerException) {
            throw new IllegalArgumentException("The array must not contain any null elements");
        }
    }

    public static int compare(boolean bl, boolean bl2) {
        if (bl == bl2) {
            return 0;
        }
        if (bl) {
            return 1;
        }
        return -1;
    }
}

