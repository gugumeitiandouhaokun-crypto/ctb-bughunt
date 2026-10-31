/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import java.io.File;
import java.util.List;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationListener;
import org.apache.logging.log4j.core.config.Reconfigurable;
import org.apache.logging.log4j.core.util.AbstractWatcher;
import org.apache.logging.log4j.core.util.FileWatcher;
import org.apache.logging.log4j.core.util.Source;
import org.apache.logging.log4j.core.util.Watcher;

public class ConfigurationFileWatcher
extends AbstractWatcher
implements FileWatcher {
    private File file;
    private long lastModifiedMillis;

    public ConfigurationFileWatcher(Configuration configuration, Reconfigurable reconfigurable, List<ConfigurationListener> list, long l) {
        super(configuration, reconfigurable, list);
        this.lastModifiedMillis = l;
    }

    @Override
    public long getLastModified() {
        if (this.file != null) {
            return this.file.lastModified();
        }
        return 0L;
    }

    @Override
    public void fileModified(File file) {
        this.lastModifiedMillis = file.lastModified();
    }

    @Override
    public void watching(Source source) {
        this.file = source.getFile();
        this.lastModifiedMillis = this.file.lastModified();
        super.watching(source);
    }

    @Override
    public boolean isModified() {
        return this.lastModifiedMillis != this.file.lastModified();
    }

    @Override
    public Watcher newWatcher(Reconfigurable object, List<ConfigurationListener> list, long l) {
        object = new ConfigurationFileWatcher(this.getConfiguration(), (Reconfigurable)object, list, l);
        if (this.getSource() != null) {
            ((ConfigurationFileWatcher)object).watching(this.getSource());
        }
        return object;
    }
}

