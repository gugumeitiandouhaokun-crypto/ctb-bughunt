/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.jmx;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.jmx.Server;
import org.apache.logging.log4j.core.jmx.StatusLoggerAdminMBean;
import org.apache.logging.log4j.status.StatusData;
import org.apache.logging.log4j.status.StatusListener;
import org.apache.logging.log4j.status.StatusLogger;

public class StatusLoggerAdmin
extends NotificationBroadcasterSupport
implements StatusLoggerAdminMBean,
StatusListener {
    private final AtomicLong sequenceNo = new AtomicLong();
    private final ObjectName objectName;
    private final String contextName;
    private Level level = Level.WARN;

    public StatusLoggerAdmin(String string, Executor object) {
        super((Executor)object, StatusLoggerAdmin.createNotificationInfo());
        this.contextName = string;
        try {
            object = String.format("org.apache.logging.log4j2:type=%s,component=StatusLogger", Server.escape(string));
            this.objectName = new ObjectName((String)object);
        }
        catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
        this.removeListeners(string);
        StatusLogger.getLogger().registerListener(this);
    }

    private void removeListeners(String string) {
        StatusLogger statusLogger = StatusLogger.getLogger();
        for (StatusListener statusListener : statusLogger.getListeners()) {
            if (!(statusListener instanceof StatusLoggerAdmin)) continue;
            statusListener = (StatusLoggerAdmin)statusListener;
            if (string == null || !string.equals(((StatusLoggerAdmin)statusListener).contextName)) continue;
            statusLogger.removeListener(statusListener);
        }
    }

    private static MBeanNotificationInfo createNotificationInfo() {
        String[] stringArray = new String[]{"com.apache.logging.log4j.core.jmx.statuslogger.data", "com.apache.logging.log4j.core.jmx.statuslogger.message"};
        String string = Notification.class.getName();
        return new MBeanNotificationInfo(stringArray, string, "StatusLogger has logged an event");
    }

    @Override
    public String[] getStatusDataHistory() {
        List<StatusData> list = this.getStatusData();
        String[] stringArray = new String[list.size()];
        for (int j = 0; j < stringArray.length; ++j) {
            stringArray[j] = list.get(j).getFormattedStatus();
        }
        return stringArray;
    }

    @Override
    public List<StatusData> getStatusData() {
        return StatusLogger.getLogger().getStatusData();
    }

    @Override
    public String getLevel() {
        return this.level.name();
    }

    @Override
    public Level getStatusLevel() {
        return this.level;
    }

    @Override
    public void setLevel(String string) {
        this.level = Level.toLevel(string, Level.ERROR);
    }

    @Override
    public String getContextName() {
        return this.contextName;
    }

    @Override
    public void log(StatusData statusData) {
        Notification notification = new Notification("com.apache.logging.log4j.core.jmx.statuslogger.message", this.getObjectName(), this.nextSeqNo(), this.nowMillis(), statusData.getFormattedStatus());
        this.sendNotification(notification);
        notification = new Notification("com.apache.logging.log4j.core.jmx.statuslogger.data", (Object)this.getObjectName(), this.nextSeqNo(), this.nowMillis());
        notification.setUserData(statusData);
        this.sendNotification(notification);
    }

    @Override
    public ObjectName getObjectName() {
        return this.objectName;
    }

    private long nextSeqNo() {
        return this.sequenceNo.getAndIncrement();
    }

    private long nowMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public void close() {
    }
}

