/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jctools.queues.MessagePassingQueue$Consumer
 *  org.jctools.queues.MpscArrayQueue
 */
package org.apache.logging.log4j.core.async;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import org.apache.logging.log4j.core.async.BlockingQueueFactory;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscArrayQueue;

@Plugin(name="JCToolsBlockingQueue", category="Core", elementType="BlockingQueueFactory")
public class JCToolsBlockingQueueFactory<E>
implements BlockingQueueFactory<E> {
    private final WaitStrategy waitStrategy;

    private JCToolsBlockingQueueFactory(WaitStrategy waitStrategy) {
        this.waitStrategy = waitStrategy;
    }

    @Override
    public BlockingQueue<E> create(int n) {
        return new MpscBlockingQueue(n, this.waitStrategy);
    }

    @PluginFactory
    public static <E> JCToolsBlockingQueueFactory<E> createFactory(@PluginAttribute(value="WaitStrategy", defaultString="PARK") WaitStrategy waitStrategy) {
        return new JCToolsBlockingQueueFactory<E>(waitStrategy);
    }

    static interface Idle {
        public int idle(int var1);
    }

    public static enum WaitStrategy {
        SPIN(new Idle(){

            @Override
            public final int idle(int n) {
                return n + 1;
            }
        }),
        YIELD(new Idle(){

            @Override
            public final int idle(int n) {
                Thread.yield();
                return n + 1;
            }
        }),
        PARK(new Idle(){

            @Override
            public final int idle(int n) {
                LockSupport.parkNanos(1L);
                return n + 1;
            }
        }),
        PROGRESSIVE(new Idle(){

            @Override
            public final int idle(int n) {
                if (n > 200) {
                    LockSupport.parkNanos(1L);
                } else if (n > 100) {
                    Thread.yield();
                }
                return n + 1;
            }
        });

        private final Idle idle;

        private int idle(int n) {
            return this.idle.idle(n);
        }

        private WaitStrategy(Idle idle) {
            this.idle = idle;
        }
    }

    static final class MpscBlockingQueue<E>
    extends MpscArrayQueue<E>
    implements BlockingQueue<E> {
        private final WaitStrategy waitStrategy;

        MpscBlockingQueue(int n, WaitStrategy waitStrategy) {
            super(n);
            this.waitStrategy = waitStrategy;
        }

        @Override
        public final int drainTo(Collection<? super E> collection) {
            return this.drainTo(collection, this.capacity());
        }

        @Override
        public final int drainTo(final Collection<? super E> collection, int n) {
            return this.drain(new MessagePassingQueue.Consumer<E>(){

                public void accept(E e2) {
                    collection.add(e2);
                }
            }, n);
        }

        @Override
        public final boolean offer(E e2, long l, TimeUnit timeUnit) {
            int n = 0;
            long l2 = System.nanoTime() + timeUnit.toNanos(l);
            do {
                if (this.offer(e2)) {
                    return true;
                }
                if (System.nanoTime() - l2 > 0L) {
                    return false;
                }
                n = this.waitStrategy.idle(n);
            } while (!Thread.interrupted());
            throw new InterruptedException();
        }

        @Override
        public final E poll(long l, TimeUnit timeUnit) {
            int n = 0;
            long l2 = System.nanoTime() + timeUnit.toNanos(l);
            do {
                Object object;
                if ((object = this.poll()) != null) {
                    return (E)object;
                }
                if (System.nanoTime() - l2 > 0L) {
                    return null;
                }
                n = this.waitStrategy.idle(n);
            } while (!Thread.interrupted());
            throw new InterruptedException();
        }

        @Override
        public final void put(E e2) {
            int n = 0;
            do {
                if (this.offer(e2)) {
                    return;
                }
                n = this.waitStrategy.idle(n);
            } while (!Thread.interrupted());
            throw new InterruptedException();
        }

        @Override
        public final boolean offer(E e2) {
            return this.offerIfBelowThreshold(e2, this.capacity() - 32);
        }

        @Override
        public final int remainingCapacity() {
            return this.capacity() - this.size();
        }

        @Override
        public final E take() {
            int n = 100;
            do {
                Object object;
                if ((object = this.relaxedPoll()) != null) {
                    return (E)object;
                }
                n = this.waitStrategy.idle(n);
            } while (!Thread.interrupted());
            throw new InterruptedException();
        }
    }
}

