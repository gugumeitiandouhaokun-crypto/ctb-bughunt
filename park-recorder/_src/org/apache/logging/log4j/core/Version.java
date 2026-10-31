/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core;

public class Version {
    public static void main(String[] stringArray) {
        System.out.println(Version.getProductString());
    }

    public static String getProductString() {
        Package package_ = Version.class.getPackage();
        if (package_ == null) {
            return "Apache Log4j";
        }
        return String.format("%s %s", package_.getSpecificationTitle(), package_.getSpecificationVersion());
    }
}

