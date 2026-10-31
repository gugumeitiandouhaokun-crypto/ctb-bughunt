/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

public class ExceptionUtils {
    static final String WRAPPED_MARKER = " [wrapped] ";
    private static final String[] CAUSE_METHOD_NAMES = new String[]{"getCause", "getNextException", "getTargetException", "getException", "getSourceException", "getRootCause", "getCausedByException", "getNested", "getLinkedException", "getNestedException", "getLinkedCause", "getThrowable"};

    @Deprecated
    public static String[] getDefaultCauseMethodNames() {
        return ArrayUtils.clone(CAUSE_METHOD_NAMES);
    }

    @Deprecated
    public static Throwable getCause(Throwable throwable) {
        return ExceptionUtils.getCause(throwable, null);
    }

    /*
     * WARNING - void declaration
     */
    @Deprecated
    public static Throwable getCause(Throwable throwable, String[] object) {
        void var1_4;
        if (throwable == null) {
            return null;
        }
        if (object == null) {
            Throwable throwable2 = throwable.getCause();
            if (throwable2 != null) {
                return throwable2;
            }
            String[] stringArray = CAUSE_METHOD_NAMES;
        }
        for (void var4_8 : var1_4) {
            Throwable throwable3;
            if (var4_8 == null || (throwable3 = ExceptionUtils.getCauseUsingMethodName(throwable, (String)var4_8)) == null) continue;
            return throwable3;
        }
        return null;
    }

    public static Throwable getRootCause(Throwable object) {
        if ((object = ExceptionUtils.getThrowableList((Throwable)object)).size() < 2) {
            return null;
        }
        Object object2 = object;
        return (Throwable)object2.get(object2.size() - 1);
    }

    private static Throwable getCauseUsingMethodName(Throwable throwable, String string) {
        Method method = null;
        try {
            method = throwable.getClass().getMethod(string, new Class[0]);
        }
        catch (NoSuchMethodException noSuchMethodException) {
        }
        catch (SecurityException securityException) {}
        if (method != null && Throwable.class.isAssignableFrom(method.getReturnType())) {
            try {
                return (Throwable)method.invoke(throwable, new Object[0]);
            }
            catch (IllegalAccessException illegalAccessException) {
            }
            catch (IllegalArgumentException illegalArgumentException) {
            }
            catch (InvocationTargetException invocationTargetException) {}
        }
        return null;
    }

    public static int getThrowableCount(Throwable throwable) {
        return ExceptionUtils.getThrowableList(throwable).size();
    }

    public static Throwable[] getThrowables(Throwable throwable) {
        List<Throwable> list = ExceptionUtils.getThrowableList(throwable);
        return list.toArray(new Throwable[list.size()]);
    }

    public static List<Throwable> getThrowableList(Throwable throwable) {
        ArrayList<Throwable> arrayList = new ArrayList<Throwable>();
        while (throwable != null && !arrayList.contains(throwable)) {
            arrayList.add(throwable);
            throwable = ExceptionUtils.getCause(throwable);
        }
        return arrayList;
    }

    public static int indexOfThrowable(Throwable throwable, Class<?> clazz) {
        return ExceptionUtils.indexOf(throwable, clazz, 0, false);
    }

    public static int indexOfThrowable(Throwable throwable, Class<?> clazz, int n) {
        return ExceptionUtils.indexOf(throwable, clazz, n, false);
    }

    public static int indexOfType(Throwable throwable, Class<?> clazz) {
        return ExceptionUtils.indexOf(throwable, clazz, 0, true);
    }

    public static int indexOfType(Throwable throwable, Class<?> clazz, int n) {
        return ExceptionUtils.indexOf(throwable, clazz, n, true);
    }

    private static int indexOf(Throwable throwableArray, Class<?> clazz, int n, boolean bl) {
        if (throwableArray == null || clazz == null) {
            return -1;
        }
        if (n < 0) {
            n = 0;
        }
        if (n >= (throwableArray = ExceptionUtils.getThrowables((Throwable)throwableArray)).length) {
            return -1;
        }
        if (bl) {
            while (n < throwableArray.length) {
                if (clazz.isAssignableFrom(throwableArray[n].getClass())) {
                    return n;
                }
                ++n;
            }
        } else {
            while (n < throwableArray.length) {
                if (clazz.equals(throwableArray[n].getClass())) {
                    return n;
                }
                ++n;
            }
        }
        return -1;
    }

    public static void printRootCauseStackTrace(Throwable throwable) {
        ExceptionUtils.printRootCauseStackTrace(throwable, System.err);
    }

    public static void printRootCauseStackTrace(Throwable stringArray, PrintStream printStream) {
        if (stringArray == null) {
            return;
        }
        if (printStream == null) {
            throw new IllegalArgumentException("The PrintStream must not be null");
        }
        for (String string : ExceptionUtils.getRootCauseStackTrace((Throwable)stringArray)) {
            printStream.println(string);
        }
        printStream.flush();
    }

    public static void printRootCauseStackTrace(Throwable stringArray, PrintWriter printWriter) {
        if (stringArray == null) {
            return;
        }
        if (printWriter == null) {
            throw new IllegalArgumentException("The PrintWriter must not be null");
        }
        for (String string : ExceptionUtils.getRootCauseStackTrace((Throwable)stringArray)) {
            printWriter.println(string);
        }
        printWriter.flush();
    }

    public static String[] getRootCauseStackTrace(Throwable throwableArray) {
        if (throwableArray == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        throwableArray = ExceptionUtils.getThrowables((Throwable)throwableArray);
        int n = throwableArray.length;
        ArrayList<String> arrayList = new ArrayList<String>();
        List<String> list = ExceptionUtils.getStackFrameList(throwableArray[n - 1]);
        int n2 = n;
        while (--n2 >= 0) {
            List<String> list2 = list;
            if (n2 != 0) {
                list = ExceptionUtils.getStackFrameList(throwableArray[n2 - 1]);
                ExceptionUtils.removeCommonFrames(list2, list);
            }
            if (n2 == n - 1) {
                arrayList.add(throwableArray[n2].toString());
            } else {
                arrayList.add(WRAPPED_MARKER + throwableArray[n2].toString());
            }
            for (int j = 0; j < list2.size(); ++j) {
                arrayList.add(list2.get(j));
            }
        }
        ArrayList<String> arrayList2 = arrayList;
        return arrayList2.toArray(new String[arrayList2.size()]);
    }

    public static void removeCommonFrames(List<String> list, List<String> list2) {
        if (list == null || list2 == null) {
            throw new IllegalArgumentException("The List must not be null");
        }
        int n = list.size() - 1;
        for (int j = list2.size() - 1; n >= 0 && j >= 0; --n, --j) {
            String string;
            String string2 = list.get(n);
            if (!string2.equals(string = list2.get(j))) continue;
            list.remove(n);
        }
    }

    public static String getStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, true);
        throwable.printStackTrace(printWriter);
        return stringWriter.getBuffer().toString();
    }

    public static String[] getStackFrames(Throwable throwable) {
        if (throwable == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return ExceptionUtils.getStackFrames(ExceptionUtils.getStackTrace(throwable));
    }

    static String[] getStackFrames(String object) {
        Object object2 = SystemUtils.LINE_SEPARATOR;
        object = new StringTokenizer((String)object, (String)object2);
        object2 = new ArrayList();
        while (((StringTokenizer)object).hasMoreTokens()) {
            object2.add(((StringTokenizer)object).nextToken());
        }
        Object object3 = object2;
        return object3.toArray(new String[object3.size()]);
    }

    static List<String> getStackFrameList(Throwable object) {
        object = ExceptionUtils.getStackTrace((Throwable)object);
        Object object2 = SystemUtils.LINE_SEPARATOR;
        object = new StringTokenizer((String)object, (String)object2);
        object2 = new ArrayList();
        boolean bl = false;
        while (((StringTokenizer)object).hasMoreTokens()) {
            String string = ((StringTokenizer)object).nextToken();
            int n = string.indexOf("at");
            if (n != -1 && string.substring(0, n).trim().isEmpty()) {
                bl = true;
                object2.add(string);
                continue;
            }
            if (!bl) continue;
        }
        return object2;
    }

    public static String getMessage(Throwable object) {
        if (object == null) {
            return "";
        }
        String string = ClassUtils.getShortClassName(object, null);
        object = ((Throwable)object).getMessage();
        return string + ": " + StringUtils.defaultString((String)object);
    }

    public static String getRootCauseMessage(Throwable throwable) {
        Throwable throwable2 = ExceptionUtils.getRootCause(throwable);
        return ExceptionUtils.getMessage(throwable2 == null ? throwable : throwable2);
    }

    public static <R> R rethrow(Throwable throwable) {
        return ExceptionUtils.typeErasure(throwable);
    }

    private static <R, T extends Throwable> R typeErasure(Throwable throwable) {
        throw throwable;
    }

    public static <R> R wrapAndThrow(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException)throwable;
        }
        if (throwable instanceof Error) {
            throw (Error)throwable;
        }
        throw new UndeclaredThrowableException(throwable);
    }

    public static boolean hasCause(Throwable throwable, Class<? extends Throwable> clazz) {
        if (throwable instanceof UndeclaredThrowableException) {
            throwable = throwable.getCause();
        }
        return clazz.isInstance(throwable);
    }
}

