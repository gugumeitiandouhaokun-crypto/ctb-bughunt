/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.convert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;

public class Base64Converter {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static Method method = null;
    private static Object decoder = null;

    public static byte[] parseBase64Binary(String string) {
        if (method == null) {
            LOGGER.error("No base64 converter");
        } else {
            try {
                return (byte[])method.invoke(decoder, string);
            }
            catch (IllegalAccessException illegalAccessException) {
                LOGGER.error("Error decoding string - " + illegalAccessException.getMessage());
            }
            catch (InvocationTargetException invocationTargetException) {
                LOGGER.error("Error decoding string - " + invocationTargetException.getMessage());
            }
        }
        return new byte[0];
    }

    static {
        try {
            decoder = LoaderUtil.loadClass("java.util.Base64").getMethod("getDecoder", null).invoke(null, null);
            method = decoder.getClass().getMethod("decode", String.class);
        }
        catch (ClassNotFoundException classNotFoundException) {
        }
        catch (NoSuchMethodException noSuchMethodException) {
        }
        catch (IllegalAccessException illegalAccessException) {
        }
        catch (InvocationTargetException invocationTargetException) {}
        if (method == null) {
            try {
                method = LoaderUtil.loadClass("javax.xml.bind.DatatypeConverter").getMethod("parseBase64Binary", String.class);
            }
            catch (ClassNotFoundException classNotFoundException) {
                LOGGER.error("No Base64 Converter is available");
            }
            catch (NoSuchMethodException noSuchMethodException) {}
        }
    }
}

