/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.io.Serializable;
import java.util.Comparator;

public final class Range<T>
implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Comparator<T> comparator;
    private final T minimum;
    private final T maximum;
    private transient int hashCode;
    private transient String toString;

    public static <T extends Comparable<T>> Range<T> is(T t) {
        T t2 = t;
        return Range.between(t2, t2, null);
    }

    public static <T> Range<T> is(T t, Comparator<T> comparator) {
        T t2 = t;
        return Range.between(t2, t2, comparator);
    }

    public static <T extends Comparable<T>> Range<T> between(T t, T t2) {
        return Range.between(t, t2, null);
    }

    public static <T> Range<T> between(T t, T t2, Comparator<T> comparator) {
        return new Range<T>(t, t2, comparator);
    }

    private Range(T t, T t2, Comparator<T> comparator) {
        if (t == null || t2 == null) {
            throw new IllegalArgumentException("Elements in a range must not be null: element1=" + t + ", element2=" + t2);
        }
        this.comparator = comparator == null ? ComparableComparator.INSTANCE : comparator;
        if (this.comparator.compare(t, t2) <= 0) {
            this.minimum = t;
            this.maximum = t2;
            return;
        }
        this.minimum = t2;
        this.maximum = t;
    }

    public final T getMinimum() {
        return this.minimum;
    }

    public final T getMaximum() {
        return this.maximum;
    }

    public final Comparator<T> getComparator() {
        return this.comparator;
    }

    public final boolean isNaturalOrdering() {
        return this.comparator == ComparableComparator.INSTANCE;
    }

    public final boolean contains(T t) {
        if (t == null) {
            return false;
        }
        return this.comparator.compare(t, this.minimum) >= 0 && this.comparator.compare(t, this.maximum) <= 0;
    }

    public final boolean isAfter(T t) {
        if (t == null) {
            return false;
        }
        return this.comparator.compare(t, this.minimum) < 0;
    }

    public final boolean isStartedBy(T t) {
        if (t == null) {
            return false;
        }
        return this.comparator.compare(t, this.minimum) == 0;
    }

    public final boolean isEndedBy(T t) {
        if (t == null) {
            return false;
        }
        return this.comparator.compare(t, this.maximum) == 0;
    }

    public final boolean isBefore(T t) {
        if (t == null) {
            return false;
        }
        return this.comparator.compare(t, this.maximum) > 0;
    }

    public final int elementCompareTo(T t) {
        if (t == null) {
            throw new NullPointerException("Element is null");
        }
        if (this.isAfter(t)) {
            return -1;
        }
        if (this.isBefore(t)) {
            return 1;
        }
        return 0;
    }

    public final boolean containsRange(Range<T> range) {
        if (range == null) {
            return false;
        }
        return this.contains(range.minimum) && this.contains(range.maximum);
    }

    public final boolean isAfterRange(Range<T> range) {
        if (range == null) {
            return false;
        }
        return this.isAfter(range.maximum);
    }

    public final boolean isOverlappedBy(Range<T> range) {
        if (range == null) {
            return false;
        }
        return range.contains(this.minimum) || range.contains(this.maximum) || this.contains(range.minimum);
    }

    public final boolean isBeforeRange(Range<T> range) {
        if (range == null) {
            return false;
        }
        return this.isBefore(range.minimum);
    }

    public final Range<T> intersectionWith(Range<T> range) {
        if (!this.isOverlappedBy(range)) {
            throw new IllegalArgumentException(String.format("Cannot calculate intersection with non-overlapping range %s", range));
        }
        if (this.equals(range)) {
            return this;
        }
        T t = this.getComparator().compare(this.minimum, range.minimum) < 0 ? range.minimum : this.minimum;
        range = this.getComparator().compare(this.maximum, range.maximum) < 0 ? this.maximum : range.maximum;
        return Range.between(t, range, this.getComparator());
    }

    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object == null || object.getClass() != this.getClass()) {
            return false;
        }
        object = (Range)object;
        return this.minimum.equals(((Range)object).minimum) && this.maximum.equals(((Range)object).maximum);
    }

    public final int hashCode() {
        int n = this.hashCode;
        if (this.hashCode == 0) {
            n = 629 + this.getClass().hashCode();
            n = 37 * n + this.minimum.hashCode();
            this.hashCode = n = 37 * n + this.maximum.hashCode();
        }
        return n;
    }

    public final String toString() {
        if (this.toString == null) {
            this.toString = "[" + this.minimum + ".." + this.maximum + "]";
        }
        return this.toString;
    }

    public final String toString(String string) {
        return String.format(string, this.minimum, this.maximum, this.comparator);
    }

    static enum ComparableComparator implements Comparator
    {
        INSTANCE;


        public final int compare(Object object, Object object2) {
            return ((Comparable)object).compareTo(object2);
        }
    }
}

