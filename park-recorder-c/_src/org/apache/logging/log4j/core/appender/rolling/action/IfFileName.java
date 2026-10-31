/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling.action;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.rolling.action.IfAll;
import org.apache.logging.log4j.core.appender.rolling.action.PathCondition;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="IfFileName", category="Core", printObject=true)
public final class IfFileName
implements PathCondition {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final PathMatcher pathMatcher;
    private final String syntaxAndPattern;
    private final PathCondition[] nestedConditions;

    private IfFileName(String string, String string2, PathCondition[] pathConditionArray) {
        if (string2 == null && string == null) {
            throw new IllegalArgumentException("Specify either a path glob or a regular expression. Both cannot be null.");
        }
        this.syntaxAndPattern = IfFileName.createSyntaxAndPatternString(string, string2);
        this.pathMatcher = FileSystems.getDefault().getPathMatcher(this.syntaxAndPattern);
        this.nestedConditions = pathConditionArray == null ? new PathCondition[]{} : Arrays.copyOf(pathConditionArray, pathConditionArray.length);
    }

    static String createSyntaxAndPatternString(String string, String string2) {
        if (string != null) {
            if (string.startsWith("glob:")) {
                return string;
            }
            return "glob:".concat(String.valueOf(string));
        }
        if (string2.startsWith("regex:")) {
            return string2;
        }
        return "regex:".concat(String.valueOf(string2));
    }

    public final String getSyntaxAndPattern() {
        return this.syntaxAndPattern;
    }

    public final List<PathCondition> getNestedConditions() {
        return Collections.unmodifiableList(Arrays.asList(this.nestedConditions));
    }

    @Override
    public final boolean accept(Path path, Path path2, BasicFileAttributes basicFileAttributes) {
        boolean bl = this.pathMatcher.matches(path2);
        String string = bl ? "matches" : "does not match";
        String string2 = bl ? "ACCEPTED" : "REJECTED";
        LOGGER.trace("IfFileName {}: '{}' {} relative path '{}'", (Object)string2, (Object)this.syntaxAndPattern, (Object)string, (Object)path2);
        if (bl) {
            return IfAll.accept(this.nestedConditions, path, path2, basicFileAttributes);
        }
        return bl;
    }

    @Override
    public final void beforeFileTreeWalk() {
        IfAll.beforeFileTreeWalk(this.nestedConditions);
    }

    @PluginFactory
    public static IfFileName createNameCondition(@PluginAttribute(value="glob") String string, @PluginAttribute(value="regex") String string2, PathCondition ... pathConditionArray) {
        return new IfFileName(string, string2, pathConditionArray);
    }

    public final String toString() {
        String string = this.nestedConditions.length == 0 ? "" : " AND " + Arrays.toString(this.nestedConditions);
        return "IfFileName(" + this.syntaxAndPattern + string + ")";
    }
}

