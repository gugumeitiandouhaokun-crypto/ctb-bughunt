/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.ThreadContext;

public class CloseableThreadContext {
    private CloseableThreadContext() {
    }

    public static Instance push(String string) {
        return new Instance().push(string);
    }

    public static Instance push(String string, Object ... objectArray) {
        return new Instance().push(string, objectArray);
    }

    public static Instance put(String string, String string2) {
        return new Instance().put(string, string2);
    }

    public static Instance pushAll(List<String> list) {
        return new Instance().pushAll(list);
    }

    public static Instance putAll(Map<String, String> map) {
        return new Instance().putAll(map);
    }

    public static class Instance
    implements AutoCloseable {
        private int pushCount = 0;
        private final Map<String, String> originalValues = new HashMap<String, String>();

        private Instance() {
        }

        public Instance push(String string) {
            ThreadContext.push(string);
            ++this.pushCount;
            return this;
        }

        public Instance push(String string, Object[] objectArray) {
            ThreadContext.push(string, objectArray);
            ++this.pushCount;
            return this;
        }

        public Instance put(String string, String string2) {
            if (!this.originalValues.containsKey(string)) {
                String string3 = string;
                this.originalValues.put(string3, ThreadContext.get(string3));
            }
            ThreadContext.put(string, string2);
            return this;
        }

        public Instance putAll(Map<String, String> object) {
            Map<String, String> map = ThreadContext.getContext();
            ThreadContext.putAll(object);
            for (String string : object.keySet()) {
                if (this.originalValues.containsKey(string)) continue;
                this.originalValues.put(string, map.get(string));
            }
            return this;
        }

        public Instance pushAll(List<String> object) {
            object = object.iterator();
            while (object.hasNext()) {
                String string = (String)object.next();
                this.push(string);
            }
            return this;
        }

        @Override
        public void close() {
            this.closeStack();
            this.closeMap();
        }

        private void closeMap() {
            Iterator<Map.Entry<String, String>> iterator = this.originalValues.entrySet().iterator();
            while (iterator.hasNext()) {
                Object object = iterator.next();
                String string = object.getKey();
                if ((object = object.getValue()) == null) {
                    ThreadContext.remove(string);
                } else {
                    ThreadContext.put(string, (String)object);
                }
                iterator.remove();
            }
        }

        private void closeStack() {
            for (int i2 = 0; i2 < this.pushCount; ++i2) {
                ThreadContext.pop();
            }
            this.pushCount = 0;
        }
    }
}

