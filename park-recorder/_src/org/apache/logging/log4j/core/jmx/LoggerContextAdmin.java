/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.jmx;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.jmx.LoggerContextAdminMBean;
import org.apache.logging.log4j.core.jmx.Server;
import org.apache.logging.log4j.core.util.Closer;
import org.apache.logging.log4j.status.StatusLogger;

public class LoggerContextAdmin
extends NotificationBroadcasterSupport
implements PropertyChangeListener,
LoggerContextAdminMBean {
    private static final int PAGE = 4096;
    private static final int TEXT_BUFFER = 65536;
    private static final int BUFFER_SIZE = 2048;
    private static final StatusLogger LOGGER = StatusLogger.getLogger();
    private final AtomicLong sequenceNo = new AtomicLong();
    private final ObjectName objectName;
    private final LoggerContext loggerContext;

    public LoggerContextAdmin(LoggerContext loggerContext, Executor object) {
        super((Executor)object, LoggerContextAdmin.createNotificationInfo());
        this.loggerContext = Objects.requireNonNull(loggerContext, "loggerContext");
        try {
            object = Server.escape(loggerContext.getName());
            object = String.format("org.apache.logging.log4j2:type=%s", object);
            this.objectName = new ObjectName((String)object);
        }
        catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
        loggerContext.addPropertyChangeListener(this);
    }

    private static MBeanNotificationInfo createNotificationInfo() {
        String[] stringArray = new String[]{"com.apache.logging.log4j.core.jmx.config.reconfigured"};
        String string = Notification.class.getName();
        return new MBeanNotificationInfo(stringArray, string, "Configuration reconfigured");
    }

    @Override
    public String getStatus() {
        return this.loggerContext.getState().toString();
    }

    @Override
    public String getName() {
        return this.loggerContext.getName();
    }

    private Configuration getConfig() {
        return this.loggerContext.getConfiguration();
    }

    @Override
    public String getConfigLocationUri() {
        if (this.loggerContext.getConfigLocation() != null) {
            return String.valueOf(this.loggerContext.getConfigLocation());
        }
        if (this.getConfigName() != null) {
            return String.valueOf(new File(this.getConfigName()).toURI());
        }
        return "";
    }

    @Override
    public void setConfigLocationUri(String object) {
        if (object == null || ((String)object).isEmpty()) {
            throw new IllegalArgumentException("Missing configuration location");
        }
        LOGGER.debug("---------");
        LOGGER.debug("Remote request to reconfigure using location ".concat(String.valueOf(object)));
        File file = new File((String)object);
        if (file.exists()) {
            LOGGER.debug("Opening config file {}", (Object)file.getAbsolutePath());
            object = new ConfigurationSource((InputStream)new FileInputStream(file), file);
        } else {
            object = new URL((String)object);
            LOGGER.debug("Opening config URL {}", object);
            object = new ConfigurationSource(((URL)object).openStream(), (URL)object);
        }
        object = ConfigurationFactory.getInstance().getConfiguration(this.loggerContext, (ConfigurationSource)object);
        this.loggerContext.start((Configuration)object);
        LOGGER.debug("Completed remote request to reconfigure.");
    }

    @Override
    public void propertyChange(PropertyChangeEvent eventObject) {
        if (!"config".equals(eventObject.getPropertyName())) {
            return;
        }
        eventObject = new Notification("com.apache.logging.log4j.core.jmx.config.reconfigured", this.getObjectName(), this.nextSeqNo(), this.now(), null);
        this.sendNotification((Notification)eventObject);
    }

    @Override
    public String getConfigText() {
        return this.getConfigText(StandardCharsets.UTF_8.name());
    }

    @Override
    public String getConfigText(String object) {
        try {
            ConfigurationSource configurationSource = this.loggerContext.getConfiguration().getConfigurationSource().resetInputStream();
            object = Charset.forName((String)object);
            return this.readContents(configurationSource.getInputStream(), (Charset)object);
        }
        catch (Exception exception) {
            StringWriter stringWriter = new StringWriter(2048);
            exception.printStackTrace(new PrintWriter(stringWriter));
            return stringWriter.toString();
        }
    }

    private String readContents(InputStream inputStream, Charset object) {
        InputStreamReader inputStreamReader = null;
        try {
            int n;
            inputStreamReader = new InputStreamReader(inputStream, (Charset)object);
            object = new StringBuilder(65536);
            char[] cArray = new char[4096];
            while ((n = inputStreamReader.read(cArray)) >= 0) {
                ((StringBuilder)object).append(cArray, 0, n);
            }
            object = ((StringBuilder)object).toString();
            return object;
        }
        finally {
            Closer.closeSilently(inputStream);
            Closer.closeSilently(inputStreamReader);
        }
    }

    @Override
    public void setConfigText(String object, String string) {
        LOGGER.debug("---------");
        LOGGER.debug("Remote request to reconfigure from config text.");
        try {
            object = new ByteArrayInputStream(((String)object).getBytes(string));
            object = new ConfigurationSource((InputStream)object);
            object = ConfigurationFactory.getInstance().getConfiguration(this.loggerContext, (ConfigurationSource)object);
            this.loggerContext.start((Configuration)object);
            LOGGER.debug("Completed remote request to reconfigure from config text.");
            return;
        }
        catch (Exception exception) {
            LOGGER.error("Could not reconfigure from config text", (Throwable)exception);
            throw new IllegalArgumentException("Could not reconfigure from config text", exception);
        }
    }

    @Override
    public String getConfigName() {
        return this.getConfig().getName();
    }

    @Override
    public String getConfigClassName() {
        return this.getConfig().getClass().getName();
    }

    @Override
    public String getConfigFilter() {
        return String.valueOf(this.getConfig().getFilter());
    }

    @Override
    public Map<String, String> getConfigProperties() {
        return this.getConfig().getProperties();
    }

    @Override
    public ObjectName getObjectName() {
        return this.objectName;
    }

    private long nextSeqNo() {
        return this.sequenceNo.getAndIncrement();
    }

    private long now() {
        return System.currentTimeMillis();
    }
}

