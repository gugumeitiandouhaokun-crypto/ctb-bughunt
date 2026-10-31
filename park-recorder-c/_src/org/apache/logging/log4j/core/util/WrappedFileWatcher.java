/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.io.File;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationListener;
import org.apache.logging.log4j.core.config.Reconfigurable;
import org.apache.logging.log4j.core.util.AbstractWatcher;
import org.apache.logging.log4j.core.util.FileWatcher;
import org.apache.logging.log4j.core.util.Source;
import org.apache.logging.log4j.core.util.Watcher;

public class WrappedFileWatcher
extends AbstractWatcher
implements FileWatcher {
    private final FileWatcher watcher;
    private volatile long lastModifiedMillis;

    public WrappedFileWatcher(FileWatcher fileWatcher, Configuration configuration, Reconfigurable reconfigurable, List<ConfigurationListener> list, long l) {
        super(configuration, reconfigurable, list);
        this.watcher = fileWatcher;
        this.lastModifiedMillis = l;
    }

    public WrappedFileWatcher(FileWatcher fileWatcher) {
        super(null, null, null);
        this.watcher = fileWatcher;
    }

    @Override
    public long getLastModified() {
        return this.lastModifiedMillis;
    }

    @Override
    public void fileModified(File file) {
        this.watcher.fileModified(file);
    }

    @Override
    public boolean isModified() {
        long l = this.getSource().getFile().lastModified();
        if (this.lastModifiedMillis != l) {
            this.lastModifiedMillis = l;
            return true;
        }
        return false;
    }

    @Override
    public List<ConfigurationListener> getListeners() {
        if (super.getListeners() != null) {
            return Collections.unmodifiableList(super.getListeners());
        }
        return null;
    }

    @Override
    public void modified() {
        if (this.getListeners() != null) {
            super.modified();
        }
        WrappedFileWatcher wrappedFileWatcher = this;
        wrappedFileWatcher.fileModified(wrappedFileWatcher.getSource().getFile());
        this.lastModifiedMillis = this.getSource().getFile().lastModified();
    }

    @Override
    public void watching(Source source) {
        this.lastModifiedMillis = source.getFile().lastModified();
        super.watching(source);
    }

    @Override
    public Watcher newWatcher(Reconfigurable object, List<ConfigurationListener> list, long l) {
        object = new WrappedFileWatcher(this.watcher, this.getConfiguration(), (Reconfigurable)object, list, l);
        if (this.getSource() != null) {
            ((WrappedFileWatcher)object).watching(this.getSource());
        }
        return object;
    }
}

