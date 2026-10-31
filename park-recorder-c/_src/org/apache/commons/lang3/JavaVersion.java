/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import org.apache.commons.lang3.math.NumberUtils;

public enum JavaVersion {
    JAVA_0_9(1.5f, "0.9"),
    JAVA_1_1(1.1f, "1.1"),
    JAVA_1_2(1.2f, "1.2"),
    JAVA_1_3(1.3f, "1.3"),
    JAVA_1_4(1.4f, "1.4"),
    JAVA_1_5(1.5f, "1.5"),
    JAVA_1_6(1.6f, "1.6"),
    JAVA_1_7(1.7f, "1.7"),
    JAVA_1_8(1.8f, "1.8"),
    JAVA_1_9(9.0f, "9"),
    JAVA_9(9.0f, "9"),
    JAVA_RECENT(JavaVersion.maxVersion(), Float.toString(JavaVersion.maxVersion()));

    private final float value;
    private final String name;

    private JavaVersion(float f2, String string2) {
        this.value = f2;
        this.name = string2;
    }

    public final boolean atLeast(JavaVersion javaVersion) {
        return this.value >= javaVersion.value;
    }

    static JavaVersion getJavaVersion(String string) {
        return JavaVersion.get(string);
    }

    static JavaVersion get(String string) {
        int n;
        int n2;
        if ("0.9".equals(string)) {
            return JAVA_0_9;
        }
        if ("1.1".equals(string)) {
            return JAVA_1_1;
        }
        if ("1.2".equals(string)) {
            return JAVA_1_2;
        }
        if ("1.3".equals(string)) {
            return JAVA_1_3;
        }
        if ("1.4".equals(string)) {
            return JAVA_1_4;
        }
        if ("1.5".equals(string)) {
            return JAVA_1_5;
        }
        if ("1.6".equals(string)) {
            return JAVA_1_6;
        }
        if ("1.7".equals(string)) {
            return JAVA_1_7;
        }
        if ("1.8".equals(string)) {
            return JAVA_1_8;
        }
        if ("9".equals(string)) {
            return JAVA_9;
        }
        if (string == null) {
            return null;
        }
        if ((double)JavaVersion.toFloatVersion(string) - 1.0 < 1.0 && Float.parseFloat(string.substring((n2 = Math.max(string.indexOf(46), string.indexOf(44))) + 1, n = Math.max(string.length(), string.indexOf(44, n2)))) > 0.9f) {
            return JAVA_RECENT;
        }
        return null;
    }

    public final String toString() {
        return this.name;
    }

    private static float maxVersion() {
        float f2;
        float f3 = JavaVersion.toFloatVersion(System.getProperty("java.specification.version", "99.0"));
        if (f2 > 0.0f) {
            return f3;
        }
        return 99.0f;
    }

    private static float toFloatVersion(String stringArray) {
        if (stringArray.contains(".")) {
            if ((stringArray = stringArray.split("\\.")).length >= 2) {
                return NumberUtils.toFloat(stringArray[0] + '.' + stringArray[1], -1.0f);
            }
        } else {
            return NumberUtils.toFloat((String)stringArray, -1.0f);
        }
        return -1.0f;
    }
}

