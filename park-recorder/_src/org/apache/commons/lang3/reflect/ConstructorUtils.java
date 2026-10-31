/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.MemberUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

public class ConstructorUtils {
    public static <T> T invokeConstructor(Class<T> clazz, Object ... objectArray) {
        objectArray = ArrayUtils.nullToEmpty(objectArray);
        Class<?>[] classArray = ClassUtils.toClass(objectArray);
        return ConstructorUtils.invokeConstructor(clazz, objectArray, classArray);
    }

    public static <T> T invokeConstructor(Class<T> classArray, Object[] objectArray, Class<?>[] object) {
        objectArray = ArrayUtils.nullToEmpty(objectArray);
        object = ArrayUtils.nullToEmpty(object);
        if ((object = ConstructorUtils.getMatchingAccessibleConstructor(classArray, object)) == null) {
            throw new NoSuchMethodException("No such accessible constructor on object: " + classArray.getName());
        }
        if (((Constructor)object).isVarArgs()) {
            classArray = ((Constructor)object).getParameterTypes();
            objectArray = MethodUtils.getVarArgs(objectArray, classArray);
        }
        return ((Constructor)object).newInstance(objectArray);
    }

    public static <T> T invokeExactConstructor(Class<T> clazz, Object ... objectArray) {
        objectArray = ArrayUtils.nullToEmpty(objectArray);
        Class<?>[] classArray = ClassUtils.toClass(objectArray);
        return ConstructorUtils.invokeExactConstructor(clazz, objectArray, classArray);
    }

    public static <T> T invokeExactConstructor(Class<T> clazz, Object[] objectArray, Class<?>[] object) {
        objectArray = ArrayUtils.nullToEmpty(objectArray);
        object = ArrayUtils.nullToEmpty(object);
        if ((object = ConstructorUtils.getAccessibleConstructor(clazz, object)) == null) {
            throw new NoSuchMethodException("No such accessible constructor on object: " + clazz.getName());
        }
        return ((Constructor)object).newInstance(objectArray);
    }

    public static <T> Constructor<T> getAccessibleConstructor(Class<T> clazz, Class<?> ... classArray) {
        Validate.notNull(clazz, "class cannot be null", new Object[0]);
        try {
            return ConstructorUtils.getAccessibleConstructor(clazz.getConstructor(classArray));
        }
        catch (NoSuchMethodException noSuchMethodException) {
            return null;
        }
    }

    public static <T> Constructor<T> getAccessibleConstructor(Constructor<T> constructor) {
        Validate.notNull(constructor, "constructor cannot be null", new Object[0]);
        if (MemberUtils.isAccessible(constructor) && ConstructorUtils.isAccessible(constructor.getDeclaringClass())) {
            return constructor;
        }
        return null;
    }

    public static <T> Constructor<T> getMatchingAccessibleConstructor(Class<T> constructorArray, Class<?> ... classArray) {
        Validate.notNull(constructorArray, "class cannot be null", new Object[0]);
        try {
            Constructor<T> constructor = constructorArray.getConstructor(classArray);
            MemberUtils.setAccessibleWorkaround(constructor);
            return constructor;
        }
        catch (NoSuchMethodException noSuchMethodException) {
            Constructor<?> constructor = null;
            for (Constructor<?> constructor2 : constructorArray.getConstructors()) {
                if (!MemberUtils.isMatchingConstructor(constructor2, classArray) || (constructor2 = ConstructorUtils.getAccessibleConstructor(constructor2)) == null) continue;
                MemberUtils.setAccessibleWorkaround(constructor2);
                if (constructor != null && MemberUtils.compareConstructorFit(constructor2, constructor, classArray) >= 0) continue;
                constructor = constructor2;
            }
            return constructor;
        }
    }

    private static boolean isAccessible(Class<?> clazz) {
        while (clazz != null) {
            if (!Modifier.isPublic(clazz.getModifiers())) {
                return false;
            }
            clazz = clazz.getEnclosingClass();
        }
        return true;
    }
}

