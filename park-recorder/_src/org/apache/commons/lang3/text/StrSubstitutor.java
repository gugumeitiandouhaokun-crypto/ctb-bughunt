/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.text;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrMatcher;

public class StrSubstitutor {
    public static final char DEFAULT_ESCAPE = '$';
    public static final StrMatcher DEFAULT_PREFIX = StrMatcher.stringMatcher("${");
    public static final StrMatcher DEFAULT_SUFFIX = StrMatcher.stringMatcher("}");
    public static final StrMatcher DEFAULT_VALUE_DELIMITER = StrMatcher.stringMatcher(":-");
    private char escapeChar;
    private StrMatcher prefixMatcher;
    private StrMatcher suffixMatcher;
    private StrMatcher valueDelimiterMatcher;
    private StrLookup<?> variableResolver;
    private boolean enableSubstitutionInVariables;
    private boolean preserveEscapes = false;

    public static <V> String replace(Object object, Map<String, V> map) {
        return new StrSubstitutor(map).replace(object);
    }

    public static <V> String replace(Object object, Map<String, V> map, String string, String string2) {
        return new StrSubstitutor(map, string, string2).replace(object);
    }

    public static String replace(Object object, Properties properties) {
        if (properties == null) {
            return object.toString();
        }
        HashMap<String, String> hashMap = new HashMap<String, String>();
        Enumeration<?> enumeration = properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String string = (String)enumeration.nextElement();
            String string2 = properties.getProperty(string);
            hashMap.put(string, string2);
        }
        return StrSubstitutor.replace(object, hashMap);
    }

    public static String replaceSystemProperties(Object object) {
        return new StrSubstitutor(StrLookup.systemPropertiesLookup()).replace(object);
    }

    public StrSubstitutor() {
        this(null, DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
    }

    public <V> StrSubstitutor(Map<String, V> map) {
        this(StrLookup.mapLookup(map), DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
    }

    public <V> StrSubstitutor(Map<String, V> map, String string, String string2) {
        this(StrLookup.mapLookup(map), string, string2, '$');
    }

    public <V> StrSubstitutor(Map<String, V> map, String string, String string2, char c2) {
        this(StrLookup.mapLookup(map), string, string2, c2);
    }

    public <V> StrSubstitutor(Map<String, V> map, String string, String string2, char c2, String string3) {
        this(StrLookup.mapLookup(map), string, string2, c2, string3);
    }

    public StrSubstitutor(StrLookup<?> strLookup) {
        this(strLookup, DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
    }

    public StrSubstitutor(StrLookup<?> strLookup, String string, String string2, char c2) {
        this.setVariableResolver(strLookup);
        this.setVariablePrefix(string);
        this.setVariableSuffix(string2);
        this.setEscapeChar(c2);
        this.setValueDelimiterMatcher(DEFAULT_VALUE_DELIMITER);
    }

    public StrSubstitutor(StrLookup<?> strLookup, String string, String string2, char c2, String string3) {
        this.setVariableResolver(strLookup);
        this.setVariablePrefix(string);
        this.setVariableSuffix(string2);
        this.setEscapeChar(c2);
        this.setValueDelimiter(string3);
    }

    public StrSubstitutor(StrLookup<?> strLookup, StrMatcher strMatcher, StrMatcher strMatcher2, char c2) {
        this(strLookup, strMatcher, strMatcher2, c2, DEFAULT_VALUE_DELIMITER);
    }

    public StrSubstitutor(StrLookup<?> strLookup, StrMatcher strMatcher, StrMatcher strMatcher2, char c2, StrMatcher strMatcher3) {
        this.setVariableResolver(strLookup);
        this.setVariablePrefixMatcher(strMatcher);
        this.setVariableSuffixMatcher(strMatcher2);
        this.setEscapeChar(c2);
        this.setValueDelimiterMatcher(strMatcher3);
    }

    public String replace(String string) {
        if (string == null) {
            return null;
        }
        StrBuilder strBuilder = new StrBuilder(string);
        if (!this.substitute(strBuilder, 0, string.length())) {
            return string;
        }
        return strBuilder.toString();
    }

    public String replace(String string, int n, int n2) {
        if (string == null) {
            return null;
        }
        StrBuilder strBuilder = new StrBuilder(n2).append(string, n, n2);
        if (!this.substitute(strBuilder, 0, n2)) {
            int n3 = n;
            return string.substring(n3, n3 + n2);
        }
        return strBuilder.toString();
    }

    public String replace(char[] cArray) {
        if (cArray == null) {
            return null;
        }
        StrBuilder strBuilder = new StrBuilder(cArray.length).append(cArray);
        this.substitute(strBuilder, 0, cArray.length);
        return strBuilder.toString();
    }

    public String replace(char[] object, int n, int n2) {
        if (object == null) {
            return null;
        }
        object = new StrBuilder(n2).append((char[])object, n, n2);
        this.substitute((StrBuilder)object, 0, n2);
        return ((StrBuilder)object).toString();
    }

    public String replace(StringBuffer charSequence) {
        if (charSequence == null) {
            return null;
        }
        charSequence = new StrBuilder(((StringBuffer)charSequence).length()).append((StringBuffer)charSequence);
        this.substitute((StrBuilder)charSequence, 0, ((StrBuilder)charSequence).length());
        return ((StrBuilder)charSequence).toString();
    }

    public String replace(StringBuffer charSequence, int n, int n2) {
        if (charSequence == null) {
            return null;
        }
        charSequence = new StrBuilder(n2).append((StringBuffer)charSequence, n, n2);
        this.substitute((StrBuilder)charSequence, 0, n2);
        return ((StrBuilder)charSequence).toString();
    }

    public String replace(CharSequence charSequence) {
        if (charSequence == null) {
            return null;
        }
        return this.replace(charSequence, 0, charSequence.length());
    }

    public String replace(CharSequence charSequence, int n, int n2) {
        if (charSequence == null) {
            return null;
        }
        charSequence = new StrBuilder(n2).append(charSequence, n, n2);
        this.substitute((StrBuilder)charSequence, 0, n2);
        return ((StrBuilder)charSequence).toString();
    }

    public String replace(StrBuilder strBuilder) {
        if (strBuilder == null) {
            return null;
        }
        strBuilder = new StrBuilder(strBuilder.length()).append(strBuilder);
        this.substitute(strBuilder, 0, strBuilder.length());
        return strBuilder.toString();
    }

    public String replace(StrBuilder strBuilder, int n, int n2) {
        if (strBuilder == null) {
            return null;
        }
        strBuilder = new StrBuilder(n2).append(strBuilder, n, n2);
        this.substitute(strBuilder, 0, n2);
        return strBuilder.toString();
    }

    public String replace(Object object) {
        if (object == null) {
            return null;
        }
        object = new StrBuilder().append(object);
        this.substitute((StrBuilder)object, 0, ((StrBuilder)object).length());
        return ((StrBuilder)object).toString();
    }

    public boolean replaceIn(StringBuffer stringBuffer) {
        if (stringBuffer == null) {
            return false;
        }
        return this.replaceIn(stringBuffer, 0, stringBuffer.length());
    }

    public boolean replaceIn(StringBuffer stringBuffer, int n, int n2) {
        if (stringBuffer == null) {
            return false;
        }
        StrBuilder strBuilder = new StrBuilder(n2).append(stringBuffer, n, n2);
        if (!this.substitute(strBuilder, 0, n2)) {
            return false;
        }
        int n3 = n;
        stringBuffer.replace(n3, n3 + n2, strBuilder.toString());
        return true;
    }

    public boolean replaceIn(StringBuilder stringBuilder) {
        if (stringBuilder == null) {
            return false;
        }
        return this.replaceIn(stringBuilder, 0, stringBuilder.length());
    }

    public boolean replaceIn(StringBuilder stringBuilder, int n, int n2) {
        if (stringBuilder == null) {
            return false;
        }
        StrBuilder strBuilder = new StrBuilder(n2).append(stringBuilder, n, n2);
        if (!this.substitute(strBuilder, 0, n2)) {
            return false;
        }
        int n3 = n;
        stringBuilder.replace(n3, n3 + n2, strBuilder.toString());
        return true;
    }

    public boolean replaceIn(StrBuilder strBuilder) {
        if (strBuilder == null) {
            return false;
        }
        return this.substitute(strBuilder, 0, strBuilder.length());
    }

    public boolean replaceIn(StrBuilder strBuilder, int n, int n2) {
        if (strBuilder == null) {
            return false;
        }
        return this.substitute(strBuilder, n, n2);
    }

    protected boolean substitute(StrBuilder strBuilder, int n, int n2) {
        return this.substitute(strBuilder, n, n2, null) > 0;
    }

    private int substitute(StrBuilder strBuilder, int n, int n2, List<String> list) {
        StrMatcher strMatcher = this.getVariablePrefixMatcher();
        StrMatcher strMatcher2 = this.getVariableSuffixMatcher();
        char c2 = this.getEscapeChar();
        StrMatcher strMatcher3 = this.getValueDelimiterMatcher();
        boolean bl = this.isEnableSubstitutionInVariables();
        boolean bl2 = list == null;
        boolean bl3 = false;
        int n3 = 0;
        char[] cArray = strBuilder.buffer;
        int n4 = n + n2;
        int n5 = n;
        block0: while (n5 < n4) {
            int n6 = strMatcher.isMatch(cArray, n5, n, n4);
            if (n6 == 0) {
                ++n5;
                continue;
            }
            if (n5 > n && cArray[n5 - 1] == c2) {
                if (this.preserveEscapes) {
                    ++n5;
                    continue;
                }
                strBuilder.deleteCharAt(n5 - 1);
                cArray = strBuilder.buffer;
                --n3;
                bl3 = true;
                --n4;
                continue;
            }
            int n7 = n5;
            n5 += n6;
            int n8 = 0;
            while (n5 < n4) {
                int n9;
                if (bl && (n9 = strMatcher.isMatch(cArray, n5, n, n4)) != 0) {
                    ++n8;
                    n5 += n9;
                    continue;
                }
                n9 = strMatcher2.isMatch(cArray, n5, n, n4);
                if (n9 == 0) {
                    ++n5;
                    continue;
                }
                if (n8 == 0) {
                    int n10;
                    int n11;
                    Object object;
                    String string = new String(cArray, n7 + n6, n5 - n7 - n6);
                    if (bl) {
                        StrBuilder strBuilder2 = new StrBuilder(string);
                        this.substitute(strBuilder2, 0, strBuilder2.length());
                        string = strBuilder2.toString();
                    }
                    n8 = n5 += n9;
                    String string2 = string;
                    String string3 = null;
                    if (strMatcher3 != null) {
                        object = string.toCharArray();
                        for (n11 = 0; n11 < ((char[])object).length; ++n11) {
                            if (!bl) {
                                int n12 = n11;
                                if (strMatcher.isMatch((char[])object, n12, n12, ((char[])object).length) != 0) break;
                            }
                            if ((n10 = strMatcher3.isMatch((char[])object, n11)) == 0) continue;
                            string2 = string.substring(0, n11);
                            string3 = string.substring(n11 + n10);
                            break;
                        }
                    }
                    if (list == null) {
                        list = new ArrayList<String>();
                        list.add(new String(cArray, n, n2));
                    }
                    this.checkCyclicSubstitution(string2, list);
                    list.add(string2);
                    String string4 = this.resolveVariable(string2, strBuilder, n7, n8);
                    object = string4;
                    if (string4 == null) {
                        object = string3;
                    }
                    if (object != null) {
                        n10 = ((String)object).length();
                        strBuilder.replace(n7, n8, (String)object);
                        bl3 = true;
                        n11 = this.substitute(strBuilder, n7, n10, list) + n10 - (n8 - n7);
                        n5 += n11;
                        n4 += n11;
                        n3 += n11;
                        cArray = strBuilder.buffer;
                    }
                    List<String> list2 = list;
                    list2.remove(list2.size() - 1);
                    continue block0;
                }
                --n8;
                n5 += n9;
            }
        }
        if (bl2) {
            if (bl3) {
                return 1;
            }
            return 0;
        }
        return n3;
    }

    private void checkCyclicSubstitution(String charSequence, List<String> list) {
        if (!list.contains(charSequence)) {
            return;
        }
        charSequence = new StrBuilder(256);
        ((StrBuilder)charSequence).append("Infinite loop in property interpolation of ");
        ((StrBuilder)charSequence).append(list.remove(0));
        ((StrBuilder)charSequence).append(": ");
        ((StrBuilder)charSequence).appendWithSeparators(list, "->");
        throw new IllegalStateException(((StrBuilder)charSequence).toString());
    }

    protected String resolveVariable(String string, StrBuilder strLookup, int n, int n2) {
        strLookup = this.getVariableResolver();
        if (strLookup == null) {
            return null;
        }
        return strLookup.lookup(string);
    }

    public char getEscapeChar() {
        return this.escapeChar;
    }

    public void setEscapeChar(char c2) {
        this.escapeChar = c2;
    }

    public StrMatcher getVariablePrefixMatcher() {
        return this.prefixMatcher;
    }

    public StrSubstitutor setVariablePrefixMatcher(StrMatcher strMatcher) {
        if (strMatcher == null) {
            throw new IllegalArgumentException("Variable prefix matcher must not be null!");
        }
        this.prefixMatcher = strMatcher;
        return this;
    }

    public StrSubstitutor setVariablePrefix(char c2) {
        return this.setVariablePrefixMatcher(StrMatcher.charMatcher(c2));
    }

    public StrSubstitutor setVariablePrefix(String string) {
        if (string == null) {
            throw new IllegalArgumentException("Variable prefix must not be null!");
        }
        return this.setVariablePrefixMatcher(StrMatcher.stringMatcher(string));
    }

    public StrMatcher getVariableSuffixMatcher() {
        return this.suffixMatcher;
    }

    public StrSubstitutor setVariableSuffixMatcher(StrMatcher strMatcher) {
        if (strMatcher == null) {
            throw new IllegalArgumentException("Variable suffix matcher must not be null!");
        }
        this.suffixMatcher = strMatcher;
        return this;
    }

    public StrSubstitutor setVariableSuffix(char c2) {
        return this.setVariableSuffixMatcher(StrMatcher.charMatcher(c2));
    }

    public StrSubstitutor setVariableSuffix(String string) {
        if (string == null) {
            throw new IllegalArgumentException("Variable suffix must not be null!");
        }
        return this.setVariableSuffixMatcher(StrMatcher.stringMatcher(string));
    }

    public StrMatcher getValueDelimiterMatcher() {
        return this.valueDelimiterMatcher;
    }

    public StrSubstitutor setValueDelimiterMatcher(StrMatcher strMatcher) {
        this.valueDelimiterMatcher = strMatcher;
        return this;
    }

    public StrSubstitutor setValueDelimiter(char c2) {
        return this.setValueDelimiterMatcher(StrMatcher.charMatcher(c2));
    }

    public StrSubstitutor setValueDelimiter(String string) {
        if (StringUtils.isEmpty(string)) {
            this.setValueDelimiterMatcher(null);
            return this;
        }
        return this.setValueDelimiterMatcher(StrMatcher.stringMatcher(string));
    }

    public StrLookup<?> getVariableResolver() {
        return this.variableResolver;
    }

    public void setVariableResolver(StrLookup<?> strLookup) {
        this.variableResolver = strLookup;
    }

    public boolean isEnableSubstitutionInVariables() {
        return this.enableSubstitutionInVariables;
    }

    public void setEnableSubstitutionInVariables(boolean bl) {
        this.enableSubstitutionInVariables = bl;
    }

    public boolean isPreserveEscapes() {
        return this.preserveEscapes;
    }

    public void setPreserveEscapes(boolean bl) {
        this.preserveEscapes = bl;
    }
}

