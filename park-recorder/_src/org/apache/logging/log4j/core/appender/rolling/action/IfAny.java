/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling.action;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Objects;
import org.apache.logging.log4j.core.appender.rolling.action.PathCondition;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name="IfAny", category="Core", printObject=true)
public final class IfAny
implements PathCondition {
    private final PathCondition[] components;

    private IfAny(PathCondition ... pathConditionArray) {
        this.components = Objects.requireNonNull(pathConditionArray, "filters");
    }

    public final PathCondition[] getDeleteFilters() {
        return this.components;
    }

    @Override
    public final boolean accept(Path path, Path path2, BasicFileAttributes basicFileAttributes) {
        PathCondition[] pathConditionArray = this.components;
        int n = this.components.length;
        for (int j = 0; j < n; ++j) {
            if (!pathConditionArray[j].accept(path, path2, basicFileAttributes)) continue;
            return true;
        }
        return false;
    }

    @Override
    public final void beforeFileTreeWalk() {
        PathCondition[] pathConditionArray = this.components;
        int n = this.components.length;
        for (int j = 0; j < n; ++j) {
            pathConditionArray[j].beforeFileTreeWalk();
        }
    }

    @PluginFactory
    public static IfAny createOrCondition(PathCondition ... pathConditionArray) {
        return new IfAny(pathConditionArray);
    }

    public final String toString() {
        return "IfAny" + Arrays.toString(this.components);
    }
}

