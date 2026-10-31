/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.routing;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.script.Bindings;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LifeCycle2;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.appender.routing.PurgePolicy;
import org.apache.logging.log4j.core.appender.routing.Route;
import org.apache.logging.log4j.core.appender.routing.Routes;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.script.AbstractScript;
import org.apache.logging.log4j.core.script.ScriptManager;
import org.apache.logging.log4j.core.util.Booleans;

@Plugin(name="Routing", category="Core", elementType="appender", printObject=true)
public final class RoutingAppender
extends AbstractAppender {
    public static final String STATIC_VARIABLES_KEY = "staticVariables";
    private static final String DEFAULT_KEY = "ROUTING_APPENDER_DEFAULT";
    private final Routes routes;
    private Route defaultRoute;
    private final Configuration configuration;
    private final ConcurrentMap<String, CreatedRouteAppenderControl> createdAppenders = new ConcurrentHashMap<String, CreatedRouteAppenderControl>();
    private final Map<String, AppenderControl> createdAppendersUnmodifiableView = Collections.unmodifiableMap(this.createdAppenders);
    private final ConcurrentMap<String, RouteAppenderControl> referencedAppenders = new ConcurrentHashMap<String, RouteAppenderControl>();
    private final RewritePolicy rewritePolicy;
    private final PurgePolicy purgePolicy;
    private final AbstractScript defaultRouteScript;
    private final ConcurrentMap<Object, Object> scriptStaticVariables = new ConcurrentHashMap<Object, Object>();

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    /*
     * WARNING - void declaration
     */
    private RoutingAppender(String object, Filter routeArray, boolean n, Routes routes, RewritePolicy object22, Configuration configuration, PurgePolicy purgePolicy, AbstractScript abstractScript, Property[] propertyArray) {
        super((String)object, (Filter)routeArray, null, n != 0, (Property[])var9_12);
        void var8_11;
        void var7_10;
        void var6_9;
        void var9_12;
        this.routes = routes;
        this.configuration = var6_9;
        this.rewritePolicy = object22;
        this.purgePolicy = var7_10;
        if (this.purgePolicy != null) {
            this.purgePolicy.initialize(this);
        }
        this.defaultRouteScript = var8_11;
        object = null;
        for (Route route : routes.getRoutes()) {
            if (route.getKey() != null) continue;
            if (object == null) {
                object = route;
                continue;
            }
            this.error("Multiple default routes. Route " + route.toString() + " will be ignored");
        }
        this.defaultRoute = object;
    }

    @Override
    public final void start() {
        if (this.defaultRouteScript != null) {
            if (this.configuration == null) {
                this.error("No Configuration defined for RoutingAppender; required for Script element.");
            } else {
                ScriptManager scriptManager = this.configuration.getScriptManager();
                scriptManager.addScript(this.defaultRouteScript);
                Bindings bindings = scriptManager.createBindings(this.defaultRouteScript);
                bindings.put(STATIC_VARIABLES_KEY, (Object)this.scriptStaticVariables);
                Object object = scriptManager.execute(this.defaultRouteScript.getName(), bindings);
                Route object2 = this.routes.getRoute(Objects.toString(object, null));
                if (object2 != null) {
                    this.defaultRoute = object2;
                }
            }
        }
        for (Route route : this.routes.getRoutes()) {
            if (route.getAppenderRef() == null) continue;
            Object t = this.configuration.getAppender(route.getAppenderRef());
            if (t != null) {
                String string = route == this.defaultRoute ? DEFAULT_KEY : route.getKey();
                this.referencedAppenders.put(string, new ReferencedRouteAppenderControl((Appender)t));
                continue;
            }
            this.error("Appender " + route.getAppenderRef() + " cannot be located. Route ignored");
        }
        super.start();
    }

    @Override
    public final boolean stop(long l, TimeUnit timeUnit) {
        this.setStopping();
        super.stop(l, timeUnit, false);
        Iterator iterator = this.createdAppenders.entrySet().iterator();
        while (iterator.hasNext()) {
            Appender appender = ((CreatedRouteAppenderControl)iterator.next().getValue()).getAppender();
            if (appender instanceof LifeCycle2) {
                ((LifeCycle2)((Object)appender)).stop(l, timeUnit);
                continue;
            }
            appender.stop();
        }
        this.setStopped();
        return true;
    }

    @Override
    public final void append(LogEvent logEvent) {
        String string;
        RouteAppenderControl routeAppenderControl;
        if (this.rewritePolicy != null) {
            logEvent = this.rewritePolicy.rewrite(logEvent);
        }
        if ((routeAppenderControl = this.getControl(string = (string = this.routes.getPattern(logEvent, this.scriptStaticVariables)) != null ? this.configuration.getStrSubstitutor().replace(logEvent, string) : (this.defaultRoute.getKey() != null ? this.defaultRoute.getKey() : DEFAULT_KEY), logEvent)) != null) {
            try {
                routeAppenderControl.callAppender(logEvent);
            }
            finally {
                routeAppenderControl.release();
            }
        }
        this.updatePurgePolicy(string, logEvent);
    }

    private void updatePurgePolicy(String string, LogEvent logEvent) {
        if (this.purgePolicy != null && !this.referencedAppenders.containsKey(string)) {
            this.purgePolicy.update(string, logEvent);
        }
    }

    private synchronized RouteAppenderControl getControl(String string, LogEvent logEvent) {
        RouteAppenderControl routeAppenderControl = this.getAppender(string);
        if (routeAppenderControl != null) {
            routeAppenderControl.checkout();
            return routeAppenderControl;
        }
        Route route = null;
        for (Route route2 : this.routes.getRoutes()) {
            if (route2.getAppenderRef() != null || !string.equals(route2.getKey())) continue;
            route = route2;
            break;
        }
        if (route == null) {
            route = this.defaultRoute;
            routeAppenderControl = this.getAppender(DEFAULT_KEY);
            if (routeAppenderControl != null) {
                routeAppenderControl.checkout();
                return routeAppenderControl;
            }
        }
        if (route != null) {
            Appender appender = this.createAppender(route, logEvent);
            if (appender == null) {
                return null;
            }
            CreatedRouteAppenderControl createdRouteAppenderControl = new CreatedRouteAppenderControl(appender);
            routeAppenderControl = createdRouteAppenderControl;
            this.createdAppenders.put(string, createdRouteAppenderControl);
        }
        if (routeAppenderControl != null) {
            routeAppenderControl.checkout();
        }
        return routeAppenderControl;
    }

    private RouteAppenderControl getAppender(String string) {
        RouteAppenderControl routeAppenderControl = (RouteAppenderControl)this.referencedAppenders.get(string);
        if (routeAppenderControl == null) {
            return (RouteAppenderControl)this.createdAppenders.get(string);
        }
        return routeAppenderControl;
    }

    private Appender createAppender(Route object, LogEvent logEvent) {
        for (Node node : ((Route)object).getNode().getChildren()) {
            if (!node.getType().getElementName().equals("appender")) continue;
            object = new Node(node);
            this.configuration.createConfiguration((Node)object, logEvent);
            if (((Node)object).getObject() instanceof Appender) {
                object = (Appender)((Node)object).getObject();
                object.start();
                return object;
            }
            this.error("Unable to create Appender of type " + node.getName());
            return null;
        }
        this.error("No Appender was configured for route " + ((Route)object).getKey());
        return null;
    }

    public final Map<String, AppenderControl> getAppenders() {
        return this.createdAppendersUnmodifiableView;
    }

    public final void deleteAppender(String object) {
        LOGGER.debug("Deleting route with {} key ", object);
        CreatedRouteAppenderControl createdRouteAppenderControl = (CreatedRouteAppenderControl)this.createdAppenders.remove(object);
        if (createdRouteAppenderControl != null) {
            LOGGER.debug("Stopping route with {} key", object);
            object = this;
            synchronized (object) {
                createdRouteAppenderControl.pendingDeletion = true;
            }
            createdRouteAppenderControl.tryStopAppender();
            return;
        }
        if (this.referencedAppenders.containsKey(object)) {
            LOGGER.debug("Route {} using an appender reference may not be removed because the appender may be used outside of the RoutingAppender", object);
            return;
        }
        LOGGER.debug("Route with {} key already deleted", object);
    }

    @Deprecated
    public static RoutingAppender createAppender(String string, String string2, Routes routes, Configuration configuration, RewritePolicy rewritePolicy, PurgePolicy purgePolicy, Filter filter) {
        boolean bl = Booleans.parseBoolean(string2, true);
        if (string == null) {
            LOGGER.error("No name provided for RoutingAppender");
            return null;
        }
        if (routes == null) {
            LOGGER.error("No routes defined for RoutingAppender");
            return null;
        }
        return new RoutingAppender(string, filter, bl, routes, rewritePolicy, configuration, purgePolicy, null, null);
    }

    public final Route getDefaultRoute() {
        return this.defaultRoute;
    }

    public final AbstractScript getDefaultRouteScript() {
        return this.defaultRouteScript;
    }

    public final PurgePolicy getPurgePolicy() {
        return this.purgePolicy;
    }

    public final RewritePolicy getRewritePolicy() {
        return this.rewritePolicy;
    }

    public final Routes getRoutes() {
        return this.routes;
    }

    public final Configuration getConfiguration() {
        return this.configuration;
    }

    public final ConcurrentMap<Object, Object> getScriptStaticVariables() {
        return this.scriptStaticVariables;
    }

    static final class ReferencedRouteAppenderControl
    extends RouteAppenderControl {
        ReferencedRouteAppenderControl(Appender appender) {
            super(appender);
        }

        @Override
        final void checkout() {
        }

        @Override
        final void release() {
        }
    }

    static final class CreatedRouteAppenderControl
    extends RouteAppenderControl {
        private volatile boolean pendingDeletion = false;
        private final AtomicInteger depth = new AtomicInteger();

        CreatedRouteAppenderControl(Appender appender) {
            super(appender);
        }

        @Override
        final void checkout() {
            if (this.pendingDeletion) {
                LOGGER.warn("CreatedRouteAppenderControl.checkout invoked on a RouteAppenderControl that is pending deletion");
            }
            this.depth.incrementAndGet();
        }

        @Override
        final void release() {
            this.depth.decrementAndGet();
            this.tryStopAppender();
        }

        final void tryStopAppender() {
            if (this.pendingDeletion && this.depth.compareAndSet(0, -100000)) {
                Appender appender = this.getAppender();
                LOGGER.debug("Stopping appender {}", (Object)appender);
                appender.stop();
            }
        }
    }

    static abstract class RouteAppenderControl
    extends AppenderControl {
        RouteAppenderControl(Appender appender) {
            super(appender, null, null);
        }

        abstract void checkout();

        abstract void release();
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractAppender.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<RoutingAppender> {
        @PluginElement(value="Script")
        private AbstractScript defaultRouteScript;
        @PluginElement(value="Routes")
        private Routes routes;
        @PluginElement(value="RewritePolicy")
        private RewritePolicy rewritePolicy;
        @PluginElement(value="PurgePolicy")
        private PurgePolicy purgePolicy;

        @Override
        public RoutingAppender build() {
            String string = this.getName();
            if (string == null) {
                LOGGER.error("No name defined for this RoutingAppender");
                return null;
            }
            if (this.routes == null) {
                LOGGER.error("No routes defined for RoutingAppender {}", (Object)string);
                return null;
            }
            return new RoutingAppender(string, this.getFilter(), this.isIgnoreExceptions(), this.routes, this.rewritePolicy, this.getConfiguration(), this.purgePolicy, this.defaultRouteScript, this.getPropertyArray());
        }

        public Routes getRoutes() {
            return this.routes;
        }

        public AbstractScript getDefaultRouteScript() {
            return this.defaultRouteScript;
        }

        public RewritePolicy getRewritePolicy() {
            return this.rewritePolicy;
        }

        public PurgePolicy getPurgePolicy() {
            return this.purgePolicy;
        }

        public B withRoutes(Routes routes) {
            this.routes = routes;
            return (B)((Builder)this.asBuilder());
        }

        public B withDefaultRouteScript(AbstractScript abstractScript) {
            this.defaultRouteScript = abstractScript;
            return (B)((Builder)this.asBuilder());
        }

        public B withRewritePolicy(RewritePolicy rewritePolicy) {
            this.rewritePolicy = rewritePolicy;
            return (B)((Builder)this.asBuilder());
        }

        public void withPurgePolicy(PurgePolicy purgePolicy) {
            this.purgePolicy = purgePolicy;
        }
    }
}

