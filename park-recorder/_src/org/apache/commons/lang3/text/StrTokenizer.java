/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.apache.commons.lang3.text.StrMatcher;

public class StrTokenizer
implements Cloneable,
ListIterator<String> {
    private static final StrTokenizer CSV_TOKENIZER_PROTOTYPE = new StrTokenizer();
    private static final StrTokenizer TSV_TOKENIZER_PROTOTYPE;
    private char[] chars;
    private String[] tokens;
    private int tokenPos;
    private StrMatcher delimMatcher = StrMatcher.splitMatcher();
    private StrMatcher quoteMatcher = StrMatcher.noneMatcher();
    private StrMatcher ignoredMatcher = StrMatcher.noneMatcher();
    private StrMatcher trimmerMatcher = StrMatcher.noneMatcher();
    private boolean emptyAsNull = false;
    private boolean ignoreEmptyTokens = true;

    private static StrTokenizer getCSVClone() {
        return (StrTokenizer)CSV_TOKENIZER_PROTOTYPE.clone();
    }

    public static StrTokenizer getCSVInstance() {
        return StrTokenizer.getCSVClone();
    }

    public static StrTokenizer getCSVInstance(String string) {
        StrTokenizer strTokenizer = StrTokenizer.getCSVClone();
        strTokenizer.reset(string);
        return strTokenizer;
    }

    public static StrTokenizer getCSVInstance(char[] cArray) {
        StrTokenizer strTokenizer = StrTokenizer.getCSVClone();
        strTokenizer.reset(cArray);
        return strTokenizer;
    }

    private static StrTokenizer getTSVClone() {
        return (StrTokenizer)TSV_TOKENIZER_PROTOTYPE.clone();
    }

    public static StrTokenizer getTSVInstance() {
        return StrTokenizer.getTSVClone();
    }

    public static StrTokenizer getTSVInstance(String string) {
        StrTokenizer strTokenizer = StrTokenizer.getTSVClone();
        strTokenizer.reset(string);
        return strTokenizer;
    }

    public static StrTokenizer getTSVInstance(char[] cArray) {
        StrTokenizer strTokenizer = StrTokenizer.getTSVClone();
        strTokenizer.reset(cArray);
        return strTokenizer;
    }

    public StrTokenizer() {
        this.chars = null;
    }

    public StrTokenizer(String string) {
        if (string != null) {
            this.chars = string.toCharArray();
            return;
        }
        this.chars = null;
    }

    public StrTokenizer(String string, char c2) {
        this(string);
        this.setDelimiterChar(c2);
    }

    public StrTokenizer(String string, String string2) {
        this(string);
        this.setDelimiterString(string2);
    }

    public StrTokenizer(String string, StrMatcher strMatcher) {
        this(string);
        this.setDelimiterMatcher(strMatcher);
    }

    public StrTokenizer(String string, char c2, char c3) {
        this(string, c2);
        this.setQuoteChar(c3);
    }

    public StrTokenizer(String string, StrMatcher strMatcher, StrMatcher strMatcher2) {
        this(string, strMatcher);
        this.setQuoteMatcher(strMatcher2);
    }

    public StrTokenizer(char[] cArray) {
        this.chars = ArrayUtils.clone(cArray);
    }

    public StrTokenizer(char[] cArray, char c2) {
        this(cArray);
        this.setDelimiterChar(c2);
    }

    public StrTokenizer(char[] cArray, String string) {
        this(cArray);
        this.setDelimiterString(string);
    }

    public StrTokenizer(char[] cArray, StrMatcher strMatcher) {
        this(cArray);
        this.setDelimiterMatcher(strMatcher);
    }

    public StrTokenizer(char[] cArray, char c2, char c3) {
        this(cArray, c2);
        this.setQuoteChar(c3);
    }

    public StrTokenizer(char[] cArray, StrMatcher strMatcher, StrMatcher strMatcher2) {
        this(cArray, strMatcher);
        this.setQuoteMatcher(strMatcher2);
    }

    public int size() {
        this.checkTokenized();
        return this.tokens.length;
    }

    public String nextToken() {
        if (this.hasNext()) {
            return this.tokens[this.tokenPos++];
        }
        return null;
    }

    public String previousToken() {
        if (this.hasPrevious()) {
            return this.tokens[--this.tokenPos];
        }
        return null;
    }

    public String[] getTokenArray() {
        this.checkTokenized();
        return (String[])this.tokens.clone();
    }

    public List<String> getTokenList() {
        this.checkTokenized();
        ArrayList<String> arrayList = new ArrayList<String>(this.tokens.length);
        String[] stringArray = this.tokens;
        int n = this.tokens.length;
        for (int j = 0; j < n; ++j) {
            String string = stringArray[j];
            arrayList.add(string);
        }
        return arrayList;
    }

    public StrTokenizer reset() {
        this.tokenPos = 0;
        this.tokens = null;
        return this;
    }

    public StrTokenizer reset(String string) {
        this.reset();
        this.chars = (char[])(string != null ? string.toCharArray() : null);
        return this;
    }

    public StrTokenizer reset(char[] cArray) {
        this.reset();
        this.chars = ArrayUtils.clone(cArray);
        return this;
    }

    @Override
    public boolean hasNext() {
        this.checkTokenized();
        return this.tokenPos < this.tokens.length;
    }

    @Override
    public String next() {
        if (this.hasNext()) {
            return this.tokens[this.tokenPos++];
        }
        throw new NoSuchElementException();
    }

    @Override
    public int nextIndex() {
        return this.tokenPos;
    }

    @Override
    public boolean hasPrevious() {
        this.checkTokenized();
        return this.tokenPos > 0;
    }

    @Override
    public String previous() {
        if (this.hasPrevious()) {
            return this.tokens[--this.tokenPos];
        }
        throw new NoSuchElementException();
    }

    @Override
    public int previousIndex() {
        return this.tokenPos - 1;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() is unsupported");
    }

    @Override
    public void set(String string) {
        throw new UnsupportedOperationException("set() is unsupported");
    }

    @Override
    public void add(String string) {
        throw new UnsupportedOperationException("add() is unsupported");
    }

    private void checkTokenized() {
        if (this.tokens == null) {
            List<String> list;
            if (this.chars == null) {
                List<String> list2;
                List<String> list3 = list2 = this.tokenize(null, 0, 0);
                this.tokens = list3.toArray(new String[list3.size()]);
                return;
            }
            StrTokenizer strTokenizer = this;
            List<String> list4 = list = strTokenizer.tokenize(strTokenizer.chars, 0, this.chars.length);
            this.tokens = list4.toArray(new String[list4.size()]);
        }
    }

    protected List<String> tokenize(char[] cArray, int n, int n2) {
        if (cArray == null || n2 == 0) {
            return Collections.emptyList();
        }
        StrBuilder strBuilder = new StrBuilder();
        ArrayList<String> arrayList = new ArrayList<String>();
        while (n >= 0 && n < n2) {
            if ((n = this.readNextToken(cArray, n, n2, strBuilder, arrayList)) < n2) continue;
            this.addToken(arrayList, "");
        }
        return arrayList;
    }

    private void addToken(List<String> list, String string) {
        if (StringUtils.isEmpty(string)) {
            if (this.isIgnoreEmptyTokens()) {
                return;
            }
            if (this.isEmptyTokenAsNull()) {
                string = null;
            }
        }
        list.add(string);
    }

    private int readNextToken(char[] cArray, int n, int n2, StrBuilder strBuilder, List<String> list) {
        int n3;
        while (n < n2) {
            int n4 = n;
            int n5 = n;
            n3 = Math.max(this.getIgnoredMatcher().isMatch(cArray, n4, n4, n2), this.getTrimmerMatcher().isMatch(cArray, n5, n5, n2));
            if (n3 == 0) break;
            int n6 = n;
            if (this.getDelimiterMatcher().isMatch(cArray, n6, n6, n2) > 0) break;
            int n7 = n;
            if (this.getQuoteMatcher().isMatch(cArray, n7, n7, n2) > 0) break;
            n += n3;
        }
        if (n >= n2) {
            this.addToken(list, "");
            return -1;
        }
        int n8 = n;
        n3 = this.getDelimiterMatcher().isMatch(cArray, n8, n8, n2);
        if (n3 > 0) {
            this.addToken(list, "");
            return n + n3;
        }
        int n9 = n;
        n3 = this.getQuoteMatcher().isMatch(cArray, n9, n9, n2);
        if (n3 > 0) {
            return this.readWithQuotes(cArray, n + n3, n2, strBuilder, list, n, n3);
        }
        return this.readWithQuotes(cArray, n, n2, strBuilder, list, 0, 0);
    }

    private int readWithQuotes(char[] cArray, int n, int n2, StrBuilder strBuilder, List<String> list, int n3, int n4) {
        strBuilder.clear();
        int n5 = n;
        boolean bl = n4 > 0;
        int n6 = 0;
        while (n5 < n2) {
            if (bl) {
                if (this.isQuote(cArray, n5, n2, n3, n4)) {
                    if (this.isQuote(cArray, n5 + n4, n2, n3, n4)) {
                        strBuilder.append(cArray, n5, n4);
                        n5 += n4 * 2;
                        n6 = strBuilder.size();
                        continue;
                    }
                    bl = false;
                    n5 += n4;
                    continue;
                }
                strBuilder.append(cArray[n5++]);
                n6 = strBuilder.size();
                continue;
            }
            int n7 = this.getDelimiterMatcher().isMatch(cArray, n5, n, n2);
            if (n7 > 0) {
                this.addToken(list, strBuilder.substring(0, n6));
                return n5 + n7;
            }
            if (n4 > 0 && this.isQuote(cArray, n5, n2, n3, n4)) {
                bl = true;
                n5 += n4;
                continue;
            }
            n7 = this.getIgnoredMatcher().isMatch(cArray, n5, n, n2);
            if (n7 > 0) {
                n5 += n7;
                continue;
            }
            n7 = this.getTrimmerMatcher().isMatch(cArray, n5, n, n2);
            if (n7 > 0) {
                strBuilder.append(cArray, n5, n7);
                n5 += n7;
                continue;
            }
            strBuilder.append(cArray[n5++]);
            n6 = strBuilder.size();
        }
        this.addToken(list, strBuilder.substring(0, n6));
        return -1;
    }

    private boolean isQuote(char[] cArray, int n, int n2, int n3, int n4) {
        for (int j = 0; j < n4; ++j) {
            if (n + j < n2 && cArray[n + j] == cArray[n3 + j]) continue;
            return false;
        }
        return true;
    }

    public StrMatcher getDelimiterMatcher() {
        return this.delimMatcher;
    }

    public StrTokenizer setDelimiterMatcher(StrMatcher strMatcher) {
        this.delimMatcher = strMatcher == null ? StrMatcher.noneMatcher() : strMatcher;
        return this;
    }

    public StrTokenizer setDelimiterChar(char c2) {
        return this.setDelimiterMatcher(StrMatcher.charMatcher(c2));
    }

    public StrTokenizer setDelimiterString(String string) {
        return this.setDelimiterMatcher(StrMatcher.stringMatcher(string));
    }

    public StrMatcher getQuoteMatcher() {
        return this.quoteMatcher;
    }

    public StrTokenizer setQuoteMatcher(StrMatcher strMatcher) {
        if (strMatcher != null) {
            this.quoteMatcher = strMatcher;
        }
        return this;
    }

    public StrTokenizer setQuoteChar(char c2) {
        return this.setQuoteMatcher(StrMatcher.charMatcher(c2));
    }

    public StrMatcher getIgnoredMatcher() {
        return this.ignoredMatcher;
    }

    public StrTokenizer setIgnoredMatcher(StrMatcher strMatcher) {
        if (strMatcher != null) {
            this.ignoredMatcher = strMatcher;
        }
        return this;
    }

    public StrTokenizer setIgnoredChar(char c2) {
        return this.setIgnoredMatcher(StrMatcher.charMatcher(c2));
    }

    public StrMatcher getTrimmerMatcher() {
        return this.trimmerMatcher;
    }

    public StrTokenizer setTrimmerMatcher(StrMatcher strMatcher) {
        if (strMatcher != null) {
            this.trimmerMatcher = strMatcher;
        }
        return this;
    }

    public boolean isEmptyTokenAsNull() {
        return this.emptyAsNull;
    }

    public StrTokenizer setEmptyTokenAsNull(boolean bl) {
        this.emptyAsNull = bl;
        return this;
    }

    public boolean isIgnoreEmptyTokens() {
        return this.ignoreEmptyTokens;
    }

    public StrTokenizer setIgnoreEmptyTokens(boolean bl) {
        this.ignoreEmptyTokens = bl;
        return this;
    }

    public String getContent() {
        if (this.chars == null) {
            return null;
        }
        return new String(this.chars);
    }

    public Object clone() {
        try {
            return this.cloneReset();
        }
        catch (CloneNotSupportedException cloneNotSupportedException) {
            return null;
        }
    }

    Object cloneReset() {
        StrTokenizer strTokenizer = (StrTokenizer)super.clone();
        if (strTokenizer.chars != null) {
            strTokenizer.chars = (char[])strTokenizer.chars.clone();
        }
        strTokenizer.reset();
        return strTokenizer;
    }

    public String toString() {
        if (this.tokens == null) {
            return "StrTokenizer[not tokenized yet]";
        }
        return "StrTokenizer" + this.getTokenList();
    }

    static {
        CSV_TOKENIZER_PROTOTYPE.setDelimiterMatcher(StrMatcher.commaMatcher());
        CSV_TOKENIZER_PROTOTYPE.setQuoteMatcher(StrMatcher.doubleQuoteMatcher());
        CSV_TOKENIZER_PROTOTYPE.setIgnoredMatcher(StrMatcher.noneMatcher());
        CSV_TOKENIZER_PROTOTYPE.setTrimmerMatcher(StrMatcher.trimMatcher());
        CSV_TOKENIZER_PROTOTYPE.setEmptyTokenAsNull(false);
        CSV_TOKENIZER_PROTOTYPE.setIgnoreEmptyTokens(false);
        TSV_TOKENIZER_PROTOTYPE = new StrTokenizer();
        TSV_TOKENIZER_PROTOTYPE.setDelimiterMatcher(StrMatcher.tabMatcher());
        TSV_TOKENIZER_PROTOTYPE.setQuoteMatcher(StrMatcher.doubleQuoteMatcher());
        TSV_TOKENIZER_PROTOTYPE.setIgnoredMatcher(StrMatcher.noneMatcher());
        TSV_TOKENIZER_PROTOTYPE.setTrimmerMatcher(StrMatcher.trimMatcher());
        TSV_TOKENIZER_PROTOTYPE.setEmptyTokenAsNull(false);
        TSV_TOKENIZER_PROTOTYPE.setIgnoreEmptyTokens(false);
    }
}

