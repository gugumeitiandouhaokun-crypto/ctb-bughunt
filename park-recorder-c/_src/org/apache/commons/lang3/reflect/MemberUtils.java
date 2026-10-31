/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.reflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.apache.commons.lang3.ClassUtils;

abstract class MemberUtils {
    private static final int ACCESS_TEST = 7;
    private static final Class<?>[] ORDERED_PRIMITIVE_TYPES = new Class[]{Byte.TYPE, Short.TYPE, Character.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE};

    MemberUtils() {
    }

    static boolean setAccessibleWorkaround(AccessibleObject accessibleObject) {
        if (accessibleObject == null || accessibleObject.isAccessible()) {
            return false;
        }
        Member member = (Member)((Object)accessibleObject);
        if (!accessibleObject.isAccessible() && Modifier.isPublic(member.getModifiers()) && MemberUtils.isPackageAccess(member.getDeclaringClass().getModifiers())) {
            try {
                accessibleObject.setAccessible(true);
                return true;
            }
            catch (SecurityException securityException) {}
        }
        return false;
    }

    static boolean isPackageAccess(int n) {
        return (n & 7) == 0;
    }

    static boolean isAccessible(Member member) {
        return member != null && Modifier.isPublic(member.getModifiers()) && !member.isSynthetic();
    }

    static int compareConstructorFit(Constructor<?> constructor, Constructor<?> constructor2, Class<?>[] classArray) {
        return MemberUtils.compareParameterTypes(Executable.of(constructor), Executable.of(constructor2), classArray);
    }

    static int compareMethodFit(Method method, Method method2, Class<?>[] classArray) {
        return MemberUtils.compareParameterTypes(Executable.of(method), Executable.of(method2), classArray);
    }

    private static int compareParameterTypes(Executable executable, Executable executable2, Class<?>[] classArray) {
        float f2;
        float f3 = MemberUtils.getTotalTransformationCost(classArray, executable);
        if (f3 < (f2 = MemberUtils.getTotalTransformationCost(classArray, executable2))) {
            return -1;
        }
        if (f2 < f3) {
            return 1;
        }
        return 0;
    }

    private static float getTotalTransformationCost(Class<?>[] classArray, Executable executable) {
        long l;
        Object object = executable.getParameterTypes();
        int n = executable.isVarArgs();
        float f2 = 0.0f;
        long l2 = l = n != 0 ? (long)(((Class<?>[])object).length - 1) : (long)((Class<?>[])object).length;
        if ((long)classArray.length < l) {
            return Float.MAX_VALUE;
        }
        int n2 = 0;
        while ((long)n2 < l) {
            f2 += MemberUtils.getObjectTransformationCost(classArray[n2], object[n2]);
            ++n2;
        }
        if (n != 0) {
            n2 = classArray.length < ((Class<?>[])object).length ? 1 : 0;
            n = classArray.length == ((Class<?>[])object).length && classArray[classArray.length - 1].isArray() ? 1 : 0;
            Class<?> clazz = object[((Class<?>[])object).length - 1].getComponentType();
            if (n2 != 0) {
                f2 += MemberUtils.getObjectTransformationCost(clazz, Object.class) + 0.001f;
            } else if (n != 0) {
                Class<?> clazz2 = classArray[classArray.length - 1].getComponentType();
                f2 += MemberUtils.getObjectTransformationCost(clazz2, clazz) + 0.001f;
            } else {
                for (n = ((Class<?>[])object).length - 1; n < classArray.length; ++n) {
                    object = classArray[n];
                    f2 += MemberUtils.getObjectTransformationCost(object, clazz) + 0.001f;
                }
            }
        }
        return f2;
    }

    private static float getObjectTransformationCost(Class<?> clazz, Class<?> clazz2) {
        if (clazz2.isPrimitive()) {
            return MemberUtils.getPrimitivePromotionCost(clazz, clazz2);
        }
        float f2 = 0.0f;
        while (clazz != null && !clazz2.equals(clazz)) {
            if (clazz2.isInterface() && ClassUtils.isAssignable(clazz, clazz2)) {
                f2 += 0.25f;
                break;
            }
            f2 += 1.0f;
            clazz = clazz.getSuperclass();
        }
        if (clazz == null) {
            f2 += 1.5f;
        }
        return f2;
    }

    private static float getPrimitivePromotionCost(Class<?> clazz, Class<?> clazz2) {
        float f2 = 0.0f;
        if (!clazz.isPrimitive()) {
            f2 = 0.1f;
            clazz = ClassUtils.wrapperToPrimitive(clazz);
        }
        for (int i2 = 0; clazz != clazz2 && i2 < ORDERED_PRIMITIVE_TYPES.length; ++i2) {
            if (clazz != ORDERED_PRIMITIVE_TYPES[i2]) continue;
            f2 += 0.1f;
            if (i2 >= ORDERED_PRIMITIVE_TYPES.length - 1) continue;
            clazz = ORDERED_PRIMITIVE_TYPES[i2 + 1];
        }
        return f2;
    }

    static boolean isMatchingMethod(Method method, Class<?>[] classArray) {
        return MemberUtils.isMatchingExecutable(Executable.of(method), classArray);
    }

    static boolean isMatchingConstructor(Constructor<?> constructor, Class<?>[] classArray) {
        return MemberUtils.isMatchingExecutable(Executable.of(constructor), classArray);
    }

    private static boolean isMatchingExecutable(Executable executable, Class<?>[] classArray) {
        Object object = executable.getParameterTypes();
        if (executable.isVarArgs()) {
            int n;
            for (n = 0; n < ((Class<?>[])object).length - 1 && n < classArray.length; ++n) {
                if (ClassUtils.isAssignable(classArray[n], object[n], true)) continue;
                return false;
            }
            object = object[((Class<?>[])object).length - 1].getComponentType();
            while (n < classArray.length) {
                if (!ClassUtils.isAssignable(classArray[n], object, true)) {
                    return false;
                }
                ++n;
            }
            return true;
        }
        return ClassUtils.isAssignable(classArray, object, true);
    }

    static final class Executable {
        private final Class<?>[] parameterTypes;
        private final boolean isVarArgs;

        private static Executable of(Method method) {
            return new Executable(method);
        }

        private static Executable of(Constructor<?> constructor) {
            return new Executable(constructor);
        }

        private Executable(Method method) {
            this.parameterTypes = method.getParameterTypes();
            this.isVarArgs = method.isVarArgs();
        }

        private Executable(Constructor<?> constructor) {
            this.parameterTypes = constructor.getParameterTypes();
            this.isVarArgs = constructor.isVarArgs();
        }

        public final Class<?>[] getParameterTypes() {
            return this.parameterTypes;
        }

        public final boolean isVarArgs() {
            return this.isVarArgs;
        }
    }
}

