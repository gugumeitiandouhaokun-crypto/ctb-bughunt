/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.CharRange;

public class CharSet
implements Serializable {
    private static final long serialVersionUID = 5947847346149275958L;
    public static final CharSet EMPTY = new CharSet(new String[]{null});
    public static final CharSet ASCII_ALPHA = new CharSet("a-zA-Z");
    public static final CharSet ASCII_ALPHA_LOWER = new CharSet("a-z");
    public static final CharSet ASCII_ALPHA_UPPER = new CharSet("A-Z");
    public static final CharSet ASCII_NUMERIC = new CharSet("0-9");
    protected static final Map<String, CharSet> COMMON = Collections.synchronizedMap(new HashMap());
    private final Set<CharRange> set = Collections.synchronizedSet(new HashSet());

    public static CharSet getInstance(String ... stringArray) {
        CharSet charSet;
        if (stringArray == null) {
            return null;
        }
        if (stringArray.length == 1 && (charSet = COMMON.get(stringArray[0])) != null) {
            return charSet;
        }
        return new CharSet(stringArray);
    }

    protected CharSet(String ... stringArray) {
        int n = stringArray.length;
        for (int j = 0; j < n; ++j) {
            this.add(stringArray[j]);
        }
    }

    protected void add(String string) {
        if (string == null) {
            return;
        }
        int n = string.length();
        int n2 = 0;
        while (n2 < n) {
            int n3 = n - n2;
            if (n3 >= 4 && string.charAt(n2) == '^' && string.charAt(n2 + 2) == '-') {
                this.set.add(CharRange.isNotIn(string.charAt(n2 + 1), string.charAt(n2 + 3)));
                n2 += 4;
                continue;
            }
            if (n3 >= 3 && string.charAt(n2 + 1) == '-') {
                this.set.add(CharRange.isIn(string.charAt(n2), string.charAt(n2 + 2)));
                n2 += 3;
                continue;
            }
            if (n3 >= 2 && string.charAt(n2) == '^') {
                this.set.add(CharRange.isNot(string.charAt(n2 + 1)));
                n2 += 2;
                continue;
            }
            this.set.add(CharRange.is(string.charAt(n2)));
            ++n2;
        }
    }

    CharRange[] getCharRanges() {
        return this.set.toArray(new CharRange[this.set.size()]);
    }

    public boolean contains(char c2) {
        Iterator<CharRange> iterator = this.set.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().contains(c2)) continue;
            return true;
        }
        return false;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof CharSet)) {
            return false;
        }
        object = (CharSet)object;
        return this.set.equals(((CharSet)object).set);
    }

    public int hashCode() {
        return 89 + this.set.hashCode();
    }

    public String toString() {
        return this.set.toString();
    }

    static {
        COMMON.put(null, EMPTY);
        COMMON.put("", EMPTY);
        COMMON.put("a-zA-Z", ASCII_ALPHA);
        COMMON.put("A-Za-z", ASCII_ALPHA);
        COMMON.put("a-z", ASCII_ALPHA_LOWER);
        COMMON.put("A-Z", ASCII_ALPHA_UPPER);
        COMMON.put("0-9", ASCII_NUMERIC);
    }
}

