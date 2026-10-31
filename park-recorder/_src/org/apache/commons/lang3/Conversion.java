/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.util.UUID;

public class Conversion {
    private static final boolean[] TTTT = new boolean[]{true, true, true, true};
    private static final boolean[] FTTT = new boolean[]{false, true, true, true};
    private static final boolean[] TFTT = new boolean[]{true, false, true, true};
    private static final boolean[] FFTT = new boolean[]{false, false, true, true};
    private static final boolean[] TTFT = new boolean[]{true, true, false, true};
    private static final boolean[] FTFT = new boolean[]{false, true, false, true};
    private static final boolean[] TFFT = new boolean[]{true, false, false, true};
    private static final boolean[] FFFT = new boolean[]{false, false, false, true};
    private static final boolean[] TTTF = new boolean[]{true, true, true, false};
    private static final boolean[] FTTF = new boolean[]{false, true, true, false};
    private static final boolean[] TFTF = new boolean[]{true, false, true, false};
    private static final boolean[] FFTF = new boolean[]{false, false, true, false};
    private static final boolean[] TTFF = new boolean[]{true, true, false, false};
    private static final boolean[] FTFF = new boolean[]{false, true, false, false};
    private static final boolean[] TFFF = new boolean[]{true, false, false, false};
    private static final boolean[] FFFF = new boolean[]{false, false, false, false};

    public static int hexDigitToInt(char c2) {
        int n = Character.digit(c2, 16);
        if (n < 0) {
            throw new IllegalArgumentException("Cannot interpret '" + c2 + "' as a hexadecimal digit");
        }
        return n;
    }

    public static int hexDigitMsb0ToInt(char c2) {
        switch (c2) {
            case '0': {
                return 0;
            }
            case '1': {
                return 8;
            }
            case '2': {
                return 4;
            }
            case '3': {
                return 12;
            }
            case '4': {
                return 2;
            }
            case '5': {
                return 10;
            }
            case '6': {
                return 6;
            }
            case '7': {
                return 14;
            }
            case '8': {
                return 1;
            }
            case '9': {
                return 9;
            }
            case 'A': 
            case 'a': {
                return 5;
            }
            case 'B': 
            case 'b': {
                return 13;
            }
            case 'C': 
            case 'c': {
                return 3;
            }
            case 'D': 
            case 'd': {
                return 11;
            }
            case 'E': 
            case 'e': {
                return 7;
            }
            case 'F': 
            case 'f': {
                return 15;
            }
        }
        throw new IllegalArgumentException("Cannot interpret '" + c2 + "' as a hexadecimal digit");
    }

    public static boolean[] hexDigitToBinary(char c2) {
        switch (c2) {
            case '0': {
                return (boolean[])FFFF.clone();
            }
            case '1': {
                return (boolean[])TFFF.clone();
            }
            case '2': {
                return (boolean[])FTFF.clone();
            }
            case '3': {
                return (boolean[])TTFF.clone();
            }
            case '4': {
                return (boolean[])FFTF.clone();
            }
            case '5': {
                return (boolean[])TFTF.clone();
            }
            case '6': {
                return (boolean[])FTTF.clone();
            }
            case '7': {
                return (boolean[])TTTF.clone();
            }
            case '8': {
                return (boolean[])FFFT.clone();
            }
            case '9': {
                return (boolean[])TFFT.clone();
            }
            case 'A': 
            case 'a': {
                return (boolean[])FTFT.clone();
            }
            case 'B': 
            case 'b': {
                return (boolean[])TTFT.clone();
            }
            case 'C': 
            case 'c': {
                return (boolean[])FFTT.clone();
            }
            case 'D': 
            case 'd': {
                return (boolean[])TFTT.clone();
            }
            case 'E': 
            case 'e': {
                return (boolean[])FTTT.clone();
            }
            case 'F': 
            case 'f': {
                return (boolean[])TTTT.clone();
            }
        }
        throw new IllegalArgumentException("Cannot interpret '" + c2 + "' as a hexadecimal digit");
    }

    public static boolean[] hexDigitMsb0ToBinary(char c2) {
        switch (c2) {
            case '0': {
                return (boolean[])FFFF.clone();
            }
            case '1': {
                return (boolean[])FFFT.clone();
            }
            case '2': {
                return (boolean[])FFTF.clone();
            }
            case '3': {
                return (boolean[])FFTT.clone();
            }
            case '4': {
                return (boolean[])FTFF.clone();
            }
            case '5': {
                return (boolean[])FTFT.clone();
            }
            case '6': {
                return (boolean[])FTTF.clone();
            }
            case '7': {
                return (boolean[])FTTT.clone();
            }
            case '8': {
                return (boolean[])TFFF.clone();
            }
            case '9': {
                return (boolean[])TFFT.clone();
            }
            case 'A': 
            case 'a': {
                return (boolean[])TFTF.clone();
            }
            case 'B': 
            case 'b': {
                return (boolean[])TFTT.clone();
            }
            case 'C': 
            case 'c': {
                return (boolean[])TTFF.clone();
            }
            case 'D': 
            case 'd': {
                return (boolean[])TTFT.clone();
            }
            case 'E': 
            case 'e': {
                return (boolean[])TTTF.clone();
            }
            case 'F': 
            case 'f': {
                return (boolean[])TTTT.clone();
            }
        }
        throw new IllegalArgumentException("Cannot interpret '" + c2 + "' as a hexadecimal digit");
    }

    public static char binaryToHexDigit(boolean[] blArray) {
        return Conversion.binaryToHexDigit(blArray, 0);
    }

    public static char binaryToHexDigit(boolean[] blArray, int n) {
        if (blArray.length == 0) {
            throw new IllegalArgumentException("Cannot convert an empty array.");
        }
        if (blArray.length > n + 3 && blArray[n + 3]) {
            if (blArray.length > n + 2 && blArray[n + 2]) {
                if (blArray.length > n + 1 && blArray[n + 1]) {
                    if (blArray[n]) {
                        return 'f';
                    }
                    return 'e';
                }
                if (blArray[n]) {
                    return 'd';
                }
                return 'c';
            }
            if (blArray.length > n + 1 && blArray[n + 1]) {
                if (blArray[n]) {
                    return 'b';
                }
                return 'a';
            }
            if (blArray[n]) {
                return '9';
            }
            return '8';
        }
        if (blArray.length > n + 2 && blArray[n + 2]) {
            if (blArray.length > n + 1 && blArray[n + 1]) {
                if (blArray[n]) {
                    return '7';
                }
                return '6';
            }
            if (blArray[n]) {
                return '5';
            }
            return '4';
        }
        if (blArray.length > n + 1 && blArray[n + 1]) {
            if (blArray[n]) {
                return '3';
            }
            return '2';
        }
        if (blArray[n]) {
            return '1';
        }
        return '0';
    }

    public static char binaryToHexDigitMsb0_4bits(boolean[] blArray) {
        return Conversion.binaryToHexDigitMsb0_4bits(blArray, 0);
    }

    public static char binaryToHexDigitMsb0_4bits(boolean[] blArray, int n) {
        if (blArray.length > 8) {
            throw new IllegalArgumentException("src.length>8: src.length=" + blArray.length);
        }
        if (blArray.length - n < 4) {
            throw new IllegalArgumentException("src.length-srcPos<4: src.length=" + blArray.length + ", srcPos=" + n);
        }
        if (blArray[n + 3]) {
            if (blArray[n + 2]) {
                if (blArray[n + 1]) {
                    if (blArray[n]) {
                        return 'f';
                    }
                    return '7';
                }
                if (blArray[n]) {
                    return 'b';
                }
                return '3';
            }
            if (blArray[n + 1]) {
                if (blArray[n]) {
                    return 'd';
                }
                return '5';
            }
            if (blArray[n]) {
                return '9';
            }
            return '1';
        }
        if (blArray[n + 2]) {
            if (blArray[n + 1]) {
                if (blArray[n]) {
                    return 'e';
                }
                return '6';
            }
            if (blArray[n]) {
                return 'a';
            }
            return '2';
        }
        if (blArray[n + 1]) {
            if (blArray[n]) {
                return 'c';
            }
            return '4';
        }
        if (blArray[n]) {
            return '8';
        }
        return '0';
    }

    public static char binaryBeMsb0ToHexDigit(boolean[] blArray) {
        return Conversion.binaryBeMsb0ToHexDigit(blArray, 0);
    }

    public static char binaryBeMsb0ToHexDigit(boolean[] blArray, int n) {
        if (blArray.length == 0) {
            throw new IllegalArgumentException("Cannot convert an empty array.");
        }
        n = blArray.length - 1 - n;
        int n2 = Math.min(4, n + 1);
        boolean[] blArray2 = new boolean[4];
        System.arraycopy(blArray, n + 1 - n2, blArray2, 4 - n2, n2);
        blArray = blArray2;
        if (blArray2[0]) {
            if (blArray[1]) {
                if (blArray[2]) {
                    if (blArray[3]) {
                        return 'f';
                    }
                    return 'e';
                }
                if (blArray[3]) {
                    return 'd';
                }
                return 'c';
            }
            if (blArray[2]) {
                if (blArray[3]) {
                    return 'b';
                }
                return 'a';
            }
            if (blArray[3]) {
                return '9';
            }
            return '8';
        }
        if (blArray[1]) {
            if (blArray[2]) {
                if (blArray[3]) {
                    return '7';
                }
                return '6';
            }
            if (blArray[3]) {
                return '5';
            }
            return '4';
        }
        if (blArray[2]) {
            if (blArray[3]) {
                return '3';
            }
            return '2';
        }
        if (blArray[3]) {
            return '1';
        }
        return '0';
    }

    public static char intToHexDigit(int n) {
        char c2 = Character.forDigit(n, 16);
        if (c2 == '\u0000') {
            throw new IllegalArgumentException("nibble value not between 0 and 15: ".concat(String.valueOf(n)));
        }
        return c2;
    }

    public static char intToHexDigitMsb0(int n) {
        switch (n) {
            case 0: {
                return '0';
            }
            case 1: {
                return '8';
            }
            case 2: {
                return '4';
            }
            case 3: {
                return 'c';
            }
            case 4: {
                return '2';
            }
            case 5: {
                return 'a';
            }
            case 6: {
                return '6';
            }
            case 7: {
                return 'e';
            }
            case 8: {
                return '1';
            }
            case 9: {
                return '9';
            }
            case 10: {
                return '5';
            }
            case 11: {
                return 'd';
            }
            case 12: {
                return '3';
            }
            case 13: {
                return 'b';
            }
            case 14: {
                return '7';
            }
            case 15: {
                return 'f';
            }
        }
        throw new IllegalArgumentException("nibble value not between 0 and 15: ".concat(String.valueOf(n)));
    }

    public static long intArrayToLong(int[] nArray, int n, long l, int n2, int n3) {
        if (nArray.length == 0 && n == 0 || n3 == 0) {
            return l;
        }
        if ((n3 - 1) * 32 + n2 >= 64) {
            throw new IllegalArgumentException("(nInts-1)*32+dstPos is greather or equal to than 64");
        }
        long l2 = l;
        for (int j = 0; j < n3; ++j) {
            int n4 = j * 32 + n2;
            long l3 = (0xFFFFFFFFL & (long)nArray[j + n]) << n4;
            long l4 = 0xFFFFFFFFL << n4;
            l2 = l2 & (l4 ^ 0xFFFFFFFFFFFFFFFFL) | l3;
        }
        return l2;
    }

    public static long shortArrayToLong(short[] sArray, int n, long l, int n2, int n3) {
        if (sArray.length == 0 && n == 0 || n3 == 0) {
            return l;
        }
        if ((n3 - 1) * 16 + n2 >= 64) {
            throw new IllegalArgumentException("(nShorts-1)*16+dstPos is greather or equal to than 64");
        }
        long l2 = l;
        for (int j = 0; j < n3; ++j) {
            int n4 = j * 16 + n2;
            long l3 = (0xFFFFL & (long)sArray[j + n]) << n4;
            long l4 = 65535L << n4;
            l2 = l2 & (l4 ^ 0xFFFFFFFFFFFFFFFFL) | l3;
        }
        return l2;
    }

    public static int shortArrayToInt(short[] sArray, int n, int n2, int n3, int n4) {
        if (sArray.length == 0 && n == 0 || n4 == 0) {
            return n2;
        }
        if ((n4 - 1) * 16 + n3 >= 32) {
            throw new IllegalArgumentException("(nShorts-1)*16+dstPos is greather or equal to than 32");
        }
        for (int j = 0; j < n4; ++j) {
            int n5 = j * 16 + n3;
            int n6 = (0xFFFF & sArray[j + n]) << n5;
            n5 = 65535 << n5;
            n2 = n2 & ~n5 | n6;
        }
        return n2;
    }

    public static long byteArrayToLong(byte[] byArray, int n, long l, int n2, int n3) {
        if (byArray.length == 0 && n == 0 || n3 == 0) {
            return l;
        }
        if ((n3 - 1) * 8 + n2 >= 64) {
            throw new IllegalArgumentException("(nBytes-1)*8+dstPos is greather or equal to than 64");
        }
        long l2 = l;
        for (int j = 0; j < n3; ++j) {
            int n4 = j * 8 + n2;
            long l3 = (0xFFL & (long)byArray[j + n]) << n4;
            long l4 = 255L << n4;
            l2 = l2 & (l4 ^ 0xFFFFFFFFFFFFFFFFL) | l3;
        }
        return l2;
    }

    public static int byteArrayToInt(byte[] byArray, int n, int n2, int n3, int n4) {
        if (byArray.length == 0 && n == 0 || n4 == 0) {
            return n2;
        }
        if ((n4 - 1) * 8 + n3 >= 32) {
            throw new IllegalArgumentException("(nBytes-1)*8+dstPos is greather or equal to than 32");
        }
        for (int j = 0; j < n4; ++j) {
            int n5 = j * 8 + n3;
            int n6 = (0xFF & byArray[j + n]) << n5;
            n5 = 255 << n5;
            n2 = n2 & ~n5 | n6;
        }
        return n2;
    }

    public static short byteArrayToShort(byte[] byArray, int n, short s, int n2, int n3) {
        if (byArray.length == 0 && n == 0 || n3 == 0) {
            return s;
        }
        if ((n3 - 1) * 8 + n2 >= 16) {
            throw new IllegalArgumentException("(nBytes-1)*8+dstPos is greather or equal to than 16");
        }
        for (int j = 0; j < n3; ++j) {
            int n4 = j * 8 + n2;
            int n5 = (0xFF & byArray[j + n]) << n4;
            n4 = 255 << n4;
            s = (short)(s & ~n4 | n5);
        }
        return s;
    }

    public static long hexToLong(String string, int n, long l, int n2, int n3) {
        if (n3 == 0) {
            return l;
        }
        if ((n3 - 1) * 4 + n2 >= 64) {
            throw new IllegalArgumentException("(nHexs-1)*4+dstPos is greather or equal to than 64");
        }
        long l2 = l;
        for (int j = 0; j < n3; ++j) {
            int n4 = j * 4 + n2;
            long l3 = (0xFL & (long)Conversion.hexDigitToInt(string.charAt(j + n))) << n4;
            long l4 = 15L << n4;
            l2 = l2 & (l4 ^ 0xFFFFFFFFFFFFFFFFL) | l3;
        }
        return l2;
    }

    public static int hexToInt(String string, int n, int n2, int n3, int n4) {
        if (n4 == 0) {
            return n2;
        }
        if ((n4 - 1) * 4 + n3 >= 32) {
            throw new IllegalArgumentException("(nHexs-1)*4+dstPos is greather or equal to than 32");
        }
        for (int j = 0; j < n4; ++j) {
            int n5 = j * 4 + n3;
            int n6 = (0xF & Conversion.hexDigitToInt(string.charAt(j + n))) << n5;
            n5 = 15 << n5;
            n2 = n2 & ~n5 | n6;
        }
        return n2;
    }

    public static short hexToShort(String string, int n, short s, int n2, int n3) {
        if (n3 == 0) {
            return s;
        }
        if ((n3 - 1) * 4 + n2 >= 16) {
            throw new IllegalArgumentException("(nHexs-1)*4+dstPos is greather or equal to than 16");
        }
        for (int j = 0; j < n3; ++j) {
            int n4 = j * 4 + n2;
            int n5 = (0xF & Conversion.hexDigitToInt(string.charAt(j + n))) << n4;
            n4 = 15 << n4;
            s = (short)(s & ~n4 | n5);
        }
        return s;
    }

    public static byte hexToByte(String string, int n, byte by, int n2, int n3) {
        if (n3 == 0) {
            return by;
        }
        if ((n3 - 1) * 4 + n2 >= 8) {
            throw new IllegalArgumentException("(nHexs-1)*4+dstPos is greather or equal to than 8");
        }
        for (int j = 0; j < n3; ++j) {
            int n4 = j * 4 + n2;
            int n5 = (0xF & Conversion.hexDigitToInt(string.charAt(j + n))) << n4;
            n4 = 15 << n4;
            by = (byte)(by & ~n4 | n5);
        }
        return by;
    }

    public static long binaryToLong(boolean[] blArray, int n, long l, int n2, int n3) {
        if (blArray.length == 0 && n == 0 || n3 == 0) {
            return l;
        }
        if (n3 - 1 + n2 >= 64) {
            throw new IllegalArgumentException("nBools-1+dstPos is greather or equal to than 64");
        }
        long l2 = l;
        for (int j = 0; j < n3; ++j) {
            int n4 = j + n2;
            long l3 = (blArray[j + n] ? 1L : 0L) << n4;
            long l4 = 1L << n4;
            l2 = l2 & (l4 ^ 0xFFFFFFFFFFFFFFFFL) | l3;
        }
        return l2;
    }

    public static int binaryToInt(boolean[] blArray, int n, int n2, int n3, int n4) {
        if (blArray.length == 0 && n == 0 || n4 == 0) {
            return n2;
        }
        if (n4 - 1 + n3 >= 32) {
            throw new IllegalArgumentException("nBools-1+dstPos is greather or equal to than 32");
        }
        for (int j = 0; j < n4; ++j) {
            int n5 = j + n3;
            int n6 = (blArray[j + n] ? 1 : 0) << n5;
            n5 = 1 << n5;
            n2 = n2 & ~n5 | n6;
        }
        return n2;
    }

    public static short binaryToShort(boolean[] blArray, int n, short s, int n2, int n3) {
        if (blArray.length == 0 && n == 0 || n3 == 0) {
            return s;
        }
        if (n3 - 1 + n2 >= 16) {
            throw new IllegalArgumentException("nBools-1+dstPos is greather or equal to than 16");
        }
        for (int j = 0; j < n3; ++j) {
            int n4 = j + n2;
            int n5 = (blArray[j + n] ? 1 : 0) << n4;
            n4 = 1 << n4;
            s = (short)(s & ~n4 | n5);
        }
        return s;
    }

    public static byte binaryToByte(boolean[] blArray, int n, byte by, int n2, int n3) {
        if (blArray.length == 0 && n == 0 || n3 == 0) {
            return by;
        }
        if (n3 - 1 + n2 >= 8) {
            throw new IllegalArgumentException("nBools-1+dstPos is greather or equal to than 8");
        }
        for (int j = 0; j < n3; ++j) {
            int n4 = j + n2;
            int n5 = (blArray[j + n] ? 1 : 0) << n4;
            n4 = 1 << n4;
            by = (byte)(by & ~n4 | n5);
        }
        return by;
    }

    public static int[] longToIntArray(long l, int n, int[] nArray, int n2, int n3) {
        if (n3 == 0) {
            return nArray;
        }
        if ((n3 - 1) * 32 + n >= 64) {
            throw new IllegalArgumentException("(nInts-1)*32+srcPos is greather or equal to than 64");
        }
        for (int j = 0; j < n3; ++j) {
            int n4 = j * 32 + n;
            nArray[n2 + j] = (int)(0xFFFFFFFFFFFFFFFFL & l >> n4);
        }
        return nArray;
    }

    public static short[] longToShortArray(long l, int n, short[] sArray, int n2, int n3) {
        if (n3 == 0) {
            return sArray;
        }
        if ((n3 - 1) * 16 + n >= 64) {
            throw new IllegalArgumentException("(nShorts-1)*16+srcPos is greather or equal to than 64");
        }
        for (int j = 0; j < n3; ++j) {
            int n4 = j * 16 + n;
            sArray[n2 + j] = (short)(0xFFFFL & l >> n4);
        }
        return sArray;
    }

    public static short[] intToShortArray(int n, int n2, short[] sArray, int n3, int n4) {
        if (n4 == 0) {
            return sArray;
        }
        if ((n4 - 1) * 16 + n2 >= 32) {
            throw new IllegalArgumentException("(nShorts-1)*16+srcPos is greather or equal to than 32");
        }
        for (int j = 0; j < n4; ++j) {
            int n5 = j * 16 + n2;
            sArray[n3 + j] = (short)(0xFFFF & n >> n5);
        }
        return sArray;
    }

    public static byte[] longToByteArray(long l, int n, byte[] byArray, int n2, int n3) {
        if (n3 == 0) {
            return byArray;
        }
        if ((n3 - 1) * 8 + n >= 64) {
            throw new IllegalArgumentException("(nBytes-1)*8+srcPos is greather or equal to than 64");
        }
        for (int j = 0; j < n3; ++j) {
            int n4 = j * 8 + n;
            byArray[n2 + j] = (byte)(0xFFL & l >> n4);
        }
        return byArray;
    }

    public static byte[] intToByteArray(int n, int n2, byte[] byArray, int n3, int n4) {
        if (n4 == 0) {
            return byArray;
        }
        if ((n4 - 1) * 8 + n2 >= 32) {
            throw new IllegalArgumentException("(nBytes-1)*8+srcPos is greather or equal to than 32");
        }
        for (int j = 0; j < n4; ++j) {
            int n5 = j * 8 + n2;
            byArray[n3 + j] = (byte)(0xFF & n >> n5);
        }
        return byArray;
    }

    public static byte[] shortToByteArray(short s, int n, byte[] byArray, int n2, int n3) {
        if (n3 == 0) {
            return byArray;
        }
        if ((n3 - 1) * 8 + n >= 16) {
            throw new IllegalArgumentException("(nBytes-1)*8+srcPos is greather or equal to than 16");
        }
        for (int j = 0; j < n3; ++j) {
            int n4 = j * 8 + n;
            byArray[n2 + j] = (byte)(0xFF & s >> n4);
        }
        return byArray;
    }

    public static String longToHex(long l, int n, String charSequence, int n2, int n3) {
        if (n3 == 0) {
            return charSequence;
        }
        if ((n3 - 1) * 4 + n >= 64) {
            throw new IllegalArgumentException("(nHexs-1)*4+srcPos is greather or equal to than 64");
        }
        charSequence = new StringBuilder((String)charSequence);
        int n4 = ((StringBuilder)charSequence).length();
        for (int j = 0; j < n3; ++j) {
            int n5 = j * 4 + n;
            n5 = (int)(0xFL & l >> n5);
            if (n2 + j == n4) {
                ++n4;
                ((StringBuilder)charSequence).append(Conversion.intToHexDigit(n5));
                continue;
            }
            ((StringBuilder)charSequence).setCharAt(n2 + j, Conversion.intToHexDigit(n5));
        }
        return ((StringBuilder)charSequence).toString();
    }

    public static String intToHex(int n, int n2, String charSequence, int n3, int n4) {
        if (n4 == 0) {
            return charSequence;
        }
        if ((n4 - 1) * 4 + n2 >= 32) {
            throw new IllegalArgumentException("(nHexs-1)*4+srcPos is greather or equal to than 32");
        }
        charSequence = new StringBuilder((String)charSequence);
        int n5 = ((StringBuilder)charSequence).length();
        for (int j = 0; j < n4; ++j) {
            int n6 = j * 4 + n2;
            n6 = 0xF & n >> n6;
            if (n3 + j == n5) {
                ++n5;
                ((StringBuilder)charSequence).append(Conversion.intToHexDigit(n6));
                continue;
            }
            ((StringBuilder)charSequence).setCharAt(n3 + j, Conversion.intToHexDigit(n6));
        }
        return ((StringBuilder)charSequence).toString();
    }

    public static String shortToHex(short s, int n, String charSequence, int n2, int n3) {
        if (n3 == 0) {
            return charSequence;
        }
        if ((n3 - 1) * 4 + n >= 16) {
            throw new IllegalArgumentException("(nHexs-1)*4+srcPos is greather or equal to than 16");
        }
        charSequence = new StringBuilder((String)charSequence);
        int n4 = ((StringBuilder)charSequence).length();
        for (int j = 0; j < n3; ++j) {
            int n5 = j * 4 + n;
            n5 = 0xF & s >> n5;
            if (n2 + j == n4) {
                ++n4;
                ((StringBuilder)charSequence).append(Conversion.intToHexDigit(n5));
                continue;
            }
            ((StringBuilder)charSequence).setCharAt(n2 + j, Conversion.intToHexDigit(n5));
        }
        return ((StringBuilder)charSequence).toString();
    }

    public static String byteToHex(byte by, int n, String charSequence, int n2, int n3) {
        if (n3 == 0) {
            return charSequence;
        }
        if ((n3 - 1) * 4 + n >= 8) {
            throw new IllegalArgumentException("(nHexs-1)*4+srcPos is greather or equal to than 8");
        }
        charSequence = new StringBuilder((String)charSequence);
        int n4 = ((StringBuilder)charSequence).length();
        for (int j = 0; j < n3; ++j) {
            int n5 = j * 4 + n;
            n5 = 0xF & by >> n5;
            if (n2 + j == n4) {
                ++n4;
                ((StringBuilder)charSequence).append(Conversion.intToHexDigit(n5));
                continue;
            }
            ((StringBuilder)charSequence).setCharAt(n2 + j, Conversion.intToHexDigit(n5));
        }
        return ((StringBuilder)charSequence).toString();
    }

    public static boolean[] longToBinary(long l, int n, boolean[] blArray, int n2, int n3) {
        if (n3 == 0) {
            return blArray;
        }
        if (n3 - 1 + n >= 64) {
            throw new IllegalArgumentException("nBools-1+srcPos is greather or equal to than 64");
        }
        for (int j = 0; j < n3; ++j) {
            int n4 = j + n;
            blArray[n2 + j] = (1L & l >> n4) != 0L;
        }
        return blArray;
    }

    public static boolean[] intToBinary(int n, int n2, boolean[] blArray, int n3, int n4) {
        if (n4 == 0) {
            return blArray;
        }
        if (n4 - 1 + n2 >= 32) {
            throw new IllegalArgumentException("nBools-1+srcPos is greather or equal to than 32");
        }
        for (int j = 0; j < n4; ++j) {
            int n5 = j + n2;
            blArray[n3 + j] = (1 & n >> n5) != 0;
        }
        return blArray;
    }

    public static boolean[] shortToBinary(short s, int n, boolean[] blArray, int n2, int n3) {
        if (n3 == 0) {
            return blArray;
        }
        if (n3 - 1 + n >= 16) {
            throw new IllegalArgumentException("nBools-1+srcPos is greather or equal to than 16");
        }
        assert (n3 - 1 < 16 - n);
        for (int j = 0; j < n3; ++j) {
            int n4 = j + n;
            blArray[n2 + j] = (1 & s >> n4) != 0;
        }
        return blArray;
    }

    public static boolean[] byteToBinary(byte by, int n, boolean[] blArray, int n2, int n3) {
        if (n3 == 0) {
            return blArray;
        }
        if (n3 - 1 + n >= 8) {
            throw new IllegalArgumentException("nBools-1+srcPos is greather or equal to than 8");
        }
        for (int j = 0; j < n3; ++j) {
            int n4 = j + n;
            blArray[n2 + j] = (1 & by >> n4) != 0;
        }
        return blArray;
    }

    public static byte[] uuidToByteArray(UUID uUID, byte[] byArray, int n, int n2) {
        if (n2 == 0) {
            return byArray;
        }
        if (n2 > 16) {
            throw new IllegalArgumentException("nBytes is greather than 16");
        }
        Conversion.longToByteArray(uUID.getMostSignificantBits(), 0, byArray, n, n2 > 8 ? 8 : n2);
        if (n2 >= 8) {
            Conversion.longToByteArray(uUID.getLeastSignificantBits(), 0, byArray, n + 8, n2 - 8);
        }
        return byArray;
    }

    public static UUID byteArrayToUuid(byte[] byArray, int n) {
        if (byArray.length - n < 16) {
            throw new IllegalArgumentException("Need at least 16 bytes for UUID");
        }
        return new UUID(Conversion.byteArrayToLong(byArray, n, 0L, 0, 8), Conversion.byteArrayToLong(byArray, n + 8, 0L, 0, 8));
    }
}

