/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class DurationFormatUtils {
    public static final String ISO_EXTENDED_FORMAT_PATTERN = "'P'yyyy'Y'M'M'd'DT'H'H'm'M's.SSS'S'";
    static final Object y = "y";
    static final Object M = "M";
    static final Object d = "d";
    static final Object H = "H";
    static final Object m = "m";
    static final Object s = "s";
    static final Object S = "S";

    public static String formatDurationHMS(long l) {
        return DurationFormatUtils.formatDuration(l, "HH:mm:ss.SSS");
    }

    public static String formatDurationISO(long l) {
        return DurationFormatUtils.formatDuration(l, ISO_EXTENDED_FORMAT_PATTERN, false);
    }

    public static String formatDuration(long l, String string) {
        return DurationFormatUtils.formatDuration(l, string, true);
    }

    public static String formatDuration(long l, String tokenArray, boolean bl) {
        Validate.inclusiveBetween(0L, Long.MAX_VALUE, l, "durationMillis must not be negative");
        tokenArray = DurationFormatUtils.lexx((String)tokenArray);
        long l2 = 0L;
        long l3 = 0L;
        long l4 = 0L;
        long l5 = 0L;
        long l6 = l;
        if (Token.containsTokenWithValue(tokenArray, d)) {
            l2 = l / 86400000L;
            l6 = l - l2 * 86400000L;
        }
        if (Token.containsTokenWithValue(tokenArray, H)) {
            l3 = l6 / 3600000L;
            l6 -= l3 * 3600000L;
        }
        if (Token.containsTokenWithValue(tokenArray, m)) {
            l4 = l6 / 60000L;
            l6 -= l4 * 60000L;
        }
        if (Token.containsTokenWithValue(tokenArray, s)) {
            l5 = l6 / 1000L;
            l6 -= l5 * 1000L;
        }
        return DurationFormatUtils.format(tokenArray, 0L, 0L, l2, l3, l4, l5, l6, bl);
    }

    public static String formatDurationWords(long l, boolean bl, boolean bl2) {
        String string;
        String string2 = DurationFormatUtils.formatDuration(l, "d' days 'H' hours 'm' minutes 's' seconds'");
        if (bl) {
            string = StringUtils.replaceOnce(string2 = " ".concat(String.valueOf(string2)), " 0 days", "");
            if (string.length() != string2.length() && (string = StringUtils.replaceOnce(string2 = string, " 0 hours", "")).length() != string2.length()) {
                string2 = string = StringUtils.replaceOnce(string, " 0 minutes", "");
                if (string.length() != string2.length()) {
                    string2 = StringUtils.replaceOnce(string, " 0 seconds", "");
                }
            }
            if (string2.length() != 0) {
                string2 = string2.substring(1);
            }
        }
        if (bl2 && (string = StringUtils.replaceOnce(string2, " 0 seconds", "")).length() != string2.length() && (string = StringUtils.replaceOnce(string2 = string, " 0 minutes", "")).length() != string2.length() && (string = StringUtils.replaceOnce(string2 = string, " 0 hours", "")).length() != string2.length()) {
            string2 = StringUtils.replaceOnce(string, " 0 days", "");
        }
        return StringUtils.replaceOnce(StringUtils.replaceOnce(StringUtils.replaceOnce(StringUtils.replaceOnce(" ".concat(String.valueOf(string2)), " 1 seconds", " 1 second"), " 1 minutes", " 1 minute"), " 1 hours", " 1 hour"), " 1 days", " 1 day").trim();
    }

    public static String formatPeriodISO(long l, long l2) {
        return DurationFormatUtils.formatPeriod(l, l2, ISO_EXTENDED_FORMAT_PATTERN, false, TimeZone.getDefault());
    }

    public static String formatPeriod(long l, long l2, String string) {
        return DurationFormatUtils.formatPeriod(l, l2, string, true, TimeZone.getDefault());
    }

    public static String formatPeriod(long l, long l2, String tokenArray, boolean bl, TimeZone timeZone) {
        Validate.isTrue(l <= l2, "startMillis must not be greater than endMillis", new Object[0]);
        tokenArray = DurationFormatUtils.lexx((String)tokenArray);
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(new Date(l));
        Calendar calendar2 = Calendar.getInstance(timeZone);
        calendar2.setTime(new Date(l2));
        int n = calendar2.get(14) - calendar.get(14);
        int n2 = calendar2.get(13) - calendar.get(13);
        int n3 = calendar2.get(12) - calendar.get(12);
        int n4 = calendar2.get(11) - calendar.get(11);
        int n5 = calendar2.get(5) - calendar.get(5);
        int n6 = calendar2.get(2) - calendar.get(2);
        int n7 = calendar2.get(1) - calendar.get(1);
        while (n < 0) {
            n += 1000;
            --n2;
        }
        while (n2 < 0) {
            n2 += 60;
            --n3;
        }
        while (n3 < 0) {
            n3 += 60;
            --n4;
        }
        while (n4 < 0) {
            n4 += 24;
            --n5;
        }
        if (Token.containsTokenWithValue(tokenArray, M)) {
            while (n5 < 0) {
                n5 += calendar.getActualMaximum(5);
                --n6;
                calendar.add(2, 1);
            }
            while (n6 < 0) {
                n6 += 12;
                --n7;
            }
            if (!Token.containsTokenWithValue(tokenArray, y) && n7 != 0) {
                while (n7 != 0) {
                    n6 += 12 * n7;
                    n7 = 0;
                }
            }
        } else {
            if (!Token.containsTokenWithValue(tokenArray, y)) {
                n7 = calendar2.get(1);
                if (n6 < 0) {
                    --n7;
                }
                while (calendar.get(1) != n7) {
                    n5 += calendar.getActualMaximum(6) - calendar.get(6);
                    if (calendar instanceof GregorianCalendar && calendar.get(2) == 1 && calendar.get(5) == 29) {
                        ++n5;
                    }
                    calendar.add(1, 1);
                    n5 += calendar.get(6);
                }
                n7 = 0;
            }
            while (calendar.get(2) != calendar2.get(2)) {
                n5 += calendar.getActualMaximum(5);
                calendar.add(2, 1);
            }
            n6 = 0;
            while (n5 < 0) {
                n5 += calendar.getActualMaximum(5);
                --n6;
                calendar.add(2, 1);
            }
        }
        if (!Token.containsTokenWithValue(tokenArray, d)) {
            n4 += 24 * n5;
            n5 = 0;
        }
        if (!Token.containsTokenWithValue(tokenArray, H)) {
            n3 += 60 * n4;
            n4 = 0;
        }
        if (!Token.containsTokenWithValue(tokenArray, m)) {
            n2 += 60 * n3;
            n3 = 0;
        }
        if (!Token.containsTokenWithValue(tokenArray, s)) {
            n += 1000 * n2;
            n2 = 0;
        }
        return DurationFormatUtils.format(tokenArray, n7, n6, n5, n4, n3, n2, n, bl);
    }

    static String format(Token[] tokenArray, long l, long l2, long l3, long l4, long l5, long l6, long l7, boolean bl) {
        StringBuilder stringBuilder = new StringBuilder();
        int n = 0;
        for (Token token : tokenArray) {
            Object object = token.getValue();
            int n2 = token.getCount();
            if (object instanceof StringBuilder) {
                stringBuilder.append(object.toString());
                continue;
            }
            if (object.equals(y)) {
                stringBuilder.append(DurationFormatUtils.paddedValue(l, bl, n2));
            } else if (object.equals(M)) {
                stringBuilder.append(DurationFormatUtils.paddedValue(l2, bl, n2));
            } else if (object.equals(d)) {
                stringBuilder.append(DurationFormatUtils.paddedValue(l3, bl, n2));
            } else if (object.equals(H)) {
                stringBuilder.append(DurationFormatUtils.paddedValue(l4, bl, n2));
            } else if (object.equals(m)) {
                stringBuilder.append(DurationFormatUtils.paddedValue(l5, bl, n2));
            } else {
                if (object.equals(s)) {
                    stringBuilder.append(DurationFormatUtils.paddedValue(l6, bl, n2));
                    n = 1;
                    continue;
                }
                if (!object.equals(S)) continue;
                if (n != 0) {
                    n = bl ? Math.max(3, n2) : 3;
                    stringBuilder.append(DurationFormatUtils.paddedValue(l7, true, n));
                } else {
                    stringBuilder.append(DurationFormatUtils.paddedValue(l7, bl, n2));
                }
            }
            n = 0;
        }
        return stringBuilder.toString();
    }

    private static String paddedValue(long l, boolean bl, int n) {
        String string = Long.toString(l);
        if (bl) {
            return StringUtils.leftPad(string, n, '0');
        }
        return string;
    }

    static Token[] lexx(String string) {
        ArrayList<Object> arrayList = new ArrayList<Object>(string.length());
        boolean bl = false;
        Object object = null;
        Object object2 = null;
        for (int i2 = 0; i2 < string.length(); ++i2) {
            char c2 = string.charAt(i2);
            if (bl && c2 != '\'') {
                ((StringBuilder)object).append(c2);
                continue;
            }
            Object object3 = null;
            switch (c2) {
                case '\'': {
                    if (bl) {
                        object = null;
                        bl = false;
                        break;
                    }
                    object = new StringBuilder();
                    arrayList.add(new Token(object));
                    bl = true;
                    break;
                }
                case 'y': {
                    object3 = y;
                    break;
                }
                case 'M': {
                    object3 = M;
                    break;
                }
                case 'd': {
                    object3 = d;
                    break;
                }
                case 'H': {
                    object3 = H;
                    break;
                }
                case 'm': {
                    object3 = m;
                    break;
                }
                case 's': {
                    object3 = s;
                    break;
                }
                case 'S': {
                    object3 = S;
                    break;
                }
                default: {
                    if (object == null) {
                        object = new StringBuilder();
                        arrayList.add(new Token(object));
                    }
                    ((StringBuilder)object).append(c2);
                }
            }
            if (object3 == null) continue;
            if (object2 != null && ((Token)object2).getValue().equals(object3)) {
                ((Token)object2).increment();
            } else {
                object = new Token(object3);
                arrayList.add(object);
                object2 = object;
            }
            object = null;
        }
        if (bl) {
            throw new IllegalArgumentException("Unmatched quote in format: ".concat(String.valueOf(string)));
        }
        ArrayList<Object> arrayList2 = arrayList;
        return arrayList2.toArray(new Token[arrayList2.size()]);
    }

    static class Token {
        private final Object value;
        private int count;

        static boolean containsTokenWithValue(Token[] tokenArray, Object object) {
            int n = tokenArray.length;
            for (int i2 = 0; i2 < n; ++i2) {
                if (tokenArray[i2].getValue() != object) continue;
                return true;
            }
            return false;
        }

        Token(Object object) {
            this.value = object;
            this.count = 1;
        }

        Token(Object object, int n) {
            this.value = object;
            this.count = n;
        }

        void increment() {
            ++this.count;
        }

        int getCount() {
            return this.count;
        }

        Object getValue() {
            return this.value;
        }

        public boolean equals(Object object) {
            if (object instanceof Token) {
                object = (Token)object;
                if (this.value.getClass() != ((Token)object).value.getClass()) {
                    return false;
                }
                if (this.count != ((Token)object).count) {
                    return false;
                }
                if (this.value instanceof StringBuilder) {
                    return this.value.toString().equals(((Token)object).value.toString());
                }
                if (this.value instanceof Number) {
                    return this.value.equals(((Token)object).value);
                }
                return this.value == ((Token)object).value;
            }
            return false;
        }

        public int hashCode() {
            return this.value.hashCode();
        }

        public String toString() {
            return StringUtils.repeat(this.value.toString(), this.count);
        }
    }
}

