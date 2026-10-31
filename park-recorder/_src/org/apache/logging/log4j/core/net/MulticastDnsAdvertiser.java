/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.net.Advertiser;
import org.apache.logging.log4j.core.util.Integers;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;

@Plugin(name="multicastdns", category="Core", elementType="advertiser", printObject=false)
public class MulticastDnsAdvertiser
implements Advertiser {
    protected static final Logger LOGGER = StatusLogger.getLogger();
    private static final int MAX_LENGTH = 255;
    private static final int DEFAULT_PORT = 4555;
    private static Object jmDNS = MulticastDnsAdvertiser.initializeJmDns();
    private static Class<?> jmDNSClass;
    private static Class<?> serviceInfoClass;

    @Override
    public Object advertise(Map<String, String> object) {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        for (Map.Entry object22 : object.entrySet()) {
            if (((String)object22.getKey()).length() > 255 || ((String)object22.getValue()).length() > 255) continue;
            hashMap.put((String)object22.getKey(), (String)object22.getValue());
        }
        object = (String)hashMap.get("protocol");
        String string = "._log4j._" + (String)(object != null ? object : "tcp") + ".local.";
        int n = Integers.parseInt((String)hashMap.get("port"), 4555);
        String string2 = (String)hashMap.get("name");
        if (jmDNS != null) {
            boolean bl = false;
            try {
                jmDNSClass.getMethod("create", new Class[0]);
                bl = true;
            }
            catch (NoSuchMethodException noSuchMethodException) {}
            Object object3 = bl ? MulticastDnsAdvertiser.buildServiceInfoVersion3(string, n, string2, hashMap) : MulticastDnsAdvertiser.buildServiceInfoVersion1(string, n, string2, hashMap);
            try {
                jmDNSClass.getMethod("registerService", serviceInfoClass).invoke(jmDNS, object3);
            }
            catch (IllegalAccessException | InvocationTargetException reflectiveOperationException) {
                LOGGER.warn("Unable to invoke registerService method", (Throwable)reflectiveOperationException);
            }
            catch (NoSuchMethodException noSuchMethodException) {
                LOGGER.warn("No registerService method", (Throwable)noSuchMethodException);
            }
            return object3;
        }
        LOGGER.warn("JMDNS not available - will not advertise ZeroConf support");
        return null;
    }

    @Override
    public void unadvertise(Object object) {
        if (jmDNS != null) {
            try {
                jmDNSClass.getMethod("unregisterService", serviceInfoClass).invoke(jmDNS, object);
                return;
            }
            catch (IllegalAccessException | InvocationTargetException reflectiveOperationException) {
                LOGGER.warn("Unable to invoke unregisterService method", (Throwable)reflectiveOperationException);
                return;
            }
            catch (NoSuchMethodException noSuchMethodException) {
                LOGGER.warn("No unregisterService method", (Throwable)noSuchMethodException);
            }
        }
    }

    private static Object createJmDnsVersion1() {
        try {
            return jmDNSClass.getConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException reflectiveOperationException) {
            LOGGER.warn("Unable to instantiate JMDNS", (Throwable)reflectiveOperationException);
            return null;
        }
    }

    private static Object createJmDnsVersion3() {
        try {
            return jmDNSClass.getMethod("create", new Class[0]).invoke(null, null);
        }
        catch (IllegalAccessException | InvocationTargetException reflectiveOperationException) {
            LOGGER.warn("Unable to invoke create method", (Throwable)reflectiveOperationException);
        }
        catch (NoSuchMethodException noSuchMethodException) {
            LOGGER.warn("Unable to get create method", (Throwable)noSuchMethodException);
        }
        return null;
    }

    private static Object buildServiceInfoVersion1(String string, int n, String string2, Map<String, String> map) {
        map = new Hashtable<String, String>(map);
        try {
            return serviceInfoClass.getConstructor(String.class, String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Hashtable.class).newInstance(string, string2, n, 0, 0, map);
        }
        catch (IllegalAccessException | InstantiationException | InvocationTargetException reflectiveOperationException) {
            LOGGER.warn("Unable to construct ServiceInfo instance", (Throwable)reflectiveOperationException);
        }
        catch (NoSuchMethodException noSuchMethodException) {
            LOGGER.warn("Unable to get ServiceInfo constructor", (Throwable)noSuchMethodException);
        }
        return null;
    }

    private static Object buildServiceInfoVersion3(String string, int n, String string2, Map<String, String> map) {
        try {
            return serviceInfoClass.getMethod("create", String.class, String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Map.class).invoke(null, string, string2, n, 0, 0, map);
        }
        catch (IllegalAccessException | InvocationTargetException reflectiveOperationException) {
            LOGGER.warn("Unable to invoke create method", (Throwable)reflectiveOperationException);
        }
        catch (NoSuchMethodException noSuchMethodException) {
            LOGGER.warn("Unable to find create method", (Throwable)noSuchMethodException);
        }
        return null;
    }

    private static Object initializeJmDns() {
        try {
            jmDNSClass = LoaderUtil.loadClass("javax.jmdns.JmDNS");
            serviceInfoClass = LoaderUtil.loadClass("javax.jmdns.ServiceInfo");
            boolean bl = false;
            try {
                jmDNSClass.getMethod("create", new Class[0]);
                bl = true;
            }
            catch (NoSuchMethodException noSuchMethodException) {}
            if (bl) {
                return MulticastDnsAdvertiser.createJmDnsVersion3();
            }
            return MulticastDnsAdvertiser.createJmDnsVersion1();
        }
        catch (ClassNotFoundException | ExceptionInInitializerError throwable) {
            LOGGER.warn("JmDNS or serviceInfo class not found", throwable);
            return null;
        }
    }
}

