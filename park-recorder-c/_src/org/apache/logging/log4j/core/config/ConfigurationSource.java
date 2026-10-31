/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.net.UrlConnectionFactory;
import org.apache.logging.log4j.core.util.FileUtils;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.core.util.Source;
import org.apache.logging.log4j.util.LoaderUtil;

public class ConfigurationSource {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final ConfigurationSource NULL_SOURCE = new ConfigurationSource(EMPTY_BYTE_ARRAY, null, 0L);
    public static final ConfigurationSource COMPOSITE_SOURCE = new ConfigurationSource(EMPTY_BYTE_ARRAY, null, 0L);
    private final File file;
    private final URL url;
    private final String location;
    private final InputStream stream;
    private volatile byte[] data;
    private volatile Source source = null;
    private final long lastModified;
    private volatile long modifiedMillis;

    public ConfigurationSource(InputStream inputStream, File file) {
        this.stream = Objects.requireNonNull(inputStream, "stream is null");
        this.file = Objects.requireNonNull(file, "file is null");
        this.location = file.getAbsolutePath();
        this.url = null;
        this.data = null;
        long l = 0L;
        try {
            l = file.lastModified();
        }
        catch (Exception exception) {}
        this.lastModified = l;
    }

    public ConfigurationSource(InputStream inputStream, URL uRL) {
        this.stream = Objects.requireNonNull(inputStream, "stream is null");
        this.url = Objects.requireNonNull(uRL, "URL is null");
        this.location = uRL.toString();
        this.file = null;
        this.data = null;
        this.lastModified = 0L;
    }

    public ConfigurationSource(InputStream inputStream, URL uRL, long l) {
        this.stream = Objects.requireNonNull(inputStream, "stream is null");
        this.url = Objects.requireNonNull(uRL, "URL is null");
        this.location = uRL.toString();
        this.file = null;
        this.data = null;
        this.lastModified = l;
    }

    public ConfigurationSource(InputStream inputStream) {
        this(ConfigurationSource.toByteArray(inputStream), null, 0L);
    }

    public ConfigurationSource(Source source, byte[] byArray, long l) {
        Objects.requireNonNull(source, "source is null");
        this.data = Objects.requireNonNull(byArray, "data is null");
        this.stream = new ByteArrayInputStream(byArray);
        this.file = source.getFile();
        this.url = source.getURI().toURL();
        this.location = source.getLocation();
        this.lastModified = l;
    }

    private ConfigurationSource(byte[] byArray, URL uRL, long l) {
        this.data = Objects.requireNonNull(byArray, "data is null");
        this.stream = new ByteArrayInputStream(byArray);
        this.file = null;
        this.url = uRL;
        this.location = null;
        this.lastModified = l;
        if (uRL == null) {
            this.data = byArray;
        }
    }

    private static byte[] toByteArray(InputStream inputStream) {
        int n = Math.max(4096, inputStream.available());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(n);
        byte[] byArray = new byte[n];
        int n2 = inputStream.read(byArray);
        while (n2 > 0) {
            byteArrayOutputStream.write(byArray, 0, n2);
            n2 = inputStream.read(byArray);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public File getFile() {
        return this.file;
    }

    public URL getURL() {
        return this.url;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public void setData(byte[] byArray) {
        this.data = byArray;
    }

    public void setModifiedMillis(long l) {
        this.modifiedMillis = l;
    }

    public URI getURI() {
        URI uRI = null;
        if (this.url != null) {
            try {
                uRI = this.url.toURI();
            }
            catch (URISyntaxException uRISyntaxException) {}
        }
        if (uRI == null && this.file != null) {
            uRI = this.file.toURI();
        }
        if (uRI == null && this.location != null) {
            try {
                uRI = new URI(this.location);
            }
            catch (URISyntaxException uRISyntaxException) {
                try {
                    uRI = new URI("file://" + this.location);
                }
                catch (URISyntaxException uRISyntaxException2) {}
            }
        }
        return uRI;
    }

    public long getLastModified() {
        return this.lastModified;
    }

    public String getLocation() {
        return this.location;
    }

    public InputStream getInputStream() {
        return this.stream;
    }

    public ConfigurationSource resetInputStream() {
        if (this.source != null) {
            return new ConfigurationSource(this.source, this.data, this.lastModified);
        }
        if (this.file != null) {
            return new ConfigurationSource((InputStream)new FileInputStream(this.file), this.file);
        }
        if (this.url != null && this.data != null) {
            return new ConfigurationSource(this.data, this.url, this.modifiedMillis == 0L ? this.lastModified : this.modifiedMillis);
        }
        if (this.url != null) {
            return ConfigurationSource.fromUri(this.getURI());
        }
        if (this.data != null) {
            return new ConfigurationSource(this.data, null, this.lastModified);
        }
        return null;
    }

    public String toString() {
        if (this.location != null) {
            return this.location;
        }
        if (this == NULL_SOURCE) {
            return "NULL_SOURCE";
        }
        int n = this.data == null ? -1 : this.data.length;
        return "stream (" + n + " bytes, unknown location)";
    }

    public static ConfigurationSource fromUri(URI uRI) {
        Object object = FileUtils.fileFromUri(uRI);
        if (object != null && ((File)object).exists() && ((File)object).canRead()) {
            try {
                return new ConfigurationSource((InputStream)new FileInputStream((File)object), (File)object);
            }
            catch (FileNotFoundException fileNotFoundException) {
                ConfigurationFactory.LOGGER.error("Cannot locate file {}", (Object)uRI.getPath(), (Object)fileNotFoundException);
            }
        }
        if (ConfigurationFactory.isClassLoaderUri(uRI)) {
            object = LoaderUtil.getThreadContextClassLoader();
            object = ConfigurationSource.fromResource(ConfigurationFactory.extractClassLoaderUriPath(uRI), (ClassLoader)object);
            if (object != null) {
                return object;
            }
        }
        if (!uRI.isAbsolute()) {
            ConfigurationFactory.LOGGER.error("File not found in file system or classpath: {}", (Object)uRI.toString());
            return null;
        }
        try {
            URLConnection uRLConnection = UrlConnectionFactory.createConnection(uRI.toURL());
            object = uRLConnection.getInputStream();
            long l = uRLConnection.getLastModified();
            return new ConfigurationSource((InputStream)object, uRI.toURL(), l);
        }
        catch (MalformedURLException malformedURLException) {
            ConfigurationFactory.LOGGER.error("Invalid URL {}", (Object)uRI.toString(), (Object)malformedURLException);
        }
        catch (Exception exception) {
            ConfigurationFactory.LOGGER.error("Unable to access {}", (Object)uRI.toString(), (Object)exception);
        }
        return null;
    }

    public static ConfigurationSource fromResource(String object, ClassLoader object2) {
        if ((object = Loader.getResource((String)object, (ClassLoader)object2)) == null) {
            return null;
        }
        try {
            object2 = ((URL)object).openStream();
        }
        catch (IOException iOException) {
            ConfigurationFactory.LOGGER.catching(Level.DEBUG, iOException);
            return null;
        }
        if (object2 == null) {
            return null;
        }
        if (FileUtils.isFile((URL)object)) {
            try {
                return new ConfigurationSource((InputStream)object2, FileUtils.fileFromUri(((URL)object).toURI()));
            }
            catch (URISyntaxException uRISyntaxException) {
                ConfigurationFactory.LOGGER.catching(Level.DEBUG, uRISyntaxException);
            }
        }
        return new ConfigurationSource((InputStream)object2, (URL)object);
    }
}

