/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.SerializationException;

public class SerializationUtils {
    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static <T extends Serializable> T clone(T object) {
        if (object == null) {
            return null;
        }
        Object object2 = SerializationUtils.serialize((Serializable)object);
        object2 = new ByteArrayInputStream((byte[])object2);
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ClassLoaderAwareObjectInputStream((InputStream)object2, object.getClass().getClassLoader());
            object = (Serializable)objectInputStream.readObject();
        }
        catch (ClassNotFoundException classNotFoundException) {
            try {
                throw new SerializationException("ClassNotFoundException while reading cloned object data", classNotFoundException);
                catch (IOException iOException) {
                    throw new SerializationException("IOException while reading cloned object data", iOException);
                }
            }
            catch (Throwable throwable) {
                try {
                    if (objectInputStream == null) throw throwable;
                    objectInputStream.close();
                    throw throwable;
                }
                catch (IOException iOException) {
                    throw new SerializationException("IOException on closing cloned object data InputStream.", iOException);
                }
            }
        }
        try {
            objectInputStream.close();
            return (T)object;
        }
        catch (IOException iOException) {
            throw new SerializationException("IOException on closing cloned object data InputStream.", iOException);
        }
    }

    public static <T extends Serializable> T roundtrip(T t) {
        return (T)((Serializable)SerializationUtils.deserialize(SerializationUtils.serialize(t)));
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static void serialize(Serializable serializable, OutputStream outputStream) {
        if (outputStream == null) {
            throw new IllegalArgumentException("The OutputStream must not be null");
        }
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(serializable);
        }
        catch (IOException iOException) {
            try {
                throw new SerializationException(iOException);
            }
            catch (Throwable throwable) {
                try {
                    if (objectOutputStream == null) throw throwable;
                    objectOutputStream.close();
                    throw throwable;
                }
                catch (IOException iOException2) {}
                throw throwable;
            }
        }
        try {
            objectOutputStream.close();
            return;
        }
        catch (IOException iOException) {
            return;
        }
    }

    public static byte[] serialize(Serializable serializable) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(512);
        SerializationUtils.serialize(serializable, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static <T> T deserialize(InputStream object) {
        if (object == null) {
            throw new IllegalArgumentException("The InputStream must not be null");
        }
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream((InputStream)object);
            object = objectInputStream.readObject();
        }
        catch (ClassNotFoundException classNotFoundException) {
            try {
                throw new SerializationException(classNotFoundException);
                catch (IOException iOException) {
                    throw new SerializationException(iOException);
                }
            }
            catch (Throwable throwable) {
                try {
                    if (objectInputStream == null) throw throwable;
                    objectInputStream.close();
                    throw throwable;
                }
                catch (IOException iOException) {}
                throw throwable;
            }
        }
        try {
            objectInputStream.close();
            return (T)object;
        }
        catch (IOException iOException) {}
        return (T)object;
    }

    public static <T> T deserialize(byte[] byArray) {
        if (byArray == null) {
            throw new IllegalArgumentException("The byte[] must not be null");
        }
        return SerializationUtils.deserialize(new ByteArrayInputStream(byArray));
    }

    static class ClassLoaderAwareObjectInputStream
    extends ObjectInputStream {
        private static final Map<String, Class<?>> primitiveTypes = new HashMap();
        private final ClassLoader classLoader;

        public ClassLoaderAwareObjectInputStream(InputStream inputStream, ClassLoader classLoader) {
            super(inputStream);
            this.classLoader = classLoader;
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass clazz) {
            clazz = ((ObjectStreamClass)((Object)clazz)).getName();
            try {
                return Class.forName((String)((Object)clazz), false, this.classLoader);
            }
            catch (ClassNotFoundException classNotFoundException) {
                try {
                    return Class.forName((String)((Object)clazz), false, Thread.currentThread().getContextClassLoader());
                }
                catch (ClassNotFoundException classNotFoundException2) {
                    clazz = primitiveTypes.get(clazz);
                    if (clazz != null) {
                        return clazz;
                    }
                    throw classNotFoundException2;
                }
            }
        }

        static {
            primitiveTypes.put("byte", Byte.TYPE);
            primitiveTypes.put("short", Short.TYPE);
            primitiveTypes.put("int", Integer.TYPE);
            primitiveTypes.put("long", Long.TYPE);
            primitiveTypes.put("float", Float.TYPE);
            primitiveTypes.put("double", Double.TYPE);
            primitiveTypes.put("boolean", Boolean.TYPE);
            primitiveTypes.put("char", Character.TYPE);
            primitiveTypes.put("void", Void.TYPE);
        }
    }
}

