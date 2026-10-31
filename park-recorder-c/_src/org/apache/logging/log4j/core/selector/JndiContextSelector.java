/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.selector;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.naming.NamingException;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.impl.ContextAnchor;
import org.apache.logging.log4j.core.net.JndiManager;
import org.apache.logging.log4j.core.selector.NamedContextSelector;
import org.apache.logging.log4j.status.StatusLogger;

public class JndiContextSelector
implements NamedContextSelector {
    private static final LoggerContext CONTEXT = new LoggerContext("Default");
    private static final ConcurrentMap<String, LoggerContext> CONTEXT_MAP = new ConcurrentHashMap<String, LoggerContext>();
    private static final StatusLogger LOGGER = StatusLogger.getLogger();

    @Override
    public void shutdown(String object, ClassLoader object2, boolean bl, boolean bl2) {
        object = ContextAnchor.THREAD_CONTEXT.get();
        if (object == null && (object2 = this.getContextName()) != null) {
            object = (LoggerContext)CONTEXT_MAP.get(object2);
        }
        if (object != null) {
            ((LoggerContext)object).stop(50L, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public boolean hasContext(String object, ClassLoader classLoader, boolean bl) {
        object = ContextAnchor.THREAD_CONTEXT.get();
        if (object == null) {
            object = this.getContextName();
            if (object == null) {
                return false;
            }
            object = (LoggerContext)CONTEXT_MAP.get(object);
        }
        return object != null && ((AbstractLifeCycle)object).isStarted();
    }

    @Override
    public LoggerContext getContext(String string, ClassLoader classLoader, boolean bl) {
        return this.getContext(string, classLoader, bl, null);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public LoggerContext getContext(String object, ClassLoader object2, boolean bl, URI uRI) {
        block10: {
            object = ContextAnchor.THREAD_CONTEXT.get();
            if (object != null) {
                return object;
            }
            object = null;
            try {
                object2 = JndiManager.getDefaultManager();
                Throwable throwable = null;
                try {
                    object = (String)((JndiManager)object2).lookup("java:comp/env/log4j/context-name");
                    if (object2 == null) break block10;
                }
                catch (Throwable throwable2) {
                    try {
                        Throwable throwable3 = throwable2;
                        throwable = throwable2;
                        throw throwable3;
                    }
                    catch (Throwable throwable4) {
                        if (object2 == null) throw throwable4;
                        if (throwable == null) {
                            ((AbstractManager)object2).close();
                            throw throwable4;
                        }
                        try {
                            ((AbstractManager)object2).close();
                            throw throwable4;
                        }
                        catch (Throwable throwable5) {
                            throwable.addSuppressed(throwable5);
                            throw throwable4;
                        }
                    }
                }
                ((AbstractManager)object2).close();
            }
            catch (NamingException namingException) {
                LOGGER.error("Unable to lookup {}", (Object)"java:comp/env/log4j/context-name", (Object)namingException);
            }
        }
        if (object != null) return this.locateContext((String)object, null, uRI);
        return CONTEXT;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private String getContextName() {
        String string = null;
        try {
            JndiManager jndiManager = JndiManager.getDefaultManager();
            Throwable throwable = null;
            try {
                string = (String)jndiManager.lookup("java:comp/env/log4j/context-name");
                if (jndiManager == null) return string;
            }
            catch (Throwable throwable2) {
                try {
                    Throwable throwable3 = throwable2;
                    throwable = throwable2;
                    throw throwable3;
                }
                catch (Throwable throwable4) {
                    if (jndiManager == null) throw throwable4;
                    if (throwable != null) {
                        try {
                            jndiManager.close();
                            throw throwable4;
                        }
                        catch (Throwable throwable5) {
                            throwable.addSuppressed(throwable5);
                        }
                        throw throwable4;
                    } else {
                        jndiManager.close();
                    }
                    throw throwable4;
                }
            }
            jndiManager.close();
            return string;
        }
        catch (NamingException namingException) {
            LOGGER.error("Unable to lookup {}", (Object)"java:comp/env/log4j/context-name", (Object)namingException);
        }
        return string;
    }

    @Override
    public LoggerContext locateContext(String string, Object object, URI uRI) {
        if (string == null) {
            LOGGER.error("A context name is required to locate a LoggerContext");
            return null;
        }
        if (!CONTEXT_MAP.containsKey(string)) {
            object = new LoggerContext(string, object, uRI);
            CONTEXT_MAP.putIfAbsent(string, (LoggerContext)object);
        }
        return (LoggerContext)CONTEXT_MAP.get(string);
    }

    @Override
    public void removeContext(LoggerContext loggerContext) {
        for (Map.Entry entry : CONTEXT_MAP.entrySet()) {
            if (!((LoggerContext)entry.getValue()).equals(loggerContext)) continue;
            CONTEXT_MAP.remove(entry.getKey());
        }
    }

    @Override
    public LoggerContext removeContext(String string) {
        return (LoggerContext)CONTEXT_MAP.remove(string);
    }

    @Override
    public List<LoggerContext> getLoggerContexts() {
        return Collections.unmodifiableList(new ArrayList(CONTEXT_MAP.values()));
    }
}

