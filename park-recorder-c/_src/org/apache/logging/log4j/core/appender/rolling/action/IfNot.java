/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling.action;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import org.apache.logging.log4j.core.appender.rolling.action.PathCondition;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name="IfNot", category="Core", printObject=true)
public final class IfNot
implements PathCondition {
    private final PathCondition negate;

    private IfNot(PathCondition pathCondition) {
        this.negate = Objects.requireNonNull(pathCondition, "filter");
    }

    public final PathCondition getWrappedFilter() {
        return this.negate;
    }

    @Override
    public final boolean accept(Path path, Path path2, BasicFileAttributes basicFileAttributes) {
        return !this.negate.accept(path, path2, basicFileAttributes);
    }

    @Override
    public final void beforeFileTreeWalk() {
        this.negate.beforeFileTreeWalk();
    }

    @PluginFactory
    public static IfNot createNotCondition(@PluginElement(value="PathConditions") PathCondition pathCondition) {
        return new IfNot(pathCondition);
    }

    public final String toString() {
        return "IfNot(" + this.negate + ")";
    }
}

