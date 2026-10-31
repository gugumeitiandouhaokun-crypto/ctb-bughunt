/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.LowLevelLogUtil;
import org.apache.logging.log4j.util.PropertiesPropertySource;
import org.apache.logging.log4j.util.PropertyFilePropertySource;
import org.apache.logging.log4j.util.PropertySource;
import org.apache.logging.log4j.util.Supplier;

public final class PropertiesUtil {
    private static final String LOG4J_PROPERTIES_FILE_NAME = "log4j2.component.properties";
    private static final String LOG4J_SYSTEM_PROPERTIES_FILE_NAME = "log4j2.system.properties";
    private static final String SYSTEM = "system:";
    private static final PropertiesUtil LOG4J_PROPERTIES = new PropertiesUtil("log4j2.component.properties");
    private final Environment environment;

    public PropertiesUtil(Properties properties) {
        this.environment = new Environment(new PropertiesPropertySource(properties));
    }

    public PropertiesUtil(String string) {
        this.environment = new Environment(new PropertyFilePropertySource(string));
    }

    static Properties loadClose(InputStream inputStream, Object object) {
        Properties properties = new Properties();
        if (inputStream != null) {
            try {
                properties.load(inputStream);
            }
            catch (IOException iOException) {
                LowLevelLogUtil.logException("Unable to read ".concat(String.valueOf(object)), iOException);
            }
            finally {
                try {
                    inputStream.close();
                }
                catch (IOException iOException) {
                    LowLevelLogUtil.logException("Unable to close ".concat(String.valueOf(object)), iOException);
                }
            }
        }
        return properties;
    }

    public static PropertiesUtil getProperties() {
        return LOG4J_PROPERTIES;
    }

    public final boolean hasProperty(String string) {
        return this.environment.containsKey(string);
    }

    public final boolean getBooleanProperty(String string) {
        return this.getBooleanProperty(string, false);
    }

    public final boolean getBooleanProperty(String string, boolean bl) {
        if ((string = this.getStringProperty(string)) == null) {
            return bl;
        }
        return "true".equalsIgnoreCase(string);
    }

    public final boolean getBooleanProperty(String string, boolean bl, boolean bl2) {
        if ((string = this.getStringProperty(string)) == null) {
            return bl;
        }
        if (string.isEmpty()) {
            return bl2;
        }
        return "true".equalsIgnoreCase(string);
    }

    public final Boolean getBooleanProperty(String[] stringArray, String string, Supplier<Boolean> supplier) {
        for (String string2 : stringArray) {
            if (!this.hasProperty(string2 + string)) continue;
            return this.getBooleanProperty(string2 + string);
        }
        if (supplier != null) {
            return supplier.get();
        }
        return null;
    }

    public final Charset getCharsetProperty(String string) {
        return this.getCharsetProperty(string, Charset.defaultCharset());
    }

    public final Charset getCharsetProperty(String string, Charset charset) {
        String string2 = this.getStringProperty(string);
        if (string2 == null) {
            return charset;
        }
        if (Charset.isSupported(string2)) {
            return Charset.forName(string2);
        }
        Object object = PropertiesUtil.getCharsetsResourceBundle();
        if (((ResourceBundle)object).containsKey(string) && Charset.isSupported((String)(object = ((ResourceBundle)object).getString(string)))) {
            return Charset.forName((String)object);
        }
        LowLevelLogUtil.log("Unable to get Charset '" + string2 + "' for property '" + string + "', using default " + charset + " and continuing.");
        return charset;
    }

    public final double getDoubleProperty(String string, double d2) {
        if ((string = this.getStringProperty(string)) != null) {
            try {
                return Double.parseDouble(string);
            }
            catch (Exception exception) {
                return d2;
            }
        }
        return d2;
    }

    public final int getIntegerProperty(String string, int n) {
        if ((string = this.getStringProperty(string)) != null) {
            try {
                return Integer.parseInt(string);
            }
            catch (Exception exception) {
                return n;
            }
        }
        return n;
    }

    public final Integer getIntegerProperty(String[] stringArray, String string, Supplier<Integer> supplier) {
        for (String string2 : stringArray) {
            if (!this.hasProperty(string2 + string)) continue;
            return this.getIntegerProperty(string2 + string, 0);
        }
        if (supplier != null) {
            return supplier.get();
        }
        return null;
    }

    public final long getLongProperty(String string, long l) {
        if ((string = this.getStringProperty(string)) != null) {
            try {
                return Long.parseLong(string);
            }
            catch (Exception exception) {
                return l;
            }
        }
        return l;
    }

    public final Long getLongProperty(String[] stringArray, String string, Supplier<Long> supplier) {
        for (String string2 : stringArray) {
            if (!this.hasProperty(string2 + string)) continue;
            return this.getLongProperty(string2 + string, 0L);
        }
        if (supplier != null) {
            return supplier.get();
        }
        return null;
    }

    public final Duration getDurationProperty(String string, Duration duration) {
        if ((string = this.getStringProperty(string)) != null) {
            return TimeUnit.getDuration(string);
        }
        return duration;
    }

    public final Duration getDurationProperty(String[] stringArray, String string, Supplier<Duration> supplier) {
        for (String string2 : stringArray) {
            if (!this.hasProperty(string2 + string)) continue;
            return this.getDurationProperty(string2 + string, null);
        }
        if (supplier != null) {
            return supplier.get();
        }
        return null;
    }

    public final String getStringProperty(String[] stringArray, String string, Supplier<String> supplier) {
        for (String string2 : stringArray) {
            if ((string2 = this.getStringProperty(string2 + string)) == null) continue;
            return string2;
        }
        if (supplier != null) {
            return supplier.get();
        }
        return null;
    }

    public final String getStringProperty(String string) {
        return this.environment.get(string);
    }

    public final String getStringProperty(String string, String string2) {
        if ((string = this.getStringProperty(string)) == null) {
            return string2;
        }
        return string;
    }

    public static Properties getSystemProperties() {
        try {
            return new Properties(System.getProperties());
        }
        catch (SecurityException securityException) {
            LowLevelLogUtil.logException("Unable to access system properties.", securityException);
            return new Properties();
        }
    }

    public final void reload() {
        this.environment.reload();
    }

    public static Properties extractSubset(Properties properties, String string) {
        Properties properties2 = new Properties();
        if (string == null || string.length() == 0) {
            return properties2;
        }
        String string2 = string;
        string = string2.charAt(string2.length() - 1) != '.' ? string + '.' : string;
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String string3 : properties.stringPropertyNames()) {
            if (!string3.startsWith(string)) continue;
            properties2.setProperty(string3.substring(string.length()), properties.getProperty(string3));
            arrayList.add(string3);
        }
        for (String string3 : arrayList) {
            properties.remove(string3);
        }
        return properties2;
    }

    static ResourceBundle getCharsetsResourceBundle() {
        return ResourceBundle.getBundle("Log4j-charsets");
    }

    public static Map<String, Properties> partitionOnCommonPrefixes(Properties properties) {
        ConcurrentHashMap<String, Properties> concurrentHashMap = new ConcurrentHashMap<String, Properties>();
        for (String string : properties.stringPropertyNames()) {
            String string2 = string.substring(0, string.indexOf(46));
            if (!concurrentHashMap.containsKey(string2)) {
                concurrentHashMap.put(string2, new Properties());
            }
            String string3 = string;
            ((Properties)concurrentHashMap.get(string2)).setProperty(string3.substring(string3.indexOf(46) + 1), properties.getProperty(string));
        }
        return concurrentHashMap;
    }

    public final boolean isOsWindows() {
        return this.getStringProperty("os.name", "").startsWith("Windows");
    }

    static enum TimeUnit {
        NANOS("ns,nano,nanos,nanosecond,nanoseconds", ChronoUnit.NANOS),
        MICROS("us,micro,micros,microsecond,microseconds", ChronoUnit.MICROS),
        MILLIS("ms,milli,millis,millsecond,milliseconds", ChronoUnit.MILLIS),
        SECONDS("s,second,seconds", ChronoUnit.SECONDS),
        MINUTES("m,minute,minutes", ChronoUnit.MINUTES),
        HOURS("h,hour,hours", ChronoUnit.HOURS),
        DAYS("d,day,days", ChronoUnit.DAYS);

        private final String[] descriptions;
        private final ChronoUnit timeUnit;

        private TimeUnit(String string2, ChronoUnit chronoUnit) {
            this.descriptions = string2.split(",");
            this.timeUnit = chronoUnit;
        }

        final ChronoUnit getTimeUnit() {
            return this.timeUnit;
        }

        static Duration getDuration(String string) {
            string = string.trim();
            ChronoUnit chronoUnit = ChronoUnit.MILLIS;
            long l = 0L;
            for (TimeUnit timeUnit : TimeUnit.values()) {
                String[] stringArray = timeUnit.descriptions;
                int n = timeUnit.descriptions.length;
                for (int i2 = 0; i2 < n; ++i2) {
                    String string2 = stringArray[i2];
                    if (!string.endsWith(string2)) continue;
                    chronoUnit = timeUnit.timeUnit;
                    l = Long.parseLong(string.substring(0, string.length() - string2.length()));
                }
            }
            return Duration.of(l, chronoUnit);
        }
    }

    static class Environment {
        private final Set<PropertySource> sources = new TreeSet<PropertySource>(new PropertySource.Comparator());
        private final Map<CharSequence, String> literal = new ConcurrentHashMap<CharSequence, String>();
        private final Map<CharSequence, String> normalized = new ConcurrentHashMap<CharSequence, String>();
        private final Map<List<CharSequence>, String> tokenized = new ConcurrentHashMap<List<CharSequence>, String>();

        private Environment(PropertySource classLoaderArray) {
            PropertyFilePropertySource propertyFilePropertySource = new PropertyFilePropertySource(PropertiesUtil.LOG4J_SYSTEM_PROPERTIES_FILE_NAME);
            try {
                propertyFilePropertySource.forEach(new BiConsumer<String, String>(){

                    @Override
                    public void accept(String string, String string2) {
                        if (System.getProperty(string) == null) {
                            System.setProperty(string, string2);
                        }
                    }
                });
            }
            catch (SecurityException securityException) {}
            this.sources.add((PropertySource)classLoaderArray);
            for (ClassLoader classLoader : LoaderUtil.getClassLoaders()) {
                try {
                    for (PropertySource propertySource : ServiceLoader.load(PropertySource.class, classLoader)) {
                        this.sources.add(propertySource);
                    }
                }
                catch (Throwable throwable) {}
            }
            this.reload();
        }

        private synchronized void reload() {
            this.literal.clear();
            this.normalized.clear();
            this.tokenized.clear();
            for (final PropertySource propertySource : this.sources) {
                propertySource.forEach(new BiConsumer<String, String>(){

                    @Override
                    public void accept(String string, String string2) {
                        if (string != null && string2 != null) {
                            literal.put(string, string2);
                            List<CharSequence> list = PropertySource.Util.tokenize(string);
                            if (list.isEmpty()) {
                                normalized.put(propertySource.getNormalForm(Collections.singleton(string)), string2);
                                return;
                            }
                            normalized.put(propertySource.getNormalForm(list), string2);
                            tokenized.put(list, string2);
                        }
                    }
                });
            }
        }

        private static boolean hasSystemProperty(String string) {
            try {
                return System.getProperties().containsKey(string);
            }
            catch (SecurityException securityException) {
                return false;
            }
        }

        private String get(String string) {
            if (this.normalized.containsKey(string)) {
                return this.normalized.get(string);
            }
            if (this.literal.containsKey(string)) {
                return this.literal.get(string);
            }
            if (Environment.hasSystemProperty(string)) {
                return System.getProperty(string);
            }
            for (PropertySource propertySource : this.sources) {
                if (!propertySource.containsProperty(string)) continue;
                return propertySource.getProperty(string);
            }
            return this.tokenized.get(PropertySource.Util.tokenize(string));
        }

        private boolean containsKey(String string) {
            return this.normalized.containsKey(string) || this.literal.containsKey(string) || Environment.hasSystemProperty(string) || this.tokenized.containsKey(PropertySource.Util.tokenize(string));
        }
    }
}

