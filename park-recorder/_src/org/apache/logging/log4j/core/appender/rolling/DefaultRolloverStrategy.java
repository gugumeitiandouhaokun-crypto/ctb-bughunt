/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.appender.rolling.AbstractRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.FileExtension;
import org.apache.logging.log4j.core.appender.rolling.PatternProcessor;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverDescription;
import org.apache.logging.log4j.core.appender.rolling.RolloverDescriptionImpl;
import org.apache.logging.log4j.core.appender.rolling.action.Action;
import org.apache.logging.log4j.core.appender.rolling.action.CompositeAction;
import org.apache.logging.log4j.core.appender.rolling.action.FileRenameAction;
import org.apache.logging.log4j.core.appender.rolling.action.PathCondition;
import org.apache.logging.log4j.core.appender.rolling.action.PosixViewAttributeAction;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.util.Integers;

@Plugin(name="DefaultRolloverStrategy", category="Core", printObject=true)
public class DefaultRolloverStrategy
extends AbstractRolloverStrategy {
    private static final int MIN_WINDOW_SIZE = 1;
    private static final int DEFAULT_WINDOW_SIZE = 7;
    private final int maxIndex;
    private final int minIndex;
    private final boolean useMax;
    private final int compressionLevel;
    private final List<Action> customActions;
    private final boolean stopCustomActionsOnError;
    private final PatternProcessor tempCompressedFilePattern;

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    @PluginFactory
    @Deprecated
    public static DefaultRolloverStrategy createStrategy(@PluginAttribute(value="max") String string, @PluginAttribute(value="min") String string2, @PluginAttribute(value="fileIndex") String string3, @PluginAttribute(value="compressionLevel") String string4, @PluginElement(value="Actions") Action[] actionArray, @PluginAttribute(value="stopCustomActionsOnError", defaultBoolean=true) boolean bl, @PluginConfiguration Configuration configuration) {
        return DefaultRolloverStrategy.newBuilder().withMin(string2).withMax(string).withFileIndex(string3).withCompressionLevelStr(string4).withCustomActions(actionArray).withStopCustomActionsOnError(bl).withConfig(configuration).build();
    }

    @Deprecated
    protected DefaultRolloverStrategy(int n, int n2, boolean bl, int n3, StrSubstitutor strSubstitutor, Action[] actionArray, boolean bl2) {
        this(n, n2, bl, n3, strSubstitutor, actionArray, bl2, null);
    }

    protected DefaultRolloverStrategy(int n, int n2, boolean bl, int n3, StrSubstitutor strSubstitutor, Action[] actionArray, boolean bl2, String string) {
        super(strSubstitutor);
        this.minIndex = n;
        this.maxIndex = n2;
        this.useMax = bl;
        this.compressionLevel = n3;
        this.stopCustomActionsOnError = bl2;
        this.customActions = actionArray == null ? Collections.emptyList() : Arrays.asList(actionArray);
        this.tempCompressedFilePattern = string != null ? new PatternProcessor(string) : null;
    }

    public int getCompressionLevel() {
        return this.compressionLevel;
    }

    public List<Action> getCustomActions() {
        return this.customActions;
    }

    public int getMaxIndex() {
        return this.maxIndex;
    }

    public int getMinIndex() {
        return this.minIndex;
    }

    public boolean isStopCustomActionsOnError() {
        return this.stopCustomActionsOnError;
    }

    public boolean isUseMax() {
        return this.useMax;
    }

    public PatternProcessor getTempCompressedFilePattern() {
        return this.tempCompressedFilePattern;
    }

    private int purge(int n, int n2, RollingFileManager rollingFileManager) {
        if (this.useMax) {
            return this.purgeAscending(n, n2, rollingFileManager);
        }
        return this.purgeDescending(n, n2, rollingFileManager);
    }

    private int purgeAscending(int n, int n2, RollingFileManager rollingFileManager) {
        boolean object;
        Serializable serializable;
        boolean bl;
        SortedMap<Integer, Path> sortedMap = this.getEligibleFiles(rollingFileManager);
        int n3 = n2 - n + 1;
        boolean bl2 = bl = !sortedMap.isEmpty() && sortedMap.lastKey() >= this.maxIndex;
        while (sortedMap.size() >= n3) {
            try {
                LOGGER.debug("Eligible files: {}", (Object)sortedMap);
                serializable = sortedMap.firstKey();
                LOGGER.debug("Deleting {}", (Object)((Path)sortedMap.get(serializable)).toFile().getAbsolutePath());
                Files.delete((Path)sortedMap.get(serializable));
                sortedMap.remove(serializable);
                object = true;
            }
            catch (IOException iOException) {
                LOGGER.error("Unable to delete {}, {}", (Object)sortedMap.firstKey(), (Object)iOException.getMessage(), (Object)iOException);
                break;
            }
        }
        serializable = new StringBuilder();
        if (object) {
            for (Map.Entry<Integer, Path> entry : sortedMap.entrySet()) {
                ((StringBuilder)serializable).setLength(0);
                rollingFileManager.getPatternProcessor().formatFileName(this.strSubstitutor, (StringBuilder)serializable, (Object)(entry.getKey() - 1));
                String string = entry.getValue().toFile().getName();
                String string2 = ((StringBuilder)serializable).toString();
                int n4 = this.suffixLength(string2);
                if (n4 > 0 && this.suffixLength(string) == 0) {
                    string2 = string2.substring(0, string2.length() - n4);
                }
                FileRenameAction fileRenameAction = new FileRenameAction(entry.getValue().toFile(), new File(string2), true);
                try {
                    LOGGER.debug("DefaultRolloverStrategy.purgeAscending executing {}", (Object)fileRenameAction);
                    if (fileRenameAction.execute()) continue;
                    return -1;
                }
                catch (Exception exception) {
                    LOGGER.warn("Exception during purge in RollingFileAppender", (Throwable)exception);
                    return -1;
                }
            }
        }
        if (sortedMap.size() > 0) {
            if (sortedMap.lastKey() < n2) {
                return sortedMap.lastKey() + 1;
            }
            return n2;
        }
        return n;
    }

    private int purgeDescending(int n, int n2, RollingFileManager rollingFileManager) {
        Serializable serializable;
        SortedMap<Integer, Path> object2 = this.getEligibleFiles(rollingFileManager, false);
        n2 = n2 - n + 1;
        LOGGER.debug("Eligible files: {}", (Object)object2);
        while (object2.size() >= n2) {
            try {
                serializable = object2.firstKey();
                Files.delete((Path)object2.get(serializable));
                object2.remove(serializable);
            }
            catch (IOException iOException) {
                LOGGER.error("Unable to delete {}, {}", (Object)object2.firstKey(), (Object)iOException.getMessage(), (Object)iOException);
                break;
            }
        }
        serializable = new StringBuilder();
        for (Map.Entry<Integer, Path> entry : object2.entrySet()) {
            ((StringBuilder)serializable).setLength(0);
            rollingFileManager.getPatternProcessor().formatFileName(this.strSubstitutor, (StringBuilder)serializable, (Object)(entry.getKey() + 1));
            String string = entry.getValue().toFile().getName();
            String string2 = ((StringBuilder)serializable).toString();
            int n3 = this.suffixLength(string2);
            if (n3 > 0 && this.suffixLength(string) == 0) {
                string2 = string2.substring(0, string2.length() - n3);
            }
            FileRenameAction fileRenameAction = new FileRenameAction(entry.getValue().toFile(), new File(string2), true);
            try {
                LOGGER.debug("DefaultRolloverStrategy.purgeDescending executing {}", (Object)fileRenameAction);
                if (fileRenameAction.execute()) continue;
                return -1;
            }
            catch (Exception exception) {
                LOGGER.warn("Exception during purge in RollingFileAppender", (Throwable)exception);
                return -1;
            }
        }
        return n;
    }

    @Override
    public RolloverDescription rollover(RollingFileManager rollingFileManager) {
        Object object;
        String string;
        int n;
        SortedMap<Integer, Path> sortedMap;
        Serializable serializable = new StringBuilder(255);
        if (this.minIndex == Integer.MIN_VALUE) {
            sortedMap = this.getEligibleFiles(rollingFileManager);
            n = sortedMap.size() > 0 ? sortedMap.lastKey() + 1 : 1;
            rollingFileManager.getPatternProcessor().formatFileName(this.strSubstitutor, (StringBuilder)serializable, (Object)n);
        } else {
            if (this.maxIndex < 0) {
                return null;
            }
            long l = System.nanoTime();
            DefaultRolloverStrategy defaultRolloverStrategy = this;
            n = defaultRolloverStrategy.purge(defaultRolloverStrategy.minIndex, this.maxIndex, rollingFileManager);
            if (n < 0) {
                return null;
            }
            rollingFileManager.getPatternProcessor().formatFileName(this.strSubstitutor, (StringBuilder)serializable, (Object)n);
            if (LOGGER.isTraceEnabled()) {
                double d2 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - l);
                LOGGER.trace("DefaultRolloverStrategy.purge() took {} milliseconds", (Object)d2);
            }
        }
        sortedMap = rollingFileManager.getFileName();
        String string2 = string = serializable.toString();
        Object object2 = null;
        FileExtension fileExtension = rollingFileManager.getFileExtension();
        if (fileExtension != null) {
            object = new File(string);
            string = string.substring(0, string.length() - fileExtension.length());
            if (this.tempCompressedFilePattern != null) {
                serializable.delete(0, serializable.length());
                this.tempCompressedFilePattern.formatFileName(this.strSubstitutor, (StringBuilder)serializable, (Object)n);
                String string3 = serializable.toString();
                serializable = new File(string3);
                object2 = ((File)serializable).getParentFile();
                if (object2 != null) {
                    ((File)object2).mkdirs();
                }
                object2 = new CompositeAction(Arrays.asList(fileExtension.createCompressAction(string, string3, true, this.compressionLevel), new FileRenameAction((File)serializable, (File)object, true)), true);
            } else {
                object2 = fileExtension.createCompressAction(string, string2, true, this.compressionLevel);
            }
        }
        if (((String)((Object)sortedMap)).equals(string)) {
            LOGGER.warn("Attempt to rename file {} to itself will be ignored", (Object)sortedMap);
            return new RolloverDescriptionImpl((String)((Object)sortedMap), false, null, null);
        }
        if (object2 != null && rollingFileManager.isAttributeViewEnabled()) {
            object = PosixViewAttributeAction.newBuilder().withBasePath(string2).withFollowLinks(false).withMaxDepth(1).withPathConditions(new PathCondition[0]).withSubst(this.getStrSubstitutor()).withFilePermissions(rollingFileManager.getFilePermissions()).withFileOwner(rollingFileManager.getFileOwner()).withFileGroup(rollingFileManager.getFileGroup()).build();
            object2 = new CompositeAction(Arrays.asList(object2, object), false);
        }
        object = new FileRenameAction(new File((String)((Object)sortedMap)), new File(string), rollingFileManager.isRenameEmptyFiles());
        Action action = this.merge((Action)object2, this.customActions, this.stopCustomActionsOnError);
        return new RolloverDescriptionImpl((String)((Object)sortedMap), false, (Action)object, action);
    }

    public String toString() {
        return "DefaultRolloverStrategy(min=" + this.minIndex + ", max=" + this.maxIndex + ", useMax=" + this.useMax + ")";
    }

    public static class Builder
    implements org.apache.logging.log4j.core.util.Builder<DefaultRolloverStrategy> {
        @PluginBuilderAttribute(value="max")
        private String max;
        @PluginBuilderAttribute(value="min")
        private String min;
        @PluginBuilderAttribute(value="fileIndex")
        private String fileIndex;
        @PluginBuilderAttribute(value="compressionLevel")
        private String compressionLevelStr;
        @PluginElement(value="Actions")
        private Action[] customActions;
        @PluginBuilderAttribute(value="stopCustomActionsOnError")
        private boolean stopCustomActionsOnError = true;
        @PluginBuilderAttribute(value="tempCompressedFilePattern")
        private String tempCompressedFilePattern;
        @PluginConfiguration
        private Configuration config;

        @Override
        public DefaultRolloverStrategy build() {
            boolean bl;
            int n;
            int n2;
            if (this.fileIndex != null && this.fileIndex.equalsIgnoreCase("nomax")) {
                n2 = Integer.MIN_VALUE;
                n = Integer.MAX_VALUE;
                bl = false;
            } else {
                bl = this.fileIndex == null ? true : this.fileIndex.equalsIgnoreCase("max");
                n2 = 1;
                if (this.min != null && (n2 = Integer.parseInt(this.min)) <= 0) {
                    AbstractRolloverStrategy.LOGGER.error("Minimum window size too small. Limited to 1");
                    n2 = 1;
                }
                n = 7;
                if (this.max != null && (n = Integer.parseInt(this.max)) < n2) {
                    n = n2 < 7 ? 7 : n2;
                    AbstractRolloverStrategy.LOGGER.error("Maximum window size must be greater than the minimum windows size. Set to ".concat(String.valueOf(n)));
                }
            }
            int n3 = Integers.parseInt(this.compressionLevelStr, -1);
            StrSubstitutor strSubstitutor = this.config != null ? this.config.getStrSubstitutor() : new StrSubstitutor();
            return new DefaultRolloverStrategy(n2, n, bl, n3, strSubstitutor, this.customActions, this.stopCustomActionsOnError, this.tempCompressedFilePattern);
        }

        public String getMax() {
            return this.max;
        }

        public Builder withMax(String string) {
            this.max = string;
            return this;
        }

        public String getMin() {
            return this.min;
        }

        public Builder withMin(String string) {
            this.min = string;
            return this;
        }

        public String getFileIndex() {
            return this.fileIndex;
        }

        public Builder withFileIndex(String string) {
            this.fileIndex = string;
            return this;
        }

        public String getCompressionLevelStr() {
            return this.compressionLevelStr;
        }

        public Builder withCompressionLevelStr(String string) {
            this.compressionLevelStr = string;
            return this;
        }

        public Action[] getCustomActions() {
            return this.customActions;
        }

        public Builder withCustomActions(Action[] actionArray) {
            this.customActions = actionArray;
            return this;
        }

        public boolean isStopCustomActionsOnError() {
            return this.stopCustomActionsOnError;
        }

        public Builder withStopCustomActionsOnError(boolean bl) {
            this.stopCustomActionsOnError = bl;
            return this;
        }

        public String getTempCompressedFilePattern() {
            return this.tempCompressedFilePattern;
        }

        public Builder withTempCompressedFilePattern(String string) {
            this.tempCompressedFilePattern = string;
            return this;
        }

        public Configuration getConfig() {
            return this.config;
        }

        public Builder withConfig(Configuration configuration) {
            this.config = configuration;
            return this;
        }
    }
}

