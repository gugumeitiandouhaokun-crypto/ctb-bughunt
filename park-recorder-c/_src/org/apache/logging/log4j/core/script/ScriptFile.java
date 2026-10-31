/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.script.AbstractScript;
import org.apache.logging.log4j.core.util.ExtensionLanguageMapping;
import org.apache.logging.log4j.core.util.FileUtils;
import org.apache.logging.log4j.core.util.IOUtils;
import org.apache.logging.log4j.core.util.NetUtils;

@Plugin(name="ScriptFile", category="Core", printObject=true)
public class ScriptFile
extends AbstractScript {
    private final Path filePath;
    private final boolean isWatched;

    public ScriptFile(String string, Path path, String string2, boolean bl, String string3) {
        super(string, string2, string3);
        this.filePath = path;
        this.isWatched = bl;
    }

    public Path getPath() {
        return this.filePath;
    }

    public boolean isWatched() {
        return this.isWatched;
    }

    @PluginFactory
    public static ScriptFile createScript(@PluginAttribute(value="name") String object, @PluginAttribute(value="language") String string, @PluginAttribute(value="path") String string2, @PluginAttribute(value="isWatched") Boolean bl, @PluginAttribute(value="charset") Charset object2) {
        Object object3;
        Object object4;
        if (string2 == null) {
            LOGGER.error("No script path provided for ScriptFile");
            return null;
        }
        if (object == null) {
            object = string2;
        }
        URI uRI = NetUtils.toURI(string2);
        File file = FileUtils.fileFromUri(uRI);
        if (string == null && file != null && (object4 = FileUtils.getFileExtension(file)) != null && (object3 = ExtensionLanguageMapping.getByExtension((String)object4)) != null) {
            string = object3.getLanguage();
        }
        if (string == null) {
            LOGGER.info("No script language supplied, defaulting to {}", (Object)"JavaScript");
            string = "JavaScript";
        }
        object4 = object2 == null ? Charset.defaultCharset() : object2;
        try {
            object2 = new InputStreamReader(file != null ? new FileInputStream(file) : uRI.toURL().openStream(), (Charset)object4);
            Throwable throwable = null;
            try {
                object3 = IOUtils.toString((Reader)object2);
            }
            catch (Throwable throwable2) {
                try {
                    object = throwable2;
                    throwable = throwable2;
                    throw object;
                }
                catch (Throwable throwable3) {
                    if (throwable != null) {
                        try {
                            ((Reader)object2).close();
                        }
                        catch (Throwable throwable4) {
                            throwable.addSuppressed(throwable4);
                        }
                    } else {
                        ((Reader)object2).close();
                    }
                    throw throwable3;
                }
            }
            ((Reader)object2).close();
        }
        catch (IOException iOException) {
            LOGGER.error("{}: language={}, path={}, actualCharset={}", (Object)iOException.getClass().getSimpleName(), (Object)string, (Object)string2, object4);
            return null;
        }
        object2 = file != null ? Paths.get(file.toURI()) : Paths.get(uRI);
        if (object2 == null) {
            LOGGER.error("Unable to convert {} to a Path", (Object)uRI.toString());
            return null;
        }
        return new ScriptFile((String)object, (Path)object2, string, bl == null ? Boolean.FALSE : bl, (String)object3);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (!this.getName().equals(this.filePath.toString())) {
            stringBuilder.append("name=").append(this.getName()).append(", ");
        }
        stringBuilder.append("path=").append(this.filePath);
        if (this.getLanguage() != null) {
            stringBuilder.append(", language=").append(this.getLanguage());
        }
        stringBuilder.append(", isWatched=").append(this.isWatched);
        return stringBuilder.toString();
    }
}

