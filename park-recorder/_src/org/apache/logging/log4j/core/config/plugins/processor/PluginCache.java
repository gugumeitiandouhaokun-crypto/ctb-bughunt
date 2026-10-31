/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.processor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.apache.logging.log4j.core.config.plugins.processor.PluginEntry;

public class PluginCache {
    private final Map<String, Map<String, PluginEntry>> categories = new TreeMap<String, Map<String, PluginEntry>>();

    public Map<String, Map<String, PluginEntry>> getAllCategories() {
        return this.categories;
    }

    public Map<String, PluginEntry> getCategory(String string2) {
        string2 = string2.toLowerCase();
        return this.categories.computeIfAbsent(string2, string -> new TreeMap());
    }

    public void writeCache(OutputStream outputStream) {
        outputStream = new DataOutputStream(new BufferedOutputStream(outputStream));
        Throwable throwable = null;
        try {
            ((DataOutputStream)outputStream).writeInt(this.categories.size());
            for (Map.Entry<String, Map<String, PluginEntry>> entry : this.categories.entrySet()) {
                ((DataOutputStream)outputStream).writeUTF(entry.getKey());
                Map<String, PluginEntry> map = entry.getValue();
                ((DataOutputStream)outputStream).writeInt(map.size());
                Iterator<Map.Entry<String, PluginEntry>> iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    PluginEntry pluginEntry = iterator.next().getValue();
                    ((DataOutputStream)outputStream).writeUTF(pluginEntry.getKey());
                    ((DataOutputStream)outputStream).writeUTF(pluginEntry.getClassName());
                    ((DataOutputStream)outputStream).writeUTF(pluginEntry.getName());
                    ((DataOutputStream)outputStream).writeBoolean(pluginEntry.isPrintable());
                    ((DataOutputStream)outputStream).writeBoolean(pluginEntry.isDefer());
                }
            }
        }
        catch (Throwable throwable2) {
            try {
                Throwable throwable3 = throwable2;
                throwable = throwable2;
                throw throwable3;
            }
            catch (Throwable throwable4) {
                if (throwable != null) {
                    try {
                        ((FilterOutputStream)outputStream).close();
                    }
                    catch (Throwable throwable5) {
                        throwable.addSuppressed(throwable5);
                    }
                } else {
                    ((FilterOutputStream)outputStream).close();
                }
                throw throwable4;
            }
        }
        ((FilterOutputStream)outputStream).close();
        return;
    }

    public void loadCacheFiles(Enumeration<URL> enumeration) {
        this.categories.clear();
        while (enumeration.hasMoreElements()) {
            Object object = enumeration.nextElement();
            object = new DataInputStream(new BufferedInputStream(((URL)object).openStream()));
            Throwable throwable = null;
            try {
                int n = ((DataInputStream)object).readInt();
                for (int j = 0; j < n; ++j) {
                    String string = ((DataInputStream)object).readUTF();
                    Map<String, PluginEntry> map = this.getCategory(string);
                    int n2 = ((DataInputStream)object).readInt();
                    for (int k = 0; k < n2; ++k) {
                        String string2 = ((DataInputStream)object).readUTF();
                        String string3 = ((DataInputStream)object).readUTF();
                        String string5 = ((DataInputStream)object).readUTF();
                        boolean bl = ((DataInputStream)object).readBoolean();
                        boolean bl2 = ((DataInputStream)object).readBoolean();
                        map.computeIfAbsent(string2, string4 -> {
                            PluginEntry pluginEntry = new PluginEntry();
                            pluginEntry.setKey((String)string4);
                            pluginEntry.setClassName(string3);
                            pluginEntry.setName(string5);
                            pluginEntry.setPrintable(bl);
                            pluginEntry.setDefer(bl2);
                            pluginEntry.setCategory(string);
                            return pluginEntry;
                        });
                    }
                }
            }
            catch (Throwable throwable2) {
                try {
                    Throwable throwable3 = throwable2;
                    throwable = throwable2;
                    throw throwable3;
                }
                catch (Throwable throwable4) {
                    if (throwable != null) {
                        try {
                            ((FilterInputStream)object).close();
                        }
                        catch (Throwable throwable5) {
                            throwable.addSuppressed(throwable5);
                        }
                    } else {
                        ((FilterInputStream)object).close();
                    }
                    throw throwable4;
                }
            }
            ((FilterInputStream)object).close();
        }
    }

    public int size() {
        return this.categories.size();
    }
}

