/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.lookup;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.ConfigurationAware;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;
import org.apache.logging.log4j.core.lookup.AbstractConfigurationAwareLookup;
import org.apache.logging.log4j.core.lookup.ContextMapLookup;
import org.apache.logging.log4j.core.lookup.DateLookup;
import org.apache.logging.log4j.core.lookup.EnvironmentLookup;
import org.apache.logging.log4j.core.lookup.JavaLookup;
import org.apache.logging.log4j.core.lookup.Log4jLookup;
import org.apache.logging.log4j.core.lookup.LowerLookup;
import org.apache.logging.log4j.core.lookup.MainMapLookup;
import org.apache.logging.log4j.core.lookup.MapLookup;
import org.apache.logging.log4j.core.lookup.MarkerLookup;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.core.lookup.SystemPropertiesLookup;
import org.apache.logging.log4j.core.lookup.UpperLookup;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.core.util.ReflectionUtil;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Constants;

public class Interpolator
extends AbstractConfigurationAwareLookup {
    public static final char PREFIX_SEPARATOR = ':';
    private static final String LOOKUP_KEY_WEB = "web";
    private static final String LOOKUP_KEY_DOCKER = "docker";
    private static final String LOOKUP_KEY_KUBERNETES = "kubernetes";
    private static final String LOOKUP_KEY_SPRING = "spring";
    private static final String LOOKUP_KEY_JNDI = "jndi";
    private static final String LOOKUP_KEY_JVMRUNARGS = "jvmrunargs";
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final Map<String, StrLookup> strLookupMap = new HashMap<String, StrLookup>();
    private final StrLookup defaultLookup;

    public Interpolator(StrLookup strLookup) {
        this(strLookup, null);
    }

    public Interpolator(StrLookup iterator, List<String> object2) {
        this.defaultLookup = iterator == null ? new MapLookup(new HashMap<String, String>()) : iterator;
        iterator = new PluginManager("Lookup");
        ((PluginManager)((Object)iterator)).collectPlugins((List<String>)object2);
        for (Map.Entry<String, PluginType<?>> entry : ((PluginManager)((Object)iterator)).getPlugins().entrySet()) {
            try {
                Class<StrLookup> clazz = entry.getValue().getPluginClass().asSubclass(StrLookup.class);
                this.strLookupMap.put(entry.getKey().toLowerCase(), ReflectionUtil.instantiate(clazz));
            }
            catch (Throwable throwable) {
                this.handleError(entry.getKey(), throwable);
            }
        }
    }

    public Interpolator() {
        this((Map<String, String>)null);
    }

    public Interpolator(Map<String, String> hashMap) {
        this.defaultLookup = new MapLookup(hashMap == null ? new HashMap() : hashMap);
        this.strLookupMap.put("log4j", new Log4jLookup());
        this.strLookupMap.put("sys", new SystemPropertiesLookup());
        this.strLookupMap.put("env", new EnvironmentLookup());
        this.strLookupMap.put("main", MainMapLookup.MAIN_SINGLETON);
        this.strLookupMap.put("marker", new MarkerLookup());
        this.strLookupMap.put("java", new JavaLookup());
        this.strLookupMap.put("lower", new LowerLookup());
        this.strLookupMap.put("upper", new UpperLookup());
        try {
            this.strLookupMap.put(LOOKUP_KEY_JNDI, Loader.newCheckedInstanceOf("org.apache.logging.log4j.core.lookup.JndiLookup", StrLookup.class));
        }
        catch (Exception | LinkageError throwable) {
            this.handleError(LOOKUP_KEY_JNDI, throwable);
        }
        try {
            this.strLookupMap.put(LOOKUP_KEY_JVMRUNARGS, Loader.newCheckedInstanceOf("org.apache.logging.log4j.core.lookup.JmxRuntimeInputArgumentsLookup", StrLookup.class));
        }
        catch (Exception | LinkageError throwable) {
            this.handleError(LOOKUP_KEY_JVMRUNARGS, throwable);
        }
        this.strLookupMap.put("date", new DateLookup());
        this.strLookupMap.put("ctx", new ContextMapLookup());
        if (Constants.IS_WEB_APP) {
            try {
                this.strLookupMap.put(LOOKUP_KEY_WEB, Loader.newCheckedInstanceOf("org.apache.logging.log4j.web.WebLookup", StrLookup.class));
            }
            catch (Exception exception) {
                this.handleError(LOOKUP_KEY_WEB, exception);
            }
        } else {
            LOGGER.debug("Not in a ServletContext environment, thus not loading WebLookup plugin.");
        }
        try {
            this.strLookupMap.put(LOOKUP_KEY_DOCKER, Loader.newCheckedInstanceOf("org.apache.logging.log4j.docker.DockerLookup", StrLookup.class));
        }
        catch (Exception exception) {
            this.handleError(LOOKUP_KEY_DOCKER, exception);
        }
        try {
            this.strLookupMap.put(LOOKUP_KEY_SPRING, Loader.newCheckedInstanceOf("org.apache.logging.log4j.spring.cloud.config.client.SpringLookup", StrLookup.class));
        }
        catch (Exception exception) {
            this.handleError(LOOKUP_KEY_SPRING, exception);
        }
        try {
            this.strLookupMap.put(LOOKUP_KEY_KUBERNETES, Loader.newCheckedInstanceOf("org.apache.logging.log4j.kubernetes.KubernetesLookup", StrLookup.class));
            return;
        }
        catch (Exception exception) {
            this.handleError(LOOKUP_KEY_KUBERNETES, exception);
            return;
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            this.handleError(LOOKUP_KEY_KUBERNETES, noClassDefFoundError);
            return;
        }
    }

    public Map<String, StrLookup> getStrLookupMap() {
        return this.strLookupMap;
    }

    private void handleError(String string, Throwable throwable) {
        switch (string) {
            case "jndi": {
                LOGGER.warn("JNDI lookup class is not available because this JRE does not support JNDI. JNDI string lookups will not be available, continuing configuration. Ignoring ".concat(String.valueOf(throwable)));
                return;
            }
            case "jvmrunargs": {
                LOGGER.warn("JMX runtime input lookup class is not available because this JRE does not support JMX. JMX lookups will not be available, continuing configuration. Ignoring ".concat(String.valueOf(throwable)));
                return;
            }
            case "web": {
                LOGGER.info("Log4j appears to be running in a Servlet environment, but there's no log4j-web module available. If you want better web container support, please add the log4j-web JAR to your web archive or server lib directory.");
                return;
            }
            case "docker": 
            case "spring": {
                return;
            }
            case "kubernetes": {
                if (!(throwable instanceof NoClassDefFoundError)) break;
                LOGGER.warn("Unable to create Kubernetes lookup due to missing dependency: {}", (Object)throwable.getMessage());
                return;
            }
            default: {
                LOGGER.error("Unable to create Lookup for {}", (Object)string, (Object)throwable);
            }
        }
    }

    @Override
    public String lookup(LogEvent logEvent, String string) {
        if (string == null) {
            return null;
        }
        int n = string.indexOf(58);
        if (n >= 0) {
            Object object = string.substring(0, n).toLowerCase(Locale.US);
            String string2 = string.substring(n + 1);
            if ((object = this.strLookupMap.get(object)) instanceof ConfigurationAware) {
                ((ConfigurationAware)object).setConfiguration(this.configuration);
            }
            String string3 = null;
            if (object != null) {
                String string4 = string3 = logEvent == null ? object.lookup(string2) : object.lookup(logEvent, string2);
            }
            if (string3 != null) {
                return string3;
            }
            string = string.substring(n + 1);
        }
        if (this.defaultLookup != null) {
            if (logEvent == null) {
                return this.defaultLookup.lookup(string);
            }
            return this.defaultLookup.lookup(logEvent, string);
        }
        return null;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : this.strLookupMap.keySet()) {
            if (stringBuilder.length() == 0) {
                stringBuilder.append('{');
            } else {
                stringBuilder.append(", ");
            }
            stringBuilder.append(string);
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.append('}');
        }
        return stringBuilder.toString();
    }
}

