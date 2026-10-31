/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.time;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.time.DateParser;

public class FastDateParser
implements Serializable,
DateParser {
    private static final long serialVersionUID = 3L;
    static final Locale JAPANESE_IMPERIAL = new Locale("ja", "JP", "JP");
    private final String pattern;
    private final TimeZone timeZone;
    private final Locale locale;
    private final int century;
    private final int startYear;
    private transient List<StrategyAndWidth> patterns;
    private static final Comparator<String> LONGER_FIRST_LOWERCASE = new Comparator<String>(){

        @Override
        public final int compare(String string, String string2) {
            return string2.compareTo(string);
        }
    };
    private static final ConcurrentMap<Locale, Strategy>[] caches = new ConcurrentMap[17];
    private static final Strategy ABBREVIATED_YEAR_STRATEGY = new NumberStrategy(1){

        @Override
        final int modify(FastDateParser fastDateParser, int n) {
            if (n < 100) {
                return fastDateParser.adjustYear(n);
            }
            return n;
        }
    };
    private static final Strategy NUMBER_MONTH_STRATEGY = new NumberStrategy(2){

        @Override
        final int modify(FastDateParser fastDateParser, int n) {
            return n - 1;
        }
    };
    private static final Strategy LITERAL_YEAR_STRATEGY = new NumberStrategy(1);
    private static final Strategy WEEK_OF_YEAR_STRATEGY = new NumberStrategy(3);
    private static final Strategy WEEK_OF_MONTH_STRATEGY = new NumberStrategy(4);
    private static final Strategy DAY_OF_YEAR_STRATEGY = new NumberStrategy(6);
    private static final Strategy DAY_OF_MONTH_STRATEGY = new NumberStrategy(5);
    private static final Strategy DAY_OF_WEEK_STRATEGY = new NumberStrategy(7){

        @Override
        final int modify(FastDateParser fastDateParser, int n) {
            if (n != 7) {
                return n + 1;
            }
            return 1;
        }
    };
    private static final Strategy DAY_OF_WEEK_IN_MONTH_STRATEGY = new NumberStrategy(8);
    private static final Strategy HOUR_OF_DAY_STRATEGY = new NumberStrategy(11);
    private static final Strategy HOUR24_OF_DAY_STRATEGY = new NumberStrategy(11){

        @Override
        final int modify(FastDateParser fastDateParser, int n) {
            if (n == 24) {
                return 0;
            }
            return n;
        }
    };
    private static final Strategy HOUR12_STRATEGY = new NumberStrategy(10){

        @Override
        final int modify(FastDateParser fastDateParser, int n) {
            if (n == 12) {
                return 0;
            }
            return n;
        }
    };
    private static final Strategy HOUR_STRATEGY = new NumberStrategy(10);
    private static final Strategy MINUTE_STRATEGY = new NumberStrategy(12);
    private static final Strategy SECOND_STRATEGY = new NumberStrategy(13);
    private static final Strategy MILLISECOND_STRATEGY = new NumberStrategy(14);

    protected FastDateParser(String string, TimeZone timeZone, Locale locale) {
        this(string, timeZone, locale, null);
    }

    protected FastDateParser(String object, TimeZone timeZone, Locale locale, Date date) {
        int n;
        this.pattern = object;
        this.timeZone = timeZone;
        this.locale = locale;
        object = Calendar.getInstance(timeZone, locale);
        if (date != null) {
            ((Calendar)object).setTime(date);
            n = ((Calendar)object).get(1);
        } else if (locale.equals(JAPANESE_IMPERIAL)) {
            n = 0;
        } else {
            ((Calendar)object).setTime(new Date());
            n = ((Calendar)object).get(1) - 80;
        }
        this.century = n / 100 * 100;
        this.startYear = n - this.century;
        this.init((Calendar)object);
    }

    private void init(Calendar object) {
        StrategyAndWidth strategyAndWidth;
        this.patterns = new ArrayList<StrategyAndWidth>();
        FastDateParser fastDateParser = this;
        object = fastDateParser.new StrategyParser(fastDateParser.pattern, (Calendar)object);
        while ((strategyAndWidth = ((StrategyParser)object).getNextStrategy()) != null) {
            this.patterns.add(strategyAndWidth);
        }
    }

    private static boolean isFormatLetter(char c2) {
        return c2 >= 'A' && c2 <= 'Z' || c2 >= 'a' && c2 <= 'z';
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }

    @Override
    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    public boolean equals(Object object) {
        if (!(object instanceof FastDateParser)) {
            return false;
        }
        object = (FastDateParser)object;
        return this.pattern.equals(((FastDateParser)object).pattern) && this.timeZone.equals(((FastDateParser)object).timeZone) && this.locale.equals(((FastDateParser)object).locale);
    }

    public int hashCode() {
        return this.pattern.hashCode() + 13 * (this.timeZone.hashCode() + 13 * this.locale.hashCode());
    }

    public String toString() {
        return "FastDateParser[" + this.pattern + "," + this.locale + "," + this.timeZone.getID() + "]";
    }

    private void readObject(ObjectInputStream object) {
        ((ObjectInputStream)object).defaultReadObject();
        object = Calendar.getInstance(this.timeZone, this.locale);
        this.init((Calendar)object);
    }

    @Override
    public Object parseObject(String string) {
        return this.parse(string);
    }

    @Override
    public Date parse(String string) {
        ParsePosition parsePosition = new ParsePosition(0);
        Date date = this.parse(string, parsePosition);
        if (date == null) {
            if (this.locale.equals(JAPANESE_IMPERIAL)) {
                throw new ParseException("(The " + this.locale + " locale does not support dates before 1868 AD)\nUnparseable date: \"" + string, parsePosition.getErrorIndex());
            }
            throw new ParseException("Unparseable date: ".concat(String.valueOf(string)), parsePosition.getErrorIndex());
        }
        return date;
    }

    @Override
    public Object parseObject(String string, ParsePosition parsePosition) {
        return this.parse(string, parsePosition);
    }

    @Override
    public Date parse(String string, ParsePosition parsePosition) {
        Calendar calendar = Calendar.getInstance(this.timeZone, this.locale);
        calendar.clear();
        if (this.parse(string, parsePosition, calendar)) {
            return calendar.getTime();
        }
        return null;
    }

    @Override
    public boolean parse(String string, ParsePosition parsePosition, Calendar calendar) {
        ListIterator<StrategyAndWidth> listIterator = this.patterns.listIterator();
        while (listIterator.hasNext()) {
            StrategyAndWidth strategyAndWidth = listIterator.next();
            int n = strategyAndWidth.getMaxWidth(listIterator);
            if (strategyAndWidth.strategy.parse(this, calendar, string, parsePosition, n)) continue;
            return false;
        }
        return true;
    }

    private static StringBuilder simpleQuote(StringBuilder stringBuilder, String string) {
        for (int j = 0; j < string.length(); ++j) {
            char c2 = string.charAt(j);
            switch (c2) {
                case '$': 
                case '(': 
                case ')': 
                case '*': 
                case '+': 
                case '.': 
                case '?': 
                case '[': 
                case '\\': 
                case '^': 
                case '{': 
                case '|': {
                    stringBuilder.append('\\');
                }
            }
            stringBuilder.append(c2);
        }
        return stringBuilder;
    }

    private static Map<String, Integer> appendDisplayNames(Calendar iterator, Locale locale, int n, StringBuilder stringBuilder) {
        HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
        iterator = ((Calendar)((Object)iterator)).getDisplayNames(n, 0, locale);
        TreeSet<String> treeSet = new TreeSet<String>(LONGER_FIRST_LOWERCASE);
        for (Map.Entry entry : iterator.entrySet()) {
            String string = ((String)entry.getKey()).toLowerCase(locale);
            if (!treeSet.add(string)) continue;
            hashMap.put(string, (Integer)entry.getValue());
        }
        for (String string : treeSet) {
            FastDateParser.simpleQuote(stringBuilder, string).append('|');
        }
        return hashMap;
    }

    private int adjustYear(int n) {
        int n2 = this.century + n;
        if (n >= this.startYear) {
            return n2;
        }
        return n2 + 100;
    }

    private Strategy getStrategy(char c2, int n, Calendar calendar) {
        switch (c2) {
            default: {
                throw new IllegalArgumentException("Format '" + c2 + "' not supported");
            }
            case 'D': {
                return DAY_OF_YEAR_STRATEGY;
            }
            case 'E': {
                return this.getLocaleSpecificStrategy(7, calendar);
            }
            case 'F': {
                return DAY_OF_WEEK_IN_MONTH_STRATEGY;
            }
            case 'G': {
                return this.getLocaleSpecificStrategy(0, calendar);
            }
            case 'H': {
                return HOUR_OF_DAY_STRATEGY;
            }
            case 'K': {
                return HOUR_STRATEGY;
            }
            case 'M': {
                if (n >= 3) {
                    return this.getLocaleSpecificStrategy(2, calendar);
                }
                return NUMBER_MONTH_STRATEGY;
            }
            case 'S': {
                return MILLISECOND_STRATEGY;
            }
            case 'W': {
                return WEEK_OF_MONTH_STRATEGY;
            }
            case 'a': {
                return this.getLocaleSpecificStrategy(9, calendar);
            }
            case 'd': {
                return DAY_OF_MONTH_STRATEGY;
            }
            case 'h': {
                return HOUR12_STRATEGY;
            }
            case 'k': {
                return HOUR24_OF_DAY_STRATEGY;
            }
            case 'm': {
                return MINUTE_STRATEGY;
            }
            case 's': {
                return SECOND_STRATEGY;
            }
            case 'u': {
                return DAY_OF_WEEK_STRATEGY;
            }
            case 'w': {
                return WEEK_OF_YEAR_STRATEGY;
            }
            case 'Y': 
            case 'y': {
                if (n > 2) {
                    return LITERAL_YEAR_STRATEGY;
                }
                return ABBREVIATED_YEAR_STRATEGY;
            }
            case 'X': {
                return ISO8601TimeZoneStrategy.getStrategy(n);
            }
            case 'Z': {
                if (n != 2) break;
                return ISO8601TimeZoneStrategy.ISO_8601_3_STRATEGY;
            }
            case 'z': 
        }
        return this.getLocaleSpecificStrategy(15, calendar);
    }

    private static ConcurrentMap<Locale, Strategy> getCache(int n) {
        ConcurrentMap<Locale, Strategy>[] concurrentMapArray = caches;
        synchronized (caches) {
            if (caches[n] == null) {
                FastDateParser.caches[n] = new ConcurrentHashMap<Locale, Strategy>(3);
            }
            // ** MonitorExit[var1_2] (shouldn't be in output)
            return caches[n];
        }
    }

    private Strategy getLocaleSpecificStrategy(int n, Calendar calendar) {
        Strategy strategy;
        ConcurrentMap<Locale, Strategy> concurrentMap = FastDateParser.getCache(n);
        Strategy strategy2 = (Strategy)concurrentMap.get(this.locale);
        if (strategy2 == null && (strategy = concurrentMap.putIfAbsent(this.locale, strategy2 = n == 15 ? new TimeZoneStrategy(this.locale) : new CaseInsensitiveTextStrategy(n, calendar, this.locale))) != null) {
            return strategy;
        }
        return strategy2;
    }

    static class ISO8601TimeZoneStrategy
    extends PatternStrategy {
        private static final Strategy ISO_8601_1_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}))");
        private static final Strategy ISO_8601_2_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}\\d{2}))");
        private static final Strategy ISO_8601_3_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}(?::)\\d{2}))");

        ISO8601TimeZoneStrategy(String string) {
            this.createPattern(string);
        }

        @Override
        void setCalendar(FastDateParser fastDateParser, Calendar calendar, String string) {
            if (string.equals("Z")) {
                calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
                return;
            }
            calendar.setTimeZone(TimeZone.getTimeZone("GMT".concat(String.valueOf(string))));
        }

        static Strategy getStrategy(int n) {
            switch (n) {
                case 1: {
                    return ISO_8601_1_STRATEGY;
                }
                case 2: {
                    return ISO_8601_2_STRATEGY;
                }
                case 3: {
                    return ISO_8601_3_STRATEGY;
                }
            }
            throw new IllegalArgumentException("invalid number of X");
        }
    }

    static class TimeZoneStrategy
    extends PatternStrategy {
        private static final String RFC_822_TIME_ZONE = "[+-]\\d{4}";
        private static final String GMT_OPTION = "GMT[+-]\\d{1,2}:\\d{2}";
        private final Locale locale;
        private final Map<String, TzInfo> tzNames = new HashMap<String, TzInfo>();
        private static final int ID = 0;

        TimeZoneStrategy(Locale locale) {
            this.locale = locale;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("((?iu)[+-]\\d{4}|GMT[+-]\\d{1,2}:\\d{2}");
            TreeSet<String> treeSet = new TreeSet<String>(LONGER_FIRST_LOWERCASE);
            for (String[] stringArray : DateFormatSymbols.getInstance(locale).getZoneStrings()) {
                TzInfo tzInfo;
                Object object = stringArray[0];
                if (((String)object).equalsIgnoreCase("GMT")) continue;
                object = TimeZone.getTimeZone((String)object);
                TzInfo tzInfo2 = tzInfo = new TzInfo((TimeZone)object, false);
                for (int j = 1; j < stringArray.length; ++j) {
                    switch (j) {
                        case 3: {
                            tzInfo2 = new TzInfo((TimeZone)object, true);
                            break;
                        }
                        case 5: {
                            tzInfo2 = tzInfo;
                        }
                    }
                    String string = stringArray[j].toLowerCase(locale);
                    if (!treeSet.add(string)) continue;
                    this.tzNames.put(string, tzInfo2);
                }
            }
            for (String string : treeSet) {
                FastDateParser.simpleQuote(stringBuilder.append('|'), string);
            }
            stringBuilder.append(")");
            this.createPattern(stringBuilder);
        }

        @Override
        void setCalendar(FastDateParser object, Calendar calendar, String string) {
            if (string.charAt(0) == '+' || string.charAt(0) == '-') {
                object = TimeZone.getTimeZone("GMT".concat(String.valueOf(string)));
                calendar.setTimeZone((TimeZone)object);
                return;
            }
            if (string.regionMatches(true, 0, "GMT", 0, 3)) {
                object = TimeZone.getTimeZone(string.toUpperCase());
                calendar.setTimeZone((TimeZone)object);
                return;
            }
            object = this.tzNames.get(string.toLowerCase(this.locale));
            calendar.set(16, ((TzInfo)object).dstOffset);
            calendar.set(15, ((TzInfo)object).zone.getRawOffset());
        }

        static class TzInfo {
            TimeZone zone;
            int dstOffset;

            TzInfo(TimeZone timeZone, boolean bl) {
                this.zone = timeZone;
                this.dstOffset = bl ? timeZone.getDSTSavings() : 0;
            }
        }
    }

    static class NumberStrategy
    extends Strategy {
        private final int field;

        NumberStrategy(int n) {
            this.field = n;
        }

        @Override
        boolean isNumber() {
            return true;
        }

        @Override
        boolean parse(FastDateParser fastDateParser, Calendar calendar, String string, ParsePosition parsePosition, int n) {
            int n2;
            int n3 = string.length();
            if (n == 0) {
                for (n2 = parsePosition.getIndex(); n2 < n3 && Character.isWhitespace(string.charAt(n2)); ++n2) {
                }
                parsePosition.setIndex(n2);
            } else if (n3 > (n = n2 + n)) {
                n3 = n;
            }
            while (n2 < n3 && Character.isDigit(string.charAt(n2))) {
                ++n2;
            }
            if (parsePosition.getIndex() == n2) {
                parsePosition.setErrorIndex(n2);
                return false;
            }
            n = Integer.parseInt(string.substring(parsePosition.getIndex(), n2));
            parsePosition.setIndex(n2);
            calendar.set(this.field, this.modify(fastDateParser, n));
            return true;
        }

        int modify(FastDateParser fastDateParser, int n) {
            return n;
        }
    }

    static class CaseInsensitiveTextStrategy
    extends PatternStrategy {
        private final int field;
        final Locale locale;
        private final Map<String, Integer> lKeyValues;

        CaseInsensitiveTextStrategy(int n, Calendar calendar, Locale locale) {
            this.field = n;
            this.locale = locale;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("((?iu)");
            this.lKeyValues = FastDateParser.appendDisplayNames(calendar, locale, n, stringBuilder);
            StringBuilder stringBuilder2 = stringBuilder;
            stringBuilder2.setLength(stringBuilder2.length() - 1);
            stringBuilder.append(")");
            this.createPattern(stringBuilder);
        }

        @Override
        void setCalendar(FastDateParser serializable, Calendar calendar, String string) {
            serializable = this.lKeyValues.get(string.toLowerCase(this.locale));
            calendar.set(this.field, (Integer)serializable);
        }
    }

    static class CopyQuotedStrategy
    extends Strategy {
        private final String formatField;

        CopyQuotedStrategy(String string) {
            this.formatField = string;
        }

        @Override
        boolean isNumber() {
            return false;
        }

        @Override
        boolean parse(FastDateParser fastDateParser, Calendar calendar, String string, ParsePosition parsePosition, int n) {
            for (int j = 0; j < this.formatField.length(); ++j) {
                int n2 = j + parsePosition.getIndex();
                if (n2 == string.length()) {
                    parsePosition.setErrorIndex(n2);
                    return false;
                }
                if (this.formatField.charAt(j) == string.charAt(n2)) continue;
                parsePosition.setErrorIndex(n2);
                return false;
            }
            parsePosition.setIndex(this.formatField.length() + parsePosition.getIndex());
            return true;
        }
    }

    static abstract class PatternStrategy
    extends Strategy {
        private Pattern pattern;

        private PatternStrategy() {
        }

        void createPattern(StringBuilder stringBuilder) {
            this.createPattern(stringBuilder.toString());
        }

        void createPattern(String string) {
            this.pattern = Pattern.compile(string);
        }

        @Override
        boolean isNumber() {
            return false;
        }

        @Override
        boolean parse(FastDateParser fastDateParser, Calendar calendar, String object, ParsePosition parsePosition, int n) {
            if (!((Matcher)(object = this.pattern.matcher(((String)object).substring(parsePosition.getIndex())))).lookingAt()) {
                ParsePosition parsePosition2 = parsePosition;
                parsePosition2.setErrorIndex(parsePosition2.getIndex());
                return false;
            }
            ParsePosition parsePosition3 = parsePosition;
            parsePosition3.setIndex(parsePosition3.getIndex() + ((Matcher)object).end(1));
            this.setCalendar(fastDateParser, calendar, ((Matcher)object).group(1));
            return true;
        }

        abstract void setCalendar(FastDateParser var1, Calendar var2, String var3);
    }

    static abstract class Strategy {
        private Strategy() {
        }

        boolean isNumber() {
            return false;
        }

        abstract boolean parse(FastDateParser var1, Calendar var2, String var3, ParsePosition var4, int var5);
    }

    class StrategyParser {
        private final String pattern;
        private final Calendar definingCalendar;
        private int currentIdx;

        StrategyParser(String string, Calendar calendar) {
            this.pattern = string;
            this.definingCalendar = calendar;
        }

        StrategyAndWidth getNextStrategy() {
            if (this.currentIdx >= this.pattern.length()) {
                return null;
            }
            char c2 = this.pattern.charAt(this.currentIdx);
            if (FastDateParser.isFormatLetter(c2)) {
                return this.letterPattern(c2);
            }
            return this.literal();
        }

        private StrategyAndWidth letterPattern(char c2) {
            int n = this.currentIdx;
            while (++this.currentIdx < this.pattern.length() && this.pattern.charAt(this.currentIdx) == c2) {
            }
            n = this.currentIdx - n;
            return new StrategyAndWidth(FastDateParser.this.getStrategy(c2, n, this.definingCalendar), n);
        }

        private StrategyAndWidth literal() {
            boolean bl = false;
            StringBuilder stringBuilder = new StringBuilder();
            while (this.currentIdx < this.pattern.length()) {
                char c2 = this.pattern.charAt(this.currentIdx);
                if (!bl && FastDateParser.isFormatLetter(c2)) break;
                if (c2 == '\'' && (++this.currentIdx == this.pattern.length() || this.pattern.charAt(this.currentIdx) != '\'')) {
                    bl = !bl;
                    continue;
                }
                ++this.currentIdx;
                stringBuilder.append(c2);
            }
            if (bl) {
                throw new IllegalArgumentException("Unterminated quote");
            }
            String string = stringBuilder.toString();
            return new StrategyAndWidth(new CopyQuotedStrategy(string), string.length());
        }
    }

    static class StrategyAndWidth {
        final Strategy strategy;
        final int width;

        StrategyAndWidth(Strategy strategy, int n) {
            this.strategy = strategy;
            this.width = n;
        }

        int getMaxWidth(ListIterator<StrategyAndWidth> listIterator) {
            if (!this.strategy.isNumber() || !listIterator.hasNext()) {
                return 0;
            }
            Strategy strategy = listIterator.next().strategy;
            listIterator.previous();
            if (strategy.isNumber()) {
                return this.width;
            }
            return 0;
        }
    }
}

