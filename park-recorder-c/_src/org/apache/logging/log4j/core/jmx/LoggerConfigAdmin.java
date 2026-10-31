/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.jmx;

import java.util.List;
import java.util.Objects;
import javax.management.ObjectName;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.jmx.LoggerConfigAdminMBean;
import org.apache.logging.log4j.core.jmx.Server;

public class LoggerConfigAdmin
implements LoggerConfigAdminMBean {
    private final LoggerContext loggerContext;
    private final LoggerConfig loggerConfig;
    private final ObjectName objectName;

    public LoggerConfigAdmin(LoggerContext object, LoggerConfig object2) {
        this.loggerContext = Objects.requireNonNull(object, "loggerContext");
        this.loggerConfig = Objects.requireNonNull(object2, "loggerConfig");
        try {
            object = Server.escape(((LoggerContext)object).getName());
            object2 = Server.escape(((LoggerConfig)object2).getName());
            object = String.format("org.apache.logging.log4j2:type=%s,component=Loggers,name=%s", object, object2);
            this.objectName = new ObjectName((String)object);
            return;
        }
        catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    public ObjectName getObjectName() {
        return this.objectName;
    }

    @Override
    public String getName() {
        return this.loggerConfig.getName();
    }

    @Override
    public String getLevel() {
        return this.loggerConfig.getLevel().name();
    }

    @Override
    public void setLevel(String string) {
        this.loggerConfig.setLevel(Level.getLevel(string));
        this.loggerContext.updateLoggers();
    }

    @Override
    public boolean isAdditive() {
        return this.loggerConfig.isAdditive();
    }

    @Override
    public void setAdditive(boolean bl) {
        this.loggerConfig.setAdditive(bl);
        this.loggerContext.updateLoggers();
    }

    @Override
    public boolean isIncludeLocation() {
        return this.loggerConfig.isIncludeLocation();
    }

    @Override
    public String getFilter() {
        return String.valueOf(this.loggerConfig.getFilter());
    }

    @Override
    public String[] getAppenderRefs() {
        List<AppenderRef> list = this.loggerConfig.getAppenderRefs();
        String[] stringArray = new String[list.size()];
        for (int i2 = 0; i2 < stringArray.length; ++i2) {
            stringArray[i2] = list.get(i2).getRef();
        }
        return stringArray;
    }
}

