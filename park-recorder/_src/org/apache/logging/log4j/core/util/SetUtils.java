/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.util.HashSet;
import java.util.Set;

public final class SetUtils {
    private SetUtils() {
    }

    public static String[] prefixSet(Set<String> object, String string) {
        HashSet<String> hashSet = new HashSet<String>();
        object = object.iterator();
        while (object.hasNext()) {
            String string2 = (String)object.next();
            if (!string2.startsWith(string)) continue;
            hashSet.add(string2);
        }
        HashSet<String> hashSet2 = hashSet;
        return hashSet2.toArray(new String[hashSet2.size()]);
    }
}

