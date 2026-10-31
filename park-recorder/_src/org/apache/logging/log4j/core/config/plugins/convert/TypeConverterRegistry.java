/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.convert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UnknownFormatConversionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.convert.EnumConverter;
import org.apache.logging.log4j.core.config.plugins.convert.TypeConverter;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.util.ReflectionUtil;
import org.apache.logging.log4j.core.util.TypeUtil;
import org.apache.logging.log4j.status.StatusLogger;

public class TypeConverterRegistry {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static volatile TypeConverterRegistry INSTANCE;
    private static final Object INSTANCE_LOCK;
    private final ConcurrentMap<Type, TypeConverter<?>> registry = new ConcurrentHashMap();

    public static TypeConverterRegistry getInstance() {
        TypeConverterRegistry typeConverterRegistry = INSTANCE;
        if (typeConverterRegistry == null) {
            Object object = INSTANCE_LOCK;
            synchronized (object) {
                typeConverterRegistry = INSTANCE;
                if (typeConverterRegistry == null) {
                    INSTANCE = typeConverterRegistry = new TypeConverterRegistry();
                }
            }
        }
        return typeConverterRegistry;
    }

    public TypeConverter<?> findCompatibleConverter(Type type) {
        Objects.requireNonNull(type, "No type was provided");
        Object object = (TypeConverter)this.registry.get(type);
        if (object != null) {
            return object;
        }
        if (type instanceof Class && ((Class)(object = (Class)type)).isEnum()) {
            EnumConverter<Enum> enumConverter = new EnumConverter<Enum>(((Class)object).asSubclass(Enum.class));
            this.registry.putIfAbsent(type, enumConverter);
            return enumConverter;
        }
        object = this.registry.entrySet().iterator();
        while (object.hasNext()) {
            Map.Entry entry = object.next();
            Type type2 = (Type)entry.getKey();
            if (!TypeUtil.isAssignable(type, type2)) continue;
            LOGGER.debug("Found compatible TypeConverter<{}> for type [{}].", (Object)type2, (Object)type);
            object = (TypeConverter)entry.getValue();
            this.registry.putIfAbsent(type, (TypeConverter<?>)object);
            return object;
        }
        throw new UnknownFormatConversionException(type.toString());
    }

    private TypeConverterRegistry() {
        LOGGER.trace("TypeConverterRegistry initializing.");
        PluginManager pluginManager = new PluginManager("TypeConverter");
        pluginManager.collectPlugins();
        this.loadKnownTypeConverters(pluginManager.getPlugins().values());
        this.registerPrimitiveTypes();
    }

    private void loadKnownTypeConverters(Collection<PluginType<?>> object) {
        object = object.iterator();
        while (object.hasNext()) {
            Class clazz = ((PluginType)object.next()).getPluginClass();
            if (!TypeConverter.class.isAssignableFrom(clazz)) continue;
            clazz = clazz.asSubclass(TypeConverter.class);
            Type type = TypeConverterRegistry.getTypeConverterSupportedType(clazz);
            if (this.registry.putIfAbsent(type, (TypeConverter<?>)((Object)(clazz = (TypeConverter)ReflectionUtil.instantiate(clazz)))) == null) continue;
            LOGGER.warn("Found a TypeConverter [{}] for type [{}] that already exists.", (Object)clazz, (Object)type);
        }
    }

    private static Type getTypeConverterSupportedType(Class<? extends TypeConverter> typeArray) {
        for (Type type : typeArray.getGenericInterfaces()) {
            if (!(type instanceof ParameterizedType) || !TypeConverter.class.equals((Object)(type = (ParameterizedType)type).getRawType())) continue;
            return type.getActualTypeArguments()[0];
        }
        return Void.TYPE;
    }

    private void registerPrimitiveTypes() {
        this.registerTypeAlias((Type)((Object)Boolean.class), Boolean.TYPE);
        this.registerTypeAlias((Type)((Object)Byte.class), Byte.TYPE);
        this.registerTypeAlias((Type)((Object)Character.class), Character.TYPE);
        this.registerTypeAlias((Type)((Object)Double.class), Double.TYPE);
        this.registerTypeAlias((Type)((Object)Float.class), Float.TYPE);
        this.registerTypeAlias((Type)((Object)Integer.class), Integer.TYPE);
        this.registerTypeAlias((Type)((Object)Long.class), Long.TYPE);
        this.registerTypeAlias((Type)((Object)Short.class), Short.TYPE);
    }

    private void registerTypeAlias(Type type, Type type2) {
        this.registry.putIfAbsent(type2, (TypeConverter<?>)this.registry.get(type));
    }

    static {
        INSTANCE_LOCK = new Object();
    }
}

