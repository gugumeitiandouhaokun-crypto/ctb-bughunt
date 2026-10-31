/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

public class CharSequenceUtils {
    private static final int NOT_FOUND = -1;

    public static CharSequence subSequence(CharSequence charSequence, int n) {
        if (charSequence == null) {
            return null;
        }
        return charSequence.subSequence(n, charSequence.length());
    }

    static int indexOf(CharSequence charSequence, int n, int n2) {
        if (charSequence instanceof String) {
            return ((String)charSequence).indexOf(n, n2);
        }
        int n3 = charSequence.length();
        if (n2 < 0) {
            n2 = 0;
        }
        while (n2 < n3) {
            if (charSequence.charAt(n2) == n) {
                return n2;
            }
            ++n2;
        }
        return -1;
    }

    static int indexOf(CharSequence charSequence, CharSequence charSequence2, int n) {
        return charSequence.toString().indexOf(charSequence2.toString(), n);
    }

    static int lastIndexOf(CharSequence charSequence, int n, int n2) {
        if (charSequence instanceof String) {
            return ((String)charSequence).lastIndexOf(n, n2);
        }
        int n3 = charSequence.length();
        if (n2 < 0) {
            return -1;
        }
        if (n2 >= n3) {
            n2 = n3 - 1;
        }
        while (n2 >= 0) {
            if (charSequence.charAt(n2) == n) {
                return n2;
            }
            --n2;
        }
        return -1;
    }

    static int lastIndexOf(CharSequence charSequence, CharSequence charSequence2, int n) {
        return charSequence.toString().lastIndexOf(charSequence2.toString(), n);
    }

    static char[] toCharArray(CharSequence charSequence) {
        if (charSequence instanceof String) {
            return ((String)charSequence).toCharArray();
        }
        int n = charSequence.length();
        char[] cArray = new char[charSequence.length()];
        for (int j = 0; j < n; ++j) {
            cArray[j] = charSequence.charAt(j);
        }
        return cArray;
    }

    static boolean regionMatches(CharSequence charSequence, boolean bl, int n, CharSequence charSequence2, int n2, int n3) {
        if (charSequence instanceof String && charSequence2 instanceof String) {
            return ((String)charSequence).regionMatches(bl, n, (String)charSequence2, n2, n3);
        }
        int n4 = n;
        int n5 = n2;
        int n6 = n3;
        int n7 = charSequence.length() - n;
        int n8 = charSequence2.length() - n2;
        if (n < 0 || n2 < 0 || n3 < 0) {
            return false;
        }
        if (n7 < n3 || n8 < n3) {
            return false;
        }
        while (n6-- > 0) {
            if ((n = (int)charSequence.charAt(n4++)) == (n2 = (int)charSequence2.charAt(n5++))) continue;
            if (!bl) {
                return false;
            }
            if (Character.toUpperCase((char)n) == Character.toUpperCase((char)n2) || Character.toLowerCase((char)n) == Character.toLowerCase((char)n2)) continue;
            return false;
        }
        return true;
    }
}

