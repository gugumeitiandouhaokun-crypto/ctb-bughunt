/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.util.PerformanceSensitive;

@PerformanceSensitive(value={"allocation"})
public final class FormattingInfo {
    private static final char[] SPACES = new char[]{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
    private static final char[] ZEROS = new char[]{'0', '0', '0', '0', '0', '0', '0', '0'};
    private static final FormattingInfo DEFAULT = new FormattingInfo(false, 0, Integer.MAX_VALUE, true);
    private final int minLength;
    private final int maxLength;
    private final boolean leftAlign;
    private final boolean leftTruncate;
    private final boolean zeroPad;

    public FormattingInfo(boolean bl, int n, int n2, boolean bl2) {
        this(bl, n, n2, bl2, false);
    }

    public FormattingInfo(boolean bl, int n, int n2, boolean bl2, boolean bl3) {
        this.leftAlign = bl;
        this.minLength = n;
        this.maxLength = n2;
        this.leftTruncate = bl2;
        this.zeroPad = bl3;
    }

    public static FormattingInfo getDefault() {
        return DEFAULT;
    }

    public final boolean isLeftAligned() {
        return this.leftAlign;
    }

    public final boolean isLeftTruncate() {
        return this.leftTruncate;
    }

    public final boolean isZeroPad() {
        return this.zeroPad;
    }

    public final int getMinLength() {
        return this.minLength;
    }

    public final int getMaxLength() {
        return this.maxLength;
    }

    public final void format(int n, StringBuilder stringBuilder) {
        int n2 = stringBuilder.length() - n;
        if (n2 > this.maxLength) {
            if (this.leftTruncate) {
                stringBuilder.delete(n, stringBuilder.length() - this.maxLength);
                return;
            }
            stringBuilder.delete(n + this.maxLength, n + stringBuilder.length());
            return;
        }
        if (n2 < this.minLength) {
            char[] cArray;
            if (this.leftAlign) {
                n2 = stringBuilder.length();
                stringBuilder.setLength(n + this.minLength);
                for (int j = n2; j < stringBuilder.length(); ++j) {
                    stringBuilder.setCharAt(j, ' ');
                }
                return;
            }
            char[] cArray2 = cArray = this.zeroPad ? ZEROS : SPACES;
            for (n2 = this.minLength - n2; n2 > cArray.length; n2 -= cArray.length) {
                stringBuilder.insert(n, cArray);
            }
            stringBuilder.insert(n, cArray, 0, n2);
        }
    }

    public final String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.toString());
        stringBuilder.append("[leftAlign=");
        stringBuilder.append(this.leftAlign);
        stringBuilder.append(", maxLength=");
        stringBuilder.append(this.maxLength);
        stringBuilder.append(", minLength=");
        stringBuilder.append(this.minLength);
        stringBuilder.append(", leftTruncate=");
        stringBuilder.append(this.leftTruncate);
        stringBuilder.append(", zeroPad=");
        stringBuilder.append(this.zeroPad);
        stringBuilder.append(']');
        return stringBuilder.toString();
    }
}

