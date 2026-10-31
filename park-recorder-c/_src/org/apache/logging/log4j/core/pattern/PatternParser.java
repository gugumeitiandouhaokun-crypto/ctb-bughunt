/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.io.Serializable;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.pattern.AnsiConverter;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.ExtendedThrowablePatternConverter;
import org.apache.logging.log4j.core.pattern.FormattingInfo;
import org.apache.logging.log4j.core.pattern.LiteralPatternConverter;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.NanoTimePatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.util.SystemNanoClock;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

public final class PatternParser {
    static final String DISABLE_ANSI = "disableAnsi";
    static final String NO_CONSOLE_NO_ANSI = "noConsoleNoAnsi";
    private static final char ESCAPE_CHAR = '%';
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final int BUF_SIZE = 32;
    private static final int DECIMAL = 10;
    private final Configuration config;
    private final Map<String, Class<PatternConverter>> converterRules;

    public PatternParser(String string) {
        this(null, string, null, null);
    }

    public PatternParser(Configuration configuration, String string, Class<?> clazz) {
        this(configuration, string, clazz, null);
    }

    public PatternParser(Configuration iterator, String linkedHashMap, Class<?> object2, Class<?> clazz) {
        this.config = iterator;
        linkedHashMap = new PluginManager((String)((Object)linkedHashMap));
        ((PluginManager)((Object)linkedHashMap)).collectPlugins(iterator == null ? null : iterator.getPluginPackages());
        iterator = ((PluginManager)((Object)linkedHashMap)).getPlugins();
        linkedHashMap = new LinkedHashMap<String, Class<PatternConverter>>();
        for (Object object2 : iterator.values()) {
            try {
                String[] stringArray;
                Class clazz2 = ((PluginType)object2).getPluginClass();
                if (clazz != null && !clazz.isAssignableFrom(clazz2) || (stringArray = clazz2.getAnnotation(ConverterKeys.class)) == null) continue;
                stringArray = stringArray.value();
                int n = stringArray.length;
                for (int i2 = 0; i2 < n; ++i2) {
                    String string = stringArray[i2];
                    if (linkedHashMap.containsKey(string)) {
                        LOGGER.warn("Converter key '{}' is already mapped to '{}'. Sorry, Dave, I can't let you do that! Ignoring plugin [{}].", (Object)string, linkedHashMap.get(string), (Object)clazz2);
                        continue;
                    }
                    linkedHashMap.put(string, clazz2);
                }
            }
            catch (Exception exception) {
                LOGGER.error("Error processing plugin " + ((PluginType)object2).getElementName(), (Throwable)exception);
            }
        }
        this.converterRules = linkedHashMap;
    }

    public final List<PatternFormatter> parse(String string) {
        return this.parse(string, false, false, false);
    }

    public final List<PatternFormatter> parse(String string, boolean bl, boolean bl2) {
        return this.parse(string, bl, false, bl2);
    }

    public final List<PatternFormatter> parse(String object, boolean bl, boolean bl2, boolean bl3) {
        ArrayList<PatternFormatter> arrayList = new ArrayList<PatternFormatter>();
        Object object2 = new ArrayList<PatternConverter>();
        Object object3 = new ArrayList<FormattingInfo>();
        this.parse((String)object, (List<PatternConverter>)object2, (List<FormattingInfo>)object3, bl2, bl3, true);
        object = object3.iterator();
        bl2 = false;
        Object object4 = object2.iterator();
        while (object4.hasNext()) {
            object2 = (PatternConverter)object4.next();
            if (object2 instanceof NanoTimePatternConverter && this.config != null) {
                this.config.setNanoClock(new SystemNanoClock());
            }
            if (object2 instanceof LogEventPatternConverter) {
                object2 = (LogEventPatternConverter)object2;
                bl2 |= ((LogEventPatternConverter)object2).handlesThrowable();
            } else {
                object2 = new LiteralPatternConverter(this.config, "", true);
            }
            object3 = object.hasNext() ? (FormattingInfo)object.next() : FormattingInfo.getDefault();
            arrayList.add(new PatternFormatter((LogEventPatternConverter)object2, (FormattingInfo)object3));
        }
        if (bl && !bl2) {
            object4 = ExtendedThrowablePatternConverter.newInstance(this.config, null);
            arrayList.add(new PatternFormatter((LogEventPatternConverter)object4, FormattingInfo.getDefault()));
        }
        return arrayList;
    }

    private static int extractConverter(char c2, String string, int n, StringBuilder stringBuilder, StringBuilder stringBuilder2) {
        int n2;
        stringBuilder.setLength(0);
        if (!Character.isUnicodeIdentifierStart(c2)) {
            return n;
        }
        stringBuilder.append(c2);
        for (n2 = n; n2 < string.length() && Character.isUnicodeIdentifierPart(string.charAt(n2)); ++n2) {
            stringBuilder.append(string.charAt(n2));
            stringBuilder2.append(string.charAt(n2));
        }
        return n2;
    }

    private static int extractOptions(String string, int n, List<String> list) {
        int n2 = n;
        while (n2 < string.length() && string.charAt(n2) == '{') {
            int n3 = ++n2;
            int n4 = 1;
            while (n4 > 0 && n2 < string.length()) {
                char c2 = string.charAt(n2);
                if (c2 == '{') {
                    ++n4;
                } else if (c2 == '}') {
                    --n4;
                }
                ++n2;
            }
            if (n4 > 0) {
                n2 = string.lastIndexOf(125);
                if (n2 == -1 || n2 < n) {
                    return n3;
                }
                return n2 + 1;
            }
            list.add(string.substring(n3, n2 - 1));
        }
        return n2;
    }

    public final void parse(String string, List<PatternConverter> list, List<FormattingInfo> list2, boolean bl, boolean bl2) {
        this.parse(string, list, list2, false, bl, bl2);
    }

    public final void parse(String string, List<PatternConverter> list, List<FormattingInfo> list2, boolean bl, boolean bl2, boolean bl3) {
        Objects.requireNonNull(string, "pattern");
        StringBuilder stringBuilder = new StringBuilder(32);
        int n = string.length();
        ParserState parserState = ParserState.LITERAL_STATE;
        int n2 = 0;
        FormattingInfo formattingInfo = FormattingInfo.getDefault();
        while (n2 < n) {
            char c2 = string.charAt(n2++);
            switch (parserState) {
                case LITERAL_STATE: {
                    if (n2 == n) {
                        stringBuilder.append(c2);
                        break;
                    }
                    if (c2 == '%') {
                        switch (string.charAt(n2)) {
                            case '%': {
                                stringBuilder.append(c2);
                                ++n2;
                                break;
                            }
                            default: {
                                if (stringBuilder.length() != 0) {
                                    list.add(new LiteralPatternConverter(this.config, stringBuilder.toString(), bl3));
                                    list2.add(FormattingInfo.getDefault());
                                }
                                stringBuilder.setLength(0);
                                stringBuilder.append(c2);
                                parserState = ParserState.CONVERTER_STATE;
                                formattingInfo = FormattingInfo.getDefault();
                                break;
                            }
                        }
                        break;
                    }
                    stringBuilder.append(c2);
                    break;
                }
                case CONVERTER_STATE: {
                    stringBuilder.append(c2);
                    switch (c2) {
                        case '0': {
                            formattingInfo = new FormattingInfo(formattingInfo.isLeftAligned(), formattingInfo.getMinLength(), formattingInfo.getMaxLength(), formattingInfo.isLeftTruncate(), true);
                            break;
                        }
                        case '-': {
                            formattingInfo = new FormattingInfo(true, formattingInfo.getMinLength(), formattingInfo.getMaxLength(), formattingInfo.isLeftTruncate(), formattingInfo.isZeroPad());
                            break;
                        }
                        case '.': {
                            parserState = ParserState.DOT_STATE;
                            break;
                        }
                        default: {
                            if (c2 >= '0' && c2 <= '9') {
                                formattingInfo = new FormattingInfo(formattingInfo.isLeftAligned(), c2 - 48, formattingInfo.getMaxLength(), formattingInfo.isLeftTruncate(), formattingInfo.isZeroPad());
                                parserState = ParserState.MIN_STATE;
                                break;
                            }
                            n2 = this.finalizeConverter(c2, string, n2, stringBuilder, formattingInfo, this.converterRules, list, list2, bl, bl2, bl3);
                            parserState = ParserState.LITERAL_STATE;
                            formattingInfo = FormattingInfo.getDefault();
                            stringBuilder.setLength(0);
                            break;
                        }
                    }
                    break;
                }
                case MIN_STATE: {
                    stringBuilder.append(c2);
                    if (c2 >= '0' && c2 <= '9') {
                        formattingInfo = new FormattingInfo(formattingInfo.isLeftAligned(), formattingInfo.getMinLength() * 10 + c2 - 48, formattingInfo.getMaxLength(), formattingInfo.isLeftTruncate(), formattingInfo.isZeroPad());
                        break;
                    }
                    if (c2 == '.') {
                        parserState = ParserState.DOT_STATE;
                        break;
                    }
                    n2 = this.finalizeConverter(c2, string, n2, stringBuilder, formattingInfo, this.converterRules, list, list2, bl, bl2, bl3);
                    parserState = ParserState.LITERAL_STATE;
                    formattingInfo = FormattingInfo.getDefault();
                    stringBuilder.setLength(0);
                    break;
                }
                case DOT_STATE: {
                    stringBuilder.append(c2);
                    switch (c2) {
                        case '-': {
                            formattingInfo = new FormattingInfo(formattingInfo.isLeftAligned(), formattingInfo.getMinLength(), formattingInfo.getMaxLength(), false, formattingInfo.isZeroPad());
                            break;
                        }
                        default: {
                            if (c2 >= '0' && c2 <= '9') {
                                formattingInfo = new FormattingInfo(formattingInfo.isLeftAligned(), formattingInfo.getMinLength(), c2 - 48, formattingInfo.isLeftTruncate(), formattingInfo.isZeroPad());
                                parserState = ParserState.MAX_STATE;
                                break;
                            }
                            LOGGER.error("Error occurred in position " + n2 + ".\n Was expecting digit, instead got char \"" + c2 + "\".");
                            parserState = ParserState.LITERAL_STATE;
                            break;
                        }
                    }
                    break;
                }
                case MAX_STATE: {
                    stringBuilder.append(c2);
                    if (c2 >= '0' && c2 <= '9') {
                        formattingInfo = new FormattingInfo(formattingInfo.isLeftAligned(), formattingInfo.getMinLength(), formattingInfo.getMaxLength() * 10 + c2 - 48, formattingInfo.isLeftTruncate(), formattingInfo.isZeroPad());
                        break;
                    }
                    n2 = this.finalizeConverter(c2, string, n2, stringBuilder, formattingInfo, this.converterRules, list, list2, bl, bl2, bl3);
                    parserState = ParserState.LITERAL_STATE;
                    formattingInfo = FormattingInfo.getDefault();
                    stringBuilder.setLength(0);
                }
            }
        }
        if (stringBuilder.length() != 0) {
            list.add(new LiteralPatternConverter(this.config, stringBuilder.toString(), bl3));
            list2.add(FormattingInfo.getDefault());
        }
    }

    private PatternConverter createConverter(String string, StringBuilder stringBuilder, Map<String, Class<PatternConverter>> object, List<String> list, boolean bl, boolean n) {
        int n2;
        String string2 = string;
        Class clazz = null;
        if (object == null) {
            LOGGER.error("Null rules for [" + string + ']');
            return null;
        }
        for (int i2 = string.length(); i2 > 0 && clazz == null; --i2) {
            string2 = string2.substring(0, i2);
            clazz = (Class)object.get(string2);
        }
        if (clazz == null) {
            LOGGER.error("Unrecognized format specifier [" + string + ']');
            return null;
        }
        if (AnsiConverter.class.isAssignableFrom(clazz)) {
            list.add("disableAnsi=".concat(String.valueOf(bl)));
            list.add("noConsoleNoAnsi=".concat(String.valueOf(n != 0)));
        }
        Method[] methodArray = clazz.getDeclaredMethods();
        object = null;
        GenericDeclaration[] genericDeclarationArray = methodArray;
        n = methodArray.length;
        for (n2 = 0; n2 < n; ++n2) {
            Method method = genericDeclarationArray[n2];
            if (!Modifier.isStatic(method.getModifiers()) || !method.getDeclaringClass().equals(clazz) || !method.getName().equals("newInstance") || !PatternParser.areValidNewInstanceParameters(method.getParameterTypes())) continue;
            if (object == null) {
                object = method;
                continue;
            }
            if (!method.getReturnType().equals(((Method)object).getReturnType())) continue;
            LOGGER.error("Class " + clazz + " cannot contain multiple static newInstance methods");
            return null;
        }
        if (object == null) {
            LOGGER.error("Class " + clazz + " does not contain a static newInstance method");
            return null;
        }
        genericDeclarationArray = ((Method)object).getParameterTypes();
        Object[] objectArray = genericDeclarationArray.length > 0 ? new Object[genericDeclarationArray.length] : null;
        if (objectArray != null) {
            n2 = 0;
            boolean bl2 = false;
            for (GenericDeclaration genericDeclaration : genericDeclarationArray) {
                if (((Class)genericDeclaration).isArray() && ((Class)genericDeclaration).getName().equals("[Ljava.lang.String;")) {
                    String[] stringArray;
                    List<String> list2 = list;
                    objectArray[n2] = stringArray = list2.toArray(new String[list2.size()]);
                } else if (((Class)genericDeclaration).isAssignableFrom(Configuration.class)) {
                    objectArray[n2] = this.config;
                } else {
                    LOGGER.error("Unknown parameter type " + ((Class)genericDeclaration).getName() + " for static newInstance method of " + clazz.getName());
                    bl2 = true;
                }
                ++n2;
            }
            if (bl2) {
                return null;
            }
        }
        try {
            Object object2 = ((Method)object).invoke(null, objectArray);
            if (object2 instanceof PatternConverter) {
                stringBuilder.delete(0, stringBuilder.length() - (string.length() - string2.length()));
                return (PatternConverter)object2;
            }
            LOGGER.warn("Class {} does not extend PatternConverter.", (Object)clazz.getName());
        }
        catch (Exception exception) {
            LOGGER.error("Error creating converter for ".concat(String.valueOf(string)), (Throwable)exception);
        }
        return null;
    }

    private static boolean areValidNewInstanceParameters(Class<?>[] classArray) {
        for (Class<Configuration> clazz : classArray) {
            if (clazz.isAssignableFrom(Configuration.class) || clazz.isArray() && "[Ljava.lang.String;".equals(clazz.getName())) continue;
            return false;
        }
        return true;
    }

    private int finalizeConverter(char c2, String object, int n, StringBuilder stringBuilder, FormattingInfo formattingInfo, Map<String, Class<PatternConverter>> map, List<PatternConverter> list, List<FormattingInfo> list2, boolean bl, boolean bl2, boolean bl3) {
        Serializable serializable = new StringBuilder();
        c2 = (char)PatternParser.extractConverter(c2, (String)object, n, serializable, stringBuilder);
        String string = serializable.toString();
        serializable = new ArrayList();
        c2 = (char)PatternParser.extractOptions((String)object, c2, (List<String>)((Object)serializable));
        object = this.createConverter(string, stringBuilder, map, (List<String>)((Object)serializable), bl, bl2);
        if (object == null) {
            if (Strings.isEmpty(string)) {
                object = new StringBuilder("Empty conversion specifier starting at position ");
            } else {
                object = new StringBuilder("Unrecognized conversion specifier [");
                ((StringBuilder)object).append(string);
                ((StringBuilder)object).append("] starting at position ");
            }
            ((StringBuilder)object).append(Integer.toString(c2));
            ((StringBuilder)object).append(" in conversion pattern.");
            LOGGER.error(((StringBuilder)object).toString());
            list.add(new LiteralPatternConverter(this.config, stringBuilder.toString(), bl3));
            list2.add(FormattingInfo.getDefault());
        } else {
            list.add((PatternConverter)object);
            list2.add(formattingInfo);
            if (stringBuilder.length() > 0) {
                list.add(new LiteralPatternConverter(this.config, stringBuilder.toString(), bl3));
                list2.add(FormattingInfo.getDefault());
            }
        }
        stringBuilder.setLength(0);
        return c2;
    }

    static enum ParserState {
        LITERAL_STATE,
        CONVERTER_STATE,
        DOT_STATE,
        MIN_STATE,
        MAX_STATE;

    }
}

