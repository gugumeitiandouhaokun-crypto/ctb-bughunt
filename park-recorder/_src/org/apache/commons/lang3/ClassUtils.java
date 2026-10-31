/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.mutable.MutableObject;

public class ClassUtils {
    public static final char PACKAGE_SEPARATOR_CHAR = '.';
    public static final String PACKAGE_SEPARATOR;
    public static final char INNER_CLASS_SEPARATOR_CHAR = '$';
    public static final String INNER_CLASS_SEPARATOR;
    private static final Map<String, Class<?>> namePrimitiveMap;
    private static final Map<Class<?>, Class<?>> primitiveWrapperMap;
    private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap;
    private static final Map<String, String> abbreviationMap;
    private static final Map<String, String> reverseAbbreviationMap;

    public static String getShortClassName(Object object, String string) {
        if (object == null) {
            return string;
        }
        return ClassUtils.getShortClassName(object.getClass());
    }

    public static String getShortClassName(Class<?> clazz) {
        if (clazz == null) {
            return "";
        }
        return ClassUtils.getShortClassName(clazz.getName());
    }

    public static String getShortClassName(String string) {
        int n;
        if (StringUtils.isEmpty(string)) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (string.startsWith("[")) {
            while (string.charAt(0) == '[') {
                string = string.substring(1);
                stringBuilder.append("[]");
            }
            if (string.charAt(0) == 'L') {
                String string2 = string;
                if (string2.charAt(string2.length() - 1) == ';') {
                    string = string.substring(1, string.length() - 1);
                }
            }
            if (reverseAbbreviationMap.containsKey(string)) {
                string = reverseAbbreviationMap.get(string);
            }
        }
        int n2 = string.indexOf(36, (n = string.lastIndexOf(46)) == -1 ? 0 : n + 1);
        string = string.substring(n + 1);
        if (n2 != -1) {
            string = string.replace('$', '.');
        }
        return string + stringBuilder;
    }

    public static String getSimpleName(Class<?> clazz) {
        if (clazz == null) {
            return "";
        }
        return clazz.getSimpleName();
    }

    public static String getSimpleName(Object object, String string) {
        if (object == null) {
            return string;
        }
        return ClassUtils.getSimpleName(object.getClass());
    }

    public static String getPackageName(Object object, String string) {
        if (object == null) {
            return string;
        }
        return ClassUtils.getPackageName(object.getClass());
    }

    public static String getPackageName(Class<?> clazz) {
        if (clazz == null) {
            return "";
        }
        return ClassUtils.getPackageName(clazz.getName());
    }

    public static String getPackageName(String string) {
        int n;
        if (StringUtils.isEmpty(string)) {
            return "";
        }
        while (string.charAt(0) == '[') {
            string = string.substring(1);
        }
        if (string.charAt(0) == 'L') {
            String string2 = string;
            if (string2.charAt(string2.length() - 1) == ';') {
                string = string.substring(1);
            }
        }
        if ((n = string.lastIndexOf(46)) == -1) {
            return "";
        }
        return string.substring(0, n);
    }

    public static String getAbbreviatedName(Class<?> clazz, int n) {
        if (clazz == null) {
            return "";
        }
        return ClassUtils.getAbbreviatedName(clazz.getName(), n);
    }

    public static String getAbbreviatedName(String string, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("len must be > 0");
        }
        if (string == null) {
            return "";
        }
        int n2 = StringUtils.countMatches((CharSequence)string, '.');
        Object[] objectArray = new String[n2 + 1];
        int n3 = string.length() - 1;
        for (int j = n2; j >= 0; --j) {
            int n4 = string.lastIndexOf(46, n3);
            String string2 = string.substring(n4 + 1, n3 + 1);
            n -= string2.length();
            if (j > 0) {
                --n;
            }
            objectArray[j] = j == n2 ? string2 : (n > 0 ? string2 : string2.substring(0, 1));
            n3 = n4 - 1;
        }
        return StringUtils.join(objectArray, '.');
    }

    public static List<Class<?>> getAllSuperclasses(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (clazz = clazz.getSuperclass(); clazz != null; clazz = clazz.getSuperclass()) {
            arrayList.add(clazz);
        }
        return arrayList;
    }

    public static List<Class<?>> getAllInterfaces(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        ClassUtils.getAllInterfaces(clazz, linkedHashSet);
        return new ArrayList(linkedHashSet);
    }

    private static void getAllInterfaces(Class<?> clazz, HashSet<Class<?>> hashSet) {
        while (clazz != null) {
            for (Class<?> clazz2 : clazz.getInterfaces()) {
                if (!hashSet.add(clazz2)) continue;
                ClassUtils.getAllInterfaces(clazz2, hashSet);
            }
            clazz = clazz.getSuperclass();
        }
    }

    public static List<Class<?>> convertClassNamesToClasses(List<String> object) {
        if (object == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList(object.size());
        object = object.iterator();
        while (object.hasNext()) {
            String string = (String)object.next();
            try {
                arrayList.add(Class.forName(string));
            }
            catch (Exception exception) {
                arrayList.add(null);
            }
        }
        return arrayList;
    }

    public static List<String> convertClassesToClassNames(List<Class<?>> object) {
        if (object == null) {
            return null;
        }
        ArrayList<String> arrayList = new ArrayList<String>(object.size());
        object = object.iterator();
        while (object.hasNext()) {
            Class clazz = (Class)object.next();
            if (clazz == null) {
                arrayList.add(null);
                continue;
            }
            arrayList.add(clazz.getName());
        }
        return arrayList;
    }

    public static boolean isAssignable(Class<?>[] classArray, Class<?> ... classArray2) {
        return ClassUtils.isAssignable(classArray, classArray2, SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_5));
    }

    public static boolean isAssignable(Class<?>[] classArray, Class<?>[] classArray2, boolean bl) {
        if (!ArrayUtils.isSameLength(classArray, classArray2)) {
            return false;
        }
        if (classArray == null) {
            classArray = ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        if (classArray2 == null) {
            classArray2 = ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        for (int j = 0; j < classArray.length; ++j) {
            if (ClassUtils.isAssignable(classArray[j], classArray2[j], bl)) continue;
            return false;
        }
        return true;
    }

    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return clazz.isPrimitive() || ClassUtils.isPrimitiveWrapper(clazz);
    }

    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        return wrapperPrimitiveMap.containsKey(clazz);
    }

    public static boolean isAssignable(Class<?> clazz, Class<?> clazz2) {
        return ClassUtils.isAssignable(clazz, clazz2, SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_5));
    }

    public static boolean isAssignable(Class<?> clazz, Class<?> clazz2, boolean bl) {
        if (clazz2 == null) {
            return false;
        }
        if (clazz == null) {
            return !clazz2.isPrimitive();
        }
        if (bl) {
            if (clazz.isPrimitive() && !clazz2.isPrimitive() && (clazz = ClassUtils.primitiveToWrapper(clazz)) == null) {
                return false;
            }
            if (clazz2.isPrimitive() && !clazz.isPrimitive() && (clazz = ClassUtils.wrapperToPrimitive(clazz)) == null) {
                return false;
            }
        }
        if (clazz.equals(clazz2)) {
            return true;
        }
        if (clazz.isPrimitive()) {
            if (!clazz2.isPrimitive()) {
                return false;
            }
            if (Integer.TYPE.equals(clazz)) {
                return Long.TYPE.equals(clazz2) || Float.TYPE.equals(clazz2) || Double.TYPE.equals(clazz2);
            }
            if (Long.TYPE.equals(clazz)) {
                return Float.TYPE.equals(clazz2) || Double.TYPE.equals(clazz2);
            }
            if (Boolean.TYPE.equals(clazz)) {
                return false;
            }
            if (Double.TYPE.equals(clazz)) {
                return false;
            }
            if (Float.TYPE.equals(clazz)) {
                return Double.TYPE.equals(clazz2);
            }
            if (Character.TYPE.equals(clazz)) {
                return Integer.TYPE.equals(clazz2) || Long.TYPE.equals(clazz2) || Float.TYPE.equals(clazz2) || Double.TYPE.equals(clazz2);
            }
            if (Short.TYPE.equals(clazz)) {
                return Integer.TYPE.equals(clazz2) || Long.TYPE.equals(clazz2) || Float.TYPE.equals(clazz2) || Double.TYPE.equals(clazz2);
            }
            if (Byte.TYPE.equals(clazz)) {
                return Short.TYPE.equals(clazz2) || Integer.TYPE.equals(clazz2) || Long.TYPE.equals(clazz2) || Float.TYPE.equals(clazz2) || Double.TYPE.equals(clazz2);
            }
            return false;
        }
        return clazz2.isAssignableFrom(clazz);
    }

    public static Class<?> primitiveToWrapper(Class<?> clazz) {
        Class<?> clazz2 = clazz;
        if (clazz != null && clazz.isPrimitive()) {
            clazz2 = primitiveWrapperMap.get(clazz);
        }
        return clazz2;
    }

    public static Class<?>[] primitivesToWrappers(Class<?> ... classArray) {
        if (classArray == null) {
            return null;
        }
        if (classArray.length == 0) {
            return classArray;
        }
        Class[] classArray2 = new Class[classArray.length];
        for (int j = 0; j < classArray.length; ++j) {
            classArray2[j] = ClassUtils.primitiveToWrapper(classArray[j]);
        }
        return classArray2;
    }

    public static Class<?> wrapperToPrimitive(Class<?> clazz) {
        return wrapperPrimitiveMap.get(clazz);
    }

    public static Class<?>[] wrappersToPrimitives(Class<?> ... classArray) {
        if (classArray == null) {
            return null;
        }
        if (classArray.length == 0) {
            return classArray;
        }
        Class[] classArray2 = new Class[classArray.length];
        for (int j = 0; j < classArray.length; ++j) {
            classArray2[j] = ClassUtils.wrapperToPrimitive(classArray[j]);
        }
        return classArray2;
    }

    public static boolean isInnerClass(Class<?> clazz) {
        return clazz != null && clazz.getEnclosingClass() != null;
    }

    public static Class<?> getClass(ClassLoader classLoader, String string, boolean bl) {
        try {
            Class<?> clazz = namePrimitiveMap.containsKey(string) ? namePrimitiveMap.get(string) : Class.forName(ClassUtils.toCanonicalName(string), bl, classLoader);
            return clazz;
        }
        catch (ClassNotFoundException classNotFoundException) {
            int n = string.lastIndexOf(46);
            if (n != -1) {
                try {
                    return ClassUtils.getClass(classLoader, string.substring(0, n) + '$' + string.substring(n + 1), bl);
                }
                catch (ClassNotFoundException classNotFoundException2) {}
            }
            throw classNotFoundException;
        }
    }

    public static Class<?> getClass(ClassLoader classLoader, String string) {
        return ClassUtils.getClass(classLoader, string, true);
    }

    public static Class<?> getClass(String string) {
        return ClassUtils.getClass(string, true);
    }

    public static Class<?> getClass(String string, boolean bl) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return ClassUtils.getClass(classLoader == null ? ClassUtils.class.getClassLoader() : classLoader, string, bl);
    }

    public static Method getPublicMethod(Class<?> object, String string, Class<?> ... classArray) {
        Object object2 = ((Class)object).getMethod(string, classArray);
        if (Modifier.isPublic(((Method)object2).getDeclaringClass().getModifiers())) {
            return object2;
        }
        object2 = new ArrayList();
        object2.addAll(ClassUtils.getAllInterfaces(object));
        object2.addAll(ClassUtils.getAllSuperclasses(object));
        object = object2.iterator();
        while (object.hasNext()) {
            object2 = (Class)object.next();
            if (!Modifier.isPublic(((Class)object2).getModifiers())) continue;
            try {
                object2 = ((Class)object2).getMethod(string, classArray);
            }
            catch (NoSuchMethodException noSuchMethodException) {
                continue;
            }
            if (!Modifier.isPublic(((Method)object2).getDeclaringClass().getModifiers())) continue;
            return object2;
        }
        throw new NoSuchMethodException("Can't find a public method for " + string + " " + ArrayUtils.toString(classArray));
    }

    private static String toCanonicalName(String string) {
        if ((string = StringUtils.deleteWhitespace(string)) == null) {
            throw new NullPointerException("className must not be null.");
        }
        if (string.endsWith("[]")) {
            StringBuilder stringBuilder = new StringBuilder();
            while (string.endsWith("[]")) {
                string = string.substring(0, string.length() - 2);
                stringBuilder.append("[");
            }
            String string2 = abbreviationMap.get(string);
            if (string2 != null) {
                stringBuilder.append(string2);
            } else {
                stringBuilder.append("L").append(string).append(";");
            }
            string = stringBuilder.toString();
        }
        return string;
    }

    public static Class<?>[] toClass(Object ... objectArray) {
        if (objectArray == null) {
            return null;
        }
        if (objectArray.length == 0) {
            return ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        Class[] classArray = new Class[objectArray.length];
        for (int j = 0; j < objectArray.length; ++j) {
            classArray[j] = objectArray[j] == null ? null : objectArray[j].getClass();
        }
        return classArray;
    }

    public static String getShortCanonicalName(Object object, String string) {
        if (object == null) {
            return string;
        }
        return ClassUtils.getShortCanonicalName(object.getClass().getName());
    }

    public static String getShortCanonicalName(Class<?> clazz) {
        if (clazz == null) {
            return "";
        }
        return ClassUtils.getShortCanonicalName(clazz.getName());
    }

    public static String getShortCanonicalName(String string) {
        return ClassUtils.getShortClassName(ClassUtils.getCanonicalName(string));
    }

    public static String getPackageCanonicalName(Object object, String string) {
        if (object == null) {
            return string;
        }
        return ClassUtils.getPackageCanonicalName(object.getClass().getName());
    }

    public static String getPackageCanonicalName(Class<?> clazz) {
        if (clazz == null) {
            return "";
        }
        return ClassUtils.getPackageCanonicalName(clazz.getName());
    }

    public static String getPackageCanonicalName(String string) {
        return ClassUtils.getPackageName(ClassUtils.getCanonicalName(string));
    }

    private static String getCanonicalName(String charSequence) {
        if ((charSequence = StringUtils.deleteWhitespace((String)charSequence)) == null) {
            return null;
        }
        int n = 0;
        while (((String)charSequence).startsWith("[")) {
            ++n;
            charSequence = ((String)charSequence).substring(1);
        }
        if (n <= 0) {
            return charSequence;
        }
        if (((String)charSequence).startsWith("L")) {
            charSequence = ((String)charSequence).substring(1, ((String)charSequence).endsWith(";") ? ((String)charSequence).length() - 1 : ((String)charSequence).length());
        } else if (((String)charSequence).length() > 0) {
            charSequence = reverseAbbreviationMap.get(((String)charSequence).substring(0, 1));
        }
        charSequence = new StringBuilder((String)charSequence);
        for (int j = 0; j < n; ++j) {
            ((StringBuilder)charSequence).append("[]");
        }
        return ((StringBuilder)charSequence).toString();
    }

    public static Iterable<Class<?>> hierarchy(Class<?> clazz) {
        return ClassUtils.hierarchy(clazz, Interfaces.EXCLUDE);
    }

    public static Iterable<Class<?>> hierarchy(Class<?> object, Interfaces interfaces) {
        object = new Iterable<Class<?>>((Class)object){
            final /* synthetic */ Class val$type;
            {
                this.val$type = clazz;
            }

            @Override
            public final Iterator<Class<?>> iterator() {
                final MutableObject<Class> mutableObject = new MutableObject<Class>(this.val$type);
                return new Iterator<Class<?>>(){

                    @Override
                    public boolean hasNext() {
                        return mutableObject.getValue() != null;
                    }

                    @Override
                    public Class<?> next() {
                        Class clazz = (Class)mutableObject.getValue();
                        mutableObject.setValue(clazz.getSuperclass());
                        return clazz;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
        if (interfaces != Interfaces.INCLUDE) {
            return object;
        }
        return new Iterable<Class<?>>((Iterable)object){
            final /* synthetic */ Iterable val$classes;
            {
                this.val$classes = iterable;
            }

            @Override
            public final Iterator<Class<?>> iterator() {
                final HashSet hashSet = new HashSet();
                final Iterator iterator = this.val$classes.iterator();
                return new Iterator<Class<?>>(){
                    Iterator<Class<?>> interfaces = Collections.emptySet().iterator();

                    @Override
                    public boolean hasNext() {
                        return this.interfaces.hasNext() || iterator.hasNext();
                    }

                    @Override
                    public Class<?> next() {
                        if (this.interfaces.hasNext()) {
                            Class<?> clazz = this.interfaces.next();
                            hashSet.add(clazz);
                            return clazz;
                        }
                        Class clazz = (Class)iterator.next();
                        LinkedHashSet linkedHashSet = new LinkedHashSet();
                        this.walkInterfaces(linkedHashSet, clazz);
                        this.interfaces = linkedHashSet.iterator();
                        return clazz;
                    }

                    private void walkInterfaces(Set<Class<?>> set, Class<?> classArray) {
                        for (Class<?> clazz : classArray.getInterfaces()) {
                            if (!hashSet.contains(clazz)) {
                                set.add(clazz);
                            }
                            this.walkInterfaces(set, clazz);
                        }
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    static {
        Map.Entry<Class<?>, Class<?>> entry22;
        PACKAGE_SEPARATOR = ".";
        INNER_CLASS_SEPARATOR = "$";
        namePrimitiveMap = new HashMap();
        namePrimitiveMap.put("boolean", Boolean.TYPE);
        namePrimitiveMap.put("byte", Byte.TYPE);
        namePrimitiveMap.put("char", Character.TYPE);
        namePrimitiveMap.put("short", Short.TYPE);
        namePrimitiveMap.put("int", Integer.TYPE);
        namePrimitiveMap.put("long", Long.TYPE);
        namePrimitiveMap.put("double", Double.TYPE);
        namePrimitiveMap.put("float", Float.TYPE);
        namePrimitiveMap.put("void", Void.TYPE);
        primitiveWrapperMap = new HashMap();
        primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
        primitiveWrapperMap.put(Byte.TYPE, Byte.class);
        primitiveWrapperMap.put(Character.TYPE, Character.class);
        primitiveWrapperMap.put(Short.TYPE, Short.class);
        primitiveWrapperMap.put(Integer.TYPE, Integer.class);
        primitiveWrapperMap.put(Long.TYPE, Long.class);
        primitiveWrapperMap.put(Double.TYPE, Double.class);
        primitiveWrapperMap.put(Float.TYPE, Float.class);
        Class<Void> clazz = Void.TYPE;
        primitiveWrapperMap.put(clazz, clazz);
        wrapperPrimitiveMap = new HashMap();
        for (Map.Entry<Class<?>, Class<?>> entry22 : primitiveWrapperMap.entrySet()) {
            Class<?> object;
            Class<?> clazz2 = entry22.getKey();
            if (clazz2.equals(object = entry22.getValue())) continue;
            wrapperPrimitiveMap.put(object, clazz2);
        }
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("int", "I");
        hashMap.put("boolean", "Z");
        hashMap.put("float", "F");
        hashMap.put("long", "J");
        hashMap.put("short", "S");
        hashMap.put("byte", "B");
        hashMap.put("double", "D");
        hashMap.put("char", "C");
        entry22 = new HashMap();
        for (Map.Entry entry : hashMap.entrySet()) {
            entry22.put((Class<?>)entry.getValue(), (Class<?>)entry.getKey());
        }
        abbreviationMap = Collections.unmodifiableMap(hashMap);
        reverseAbbreviationMap = Collections.unmodifiableMap(entry22);
    }

    public static enum Interfaces {
        INCLUDE,
        EXCLUDE;

    }
}

