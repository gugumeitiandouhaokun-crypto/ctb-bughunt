/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.jms.Connection
 *  javax.jms.ConnectionFactory
 *  javax.jms.Destination
 *  javax.jms.JMSException
 *  javax.jms.MapMessage
 *  javax.jms.Message
 *  javax.jms.MessageConsumer
 *  javax.jms.MessageProducer
 *  javax.jms.Session
 */
package org.apache.logging.log4j.core.appender.mom;

import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.NamingException;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.net.JndiManager;
import org.apache.logging.log4j.core.util.Log4jThread;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.BiConsumer;

public class JmsManager
extends AbstractManager {
    static final JmsManagerFactory FACTORY = new JmsManagerFactory();
    private final JmsManagerConfiguration configuration;
    private volatile Reconnector reconnector;
    private volatile JndiManager jndiManager;
    private volatile Connection connection;
    private volatile Session session;
    private volatile Destination destination;
    private volatile MessageProducer messageProducer;

    public static JmsManager getJmsManager(String string, Properties object, String string2, String string3, String string4, char[] cArray, boolean bl, long l) {
        object = new JmsManagerConfiguration((Properties)object, string2, string3, string4, cArray, bl, l);
        return JmsManager.getManager(string, FACTORY, object);
    }

    private JmsManager(String string, JmsManagerConfiguration jmsManagerConfiguration) {
        super(null, string);
        this.configuration = jmsManagerConfiguration;
        this.jndiManager = jmsManagerConfiguration.getJndiManager();
        try {
            this.connection = this.createConnection(this.jndiManager);
            this.session = this.createSession(this.connection);
            this.destination = this.createDestination(this.jndiManager);
            this.messageProducer = this.createMessageProducer(this.session, this.destination);
            this.connection.start();
            return;
        }
        catch (JMSException | NamingException throwable) {
            this.reconnector = this.createReconnector();
            this.reconnector.start();
            return;
        }
    }

    private boolean closeConnection() {
        if (this.connection == null) {
            return true;
        }
        Connection connection = this.connection;
        this.connection = null;
        try {
            connection.close();
            return true;
        }
        catch (JMSException jMSException) {
            StatusLogger.getLogger().debug("Caught exception closing JMS Connection: {} ({}); continuing JMS manager shutdown", (Object)jMSException.getLocalizedMessage(), (Object)connection, (Object)jMSException);
            return false;
        }
    }

    private boolean closeJndiManager() {
        if (this.jndiManager == null) {
            return true;
        }
        JndiManager jndiManager = this.jndiManager;
        this.jndiManager = null;
        jndiManager.close();
        return true;
    }

    private boolean closeMessageProducer() {
        if (this.messageProducer == null) {
            return true;
        }
        MessageProducer messageProducer = this.messageProducer;
        this.messageProducer = null;
        try {
            messageProducer.close();
            return true;
        }
        catch (JMSException jMSException) {
            StatusLogger.getLogger().debug("Caught exception closing JMS MessageProducer: {} ({}); continuing JMS manager shutdown", (Object)jMSException.getLocalizedMessage(), (Object)messageProducer, (Object)jMSException);
            return false;
        }
    }

    private boolean closeSession() {
        if (this.session == null) {
            return true;
        }
        Session session = this.session;
        this.session = null;
        try {
            session.close();
            return true;
        }
        catch (JMSException jMSException) {
            StatusLogger.getLogger().debug("Caught exception closing JMS Session: {} ({}); continuing JMS manager shutdown", (Object)jMSException.getLocalizedMessage(), (Object)session, (Object)jMSException);
            return false;
        }
    }

    private Connection createConnection(JndiManager jndiManager) {
        jndiManager = (ConnectionFactory)jndiManager.lookup(this.configuration.getConnectionFactoryName());
        if (this.configuration.getUserName() != null && this.configuration.getPassword() != null) {
            return jndiManager.createConnection(this.configuration.getUserName(), this.configuration.getPassword() == null ? null : String.valueOf(this.configuration.getPassword()));
        }
        return jndiManager.createConnection();
    }

    private Destination createDestination(JndiManager jndiManager) {
        return (Destination)jndiManager.lookup(this.configuration.getDestinationName());
    }

    public Message createMessage(Serializable serializable) {
        if (serializable instanceof String) {
            return this.session.createTextMessage((String)((Object)serializable));
        }
        if (serializable instanceof org.apache.logging.log4j.message.MapMessage) {
            return this.map((org.apache.logging.log4j.message.MapMessage)serializable, this.session.createMapMessage());
        }
        return this.session.createObjectMessage(serializable);
    }

    private void createMessageAndSend(LogEvent logEvent, Serializable serializable) {
        serializable = this.createMessage(serializable);
        serializable.setJMSTimestamp(logEvent.getTimeMillis());
        this.messageProducer.send((Message)serializable);
    }

    public MessageConsumer createMessageConsumer() {
        return this.session.createConsumer(this.destination);
    }

    public MessageProducer createMessageProducer(Session session, Destination destination) {
        return session.createProducer(destination);
    }

    private Reconnector createReconnector() {
        JmsManager jmsManager = this;
        Reconnector reconnector = jmsManager.new Reconnector(jmsManager);
        reconnector.setDaemon(true);
        reconnector.setPriority(1);
        return reconnector;
    }

    private Session createSession(Connection connection) {
        return connection.createSession(false, 1);
    }

    public JmsManagerConfiguration getJmsManagerConfiguration() {
        return this.configuration;
    }

    JndiManager getJndiManager() {
        return this.configuration.getJndiManager();
    }

    <T> T lookup(String string) {
        return this.jndiManager.lookup(string);
    }

    private MapMessage map(org.apache.logging.log4j.message.MapMessage<?, ?> mapMessage, final MapMessage mapMessage2) {
        mapMessage.forEach(new BiConsumer<String, Object>(){

            @Override
            public void accept(String string, Object object) {
                try {
                    mapMessage2.setObject(string, object);
                    return;
                }
                catch (JMSException jMSException) {
                    throw new IllegalArgumentException(String.format("%s mapping key '%s' to value '%s': %s", ((Object)((Object)jMSException)).getClass(), string, object, jMSException.getLocalizedMessage()), jMSException);
                }
            }
        });
        return mapMessage2;
    }

    @Override
    public boolean releaseSub(long l, TimeUnit timeUnit) {
        if (this.reconnector != null) {
            this.reconnector.shutdown();
            this.reconnector.interrupt();
            this.reconnector = null;
        }
        return false & this.closeJndiManager() & this.closeMessageProducer() & this.closeSession() & this.closeConnection() && this.jndiManager.stop(l, timeUnit);
    }

    void send(LogEvent logEvent, Serializable serializable) {
        if (this.messageProducer == null && this.reconnector != null && !this.configuration.isImmediateFail()) {
            this.reconnector.latch();
            if (this.messageProducer == null) {
                throw new AppenderLoggingException("Error sending to JMS Manager '" + this.getName() + "': JMS message producer not available");
            }
        }
        JmsManager jmsManager = this;
        synchronized (jmsManager) {
            block11: {
                try {
                    this.createMessageAndSend(logEvent, serializable);
                }
                catch (JMSException jMSException) {
                    if (!this.configuration.isRetry() || this.reconnector != null) break block11;
                    this.reconnector = this.createReconnector();
                    try {
                        this.closeJndiManager();
                        this.reconnector.reconnect();
                    }
                    catch (JMSException | NamingException throwable) {
                        JmsManager.logger().debug("Cannot reestablish JMS connection to {}: {}; starting reconnector thread {}", (Object)this.configuration, (Object)throwable.getLocalizedMessage(), (Object)this.reconnector.getName(), (Object)throwable);
                        this.reconnector.start();
                        throw new AppenderLoggingException(String.format("JMS exception sending to %s for %s", this.getName(), this.configuration), jMSException);
                    }
                    try {
                        this.createMessageAndSend(logEvent, serializable);
                    }
                    catch (JMSException jMSException2) {
                        throw new AppenderLoggingException(String.format("Error sending to %s after reestablishing JMS connection for %s", this.getName(), this.configuration), jMSException);
                    }
                }
            }
            return;
        }
    }

    class Reconnector
    extends Log4jThread {
        private final CountDownLatch latch;
        private volatile boolean shutdown;
        private final Object owner;

        private Reconnector(Object object) {
            super("JmsManager-Reconnector");
            this.latch = new CountDownLatch(1);
            this.shutdown = false;
            this.owner = object;
        }

        public void latch() {
            try {
                this.latch.await();
                return;
            }
            catch (InterruptedException interruptedException) {
                return;
            }
        }

        void reconnect() {
            JndiManager jndiManager = JmsManager.this.getJndiManager();
            Connection connection = JmsManager.this.createConnection(jndiManager);
            Session session = JmsManager.this.createSession(connection);
            Destination destination = JmsManager.this.createDestination(jndiManager);
            MessageProducer messageProducer = JmsManager.this.createMessageProducer(session, destination);
            connection.start();
            Object object = this.owner;
            synchronized (object) {
                JmsManager.this.jndiManager = jndiManager;
                JmsManager.this.connection = connection;
                JmsManager.this.session = session;
                JmsManager.this.destination = destination;
                JmsManager.this.messageProducer = messageProducer;
                JmsManager.this.reconnector = null;
                this.shutdown = true;
            }
            JmsManager.logger().debug("Connection reestablished to {}", (Object)JmsManager.this.configuration);
        }

        @Override
        public void run() {
            while (!this.shutdown) {
                try {
                    Reconnector.sleep(JmsManager.this.configuration.getReconnectIntervalMillis());
                    this.reconnect();
                }
                catch (InterruptedException | JMSException | NamingException throwable) {
                    JmsManager.logger().debug("Cannot reestablish JMS connection to {}: {}", (Object)JmsManager.this.configuration, (Object)throwable.getLocalizedMessage(), (Object)throwable);
                }
                finally {
                    this.latch.countDown();
                }
            }
        }

        public void shutdown() {
            this.shutdown = true;
        }
    }

    static class JmsManagerFactory
    implements ManagerFactory<JmsManager, JmsManagerConfiguration> {
        private JmsManagerFactory() {
        }

        @Override
        public JmsManager createManager(String string, JmsManagerConfiguration jmsManagerConfiguration) {
            try {
                return new JmsManager(string, jmsManagerConfiguration);
            }
            catch (Exception exception) {
                JmsManager.logger().error("Error creating JmsManager using JmsManagerConfiguration [{}]", (Object)jmsManagerConfiguration, (Object)exception);
                return null;
            }
        }
    }

    public static class JmsManagerConfiguration {
        private final Properties jndiProperties;
        private final String connectionFactoryName;
        private final String destinationName;
        private final String userName;
        private final char[] password;
        private final boolean immediateFail;
        private final boolean retry;
        private final long reconnectIntervalMillis;

        JmsManagerConfiguration(Properties properties, String string, String string2, String string3, char[] cArray, boolean bl, long l) {
            this.jndiProperties = properties;
            this.connectionFactoryName = string;
            this.destinationName = string2;
            this.userName = string3;
            this.password = cArray;
            this.immediateFail = bl;
            this.reconnectIntervalMillis = l;
            this.retry = l > 0L;
        }

        public String getConnectionFactoryName() {
            return this.connectionFactoryName;
        }

        public String getDestinationName() {
            return this.destinationName;
        }

        public JndiManager getJndiManager() {
            return JndiManager.getJndiManager(this.getJndiProperties());
        }

        public Properties getJndiProperties() {
            return this.jndiProperties;
        }

        public char[] getPassword() {
            return this.password;
        }

        public long getReconnectIntervalMillis() {
            return this.reconnectIntervalMillis;
        }

        public String getUserName() {
            return this.userName;
        }

        public boolean isImmediateFail() {
            return this.immediateFail;
        }

        public boolean isRetry() {
            return this.retry;
        }

        public String toString() {
            return "JmsManagerConfiguration [jndiProperties=" + this.jndiProperties + ", connectionFactoryName=" + this.connectionFactoryName + ", destinationName=" + this.destinationName + ", userName=" + this.userName + ", immediateFail=" + this.immediateFail + ", retry=" + this.retry + ", reconnectIntervalMillis=" + this.reconnectIntervalMillis + "]";
        }
    }
}

