/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.selector;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.ContextAnchor;
import org.apache.logging.log4j.core.selector.ContextSelector;

public class BasicContextSelector
implements ContextSelector {
    private static final LoggerContext CONTEXT = new LoggerContext("Default");

    @Override
    public void shutdown(String string, ClassLoader classLoader, boolean bl, boolean bl2) {
        ContextAnchor.THREAD_CONTEXT.get().stop(50L, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean hasContext(String object, ClassLoader classLoader, boolean bl) {
        object = ContextAnchor.THREAD_CONTEXT.get();
        return object != null && ((AbstractLifeCycle)object).isStarted();
    }

    @Override
    public LoggerContext getContext(String object, ClassLoader classLoader, boolean bl) {
        object = ContextAnchor.THREAD_CONTEXT.get();
        if (object != null) {
            return object;
        }
        return CONTEXT;
    }

    @Override
    public LoggerContext getContext(String object, ClassLoader classLoader, boolean bl, URI uRI) {
        object = ContextAnchor.THREAD_CONTEXT.get();
        if (object != null) {
            return object;
        }
        return CONTEXT;
    }

    public LoggerContext locateContext(String string, String string2) {
        return CONTEXT;
    }

    @Override
    public void removeContext(LoggerContext loggerContext) {
    }

    @Override
    public List<LoggerContext> getLoggerContexts() {
        ArrayList<LoggerContext> arrayList = new ArrayList<LoggerContext>();
        arrayList.add(CONTEXT);
        return Collections.unmodifiableList(arrayList);
    }
}

