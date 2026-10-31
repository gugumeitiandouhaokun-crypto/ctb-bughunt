/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.util.JndiCloser;

public class JndiManager
extends AbstractManager {
    private static final JndiManagerFactory FACTORY = new JndiManagerFactory();
    private final Context context;

    private JndiManager(String string, Context context) {
        super(null, string);
        this.context = context;
    }

    public static JndiManager getDefaultManager() {
        return JndiManager.getManager(JndiManager.class.getName(), FACTORY, null);
    }

    public static JndiManager getDefaultManager(String string) {
        return JndiManager.getManager(string, FACTORY, null);
    }

    public static JndiManager getJndiManager(String object, String string, String string2, String string3, String string4, Properties properties) {
        object = JndiManager.createProperties((String)object, string, string2, string3, string4, properties);
        return JndiManager.getManager(JndiManager.createManagerName(), FACTORY, object);
    }

    public static JndiManager getJndiManager(Properties properties) {
        return JndiManager.getManager(JndiManager.createManagerName(), FACTORY, properties);
    }

    private static String createManagerName() {
        return JndiManager.class.getName() + '@' + JndiManager.class.hashCode();
    }

    public static Properties createProperties(String string, String string2, String string3, String string4, String string5, Properties properties) {
        if (string == null) {
            return null;
        }
        Properties properties2 = new Properties();
        properties2.setProperty("java.naming.factory.initial", string);
        if (string2 != null) {
            properties2.setProperty("java.naming.provider.url", string2);
        } else {
            LOGGER.warn("The JNDI InitialContextFactory class name [{}] was provided, but there was no associated provider URL. This is likely to cause problems.", (Object)string);
        }
        if (string3 != null) {
            properties2.setProperty("java.naming.factory.url.pkgs", string3);
        }
        if (string4 != null) {
            properties2.setProperty("java.naming.security.principal", string4);
            if (string5 != null) {
                properties2.setProperty("java.naming.security.credentials", string5);
            } else {
                LOGGER.warn("A security principal [{}] was provided, but with no corresponding security credentials.", (Object)string4);
            }
        }
        if (properties != null) {
            properties2.putAll(properties);
        }
        return properties2;
    }

    @Override
    public boolean releaseSub(long l, TimeUnit timeUnit) {
        return JndiCloser.closeSilently(this.context);
    }

    public <T> T lookup(String string) {
        return (T)this.context.lookup(string);
    }

    public String toString() {
        return "JndiManager [context=" + this.context + ", count=" + this.count + "]";
    }

    static class JndiManagerFactory
    implements ManagerFactory<JndiManager, Properties> {
        private JndiManagerFactory() {
        }

        @Override
        public JndiManager createManager(String string, Properties properties) {
            try {
                return new JndiManager(string, new InitialContext(properties));
            }
            catch (NamingException namingException) {
                LOGGER.error("Error creating JNDI InitialContext.", (Throwable)namingException);
                return null;
            }
        }
    }
}

