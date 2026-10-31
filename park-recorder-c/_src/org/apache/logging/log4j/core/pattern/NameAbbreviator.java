/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.util.PerformanceSensitive;

@PerformanceSensitive(value={"allocation"})
public abstract class NameAbbreviator {
    private static final NameAbbreviator DEFAULT = new NOPAbbreviator();

    public static NameAbbreviator getAbbreviator(String string) {
        if (string.length() > 0) {
            int n;
            String string2;
            boolean bl;
            if ((string = string.trim()).isEmpty()) {
                return DEFAULT;
            }
            if (string.length() > 1 && string.charAt(0) == '-') {
                bl = true;
                string2 = string.substring(1);
            } else {
                bl = false;
                string2 = string;
            }
            for (n = 0; n < string2.length() && string2.charAt(n) >= '0' && string2.charAt(n) <= '9'; ++n) {
            }
            if (n == string2.length()) {
                return new MaxElementAbbreviator(Integer.parseInt(string2), bl ? MaxElementAbbreviator.Strategy.DROP : MaxElementAbbreviator.Strategy.RETAIN);
            }
            ArrayList<PatternAbbreviatorFragment> arrayList = new ArrayList<PatternAbbreviatorFragment>(5);
            for (int i2 = 0; i2 < string.length() && i2 >= 0; ++i2) {
                int n2 = i2;
                if (string.charAt(i2) == '*') {
                    n = Integer.MAX_VALUE;
                    ++n2;
                } else if (string.charAt(i2) >= '0' && string.charAt(i2) <= '9') {
                    n = string.charAt(i2) - 48;
                    ++n2;
                } else {
                    n = 0;
                }
                char c2 = '\u0000';
                if (n2 < string.length() && (c2 = string.charAt(n2)) == '.') {
                    c2 = '\u0000';
                }
                arrayList.add(new PatternAbbreviatorFragment(n, c2));
                i2 = string.indexOf(46, i2);
                if (i2 == -1) break;
            }
            return new PatternAbbreviator(arrayList);
        }
        return DEFAULT;
    }

    public static NameAbbreviator getDefaultAbbreviator() {
        return DEFAULT;
    }

    public abstract void abbreviate(String var1, StringBuilder var2);

    static class PatternAbbreviator
    extends NameAbbreviator {
        private final PatternAbbreviatorFragment[] fragments;

        public PatternAbbreviator(List<PatternAbbreviatorFragment> list) {
            if (list.isEmpty()) {
                throw new IllegalArgumentException("fragments must have at least one element");
            }
            this.fragments = new PatternAbbreviatorFragment[list.size()];
            list.toArray(this.fragments);
        }

        @Override
        public void abbreviate(String string, StringBuilder stringBuilder) {
            int n = stringBuilder.length();
            int n2 = n + string.length();
            stringBuilder.append(string);
            int n3 = 0;
            while (n < n2 && n >= 0) {
                n = this.fragments[n3].abbreviate(stringBuilder, n);
                if (n3 >= this.fragments.length - 1) continue;
                ++n3;
            }
        }
    }

    static class PatternAbbreviatorFragment {
        private final int charCount;
        private final char ellipsis;

        public PatternAbbreviatorFragment(int n, char c2) {
            this.charCount = n;
            this.ellipsis = c2;
        }

        public int abbreviate(StringBuilder stringBuilder, int n) {
            int n2 = stringBuilder.length();
            int n3 = -1;
            for (int i2 = n < 0 ? 0 : n; i2 < n2; ++i2) {
                if (stringBuilder.charAt(i2) != '.') continue;
                n3 = i2;
                break;
            }
            if (n3 != -1) {
                if (n3 - n > this.charCount) {
                    stringBuilder.delete(n + this.charCount, n3);
                    n3 = n + this.charCount;
                    if (this.ellipsis != '\u0000') {
                        stringBuilder.insert(n3, this.ellipsis);
                        ++n3;
                    }
                }
                ++n3;
            }
            return n3;
        }
    }

    static class MaxElementAbbreviator
    extends NameAbbreviator {
        private final int count;
        private final Strategy strategy;

        public MaxElementAbbreviator(int n, Strategy strategy) {
            this.count = Math.max(n, strategy.minCount);
            this.strategy = strategy;
        }

        @Override
        public void abbreviate(String string, StringBuilder stringBuilder) {
            this.strategy.abbreviate(this.count, string, stringBuilder);
        }

        static enum Strategy {
            DROP(0){

                @Override
                final void abbreviate(int n, String string, StringBuilder stringBuilder) {
                    int n2 = 0;
                    for (int i2 = 0; i2 < n; ++i2) {
                        if ((n2 = string.indexOf(46, n2)) == -1) {
                            stringBuilder.append(string);
                            return;
                        }
                        ++n2;
                    }
                    stringBuilder.append(string, n2, string.length());
                }
            }
            ,
            RETAIN(1){

                @Override
                final void abbreviate(int n, String string, StringBuilder stringBuilder) {
                    int n2 = string.length() - 1;
                    while (n > 0) {
                        if ((n2 = string.lastIndexOf(46, n2 - 1)) == -1) {
                            stringBuilder.append(string);
                            return;
                        }
                        --n;
                    }
                    stringBuilder.append(string, n2 + 1, string.length());
                }
            };

            final int minCount;

            private Strategy(int n2) {
                this.minCount = n2;
            }

            abstract void abbreviate(int var1, String var2, StringBuilder var3);
        }
    }

    static class NOPAbbreviator
    extends NameAbbreviator {
        @Override
        public void abbreviate(String string, StringBuilder stringBuilder) {
            stringBuilder.append(string);
        }
    }
}

