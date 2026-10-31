/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ThreadUtils {
    public static final AlwaysTruePredicate ALWAYS_TRUE_PREDICATE = new AlwaysTruePredicate();

    public static Thread findThreadById(long l, ThreadGroup threadGroup) {
        if (threadGroup == null) {
            throw new IllegalArgumentException("The thread group must not be null");
        }
        Thread thread = ThreadUtils.findThreadById(l);
        if (thread != null && threadGroup.equals(thread.getThreadGroup())) {
            return thread;
        }
        return null;
    }

    public static Thread findThreadById(long l, String string) {
        if (string == null) {
            throw new IllegalArgumentException("The thread group name must not be null");
        }
        Thread thread = ThreadUtils.findThreadById(l);
        if (thread != null && thread.getThreadGroup() != null && thread.getThreadGroup().getName().equals(string)) {
            return thread;
        }
        return null;
    }

    public static Collection<Thread> findThreadsByName(String string, ThreadGroup threadGroup) {
        return ThreadUtils.findThreads(threadGroup, false, new NamePredicate(string));
    }

    public static Collection<Thread> findThreadsByName(String object, String iterator) {
        if (object == null) {
            throw new IllegalArgumentException("The thread name must not be null");
        }
        if (iterator == null) {
            throw new IllegalArgumentException("The thread group name must not be null");
        }
        if ((iterator = ThreadUtils.findThreadGroups(new NamePredicate((String)((Object)iterator)))).isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<Thread> arrayList = new ArrayList<Thread>();
        object = new NamePredicate((String)object);
        iterator = iterator.iterator();
        while (iterator.hasNext()) {
            ThreadGroup threadGroup = (ThreadGroup)iterator.next();
            arrayList.addAll(ThreadUtils.findThreads(threadGroup, false, (ThreadPredicate)object));
        }
        return Collections.unmodifiableCollection(arrayList);
    }

    public static Collection<ThreadGroup> findThreadGroupsByName(String string) {
        return ThreadUtils.findThreadGroups(new NamePredicate(string));
    }

    public static Collection<ThreadGroup> getAllThreadGroups() {
        return ThreadUtils.findThreadGroups(ALWAYS_TRUE_PREDICATE);
    }

    public static ThreadGroup getSystemThreadGroup() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        while (threadGroup.getParent() != null) {
            threadGroup = threadGroup.getParent();
        }
        return threadGroup;
    }

    public static Collection<Thread> getAllThreads() {
        return ThreadUtils.findThreads(ALWAYS_TRUE_PREDICATE);
    }

    public static Collection<Thread> findThreadsByName(String string) {
        return ThreadUtils.findThreads(new NamePredicate(string));
    }

    public static Thread findThreadById(long l) {
        Collection<Thread> collection = ThreadUtils.findThreads(new ThreadIdPredicate(l));
        if (collection.isEmpty()) {
            return null;
        }
        return collection.iterator().next();
    }

    public static Collection<Thread> findThreads(ThreadPredicate threadPredicate) {
        return ThreadUtils.findThreads(ThreadUtils.getSystemThreadGroup(), true, threadPredicate);
    }

    public static Collection<ThreadGroup> findThreadGroups(ThreadGroupPredicate threadGroupPredicate) {
        return ThreadUtils.findThreadGroups(ThreadUtils.getSystemThreadGroup(), true, threadGroupPredicate);
    }

    public static Collection<Thread> findThreads(ThreadGroup object, boolean n, ThreadPredicate threadPredicate) {
        int n2;
        Thread[] threadArray;
        if (object == null) {
            throw new IllegalArgumentException("The group must not be null");
        }
        if (threadPredicate == null) {
            throw new IllegalArgumentException("The predicate must not be null");
        }
        int n3 = ((ThreadGroup)object).activeCount();
        do {
            n2 = n3;
        } while ((n3 = ((ThreadGroup)object).enumerate(threadArray = new Thread[n2 + n2 / 2 + 1], n != 0)) >= threadArray.length);
        object = new ArrayList(n3);
        for (n = 0; n < n3; ++n) {
            if (!threadPredicate.test(threadArray[n])) continue;
            object.add(threadArray[n]);
        }
        return Collections.unmodifiableCollection(object);
    }

    public static Collection<ThreadGroup> findThreadGroups(ThreadGroup object, boolean n, ThreadGroupPredicate threadGroupPredicate) {
        int n2;
        ThreadGroup[] threadGroupArray;
        if (object == null) {
            throw new IllegalArgumentException("The group must not be null");
        }
        if (threadGroupPredicate == null) {
            throw new IllegalArgumentException("The predicate must not be null");
        }
        int n3 = ((ThreadGroup)object).activeGroupCount();
        do {
            n2 = n3;
        } while ((n3 = ((ThreadGroup)object).enumerate(threadGroupArray = new ThreadGroup[n2 + n2 / 2 + 1], n != 0)) >= threadGroupArray.length);
        object = new ArrayList(n3);
        for (n = 0; n < n3; ++n) {
            if (!threadGroupPredicate.test(threadGroupArray[n])) continue;
            object.add(threadGroupArray[n]);
        }
        return Collections.unmodifiableCollection(object);
    }

    public static class ThreadIdPredicate
    implements ThreadPredicate {
        private final long threadId;

        public ThreadIdPredicate(long l) {
            if (l <= 0L) {
                throw new IllegalArgumentException("The thread id must be greater than zero");
            }
            this.threadId = l;
        }

        @Override
        public boolean test(Thread thread) {
            return thread != null && thread.getId() == this.threadId;
        }
    }

    public static class NamePredicate
    implements ThreadGroupPredicate,
    ThreadPredicate {
        private final String name;

        public NamePredicate(String string) {
            if (string == null) {
                throw new IllegalArgumentException("The name must not be null");
            }
            this.name = string;
        }

        @Override
        public boolean test(ThreadGroup threadGroup) {
            return threadGroup != null && threadGroup.getName().equals(this.name);
        }

        @Override
        public boolean test(Thread thread) {
            return thread != null && thread.getName().equals(this.name);
        }
    }

    static final class AlwaysTruePredicate
    implements ThreadGroupPredicate,
    ThreadPredicate {
        private AlwaysTruePredicate() {
        }

        @Override
        public final boolean test(ThreadGroup threadGroup) {
            return true;
        }

        @Override
        public final boolean test(Thread thread) {
            return true;
        }
    }

    public static interface ThreadGroupPredicate {
        public boolean test(ThreadGroup var1);
    }

    public static interface ThreadPredicate {
        public boolean test(Thread var1);
    }
}

