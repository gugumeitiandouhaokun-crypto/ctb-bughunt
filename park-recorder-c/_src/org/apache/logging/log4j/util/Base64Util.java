/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.lang.reflect.Method;
import org.apache.logging.log4j.LoggingException;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.LowLevelLogUtil;

public final class Base64Util {
    private static Method encodeMethod = null;
    private static Object encoder = null;

    private Base64Util() {
    }

    public static String encode(String object) {
        if (object == null) {
            return null;
        }
        object = ((String)object).getBytes();
        if (encodeMethod != null) {
            try {
                return (String)encodeMethod.invoke(encoder, object);
            }
            catch (Exception exception) {
                throw new LoggingException("Unable to encode String", exception);
            }
        }
        throw new LoggingException("No Encoder, unable to encode string");
    }

    static {
        try {
            Class<?> clazz = LoaderUtil.loadClass("java.util.Base64");
            Class<?> clazz2 = LoaderUtil.loadClass("java.util.Base64$Encoder");
            encoder = clazz.getMethod("getEncoder", new Class[0]).invoke(null, new Object[0]);
            encodeMethod = clazz2.getMethod("encodeToString", byte[].class);
        }
        catch (Exception exception) {
            try {
                encodeMethod = LoaderUtil.loadClass("javax.xml.bind.DataTypeConverter").getMethod("printBase64Binary", new Class[0]);
            }
            catch (Exception exception2) {
                LowLevelLogUtil.logException("Unable to create a Base64 Encoder", exception2);
            }
        }
    }
}

