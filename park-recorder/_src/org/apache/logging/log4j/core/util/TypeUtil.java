/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TypeUtil {
    private TypeUtil() {
    }

    public static List<Field> getAllDeclaredFields(Class<?> clazz) {
        ArrayList<Field> arrayList = new ArrayList<Field>();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                arrayList.add(field);
            }
            clazz = clazz.getSuperclass();
        }
        return arrayList;
    }

    public static boolean isAssignable(Type type, Type clazz) {
        while (true) {
            Type type2;
            Type type3;
            Objects.requireNonNull(type, "No left hand side type provided");
            Objects.requireNonNull(clazz, "No right hand side type provided");
            if (type.equals(clazz)) {
                return true;
            }
            if (Object.class.equals((Object)type)) {
                return true;
            }
            if (type instanceof Class) {
                type3 = type;
                if (clazz instanceof Class) {
                    type2 = clazz;
                    return ((Class)type3).isAssignableFrom((Class<?>)type2);
                }
                if (clazz instanceof ParameterizedType && (type2 = ((ParameterizedType)((Object)clazz)).getRawType()) instanceof Class) {
                    return ((Class)type3).isAssignableFrom((Class<?>)type2);
                }
                if (((Class)type3).isArray() && clazz instanceof GenericArrayType) {
                    clazz = ((GenericArrayType)((Object)clazz)).getGenericComponentType();
                    type = ((Class)type3).getComponentType();
                    continue;
                }
            }
            if (type instanceof ParameterizedType) {
                type3 = (ParameterizedType)type;
                if (clazz instanceof Class) {
                    type2 = type3.getRawType();
                    if (type2 instanceof Class) {
                        return ((Class)type2).isAssignableFrom(clazz);
                    }
                } else if (clazz instanceof ParameterizedType) {
                    type2 = (ParameterizedType)((Object)clazz);
                    return TypeUtil.isParameterizedAssignable((ParameterizedType)type3, (ParameterizedType)type2);
                }
            }
            if (!(type instanceof GenericArrayType)) break;
            type3 = ((GenericArrayType)type).getGenericComponentType();
            if (clazz instanceof Class) {
                type2 = clazz;
                if (!((Class)type2).isArray()) break;
                clazz = ((Class)type2).getComponentType();
                type = type3;
                continue;
            }
            if (!(clazz instanceof GenericArrayType)) break;
            clazz = ((GenericArrayType)((Object)clazz)).getGenericComponentType();
            type = type3;
        }
        if (type instanceof WildcardType) {
            return TypeUtil.isWildcardAssignable((WildcardType)type, clazz);
        }
        return false;
    }

    private static boolean isParameterizedAssignable(ParameterizedType typeArray, ParameterizedType typeArray2) {
        int n;
        if (typeArray.equals(typeArray2)) {
            return true;
        }
        typeArray = typeArray.getActualTypeArguments();
        if ((typeArray2 = typeArray2.getActualTypeArguments()).length != (n = typeArray.length)) {
            return false;
        }
        for (int j = 0; j < n; ++j) {
            Type type = typeArray[j];
            Type type2 = typeArray2[j];
            if (type.equals(type2) || type instanceof WildcardType && TypeUtil.isWildcardAssignable((WildcardType)type, type2)) continue;
            return false;
        }
        return true;
    }

    private static boolean isWildcardAssignable(WildcardType typeArray, Type type) {
        Type[] typeArray2 = TypeUtil.getEffectiveUpperBounds((WildcardType)typeArray);
        typeArray = TypeUtil.getEffectiveLowerBounds((WildcardType)typeArray);
        if (type instanceof WildcardType) {
            int n;
            int n2;
            Type[] typeArray3;
            WildcardType wildcardType = (WildcardType)type;
            Type[] typeArray4 = TypeUtil.getEffectiveUpperBounds(wildcardType);
            Type[] typeArray5 = TypeUtil.getEffectiveLowerBounds(wildcardType);
            for (Type type2 : typeArray2) {
                Type type3;
                typeArray3 = typeArray4;
                n2 = typeArray4.length;
                for (n = 0; n < n2; ++n) {
                    type3 = typeArray3[n];
                    if (TypeUtil.isBoundAssignable(type2, type3)) continue;
                    return false;
                }
                typeArray3 = typeArray5;
                n2 = typeArray5.length;
                for (n = 0; n < n2; ++n) {
                    type3 = typeArray3[n];
                    if (TypeUtil.isBoundAssignable(type2, type3)) continue;
                    return false;
                }
            }
            typeArray2 = typeArray;
            int n3 = typeArray.length;
            for (int j = 0; j < n3; ++j) {
                Type type2;
                type2 = typeArray2[j];
                typeArray3 = typeArray4;
                n2 = typeArray4.length;
                for (n = 0; n < n2; ++n) {
                    if (TypeUtil.isBoundAssignable(typeArray3[n], type2)) continue;
                    return false;
                }
                typeArray3 = typeArray5;
                n2 = typeArray5.length;
                for (n = 0; n < n2; ++n) {
                    if (TypeUtil.isBoundAssignable(typeArray3[n], type2)) continue;
                    return false;
                }
            }
        } else {
            int n;
            Type[] typeArray6 = typeArray2;
            int n4 = typeArray2.length;
            for (n = 0; n < n4; ++n) {
                if (TypeUtil.isBoundAssignable(typeArray6[n], type)) continue;
                return false;
            }
            typeArray6 = typeArray;
            n4 = typeArray.length;
            for (n = 0; n < n4; ++n) {
                if (TypeUtil.isBoundAssignable(typeArray6[n], type)) continue;
                return false;
            }
        }
        return true;
    }

    private static Type[] getEffectiveUpperBounds(WildcardType typeArray) {
        if ((typeArray = typeArray.getUpperBounds()).length == 0) {
            return new Type[]{Object.class};
        }
        return typeArray;
    }

    private static Type[] getEffectiveLowerBounds(WildcardType typeArray) {
        if ((typeArray = typeArray.getLowerBounds()).length == 0) {
            return new Type[]{null};
        }
        return typeArray;
    }

    private static boolean isBoundAssignable(Type type, Type type2) {
        return type2 == null || type != null && TypeUtil.isAssignable(type, type2);
    }
}

