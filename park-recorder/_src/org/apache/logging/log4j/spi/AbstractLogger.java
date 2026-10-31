/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.spi;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogBuilder;
import org.apache.logging.log4j.LoggingException;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.internal.DefaultLogBuilder;
import org.apache.logging.log4j.message.DefaultFlowMessageFactory;
import org.apache.logging.log4j.message.EntryMessage;
import org.apache.logging.log4j.message.FlowMessageFactory;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.MessageFactory2;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;
import org.apache.logging.log4j.message.ReusableMessageFactory;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LocationAwareLogger;
import org.apache.logging.log4j.spi.MessageFactory2Adapter;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Constants;
import org.apache.logging.log4j.util.LambdaUtil;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.MessageSupplier;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.apache.logging.log4j.util.Strings;
import org.apache.logging.log4j.util.Supplier;

public abstract class AbstractLogger
implements Serializable,
ExtendedLogger,
LocationAwareLogger {
    public static final Marker FLOW_MARKER = MarkerManager.getMarker("FLOW");
    public static final Marker ENTRY_MARKER = MarkerManager.getMarker("ENTER").setParents(FLOW_MARKER);
    public static final Marker EXIT_MARKER = MarkerManager.getMarker("EXIT").setParents(FLOW_MARKER);
    public static final Marker EXCEPTION_MARKER = MarkerManager.getMarker("EXCEPTION");
    public static final Marker THROWING_MARKER = MarkerManager.getMarker("THROWING").setParents(EXCEPTION_MARKER);
    public static final Marker CATCHING_MARKER = MarkerManager.getMarker("CATCHING").setParents(EXCEPTION_MARKER);
    public static final Class<? extends MessageFactory> DEFAULT_MESSAGE_FACTORY_CLASS = AbstractLogger.createClassForProperty("log4j2.messageFactory", ReusableMessageFactory.class, ParameterizedMessageFactory.class);
    public static final Class<? extends FlowMessageFactory> DEFAULT_FLOW_MESSAGE_FACTORY_CLASS = AbstractLogger.createFlowClassForProperty("log4j2.flowMessageFactory", DefaultFlowMessageFactory.class);
    private static final long serialVersionUID = 2L;
    private static final String FQCN = AbstractLogger.class.getName();
    private static final String THROWING = "Throwing";
    private static final String CATCHING = "Catching";
    protected final String name;
    private final MessageFactory2 messageFactory;
    private final FlowMessageFactory flowMessageFactory;
    private static ThreadLocal<int[]> recursionDepthHolder = new ThreadLocal();
    protected final transient ThreadLocal<DefaultLogBuilder> logBuilder;

    public AbstractLogger() {
        this.name = this.getClass().getName();
        this.messageFactory = AbstractLogger.createDefaultMessageFactory();
        this.flowMessageFactory = AbstractLogger.createDefaultFlowMessageFactory();
        AbstractLogger abstractLogger = this;
        this.logBuilder = abstractLogger.new LocalLogBuilder(abstractLogger);
    }

    public AbstractLogger(String string) {
        this(string, AbstractLogger.createDefaultMessageFactory());
    }

    public AbstractLogger(String string, MessageFactory messageFactory) {
        this.name = string;
        this.messageFactory = messageFactory == null ? AbstractLogger.createDefaultMessageFactory() : AbstractLogger.narrow(messageFactory);
        this.flowMessageFactory = AbstractLogger.createDefaultFlowMessageFactory();
        AbstractLogger abstractLogger = this;
        this.logBuilder = abstractLogger.new LocalLogBuilder(abstractLogger);
    }

    public static void checkMessageFactory(ExtendedLogger extendedLogger, MessageFactory messageFactory) {
        String string = extendedLogger.getName();
        extendedLogger = extendedLogger.getMessageFactory();
        if (messageFactory != null && !extendedLogger.equals(messageFactory)) {
            StatusLogger.getLogger().warn("The Logger {} was created with the message factory {} and is now requested with the message factory {}, which may create log events with unexpected formatting.", (Object)string, (Object)extendedLogger, (Object)messageFactory);
            return;
        }
        if (messageFactory == null && !extendedLogger.getClass().equals(DEFAULT_MESSAGE_FACTORY_CLASS)) {
            StatusLogger.getLogger().warn("The Logger {} was created with the message factory {} and is now requested with a null message factory (defaults to {}), which may create log events with unexpected formatting.", (Object)string, (Object)extendedLogger, (Object)DEFAULT_MESSAGE_FACTORY_CLASS.getName());
        }
    }

    @Override
    public void catching(Level level, Throwable throwable) {
        this.catching(FQCN, level, throwable);
    }

    protected void catching(String string, Level level, Throwable throwable) {
        if (this.isEnabled(level, CATCHING_MARKER, (Object)null, null)) {
            this.logMessageSafely(string, level, CATCHING_MARKER, this.catchingMsg(throwable), throwable);
        }
    }

    @Override
    public void catching(Throwable throwable) {
        if (this.isEnabled(Level.ERROR, CATCHING_MARKER, (Object)null, null)) {
            this.logMessageSafely(FQCN, Level.ERROR, CATCHING_MARKER, this.catchingMsg(throwable), throwable);
        }
    }

    protected Message catchingMsg(Throwable throwable) {
        return this.messageFactory.newMessage(CATCHING);
    }

    private static Class<? extends MessageFactory> createClassForProperty(String string, Class<ReusableMessageFactory> object, Class<ParameterizedMessageFactory> clazz) {
        try {
            object = Constants.ENABLE_THREADLOCALS ? ((Class)object).getName() : clazz.getName();
            return LoaderUtil.loadClass(PropertiesUtil.getProperties().getStringProperty(string, (String)object)).asSubclass(MessageFactory.class);
        }
        catch (Throwable throwable) {
            return clazz;
        }
    }

    private static Class<? extends FlowMessageFactory> createFlowClassForProperty(String string, Class<DefaultFlowMessageFactory> clazz) {
        try {
            return LoaderUtil.loadClass(PropertiesUtil.getProperties().getStringProperty(string, clazz.getName())).asSubclass(FlowMessageFactory.class);
        }
        catch (Throwable throwable) {
            return clazz;
        }
    }

    private static MessageFactory2 createDefaultMessageFactory() {
        try {
            return AbstractLogger.narrow(DEFAULT_MESSAGE_FACTORY_CLASS.newInstance());
        }
        catch (IllegalAccessException | InstantiationException reflectiveOperationException) {
            throw new IllegalStateException(reflectiveOperationException);
        }
    }

    private static MessageFactory2 narrow(MessageFactory messageFactory) {
        if (messageFactory instanceof MessageFactory2) {
            return (MessageFactory2)messageFactory;
        }
        return new MessageFactory2Adapter(messageFactory);
    }

    private static FlowMessageFactory createDefaultFlowMessageFactory() {
        try {
            return DEFAULT_FLOW_MESSAGE_FACTORY_CLASS.newInstance();
        }
        catch (IllegalAccessException | InstantiationException reflectiveOperationException) {
            throw new IllegalStateException(reflectiveOperationException);
        }
    }

    @Override
    public void debug(Marker marker, CharSequence charSequence) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, charSequence, null);
    }

    @Override
    public void debug(Marker marker, CharSequence charSequence, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, charSequence, throwable);
    }

    @Override
    public void debug(Marker marker, Message message) {
        Message message2 = message;
        this.logIfEnabled(FQCN, Level.DEBUG, marker, message2, message2 != null ? message.getThrowable() : null);
    }

    @Override
    public void debug(Marker marker, Message message, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, message, throwable);
    }

    @Override
    public void debug(Marker marker, Object object) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, object, null);
    }

    @Override
    public void debug(Marker marker, Object object, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, object, throwable);
    }

    @Override
    public void debug(Marker marker, String string) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, string, (Throwable)null);
    }

    @Override
    public void debug(Marker marker, String string, Object ... objectArray) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, string, objectArray);
    }

    @Override
    public void debug(Marker marker, String string, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, string, throwable);
    }

    @Override
    public void debug(Message message) {
        Message message2 = message;
        this.logIfEnabled(FQCN, Level.DEBUG, null, message2, message2 != null ? message.getThrowable() : null);
    }

    @Override
    public void debug(Message message, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.DEBUG, null, message, throwable);
    }

    @Override
    public void debug(CharSequence charSequence) {
        this.logIfEnabled(FQCN, Level.DEBUG, null, charSequence, null);
    }

    @Override
    public void debug(CharSequence charSequence, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.DEBUG, null, charSequence, throwable);
    }

    @Override
    public void debug(Object object) {
        this.logIfEnabled(FQCN, Level.DEBUG, null, object, null);
    }

    @Override
    public void debug(Object object, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.DEBUG, null, object, throwable);
    }

    @Override
    public void debug(String string) {
        this.logIfEnabled(FQCN, Level.DEBUG, (Marker)null, string, (Throwable)null);
    }

    @Override
    public void debug(String string, Object ... objectArray) {
        this.logIfEnabled(FQCN, Level.DEBUG, (Marker)null, string, objectArray);
    }

    @Override
    public void debug(String string, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.DEBUG, (Marker)null, string, throwable);
    }

    @Override
    public void debug(Supplier<?> supplier) {
        this.logIfEnabled(FQCN, Level.DEBUG, null, supplier, null);
    }

    @Override
    public void debug(Supplier<?> supplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.DEBUG, null, supplier, throwable);
    }

    @Override
    public void debug(Marker marker, Supplier<?> supplier) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, supplier, null);
    }

    @Override
    public void debug(Marker marker, String string, Supplier<?> ... supplierArray) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, string, supplierArray);
    }

    @Override
    public void debug(Marker marker, Supplier<?> supplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, supplier, throwable);
    }

    @Override
    public void debug(String string, Supplier<?> ... supplierArray) {
        this.logIfEnabled(FQCN, Level.DEBUG, (Marker)null, string, supplierArray);
    }

    @Override
    public void debug(Marker marker, MessageSupplier messageSupplier) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, messageSupplier, null);
    }

    @Override
    public void debug(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, messageSupplier, throwable);
    }

    @Override
    public void debug(MessageSupplier messageSupplier) {
        this.logIfEnabled(FQCN, Level.DEBUG, null, messageSupplier, null);
    }

    @Override
    public void debug(MessageSupplier messageSupplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.DEBUG, null, messageSupplier, throwable);
    }

    @Override
    public void debug(Marker marker, String string, Object object) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, string, object);
    }

    @Override
    public void debug(Marker marker, String string, Object object, Object object2) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, string, object, object2);
    }

    @Override
    public void debug(Marker marker, String string, Object object, Object object2, Object object3) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, string, object, object2, object3);
    }

    @Override
    public void debug(Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, string, object, object2, object3, object4);
    }

    @Override
    public void debug(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, string, object, object2, object3, object4, object5);
    }

    @Override
    public void debug(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, string, object, object2, object3, object4, object5, object6);
    }

    @Override
    public void debug(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, string, object, object2, object3, object4, object5, object6, object7);
    }

    @Override
    public void debug(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, string, object, object2, object3, object4, object5, object6, object7, object8);
    }

    @Override
    public void debug(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9);
    }

    @Override
    public void debug(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.logIfEnabled(FQCN, Level.DEBUG, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
    }

    @Override
    public void debug(String string, Object object) {
        this.logIfEnabled(FQCN, Level.DEBUG, (Marker)null, string, object);
    }

    @Override
    public void debug(String string, Object object, Object object2) {
        this.logIfEnabled(FQCN, Level.DEBUG, (Marker)null, string, object, object2);
    }

    @Override
    public void debug(String string, Object object, Object object2, Object object3) {
        this.logIfEnabled(FQCN, Level.DEBUG, (Marker)null, string, object, object2, object3);
    }

    @Override
    public void debug(String string, Object object, Object object2, Object object3, Object object4) {
        this.logIfEnabled(FQCN, Level.DEBUG, (Marker)null, string, object, object2, object3, object4);
    }

    @Override
    public void debug(String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.logIfEnabled(FQCN, Level.DEBUG, (Marker)null, string, object, object2, object3, object4, object5);
    }

    @Override
    public void debug(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.logIfEnabled(FQCN, Level.DEBUG, (Marker)null, string, object, object2, object3, object4, object5, object6);
    }

    @Override
    public void debug(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.logIfEnabled(FQCN, Level.DEBUG, (Marker)null, string, object, object2, object3, object4, object5, object6, object7);
    }

    @Override
    public void debug(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.logIfEnabled(FQCN, Level.DEBUG, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8);
    }

    @Override
    public void debug(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.logIfEnabled(FQCN, Level.DEBUG, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8, object9);
    }

    @Override
    public void debug(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.logIfEnabled(FQCN, Level.DEBUG, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
    }

    protected EntryMessage enter(String string, String string2, Supplier<?> ... supplierArray) {
        EntryMessage entryMessage = null;
        if (this.isEnabled(Level.TRACE, ENTRY_MARKER, (Object)null, null)) {
            entryMessage = this.entryMsg(string2, supplierArray);
            this.logMessageSafely(string, Level.TRACE, ENTRY_MARKER, entryMessage, null);
        }
        return entryMessage;
    }

    @Deprecated
    protected EntryMessage enter(String string, String string2, MessageSupplier ... messageSupplierArray) {
        EntryMessage entryMessage = null;
        if (this.isEnabled(Level.TRACE, ENTRY_MARKER, (Object)null, null)) {
            entryMessage = this.entryMsg(string2, messageSupplierArray);
            this.logMessageSafely(string, Level.TRACE, ENTRY_MARKER, entryMessage, null);
        }
        return entryMessage;
    }

    protected EntryMessage enter(String string, String string2, Object ... objectArray) {
        EntryMessage entryMessage = null;
        if (this.isEnabled(Level.TRACE, ENTRY_MARKER, (Object)null, null)) {
            entryMessage = this.entryMsg(string2, objectArray);
            this.logMessageSafely(string, Level.TRACE, ENTRY_MARKER, entryMessage, null);
        }
        return entryMessage;
    }

    @Deprecated
    protected EntryMessage enter(String string, MessageSupplier messageSupplier) {
        EntryMessage entryMessage = null;
        if (this.isEnabled(Level.TRACE, ENTRY_MARKER, (Object)null, null)) {
            entryMessage = this.flowMessageFactory.newEntryMessage(messageSupplier.get());
            this.logMessageSafely(string, Level.TRACE, ENTRY_MARKER, entryMessage, null);
        }
        return entryMessage;
    }

    protected EntryMessage enter(String string, Message message) {
        EntryMessage entryMessage = null;
        if (this.isEnabled(Level.TRACE, ENTRY_MARKER, (Object)null, null)) {
            entryMessage = this.flowMessageFactory.newEntryMessage(message);
            this.logMessageSafely(string, Level.TRACE, ENTRY_MARKER, entryMessage, null);
        }
        return entryMessage;
    }

    @Override
    @Deprecated
    public void entry() {
        this.entry(FQCN, null);
    }

    @Override
    public void entry(Object ... objectArray) {
        this.entry(FQCN, objectArray);
    }

    protected void entry(String string, Object ... objectArray) {
        if (this.isEnabled(Level.TRACE, ENTRY_MARKER, (Object)null, null)) {
            if (objectArray == null) {
                this.logMessageSafely(string, Level.TRACE, ENTRY_MARKER, this.entryMsg((String)null, (Supplier<?>[])null), null);
                return;
            }
            this.logMessageSafely(string, Level.TRACE, ENTRY_MARKER, this.entryMsg(null, objectArray), null);
        }
    }

    protected EntryMessage entryMsg(String charSequence, Object ... objectArray) {
        int n = objectArray == null ? 0 : objectArray.length;
        if (n == 0) {
            if (Strings.isEmpty(charSequence)) {
                return this.flowMessageFactory.newEntryMessage(null);
            }
            return this.flowMessageFactory.newEntryMessage(new SimpleMessage((String)charSequence));
        }
        if (charSequence != null) {
            return this.flowMessageFactory.newEntryMessage(new ParameterizedMessage((String)charSequence, objectArray));
        }
        charSequence = new StringBuilder();
        ((StringBuilder)charSequence).append("params(");
        for (int j = 0; j < n; ++j) {
            Object object;
            if (j > 0) {
                ((StringBuilder)charSequence).append(", ");
            }
            ((StringBuilder)charSequence).append((object = objectArray[j]) instanceof Message ? ((Message)object).getFormattedMessage() : String.valueOf(object));
        }
        ((StringBuilder)charSequence).append(')');
        return this.flowMessageFactory.newEntryMessage(new SimpleMessage(charSequence));
    }

    protected EntryMessage entryMsg(String string, MessageSupplier ... messageSupplierArray) {
        int n = messageSupplierArray == null ? 0 : messageSupplierArray.length;
        Object[] objectArray = new Object[n];
        for (int j = 0; j < n; ++j) {
            objectArray[j] = messageSupplierArray[j].get();
            objectArray[j] = objectArray[j] != null ? ((Message)objectArray[j]).getFormattedMessage() : null;
        }
        return this.entryMsg(string, objectArray);
    }

    protected EntryMessage entryMsg(String string, Supplier<?> ... supplierArray) {
        int n = supplierArray == null ? 0 : supplierArray.length;
        Object[] objectArray = new Object[n];
        for (int j = 0; j < n; ++j) {
            objectArray[j] = supplierArray[j].get();
            if (!(objectArray[j] instanceof Message)) continue;
            objectArray[j] = ((Message)objectArray[j]).getFormattedMessage();
        }
        return this.entryMsg(string, objectArray);
    }

    @Override
    public void error(Marker marker, Message message) {
        Message message2 = message;
        this.logIfEnabled(FQCN, Level.ERROR, marker, message2, message2 != null ? message.getThrowable() : null);
    }

    @Override
    public void error(Marker marker, Message message, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, message, throwable);
    }

    @Override
    public void error(Marker marker, CharSequence charSequence) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, charSequence, null);
    }

    @Override
    public void error(Marker marker, CharSequence charSequence, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, charSequence, throwable);
    }

    @Override
    public void error(Marker marker, Object object) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, object, null);
    }

    @Override
    public void error(Marker marker, Object object, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, object, throwable);
    }

    @Override
    public void error(Marker marker, String string) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, string, (Throwable)null);
    }

    @Override
    public void error(Marker marker, String string, Object ... objectArray) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, string, objectArray);
    }

    @Override
    public void error(Marker marker, String string, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, string, throwable);
    }

    @Override
    public void error(Message message) {
        Message message2 = message;
        this.logIfEnabled(FQCN, Level.ERROR, null, message2, message2 != null ? message.getThrowable() : null);
    }

    @Override
    public void error(Message message, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.ERROR, null, message, throwable);
    }

    @Override
    public void error(CharSequence charSequence) {
        this.logIfEnabled(FQCN, Level.ERROR, null, charSequence, null);
    }

    @Override
    public void error(CharSequence charSequence, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.ERROR, null, charSequence, throwable);
    }

    @Override
    public void error(Object object) {
        this.logIfEnabled(FQCN, Level.ERROR, null, object, null);
    }

    @Override
    public void error(Object object, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.ERROR, null, object, throwable);
    }

    @Override
    public void error(String string) {
        this.logIfEnabled(FQCN, Level.ERROR, (Marker)null, string, (Throwable)null);
    }

    @Override
    public void error(String string, Object ... objectArray) {
        this.logIfEnabled(FQCN, Level.ERROR, (Marker)null, string, objectArray);
    }

    @Override
    public void error(String string, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.ERROR, (Marker)null, string, throwable);
    }

    @Override
    public void error(Supplier<?> supplier) {
        this.logIfEnabled(FQCN, Level.ERROR, null, supplier, null);
    }

    @Override
    public void error(Supplier<?> supplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.ERROR, null, supplier, throwable);
    }

    @Override
    public void error(Marker marker, Supplier<?> supplier) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, supplier, null);
    }

    @Override
    public void error(Marker marker, String string, Supplier<?> ... supplierArray) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, string, supplierArray);
    }

    @Override
    public void error(Marker marker, Supplier<?> supplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, supplier, throwable);
    }

    @Override
    public void error(String string, Supplier<?> ... supplierArray) {
        this.logIfEnabled(FQCN, Level.ERROR, (Marker)null, string, supplierArray);
    }

    @Override
    public void error(Marker marker, MessageSupplier messageSupplier) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, messageSupplier, null);
    }

    @Override
    public void error(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, messageSupplier, throwable);
    }

    @Override
    public void error(MessageSupplier messageSupplier) {
        this.logIfEnabled(FQCN, Level.ERROR, null, messageSupplier, null);
    }

    @Override
    public void error(MessageSupplier messageSupplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.ERROR, null, messageSupplier, throwable);
    }

    @Override
    public void error(Marker marker, String string, Object object) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, string, object);
    }

    @Override
    public void error(Marker marker, String string, Object object, Object object2) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, string, object, object2);
    }

    @Override
    public void error(Marker marker, String string, Object object, Object object2, Object object3) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, string, object, object2, object3);
    }

    @Override
    public void error(Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, string, object, object2, object3, object4);
    }

    @Override
    public void error(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, string, object, object2, object3, object4, object5);
    }

    @Override
    public void error(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, string, object, object2, object3, object4, object5, object6);
    }

    @Override
    public void error(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, string, object, object2, object3, object4, object5, object6, object7);
    }

    @Override
    public void error(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, string, object, object2, object3, object4, object5, object6, object7, object8);
    }

    @Override
    public void error(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9);
    }

    @Override
    public void error(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.logIfEnabled(FQCN, Level.ERROR, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
    }

    @Override
    public void error(String string, Object object) {
        this.logIfEnabled(FQCN, Level.ERROR, (Marker)null, string, object);
    }

    @Override
    public void error(String string, Object object, Object object2) {
        this.logIfEnabled(FQCN, Level.ERROR, (Marker)null, string, object, object2);
    }

    @Override
    public void error(String string, Object object, Object object2, Object object3) {
        this.logIfEnabled(FQCN, Level.ERROR, (Marker)null, string, object, object2, object3);
    }

    @Override
    public void error(String string, Object object, Object object2, Object object3, Object object4) {
        this.logIfEnabled(FQCN, Level.ERROR, (Marker)null, string, object, object2, object3, object4);
    }

    @Override
    public void error(String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.logIfEnabled(FQCN, Level.ERROR, (Marker)null, string, object, object2, object3, object4, object5);
    }

    @Override
    public void error(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.logIfEnabled(FQCN, Level.ERROR, (Marker)null, string, object, object2, object3, object4, object5, object6);
    }

    @Override
    public void error(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.logIfEnabled(FQCN, Level.ERROR, (Marker)null, string, object, object2, object3, object4, object5, object6, object7);
    }

    @Override
    public void error(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.logIfEnabled(FQCN, Level.ERROR, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8);
    }

    @Override
    public void error(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.logIfEnabled(FQCN, Level.ERROR, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8, object9);
    }

    @Override
    public void error(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.logIfEnabled(FQCN, Level.ERROR, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
    }

    @Override
    @Deprecated
    public void exit() {
        this.exit(FQCN, null);
    }

    @Override
    @Deprecated
    public <R> R exit(R r) {
        return this.exit(FQCN, r);
    }

    protected <R> R exit(String string, R r) {
        if (this.isEnabled(Level.TRACE, EXIT_MARKER, (CharSequence)null, (Throwable)null)) {
            this.logMessageSafely(string, Level.TRACE, EXIT_MARKER, this.exitMsg(null, r), null);
        }
        return r;
    }

    protected <R> R exit(String string, String string2, R r) {
        if (this.isEnabled(Level.TRACE, EXIT_MARKER, (CharSequence)null, (Throwable)null)) {
            this.logMessageSafely(string, Level.TRACE, EXIT_MARKER, this.exitMsg(string2, r), null);
        }
        return r;
    }

    protected Message exitMsg(String string, Object object) {
        if (object == null) {
            if (string == null) {
                return this.messageFactory.newMessage("Exit");
            }
            return this.messageFactory.newMessage("Exit: ".concat(String.valueOf(string)));
        }
        if (string == null) {
            return this.messageFactory.newMessage("Exit with(" + object + ')');
        }
        return this.messageFactory.newMessage("Exit: ".concat(String.valueOf(string)), object);
    }

    @Override
    public void fatal(Marker marker, Message message) {
        Message message2 = message;
        this.logIfEnabled(FQCN, Level.FATAL, marker, message2, message2 != null ? message.getThrowable() : null);
    }

    @Override
    public void fatal(Marker marker, Message message, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, message, throwable);
    }

    @Override
    public void fatal(Marker marker, CharSequence charSequence) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, charSequence, null);
    }

    @Override
    public void fatal(Marker marker, CharSequence charSequence, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, charSequence, throwable);
    }

    @Override
    public void fatal(Marker marker, Object object) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, object, null);
    }

    @Override
    public void fatal(Marker marker, Object object, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, object, throwable);
    }

    @Override
    public void fatal(Marker marker, String string) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, string, (Throwable)null);
    }

    @Override
    public void fatal(Marker marker, String string, Object ... objectArray) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, string, objectArray);
    }

    @Override
    public void fatal(Marker marker, String string, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, string, throwable);
    }

    @Override
    public void fatal(Message message) {
        Message message2 = message;
        this.logIfEnabled(FQCN, Level.FATAL, null, message2, message2 != null ? message.getThrowable() : null);
    }

    @Override
    public void fatal(Message message, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.FATAL, null, message, throwable);
    }

    @Override
    public void fatal(CharSequence charSequence) {
        this.logIfEnabled(FQCN, Level.FATAL, null, charSequence, null);
    }

    @Override
    public void fatal(CharSequence charSequence, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.FATAL, null, charSequence, throwable);
    }

    @Override
    public void fatal(Object object) {
        this.logIfEnabled(FQCN, Level.FATAL, null, object, null);
    }

    @Override
    public void fatal(Object object, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.FATAL, null, object, throwable);
    }

    @Override
    public void fatal(String string) {
        this.logIfEnabled(FQCN, Level.FATAL, (Marker)null, string, (Throwable)null);
    }

    @Override
    public void fatal(String string, Object ... objectArray) {
        this.logIfEnabled(FQCN, Level.FATAL, (Marker)null, string, objectArray);
    }

    @Override
    public void fatal(String string, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.FATAL, (Marker)null, string, throwable);
    }

    @Override
    public void fatal(Supplier<?> supplier) {
        this.logIfEnabled(FQCN, Level.FATAL, null, supplier, null);
    }

    @Override
    public void fatal(Supplier<?> supplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.FATAL, null, supplier, throwable);
    }

    @Override
    public void fatal(Marker marker, Supplier<?> supplier) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, supplier, null);
    }

    @Override
    public void fatal(Marker marker, String string, Supplier<?> ... supplierArray) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, string, supplierArray);
    }

    @Override
    public void fatal(Marker marker, Supplier<?> supplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, supplier, throwable);
    }

    @Override
    public void fatal(String string, Supplier<?> ... supplierArray) {
        this.logIfEnabled(FQCN, Level.FATAL, (Marker)null, string, supplierArray);
    }

    @Override
    public void fatal(Marker marker, MessageSupplier messageSupplier) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, messageSupplier, null);
    }

    @Override
    public void fatal(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, messageSupplier, throwable);
    }

    @Override
    public void fatal(MessageSupplier messageSupplier) {
        this.logIfEnabled(FQCN, Level.FATAL, null, messageSupplier, null);
    }

    @Override
    public void fatal(MessageSupplier messageSupplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.FATAL, null, messageSupplier, throwable);
    }

    @Override
    public void fatal(Marker marker, String string, Object object) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, string, object);
    }

    @Override
    public void fatal(Marker marker, String string, Object object, Object object2) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, string, object, object2);
    }

    @Override
    public void fatal(Marker marker, String string, Object object, Object object2, Object object3) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, string, object, object2, object3);
    }

    @Override
    public void fatal(Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, string, object, object2, object3, object4);
    }

    @Override
    public void fatal(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, string, object, object2, object3, object4, object5);
    }

    @Override
    public void fatal(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, string, object, object2, object3, object4, object5, object6);
    }

    @Override
    public void fatal(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, string, object, object2, object3, object4, object5, object6, object7);
    }

    @Override
    public void fatal(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, string, object, object2, object3, object4, object5, object6, object7, object8);
    }

    @Override
    public void fatal(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9);
    }

    @Override
    public void fatal(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.logIfEnabled(FQCN, Level.FATAL, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
    }

    @Override
    public void fatal(String string, Object object) {
        this.logIfEnabled(FQCN, Level.FATAL, (Marker)null, string, object);
    }

    @Override
    public void fatal(String string, Object object, Object object2) {
        this.logIfEnabled(FQCN, Level.FATAL, (Marker)null, string, object, object2);
    }

    @Override
    public void fatal(String string, Object object, Object object2, Object object3) {
        this.logIfEnabled(FQCN, Level.FATAL, (Marker)null, string, object, object2, object3);
    }

    @Override
    public void fatal(String string, Object object, Object object2, Object object3, Object object4) {
        this.logIfEnabled(FQCN, Level.FATAL, (Marker)null, string, object, object2, object3, object4);
    }

    @Override
    public void fatal(String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.logIfEnabled(FQCN, Level.FATAL, (Marker)null, string, object, object2, object3, object4, object5);
    }

    @Override
    public void fatal(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.logIfEnabled(FQCN, Level.FATAL, (Marker)null, string, object, object2, object3, object4, object5, object6);
    }

    @Override
    public void fatal(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.logIfEnabled(FQCN, Level.FATAL, (Marker)null, string, object, object2, object3, object4, object5, object6, object7);
    }

    @Override
    public void fatal(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.logIfEnabled(FQCN, Level.FATAL, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8);
    }

    @Override
    public void fatal(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.logIfEnabled(FQCN, Level.FATAL, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8, object9);
    }

    @Override
    public void fatal(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.logIfEnabled(FQCN, Level.FATAL, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
    }

    @Override
    public <MF extends MessageFactory> MF getMessageFactory() {
        return (MF)this.messageFactory;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void info(Marker marker, Message message) {
        Message message2 = message;
        this.logIfEnabled(FQCN, Level.INFO, marker, message2, message2 != null ? message.getThrowable() : null);
    }

    @Override
    public void info(Marker marker, Message message, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.INFO, marker, message, throwable);
    }

    @Override
    public void info(Marker marker, CharSequence charSequence) {
        this.logIfEnabled(FQCN, Level.INFO, marker, charSequence, null);
    }

    @Override
    public void info(Marker marker, CharSequence charSequence, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.INFO, marker, charSequence, throwable);
    }

    @Override
    public void info(Marker marker, Object object) {
        this.logIfEnabled(FQCN, Level.INFO, marker, object, null);
    }

    @Override
    public void info(Marker marker, Object object, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.INFO, marker, object, throwable);
    }

    @Override
    public void info(Marker marker, String string) {
        this.logIfEnabled(FQCN, Level.INFO, marker, string, (Throwable)null);
    }

    @Override
    public void info(Marker marker, String string, Object ... objectArray) {
        this.logIfEnabled(FQCN, Level.INFO, marker, string, objectArray);
    }

    @Override
    public void info(Marker marker, String string, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.INFO, marker, string, throwable);
    }

    @Override
    public void info(Message message) {
        Message message2 = message;
        this.logIfEnabled(FQCN, Level.INFO, null, message2, message2 != null ? message.getThrowable() : null);
    }

    @Override
    public void info(Message message, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.INFO, null, message, throwable);
    }

    @Override
    public void info(CharSequence charSequence) {
        this.logIfEnabled(FQCN, Level.INFO, null, charSequence, null);
    }

    @Override
    public void info(CharSequence charSequence, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.INFO, null, charSequence, throwable);
    }

    @Override
    public void info(Object object) {
        this.logIfEnabled(FQCN, Level.INFO, null, object, null);
    }

    @Override
    public void info(Object object, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.INFO, null, object, throwable);
    }

    @Override
    public void info(String string) {
        this.logIfEnabled(FQCN, Level.INFO, (Marker)null, string, (Throwable)null);
    }

    @Override
    public void info(String string, Object ... objectArray) {
        this.logIfEnabled(FQCN, Level.INFO, (Marker)null, string, objectArray);
    }

    @Override
    public void info(String string, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.INFO, (Marker)null, string, throwable);
    }

    @Override
    public void info(Supplier<?> supplier) {
        this.logIfEnabled(FQCN, Level.INFO, null, supplier, null);
    }

    @Override
    public void info(Supplier<?> supplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.INFO, null, supplier, throwable);
    }

    @Override
    public void info(Marker marker, Supplier<?> supplier) {
        this.logIfEnabled(FQCN, Level.INFO, marker, supplier, null);
    }

    @Override
    public void info(Marker marker, String string, Supplier<?> ... supplierArray) {
        this.logIfEnabled(FQCN, Level.INFO, marker, string, supplierArray);
    }

    @Override
    public void info(Marker marker, Supplier<?> supplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.INFO, marker, supplier, throwable);
    }

    @Override
    public void info(String string, Supplier<?> ... supplierArray) {
        this.logIfEnabled(FQCN, Level.INFO, (Marker)null, string, supplierArray);
    }

    @Override
    public void info(Marker marker, MessageSupplier messageSupplier) {
        this.logIfEnabled(FQCN, Level.INFO, marker, messageSupplier, null);
    }

    @Override
    public void info(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.INFO, marker, messageSupplier, throwable);
    }

    @Override
    public void info(MessageSupplier messageSupplier) {
        this.logIfEnabled(FQCN, Level.INFO, null, messageSupplier, null);
    }

    @Override
    public void info(MessageSupplier messageSupplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.INFO, null, messageSupplier, throwable);
    }

    @Override
    public void info(Marker marker, String string, Object object) {
        this.logIfEnabled(FQCN, Level.INFO, marker, string, object);
    }

    @Override
    public void info(Marker marker, String string, Object object, Object object2) {
        this.logIfEnabled(FQCN, Level.INFO, marker, string, object, object2);
    }

    @Override
    public void info(Marker marker, String string, Object object, Object object2, Object object3) {
        this.logIfEnabled(FQCN, Level.INFO, marker, string, object, object2, object3);
    }

    @Override
    public void info(Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        this.logIfEnabled(FQCN, Level.INFO, marker, string, object, object2, object3, object4);
    }

    @Override
    public void info(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.logIfEnabled(FQCN, Level.INFO, marker, string, object, object2, object3, object4, object5);
    }

    @Override
    public void info(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.logIfEnabled(FQCN, Level.INFO, marker, string, object, object2, object3, object4, object5, object6);
    }

    @Override
    public void info(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.logIfEnabled(FQCN, Level.INFO, marker, string, object, object2, object3, object4, object5, object6, object7);
    }

    @Override
    public void info(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.logIfEnabled(FQCN, Level.INFO, marker, string, object, object2, object3, object4, object5, object6, object7, object8);
    }

    @Override
    public void info(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.logIfEnabled(FQCN, Level.INFO, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9);
    }

    @Override
    public void info(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.logIfEnabled(FQCN, Level.INFO, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
    }

    @Override
    public void info(String string, Object object) {
        this.logIfEnabled(FQCN, Level.INFO, (Marker)null, string, object);
    }

    @Override
    public void info(String string, Object object, Object object2) {
        this.logIfEnabled(FQCN, Level.INFO, (Marker)null, string, object, object2);
    }

    @Override
    public void info(String string, Object object, Object object2, Object object3) {
        this.logIfEnabled(FQCN, Level.INFO, (Marker)null, string, object, object2, object3);
    }

    @Override
    public void info(String string, Object object, Object object2, Object object3, Object object4) {
        this.logIfEnabled(FQCN, Level.INFO, (Marker)null, string, object, object2, object3, object4);
    }

    @Override
    public void info(String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.logIfEnabled(FQCN, Level.INFO, (Marker)null, string, object, object2, object3, object4, object5);
    }

    @Override
    public void info(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.logIfEnabled(FQCN, Level.INFO, (Marker)null, string, object, object2, object3, object4, object5, object6);
    }

    @Override
    public void info(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.logIfEnabled(FQCN, Level.INFO, (Marker)null, string, object, object2, object3, object4, object5, object6, object7);
    }

    @Override
    public void info(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.logIfEnabled(FQCN, Level.INFO, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8);
    }

    @Override
    public void info(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.logIfEnabled(FQCN, Level.INFO, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8, object9);
    }

    @Override
    public void info(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.logIfEnabled(FQCN, Level.INFO, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
    }

    @Override
    public boolean isDebugEnabled() {
        return this.isEnabled(Level.DEBUG, null, null);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return this.isEnabled(Level.DEBUG, marker, (Object)null, null);
    }

    @Override
    public boolean isEnabled(Level level) {
        return this.isEnabled(level, null, (Object)null, null);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker) {
        return this.isEnabled(level, marker, (Object)null, null);
    }

    @Override
    public boolean isErrorEnabled() {
        return this.isEnabled(Level.ERROR, null, (Object)null, null);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return this.isEnabled(Level.ERROR, marker, (Object)null, null);
    }

    @Override
    public boolean isFatalEnabled() {
        return this.isEnabled(Level.FATAL, null, (Object)null, null);
    }

    @Override
    public boolean isFatalEnabled(Marker marker) {
        return this.isEnabled(Level.FATAL, marker, (Object)null, null);
    }

    @Override
    public boolean isInfoEnabled() {
        return this.isEnabled(Level.INFO, null, (Object)null, null);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return this.isEnabled(Level.INFO, marker, (Object)null, null);
    }

    @Override
    public boolean isTraceEnabled() {
        return this.isEnabled(Level.TRACE, null, (Object)null, null);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return this.isEnabled(Level.TRACE, marker, (Object)null, null);
    }

    @Override
    public boolean isWarnEnabled() {
        return this.isEnabled(Level.WARN, null, (Object)null, null);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return this.isEnabled(Level.WARN, marker, (Object)null, null);
    }

    @Override
    public void log(Level level, Marker marker, Message message) {
        Message message2 = message;
        this.logIfEnabled(FQCN, level, marker, message2, message2 != null ? message.getThrowable() : null);
    }

    @Override
    public void log(Level level, Marker marker, Message message, Throwable throwable) {
        this.logIfEnabled(FQCN, level, marker, message, throwable);
    }

    @Override
    public void log(Level level, Marker marker, CharSequence charSequence) {
        this.logIfEnabled(FQCN, level, marker, charSequence, null);
    }

    @Override
    public void log(Level level, Marker marker, CharSequence charSequence, Throwable throwable) {
        if (this.isEnabled(level, marker, charSequence, throwable)) {
            this.logMessage(FQCN, level, marker, charSequence, throwable);
        }
    }

    @Override
    public void log(Level level, Marker marker, Object object) {
        this.logIfEnabled(FQCN, level, marker, object, null);
    }

    @Override
    public void log(Level level, Marker marker, Object object, Throwable throwable) {
        if (this.isEnabled(level, marker, object, throwable)) {
            this.logMessage(FQCN, level, marker, object, throwable);
        }
    }

    @Override
    public void log(Level level, Marker marker, String string) {
        this.logIfEnabled(FQCN, level, marker, string, (Throwable)null);
    }

    @Override
    public void log(Level level, Marker marker, String string, Object ... objectArray) {
        this.logIfEnabled(FQCN, level, marker, string, objectArray);
    }

    @Override
    public void log(Level level, Marker marker, String string, Throwable throwable) {
        this.logIfEnabled(FQCN, level, marker, string, throwable);
    }

    @Override
    public void log(Level level, Message message) {
        Message message2 = message;
        this.logIfEnabled(FQCN, level, null, message2, message2 != null ? message.getThrowable() : null);
    }

    @Override
    public void log(Level level, Message message, Throwable throwable) {
        this.logIfEnabled(FQCN, level, null, message, throwable);
    }

    @Override
    public void log(Level level, CharSequence charSequence) {
        this.logIfEnabled(FQCN, level, null, charSequence, null);
    }

    @Override
    public void log(Level level, CharSequence charSequence, Throwable throwable) {
        this.logIfEnabled(FQCN, level, null, charSequence, throwable);
    }

    @Override
    public void log(Level level, Object object) {
        this.logIfEnabled(FQCN, level, null, object, null);
    }

    @Override
    public void log(Level level, Object object, Throwable throwable) {
        this.logIfEnabled(FQCN, level, null, object, throwable);
    }

    @Override
    public void log(Level level, String string) {
        this.logIfEnabled(FQCN, level, (Marker)null, string, (Throwable)null);
    }

    @Override
    public void log(Level level, String string, Object ... objectArray) {
        this.logIfEnabled(FQCN, level, (Marker)null, string, objectArray);
    }

    @Override
    public void log(Level level, String string, Throwable throwable) {
        this.logIfEnabled(FQCN, level, (Marker)null, string, throwable);
    }

    @Override
    public void log(Level level, Supplier<?> supplier) {
        this.logIfEnabled(FQCN, level, null, supplier, null);
    }

    @Override
    public void log(Level level, Supplier<?> supplier, Throwable throwable) {
        this.logIfEnabled(FQCN, level, null, supplier, throwable);
    }

    @Override
    public void log(Level level, Marker marker, Supplier<?> supplier) {
        this.logIfEnabled(FQCN, level, marker, supplier, null);
    }

    @Override
    public void log(Level level, Marker marker, String string, Supplier<?> ... supplierArray) {
        this.logIfEnabled(FQCN, level, marker, string, supplierArray);
    }

    @Override
    public void log(Level level, Marker marker, Supplier<?> supplier, Throwable throwable) {
        this.logIfEnabled(FQCN, level, marker, supplier, throwable);
    }

    @Override
    public void log(Level level, String string, Supplier<?> ... supplierArray) {
        this.logIfEnabled(FQCN, level, (Marker)null, string, supplierArray);
    }

    @Override
    public void log(Level level, Marker marker, MessageSupplier messageSupplier) {
        this.logIfEnabled(FQCN, level, marker, messageSupplier, null);
    }

    @Override
    public void log(Level level, Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        this.logIfEnabled(FQCN, level, marker, messageSupplier, throwable);
    }

    @Override
    public void log(Level level, MessageSupplier messageSupplier) {
        this.logIfEnabled(FQCN, level, null, messageSupplier, null);
    }

    @Override
    public void log(Level level, MessageSupplier messageSupplier, Throwable throwable) {
        this.logIfEnabled(FQCN, level, null, messageSupplier, throwable);
    }

    @Override
    public void log(Level level, Marker marker, String string, Object object) {
        this.logIfEnabled(FQCN, level, marker, string, object);
    }

    @Override
    public void log(Level level, Marker marker, String string, Object object, Object object2) {
        this.logIfEnabled(FQCN, level, marker, string, object, object2);
    }

    @Override
    public void log(Level level, Marker marker, String string, Object object, Object object2, Object object3) {
        this.logIfEnabled(FQCN, level, marker, string, object, object2, object3);
    }

    @Override
    public void log(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        this.logIfEnabled(FQCN, level, marker, string, object, object2, object3, object4);
    }

    @Override
    public void log(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.logIfEnabled(FQCN, level, marker, string, object, object2, object3, object4, object5);
    }

    @Override
    public void log(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.logIfEnabled(FQCN, level, marker, string, object, object2, object3, object4, object5, object6);
    }

    @Override
    public void log(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.logIfEnabled(FQCN, level, marker, string, object, object2, object3, object4, object5, object6, object7);
    }

    @Override
    public void log(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.logIfEnabled(FQCN, level, marker, string, object, object2, object3, object4, object5, object6, object7, object8);
    }

    @Override
    public void log(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.logIfEnabled(FQCN, level, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9);
    }

    @Override
    public void log(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.logIfEnabled(FQCN, level, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
    }

    @Override
    public void log(Level level, String string, Object object) {
        this.logIfEnabled(FQCN, level, (Marker)null, string, object);
    }

    @Override
    public void log(Level level, String string, Object object, Object object2) {
        this.logIfEnabled(FQCN, level, (Marker)null, string, object, object2);
    }

    @Override
    public void log(Level level, String string, Object object, Object object2, Object object3) {
        this.logIfEnabled(FQCN, level, (Marker)null, string, object, object2, object3);
    }

    @Override
    public void log(Level level, String string, Object object, Object object2, Object object3, Object object4) {
        this.logIfEnabled(FQCN, level, (Marker)null, string, object, object2, object3, object4);
    }

    @Override
    public void log(Level level, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.logIfEnabled(FQCN, level, (Marker)null, string, object, object2, object3, object4, object5);
    }

    @Override
    public void log(Level level, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.logIfEnabled(FQCN, level, (Marker)null, string, object, object2, object3, object4, object5, object6);
    }

    @Override
    public void log(Level level, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.logIfEnabled(FQCN, level, (Marker)null, string, object, object2, object3, object4, object5, object6, object7);
    }

    @Override
    public void log(Level level, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.logIfEnabled(FQCN, level, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8);
    }

    @Override
    public void log(Level level, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.logIfEnabled(FQCN, level, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8, object9);
    }

    @Override
    public void log(Level level, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.logIfEnabled(FQCN, level, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, Message message, Throwable throwable) {
        if (this.isEnabled(level, marker, message, throwable)) {
            this.logMessageSafely(string, level, marker, message, throwable);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        if (this.isEnabled(level, marker, messageSupplier, throwable)) {
            this.logMessage(string, level, marker, messageSupplier, throwable);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, Object object, Throwable throwable) {
        if (this.isEnabled(level, marker, object, throwable)) {
            this.logMessage(string, level, marker, object, throwable);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, CharSequence charSequence, Throwable throwable) {
        if (this.isEnabled(level, marker, charSequence, throwable)) {
            this.logMessage(string, level, marker, charSequence, throwable);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, Supplier<?> supplier, Throwable throwable) {
        if (this.isEnabled(level, marker, supplier, throwable)) {
            this.logMessage(string, level, marker, supplier, throwable);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, String string2) {
        if (this.isEnabled(level, marker, string2)) {
            this.logMessage(string, level, marker, string2);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, String string2, Supplier<?> ... supplierArray) {
        if (this.isEnabled(level, marker, string2)) {
            this.logMessage(string, level, marker, string2, supplierArray);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, String string2, Object ... objectArray) {
        if (this.isEnabled(level, marker, string2, objectArray)) {
            this.logMessage(string, level, marker, string2, objectArray);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, String string2, Object object) {
        if (this.isEnabled(level, marker, string2, object)) {
            this.logMessage(string, level, marker, string2, object);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, String string2, Object object, Object object2) {
        if (this.isEnabled(level, marker, string2, object, object2)) {
            this.logMessage(string, level, marker, string2, object, object2);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, String string2, Object object, Object object2, Object object3) {
        if (this.isEnabled(level, marker, string2, object, object2, object3)) {
            this.logMessage(string, level, marker, string2, object, object2, object3);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, String string2, Object object, Object object2, Object object3, Object object4) {
        if (this.isEnabled(level, marker, string2, object, object2, object3, object4)) {
            this.logMessage(string, level, marker, string2, object, object2, object3, object4);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, String string2, Object object, Object object2, Object object3, Object object4, Object object5) {
        if (this.isEnabled(level, marker, string2, object, object2, object3, object4, object5)) {
            this.logMessage(string, level, marker, string2, object, object2, object3, object4, object5);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, String string2, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        if (this.isEnabled(level, marker, string2, object, object2, object3, object4, object5, object6)) {
            this.logMessage(string, level, marker, string2, object, object2, object3, object4, object5, object6);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, String string2, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        if (this.isEnabled(level, marker, string2, object, object2, object3, object4, object5, object6, object7)) {
            this.logMessage(string, level, marker, string2, object, object2, object3, object4, object5, object6, object7);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, String string2, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        if (this.isEnabled(level, marker, string2, object, object2, object3, object4, object5, object6, object7, object8)) {
            this.logMessage(string, level, marker, string2, object, object2, object3, object4, object5, object6, object7, object8);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, String string2, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        if (this.isEnabled(level, marker, string2, object, object2, object3, object4, object5, object6, object7, object8, object9)) {
            this.logMessage(string, level, marker, string2, object, object2, object3, object4, object5, object6, object7, object8, object9);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, String string2, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        if (this.isEnabled(level, marker, string2, object, object2, object3, object4, object5, object6, object7, object8, object9, object10)) {
            this.logMessage(string, level, marker, string2, object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
        }
    }

    @Override
    public void logIfEnabled(String string, Level level, Marker marker, String string2, Throwable throwable) {
        if (this.isEnabled(level, marker, string2, throwable)) {
            this.logMessage(string, level, marker, string2, throwable);
        }
    }

    protected void logMessage(String string, Level level, Marker marker, CharSequence charSequence, Throwable throwable) {
        this.logMessageSafely(string, level, marker, this.messageFactory.newMessage(charSequence), throwable);
    }

    protected void logMessage(String string, Level level, Marker marker, Object object, Throwable throwable) {
        this.logMessageSafely(string, level, marker, this.messageFactory.newMessage(object), throwable);
    }

    protected void logMessage(String string, Level level, Marker marker, MessageSupplier object, Throwable throwable) {
        object = LambdaUtil.get((MessageSupplier)object);
        this.logMessageSafely(string, level, marker, (Message)object, throwable == null && object != null ? object.getThrowable() : throwable);
    }

    protected void logMessage(String string, Level level, Marker marker, Supplier<?> object, Throwable throwable) {
        object = LambdaUtil.getMessage(object, this.messageFactory);
        this.logMessageSafely(string, level, marker, (Message)object, throwable == null && object != null ? object.getThrowable() : throwable);
    }

    protected void logMessage(String string, Level level, Marker marker, String string2, Throwable throwable) {
        this.logMessageSafely(string, level, marker, this.messageFactory.newMessage(string2), throwable);
    }

    protected void logMessage(String string, Level level, Marker marker, String object) {
        Object object2 = object = this.messageFactory.newMessage((String)object);
        this.logMessageSafely(string, level, marker, (Message)object2, object2.getThrowable());
    }

    protected void logMessage(String string, Level level, Marker marker, String object, Object ... objectArray) {
        Object object2 = object = this.messageFactory.newMessage((String)object, objectArray);
        this.logMessageSafely(string, level, marker, (Message)object2, object2.getThrowable());
    }

    protected void logMessage(String string, Level level, Marker marker, String object, Object object2) {
        Object object3 = object = this.messageFactory.newMessage((String)object, object2);
        this.logMessageSafely(string, level, marker, (Message)object3, object3.getThrowable());
    }

    protected void logMessage(String string, Level level, Marker marker, String object, Object object2, Object object3) {
        Object object4 = object = this.messageFactory.newMessage((String)object, object2, object3);
        this.logMessageSafely(string, level, marker, (Message)object4, object4.getThrowable());
    }

    protected void logMessage(String string, Level level, Marker marker, String object, Object object2, Object object3, Object object4) {
        Object object5 = object = this.messageFactory.newMessage((String)object, object2, object3, object4);
        this.logMessageSafely(string, level, marker, (Message)object5, object5.getThrowable());
    }

    protected void logMessage(String string, Level level, Marker marker, String object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object = this.messageFactory.newMessage((String)object, object2, object3, object4, object5);
        this.logMessageSafely(string, level, marker, (Message)object6, object6.getThrowable());
    }

    protected void logMessage(String string, Level level, Marker marker, String object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object = this.messageFactory.newMessage((String)object, object2, object3, object4, object5, object6);
        this.logMessageSafely(string, level, marker, (Message)object7, object7.getThrowable());
    }

    protected void logMessage(String string, Level level, Marker marker, String object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        Object object8 = object = this.messageFactory.newMessage((String)object, object2, object3, object4, object5, object6, object7);
        this.logMessageSafely(string, level, marker, (Message)object8, object8.getThrowable());
    }

    protected void logMessage(String string, Level level, Marker marker, String object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        Object object9 = object = this.messageFactory.newMessage((String)object, object2, object3, object4, object5, object6, object7, object8);
        this.logMessageSafely(string, level, marker, (Message)object9, object9.getThrowable());
    }

    protected void logMessage(String string, Level level, Marker marker, String object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        Object object10 = object = this.messageFactory.newMessage((String)object, object2, object3, object4, object5, object6, object7, object8, object9);
        this.logMessageSafely(string, level, marker, (Message)object10, object10.getThrowable());
    }

    protected void logMessage(String string, Level level, Marker marker, String object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        Object object11 = object = this.messageFactory.newMessage((String)object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
        this.logMessageSafely(string, level, marker, (Message)object11, object11.getThrowable());
    }

    protected void logMessage(String string, Level level, Marker marker, String object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11) {
        Object object12 = object = this.messageFactory.newMessage((String)object, object2, object3, object4, object5, object6, object7, object8, object9, object10, object11);
        this.logMessageSafely(string, level, marker, (Message)object12, object12.getThrowable());
    }

    protected void logMessage(String string, Level level, Marker marker, String object, Supplier<?> ... supplierArray) {
        Object object2 = object = this.messageFactory.newMessage((String)object, LambdaUtil.getAll(supplierArray));
        this.logMessageSafely(string, level, marker, (Message)object2, object2.getThrowable());
    }

    @Override
    public void logMessage(Level level, Marker marker, String string, StackTraceElement stackTraceElement, Message message, Throwable throwable) {
        try {
            AbstractLogger.incrementRecursionDepth();
            this.log(level, marker, string, stackTraceElement, message, throwable);
            return;
        }
        catch (Exception exception) {
            this.handleLogMessageException(exception, string, message);
            return;
        }
        finally {
            AbstractLogger.decrementRecursionDepth();
            ReusableMessageFactory.release(message);
        }
    }

    protected void log(Level level, Marker marker, String string, StackTraceElement stackTraceElement, Message message, Throwable throwable) {
        this.logMessage(string, level, marker, message, throwable);
    }

    @Override
    public void printf(Level level, Marker marker, String object, Object ... objectArray) {
        if (this.isEnabled(level, marker, (String)object, objectArray)) {
            Object object2 = object = new StringFormattedMessage((String)object, objectArray);
            this.logMessageSafely(FQCN, level, marker, (Message)object2, object2.getThrowable());
        }
    }

    @Override
    public void printf(Level level, String object, Object ... objectArray) {
        if (this.isEnabled(level, (Marker)null, (String)object, objectArray)) {
            Object object2 = object = new StringFormattedMessage((String)object, objectArray);
            this.logMessageSafely(FQCN, level, null, (Message)object2, object2.getThrowable());
        }
    }

    @PerformanceSensitive
    private void logMessageSafely(String string, Level level, Marker marker, Message message, Throwable throwable) {
        try {
            this.logMessageTrackRecursion(string, level, marker, message, throwable);
            return;
        }
        finally {
            ReusableMessageFactory.release(message);
        }
    }

    @PerformanceSensitive
    private void logMessageTrackRecursion(String string, Level level, Marker marker, Message message, Throwable throwable) {
        try {
            AbstractLogger.incrementRecursionDepth();
            this.tryLogMessage(string, this.getLocation(string), level, marker, message, throwable);
            return;
        }
        finally {
            AbstractLogger.decrementRecursionDepth();
        }
    }

    private static int[] getRecursionDepthHolder() {
        int[] nArray = recursionDepthHolder.get();
        if (nArray == null) {
            nArray = new int[1];
            recursionDepthHolder.set(nArray);
        }
        return nArray;
    }

    private static void incrementRecursionDepth() {
        int[] nArray = AbstractLogger.getRecursionDepthHolder();
        nArray[0] = nArray[0] + 1;
    }

    private static void decrementRecursionDepth() {
        int[] nArray = AbstractLogger.getRecursionDepthHolder();
        int[] nArray2 = nArray;
        nArray[0] = nArray[0] - 1;
        if (nArray2[0] < 0) {
            throw new IllegalStateException("Recursion depth became negative: " + nArray2[0]);
        }
    }

    public static int getRecursionDepth() {
        return AbstractLogger.getRecursionDepthHolder()[0];
    }

    @PerformanceSensitive
    private void tryLogMessage(String string, StackTraceElement stackTraceElement, Level level, Marker marker, Message message, Throwable throwable) {
        try {
            this.log(level, marker, string, stackTraceElement, message, throwable);
            return;
        }
        catch (Exception exception) {
            this.handleLogMessageException(exception, string, message);
            return;
        }
    }

    @PerformanceSensitive
    private StackTraceElement getLocation(String string) {
        if (this.requiresLocation()) {
            return StackLocatorUtil.calcLocation(string);
        }
        return null;
    }

    private void handleLogMessageException(Exception exception, String string, Message message) {
        if (exception instanceof LoggingException) {
            throw (LoggingException)exception;
        }
        String string2 = message.getFormat();
        int n = string2 == null ? 4 : string2.length();
        StringBuilder stringBuilder = new StringBuilder(n + 100);
        stringBuilder.append(string);
        stringBuilder.append(" caught ");
        stringBuilder.append(exception.getClass().getName());
        stringBuilder.append(" logging ");
        stringBuilder.append(message.getClass().getSimpleName());
        stringBuilder.append(": ");
        stringBuilder.append(string2);
        StatusLogger.getLogger().warn(stringBuilder.toString(), (Throwable)exception);
    }

    @Override
    public <T extends Throwable> T throwing(T t) {
        return this.throwing(FQCN, Level.ERROR, t);
    }

    @Override
    public <T extends Throwable> T throwing(Level level, T t) {
        return this.throwing(FQCN, level, t);
    }

    protected <T extends Throwable> T throwing(String string, Level level, T t) {
        if (this.isEnabled(level, THROWING_MARKER, (Object)null, null)) {
            this.logMessageSafely(string, level, THROWING_MARKER, this.throwingMsg(t), t);
        }
        return t;
    }

    protected Message throwingMsg(Throwable throwable) {
        return this.messageFactory.newMessage(THROWING);
    }

    @Override
    public void trace(Marker marker, Message message) {
        Message message2 = message;
        this.logIfEnabled(FQCN, Level.TRACE, marker, message2, message2 != null ? message.getThrowable() : null);
    }

    @Override
    public void trace(Marker marker, Message message, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, message, throwable);
    }

    @Override
    public void trace(Marker marker, CharSequence charSequence) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, charSequence, null);
    }

    @Override
    public void trace(Marker marker, CharSequence charSequence, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, charSequence, throwable);
    }

    @Override
    public void trace(Marker marker, Object object) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, object, null);
    }

    @Override
    public void trace(Marker marker, Object object, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, object, throwable);
    }

    @Override
    public void trace(Marker marker, String string) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, string, (Throwable)null);
    }

    @Override
    public void trace(Marker marker, String string, Object ... objectArray) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, string, objectArray);
    }

    @Override
    public void trace(Marker marker, String string, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, string, throwable);
    }

    @Override
    public void trace(Message message) {
        Message message2 = message;
        this.logIfEnabled(FQCN, Level.TRACE, null, message2, message2 != null ? message.getThrowable() : null);
    }

    @Override
    public void trace(Message message, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.TRACE, null, message, throwable);
    }

    @Override
    public void trace(CharSequence charSequence) {
        this.logIfEnabled(FQCN, Level.TRACE, null, charSequence, null);
    }

    @Override
    public void trace(CharSequence charSequence, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.TRACE, null, charSequence, throwable);
    }

    @Override
    public void trace(Object object) {
        this.logIfEnabled(FQCN, Level.TRACE, null, object, null);
    }

    @Override
    public void trace(Object object, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.TRACE, null, object, throwable);
    }

    @Override
    public void trace(String string) {
        this.logIfEnabled(FQCN, Level.TRACE, (Marker)null, string, (Throwable)null);
    }

    @Override
    public void trace(String string, Object ... objectArray) {
        this.logIfEnabled(FQCN, Level.TRACE, (Marker)null, string, objectArray);
    }

    @Override
    public void trace(String string, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.TRACE, (Marker)null, string, throwable);
    }

    @Override
    public void trace(Supplier<?> supplier) {
        this.logIfEnabled(FQCN, Level.TRACE, null, supplier, null);
    }

    @Override
    public void trace(Supplier<?> supplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.TRACE, null, supplier, throwable);
    }

    @Override
    public void trace(Marker marker, Supplier<?> supplier) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, supplier, null);
    }

    @Override
    public void trace(Marker marker, String string, Supplier<?> ... supplierArray) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, string, supplierArray);
    }

    @Override
    public void trace(Marker marker, Supplier<?> supplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, supplier, throwable);
    }

    @Override
    public void trace(String string, Supplier<?> ... supplierArray) {
        this.logIfEnabled(FQCN, Level.TRACE, (Marker)null, string, supplierArray);
    }

    @Override
    public void trace(Marker marker, MessageSupplier messageSupplier) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, messageSupplier, null);
    }

    @Override
    public void trace(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, messageSupplier, throwable);
    }

    @Override
    public void trace(MessageSupplier messageSupplier) {
        this.logIfEnabled(FQCN, Level.TRACE, null, messageSupplier, null);
    }

    @Override
    public void trace(MessageSupplier messageSupplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.TRACE, null, messageSupplier, throwable);
    }

    @Override
    public void trace(Marker marker, String string, Object object) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, string, object);
    }

    @Override
    public void trace(Marker marker, String string, Object object, Object object2) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, string, object, object2);
    }

    @Override
    public void trace(Marker marker, String string, Object object, Object object2, Object object3) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, string, object, object2, object3);
    }

    @Override
    public void trace(Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, string, object, object2, object3, object4);
    }

    @Override
    public void trace(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, string, object, object2, object3, object4, object5);
    }

    @Override
    public void trace(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, string, object, object2, object3, object4, object5, object6);
    }

    @Override
    public void trace(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, string, object, object2, object3, object4, object5, object6, object7);
    }

    @Override
    public void trace(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, string, object, object2, object3, object4, object5, object6, object7, object8);
    }

    @Override
    public void trace(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9);
    }

    @Override
    public void trace(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.logIfEnabled(FQCN, Level.TRACE, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
    }

    @Override
    public void trace(String string, Object object) {
        this.logIfEnabled(FQCN, Level.TRACE, (Marker)null, string, object);
    }

    @Override
    public void trace(String string, Object object, Object object2) {
        this.logIfEnabled(FQCN, Level.TRACE, (Marker)null, string, object, object2);
    }

    @Override
    public void trace(String string, Object object, Object object2, Object object3) {
        this.logIfEnabled(FQCN, Level.TRACE, (Marker)null, string, object, object2, object3);
    }

    @Override
    public void trace(String string, Object object, Object object2, Object object3, Object object4) {
        this.logIfEnabled(FQCN, Level.TRACE, (Marker)null, string, object, object2, object3, object4);
    }

    @Override
    public void trace(String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.logIfEnabled(FQCN, Level.TRACE, (Marker)null, string, object, object2, object3, object4, object5);
    }

    @Override
    public void trace(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.logIfEnabled(FQCN, Level.TRACE, (Marker)null, string, object, object2, object3, object4, object5, object6);
    }

    @Override
    public void trace(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.logIfEnabled(FQCN, Level.TRACE, (Marker)null, string, object, object2, object3, object4, object5, object6, object7);
    }

    @Override
    public void trace(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.logIfEnabled(FQCN, Level.TRACE, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8);
    }

    @Override
    public void trace(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.logIfEnabled(FQCN, Level.TRACE, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8, object9);
    }

    @Override
    public void trace(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.logIfEnabled(FQCN, Level.TRACE, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
    }

    @Override
    public EntryMessage traceEntry() {
        return this.enter(FQCN, (String)null, (Object[])null);
    }

    @Override
    public EntryMessage traceEntry(String string, Object ... objectArray) {
        return this.enter(FQCN, string, objectArray);
    }

    @Override
    public EntryMessage traceEntry(Supplier<?> ... supplierArray) {
        return this.enter(FQCN, (String)null, supplierArray);
    }

    @Override
    public EntryMessage traceEntry(String string, Supplier<?> ... supplierArray) {
        return this.enter(FQCN, string, supplierArray);
    }

    @Override
    public EntryMessage traceEntry(Message message) {
        return this.enter(FQCN, message);
    }

    @Override
    public void traceExit() {
        this.exit(FQCN, null, null);
    }

    @Override
    public <R> R traceExit(R r) {
        return this.exit(FQCN, null, r);
    }

    @Override
    public <R> R traceExit(String string, R r) {
        return this.exit(FQCN, string, r);
    }

    @Override
    public void traceExit(EntryMessage entryMessage) {
        if (entryMessage != null && this.isEnabled(Level.TRACE, EXIT_MARKER, entryMessage, null)) {
            this.logMessageSafely(FQCN, Level.TRACE, EXIT_MARKER, this.flowMessageFactory.newExitMessage(entryMessage), null);
        }
    }

    @Override
    public <R> R traceExit(EntryMessage entryMessage, R r) {
        if (entryMessage != null && this.isEnabled(Level.TRACE, EXIT_MARKER, entryMessage, null)) {
            this.logMessageSafely(FQCN, Level.TRACE, EXIT_MARKER, this.flowMessageFactory.newExitMessage(r, entryMessage), null);
        }
        return r;
    }

    @Override
    public <R> R traceExit(Message message, R r) {
        if (message != null && this.isEnabled(Level.TRACE, EXIT_MARKER, message, null)) {
            this.logMessageSafely(FQCN, Level.TRACE, EXIT_MARKER, this.flowMessageFactory.newExitMessage(r, message), null);
        }
        return r;
    }

    @Override
    public void warn(Marker marker, Message message) {
        Message message2 = message;
        this.logIfEnabled(FQCN, Level.WARN, marker, message2, message2 != null ? message.getThrowable() : null);
    }

    @Override
    public void warn(Marker marker, Message message, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.WARN, marker, message, throwable);
    }

    @Override
    public void warn(Marker marker, CharSequence charSequence) {
        this.logIfEnabled(FQCN, Level.WARN, marker, charSequence, null);
    }

    @Override
    public void warn(Marker marker, CharSequence charSequence, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.WARN, marker, charSequence, throwable);
    }

    @Override
    public void warn(Marker marker, Object object) {
        this.logIfEnabled(FQCN, Level.WARN, marker, object, null);
    }

    @Override
    public void warn(Marker marker, Object object, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.WARN, marker, object, throwable);
    }

    @Override
    public void warn(Marker marker, String string) {
        this.logIfEnabled(FQCN, Level.WARN, marker, string, (Throwable)null);
    }

    @Override
    public void warn(Marker marker, String string, Object ... objectArray) {
        this.logIfEnabled(FQCN, Level.WARN, marker, string, objectArray);
    }

    @Override
    public void warn(Marker marker, String string, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.WARN, marker, string, throwable);
    }

    @Override
    public void warn(Message message) {
        Message message2 = message;
        this.logIfEnabled(FQCN, Level.WARN, null, message2, message2 != null ? message.getThrowable() : null);
    }

    @Override
    public void warn(Message message, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.WARN, null, message, throwable);
    }

    @Override
    public void warn(CharSequence charSequence) {
        this.logIfEnabled(FQCN, Level.WARN, null, charSequence, null);
    }

    @Override
    public void warn(CharSequence charSequence, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.WARN, null, charSequence, throwable);
    }

    @Override
    public void warn(Object object) {
        this.logIfEnabled(FQCN, Level.WARN, null, object, null);
    }

    @Override
    public void warn(Object object, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.WARN, null, object, throwable);
    }

    @Override
    public void warn(String string) {
        this.logIfEnabled(FQCN, Level.WARN, (Marker)null, string, (Throwable)null);
    }

    @Override
    public void warn(String string, Object ... objectArray) {
        this.logIfEnabled(FQCN, Level.WARN, (Marker)null, string, objectArray);
    }

    @Override
    public void warn(String string, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.WARN, (Marker)null, string, throwable);
    }

    @Override
    public void warn(Supplier<?> supplier) {
        this.logIfEnabled(FQCN, Level.WARN, null, supplier, null);
    }

    @Override
    public void warn(Supplier<?> supplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.WARN, null, supplier, throwable);
    }

    @Override
    public void warn(Marker marker, Supplier<?> supplier) {
        this.logIfEnabled(FQCN, Level.WARN, marker, supplier, null);
    }

    @Override
    public void warn(Marker marker, String string, Supplier<?> ... supplierArray) {
        this.logIfEnabled(FQCN, Level.WARN, marker, string, supplierArray);
    }

    @Override
    public void warn(Marker marker, Supplier<?> supplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.WARN, marker, supplier, throwable);
    }

    @Override
    public void warn(String string, Supplier<?> ... supplierArray) {
        this.logIfEnabled(FQCN, Level.WARN, (Marker)null, string, supplierArray);
    }

    @Override
    public void warn(Marker marker, MessageSupplier messageSupplier) {
        this.logIfEnabled(FQCN, Level.WARN, marker, messageSupplier, null);
    }

    @Override
    public void warn(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.WARN, marker, messageSupplier, throwable);
    }

    @Override
    public void warn(MessageSupplier messageSupplier) {
        this.logIfEnabled(FQCN, Level.WARN, null, messageSupplier, null);
    }

    @Override
    public void warn(MessageSupplier messageSupplier, Throwable throwable) {
        this.logIfEnabled(FQCN, Level.WARN, null, messageSupplier, throwable);
    }

    @Override
    public void warn(Marker marker, String string, Object object) {
        this.logIfEnabled(FQCN, Level.WARN, marker, string, object);
    }

    @Override
    public void warn(Marker marker, String string, Object object, Object object2) {
        this.logIfEnabled(FQCN, Level.WARN, marker, string, object, object2);
    }

    @Override
    public void warn(Marker marker, String string, Object object, Object object2, Object object3) {
        this.logIfEnabled(FQCN, Level.WARN, marker, string, object, object2, object3);
    }

    @Override
    public void warn(Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        this.logIfEnabled(FQCN, Level.WARN, marker, string, object, object2, object3, object4);
    }

    @Override
    public void warn(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.logIfEnabled(FQCN, Level.WARN, marker, string, object, object2, object3, object4, object5);
    }

    @Override
    public void warn(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.logIfEnabled(FQCN, Level.WARN, marker, string, object, object2, object3, object4, object5, object6);
    }

    @Override
    public void warn(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.logIfEnabled(FQCN, Level.WARN, marker, string, object, object2, object3, object4, object5, object6, object7);
    }

    @Override
    public void warn(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.logIfEnabled(FQCN, Level.WARN, marker, string, object, object2, object3, object4, object5, object6, object7, object8);
    }

    @Override
    public void warn(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.logIfEnabled(FQCN, Level.WARN, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9);
    }

    @Override
    public void warn(Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.logIfEnabled(FQCN, Level.WARN, marker, string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
    }

    @Override
    public void warn(String string, Object object) {
        this.logIfEnabled(FQCN, Level.WARN, (Marker)null, string, object);
    }

    @Override
    public void warn(String string, Object object, Object object2) {
        this.logIfEnabled(FQCN, Level.WARN, (Marker)null, string, object, object2);
    }

    @Override
    public void warn(String string, Object object, Object object2, Object object3) {
        this.logIfEnabled(FQCN, Level.WARN, (Marker)null, string, object, object2, object3);
    }

    @Override
    public void warn(String string, Object object, Object object2, Object object3, Object object4) {
        this.logIfEnabled(FQCN, Level.WARN, (Marker)null, string, object, object2, object3, object4);
    }

    @Override
    public void warn(String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        this.logIfEnabled(FQCN, Level.WARN, (Marker)null, string, object, object2, object3, object4, object5);
    }

    @Override
    public void warn(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.logIfEnabled(FQCN, Level.WARN, (Marker)null, string, object, object2, object3, object4, object5, object6);
    }

    @Override
    public void warn(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.logIfEnabled(FQCN, Level.WARN, (Marker)null, string, object, object2, object3, object4, object5, object6, object7);
    }

    @Override
    public void warn(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.logIfEnabled(FQCN, Level.WARN, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8);
    }

    @Override
    public void warn(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.logIfEnabled(FQCN, Level.WARN, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8, object9);
    }

    @Override
    public void warn(String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        this.logIfEnabled(FQCN, Level.WARN, (Marker)null, string, object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
    }

    protected boolean requiresLocation() {
        return false;
    }

    @Override
    public LogBuilder atTrace() {
        return this.atLevel(Level.TRACE);
    }

    @Override
    public LogBuilder atDebug() {
        return this.atLevel(Level.DEBUG);
    }

    @Override
    public LogBuilder atInfo() {
        return this.atLevel(Level.INFO);
    }

    @Override
    public LogBuilder atWarn() {
        return this.atLevel(Level.WARN);
    }

    @Override
    public LogBuilder atError() {
        return this.atLevel(Level.ERROR);
    }

    @Override
    public LogBuilder atFatal() {
        return this.atLevel(Level.FATAL);
    }

    @Override
    public LogBuilder always() {
        DefaultLogBuilder defaultLogBuilder = this.logBuilder.get();
        if (defaultLogBuilder.isInUse()) {
            return new DefaultLogBuilder(this);
        }
        return defaultLogBuilder.reset(Level.OFF);
    }

    @Override
    public LogBuilder atLevel(Level level) {
        if (this.isEnabled(level)) {
            return this.getLogBuilder(level).reset(level);
        }
        return LogBuilder.NOOP;
    }

    private DefaultLogBuilder getLogBuilder(Level level) {
        DefaultLogBuilder defaultLogBuilder = this.logBuilder.get();
        if (Constants.ENABLE_THREADLOCALS && !defaultLogBuilder.isInUse()) {
            return defaultLogBuilder;
        }
        return new DefaultLogBuilder(this, level);
    }

    private void readObject(ObjectInputStream object) {
        ((ObjectInputStream)object).defaultReadObject();
        try {
            object = this.getClass().getDeclaredField("logBuilder");
            ((AccessibleObject)object).setAccessible(true);
            AbstractLogger abstractLogger = this;
            ((Field)object).set(this, abstractLogger.new LocalLogBuilder(abstractLogger));
            return;
        }
        catch (IllegalAccessException | NoSuchFieldException reflectiveOperationException) {
            StatusLogger.getLogger().warn("Unable to initialize LogBuilder");
            return;
        }
    }

    class LocalLogBuilder
    extends ThreadLocal<DefaultLogBuilder> {
        private AbstractLogger logger;

        LocalLogBuilder(AbstractLogger abstractLogger2) {
            this.logger = abstractLogger2;
        }

        @Override
        protected DefaultLogBuilder initialValue() {
            return new DefaultLogBuilder(this.logger);
        }
    }
}

