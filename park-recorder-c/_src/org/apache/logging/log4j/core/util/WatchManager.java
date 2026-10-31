/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.config.ConfigurationFileWatcher;
import org.apache.logging.log4j.core.config.ConfigurationScheduler;
import org.apache.logging.log4j.core.util.FileWatcher;
import org.apache.logging.log4j.core.util.Source;
import org.apache.logging.log4j.core.util.WatchEventService;
import org.apache.logging.log4j.core.util.Watcher;
import org.apache.logging.log4j.core.util.WrappedFileWatcher;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;

public class WatchManager
extends AbstractLifeCycle {
    private static Logger logger = StatusLogger.getLogger();
    private final ConcurrentMap<Source, ConfigurationMonitor> watchers = new ConcurrentHashMap<Source, ConfigurationMonitor>();
    private int intervalSeconds = 0;
    private ScheduledFuture<?> future;
    private final ConfigurationScheduler scheduler;
    private final List<WatchEventService> eventServiceList;
    private final UUID id = LocalUUID.get();

    public WatchManager(ConfigurationScheduler configurationScheduler) {
        this.scheduler = configurationScheduler;
        this.eventServiceList = this.getEventServices();
    }

    public UUID getId() {
        return this.id;
    }

    public boolean hasEventListeners() {
        return this.eventServiceList.size() > 0;
    }

    public void reset() {
        logger.debug("Resetting {}", (Object)this);
        for (Source source : this.watchers.keySet()) {
            this.reset(source);
        }
    }

    public void reset(File object) {
        if (object == null) {
            return;
        }
        object = new Source((File)object);
        this.reset((Source)object);
    }

    public void reset(Source source) {
        Watcher watcher;
        if (source == null) {
            return;
        }
        ConfigurationMonitor configurationMonitor = (ConfigurationMonitor)this.watchers.get(source);
        if (configurationMonitor != null && (watcher = configurationMonitor.getWatcher()).isModified()) {
            long l = watcher.getLastModified();
            if (logger.isDebugEnabled()) {
                logger.debug("Resetting file monitor for '{}' from {} ({}) to {} ({})", (Object)source.getLocation(), (Object)this.millisToString(configurationMonitor.lastModifiedMillis), (Object)configurationMonitor.lastModifiedMillis, (Object)this.millisToString(l), (Object)l);
            }
            configurationMonitor.setLastModifiedMillis(l);
        }
    }

    public void setIntervalSeconds(int n) {
        if (!this.isStarted()) {
            if (this.intervalSeconds > 0 && n == 0) {
                this.scheduler.decrementScheduledItems();
            } else if (this.intervalSeconds == 0 && n > 0) {
                this.scheduler.incrementScheduledItems();
            }
            this.intervalSeconds = n;
        }
    }

    public int getIntervalSeconds() {
        return this.intervalSeconds;
    }

    @Override
    public void start() {
        super.start();
        if (this.intervalSeconds > 0) {
            this.future = this.scheduler.scheduleWithFixedDelay(new WatchRunnable(), this.intervalSeconds, this.intervalSeconds, TimeUnit.SECONDS);
        }
        Iterator<WatchEventService> iterator = this.eventServiceList.iterator();
        while (iterator.hasNext()) {
            iterator.next().subscribe(this);
        }
    }

    @Override
    public boolean stop(long l, TimeUnit timeUnit) {
        this.setStopping();
        Iterator<WatchEventService> iterator = this.eventServiceList.iterator();
        while (iterator.hasNext()) {
            iterator.next().unsubscribe(this);
        }
        WatchManager watchManager = this;
        boolean bl = watchManager.stop(watchManager.future);
        this.setStopped();
        return bl;
    }

    public void unwatchFile(File object) {
        object = new Source((File)object);
        this.unwatch((Source)object);
    }

    public void unwatch(Source source) {
        logger.debug("Unwatching configuration {}", (Object)source);
        this.watchers.remove(source);
    }

    public void checkFiles() {
        new WatchRunnable().run();
    }

    public void watchFile(File object, FileWatcher object2) {
        object2 = object2 instanceof Watcher ? (Watcher)object2 : new WrappedFileWatcher((FileWatcher)object2);
        object = new Source((File)object);
        this.watch((Source)object, (Watcher)object2);
    }

    public void watch(Source source, Watcher watcher) {
        watcher.watching(source);
        long l = watcher.getLastModified();
        if (logger.isDebugEnabled()) {
            logger.debug("Watching configuration '{}' for lastModified {} ({})", (Object)source, (Object)this.millisToString(l), (Object)l);
        }
        this.watchers.put(source, new ConfigurationMonitor(l, watcher));
    }

    public Map<File, FileWatcher> getWatchers() {
        HashMap<File, FileWatcher> hashMap = new HashMap<File, FileWatcher>(this.watchers.size());
        for (Map.Entry entry : this.watchers.entrySet()) {
            if (((ConfigurationMonitor)entry.getValue()).getWatcher() instanceof ConfigurationFileWatcher) {
                hashMap.put(((Source)entry.getKey()).getFile(), (FileWatcher)((Object)((ConfigurationMonitor)entry.getValue()).getWatcher()));
                continue;
            }
            hashMap.put(((Source)entry.getKey()).getFile(), new WrappedFileWatcher((FileWatcher)((Object)((ConfigurationMonitor)entry.getValue()).getWatcher())));
        }
        return hashMap;
    }

    public Map<Source, Watcher> getConfigurationWatchers() {
        HashMap<Source, Watcher> hashMap = new HashMap<Source, Watcher>(this.watchers.size());
        for (Map.Entry entry : this.watchers.entrySet()) {
            hashMap.put((Source)entry.getKey(), ((ConfigurationMonitor)entry.getValue()).getWatcher());
        }
        return hashMap;
    }

    private String millisToString(long l) {
        return new Date(l).toString();
    }

    private List<WatchEventService> getEventServices() {
        ArrayList<WatchEventService> arrayList = new ArrayList<WatchEventService>();
        for (ClassLoader classLoader : LoaderUtil.getClassLoaders()) {
            try {
                for (WatchEventService watchEventService : ServiceLoader.load(WatchEventService.class, classLoader)) {
                    arrayList.add(watchEventService);
                }
            }
            catch (Throwable throwable) {
                LOGGER.debug("Unable to retrieve WatchEventService from ClassLoader {}", (Object)classLoader, (Object)throwable);
            }
        }
        return arrayList;
    }

    public String toString() {
        return "WatchManager [intervalSeconds=" + this.intervalSeconds + ", watchers=" + this.watchers + ", scheduler=" + this.scheduler + ", future=" + this.future + "]";
    }

    static class LocalUUID {
        private static final long LOW_MASK = 0xFFFFFFFFL;
        private static final long MID_MASK = 0xFFFF00000000L;
        private static final long HIGH_MASK = 0xFFF000000000000L;
        private static final int NODE_SIZE = 8;
        private static final int SHIFT_2 = 16;
        private static final int SHIFT_4 = 32;
        private static final int SHIFT_6 = 48;
        private static final int HUNDRED_NANOS_PER_MILLI = 10000;
        private static final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 122192928000000000L;
        private static final AtomicInteger COUNT = new AtomicInteger(0);
        private static final long TYPE1 = 4096L;
        private static final byte VARIANT = -128;
        private static final int SEQUENCE_MASK = 16383;

        private LocalUUID() {
        }

        public static UUID get() {
            long l = System.currentTimeMillis() * 10000L + 122192928000000000L + (long)(COUNT.incrementAndGet() % 10000);
            long l2 = (l & 0xFFFFFFFFL) << 32;
            long l3 = (l & 0xFFFF00000000L) >> 16;
            long l4 = (l & 0xFFF000000000000L) >> 48;
            long l5 = l2 | l3 | 0x1000L | l4;
            return new UUID(l5, COUNT.incrementAndGet());
        }
    }

    final class ConfigurationMonitor {
        private final Watcher watcher;
        private volatile long lastModifiedMillis;

        public final Watcher getWatcher() {
            return this.watcher;
        }

        public ConfigurationMonitor(long l, Watcher watcher) {
            this.watcher = watcher;
            this.lastModifiedMillis = l;
        }

        private void setLastModifiedMillis(long l) {
            this.lastModifiedMillis = l;
        }

        public final String toString() {
            return "ConfigurationMonitor [watcher=" + this.watcher + ", lastModifiedMillis=" + this.lastModifiedMillis + "]";
        }
    }

    final class WatchRunnable
    implements Runnable {
        private final String SIMPLE_NAME = WatchRunnable.class.getSimpleName();

        private WatchRunnable() {
        }

        @Override
        public final void run() {
            logger.trace("{} run triggered.", (Object)this.SIMPLE_NAME);
            for (Map.Entry entry : WatchManager.this.watchers.entrySet()) {
                Source source = (Source)entry.getKey();
                ConfigurationMonitor object = (ConfigurationMonitor)entry.getValue();
                if (!object.getWatcher().isModified()) continue;
                long l = object.getWatcher().getLastModified();
                if (logger.isInfoEnabled()) {
                    logger.info("Source '{}' was modified on {} ({}), previous modification was on {} ({})", (Object)source, (Object)WatchManager.this.millisToString(l), (Object)l, (Object)WatchManager.this.millisToString(object.lastModifiedMillis), (Object)object.lastModifiedMillis);
                }
                object.lastModifiedMillis = l;
                object.getWatcher().modified();
            }
            logger.trace("{} run ended.", (Object)this.SIMPLE_NAME);
        }
    }
}

