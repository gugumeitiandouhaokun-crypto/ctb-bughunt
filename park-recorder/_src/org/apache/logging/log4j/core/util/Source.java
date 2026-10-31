/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.io.File;
import java.net.URI;
import java.util.Objects;
import org.apache.logging.log4j.core.config.ConfigurationSource;

public class Source {
    private final File file;
    private final URI uri;
    private final String location;

    public Source(ConfigurationSource configurationSource) {
        this.file = configurationSource.getFile();
        this.uri = configurationSource.getURI();
        this.location = configurationSource.getLocation();
    }

    public Source(File file) {
        this.file = Objects.requireNonNull(file, "file is null");
        this.location = file.getAbsolutePath();
        this.uri = null;
    }

    public Source(URI uRI, long l) {
        this.uri = Objects.requireNonNull(uRI, "URI is null");
        this.location = uRI.toString();
        this.file = null;
    }

    public File getFile() {
        return this.file;
    }

    public URI getURI() {
        return this.uri;
    }

    public String getLocation() {
        return this.location;
    }

    public String toString() {
        return this.location;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Source)) {
            return false;
        }
        object = (Source)object;
        return Objects.equals(this.location, ((Source)object).location);
    }

    public int hashCode() {
        return Objects.hash(this.location);
    }
}

