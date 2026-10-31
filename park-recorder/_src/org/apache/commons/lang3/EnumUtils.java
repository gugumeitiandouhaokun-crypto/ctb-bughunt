/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

public class EnumUtils {
    private static final String NULL_ELEMENTS_NOT_PERMITTED = "null elements not permitted";
    private static final String CANNOT_STORE_S_S_VALUES_IN_S_BITS = "Cannot store %s %s values in %s bits";
    private static final String S_DOES_NOT_SEEM_TO_BE_AN_ENUM_TYPE = "%s does not seem to be an Enum type";
    private static final String ENUM_CLASS_MUST_BE_DEFINED = "EnumClass must be defined.";

    public static <E extends Enum<E>> Map<String, E> getEnumMap(Class<E> enumArray) {
        LinkedHashMap<String, Enum> linkedHashMap = new LinkedHashMap<String, Enum>();
        for (Enum enum_ : (Enum[])enumArray.getEnumConstants()) {
            linkedHashMap.put(enum_.name(), enum_);
        }
        return linkedHashMap;
    }

    public static <E extends Enum<E>> List<E> getEnumList(Class<E> clazz) {
        return new ArrayList<E>(Arrays.asList(clazz.getEnumConstants()));
    }

    public static <E extends Enum<E>> boolean isValidEnum(Class<E> clazz, String string) {
        if (string == null) {
            return false;
        }
        try {
            Enum.valueOf(clazz, string);
            return true;
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return false;
        }
    }

    public static <E extends Enum<E>> E getEnum(Class<E> clazz, String string) {
        if (string == null) {
            return null;
        }
        try {
            return Enum.valueOf(clazz, string);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return null;
        }
    }

    public static <E extends Enum<E>> long generateBitVector(Class<E> object, Iterable<? extends E> object2) {
        EnumUtils.checkBitVectorable(object);
        Validate.notNull(object2);
        long l = 0L;
        object = object2.iterator();
        while (object.hasNext()) {
            object2 = (Enum)object.next();
            Validate.isTrue(object2 != null, NULL_ELEMENTS_NOT_PERMITTED, new Object[0]);
            l |= 1L << ((Enum)object2).ordinal();
        }
        return l;
    }

    public static <E extends Enum<E>> long[] generateBitVectors(Class<E> object3, Iterable<? extends E> object2) {
        Object object;
        EnumUtils.asEnum(object3);
        Validate.notNull(object);
        EnumSet<Enum> enumSet = EnumSet.noneOf(object3);
        object = object.iterator();
        while (object.hasNext()) {
            Enum enum_ = (Enum)object.next();
            Validate.isTrue(enum_ != null, NULL_ELEMENTS_NOT_PERMITTED, new Object[0]);
            enumSet.add(enum_);
        }
        object = new long[(((Enum[])((Class)object3).getEnumConstants()).length - 1) / 64 + 1];
        for (Enum enum_ : enumSet) {
            Object object4 = object;
            int n = enum_.ordinal() / 64;
            object4[n] = object4[n] | 1L << enum_.ordinal() % 64;
        }
        ArrayUtils.reverse((long[])object);
        return object;
    }

    public static <E extends Enum<E>> long generateBitVector(Class<E> clazz, E ... EArray) {
        Validate.noNullElements(EArray);
        return EnumUtils.generateBitVector(clazz, Arrays.asList(EArray));
    }

    public static <E extends Enum<E>> long[] generateBitVectors(Class<E> object, E ... object2) {
        EnumUtils.asEnum(object);
        Validate.noNullElements(object2);
        Object object3 = EnumSet.noneOf(object);
        Collections.addAll(object3, object2);
        object = new long[(((Enum[])((Class)object).getEnumConstants()).length - 1) / 64 + 1];
        object2 = ((AbstractCollection)object3).iterator();
        while (object2.hasNext()) {
            object3 = (Enum)object2.next();
            Object object4 = object;
            int n = ((Enum)object3).ordinal() / 64;
            object4[n] = object4[n] | 1L << ((Enum)object3).ordinal() % 64;
        }
        ArrayUtils.reverse((long[])object);
        return object;
    }

    public static <E extends Enum<E>> EnumSet<E> processBitVector(Class<E> clazz, long l) {
        EnumUtils.checkBitVectorable(clazz).getEnumConstants();
        return EnumUtils.processBitVectors(clazz, l);
    }

    public static <E extends Enum<E>> EnumSet<E> processBitVectors(Class<E> enumArray, long ... lArray) {
        EnumSet<Enum> enumSet = EnumSet.noneOf(EnumUtils.asEnum(enumArray));
        lArray = ArrayUtils.clone(Validate.notNull(lArray));
        ArrayUtils.reverse(lArray);
        for (Enum enum_ : (Enum[])enumArray.getEnumConstants()) {
            int n = enum_.ordinal() / 64;
            if (n >= lArray.length || (lArray[n] & 1L << enum_.ordinal() % 64) == 0L) continue;
            enumSet.add(enum_);
        }
        return enumSet;
    }

    private static <E extends Enum<E>> Class<E> checkBitVectorable(Class<E> clazz) {
        Enum[] enumArray = (Enum[])EnumUtils.asEnum(clazz).getEnumConstants();
        Validate.isTrue(enumArray.length <= 64, CANNOT_STORE_S_S_VALUES_IN_S_BITS, enumArray.length, clazz.getSimpleName(), 64);
        return clazz;
    }

    private static <E extends Enum<E>> Class<E> asEnum(Class<E> clazz) {
        Validate.notNull(clazz, ENUM_CLASS_MUST_BE_DEFINED, new Object[0]);
        Validate.isTrue(clazz.isEnum(), S_DOES_NOT_SEEM_TO_BE_AN_ENUM_TYPE, clazz);
        return clazz;
    }
}

