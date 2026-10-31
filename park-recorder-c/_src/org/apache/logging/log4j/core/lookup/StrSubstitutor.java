/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.lookup;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationAware;
import org.apache.logging.log4j.core.lookup.MapLookup;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.core.lookup.StrMatcher;
import org.apache.logging.log4j.util.Strings;

public class StrSubstitutor
implements ConfigurationAware {
    public static final char DEFAULT_ESCAPE = '$';
    public static final StrMatcher DEFAULT_PREFIX = StrMatcher.stringMatcher("${");
    public static final StrMatcher DEFAULT_SUFFIX = StrMatcher.stringMatcher("}");
    public static final String DEFAULT_VALUE_DELIMITER_STRING = ":-";
    public static final StrMatcher DEFAULT_VALUE_DELIMITER = StrMatcher.stringMatcher(":-");
    public static final String ESCAPE_DELIMITER_STRING = ":\\-";
    public static final StrMatcher DEFAULT_VALUE_ESCAPE_DELIMITER = StrMatcher.stringMatcher(":\\-");
    private static final int BUF_SIZE = 256;
    private char escapeChar;
    private StrMatcher prefixMatcher;
    private StrMatcher suffixMatcher;
    private String valueDelimiterString;
    private StrMatcher valueDelimiterMatcher;
    private StrMatcher valueEscapeDelimiterMatcher;
    private StrLookup variableResolver;
    private boolean enableSubstitutionInVariables = true;
    private Configuration configuration;

    public StrSubstitutor() {
        this(null, DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
    }

    public StrSubstitutor(Map<String, String> map) {
        this((StrLookup)new MapLookup(map), DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
    }

    public StrSubstitutor(Map<String, String> map, String string, String string2) {
        this((StrLookup)new MapLookup(map), string, string2, '$');
    }

    public StrSubstitutor(Map<String, String> map, String string, String string2, char c2) {
        this((StrLookup)new MapLookup(map), string, string2, c2);
    }

    public StrSubstitutor(Map<String, String> map, String string, String string2, char c2, String string3) {
        this((StrLookup)new MapLookup(map), string, string2, c2, string3);
    }

    public StrSubstitutor(Properties properties) {
        this(StrSubstitutor.toTypeSafeMap(properties));
    }

    public StrSubstitutor(StrLookup strLookup) {
        this(strLookup, DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
    }

    public StrSubstitutor(StrLookup strLookup, String string, String string2, char c2) {
        this.setVariableResolver(strLookup);
        this.setVariablePrefix(string);
        this.setVariableSuffix(string2);
        this.setEscapeChar(c2);
    }

    public StrSubstitutor(StrLookup strLookup, String string, String string2, char c2, String string3) {
        this.setVariableResolver(strLookup);
        this.setVariablePrefix(string);
        this.setVariableSuffix(string2);
        this.setEscapeChar(c2);
        this.setValueDelimiter(string3);
    }

    public StrSubstitutor(StrLookup strLookup, StrMatcher strMatcher, StrMatcher strMatcher2, char c2) {
        this(strLookup, strMatcher, strMatcher2, c2, DEFAULT_VALUE_DELIMITER, DEFAULT_VALUE_ESCAPE_DELIMITER);
        this.valueDelimiterString = DEFAULT_VALUE_DELIMITER_STRING;
    }

    public StrSubstitutor(StrLookup strLookup, StrMatcher strMatcher, StrMatcher strMatcher2, char c2, StrMatcher strMatcher3) {
        this.setVariableResolver(strLookup);
        this.setVariablePrefixMatcher(strMatcher);
        this.setVariableSuffixMatcher(strMatcher2);
        this.setEscapeChar(c2);
        this.setValueDelimiterMatcher(strMatcher3);
    }

    public StrSubstitutor(StrLookup strLookup, StrMatcher strMatcher, StrMatcher strMatcher2, char c2, StrMatcher strMatcher3, StrMatcher strMatcher4) {
        this.setVariableResolver(strLookup);
        this.setVariablePrefixMatcher(strMatcher);
        this.setVariableSuffixMatcher(strMatcher2);
        this.setEscapeChar(c2);
        this.setValueDelimiterMatcher(strMatcher3);
        this.valueEscapeDelimiterMatcher = strMatcher4;
    }

    public static String replace(Object object, Map<String, String> map) {
        return new StrSubstitutor(map).replace(object);
    }

    public static String replace(Object object, Map<String, String> map, String string, String string2) {
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

    private static Map<String, String> toTypeSafeMap(Properties properties) {
        HashMap<String, String> hashMap = new HashMap<String, String>(properties.size());
        for (String string : properties.stringPropertyNames()) {
            hashMap.put(string, properties.getProperty(string));
        }
        return hashMap;
    }

    public String replace(String string) {
        return this.replace(null, string);
    }

    public String replace(LogEvent logEvent, String string) {
        if (string == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(string);
        if (!this.substitute(logEvent, stringBuilder, 0, string.length())) {
            return string;
        }
        return stringBuilder.toString();
    }

    public String replace(String string, int n, int n2) {
        return this.replace(null, string, n, n2);
    }

    public String replace(LogEvent logEvent, String string, int n, int n2) {
        if (string == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(n2).append(string, n, n2);
        if (!this.substitute(logEvent, stringBuilder, 0, n2)) {
            int n3 = n;
            return string.substring(n3, n3 + n2);
        }
        return stringBuilder.toString();
    }

    public String replace(char[] cArray) {
        return this.replace((LogEvent)null, cArray);
    }

    public String replace(LogEvent logEvent, char[] cArray) {
        if (cArray == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(cArray.length).append(cArray);
        this.substitute(logEvent, stringBuilder, 0, cArray.length);
        return stringBuilder.toString();
    }

    public String replace(char[] cArray, int n, int n2) {
        return this.replace(null, cArray, n, n2);
    }

    public String replace(LogEvent logEvent, char[] object, int n, int n2) {
        if (object == null) {
            return null;
        }
        object = new StringBuilder(n2).append((char[])object, n, n2);
        this.substitute(logEvent, (StringBuilder)object, 0, n2);
        return ((StringBuilder)object).toString();
    }

    public String replace(StringBuffer stringBuffer) {
        return this.replace(null, stringBuffer);
    }

    public String replace(LogEvent logEvent, StringBuffer abstractStringBuilder) {
        if (abstractStringBuilder == null) {
            return null;
        }
        abstractStringBuilder = new StringBuilder(((StringBuffer)abstractStringBuilder).length()).append((StringBuffer)abstractStringBuilder);
        this.substitute(logEvent, (StringBuilder)abstractStringBuilder, 0, ((StringBuilder)abstractStringBuilder).length());
        return ((StringBuilder)abstractStringBuilder).toString();
    }

    public String replace(StringBuffer stringBuffer, int n, int n2) {
        return this.replace(null, stringBuffer, n, n2);
    }

    public String replace(LogEvent logEvent, StringBuffer abstractStringBuilder, int n, int n2) {
        if (abstractStringBuilder == null) {
            return null;
        }
        abstractStringBuilder = new StringBuilder(n2).append(abstractStringBuilder, n, n2);
        this.substitute(logEvent, (StringBuilder)abstractStringBuilder, 0, n2);
        return ((StringBuilder)abstractStringBuilder).toString();
    }

    public String replace(StringBuilder stringBuilder) {
        return this.replace(null, stringBuilder);
    }

    public String replace(LogEvent logEvent, StringBuilder stringBuilder) {
        if (stringBuilder == null) {
            return null;
        }
        stringBuilder = new StringBuilder(stringBuilder.length()).append((CharSequence)stringBuilder);
        this.substitute(logEvent, stringBuilder, 0, stringBuilder.length());
        return stringBuilder.toString();
    }

    public String replace(StringBuilder stringBuilder, int n, int n2) {
        return this.replace(null, stringBuilder, n, n2);
    }

    public String replace(LogEvent logEvent, StringBuilder stringBuilder, int n, int n2) {
        if (stringBuilder == null) {
            return null;
        }
        stringBuilder = new StringBuilder(n2).append(stringBuilder, n, n2);
        this.substitute(logEvent, stringBuilder, 0, n2);
        return stringBuilder.toString();
    }

    public String replace(Object object) {
        return this.replace(null, object);
    }

    public String replace(LogEvent logEvent, Object object) {
        if (object == null) {
            return null;
        }
        object = new StringBuilder().append(object);
        this.substitute(logEvent, (StringBuilder)object, 0, ((StringBuilder)object).length());
        return ((StringBuilder)object).toString();
    }

    public boolean replaceIn(StringBuffer stringBuffer) {
        if (stringBuffer == null) {
            return false;
        }
        return this.replaceIn(stringBuffer, 0, stringBuffer.length());
    }

    public boolean replaceIn(StringBuffer stringBuffer, int n, int n2) {
        return this.replaceIn(null, stringBuffer, n, n2);
    }

    public boolean replaceIn(LogEvent logEvent, StringBuffer stringBuffer, int n, int n2) {
        if (stringBuffer == null) {
            return false;
        }
        StringBuilder stringBuilder = new StringBuilder(n2).append(stringBuffer, n, n2);
        if (!this.substitute(logEvent, stringBuilder, 0, n2)) {
            return false;
        }
        int n3 = n;
        stringBuffer.replace(n3, n3 + n2, stringBuilder.toString());
        return true;
    }

    public boolean replaceIn(StringBuilder stringBuilder) {
        return this.replaceIn(null, stringBuilder);
    }

    public boolean replaceIn(LogEvent logEvent, StringBuilder stringBuilder) {
        if (stringBuilder == null) {
            return false;
        }
        return this.substitute(logEvent, stringBuilder, 0, stringBuilder.length());
    }

    public boolean replaceIn(StringBuilder stringBuilder, int n, int n2) {
        return this.replaceIn(null, stringBuilder, n, n2);
    }

    public boolean replaceIn(LogEvent logEvent, StringBuilder stringBuilder, int n, int n2) {
        if (stringBuilder == null) {
            return false;
        }
        return this.substitute(logEvent, stringBuilder, n, n2);
    }

    protected boolean substitute(LogEvent logEvent, StringBuilder stringBuilder, int n, int n2) {
        return this.substitute(logEvent, stringBuilder, n, n2, null) > 0;
    }

    private int substitute(LogEvent logEvent, StringBuilder stringBuilder, int n, int n2, List<String> list) {
        StrMatcher strMatcher = this.getVariablePrefixMatcher();
        StrMatcher strMatcher2 = this.getVariableSuffixMatcher();
        char c2 = this.getEscapeChar();
        StrMatcher strMatcher3 = this.getValueDelimiterMatcher();
        boolean bl = this.isEnableSubstitutionInVariables();
        boolean bl2 = list == null;
        boolean bl3 = false;
        int n3 = 0;
        char[] cArray = this.getChars(stringBuilder);
        int n4 = n + n2;
        int n5 = n;
        block0: while (n5 < n4) {
            int n6 = strMatcher.isMatch(cArray, n5, n, n4);
            if (n6 == 0) {
                ++n5;
                continue;
            }
            if (n5 > n && cArray[n5 - 1] == c2) {
                stringBuilder.deleteCharAt(n5 - 1);
                cArray = this.getChars(stringBuilder);
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
                        StringBuilder stringBuilder2 = new StringBuilder(string);
                        this.substitute(logEvent, stringBuilder2, 0, stringBuilder2.length());
                        string = stringBuilder2.toString();
                    }
                    n8 = n5 += n9;
                    String string2 = string;
                    String string3 = null;
                    if (strMatcher3 != null) {
                        object = string.toCharArray();
                        block2: for (n11 = 0; n11 < ((char[])object).length; ++n11) {
                            if (!bl) {
                                int n12 = n11;
                                if (strMatcher.isMatch((char[])object, n12, n12, ((char[])object).length) != 0) break;
                            }
                            if (this.valueEscapeDelimiterMatcher != null) {
                                int n13 = this.valueEscapeDelimiterMatcher.isMatch((char[])object, n11);
                                if (n13 != 0) {
                                    String string4 = string.substring(0, n11) + ':';
                                    string2 = string4 + string.substring(n11 + n13 - 1);
                                    for (int i2 = n11 + n13; i2 < ((Object)object).length; ++i2) {
                                        n10 = strMatcher3.isMatch((char[])object, i2);
                                        if (n10 == 0) continue;
                                        string2 = string4 + string.substring(n11 + n13, i2);
                                        string3 = string.substring(i2 + n10);
                                        break block2;
                                    }
                                    break;
                                }
                                n10 = strMatcher3.isMatch((char[])object, n11);
                                if (n10 == 0) continue;
                                string2 = string.substring(0, n11);
                                string3 = string.substring(n11 + n10);
                                break;
                            }
                            n10 = strMatcher3.isMatch((char[])object, n11);
                            if (n10 == 0) continue;
                            string2 = string.substring(0, n11);
                            string3 = string.substring(n11 + n10);
                            break;
                        }
                    }
                    if (list == null) {
                        list = new ArrayList<String>();
                        list.add(new String(cArray, n, n2 + n3));
                    }
                    this.checkCyclicSubstitution(string2, list);
                    list.add(string2);
                    String string5 = this.resolveVariable(logEvent, string2, stringBuilder, n7, n8);
                    object = string5;
                    if (string5 == null) {
                        object = string3;
                    }
                    if (object != null) {
                        n10 = ((String)object).length();
                        stringBuilder.replace(n7, n8, (String)object);
                        bl3 = true;
                        n11 = this.substitute(logEvent, stringBuilder, n7, n10, list) + (n10 - (n8 - n7));
                        n5 += n11;
                        n4 += n11;
                        n3 += n11;
                        cArray = this.getChars(stringBuilder);
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
        charSequence = new StringBuilder(256);
        ((StringBuilder)charSequence).append("Infinite loop in property interpolation of ");
        ((StringBuilder)charSequence).append(list.remove(0));
        ((StringBuilder)charSequence).append(": ");
        this.appendWithSeparators((StringBuilder)charSequence, list, "->");
        throw new IllegalStateException(((StringBuilder)charSequence).toString());
    }

    protected String resolveVariable(LogEvent logEvent, String string, StringBuilder object, int n, int n2) {
        object = this.getVariableResolver();
        if (object == null) {
            return null;
        }
        return object.lookup(logEvent, string);
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
        if (Strings.isEmpty(string)) {
            this.setValueDelimiterMatcher(null);
            return this;
        }
        String string2 = string;
        String string3 = string.substring(0, string.length() - 1) + "\\" + string2.substring(string2.length() - 1);
        this.valueEscapeDelimiterMatcher = StrMatcher.stringMatcher(string3);
        return this.setValueDelimiterMatcher(StrMatcher.stringMatcher(string));
    }

    public StrLookup getVariableResolver() {
        return this.variableResolver;
    }

    public void setVariableResolver(StrLookup strLookup) {
        if (strLookup instanceof ConfigurationAware && this.configuration != null) {
            ((ConfigurationAware)((Object)strLookup)).setConfiguration(this.configuration);
        }
        this.variableResolver = strLookup;
    }

    public boolean isEnableSubstitutionInVariables() {
        return this.enableSubstitutionInVariables;
    }

    public void setEnableSubstitutionInVariables(boolean bl) {
        this.enableSubstitutionInVariables = bl;
    }

    private char[] getChars(StringBuilder stringBuilder) {
        char[] cArray = new char[stringBuilder.length()];
        stringBuilder.getChars(0, stringBuilder.length(), cArray, 0);
        return cArray;
    }

    public void appendWithSeparators(StringBuilder stringBuilder, Iterable<?> object, String string) {
        if (object != null) {
            string = string == null ? "" : string;
            object = object.iterator();
            while (object.hasNext()) {
                stringBuilder.append(object.next());
                if (!object.hasNext()) continue;
                stringBuilder.append(string);
            }
        }
    }

    public String toString() {
        return "StrSubstitutor(" + this.variableResolver.toString() + ')';
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        if (this.variableResolver instanceof ConfigurationAware) {
            ((ConfigurationAware)((Object)this.variableResolver)).setConfiguration(this.configuration);
        }
    }
}

