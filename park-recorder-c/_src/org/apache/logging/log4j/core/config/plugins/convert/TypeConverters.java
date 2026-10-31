/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.convert;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Provider;
import java.security.Security;
import java.util.UUID;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.rolling.action.Duration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.convert.Base64Converter;
import org.apache.logging.log4j.core.config.plugins.convert.HexConverter;
import org.apache.logging.log4j.core.config.plugins.convert.TypeConverter;
import org.apache.logging.log4j.core.config.plugins.convert.TypeConverterRegistry;
import org.apache.logging.log4j.core.util.CronExpression;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;

public final class TypeConverters {
    public static final String CATEGORY = "TypeConverter";
    private static final Logger LOGGER = StatusLogger.getLogger();

    public static <T> T convert(String string, Class<? extends T> clazz, Object object) {
        TypeConverter<?> typeConverter = TypeConverterRegistry.getInstance().findCompatibleConverter(clazz);
        if (string == null) {
            return (T)TypeConverters.parseDefaultValue(typeConverter, object);
        }
        try {
            return (T)typeConverter.convert(string);
        }
        catch (Exception exception) {
            LOGGER.warn("Error while converting string [{}] to type [{}]. Using default value [{}].", (Object)string, (Object)clazz, object, (Object)exception);
            return (T)TypeConverters.parseDefaultValue(typeConverter, object);
        }
    }

    private static <T> T parseDefaultValue(TypeConverter<T> typeConverter, Object object) {
        if (object == null) {
            return null;
        }
        if (!(object instanceof String)) {
            return (T)object;
        }
        try {
            return typeConverter.convert((String)object);
        }
        catch (Exception exception) {
            LOGGER.debug("Can't parse default value [{}] for type [{}].", object, (Object)typeConverter.getClass(), (Object)exception);
            return null;
        }
    }

    @Plugin(name="UUID", category="TypeConverter")
    public static class UuidConverter
    implements TypeConverter<UUID> {
        @Override
        public UUID convert(String string) {
            return UUID.fromString(string);
        }
    }

    @Plugin(name="URL", category="TypeConverter")
    public static class UrlConverter
    implements TypeConverter<URL> {
        @Override
        public URL convert(String string) {
            return new URL(string);
        }
    }

    @Plugin(name="URI", category="TypeConverter")
    public static class UriConverter
    implements TypeConverter<URI> {
        @Override
        public URI convert(String string) {
            return new URI(string);
        }
    }

    @Plugin(name="String", category="TypeConverter")
    public static class StringConverter
    implements TypeConverter<String> {
        @Override
        public String convert(String string) {
            return string;
        }
    }

    @Plugin(name="Short", category="TypeConverter")
    public static class ShortConverter
    implements TypeConverter<Short> {
        @Override
        public Short convert(String string) {
            return Short.valueOf(string);
        }
    }

    @Plugin(name="SecurityProvider", category="TypeConverter")
    public static class SecurityProviderConverter
    implements TypeConverter<Provider> {
        @Override
        public Provider convert(String string) {
            return Security.getProvider(string);
        }
    }

    @Plugin(name="Pattern", category="TypeConverter")
    public static class PatternConverter
    implements TypeConverter<Pattern> {
        @Override
        public Pattern convert(String string) {
            return Pattern.compile(string);
        }
    }

    @Plugin(name="Path", category="TypeConverter")
    public static class PathConverter
    implements TypeConverter<Path> {
        @Override
        public Path convert(String string) {
            return Paths.get(string, new String[0]);
        }
    }

    @Plugin(name="Long", category="TypeConverter")
    public static class LongConverter
    implements TypeConverter<Long> {
        @Override
        public Long convert(String string) {
            return Long.valueOf(string);
        }
    }

    @Plugin(name="Level", category="TypeConverter")
    public static class LevelConverter
    implements TypeConverter<Level> {
        @Override
        public Level convert(String string) {
            return Level.valueOf(string);
        }
    }

    @Plugin(name="Integer", category="TypeConverter")
    public static class IntegerConverter
    implements TypeConverter<Integer> {
        @Override
        public Integer convert(String string) {
            return Integer.valueOf(string);
        }
    }

    @Plugin(name="InetAddress", category="TypeConverter")
    public static class InetAddressConverter
    implements TypeConverter<InetAddress> {
        @Override
        public InetAddress convert(String string) {
            return InetAddress.getByName(string);
        }
    }

    @Plugin(name="Float", category="TypeConverter")
    public static class FloatConverter
    implements TypeConverter<Float> {
        @Override
        public Float convert(String string) {
            return Float.valueOf(string);
        }
    }

    @Plugin(name="File", category="TypeConverter")
    public static class FileConverter
    implements TypeConverter<File> {
        @Override
        public File convert(String string) {
            return new File(string);
        }
    }

    @Plugin(name="Duration", category="TypeConverter")
    public static class DurationConverter
    implements TypeConverter<Duration> {
        @Override
        public Duration convert(String string) {
            return Duration.parse(string);
        }
    }

    @Plugin(name="Double", category="TypeConverter")
    public static class DoubleConverter
    implements TypeConverter<Double> {
        @Override
        public Double convert(String string) {
            return Double.valueOf(string);
        }
    }

    @Plugin(name="CronExpression", category="TypeConverter")
    public static class CronExpressionConverter
    implements TypeConverter<CronExpression> {
        @Override
        public CronExpression convert(String string) {
            return new CronExpression(string);
        }
    }

    @Plugin(name="Class", category="TypeConverter")
    public static class ClassConverter
    implements TypeConverter<Class<?>> {
        @Override
        public Class<?> convert(String string) {
            switch (string.toLowerCase()) {
                case "boolean": {
                    return Boolean.TYPE;
                }
                case "byte": {
                    return Byte.TYPE;
                }
                case "char": {
                    return Character.TYPE;
                }
                case "double": {
                    return Double.TYPE;
                }
                case "float": {
                    return Float.TYPE;
                }
                case "int": {
                    return Integer.TYPE;
                }
                case "long": {
                    return Long.TYPE;
                }
                case "short": {
                    return Short.TYPE;
                }
                case "void": {
                    return Void.TYPE;
                }
            }
            return LoaderUtil.loadClass(string);
        }
    }

    @Plugin(name="Charset", category="TypeConverter")
    public static class CharsetConverter
    implements TypeConverter<Charset> {
        @Override
        public Charset convert(String string) {
            return Charset.forName(string);
        }
    }

    @Plugin(name="CharacterArray", category="TypeConverter")
    public static class CharArrayConverter
    implements TypeConverter<char[]> {
        @Override
        public char[] convert(String string) {
            return string.toCharArray();
        }
    }

    @Plugin(name="Character", category="TypeConverter")
    public static class CharacterConverter
    implements TypeConverter<Character> {
        @Override
        public Character convert(String string) {
            if (string.length() != 1) {
                throw new IllegalArgumentException("Character string must be of length 1: ".concat(String.valueOf(string)));
            }
            return Character.valueOf(string.toCharArray()[0]);
        }
    }

    @Plugin(name="Byte", category="TypeConverter")
    public static class ByteConverter
    implements TypeConverter<Byte> {
        @Override
        public Byte convert(String string) {
            return Byte.valueOf(string);
        }
    }

    @Plugin(name="ByteArray", category="TypeConverter")
    public static class ByteArrayConverter
    implements TypeConverter<byte[]> {
        private static final String PREFIX_0x = "0x";
        private static final String PREFIX_BASE64 = "Base64:";

        @Override
        public byte[] convert(String object) {
            object = object == null || ((String)object).isEmpty() ? (Object)new byte[0] : (((String)object).startsWith(PREFIX_BASE64) ? (Object)Base64Converter.parseBase64Binary(((String)object).substring(7)) : (((String)object).startsWith(PREFIX_0x) ? (Object)HexConverter.parseHexBinary(((String)object).substring(2)) : (Object)((String)object).getBytes(Charset.defaultCharset())));
            return object;
        }
    }

    @Plugin(name="Boolean", category="TypeConverter")
    public static class BooleanConverter
    implements TypeConverter<Boolean> {
        @Override
        public Boolean convert(String string) {
            return Boolean.valueOf(string);
        }
    }

    @Plugin(name="BigInteger", category="TypeConverter")
    public static class BigIntegerConverter
    implements TypeConverter<BigInteger> {
        @Override
        public BigInteger convert(String string) {
            return new BigInteger(string);
        }
    }

    @Plugin(name="BigDecimal", category="TypeConverter")
    public static class BigDecimalConverter
    implements TypeConverter<BigDecimal> {
        @Override
        public BigDecimal convert(String string) {
            return new BigDecimal(string);
        }
    }
}

