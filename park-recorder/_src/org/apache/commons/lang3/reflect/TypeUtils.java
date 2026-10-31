/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.lang3.reflect.Typed;

public class TypeUtils {
    public static final WildcardType WILDCARD_ALL = TypeUtils.wildcardType().withUpperBounds(new Type[]{Object.class}).build();

    public static boolean isAssignable(Type type, Type type2) {
        return TypeUtils.isAssignable(type, type2, null);
    }

    private static boolean isAssignable(Type type, Type type2, Map<TypeVariable<?>, Type> map) {
        if (type2 == null || type2 instanceof Class) {
            return TypeUtils.isAssignable(type, (Class)type2);
        }
        if (type2 instanceof ParameterizedType) {
            return TypeUtils.isAssignable(type, (ParameterizedType)type2, map);
        }
        if (type2 instanceof GenericArrayType) {
            return TypeUtils.isAssignable(type, (GenericArrayType)type2, map);
        }
        if (type2 instanceof WildcardType) {
            return TypeUtils.isAssignable(type, (WildcardType)type2, map);
        }
        if (type2 instanceof TypeVariable) {
            return TypeUtils.isAssignable(type, (TypeVariable)type2, map);
        }
        throw new IllegalStateException("found an unhandled type: ".concat(String.valueOf(type2)));
    }

    private static boolean isAssignable(Type typeArray, Class<?> clazz) {
        while (true) {
            if (typeArray == null) {
                return clazz == null || !clazz.isPrimitive();
            }
            if (clazz == null) {
                return false;
            }
            if (clazz.equals(typeArray)) {
                return true;
            }
            if (typeArray instanceof Class) {
                return ClassUtils.isAssignable((Class)typeArray, clazz);
            }
            if (!(typeArray instanceof ParameterizedType)) break;
            typeArray = TypeUtils.getRawType((ParameterizedType)typeArray);
        }
        if (typeArray instanceof TypeVariable) {
            typeArray = ((TypeVariable)typeArray).getBounds();
            int n = typeArray.length;
            for (int j = 0; j < n; ++j) {
                if (!TypeUtils.isAssignable(typeArray[j], clazz)) continue;
                return true;
            }
            return false;
        }
        if (typeArray instanceof GenericArrayType) {
            return clazz.equals(Object.class) || clazz.isArray() && TypeUtils.isAssignable(((GenericArrayType)typeArray).getGenericComponentType(), clazz.getComponentType());
        }
        if (typeArray instanceof WildcardType) {
            return false;
        }
        throw new IllegalStateException("found an unhandled type: ".concat(String.valueOf(typeArray)));
    }

    private static boolean isAssignable(Type object, ParameterizedType object2, Map<TypeVariable<?>, Type> map) {
        if (object == null) {
            return true;
        }
        if (object2 == null) {
            return false;
        }
        if (((Object)object2).equals(object)) {
            return true;
        }
        Class<?> clazz = TypeUtils.getRawType((ParameterizedType)((Object)object2));
        if ((object = TypeUtils.getTypeArguments((Type)((Object)object), clazz, null)) == null) {
            return false;
        }
        if (object.isEmpty()) {
            return true;
        }
        object2 = TypeUtils.getTypeArguments((ParameterizedType)((Object)object2), clazz, map);
        for (TypeVariable typeVariable : object2.keySet()) {
            Type type = TypeUtils.unrollVariableAssignments(typeVariable, object2);
            Type type2 = TypeUtils.unrollVariableAssignments(typeVariable, object);
            if (type == null && type2 instanceof Class || type2 == null || type.equals(type2) || type instanceof WildcardType && TypeUtils.isAssignable(type2, type, map)) continue;
            return false;
        }
        return true;
    }

    private static Type unrollVariableAssignments(TypeVariable<?> typeVariable, Map<TypeVariable<?>, Type> map) {
        Type type;
        while ((type = map.get(typeVariable)) instanceof TypeVariable && !type.equals(typeVariable)) {
            typeVariable = (TypeVariable)type;
        }
        return type;
    }

    private static boolean isAssignable(Type typeArray, GenericArrayType genericArrayType, Map<TypeVariable<?>, Type> map) {
        if (typeArray == null) {
            return true;
        }
        if (genericArrayType == null) {
            return false;
        }
        if (genericArrayType.equals(typeArray)) {
            return true;
        }
        Type type = genericArrayType.getGenericComponentType();
        if (typeArray instanceof Class) {
            return (typeArray = (Class)typeArray).isArray() && TypeUtils.isAssignable(typeArray.getComponentType(), type, map);
        }
        if (typeArray instanceof GenericArrayType) {
            return TypeUtils.isAssignable(((GenericArrayType)typeArray).getGenericComponentType(), type, map);
        }
        if (typeArray instanceof WildcardType) {
            typeArray = TypeUtils.getImplicitUpperBounds((WildcardType)typeArray);
            int n = typeArray.length;
            for (int j = 0; j < n; ++j) {
                if (!TypeUtils.isAssignable(typeArray[j], genericArrayType)) continue;
                return true;
            }
            return false;
        }
        if (typeArray instanceof TypeVariable) {
            typeArray = TypeUtils.getImplicitBounds((TypeVariable)typeArray);
            int n = typeArray.length;
            for (int j = 0; j < n; ++j) {
                if (!TypeUtils.isAssignable(typeArray[j], genericArrayType)) continue;
                return true;
            }
            return false;
        }
        if (typeArray instanceof ParameterizedType) {
            return false;
        }
        throw new IllegalStateException("found an unhandled type: ".concat(String.valueOf(typeArray)));
    }

    private static boolean isAssignable(Type type, WildcardType object, Map<TypeVariable<?>, Type> map) {
        int n;
        if (type == null) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (object.equals(type)) {
            return true;
        }
        Object object2 = TypeUtils.getImplicitUpperBounds((WildcardType)object);
        object = TypeUtils.getImplicitLowerBounds((WildcardType)object);
        if (type instanceof WildcardType) {
            int n2;
            int n3;
            Type[] typeArray;
            Type type2;
            int n4;
            WildcardType wildcardType = (WildcardType)type;
            Type[] typeArray2 = TypeUtils.getImplicitUpperBounds(wildcardType);
            Type[] typeArray3 = TypeUtils.getImplicitLowerBounds(wildcardType);
            int n5 = ((Type[])object2).length;
            for (n4 = 0; n4 < n5; ++n4) {
                type2 = TypeUtils.substituteTypeVariables(object2[n4], map);
                typeArray = typeArray2;
                n3 = typeArray2.length;
                for (n2 = 0; n2 < n3; ++n2) {
                    if (TypeUtils.isAssignable(typeArray[n2], type2, map)) continue;
                    return false;
                }
            }
            object2 = object;
            n5 = ((Type[])object).length;
            for (n4 = 0; n4 < n5; ++n4) {
                type2 = TypeUtils.substituteTypeVariables(object2[n4], map);
                typeArray = typeArray3;
                n3 = typeArray3.length;
                for (n2 = 0; n2 < n3; ++n2) {
                    object = typeArray[n2];
                    if (TypeUtils.isAssignable(type2, (Type)object, map)) continue;
                    return false;
                }
            }
            return true;
        }
        Type[] typeArray = object2;
        int n6 = ((Type[])object2).length;
        for (n = 0; n < n6; ++n) {
            object2 = typeArray[n];
            if (TypeUtils.isAssignable(type, TypeUtils.substituteTypeVariables((Type)object2, map), map)) continue;
            return false;
        }
        typeArray = object;
        n6 = ((Type[])object).length;
        for (n = 0; n < n6; ++n) {
            if (TypeUtils.isAssignable(TypeUtils.substituteTypeVariables(typeArray[n], map), type, map)) continue;
            return false;
        }
        return true;
    }

    private static boolean isAssignable(Type type, TypeVariable<?> typeVariable, Map<TypeVariable<?>, Type> map) {
        if (type == null) {
            return true;
        }
        if (typeVariable == null) {
            return false;
        }
        if (typeVariable.equals(type)) {
            return true;
        }
        if (type instanceof TypeVariable) {
            Type[] typeArray = TypeUtils.getImplicitBounds((TypeVariable)type);
            int n = typeArray.length;
            for (int j = 0; j < n; ++j) {
                if (!TypeUtils.isAssignable(typeArray[j], typeVariable, map)) continue;
                return true;
            }
        }
        if (type instanceof Class || type instanceof ParameterizedType || type instanceof GenericArrayType || type instanceof WildcardType) {
            return false;
        }
        throw new IllegalStateException("found an unhandled type: ".concat(String.valueOf(type)));
    }

    private static Type substituteTypeVariables(Type type, Map<TypeVariable<?>, Type> object) {
        if (type instanceof TypeVariable && object != null) {
            if ((object = object.get(type)) == null) {
                throw new IllegalArgumentException("missing assignment type for type variable ".concat(String.valueOf(type)));
            }
            return object;
        }
        return type;
    }

    public static Map<TypeVariable<?>, Type> getTypeArguments(ParameterizedType parameterizedType) {
        ParameterizedType parameterizedType2 = parameterizedType;
        return TypeUtils.getTypeArguments(parameterizedType2, TypeUtils.getRawType(parameterizedType2), null);
    }

    public static Map<TypeVariable<?>, Type> getTypeArguments(Type type, Class<?> clazz) {
        return TypeUtils.getTypeArguments(type, clazz, null);
    }

    private static Map<TypeVariable<?>, Type> getTypeArguments(Type object, Class<?> clazz, Map<TypeVariable<?>, Type> map) {
        block7: {
            block0: while (true) {
                Type type;
                int n;
                int n2;
                if (object instanceof Class) {
                    return TypeUtils.getTypeArguments((Class)object, clazz, map);
                }
                if (object instanceof ParameterizedType) {
                    return TypeUtils.getTypeArguments((ParameterizedType)object, clazz, map);
                }
                if (object instanceof GenericArrayType) {
                    clazz = clazz.isArray() ? clazz.getComponentType() : clazz;
                    object = ((GenericArrayType)object).getGenericComponentType();
                    continue;
                }
                if (object instanceof WildcardType) {
                    object = TypeUtils.getImplicitUpperBounds((WildcardType)object);
                    n2 = ((Type[])object).length;
                    for (n = 0; n < n2; ++n) {
                        type = object[n];
                        if (!TypeUtils.isAssignable(type, clazz)) continue;
                        object = type;
                        continue block0;
                    }
                    return null;
                }
                if (!(object instanceof TypeVariable)) break block7;
                object = TypeUtils.getImplicitBounds((TypeVariable)object);
                n2 = ((Type[])object).length;
                for (n = 0; n < n2; ++n) {
                    type = object[n];
                    if (!TypeUtils.isAssignable(type, clazz)) continue;
                    object = type;
                    continue block0;
                }
                break;
            }
            return null;
        }
        throw new IllegalStateException("found an unhandled type: ".concat(String.valueOf(object)));
    }

    private static Map<TypeVariable<?>, Type> getTypeArguments(ParameterizedType typeVariableArray, Class<?> clazz, Map<TypeVariable<?>, Type> map) {
        Map<Object, Object> map2;
        Class<?> clazz2 = TypeUtils.getRawType((ParameterizedType)typeVariableArray);
        if (!TypeUtils.isAssignable(clazz2, clazz)) {
            return null;
        }
        Type[] typeArray = typeVariableArray.getOwnerType();
        if (typeArray instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)typeArray;
            map2 = TypeUtils.getTypeArguments(parameterizedType, TypeUtils.getRawType(parameterizedType), map);
        } else {
            map2 = map == null ? new HashMap() : new HashMap(map);
        }
        map = map2;
        typeArray = typeVariableArray.getActualTypeArguments();
        typeVariableArray = clazz2.getTypeParameters();
        for (int j = 0; j < typeVariableArray.length; ++j) {
            Type type = typeArray[j];
            map.put(typeVariableArray[j], map.containsKey(type) ? map.get(type) : type);
        }
        if (clazz.equals(clazz2)) {
            return map;
        }
        return TypeUtils.getTypeArguments(TypeUtils.getClosestParentType(clazz2, clazz), clazz, map);
    }

    private static Map<TypeVariable<?>, Type> getTypeArguments(Class<?> clazz, Class<?> clazz2, Map<TypeVariable<?>, Type> map) {
        if (!TypeUtils.isAssignable(clazz, clazz2)) {
            return null;
        }
        if (clazz.isPrimitive()) {
            if (clazz2.isPrimitive()) {
                return new HashMap();
            }
            clazz = ClassUtils.primitiveToWrapper(clazz);
        }
        Map<TypeVariable<?>, Type> map2 = map = map == null ? new HashMap() : new HashMap(map);
        if (clazz2.equals(clazz)) {
            return map;
        }
        return TypeUtils.getTypeArguments(TypeUtils.getClosestParentType(clazz, clazz2), clazz2, map);
    }

    public static Map<TypeVariable<?>, Type> determineTypeArguments(Class<?> clazz, ParameterizedType object) {
        Type type;
        while (true) {
            Validate.notNull(clazz, "cls is null", new Object[0]);
            Validate.notNull(object, "superType is null", new Object[0]);
            type = TypeUtils.getRawType((ParameterizedType)object);
            if (!TypeUtils.isAssignable(clazz, type)) {
                return null;
            }
            if (clazz.equals(type)) {
                return TypeUtils.getTypeArguments((ParameterizedType)object, type, null);
            }
            if (!((type = TypeUtils.getClosestParentType(clazz, type)) instanceof Class)) break;
            clazz = type;
        }
        type = (ParameterizedType)type;
        object = TypeUtils.determineTypeArguments(TypeUtils.getRawType((ParameterizedType)type), (ParameterizedType)object);
        TypeUtils.mapTypeVariablesToArguments(clazz, type, object);
        return object;
    }

    private static <T> void mapTypeVariablesToArguments(Class<T> object, ParameterizedType typeVariableArray, Map<TypeVariable<?>, Type> map) {
        Type[] typeArray = typeVariableArray.getOwnerType();
        if (typeArray instanceof ParameterizedType) {
            TypeUtils.mapTypeVariablesToArguments(object, (ParameterizedType)typeArray, map);
        }
        typeArray = typeVariableArray.getActualTypeArguments();
        typeVariableArray = TypeUtils.getRawType((ParameterizedType)typeVariableArray).getTypeParameters();
        object = Arrays.asList(((Class)object).getTypeParameters());
        for (int j = 0; j < typeArray.length; ++j) {
            TypeVariable<Class<?>> typeVariable = typeVariableArray[j];
            Type type = typeArray[j];
            if (!object.contains(type) || !map.containsKey(typeVariable)) continue;
            map.put((TypeVariable)type, map.get(typeVariable));
        }
    }

    private static Type getClosestParentType(Class<?> clazz, Class<?> clazz2) {
        if (clazz2.isInterface()) {
            Type[] typeArray = clazz.getGenericInterfaces();
            Type type = null;
            for (Type type2 : typeArray) {
                Class clazz3;
                if (type2 instanceof ParameterizedType) {
                    clazz3 = TypeUtils.getRawType((ParameterizedType)type2);
                } else if (type2 instanceof Class) {
                    clazz3 = (Class)type2;
                } else {
                    throw new IllegalStateException("Unexpected generic interface type found: ".concat(String.valueOf(type2)));
                }
                if (!TypeUtils.isAssignable((Type)clazz3, clazz2) || !TypeUtils.isAssignable(type, (Type)clazz3)) continue;
                type = type2;
            }
            if (type != null) {
                return type;
            }
        }
        return clazz.getGenericSuperclass();
    }

    public static boolean isInstance(Object object, Type type) {
        if (type == null) {
            return false;
        }
        if (object == null) {
            return !(type instanceof Class) || !((Class)type).isPrimitive();
        }
        return TypeUtils.isAssignable(object.getClass(), type, null);
    }

    public static Type[] normalizeUpperBounds(Type[] typeArray) {
        Validate.notNull(typeArray, "null value specified for bounds array", new Object[0]);
        if (typeArray.length < 2) {
            return typeArray;
        }
        HashSet<Type> hashSet = new HashSet<Type>(typeArray.length);
        Type[] typeArray2 = typeArray;
        int n = typeArray.length;
        for (int j = 0; j < n; ++j) {
            Type type = typeArray2[j];
            boolean bl = false;
            Type[] typeArray3 = typeArray;
            int n2 = typeArray.length;
            for (int k = 0; k < n2; ++k) {
                Type type2 = typeArray3[k];
                if (type == type2 || !TypeUtils.isAssignable(type2, type, null)) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            hashSet.add(type);
        }
        HashSet<Type> hashSet2 = hashSet;
        return hashSet2.toArray(new Type[hashSet2.size()]);
    }

    public static Type[] getImplicitBounds(TypeVariable<?> typeArray) {
        Validate.notNull(typeArray, "typeVariable is null", new Object[0]);
        typeArray = typeArray.getBounds();
        if (typeArray.length == 0) {
            return new Type[]{Object.class};
        }
        return TypeUtils.normalizeUpperBounds(typeArray);
    }

    public static Type[] getImplicitUpperBounds(WildcardType typeArray) {
        Validate.notNull(typeArray, "wildcardType is null", new Object[0]);
        typeArray = typeArray.getUpperBounds();
        if (typeArray.length == 0) {
            return new Type[]{Object.class};
        }
        return TypeUtils.normalizeUpperBounds(typeArray);
    }

    public static Type[] getImplicitLowerBounds(WildcardType typeArray) {
        Validate.notNull(typeArray, "wildcardType is null", new Object[0]);
        typeArray = typeArray.getLowerBounds();
        if (typeArray.length == 0) {
            return new Type[]{null};
        }
        return typeArray;
    }

    public static boolean typesSatisfyVariables(Map<TypeVariable<?>, Type> map) {
        Validate.notNull(map, "typeVarAssigns is null", new Object[0]);
        for (Map.Entry<TypeVariable<?>, Type> entry : map.entrySet()) {
            Type[] typeArray = entry.getKey();
            Type object = entry.getValue();
            for (Type type : TypeUtils.getImplicitBounds(typeArray)) {
                if (TypeUtils.isAssignable(object, TypeUtils.substituteTypeVariables(type, map), map)) continue;
                return false;
            }
        }
        return true;
    }

    private static Class<?> getRawType(ParameterizedType type) {
        if (!((type = type.getRawType()) instanceof Class)) {
            throw new IllegalStateException("Wait... What!? Type of rawType: ".concat(String.valueOf(type)));
        }
        return (Class)type;
    }

    public static Class<?> getRawType(Type type, Type type2) {
        block7: {
            Object object;
            do {
                if (type instanceof Class) {
                    return (Class)type;
                }
                if (type instanceof ParameterizedType) {
                    return TypeUtils.getRawType((ParameterizedType)type);
                }
                if (!(type instanceof TypeVariable)) break block7;
                if (type2 == null) {
                    return null;
                }
                object = ((TypeVariable)type).getGenericDeclaration();
                if (!(object instanceof Class)) {
                    return null;
                }
                if ((object = TypeUtils.getTypeArguments(type2, (Class)object)) != null) continue;
                return null;
            } while ((type = (Type)object.get(type)) != null);
            return null;
        }
        if (type instanceof GenericArrayType) {
            return Array.newInstance(TypeUtils.getRawType(((GenericArrayType)type).getGenericComponentType(), type2), 0).getClass();
        }
        if (type instanceof WildcardType) {
            return null;
        }
        throw new IllegalArgumentException("unknown type: ".concat(String.valueOf(type)));
    }

    public static boolean isArrayType(Type type) {
        return type instanceof GenericArrayType || type instanceof Class && ((Class)type).isArray();
    }

    public static Type getArrayComponentType(Type type) {
        if (type instanceof Class) {
            if (((Class)(type = (Class)type)).isArray()) {
                return ((Class)type).getComponentType();
            }
            return null;
        }
        if (type instanceof GenericArrayType) {
            return ((GenericArrayType)type).getGenericComponentType();
        }
        return null;
    }

    public static Type unrollVariables(Map<TypeVariable<?>, Type> map, Type type) {
        block6: {
            while (true) {
                if (map == null) {
                    map = Collections.emptyMap();
                }
                if (!TypeUtils.containsTypeVariables(type)) break block6;
                if (!(type instanceof TypeVariable)) break;
                type = map.get(type);
            }
            if (type instanceof ParameterizedType) {
                if ((type = (ParameterizedType)type).getOwnerType() != null) {
                    map = new HashMap<TypeVariable<Object>, Type>(map);
                    map.putAll(TypeUtils.getTypeArguments((ParameterizedType)type));
                }
                Type[] typeArray = type.getActualTypeArguments();
                for (int j = 0; j < typeArray.length; ++j) {
                    Type type2 = TypeUtils.unrollVariables(map, typeArray[j]);
                    if (type2 == null) continue;
                    typeArray[j] = type2;
                }
                return TypeUtils.parameterizeWithOwner(type.getOwnerType(), (Class)type.getRawType(), typeArray);
            }
            if (type instanceof WildcardType) {
                type = (WildcardType)type;
                return TypeUtils.wildcardType().withUpperBounds(TypeUtils.unrollBounds(map, type.getUpperBounds())).withLowerBounds(TypeUtils.unrollBounds(map, type.getLowerBounds())).build();
            }
        }
        return type;
    }

    private static Type[] unrollBounds(Map<TypeVariable<?>, Type> map, Type[] typeArray) {
        for (int j = 0; j < typeArray.length; ++j) {
            Type type = TypeUtils.unrollVariables(map, typeArray[j]);
            if (type == null) {
                typeArray = ArrayUtils.remove(typeArray, j--);
                continue;
            }
            typeArray[j] = type;
        }
        return typeArray;
    }

    public static boolean containsTypeVariables(Type object) {
        if (object instanceof TypeVariable) {
            return true;
        }
        if (object instanceof Class) {
            return ((Class)object).getTypeParameters().length > 0;
        }
        if (object instanceof ParameterizedType) {
            object = ((ParameterizedType)object).getActualTypeArguments();
            int n = ((Type[])object).length;
            for (int j = 0; j < n; ++j) {
                if (!TypeUtils.containsTypeVariables(object[j])) continue;
                return true;
            }
            return false;
        }
        if (object instanceof WildcardType) {
            return TypeUtils.containsTypeVariables(TypeUtils.getImplicitLowerBounds((WildcardType)(object = (WildcardType)object))[0]) || TypeUtils.containsTypeVariables(TypeUtils.getImplicitUpperBounds((WildcardType)object)[0]);
        }
        return false;
    }

    public static final ParameterizedType parameterize(Class<?> clazz, Type ... typeArray) {
        return TypeUtils.parameterizeWithOwner(null, clazz, typeArray);
    }

    public static final ParameterizedType parameterize(Class<?> clazz, Map<TypeVariable<?>, Type> map) {
        Validate.notNull(clazz, "raw class is null", new Object[0]);
        Validate.notNull(map, "typeArgMappings is null", new Object[0]);
        return TypeUtils.parameterizeWithOwner(null, clazz, TypeUtils.extractTypeArgumentsFrom(map, clazz.getTypeParameters()));
    }

    public static final ParameterizedType parameterizeWithOwner(Type clazz, Class<?> clazz2, Type ... typeArray) {
        Validate.notNull(clazz2, "raw class is null", new Object[0]);
        if (clazz2.getEnclosingClass() == null) {
            Validate.isTrue(clazz == null, "no owner allowed for top-level %s", clazz2);
            clazz = null;
        } else if (clazz == null) {
            clazz = clazz2.getEnclosingClass();
        } else {
            Validate.isTrue(TypeUtils.isAssignable(clazz, clazz2.getEnclosingClass()), "%s is invalid owner type for parameterized %s", clazz, clazz2);
        }
        Validate.noNullElements(typeArray, "null type argument at index %s", new Object[0]);
        Validate.isTrue(clazz2.getTypeParameters().length == typeArray.length, "invalid number of type parameters specified: expected %d, got %d", clazz2.getTypeParameters().length, typeArray.length);
        return new ParameterizedTypeImpl(clazz2, clazz, typeArray);
    }

    public static final ParameterizedType parameterizeWithOwner(Type type, Class<?> clazz, Map<TypeVariable<?>, Type> map) {
        Validate.notNull(clazz, "raw class is null", new Object[0]);
        Validate.notNull(map, "typeArgMappings is null", new Object[0]);
        return TypeUtils.parameterizeWithOwner(type, clazz, TypeUtils.extractTypeArgumentsFrom(map, clazz.getTypeParameters()));
    }

    private static Type[] extractTypeArgumentsFrom(Map<TypeVariable<?>, Type> map, TypeVariable<?>[] typeVariableArray) {
        Type[] typeArray = new Type[typeVariableArray.length];
        int n = 0;
        for (TypeVariable<?> typeVariable : typeVariableArray) {
            Validate.isTrue(map.containsKey(typeVariable), "missing argument mapping for %s", TypeUtils.toString(typeVariable));
            typeArray[n++] = map.get(typeVariable);
        }
        return typeArray;
    }

    public static WildcardTypeBuilder wildcardType() {
        return new WildcardTypeBuilder();
    }

    public static GenericArrayType genericArrayType(Type type) {
        return new GenericArrayTypeImpl(Validate.notNull(type, "componentType is null", new Object[0]));
    }

    public static boolean equals(Type type, Type type2) {
        if (ObjectUtils.equals(type, type2)) {
            return true;
        }
        if (type instanceof ParameterizedType) {
            return TypeUtils.equals((ParameterizedType)type, type2);
        }
        if (type instanceof GenericArrayType) {
            return TypeUtils.equals((GenericArrayType)type, type2);
        }
        if (type instanceof WildcardType) {
            return TypeUtils.equals((WildcardType)type, type2);
        }
        return false;
    }

    private static boolean equals(ParameterizedType parameterizedType, Type type) {
        if (type instanceof ParameterizedType) {
            type = (ParameterizedType)type;
            if (TypeUtils.equals(parameterizedType.getRawType(), type.getRawType()) && TypeUtils.equals(parameterizedType.getOwnerType(), type.getOwnerType())) {
                return TypeUtils.equals(parameterizedType.getActualTypeArguments(), type.getActualTypeArguments());
            }
        }
        return false;
    }

    private static boolean equals(GenericArrayType genericArrayType, Type type) {
        return type instanceof GenericArrayType && TypeUtils.equals(genericArrayType.getGenericComponentType(), ((GenericArrayType)type).getGenericComponentType());
    }

    private static boolean equals(WildcardType wildcardType, Type type) {
        if (type instanceof WildcardType) {
            type = (WildcardType)type;
            return TypeUtils.equals(TypeUtils.getImplicitLowerBounds(wildcardType), TypeUtils.getImplicitLowerBounds((WildcardType)type)) && TypeUtils.equals(TypeUtils.getImplicitUpperBounds(wildcardType), TypeUtils.getImplicitUpperBounds((WildcardType)type));
        }
        return false;
    }

    private static boolean equals(Type[] typeArray, Type[] typeArray2) {
        if (typeArray.length == typeArray2.length) {
            for (int j = 0; j < typeArray.length; ++j) {
                if (TypeUtils.equals(typeArray[j], typeArray2[j])) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    public static String toString(Type type) {
        Validate.notNull(type);
        if (type instanceof Class) {
            return TypeUtils.classToString((Class)type);
        }
        if (type instanceof ParameterizedType) {
            return TypeUtils.parameterizedTypeToString((ParameterizedType)type);
        }
        if (type instanceof WildcardType) {
            return TypeUtils.wildcardTypeToString((WildcardType)type);
        }
        if (type instanceof TypeVariable) {
            return TypeUtils.typeVariableToString((TypeVariable)type);
        }
        if (type instanceof GenericArrayType) {
            return TypeUtils.genericArrayTypeToString((GenericArrayType)type);
        }
        throw new IllegalArgumentException(ObjectUtils.identityToString(type));
    }

    public static String toLongString(TypeVariable<?> typeVariable) {
        StringBuilder stringBuilder;
        block5: {
            Validate.notNull(typeVariable, "var is null", new Object[0]);
            stringBuilder = new StringBuilder();
            Object object = typeVariable.getGenericDeclaration();
            if (object instanceof Class) {
                object = (Class)object;
                while (true) {
                    if (((Class)object).getEnclosingClass() == null) {
                        stringBuilder.insert(0, ((Class)object).getName());
                        break block5;
                    }
                    stringBuilder.insert(0, ((Class)object).getSimpleName()).insert(0, '.');
                    object = ((Class)object).getEnclosingClass();
                }
            }
            if (object instanceof Type) {
                stringBuilder.append(TypeUtils.toString((Type)object));
            } else {
                stringBuilder.append(object);
            }
        }
        return stringBuilder.append(':').append(TypeUtils.typeVariableToString(typeVariable)).toString();
    }

    public static <T> Typed<T> wrap(final Type type) {
        return new Typed<T>(){

            @Override
            public final Type getType() {
                return type;
            }
        };
    }

    public static <T> Typed<T> wrap(Class<T> clazz) {
        return TypeUtils.wrap(clazz);
    }

    private static String classToString(Class<?> clazz) {
        StringBuilder stringBuilder = new StringBuilder();
        if (clazz.getEnclosingClass() != null) {
            stringBuilder.append(TypeUtils.classToString(clazz.getEnclosingClass())).append('.').append(clazz.getSimpleName());
        } else {
            stringBuilder.append(clazz.getName());
        }
        if (clazz.getTypeParameters().length > 0) {
            stringBuilder.append('<');
            TypeUtils.appendAllTo(stringBuilder, ", ", clazz.getTypeParameters());
            stringBuilder.append('>');
        }
        return stringBuilder.toString();
    }

    private static String typeVariableToString(TypeVariable<?> typeVariable) {
        StringBuilder stringBuilder = new StringBuilder(typeVariable.getName());
        Type[] typeArray = typeVariable.getBounds();
        if (!(typeArray.length <= 0 || typeArray.length == 1 && Object.class.equals((Object)typeArray[0]))) {
            stringBuilder.append(" extends ");
            TypeUtils.appendAllTo(stringBuilder, " & ", typeVariable.getBounds());
        }
        return stringBuilder.toString();
    }

    private static String parameterizedTypeToString(ParameterizedType typeArray) {
        StringBuilder stringBuilder = new StringBuilder();
        Type type = typeArray.getOwnerType();
        Class clazz = (Class)typeArray.getRawType();
        typeArray = typeArray.getActualTypeArguments();
        if (type == null) {
            stringBuilder.append(clazz.getName());
        } else {
            if (type instanceof Class) {
                stringBuilder.append(((Class)type).getName());
            } else {
                stringBuilder.append(type.toString());
            }
            stringBuilder.append('.').append(clazz.getSimpleName());
        }
        TypeUtils.appendAllTo(stringBuilder.append('<'), ", ", typeArray).append('>');
        return stringBuilder.toString();
    }

    private static String wildcardTypeToString(WildcardType typeArray) {
        StringBuilder stringBuilder = new StringBuilder("?");
        Type[] typeArray2 = typeArray.getLowerBounds();
        typeArray = typeArray.getUpperBounds();
        if (typeArray2.length > 1 || typeArray2.length == 1 && typeArray2[0] != null) {
            TypeUtils.appendAllTo(stringBuilder.append(" super "), " & ", typeArray2);
        } else if (typeArray.length > 1 || typeArray.length == 1 && !Object.class.equals((Object)typeArray[0])) {
            TypeUtils.appendAllTo(stringBuilder.append(" extends "), " & ", typeArray);
        }
        return stringBuilder.toString();
    }

    private static String genericArrayTypeToString(GenericArrayType genericArrayType) {
        return String.format("%s[]", TypeUtils.toString(genericArrayType.getGenericComponentType()));
    }

    private static StringBuilder appendAllTo(StringBuilder stringBuilder, String string, Type ... typeArray) {
        Validate.notEmpty(Validate.noNullElements(typeArray));
        if (typeArray.length > 0) {
            stringBuilder.append(TypeUtils.toString(typeArray[0]));
            for (int j = 1; j < typeArray.length; ++j) {
                stringBuilder.append(string).append(TypeUtils.toString(typeArray[j]));
            }
        }
        return stringBuilder;
    }

    static final class WildcardTypeImpl
    implements WildcardType {
        private static final Type[] EMPTY_BOUNDS = new Type[0];
        private final Type[] upperBounds;
        private final Type[] lowerBounds;

        private WildcardTypeImpl(Type[] typeArray, Type[] typeArray2) {
            this.upperBounds = ObjectUtils.defaultIfNull(typeArray, EMPTY_BOUNDS);
            this.lowerBounds = ObjectUtils.defaultIfNull(typeArray2, EMPTY_BOUNDS);
        }

        @Override
        public final Type[] getUpperBounds() {
            return (Type[])this.upperBounds.clone();
        }

        @Override
        public final Type[] getLowerBounds() {
            return (Type[])this.lowerBounds.clone();
        }

        public final String toString() {
            return TypeUtils.toString(this);
        }

        public final boolean equals(Object object) {
            return object == this || object instanceof WildcardType && TypeUtils.equals(this, (WildcardType)object);
        }

        public final int hashCode() {
            return (0x4900 | Arrays.hashCode(this.upperBounds)) << 8 | Arrays.hashCode(this.lowerBounds);
        }
    }

    static final class ParameterizedTypeImpl
    implements ParameterizedType {
        private final Class<?> raw;
        private final Type useOwner;
        private final Type[] typeArguments;

        private ParameterizedTypeImpl(Class<?> clazz, Type type, Type[] typeArray) {
            this.raw = clazz;
            this.useOwner = type;
            this.typeArguments = (Type[])typeArray.clone();
        }

        @Override
        public final Type getRawType() {
            return this.raw;
        }

        @Override
        public final Type getOwnerType() {
            return this.useOwner;
        }

        @Override
        public final Type[] getActualTypeArguments() {
            return (Type[])this.typeArguments.clone();
        }

        public final String toString() {
            return TypeUtils.toString(this);
        }

        public final boolean equals(Object object) {
            return object == this || object instanceof ParameterizedType && TypeUtils.equals(this, (ParameterizedType)object);
        }

        public final int hashCode() {
            return ((0x470 | this.raw.hashCode()) << 4 | ObjectUtils.hashCode(this.useOwner)) << 8 | Arrays.hashCode(this.typeArguments);
        }
    }

    static final class GenericArrayTypeImpl
    implements GenericArrayType {
        private final Type componentType;

        private GenericArrayTypeImpl(Type type) {
            this.componentType = type;
        }

        @Override
        public final Type getGenericComponentType() {
            return this.componentType;
        }

        public final String toString() {
            return TypeUtils.toString(this);
        }

        public final boolean equals(Object object) {
            return object == this || object instanceof GenericArrayType && TypeUtils.equals(this, (GenericArrayType)object);
        }

        public final int hashCode() {
            return 0x430 | this.componentType.hashCode();
        }
    }

    public static class WildcardTypeBuilder
    implements Builder<WildcardType> {
        private Type[] upperBounds;
        private Type[] lowerBounds;

        private WildcardTypeBuilder() {
        }

        public WildcardTypeBuilder withUpperBounds(Type ... typeArray) {
            this.upperBounds = typeArray;
            return this;
        }

        public WildcardTypeBuilder withLowerBounds(Type ... typeArray) {
            this.lowerBounds = typeArray;
            return this;
        }

        @Override
        public WildcardType build() {
            return new WildcardTypeImpl(this.upperBounds, this.lowerBounds);
        }
    }
}

