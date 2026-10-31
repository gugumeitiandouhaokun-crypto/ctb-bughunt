/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LoggingException;
import org.apache.logging.log4j.core.appender.rolling.FileExtension;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.action.Action;
import org.apache.logging.log4j.core.appender.rolling.action.CompositeAction;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.pattern.NotANumber;
import org.apache.logging.log4j.status.StatusLogger;

public abstract class AbstractRolloverStrategy
implements RolloverStrategy {
    protected static final Logger LOGGER = StatusLogger.getLogger();
    public static final Pattern PATTERN_COUNTER = Pattern.compile(".*%((?<ZEROPAD>0)?(?<PADDING>\\d+))?i.*");
    protected final StrSubstitutor strSubstitutor;

    protected AbstractRolloverStrategy(StrSubstitutor strSubstitutor) {
        this.strSubstitutor = strSubstitutor;
    }

    public StrSubstitutor getStrSubstitutor() {
        return this.strSubstitutor;
    }

    protected Action merge(Action action, List<Action> list, boolean bl) {
        if (list.isEmpty()) {
            return action;
        }
        if (action == null) {
            return new CompositeAction(list, bl);
        }
        ArrayList<Action> arrayList = new ArrayList<Action>();
        arrayList.add(action);
        arrayList.addAll(list);
        return new CompositeAction(arrayList, bl);
    }

    protected int suffixLength(String string) {
        for (FileExtension fileExtension : FileExtension.values()) {
            if (!fileExtension.isExtensionFor(string)) continue;
            return fileExtension.length();
        }
        return 0;
    }

    protected SortedMap<Integer, Path> getEligibleFiles(RollingFileManager rollingFileManager) {
        return this.getEligibleFiles(rollingFileManager, true);
    }

    protected SortedMap<Integer, Path> getEligibleFiles(RollingFileManager object, boolean bl) {
        StringBuilder stringBuilder = new StringBuilder();
        String string = ((RollingFileManager)object).getPatternProcessor().getPattern();
        ((RollingFileManager)object).getPatternProcessor().formatFileName(this.strSubstitutor, stringBuilder, (Object)NotANumber.NAN);
        object = ((RollingFileManager)object).isDirectWrite() ? "" : ((RollingFileManager)object).getFileName();
        return this.getEligibleFiles((String)object, stringBuilder.toString(), string, bl);
    }

    protected SortedMap<Integer, Path> getEligibleFiles(String string, String string2) {
        return this.getEligibleFiles("", string, string2, true);
    }

    @Deprecated
    protected SortedMap<Integer, Path> getEligibleFiles(String string, String string2, boolean bl) {
        return this.getEligibleFiles("", string, string2, bl);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected SortedMap<Integer, Path> getEligibleFiles(String directoryStream, String object, String object2, boolean bl) {
        TreeMap<Integer, Path> treeMap;
        block16: {
            treeMap = new TreeMap<Integer, Path>();
            File file = ((File)(object = new File((String)object))).getParentFile();
            if (file == null) {
                file = new File(".");
            } else {
                file.mkdirs();
            }
            if (!PATTERN_COUNTER.matcher((CharSequence)object2).matches()) {
                return treeMap;
            }
            object2 = file.toPath();
            int n = this.suffixLength((String)(object = ((File)object).getName()));
            if (n > 0) {
                object = ((String)object).substring(0, ((String)object).length() - n) + ".*";
            }
            object = Pattern.compile(((String)object).replaceFirst("0?\\u0000", "(0?\\\\d+)"));
            Path path = ((String)((Object)directoryStream)).length() > 0 ? new File((String)((Object)directoryStream)).toPath() : null;
            LOGGER.debug("Current file: {}", (Object)directoryStream);
            try {
                directoryStream = Files.newDirectoryStream((Path)object2);
                Throwable throwable = null;
                try {
                    for (Path path2 : directoryStream) {
                        Object object3 = ((Pattern)object).matcher(path2.toFile().getName());
                        if (!((Matcher)object3).matches() || path2.equals(path)) continue;
                        try {
                            object3 = Integer.parseInt(((Matcher)object3).group(1));
                            treeMap.put((Integer)object3, path2);
                        }
                        catch (NumberFormatException numberFormatException) {
                            LOGGER.debug("Ignoring file {} which matches pattern but the index is invalid.", (Object)path2.toFile().getName());
                        }
                    }
                    if (directoryStream == null) break block16;
                }
                catch (Throwable throwable2) {
                    try {
                        Throwable throwable3 = throwable2;
                        throwable = throwable2;
                        throw throwable3;
                    }
                    catch (Throwable throwable4) {
                        if (directoryStream == null) throw throwable4;
                        if (throwable == null) {
                            directoryStream.close();
                            throw throwable4;
                        }
                        try {
                            directoryStream.close();
                            throw throwable4;
                        }
                        catch (Throwable throwable5) {
                            throwable.addSuppressed(throwable5);
                            throw throwable4;
                        }
                    }
                }
                directoryStream.close();
            }
            catch (IOException iOException) {
                throw new LoggingException("Error reading folder " + object2 + " " + iOException.getMessage(), iOException);
            }
        }
        if (!bl) return treeMap.descendingMap();
        return treeMap;
    }
}

