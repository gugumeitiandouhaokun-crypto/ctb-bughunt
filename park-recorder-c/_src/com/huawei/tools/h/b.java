/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.h;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class b {
    public static boolean ak(String object) {
        try {
            object = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse((String)object);
            Date date = Calendar.getInstance().getTime();
            return ((Date)object).compareTo(date) > 0;
        }
        catch (ParseException parseException) {
            parseException.printStackTrace();
            return false;
        }
    }
}

