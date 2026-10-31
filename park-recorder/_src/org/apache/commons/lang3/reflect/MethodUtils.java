/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.MemberUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

public class MethodUtils {
    public static Object invokeMethod(Object object, String string) {
        return MethodUtils.invokeMethod(object, string, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
    }

    public static Object invokeMethod(Object object, boolean bl, String string) {
        return MethodUtils.invokeMethod(object, bl, string, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
    }

    public static Object invokeMethod(Object object, String string, Object ... objectArray) {
        objectArray = ArrayUtils.nullToEmpty(objectArray);
        Class<?>[] classArray = ClassUtils.toClass(objectArray);
        return MethodUtils.invokeMethod(object, string, objectArray, classArray);
    }

    public static Object invokeMethod(Object object, boolean bl, String string, Object ... objectArray) {
        objectArray = ArrayUtils.nullToEmpty(objectArray);
        Class<?>[] classArray = ClassUtils.toClass(objectArray);
        return MethodUtils.invokeMethod(object, bl, string, objectArray, classArray);
    }

    public static Object invokeMethod(Object object, boolean bl, String string, Object[] objectArray, Class<?>[] classArray) {
        classArray = ArrayUtils.nullToEmpty(classArray);
        objectArray = ArrayUtils.nullToEmpty(objectArray);
        AccessibleObject accessibleObject = null;
        boolean bl2 = false;
        try {
            String string2;
            if (bl) {
                string2 = "No such method: ";
                accessibleObject = MethodUtils.getMatchingMethod(object.getClass(), string, classArray);
                if (accessibleObject != null && !(bl2 = accessibleObject.isAccessible())) {
                    accessibleObject.setAccessible(true);
                }
            } else {
                string2 = "No such accessible method: ";
                accessibleObject = MethodUtils.getMatchingAccessibleMethod(object.getClass(), string, classArray);
            }
            if (accessibleObject == null) {
                throw new NoSuchMethodException(string2 + string + "() on object: " + object.getClass().getName());
            }
            objectArray = MethodUtils.toVarArgs((Method)accessibleObject, objectArray);
            object = ((Method)accessibleObject).invoke(object, objectArray);
        }
        finally {
            if (accessibleObject != null && bl && accessibleObject.isAccessible() != bl2) {
                accessibleObject.setAccessible(bl2);
            }
        }
        return object;
    }

    public static Object invokeMethod(Object object, String string, Object[] objectArray, Class<?>[] classArray) {
        return MethodUtils.invokeMethod(object, false, string, objectArray, classArray);
    }

    public static Object invokeExactMethod(Object object, String string) {
        return MethodUtils.invokeExactMethod(object, string, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
    }

    public static Object invokeExactMethod(Object object, String string, Object ... objectArray) {
        objectArray = ArrayUtils.nullToEmpty(objectArray);
        Class<?>[] classArray = ClassUtils.toClass(objectArray);
        return MethodUtils.invokeExactMethod(object, string, objectArray, classArray);
    }

    public static Object invokeExactMethod(Object object, String string, Object[] objectArray, Class<?>[] object2) {
        objectArray = ArrayUtils.nullToEmpty(objectArray);
        object2 = ArrayUtils.nullToEmpty(object2);
        object2 = MethodUtils.getAccessibleMethod(object.getClass(), string, object2);
        if (object2 == null) {
            throw new NoSuchMethodException("No such accessible method: " + string + "() on object: " + object.getClass().getName());
        }
        return ((Method)object2).invoke(object, objectArray);
    }

    public static Object invokeExactStaticMethod(Class<?> clazz, String string, Object[] objectArray, Class<?>[] object) {
        objectArray = ArrayUtils.nullToEmpty(objectArray);
        object = ArrayUtils.nullToEmpty(object);
        if ((object = MethodUtils.getAccessibleMethod(clazz, string, object)) == null) {
            throw new NoSuchMethodException("No such accessible method: " + string + "() on class: " + clazz.getName());
        }
        return ((Method)object).invoke(null, objectArray);
    }

    public static Object invokeStaticMethod(Class<?> clazz, String string, Object ... objectArray) {
        objectArray = ArrayUtils.nullToEmpty(objectArray);
        Class<?>[] classArray = ClassUtils.toClass(objectArray);
        return MethodUtils.invokeStaticMethod(clazz, string, objectArray, classArray);
    }

    public static Object invokeStaticMethod(Class<?> clazz, String string, Object[] objectArray, Class<?>[] object) {
        objectArray = ArrayUtils.nullToEmpty(objectArray);
        object = ArrayUtils.nullToEmpty(object);
        if ((object = MethodUtils.getMatchingAccessibleMethod(clazz, string, object)) == null) {
            throw new NoSuchMethodException("No such accessible method: " + string + "() on class: " + clazz.getName());
        }
        objectArray = MethodUtils.toVarArgs(object, objectArray);
        return ((Method)object).invoke(null, objectArray);
    }

    private static Object[] toVarArgs(Method classArray, Object[] objectArray) {
        if (classArray.isVarArgs()) {
            classArray = classArray.getParameterTypes();
            objectArray = MethodUtils.getVarArgs(objectArray, classArray);
        }
        return objectArray;
    }

    static Object[] getVarArgs(Object[] objectArray, Class<?>[] classArray) {
        if (objectArray.length == classArray.length && objectArray[objectArray.length - 1].getClass().equals(classArray[classArray.length - 1])) {
            return objectArray;
        }
        Object[] objectArray2 = new Object[classArray.length];
        System.arraycopy(objectArray, 0, objectArray2, 0, classArray.length - 1);
        Class<?> clazz = classArray[classArray.length - 1].getComponentType();
        int n = objectArray.length - classArray.length + 1;
        Object object = Array.newInstance(ClassUtils.primitiveToWrapper(clazz), n);
        System.arraycopy(objectArray, classArray.length - 1, object, 0, n);
        if (clazz.isPrimitive()) {
            object = ArrayUtils.toPrimitive(object);
        }
        objectArray2[classArray.length - 1] = object;
        return objectArray2;
    }

    public static Object invokeExactStaticMethod(Class<?> clazz, String string, Object ... objectArray) {
        objectArray = ArrayUtils.nullToEmpty(objectArray);
        Class<?>[] classArray = ClassUtils.toClass(objectArray);
        return MethodUtils.invokeExactStaticMethod(clazz, string, objectArray, classArray);
    }

    public static Method getAccessibleMethod(Class<?> clazz, String string, Class<?> ... classArray) {
        try {
            return MethodUtils.getAccessibleMethod(clazz.getMethod(string, classArray));
        }
        catch (NoSuchMethodException noSuchMethodException) {
            return null;
        }
    }

    public static Method getAccessibleMethod(Method method) {
        Class<?>[] classArray;
        if (!MemberUtils.isAccessible(method)) {
            return null;
        }
        Class<?> clazz = method.getDeclaringClass();
        if (Modifier.isPublic(clazz.getModifiers())) {
            return method;
        }
        String string = method.getName();
        if ((method = MethodUtils.getAccessibleMethodFromInterfaceNest(clazz, string, classArray = method.getParameterTypes())) == null) {
            method = MethodUtils.getAccessibleMethodFromSuperclass(clazz, string, classArray);
        }
        return method;
    }

    private static Method getAccessibleMethodFromSuperclass(Class<?> clazz, String string, Class<?> ... classArray) {
        for (clazz = clazz.getSuperclass(); clazz != null; clazz = clazz.getSuperclass()) {
            if (!Modifier.isPublic(clazz.getModifiers())) continue;
            try {
                return clazz.getMethod(string, classArray);
            }
            catch (NoSuchMethodException noSuchMethodException) {
                return null;
            }
        }
        return null;
    }

    private static Method getAccessibleMethodFromInterfaceNest(Class<?> clazz, String string, Class<?> ... classArray) {
        while (clazz != null) {
            Class<?>[] classArray2 = clazz.getInterfaces();
            for (int j = 0; j < classArray2.length; ++j) {
                if (!Modifier.isPublic(classArray2[j].getModifiers())) continue;
                try {
                    return classArray2[j].getDeclaredMethod(string, classArray);
                }
                catch (NoSuchMethodException noSuchMethodException) {
                    Method method = MethodUtils.getAccessibleMethodFromInterfaceNest(classArray2[j], string, classArray);
                    if (method == null) continue;
                    return method;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    public static Method getMatchingAccessibleMethod(Class<?> methodArray, String string, Class<?> ... classArray) {
        try {
            Method method = methodArray.getMethod(string, classArray);
            MemberUtils.setAccessibleWorkaround(method);
            return method;
        }
        catch (NoSuchMethodException noSuchMethodException) {
            Method method = null;
            for (Method method2 : methodArray.getMethods()) {
                if (!method2.getName().equals(string) || !MemberUtils.isMatchingMethod(method2, classArray) || (method2 = MethodUtils.getAccessibleMethod(method2)) == null || method != null && MemberUtils.compareMethodFit(method2, method, classArray) >= 0) continue;
                method = method2;
            }
            if (method != null) {
                MemberUtils.setAccessibleWorkaround(method);
            }
            return method;
        }
    }

    public static Method getMatchingMethod(Class<?> object, String string, Class<?> ... classArray) {
        Validate.notNull(object, "Null class not allowed.", new Object[0]);
        Validate.notEmpty(string, "Null or blank methodName not allowed.", new Object[0]);
        Method[] methodArray2 = ((Class)object).getDeclaredMethods();
        for (Class<?> methodArray3 : ClassUtils.getAllSuperclasses(object)) {
            methodArray2 = ArrayUtils.addAll(methodArray2, methodArray3.getDeclaredMethods());
        }
        object = null;
        Method[] methodArray = methodArray2;
        int n = methodArray2.length;
        for (int j = 0; j < n; ++j) {
            Method method = methodArray[j];
            if (string.equals(method.getName()) && ArrayUtils.isEquals(classArray, method.getParameterTypes())) {
                return method;
            }
            if (!string.equals(method.getName()) || !ClassUtils.isAssignable(classArray, method.getParameterTypes(), true)) continue;
            if (object == null) {
                object = method;
                continue;
            }
            if (MethodUtils.distance(classArray, method.getParameterTypes()) >= MethodUtils.distance(classArray, ((Method)object).getParameterTypes())) continue;
            object = method;
        }
        return object;
    }

    private static int distance(Class<?>[] classArray, Class<?>[] classArray2) {
        int n = 0;
        if (!ClassUtils.isAssignable(classArray, classArray2, true)) {
            return -1;
        }
        for (int j = 0; j < classArray.length; ++j) {
            if (classArray[j].equals(classArray2[j])) continue;
            if (ClassUtils.isAssignable(classArray[j], classArray2[j], true) && !ClassUtils.isAssignable(classArray[j], classArray2[j], false)) {
                ++n;
                continue;
            }
            n += 2;
        }
        return n;
    }

    public static Set<Method> getOverrideHierarchy(Method method, ClassUtils.Interfaces object) {
        Validate.notNull(method);
        LinkedHashSet<Method> linkedHashSet = new LinkedHashSet<Method>();
        linkedHashSet.add(method);
        Object[] objectArray = method.getParameterTypes();
        Class<?> clazz = method.getDeclaringClass();
        object = ClassUtils.hierarchy(clazz, (ClassUtils.Interfaces)((Object)object)).iterator();
        object.next();
        block0: while (object.hasNext()) {
            Method method2 = MethodUtils.getMatchingAccessibleMethod((Class)object.next(), method.getName(), objectArray);
            if (method2 == null) continue;
            if (Arrays.equals(method2.getParameterTypes(), objectArray)) {
                linkedHashSet.add(method2);
                continue;
            }
            Map<TypeVariable<?>, Type> map = TypeUtils.getTypeArguments(clazz, method2.getDeclaringClass());
            for (int j = 0; j < objectArray.length; ++j) {
                Type type;
                Type type2 = TypeUtils.unrollVariables(map, method.getGenericParameterTypes()[j]);
                if (!TypeUtils.equals(type2, type = TypeUtils.unrollVariables(map, method2.getGenericParameterTypes()[j]))) continue block0;
            }
            linkedHashSet.add(method2);
        }
        return linkedHashSet;
    }

    public static Method[] getMethodsWithAnnotation(Class<?> clazz, Class<? extends Annotation> clazz2) {
        List<Method> list = MethodUtils.getMethodsListWithAnnotation(clazz, clazz2);
        return list.toArray(new Method[list.size()]);
    }

    public static List<Method> getMethodsListWithAnnotation(Class<?> methodArray, Class<? extends Annotation> clazz) {
        Validate.isTrue(methodArray != null, "The class must not be null", new Object[0]);
        Validate.isTrue(clazz != null, "The annotation class must not be null", new Object[0]);
        methodArray = methodArray.getMethods();
        ArrayList<Method> arrayList = new ArrayList<Method>();
        for (Method method : methodArray) {
            if (method.getAnnotation(clazz) == null) continue;
            arrayList.add(method);
        }
        return arrayList;
    }
}

