/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Objects;
import java.util.Set;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;

public final class FileUtils {
    private static final String PROTOCOL_FILE = "file";
    private static final String JBOSS_FILE = "vfsfile";
    private static final Logger LOGGER = StatusLogger.getLogger();

    private FileUtils() {
    }

    public static File fileFromUri(URI uRI) {
        if (uRI == null) {
            return null;
        }
        if (uRI.isAbsolute()) {
            if (JBOSS_FILE.equals(uRI.getScheme())) {
                try {
                    uRI = new URI(PROTOCOL_FILE, uRI.getSchemeSpecificPart(), uRI.getFragment());
                }
                catch (URISyntaxException uRISyntaxException) {}
            }
            try {
                if (PROTOCOL_FILE.equals(uRI.getScheme())) {
                    return new File(uRI);
                }
            }
            catch (Exception exception) {
                LOGGER.warn("Invalid URI {}", (Object)uRI);
            }
        } else {
            Object object = new File(uRI.toString());
            try {
                if (((File)object).exists()) {
                    return object;
                }
                object = uRI.getPath();
                return new File((String)object);
            }
            catch (Exception exception) {
                LOGGER.warn("Invalid URI {}", (Object)uRI);
            }
        }
        return null;
    }

    public static boolean isFile(URL uRL) {
        return uRL != null && (uRL.getProtocol().equals(PROTOCOL_FILE) || uRL.getProtocol().equals(JBOSS_FILE));
    }

    public static String getFileExtension(File object) {
        if (((String)(object = ((File)object).getName())).lastIndexOf(".") != -1 && ((String)object).lastIndexOf(".") != 0) {
            Object object2 = object;
            return ((String)object2).substring(((String)object2).lastIndexOf(".") + 1);
        }
        return null;
    }

    public static void mkdir(File file, boolean bl) {
        if (!file.exists()) {
            if (!bl) {
                throw new IOException("The directory " + file.getAbsolutePath() + " does not exist.");
            }
            if (!file.mkdirs()) {
                throw new IOException("Could not create directory " + file.getAbsolutePath());
            }
        }
        if (!file.isDirectory()) {
            throw new IOException("File " + file + " exists and is not a directory. Unable to create directory.");
        }
    }

    public static void makeParentDirs(File file) {
        if ((file = Objects.requireNonNull(file, PROTOCOL_FILE).getCanonicalFile().getParentFile()) != null) {
            FileUtils.mkdir(file, true);
        }
    }

    public static void defineFilePosixAttributeView(Path object, Set<PosixFilePermission> set, String object2, String string) {
        if ((object = Files.getFileAttributeView((Path)object, PosixFileAttributeView.class, new LinkOption[0])) != null) {
            UserPrincipalLookupService userPrincipalLookupService = FileSystems.getDefault().getUserPrincipalLookupService();
            if (object2 != null && (object2 = userPrincipalLookupService.lookupPrincipalByName((String)object2)) != null) {
                object.setOwner((UserPrincipal)object2);
            }
            if (string != null && (object2 = userPrincipalLookupService.lookupPrincipalByGroupName(string)) != null) {
                object.setGroup((GroupPrincipal)object2);
            }
            if (set != null) {
                object.setPermissions(set);
            }
        }
    }

    public static boolean isFilePosixAttributeViewSupported() {
        return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    }
}

