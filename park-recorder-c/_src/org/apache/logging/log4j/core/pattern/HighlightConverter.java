/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.AnsiConverter;
import org.apache.logging.log4j.core.pattern.AnsiEscape;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name="highlight", category="Converter")
@ConverterKeys(value={"highlight"})
@PerformanceSensitive(value={"allocation"})
public final class HighlightConverter
extends LogEventPatternConverter
implements AnsiConverter {
    private static final Map<String, String> DEFAULT_STYLES = new HashMap<String, String>();
    private static final Map<String, String> LOGBACK_STYLES = new HashMap<String, String>();
    private static final String STYLE_KEY = "STYLE";
    private static final String STYLE_KEY_DEFAULT = "DEFAULT";
    private static final String STYLE_KEY_LOGBACK = "LOGBACK";
    private static final Map<String, Map<String, String>> STYLES = new HashMap<String, Map<String, String>>();
    private final Map<String, String> levelStyles;
    private final List<PatternFormatter> patternFormatters;
    private final boolean noAnsi;
    private final String defaultStyle;

    private static Map<String, String> createLevelStyleMap(String[] object) {
        if (((Object)object).length < 2) {
            return DEFAULT_STYLES;
        }
        object = AnsiEscape.createMap(((String)object[1]).replaceAll("disableAnsi=(true|false)", "").replaceAll("noConsoleNoAnsi=(true|false)", ""), new String[]{STYLE_KEY});
        HashMap<String, String> hashMap = new HashMap<String, String>(DEFAULT_STYLES);
        for (Map.Entry entry : object.entrySet()) {
            Object object2;
            String string = ((String)entry.getKey()).toUpperCase(Locale.ENGLISH);
            String object22 = (String)entry.getValue();
            if (STYLE_KEY.equalsIgnoreCase(string)) {
                object2 = STYLES.get(object22.toUpperCase(Locale.ENGLISH));
                if (object2 == null) {
                    LOGGER.error("Unknown level style: " + object22 + ". Use one of " + Arrays.toString(STYLES.keySet().toArray()));
                    continue;
                }
                hashMap.putAll((Map<String, String>)object2);
                continue;
            }
            object2 = Level.toLevel(string, null);
            if (object2 == null) {
                LOGGER.warn("Setting style for yet unknown level name {}", (Object)string);
                hashMap.put(string, object22);
                continue;
            }
            hashMap.put(((Level)object2).name(), object22);
        }
        return hashMap;
    }

    public static HighlightConverter newInstance(Configuration object, String[] stringArray) {
        if (stringArray.length <= 0) {
            LOGGER.error("Incorrect number of options on style. Expected at least 1, received " + stringArray.length);
            return null;
        }
        if (stringArray[0] == null) {
            LOGGER.error("No pattern supplied on style");
            return null;
        }
        object = PatternLayout.createPatternParser((Configuration)object).parse(stringArray[0]);
        boolean bl = Arrays.toString(stringArray).contains("disableAnsi=true");
        boolean bl2 = Arrays.toString(stringArray).contains("noConsoleNoAnsi=true");
        bl = bl || bl2 && System.console() == null;
        return new HighlightConverter((List<PatternFormatter>)object, HighlightConverter.createLevelStyleMap(stringArray), bl);
    }

    private HighlightConverter(List<PatternFormatter> list, Map<String, String> map, boolean bl) {
        super("style", "style");
        this.patternFormatters = list;
        this.levelStyles = map;
        this.defaultStyle = AnsiEscape.getDefaultStyle();
        this.noAnsi = bl;
    }

    @Override
    public final void format(LogEvent logEvent, StringBuilder stringBuilder) {
        int n;
        int n2 = 0;
        int n3 = 0;
        String string = this.levelStyles.get(logEvent.getLevel().name());
        if (!this.noAnsi) {
            n2 = stringBuilder.length();
            if (string != null) {
                stringBuilder.append(string);
            }
            n3 = stringBuilder.length();
        }
        int n4 = this.patternFormatters.size();
        for (n = 0; n < n4; ++n) {
            this.patternFormatters.get(n).format(logEvent, stringBuilder);
        }
        int n5 = n = stringBuilder.length() == n3 ? 1 : 0;
        if (!this.noAnsi) {
            if (n != 0) {
                stringBuilder.setLength(n2);
                return;
            }
            if (string != null) {
                stringBuilder.append(this.defaultStyle);
            }
        }
    }

    final String getLevelStyle(Level level) {
        return this.levelStyles.get(level.name());
    }

    @Override
    public final boolean handlesThrowable() {
        Iterator<PatternFormatter> iterator = this.patternFormatters.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().handlesThrowable()) continue;
            return true;
        }
        return false;
    }

    static {
        DEFAULT_STYLES.put(Level.FATAL.name(), AnsiEscape.createSequence("BRIGHT", "RED"));
        DEFAULT_STYLES.put(Level.ERROR.name(), AnsiEscape.createSequence("BRIGHT", "RED"));
        DEFAULT_STYLES.put(Level.WARN.name(), AnsiEscape.createSequence("YELLOW"));
        DEFAULT_STYLES.put(Level.INFO.name(), AnsiEscape.createSequence("GREEN"));
        DEFAULT_STYLES.put(Level.DEBUG.name(), AnsiEscape.createSequence("CYAN"));
        DEFAULT_STYLES.put(Level.TRACE.name(), AnsiEscape.createSequence("BLACK"));
        LOGBACK_STYLES.put(Level.FATAL.name(), AnsiEscape.createSequence("BLINK", "BRIGHT", "RED"));
        LOGBACK_STYLES.put(Level.ERROR.name(), AnsiEscape.createSequence("BRIGHT", "RED"));
        LOGBACK_STYLES.put(Level.WARN.name(), AnsiEscape.createSequence("RED"));
        LOGBACK_STYLES.put(Level.INFO.name(), AnsiEscape.createSequence("BLUE"));
        LOGBACK_STYLES.put(Level.DEBUG.name(), AnsiEscape.createSequence(null));
        LOGBACK_STYLES.put(Level.TRACE.name(), AnsiEscape.createSequence(null));
        STYLES.put(STYLE_KEY_DEFAULT, DEFAULT_STYLES);
        STYLES.put(STYLE_KEY_LOGBACK, LOGBACK_STYLES);
    }
}

