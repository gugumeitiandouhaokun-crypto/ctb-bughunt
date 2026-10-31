/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.jms.JMSException
 */
package org.apache.logging.log4j.core.appender.mom;

import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.jms.JMSException;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.mom.JmsManager;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAliases;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.net.JndiManager;

@Plugin(name="JMS", category="Core", elementType="appender", printObject=true)
@PluginAliases(value={"JMSQueue", "JMSTopic"})
public class JmsAppender
extends AbstractAppender {
    private volatile JmsManager manager;

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    protected JmsAppender(String string, Filter filter, Layout<? extends Serializable> layout, boolean bl, Property[] propertyArray, JmsManager jmsManager) {
        super(string, filter, layout, bl, propertyArray);
        this.manager = jmsManager;
    }

    @Deprecated
    protected JmsAppender(String string, Filter filter, Layout<? extends Serializable> layout, boolean bl, JmsManager jmsManager) {
        super(string, filter, layout, bl, Property.EMPTY_ARRAY);
        this.manager = jmsManager;
    }

    @Override
    public void append(LogEvent logEvent) {
        this.manager.send(logEvent, this.toSerializable(logEvent));
    }

    public JmsManager getManager() {
        return this.manager;
    }

    @Override
    public boolean stop(long l, TimeUnit timeUnit) {
        this.setStopping();
        boolean bl = super.stop(l, timeUnit, false) & this.manager.stop(l, timeUnit);
        this.setStopped();
        return bl;
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractAppender.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<JmsAppender> {
        public static final int DEFAULT_RECONNECT_INTERVAL_MILLIS = 5000;
        @PluginBuilderAttribute
        private String factoryName;
        @PluginBuilderAttribute
        private String providerUrl;
        @PluginBuilderAttribute
        private String urlPkgPrefixes;
        @PluginBuilderAttribute
        private String securityPrincipalName;
        @PluginBuilderAttribute(sensitive=true)
        private String securityCredentials;
        @PluginBuilderAttribute
        @Required(message="A javax.jms.ConnectionFactory JNDI name must be specified")
        private String factoryBindingName;
        @PluginBuilderAttribute
        @PluginAliases(value={"queueBindingName", "topicBindingName"})
        @Required(message="A javax.jms.Destination JNDI name must be specified")
        private String destinationBindingName;
        @PluginBuilderAttribute
        private String userName;
        @PluginBuilderAttribute(sensitive=true)
        private char[] password;
        @PluginBuilderAttribute
        private long reconnectIntervalMillis = 5000L;
        @PluginBuilderAttribute
        private boolean immediateFail;
        private JmsManager jmsManager;

        private Builder() {
        }

        @Override
        public JmsAppender build() {
            Layout<Serializable> layout;
            Object object = this.jmsManager;
            if (object == null) {
                layout = JndiManager.createProperties(this.factoryName, this.providerUrl, this.urlPkgPrefixes, this.securityPrincipalName, this.securityCredentials, null);
                object = new JmsManager.JmsManagerConfiguration((Properties)((Object)layout), this.factoryBindingName, this.destinationBindingName, this.userName, this.password, false, this.reconnectIntervalMillis);
                object = AbstractManager.getManager(this.getName(), JmsManager.FACTORY, object);
            }
            if (object == null) {
                return null;
            }
            layout = this.getLayout();
            if (layout == null) {
                LOGGER.error("No layout provided for JmsAppender");
                return null;
            }
            try {
                return new JmsAppender(this.getName(), this.getFilter(), (Layout<? extends Serializable>)layout, this.isIgnoreExceptions(), this.getPropertyArray(), (JmsManager)object);
            }
            catch (JMSException jMSException) {
                throw new IllegalStateException(jMSException);
            }
        }

        public Builder setDestinationBindingName(String string) {
            this.destinationBindingName = string;
            return this;
        }

        public Builder setFactoryBindingName(String string) {
            this.factoryBindingName = string;
            return this;
        }

        public Builder setFactoryName(String string) {
            this.factoryName = string;
            return this;
        }

        public Builder setImmediateFail(boolean bl) {
            this.immediateFail = bl;
            return this;
        }

        public Builder setJmsManager(JmsManager jmsManager) {
            this.jmsManager = jmsManager;
            return this;
        }

        public Builder setPassword(char[] cArray) {
            this.password = cArray;
            return this;
        }

        @Deprecated
        public Builder setPassword(String string) {
            this.password = string == null ? null : string.toCharArray();
            return this;
        }

        public Builder setProviderUrl(String string) {
            this.providerUrl = string;
            return this;
        }

        public Builder setReconnectIntervalMillis(long l) {
            this.reconnectIntervalMillis = l;
            return this;
        }

        public Builder setSecurityCredentials(String string) {
            this.securityCredentials = string;
            return this;
        }

        public Builder setSecurityPrincipalName(String string) {
            this.securityPrincipalName = string;
            return this;
        }

        public Builder setUrlPkgPrefixes(String string) {
            this.urlPkgPrefixes = string;
            return this;
        }

        @Deprecated
        public Builder setUsername(String string) {
            this.userName = string;
            return this;
        }

        public Builder setUserName(String string) {
            this.userName = string;
            return this;
        }

        public String toString() {
            return "Builder [name=" + this.getName() + ", factoryName=" + this.factoryName + ", providerUrl=" + this.providerUrl + ", urlPkgPrefixes=" + this.urlPkgPrefixes + ", securityPrincipalName=" + this.securityPrincipalName + ", securityCredentials=" + this.securityCredentials + ", factoryBindingName=" + this.factoryBindingName + ", destinationBindingName=" + this.destinationBindingName + ", username=" + this.userName + ", layout=" + this.getLayout() + ", filter=" + this.getFilter() + ", ignoreExceptions=" + this.isIgnoreExceptions() + ", jmsManager=" + this.jmsManager + "]";
        }
    }
}

