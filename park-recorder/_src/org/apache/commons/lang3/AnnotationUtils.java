/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class AnnotationUtils {
    private static final ToStringStyle TO_STRING_STYLE = new ToStringStyle(){
        private static final long serialVersionUID = 1L;
        {
            this.setDefaultFullDetail(true);
            this.setArrayContentDetail(true);
            this.setUseClassName(true);
            this.setUseShortClassName(true);
            this.setUseIdentityHashCode(false);
            this.setContentStart("(");
            this.setContentEnd(")");
            this.setFieldSeparator(", ");
            this.setArrayStart("[");
            this.setArrayEnd("]");
        }

        @Override
        public final String getShortClassName(Class<?> object) {
            Class<?> clazz = null;
            for (Class<?> clazz2 : ClassUtils.getAllInterfaces(object)) {
                if (!Annotation.class.isAssignableFrom(clazz2)) continue;
                clazz = clazz2;
                break;
            }
            return new StringBuilder(clazz == null ? "" : clazz.getName()).insert(0, '@').toString();
        }

        @Override
        public final void appendDetail(StringBuffer stringBuffer, String string, Object object) {
            if (object instanceof Annotation) {
                object = AnnotationUtils.toString((Annotation)object);
            }
            super.appendDetail(stringBuffer, string, object);
        }
    };

    public static boolean equals(Annotation annotation, Annotation annotation2) {
        if (annotation == annotation2) {
            return true;
        }
        if (annotation == null || annotation2 == null) {
            return false;
        }
        Method[] methodArray = annotation.annotationType();
        Class<? extends Annotation> clazz = annotation2.annotationType();
        Validate.notNull(methodArray, "Annotation %s with null annotationType()", annotation);
        Validate.notNull(clazz, "Annotation %s with null annotationType()", annotation2);
        if (!methodArray.equals(clazz)) {
            return false;
        }
        try {
            for (Method method : methodArray.getDeclaredMethods()) {
                if (method.getParameterTypes().length != 0 || !AnnotationUtils.isValidAnnotationMemberType(method.getReturnType())) continue;
                Object object = method.invoke(annotation, new Object[0]);
                Object object2 = method.invoke(annotation2, new Object[0]);
                if (AnnotationUtils.memberEquals(method.getReturnType(), object, object2)) continue;
                return false;
            }
        }
        catch (IllegalAccessException illegalAccessException) {
            return false;
        }
        catch (InvocationTargetException invocationTargetException) {
            return false;
        }
        return true;
    }

    public static int hashCode(Annotation annotation) {
        int n = 0;
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            try {
                Object object = method.invoke(annotation, new Object[0]);
                if (object == null) {
                    throw new IllegalStateException(String.format("Annotation method %s returned null", method));
                }
                n += AnnotationUtils.hashMember(method.getName(), object);
            }
            catch (RuntimeException runtimeException) {
                throw runtimeException;
            }
            catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
        return n;
    }

    public static String toString(Annotation annotation) {
        ToStringBuilder toStringBuilder = new ToStringBuilder(annotation, TO_STRING_STYLE);
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            if (method.getParameterTypes().length > 0) continue;
            try {
                toStringBuilder.append(method.getName(), method.invoke(annotation, new Object[0]));
            }
            catch (RuntimeException runtimeException) {
                throw runtimeException;
            }
            catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
        return toStringBuilder.build();
    }

    public static boolean isValidAnnotationMemberType(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        if (clazz.isArray()) {
            clazz = clazz.getComponentType();
        }
        return clazz.isPrimitive() || clazz.isEnum() || clazz.isAnnotation() || String.class.equals(clazz) || Class.class.equals(clazz);
    }

    private static int hashMember(String string, Object object) {
        int n = string.hashCode() * 127;
        if (object.getClass().isArray()) {
            return n ^ AnnotationUtils.arrayMemberHash(object.getClass().getComponentType(), object);
        }
        if (object instanceof Annotation) {
            return n ^ AnnotationUtils.hashCode((Annotation)object);
        }
        return n ^ object.hashCode();
    }

    private static boolean memberEquals(Class<?> clazz, Object object, Object object2) {
        if (object == object2) {
            return true;
        }
        if (object == null || object2 == null) {
            return false;
        }
        if (clazz.isArray()) {
            return AnnotationUtils.arrayMemberEquals(clazz.getComponentType(), object, object2);
        }
        if (clazz.isAnnotation()) {
            return AnnotationUtils.equals((Annotation)object, (Annotation)object2);
        }
        return object.equals(object2);
    }

    private static boolean arrayMemberEquals(Class<?> clazz, Object object, Object object2) {
        if (clazz.isAnnotation()) {
            return AnnotationUtils.annotationArrayMemberEquals((Annotation[])object, (Annotation[])object2);
        }
        if (clazz.equals(Byte.TYPE)) {
            return Arrays.equals((byte[])object, (byte[])object2);
        }
        if (clazz.equals(Short.TYPE)) {
            return Arrays.equals((short[])object, (short[])object2);
        }
        if (clazz.equals(Integer.TYPE)) {
            return Arrays.equals((int[])object, (int[])object2);
        }
        if (clazz.equals(Character.TYPE)) {
            return Arrays.equals((char[])object, (char[])object2);
        }
        if (clazz.equals(Long.TYPE)) {
            return Arrays.equals((long[])object, (long[])object2);
        }
        if (clazz.equals(Float.TYPE)) {
            return Arrays.equals((float[])object, (float[])object2);
        }
        if (clazz.equals(Double.TYPE)) {
            return Arrays.equals((double[])object, (double[])object2);
        }
        if (clazz.equals(Boolean.TYPE)) {
            return Arrays.equals((boolean[])object, (boolean[])object2);
        }
        return Arrays.equals((Object[])object, (Object[])object2);
    }

    private static boolean annotationArrayMemberEquals(Annotation[] annotationArray, Annotation[] annotationArray2) {
        if (annotationArray.length != annotationArray2.length) {
            return false;
        }
        for (int j = 0; j < annotationArray.length; ++j) {
            if (AnnotationUtils.equals(annotationArray[j], annotationArray2[j])) continue;
            return false;
        }
        return true;
    }

    private static int arrayMemberHash(Class<?> clazz, Object object) {
        if (clazz.equals(Byte.TYPE)) {
            return Arrays.hashCode((byte[])object);
        }
        if (clazz.equals(Short.TYPE)) {
            return Arrays.hashCode((short[])object);
        }
        if (clazz.equals(Integer.TYPE)) {
            return Arrays.hashCode((int[])object);
        }
        if (clazz.equals(Character.TYPE)) {
            return Arrays.hashCode((char[])object);
        }
        if (clazz.equals(Long.TYPE)) {
            return Arrays.hashCode((long[])object);
        }
        if (clazz.equals(Float.TYPE)) {
            return Arrays.hashCode((float[])object);
        }
        if (clazz.equals(Double.TYPE)) {
            return Arrays.hashCode((double[])object);
        }
        if (clazz.equals(Boolean.TYPE)) {
            return Arrays.hashCode((boolean[])object);
        }
        return Arrays.hashCode((Object[])object);
    }
}

