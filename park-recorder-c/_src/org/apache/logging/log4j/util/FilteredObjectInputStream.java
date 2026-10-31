/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class FilteredObjectInputStream
extends ObjectInputStream {
    private static final List<String> REQUIRED_JAVA_CLASSES = Arrays.asList("java.math.BigDecimal", "java.math.BigInteger", "java.rmi.MarshalledObject", "[B");
    private static final List<String> REQUIRED_JAVA_PACKAGES = Arrays.asList("java.lang.", "java.time", "java.util.", "org.apache.logging.log4j.", "[Lorg.apache.logging.log4j.");
    private final Collection<String> allowedClasses;

    public FilteredObjectInputStream() {
        this.allowedClasses = new HashSet<String>();
    }

    public FilteredObjectInputStream(InputStream inputStream) {
        super(inputStream);
        this.allowedClasses = new HashSet<String>();
    }

    public FilteredObjectInputStream(Collection<String> collection) {
        this.allowedClasses = collection;
    }

    public FilteredObjectInputStream(InputStream inputStream, Collection<String> collection) {
        super(inputStream);
        this.allowedClasses = collection;
    }

    public Collection<String> getAllowedClasses() {
        return this.allowedClasses;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) {
        String string = objectStreamClass.getName();
        if (!FilteredObjectInputStream.isAllowedByDefault(string) && !this.allowedClasses.contains(string)) {
            throw new InvalidObjectException("Class is not allowed for deserialization: ".concat(String.valueOf(string)));
        }
        return super.resolveClass(objectStreamClass);
    }

    private static boolean isAllowedByDefault(String string) {
        return FilteredObjectInputStream.isRequiredPackage(string) || REQUIRED_JAVA_CLASSES.contains(string);
    }

    private static boolean isRequiredPackage(String string) {
        for (String string2 : REQUIRED_JAVA_PACKAGES) {
            if (!string.startsWith(string2)) continue;
            return true;
        }
        return false;
    }
}

