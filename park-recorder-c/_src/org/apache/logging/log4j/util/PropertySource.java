/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.util.BiConsumer;

public interface PropertySource {
    public int getPriority();

    default public void forEach(BiConsumer<String, String> biConsumer) {
    }

    default public CharSequence getNormalForm(Iterable<? extends CharSequence> iterable) {
        return null;
    }

    default public String getProperty(String string) {
        return null;
    }

    default public boolean containsProperty(String string) {
        return false;
    }

    public static final class Util {
        private static final String PREFIXES = "(?i:^log4j2?[-._/]?|^org\\.apache\\.logging\\.log4j\\.)?";
        private static final Pattern PROPERTY_TOKENIZER = Pattern.compile("(?i:^log4j2?[-._/]?|^org\\.apache\\.logging\\.log4j\\.)?([A-Z]*[a-z0-9]+|[A-Z0-9]+)[-._/]?");
        private static final Map<CharSequence, List<CharSequence>> CACHE = new ConcurrentHashMap<CharSequence, List<CharSequence>>();

        public static List<CharSequence> tokenize(CharSequence charSequence) {
            if (CACHE.containsKey(charSequence)) {
                return CACHE.get(charSequence);
            }
            ArrayList<CharSequence> arrayList = new ArrayList<CharSequence>();
            Matcher matcher = PROPERTY_TOKENIZER.matcher(charSequence);
            while (matcher.find()) {
                arrayList.add(matcher.group(1).toLowerCase());
            }
            CACHE.put(charSequence, arrayList);
            return arrayList;
        }

        public static CharSequence joinAsCamelCase(Iterable<? extends CharSequence> object) {
            StringBuilder stringBuilder = new StringBuilder();
            boolean bl = true;
            object = object.iterator();
            while (object.hasNext()) {
                CharSequence charSequence = (CharSequence)object.next();
                if (bl) {
                    stringBuilder.append(charSequence);
                } else {
                    stringBuilder.append(Character.toUpperCase(charSequence.charAt(0)));
                    if (charSequence.length() > 1) {
                        stringBuilder.append(charSequence.subSequence(1, charSequence.length()));
                    }
                }
                bl = false;
            }
            return stringBuilder.toString();
        }

        private Util() {
        }
    }

    public static class Comparator
    implements Serializable,
    java.util.Comparator<PropertySource> {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(PropertySource propertySource, PropertySource propertySource2) {
            return Integer.compare(Objects.requireNonNull(propertySource).getPriority(), Objects.requireNonNull(propertySource2).getPriority());
        }
    }
}

