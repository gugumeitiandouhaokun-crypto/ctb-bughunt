/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.FilteredObjectInputStream;
import org.apache.logging.log4j.util.IndexedStringMap;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.StringMap;
import org.apache.logging.log4j.util.TriConsumer;

public class SortedArrayStringMap
implements IndexedStringMap {
    private static final int DEFAULT_INITIAL_CAPACITY = 4;
    private static final long serialVersionUID = -5748905872274478116L;
    private static final int HASHVAL = 31;
    private static final TriConsumer<String, Object, StringMap> PUT_ALL;
    private static final String[] EMPTY;
    private static final String FROZEN = "Frozen collection cannot be modified";
    private transient String[] keys = EMPTY;
    private transient Object[] values = EMPTY;
    private transient int size;
    private static final Method setObjectInputFilter;
    private static final Method getObjectInputFilter;
    private static final Method newObjectInputFilter;
    private int threshold;
    private boolean immutable;
    private transient boolean iterating;

    public SortedArrayStringMap() {
        this(4);
    }

    public SortedArrayStringMap(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Initial capacity must be at least zero but was ".concat(String.valueOf(n)));
        }
        this.threshold = SortedArrayStringMap.ceilingNextPowerOfTwo(n == 0 ? 1 : n);
    }

    public SortedArrayStringMap(ReadOnlyStringMap readOnlyStringMap) {
        if (readOnlyStringMap instanceof SortedArrayStringMap) {
            this.initFrom0((SortedArrayStringMap)readOnlyStringMap);
            return;
        }
        if (readOnlyStringMap != null) {
            this.resize(SortedArrayStringMap.ceilingNextPowerOfTwo(readOnlyStringMap.size()));
            readOnlyStringMap.forEach(PUT_ALL, this);
        }
    }

    public SortedArrayStringMap(Map<String, ?> object) {
        this.resize(SortedArrayStringMap.ceilingNextPowerOfTwo(object.size()));
        for (Map.Entry entry : object.entrySet()) {
            this.putValue((String)entry.getKey(), entry.getValue());
        }
    }

    private void assertNotFrozen() {
        if (this.immutable) {
            throw new UnsupportedOperationException(FROZEN);
        }
    }

    private void assertNoConcurrentModification() {
        if (this.iterating) {
            throw new ConcurrentModificationException();
        }
    }

    @Override
    public void clear() {
        if (this.keys == EMPTY) {
            return;
        }
        this.assertNotFrozen();
        this.assertNoConcurrentModification();
        Arrays.fill(this.keys, 0, this.size, null);
        Arrays.fill(this.values, 0, this.size, null);
        this.size = 0;
    }

    @Override
    public boolean containsKey(String string) {
        return this.indexOfKey(string) >= 0;
    }

    @Override
    public Map<String, String> toMap() {
        HashMap<String, String> hashMap = new HashMap<String, String>(this.size());
        for (int j = 0; j < this.size(); ++j) {
            Object v = this.getValueAt(j);
            hashMap.put(this.getKeyAt(j), v == null ? null : String.valueOf(v));
        }
        return hashMap;
    }

    @Override
    public void freeze() {
        this.immutable = true;
    }

    @Override
    public boolean isFrozen() {
        return this.immutable;
    }

    @Override
    public <V> V getValue(String string) {
        int n = this.indexOfKey(string);
        if (n < 0) {
            return null;
        }
        return (V)this.values[n];
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public int indexOfKey(String string) {
        if (this.keys == EMPTY) {
            return -1;
        }
        if (string == null) {
            return this.nullKeyIndex();
        }
        int n = this.size > 0 && this.keys[0] == null ? 1 : 0;
        return Arrays.binarySearch(this.keys, n, this.size, string);
    }

    private int nullKeyIndex() {
        if (this.size > 0 && this.keys[0] == null) {
            return 0;
        }
        return -1;
    }

    @Override
    public void putValue(String string, Object object) {
        int n;
        this.assertNotFrozen();
        this.assertNoConcurrentModification();
        if (this.keys == EMPTY) {
            SortedArrayStringMap sortedArrayStringMap = this;
            sortedArrayStringMap.inflateTable(sortedArrayStringMap.threshold);
        }
        if ((n = this.indexOfKey(string)) >= 0) {
            this.keys[n] = string;
            this.values[n] = object;
            return;
        }
        this.insertAt(~n, string, object);
    }

    private void insertAt(int n, String string, Object object) {
        this.ensureCapacity();
        System.arraycopy(this.keys, n, this.keys, n + 1, this.size - n);
        System.arraycopy(this.values, n, this.values, n + 1, this.size - n);
        this.keys[n] = string;
        this.values[n] = object;
        ++this.size;
    }

    @Override
    public void putAll(ReadOnlyStringMap readOnlyStringMap) {
        if (readOnlyStringMap == this || readOnlyStringMap == null || readOnlyStringMap.isEmpty()) {
            return;
        }
        this.assertNotFrozen();
        this.assertNoConcurrentModification();
        if (readOnlyStringMap instanceof SortedArrayStringMap) {
            if (this.size == 0) {
                this.initFrom0((SortedArrayStringMap)readOnlyStringMap);
                return;
            }
            this.merge((SortedArrayStringMap)readOnlyStringMap);
            return;
        }
        if (readOnlyStringMap != null) {
            readOnlyStringMap.forEach(PUT_ALL, this);
        }
    }

    private void initFrom0(SortedArrayStringMap sortedArrayStringMap) {
        if (this.keys.length < sortedArrayStringMap.size) {
            this.keys = new String[sortedArrayStringMap.threshold];
            this.values = new Object[sortedArrayStringMap.threshold];
        }
        System.arraycopy(sortedArrayStringMap.keys, 0, this.keys, 0, sortedArrayStringMap.size);
        System.arraycopy(sortedArrayStringMap.values, 0, this.values, 0, sortedArrayStringMap.size);
        this.size = sortedArrayStringMap.size;
        this.threshold = sortedArrayStringMap.threshold;
    }

    private void merge(SortedArrayStringMap sortedArrayStringMap) {
        String[] stringArray = this.keys;
        Object[] objectArray = this.values;
        int n = sortedArrayStringMap.size + this.size;
        this.threshold = SortedArrayStringMap.ceilingNextPowerOfTwo(n);
        if (this.keys.length < this.threshold) {
            this.keys = new String[this.threshold];
            this.values = new Object[this.threshold];
        }
        boolean bl = true;
        if (sortedArrayStringMap.size() > this.size()) {
            System.arraycopy(stringArray, 0, this.keys, sortedArrayStringMap.size, this.size);
            System.arraycopy(objectArray, 0, this.values, sortedArrayStringMap.size, this.size);
            System.arraycopy(sortedArrayStringMap.keys, 0, this.keys, 0, sortedArrayStringMap.size);
            System.arraycopy(sortedArrayStringMap.values, 0, this.values, 0, sortedArrayStringMap.size);
            this.size = sortedArrayStringMap.size;
            bl = false;
        } else {
            System.arraycopy(stringArray, 0, this.keys, 0, this.size);
            System.arraycopy(objectArray, 0, this.values, 0, this.size);
            System.arraycopy(sortedArrayStringMap.keys, 0, this.keys, this.size, sortedArrayStringMap.size);
            System.arraycopy(sortedArrayStringMap.values, 0, this.values, this.size, sortedArrayStringMap.size);
        }
        for (int j = this.size; j < n; ++j) {
            SortedArrayStringMap sortedArrayStringMap2 = this;
            int n2 = sortedArrayStringMap2.indexOfKey(sortedArrayStringMap2.keys[j]);
            if (n2 < 0) {
                this.insertAt(~n2, this.keys[j], this.values[j]);
                continue;
            }
            if (!bl) continue;
            this.keys[n2] = this.keys[j];
            this.values[n2] = this.values[j];
        }
        Arrays.fill(this.keys, this.size, n, null);
        Arrays.fill(this.values, this.size, n, null);
    }

    private void ensureCapacity() {
        if (this.size >= this.threshold) {
            SortedArrayStringMap sortedArrayStringMap = this;
            sortedArrayStringMap.resize(sortedArrayStringMap.threshold * 2);
        }
    }

    private void resize(int n) {
        String[] stringArray = this.keys;
        Object[] objectArray = this.values;
        this.keys = new String[n];
        this.values = new Object[n];
        System.arraycopy(stringArray, 0, this.keys, 0, this.size);
        System.arraycopy(objectArray, 0, this.values, 0, this.size);
        this.threshold = n;
    }

    private void inflateTable(int n) {
        this.threshold = n;
        this.keys = new String[n];
        this.values = new Object[n];
    }

    @Override
    public void remove(String string) {
        if (this.keys == EMPTY) {
            return;
        }
        int n = this.indexOfKey(string);
        if (n >= 0) {
            this.assertNotFrozen();
            this.assertNoConcurrentModification();
            System.arraycopy(this.keys, n + 1, this.keys, n, this.size - 1 - n);
            System.arraycopy(this.values, n + 1, this.values, n, this.size - 1 - n);
            this.keys[this.size - 1] = null;
            this.values[this.size - 1] = null;
            --this.size;
        }
    }

    @Override
    public String getKeyAt(int n) {
        if (n < 0 || n >= this.size) {
            return null;
        }
        return this.keys[n];
    }

    @Override
    public <V> V getValueAt(int n) {
        if (n < 0 || n >= this.size) {
            return null;
        }
        return (V)this.values[n];
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public <V> void forEach(BiConsumer<String, ? super V> biConsumer) {
        this.iterating = true;
        try {
            for (int j = 0; j < this.size; ++j) {
                biConsumer.accept(this.keys[j], this.values[j]);
            }
            return;
        }
        finally {
            this.iterating = false;
        }
    }

    public <V, T> void forEach(TriConsumer<String, ? super V, T> triConsumer, T t) {
        this.iterating = true;
        try {
            for (int j = 0; j < this.size; ++j) {
                triConsumer.accept(this.keys[j], this.values[j], t);
            }
            return;
        }
        finally {
            this.iterating = false;
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof SortedArrayStringMap)) {
            return false;
        }
        object = (SortedArrayStringMap)object;
        if (this.size() != ((SortedArrayStringMap)object).size()) {
            return false;
        }
        for (int j = 0; j < this.size(); ++j) {
            if (!Objects.equals(this.keys[j], ((SortedArrayStringMap)object).keys[j])) {
                return false;
            }
            if (Objects.equals(this.values[j], ((SortedArrayStringMap)object).values[j])) continue;
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int n = 1147 + this.size;
        n = 31 * n + SortedArrayStringMap.hashCode(this.keys, this.size);
        return 31 * n + SortedArrayStringMap.hashCode(this.values, this.size);
    }

    private static int hashCode(Object[] objectArray, int n) {
        int n2 = 1;
        for (int j = 0; j < n; ++j) {
            n2 = 31 * n2 + (objectArray[j] == null ? 0 : objectArray[j].hashCode());
        }
        return n2;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(256);
        stringBuilder.append('{');
        for (int j = 0; j < this.size; ++j) {
            if (j > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(this.keys[j]).append('=');
            stringBuilder.append(this.values[j] == this ? "(this map)" : this.values[j]);
        }
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    private void writeObject(ObjectOutputStream objectOutputStream) {
        objectOutputStream.defaultWriteObject();
        if (this.keys == EMPTY) {
            objectOutputStream.writeInt(SortedArrayStringMap.ceilingNextPowerOfTwo(this.threshold));
        } else {
            objectOutputStream.writeInt(this.keys.length);
        }
        objectOutputStream.writeInt(this.size);
        if (this.size > 0) {
            for (int j = 0; j < this.size; ++j) {
                objectOutputStream.writeObject(this.keys[j]);
                try {
                    objectOutputStream.writeObject(SortedArrayStringMap.marshall(this.values[j]));
                    continue;
                }
                catch (Exception exception) {
                    this.handleSerializationException(exception, j, this.keys[j]);
                    objectOutputStream.writeObject(null);
                }
            }
        }
    }

    private static byte[] marshall(Object object) {
        if (object == null) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        Throwable throwable = null;
        try {
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            object = byteArrayOutputStream.toByteArray();
        }
        catch (Throwable throwable2) {
            try {
                object = throwable2;
                throwable = throwable2;
                throw object;
            }
            catch (Throwable throwable3) {
                if (throwable != null) {
                    try {
                        objectOutputStream.close();
                    }
                    catch (Throwable throwable4) {
                        throwable.addSuppressed(throwable4);
                    }
                } else {
                    objectOutputStream.close();
                }
                throw throwable3;
            }
        }
        objectOutputStream.close();
        return object;
    }

    private static Object unmarshall(byte[] object, ObjectInputStream object2) {
        object = new ByteArrayInputStream((byte[])object);
        if (object2 instanceof FilteredObjectInputStream) {
            object2 = ((FilteredObjectInputStream)object2).getAllowedClasses();
            object = new FilteredObjectInputStream((InputStream)object, (Collection<String>)object2);
        } else {
            try {
                object2 = getObjectInputFilter.invoke(object2, new Object[0]);
                object2 = newObjectInputFilter.invoke(null, object2);
                object = new ObjectInputStream((InputStream)object);
                setObjectInputFilter.invoke(object, object2);
            }
            catch (IllegalAccessException | InvocationTargetException reflectiveOperationException) {
                throw new StreamCorruptedException("Unable to set ObjectInputFilter on stream");
            }
        }
        try {
            object2 = ((ObjectInputStream)object).readObject();
            return object2;
        }
        finally {
            ((ObjectInputStream)object).close();
        }
    }

    private static int ceilingNextPowerOfTwo(int n) {
        return 1 << 32 - Integer.numberOfLeadingZeros(n - 1);
    }

    private void readObject(ObjectInputStream objectInputStream) {
        if (!(objectInputStream instanceof FilteredObjectInputStream) && setObjectInputFilter == null) {
            throw new IllegalArgumentException("readObject requires a FilteredObjectInputStream or an ObjectInputStream that accepts an ObjectInputFilter");
        }
        objectInputStream.defaultReadObject();
        this.keys = EMPTY;
        this.values = EMPTY;
        int n = objectInputStream.readInt();
        if (n < 0) {
            throw new InvalidObjectException("Illegal capacity: ".concat(String.valueOf(n)));
        }
        int n2 = objectInputStream.readInt();
        if (n2 < 0) {
            throw new InvalidObjectException("Illegal mappings count: ".concat(String.valueOf(n2)));
        }
        if (n2 > 0) {
            this.inflateTable(n);
        } else {
            this.threshold = n;
        }
        for (n = 0; n < n2; ++n) {
            this.keys[n] = (String)objectInputStream.readObject();
            try {
                byte[] byArray = (byte[])objectInputStream.readObject();
                this.values[n] = byArray == null ? null : SortedArrayStringMap.unmarshall(byArray, objectInputStream);
                continue;
            }
            catch (Exception | LinkageError throwable) {
                this.handleSerializationException(throwable, n, this.keys[n]);
                this.values[n] = null;
            }
        }
        this.size = n2;
    }

    private void handleSerializationException(Throwable throwable, int n, String string) {
        StatusLogger.getLogger().warn("Ignoring {} for key[{}] ('{}')", (Object)String.valueOf(throwable), (Object)n, (Object)this.keys[n]);
    }

    static {
        Object object;
        Method method;
        Method method2;
        block5: {
            PUT_ALL = new TriConsumer<String, Object, StringMap>(){

                @Override
                public final void accept(String string, Object object, StringMap stringMap) {
                    stringMap.putValue(string, object);
                }
            };
            EMPTY = new String[0];
            Method[] methodArray = ObjectInputStream.class.getMethods();
            method2 = null;
            method = null;
            object = methodArray;
            int n = methodArray.length;
            for (int j = 0; j < n; ++j) {
                Method method3 = object[j];
                if (method3.getName().equals("setObjectInputFilter")) {
                    method2 = method3;
                    continue;
                }
                if (!method3.getName().equals("getObjectInputFilter")) continue;
                method = method3;
            }
            object = null;
            try {
                if (method2 == null) break block5;
                for (Method method4 : Class.forName("org.apache.logging.log4j.util.internal.DefaultObjectInputFilter").getMethods()) {
                    if (!method4.getName().equals("newInstance") || !Modifier.isStatic(method4.getModifiers())) continue;
                    object = method4;
                    break;
                }
            }
            catch (ClassNotFoundException classNotFoundException) {}
        }
        newObjectInputFilter = object;
        setObjectInputFilter = method2;
        getObjectInputFilter = method;
    }
}

