/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.builder;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DiffResult
implements Iterable<Diff<?>> {
    public static final String OBJECTS_SAME_STRING = "";
    private static final String DIFFERS_STRING = "differs from";
    private final List<Diff<?>> diffs;
    private final Object lhs;
    private final Object rhs;
    private final ToStringStyle style;

    DiffResult(Object object, Object object2, List<Diff<?>> list, ToStringStyle toStringStyle) {
        if (object == null) {
            throw new IllegalArgumentException("Left hand object cannot be null");
        }
        if (object2 == null) {
            throw new IllegalArgumentException("Right hand object cannot be null");
        }
        if (list == null) {
            throw new IllegalArgumentException("List of differences cannot be null");
        }
        this.diffs = list;
        this.lhs = object;
        this.rhs = object2;
        if (toStringStyle == null) {
            this.style = ToStringStyle.DEFAULT_STYLE;
            return;
        }
        this.style = toStringStyle;
    }

    public List<Diff<?>> getDiffs() {
        return Collections.unmodifiableList(this.diffs);
    }

    public int getNumberOfDiffs() {
        return this.diffs.size();
    }

    public ToStringStyle getToStringStyle() {
        return this.style;
    }

    public String toString() {
        DiffResult diffResult = this;
        return diffResult.toString(diffResult.style);
    }

    public String toString(ToStringStyle object) {
        if (this.diffs.size() == 0) {
            return OBJECTS_SAME_STRING;
        }
        ToStringBuilder toStringBuilder = new ToStringBuilder(this.lhs, (ToStringStyle)object);
        object = new ToStringBuilder(this.rhs, (ToStringStyle)object);
        for (Diff<?> diff : this.diffs) {
            toStringBuilder.append(diff.getFieldName(), diff.getLeft());
            ((ToStringBuilder)object).append(diff.getFieldName(), diff.getRight());
        }
        return String.format("%s %s %s", toStringBuilder.build(), DIFFERS_STRING, ((ToStringBuilder)object).build());
    }

    @Override
    public Iterator<Diff<?>> iterator() {
        return this.diffs.iterator();
    }
}

