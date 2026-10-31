/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j;

import java.io.Closeable;
import java.util.Map;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.helpers.Util;
import org.slf4j.impl.StaticMDCBinder;
import org.slf4j.spi.MDCAdapter;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class MDC {
    static final String NULL_MDCA_URL = "http://www.slf4j.org/codes.html#null_MDCA";
    static final String NO_STATIC_MDC_BINDER_URL = "http://www.slf4j.org/codes.html#no_static_mdc_binder";
    static MDCAdapter mdcAdapter;

    private MDC() {
    }

    private static MDCAdapter bwCompatibleGetMDCAdapterFromBinder() {
        try {
            return StaticMDCBinder.getSingleton().getMDCA();
        }
        catch (NoSuchMethodError noSuchMethodError) {
            return StaticMDCBinder.SINGLETON.getMDCA();
        }
    }

    public static void put(String string, String string2) {
        if (string == null) {
            throw new IllegalArgumentException("key parameter cannot be null");
        }
        if (mdcAdapter == null) {
            throw new IllegalStateException("MDCAdapter cannot be null. See also http://www.slf4j.org/codes.html#null_MDCA");
        }
        mdcAdapter.put(string, string2);
    }

    public static MDCCloseable putCloseable(String string, String string2) {
        MDC.put(string, string2);
        return new MDCCloseable(string);
    }

    public static String get(String string) {
        if (string == null) {
            throw new IllegalArgumentException("key parameter cannot be null");
        }
        if (mdcAdapter == null) {
            throw new IllegalStateException("MDCAdapter cannot be null. See also http://www.slf4j.org/codes.html#null_MDCA");
        }
        return mdcAdapter.get(string);
    }

    public static void remove(String string) {
        if (string == null) {
            throw new IllegalArgumentException("key parameter cannot be null");
        }
        if (mdcAdapter == null) {
            throw new IllegalStateException("MDCAdapter cannot be null. See also http://www.slf4j.org/codes.html#null_MDCA");
        }
        mdcAdapter.remove(string);
    }

    public static void clear() {
        if (mdcAdapter == null) {
            throw new IllegalStateException("MDCAdapter cannot be null. See also http://www.slf4j.org/codes.html#null_MDCA");
        }
        mdcAdapter.clear();
    }

    public static Map<String, String> getCopyOfContextMap() {
        if (mdcAdapter == null) {
            throw new IllegalStateException("MDCAdapter cannot be null. See also http://www.slf4j.org/codes.html#null_MDCA");
        }
        return mdcAdapter.getCopyOfContextMap();
    }

    public static void setContextMap(Map<String, String> map) {
        if (mdcAdapter == null) {
            throw new IllegalStateException("MDCAdapter cannot be null. See also http://www.slf4j.org/codes.html#null_MDCA");
        }
        mdcAdapter.setContextMap(map);
    }

    public static MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    static {
        try {
            mdcAdapter = MDC.bwCompatibleGetMDCAdapterFromBinder();
            return;
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            mdcAdapter = new NOPMDCAdapter();
            String string = noClassDefFoundError.getMessage();
            if (string != null && string.contains("StaticMDCBinder")) {
                Util.report("Failed to load class \"org.slf4j.impl.StaticMDCBinder\".");
                Util.report("Defaulting to no-operation MDCAdapter implementation.");
                Util.report("See http://www.slf4j.org/codes.html#no_static_mdc_binder for further details.");
                return;
            }
            throw noClassDefFoundError;
        }
        catch (Exception exception) {
            Util.report("MDC binding unsuccessful.", exception);
            return;
        }
    }

    public static class MDCCloseable
    implements Closeable {
        private final String key;

        private MDCCloseable(String string) {
            this.key = string;
        }

        public void close() {
            MDC.remove(this.key);
        }
    }
}

