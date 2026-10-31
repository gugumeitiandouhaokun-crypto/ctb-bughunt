/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.zeromq.ZMQ
 *  org.zeromq.ZMQ$Context
 *  org.zeromq.ZMQ$Socket
 */
package org.apache.logging.log4j.core.appender.mom.jeromq;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.util.ShutdownCallbackRegistry;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.zeromq.ZMQ;

public class JeroMqManager
extends AbstractManager {
    public static final String SYS_PROPERTY_ENABLE_SHUTDOWN_HOOK = "log4j.jeromq.enableShutdownHook";
    public static final String SYS_PROPERTY_IO_THREADS = "log4j.jeromq.ioThreads";
    private static final JeroMqManagerFactory FACTORY = new JeroMqManagerFactory();
    private static final ZMQ.Context CONTEXT;
    private final ZMQ.Socket publisher = CONTEXT.socket(1);

    private JeroMqManager(String object, JeroMqConfiguration jeroMqConfiguration) {
        super(null, (String)object);
        this.publisher.setAffinity(jeroMqConfiguration.affinity);
        this.publisher.setBacklog(jeroMqConfiguration.backlog);
        this.publisher.setDelayAttachOnConnect(jeroMqConfiguration.delayAttachOnConnect);
        if (jeroMqConfiguration.identity != null) {
            this.publisher.setIdentity(jeroMqConfiguration.identity);
        }
        this.publisher.setIPv4Only(jeroMqConfiguration.ipv4Only);
        this.publisher.setLinger(jeroMqConfiguration.linger);
        this.publisher.setMaxMsgSize(jeroMqConfiguration.maxMsgSize);
        this.publisher.setRcvHWM(jeroMqConfiguration.rcvHwm);
        this.publisher.setReceiveBufferSize(jeroMqConfiguration.receiveBufferSize);
        this.publisher.setReceiveTimeOut(jeroMqConfiguration.receiveTimeOut);
        this.publisher.setReconnectIVL(jeroMqConfiguration.reconnectIVL);
        this.publisher.setReconnectIVLMax(jeroMqConfiguration.reconnectIVLMax);
        this.publisher.setSendBufferSize(jeroMqConfiguration.sendBufferSize);
        this.publisher.setSendTimeOut(jeroMqConfiguration.sendTimeOut);
        this.publisher.setSndHWM(jeroMqConfiguration.sndHwm);
        this.publisher.setTCPKeepAlive(jeroMqConfiguration.tcpKeepAlive);
        this.publisher.setTCPKeepAliveCount(jeroMqConfiguration.tcpKeepAliveCount);
        this.publisher.setTCPKeepAliveIdle(jeroMqConfiguration.tcpKeepAliveIdle);
        this.publisher.setTCPKeepAliveInterval(jeroMqConfiguration.tcpKeepAliveInterval);
        this.publisher.setXpubVerbose(jeroMqConfiguration.xpubVerbose);
        for (String string : jeroMqConfiguration.endpoints) {
            this.publisher.bind(string);
        }
        LOGGER.debug("Created JeroMqManager with {}", (Object)jeroMqConfiguration);
    }

    public boolean send(byte[] byArray) {
        return this.publisher.send(byArray);
    }

    @Override
    public boolean releaseSub(long l, TimeUnit timeUnit) {
        this.publisher.close();
        return true;
    }

    public static JeroMqManager getJeroMqManager(String string, long l, long l2, boolean bl, byte[] byArray, boolean bl2, long l3, long l4, long l5, long l6, int n, long l7, long l8, long l9, int n2, long l10, int n3, long l11, long l12, long l13, boolean bl3, List<String> list) {
        return JeroMqManager.getManager(string, FACTORY, new JeroMqConfiguration(l, l2, bl, byArray, bl2, l3, l4, l5, l6, n, l7, l8, l9, n2, l10, n3, l11, l12, l13, bl3, list));
    }

    public static ZMQ.Context getContext() {
        return CONTEXT;
    }

    static {
        LOGGER.trace("JeroMqManager using ZMQ version {}", (Object)ZMQ.getVersionString());
        int n = PropertiesUtil.getProperties().getIntegerProperty(SYS_PROPERTY_IO_THREADS, 1);
        LOGGER.trace("JeroMqManager creating ZMQ context with ioThreads = {}", (Object)n);
        CONTEXT = ZMQ.context((int)n);
        if (PropertiesUtil.getProperties().getBooleanProperty(SYS_PROPERTY_ENABLE_SHUTDOWN_HOOK, true)) {
            ((ShutdownCallbackRegistry)((Object)LogManager.getFactory())).addShutdownCallback(new Runnable(){

                @Override
                public final void run() {
                    CONTEXT.close();
                }
            });
        }
    }

    static class JeroMqManagerFactory
    implements ManagerFactory<JeroMqManager, JeroMqConfiguration> {
        private JeroMqManagerFactory() {
        }

        @Override
        public JeroMqManager createManager(String string, JeroMqConfiguration jeroMqConfiguration) {
            return new JeroMqManager(string, jeroMqConfiguration);
        }
    }

    static class JeroMqConfiguration {
        private final long affinity;
        private final long backlog;
        private final boolean delayAttachOnConnect;
        private final byte[] identity;
        private final boolean ipv4Only;
        private final long linger;
        private final long maxMsgSize;
        private final long rcvHwm;
        private final long receiveBufferSize;
        private final int receiveTimeOut;
        private final long reconnectIVL;
        private final long reconnectIVLMax;
        private final long sendBufferSize;
        private final int sendTimeOut;
        private final long sndHwm;
        private final int tcpKeepAlive;
        private final long tcpKeepAliveCount;
        private final long tcpKeepAliveIdle;
        private final long tcpKeepAliveInterval;
        private final boolean xpubVerbose;
        private final List<String> endpoints;

        private JeroMqConfiguration(long l, long l2, boolean bl, byte[] byArray, boolean bl2, long l3, long l4, long l5, long l6, int n, long l7, long l8, long l9, int n2, long l10, int n3, long l11, long l12, long l13, boolean bl3, List<String> list) {
            this.affinity = l;
            this.backlog = l2;
            this.delayAttachOnConnect = bl;
            this.identity = byArray;
            this.ipv4Only = bl2;
            this.linger = l3;
            this.maxMsgSize = l4;
            this.rcvHwm = l5;
            this.receiveBufferSize = l6;
            this.receiveTimeOut = n;
            this.reconnectIVL = l7;
            this.reconnectIVLMax = l8;
            this.sendBufferSize = l9;
            this.sendTimeOut = n2;
            this.sndHwm = l10;
            this.tcpKeepAlive = n3;
            this.tcpKeepAliveCount = l11;
            this.tcpKeepAliveIdle = l12;
            this.tcpKeepAliveInterval = l13;
            this.xpubVerbose = bl3;
            this.endpoints = list;
        }

        public String toString() {
            return "JeroMqConfiguration{affinity=" + this.affinity + ", backlog=" + this.backlog + ", delayAttachOnConnect=" + this.delayAttachOnConnect + ", identity=" + Arrays.toString(this.identity) + ", ipv4Only=" + this.ipv4Only + ", linger=" + this.linger + ", maxMsgSize=" + this.maxMsgSize + ", rcvHwm=" + this.rcvHwm + ", receiveBufferSize=" + this.receiveBufferSize + ", receiveTimeOut=" + this.receiveTimeOut + ", reconnectIVL=" + this.reconnectIVL + ", reconnectIVLMax=" + this.reconnectIVLMax + ", sendBufferSize=" + this.sendBufferSize + ", sendTimeOut=" + this.sendTimeOut + ", sndHwm=" + this.sndHwm + ", tcpKeepAlive=" + this.tcpKeepAlive + ", tcpKeepAliveCount=" + this.tcpKeepAliveCount + ", tcpKeepAliveIdle=" + this.tcpKeepAliveIdle + ", tcpKeepAliveInterval=" + this.tcpKeepAliveInterval + ", xpubVerbose=" + this.xpubVerbose + ", endpoints=" + this.endpoints + '}';
        }
    }
}

