/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.OrderComparator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.composite.CompositeConfiguration;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.lookup.Interpolator;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.net.UrlConnectionFactory;
import org.apache.logging.log4j.core.util.AuthorizationProvider;
import org.apache.logging.log4j.core.util.BasicAuthorizationProvider;
import org.apache.logging.log4j.core.util.FileUtils;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.core.util.NetUtils;
import org.apache.logging.log4j.core.util.ReflectionUtil;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.Strings;

public abstract class ConfigurationFactory
extends ConfigurationBuilderFactory {
    public static final String CONFIGURATION_FACTORY_PROPERTY = "log4j.configurationFactory";
    public static final String CONFIGURATION_FILE_PROPERTY = "log4j.configurationFile";
    public static final String LOG4J1_CONFIGURATION_FILE_PROPERTY = "log4j.configuration";
    public static final String LOG4J1_EXPERIMENTAL = "log4j1.compatibility";
    public static final String AUTHORIZATION_PROVIDER = "log4j2.authorizationProvider";
    public static final String CATEGORY = "ConfigurationFactory";
    protected static final Logger LOGGER = StatusLogger.getLogger();
    protected static final String TEST_PREFIX = "log4j2-test";
    protected static final String DEFAULT_PREFIX = "log4j2";
    protected static final String LOG4J1_VERSION = "1";
    protected static final String LOG4J2_VERSION = "2";
    private static final String CLASS_LOADER_SCHEME = "classloader";
    private static final String CLASS_PATH_SCHEME = "classpath";
    private static final String OVERRIDE_PARAM = "override";
    private static volatile List<ConfigurationFactory> factories = null;
    private static ConfigurationFactory configFactory = new Factory();
    protected final StrSubstitutor substitutor = new StrSubstitutor(new Interpolator());
    private static final Lock LOCK = new ReentrantLock();
    private static final String HTTPS = "https";
    private static final String HTTP = "http";
    private static volatile AuthorizationProvider authorizationProvider = null;

    public static ConfigurationFactory getInstance() {
        if (factories == null) {
            LOCK.lock();
            try {
                if (factories == null) {
                    ArrayList<ConfigurationFactory> arrayList = new ArrayList<ConfigurationFactory>();
                    PropertiesUtil propertiesUtil = PropertiesUtil.getProperties();
                    Object object = propertiesUtil.getStringProperty(CONFIGURATION_FACTORY_PROPERTY);
                    if (object != null) {
                        ConfigurationFactory.addFactory(arrayList, (String)object);
                    }
                    object = new PluginManager(CATEGORY);
                    ((PluginManager)object).collectPlugins();
                    object = ((PluginManager)object).getPlugins();
                    ArrayList<Class<ConfigurationFactory>> arrayList2 = new ArrayList<Class<ConfigurationFactory>>(object.size());
                    for (PluginType object2 : object.values()) {
                        try {
                            arrayList2.add(object2.getPluginClass().asSubclass(ConfigurationFactory.class));
                        }
                        catch (Exception exception) {
                            LOGGER.warn("Unable to add class {}", (Object)object2.getPluginClass(), (Object)exception);
                        }
                    }
                    Collections.sort(arrayList2, OrderComparator.getInstance());
                    for (Class clazz : arrayList2) {
                        ConfigurationFactory.addFactory(arrayList, clazz);
                    }
                    factories = Collections.unmodifiableList(arrayList);
                    authorizationProvider = ConfigurationFactory.authorizationProvider(propertiesUtil);
                }
            }
            finally {
                LOCK.unlock();
            }
        }
        LOGGER.debug("Using configurationFactory {}", (Object)configFactory);
        return configFactory;
    }

    public static AuthorizationProvider authorizationProvider(PropertiesUtil propertiesUtil) {
        String string = propertiesUtil.getStringProperty(AUTHORIZATION_PROVIDER);
        AuthorizationProvider authorizationProvider = null;
        if (string != null) {
            try {
                Object t = LoaderUtil.newInstanceOf(string);
                if (t instanceof AuthorizationProvider) {
                    authorizationProvider = (AuthorizationProvider)t;
                } else {
                    LOGGER.warn("{} is not an AuthorizationProvider, using default", (Object)t.getClass().getName());
                }
            }
            catch (Exception exception) {
                LOGGER.warn("Unable to create {}, using default: {}", (Object)string, (Object)exception.getMessage());
            }
        }
        if (authorizationProvider == null) {
            authorizationProvider = new BasicAuthorizationProvider(propertiesUtil);
        }
        return authorizationProvider;
    }

    public static AuthorizationProvider getAuthorizationProvider() {
        return authorizationProvider;
    }

    private static void addFactory(Collection<ConfigurationFactory> collection, String string) {
        try {
            ConfigurationFactory.addFactory(collection, Loader.loadClass(string).asSubclass(ConfigurationFactory.class));
            return;
        }
        catch (Exception exception) {
            LOGGER.error("Unable to load class {}", (Object)string, (Object)exception);
            return;
        }
    }

    private static void addFactory(Collection<ConfigurationFactory> collection, Class<? extends ConfigurationFactory> clazz) {
        try {
            collection.add(ReflectionUtil.instantiate(clazz));
            return;
        }
        catch (Exception exception) {
            LOGGER.error("Unable to create instance of {}", (Object)clazz.getName(), (Object)exception);
            return;
        }
    }

    public static void setConfigurationFactory(ConfigurationFactory configurationFactory) {
        configFactory = configurationFactory;
    }

    public static void resetConfigurationFactory() {
        configFactory = new Factory();
    }

    public static void removeConfigurationFactory(ConfigurationFactory configurationFactory) {
        if (configFactory == configurationFactory) {
            configFactory = new Factory();
        }
    }

    protected abstract String[] getSupportedTypes();

    protected String getTestPrefix() {
        return TEST_PREFIX;
    }

    protected String getDefaultPrefix() {
        return DEFAULT_PREFIX;
    }

    protected String getVersion() {
        return LOG4J2_VERSION;
    }

    protected boolean isActive() {
        return true;
    }

    public abstract Configuration getConfiguration(LoggerContext var1, ConfigurationSource var2);

    public Configuration getConfiguration(LoggerContext loggerContext, String object, URI uRI) {
        if (!this.isActive()) {
            return null;
        }
        if (uRI != null && (object = ConfigurationSource.fromUri(uRI)) != null) {
            return this.getConfiguration(loggerContext, (ConfigurationSource)object);
        }
        return null;
    }

    public Configuration getConfiguration(LoggerContext loggerContext, String string, URI uRI, ClassLoader object) {
        if (!this.isActive()) {
            return null;
        }
        if (object == null) {
            return this.getConfiguration(loggerContext, string, uRI);
        }
        if (ConfigurationFactory.isClassLoaderUri(uRI) && (object = ConfigurationSource.fromResource(ConfigurationFactory.extractClassLoaderUriPath(uRI), (ClassLoader)object)) != null && (object = this.getConfiguration(loggerContext, (ConfigurationSource)object)) != null) {
            return object;
        }
        return this.getConfiguration(loggerContext, string, uRI);
    }

    static boolean isClassLoaderUri(URI object) {
        if (object == null) {
            return false;
        }
        return (object = ((URI)object).getScheme()) == null || ((String)object).equals(CLASS_LOADER_SCHEME) || ((String)object).equals(CLASS_PATH_SCHEME);
    }

    static String extractClassLoaderUriPath(URI uRI) {
        if (uRI.getScheme() == null) {
            return uRI.getPath();
        }
        return uRI.getSchemeSpecificPart();
    }

    protected ConfigurationSource getInputFromString(String object, ClassLoader classLoader) {
        try {
            URL uRL = new URL((String)object);
            URLConnection uRLConnection = UrlConnectionFactory.createConnection(uRL);
            if (FileUtils.fileFromUri(uRL.toURI()) != null) {
                return new ConfigurationSource(uRLConnection.getInputStream(), FileUtils.fileFromUri(uRL.toURI()));
            }
            return new ConfigurationSource(uRLConnection.getInputStream(), uRL, uRLConnection.getLastModified());
        }
        catch (Exception exception) {
            ConfigurationSource configurationSource = ConfigurationSource.fromResource((String)object, classLoader);
            if (configurationSource == null) {
                try {
                    object = new File((String)object);
                    return new ConfigurationSource((InputStream)new FileInputStream((File)object), (File)object);
                }
                catch (FileNotFoundException fileNotFoundException) {
                    LOGGER.catching(Level.DEBUG, fileNotFoundException);
                }
            }
            return configurationSource;
        }
    }

    static List<ConfigurationFactory> getFactories() {
        return factories;
    }

    static class Factory
    extends ConfigurationFactory {
        private static final String ALL_TYPES = "*";

        private Factory() {
        }

        /*
         * WARNING - void declaration
         */
        @Override
        public Configuration getConfiguration(LoggerContext loggerContext, String string, URI uRI) {
            void var4_15;
            Configuration configuration;
            Configuration configuration2;
            Configuration configuration3;
            if (uRI == null) {
                String object2 = this.substitutor.replace(PropertiesUtil.getProperties().getStringProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY));
                if (object2 != null) {
                    String[] stringArray = this.parseConfigLocations(object2);
                    if (stringArray.length > 1) {
                        ArrayList<AbstractConfiguration> arrayList = new ArrayList<AbstractConfiguration>();
                        String[] stringArray2 = stringArray;
                        int n = stringArray.length;
                        for (int j = 0; j < n; ++j) {
                            String string2 = stringArray2[j];
                            Configuration configuration4 = this.getConfiguration(loggerContext, string2.trim());
                            if (configuration4 == null || !(configuration4 instanceof AbstractConfiguration)) {
                                LOGGER.error("Failed to created configuration at {}", (Object)string2);
                                return null;
                            }
                            arrayList.add((AbstractConfiguration)configuration4);
                        }
                        return new CompositeConfiguration(arrayList);
                    }
                    return this.getConfiguration(loggerContext, object2);
                }
                String string3 = this.substitutor.replace(PropertiesUtil.getProperties().getStringProperty(ConfigurationFactory.LOG4J1_CONFIGURATION_FILE_PROPERTY));
                if (string3 != null) {
                    System.setProperty(ConfigurationFactory.LOG4J1_EXPERIMENTAL, "true");
                    return this.getConfiguration(ConfigurationFactory.LOG4J1_VERSION, loggerContext, string3);
                }
                for (ConfigurationFactory configurationFactory : Factory.getFactories()) {
                    String[] stringArray = configurationFactory.getSupportedTypes();
                    if (stringArray == null) continue;
                    String[] stringArray3 = stringArray;
                    int n = stringArray.length;
                    for (int j = 0; j < n; ++j) {
                        Configuration configuration5;
                        if (!stringArray3[j].equals(ALL_TYPES) || (configuration5 = configurationFactory.getConfiguration(loggerContext, string, uRI)) == null) continue;
                        return configuration5;
                    }
                }
            } else {
                String[] stringArray = this.parseConfigLocations(uRI);
                if (stringArray.length > 1) {
                    ArrayList<AbstractConfiguration> arrayList = new ArrayList<AbstractConfiguration>();
                    for (String string4 : stringArray) {
                        Configuration configuration6 = this.getConfiguration(loggerContext, string4.trim());
                        if (!(configuration6 instanceof AbstractConfiguration)) {
                            LOGGER.error("Failed to created configuration at {}", (Object)string4);
                            return null;
                        }
                        arrayList.add((AbstractConfiguration)configuration6);
                    }
                    return new CompositeConfiguration(arrayList);
                }
                String string5 = uRI.toString();
                for (ConfigurationFactory configurationFactory : Factory.getFactories()) {
                    Object object = configurationFactory.getSupportedTypes();
                    if (object == null) continue;
                    String[] stringArray4 = object;
                    int n = ((String[])object).length;
                    for (int j = 0; j < n; ++j) {
                        String string6 = stringArray4[j];
                        if (!string6.equals(ALL_TYPES) && !string5.endsWith(string6) || (object = configurationFactory.getConfiguration(loggerContext, string, uRI)) == null) continue;
                        return object;
                    }
                }
            }
            if ((configuration3 = this.getConfiguration(loggerContext, true, string)) == null && (configuration2 = this.getConfiguration(loggerContext, true, null)) == null && (configuration = this.getConfiguration(loggerContext, false, string)) == null) {
                Configuration configuration7 = this.getConfiguration(loggerContext, false, null);
            }
            if (var4_15 != null) {
                return var4_15;
            }
            LOGGER.warn("No Log4j 2 configuration file found. Using default configuration (logging only errors to the console), or user programmatically provided configurations. Set system property 'log4j2.debug' to show Log4j 2 internal initialization logging. See https://logging.apache.org/log4j/2.x/manual/configuration.html for instructions on how to configure Log4j 2");
            return new DefaultConfiguration();
        }

        private Configuration getConfiguration(LoggerContext loggerContext, String string) {
            return this.getConfiguration(null, loggerContext, string);
        }

        private Configuration getConfiguration(String string, LoggerContext loggerContext, String string2) {
            ConfigurationSource configurationSource = null;
            try {
                configurationSource = ConfigurationSource.fromUri(NetUtils.toURI(string2));
            }
            catch (Exception exception) {
                LOGGER.catching(Level.DEBUG, exception);
            }
            if (configurationSource == null) {
                ClassLoader classLoader = LoaderUtil.getThreadContextClassLoader();
                configurationSource = this.getInputFromString(string2, classLoader);
            }
            if (configurationSource != null) {
                for (ConfigurationFactory configurationFactory : Factory.getFactories()) {
                    String[] stringArray;
                    if (string != null && !configurationFactory.getVersion().equals(string) || (stringArray = configurationFactory.getSupportedTypes()) == null) continue;
                    int n = stringArray.length;
                    for (int j = 0; j < n; ++j) {
                        Object object = stringArray[j];
                        if (!((String)object).equals(ALL_TYPES) && !string2.endsWith((String)object) || (object = configurationFactory.getConfiguration(loggerContext, configurationSource)) == null) continue;
                        return object;
                    }
                }
            }
            return null;
        }

        private Configuration getConfiguration(LoggerContext loggerContext, boolean bl, String string) {
            boolean bl2 = Strings.isNotEmpty(string);
            ClassLoader classLoader = LoaderUtil.getThreadContextClassLoader();
            for (ConfigurationFactory configurationFactory : Factory.getFactories()) {
                String string2;
                String string3 = string2 = bl ? configurationFactory.getTestPrefix() : configurationFactory.getDefaultPrefix();
                Object object = configurationFactory.getSupportedTypes();
                if (object == null) continue;
                String[] stringArray = object;
                int n = ((String[])object).length;
                for (int j = 0; j < n; ++j) {
                    ConfigurationSource configurationSource;
                    object = stringArray[j];
                    if (object.equals(ALL_TYPES) || (configurationSource = ConfigurationSource.fromResource((String)(object = bl2 ? string2 + string + (String)object : string2 + (String)object), classLoader)) == null) continue;
                    if (!configurationFactory.isActive()) {
                        LOGGER.warn("Found configuration file {} for inactive ConfigurationFactory {}", object, (Object)configurationFactory.getClass().getName());
                    }
                    return configurationFactory.getConfiguration(loggerContext, configurationSource);
                }
            }
            return null;
        }

        @Override
        public String[] getSupportedTypes() {
            return null;
        }

        @Override
        public Configuration getConfiguration(LoggerContext lifeCycle, ConfigurationSource configurationSource) {
            if (configurationSource != null) {
                String string = configurationSource.getLocation();
                for (ConfigurationFactory configurationFactory : Factory.getFactories()) {
                    String[] stringArray = configurationFactory.getSupportedTypes();
                    if (stringArray == null) continue;
                    for (String string2 : stringArray) {
                        if (!string2.equals(ALL_TYPES) && (string == null || !string.endsWith(string2))) continue;
                        if ((lifeCycle = configurationFactory.getConfiguration((LoggerContext)lifeCycle, configurationSource)) != null) {
                            LOGGER.debug("Loaded configuration from {}", (Object)configurationSource);
                            return lifeCycle;
                        }
                        LOGGER.error("Cannot determine the ConfigurationFactory to use for {}", (Object)string);
                        return null;
                    }
                }
            }
            LOGGER.error("Cannot process configuration, input source is null");
            return null;
        }

        private String[] parseConfigLocations(URI uRI) {
            String[] stringArray = uRI.toString().split("\\?");
            ArrayList<String> arrayList = new ArrayList<String>();
            if (stringArray.length > 1) {
                arrayList.add(stringArray[0]);
                for (String string : uRI.getQuery().split("&")) {
                    int n = string.indexOf("=");
                    try {
                        if (!(n > 0 ? URLDecoder.decode(string.substring(0, n), "UTF-8") : string).equalsIgnoreCase(ConfigurationFactory.OVERRIDE_PARAM)) continue;
                        arrayList.add(URLDecoder.decode(string.substring(n + 1), "UTF-8"));
                    }
                    catch (UnsupportedEncodingException unsupportedEncodingException) {
                        LOGGER.warn("Invalid query parameter in {}", (Object)uRI);
                    }
                }
                return arrayList.toArray(new String[0]);
            }
            return new String[]{stringArray[0]};
        }

        private String[] parseConfigLocations(String string) {
            String[] stringArray = string.split(",");
            if (stringArray.length > 1) {
                return stringArray;
            }
            try {
                return this.parseConfigLocations(new URI(string));
            }
            catch (URISyntaxException uRISyntaxException) {
                LOGGER.warn("Error parsing URI {}", (Object)string);
                return new String[]{string};
            }
        }
    }
}

