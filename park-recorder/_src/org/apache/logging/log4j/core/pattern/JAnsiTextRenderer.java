/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.fusesource.jansi.Ansi
 *  org.fusesource.jansi.AnsiRenderer$Code
 */
package org.apache.logging.log4j.core.pattern;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.logging.log4j.core.pattern.TextRenderer;
import org.apache.logging.log4j.status.StatusLogger;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiRenderer;

public final class JAnsiTextRenderer
implements TextRenderer {
    public static final Map<String, AnsiRenderer.Code[]> DefaultExceptionStyleMap;
    static final Map<String, AnsiRenderer.Code[]> DefaultMessageStyleMap;
    private static final Map<String, Map<String, AnsiRenderer.Code[]>> PrefedinedStyleMaps;
    private final String beginToken;
    private final int beginTokenLen;
    private final String endToken;
    private final int endTokenLen;
    private final Map<String, AnsiRenderer.Code[]> styleMap;

    private static void put(Map<String, AnsiRenderer.Code[]> map, String string, AnsiRenderer.Code ... codeArray) {
        map.put(string, codeArray);
    }

    public JAnsiTextRenderer(String[] object, Map<String, AnsiRenderer.Code[]> stringArray) {
        String string = "@|";
        String string2 = "|@";
        if (((String[])object).length > 1) {
            String[] stringArray2 = object[1].split(" ");
            object = new HashMap(stringArray2.length + stringArray.size());
            object.putAll(stringArray);
            stringArray = stringArray2;
            int n = stringArray2.length;
            block10: for (int j = 0; j < n; ++j) {
                AnsiRenderer.Code[] codeArray = stringArray[j];
                Object object2 = codeArray.split("=");
                if (((String[])object2).length != 2) {
                    StatusLogger.getLogger().warn("{} parsing style \"{}\", expected format: StyleName=Code(,Code)*", (Object)this.getClass().getSimpleName(), (Object)codeArray);
                    continue;
                }
                String string3 = object2[0];
                if (((String[])(object2 = object2[1].split(","))).length == 0) {
                    StatusLogger.getLogger().warn("{} parsing style \"{}\", expected format: StyleName=Code(,Code)*", (Object)this.getClass().getSimpleName(), (Object)codeArray);
                    continue;
                }
                codeArray = string3;
                int n2 = -1;
                switch (codeArray.hashCode()) {
                    case -1199624464: {
                        if (!codeArray.equals("BeginToken")) break;
                        n2 = 0;
                        break;
                    }
                    case 1779889662: {
                        if (!codeArray.equals("EndToken")) break;
                        n2 = 1;
                        break;
                    }
                    case -1967473866: {
                        if (!codeArray.equals("StyleMapName")) break;
                        n2 = 2;
                    }
                }
                switch (n2) {
                    case 0: {
                        string = object2[0];
                        continue block10;
                    }
                    case 1: {
                        string2 = object2[0];
                        continue block10;
                    }
                    case 2: {
                        codeArray = object2[0];
                        object2 = PrefedinedStyleMaps.get(codeArray);
                        if (object2 != null) {
                            object.putAll(object2);
                            continue block10;
                        }
                        StatusLogger.getLogger().warn("Unknown predefined map name {}, pick one of {}", (Object)codeArray, (Object)null);
                        continue block10;
                    }
                    default: {
                        codeArray = new AnsiRenderer.Code[((String[])object2).length];
                        for (n2 = 0; n2 < codeArray.length; ++n2) {
                            codeArray[n2] = this.toCode(object2[n2]);
                        }
                        object.put(string3, codeArray);
                    }
                }
            }
        } else {
            object = stringArray;
        }
        this.styleMap = object;
        this.beginToken = string;
        this.endToken = string2;
        this.beginTokenLen = string.length();
        this.endTokenLen = string2.length();
    }

    public final Map<String, AnsiRenderer.Code[]> getStyleMap() {
        return this.styleMap;
    }

    private void render(Ansi ansi, AnsiRenderer.Code code) {
        if (code.isColor()) {
            if (code.isBackground()) {
                ansi.bg(code.getColor());
                return;
            }
            ansi.fg(code.getColor());
            return;
        }
        if (code.isAttribute()) {
            ansi.a(code.getAttribute());
        }
    }

    private void render(Ansi ansi, AnsiRenderer.Code ... codeArray) {
        for (AnsiRenderer.Code code : codeArray) {
            this.render(ansi, code);
        }
    }

    private String render(String string, String ... stringArray) {
        Ansi ansi = Ansi.ansi();
        for (String string2 : stringArray) {
            AnsiRenderer.Code[] codeArray = this.styleMap.get(string2);
            if (codeArray != null) {
                this.render(ansi, codeArray);
                continue;
            }
            this.render(ansi, this.toCode(string2));
        }
        return ansi.a(string).reset().toString();
    }

    @Override
    public final void render(String string, StringBuilder stringBuilder, String string2) {
        stringBuilder.append(this.render(string, string2));
    }

    @Override
    public final void render(StringBuilder stringBuilder, StringBuilder stringBuilder2) {
        int n = 0;
        while (true) {
            int n2;
            if ((n2 = stringBuilder.indexOf(this.beginToken, n)) == -1) {
                if (n == 0) {
                    stringBuilder2.append((CharSequence)stringBuilder);
                    return;
                }
                stringBuilder2.append(stringBuilder.substring(n, stringBuilder.length()));
                return;
            }
            stringBuilder2.append(stringBuilder.substring(n, n2));
            n = stringBuilder.indexOf(this.endToken, n2);
            if (n == -1) {
                stringBuilder2.append((CharSequence)stringBuilder);
                return;
            }
            Object object = stringBuilder.substring(n2 += this.beginTokenLen, n).split(" ", 2);
            if (((String[])object).length == 1) {
                stringBuilder2.append((CharSequence)stringBuilder);
                return;
            }
            object = this.render(object[1], object[0].split(","));
            stringBuilder2.append((String)object);
            n += this.endTokenLen;
        }
    }

    private AnsiRenderer.Code toCode(String string) {
        return AnsiRenderer.Code.valueOf((String)string.toUpperCase(Locale.ENGLISH));
    }

    public final String toString() {
        return "JAnsiMessageRenderer [beginToken=" + this.beginToken + ", beginTokenLen=" + this.beginTokenLen + ", endToken=" + this.endToken + ", endTokenLen=" + this.endTokenLen + ", styleMap=" + this.styleMap + "]";
    }

    static {
        HashMap<String, Map<String, AnsiRenderer.Code[]>> hashMap = new HashMap<String, Map<String, AnsiRenderer.Code[]>>();
        HashMap<String, AnsiRenderer.Code[]> hashMap2 = new HashMap<String, AnsiRenderer.Code[]>();
        JAnsiTextRenderer.put(hashMap2, "Prefix", AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "Name", AnsiRenderer.Code.BG_RED, AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "NameMessageSeparator", AnsiRenderer.Code.BG_RED, AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "Message", AnsiRenderer.Code.BG_RED, AnsiRenderer.Code.WHITE, AnsiRenderer.Code.BOLD);
        JAnsiTextRenderer.put(hashMap2, "At", AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "CauseLabel", AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "Text", AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "More", AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "Suppressed", AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.ClassName", AnsiRenderer.Code.YELLOW);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.ClassMethodSeparator", AnsiRenderer.Code.YELLOW);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.MethodName", AnsiRenderer.Code.YELLOW);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.NativeMethod", AnsiRenderer.Code.YELLOW);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.FileName", AnsiRenderer.Code.RED);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.LineNumber", AnsiRenderer.Code.RED);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.Container", AnsiRenderer.Code.RED);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.ContainerSeparator", AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.UnknownSource", AnsiRenderer.Code.RED);
        JAnsiTextRenderer.put(hashMap2, "ExtraClassInfo.Inexact", AnsiRenderer.Code.YELLOW);
        JAnsiTextRenderer.put(hashMap2, "ExtraClassInfo.Container", AnsiRenderer.Code.YELLOW);
        JAnsiTextRenderer.put(hashMap2, "ExtraClassInfo.ContainerSeparator", AnsiRenderer.Code.YELLOW);
        JAnsiTextRenderer.put(hashMap2, "ExtraClassInfo.Location", AnsiRenderer.Code.YELLOW);
        JAnsiTextRenderer.put(hashMap2, "ExtraClassInfo.Version", AnsiRenderer.Code.YELLOW);
        DefaultExceptionStyleMap = Collections.unmodifiableMap(hashMap2);
        hashMap.put("Spock", DefaultExceptionStyleMap);
        hashMap2 = new HashMap<String, AnsiRenderer.Code[]>();
        JAnsiTextRenderer.put(hashMap2, "Prefix", AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "Name", AnsiRenderer.Code.BG_RED, AnsiRenderer.Code.YELLOW, AnsiRenderer.Code.BOLD);
        JAnsiTextRenderer.put(hashMap2, "NameMessageSeparator", AnsiRenderer.Code.BG_RED, AnsiRenderer.Code.YELLOW);
        JAnsiTextRenderer.put(hashMap2, "Message", AnsiRenderer.Code.BG_RED, AnsiRenderer.Code.WHITE, AnsiRenderer.Code.BOLD);
        JAnsiTextRenderer.put(hashMap2, "At", AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "CauseLabel", AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "Text", AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "More", AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "Suppressed", AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.ClassName", AnsiRenderer.Code.BG_RED, AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.ClassMethodSeparator", AnsiRenderer.Code.BG_RED, AnsiRenderer.Code.YELLOW);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.MethodName", AnsiRenderer.Code.BG_RED, AnsiRenderer.Code.YELLOW);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.NativeMethod", AnsiRenderer.Code.BG_RED, AnsiRenderer.Code.YELLOW);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.FileName", AnsiRenderer.Code.RED);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.LineNumber", AnsiRenderer.Code.RED);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.Container", AnsiRenderer.Code.RED);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.ContainerSeparator", AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "StackTraceElement.UnknownSource", AnsiRenderer.Code.RED);
        JAnsiTextRenderer.put(hashMap2, "ExtraClassInfo.Inexact", AnsiRenderer.Code.YELLOW);
        JAnsiTextRenderer.put(hashMap2, "ExtraClassInfo.Container", AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "ExtraClassInfo.ContainerSeparator", AnsiRenderer.Code.WHITE);
        JAnsiTextRenderer.put(hashMap2, "ExtraClassInfo.Location", AnsiRenderer.Code.YELLOW);
        JAnsiTextRenderer.put(hashMap2, "ExtraClassInfo.Version", AnsiRenderer.Code.YELLOW);
        hashMap.put("Kirk", Collections.unmodifiableMap(hashMap2));
        DefaultMessageStyleMap = Collections.unmodifiableMap(new HashMap());
        PrefedinedStyleMaps = Collections.unmodifiableMap(hashMap);
    }
}

