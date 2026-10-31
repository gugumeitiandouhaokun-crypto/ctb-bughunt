/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.appender.rolling.AbstractRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.DirectFileRolloverStrategy;
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

@Plugin(name="DirectWriteRolloverStrategy", category="Core", printObject=true)
public class DirectWriteRolloverStrategy
extends AbstractRolloverStrategy
implements DirectFileRolloverStrategy {
    private static final int DEFAULT_MAX_FILES = 7;
    private final int maxFiles;
    private final int compressionLevel;
    private final List<Action> customActions;
    private final boolean stopCustomActionsOnError;
    private volatile String currentFileName;
    private int nextIndex = -1;
    private final PatternProcessor tempCompressedFilePattern;
    private volatile boolean usePrevTime = false;

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    @Deprecated
    @PluginFactory
    public static DirectWriteRolloverStrategy createStrategy(@PluginAttribute(value="maxFiles") String string, @PluginAttribute(value="compressionLevel") String string2, @PluginElement(value="Actions") Action[] actionArray, @PluginAttribute(value="stopCustomActionsOnError", defaultBoolean=true) boolean bl, @PluginConfiguration Configuration configuration) {
        return DirectWriteRolloverStrategy.newBuilder().withMaxFiles(string).withCompressionLevelStr(string2).withCustomActions(actionArray).withStopCustomActionsOnError(bl).withConfig(configuration).build();
    }

    @Deprecated
    protected DirectWriteRolloverStrategy(int n, int n2, StrSubstitutor strSubstitutor, Action[] actionArray, boolean bl) {
        this(n, n2, strSubstitutor, actionArray, bl, null);
    }

    protected DirectWriteRolloverStrategy(int n, int n2, StrSubstitutor strSubstitutor, Action[] actionArray, boolean bl, String string) {
        super(strSubstitutor);
        this.maxFiles = n;
        this.compressionLevel = n2;
        this.stopCustomActionsOnError = bl;
        this.customActions = actionArray == null ? Collections.emptyList() : Arrays.asList(actionArray);
        this.tempCompressedFilePattern = string != null ? new PatternProcessor(string) : null;
    }

    public int getCompressionLevel() {
        return this.compressionLevel;
    }

    public List<Action> getCustomActions() {
        return this.customActions;
    }

    public int getMaxFiles() {
        return this.maxFiles;
    }

    public boolean isStopCustomActionsOnError() {
        return this.stopCustomActionsOnError;
    }

    public PatternProcessor getTempCompressedFilePattern() {
        return this.tempCompressedFilePattern;
    }

    private int purge(RollingFileManager object) {
        object = this.getEligibleFiles((RollingFileManager)object);
        LOGGER.debug("Found {} eligible files, max is  {}", (Object)object.size(), (Object)this.maxFiles);
        while (object.size() >= this.maxFiles) {
            try {
                Integer n = (Integer)object.firstKey();
                Files.delete((Path)object.get(n));
                object.remove(n);
            }
            catch (IOException iOException) {
                LOGGER.error("Unable to delete {}", object.firstKey(), (Object)iOException);
                break;
            }
        }
        if (object.size() > 0) {
            return (Integer)object.lastKey();
        }
        return 1;
    }

    @Override
    public String getCurrentFileName(RollingFileManager rollingFileManager) {
        if (this.currentFileName == null) {
            String string;
            SortedMap<Integer, Path> sortedMap = this.getEligibleFiles(rollingFileManager);
            int n = sortedMap.size() > 0 ? (this.nextIndex > 0 ? this.nextIndex : sortedMap.size()) : 1;
            StringBuilder stringBuilder = new StringBuilder(255);
            rollingFileManager.getPatternProcessor().formatFileName(this.strSubstitutor, stringBuilder, true, (Object)n);
            int n2 = this.suffixLength(stringBuilder.toString());
            this.currentFileName = string = n2 > 0 ? stringBuilder.substring(0, stringBuilder.length() - n2) : stringBuilder.toString();
        }
        return this.currentFileName;
    }

    @Override
    public void clearCurrentFileName() {
        this.currentFileName = null;
    }

    @Override
    public RolloverDescription rollover(RollingFileManager rollingFileManager) {
        Object object;
        String string;
        LOGGER.debug("Rolling " + this.currentFileName);
        if (this.maxFiles < 0) {
            return null;
        }
        long l = System.nanoTime();
        int n = this.purge(rollingFileManager);
        if (LOGGER.isTraceEnabled()) {
            double d2 = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - l);
            LOGGER.trace("DirectWriteRolloverStrategy.purge() took {} milliseconds", (Object)d2);
        }
        Object object2 = null;
        String string2 = string = this.getCurrentFileName(rollingFileManager);
        this.currentFileName = null;
        this.nextIndex = n + 1;
        FileExtension fileExtension = rollingFileManager.getFileExtension();
        if (fileExtension != null) {
            string2 = string2 + fileExtension.getExtension();
            if (this.tempCompressedFilePattern != null) {
                object = new StringBuilder();
                this.tempCompressedFilePattern.formatFileName(this.strSubstitutor, (StringBuilder)object, (Object)n);
                String string3 = ((StringBuilder)object).toString();
                object2 = new File(string3);
                object = ((File)object2).getParentFile();
                if (object != null) {
                    ((File)object).mkdirs();
                }
                object2 = new CompositeAction(Arrays.asList(fileExtension.createCompressAction(string, string3, true, this.compressionLevel), new FileRenameAction((File)object2, new File(string2), true)), true);
            } else {
                object2 = fileExtension.createCompressAction(string, string2, true, this.compressionLevel);
            }
        }
        if (object2 != null && rollingFileManager.isAttributeViewEnabled()) {
            object = PosixViewAttributeAction.newBuilder().withBasePath(string2).withFollowLinks(false).withMaxDepth(1).withPathConditions(new PathCondition[0]).withSubst(this.getStrSubstitutor()).withFilePermissions(rollingFileManager.getFilePermissions()).withFileOwner(rollingFileManager.getFileOwner()).withFileGroup(rollingFileManager.getFileGroup()).build();
            object2 = new CompositeAction(Arrays.asList(object2, object), false);
        }
        object = this.merge((Action)object2, this.customActions, this.stopCustomActionsOnError);
        return new RolloverDescriptionImpl(string, false, null, (Action)object);
    }

    public String toString() {
        return "DirectWriteRolloverStrategy(maxFiles=" + this.maxFiles + ')';
    }

    public static class Builder
    implements org.apache.logging.log4j.core.util.Builder<DirectWriteRolloverStrategy> {
        @PluginBuilderAttribute(value="maxFiles")
        private String maxFiles;
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
        public DirectWriteRolloverStrategy build() {
            int n = Integer.MAX_VALUE;
            if (this.maxFiles != null) {
                n = Integer.parseInt(this.maxFiles);
                if (n < 0) {
                    n = Integer.MAX_VALUE;
                } else if (n < 2) {
                    AbstractRolloverStrategy.LOGGER.error("Maximum files too small. Limited to 7");
                    n = 7;
                }
            }
            int n2 = Integers.parseInt(this.compressionLevelStr, -1);
            return new DirectWriteRolloverStrategy(n, n2, this.config.getStrSubstitutor(), this.customActions, this.stopCustomActionsOnError, this.tempCompressedFilePattern);
        }

        public String getMaxFiles() {
            return this.maxFiles;
        }

        public Builder withMaxFiles(String string) {
            this.maxFiles = string;
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

