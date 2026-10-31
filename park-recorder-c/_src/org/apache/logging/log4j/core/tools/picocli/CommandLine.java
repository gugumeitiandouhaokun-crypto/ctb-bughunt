/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.tools.picocli;

import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.text.BreakIterator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

public class CommandLine {
    public static final String VERSION = "2.0.3";
    private final Tracer tracer = new Tracer();
    private final Interpreter interpreter;
    private String commandName = "<main class>";
    private boolean overwrittenOptionsAllowed = false;
    private boolean unmatchedArgumentsAllowed = false;
    private final List<String> unmatchedArguments = new ArrayList<String>();
    private CommandLine parent;
    private boolean usageHelpRequested;
    private boolean versionHelpRequested;
    private final List<String> versionLines = new ArrayList<String>();

    public CommandLine(Object object) {
        this.interpreter = new Interpreter(object);
    }

    public CommandLine addSubcommand(String string, Object object) {
        object = CommandLine.toCommandLine(object);
        v0.parent = this;
        this.interpreter.commands.put(string, object);
        return this;
    }

    public Map<String, CommandLine> getSubcommands() {
        return new LinkedHashMap<String, CommandLine>(this.interpreter.commands);
    }

    public CommandLine getParent() {
        return this.parent;
    }

    public <T> T getCommand() {
        return (T)this.interpreter.command;
    }

    public boolean isUsageHelpRequested() {
        return this.usageHelpRequested;
    }

    public boolean isVersionHelpRequested() {
        return this.versionHelpRequested;
    }

    public boolean isOverwrittenOptionsAllowed() {
        return this.overwrittenOptionsAllowed;
    }

    public CommandLine setOverwrittenOptionsAllowed(boolean bl) {
        this.overwrittenOptionsAllowed = bl;
        Iterator iterator = this.interpreter.commands.values().iterator();
        while (iterator.hasNext()) {
            ((CommandLine)iterator.next()).setOverwrittenOptionsAllowed(bl);
        }
        return this;
    }

    public boolean isUnmatchedArgumentsAllowed() {
        return this.unmatchedArgumentsAllowed;
    }

    public CommandLine setUnmatchedArgumentsAllowed(boolean bl) {
        this.unmatchedArgumentsAllowed = bl;
        Iterator iterator = this.interpreter.commands.values().iterator();
        while (iterator.hasNext()) {
            ((CommandLine)iterator.next()).setUnmatchedArgumentsAllowed(bl);
        }
        return this;
    }

    public List<String> getUnmatchedArguments() {
        return this.unmatchedArguments;
    }

    public static <T> T populateCommand(T t, String ... stringArray) {
        CommandLine.toCommandLine(t).parse(stringArray);
        return t;
    }

    public List<CommandLine> parse(String ... stringArray) {
        return this.interpreter.parse(stringArray);
    }

    public static boolean printHelpIfRequested(List<CommandLine> object, PrintStream printStream, Help.Ansi ansi) {
        object = object.iterator();
        while (object.hasNext()) {
            CommandLine commandLine = (CommandLine)object.next();
            if (commandLine.isUsageHelpRequested()) {
                commandLine.usage(printStream, ansi);
                return true;
            }
            if (!commandLine.isVersionHelpRequested()) continue;
            commandLine.printVersionHelp(printStream, ansi);
            return true;
        }
        return false;
    }

    private static Object execute(CommandLine commandLine) {
        Object t = commandLine.getCommand();
        if (t instanceof Runnable) {
            try {
                ((Runnable)t).run();
                return null;
            }
            catch (Exception exception) {
                throw new ExecutionException(commandLine, "Error while running command (" + t + ")", exception);
            }
        }
        if (t instanceof Callable) {
            try {
                return ((Callable)t).call();
            }
            catch (Exception exception) {
                throw new ExecutionException(commandLine, "Error while calling command (" + t + ")", exception);
            }
        }
        throw new ExecutionException(commandLine, "Parsed command (" + t + ") is not Runnable or Callable");
    }

    public List<Object> parseWithHandler(IParseResultHandler iParseResultHandler, PrintStream printStream, String ... stringArray) {
        return this.parseWithHandlers(iParseResultHandler, printStream, Help.Ansi.AUTO, new DefaultExceptionHandler(), stringArray);
    }

    public List<Object> parseWithHandlers(IParseResultHandler iParseResultHandler, PrintStream printStream, Help.Ansi ansi, IExceptionHandler iExceptionHandler, String ... stringArray) {
        try {
            List<CommandLine> list = this.parse(stringArray);
            return iParseResultHandler.handleParseResult(list, printStream, ansi);
        }
        catch (ParameterException parameterException) {
            return iExceptionHandler.handleException(parameterException, printStream, ansi, stringArray);
        }
    }

    public static void usage(Object object, PrintStream printStream) {
        CommandLine.toCommandLine(object).usage(printStream);
    }

    public static void usage(Object object, PrintStream printStream, Help.Ansi ansi) {
        CommandLine.toCommandLine(object).usage(printStream, ansi);
    }

    public static void usage(Object object, PrintStream printStream, Help.ColorScheme colorScheme) {
        CommandLine.toCommandLine(object).usage(printStream, colorScheme);
    }

    public void usage(PrintStream printStream) {
        this.usage(printStream, Help.Ansi.AUTO);
    }

    public void usage(PrintStream printStream, Help.Ansi ansi) {
        this.usage(printStream, Help.defaultColorScheme(ansi));
    }

    public void usage(PrintStream printStream, Help.ColorScheme object) {
        object = new Help(this.interpreter.command, (Help.ColorScheme)object).addAllSubcommands(this.getSubcommands());
        if (!"=".equals(this.getSeparator())) {
            ((Help)object).separator = this.getSeparator();
            ((Help)object).parameterLabelRenderer = ((Help)object).createDefaultParamLabelRenderer();
        }
        if (!"<main class>".equals(this.getCommandName())) {
            ((Help)object).commandName = this.getCommandName();
        }
        Object object2 = object;
        object = new StringBuilder().append(((Help)object).headerHeading(new Object[0])).append(((Help)object).header(new Object[0])).append(((Help)object).synopsisHeading(new Object[0])).append(((Help)object2).synopsis(((Help)object2).synopsisHeadingLength())).append(((Help)object).descriptionHeading(new Object[0])).append(((Help)object).description(new Object[0])).append(((Help)object).parameterListHeading(new Object[0])).append(((Help)object).parameterList()).append(((Help)object).optionListHeading(new Object[0])).append(((Help)object).optionList()).append(((Help)object).commandListHeading(new Object[0])).append(((Help)object).commandList()).append(((Help)object).footerHeading(new Object[0])).append(((Help)object).footer(new Object[0]));
        printStream.print(object);
    }

    public void printVersionHelp(PrintStream printStream) {
        this.printVersionHelp(printStream, Help.Ansi.AUTO);
    }

    public void printVersionHelp(PrintStream printStream, Help.Ansi ansi) {
        for (String string : this.versionLines) {
            Help.Ansi ansi2 = ansi;
            ((Object)((Object)ansi2)).getClass();
            printStream.println(ansi2.new Help.Ansi.Text(string));
        }
    }

    public void printVersionHelp(PrintStream printStream, Help.Ansi ansi, Object ... objectArray) {
        for (String string : this.versionLines) {
            Help.Ansi ansi2 = ansi;
            ((Object)((Object)ansi2)).getClass();
            printStream.println(ansi2.new Help.Ansi.Text(String.format(string, objectArray)));
        }
    }

    public static <C extends Callable<T>, T> T call(C c2, PrintStream printStream, String ... stringArray) {
        return CommandLine.call(c2, printStream, Help.Ansi.AUTO, stringArray);
    }

    public static <C extends Callable<T>, T> T call(C object, PrintStream printStream, Help.Ansi ansi, String ... stringArray) {
        if ((object = new CommandLine(object).parseWithHandlers(new RunLast(), printStream, ansi, new DefaultExceptionHandler(), stringArray)) == null || object.isEmpty()) {
            return null;
        }
        return (T)object.get(0);
    }

    public static <R extends Runnable> void run(R r, PrintStream printStream, String ... stringArray) {
        CommandLine.run(r, printStream, Help.Ansi.AUTO, stringArray);
    }

    public static <R extends Runnable> void run(R r, PrintStream printStream, Help.Ansi ansi, String ... stringArray) {
        new CommandLine(r).parseWithHandlers(new RunLast(), printStream, ansi, new DefaultExceptionHandler(), stringArray);
    }

    public <K> CommandLine registerConverter(Class<K> clazz, ITypeConverter<K> iTypeConverter) {
        this.interpreter.converterRegistry.put(Assert.notNull(clazz, "class"), Assert.notNull(iTypeConverter, "converter"));
        Iterator iterator = this.interpreter.commands.values().iterator();
        while (iterator.hasNext()) {
            ((CommandLine)iterator.next()).registerConverter(clazz, iTypeConverter);
        }
        return this;
    }

    public String getSeparator() {
        return this.interpreter.separator;
    }

    public CommandLine setSeparator(String string) {
        this.interpreter.separator = Assert.notNull(string, "separator");
        return this;
    }

    public String getCommandName() {
        return this.commandName;
    }

    public CommandLine setCommandName(String string) {
        this.commandName = Assert.notNull(string, "commandName");
        return this;
    }

    private static boolean empty(String string) {
        return string == null || string.trim().length() == 0;
    }

    private static boolean empty(Object[] objectArray) {
        return objectArray == null || objectArray.length == 0;
    }

    private static boolean empty(Help.Ansi.Text text) {
        return text == null || text.plain.toString().trim().length() == 0;
    }

    private static String str(String[] stringArray, int n) {
        if (stringArray == null || stringArray.length == 0) {
            return "";
        }
        return stringArray[n];
    }

    private static boolean isBoolean(Class<?> clazz) {
        return clazz == Boolean.class || clazz == Boolean.TYPE;
    }

    private static CommandLine toCommandLine(Object object) {
        if (object instanceof CommandLine) {
            return (CommandLine)object;
        }
        return new CommandLine(object);
    }

    private static boolean isMultiValue(Field field) {
        return CommandLine.isMultiValue(field.getType());
    }

    private static boolean isMultiValue(Class<?> clazz) {
        return clazz.isArray() || Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz);
    }

    private static Class<?>[] getTypeAttribute(Field typeArray) {
        Object[] objectArray = typeArray.isAnnotationPresent(Parameters.class) ? typeArray.getAnnotation(Parameters.class).type() : typeArray.getAnnotation(Option.class).type();
        if (objectArray.length > 0) {
            return objectArray;
        }
        if (typeArray.getType().isArray()) {
            return new Class[]{typeArray.getType().getComponentType()};
        }
        if (CommandLine.isMultiValue((Field)typeArray)) {
            if ((typeArray = typeArray.getGenericType()) instanceof ParameterizedType) {
                typeArray = ((ParameterizedType)typeArray).getActualTypeArguments();
                objectArray = new Class[typeArray.length];
                for (int i2 = 0; i2 < typeArray.length; ++i2) {
                    if (typeArray[i2] instanceof Class) {
                        objectArray[i2] = (Class)typeArray[i2];
                        continue;
                    }
                    if (typeArray[i2] instanceof WildcardType) {
                        Type[] typeArray2 = (Type[])typeArray[i2];
                        Type[] typeArray3 = typeArray2.getLowerBounds();
                        if (typeArray3.length > 0 && typeArray3[0] instanceof Class) {
                            objectArray[i2] = (Class)typeArray3[0];
                            continue;
                        }
                        if ((typeArray2 = typeArray2.getUpperBounds()).length > 0 && typeArray2[0] instanceof Class) {
                            objectArray[i2] = (Class)typeArray2[0];
                            continue;
                        }
                    }
                    Arrays.fill(objectArray, String.class);
                    return objectArray;
                }
                return objectArray;
            }
            return new Class[]{String.class, String.class};
        }
        return new Class[]{typeArray.getType()};
    }

    static void init(Class<?> fieldArray, List<Field> list, Map<String, Field> map, Map<Character, Field> map2, List<Field> list2) {
        for (Field field : fieldArray.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Option.class)) {
                String[] stringArray = field.getAnnotation(Option.class);
                if (stringArray.required()) {
                    list.add(field);
                }
                for (String string : stringArray.names()) {
                    char c2;
                    Field field2 = map.put(string, field);
                    if (field2 != null && field2 != field) {
                        throw DuplicateOptionAnnotationsException.create(string, field, field2);
                    }
                    if (string.length() != 2 || !string.startsWith("-") || (field2 = map2.put(Character.valueOf(c2 = string.charAt(1)), field)) == null || field2 == field) continue;
                    throw DuplicateOptionAnnotationsException.create(string, field, field2);
                }
            }
            if (!field.isAnnotationPresent(Parameters.class)) continue;
            if (field.isAnnotationPresent(Option.class)) {
                throw new DuplicateOptionAnnotationsException("A field can be either @Option or @Parameters, but '" + field.getName() + "' is both.");
            }
            list2.add(field);
            if (Range.parameterArity((Field)field).min <= 0) continue;
            list.add(field);
        }
    }

    static void validatePositionalParameters(List<Field> object) {
        int n = 0;
        object = object.iterator();
        while (object.hasNext()) {
            Field field = (Field)object.next();
            Range range = Range.parameterIndex(field);
            if (range.min > n) {
                throw new ParameterIndexGapException("Missing field annotated with @Parameter(index=" + n + "). Nearest field '" + field.getName() + "' has index=" + range.min);
            }
            n = (n = Math.max(n, range.max)) == Integer.MAX_VALUE ? n : n + 1;
        }
    }

    private static <T> Stack<T> reverse(Stack<T> stack) {
        Collections.reverse(stack);
        return stack;
    }

    public static class MissingTypeConverterException
    extends ParameterException {
        private static final long serialVersionUID = -6050931703233083760L;

        public MissingTypeConverterException(CommandLine commandLine, String string) {
            super(commandLine, string);
        }
    }

    public static class OverwrittenOptionException
    extends ParameterException {
        private static final long serialVersionUID = 1338029208271055776L;

        public OverwrittenOptionException(CommandLine commandLine, String string) {
            super(commandLine, string);
        }
    }

    public static class MaxValuesforFieldExceededException
    extends ParameterException {
        private static final long serialVersionUID = 6536145439570100641L;

        public MaxValuesforFieldExceededException(CommandLine commandLine, String string) {
            super(commandLine, string);
        }
    }

    public static class UnmatchedArgumentException
    extends ParameterException {
        private static final long serialVersionUID = -8700426380701452440L;

        public UnmatchedArgumentException(CommandLine commandLine, String string) {
            super(commandLine, string);
        }

        public UnmatchedArgumentException(CommandLine commandLine, Stack<String> stack) {
            this(commandLine, new ArrayList<String>(CommandLine.reverse(stack)));
        }

        public UnmatchedArgumentException(CommandLine commandLine, List<String> list) {
            this(commandLine, "Unmatched argument" + (list.size() == 1 ? " " : "s ") + list);
        }
    }

    public static class ParameterIndexGapException
    extends InitializationException {
        private static final long serialVersionUID = -1520981133257618319L;

        public ParameterIndexGapException(String string) {
            super(string);
        }
    }

    public static class DuplicateOptionAnnotationsException
    extends InitializationException {
        private static final long serialVersionUID = -3355128012575075641L;

        public DuplicateOptionAnnotationsException(String string) {
            super(string);
        }

        private static DuplicateOptionAnnotationsException create(String string, Field field, Field field2) {
            return new DuplicateOptionAnnotationsException("Option name '" + string + "' is used by both " + field.getDeclaringClass().getName() + "." + field.getName() + " and " + field2.getDeclaringClass().getName() + "." + field2.getName());
        }
    }

    public static class MissingParameterException
    extends ParameterException {
        private static final long serialVersionUID = 5075678535706338753L;

        public MissingParameterException(CommandLine commandLine, String string) {
            super(commandLine, string);
        }

        private static MissingParameterException create(CommandLine commandLine, Collection<Field> object, String string) {
            if (object.size() == 1) {
                return new MissingParameterException(commandLine, "Missing required option '" + MissingParameterException.describe(object.iterator().next(), string) + "'");
            }
            ArrayList<String> arrayList = new ArrayList<String>(object.size());
            object = object.iterator();
            while (object.hasNext()) {
                Field field = (Field)object.next();
                arrayList.add(MissingParameterException.describe(field, string));
            }
            return new MissingParameterException(commandLine, "Missing required options " + ((Object)arrayList).toString());
        }

        private static String describe(Field field, String string) {
            string = field.isAnnotationPresent(Option.class) ? field.getAnnotation(Option.class).names()[0] + string : "params[" + field.getAnnotation(Parameters.class).index() + "]" + string;
            return string + Help.DefaultParamLabelRenderer.renderParameterName(field);
        }
    }

    public static class ParameterException
    extends PicocliException {
        private static final long serialVersionUID = 1477112829129763139L;
        private final CommandLine commandLine;

        public ParameterException(CommandLine commandLine, String string) {
            super(string);
            this.commandLine = Assert.notNull(commandLine, "commandLine");
        }

        public ParameterException(CommandLine commandLine, String string, Exception exception) {
            super(string, exception);
            this.commandLine = Assert.notNull(commandLine, "commandLine");
        }

        public CommandLine getCommandLine() {
            return this.commandLine;
        }

        private static ParameterException create(CommandLine commandLine, Exception exception, String string, int n, String[] stringArray) {
            string = exception.getClass().getSimpleName() + ": " + exception.getLocalizedMessage() + " while processing argument at or before arg[" + n + "] '" + string + "' in " + Arrays.toString(stringArray) + ": " + exception.toString();
            return new ParameterException(commandLine, string, exception);
        }
    }

    public static class TypeConversionException
    extends PicocliException {
        private static final long serialVersionUID = 4251973913816346114L;

        public TypeConversionException(String string) {
            super(string);
        }
    }

    public static class ExecutionException
    extends PicocliException {
        private static final long serialVersionUID = 7764539594267007998L;
        private final CommandLine commandLine;

        public ExecutionException(CommandLine commandLine, String string) {
            super(string);
            this.commandLine = Assert.notNull(commandLine, "commandLine");
        }

        public ExecutionException(CommandLine commandLine, String string, Exception exception) {
            super(string, exception);
            this.commandLine = Assert.notNull(commandLine, "commandLine");
        }

        public CommandLine getCommandLine() {
            return this.commandLine;
        }
    }

    public static class InitializationException
    extends PicocliException {
        private static final long serialVersionUID = 8423014001666638895L;

        public InitializationException(String string) {
            super(string);
        }

        public InitializationException(String string, Exception exception) {
            super(string, exception);
        }
    }

    public static class PicocliException
    extends RuntimeException {
        private static final long serialVersionUID = -2574128880125050818L;

        public PicocliException(String string) {
            super(string);
        }

        public PicocliException(String string, Exception exception) {
            super(string, exception);
        }
    }

    static class Tracer {
        TraceLevel level = TraceLevel.lookup(System.getProperty("picocli.trace"));
        PrintStream stream = System.err;

        private Tracer() {
        }

        void warn(String string, Object ... objectArray) {
            TraceLevel.WARN.print(this, string, objectArray);
        }

        void info(String string, Object ... objectArray) {
            TraceLevel.INFO.print(this, string, objectArray);
        }

        void debug(String string, Object ... objectArray) {
            TraceLevel.DEBUG.print(this, string, objectArray);
        }

        boolean isWarn() {
            return this.level.isEnabled(TraceLevel.WARN);
        }

        boolean isInfo() {
            return this.level.isEnabled(TraceLevel.INFO);
        }

        boolean isDebug() {
            return this.level.isEnabled(TraceLevel.DEBUG);
        }
    }

    static enum TraceLevel {
        OFF,
        WARN,
        INFO,
        DEBUG;


        public final boolean isEnabled(TraceLevel traceLevel) {
            return this.ordinal() >= traceLevel.ordinal();
        }

        private void print(Tracer tracer, String string, Object ... objectArray) {
            if (tracer.level.isEnabled(this)) {
                tracer.stream.printf(this.prefix(string), objectArray);
            }
        }

        private String prefix(String string) {
            return "[picocli " + (Object)((Object)this) + "] " + string;
        }

        static TraceLevel lookup(String string) {
            if (string == null) {
                return WARN;
            }
            if (CommandLine.empty(string) || "true".equalsIgnoreCase(string)) {
                return INFO;
            }
            return TraceLevel.valueOf(string);
        }
    }

    static final class Assert {
        static <T> T notNull(T t, String string) {
            if (t == null) {
                throw new NullPointerException(string);
            }
            return t;
        }

        private Assert() {
        }
    }

    public static class Help {
        protected static final String DEFAULT_COMMAND_NAME = "<main class>";
        protected static final String DEFAULT_SEPARATOR = "=";
        private static final int usageHelpWidth = 80;
        private static final int optionsColumnWidth = 29;
        private final Object command;
        private final Map<String, Help> commands = new LinkedHashMap<String, Help>();
        final ColorScheme colorScheme;
        public final List<Field> optionFields;
        public final List<Field> positionalParametersFields;
        public String separator;
        public String commandName = "<main class>";
        public String[] description = new String[0];
        public String[] customSynopsis = new String[0];
        public String[] header = new String[0];
        public String[] footer = new String[0];
        public IParamLabelRenderer parameterLabelRenderer;
        public Boolean abbreviateSynopsis;
        public Boolean sortOptions;
        public Boolean showDefaultValues;
        public Character requiredOptionMarker;
        public String headerHeading;
        public String synopsisHeading;
        public String descriptionHeading;
        public String parameterListHeading;
        public String optionListHeading;
        public String commandListHeading;
        public String footerHeading;

        public Help(Object object) {
            this(object, Ansi.AUTO);
        }

        public Help(Object object, Ansi ansi) {
            this(object, Help.defaultColorScheme(ansi));
        }

        public Help(Object clazz, ColorScheme object) {
            this.command = Assert.notNull(clazz, "command");
            this.colorScheme = Assert.notNull(object, "colorScheme").applySystemProperties();
            object = new ArrayList();
            ArrayList<Field> arrayList = new ArrayList<Field>();
            for (clazz = clazz.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(Option.class) && !field.getAnnotation(Option.class).hidden()) {
                        object.add(field);
                    }
                    if (!field.isAnnotationPresent(Parameters.class)) continue;
                    arrayList.add(field);
                }
                if (!clazz.isAnnotationPresent(Command.class)) continue;
                Command command = clazz.getAnnotation(Command.class);
                if (DEFAULT_COMMAND_NAME.equals(this.commandName)) {
                    this.commandName = command.name();
                }
                this.separator = this.separator == null ? command.separator() : this.separator;
                this.abbreviateSynopsis = this.abbreviateSynopsis == null ? command.abbreviateSynopsis() : this.abbreviateSynopsis.booleanValue();
                this.sortOptions = this.sortOptions == null ? command.sortOptions() : this.sortOptions.booleanValue();
                this.requiredOptionMarker = Character.valueOf(this.requiredOptionMarker == null ? command.requiredOptionMarker() : this.requiredOptionMarker.charValue());
                this.showDefaultValues = this.showDefaultValues == null ? command.showDefaultValues() : this.showDefaultValues.booleanValue();
                this.customSynopsis = CommandLine.empty(this.customSynopsis) ? command.customSynopsis() : this.customSynopsis;
                this.description = CommandLine.empty(this.description) ? command.description() : this.description;
                this.header = CommandLine.empty(this.header) ? command.header() : this.header;
                this.footer = CommandLine.empty(this.footer) ? command.footer() : this.footer;
                this.headerHeading = CommandLine.empty(this.headerHeading) ? command.headerHeading() : this.headerHeading;
                this.synopsisHeading = CommandLine.empty(this.synopsisHeading) || "Usage: ".equals(this.synopsisHeading) ? command.synopsisHeading() : this.synopsisHeading;
                this.descriptionHeading = CommandLine.empty(this.descriptionHeading) ? command.descriptionHeading() : this.descriptionHeading;
                this.parameterListHeading = CommandLine.empty(this.parameterListHeading) ? command.parameterListHeading() : this.parameterListHeading;
                this.optionListHeading = CommandLine.empty(this.optionListHeading) ? command.optionListHeading() : this.optionListHeading;
                this.commandListHeading = CommandLine.empty(this.commandListHeading) || "Commands:%n".equals(this.commandListHeading) ? command.commandListHeading() : this.commandListHeading;
                this.footerHeading = CommandLine.empty(this.footerHeading) ? command.footerHeading() : this.footerHeading;
            }
            this.sortOptions = this.sortOptions == null ? true : this.sortOptions;
            this.abbreviateSynopsis = this.abbreviateSynopsis == null ? false : this.abbreviateSynopsis;
            this.requiredOptionMarker = Character.valueOf(this.requiredOptionMarker == null ? (char)' ' : this.requiredOptionMarker.charValue());
            this.showDefaultValues = this.showDefaultValues == null ? false : this.showDefaultValues;
            this.synopsisHeading = this.synopsisHeading == null ? "Usage: " : this.synopsisHeading;
            this.commandListHeading = this.commandListHeading == null ? "Commands:%n" : this.commandListHeading;
            this.separator = this.separator == null ? DEFAULT_SEPARATOR : this.separator;
            this.parameterLabelRenderer = this.createDefaultParamLabelRenderer();
            Collections.sort(arrayList, new PositionalParametersSorter());
            this.positionalParametersFields = Collections.unmodifiableList(arrayList);
            this.optionFields = Collections.unmodifiableList(object);
        }

        public Help addAllSubcommands(Map<String, CommandLine> object) {
            if (object != null) {
                for (Map.Entry entry : object.entrySet()) {
                    this.addSubcommand((String)entry.getKey(), ((CommandLine)entry.getValue()).getCommand());
                }
            }
            return this;
        }

        public Help addSubcommand(String string, Object object) {
            this.commands.put(string, new Help(object));
            return this;
        }

        @Deprecated
        public String synopsis() {
            return this.synopsis(0);
        }

        public String synopsis(int n) {
            if (!CommandLine.empty(this.customSynopsis)) {
                return this.customSynopsis(new Object[0]);
            }
            if (this.abbreviateSynopsis.booleanValue()) {
                return this.abbreviatedSynopsis();
            }
            return this.detailedSynopsis(n, Help.createShortOptionArityAndNameComparator(), true);
        }

        public String abbreviatedSynopsis() {
            StringBuilder stringBuilder = new StringBuilder();
            if (!this.optionFields.isEmpty()) {
                stringBuilder.append(" [OPTIONS]");
            }
            for (Field field : this.positionalParametersFields) {
                if (field.getAnnotation(Parameters.class).hidden()) continue;
                stringBuilder.append(' ').append(this.parameterLabelRenderer.renderParameterLabel(field, this.ansi(), this.colorScheme.parameterStyles));
            }
            return this.colorScheme.commandText(this.commandName).toString() + stringBuilder.toString() + System.getProperty("line.separator");
        }

        @Deprecated
        public String detailedSynopsis(Comparator<Field> comparator, boolean bl) {
            return this.detailedSynopsis(0, comparator, bl);
        }

        public String detailedSynopsis(int n, Comparator<Field> object, boolean bl) {
            Object object2;
            Ansi ansi = this.ansi();
            ((Object)((Object)ansi)).getClass();
            Ansi.Text text = ansi.new Ansi.Text(0);
            ArrayList<Field> arrayList = new ArrayList<Field>(this.optionFields);
            if (object != null) {
                Collections.sort(arrayList, object);
            }
            if (bl) {
                object = new ArrayList();
                StringBuilder stringBuilder = new StringBuilder("-");
                object2 = new StringBuilder("-");
                for (Field field : arrayList) {
                    Option option;
                    String string;
                    if (field.getType() != Boolean.TYPE && field.getType() != Boolean.class || (string = ShortestFirst.sort((option = field.getAnnotation(Option.class)).names())[0]).length() != 2 || !string.startsWith("-")) continue;
                    object.add(field);
                    if (option.required()) {
                        stringBuilder.append(string.substring(1));
                        continue;
                    }
                    ((StringBuilder)object2).append(string.substring(1));
                }
                arrayList.removeAll((Collection<?>)object);
                if (stringBuilder.length() > 1) {
                    text = text.append(" ").append(this.colorScheme.optionText(stringBuilder.toString()));
                }
                if (((StringBuilder)object2).length() > 1) {
                    text = text.append(" [").append(this.colorScheme.optionText(((StringBuilder)object2).toString())).append("]");
                }
            }
            for (Field field : arrayList) {
                object2 = field.getAnnotation(Option.class);
                if (object2.hidden()) continue;
                if (object2.required()) {
                    text = this.appendOptionSynopsis(text, field, ShortestFirst.sort(object2.names())[0], " ", "");
                    if (!CommandLine.isMultiValue(field)) continue;
                    text = this.appendOptionSynopsis(text, field, ShortestFirst.sort(object2.names())[0], " [", "]...");
                    continue;
                }
                text = this.appendOptionSynopsis(text, field, ShortestFirst.sort(object2.names())[0], " [", "]");
                if (!CommandLine.isMultiValue(field)) continue;
                text = text.append("...");
            }
            for (Field field : this.positionalParametersFields) {
                if (field.getAnnotation(Parameters.class).hidden()) continue;
                text = text.append(" ");
                object2 = this.parameterLabelRenderer.renderParameterLabel(field, this.colorScheme.ansi(), this.colorScheme.parameterStyles);
                text = text.append((Ansi.Text)object2);
            }
            int n2 = this.commandName.length() + n;
            TextTable textTable = new TextTable(this.ansi(), n2, 80 - n2);
            new TextTable(this.ansi(), n2, 80 - n2).indentWrappedLines = 1;
            Ansi ansi2 = Ansi.OFF;
            ((Object)((Object)ansi2)).getClass();
            object2 = ansi2.new Ansi.Text(Help.stringOf('X', n));
            textTable.addRowValues(((Ansi.Text)object2).append(this.colorScheme.commandText(this.commandName)), text);
            return textTable.toString().substring(n);
        }

        private Ansi.Text appendOptionSynopsis(Ansi.Text text, Field object, String string, String string2, String string3) {
            object = this.parameterLabelRenderer.renderParameterLabel((Field)object, this.colorScheme.ansi(), this.colorScheme.optionParamStyles);
            return text.append(string2).append(this.colorScheme.optionText(string)).append((Ansi.Text)object).append(string3);
        }

        public int synopsisHeadingLength() {
            Ansi ansi = Ansi.OFF;
            ((Object)((Object)ansi)).getClass();
            String[] stringArray = ansi.new Ansi.Text(this.synopsisHeading).toString().split("\\r?\\n|\\r|%n", -1);
            return stringArray[stringArray.length - 1].length();
        }

        public String optionList() {
            Comparator<Field> comparator = this.sortOptions == null || this.sortOptions != false ? Help.createShortOptionNameComparator() : null;
            Help help = this;
            return help.optionList(help.createDefaultLayout(), comparator, this.parameterLabelRenderer);
        }

        public String optionList(Layout layout, Comparator<Field> comparator, IParamLabelRenderer iParamLabelRenderer) {
            ArrayList<Field> arrayList = new ArrayList<Field>(this.optionFields);
            if (comparator != null) {
                Collections.sort(arrayList, comparator);
            }
            layout.addOptions(arrayList, iParamLabelRenderer);
            return layout.toString();
        }

        public String parameterList() {
            Help help = this;
            return help.parameterList(help.createDefaultLayout(), this.parameterLabelRenderer);
        }

        public String parameterList(Layout layout, IParamLabelRenderer iParamLabelRenderer) {
            layout.addPositionalParameters(this.positionalParametersFields, iParamLabelRenderer);
            return layout.toString();
        }

        private static String heading(Ansi object, String string, Object ... objectArray) {
            object = ((String)(object = Help.join((Ansi)((Object)object), new String[]{string}, new StringBuilder(), objectArray).toString())).endsWith(System.getProperty("line.separator")) ? ((String)object).substring(0, ((String)object).length() - System.getProperty("line.separator").length()) : object;
            return (String)object + new String(Help.spaces(Help.countTrailingSpaces(string)));
        }

        private static char[] spaces(int n) {
            char[] cArray = new char[n];
            Arrays.fill(cArray, ' ');
            return cArray;
        }

        private static int countTrailingSpaces(String string) {
            if (string == null) {
                return 0;
            }
            int n = 0;
            for (int i2 = string.length() - 1; i2 >= 0 && string.charAt(i2) == ' '; --i2) {
                ++n;
            }
            return n;
        }

        public static StringBuilder join(Ansi ansi, String[] stringArray, StringBuilder stringBuilder, Object ... objectArray) {
            if (stringArray != null) {
                TextTable textTable = new TextTable(ansi, 80);
                new TextTable(ansi, 80).indentWrappedLines = 0;
                for (String string : stringArray) {
                    Ansi ansi2 = ansi;
                    ((Object)((Object)ansi2)).getClass();
                    for (Ansi.Text text : ansi2.new Ansi.Text(Help.format(string, objectArray)).splitLines()) {
                        textTable.addRowValues(text);
                    }
                }
                textTable.toString(stringBuilder);
            }
            return stringBuilder;
        }

        private static String format(String string, Object ... objectArray) {
            if (string == null) {
                return "";
            }
            return String.format(string, objectArray);
        }

        public String customSynopsis(Object ... objectArray) {
            return Help.join(this.ansi(), this.customSynopsis, new StringBuilder(), objectArray).toString();
        }

        public String description(Object ... objectArray) {
            return Help.join(this.ansi(), this.description, new StringBuilder(), objectArray).toString();
        }

        public String header(Object ... objectArray) {
            return Help.join(this.ansi(), this.header, new StringBuilder(), objectArray).toString();
        }

        public String footer(Object ... objectArray) {
            return Help.join(this.ansi(), this.footer, new StringBuilder(), objectArray).toString();
        }

        public String headerHeading(Object ... objectArray) {
            return Help.heading(this.ansi(), this.headerHeading, objectArray);
        }

        public String synopsisHeading(Object ... objectArray) {
            return Help.heading(this.ansi(), this.synopsisHeading, objectArray);
        }

        public String descriptionHeading(Object ... objectArray) {
            if (CommandLine.empty(this.descriptionHeading)) {
                return "";
            }
            return Help.heading(this.ansi(), this.descriptionHeading, objectArray);
        }

        public String parameterListHeading(Object ... objectArray) {
            if (this.positionalParametersFields.isEmpty()) {
                return "";
            }
            return Help.heading(this.ansi(), this.parameterListHeading, objectArray);
        }

        public String optionListHeading(Object ... objectArray) {
            if (this.optionFields.isEmpty()) {
                return "";
            }
            return Help.heading(this.ansi(), this.optionListHeading, objectArray);
        }

        public String commandListHeading(Object ... objectArray) {
            if (this.commands.isEmpty()) {
                return "";
            }
            return Help.heading(this.ansi(), this.commandListHeading, objectArray);
        }

        public String footerHeading(Object ... objectArray) {
            return Help.heading(this.ansi(), this.footerHeading, objectArray);
        }

        public String commandList() {
            if (this.commands.isEmpty()) {
                return "";
            }
            int n = Help.maxLength(this.commands.keySet());
            TextTable textTable = new TextTable(this.ansi(), new Column(n + 2, 2, Column.Overflow.SPAN), new Column(80 - (n + 2), 2, Column.Overflow.WRAP));
            for (Map.Entry<String, Help> entry : this.commands.entrySet()) {
                Object object = entry.getValue();
                object = ((Help)object).header != null && ((Help)object).header.length > 0 ? ((Help)object).header[0] : (((Help)object).description != null && ((Help)object).description.length > 0 ? ((Help)object).description[0] : "");
                Ansi.Text[] textArray = new Ansi.Text[2];
                textArray[0] = this.colorScheme.commandText(entry.getKey());
                Ansi ansi = this.ansi();
                ((Object)((Object)ansi)).getClass();
                textArray[1] = ansi.new Ansi.Text((String)object);
                textTable.addRowValues(textArray);
            }
            return textTable.toString();
        }

        private static int maxLength(Collection<String> collection) {
            collection = new ArrayList<String>(collection);
            Collections.sort(collection, Collections.reverseOrder(Help.shortestFirst()));
            return ((String)collection.get(0)).length();
        }

        private static String join(String[] stringArray, int n, int n2, String string) {
            if (stringArray == null) {
                return "";
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (int i2 = n; i2 < n + n2; ++i2) {
                stringBuilder.append(i2 > n ? string : "").append(stringArray[i2]);
            }
            return stringBuilder.toString();
        }

        private static String stringOf(char c2, int n) {
            char[] cArray = new char[n];
            Arrays.fill(cArray, c2);
            return new String(cArray);
        }

        public Layout createDefaultLayout() {
            return new Layout(this.colorScheme, new TextTable(this.colorScheme.ansi()), this.createDefaultOptionRenderer(), this.createDefaultParameterRenderer());
        }

        public IOptionRenderer createDefaultOptionRenderer() {
            DefaultOptionRenderer defaultOptionRenderer = new DefaultOptionRenderer();
            new DefaultOptionRenderer().requiredMarker = String.valueOf(this.requiredOptionMarker);
            if (this.showDefaultValues != null && this.showDefaultValues.booleanValue()) {
                defaultOptionRenderer.command = this.command;
            }
            return defaultOptionRenderer;
        }

        public static IOptionRenderer createMinimalOptionRenderer() {
            return new MinimalOptionRenderer();
        }

        public IParameterRenderer createDefaultParameterRenderer() {
            DefaultParameterRenderer defaultParameterRenderer = new DefaultParameterRenderer();
            new DefaultParameterRenderer().requiredMarker = String.valueOf(this.requiredOptionMarker);
            return defaultParameterRenderer;
        }

        public static IParameterRenderer createMinimalParameterRenderer() {
            return new MinimalParameterRenderer();
        }

        public static IParamLabelRenderer createMinimalParamLabelRenderer() {
            return new IParamLabelRenderer(){

                @Override
                public final Ansi.Text renderParameterLabel(Field object, Ansi ansi, List<Ansi.IStyle> list) {
                    object = DefaultParamLabelRenderer.renderParameterName((Field)object);
                    return ansi.apply((String)object, list);
                }

                @Override
                public final String separator() {
                    return "";
                }
            };
        }

        public IParamLabelRenderer createDefaultParamLabelRenderer() {
            return new DefaultParamLabelRenderer(this.separator);
        }

        public static Comparator<Field> createShortOptionNameComparator() {
            return new SortByShortestOptionNameAlphabetically();
        }

        public static Comparator<Field> createShortOptionArityAndNameComparator() {
            return new SortByOptionArityAndNameAlphabetically();
        }

        public static Comparator<String> shortestFirst() {
            return new ShortestFirst();
        }

        public Ansi ansi() {
            return this.colorScheme.ansi;
        }

        public static ColorScheme defaultColorScheme(Ansi ansi) {
            return new ColorScheme(ansi).commands(Ansi.Style.bold).options(Ansi.Style.fg_yellow).parameters(Ansi.Style.fg_yellow).optionParams(Ansi.Style.italic);
        }

        public static enum Ansi {
            AUTO,
            ON,
            OFF;

            static Text EMPTY_TEXT;
            static final boolean isWindows;
            static final boolean isXterm;
            static final boolean ISATTY;

            static final boolean calcTTY() {
                if (isWindows && isXterm) {
                    return true;
                }
                try {
                    return System.class.getDeclaredMethod("console", new Class[0]).invoke(null, new Object[0]) != null;
                }
                catch (Throwable throwable) {
                    return true;
                }
            }

            private static boolean ansiPossible() {
                return ISATTY && (!isWindows || isXterm);
            }

            public final boolean enabled() {
                if (this == ON) {
                    return true;
                }
                if (this == OFF) {
                    return false;
                }
                if (System.getProperty("picocli.ansi") == null) {
                    return Ansi.ansiPossible();
                }
                return Boolean.getBoolean("picocli.ansi");
            }

            public final Text apply(String string, List<IStyle> iStyleArray) {
                if (string.length() == 0) {
                    return new Text(0);
                }
                Text text = new Text(string.length());
                iStyleArray = iStyleArray.toArray(new IStyle[iStyleArray.size()]);
                text.sections.add(new StyledSection(0, string.length(), Style.on(iStyleArray), Style.off(Ansi.reverse(iStyleArray)) + Style.reset.off()));
                text.plain.append(string);
                Text text2 = text;
                text2.length = text2.plain.length();
                return text;
            }

            private static <T> T[] reverse(T[] TArray) {
                for (int i2 = 0; i2 < TArray.length / 2; ++i2) {
                    T t = TArray[i2];
                    TArray[i2] = TArray[TArray.length - i2 - 1];
                    TArray[TArray.length - i2 - 1] = t;
                }
                return TArray;
            }

            static {
                Ansi ansi = OFF;
                ((Object)((Object)ansi)).getClass();
                EMPTY_TEXT = ansi.new Text(0);
                isWindows = System.getProperty("os.name").startsWith("Windows");
                isXterm = System.getenv("TERM") != null && System.getenv("TERM").startsWith("xterm");
                ISATTY = Ansi.calcTTY();
            }

            public class Text
            implements Cloneable {
                private final int maxLength;
                private int from;
                private int length;
                private StringBuilder plain = new StringBuilder();
                private List<StyledSection> sections = new ArrayList<StyledSection>();

                public Text(int n) {
                    this.maxLength = n;
                }

                public Text(String string) {
                    this.maxLength = -1;
                    this.plain.setLength(0);
                    int n = 0;
                    while (true) {
                        int n2;
                        if ((n2 = string.indexOf("@|", n)) == -1) {
                            if (n == 0) {
                                this.plain.append(string);
                                this.length = this.plain.length();
                                return;
                            }
                            this.plain.append(string.substring(n, string.length()));
                            this.length = this.plain.length();
                            return;
                        }
                        this.plain.append(string.substring(n, n2));
                        n = string.indexOf("|@", n2);
                        if (n == -1) {
                            this.plain.append(string);
                            this.length = this.plain.length();
                            return;
                        }
                        String[] stringArray = string.substring(n2 += 2, n).split(" ", 2);
                        if (stringArray.length == 1) {
                            this.plain.append(string);
                            this.length = this.plain.length();
                            return;
                        }
                        Object[] objectArray = Style.parse(stringArray[0]);
                        Text text = this;
                        text.addStyledSection(text.plain.length(), stringArray[1].length(), Style.on((IStyle[])objectArray), Style.off((IStyle[])Ansi.reverse(objectArray)) + Style.reset.off());
                        this.plain.append(stringArray[1]);
                        n += 2;
                    }
                }

                private void addStyledSection(int n, int n2, String string, String string2) {
                    this.sections.add(new StyledSection(n, n2, string, string2));
                }

                public Object clone() {
                    try {
                        return super.clone();
                    }
                    catch (CloneNotSupportedException cloneNotSupportedException) {
                        throw new IllegalStateException(cloneNotSupportedException);
                    }
                }

                public Text[] splitLines() {
                    ArrayList<Text> arrayList = new ArrayList<Text>();
                    boolean bl = false;
                    int n = 0;
                    int n2 = 0;
                    int n3 = 0;
                    while (n3 < this.plain.length()) {
                        char c2 = this.plain.charAt(n3);
                        if (c2 == '\n' | (c2 == '\r' && n3 + 1 < this.plain.length() && this.plain.charAt(n3 + 1) == '\n' && ++n3 > 0) | c2 == '\r') {
                            arrayList.add(this.substring(n, n2));
                            bl = n3 == this.plain.length() - 1;
                            n = n3 + 1;
                        }
                        n2 = ++n3;
                    }
                    if (n < this.plain.length() || bl) {
                        arrayList.add(this.substring(n, this.plain.length()));
                    }
                    ArrayList<Text> arrayList2 = arrayList;
                    return arrayList2.toArray(new Text[arrayList2.size()]);
                }

                public Text substring(int n) {
                    return this.substring(n, this.length);
                }

                public Text substring(int n, int n2) {
                    Text text = (Text)this.clone();
                    ((Text)this.clone()).from = this.from + n;
                    text.length = n2 - n;
                    return text;
                }

                public Text append(String string) {
                    return this.append(new Text(string));
                }

                public Text append(Text text) {
                    Text text2 = (Text)this.clone();
                    ((Text)this.clone()).plain = new StringBuilder(this.plain.toString().substring(this.from, this.from + this.length));
                    text2.from = 0;
                    text2.sections = new ArrayList<StyledSection>();
                    Iterator<StyledSection> iterator = this.sections.iterator();
                    while (iterator.hasNext()) {
                        StyledSection styledSection;
                        StyledSection styledSection2 = styledSection = iterator.next();
                        text2.sections.add(styledSection2.withStartIndex(styledSection2.startIndex - this.from));
                    }
                    text2.plain.append(text.plain.toString().substring(text.from, text.from + text.length));
                    for (StyledSection styledSection : text.sections) {
                        int n = text2.length + styledSection.startIndex - text.from;
                        text2.sections.add(styledSection.withStartIndex(n));
                    }
                    text2.length = text2.plain.length();
                    return text2;
                }

                public void getStyledChars(int n, int n2, Text text, int n3) {
                    if (text.length < n3) {
                        for (int i2 = text.length; i2 < n3; ++i2) {
                            text.plain.append(' ');
                        }
                        text.length = n3;
                    }
                    Iterator<StyledSection> iterator = this.sections.iterator();
                    while (iterator.hasNext()) {
                        StyledSection styledSection;
                        StyledSection styledSection2 = styledSection = iterator.next();
                        text.sections.add(styledSection2.withStartIndex(styledSection2.startIndex - n + text.length));
                    }
                    int n4 = n;
                    text.plain.append(this.plain.toString().substring(n4, n4 + n2));
                    text.length = text.plain.length();
                }

                public String plainString() {
                    return this.plain.toString().substring(this.from, this.from + this.length);
                }

                public boolean equals(Object object) {
                    return this.toString().equals(String.valueOf(object));
                }

                public int hashCode() {
                    return this.toString().hashCode();
                }

                public String toString() {
                    if (!Ansi.this.enabled()) {
                        return this.plain.toString().substring(this.from, this.from + this.length);
                    }
                    if (this.length == 0) {
                        return "";
                    }
                    StringBuilder stringBuilder = new StringBuilder(this.plain.length() + 20 * this.sections.size());
                    StyledSection styledSection = null;
                    int n = Math.min(this.from + this.length, this.plain.length());
                    for (int i2 = this.from; i2 < n; ++i2) {
                        StyledSection styledSection2 = this.findSectionContaining(i2);
                        if (styledSection2 != styledSection) {
                            if (styledSection != null) {
                                stringBuilder.append(styledSection.endStyles);
                            }
                            if (styledSection2 != null) {
                                stringBuilder.append(styledSection2.startStyles);
                            }
                            styledSection = styledSection2;
                        }
                        stringBuilder.append(this.plain.charAt(i2));
                    }
                    if (styledSection != null) {
                        stringBuilder.append(styledSection.endStyles);
                    }
                    return stringBuilder.toString();
                }

                private StyledSection findSectionContaining(int n) {
                    for (StyledSection styledSection : this.sections) {
                        if (n < styledSection.startIndex || n >= styledSection.startIndex + styledSection.length) continue;
                        return styledSection;
                    }
                    return null;
                }
            }

            static class StyledSection {
                int startIndex;
                int length;
                String startStyles;
                String endStyles;

                StyledSection(int n, int n2, String string, String string2) {
                    this.startIndex = n;
                    this.length = n2;
                    this.startStyles = string;
                    this.endStyles = string2;
                }

                StyledSection withStartIndex(int n) {
                    return new StyledSection(n, this.length, this.startStyles, this.endStyles);
                }
            }

            static class Palette256Color
            implements IStyle {
                private final int fgbg;
                private final int color;

                Palette256Color(boolean bl, String string) {
                    this.fgbg = bl ? 38 : 48;
                    String[] stringArray = string.split(";");
                    if (stringArray.length == 3) {
                        this.color = 16 + 36 * Integer.decode(stringArray[0]) + 6 * Integer.decode(stringArray[1]) + Integer.decode(stringArray[2]);
                        return;
                    }
                    this.color = Integer.decode(string);
                }

                @Override
                public String on() {
                    return String.format("\u001b[%d;5;%dm", this.fgbg, this.color);
                }

                @Override
                public String off() {
                    return "\u001b[" + (this.fgbg + 1) + "m";
                }
            }

            public static enum Style implements IStyle
            {
                reset(0, 0),
                bold(1, 21),
                faint(2, 22),
                italic(3, 23),
                underline(4, 24),
                blink(5, 25),
                reverse(7, 27),
                fg_black(30, 39),
                fg_red(31, 39),
                fg_green(32, 39),
                fg_yellow(33, 39),
                fg_blue(34, 39),
                fg_magenta(35, 39),
                fg_cyan(36, 39),
                fg_white(37, 39),
                bg_black(40, 49),
                bg_red(41, 49),
                bg_green(42, 49),
                bg_yellow(43, 49),
                bg_blue(44, 49),
                bg_magenta(45, 49),
                bg_cyan(46, 49),
                bg_white(47, 49);

                private final int startCode;
                private final int endCode;

                private Style(int n2, int n3) {
                    this.startCode = n2;
                    this.endCode = n3;
                }

                @Override
                public final String on() {
                    return "\u001b[" + this.startCode + "m";
                }

                @Override
                public final String off() {
                    return "\u001b[" + this.endCode + "m";
                }

                public static String on(IStyle ... iStyleArray) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (IStyle iStyle : iStyleArray) {
                        stringBuilder.append(iStyle.on());
                    }
                    return stringBuilder.toString();
                }

                public static String off(IStyle ... iStyleArray) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (IStyle iStyle : iStyleArray) {
                        stringBuilder.append(iStyle.off());
                    }
                    return stringBuilder.toString();
                }

                public static IStyle fg(String string) {
                    try {
                        return Style.valueOf(string.toLowerCase(Locale.ENGLISH));
                    }
                    catch (Exception exception) {
                        try {
                            return Style.valueOf("fg_" + string.toLowerCase(Locale.ENGLISH));
                        }
                        catch (Exception exception2) {
                            return new Palette256Color(true, string);
                        }
                    }
                }

                public static IStyle bg(String string) {
                    try {
                        return Style.valueOf(string.toLowerCase(Locale.ENGLISH));
                    }
                    catch (Exception exception) {
                        try {
                            return Style.valueOf("bg_" + string.toLowerCase(Locale.ENGLISH));
                        }
                        catch (Exception exception2) {
                            return new Palette256Color(false, string);
                        }
                    }
                }

                public static IStyle[] parse(String stringArray) {
                    stringArray = stringArray.split(",");
                    IStyle[] iStyleArray = new IStyle[stringArray.length];
                    for (int i2 = 0; i2 < stringArray.length; ++i2) {
                        int n;
                        if (stringArray[i2].toLowerCase(Locale.ENGLISH).startsWith("fg(")) {
                            n = stringArray[i2].indexOf(41);
                            iStyleArray[i2] = Style.fg(stringArray[i2].substring(3, n < 0 ? stringArray[i2].length() : n));
                            continue;
                        }
                        if (stringArray[i2].toLowerCase(Locale.ENGLISH).startsWith("bg(")) {
                            n = stringArray[i2].indexOf(41);
                            iStyleArray[i2] = Style.bg(stringArray[i2].substring(3, n < 0 ? stringArray[i2].length() : n));
                            continue;
                        }
                        iStyleArray[i2] = Style.fg(stringArray[i2]);
                    }
                    return iStyleArray;
                }
            }

            public static interface IStyle {
                public static final String CSI = "\u001b[";

                public String on();

                public String off();
            }
        }

        public static class ColorScheme {
            public final List<Ansi.IStyle> commandStyles = new ArrayList<Ansi.IStyle>();
            public final List<Ansi.IStyle> optionStyles = new ArrayList<Ansi.IStyle>();
            public final List<Ansi.IStyle> parameterStyles = new ArrayList<Ansi.IStyle>();
            public final List<Ansi.IStyle> optionParamStyles = new ArrayList<Ansi.IStyle>();
            private final Ansi ansi;

            public ColorScheme() {
                this(Ansi.AUTO);
            }

            public ColorScheme(Ansi ansi) {
                this.ansi = Assert.notNull(ansi, "ansi");
            }

            public ColorScheme commands(Ansi.IStyle ... iStyleArray) {
                ColorScheme colorScheme = this;
                return colorScheme.addAll(colorScheme.commandStyles, iStyleArray);
            }

            public ColorScheme options(Ansi.IStyle ... iStyleArray) {
                ColorScheme colorScheme = this;
                return colorScheme.addAll(colorScheme.optionStyles, iStyleArray);
            }

            public ColorScheme parameters(Ansi.IStyle ... iStyleArray) {
                ColorScheme colorScheme = this;
                return colorScheme.addAll(colorScheme.parameterStyles, iStyleArray);
            }

            public ColorScheme optionParams(Ansi.IStyle ... iStyleArray) {
                ColorScheme colorScheme = this;
                return colorScheme.addAll(colorScheme.optionParamStyles, iStyleArray);
            }

            public Ansi.Text commandText(String string) {
                return this.ansi().apply(string, this.commandStyles);
            }

            public Ansi.Text optionText(String string) {
                return this.ansi().apply(string, this.optionStyles);
            }

            public Ansi.Text parameterText(String string) {
                return this.ansi().apply(string, this.parameterStyles);
            }

            public Ansi.Text optionParamText(String string) {
                return this.ansi().apply(string, this.optionParamStyles);
            }

            public ColorScheme applySystemProperties() {
                ColorScheme colorScheme = this;
                colorScheme.replace(colorScheme.commandStyles, System.getProperty("picocli.color.commands"));
                ColorScheme colorScheme2 = this;
                colorScheme2.replace(colorScheme2.optionStyles, System.getProperty("picocli.color.options"));
                ColorScheme colorScheme3 = this;
                colorScheme3.replace(colorScheme3.parameterStyles, System.getProperty("picocli.color.parameters"));
                ColorScheme colorScheme4 = this;
                colorScheme4.replace(colorScheme4.optionParamStyles, System.getProperty("picocli.color.optionParams"));
                return this;
            }

            private void replace(List<Ansi.IStyle> list, String string) {
                if (string != null) {
                    list.clear();
                    this.addAll(list, Ansi.Style.parse(string));
                }
            }

            private ColorScheme addAll(List<Ansi.IStyle> list, Ansi.IStyle ... iStyleArray) {
                list.addAll(Arrays.asList(iStyleArray));
                return this;
            }

            public Ansi ansi() {
                return this.ansi;
            }
        }

        public static class Column {
            public final int width;
            public final int indent;
            public final Overflow overflow;

            public Column(int n, int n2, Overflow overflow) {
                this.width = n;
                this.indent = n2;
                this.overflow = Assert.notNull(overflow, "overflow");
            }

            public static enum Overflow {
                TRUNCATE,
                SPAN,
                WRAP;

            }
        }

        public static class TextTable {
            public final Column[] columns;
            protected final List<Ansi.Text> columnValues = new ArrayList<Ansi.Text>();
            public int indentWrappedLines = 2;
            private final Ansi ansi;

            public TextTable(Ansi ansi) {
                this(ansi, new Column(2, 0, Column.Overflow.TRUNCATE), new Column(2, 0, Column.Overflow.TRUNCATE), new Column(1, 0, Column.Overflow.TRUNCATE), new Column(24, 1, Column.Overflow.SPAN), new Column(51, 1, Column.Overflow.WRAP));
            }

            public TextTable(Ansi ansi, int ... nArray) {
                this.ansi = Assert.notNull(ansi, "ansi");
                this.columns = new Column[nArray.length];
                for (int i2 = 0; i2 < nArray.length; ++i2) {
                    this.columns[i2] = new Column(nArray[i2], 0, i2 == nArray.length - 1 ? Column.Overflow.SPAN : Column.Overflow.WRAP);
                }
            }

            public TextTable(Ansi ansi, Column ... columnArray) {
                this.ansi = Assert.notNull(ansi, "ansi");
                this.columns = Assert.notNull(columnArray, "columns");
                if (columnArray.length == 0) {
                    throw new IllegalArgumentException("At least one column is required");
                }
            }

            public Ansi.Text textAt(int n, int n2) {
                return this.columnValues.get(n2 + n * this.columns.length);
            }

            @Deprecated
            public Ansi.Text cellAt(int n, int n2) {
                return this.textAt(n, n2);
            }

            public int rowCount() {
                return this.columnValues.size() / this.columns.length;
            }

            public void addEmptyRow() {
                for (int i2 = 0; i2 < this.columns.length; ++i2) {
                    Ansi ansi = this.ansi;
                    ((Object)((Object)ansi)).getClass();
                    this.columnValues.add(ansi.new Ansi.Text(this.columns[i2].width));
                }
            }

            public void addRowValues(String ... stringArray) {
                Ansi.Text[] textArray = new Ansi.Text[stringArray.length];
                for (int i2 = 0; i2 < textArray.length; ++i2) {
                    Ansi.Text text;
                    if (stringArray[i2] == null) {
                        text = Ansi.EMPTY_TEXT;
                    } else {
                        Ansi ansi = this.ansi;
                        ((Object)((Object)ansi)).getClass();
                        text = ansi.new Ansi.Text(stringArray[i2]);
                    }
                    textArray[i2] = text;
                }
                this.addRowValues(textArray);
            }

            public void addRowValues(Ansi.Text ... textArray) {
                if (textArray.length > this.columns.length) {
                    throw new IllegalArgumentException(textArray.length + " values don't fit in " + this.columns.length + " columns");
                }
                this.addEmptyRow();
                for (int i2 = 0; i2 < textArray.length; ++i2) {
                    int n = this.rowCount() - 1;
                    Cell cell = this.putValue(n, i2, textArray[i2]);
                    if (cell.row == n && cell.column == i2 || i2 == textArray.length - 1) continue;
                    this.addEmptyRow();
                }
            }

            public Cell putValue(int n, int n2, Ansi.Text text) {
                if (n > this.rowCount() - 1) {
                    throw new IllegalArgumentException("Cannot write to row " + n + ": rowCount=" + this.rowCount());
                }
                if (text == null || text.plain.length() == 0) {
                    return new Cell(n2, n);
                }
                Column column = this.columns[n2];
                int n3 = column.indent;
                switch (column.overflow) {
                    case TRUNCATE: {
                        TextTable.copy(text, this.textAt(n, n2), n3);
                        return new Cell(n2, n);
                    }
                    case SPAN: {
                        int n4 = n2;
                        do {
                            n3 = n2 == this.columns.length - 1 ? this.copy(BreakIterator.getLineInstance(), text, this.textAt(n, n2), n3) : TextTable.copy(text, this.textAt(n, n2), n3);
                            text = text.substring(n3);
                            n3 = 0;
                            if (text.length > 0) {
                                ++n2;
                            }
                            if (text.length <= 0 || n2 < this.columns.length) continue;
                            this.addEmptyRow();
                            ++n;
                            n2 = n4;
                            n3 = column.indent + this.indentWrappedLines;
                        } while (text.length > 0);
                        return new Cell(n2, n);
                    }
                    case WRAP: {
                        BreakIterator breakIterator = BreakIterator.getLineInstance();
                        do {
                            n3 = this.copy(breakIterator, text, this.textAt(n, n2), n3);
                            text = text.substring(n3);
                            n3 = column.indent + this.indentWrappedLines;
                            if (text.length <= 0) continue;
                            ++n;
                            this.addEmptyRow();
                        } while (text.length > 0);
                        return new Cell(n2, n);
                    }
                }
                throw new IllegalStateException(column.overflow.toString());
            }

            private static int length(Ansi.Text text) {
                return text.length;
            }

            private int copy(BreakIterator breakIterator, Ansi.Text text, Ansi.Text text2, int n) {
                breakIterator.setText(text.plainString().replace("-", "\u00ff"));
                int n2 = 0;
                int n3 = breakIterator.first();
                int n4 = breakIterator.next();
                while (n4 != -1) {
                    Ansi.Text text3 = text.substring(n3, n4);
                    if (text2.maxLength < n + n2 + TextTable.length(text3)) break;
                    n2 += TextTable.copy(text3, text2, n + n2);
                    n3 = n4;
                    n4 = breakIterator.next();
                }
                if (n2 == 0 && TextTable.length(text) > text2.maxLength) {
                    n2 = TextTable.copy(text, text2, n);
                }
                return n2;
            }

            private static int copy(Ansi.Text text, Ansi.Text text2, int n) {
                int n2 = Math.min(text.length, text2.maxLength - n);
                Ansi.Text text3 = text;
                text3.getStyledChars(text3.from, n2, text2, n);
                return n2;
            }

            public StringBuilder toString(StringBuilder stringBuilder) {
                int n = this.columns.length;
                StringBuilder stringBuilder2 = new StringBuilder(80);
                for (int i2 = 0; i2 < this.columnValues.size(); ++i2) {
                    int n2;
                    Ansi.Text text = this.columnValues.get(i2);
                    stringBuilder2.append(text.toString());
                    stringBuilder2.append(new String(Help.spaces(this.columns[i2 % n].width - text.length)));
                    if (i2 % n != n - 1) continue;
                    for (n2 = stringBuilder2.length() - 1; n2 >= 0 && stringBuilder2.charAt(n2) == ' '; --n2) {
                    }
                    stringBuilder2.setLength(n2 + 1);
                    stringBuilder.append(stringBuilder2.toString()).append(System.getProperty("line.separator"));
                    stringBuilder2.setLength(0);
                }
                return stringBuilder;
            }

            public String toString() {
                return this.toString(new StringBuilder()).toString();
            }

            public static class Cell {
                public final int column;
                public final int row;

                public Cell(int n, int n2) {
                    this.column = n;
                    this.row = n2;
                }
            }
        }

        static class SortByOptionArityAndNameAlphabetically
        extends SortByShortestOptionNameAlphabetically {
            SortByOptionArityAndNameAlphabetically() {
            }

            @Override
            public int compare(Field field, Field field2) {
                field.getAnnotation(Option.class);
                field2.getAnnotation(Option.class);
                Range range = Range.optionArity(field);
                Range range2 = Range.optionArity(field2);
                int n = range.max - range2.max;
                if (n == 0) {
                    n = range.min - range2.min;
                }
                if (n == 0) {
                    if (CommandLine.isMultiValue(field) && !CommandLine.isMultiValue(field2)) {
                        n = 1;
                    }
                    if (!CommandLine.isMultiValue(field) && CommandLine.isMultiValue(field2)) {
                        n = -1;
                    }
                }
                if (n == 0) {
                    return super.compare(field, field2);
                }
                return n;
            }
        }

        static class SortByShortestOptionNameAlphabetically
        implements Comparator<Field> {
            SortByShortestOptionNameAlphabetically() {
            }

            @Override
            public int compare(Field object, Field object2) {
                object = ((Field)object).getAnnotation(Option.class);
                object2 = ((Field)object2).getAnnotation(Option.class);
                if (object == null) {
                    return 1;
                }
                if (object2 == null) {
                    return -1;
                }
                String[] stringArray = ShortestFirst.sort(object.names());
                String[] stringArray2 = ShortestFirst.sort(object2.names());
                int n = stringArray[0].toUpperCase().compareTo(stringArray2[0].toUpperCase());
                int n2 = n = n == 0 ? -stringArray[0].compareTo(stringArray2[0]) : n;
                if (object.help() == object2.help()) {
                    return n;
                }
                if (object2.help()) {
                    return -1;
                }
                return 1;
            }
        }

        static class ShortestFirst
        implements Comparator<String> {
            ShortestFirst() {
            }

            @Override
            public int compare(String string, String string2) {
                return string.length() - string2.length();
            }

            public static String[] sort(String[] stringArray) {
                Arrays.sort(stringArray, new ShortestFirst());
                return stringArray;
            }
        }

        public static class Layout {
            protected final ColorScheme colorScheme;
            protected final TextTable table;
            protected IOptionRenderer optionRenderer;
            protected IParameterRenderer parameterRenderer;

            public Layout(ColorScheme colorScheme) {
                this(colorScheme, new TextTable(colorScheme.ansi()));
            }

            public Layout(ColorScheme colorScheme, TextTable textTable) {
                this(colorScheme, textTable, new DefaultOptionRenderer(), new DefaultParameterRenderer());
            }

            public Layout(ColorScheme colorScheme, TextTable textTable, IOptionRenderer iOptionRenderer, IParameterRenderer iParameterRenderer) {
                this.colorScheme = Assert.notNull(colorScheme, "colorScheme");
                this.table = Assert.notNull(textTable, "textTable");
                this.optionRenderer = Assert.notNull(iOptionRenderer, "optionRenderer");
                this.parameterRenderer = Assert.notNull(iParameterRenderer, "parameterRenderer");
            }

            public void layout(Field textArray, Ansi.Text[][] textArray2) {
                textArray = textArray2;
                int n = textArray2.length;
                for (int i2 = 0; i2 < n; ++i2) {
                    Ansi.Text[] textArray3 = textArray[i2];
                    this.table.addRowValues(textArray3);
                }
            }

            public void addOptions(List<Field> object, IParamLabelRenderer iParamLabelRenderer) {
                object = object.iterator();
                while (object.hasNext()) {
                    Field field = (Field)object.next();
                    if (field.getAnnotation(Option.class).hidden()) continue;
                    this.addOption(field, iParamLabelRenderer);
                }
            }

            public void addOption(Field field, IParamLabelRenderer textArray) {
                Option option = field.getAnnotation(Option.class);
                textArray = this.optionRenderer.render(option, field, (IParamLabelRenderer)textArray, this.colorScheme);
                this.layout(field, textArray);
            }

            public void addPositionalParameters(List<Field> object, IParamLabelRenderer iParamLabelRenderer) {
                object = object.iterator();
                while (object.hasNext()) {
                    Field field = (Field)object.next();
                    if (field.getAnnotation(Parameters.class).hidden()) continue;
                    this.addPositionalParameter(field, iParamLabelRenderer);
                }
            }

            public void addPositionalParameter(Field field, IParamLabelRenderer textArray) {
                Parameters parameters = field.getAnnotation(Parameters.class);
                textArray = this.parameterRenderer.render(parameters, field, (IParamLabelRenderer)textArray, this.colorScheme);
                this.layout(field, textArray);
            }

            public String toString() {
                return this.table.toString();
            }
        }

        static class DefaultParamLabelRenderer
        implements IParamLabelRenderer {
            public final String separator;

            public DefaultParamLabelRenderer(String string) {
                this.separator = Assert.notNull(string, "separator");
            }

            @Override
            public String separator() {
                return this.separator;
            }

            @Override
            public Ansi.Text renderParameterLabel(Field object, Ansi ansi, List<Ansi.IStyle> list) {
                Ansi.Text text;
                block6: {
                    int n;
                    String string;
                    Range range;
                    boolean bl;
                    block4: {
                        block5: {
                            bl = ((AccessibleObject)object).isAnnotationPresent(Option.class);
                            range = bl ? Range.optionArity((Field)object) : Range.parameterCapacity((Field)object);
                            String string2 = bl ? ((Field)object).getAnnotation(Option.class).split() : ((Field)object).getAnnotation(Parameters.class).split();
                            Ansi ansi2 = ansi;
                            ((Object)((Object)ansi2)).getClass();
                            text = ansi2.new Ansi.Text("");
                            string = bl ? this.separator : "";
                            object = ansi.apply(DefaultParamLabelRenderer.renderParameterName((Field)object), list);
                            if (!CommandLine.empty(string2)) {
                                object = ((Ansi.Text)object).append("[".concat(String.valueOf(string2))).append((Ansi.Text)object).append("]...");
                            }
                            for (n = 0; n < range.min; ++n) {
                                text = text.append(string).append((Ansi.Text)object);
                                string = " ";
                            }
                            if (!range.isVariable) break block4;
                            if (text.length != 0) break block5;
                            text = text.append(string + "[").append((Ansi.Text)object).append("]...");
                            break block6;
                        }
                        if (text.plainString().endsWith("...")) break block6;
                        text = text.append("...");
                        break block6;
                    }
                    string = text.length == 0 ? (bl ? this.separator : "") : " ";
                    for (n = range.min; n < range.max; ++n) {
                        text = string.trim().length() == 0 ? text.append(string + "[").append((Ansi.Text)object) : text.append("[".concat(String.valueOf(string))).append((Ansi.Text)object);
                        string = " ";
                    }
                    for (n = range.min; n < range.max; ++n) {
                        text = text.append("]");
                    }
                }
                return text;
            }

            private static String renderParameterName(Field classArray) {
                String string = null;
                if (classArray.isAnnotationPresent(Option.class)) {
                    string = classArray.getAnnotation(Option.class).paramLabel();
                } else if (classArray.isAnnotationPresent(Parameters.class)) {
                    string = classArray.getAnnotation(Parameters.class).paramLabel();
                }
                if (string != null && string.trim().length() > 0) {
                    return string.trim();
                }
                string = classArray.getName();
                if (Map.class.isAssignableFrom(classArray.getType())) {
                    string = (classArray = CommandLine.getTypeAttribute((Field)classArray)).length < 2 || classArray[0] == null || classArray[1] == null ? "String=String" : classArray[0].getSimpleName() + Help.DEFAULT_SEPARATOR + classArray[1].getSimpleName();
                }
                return "<" + string + ">";
            }
        }

        public static interface IParamLabelRenderer {
            public Ansi.Text renderParameterLabel(Field var1, Ansi var2, List<Ansi.IStyle> var3);

            public String separator();
        }

        static class DefaultParameterRenderer
        implements IParameterRenderer {
            public String requiredMarker = " ";

            DefaultParameterRenderer() {
            }

            @Override
            public Ansi.Text[][] render(Parameters parameters, Field object, IParamLabelRenderer textArray, ColorScheme colorScheme) {
                int n;
                textArray = textArray.renderParameterLabel((Field)object, colorScheme.ansi(), colorScheme.parameterStyles);
                object = colorScheme.parameterText(Range.parameterArity((Field)object).min > 0 ? this.requiredMarker : "");
                Ansi.Text text = Ansi.EMPTY_TEXT;
                ArrayList<Ansi.Text[]> arrayList = new ArrayList<Ansi.Text[]>();
                Ansi ansi = colorScheme.ansi();
                ((Object)((Object)ansi)).getClass();
                Ansi.Text[] textArray2 = ansi.new Ansi.Text(CommandLine.str(parameters.description(), 0)).splitLines();
                if (textArray2.length == 0) {
                    textArray2 = new Ansi.Text[]{text};
                }
                arrayList.add(new Ansi.Text[]{object, text, text, textArray, textArray2[0]});
                for (n = 1; n < textArray2.length; ++n) {
                    arrayList.add(new Ansi.Text[]{text, text, text, text, textArray2[n]});
                }
                for (n = 1; n < parameters.description().length; ++n) {
                    Ansi ansi2 = colorScheme.ansi();
                    ((Object)((Object)ansi2)).getClass();
                    for (Ansi.Text text2 : ansi2.new Ansi.Text(parameters.description()[n]).splitLines()) {
                        arrayList.add(new Ansi.Text[]{text, text, text, text, text2});
                    }
                }
                ArrayList<Ansi.Text[]> arrayList2 = arrayList;
                return (Ansi.Text[][])arrayList2.toArray((T[])new Ansi.Text[arrayList2.size()][]);
            }
        }

        public static interface IParameterRenderer {
            public Ansi.Text[][] render(Parameters var1, Field var2, IParamLabelRenderer var3, ColorScheme var4);
        }

        static class MinimalParameterRenderer
        implements IParameterRenderer {
            MinimalParameterRenderer() {
            }

            @Override
            public Ansi.Text[][] render(Parameters parameters, Field field, IParamLabelRenderer iParamLabelRenderer, ColorScheme colorScheme) {
                Ansi.Text[][] textArray = new Ansi.Text[1][];
                Ansi.Text[] textArray2 = new Ansi.Text[2];
                textArray2[0] = iParamLabelRenderer.renderParameterLabel(field, colorScheme.ansi(), colorScheme.parameterStyles);
                Ansi ansi = colorScheme.ansi();
                ((Object)((Object)ansi)).getClass();
                textArray2[1] = ansi.new Ansi.Text(parameters.description().length == 0 ? "" : parameters.description()[0]);
                textArray[0] = textArray2;
                return textArray;
            }
        }

        static class MinimalOptionRenderer
        implements IOptionRenderer {
            MinimalOptionRenderer() {
            }

            @Override
            public Ansi.Text[][] render(Option option, Field object, IParamLabelRenderer iParamLabelRenderer, ColorScheme colorScheme) {
                Ansi.Text text = colorScheme.optionText(option.names()[0]);
                object = iParamLabelRenderer.renderParameterLabel((Field)object, colorScheme.ansi(), colorScheme.optionParamStyles);
                text = text.append((Ansi.Text)object);
                Ansi.Text[][] textArray = new Ansi.Text[1][];
                Ansi.Text[] textArray2 = new Ansi.Text[2];
                textArray2[0] = text;
                Ansi ansi = colorScheme.ansi();
                ((Object)((Object)ansi)).getClass();
                textArray2[1] = ansi.new Ansi.Text(option.description().length == 0 ? "" : option.description()[0]);
                textArray[0] = textArray2;
                return textArray;
            }
        }

        static class DefaultOptionRenderer
        implements IOptionRenderer {
            public String requiredMarker = " ";
            public Object command;
            private String sep;
            private boolean showDefault;

            DefaultOptionRenderer() {
            }

            @Override
            public Ansi.Text[][] render(Option option, Field object, IParamLabelRenderer object2, ColorScheme colorScheme) {
                Object object3 = ShortestFirst.sort(option.names());
                int n = object3[0].length() == 2 ? 1 : 0;
                String string = n > 0 ? object3[0] : "";
                this.sep = n > 0 && ((String[])object3).length > 1 ? "," : "";
                object3 = Help.join(object3, n, ((String[])object3).length - n, ", ");
                object2 = this.createLongOptionText((Field)object, (IParamLabelRenderer)object2, colorScheme, (String)object3);
                this.showDefault = this.command != null && !option.help() && !CommandLine.isBoolean(((Field)object).getType());
                object = this.createDefaultValue((Field)object);
                object3 = option.required() ? this.requiredMarker : "";
                return this.renderDescriptionLines(option, colorScheme, (String)object3, string, (Ansi.Text)object2, object);
            }

            private Object createDefaultValue(Field object) {
                Object object2 = null;
                try {
                    object2 = ((Field)object).get(this.command);
                    if (object2 == null) {
                        this.showDefault = false;
                    } else if (((Field)object).getType().isArray()) {
                        object = new StringBuilder();
                        for (int i2 = 0; i2 < Array.getLength(object2); ++i2) {
                            ((StringBuilder)object).append(i2 > 0 ? ", " : "").append(Array.get(object2, i2));
                        }
                        object2 = ((StringBuilder)object).insert(0, "[").append("]").toString();
                    }
                }
                catch (Exception exception) {
                    this.showDefault = false;
                }
                return object2;
            }

            private Ansi.Text createLongOptionText(Field object, IParamLabelRenderer iParamLabelRenderer, ColorScheme colorScheme, String string) {
                if (((Ansi.Text)(object = iParamLabelRenderer.renderParameterLabel((Field)object, colorScheme.ansi(), colorScheme.optionParamStyles))).length > 0 && string.length() == 0) {
                    this.sep = iParamLabelRenderer.separator();
                    int n = ((Ansi.Text)object).plainString().indexOf(this.sep);
                    object = ((Ansi.Text)object).substring(0, n).append(((Ansi.Text)object).substring(n + this.sep.length()));
                }
                return colorScheme.optionText(string).append((Ansi.Text)object);
            }

            private Ansi.Text[][] renderDescriptionLines(Option option, ColorScheme colorScheme, String string, String textArray, Ansi.Text text, Object object) {
                int n;
                Ansi.Text text2 = Ansi.EMPTY_TEXT;
                ArrayList<Ansi.Text[]> arrayList = new ArrayList<Ansi.Text[]>();
                Ansi ansi = colorScheme.ansi();
                ((Object)((Object)ansi)).getClass();
                Ansi.Text[] textArray2 = ansi.new Ansi.Text(CommandLine.str(option.description(), 0)).splitLines();
                if (textArray2.length == 0) {
                    if (this.showDefault) {
                        Ansi.Text[] textArray3 = new Ansi.Text[1];
                        Ansi ansi2 = colorScheme.ansi();
                        ((Object)((Object)ansi2)).getClass();
                        textArray3[0] = ansi2.new Ansi.Text("  Default: ".concat(String.valueOf(object)));
                        textArray2 = textArray3;
                        this.showDefault = false;
                    } else {
                        textArray2 = new Ansi.Text[]{text2};
                    }
                }
                Ansi.Text[] textArray4 = new Ansi.Text[5];
                textArray4[0] = colorScheme.optionText(string);
                textArray4[1] = colorScheme.optionText((String)textArray);
                Ansi ansi3 = colorScheme.ansi();
                ((Object)((Object)ansi3)).getClass();
                textArray4[2] = ansi3.new Ansi.Text(this.sep);
                textArray4[3] = text;
                textArray4[4] = textArray2[0];
                arrayList.add(textArray4);
                for (n = 1; n < textArray2.length; ++n) {
                    arrayList.add(new Ansi.Text[]{text2, text2, text2, text2, textArray2[n]});
                }
                for (n = 1; n < option.description().length; ++n) {
                    Ansi ansi4 = colorScheme.ansi();
                    ((Object)((Object)ansi4)).getClass();
                    for (Ansi.Text text3 : ansi4.new Ansi.Text(option.description()[n]).splitLines()) {
                        arrayList.add(new Ansi.Text[]{text2, text2, text2, text2, text3});
                    }
                }
                if (this.showDefault) {
                    Ansi.Text[] textArray5 = new Ansi.Text[5];
                    textArray5[0] = text2;
                    textArray5[1] = text2;
                    textArray5[2] = text2;
                    textArray5[3] = text2;
                    Ansi ansi5 = colorScheme.ansi();
                    ((Object)((Object)ansi5)).getClass();
                    textArray5[4] = ansi5.new Ansi.Text("  Default: ".concat(String.valueOf(object)));
                    arrayList.add(textArray5);
                }
                ArrayList<Ansi.Text[]> arrayList2 = arrayList;
                return (Ansi.Text[][])arrayList2.toArray((T[])new Ansi.Text[arrayList2.size()][]);
            }
        }

        public static interface IOptionRenderer {
            public Ansi.Text[][] render(Option var1, Field var2, IParamLabelRenderer var3, ColorScheme var4);
        }
    }

    static class BuiltIn {
        private BuiltIn() {
        }

        static class UUIDConverter
        implements ITypeConverter<UUID> {
            UUIDConverter() {
            }

            @Override
            public UUID convert(String string) {
                return UUID.fromString(string);
            }
        }

        static class PatternConverter
        implements ITypeConverter<Pattern> {
            PatternConverter() {
            }

            @Override
            public Pattern convert(String string) {
                return Pattern.compile(string);
            }
        }

        static class InetAddressConverter
        implements ITypeConverter<InetAddress> {
            InetAddressConverter() {
            }

            @Override
            public InetAddress convert(String string) {
                return InetAddress.getByName(string);
            }
        }

        static class CharsetConverter
        implements ITypeConverter<Charset> {
            CharsetConverter() {
            }

            @Override
            public Charset convert(String string) {
                return Charset.forName(string);
            }
        }

        static class BigIntegerConverter
        implements ITypeConverter<BigInteger> {
            BigIntegerConverter() {
            }

            @Override
            public BigInteger convert(String string) {
                return new BigInteger(string);
            }
        }

        static class BigDecimalConverter
        implements ITypeConverter<BigDecimal> {
            BigDecimalConverter() {
            }

            @Override
            public BigDecimal convert(String string) {
                return new BigDecimal(string);
            }
        }

        static class ISO8601TimeConverter
        implements ITypeConverter<Time> {
            ISO8601TimeConverter() {
            }

            @Override
            public Time convert(String string) {
                try {
                    if (string.length() <= 5) {
                        return new Time(new SimpleDateFormat("HH:mm").parse(string).getTime());
                    }
                    if (string.length() <= 8) {
                        return new Time(new SimpleDateFormat("HH:mm:ss").parse(string).getTime());
                    }
                    if (string.length() <= 12) {
                        try {
                            return new Time(new SimpleDateFormat("HH:mm:ss.SSS").parse(string).getTime());
                        }
                        catch (ParseException parseException) {
                            return new Time(new SimpleDateFormat("HH:mm:ss,SSS").parse(string).getTime());
                        }
                    }
                }
                catch (ParseException parseException) {}
                throw new TypeConversionException("'" + string + "' is not a HH:mm[:ss[.SSS]] time");
            }
        }

        static class ISO8601DateConverter
        implements ITypeConverter<Date> {
            ISO8601DateConverter() {
            }

            @Override
            public Date convert(String string) {
                try {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(string);
                }
                catch (ParseException parseException) {
                    throw new TypeConversionException("'" + string + "' is not a yyyy-MM-dd date");
                }
            }
        }

        static class URIConverter
        implements ITypeConverter<URI> {
            URIConverter() {
            }

            @Override
            public URI convert(String string) {
                return new URI(string);
            }
        }

        static class URLConverter
        implements ITypeConverter<URL> {
            URLConverter() {
            }

            @Override
            public URL convert(String string) {
                return new URL(string);
            }
        }

        static class FileConverter
        implements ITypeConverter<File> {
            FileConverter() {
            }

            @Override
            public File convert(String string) {
                return new File(string);
            }
        }

        static class DoubleConverter
        implements ITypeConverter<Double> {
            DoubleConverter() {
            }

            @Override
            public Double convert(String string) {
                return Double.valueOf(string);
            }
        }

        static class FloatConverter
        implements ITypeConverter<Float> {
            FloatConverter() {
            }

            @Override
            public Float convert(String string) {
                return Float.valueOf(string);
            }
        }

        static class LongConverter
        implements ITypeConverter<Long> {
            LongConverter() {
            }

            @Override
            public Long convert(String string) {
                return Long.valueOf(string);
            }
        }

        static class IntegerConverter
        implements ITypeConverter<Integer> {
            IntegerConverter() {
            }

            @Override
            public Integer convert(String string) {
                return Integer.valueOf(string);
            }
        }

        static class ShortConverter
        implements ITypeConverter<Short> {
            ShortConverter() {
            }

            @Override
            public Short convert(String string) {
                return Short.valueOf(string);
            }
        }

        static class CharacterConverter
        implements ITypeConverter<Character> {
            CharacterConverter() {
            }

            @Override
            public Character convert(String string) {
                if (string.length() > 1) {
                    throw new TypeConversionException("'" + string + "' is not a single character");
                }
                return Character.valueOf(string.charAt(0));
            }
        }

        static class BooleanConverter
        implements ITypeConverter<Boolean> {
            BooleanConverter() {
            }

            @Override
            public Boolean convert(String string) {
                if ("true".equalsIgnoreCase(string) || "false".equalsIgnoreCase(string)) {
                    return Boolean.parseBoolean(string);
                }
                throw new TypeConversionException("'" + string + "' is not a boolean");
            }
        }

        static class ByteConverter
        implements ITypeConverter<Byte> {
            ByteConverter() {
            }

            @Override
            public Byte convert(String string) {
                return Byte.valueOf(string);
            }
        }

        static class CharSequenceConverter
        implements ITypeConverter<CharSequence> {
            CharSequenceConverter() {
            }

            @Override
            public String convert(String string) {
                return string;
            }
        }

        static class StringBuilderConverter
        implements ITypeConverter<StringBuilder> {
            StringBuilderConverter() {
            }

            @Override
            public StringBuilder convert(String string) {
                return new StringBuilder(string);
            }
        }

        static class StringConverter
        implements ITypeConverter<String> {
            StringConverter() {
            }

            @Override
            public String convert(String string) {
                return string;
            }
        }

        static class PathConverter
        implements ITypeConverter<Path> {
            PathConverter() {
            }

            @Override
            public Path convert(String string) {
                return Paths.get(string, new String[0]);
            }
        }
    }

    static class PositionalParametersSorter
    implements Comparator<Field> {
        private PositionalParametersSorter() {
        }

        @Override
        public int compare(Field field, Field field2) {
            int n = Range.parameterIndex(field).compareTo(Range.parameterIndex(field2));
            if (n == 0) {
                return Range.parameterArity(field).compareTo(Range.parameterArity(field2));
            }
            return n;
        }
    }

    class Interpreter {
        private final Map<String, CommandLine> commands = new LinkedHashMap<String, CommandLine>();
        private final Map<Class<?>, ITypeConverter<?>> converterRegistry = new HashMap();
        private final Map<String, Field> optionName2Field = new HashMap<String, Field>();
        private final Map<Character, Field> singleCharOption2Field = new HashMap<Character, Field>();
        private final List<Field> requiredFields = new ArrayList<Field>();
        private final List<Field> positionalParametersFields = new ArrayList<Field>();
        private final Object command;
        private boolean isHelpRequested;
        private String separator = "=";
        private int position;

        Interpreter(Object object) {
            this.converterRegistry.put(Path.class, new BuiltIn.PathConverter());
            this.converterRegistry.put(Object.class, new BuiltIn.StringConverter());
            this.converterRegistry.put(String.class, new BuiltIn.StringConverter());
            this.converterRegistry.put(StringBuilder.class, new BuiltIn.StringBuilderConverter());
            this.converterRegistry.put(CharSequence.class, new BuiltIn.CharSequenceConverter());
            this.converterRegistry.put(Byte.class, new BuiltIn.ByteConverter());
            this.converterRegistry.put(Byte.TYPE, new BuiltIn.ByteConverter());
            this.converterRegistry.put(Boolean.class, new BuiltIn.BooleanConverter());
            this.converterRegistry.put(Boolean.TYPE, new BuiltIn.BooleanConverter());
            this.converterRegistry.put(Character.class, new BuiltIn.CharacterConverter());
            this.converterRegistry.put(Character.TYPE, new BuiltIn.CharacterConverter());
            this.converterRegistry.put(Short.class, new BuiltIn.ShortConverter());
            this.converterRegistry.put(Short.TYPE, new BuiltIn.ShortConverter());
            this.converterRegistry.put(Integer.class, new BuiltIn.IntegerConverter());
            this.converterRegistry.put(Integer.TYPE, new BuiltIn.IntegerConverter());
            this.converterRegistry.put(Long.class, new BuiltIn.LongConverter());
            this.converterRegistry.put(Long.TYPE, new BuiltIn.LongConverter());
            this.converterRegistry.put(Float.class, new BuiltIn.FloatConverter());
            this.converterRegistry.put(Float.TYPE, new BuiltIn.FloatConverter());
            this.converterRegistry.put(Double.class, new BuiltIn.DoubleConverter());
            this.converterRegistry.put(Double.TYPE, new BuiltIn.DoubleConverter());
            this.converterRegistry.put(File.class, new BuiltIn.FileConverter());
            this.converterRegistry.put(URI.class, new BuiltIn.URIConverter());
            this.converterRegistry.put(URL.class, new BuiltIn.URLConverter());
            this.converterRegistry.put(Date.class, new BuiltIn.ISO8601DateConverter());
            this.converterRegistry.put(Time.class, new BuiltIn.ISO8601TimeConverter());
            this.converterRegistry.put(BigDecimal.class, new BuiltIn.BigDecimalConverter());
            this.converterRegistry.put(BigInteger.class, new BuiltIn.BigIntegerConverter());
            this.converterRegistry.put(Charset.class, new BuiltIn.CharsetConverter());
            this.converterRegistry.put(InetAddress.class, new BuiltIn.InetAddressConverter());
            this.converterRegistry.put(Pattern.class, new BuiltIn.PatternConverter());
            this.converterRegistry.put(UUID.class, new BuiltIn.UUIDConverter());
            this.command = Assert.notNull(object, "command");
            String string = null;
            String string2 = null;
            boolean bl = false;
            for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
                CommandLine.init(clazz, this.requiredFields, this.optionName2Field, this.singleCharOption2Field, this.positionalParametersFields);
                if (!clazz.isAnnotationPresent(Command.class)) continue;
                bl = true;
                Class<?>[] classArray = clazz.getAnnotation(Command.class);
                string2 = string2 == null ? classArray.separator() : string2;
                string = string == null ? classArray.name() : string;
                CommandLine.this.versionLines.addAll(Arrays.asList(classArray.version()));
                for (Class<?> clazz2 : classArray.subcommands()) {
                    Command command = clazz2.getAnnotation(Command.class);
                    if (command == null || "<main class>".equals(command.name())) {
                        throw new InitializationException("Subcommand " + clazz2.getName() + " is missing the mandatory @Command annotation with a 'name' attribute");
                    }
                    try {
                        Object object2 = clazz2.getDeclaredConstructor(new Class[0]);
                        ((AccessibleObject)object2).setAccessible(true);
                        object2 = CommandLine.toCommandLine(((Constructor)object2).newInstance(new Object[0]));
                        ((CommandLine)object2).parent = CommandLine.this;
                        this.commands.put(command.name(), (CommandLine)object2);
                    }
                    catch (InitializationException initializationException) {
                        throw initializationException;
                    }
                    catch (NoSuchMethodException noSuchMethodException) {
                        throw new InitializationException("Cannot instantiate subcommand " + clazz2.getName() + ": the class has no constructor", noSuchMethodException);
                    }
                    catch (Exception exception) {
                        throw new InitializationException("Could not instantiate and add subcommand " + clazz2.getName() + ": " + exception, exception);
                    }
                }
            }
            this.separator = string2 != null ? string2 : this.separator;
            CommandLine.this.commandName = string != null ? string : CommandLine.this.commandName;
            Collections.sort(this.positionalParametersFields, new PositionalParametersSorter());
            CommandLine.validatePositionalParameters(this.positionalParametersFields);
            if (this.positionalParametersFields.isEmpty() && this.optionName2Field.isEmpty() && !bl) {
                throw new InitializationException(object + " (" + object.getClass() + ") is not a command: it has no @Command, @Option or @Parameters annotations");
            }
        }

        List<CommandLine> parse(String ... stringArray) {
            Assert.notNull(stringArray, "argument array");
            if (CommandLine.this.tracer.isInfo()) {
                CommandLine.this.tracer.info("Parsing %d command line args %s%n", stringArray.length, Arrays.toString(stringArray));
            }
            Stack<String> stack = new Stack<String>();
            for (int i2 = stringArray.length - 1; i2 >= 0; --i2) {
                stack.push(stringArray[i2]);
            }
            ArrayList<CommandLine> arrayList = new ArrayList<CommandLine>();
            this.parse(arrayList, stack, stringArray);
            return arrayList;
        }

        private void parse(List<CommandLine> object, Stack<String> object2, String[] stringArray) {
            this.isHelpRequested = false;
            CommandLine.this.versionHelpRequested = false;
            CommandLine.this.usageHelpRequested = false;
            Serializable serializable = this.command.getClass();
            if (CommandLine.this.tracer.isDebug()) {
                CommandLine.this.tracer.debug("Initializing %s: %d options, %d positional parameters, %d required, %d subcommands.%n", ((Class)serializable).getName(), new HashSet<Field>(this.optionName2Field.values()).size(), this.positionalParametersFields.size(), this.requiredFields.size(), this.commands.size());
            }
            object.add((CommandLine)CommandLine.this);
            serializable = new ArrayList<Field>(this.requiredFields);
            Object object3 = new HashSet<Field>();
            Collections.sort(serializable, new PositionalParametersSorter());
            try {
                this.processArguments((List<CommandLine>)object, (Stack<String>)object2, (Collection<Field>)((Object)serializable), (Set<Field>)object3, stringArray);
            }
            catch (ParameterException parameterException) {
                throw parameterException;
            }
            catch (Exception exception) {
                int n = stringArray.length - ((Vector)object2).size() - 1;
                object2 = n >= 0 && n < stringArray.length ? stringArray[n] : "?";
                throw ParameterException.create(CommandLine.this, exception, (String)object2, n, stringArray);
            }
            if (!this.isAnyHelpRequested() && !serializable.isEmpty()) {
                object = serializable.iterator();
                while (object.hasNext()) {
                    object3 = (Field)object.next();
                    if (((AccessibleObject)object3).isAnnotationPresent(Option.class)) {
                        throw MissingParameterException.create(CommandLine.this, (Collection)((Object)serializable), this.separator);
                    }
                    Object object4 = object3;
                    this.assertNoMissingParameters((Field)object4, Range.parameterArity((Field)object4).min, (Stack<String>)object2);
                }
            }
            if (!CommandLine.this.unmatchedArguments.isEmpty()) {
                if (!CommandLine.this.isUnmatchedArgumentsAllowed()) {
                    throw new UnmatchedArgumentException(CommandLine.this, CommandLine.this.unmatchedArguments);
                }
                if (CommandLine.this.tracer.isWarn()) {
                    CommandLine.this.tracer.warn("Unmatched arguments: %s%n", CommandLine.this.unmatchedArguments);
                }
            }
        }

        private void processArguments(List<CommandLine> list, Stack<String> stack, Collection<Field> collection, Set<Field> set, String[] stringArray) {
            while (!stack.isEmpty()) {
                String string = stack.pop();
                if (CommandLine.this.tracer.isDebug()) {
                    CommandLine.this.tracer.debug("Processing argument '%s'. Remainder=%s%n", string, CommandLine.reverse((Stack)stack.clone()));
                }
                if ("--".equals(string)) {
                    CommandLine.this.tracer.info("Found end-of-options delimiter '--'. Treating remainder as positional parameters.%n", new Object[0]);
                    this.processRemainderAsPositionalParameters(collection, set, stack);
                    return;
                }
                if (this.commands.containsKey(string)) {
                    if (!this.isHelpRequested && !collection.isEmpty()) {
                        throw MissingParameterException.create(CommandLine.this, collection, this.separator);
                    }
                    if (CommandLine.this.tracer.isDebug()) {
                        CommandLine.this.tracer.debug("Found subcommand '%s' (%s)%n", string, ((CommandLine)this.commands.get((Object)string)).interpreter.command.getClass().getName());
                    }
                    this.commands.get(string).interpreter.parse(list, stack, stringArray);
                    return;
                }
                boolean bl = false;
                int n = string.indexOf(this.separator);
                if (n > 0) {
                    String string2 = string.substring(0, n);
                    if (this.optionName2Field.containsKey(string2) && !this.optionName2Field.containsKey(string)) {
                        bl = true;
                        String string3 = string.substring(n + this.separator.length());
                        stack.push(string3);
                        string = string2;
                        if (CommandLine.this.tracer.isDebug()) {
                            CommandLine.this.tracer.debug("Separated '%s' option from '%s' option parameter%n", string2, string3);
                        }
                    } else if (CommandLine.this.tracer.isDebug()) {
                        CommandLine.this.tracer.debug("'%s' contains separator '%s' but '%s' is not a known option%n", string, this.separator, string2);
                    }
                } else if (CommandLine.this.tracer.isDebug()) {
                    CommandLine.this.tracer.debug("'%s' cannot be separated into <option>%s<option-parameter>%n", string, this.separator);
                }
                if (this.optionName2Field.containsKey(string)) {
                    this.processStandaloneOption(collection, set, string, stack, bl);
                    continue;
                }
                if (string.length() > 2 && string.startsWith("-")) {
                    if (CommandLine.this.tracer.isDebug()) {
                        CommandLine.this.tracer.debug("Trying to process '%s' as clustered short options%n", string, stack);
                    }
                    this.processClusteredShortOptions(collection, set, string, stack);
                    continue;
                }
                stack.push(string);
                if (CommandLine.this.tracer.isDebug()) {
                    CommandLine.this.tracer.debug("Could not find option '%s', deciding whether to treat as unmatched option or positional parameter...%n", string);
                }
                if (this.resemblesOption(string)) {
                    this.handleUnmatchedArguments(stack.pop());
                    continue;
                }
                if (CommandLine.this.tracer.isDebug()) {
                    CommandLine.this.tracer.debug("No option named '%s' found. Processing remainder as positional parameters%n", string);
                }
                this.processPositionalParameter(collection, set, stack);
            }
        }

        private boolean resemblesOption(String string) {
            boolean bl;
            int n = 0;
            for (String string2 : this.optionName2Field.keySet()) {
                for (int i2 = 0; i2 < string.length() && string2.length() > i2 && string.charAt(i2) == string2.charAt(i2); ++i2) {
                    ++n;
                }
            }
            boolean bl2 = bl = n > 0 && n * 10 >= this.optionName2Field.size() * 9;
            if (CommandLine.this.tracer.isDebug()) {
                CommandLine.this.tracer.debug("%s %s an option: %d matching prefix chars out of %d option names%n", string, bl ? "resembles" : "doesn't resemble", n, this.optionName2Field.size());
            }
            return bl;
        }

        private void handleUnmatchedArguments(String string) {
            Stack<String> stack = new Stack<String>();
            stack.add(string);
            this.handleUnmatchedArguments(stack);
        }

        private void handleUnmatchedArguments(Stack<String> stack) {
            while (!stack.isEmpty()) {
                CommandLine.this.unmatchedArguments.add(stack.pop());
            }
        }

        private void processRemainderAsPositionalParameters(Collection<Field> collection, Set<Field> set, Stack<String> stack) {
            while (!stack.empty()) {
                this.processPositionalParameter(collection, set, stack);
            }
        }

        private void processPositionalParameter(Collection<Field> collection, Set<Field> set, Stack<String> stack) {
            if (CommandLine.this.tracer.isDebug()) {
                CommandLine.this.tracer.debug("Processing next arg as a positional parameter at index=%d. Remainder=%s%n", this.position, CommandLine.reverse((Stack)stack.clone()));
            }
            int n = 0;
            for (Field field : this.positionalParametersFields) {
                Range range = Range.parameterIndex(field);
                if (!range.contains(this.position)) continue;
                Stack stack2 = (Stack)stack.clone();
                Range range2 = Range.parameterArity(field);
                if (CommandLine.this.tracer.isDebug()) {
                    CommandLine.this.tracer.debug("Position %d is in index range %s. Trying to assign args to %s, arity=%s%n", this.position, range, field, range2);
                }
                this.assertNoMissingParameters(field, range2.min, stack2);
                int n2 = stack2.size();
                this.applyOption(field, Parameters.class, range2, false, stack2, set, "args[" + range + "] at position " + this.position);
                int n3 = n2 - stack2.size();
                if (n3 > 0) {
                    collection.remove(field);
                }
                n = Math.max(n, n3);
            }
            for (int i2 = 0; i2 < n; ++i2) {
                stack.pop();
            }
            this.position += n;
            if (CommandLine.this.tracer.isDebug()) {
                CommandLine.this.tracer.debug("Consumed %d arguments, moving position to index %d.%n", n, this.position);
            }
            if (n == 0 && !stack.isEmpty()) {
                this.handleUnmatchedArguments(stack.pop());
            }
        }

        private void processStandaloneOption(Collection<Field> object, Set<Field> set, String string, Stack<String> stack, boolean bl) {
            Field field = this.optionName2Field.get(string);
            object.remove(field);
            object = Range.optionArity(field);
            if (bl) {
                object = ((Range)object).min(Math.max(1, ((Range)object).min));
            }
            if (CommandLine.this.tracer.isDebug()) {
                CommandLine.this.tracer.debug("Found option named '%s': field %s, arity=%s%n", string, field, object);
            }
            this.applyOption(field, Option.class, (Range)object, bl, stack, set, "option ".concat(String.valueOf(string)));
        }

        private void processClusteredShortOptions(Collection<Field> collection, Set<Field> set, String string, Stack<String> stack) {
            String string2 = string.substring(0, 1);
            String string3 = string.substring(1);
            boolean bl = true;
            while (string3.length() > 0 && this.singleCharOption2Field.containsKey(Character.valueOf(string3.charAt(0)))) {
                Field field = this.singleCharOption2Field.get(Character.valueOf(string3.charAt(0)));
                Range range = Range.optionArity(field);
                String string4 = "option " + string2 + string3.charAt(0);
                if (CommandLine.this.tracer.isDebug()) {
                    CommandLine.this.tracer.debug("Found option '%s%s' in %s: field %s, arity=%s%n", string2, Character.valueOf(string3.charAt(0)), string, field, range);
                }
                collection.remove(field);
                string3 = string3.length() > 0 ? string3.substring(1) : "";
                boolean bl2 = bl = string3.length() > 0;
                if (string3.startsWith(this.separator)) {
                    string3 = string3.substring(this.separator.length());
                    range = range.min(Math.max(1, range.min));
                }
                if (range.min > 0 && !CommandLine.empty(string3) && CommandLine.this.tracer.isDebug()) {
                    CommandLine.this.tracer.debug("Trying to process '%s' as option parameter%n", string3);
                }
                if (!CommandLine.empty(string3)) {
                    stack.push(string3);
                }
                int n = this.applyOption(field, Option.class, range, bl, stack, set, string4);
                if (CommandLine.empty(string3) || n > 0 || stack.isEmpty()) {
                    return;
                }
                string3 = stack.pop();
            }
            if (string3.length() == 0) {
                return;
            }
            if (string.endsWith(string3)) {
                stack.push(bl ? string2 + string3 : string3);
                if (stack.peek().equals(string)) {
                    if (CommandLine.this.tracer.isDebug()) {
                        CommandLine.this.tracer.debug("Could not match any short options in %s, deciding whether to treat as unmatched option or positional parameter...%n", string);
                    }
                    if (this.resemblesOption(string)) {
                        this.handleUnmatchedArguments(stack.pop());
                        return;
                    }
                    this.processPositionalParameter(collection, set, stack);
                    return;
                }
                if (CommandLine.this.tracer.isDebug()) {
                    CommandLine.this.tracer.debug("No option found for %s in %s%n", string3, string);
                }
                this.handleUnmatchedArguments(stack.pop());
                return;
            }
            stack.push(string3);
            if (CommandLine.this.tracer.isDebug()) {
                CommandLine.this.tracer.debug("%s is not an option parameter for %s%n", string3, string);
            }
            this.processPositionalParameter(collection, set, stack);
        }

        private int applyOption(Field field, Class<?> clazz, Range range, boolean bl, Stack<String> stack, Set<Field> set, String string) {
            this.updateHelpRequested(field);
            stack.size();
            this.assertNoMissingParameters(field, range.min, stack);
            Class clazz2 = field.getType();
            if (clazz2.isArray()) {
                return this.applyValuesToArrayField(field, clazz, range, stack, clazz2, string);
            }
            if (Collection.class.isAssignableFrom(clazz2)) {
                return this.applyValuesToCollectionField(field, clazz, range, stack, clazz2, string);
            }
            if (Map.class.isAssignableFrom(clazz2)) {
                return this.applyValuesToMapField(field, clazz, range, stack, clazz2, string);
            }
            clazz2 = CommandLine.getTypeAttribute(field)[0];
            return this.applyValueToSingleValuedField(field, range, stack, clazz2, set, string);
        }

        private int applyValueToSingleValuedField(Field field, Range object, Stack<String> object2, Class<?> object3, Set<Field> set, String string) {
            boolean bl = ((Vector)object2).isEmpty();
            String string2 = ((Vector)object2).isEmpty() ? null : this.trim((String)((Stack)object2).pop());
            int n = ((Range)object).min;
            if ((object3 == Boolean.class || object3 == Boolean.TYPE) && ((Range)object).min <= 0) {
                if (((Range)object).max > 0 && ("true".equalsIgnoreCase(string2) || "false".equalsIgnoreCase(string2))) {
                    n = 1;
                } else {
                    if (string2 != null) {
                        ((Stack)object2).push((String)string2);
                    }
                    string2 = String.valueOf((object = (Boolean)field.get(this.command)) == null ? true : !((Boolean)object).booleanValue());
                }
            }
            if (bl && string2 == null) {
                return 0;
            }
            object = this.getTypeConverter((Class<?>)object3, field);
            object = this.tryConvert(field, -1, (ITypeConverter<?>)object, string2, (Class<?>)object3);
            object2 = field.get(this.command);
            object3 = TraceLevel.INFO;
            String string3 = "Setting %s field '%s.%s' to '%5$s' (was '%4$s') for %6$s%n";
            if (set != null) {
                if (set.contains(field)) {
                    if (!CommandLine.this.isOverwrittenOptionsAllowed()) {
                        throw new OverwrittenOptionException(CommandLine.this, this.optionDescription("", field, 0) + " should be specified only once");
                    }
                    object3 = TraceLevel.WARN;
                    string3 = "Overwriting %s field '%s.%s' value '%s' with '%s' for %s%n";
                }
                set.add(field);
            }
            if (((CommandLine)CommandLine.this).tracer.level.isEnabled((TraceLevel)((Object)object3))) {
                ((TraceLevel)object3).print(CommandLine.this.tracer, string3, new Object[]{field.getType().getSimpleName(), field.getDeclaringClass().getSimpleName(), field.getName(), String.valueOf(object2), String.valueOf(object), string});
            }
            field.set(this.command, object);
            return n;
        }

        private int applyValuesToMapField(Field field, Class<?> classArray, Range range, Stack<String> stack, Class<?> clazz, String string) {
            classArray = CommandLine.getTypeAttribute(field);
            if (classArray.length < 2) {
                throw new ParameterException(CommandLine.this, "Field " + field + " needs two types (one for the map key, one for the value) but only has " + classArray.length + " types configured.");
            }
            ITypeConverter<?> iTypeConverter = this.getTypeConverter(classArray[0], field);
            ITypeConverter<?> iTypeConverter2 = this.getTypeConverter(classArray[1], field);
            Map<Object, Object> map = (Map<Object, Object>)field.get(this.command);
            if (map == null) {
                map = this.createMap(clazz);
                field.set(this.command, map);
            }
            int n = map.size();
            this.consumeMapArguments(field, range, stack, classArray, iTypeConverter, iTypeConverter2, map, string);
            return map.size() - n;
        }

        private void consumeMapArguments(Field field, Range range, Stack<String> stack, Class<?>[] classArray, ITypeConverter<?> iTypeConverter, ITypeConverter<?> iTypeConverter2, Map<Object, Object> map, String string) {
            int n;
            for (n = 0; n < range.min; ++n) {
                this.consumeOneMapArgument(field, range, stack, classArray, iTypeConverter, iTypeConverter2, map, n, string);
            }
            for (n = range.min; n < range.max && !stack.isEmpty(); ++n) {
                if (!field.isAnnotationPresent(Parameters.class) && (this.commands.containsKey(stack.peek()) || this.isOption(stack.peek()))) {
                    return;
                }
                this.consumeOneMapArgument(field, range, stack, classArray, iTypeConverter, iTypeConverter2, map, n, string);
            }
        }

        private void consumeOneMapArgument(Field field, Range stringArray, Stack<String> stack, Class<?>[] classArray, ITypeConverter<?> iTypeConverter, ITypeConverter<?> iTypeConverter2, Map<Object, Object> map, int n, String string) {
            Interpreter interpreter = this;
            for (String string2 : interpreter.split(interpreter.trim(stack.pop()), field)) {
                Object object;
                String[] stringArray2 = string2.split("=");
                if (stringArray2.length < 2) {
                    object = this.splitRegex(field);
                    if (((String)object).length() == 0) {
                        throw new ParameterException(CommandLine.this, "Value for option " + this.optionDescription("", field, 0) + " should be in KEY=VALUE format but was " + string2);
                    }
                    throw new ParameterException(CommandLine.this, "Value for option " + this.optionDescription("", field, 0) + " should be in KEY=VALUE[" + (String)object + "KEY=VALUE]... format but was " + string2);
                }
                object = this.tryConvert(field, n, iTypeConverter, stringArray2[0], classArray[0]);
                Object object2 = this.tryConvert(field, n, iTypeConverter2, stringArray2[1], classArray[1]);
                map.put(object, object2);
                if (!CommandLine.this.tracer.isInfo()) continue;
                CommandLine.this.tracer.info("Putting [%s : %s] in %s<%s, %s> field '%s.%s' for %s%n", String.valueOf(object), String.valueOf(object2), map.getClass().getSimpleName(), classArray[0].getSimpleName(), classArray[1].getSimpleName(), field.getDeclaringClass().getSimpleName(), field.getName(), string);
            }
        }

        private void checkMaxArityExceeded(Range range, int n, Field field, String[] stringArray) {
            if (stringArray.length <= n) {
                return;
            }
            throw new MaxValuesforFieldExceededException(CommandLine.this, this.optionDescription("", field, -1) + " max number of values (" + range.max + ") exceeded: remainder is " + n + " but " + stringArray.length + " values were specified: " + Arrays.toString(stringArray));
        }

        private int applyValuesToArrayField(Field field, Class<?> object, Range object2, Stack<String> stack, Class<?> object3, String string) {
            object3 = field.get(this.command);
            int n = object3 == null ? 0 : Array.getLength(object3);
            Class clazz = CommandLine.getTypeAttribute(field)[0];
            object = this.consumeArguments(field, (Class<?>)object, (Range)object2, stack, clazz, n, string);
            object2 = new ArrayList();
            for (int i2 = 0; i2 < n; ++i2) {
                object2.add(Array.get(object3, i2));
            }
            Object object4 = object.iterator();
            while (object4.hasNext()) {
                object3 = object4.next();
                if (object3 instanceof Collection) {
                    object2.addAll((Collection)object3);
                    continue;
                }
                object2.add(object3);
            }
            object4 = Array.newInstance(clazz, object2.size());
            field.set(this.command, object4);
            for (int i3 = 0; i3 < object2.size(); ++i3) {
                Array.set(object4, i3, object2.get(i3));
            }
            return object.size();
        }

        private int applyValuesToCollectionField(Field object, Class<?> object2, Range range, Stack<String> stack, Class<?> clazz, String string) {
            Collection<Object> collection = (Collection<Object>)((Field)object).get(this.command);
            Class clazz2 = CommandLine.getTypeAttribute((Field)object)[0];
            int n = collection == null ? 0 : collection.size();
            object2 = this.consumeArguments((Field)object, (Class<?>)object2, range, stack, clazz2, n, string);
            if (collection == null) {
                collection = this.createCollection(clazz);
                ((Field)object).set(this.command, collection);
            }
            object = object2.iterator();
            while (object.hasNext()) {
                range = object.next();
                if (range instanceof Collection) {
                    collection.addAll((Collection)((Object)range));
                    continue;
                }
                collection.add(range);
            }
            return object2.size();
        }

        private List<Object> consumeArguments(Field field, Class<?> clazz, Range range, Stack<String> stack, Class<?> clazz2, int n, String string) {
            int n2;
            ArrayList<Object> arrayList = new ArrayList<Object>();
            for (n2 = 0; n2 < range.min; ++n2) {
                this.consumeOneArgument(field, range, stack, clazz2, arrayList, n2, n, string);
            }
            for (n2 = range.min; n2 < range.max && !stack.isEmpty(); ++n2) {
                if (clazz != Parameters.class && (this.commands.containsKey(stack.peek()) || this.isOption(stack.peek()))) {
                    return arrayList;
                }
                this.consumeOneArgument(field, range, stack, clazz2, arrayList, n2, n, string);
            }
            return arrayList;
        }

        private int consumeOneArgument(Field field, Range stringArray, Stack<String> object, Class<?> clazz, List<Object> list, int n, int n2, String string) {
            Interpreter interpreter = this;
            stringArray = interpreter.split(interpreter.trim((String)((Stack)object).pop()), field);
            object = this.getTypeConverter(clazz, field);
            for (n2 = 0; n2 < stringArray.length; ++n2) {
                list.add(this.tryConvert(field, n, (ITypeConverter<?>)object, stringArray[n2], clazz));
                if (!CommandLine.this.tracer.isInfo()) continue;
                if (field.getType().isArray()) {
                    Object[] objectArray = new Object[5];
                    List<Object> list2 = list;
                    objectArray[0] = String.valueOf(list2.get(list2.size() - 1));
                    objectArray[1] = clazz.getSimpleName();
                    objectArray[2] = field.getDeclaringClass().getSimpleName();
                    objectArray[3] = field.getName();
                    objectArray[4] = string;
                    CommandLine.this.tracer.info("Adding [%s] to %s[] field '%s.%s' for %s%n", objectArray);
                    continue;
                }
                Object[] objectArray = new Object[6];
                List<Object> list3 = list;
                objectArray[0] = String.valueOf(list3.get(list3.size() - 1));
                objectArray[1] = field.getType().getSimpleName();
                objectArray[2] = clazz.getSimpleName();
                objectArray[3] = field.getDeclaringClass().getSimpleName();
                objectArray[4] = field.getName();
                objectArray[5] = string;
                CommandLine.this.tracer.info("Adding [%s] to %s<%s> field '%s.%s' for %s%n", objectArray);
            }
            return ++n;
        }

        private String splitRegex(Field field) {
            if (field.isAnnotationPresent(Option.class)) {
                return field.getAnnotation(Option.class).split();
            }
            if (field.isAnnotationPresent(Parameters.class)) {
                return field.getAnnotation(Parameters.class).split();
            }
            return "";
        }

        private String[] split(String string, Field object) {
            if (((String)(object = this.splitRegex((Field)object))).length() == 0) {
                return new String[]{string};
            }
            return string.split((String)object);
        }

        private boolean isOption(String string) {
            if ("--".equals(string)) {
                return true;
            }
            if (this.optionName2Field.containsKey(string)) {
                return true;
            }
            int n = string.indexOf(this.separator);
            if (n > 0 && this.optionName2Field.containsKey(string.substring(0, n))) {
                return true;
            }
            return string.length() > 2 && string.startsWith("-") && this.singleCharOption2Field.containsKey(Character.valueOf(string.charAt(1)));
        }

        private Object tryConvert(Field object, int n, ITypeConverter<?> iTypeConverter, String string, Class<?> clazz) {
            try {
                return iTypeConverter.convert(string);
            }
            catch (TypeConversionException typeConversionException) {
                throw new ParameterException(CommandLine.this, typeConversionException.getMessage() + this.optionDescription(" for ", (Field)object, n));
            }
            catch (Exception exception) {
                object = this.optionDescription(" for ", (Field)object, n) + ": " + exception;
                throw new ParameterException(CommandLine.this, "Could not convert '" + string + "' to " + clazz.getSimpleName() + (String)object, exception);
            }
        }

        private String optionDescription(String string, Field object, int n) {
            Help.IParamLabelRenderer iParamLabelRenderer = Help.createMinimalParamLabelRenderer();
            String string2 = "";
            if (((AccessibleObject)object).isAnnotationPresent(Option.class)) {
                string2 = string + "option '" + ((Field)object).getAnnotation(Option.class).names()[0] + "'";
                if (n >= 0) {
                    if (Range.optionArity((Field)object).max > 1) {
                        string2 = string2 + " at index " + n;
                    }
                    string2 = string2 + " (" + iParamLabelRenderer.renderParameterLabel((Field)object, Help.Ansi.OFF, Collections.emptyList()) + ")";
                }
            } else if (((AccessibleObject)object).isAnnotationPresent(Parameters.class)) {
                Range range = Range.parameterIndex((Field)object);
                object = iParamLabelRenderer.renderParameterLabel((Field)object, Help.Ansi.OFF, Collections.emptyList());
                string2 = string + "positional parameter at index " + range + " (" + object + ")";
            }
            return string2;
        }

        private boolean isAnyHelpRequested() {
            return this.isHelpRequested || CommandLine.this.versionHelpRequested || CommandLine.this.usageHelpRequested;
        }

        private void updateHelpRequested(Field field) {
            if (field.isAnnotationPresent(Option.class)) {
                this.isHelpRequested |= this.is(field, "help", field.getAnnotation(Option.class).help());
                CommandLine commandLine = CommandLine.this;
                commandLine.versionHelpRequested = commandLine.versionHelpRequested | this.is(field, "versionHelp", field.getAnnotation(Option.class).versionHelp());
                CommandLine commandLine2 = CommandLine.this;
                commandLine2.usageHelpRequested = commandLine2.usageHelpRequested | this.is(field, "usageHelp", field.getAnnotation(Option.class).usageHelp());
            }
        }

        private boolean is(Field field, String string, boolean bl) {
            if (bl && CommandLine.this.tracer.isInfo()) {
                CommandLine.this.tracer.info("Field '%s.%s' has '%s' annotation: not validating required fields%n", field.getDeclaringClass().getSimpleName(), field.getName(), string);
            }
            return bl;
        }

        private Collection<Object> createCollection(Class<?> clazz) {
            if (clazz.isInterface()) {
                if (List.class.isAssignableFrom(clazz)) {
                    return new ArrayList<Object>();
                }
                if (SortedSet.class.isAssignableFrom(clazz)) {
                    return new TreeSet<Object>();
                }
                if (Set.class.isAssignableFrom(clazz)) {
                    return new LinkedHashSet<Object>();
                }
                if (Queue.class.isAssignableFrom(clazz)) {
                    return new LinkedList<Object>();
                }
                return new ArrayList<Object>();
            }
            return (Collection)clazz.newInstance();
        }

        private Map<Object, Object> createMap(Class<?> clazz) {
            try {
                return (Map)clazz.newInstance();
            }
            catch (Exception exception) {
                return new LinkedHashMap<Object, Object>();
            }
        }

        private ITypeConverter<?> getTypeConverter(final Class<?> clazz, Field field) {
            ITypeConverter<?> iTypeConverter = this.converterRegistry.get(clazz);
            if (iTypeConverter != null) {
                return iTypeConverter;
            }
            if (clazz.isEnum()) {
                return new ITypeConverter<Object>(){

                    @Override
                    public Object convert(String string) {
                        return Enum.valueOf(clazz, string);
                    }
                };
            }
            throw new MissingTypeConverterException(CommandLine.this, "No TypeConverter registered for " + clazz.getName() + " of field " + field);
        }

        /*
         * Unable to fully structure code
         */
        private void assertNoMissingParameters(Field var1_1, int var2_2, Stack<String> var3_4) {
            block5: {
                block6: {
                    block7: {
                        if (var2_2 <= var3_4.size()) break block5;
                        if (var2_2 != 1) break block6;
                        if (var1_1.isAnnotationPresent(Option.class)) {
                            throw new MissingParameterException(CommandLine.this, "Missing required parameter for " + this.optionDescription("", var1_1, 0));
                        }
                        var2_3 = Range.parameterIndex(var1_1);
                        var3_4 = Help.createMinimalParamLabelRenderer();
                        var4_5 = "";
                        var5_6 = "";
                        var6_7 = 0;
                        for (var7_8 = var2_3.min; var7_8 < this.positionalParametersFields.size(); ++var7_8) {
                            if (Range.parameterArity((Field)this.positionalParametersFields.get((int)var7_8)).min <= 0) continue;
                            var5_6 = var5_6 + var4_5 + var3_4.renderParameterLabel(this.positionalParametersFields.get(var7_8), Help.Ansi.OFF, Collections.emptyList());
                            var4_5 = ", ";
                            ++var6_7;
                        }
                        var7_9 = "Missing required parameter";
                        if (!Range.parameterArity((Field)var1_1).isVariable) break block7;
                        v0 = new StringBuilder().append(var7_9).append("s at positions ").append(var2_3);
                        ** GOTO lbl-1000
                    }
                    v0 = new StringBuilder().append(var7_9);
                    if (var6_7 > 1) {
                        v1 = "s: ";
                    } else lbl-1000:
                    // 2 sources

                    {
                        v1 = ": ";
                    }
                    var7_9 = v0.append(v1).toString();
                    throw new MissingParameterException(CommandLine.this, var7_9 + var5_6);
                }
                if (var3_4.isEmpty()) {
                    throw new MissingParameterException(CommandLine.this, this.optionDescription("", var1_1, 0) + " requires at least " + var2_2 + " values, but none were specified.");
                }
                throw new MissingParameterException(CommandLine.this, this.optionDescription("", var1_1, 0) + " requires at least " + var2_2 + " values, but only " + var3_4.size() + " were specified: " + CommandLine.access$2200((Stack)var3_4));
            }
        }

        private String trim(String string) {
            return this.unquote(string);
        }

        private String unquote(String string) {
            if (string == null) {
                return null;
            }
            if (string.length() > 1 && string.startsWith("\"") && string.endsWith("\"")) {
                return string.substring(1, string.length() - 1);
            }
            return string;
        }
    }

    public static class Range
    implements Comparable<Range> {
        public final int min;
        public final int max;
        public final boolean isVariable;
        private final boolean isUnspecified;
        private final String originalValue;

        public Range(int n, int n2, boolean bl, boolean bl2, String string) {
            this.min = n;
            this.max = n2;
            this.isVariable = bl;
            this.isUnspecified = bl2;
            this.originalValue = string;
        }

        public static Range optionArity(Field field) {
            if (field.isAnnotationPresent(Option.class)) {
                return Range.adjustForType(Range.valueOf(field.getAnnotation(Option.class).arity()), field);
            }
            return new Range(0, 0, false, true, "0");
        }

        public static Range parameterArity(Field field) {
            if (field.isAnnotationPresent(Parameters.class)) {
                return Range.adjustForType(Range.valueOf(field.getAnnotation(Parameters.class).arity()), field);
            }
            return new Range(0, 0, false, true, "0");
        }

        public static Range parameterIndex(Field field) {
            if (field.isAnnotationPresent(Parameters.class)) {
                return Range.valueOf(field.getAnnotation(Parameters.class).index());
            }
            return new Range(0, 0, false, true, "0");
        }

        static Range adjustForType(Range range, Field field) {
            if (range.isUnspecified) {
                return Range.defaultArity(field);
            }
            return range;
        }

        public static Range defaultArity(Field field) {
            Class<?> clazz = field.getType();
            if (field.isAnnotationPresent(Option.class)) {
                return Range.defaultArity(clazz);
            }
            if (CommandLine.isMultiValue(clazz)) {
                return Range.valueOf("0..1");
            }
            return Range.valueOf("1");
        }

        public static Range defaultArity(Class<?> clazz) {
            if (CommandLine.isBoolean(clazz)) {
                return Range.valueOf("0");
            }
            return Range.valueOf("1");
        }

        private int size() {
            return 1 + this.max - this.min;
        }

        static Range parameterCapacity(Field object) {
            Range range = Range.parameterArity((Field)object);
            if (!CommandLine.isMultiValue((Field)object)) {
                return range;
            }
            object = Range.parameterIndex((Field)object);
            if (range.max == 0) {
                return range;
            }
            if (super.size() == 1) {
                return range;
            }
            if (((Range)object).isVariable) {
                return Range.valueOf(range.min + "..*");
            }
            if (range.size() == 1) {
                return Range.valueOf("" + range.min * super.size());
            }
            if (range.isVariable) {
                return Range.valueOf(range.min * super.size() + "..*");
            }
            return Range.valueOf(range.min * super.size() + ".." + range.max * super.size());
        }

        public static Range valueOf(String string) {
            boolean bl;
            int n;
            boolean bl2 = (string = string.trim()).length() == 0 || string.startsWith("..");
            int n2 = string.indexOf("..");
            if (n2 >= 0) {
                n = Range.parseInt(string.substring(0, n2), 0);
                bl = (n2 = Range.parseInt(string.substring(n2 + 2), Integer.MAX_VALUE)) == Integer.MAX_VALUE;
            } else {
                n2 = Range.parseInt(string, Integer.MAX_VALUE);
                bl = n2 == Integer.MAX_VALUE;
                n = bl ? 0 : n2;
            }
            return new Range(n, n2, bl, bl2, string);
        }

        private static int parseInt(String string, int n) {
            try {
                return Integer.parseInt(string);
            }
            catch (Exception exception) {
                return n;
            }
        }

        public Range min(int n) {
            int n2 = n;
            return new Range(n2, Math.max(n2, this.max), this.isVariable, this.isUnspecified, this.originalValue);
        }

        public Range max(int n) {
            return new Range(Math.min(this.min, n), n, this.isVariable, this.isUnspecified, this.originalValue);
        }

        public boolean contains(int n) {
            return this.min <= n && this.max >= n;
        }

        public boolean equals(Object object) {
            if (!(object instanceof Range)) {
                return false;
            }
            object = (Range)object;
            return ((Range)object).max == this.max && ((Range)object).min == this.min && ((Range)object).isVariable == this.isVariable;
        }

        public int hashCode() {
            return ((629 + this.max) * 37 + this.min) * 37 + (this.isVariable ? 1 : 0);
        }

        public String toString() {
            if (this.min == this.max) {
                return String.valueOf(this.min);
            }
            return this.min + ".." + (this.isVariable ? "*" : Integer.valueOf(this.max));
        }

        @Override
        public int compareTo(Range range) {
            int n = this.min - range.min;
            if (n == 0) {
                return this.max - range.max;
            }
            return n;
        }
    }

    public static interface ITypeConverter<K> {
        public K convert(String var1);
    }

    @Retention(value=RetentionPolicy.RUNTIME)
    @Target(value={ElementType.TYPE, ElementType.LOCAL_VARIABLE, ElementType.PACKAGE})
    public static @interface Command {
        public String name() default "<main class>";

        public Class<?>[] subcommands() default {};

        public String separator() default "=";

        public String[] version() default {};

        public String headerHeading() default "";

        public String[] header() default {};

        public String synopsisHeading() default "Usage: ";

        public boolean abbreviateSynopsis() default false;

        public String[] customSynopsis() default {};

        public String descriptionHeading() default "";

        public String[] description() default {};

        public String parameterListHeading() default "";

        public String optionListHeading() default "";

        public boolean sortOptions() default true;

        public char requiredOptionMarker() default 32;

        public boolean showDefaultValues() default false;

        public String commandListHeading() default "Commands:%n";

        public String footerHeading() default "";

        public String[] footer() default {};
    }

    @Retention(value=RetentionPolicy.RUNTIME)
    @Target(value={ElementType.FIELD})
    public static @interface Parameters {
        public String index() default "*";

        public String[] description() default {};

        public String arity() default "";

        public String paramLabel() default "";

        public Class<?>[] type() default {};

        public String split() default "";

        public boolean hidden() default false;
    }

    @Retention(value=RetentionPolicy.RUNTIME)
    @Target(value={ElementType.FIELD})
    public static @interface Option {
        public String[] names();

        public boolean required() default false;

        public boolean help() default false;

        public boolean usageHelp() default false;

        public boolean versionHelp() default false;

        public String[] description() default {};

        public String arity() default "";

        public String paramLabel() default "";

        public Class<?>[] type() default {};

        public String split() default "";

        public boolean hidden() default false;
    }

    public static class RunAll
    implements IParseResultHandler {
        @Override
        public List<Object> handleParseResult(List<CommandLine> object, PrintStream object2, Help.Ansi object3) {
            if (CommandLine.printHelpIfRequested(object, (PrintStream)object2, object3)) {
                return null;
            }
            object2 = new ArrayList();
            object = object.iterator();
            while (object.hasNext()) {
                object3 = (CommandLine)object.next();
                object2.add(CommandLine.execute((CommandLine)object3));
            }
            return object2;
        }
    }

    public static class RunLast
    implements IParseResultHandler {
        @Override
        public List<Object> handleParseResult(List<CommandLine> object, PrintStream printStream, Help.Ansi ansi) {
            if (CommandLine.printHelpIfRequested(object, printStream, ansi)) {
                return Collections.emptyList();
            }
            List<CommandLine> list = object;
            object = list.get(list.size() - 1);
            return Arrays.asList(CommandLine.execute((CommandLine)object));
        }
    }

    public static class RunFirst
    implements IParseResultHandler {
        @Override
        public List<Object> handleParseResult(List<CommandLine> list, PrintStream printStream, Help.Ansi ansi) {
            if (CommandLine.printHelpIfRequested(list, printStream, ansi)) {
                return Collections.emptyList();
            }
            return Arrays.asList(CommandLine.execute(list.get(0)));
        }
    }

    public static class DefaultExceptionHandler
    implements IExceptionHandler {
        @Override
        public List<Object> handleException(ParameterException parameterException, PrintStream printStream, Help.Ansi ansi, String ... stringArray) {
            printStream.println(parameterException.getMessage());
            parameterException.getCommandLine().usage(printStream, ansi);
            return Collections.emptyList();
        }
    }

    public static interface IExceptionHandler {
        public List<Object> handleException(ParameterException var1, PrintStream var2, Help.Ansi var3, String ... var4);
    }

    public static interface IParseResultHandler {
        public List<Object> handleParseResult(List<CommandLine> var1, PrintStream var2, Help.Ansi var3);
    }
}

