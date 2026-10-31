/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.io.InterruptedIOException;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Properties;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.Strings;

public final class OptionConverter {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String DELIM_START = "${";
    private static final char DELIM_STOP = '}';
    private static final int DELIM_START_LEN = 2;
    private static final int DELIM_STOP_LEN = 1;
    private static final int ONE_K = 1024;

    private OptionConverter() {
    }

    public static String[] concatenateArrays(String[] stringArray, String[] stringArray2) {
        String[] stringArray3 = new String[stringArray.length + stringArray2.length];
        System.arraycopy(stringArray, 0, stringArray3, 0, stringArray.length);
        System.arraycopy(stringArray2, 0, stringArray3, stringArray.length, stringArray2.length);
        return stringArray3;
    }

    public static String convertSpecialChars(String string) {
        int n = string.length();
        StringBuilder stringBuilder = new StringBuilder(n);
        int n2 = 0;
        while (n2 < n) {
            int n3;
            if ((n3 = string.charAt(n2++)) == 92) {
                n3 = string.charAt(n2++);
                switch (n3) {
                    case 110: {
                        n3 = 10;
                        break;
                    }
                    case 114: {
                        n3 = 13;
                        break;
                    }
                    case 116: {
                        n3 = 9;
                        break;
                    }
                    case 102: {
                        n3 = 12;
                        break;
                    }
                    case 98: {
                        n3 = 8;
                        break;
                    }
                    case 34: {
                        n3 = 34;
                        break;
                    }
                    case 39: {
                        n3 = 39;
                        break;
                    }
                    case 92: {
                        n3 = 92;
                    }
                }
            }
            stringBuilder.append((char)n3);
        }
        return stringBuilder.toString();
    }

    public static Object instantiateByKey(Properties object, String string, Class<?> clazz, Object object2) {
        if ((object = OptionConverter.findAndSubst(string, (Properties)object)) == null) {
            LOGGER.error("Could not find value for key {}", (Object)string);
            return object2;
        }
        return OptionConverter.instantiateByClassName(((String)object).trim(), clazz, object2);
    }

    public static boolean toBoolean(String string, boolean bl) {
        if (string == null) {
            return bl;
        }
        if ("true".equalsIgnoreCase(string = string.trim())) {
            return true;
        }
        if ("false".equalsIgnoreCase(string)) {
            return false;
        }
        return bl;
    }

    public static int toInt(String string, int n) {
        if (string != null) {
            string = string.trim();
            try {
                return Integer.parseInt(string);
            }
            catch (NumberFormatException numberFormatException) {
                LOGGER.error("[{}] is not in proper int form.", (Object)string, (Object)numberFormatException);
            }
        }
        return n;
    }

    public static Level toLevel(String string, Level objectArray) {
        if (string == null) {
            return objectArray;
        }
        int n = (string = string.trim()).indexOf(35);
        if (n == -1) {
            if ("NULL".equalsIgnoreCase(string)) {
                return null;
            }
            return Level.toLevel(string, (Level)objectArray);
        }
        Object object = objectArray;
        String string2 = string.substring(n + 1);
        if ("NULL".equalsIgnoreCase(string = string.substring(0, n))) {
            return null;
        }
        LOGGER.debug("toLevel:class=[" + string2 + "]:pri=[" + string + "]");
        try {
            GenericDeclaration genericDeclaration = Loader.loadClass(string2);
            Class[] classArray = new Class[]{String.class, Level.class};
            genericDeclaration = ((Class)genericDeclaration).getMethod("toLevel", classArray);
            objectArray = new Object[]{string, objectArray};
            object = (Level)((Method)genericDeclaration).invoke(null, objectArray);
        }
        catch (ClassNotFoundException classNotFoundException) {
            LOGGER.warn("custom level class [" + string2 + "] not found.");
        }
        catch (NoSuchMethodException noSuchMethodException) {
            LOGGER.warn("custom level class [" + string2 + "] does not have a class function toLevel(String, Level)", (Throwable)noSuchMethodException);
        }
        catch (InvocationTargetException invocationTargetException) {
            InvocationTargetException invocationTargetException2 = invocationTargetException;
            if (invocationTargetException.getTargetException() instanceof InterruptedException || invocationTargetException2.getTargetException() instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.warn("custom level class [" + string2 + "] could not be instantiated", (Throwable)invocationTargetException2);
        }
        catch (ClassCastException classCastException) {
            LOGGER.warn("class [" + string2 + "] is not a subclass of org.apache.log4j.Level", (Throwable)classCastException);
        }
        catch (IllegalAccessException illegalAccessException) {
            LOGGER.warn("class [" + string2 + "] cannot be instantiated due to access restrictions", (Throwable)illegalAccessException);
        }
        catch (RuntimeException runtimeException) {
            LOGGER.warn("class [" + string2 + "], level [" + string + "] conversion failed.", (Throwable)runtimeException);
        }
        return object;
    }

    public static long toFileSize(String string, long l) {
        if (string == null) {
            return l;
        }
        String string2 = string.trim().toUpperCase(Locale.ENGLISH);
        long l2 = 1L;
        int n = string2.indexOf("KB");
        if (n != -1) {
            l2 = 1024L;
            string2 = string2.substring(0, n);
        } else {
            n = string2.indexOf("MB");
            if (n != -1) {
                l2 = 0x100000L;
                string2 = string2.substring(0, n);
            } else {
                n = string2.indexOf("GB");
                if (n != -1) {
                    l2 = 0x40000000L;
                    string2 = string2.substring(0, n);
                }
            }
        }
        try {
            return Long.parseLong(string2) * l2;
        }
        catch (NumberFormatException numberFormatException) {
            LOGGER.error("[{}] is not in proper int form.", (Object)string2);
            LOGGER.error("[{}] not in expected format.", (Object)string, (Object)numberFormatException);
            return l;
        }
    }

    public static String findAndSubst(String string, Properties properties) {
        if ((string = properties.getProperty(string)) == null) {
            return null;
        }
        try {
            return OptionConverter.substVars(string, properties);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            LOGGER.error("Bad option value [{}].", (Object)string, (Object)illegalArgumentException);
            return string;
        }
    }

    public static Object instantiateByClassName(String string, Class<?> clazz, Object object) {
        if (string != null) {
            try {
                Class<?> clazz2 = Loader.loadClass(string);
                if (!clazz.isAssignableFrom(clazz2)) {
                    LOGGER.error("A \"{}\" object is not assignable to a \"{}\" variable.", (Object)string, (Object)clazz.getName());
                    LOGGER.error("The class \"{}\" was loaded by [{}] whereas object of type [{}] was loaded by [{}].", (Object)clazz.getName(), (Object)clazz.getClassLoader(), (Object)clazz2.getName());
                    return object;
                }
                return clazz2.newInstance();
            }
            catch (Exception exception) {
                LOGGER.error("Could not instantiate class [{}].", (Object)string, (Object)exception);
            }
        }
        return object;
    }

    public static String substVars(String string, Properties properties) {
        StringBuilder stringBuilder = new StringBuilder();
        int n = 0;
        while (true) {
            int n2;
            if ((n2 = string.indexOf(DELIM_START, n)) == -1) {
                if (n == 0) {
                    return string;
                }
                stringBuilder.append(string.substring(n, string.length()));
                return stringBuilder.toString();
            }
            stringBuilder.append(string.substring(n, n2));
            n = string.indexOf(125, n2);
            if (n == -1) {
                throw new IllegalArgumentException(Strings.dquote(string) + " has no closing brace. Opening brace at position " + n2 + '.');
            }
            String string2 = string.substring(n2 += 2, n);
            String string3 = PropertiesUtil.getProperties().getStringProperty(string2, null);
            if (string3 == null && properties != null) {
                string3 = properties.getProperty(string2);
            }
            if (string3 != null) {
                string2 = OptionConverter.substVars(string3, properties);
                stringBuilder.append(string2);
            }
            ++n;
        }
    }
}

