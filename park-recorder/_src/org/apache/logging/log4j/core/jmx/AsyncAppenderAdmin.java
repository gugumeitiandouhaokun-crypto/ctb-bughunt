/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.jmx;

import java.util.Objects;
import javax.management.ObjectName;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.jmx.AsyncAppenderAdminMBean;
import org.apache.logging.log4j.core.jmx.Server;

public class AsyncAppenderAdmin
implements AsyncAppenderAdminMBean {
    private final String contextName;
    private final AsyncAppender asyncAppender;
    private final ObjectName objectName;

    public AsyncAppenderAdmin(String string, AsyncAppender object) {
        this.contextName = Objects.requireNonNull(string, "contextName");
        this.asyncAppender = Objects.requireNonNull(object, "async appender");
        try {
            string = Server.escape(this.contextName);
            object = Server.escape(((AbstractAppender)object).getName());
            string = String.format("org.apache.logging.log4j2:type=%s,component=AsyncAppenders,name=%s", string, object);
            this.objectName = new ObjectName(string);
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
        return this.asyncAppender.getName();
    }

    @Override
    public String getLayout() {
        return String.valueOf(this.asyncAppender.getLayout());
    }

    @Override
    public boolean isIgnoreExceptions() {
        return this.asyncAppender.ignoreExceptions();
    }

    @Override
    public String getErrorHandler() {
        return String.valueOf(this.asyncAppender.getHandler());
    }

    @Override
    public String getFilter() {
        return String.valueOf(this.asyncAppender.getFilter());
    }

    @Override
    public String[] getAppenderRefs() {
        return this.asyncAppender.getAppenderRefStrings();
    }

    @Override
    public boolean isIncludeLocation() {
        return this.asyncAppender.isIncludeLocation();
    }

    @Override
    public boolean isBlocking() {
        return this.asyncAppender.isBlocking();
    }

    @Override
    public String getErrorRef() {
        return this.asyncAppender.getErrorRef();
    }

    @Override
    public int getQueueCapacity() {
        return this.asyncAppender.getQueueCapacity();
    }

    @Override
    public int getQueueRemainingCapacity() {
        return this.asyncAppender.getQueueRemainingCapacity();
    }
}

