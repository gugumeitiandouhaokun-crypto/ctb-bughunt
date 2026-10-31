/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.util.PerformanceSensitive;

@PerformanceSensitive
public class AppenderControlArraySet {
    private final AtomicReference<AppenderControl[]> appenderArray = new AtomicReference<AppenderControl[]>(new AppenderControl[0]);

    public boolean add(AppenderControl appenderControl) {
        AppenderControl[] appenderControlArray;
        AppenderControl[] appenderControlArray2;
        do {
            appenderControlArray = appenderControlArray2 = this.appenderArray.get();
            int n = appenderControlArray2.length;
            for (int i2 = 0; i2 < n; ++i2) {
                if (!appenderControlArray[i2].equals(appenderControl)) continue;
                return false;
            }
            AppenderControl[] appenderControlArray3 = Arrays.copyOf(appenderControlArray2, appenderControlArray2.length + 1);
            appenderControlArray = appenderControlArray3;
            appenderControlArray3[appenderControlArray3.length - 1] = appenderControl;
        } while (!this.appenderArray.compareAndSet(appenderControlArray2, appenderControlArray));
        return true;
    }

    public AppenderControl remove(String string) {
        boolean bl;
        block0: do {
            bl = true;
            AppenderControl[] appenderControlArray = this.appenderArray.get();
            for (int i2 = 0; i2 < appenderControlArray.length; ++i2) {
                AppenderControl appenderControl = appenderControlArray[i2];
                if (!Objects.equals(string, appenderControl.getAppenderName())) continue;
                AppenderControl[] appenderControlArray2 = this.removeElementAt(i2, appenderControlArray);
                if (this.appenderArray.compareAndSet(appenderControlArray, appenderControlArray2)) {
                    return appenderControl;
                }
                bl = false;
                continue block0;
            }
        } while (!bl);
        return null;
    }

    private AppenderControl[] removeElementAt(int n, AppenderControl[] appenderControlArray) {
        AppenderControl[] appenderControlArray2 = Arrays.copyOf(appenderControlArray, appenderControlArray.length - 1);
        System.arraycopy(appenderControlArray, n + 1, appenderControlArray2, n, appenderControlArray2.length - n);
        return appenderControlArray2;
    }

    public Map<String, Appender> asMap() {
        HashMap<String, Appender> hashMap = new HashMap<String, Appender>();
        for (AppenderControl appenderControl : this.appenderArray.get()) {
            hashMap.put(appenderControl.getAppenderName(), appenderControl.getAppender());
        }
        return hashMap;
    }

    public AppenderControl[] clear() {
        return this.appenderArray.getAndSet(new AppenderControl[0]);
    }

    public boolean isEmpty() {
        return this.appenderArray.get().length == 0;
    }

    public AppenderControl[] get() {
        return this.appenderArray.get();
    }

    public String toString() {
        return "AppenderControlArraySet [appenderArray=" + this.appenderArray + "]";
    }
}

