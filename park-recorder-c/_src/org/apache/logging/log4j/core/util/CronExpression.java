/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeSet;

public final class CronExpression {
    protected static final int SECOND = 0;
    protected static final int MINUTE = 1;
    protected static final int HOUR = 2;
    protected static final int DAY_OF_MONTH = 3;
    protected static final int MONTH = 4;
    protected static final int DAY_OF_WEEK = 5;
    protected static final int YEAR = 6;
    protected static final int ALL_SPEC_INT = 99;
    protected static final int NO_SPEC_INT = 98;
    protected static final Integer ALL_SPEC = 99;
    protected static final Integer NO_SPEC = 98;
    protected static final Map<String, Integer> monthMap = new HashMap<String, Integer>(20);
    protected static final Map<String, Integer> dayMap = new HashMap<String, Integer>(60);
    private final String cronExpression;
    private TimeZone timeZone = null;
    protected transient TreeSet<Integer> seconds;
    protected transient TreeSet<Integer> minutes;
    protected transient TreeSet<Integer> hours;
    protected transient TreeSet<Integer> daysOfMonth;
    protected transient TreeSet<Integer> months;
    protected transient TreeSet<Integer> daysOfWeek;
    protected transient TreeSet<Integer> years;
    protected transient boolean lastdayOfWeek = false;
    protected transient int nthdayOfWeek = 0;
    protected transient boolean lastdayOfMonth = false;
    protected transient boolean nearestWeekday = false;
    protected transient int lastdayOffset = 0;
    protected transient boolean expressionParsed = false;
    public static final int MAX_YEAR;
    public static final Calendar MIN_CAL;
    public static final Date MIN_DATE;

    public CronExpression(String string) {
        if (string == null) {
            throw new IllegalArgumentException("cronExpression cannot be null");
        }
        this.cronExpression = string.toUpperCase(Locale.US);
        CronExpression cronExpression = this;
        cronExpression.buildExpression(cronExpression.cronExpression);
    }

    public final boolean isSatisfiedBy(Date date) {
        Comparable<Calendar> comparable = Calendar.getInstance(this.getTimeZone());
        ((Calendar)comparable).setTime(date);
        ((Calendar)comparable).set(14, 0);
        date = ((Calendar)comparable).getTime();
        ((Calendar)comparable).add(13, -1);
        comparable = this.getTimeAfter(((Calendar)comparable).getTime());
        return comparable != null && ((Date)comparable).equals(date);
    }

    public final Date getNextValidTimeAfter(Date date) {
        return this.getTimeAfter(date);
    }

    public final Date getNextInvalidTimeAfter(Date comparable) {
        long l = 1000L;
        Comparable<Calendar> comparable2 = Calendar.getInstance(this.getTimeZone());
        ((Calendar)comparable2).setTime((Date)comparable);
        ((Calendar)comparable2).set(14, 0);
        comparable = ((Calendar)comparable2).getTime();
        while (l == 1000L && (comparable2 = this.getTimeAfter((Date)comparable)) != null) {
            l = ((Date)comparable2).getTime() - comparable.getTime();
            if (l != 1000L) continue;
            comparable = comparable2;
        }
        return new Date(comparable.getTime() + 1000L);
    }

    public final TimeZone getTimeZone() {
        if (this.timeZone == null) {
            this.timeZone = TimeZone.getDefault();
        }
        return this.timeZone;
    }

    public final void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public final String toString() {
        return this.cronExpression;
    }

    public static boolean isValidExpression(String string) {
        try {
            new CronExpression(string);
        }
        catch (ParseException parseException) {
            return false;
        }
        return true;
    }

    public static void validateExpression(String string) {
        new CronExpression(string);
    }

    protected final void buildExpression(String string) {
        this.expressionParsed = true;
        try {
            boolean bl;
            Object object;
            int n;
            if (this.seconds == null) {
                this.seconds = new TreeSet();
            }
            if (this.minutes == null) {
                this.minutes = new TreeSet();
            }
            if (this.hours == null) {
                this.hours = new TreeSet();
            }
            if (this.daysOfMonth == null) {
                this.daysOfMonth = new TreeSet();
            }
            if (this.months == null) {
                this.months = new TreeSet();
            }
            if (this.daysOfWeek == null) {
                this.daysOfWeek = new TreeSet();
            }
            if (this.years == null) {
                this.years = new TreeSet();
            }
            StringTokenizer stringTokenizer = new StringTokenizer(string, " \t", false);
            for (n = 0; stringTokenizer.hasMoreTokens() && n <= 6; ++n) {
                object = stringTokenizer.nextToken().trim();
                if (n == 3 && ((String)object).indexOf(76) != -1 && ((String)object).length() > 1 && ((String)object).contains(",")) {
                    throw new ParseException("Support for specifying 'L' and 'LW' with other days of the month is not implemented", -1);
                }
                if (n == 5 && ((String)object).indexOf(76) != -1 && ((String)object).length() > 1 && ((String)object).contains(",")) {
                    throw new ParseException("Support for specifying 'L' with other days of the week is not implemented", -1);
                }
                if (n == 5 && ((String)object).indexOf(35) != -1 && ((String)object).indexOf(35, ((String)object).indexOf(35) + 1) != -1) {
                    throw new ParseException("Support for specifying multiple \"nth\" days is not implemented.", -1);
                }
                object = new StringTokenizer((String)object, ",");
                while (((StringTokenizer)object).hasMoreTokens()) {
                    String string2 = ((StringTokenizer)object).nextToken();
                    this.storeExpressionVals(0, string2, n);
                }
            }
            if (n <= 5) {
                throw new ParseException("Unexpected end of expression.", string.length());
            }
            if (n <= 6) {
                this.storeExpressionVals(0, "*", 6);
            }
            object = this.getSet(5);
            boolean bl2 = !this.getSet(3).contains(NO_SPEC);
            boolean bl3 = bl = !((TreeSet)object).contains(NO_SPEC);
            if (!(bl2 && !bl || bl && !bl2)) {
                throw new ParseException("Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.", 0);
            }
            return;
        }
        catch (ParseException parseException) {
            throw parseException;
        }
        catch (Exception exception) {
            throw new ParseException("Illegal cron expression format (" + exception.toString() + ")", 0);
        }
    }

    protected final int storeExpressionVals(int n, String string, int n2) {
        int n3;
        int n4 = 0;
        if ((n = this.skipWhiteSpace(n, string)) >= string.length()) {
            return n;
        }
        char c2 = string.charAt(n);
        if (!(c2 < 'A' || c2 > 'Z' || string.equals("L") || string.equals("LW") || string.matches("^L-[0-9]*[W]?"))) {
            int n5;
            int n6;
            block48: {
                int n7 = n;
                String string2 = string.substring(n7, n7 + 3);
                n6 = -1;
                if (n2 == 4) {
                    n5 = this.getMonthNumber(string2) + 1;
                    if (n5 <= 0) {
                        throw new ParseException("Invalid Month value: '" + string2 + "'", n);
                    }
                    if (string.length() > n + 3 && string.charAt(n + 3) == '-') {
                        int n8 = n += 4;
                        string2 = string.substring(n8, n8 + 3);
                        n6 = this.getMonthNumber(string2) + 1;
                        if (n6 <= 0) {
                            throw new ParseException("Invalid Month value: '" + string2 + "'", n);
                        }
                    }
                } else if (n2 == 5) {
                    n5 = this.getDayOfWeekNumber(string2);
                    if (n5 < 0) {
                        throw new ParseException("Invalid Day-of-Week value: '" + string2 + "'", n);
                    }
                    if (string.length() > n + 3) {
                        c2 = string.charAt(n + 3);
                        if (c2 == '-') {
                            int n9 = n += 4;
                            string2 = string.substring(n9, n9 + 3);
                            n6 = this.getDayOfWeekNumber(string2);
                            if (n6 < 0) {
                                throw new ParseException("Invalid Day-of-Week value: '" + string2 + "'", n);
                            }
                        } else {
                            if (c2 == '#') {
                                try {
                                    this.nthdayOfWeek = Integer.parseInt(string.substring(n += 4));
                                    if (this.nthdayOfWeek <= 0 || this.nthdayOfWeek > 5) {
                                        throw new Exception();
                                    }
                                    break block48;
                                }
                                catch (Exception exception) {
                                    throw new ParseException("A numeric value between 1 and 5 must follow the '#' option", n);
                                }
                            }
                            if (c2 == 'L') {
                                this.lastdayOfWeek = true;
                                ++n;
                            }
                        }
                    }
                } else {
                    throw new ParseException("Illegal characters for this position: '" + string2 + "'", n);
                }
            }
            if (n6 != -1) {
                n4 = 1;
            }
            this.addToSet(n5, n6, n4, n2);
            return n + 3;
        }
        if (c2 == '?') {
            if (++n + 1 < string.length() && string.charAt(n) != ' ' && string.charAt(n + 1) != '\t') {
                throw new ParseException("Illegal character after '?': " + string.charAt(n), n);
            }
            if (n2 != 5 && n2 != 3) {
                throw new ParseException("'?' can only be specfied for Day-of-Month or Day-of-Week.", n);
            }
            if (n2 == 5 && !this.lastdayOfMonth && this.daysOfMonth.last() == 98) {
                throw new ParseException("'?' can only be specfied for Day-of-Month -OR- Day-of-Week.", n);
            }
            this.addToSet(98, -1, 0, n2);
            return n;
        }
        if (c2 == '*' || c2 == '/') {
            if (c2 == '*' && n + 1 >= string.length()) {
                this.addToSet(99, -1, 0, n2);
                return n + 1;
            }
            if (c2 == '/' && (n + 1 >= string.length() || string.charAt(n + 1) == ' ' || string.charAt(n + 1) == '\t')) {
                throw new ParseException("'/' must be followed by an integer.", n);
            }
            if (c2 == '*') {
                ++n;
            }
            if (string.charAt(n) == '/') {
                if (++n >= string.length()) {
                    throw new ParseException("Unexpected end of string.", n);
                }
                n4 = this.getNumericValue(string, n);
                ++n;
                if (n4 > 10) {
                    ++n;
                }
                if (n4 > 59 && (n2 == 0 || n2 == 1)) {
                    throw new ParseException("Increment > 60 : ".concat(String.valueOf(n4)), n);
                }
                if (n4 > 23 && n2 == 2) {
                    throw new ParseException("Increment > 24 : ".concat(String.valueOf(n4)), n);
                }
                if (n4 > 31 && n2 == 3) {
                    throw new ParseException("Increment > 31 : ".concat(String.valueOf(n4)), n);
                }
                if (n4 > 7 && n2 == 5) {
                    throw new ParseException("Increment > 7 : ".concat(String.valueOf(n4)), n);
                }
                if (n4 > 12 && n2 == 4) {
                    throw new ParseException("Increment > 12 : ".concat(String.valueOf(n4)), n);
                }
            } else {
                n4 = 1;
            }
            this.addToSet(99, -1, n4, n2);
            return n;
        }
        if (c2 == 'L') {
            ++n;
            if (n2 == 3) {
                this.lastdayOfMonth = true;
            }
            if (n2 == 5) {
                this.addToSet(7, 7, 0, n2);
            }
            if (n2 == 3 && string.length() > n) {
                if (string.charAt(n) == '-') {
                    ValueSet valueSet = this.getValue(0, string, n + 1);
                    this.lastdayOffset = valueSet.value;
                    if (this.lastdayOffset > 30) {
                        throw new ParseException("Offset from last day must be <= 30", n + 1);
                    }
                    n = valueSet.pos;
                }
                if (string.length() > n && string.charAt(n) == 'W') {
                    this.nearestWeekday = true;
                    ++n;
                }
            }
            return n;
        }
        if (c2 >= '0' && c2 <= '9') {
            n3 = Integer.parseInt(String.valueOf(c2));
            if (++n < string.length()) {
                c2 = string.charAt(n);
                if (c2 >= '0' && c2 <= '9') {
                    ValueSet valueSet = this.getValue(n3, string, n);
                    n3 = valueSet.value;
                    n = valueSet.pos;
                }
                return this.checkNext(n, string, n3, n2);
            }
        } else {
            throw new ParseException("Unexpected character: ".concat(String.valueOf(c2)), n);
        }
        this.addToSet(n3, -1, -1, n2);
        return n;
    }

    protected final int checkNext(int n, String object, int n2, int n3) {
        int n4 = n;
        if (n >= ((String)object).length()) {
            this.addToSet(n2, -1, -1, n3);
            return n;
        }
        char c2 = ((String)object).charAt(n);
        if (c2 == 'L') {
            if (n3 == 5) {
                if (n2 <= 0 || n2 > 7) {
                    throw new ParseException("Day-of-Week values must be between 1 and 7", -1);
                }
            } else {
                throw new ParseException("'L' option is not valid here. (pos=" + n + ")", n);
            }
            this.lastdayOfWeek = true;
            this.getSet(n3).add(n2);
            return ++n4;
        }
        if (c2 == 'W') {
            if (n3 != 3) {
                throw new ParseException("'W' option is not valid here. (pos=" + n + ")", n);
            }
            this.nearestWeekday = true;
            if (n2 > 31) {
                throw new ParseException("The 'W' option does not make sense with values larger than 31 (max number of days in a month)", n);
            }
            this.getSet(n3).add(n2);
            return ++n4;
        }
        if (c2 == '#') {
            if (n3 != 5) {
                throw new ParseException("'#' option is not valid here. (pos=" + n + ")", n);
            }
            ++n4;
            try {
                this.nthdayOfWeek = Integer.parseInt(((String)object).substring(n4));
                if (this.nthdayOfWeek <= 0 || this.nthdayOfWeek > 5) {
                    throw new Exception();
                }
            }
            catch (Exception exception) {
                throw new ParseException("A numeric value between 1 and 5 must follow the '#' option", n4);
            }
            this.getSet(n3).add(n2);
            return ++n4;
        }
        if (c2 == '-') {
            n = Integer.parseInt(String.valueOf(((String)object).charAt(++n4)));
            if (++n4 >= ((String)object).length()) {
                this.addToSet(n2, n, 1, n3);
                return n4;
            }
            c2 = ((String)object).charAt(n4);
            if (c2 >= '0' && c2 <= '9') {
                ValueSet valueSet = this.getValue(n, (String)object, n4);
                n = valueSet.value;
                n4 = valueSet.pos;
            }
            if (n4 < ((String)object).length() && ((String)object).charAt(n4) == '/') {
                int n5 = Integer.parseInt(String.valueOf(((String)object).charAt(++n4)));
                if (++n4 >= ((String)object).length()) {
                    this.addToSet(n2, n, n5, n3);
                    return n4;
                }
                c2 = ((String)object).charAt(n4);
                if (c2 >= '0' && c2 <= '9') {
                    object = this.getValue(n5, (String)object, n4);
                    n4 = ((ValueSet)object).value;
                    this.addToSet(n2, n, n4, n3);
                    return ((ValueSet)object).pos;
                }
                this.addToSet(n2, n, n5, n3);
                return n4;
            }
            this.addToSet(n2, n, 1, n3);
            return n4;
        }
        if (c2 == '/') {
            n = Integer.parseInt(String.valueOf(((String)object).charAt(++n4)));
            if (++n4 >= ((String)object).length()) {
                this.addToSet(n2, -1, n, n3);
                return n4;
            }
            c2 = ((String)object).charAt(n4);
            if (c2 >= '0' && c2 <= '9') {
                ValueSet valueSet = this.getValue(n, (String)object, n4);
                int n6 = valueSet.value;
                this.addToSet(n2, -1, n6, n3);
                return valueSet.pos;
            }
            throw new ParseException("Unexpected character '" + c2 + "' after '/'", n4);
        }
        this.addToSet(n2, -1, 0, n3);
        return ++n4;
    }

    public final String getCronExpression() {
        return this.cronExpression;
    }

    public final String getExpressionSummary() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("seconds: ");
        CronExpression cronExpression = this;
        stringBuilder.append(cronExpression.getExpressionSetSummary(cronExpression.seconds));
        stringBuilder.append("\n");
        stringBuilder.append("minutes: ");
        CronExpression cronExpression2 = this;
        stringBuilder.append(cronExpression2.getExpressionSetSummary(cronExpression2.minutes));
        stringBuilder.append("\n");
        stringBuilder.append("hours: ");
        CronExpression cronExpression3 = this;
        stringBuilder.append(cronExpression3.getExpressionSetSummary(cronExpression3.hours));
        stringBuilder.append("\n");
        stringBuilder.append("daysOfMonth: ");
        CronExpression cronExpression4 = this;
        stringBuilder.append(cronExpression4.getExpressionSetSummary(cronExpression4.daysOfMonth));
        stringBuilder.append("\n");
        stringBuilder.append("months: ");
        CronExpression cronExpression5 = this;
        stringBuilder.append(cronExpression5.getExpressionSetSummary(cronExpression5.months));
        stringBuilder.append("\n");
        stringBuilder.append("daysOfWeek: ");
        CronExpression cronExpression6 = this;
        stringBuilder.append(cronExpression6.getExpressionSetSummary(cronExpression6.daysOfWeek));
        stringBuilder.append("\n");
        stringBuilder.append("lastdayOfWeek: ");
        stringBuilder.append(this.lastdayOfWeek);
        stringBuilder.append("\n");
        stringBuilder.append("nearestWeekday: ");
        stringBuilder.append(this.nearestWeekday);
        stringBuilder.append("\n");
        stringBuilder.append("NthDayOfWeek: ");
        stringBuilder.append(this.nthdayOfWeek);
        stringBuilder.append("\n");
        stringBuilder.append("lastdayOfMonth: ");
        stringBuilder.append(this.lastdayOfMonth);
        stringBuilder.append("\n");
        stringBuilder.append("years: ");
        CronExpression cronExpression7 = this;
        stringBuilder.append(cronExpression7.getExpressionSetSummary(cronExpression7.years));
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    protected final String getExpressionSetSummary(Set<Integer> object) {
        if (object.contains(NO_SPEC)) {
            return "?";
        }
        if (object.contains(ALL_SPEC)) {
            return "*";
        }
        StringBuilder stringBuilder = new StringBuilder();
        object = object.iterator();
        boolean bl = true;
        while (object.hasNext()) {
            String string = ((Integer)object.next()).toString();
            if (!bl) {
                stringBuilder.append(",");
            }
            stringBuilder.append(string);
            bl = false;
        }
        return stringBuilder.toString();
    }

    protected final String getExpressionSetSummary(ArrayList<Integer> object) {
        if (((ArrayList)object).contains(NO_SPEC)) {
            return "?";
        }
        if (((ArrayList)object).contains(ALL_SPEC)) {
            return "*";
        }
        StringBuilder stringBuilder = new StringBuilder();
        object = ((ArrayList)object).iterator();
        boolean bl = true;
        while (object.hasNext()) {
            String string = ((Integer)object.next()).toString();
            if (!bl) {
                stringBuilder.append(",");
            }
            stringBuilder.append(string);
            bl = false;
        }
        return stringBuilder.toString();
    }

    protected final int skipWhiteSpace(int n, String string) {
        while (n < string.length() && (string.charAt(n) == ' ' || string.charAt(n) == '\t')) {
            ++n;
        }
        return n;
    }

    protected final int findNextWhiteSpace(int n, String string) {
        while (n < string.length() && (string.charAt(n) != ' ' || string.charAt(n) != '\t')) {
            ++n;
        }
        return n;
    }

    protected final void addToSet(int n, int n2, int n3, int n4) {
        TreeSet<Integer> treeSet = this.getSet(n4);
        if (n4 == 0 || n4 == 1) {
            if ((n < 0 || n > 59 || n2 > 59) && n != 99) {
                throw new ParseException("Minute and Second values must be between 0 and 59", -1);
            }
        } else if (n4 == 2) {
            if ((n < 0 || n > 23 || n2 > 23) && n != 99) {
                throw new ParseException("Hour values must be between 0 and 23", -1);
            }
        } else if (n4 == 3) {
            if ((n <= 0 || n > 31 || n2 > 31) && n != 99 && n != 98) {
                throw new ParseException("Day of month values must be between 1 and 31", -1);
            }
        } else if (n4 == 4) {
            if ((n <= 0 || n > 12 || n2 > 12) && n != 99) {
                throw new ParseException("Month values must be between 1 and 12", -1);
            }
        } else if (n4 == 5 && (n == 0 || n > 7 || n2 > 7) && n != 99 && n != 98) {
            throw new ParseException("Day-of-Week values must be between 1 and 7", -1);
        }
        if ((n3 == 0 || n3 == -1) && n != 99) {
            if (n != -1) {
                treeSet.add(n);
                return;
            }
            treeSet.add(NO_SPEC);
            return;
        }
        int n5 = n;
        int n6 = n2;
        if (n == 99 && n3 <= 0) {
            n3 = 1;
            treeSet.add(ALL_SPEC);
        }
        if (n4 == 0 || n4 == 1) {
            if (n2 == -1) {
                n6 = 59;
            }
            if (n == -1 || n == 99) {
                n5 = 0;
            }
        } else if (n4 == 2) {
            if (n2 == -1) {
                n6 = 23;
            }
            if (n == -1 || n == 99) {
                n5 = 0;
            }
        } else if (n4 == 3) {
            if (n2 == -1) {
                n6 = 31;
            }
            if (n == -1 || n == 99) {
                n5 = 1;
            }
        } else if (n4 == 4) {
            if (n2 == -1) {
                n6 = 12;
            }
            if (n == -1 || n == 99) {
                n5 = 1;
            }
        } else if (n4 == 5) {
            if (n2 == -1) {
                n6 = 7;
            }
            if (n == -1 || n == 99) {
                n5 = 1;
            }
        } else if (n4 == 6) {
            if (n2 == -1) {
                n6 = MAX_YEAR;
            }
            if (n == -1 || n == 99) {
                n5 = 1970;
            }
        }
        n = -1;
        if (n6 < n5) {
            switch (n4) {
                case 0: {
                    n = 60;
                    break;
                }
                case 1: {
                    n = 60;
                    break;
                }
                case 2: {
                    n = 24;
                    break;
                }
                case 4: {
                    n = 12;
                    break;
                }
                case 5: {
                    n = 7;
                    break;
                }
                case 3: {
                    n = 31;
                    break;
                }
                case 6: {
                    throw new IllegalArgumentException("Start year must be less than stop year");
                }
                default: {
                    throw new IllegalArgumentException("Unexpected type encountered");
                }
            }
            n6 += n;
        }
        for (n2 = n5; n2 <= n6; n2 += n3) {
            if (n == -1) {
                treeSet.add(n2);
                continue;
            }
            n5 = n2 % n;
            if (n5 == 0 && (n4 == 4 || n4 == 5 || n4 == 3)) {
                n5 = n;
            }
            treeSet.add(n5);
        }
    }

    final TreeSet<Integer> getSet(int n) {
        switch (n) {
            case 0: {
                return this.seconds;
            }
            case 1: {
                return this.minutes;
            }
            case 2: {
                return this.hours;
            }
            case 3: {
                return this.daysOfMonth;
            }
            case 4: {
                return this.months;
            }
            case 5: {
                return this.daysOfWeek;
            }
            case 6: {
                return this.years;
            }
        }
        return null;
    }

    protected final ValueSet getValue(int n, String string, int n2) {
        char c2 = string.charAt(n2);
        StringBuilder stringBuilder = new StringBuilder(String.valueOf(n));
        while (c2 >= '0' && c2 <= '9') {
            stringBuilder.append(c2);
            if (++n2 >= string.length()) break;
            c2 = string.charAt(n2);
        }
        ValueSet valueSet = new ValueSet();
        new ValueSet().pos = n2 < string.length() ? n2 : n2 + 1;
        valueSet.value = Integer.parseInt(stringBuilder.toString());
        return valueSet;
    }

    protected final int getNumericValue(String string, int n) {
        int n2 = this.findNextWhiteSpace(n, string);
        return Integer.parseInt(string.substring(n, n2));
    }

    protected final int getMonthNumber(String object) {
        if ((object = monthMap.get(object)) == null) {
            return -1;
        }
        return (Integer)object;
    }

    protected final int getDayOfWeekNumber(String object) {
        if ((object = dayMap.get(object)) == null) {
            return -1;
        }
        return (Integer)object;
    }

    public final Date getTimeAfter(Date date) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar(this.getTimeZone());
        date = new Date(date.getTime() + 1000L);
        gregorianCalendar.setTime(date);
        gregorianCalendar.set(14, 0);
        boolean bl = false;
        while (!bl) {
            int n;
            int n2;
            if (gregorianCalendar.get(1) > 2999) {
                return null;
            }
            int n3 = gregorianCalendar.get(13);
            int n4 = gregorianCalendar.get(12);
            SortedSet<Integer> sortedSet = this.seconds.tailSet(n3);
            if (sortedSet != null && sortedSet.size() != 0) {
                n3 = sortedSet.first();
            } else {
                n3 = this.seconds.first();
                gregorianCalendar.set(12, ++n4);
            }
            gregorianCalendar.set(13, n3);
            n4 = gregorianCalendar.get(12);
            int n5 = gregorianCalendar.get(11);
            int n6 = -1;
            sortedSet = this.minutes.tailSet(n4);
            if (sortedSet != null && sortedSet.size() != 0) {
                n6 = n4;
                n4 = sortedSet.first();
            } else {
                n4 = this.minutes.first();
                ++n5;
            }
            if (n4 != n6) {
                gregorianCalendar.set(13, 0);
                gregorianCalendar.set(12, n4);
                this.setCalendarHour(gregorianCalendar, n5);
                continue;
            }
            gregorianCalendar.set(12, n4);
            n5 = gregorianCalendar.get(11);
            int n7 = gregorianCalendar.get(5);
            n6 = -1;
            sortedSet = this.hours.tailSet(n5);
            if (sortedSet != null && sortedSet.size() != 0) {
                n6 = n5;
                n5 = sortedSet.first();
            } else {
                n5 = this.hours.first();
                ++n7;
            }
            if (n5 != n6) {
                gregorianCalendar.set(13, 0);
                gregorianCalendar.set(12, 0);
                gregorianCalendar.set(5, n7);
                this.setCalendarHour(gregorianCalendar, n5);
                continue;
            }
            gregorianCalendar.set(11, n5);
            n7 = gregorianCalendar.get(5);
            int n8 = gregorianCalendar.get(2) + 1;
            n6 = -1;
            int n9 = n8;
            int n10 = !this.daysOfMonth.contains(NO_SPEC) ? 1 : 0;
            int n11 = n2 = !this.daysOfWeek.contains(NO_SPEC) ? 1 : 0;
            if (n10 != 0 && n2 == 0) {
                SortedSet<Integer> sortedSet2 = this.daysOfMonth.tailSet(n7);
                if (this.lastdayOfMonth) {
                    if (!this.nearestWeekday) {
                        n6 = n7;
                        if (n6 > (n7 = this.getLastDayOfMonth(n8, gregorianCalendar.get(1)) - this.lastdayOffset)) {
                            if (++n8 > 12) {
                                n8 = 1;
                                n9 = 3333;
                                ((Calendar)gregorianCalendar).add(1, 1);
                            }
                            n7 = 1;
                        }
                    } else {
                        n6 = n7;
                        n7 = this.getLastDayOfMonth(n8, gregorianCalendar.get(1)) - this.lastdayOffset;
                        Calendar calendar = Calendar.getInstance(this.getTimeZone());
                        calendar.set(13, 0);
                        calendar.set(12, 0);
                        calendar.set(11, 0);
                        calendar.set(5, n7);
                        calendar.set(2, n8 - 1);
                        calendar.set(1, gregorianCalendar.get(1));
                        n = this.getLastDayOfMonth(n8, gregorianCalendar.get(1));
                        int n12 = calendar.get(7);
                        if (n12 == 7 && n7 == 1) {
                            n7 += 2;
                        } else if (n12 == 7) {
                            --n7;
                        } else if (n12 == 1 && n7 == n) {
                            n7 -= 2;
                        } else if (n12 == 1) {
                            ++n7;
                        }
                        calendar.set(13, n3);
                        calendar.set(12, n4);
                        calendar.set(11, n5);
                        calendar.set(5, n7);
                        calendar.set(2, n8 - 1);
                        if (calendar.getTime().before(date)) {
                            n7 = 1;
                            ++n8;
                        }
                    }
                } else if (this.nearestWeekday) {
                    n6 = n7;
                    n7 = this.daysOfMonth.first();
                    Calendar calendar = Calendar.getInstance(this.getTimeZone());
                    calendar.set(13, 0);
                    calendar.set(12, 0);
                    calendar.set(11, 0);
                    calendar.set(5, n7);
                    calendar.set(2, n8 - 1);
                    calendar.set(1, gregorianCalendar.get(1));
                    n = this.getLastDayOfMonth(n8, gregorianCalendar.get(1));
                    int n13 = calendar.get(7);
                    if (n13 == 7 && n7 == 1) {
                        n7 += 2;
                    } else if (n13 == 7) {
                        --n7;
                    } else if (n13 == 1 && n7 == n) {
                        n7 -= 2;
                    } else if (n13 == 1) {
                        ++n7;
                    }
                    calendar.set(13, n3);
                    calendar.set(12, n4);
                    calendar.set(11, n5);
                    calendar.set(5, n7);
                    calendar.set(2, n8 - 1);
                    if (calendar.getTime().before(date)) {
                        n7 = this.daysOfMonth.first();
                        ++n8;
                    }
                } else if (sortedSet2 != null && sortedSet2.size() != 0) {
                    n6 = n7;
                    n7 = sortedSet2.first();
                    if (n7 > (n2 = this.getLastDayOfMonth(n8, gregorianCalendar.get(1)))) {
                        n7 = this.daysOfMonth.first();
                        ++n8;
                    }
                } else {
                    n7 = this.daysOfMonth.first();
                    ++n8;
                }
                if (n7 != n6 || n8 != n9) {
                    gregorianCalendar.set(13, 0);
                    gregorianCalendar.set(12, 0);
                    gregorianCalendar.set(11, 0);
                    gregorianCalendar.set(5, n7);
                    gregorianCalendar.set(2, n8 - 1);
                    continue;
                }
            } else if (n2 != 0 && n10 == 0) {
                if (this.lastdayOfWeek) {
                    n2 = this.daysOfWeek.first();
                    n = gregorianCalendar.get(7);
                    n10 = 0;
                    if (n < n2) {
                        n10 = n2 - n;
                    }
                    if (n > n2) {
                        n10 = n2 + (7 - n);
                    }
                    if (n7 + n10 > (n3 = this.getLastDayOfMonth(n8, gregorianCalendar.get(1)))) {
                        gregorianCalendar.set(13, 0);
                        gregorianCalendar.set(12, 0);
                        gregorianCalendar.set(11, 0);
                        gregorianCalendar.set(5, 1);
                        gregorianCalendar.set(2, n8);
                        continue;
                    }
                    while (n7 + n10 + 7 <= n3) {
                        n10 += 7;
                    }
                    n7 += n10;
                    if (n10 > 0) {
                        gregorianCalendar.set(13, 0);
                        gregorianCalendar.set(12, 0);
                        gregorianCalendar.set(11, 0);
                        gregorianCalendar.set(5, n7);
                        gregorianCalendar.set(2, n8 - 1);
                        continue;
                    }
                } else if (this.nthdayOfWeek != 0) {
                    n2 = this.daysOfWeek.first();
                    n = gregorianCalendar.get(7);
                    n10 = 0;
                    if (n < n2) {
                        n10 = n2 - n;
                    } else if (n > n2) {
                        n10 = n2 + (7 - n);
                    }
                    n3 = 0;
                    if (n10 > 0) {
                        n3 = 1;
                    }
                    n7 += n10;
                    n10 = n7 / 7;
                    if (n7 % 7 > 0) {
                        ++n10;
                    }
                    if ((n10 = (this.nthdayOfWeek - n10) * 7) < 0 || (n7 += n10) > this.getLastDayOfMonth(n8, gregorianCalendar.get(1))) {
                        gregorianCalendar.set(13, 0);
                        gregorianCalendar.set(12, 0);
                        gregorianCalendar.set(11, 0);
                        gregorianCalendar.set(5, 1);
                        gregorianCalendar.set(2, n8);
                        continue;
                    }
                    if (n10 > 0 || n3 != 0) {
                        gregorianCalendar.set(13, 0);
                        gregorianCalendar.set(12, 0);
                        gregorianCalendar.set(11, 0);
                        gregorianCalendar.set(5, n7);
                        gregorianCalendar.set(2, n8 - 1);
                        continue;
                    }
                } else {
                    n2 = gregorianCalendar.get(7);
                    n = this.daysOfWeek.first();
                    SortedSet<Integer> sortedSet3 = this.daysOfWeek.tailSet(n2);
                    if (sortedSet3 != null && sortedSet3.size() > 0) {
                        n = sortedSet3.first();
                    }
                    int n14 = 0;
                    if (n2 < n) {
                        n14 = n - n2;
                    }
                    if (n2 > n) {
                        n14 = n + (7 - n2);
                    }
                    if (n7 + n14 > (n3 = this.getLastDayOfMonth(n8, gregorianCalendar.get(1)))) {
                        gregorianCalendar.set(13, 0);
                        gregorianCalendar.set(12, 0);
                        gregorianCalendar.set(11, 0);
                        gregorianCalendar.set(5, 1);
                        gregorianCalendar.set(2, n8);
                        continue;
                    }
                    if (n14 > 0) {
                        gregorianCalendar.set(13, 0);
                        gregorianCalendar.set(12, 0);
                        gregorianCalendar.set(11, 0);
                        gregorianCalendar.set(5, n7 + n14);
                        gregorianCalendar.set(2, n8 - 1);
                        continue;
                    }
                }
            } else {
                throw new UnsupportedOperationException("Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.");
            }
            gregorianCalendar.set(5, n7);
            n8 = gregorianCalendar.get(2) + 1;
            n2 = gregorianCalendar.get(1);
            n6 = -1;
            if (n2 > MAX_YEAR) {
                return null;
            }
            SortedSet<Integer> sortedSet4 = this.months.tailSet(n8);
            if (sortedSet4 != null && sortedSet4.size() != 0) {
                n6 = n8;
                n8 = sortedSet4.first();
            } else {
                n8 = this.months.first();
                ++n2;
            }
            if (n8 != n6) {
                gregorianCalendar.set(13, 0);
                gregorianCalendar.set(12, 0);
                gregorianCalendar.set(11, 0);
                gregorianCalendar.set(5, 1);
                gregorianCalendar.set(2, n8 - 1);
                gregorianCalendar.set(1, n2);
                continue;
            }
            gregorianCalendar.set(2, n8 - 1);
            n2 = gregorianCalendar.get(1);
            sortedSet4 = this.years.tailSet(n2);
            if (sortedSet4 == null || sortedSet4.size() == 0) {
                return null;
            }
            n6 = n2;
            n2 = sortedSet4.first();
            if (n2 != n6) {
                gregorianCalendar.set(13, 0);
                gregorianCalendar.set(12, 0);
                gregorianCalendar.set(11, 0);
                gregorianCalendar.set(5, 1);
                gregorianCalendar.set(2, 0);
                gregorianCalendar.set(1, n2);
                continue;
            }
            gregorianCalendar.set(1, n2);
            bl = true;
        }
        return gregorianCalendar.getTime();
    }

    protected final void setCalendarHour(Calendar calendar, int n) {
        calendar.set(11, n);
        if (calendar.get(11) != n && n != 24) {
            calendar.set(11, n + 1);
        }
    }

    protected final Date getTimeBefore(Date date) {
        Date date2;
        Comparable<Calendar> comparable = Calendar.getInstance(this.getTimeZone());
        ((Calendar)comparable).setTime(date);
        ((Calendar)comparable).set(14, 0);
        date = ((Calendar)comparable).getTime();
        comparable = date;
        long l = this.findMinIncrement();
        do {
            if ((date2 = this.getTimeAfter((Date)(comparable = new Date(((Date)comparable).getTime() - l)))) != null && !date2.before(MIN_DATE)) continue;
            return null;
        } while (date2.compareTo(date) >= 0);
        return date2;
    }

    public final Date getPrevFireTime(Date date) {
        return this.getTimeBefore(date);
    }

    private long findMinIncrement() {
        if (this.seconds.size() != 1) {
            CronExpression cronExpression = this;
            return cronExpression.minInSet(cronExpression.seconds) * 1000;
        }
        if (this.seconds.first() == 99) {
            return 1000L;
        }
        if (this.minutes.size() != 1) {
            CronExpression cronExpression = this;
            return cronExpression.minInSet(cronExpression.minutes) * 60000;
        }
        if (this.minutes.first() == 99) {
            return 60000L;
        }
        if (this.hours.size() != 1) {
            CronExpression cronExpression = this;
            return cronExpression.minInSet(cronExpression.hours) * 3600000;
        }
        if (this.hours.first() == 99) {
            return 3600000L;
        }
        return 86400000L;
    }

    private int minInSet(TreeSet<Integer> object) {
        int n = 0;
        int n2 = Integer.MAX_VALUE;
        boolean bl = true;
        object = ((TreeSet)object).iterator();
        while (object.hasNext()) {
            int n3 = (Integer)object.next();
            if (bl) {
                n = n3;
                bl = false;
                continue;
            }
            if ((n3 -= n) >= n2) continue;
            n2 = n3;
        }
        return n2;
    }

    public final Date getFinalFireTime() {
        return null;
    }

    protected final boolean isLeapYear(int n) {
        return n % 4 == 0 && n % 100 != 0 || n % 400 == 0;
    }

    protected final int getLastDayOfMonth(int n, int n2) {
        switch (n) {
            case 1: {
                return 31;
            }
            case 2: {
                if (this.isLeapYear(n2)) {
                    return 29;
                }
                return 28;
            }
            case 3: {
                return 31;
            }
            case 4: {
                return 30;
            }
            case 5: {
                return 31;
            }
            case 6: {
                return 30;
            }
            case 7: {
                return 31;
            }
            case 8: {
                return 31;
            }
            case 9: {
                return 30;
            }
            case 10: {
                return 31;
            }
            case 11: {
                return 30;
            }
            case 12: {
                return 31;
            }
        }
        throw new IllegalArgumentException("Illegal month number: ".concat(String.valueOf(n)));
    }

    static {
        monthMap.put("JAN", 0);
        monthMap.put("FEB", 1);
        monthMap.put("MAR", 2);
        monthMap.put("APR", 3);
        monthMap.put("MAY", 4);
        monthMap.put("JUN", 5);
        monthMap.put("JUL", 6);
        monthMap.put("AUG", 7);
        monthMap.put("SEP", 8);
        monthMap.put("OCT", 9);
        monthMap.put("NOV", 10);
        monthMap.put("DEC", 11);
        dayMap.put("SUN", 1);
        dayMap.put("MON", 2);
        dayMap.put("TUE", 3);
        dayMap.put("WED", 4);
        dayMap.put("THU", 5);
        dayMap.put("FRI", 6);
        dayMap.put("SAT", 7);
        MAX_YEAR = Calendar.getInstance().get(1) + 100;
        MIN_CAL = Calendar.getInstance();
        MIN_CAL.set(1970, 0, 1);
        MIN_DATE = MIN_CAL.getTime();
    }

    class ValueSet {
        public int value;
        public int pos;

        private ValueSet() {
        }
    }
}

