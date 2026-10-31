/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.simple;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.Strings;

public class SimpleLogger
extends AbstractLogger {
    private static final long serialVersionUID = 1L;
    private static final char SPACE = ' ';
    private final DateFormat dateFormatter;
    private Level level;
    private final boolean showDateTime;
    private final boolean showContextMap;
    private PrintStream stream;
    private final String logName;

    public SimpleLogger(String string, Level level, boolean bl, boolean bl2, boolean bl3, boolean bl4, String string2, MessageFactory object, PropertiesUtil propertiesUtil, PrintStream printStream) {
        super(string, (MessageFactory)object);
        int n;
        object = propertiesUtil.getStringProperty("org.apache.logging.log4j.simplelog." + string + ".level");
        this.level = Level.toLevel((String)object, level);
        this.logName = bl2 ? ((n = string.lastIndexOf(".")) > 0 && n < string.length() ? string.substring(n + 1) : string) : (bl ? string : null);
        this.showDateTime = bl3;
        this.showContextMap = bl4;
        this.stream = printStream;
        if (bl3) {
            SimpleDateFormat simpleDateFormat;
            try {
                simpleDateFormat = new SimpleDateFormat(string2);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS zzz");
            }
            this.dateFormatter = simpleDateFormat;
            return;
        }
        this.dateFormatter = null;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Message message, Throwable throwable) {
        return this.level.intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, CharSequence charSequence, Throwable throwable) {
        return this.level.intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Object object, Throwable throwable) {
        return this.level.intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string) {
        return this.level.intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object ... objectArray) {
        return this.level.intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object) {
        return this.level.intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2) {
        return this.level.intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3) {
        return this.level.intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4) {
        return this.level.intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5) {
        return this.level.intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        return this.level.intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        return this.level.intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        return this.level.intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        return this.level.intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10) {
        return this.level.intLevel() >= level.intLevel();
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String string, Throwable throwable) {
        return this.level.intLevel() >= level.intLevel();
    }

    /*
     * WARNING - void declaration
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void logMessage(String charSequence, Level level, Marker object, Message message, Throwable throwable) {
        void var5_5;
        void var4_4;
        void var2_2;
        Object object2;
        charSequence = new StringBuilder();
        if (this.showDateTime) {
            object2 = new Date();
            DateFormat dateFormat = this.dateFormatter;
            synchronized (dateFormat) {
                object2 = this.dateFormatter.format((Date)object2);
            }
            ((StringBuilder)charSequence).append((String)object2);
            ((StringBuilder)charSequence).append(' ');
        }
        ((StringBuilder)charSequence).append(var2_2.toString());
        ((StringBuilder)charSequence).append(' ');
        if (Strings.isNotEmpty(this.logName)) {
            ((StringBuilder)charSequence).append(this.logName);
            ((StringBuilder)charSequence).append(' ');
        }
        ((StringBuilder)charSequence).append(var4_4.getFormattedMessage());
        if (this.showContextMap && (object2 = ThreadContext.getImmutableContext()).size() > 0) {
            ((StringBuilder)charSequence).append(' ');
            ((StringBuilder)charSequence).append(object2.toString());
            ((StringBuilder)charSequence).append(' ');
        }
        object2 = var4_4.getParameters();
        object2 = var5_5 == null && object2 != null && ((Object[])object2).length > 0 && object2[((Object[])object2).length - 1] instanceof Throwable ? (Throwable)object2[((Object[])object2).length - 1] : var5_5;
        this.stream.println(((StringBuilder)charSequence).toString());
        if (object2 != null) {
            this.stream.print(' ');
            object2.printStackTrace(this.stream);
        }
    }

    public void setLevel(Level level) {
        if (level != null) {
            this.level = level;
        }
    }

    public void setStream(PrintStream printStream) {
        this.stream = printStream;
    }
}

