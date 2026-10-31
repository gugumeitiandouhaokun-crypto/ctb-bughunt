/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling.action;

import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.core.appender.rolling.action.AbstractPathAction;
import org.apache.logging.log4j.core.appender.rolling.action.DeletingVisitor;
import org.apache.logging.log4j.core.appender.rolling.action.PathCondition;
import org.apache.logging.log4j.core.appender.rolling.action.PathSortByModificationTime;
import org.apache.logging.log4j.core.appender.rolling.action.PathSorter;
import org.apache.logging.log4j.core.appender.rolling.action.PathWithAttributes;
import org.apache.logging.log4j.core.appender.rolling.action.ScriptCondition;
import org.apache.logging.log4j.core.appender.rolling.action.SortingVisitor;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

@Plugin(name="Delete", category="Core", printObject=true)
public class DeleteAction
extends AbstractPathAction {
    private final PathSorter pathSorter;
    private final boolean testMode;
    private final ScriptCondition scriptCondition;

    DeleteAction(String string, boolean bl, int n, boolean bl2, PathSorter pathSorter, PathCondition[] pathConditionArray, ScriptCondition scriptCondition, StrSubstitutor strSubstitutor) {
        super(string, bl, n, pathConditionArray, strSubstitutor);
        this.testMode = bl2;
        this.pathSorter = Objects.requireNonNull(pathSorter, "sorter");
        this.scriptCondition = scriptCondition;
        if (scriptCondition == null && (pathConditionArray == null || pathConditionArray.length == 0)) {
            LOGGER.error("Missing Delete conditions: unconditional Delete not supported");
            throw new IllegalArgumentException("Unconditional Delete not supported");
        }
    }

    @Override
    public boolean execute() {
        if (this.scriptCondition != null) {
            return this.executeScript();
        }
        return super.execute();
    }

    private boolean executeScript() {
        List<PathWithAttributes> list = this.callScript();
        if (list == null) {
            LOGGER.trace("Script returned null list (no files to delete)");
            return true;
        }
        this.deleteSelectedFiles(list);
        return true;
    }

    private List<PathWithAttributes> callScript() {
        List<PathWithAttributes> list = this.getSortedPaths();
        this.trace("Sorted paths:", list);
        return this.scriptCondition.selectFilesToDelete(this.getBasePath(), list);
    }

    private void deleteSelectedFiles(List<PathWithAttributes> object) {
        this.trace("Paths the script selected for deletion:", (List<PathWithAttributes>)object);
        object = object.iterator();
        while (object.hasNext()) {
            Object object2 = (PathWithAttributes)object.next();
            Object object3 = object2 = object2 == null ? null : ((PathWithAttributes)object2).getPath();
            if (this.isTestMode()) {
                LOGGER.info("Deleting {} (TEST MODE: file not actually deleted)", object2);
                continue;
            }
            this.delete((Path)object2);
        }
    }

    protected void delete(Path path) {
        LOGGER.trace("Deleting {}", (Object)path);
        Files.deleteIfExists(path);
    }

    @Override
    public boolean execute(FileVisitor<Path> fileVisitor) {
        Object object = this.getSortedPaths();
        this.trace("Sorted paths:", (List<PathWithAttributes>)object);
        object = object.iterator();
        while (object.hasNext()) {
            PathWithAttributes pathWithAttributes = (PathWithAttributes)object.next();
            try {
                fileVisitor.visitFile(pathWithAttributes.getPath(), pathWithAttributes.getAttributes());
            }
            catch (IOException iOException) {
                LOGGER.error("Error in post-rollover Delete when visiting {}", (Object)pathWithAttributes.getPath(), (Object)iOException);
                fileVisitor.visitFileFailed(pathWithAttributes.getPath(), iOException);
            }
        }
        return true;
    }

    private void trace(String object, List<PathWithAttributes> object2) {
        LOGGER.trace((String)object);
        object = object2.iterator();
        while (object.hasNext()) {
            object2 = (PathWithAttributes)object.next();
            LOGGER.trace(object2);
        }
    }

    List<PathWithAttributes> getSortedPaths() {
        SortingVisitor sortingVisitor = new SortingVisitor(this.pathSorter);
        super.execute(sortingVisitor);
        return sortingVisitor.getSortedPaths();
    }

    public boolean isTestMode() {
        return this.testMode;
    }

    @Override
    protected FileVisitor<Path> createFileVisitor(Path path, List<PathCondition> list) {
        return new DeletingVisitor(path, list, this.testMode);
    }

    @PluginFactory
    public static DeleteAction createDeleteAction(@PluginAttribute(value="basePath") String string, @PluginAttribute(value="followLinks") boolean bl, @PluginAttribute(value="maxDepth", defaultInt=1) int n, @PluginAttribute(value="testMode") boolean bl2, @PluginElement(value="PathSorter") PathSorter pathSorter, @PluginElement(value="PathConditions") PathCondition[] pathConditionArray, @PluginElement(value="ScriptCondition") ScriptCondition scriptCondition, @PluginConfiguration Configuration configuration) {
        pathSorter = pathSorter == null ? new PathSortByModificationTime(true) : pathSorter;
        return new DeleteAction(string, bl, n, bl2, pathSorter, pathConditionArray, scriptCondition, configuration.getStrSubstitutor());
    }
}

