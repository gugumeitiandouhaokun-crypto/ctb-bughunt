/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

final class CharRange
implements Serializable,
Iterable<Character> {
    private static final long serialVersionUID = 8270183163158333422L;
    private final char start;
    private final char end;
    private final boolean negated;
    private transient String iToString;

    private CharRange(char c2, char c3, boolean bl) {
        if (c2 > c3) {
            char c4 = c2;
            c2 = c3;
            c3 = c4;
        }
        this.start = c2;
        this.end = c3;
        this.negated = bl;
    }

    public static CharRange is(char c2) {
        char c3 = c2;
        return new CharRange(c3, c3, false);
    }

    public static CharRange isNot(char c2) {
        char c3 = c2;
        return new CharRange(c3, c3, true);
    }

    public static CharRange isIn(char c2, char c3) {
        return new CharRange(c2, c3, false);
    }

    public static CharRange isNotIn(char c2, char c3) {
        return new CharRange(c2, c3, true);
    }

    public final char getStart() {
        return this.start;
    }

    public final char getEnd() {
        return this.end;
    }

    public final boolean isNegated() {
        return this.negated;
    }

    public final boolean contains(char c2) {
        return (c2 >= this.start && c2 <= this.end) != this.negated;
    }

    public final boolean contains(CharRange charRange) {
        if (charRange == null) {
            throw new IllegalArgumentException("The Range must not be null");
        }
        if (this.negated) {
            if (charRange.negated) {
                return this.start >= charRange.start && this.end <= charRange.end;
            }
            return charRange.end < this.start || charRange.start > this.end;
        }
        if (charRange.negated) {
            return this.start == '\u0000' && this.end == '\uffff';
        }
        return this.start <= charRange.start && this.end >= charRange.end;
    }

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof CharRange)) {
            return false;
        }
        object = (CharRange)object;
        return this.start == ((CharRange)object).start && this.end == ((CharRange)object).end && this.negated == ((CharRange)object).negated;
    }

    public final int hashCode() {
        return 83 + this.start + 7 * this.end + (this.negated ? 1 : 0);
    }

    public final String toString() {
        if (this.iToString == null) {
            StringBuilder stringBuilder = new StringBuilder(4);
            if (this.isNegated()) {
                stringBuilder.append('^');
            }
            stringBuilder.append(this.start);
            if (this.start != this.end) {
                stringBuilder.append('-');
                stringBuilder.append(this.end);
            }
            this.iToString = stringBuilder.toString();
        }
        return this.iToString;
    }

    @Override
    public final Iterator<Character> iterator() {
        return new CharacterIterator(this);
    }

    static class CharacterIterator
    implements Iterator<Character> {
        private char current;
        private final CharRange range;
        private boolean hasNext;

        private CharacterIterator(CharRange charRange) {
            this.range = charRange;
            this.hasNext = true;
            if (this.range.negated) {
                if (this.range.start == '\u0000') {
                    if (this.range.end == '\uffff') {
                        this.hasNext = false;
                        return;
                    }
                    this.current = (char)(this.range.end + '\u0001');
                    return;
                }
                this.current = '\u0000';
                return;
            }
            this.current = this.range.start;
        }

        /*
         * Enabled aggressive block sorting
         */
        private void prepareNext() {
            if (this.range.negated) {
                if (this.current != '\uffff') {
                    if (this.current + '\u0001' != this.range.start) {
                        this.current = (char)(this.current + '\u0001');
                        return;
                    }
                    if (this.range.end != '\uffff') {
                        this.current = (char)(this.range.end + '\u0001');
                        return;
                    }
                }
            } else if (this.current < this.range.end) {
                this.current = (char)(this.current + '\u0001');
                return;
            }
            this.hasNext = false;
        }

        @Override
        public boolean hasNext() {
            return this.hasNext;
        }

        @Override
        public Character next() {
            if (!this.hasNext) {
                throw new NoSuchElementException();
            }
            char c2 = this.current;
            this.prepareNext();
            return Character.valueOf(c2);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

