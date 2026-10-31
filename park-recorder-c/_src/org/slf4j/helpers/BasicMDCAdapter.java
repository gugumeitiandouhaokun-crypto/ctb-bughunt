/*
 * Decompiled with CFR 0.152.
 */
package org.slf4j.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.spi.MDCAdapter;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class BasicMDCAdapter
implements MDCAdapter {
    private InheritableThreadLocal<Map<String, String>> inheritableThreadLocal = new InheritableThreadLocal<Map<String, String>>(){

        @Override
        protected Map<String, String> childValue(Map<String, String> map) {
            if (map == null) {
                return null;
            }
            return new HashMap<String, String>(map);
        }
    };

    @Override
    public void put(String string, String string2) {
        if (string == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        HashMap<String, String> hashMap = (HashMap<String, String>)this.inheritableThreadLocal.get();
        if (hashMap == null) {
            hashMap = new HashMap<String, String>();
            this.inheritableThreadLocal.set(hashMap);
        }
        hashMap.put(string, string2);
    }

    @Override
    public String get(String string) {
        Map map = (Map)this.inheritableThreadLocal.get();
        if (map != null && string != null) {
            return (String)map.get(string);
        }
        return null;
    }

    @Override
    public void remove(String string) {
        Map map = (Map)this.inheritableThreadLocal.get();
        if (map != null) {
            map.remove(string);
        }
    }

    @Override
    public void clear() {
        Map map = (Map)this.inheritableThreadLocal.get();
        if (map != null) {
            map.clear();
            this.inheritableThreadLocal.remove();
        }
    }

    public Set<String> getKeys() {
        Map map = (Map)this.inheritableThreadLocal.get();
        if (map != null) {
            return map.keySet();
        }
        return null;
    }

    @Override
    public Map<String, String> getCopyOfContextMap() {
        Map map = (Map)this.inheritableThreadLocal.get();
        if (map != null) {
            return new HashMap<String, String>(map);
        }
        return null;
    }

    @Override
    public void setContextMap(Map<String, String> map) {
        this.inheritableThreadLocal.set(new HashMap<String, String>(map));
    }
}

