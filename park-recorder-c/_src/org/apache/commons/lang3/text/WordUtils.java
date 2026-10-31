/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

public class WordUtils {
    public static String wrap(String string, int n) {
        return WordUtils.wrap(string, n, null, false);
    }

    public static String wrap(String string, int n, String string2, boolean bl) {
        return WordUtils.wrap(string, n, string2, bl, " ");
    }

    public static String wrap(String string, int n, String string2, boolean bl, String object) {
        if (string == null) {
            return null;
        }
        if (string2 == null) {
            string2 = SystemUtils.LINE_SEPARATOR;
        }
        if (n <= 0) {
            n = 1;
        }
        if (StringUtils.isBlank((CharSequence)object)) {
            object = " ";
        }
        object = Pattern.compile((String)object);
        int n2 = string.length();
        int n3 = 0;
        StringBuilder stringBuilder = new StringBuilder(n2 + 32);
        while (n3 < n2) {
            int n4 = -1;
            int n5 = n3;
            Matcher matcher = ((Pattern)object).matcher(string.substring(n5, Math.min(n5 + n + 1, n2)));
            if (matcher.find()) {
                if (matcher.start() == 0) {
                    n3 += matcher.end();
                    continue;
                }
                n4 = matcher.start();
            }
            if (n2 - n3 <= n) break;
            while (matcher.find()) {
                n4 = matcher.start() + n3;
            }
            if (n4 >= n3) {
                stringBuilder.append(string.substring(n3, n4));
                stringBuilder.append(string2);
                n3 = n4 + 1;
                continue;
            }
            if (bl) {
                stringBuilder.append(string.substring(n3, n + n3));
                stringBuilder.append(string2);
                n3 += n;
                continue;
            }
            matcher = ((Pattern)object).matcher(string.substring(n3 + n));
            if (matcher.find()) {
                n4 = matcher.start() + n3 + n;
            }
            if (n4 >= 0) {
                stringBuilder.append(string.substring(n3, n4));
                stringBuilder.append(string2);
                n3 = n4 + 1;
                continue;
            }
            stringBuilder.append(string.substring(n3));
            n3 = n2;
        }
        stringBuilder.append(string.substring(n3));
        return stringBuilder.toString();
    }

    public static String capitalize(String string) {
        return WordUtils.capitalize(string, null);
    }

    public static String capitalize(String object, char ... cArray) {
        int n;
        int n2 = n = cArray == null ? -1 : cArray.length;
        if (StringUtils.isEmpty((CharSequence)object) || n == 0) {
            return object;
        }
        object = ((String)object).toCharArray();
        n = 1;
        for (int i2 = 0; i2 < ((Object)object).length; ++i2) {
            Object object2 = object[i2];
            if (WordUtils.isDelimiter((char)object2, cArray)) {
                n = 1;
                continue;
            }
            if (n == 0) continue;
            object[i2] = Character.toTitleCase((char)object2);
            n = 0;
        }
        return new String((char[])object);
    }

    public static String capitalizeFully(String string) {
        return WordUtils.capitalizeFully(string, null);
    }

    public static String capitalizeFully(String string, char ... cArray) {
        int n;
        int n2 = n = cArray == null ? -1 : cArray.length;
        if (StringUtils.isEmpty(string) || n == 0) {
            return string;
        }
        return WordUtils.capitalize(string.toLowerCase(), cArray);
    }

    public static String uncapitalize(String string) {
        return WordUtils.uncapitalize(string, null);
    }

    public static String uncapitalize(String object, char ... cArray) {
        int n;
        int n2 = n = cArray == null ? -1 : cArray.length;
        if (StringUtils.isEmpty((CharSequence)object) || n == 0) {
            return object;
        }
        object = ((String)object).toCharArray();
        n = 1;
        for (int i2 = 0; i2 < ((Object)object).length; ++i2) {
            Object object2 = object[i2];
            if (WordUtils.isDelimiter((char)object2, cArray)) {
                n = 1;
                continue;
            }
            if (n == 0) continue;
            object[i2] = Character.toLowerCase((char)object2);
            n = 0;
        }
        return new String((char[])object);
    }

    public static String swapCase(String object) {
        if (StringUtils.isEmpty((CharSequence)object)) {
            return object;
        }
        object = ((String)object).toCharArray();
        boolean bl = true;
        for (int i2 = 0; i2 < ((Object)object).length; ++i2) {
            Object object2 = object[i2];
            if (Character.isUpperCase((char)object2)) {
                object[i2] = Character.toLowerCase((char)object2);
                bl = false;
                continue;
            }
            if (Character.isTitleCase((char)object2)) {
                object[i2] = Character.toLowerCase((char)object2);
                bl = false;
                continue;
            }
            if (Character.isLowerCase((char)object2)) {
                if (bl) {
                    object[i2] = Character.toTitleCase((char)object2);
                    bl = false;
                    continue;
                }
                object[i2] = Character.toUpperCase((char)object2);
                continue;
            }
            bl = Character.isWhitespace((char)object2);
        }
        return new String((char[])object);
    }

    public static String initials(String string) {
        return WordUtils.initials(string, null);
    }

    public static String initials(String string, char ... cArray) {
        if (StringUtils.isEmpty(string)) {
            return string;
        }
        if (cArray != null && cArray.length == 0) {
            return "";
        }
        int n = string.length();
        char[] cArray2 = new char[n / 2 + 1];
        int n2 = 0;
        boolean bl = true;
        for (int i2 = 0; i2 < n; ++i2) {
            char c2 = string.charAt(i2);
            if (WordUtils.isDelimiter(c2, cArray)) {
                bl = true;
                continue;
            }
            if (!bl) continue;
            cArray2[n2++] = c2;
            bl = false;
        }
        return new String(cArray2, 0, n2);
    }

    public static boolean containsAllWords(CharSequence charSequence, CharSequence ... charSequenceArray) {
        if (StringUtils.isEmpty(charSequence) || ArrayUtils.isEmpty(charSequenceArray)) {
            return false;
        }
        for (CharSequence charSequence2 : charSequenceArray) {
            if (StringUtils.isBlank(charSequence2)) {
                return false;
            }
            if (Pattern.compile(".*\\b" + charSequence2 + "\\b.*").matcher(charSequence).matches()) continue;
            return false;
        }
        return true;
    }

    private static boolean isDelimiter(char c2, char[] cArray) {
        if (cArray == null) {
            return Character.isWhitespace(c2);
        }
        for (char c3 : cArray) {
            if (c2 != c3) continue;
            return true;
        }
        return false;
    }
}

