/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.builder;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.lang3.builder.EqualsExclude;
import org.apache.commons.lang3.builder.IDKey;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;

public class EqualsBuilder
implements Builder<Boolean> {
    private static final ThreadLocal<Set<Pair<IDKey, IDKey>>> REGISTRY = new ThreadLocal();
    private boolean isEquals = true;

    static Set<Pair<IDKey, IDKey>> getRegistry() {
        return REGISTRY.get();
    }

    static Pair<IDKey, IDKey> getRegisterPair(Object object, Object object2) {
        object = new IDKey(object);
        object2 = new IDKey(object2);
        return Pair.of(object, object2);
    }

    static boolean isRegistered(Object pair, Object pair2) {
        Set<Pair<IDKey, IDKey>> set = EqualsBuilder.getRegistry();
        pair = EqualsBuilder.getRegisterPair(pair, pair2);
        pair2 = Pair.of(pair.getLeft(), pair.getRight());
        return set != null && (set.contains(pair) || set.contains(pair2));
    }

    private static void register(Object pair, Object object) {
        Set<Pair<IDKey, IDKey>> set = EqualsBuilder.getRegistry();
        if (set == null) {
            set = new HashSet<Pair<IDKey, IDKey>>();
            REGISTRY.set(set);
        }
        pair = EqualsBuilder.getRegisterPair(pair, object);
        set.add(pair);
    }

    private static void unregister(Object pair, Object object) {
        Set<Pair<IDKey, IDKey>> set = EqualsBuilder.getRegistry();
        if (set != null) {
            pair = EqualsBuilder.getRegisterPair(pair, object);
            set.remove(pair);
            if (set.isEmpty()) {
                REGISTRY.remove();
            }
        }
    }

    public static boolean reflectionEquals(Object object, Object object2, Collection<String> collection) {
        return EqualsBuilder.reflectionEquals(object, object2, ReflectionToStringBuilder.toNoNullStringArray(collection));
    }

    public static boolean reflectionEquals(Object object, Object object2, String ... stringArray) {
        return EqualsBuilder.reflectionEquals(object, object2, false, null, stringArray);
    }

    public static boolean reflectionEquals(Object object, Object object2, boolean bl) {
        return EqualsBuilder.reflectionEquals(object, object2, bl, null, new String[0]);
    }

    public static boolean reflectionEquals(Object object, Object object2, boolean bl, Class<?> clazz, String ... stringArray) {
        Object object3;
        block13: {
            Class clazz2;
            if (object == object2) {
                return true;
            }
            if (object == null || object2 == null) {
                return false;
            }
            object3 = object.getClass();
            Class<?> clazz3 = object2.getClass();
            if (((Class)object3).isInstance(object2)) {
                clazz2 = object3;
                if (!clazz3.isInstance(object)) {
                    clazz2 = clazz3;
                }
            } else if (clazz3.isInstance(object)) {
                clazz2 = clazz3;
                if (!((Class)object3).isInstance(object2)) {
                    clazz2 = object3;
                }
            } else {
                return false;
            }
            object3 = new EqualsBuilder();
            try {
                if (clazz2.isArray()) {
                    ((EqualsBuilder)object3).append(object, object2);
                    break block13;
                }
                while (true) {
                    EqualsBuilder.reflectionAppend(object, object2, clazz2, (EqualsBuilder)object3, bl, stringArray);
                    if (clazz2.getSuperclass() != null && clazz2 != clazz) {
                        clazz2 = clazz2.getSuperclass();
                        continue;
                    }
                    break;
                }
            }
            catch (IllegalArgumentException illegalArgumentException) {
                return false;
            }
        }
        return ((EqualsBuilder)object3).isEquals();
    }

    private static void reflectionAppend(Object object, Object object2, Class<?> accessibleObjectArray, EqualsBuilder equalsBuilder, boolean bl, String[] stringArray) {
        if (EqualsBuilder.isRegistered(object, object2)) {
            return;
        }
        try {
            EqualsBuilder.register(object, object2);
            accessibleObjectArray = accessibleObjectArray.getDeclaredFields();
            AccessibleObject.setAccessible(accessibleObjectArray, true);
            for (int i2 = 0; i2 < accessibleObjectArray.length && equalsBuilder.isEquals; ++i2) {
                AccessibleObject accessibleObject = accessibleObjectArray[i2];
                if (ArrayUtils.contains(stringArray, ((Field)accessibleObject).getName()) || ((Field)accessibleObject).getName().contains("$") || !bl && Modifier.isTransient(((Field)accessibleObject).getModifiers()) || Modifier.isStatic(((Field)accessibleObject).getModifiers()) || accessibleObject.isAnnotationPresent(EqualsExclude.class)) continue;
                try {
                    equalsBuilder.append(((Field)accessibleObject).get(object), ((Field)accessibleObject).get(object2));
                    continue;
                }
                catch (IllegalAccessException illegalAccessException) {
                    throw new InternalError("Unexpected IllegalAccessException");
                }
            }
            return;
        }
        finally {
            EqualsBuilder.unregister(object, object2);
        }
    }

    public EqualsBuilder appendSuper(boolean bl) {
        if (!this.isEquals) {
            return this;
        }
        this.isEquals = bl;
        return this;
    }

    public EqualsBuilder append(Object object, Object object2) {
        if (!this.isEquals) {
            return this;
        }
        if (object == object2) {
            return this;
        }
        if (object == null || object2 == null) {
            this.setEquals(false);
            return this;
        }
        if (!object.getClass().isArray()) {
            this.isEquals = object.equals(object2);
        } else {
            this.appendArray(object, object2);
        }
        return this;
    }

    private void appendArray(Object object, Object object2) {
        if (object.getClass() != object2.getClass()) {
            this.setEquals(false);
            return;
        }
        if (object instanceof long[]) {
            this.append((long[])object, (long[])object2);
            return;
        }
        if (object instanceof int[]) {
            this.append((int[])object, (int[])object2);
            return;
        }
        if (object instanceof short[]) {
            this.append((short[])object, (short[])object2);
            return;
        }
        if (object instanceof char[]) {
            this.append((char[])object, (char[])object2);
            return;
        }
        if (object instanceof byte[]) {
            this.append((byte[])object, (byte[])object2);
            return;
        }
        if (object instanceof double[]) {
            this.append((double[])object, (double[])object2);
            return;
        }
        if (object instanceof float[]) {
            this.append((float[])object, (float[])object2);
            return;
        }
        if (object instanceof boolean[]) {
            this.append((boolean[])object, (boolean[])object2);
            return;
        }
        this.append((Object[])object, (Object[])object2);
    }

    public EqualsBuilder append(long l, long l2) {
        if (!this.isEquals) {
            return this;
        }
        this.isEquals = l == l2;
        return this;
    }

    public EqualsBuilder append(int n, int n2) {
        if (!this.isEquals) {
            return this;
        }
        this.isEquals = n == n2;
        return this;
    }

    public EqualsBuilder append(short s, short s2) {
        if (!this.isEquals) {
            return this;
        }
        this.isEquals = s == s2;
        return this;
    }

    public EqualsBuilder append(char c2, char c3) {
        if (!this.isEquals) {
            return this;
        }
        this.isEquals = c2 == c3;
        return this;
    }

    public EqualsBuilder append(byte by, byte by2) {
        if (!this.isEquals) {
            return this;
        }
        this.isEquals = by == by2;
        return this;
    }

    public EqualsBuilder append(double d2, double d3) {
        if (!this.isEquals) {
            return this;
        }
        return this.append(Double.doubleToLongBits(d2), Double.doubleToLongBits(d3));
    }

    public EqualsBuilder append(float f2, float f3) {
        if (!this.isEquals) {
            return this;
        }
        return this.append(Float.floatToIntBits(f2), Float.floatToIntBits(f3));
    }

    public EqualsBuilder append(boolean bl, boolean bl2) {
        if (!this.isEquals) {
            return this;
        }
        this.isEquals = bl == bl2;
        return this;
    }

    public EqualsBuilder append(Object[] objectArray, Object[] objectArray2) {
        if (!this.isEquals) {
            return this;
        }
        if (objectArray == objectArray2) {
            return this;
        }
        if (objectArray == null || objectArray2 == null) {
            this.setEquals(false);
            return this;
        }
        if (objectArray.length != objectArray2.length) {
            this.setEquals(false);
            return this;
        }
        for (int i2 = 0; i2 < objectArray.length && this.isEquals; ++i2) {
            this.append(objectArray[i2], objectArray2[i2]);
        }
        return this;
    }

    public EqualsBuilder append(long[] lArray, long[] lArray2) {
        if (!this.isEquals) {
            return this;
        }
        if (lArray == lArray2) {
            return this;
        }
        if (lArray == null || lArray2 == null) {
            this.setEquals(false);
            return this;
        }
        if (lArray.length != lArray2.length) {
            this.setEquals(false);
            return this;
        }
        for (int i2 = 0; i2 < lArray.length && this.isEquals; ++i2) {
            this.append(lArray[i2], lArray2[i2]);
        }
        return this;
    }

    public EqualsBuilder append(int[] nArray, int[] nArray2) {
        if (!this.isEquals) {
            return this;
        }
        if (nArray == nArray2) {
            return this;
        }
        if (nArray == null || nArray2 == null) {
            this.setEquals(false);
            return this;
        }
        if (nArray.length != nArray2.length) {
            this.setEquals(false);
            return this;
        }
        for (int i2 = 0; i2 < nArray.length && this.isEquals; ++i2) {
            this.append(nArray[i2], nArray2[i2]);
        }
        return this;
    }

    public EqualsBuilder append(short[] sArray, short[] sArray2) {
        if (!this.isEquals) {
            return this;
        }
        if (sArray == sArray2) {
            return this;
        }
        if (sArray == null || sArray2 == null) {
            this.setEquals(false);
            return this;
        }
        if (sArray.length != sArray2.length) {
            this.setEquals(false);
            return this;
        }
        for (int i2 = 0; i2 < sArray.length && this.isEquals; ++i2) {
            this.append(sArray[i2], sArray2[i2]);
        }
        return this;
    }

    public EqualsBuilder append(char[] cArray, char[] cArray2) {
        if (!this.isEquals) {
            return this;
        }
        if (cArray == cArray2) {
            return this;
        }
        if (cArray == null || cArray2 == null) {
            this.setEquals(false);
            return this;
        }
        if (cArray.length != cArray2.length) {
            this.setEquals(false);
            return this;
        }
        for (int i2 = 0; i2 < cArray.length && this.isEquals; ++i2) {
            this.append(cArray[i2], cArray2[i2]);
        }
        return this;
    }

    public EqualsBuilder append(byte[] byArray, byte[] byArray2) {
        if (!this.isEquals) {
            return this;
        }
        if (byArray == byArray2) {
            return this;
        }
        if (byArray == null || byArray2 == null) {
            this.setEquals(false);
            return this;
        }
        if (byArray.length != byArray2.length) {
            this.setEquals(false);
            return this;
        }
        for (int i2 = 0; i2 < byArray.length && this.isEquals; ++i2) {
            this.append(byArray[i2], byArray2[i2]);
        }
        return this;
    }

    public EqualsBuilder append(double[] dArray, double[] dArray2) {
        if (!this.isEquals) {
            return this;
        }
        if (dArray == dArray2) {
            return this;
        }
        if (dArray == null || dArray2 == null) {
            this.setEquals(false);
            return this;
        }
        if (dArray.length != dArray2.length) {
            this.setEquals(false);
            return this;
        }
        for (int i2 = 0; i2 < dArray.length && this.isEquals; ++i2) {
            this.append(dArray[i2], dArray2[i2]);
        }
        return this;
    }

    public EqualsBuilder append(float[] fArray, float[] fArray2) {
        if (!this.isEquals) {
            return this;
        }
        if (fArray == fArray2) {
            return this;
        }
        if (fArray == null || fArray2 == null) {
            this.setEquals(false);
            return this;
        }
        if (fArray.length != fArray2.length) {
            this.setEquals(false);
            return this;
        }
        for (int i2 = 0; i2 < fArray.length && this.isEquals; ++i2) {
            this.append(fArray[i2], fArray2[i2]);
        }
        return this;
    }

    public EqualsBuilder append(boolean[] blArray, boolean[] blArray2) {
        if (!this.isEquals) {
            return this;
        }
        if (blArray == blArray2) {
            return this;
        }
        if (blArray == null || blArray2 == null) {
            this.setEquals(false);
            return this;
        }
        if (blArray.length != blArray2.length) {
            this.setEquals(false);
            return this;
        }
        for (int i2 = 0; i2 < blArray.length && this.isEquals; ++i2) {
            this.append(blArray[i2], blArray2[i2]);
        }
        return this;
    }

    public boolean isEquals() {
        return this.isEquals;
    }

    @Override
    public Boolean build() {
        return this.isEquals();
    }

    protected void setEquals(boolean bl) {
        this.isEquals = bl;
    }

    public void reset() {
        this.isEquals = true;
    }
}

