/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.osgi.framework.FrameworkUtil
 *  org.osgi.framework.wiring.BundleWiring
 */
package org.apache.logging.log4j.core.config.plugins.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipFile;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.status.StatusLogger;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;

public class ResolverUtil {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String VFSZIP = "vfszip";
    private static final String VFS = "vfs";
    private static final String JAR = "jar";
    private static final String BUNDLE_RESOURCE = "bundleresource";
    private final Set<Class<?>> classMatches = new HashSet();
    private final Set<URI> resourceMatches = new HashSet<URI>();
    private ClassLoader classloader;

    public Set<Class<?>> getClasses() {
        return this.classMatches;
    }

    public Set<URI> getResources() {
        return this.resourceMatches;
    }

    public ClassLoader getClassLoader() {
        if (this.classloader != null) {
            return this.classloader;
        }
        this.classloader = Loader.getClassLoader(ResolverUtil.class, null);
        return this.classloader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classloader = classLoader;
    }

    public void find(Test test, String ... stringArray) {
        if (stringArray == null) {
            return;
        }
        for (String string : stringArray) {
            this.findInPackage(test, string);
        }
    }

    public void findInPackage(Test test, String string) {
        string = string.replace('.', '/');
        Object object = this.getClassLoader();
        try {
            object = ((ClassLoader)object).getResources(string);
        }
        catch (IOException iOException) {
            LOGGER.warn("Could not read package: {}", (Object)string, (Object)iOException);
            return;
        }
        while (object.hasMoreElements()) {
            try {
                Object object2;
                Object object3;
                Object object4 = (URL)object.nextElement();
                Object object5 = this.extractPath((URL)object4);
                LOGGER.info("Scanning for classes in '{}' matching criteria {}", object5, (Object)test);
                if (VFSZIP.equals(((URL)object4).getProtocol())) {
                    object3 = ((String)object5).substring(0, ((String)object5).length() - string.length() - 2);
                    object2 = new URL(((URL)object4).getProtocol(), ((URL)object4).getHost(), (String)object3);
                    object5 = new JarInputStream(((URL)object2).openStream());
                    try {
                        this.loadImplementationsInJar(test, string, (String)object3, (JarInputStream)object5);
                        continue;
                    }
                    finally {
                        this.close((JarInputStream)object5, object2);
                        continue;
                    }
                }
                if (VFS.equals(((URL)object4).getProtocol())) {
                    object3 = ((String)object5).substring(1, ((String)object5).length() - string.length() - 2);
                    object2 = new File((String)object3);
                    if (((File)object2).exists()) {
                        if (((File)object2).isDirectory()) {
                            this.loadImplementationsInDirectory(test, string, new File((File)object2, string));
                            continue;
                        }
                        this.loadImplementationsInJar(test, string, (File)object2);
                        continue;
                    }
                    object5 = ((String)object5).substring(0, ((String)object5).length() - string.length() - 2);
                    object4 = new URL(((URL)object4).getProtocol(), ((URL)object4).getHost(), (String)object5).openStream();
                    object3 = null;
                    try {
                        object2 = object4 instanceof JarInputStream ? (JarInputStream)object4 : new JarInputStream((InputStream)object4);
                        this.loadImplementationsInJar(test, string, (String)object5, (JarInputStream)object2);
                        if (object4 == null) continue;
                    }
                    catch (Throwable throwable) {
                        try {
                            object2 = throwable;
                            object3 = throwable;
                            throw object2;
                        }
                        catch (Throwable throwable2) {
                            if (object4 != null) {
                                if (object3 != null) {
                                    try {
                                        ((InputStream)object4).close();
                                    }
                                    catch (Throwable throwable3) {
                                        ((Throwable)object3).addSuppressed(throwable3);
                                    }
                                } else {
                                    ((InputStream)object4).close();
                                }
                            }
                            throw throwable2;
                        }
                    }
                    ((InputStream)object4).close();
                    continue;
                }
                if (BUNDLE_RESOURCE.equals(((URL)object4).getProtocol())) {
                    this.loadImplementationsInBundle(test, string);
                    continue;
                }
                if (JAR.equals(((URL)object4).getProtocol())) {
                    this.loadImplementationsInJar(test, string, (URL)object4);
                    continue;
                }
                object3 = new File((String)object5);
                if (((File)object3).isDirectory()) {
                    this.loadImplementationsInDirectory(test, string, (File)object3);
                    continue;
                }
                this.loadImplementationsInJar(test, string, (File)object3);
            }
            catch (IOException | URISyntaxException exception) {
                LOGGER.warn("Could not read entries", (Throwable)exception);
            }
        }
    }

    String extractPath(URL object) {
        int n;
        String string = ((URL)object).getPath();
        if (string.startsWith("jar:")) {
            string = string.substring(4);
        }
        if (string.startsWith("file:")) {
            string = string.substring(5);
        }
        if ((n = string.indexOf(33)) > 0) {
            string = string.substring(0, n);
        }
        object = ((URL)object).getProtocol();
        if (Arrays.asList(VFS, VFSZIP, BUNDLE_RESOURCE).contains(object)) {
            return string;
        }
        object = new URI(string).getPath();
        if (new File((String)object).exists()) {
            return object;
        }
        return URLDecoder.decode(string, StandardCharsets.UTF_8.name());
    }

    private void loadImplementationsInBundle(Test test, String object) {
        for (String string : ((BundleWiring)FrameworkUtil.getBundle(ResolverUtil.class).adapt(BundleWiring.class)).listResources((String)object, "*.class", 1)) {
            this.addIfMatching(test, string);
        }
    }

    private void loadImplementationsInDirectory(Test test, String string, File object) {
        if ((object = ((File)object).listFiles()) == null) {
            return;
        }
        File[] fileArray = object;
        int n = ((File[])object).length;
        for (int i2 = 0; i2 < n; ++i2) {
            File file = fileArray[i2];
            object = new StringBuilder();
            ((StringBuilder)object).append(string).append('/').append(file.getName());
            Object object2 = object = string == null ? file.getName() : ((StringBuilder)object).toString();
            if (file.isDirectory()) {
                this.loadImplementationsInDirectory(test, (String)object, file);
                continue;
            }
            if (!this.isTestApplicable(test, file.getName())) continue;
            this.addIfMatching(test, (String)object);
        }
    }

    private boolean isTestApplicable(Test test, String string) {
        return test.doesMatchResource() || string.endsWith(".class") && test.doesMatchClass();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void loadImplementationsInJar(Test test, String string, URL uRL) {
        try {
            Object object = (JarURLConnection)uRL.openConnection();
            if (object != null) {
                object = ((JarURLConnection)object).getJarFile();
                Throwable throwable = null;
                try {
                    Enumeration<JarEntry> enumeration = ((JarFile)object).entries();
                    while (enumeration.hasMoreElements()) {
                        JarEntry jarEntry = enumeration.nextElement();
                        String string2 = jarEntry.getName();
                        if (jarEntry.isDirectory() || !string2.startsWith(string) || !this.isTestApplicable(test, string2)) continue;
                        this.addIfMatching(test, string2);
                    }
                    if (object == null) return;
                }
                catch (Throwable throwable2) {
                    try {
                        Throwable throwable3 = throwable2;
                        throwable = throwable2;
                        throw throwable3;
                    }
                    catch (Throwable throwable4) {
                        if (object == null) throw throwable4;
                        if (throwable != null) {
                            try {
                                ((ZipFile)object).close();
                                throw throwable4;
                            }
                            catch (Throwable throwable5) {
                                throwable.addSuppressed(throwable5);
                            }
                            throw throwable4;
                        } else {
                            ((ZipFile)object).close();
                        }
                        throw throwable4;
                    }
                }
                ((ZipFile)object).close();
                return;
            }
            LOGGER.error("Could not establish connection to {}", (Object)uRL.toString());
            return;
        }
        catch (IOException iOException) {
            LOGGER.error("Could not search JAR file '{}' for classes matching criteria {}, file not found", (Object)uRL.toString(), (Object)test, (Object)iOException);
            return;
        }
    }

    private void loadImplementationsInJar(Test test, String string, File file) {
        JarInputStream jarInputStream = null;
        try {
            jarInputStream = new JarInputStream(new FileInputStream(file));
            this.loadImplementationsInJar(test, string, file.getPath(), jarInputStream);
            this.close(jarInputStream, file);
            return;
        }
        catch (IOException iOException) {
            try {
                LOGGER.error("Could not search JAR file '{}' for classes matching criteria {}, file not found", (Object)file, (Object)test, (Object)iOException);
                this.close(jarInputStream, file);
                return;
            }
            catch (Throwable throwable) {
                this.close(jarInputStream, file);
                throw throwable;
            }
        }
    }

    private void close(JarInputStream jarInputStream, Object object) {
        if (jarInputStream != null) {
            try {
                jarInputStream.close();
                return;
            }
            catch (IOException iOException) {
                LOGGER.error("Error closing JAR file stream for {}", object, (Object)iOException);
            }
        }
    }

    private void loadImplementationsInJar(Test test, String string, String string2, JarInputStream jarInputStream) {
        try {
            JarEntry jarEntry;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                String string3 = jarEntry.getName();
                if (jarEntry.isDirectory() || !string3.startsWith(string) || !this.isTestApplicable(test, string3)) continue;
                this.addIfMatching(test, string3);
            }
            return;
        }
        catch (IOException iOException) {
            LOGGER.error("Could not search JAR file '{}' for classes matching criteria {} due to an IOException", (Object)string2, (Object)test, (Object)iOException);
            return;
        }
    }

    protected void addIfMatching(Test test, String string) {
        try {
            Serializable serializable;
            ClassLoader classLoader = this.getClassLoader();
            if (test.doesMatchClass()) {
                serializable = string.substring(0, string.indexOf(46)).replace('/', '.');
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Checking to see if class {} matches criteria {}", (Object)serializable, (Object)test);
                }
                if (test.matches((Class<?>)(serializable = classLoader.loadClass((String)((Object)serializable))))) {
                    this.classMatches.add((Class<?>)serializable);
                }
            }
            if (test.doesMatchResource()) {
                serializable = classLoader.getResource(string);
                if (serializable == null) {
                    serializable = classLoader.getResource(string.substring(1));
                }
                if (serializable != null && test.matches(((URL)serializable).toURI())) {
                    this.resourceMatches.add(((URL)serializable).toURI());
                }
            }
            return;
        }
        catch (Throwable throwable) {
            LOGGER.warn("Could not examine class {}", (Object)string, (Object)throwable);
            return;
        }
    }

    public static interface Test {
        public boolean matches(Class<?> var1);

        public boolean matches(URI var1);

        public boolean doesMatchClass();

        public boolean doesMatchResource();
    }
}

