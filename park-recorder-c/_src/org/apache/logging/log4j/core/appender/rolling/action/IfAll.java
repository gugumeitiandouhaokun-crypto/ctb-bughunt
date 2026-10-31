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

@Plugin(name="IfAll", category="Core", printObject=true)
public final class IfAll
implements PathCondition {
    private final PathCondition[] components;

    private IfAll(PathCondition ... pathConditionArray) {
        this.components = Objects.requireNonNull(pathConditionArray, "filters");
    }

    public final PathCondition[] getDeleteFilters() {
        return this.components;
    }

    @Override
    public final boolean accept(Path path, Path path2, BasicFileAttributes basicFileAttributes) {
        if (this.components == null || this.components.length == 0) {
            return false;
        }
        return IfAll.accept(this.components, path, path2, basicFileAttributes);
    }

    public static boolean accept(PathCondition[] pathConditionArray, Path path, Path path2, BasicFileAttributes basicFileAttributes) {
        int n = pathConditionArray.length;
        for (int i2 = 0; i2 < n; ++i2) {
            if (pathConditionArray[i2].accept(path, path2, basicFileAttributes)) continue;
            return false;
        }
        return true;
    }

    @Override
    public final void beforeFileTreeWalk() {
        IfAll.beforeFileTreeWalk(this.components);
    }

    public static void beforeFileTreeWalk(PathCondition[] pathConditionArray) {
        int n = pathConditionArray.length;
        for (int i2 = 0; i2 < n; ++i2) {
            pathConditionArray[i2].beforeFileTreeWalk();
        }
    }

    @PluginFactory
    public static IfAll createAndCondition(PathCondition ... pathConditionArray) {
        return new IfAll(pathConditionArray);
    }

    public final String toString() {
        return "IfAll" + Arrays.toString(this.components);
    }
}

