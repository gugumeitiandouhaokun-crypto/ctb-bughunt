/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling.action;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.rolling.action.PathCondition;
import org.apache.logging.log4j.status.StatusLogger;

public class DeletingVisitor
extends SimpleFileVisitor<Path> {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final Path basePath;
    private final boolean testMode;
    private final List<? extends PathCondition> pathConditions;

    public DeletingVisitor(Path object, List<? extends PathCondition> list, boolean bl) {
        this.testMode = bl;
        this.basePath = Objects.requireNonNull(object, "basePath");
        this.pathConditions = Objects.requireNonNull(list, "pathConditions");
        object = list.iterator();
        while (object.hasNext()) {
            ((PathCondition)object.next()).beforeFileTreeWalk();
        }
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) {
        for (PathCondition pathCondition : this.pathConditions) {
            Path path2;
            if (pathCondition.accept(this.basePath, path2 = this.basePath.relativize(path), basicFileAttributes)) continue;
            LOGGER.trace("Not deleting base={}, relative={}", (Object)this.basePath, (Object)path2);
            return FileVisitResult.CONTINUE;
        }
        if (this.isTestMode()) {
            LOGGER.info("Deleting {} (TEST MODE: file not actually deleted)", (Object)path);
        } else {
            this.delete(path);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException iOException) {
        if (iOException instanceof NoSuchFileException) {
            LOGGER.info("File {} could not be accessed, it has likely already been deleted", (Object)path, (Object)iOException);
            return FileVisitResult.CONTINUE;
        }
        return super.visitFileFailed(path, iOException);
    }

    protected void delete(Path path) {
        LOGGER.trace("Deleting {}", (Object)path);
        Files.deleteIfExists(path);
    }

    public boolean isTestMode() {
        return this.testMode;
    }
}

