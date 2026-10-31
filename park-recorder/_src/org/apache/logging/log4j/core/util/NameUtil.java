/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.security.MessageDigest;
import org.apache.logging.log4j.util.Strings;

public final class NameUtil {
    private static final int MASK = 255;

    private NameUtil() {
    }

    public static String getSubName(String string) {
        if (Strings.isEmpty(string)) {
            return null;
        }
        int n = string.lastIndexOf(46);
        if (n > 0) {
            return string.substring(0, n);
        }
        return "";
    }

    public static String md5(String string) {
        try {
            Object object = MessageDigest.getInstance("MD5");
            ((MessageDigest)object).update(string.getBytes());
            object = ((MessageDigest)object).digest();
            StringBuilder stringBuilder = new StringBuilder();
            for (Object object2 : object) {
                String string2 = Integer.toHexString(0xFF & object2);
                if (string2.length() == 1) {
                    stringBuilder.append('0');
                }
                stringBuilder.append(string2);
            }
            return stringBuilder.toString();
        }
        catch (Exception exception) {
            return string;
        }
    }
}

