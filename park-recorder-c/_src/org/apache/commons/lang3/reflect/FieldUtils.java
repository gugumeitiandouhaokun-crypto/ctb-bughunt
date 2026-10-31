/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.MemberUtils;

public class FieldUtils {
    public static Field getField(Class<?> annotatedElement, String string) {
        annotatedElement = FieldUtils.getField(annotatedElement, string, false);
        MemberUtils.setAccessibleWorkaround((AccessibleObject)annotatedElement);
        return annotatedElement;
    }

    public static Field getField(Class<?> annotatedElement, String string, boolean bl) {
        AnnotatedElement annotatedElement2;
        Validate.isTrue(annotatedElement != null, "The class must not be null", new Object[0]);
        Validate.isTrue(StringUtils.isNotBlank(string), "The field name must not be blank/empty", new Object[0]);
        for (annotatedElement2 = annotatedElement; annotatedElement2 != null; annotatedElement2 = annotatedElement2.getSuperclass()) {
            try {
                Field field = annotatedElement2.getDeclaredField(string);
                if (!Modifier.isPublic(field.getModifiers())) {
                    if (!bl) continue;
                    field.setAccessible(true);
                }
                return field;
            }
            catch (NoSuchFieldException noSuchFieldException) {}
        }
        annotatedElement2 = null;
        for (Class annotatedElement3 : ClassUtils.getAllInterfaces(annotatedElement)) {
            try {
                Field field = annotatedElement3.getField(string);
                Validate.isTrue(annotatedElement2 == null, "Reference to field %s is ambiguous relative to %s; a matching field exists on two or more implemented interfaces.", string, annotatedElement);
                annotatedElement2 = field;
            }
            catch (NoSuchFieldException noSuchFieldException) {}
        }
        return annotatedElement2;
    }

    public static Field getDeclaredField(Class<?> clazz, String string) {
        return FieldUtils.getDeclaredField(clazz, string, false);
    }

    public static Field getDeclaredField(Class<?> annotatedElement, String string, boolean bl) {
        Validate.isTrue(annotatedElement != null, "The class must not be null", new Object[0]);
        Validate.isTrue(StringUtils.isNotBlank(string), "The field name must not be blank/empty", new Object[0]);
        try {
            annotatedElement = ((Class)annotatedElement).getDeclaredField(string);
            if (!MemberUtils.isAccessible((Member)((Object)annotatedElement))) {
                if (bl) {
                    ((AccessibleObject)annotatedElement).setAccessible(true);
                } else {
                    return null;
                }
            }
            return annotatedElement;
        }
        catch (NoSuchFieldException noSuchFieldException) {
            return null;
        }
    }

    public static Field[] getAllFields(Class<?> clazz) {
        List<Field> list = FieldUtils.getAllFieldsList(clazz);
        return list.toArray(new Field[list.size()]);
    }

    public static List<Field> getAllFieldsList(Class<?> clazz) {
        Validate.isTrue(clazz != null, "The class must not be null", new Object[0]);
        ArrayList<Field> arrayList = new ArrayList<Field>();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                arrayList.add(field);
            }
            clazz = clazz.getSuperclass();
        }
        return arrayList;
    }

    public static Field[] getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> clazz2) {
        List<Field> list = FieldUtils.getFieldsListWithAnnotation(clazz, clazz2);
        return list.toArray(new Field[list.size()]);
    }

    public static List<Field> getFieldsListWithAnnotation(Class<?> iterator, Class<? extends Annotation> clazz) {
        Validate.isTrue(clazz != null, "The annotation class must not be null", new Object[0]);
        iterator = FieldUtils.getAllFieldsList(iterator);
        ArrayList<Field> arrayList = new ArrayList<Field>();
        iterator = iterator.iterator();
        while (iterator.hasNext()) {
            Field field = (Field)iterator.next();
            if (field.getAnnotation(clazz) == null) continue;
            arrayList.add(field);
        }
        return arrayList;
    }

    public static Object readStaticField(Field field) {
        return FieldUtils.readStaticField(field, false);
    }

    public static Object readStaticField(Field field, boolean bl) {
        Validate.isTrue(field != null, "The field must not be null", new Object[0]);
        Validate.isTrue(Modifier.isStatic(field.getModifiers()), "The field '%s' is not static", field.getName());
        return FieldUtils.readField(field, null, bl);
    }

    public static Object readStaticField(Class<?> clazz, String string) {
        return FieldUtils.readStaticField(clazz, string, false);
    }

    public static Object readStaticField(Class<?> clazz, String string, boolean bl) {
        Field field = FieldUtils.getField(clazz, string, bl);
        Validate.isTrue(field != null, "Cannot locate field '%s' on %s", string, clazz);
        return FieldUtils.readStaticField(field, false);
    }

    public static Object readDeclaredStaticField(Class<?> clazz, String string) {
        return FieldUtils.readDeclaredStaticField(clazz, string, false);
    }

    public static Object readDeclaredStaticField(Class<?> clazz, String string, boolean bl) {
        Field field = FieldUtils.getDeclaredField(clazz, string, bl);
        Validate.isTrue(field != null, "Cannot locate declared field %s.%s", clazz.getName(), string);
        return FieldUtils.readStaticField(field, false);
    }

    public static Object readField(Field field, Object object) {
        return FieldUtils.readField(field, object, false);
    }

    public static Object readField(Field field, Object object, boolean bl) {
        Validate.isTrue(field != null, "The field must not be null", new Object[0]);
        if (bl && !field.isAccessible()) {
            field.setAccessible(true);
        } else {
            MemberUtils.setAccessibleWorkaround(field);
        }
        return field.get(object);
    }

    public static Object readField(Object object, String string) {
        return FieldUtils.readField(object, string, false);
    }

    public static Object readField(Object object, String string, boolean bl) {
        Validate.isTrue(object != null, "target object must not be null", new Object[0]);
        Class<?> clazz = object.getClass();
        Field field = FieldUtils.getField(clazz, string, bl);
        Validate.isTrue(field != null, "Cannot locate field %s on %s", string, clazz);
        return FieldUtils.readField(field, object, false);
    }

    public static Object readDeclaredField(Object object, String string) {
        return FieldUtils.readDeclaredField(object, string, false);
    }

    public static Object readDeclaredField(Object object, String string, boolean bl) {
        Validate.isTrue(object != null, "target object must not be null", new Object[0]);
        Class<?> clazz = object.getClass();
        Field field = FieldUtils.getDeclaredField(clazz, string, bl);
        Validate.isTrue(field != null, "Cannot locate declared field %s.%s", clazz, string);
        return FieldUtils.readField(field, object, false);
    }

    public static void writeStaticField(Field field, Object object) {
        FieldUtils.writeStaticField(field, object, false);
    }

    public static void writeStaticField(Field field, Object object, boolean bl) {
        Validate.isTrue(field != null, "The field must not be null", new Object[0]);
        Validate.isTrue(Modifier.isStatic(field.getModifiers()), "The field %s.%s is not static", field.getDeclaringClass().getName(), field.getName());
        FieldUtils.writeField(field, null, object, bl);
    }

    public static void writeStaticField(Class<?> clazz, String string, Object object) {
        FieldUtils.writeStaticField(clazz, string, object, false);
    }

    public static void writeStaticField(Class<?> clazz, String string, Object object, boolean bl) {
        Field field = FieldUtils.getField(clazz, string, bl);
        Validate.isTrue(field != null, "Cannot locate field %s on %s", string, clazz);
        FieldUtils.writeStaticField(field, object, false);
    }

    public static void writeDeclaredStaticField(Class<?> clazz, String string, Object object) {
        FieldUtils.writeDeclaredStaticField(clazz, string, object, false);
    }

    public static void writeDeclaredStaticField(Class<?> clazz, String string, Object object, boolean bl) {
        Field field = FieldUtils.getDeclaredField(clazz, string, bl);
        Validate.isTrue(field != null, "Cannot locate declared field %s.%s", clazz.getName(), string);
        FieldUtils.writeField(field, null, object, false);
    }

    public static void writeField(Field field, Object object, Object object2) {
        FieldUtils.writeField(field, object, object2, false);
    }

    public static void writeField(Field field, Object object, Object object2, boolean bl) {
        Validate.isTrue(field != null, "The field must not be null", new Object[0]);
        if (bl && !field.isAccessible()) {
            field.setAccessible(true);
        } else {
            MemberUtils.setAccessibleWorkaround(field);
        }
        field.set(object, object2);
    }

    public static void removeFinalModifier(Field field) {
        FieldUtils.removeFinalModifier(field, true);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static void removeFinalModifier(Field field, boolean bl) {
        Validate.isTrue(field != null, "The field must not be null", new Object[0]);
        try {
            if (!Modifier.isFinal(field.getModifiers())) return;
            Field field2 = Field.class.getDeclaredField("modifiers");
            if (bl = bl && !field2.isAccessible()) {
                field2.setAccessible(true);
            }
            try {
                Field field3 = field;
                field2.setInt(field3, field3.getModifiers() & 0xFFFFFFEF);
                if (!bl) return;
            }
            catch (Throwable throwable) {
                if (!bl) throw throwable;
                field2.setAccessible(false);
                throw throwable;
            }
            field2.setAccessible(false);
            return;
        }
        catch (NoSuchFieldException noSuchFieldException) {
            return;
        }
        catch (IllegalAccessException illegalAccessException) {
            return;
        }
    }

    public static void writeField(Object object, String string, Object object2) {
        FieldUtils.writeField(object, string, object2, false);
    }

    public static void writeField(Object object, String string, Object object2, boolean bl) {
        Validate.isTrue(object != null, "target object must not be null", new Object[0]);
        Class<?> clazz = object.getClass();
        Field field = FieldUtils.getField(clazz, string, bl);
        Validate.isTrue(field != null, "Cannot locate declared field %s.%s", clazz.getName(), string);
        FieldUtils.writeField(field, object, object2, false);
    }

    public static void writeDeclaredField(Object object, String string, Object object2) {
        FieldUtils.writeDeclaredField(object, string, object2, false);
    }

    public static void writeDeclaredField(Object object, String string, Object object2, boolean bl) {
        Validate.isTrue(object != null, "target object must not be null", new Object[0]);
        Class<?> clazz = object.getClass();
        Field field = FieldUtils.getDeclaredField(clazz, string, bl);
        Validate.isTrue(field != null, "Cannot locate declared field %s.%s", clazz.getName(), string);
        FieldUtils.writeField(field, object, object2, false);
    }
}

