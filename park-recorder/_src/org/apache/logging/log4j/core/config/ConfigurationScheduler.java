/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.config.CronScheduledFuture;
import org.apache.logging.log4j.core.util.CronExpression;
import org.apache.logging.log4j.core.util.Log4jThreadFactory;
import org.apache.logging.log4j.status.StatusLogger;

public class ConfigurationScheduler
extends AbstractLifeCycle {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String SIMPLE_NAME = "Log4j2 " + ConfigurationScheduler.class.getSimpleName();
    private static final int MAX_SCHEDULED_ITEMS = 5;
    private ScheduledExecutorService executorService;
    private int scheduledItems = 0;
    private final String name;

    public ConfigurationScheduler() {
        this(SIMPLE_NAME);
    }

    public ConfigurationScheduler(String string) {
        this.name = string;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public boolean stop(long l, TimeUnit timeUnit) {
        this.setStopping();
        if (this.isExecutorServiceSet()) {
            LOGGER.debug("{} shutting down threads in {}", (Object)this.name, (Object)this.getExecutorService());
            this.executorService.shutdown();
            try {
                this.executorService.awaitTermination(l, timeUnit);
            }
            catch (InterruptedException interruptedException) {
                this.executorService.shutdownNow();
                try {
                    this.executorService.awaitTermination(l, timeUnit);
                }
                catch (InterruptedException interruptedException2) {
                    LOGGER.warn("{} stopped but some scheduled services may not have completed.", (Object)this.name);
                }
                Thread.currentThread().interrupt();
            }
        }
        this.setStopped();
        return true;
    }

    public boolean isExecutorServiceSet() {
        return this.executorService != null;
    }

    public void incrementScheduledItems() {
        if (this.isExecutorServiceSet()) {
            LOGGER.error("{} attempted to increment scheduled items after start", (Object)this.name);
            return;
        }
        ++this.scheduledItems;
    }

    public void decrementScheduledItems() {
        if (!this.isStarted() && this.scheduledItems > 0) {
            --this.scheduledItems;
        }
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long l, TimeUnit timeUnit) {
        return this.getExecutorService().schedule(callable, l, timeUnit);
    }

    public ScheduledFuture<?> schedule(Runnable runnable, long l, TimeUnit timeUnit) {
        return this.getExecutorService().schedule(runnable, l, timeUnit);
    }

    public CronScheduledFuture<?> scheduleWithCron(CronExpression cronExpression, Runnable runnable) {
        return this.scheduleWithCron(cronExpression, new Date(), runnable);
    }

    public CronScheduledFuture<?> scheduleWithCron(CronExpression cronExpression, Date date, Runnable runnable) {
        date = cronExpression.getNextValidTimeAfter(date == null ? new Date() : date);
        runnable = new CronRunnable(runnable, cronExpression);
        ScheduledFuture<?> scheduledFuture = this.schedule(runnable, this.nextFireInterval(date), TimeUnit.MILLISECONDS);
        scheduledFuture = new CronScheduledFuture(scheduledFuture, date);
        ((CronRunnable)runnable).setScheduledFuture((CronScheduledFuture<?>)scheduledFuture);
        LOGGER.debug("{} scheduled cron expression {} to fire at {}", (Object)this.name, (Object)cronExpression.getCronExpression(), (Object)date);
        return scheduledFuture;
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long l, long l2, TimeUnit timeUnit) {
        return this.getExecutorService().scheduleAtFixedRate(runnable, l, l2, timeUnit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long l, long l2, TimeUnit timeUnit) {
        return this.getExecutorService().scheduleWithFixedDelay(runnable, l, l2, timeUnit);
    }

    public long nextFireInterval(Date date) {
        return date.getTime() - new Date().getTime();
    }

    private ScheduledExecutorService getExecutorService() {
        if (this.executorService == null) {
            if (this.scheduledItems > 0) {
                LOGGER.debug("{} starting {} threads", (Object)this.name, (Object)this.scheduledItems);
                this.scheduledItems = Math.min(this.scheduledItems, 5);
                ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(this.scheduledItems, Log4jThreadFactory.createDaemonThreadFactory("Scheduled"));
                scheduledThreadPoolExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
                scheduledThreadPoolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
                this.executorService = scheduledThreadPoolExecutor;
            } else {
                LOGGER.debug("{}: No scheduled items", (Object)this.name);
            }
        }
        return this.executorService;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("ConfigurationScheduler [name=");
        stringBuilder.append(this.name);
        stringBuilder.append(", [");
        if (this.executorService != null) {
            Object object = ((ScheduledThreadPoolExecutor)this.executorService).getQueue();
            boolean bl = true;
            object = object.iterator();
            while (object.hasNext()) {
                Runnable runnable = (Runnable)object.next();
                if (!bl) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(runnable.toString());
                bl = false;
            }
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public class CronRunnable
    implements Runnable {
        private final CronExpression cronExpression;
        private final Runnable runnable;
        private CronScheduledFuture<?> scheduledFuture;

        public CronRunnable(Runnable runnable, CronExpression cronExpression) {
            this.cronExpression = cronExpression;
            this.runnable = runnable;
        }

        public void setScheduledFuture(CronScheduledFuture<?> cronScheduledFuture) {
            this.scheduledFuture = cronScheduledFuture;
        }

        @Override
        public void run() {
            try {
                long l = this.scheduledFuture.getFireTime().getTime() - System.currentTimeMillis();
                if (l > 0L) {
                    LOGGER.debug("{} Cron thread woke up {} millis early. Sleeping", (Object)ConfigurationScheduler.this.name, (Object)l);
                    try {
                        Thread.sleep(l);
                    }
                    catch (InterruptedException interruptedException) {}
                }
                this.runnable.run();
                Date date = this.cronExpression.getNextValidTimeAfter(new Date());
                CronRunnable cronRunnable = this;
                ScheduledFuture<?> scheduledFuture = ConfigurationScheduler.this.schedule(cronRunnable, cronRunnable.ConfigurationScheduler.this.nextFireInterval(date), TimeUnit.MILLISECONDS);
                LOGGER.debug("{} Cron expression {} scheduled to fire again at {}", (Object)ConfigurationScheduler.this.name, (Object)this.cronExpression.getCronExpression(), (Object)date);
                this.scheduledFuture.reset(scheduledFuture, date);
                return;
            }
            catch (Throwable throwable) {
                try {
                    LOGGER.error("{} caught error running command", (Object)ConfigurationScheduler.this.name, (Object)throwable);
                    Date date = this.cronExpression.getNextValidTimeAfter(new Date());
                    CronRunnable cronRunnable = this;
                    ScheduledFuture<?> scheduledFuture = ConfigurationScheduler.this.schedule(cronRunnable, cronRunnable.ConfigurationScheduler.this.nextFireInterval(date), TimeUnit.MILLISECONDS);
                    LOGGER.debug("{} Cron expression {} scheduled to fire again at {}", (Object)ConfigurationScheduler.this.name, (Object)this.cronExpression.getCronExpression(), (Object)date);
                    this.scheduledFuture.reset(scheduledFuture, date);
                    return;
                }
                catch (Throwable throwable2) {
                    Date date = this.cronExpression.getNextValidTimeAfter(new Date());
                    CronRunnable cronRunnable = this;
                    ScheduledFuture<?> scheduledFuture = ConfigurationScheduler.this.schedule(cronRunnable, cronRunnable.ConfigurationScheduler.this.nextFireInterval(date), TimeUnit.MILLISECONDS);
                    LOGGER.debug("{} Cron expression {} scheduled to fire again at {}", (Object)ConfigurationScheduler.this.name, (Object)this.cronExpression.getCronExpression(), (Object)date);
                    this.scheduledFuture.reset(scheduledFuture, date);
                    throw throwable2;
                }
            }
        }

        public String toString() {
            return "CronRunnable{" + this.cronExpression.getCronExpression() + " - " + this.scheduledFuture.getFireTime();
        }
    }
}

