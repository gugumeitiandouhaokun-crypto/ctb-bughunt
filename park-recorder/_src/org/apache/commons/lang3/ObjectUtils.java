/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractCollection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.CloneFailedException;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.text.StrBuilder;

public class ObjectUtils {
    public static final Null NULL = new Null();

    public static <T> T defaultIfNull(T t, T t2) {
        if (t != null) {
            return t;
        }
        return t2;
    }

    public static <T> T firstNonNull(T ... TArray) {
        if (TArray != null) {
            for (T t : TArray) {
                if (t == null) continue;
                return t;
            }
        }
        return null;
    }

    public static boolean anyNotNull(Object ... objectArray) {
        return ObjectUtils.firstNonNull(objectArray) != null;
    }

    public static boolean allNotNull(Object ... objectArray) {
        if (objectArray == null) {
            return false;
        }
        int n = objectArray.length;
        for (int j = 0; j < n; ++j) {
            if (objectArray[j] != null) continue;
            return false;
        }
        return true;
    }

    @Deprecated
    public static boolean equals(Object object, Object object2) {
        if (object == object2) {
            return true;
        }
        if (object == null || object2 == null) {
            return false;
        }
        return object.equals(object2);
    }

    public static boolean notEqual(Object object, Object object2) {
        return !ObjectUtils.equals(object, object2);
    }

    @Deprecated
    public static int hashCode(Object object) {
        if (object == null) {
            return 0;
        }
        return object.hashCode();
    }

    @Deprecated
    public static int hashCodeMulti(Object ... objectArray) {
        int n = 1;
        if (objectArray != null) {
            int n2 = objectArray.length;
            for (int j = 0; j < n2; ++j) {
                int n3 = ObjectUtils.hashCode(objectArray[j]);
                n = n * 31 + n3;
            }
        }
        return n;
    }

    public static String identityToString(Object object) {
        if (object == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        ObjectUtils.identityToString(stringBuilder, object);
        return stringBuilder.toString();
    }

    public static void identityToString(Appendable appendable, Object object) {
        if (object == null) {
            throw new NullPointerException("Cannot get the toString of a null identity");
        }
        appendable.append(object.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
    }

    public static void identityToString(StrBuilder strBuilder, Object object) {
        if (object == null) {
            throw new NullPointerException("Cannot get the toString of a null identity");
        }
        strBuilder.append(object.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
    }

    public static void identityToString(StringBuffer stringBuffer, Object object) {
        if (object == null) {
            throw new NullPointerException("Cannot get the toString of a null identity");
        }
        stringBuffer.append(object.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
    }

    public static void identityToString(StringBuilder stringBuilder, Object object) {
        if (object == null) {
            throw new NullPointerException("Cannot get the toString of a null identity");
        }
        stringBuilder.append(object.getClass().getName()).append('@').append(Integer.toHexString(System.identityHashCode(object)));
    }

    @Deprecated
    public static String toString(Object object) {
        if (object == null) {
            return "";
        }
        return object.toString();
    }

    @Deprecated
    public static String toString(Object object, String string) {
        if (object == null) {
            return string;
        }
        return object.toString();
    }

    public static <T extends Comparable<? super T>> T min(T ... TArray) {
        T t = null;
        if (TArray != null) {
            for (T t2 : TArray) {
                if (ObjectUtils.compare(t2, t, true) >= 0) continue;
                t = t2;
            }
        }
        return t;
    }

    public static <T extends Comparable<? super T>> T max(T ... TArray) {
        T t = null;
        if (TArray != null) {
            for (T t2 : TArray) {
                if (ObjectUtils.compare(t2, t, false) <= 0) continue;
                t = t2;
            }
        }
        return t;
    }

    public static <T extends Comparable<? super T>> int compare(T t, T t2) {
        return ObjectUtils.compare(t, t2, false);
    }

    public static <T extends Comparable<? super T>> int compare(T t, T t2, boolean bl) {
        if (t == t2) {
            return 0;
        }
        if (t == null) {
            if (bl) {
                return 1;
            }
            return -1;
        }
        if (t2 == null) {
            if (bl) {
                return -1;
            }
            return 1;
        }
        return t.compareTo(t2);
    }

    public static <T extends Comparable<? super T>> T median(T ... TArray) {
        Validate.notEmpty(TArray);
        Validate.noNullElements(TArray);
        TreeSet treeSet = new TreeSet();
        Collections.addAll(treeSet, TArray);
        return (T)((Comparable)treeSet.toArray()[(treeSet.size() - 1) / 2]);
    }

    public static <T> T median(Comparator<T> object, T ... TArray) {
        Validate.notEmpty(TArray, "null/empty items", new Object[0]);
        Validate.noNullElements(TArray);
        Validate.notNull(object, "null comparator", new Object[0]);
        object = new TreeSet<T>(object);
        Collections.addAll(object, TArray);
        return (T)((AbstractCollection)object).toArray()[(((TreeSet)object).size() - 1) / 2];
    }

    /*
     * WARNING - void declaration
     */
    public static <T> T mode(T ... object) {
        if (ArrayUtils.isNotEmpty(object)) {
            void var0_2;
            HashMap<Object, MutableInt> hashMap = new HashMap<Object, MutableInt>(((Object)object).length);
            for (Object object2 : object) {
                MutableInt mutableInt = (MutableInt)hashMap.get(object2);
                if (mutableInt == null) {
                    hashMap.put(object2, new MutableInt(1));
                    continue;
                }
                mutableInt.increment();
            }
            Object var0_1 = null;
            int n = 0;
            for (Map.Entry entry : hashMap.entrySet()) {
                int n2 = ((MutableInt)entry.getValue()).intValue();
                if (n2 == n) {
                    Object var0_3 = null;
                    continue;
                }
                if (n2 <= n) continue;
                n = n2;
                Object k = entry.getKey();
            }
            return var0_2;
        }
        return null;
    }

    public static <T> T clone(T t) {
        if (t instanceof Cloneable) {
            Class<?> clazz;
            if (t.getClass().isArray()) {
                clazz = t.getClass().getComponentType();
                if (!clazz.isPrimitive()) {
                    clazz = ((Object[])t).clone();
                } else {
                    int n = Array.getLength(t);
                    clazz = Array.newInstance(clazz, n);
                    while (n-- > 0) {
                        Array.set(clazz, n, Array.get(t, n));
                    }
                }
            } else {
                try {
                    clazz = t.getClass().getMethod("clone", new Class[0]).invoke(t, new Object[0]);
                }
                catch (NoSuchMethodException noSuchMethodException) {
                    throw new CloneFailedException("Cloneable type " + t.getClass().getName() + " has no clone method", noSuchMethodException);
                }
                catch (IllegalAccessException illegalAccessException) {
                    throw new CloneFailedException("Cannot clone Cloneable type " + t.getClass().getName(), illegalAccessException);
                }
                catch (InvocationTargetException invocationTargetException) {
                    throw new CloneFailedException("Exception cloning Cloneable type " + t.getClass().getName(), invocationTargetException.getCause());
                }
            }
            return (T)clazz;
        }
        return null;
    }

    public static <T> T cloneIfPossible(T t) {
        T t2 = ObjectUtils.clone(t);
        if (t2 == null) {
            return t;
        }
        return t2;
    }

    public static boolean CONST(boolean bl) {
        return bl;
    }

    public static byte CONST(byte by) {
        return by;
    }

    public static byte CONST_BYTE(int n) {
        if (n < -128 || n > 127) {
            throw new IllegalArgumentException("Supplied value must be a valid byte literal between -128 and 127: [" + n + "]");
        }
        return (byte)n;
    }

    public static char CONST(char c2) {
        return c2;
    }

    public static short CONST(short s) {
        return s;
    }

    public static short CONST_SHORT(int n) {
        if (n < Short.MIN_VALUE || n > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Supplied value must be a valid byte literal between -32768 and 32767: [" + n + "]");
        }
        return (short)n;
    }

    public static int CONST(int n) {
        return n;
    }

    public static long CONST(long l) {
        return l;
    }

    public static float CONST(float f2) {
        return f2;
    }

    public static double CONST(double d2) {
        return d2;
    }

    public static <T> T CONST(T t) {
        return t;
    }

    public static class Null
    implements Serializable {
        private static final long serialVersionUID = 7092611880189329093L;

        Null() {
        }

        private Object readResolve() {
            return NULL;
        }
    }
}

