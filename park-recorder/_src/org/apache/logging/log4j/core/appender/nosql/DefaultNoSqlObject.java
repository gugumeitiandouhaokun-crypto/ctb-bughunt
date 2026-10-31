/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.nosql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.core.appender.nosql.NoSqlObject;

public class DefaultNoSqlObject
implements NoSqlObject<Map<String, Object>> {
    private final Map<String, Object> map = new HashMap<String, Object>();

    @Override
    public void set(String string, Object object) {
        this.map.put(string, object);
    }

    @Override
    public void set(String string, NoSqlObject<Map<String, Object>> noSqlObject) {
        this.map.put(string, noSqlObject.unwrap());
    }

    @Override
    public void set(String string, Object[] objectArray) {
        this.map.put(string, Arrays.asList(objectArray));
    }

    @Override
    public void set(String string, NoSqlObject<Map<String, Object>>[] noSqlObjectArray) {
        ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>(noSqlObjectArray.length);
        for (NoSqlObject<Map<String, Object>> noSqlObject : noSqlObjectArray) {
            arrayList.add(noSqlObject.unwrap());
        }
        this.map.put(string, arrayList);
    }

    @Override
    public Map<String, Object> unwrap() {
        return this.map;
    }
}

