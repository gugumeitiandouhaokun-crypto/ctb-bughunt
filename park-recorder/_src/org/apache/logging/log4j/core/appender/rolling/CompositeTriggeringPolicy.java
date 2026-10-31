/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LifeCycle2;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rolling.AbstractTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name="Policies", category="Core", printObject=true)
public final class CompositeTriggeringPolicy
extends AbstractTriggeringPolicy {
    private final TriggeringPolicy[] triggeringPolicies;

    private CompositeTriggeringPolicy(TriggeringPolicy ... triggeringPolicyArray) {
        this.triggeringPolicies = triggeringPolicyArray;
    }

    public final TriggeringPolicy[] getTriggeringPolicies() {
        return this.triggeringPolicies;
    }

    @Override
    public final void initialize(RollingFileManager rollingFileManager) {
        TriggeringPolicy[] triggeringPolicyArray = this.triggeringPolicies;
        int n = this.triggeringPolicies.length;
        for (int j = 0; j < n; ++j) {
            TriggeringPolicy triggeringPolicy = triggeringPolicyArray[j];
            LOGGER.debug("Initializing triggering policy {}", (Object)triggeringPolicy.toString());
            triggeringPolicy.initialize(rollingFileManager);
        }
    }

    @Override
    public final boolean isTriggeringEvent(LogEvent logEvent) {
        TriggeringPolicy[] triggeringPolicyArray = this.triggeringPolicies;
        int n = this.triggeringPolicies.length;
        for (int j = 0; j < n; ++j) {
            if (!triggeringPolicyArray[j].isTriggeringEvent(logEvent)) continue;
            return true;
        }
        return false;
    }

    @PluginFactory
    public static CompositeTriggeringPolicy createPolicy(TriggeringPolicy ... triggeringPolicyArray) {
        return new CompositeTriggeringPolicy(triggeringPolicyArray);
    }

    @Override
    public final boolean stop(long l, TimeUnit timeUnit) {
        this.setStopping();
        boolean bl = true;
        TriggeringPolicy[] triggeringPolicyArray = this.triggeringPolicies;
        int n = this.triggeringPolicies.length;
        for (int j = 0; j < n; ++j) {
            TriggeringPolicy triggeringPolicy = triggeringPolicyArray[j];
            if (triggeringPolicy instanceof LifeCycle2) {
                bl &= ((LifeCycle2)((Object)triggeringPolicy)).stop(l, timeUnit);
                continue;
            }
            if (!(triggeringPolicy instanceof LifeCycle)) continue;
            ((LifeCycle)((Object)triggeringPolicy)).stop();
            bl &= true;
        }
        this.setStopped();
        return bl;
    }

    public final String toString() {
        return "CompositeTriggeringPolicy(policies=" + Arrays.toString(this.triggeringPolicies) + ")";
    }
}

