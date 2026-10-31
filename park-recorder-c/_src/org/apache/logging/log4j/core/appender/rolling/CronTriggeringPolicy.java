/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rolling.AbstractTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationScheduler;
import org.apache.logging.log4j.core.config.CronScheduledFuture;
import org.apache.logging.log4j.core.config.Scheduled;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.util.CronExpression;

@Plugin(name="CronTriggeringPolicy", category="Core", printObject=true)
@Scheduled
public final class CronTriggeringPolicy
extends AbstractTriggeringPolicy {
    private static final String defaultSchedule = "0 0 0 * * ?";
    private RollingFileManager manager;
    private final CronExpression cronExpression;
    private final Configuration configuration;
    private final boolean checkOnStartup;
    private volatile Date lastRollDate;
    private CronScheduledFuture<?> future;

    private CronTriggeringPolicy(CronExpression cronExpression, boolean bl, Configuration configuration) {
        this.cronExpression = Objects.requireNonNull(cronExpression, "schedule");
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.checkOnStartup = bl;
    }

    @Override
    public final void initialize(RollingFileManager object) {
        this.manager = object;
        Date date = new Date();
        Date date2 = this.cronExpression.getPrevFireTime(new Date(this.manager.getFileTime()));
        Date date3 = this.cronExpression.getPrevFireTime(new Date());
        ((RollingFileManager)object).getPatternProcessor().setCurrentFileTime(date3.getTime());
        LOGGER.debug("LastRollForFile {}, LastRegularRole {}", (Object)date2, (Object)date3);
        ((RollingFileManager)object).getPatternProcessor().setPrevFileTime(date3.getTime());
        ((RollingFileManager)object).getPatternProcessor().setTimeBased(true);
        if (this.checkOnStartup && date2 != null && date3 != null && date2.before(date3)) {
            this.lastRollDate = date2;
            this.rollover();
        }
        if (!((ConfigurationScheduler)(object = this.configuration.getScheduler())).isExecutorServiceSet()) {
            ((ConfigurationScheduler)object).incrementScheduledItems();
        }
        if (!((AbstractLifeCycle)object).isStarted()) {
            ((ConfigurationScheduler)object).start();
        }
        this.lastRollDate = date3;
        this.future = ((ConfigurationScheduler)object).scheduleWithCron(this.cronExpression, date, new CronTrigger());
        LOGGER.debug(((ConfigurationScheduler)object).toString());
    }

    @Override
    public final boolean isTriggeringEvent(LogEvent logEvent) {
        return false;
    }

    public final CronExpression getCronExpression() {
        return this.cronExpression;
    }

    @PluginFactory
    public static CronTriggeringPolicy createPolicy(@PluginConfiguration Configuration configuration, @PluginAttribute(value="evaluateOnStartup") String object, @PluginAttribute(value="schedule") String string) {
        boolean bl = Boolean.parseBoolean((String)object);
        if (string == null) {
            LOGGER.info("No schedule specified, defaulting to Daily");
            object = CronTriggeringPolicy.getSchedule(defaultSchedule);
        } else {
            object = CronTriggeringPolicy.getSchedule(string);
            if (object == null) {
                LOGGER.error("Invalid expression specified. Defaulting to Daily");
                object = CronTriggeringPolicy.getSchedule(defaultSchedule);
            }
        }
        return new CronTriggeringPolicy((CronExpression)object, bl, configuration);
    }

    private static CronExpression getSchedule(String string) {
        try {
            return new CronExpression(string);
        }
        catch (ParseException parseException) {
            LOGGER.error("Invalid cron expression - ".concat(String.valueOf(string)), (Throwable)parseException);
            return null;
        }
    }

    private void rollover() {
        this.manager.rollover(this.cronExpression.getPrevFireTime(new Date()), this.lastRollDate);
        if (this.future != null) {
            this.lastRollDate = this.future.getFireTime();
        }
    }

    @Override
    public final boolean stop(long l, TimeUnit timeUnit) {
        this.setStopping();
        CronTriggeringPolicy cronTriggeringPolicy = this;
        boolean bl = cronTriggeringPolicy.stop(cronTriggeringPolicy.future);
        this.setStopped();
        return bl;
    }

    public final String toString() {
        return "CronTriggeringPolicy(schedule=" + this.cronExpression.getCronExpression() + ")";
    }

    class CronTrigger
    implements Runnable {
        private CronTrigger() {
        }

        @Override
        public void run() {
            CronTriggeringPolicy.this.rollover();
        }
    }
}

