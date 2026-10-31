/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import org.apache.commons.lang3.CharSet;
import org.apache.commons.lang3.StringUtils;

public class CharSetUtils {
    public static String squeeze(String object, String ... object2) {
        if (StringUtils.isEmpty((CharSequence)object) || CharSetUtils.deepEmpty((String[])object2)) {
            return object;
        }
        object2 = CharSet.getInstance((String[])object2);
        StringBuilder stringBuilder = new StringBuilder(((String)object).length());
        char[] cArray = ((String)object).toCharArray();
        object = cArray;
        int n = cArray.length;
        Object object3 = object[0];
        Character c2 = null;
        Character c3 = null;
        stringBuilder.append((char)object3);
        for (int i2 = 1; i2 < n; ++i2) {
            Object object4 = object[i2];
            if (object4 == object3) {
                if (c2 != null && object4 == c2.charValue()) continue;
                if (c3 == null || object4 != c3.charValue()) {
                    if (((CharSet)object2).contains((char)object4)) {
                        c2 = Character.valueOf((char)object4);
                        continue;
                    }
                    c3 = Character.valueOf((char)object4);
                }
            }
            stringBuilder.append((char)object4);
            object3 = object4;
        }
        return stringBuilder.toString();
    }

    public static boolean containsAny(String object, String ... object2) {
        if (StringUtils.isEmpty((CharSequence)object) || CharSetUtils.deepEmpty((String[])object2)) {
            return false;
        }
        object2 = CharSet.getInstance((String[])object2);
        char[] cArray = ((String)object).toCharArray();
        object = cArray;
        int n = cArray.length;
        for (int i2 = 0; i2 < n; ++i2) {
            Object object3 = object[i2];
            if (!((CharSet)object2).contains((char)object3)) continue;
            return true;
        }
        return false;
    }

    public static int count(String object, String ... object2) {
        if (StringUtils.isEmpty((CharSequence)object) || CharSetUtils.deepEmpty((String[])object2)) {
            return 0;
        }
        object2 = CharSet.getInstance((String[])object2);
        int n = 0;
        char[] cArray = ((String)object).toCharArray();
        object = cArray;
        int n2 = cArray.length;
        for (int i2 = 0; i2 < n2; ++i2) {
            Object object3 = object[i2];
            if (!((CharSet)object2).contains((char)object3)) continue;
            ++n;
        }
        return n;
    }

    public static String keep(String string, String ... stringArray) {
        if (string == null) {
            return null;
        }
        if (string.isEmpty() || CharSetUtils.deepEmpty(stringArray)) {
            return "";
        }
        return CharSetUtils.modify(string, stringArray, true);
    }

    public static String delete(String string, String ... stringArray) {
        if (StringUtils.isEmpty(string) || CharSetUtils.deepEmpty(stringArray)) {
            return string;
        }
        return CharSetUtils.modify(string, stringArray, false);
    }

    private static String modify(String object, String[] object2, boolean bl) {
        object2 = CharSet.getInstance((String[])object2);
        StringBuilder stringBuilder = new StringBuilder(((String)object).length());
        char[] cArray = ((String)object).toCharArray();
        object = cArray;
        int n = cArray.length;
        for (int i2 = 0; i2 < n; ++i2) {
            if (((CharSet)object2).contains((char)object[i2]) != bl) continue;
            stringBuilder.append((char)object[i2]);
        }
        return stringBuilder.toString();
    }

    private static boolean deepEmpty(String[] stringArray) {
        if (stringArray != null) {
            int n = stringArray.length;
            for (int i2 = 0; i2 < n; ++i2) {
                if (!StringUtils.isNotEmpty(stringArray[i2])) continue;
                return false;
            }
        }
        return true;
    }
}

