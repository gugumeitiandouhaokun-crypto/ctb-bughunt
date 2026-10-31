/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.slf4j;

import java.util.Map;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.spi.MDCAdapter;

public class Log4jMDCAdapter
implements MDCAdapter {
    @Override
    public void put(String string, String string2) {
        ThreadContext.put(string, string2);
    }

    @Override
    public String get(String string) {
        return ThreadContext.get(string);
    }

    @Override
    public void remove(String string) {
        ThreadContext.remove(string);
    }

    @Override
    public void clear() {
        ThreadContext.clearMap();
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        return ThreadContext.getContext();
    }

    public void setContextMap(Map object) {
        ThreadContext.clearMap();
        for (Map.Entry entry : object.entrySet()) {
            ThreadContext.put((String)entry.getKey(), (String)entry.getValue());
        }
    }
}

