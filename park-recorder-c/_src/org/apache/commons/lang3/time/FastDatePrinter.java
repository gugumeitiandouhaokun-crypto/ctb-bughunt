/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.time;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.CalendarReflection;
import org.apache.commons.lang3.time.DatePrinter;

public class FastDatePrinter
implements Serializable,
DatePrinter {
    private static final long serialVersionUID = 1L;
    public static final int FULL = 0;
    public static final int LONG = 1;
    public static final int MEDIUM = 2;
    public static final int SHORT = 3;
    private final String mPattern;
    private final TimeZone mTimeZone;
    private final Locale mLocale;
    private transient Rule[] mRules;
    private transient int mMaxLengthEstimate;
    private static final int MAX_DIGITS = 10;
    private static final ConcurrentMap<TimeZoneDisplayKey, String> cTimeZoneDisplayCache = new ConcurrentHashMap<TimeZoneDisplayKey, String>(7);

    protected FastDatePrinter(String string, TimeZone timeZone, Locale locale) {
        this.mPattern = string;
        this.mTimeZone = timeZone;
        this.mLocale = locale;
        this.init();
    }

    private void init() {
        List<Rule> list;
        List<Rule> list2 = list = this.parsePattern();
        this.mRules = list2.toArray(new Rule[list2.size()]);
        int n = 0;
        int n2 = this.mRules.length;
        while (--n2 >= 0) {
            n += this.mRules[n2].estimateLength();
        }
        this.mMaxLengthEstimate = n;
    }

    protected List<Rule> parsePattern() {
        String[] stringArray = new DateFormatSymbols(this.mLocale);
        ArrayList<Rule> arrayList = new ArrayList<Rule>();
        String[] stringArray2 = stringArray.getEras();
        String[] stringArray3 = stringArray.getMonths();
        String[] stringArray4 = stringArray.getShortMonths();
        String[] stringArray5 = stringArray.getWeekdays();
        String[] stringArray6 = stringArray.getShortWeekdays();
        stringArray = stringArray.getAmPmStrings();
        int n = this.mPattern.length();
        int[] nArray = new int[1];
        for (int i2 = 0; i2 < n; ++i2) {
            nArray[0] = i2;
            FastDatePrinter fastDatePrinter = this;
            Object object = fastDatePrinter.parseToken(fastDatePrinter.mPattern, nArray);
            i2 = nArray[0];
            int n2 = ((String)object).length();
            if (n2 == 0) break;
            char c2 = ((String)object).charAt(0);
            switch (c2) {
                case 'G': {
                    object = new TextField(0, stringArray2);
                    break;
                }
                case 'Y': 
                case 'y': {
                    object = n2 == 2 ? TwoDigitYearField.INSTANCE : this.selectNumberRule(1, n2 < 4 ? 4 : n2);
                    if (c2 != 'Y') break;
                    object = new WeekYear((NumberRule)object);
                    break;
                }
                case 'M': {
                    if (n2 >= 4) {
                        object = new TextField(2, stringArray3);
                        break;
                    }
                    if (n2 == 3) {
                        object = new TextField(2, stringArray4);
                        break;
                    }
                    if (n2 == 2) {
                        object = TwoDigitMonthField.INSTANCE;
                        break;
                    }
                    object = UnpaddedMonthField.INSTANCE;
                    break;
                }
                case 'd': {
                    object = this.selectNumberRule(5, n2);
                    break;
                }
                case 'h': {
                    object = new TwelveHourField(this.selectNumberRule(10, n2));
                    break;
                }
                case 'H': {
                    object = this.selectNumberRule(11, n2);
                    break;
                }
                case 'm': {
                    object = this.selectNumberRule(12, n2);
                    break;
                }
                case 's': {
                    object = this.selectNumberRule(13, n2);
                    break;
                }
                case 'S': {
                    object = this.selectNumberRule(14, n2);
                    break;
                }
                case 'E': {
                    object = new TextField(7, n2 < 4 ? stringArray6 : stringArray5);
                    break;
                }
                case 'u': {
                    object = new DayInWeekField(this.selectNumberRule(7, n2));
                    break;
                }
                case 'D': {
                    object = this.selectNumberRule(6, n2);
                    break;
                }
                case 'F': {
                    object = this.selectNumberRule(8, n2);
                    break;
                }
                case 'w': {
                    object = this.selectNumberRule(3, n2);
                    break;
                }
                case 'W': {
                    object = this.selectNumberRule(4, n2);
                    break;
                }
                case 'a': {
                    object = new TextField(9, stringArray);
                    break;
                }
                case 'k': {
                    object = new TwentyFourHourField(this.selectNumberRule(11, n2));
                    break;
                }
                case 'K': {
                    object = this.selectNumberRule(10, n2);
                    break;
                }
                case 'X': {
                    object = Iso8601_Rule.getRule(n2);
                    break;
                }
                case 'z': {
                    if (n2 >= 4) {
                        object = new TimeZoneNameRule(this.mTimeZone, this.mLocale, 1);
                        break;
                    }
                    object = new TimeZoneNameRule(this.mTimeZone, this.mLocale, 0);
                    break;
                }
                case 'Z': {
                    if (n2 == 1) {
                        object = TimeZoneNumberRule.INSTANCE_NO_COLON;
                        break;
                    }
                    if (n2 == 2) {
                        object = Iso8601_Rule.ISO8601_HOURS_COLON_MINUTES;
                        break;
                    }
                    object = TimeZoneNumberRule.INSTANCE_COLON;
                    break;
                }
                case '\'': {
                    object = ((String)object).substring(1);
                    if (((String)object).length() == 1) {
                        object = new CharacterLiteral(((String)object).charAt(0));
                        break;
                    }
                    object = new StringLiteral((String)object);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Illegal pattern component: ".concat(String.valueOf(object)));
                }
            }
            arrayList.add((Rule)object);
        }
        return arrayList;
    }

    protected String parseToken(String string, int[] nArray) {
        int n;
        StringBuilder stringBuilder = new StringBuilder();
        int n2 = string.length();
        char c2 = string.charAt(n);
        if (c2 >= 'A' && c2 <= 'Z' || c2 >= 'a' && c2 <= 'z') {
            stringBuilder.append(c2);
            while (n + 1 < n2 && string.charAt(n + 1) == c2) {
                stringBuilder.append(c2);
                ++n;
            }
        } else {
            stringBuilder.append('\'');
            boolean bl = false;
            for (n = nArray[0]; n < n2; ++n) {
                c2 = string.charAt(n);
                if (c2 == '\'') {
                    if (n + 1 < n2 && string.charAt(n + 1) == '\'') {
                        ++n;
                        stringBuilder.append(c2);
                        continue;
                    }
                    bl = !bl;
                    continue;
                }
                if (bl || (c2 < 'A' || c2 > 'Z') && (c2 < 'a' || c2 > 'z')) {
                    stringBuilder.append(c2);
                    continue;
                }
                break;
            }
        }
        nArray[0] = --n;
        return stringBuilder.toString();
    }

    protected NumberRule selectNumberRule(int n, int n2) {
        switch (n2) {
            case 1: {
                return new UnpaddedNumberField(n);
            }
            case 2: {
                return new TwoDigitNumberField(n);
            }
        }
        return new PaddedNumberField(n, n2);
    }

    @Override
    @Deprecated
    public StringBuffer format(Object object, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        if (object instanceof Date) {
            return this.format((Date)object, stringBuffer);
        }
        if (object instanceof Calendar) {
            return this.format((Calendar)object, stringBuffer);
        }
        if (object instanceof Long) {
            return this.format((long)((Long)object), stringBuffer);
        }
        throw new IllegalArgumentException("Unknown class: " + (object == null ? "<null>" : object.getClass().getName()));
    }

    String format(Object object) {
        if (object instanceof Date) {
            return this.format((Date)object);
        }
        if (object instanceof Calendar) {
            return this.format((Calendar)object);
        }
        if (object instanceof Long) {
            return this.format((Long)object);
        }
        throw new IllegalArgumentException("Unknown class: " + (object == null ? "<null>" : object.getClass().getName()));
    }

    @Override
    public String format(long l) {
        Calendar calendar = this.newCalendar();
        calendar.setTimeInMillis(l);
        return this.applyRulesToString(calendar);
    }

    private String applyRulesToString(Calendar calendar) {
        return this.applyRules(calendar, new StringBuilder(this.mMaxLengthEstimate)).toString();
    }

    private Calendar newCalendar() {
        return Calendar.getInstance(this.mTimeZone, this.mLocale);
    }

    @Override
    public String format(Date date) {
        Calendar calendar = this.newCalendar();
        calendar.setTime(date);
        return this.applyRulesToString(calendar);
    }

    @Override
    public String format(Calendar calendar) {
        return this.format(calendar, new StringBuilder(this.mMaxLengthEstimate)).toString();
    }

    @Override
    public StringBuffer format(long l, StringBuffer stringBuffer) {
        Calendar calendar = this.newCalendar();
        calendar.setTimeInMillis(l);
        return this.applyRules(calendar, (Appendable)stringBuffer);
    }

    @Override
    public StringBuffer format(Date date, StringBuffer stringBuffer) {
        Calendar calendar = this.newCalendar();
        calendar.setTime(date);
        return this.applyRules(calendar, (Appendable)stringBuffer);
    }

    @Override
    public StringBuffer format(Calendar calendar, StringBuffer stringBuffer) {
        return this.format(calendar.getTime(), stringBuffer);
    }

    @Override
    public <B extends Appendable> B format(long l, B b2) {
        Calendar calendar = this.newCalendar();
        calendar.setTimeInMillis(l);
        return this.applyRules(calendar, b2);
    }

    @Override
    public <B extends Appendable> B format(Date date, B b2) {
        Calendar calendar = this.newCalendar();
        calendar.setTime(date);
        return this.applyRules(calendar, b2);
    }

    @Override
    public <B extends Appendable> B format(Calendar calendar, B b2) {
        if (!calendar.getTimeZone().equals(this.mTimeZone)) {
            calendar = (Calendar)calendar.clone();
            calendar.setTimeZone(this.mTimeZone);
        }
        return this.applyRules(calendar, b2);
    }

    @Deprecated
    protected StringBuffer applyRules(Calendar calendar, StringBuffer stringBuffer) {
        return this.applyRules(calendar, (Appendable)stringBuffer);
    }

    private <B extends Appendable> B applyRules(Calendar calendar, B b2) {
        try {
            Rule[] ruleArray = this.mRules;
            int n = this.mRules.length;
            for (int i2 = 0; i2 < n; ++i2) {
                ruleArray[i2].appendTo(b2, calendar);
            }
        }
        catch (IOException iOException) {
            ExceptionUtils.rethrow(iOException);
        }
        return b2;
    }

    @Override
    public String getPattern() {
        return this.mPattern;
    }

    @Override
    public TimeZone getTimeZone() {
        return this.mTimeZone;
    }

    @Override
    public Locale getLocale() {
        return this.mLocale;
    }

    public int getMaxLengthEstimate() {
        return this.mMaxLengthEstimate;
    }

    public boolean equals(Object object) {
        if (!(object instanceof FastDatePrinter)) {
            return false;
        }
        object = (FastDatePrinter)object;
        return this.mPattern.equals(((FastDatePrinter)object).mPattern) && this.mTimeZone.equals(((FastDatePrinter)object).mTimeZone) && this.mLocale.equals(((FastDatePrinter)object).mLocale);
    }

    public int hashCode() {
        return this.mPattern.hashCode() + 13 * (this.mTimeZone.hashCode() + 13 * this.mLocale.hashCode());
    }

    public String toString() {
        return "FastDatePrinter[" + this.mPattern + "," + this.mLocale + "," + this.mTimeZone.getID() + "]";
    }

    private void readObject(ObjectInputStream objectInputStream) {
        objectInputStream.defaultReadObject();
        this.init();
    }

    private static void appendDigits(Appendable appendable, int n) {
        appendable.append((char)(n / 10 + 48));
        appendable.append((char)(n % 10 + 48));
    }

    private static void appendFullDigits(Appendable appendable, int n, int n2) {
        if (n < 10000) {
            int n3 = 4;
            if (n < 1000) {
                --n3;
                if (n < 100) {
                    --n3;
                    if (n < 10) {
                        --n3;
                    }
                }
            }
            for (int i2 = n2 - n3; i2 > 0; --i2) {
                appendable.append('0');
            }
            switch (n3) {
                case 4: {
                    appendable.append((char)(n / 1000 + 48));
                    n %= 1000;
                }
                case 3: {
                    if (n >= 100) {
                        appendable.append((char)(n / 100 + 48));
                        n %= 100;
                    } else {
                        appendable.append('0');
                    }
                }
                case 2: {
                    if (n >= 10) {
                        appendable.append((char)(n / 10 + 48));
                        n %= 10;
                    } else {
                        appendable.append('0');
                    }
                }
                case 1: {
                    appendable.append((char)(n + 48));
                }
            }
            return;
        }
        char[] cArray = new char[10];
        int n4 = 0;
        while (n != 0) {
            cArray[n4++] = (char)(n % 10 + 48);
            n /= 10;
        }
        while (n4 < n2) {
            appendable.append('0');
            --n2;
        }
        while (--n4 >= 0) {
            appendable.append(cArray[n4]);
        }
    }

    static String getTimeZoneDisplay(TimeZone object, boolean bl, int n, Locale locale) {
        TimeZoneDisplayKey timeZoneDisplayKey = new TimeZoneDisplayKey((TimeZone)object, bl, n, locale);
        Object object2 = (String)cTimeZoneDisplayCache.get(timeZoneDisplayKey);
        if (object2 == null && (object = cTimeZoneDisplayCache.putIfAbsent(timeZoneDisplayKey, (String)(object2 = ((TimeZone)object).getDisplayName(bl, n, locale)))) != null) {
            object2 = object;
        }
        return object2;
    }

    static class TimeZoneDisplayKey {
        private final TimeZone mTimeZone;
        private final int mStyle;
        private final Locale mLocale;

        TimeZoneDisplayKey(TimeZone timeZone, boolean bl, int n, Locale locale) {
            this.mTimeZone = timeZone;
            this.mStyle = bl ? n | Integer.MIN_VALUE : n;
            this.mLocale = locale;
        }

        public int hashCode() {
            return (this.mStyle * 31 + this.mLocale.hashCode()) * 31 + this.mTimeZone.hashCode();
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof TimeZoneDisplayKey) {
                object = (TimeZoneDisplayKey)object;
                return this.mTimeZone.equals(((TimeZoneDisplayKey)object).mTimeZone) && this.mStyle == ((TimeZoneDisplayKey)object).mStyle && this.mLocale.equals(((TimeZoneDisplayKey)object).mLocale);
            }
            return false;
        }
    }

    static class Iso8601_Rule
    implements Rule {
        static final Iso8601_Rule ISO8601_HOURS = new Iso8601_Rule(3);
        static final Iso8601_Rule ISO8601_HOURS_MINUTES = new Iso8601_Rule(5);
        static final Iso8601_Rule ISO8601_HOURS_COLON_MINUTES = new Iso8601_Rule(6);
        final int length;

        static Iso8601_Rule getRule(int n) {
            switch (n) {
                case 1: {
                    return ISO8601_HOURS;
                }
                case 2: {
                    return ISO8601_HOURS_MINUTES;
                }
                case 3: {
                    return ISO8601_HOURS_COLON_MINUTES;
                }
            }
            throw new IllegalArgumentException("invalid number of X");
        }

        Iso8601_Rule(int n) {
            this.length = n;
        }

        @Override
        public int estimateLength() {
            return this.length;
        }

        @Override
        public void appendTo(Appendable appendable, Calendar calendar) {
            int n = calendar.get(15) + calendar.get(16);
            if (n == 0) {
                appendable.append("Z");
                return;
            }
            if (n < 0) {
                appendable.append('-');
                n = -n;
            } else {
                appendable.append('+');
            }
            int n2 = n / 3600000;
            FastDatePrinter.appendDigits(appendable, n2);
            if (this.length < 5) {
                return;
            }
            if (this.length == 6) {
                appendable.append(':');
            }
            n = n / 60000 - 60 * n2;
            FastDatePrinter.appendDigits(appendable, n);
        }
    }

    static class TimeZoneNumberRule
    implements Rule {
        static final TimeZoneNumberRule INSTANCE_COLON = new TimeZoneNumberRule(true);
        static final TimeZoneNumberRule INSTANCE_NO_COLON = new TimeZoneNumberRule(false);
        final boolean mColon;

        TimeZoneNumberRule(boolean bl) {
            this.mColon = bl;
        }

        @Override
        public int estimateLength() {
            return 5;
        }

        @Override
        public void appendTo(Appendable appendable, Calendar calendar) {
            int n = calendar.get(15) + calendar.get(16);
            if (n < 0) {
                appendable.append('-');
                n = -n;
            } else {
                appendable.append('+');
            }
            int n2 = n / 3600000;
            FastDatePrinter.appendDigits(appendable, n2);
            if (this.mColon) {
                appendable.append(':');
            }
            n = n / 60000 - 60 * n2;
            FastDatePrinter.appendDigits(appendable, n);
        }
    }

    static class TimeZoneNameRule
    implements Rule {
        private final Locale mLocale;
        private final int mStyle;
        private final String mStandard;
        private final String mDaylight;

        TimeZoneNameRule(TimeZone timeZone, Locale locale, int n) {
            this.mLocale = locale;
            this.mStyle = n;
            this.mStandard = FastDatePrinter.getTimeZoneDisplay(timeZone, false, n, locale);
            this.mDaylight = FastDatePrinter.getTimeZoneDisplay(timeZone, true, n, locale);
        }

        @Override
        public int estimateLength() {
            return Math.max(this.mStandard.length(), this.mDaylight.length());
        }

        @Override
        public void appendTo(Appendable appendable, Calendar calendar) {
            TimeZone timeZone = calendar.getTimeZone();
            if (calendar.get(16) != 0) {
                appendable.append(FastDatePrinter.getTimeZoneDisplay(timeZone, true, this.mStyle, this.mLocale));
                return;
            }
            appendable.append(FastDatePrinter.getTimeZoneDisplay(timeZone, false, this.mStyle, this.mLocale));
        }
    }

    static class WeekYear
    implements NumberRule {
        private final NumberRule mRule;

        WeekYear(NumberRule numberRule) {
            this.mRule = numberRule;
        }

        @Override
        public int estimateLength() {
            return this.mRule.estimateLength();
        }

        @Override
        public void appendTo(Appendable appendable, Calendar calendar) {
            this.mRule.appendTo(appendable, CalendarReflection.getWeekYear(calendar));
        }

        @Override
        public void appendTo(Appendable appendable, int n) {
            this.mRule.appendTo(appendable, n);
        }
    }

    static class DayInWeekField
    implements NumberRule {
        private final NumberRule mRule;

        DayInWeekField(NumberRule numberRule) {
            this.mRule = numberRule;
        }

        @Override
        public int estimateLength() {
            return this.mRule.estimateLength();
        }

        @Override
        public void appendTo(Appendable appendable, Calendar calendar) {
            int n = calendar.get(7);
            this.mRule.appendTo(appendable, n != 1 ? n - 1 : 7);
        }

        @Override
        public void appendTo(Appendable appendable, int n) {
            this.mRule.appendTo(appendable, n);
        }
    }

    static class TwentyFourHourField
    implements NumberRule {
        private final NumberRule mRule;

        TwentyFourHourField(NumberRule numberRule) {
            this.mRule = numberRule;
        }

        @Override
        public int estimateLength() {
            return this.mRule.estimateLength();
        }

        @Override
        public void appendTo(Appendable appendable, Calendar calendar) {
            int n = calendar.get(11);
            if (n == 0) {
                n = calendar.getMaximum(11) + 1;
            }
            this.mRule.appendTo(appendable, n);
        }

        @Override
        public void appendTo(Appendable appendable, int n) {
            this.mRule.appendTo(appendable, n);
        }
    }

    static class TwelveHourField
    implements NumberRule {
        private final NumberRule mRule;

        TwelveHourField(NumberRule numberRule) {
            this.mRule = numberRule;
        }

        @Override
        public int estimateLength() {
            return this.mRule.estimateLength();
        }

        @Override
        public void appendTo(Appendable appendable, Calendar calendar) {
            int n = calendar.get(10);
            if (n == 0) {
                n = calendar.getLeastMaximum(10) + 1;
            }
            this.mRule.appendTo(appendable, n);
        }

        @Override
        public void appendTo(Appendable appendable, int n) {
            this.mRule.appendTo(appendable, n);
        }
    }

    static class TwoDigitMonthField
    implements NumberRule {
        static final TwoDigitMonthField INSTANCE = new TwoDigitMonthField();

        TwoDigitMonthField() {
        }

        @Override
        public int estimateLength() {
            return 2;
        }

        @Override
        public void appendTo(Appendable appendable, Calendar calendar) {
            this.appendTo(appendable, calendar.get(2) + 1);
        }

        @Override
        public final void appendTo(Appendable appendable, int n) {
            FastDatePrinter.appendDigits(appendable, n);
        }
    }

    static class TwoDigitYearField
    implements NumberRule {
        static final TwoDigitYearField INSTANCE = new TwoDigitYearField();

        TwoDigitYearField() {
        }

        @Override
        public int estimateLength() {
            return 2;
        }

        @Override
        public void appendTo(Appendable appendable, Calendar calendar) {
            this.appendTo(appendable, calendar.get(1) % 100);
        }

        @Override
        public final void appendTo(Appendable appendable, int n) {
            FastDatePrinter.appendDigits(appendable, n);
        }
    }

    static class TwoDigitNumberField
    implements NumberRule {
        private final int mField;

        TwoDigitNumberField(int n) {
            this.mField = n;
        }

        @Override
        public int estimateLength() {
            return 2;
        }

        @Override
        public void appendTo(Appendable appendable, Calendar calendar) {
            this.appendTo(appendable, calendar.get(this.mField));
        }

        @Override
        public final void appendTo(Appendable appendable, int n) {
            if (n < 100) {
                FastDatePrinter.appendDigits(appendable, n);
                return;
            }
            FastDatePrinter.appendFullDigits(appendable, n, 2);
        }
    }

    static class PaddedNumberField
    implements NumberRule {
        private final int mField;
        private final int mSize;

        PaddedNumberField(int n, int n2) {
            if (n2 < 3) {
                throw new IllegalArgumentException();
            }
            this.mField = n;
            this.mSize = n2;
        }

        @Override
        public int estimateLength() {
            return this.mSize;
        }

        @Override
        public void appendTo(Appendable appendable, Calendar calendar) {
            this.appendTo(appendable, calendar.get(this.mField));
        }

        @Override
        public final void appendTo(Appendable appendable, int n) {
            FastDatePrinter.appendFullDigits(appendable, n, this.mSize);
        }
    }

    static class UnpaddedMonthField
    implements NumberRule {
        static final UnpaddedMonthField INSTANCE = new UnpaddedMonthField();

        UnpaddedMonthField() {
        }

        @Override
        public int estimateLength() {
            return 2;
        }

        @Override
        public void appendTo(Appendable appendable, Calendar calendar) {
            this.appendTo(appendable, calendar.get(2) + 1);
        }

        @Override
        public final void appendTo(Appendable appendable, int n) {
            if (n < 10) {
                appendable.append((char)(n + 48));
                return;
            }
            FastDatePrinter.appendDigits(appendable, n);
        }
    }

    static class UnpaddedNumberField
    implements NumberRule {
        private final int mField;

        UnpaddedNumberField(int n) {
            this.mField = n;
        }

        @Override
        public int estimateLength() {
            return 4;
        }

        @Override
        public void appendTo(Appendable appendable, Calendar calendar) {
            this.appendTo(appendable, calendar.get(this.mField));
        }

        @Override
        public final void appendTo(Appendable appendable, int n) {
            if (n < 10) {
                appendable.append((char)(n + 48));
                return;
            }
            if (n < 100) {
                FastDatePrinter.appendDigits(appendable, n);
                return;
            }
            FastDatePrinter.appendFullDigits(appendable, n, 1);
        }
    }

    static class TextField
    implements Rule {
        private final int mField;
        private final String[] mValues;

        TextField(int n, String[] stringArray) {
            this.mField = n;
            this.mValues = stringArray;
        }

        @Override
        public int estimateLength() {
            int n = 0;
            int n2 = this.mValues.length;
            while (--n2 >= 0) {
                int n3 = this.mValues[n2].length();
                if (n3 <= n) continue;
                n = n3;
            }
            return n;
        }

        @Override
        public void appendTo(Appendable appendable, Calendar calendar) {
            appendable.append(this.mValues[calendar.get(this.mField)]);
        }
    }

    static class StringLiteral
    implements Rule {
        private final String mValue;

        StringLiteral(String string) {
            this.mValue = string;
        }

        @Override
        public int estimateLength() {
            return this.mValue.length();
        }

        @Override
        public void appendTo(Appendable appendable, Calendar calendar) {
            appendable.append(this.mValue);
        }
    }

    static class CharacterLiteral
    implements Rule {
        private final char mValue;

        CharacterLiteral(char c2) {
            this.mValue = c2;
        }

        @Override
        public int estimateLength() {
            return 1;
        }

        @Override
        public void appendTo(Appendable appendable, Calendar calendar) {
            appendable.append(this.mValue);
        }
    }

    static interface NumberRule
    extends Rule {
        public void appendTo(Appendable var1, int var2);
    }

    static interface Rule {
        public int estimateLength();

        public void appendTo(Appendable var1, Calendar var2);
    }
}

