/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import org.apache.logging.log4j.core.net.AbstractSocketManager;
import org.apache.logging.log4j.core.net.SocketOptions;
import org.apache.logging.log4j.core.util.Closer;
import org.apache.logging.log4j.core.util.Log4jThread;
import org.apache.logging.log4j.core.util.NullOutputStream;
import org.apache.logging.log4j.util.Strings;

public class TcpSocketManager
extends AbstractSocketManager {
    public static final int DEFAULT_RECONNECTION_DELAY_MILLIS = 30000;
    private static final int DEFAULT_PORT = 4560;
    private static final TcpSocketManagerFactory<TcpSocketManager, FactoryData> FACTORY = new TcpSocketManagerFactory();
    private final int reconnectionDelayMillis;
    private Reconnector reconnector;
    private Socket socket;
    private final SocketOptions socketOptions;
    private final boolean retry;
    private final boolean immediateFail;
    private final int connectTimeoutMillis;

    @Deprecated
    public TcpSocketManager(String string, OutputStream outputStream, Socket socket, InetAddress inetAddress, String string2, int n, int n2, int n3, boolean bl, Layout<? extends Serializable> layout, int n4) {
        this(string, outputStream, socket, inetAddress, string2, n, n2, n3, bl, layout, n4, null);
    }

    public TcpSocketManager(String string, OutputStream outputStream, Socket socket, InetAddress inetAddress, String string2, int n, int n2, int n3, boolean bl, Layout<? extends Serializable> layout, int n4, SocketOptions socketOptions) {
        super(string, outputStream, inetAddress, string2, n, layout, true, n4);
        this.connectTimeoutMillis = n2;
        this.reconnectionDelayMillis = n3;
        this.socket = socket;
        this.immediateFail = bl;
        boolean bl2 = this.retry = n3 > 0;
        if (socket == null) {
            this.reconnector = this.createReconnector();
            this.reconnector.start();
        }
        this.socketOptions = socketOptions;
    }

    @Deprecated
    public static TcpSocketManager getSocketManager(String string, int n, int n2, int n3, boolean bl, Layout<? extends Serializable> layout, int n4) {
        return TcpSocketManager.getSocketManager(string, n, n2, n3, bl, layout, n4, null);
    }

    public static TcpSocketManager getSocketManager(String string, int n, int n2, int n3, boolean bl, Layout<? extends Serializable> layout, int n4, SocketOptions socketOptions) {
        if (Strings.isEmpty(string)) {
            throw new IllegalArgumentException("A host name is required");
        }
        if (n <= 0) {
            n = 4560;
        }
        if (n3 == 0) {
            n3 = 30000;
        }
        return (TcpSocketManager)TcpSocketManager.getManager("TCP:" + string + ':' + n, new FactoryData(string, n, n2, n3, bl, layout, n4, socketOptions), FACTORY);
    }

    @Override
    public void write(byte[] byArray, int n, int n2, boolean bl) {
        if (this.socket == null) {
            if (this.reconnector != null && !this.immediateFail) {
                this.reconnector.latch();
            }
            if (this.socket == null) {
                throw new AppenderLoggingException("Error writing to " + this.getName() + ": socket not available");
            }
        }
        TcpSocketManager tcpSocketManager = this;
        synchronized (tcpSocketManager) {
            block12: {
                try {
                    this.writeAndFlush(byArray, n, n2, bl);
                }
                catch (IOException iOException) {
                    if (!this.retry || this.reconnector != null) break block12;
                    String string = this.inetAddress + ":" + this.port;
                    this.reconnector = this.createReconnector();
                    try {
                        this.reconnector.reconnect();
                    }
                    catch (IOException iOException2) {
                        LOGGER.debug("Cannot reestablish socket connection to {}: {}; starting reconnector thread {}", (Object)string, (Object)iOException2.getLocalizedMessage(), (Object)this.reconnector.getName(), (Object)iOException2);
                        this.reconnector.start();
                        throw new AppenderLoggingException(String.format("Error sending to %s for %s", this.getName(), string), iOException);
                    }
                    try {
                        this.writeAndFlush(byArray, n, n2, bl);
                    }
                    catch (IOException iOException3) {
                        throw new AppenderLoggingException(String.format("Error writing to %s after reestablishing connection for %s", this.getName(), string), iOException);
                    }
                }
            }
            return;
        }
    }

    private void writeAndFlush(byte[] byArray, int n, int n2, boolean bl) {
        OutputStream outputStream = this.getOutputStream();
        outputStream.write(byArray, n, n2);
        if (bl) {
            outputStream.flush();
        }
    }

    @Override
    public synchronized boolean closeOutputStream() {
        boolean bl = super.closeOutputStream();
        if (this.reconnector != null) {
            this.reconnector.shutdown();
            this.reconnector.interrupt();
            this.reconnector = null;
        }
        Socket socket = this.socket;
        this.socket = null;
        if (socket != null) {
            try {
                socket.close();
            }
            catch (IOException iOException) {
                LOGGER.error("Could not close socket {}", (Object)this.socket);
                return false;
            }
        }
        return bl;
    }

    public int getConnectTimeoutMillis() {
        return this.connectTimeoutMillis;
    }

    @Override
    public Map<String, String> getContentFormat() {
        HashMap<String, String> hashMap = new HashMap<String, String>(super.getContentFormat());
        hashMap.put("protocol", "tcp");
        hashMap.put("direction", "out");
        return hashMap;
    }

    private Reconnector createReconnector() {
        TcpSocketManager tcpSocketManager = this;
        Reconnector reconnector = tcpSocketManager.new Reconnector(tcpSocketManager);
        reconnector.setDaemon(true);
        reconnector.setPriority(1);
        return reconnector;
    }

    protected Socket createSocket(InetSocketAddress inetSocketAddress) {
        return TcpSocketManager.createSocket(inetSocketAddress, this.socketOptions, this.connectTimeoutMillis);
    }

    protected static Socket createSocket(InetSocketAddress inetSocketAddress, SocketOptions socketOptions, int n) {
        LOGGER.debug("Creating socket {}", (Object)inetSocketAddress.toString());
        Socket socket = new Socket();
        if (socketOptions != null) {
            socketOptions.apply(socket);
        }
        socket.connect(inetSocketAddress, n);
        if (socketOptions != null) {
            socketOptions.apply(socket);
        }
        return socket;
    }

    public static void setHostResolver(HostResolver hostResolver) {
        TcpSocketManagerFactory.resolver = hostResolver;
    }

    public SocketOptions getSocketOptions() {
        return this.socketOptions;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public int getReconnectionDelayMillis() {
        return this.reconnectionDelayMillis;
    }

    public String toString() {
        return "TcpSocketManager [reconnectionDelayMillis=" + this.reconnectionDelayMillis + ", reconnector=" + this.reconnector + ", socket=" + this.socket + ", socketOptions=" + this.socketOptions + ", retry=" + this.retry + ", immediateFail=" + this.immediateFail + ", connectTimeoutMillis=" + this.connectTimeoutMillis + ", inetAddress=" + this.inetAddress + ", host=" + this.host + ", port=" + this.port + ", layout=" + this.layout + ", byteBuffer=" + this.byteBuffer + ", count=" + this.count + "]";
    }

    public static class HostResolver {
        public List<InetSocketAddress> resolveHost(String inetAddressArray, int n) {
            inetAddressArray = InetAddress.getAllByName((String)inetAddressArray);
            ArrayList<InetSocketAddress> arrayList = new ArrayList<InetSocketAddress>(inetAddressArray.length);
            for (InetAddress inetAddress : inetAddressArray) {
                arrayList.add(new InetSocketAddress(inetAddress, n));
            }
            return arrayList;
        }
    }

    protected static class TcpSocketManagerFactory<M extends TcpSocketManager, T extends FactoryData>
    implements ManagerFactory<M, T> {
        static HostResolver resolver = new HostResolver();

        protected TcpSocketManagerFactory() {
        }

        @Override
        public M createManager(String string, T t) {
            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByName(((FactoryData)t).host);
            }
            catch (UnknownHostException unknownHostException) {
                UnknownHostException unknownHostException2 = unknownHostException;
                LOGGER.error("Could not find address of {}: {}", (Object)((FactoryData)t).host, (Object)unknownHostException2, (Object)unknownHostException2);
                return null;
            }
            Socket socket = null;
            try {
                socket = this.createSocket(t);
                OutputStream outputStream = socket.getOutputStream();
                return this.createManager(string, outputStream, socket, inetAddress, t);
            }
            catch (IOException iOException) {
                IOException iOException2 = iOException;
                LOGGER.error("TcpSocketManager ({}) caught exception and will continue:", (Object)string, (Object)iOException2, (Object)iOException2);
                NullOutputStream nullOutputStream = NullOutputStream.getInstance();
                if (((FactoryData)t).reconnectDelayMillis == 0) {
                    Closer.closeSilently(socket);
                    return null;
                }
                return this.createManager(string, nullOutputStream, null, inetAddress, t);
            }
        }

        M createManager(String string, OutputStream outputStream, Socket socket, InetAddress inetAddress, T t) {
            return (M)new TcpSocketManager(string, outputStream, socket, inetAddress, ((FactoryData)t).host, ((FactoryData)t).port, ((FactoryData)t).connectTimeoutMillis, ((FactoryData)t).reconnectDelayMillis, ((FactoryData)t).immediateFail, ((FactoryData)t).layout, ((FactoryData)t).bufferSize, ((FactoryData)t).socketOptions);
        }

        Socket createSocket(T t) {
            List<InetSocketAddress> list = resolver.resolveHost(((FactoryData)t).host, ((FactoryData)t).port);
            InetSocketAddress inetSocketAddress2 = null;
            for (InetSocketAddress inetSocketAddress2 : list) {
                try {
                    return TcpSocketManager.createSocket(inetSocketAddress2, ((FactoryData)t).socketOptions, ((FactoryData)t).connectTimeoutMillis);
                }
                catch (IOException iOException) {
                }
            }
            throw new IOException(this.errorMessage(t, list), (Throwable)((Object)inetSocketAddress2));
        }

        protected String errorMessage(T t, List<InetSocketAddress> list) {
            StringBuilder stringBuilder = new StringBuilder("Unable to create socket for ");
            stringBuilder.append(((FactoryData)t).host).append(" at port ").append(((FactoryData)t).port);
            if (list.size() == 1) {
                if (!list.get(0).getAddress().getHostAddress().equals(((FactoryData)t).host)) {
                    stringBuilder.append(" using ip address ").append(list.get(0).getAddress().getHostAddress());
                    stringBuilder.append(" and port ").append(list.get(0).getPort());
                }
            } else {
                stringBuilder.append(" using ip addresses and ports ");
                for (int i2 = 0; i2 < list.size(); ++i2) {
                    if (i2 <= 0) continue;
                    stringBuilder.append(", ");
                    stringBuilder.append(list.get(i2).getAddress().getHostAddress());
                    stringBuilder.append(":").append(list.get(i2).getPort());
                }
            }
            return stringBuilder.toString();
        }
    }

    static class FactoryData {
        protected final String host;
        protected final int port;
        protected final int connectTimeoutMillis;
        protected final int reconnectDelayMillis;
        protected final boolean immediateFail;
        protected final Layout<? extends Serializable> layout;
        protected final int bufferSize;
        protected final SocketOptions socketOptions;

        public FactoryData(String string, int n, int n2, int n3, boolean bl, Layout<? extends Serializable> layout, int n4, SocketOptions socketOptions) {
            this.host = string;
            this.port = n;
            this.connectTimeoutMillis = n2;
            this.reconnectDelayMillis = n3;
            this.immediateFail = bl;
            this.layout = layout;
            this.bufferSize = n4;
            this.socketOptions = socketOptions;
        }

        public String toString() {
            return "FactoryData [host=" + this.host + ", port=" + this.port + ", connectTimeoutMillis=" + this.connectTimeoutMillis + ", reconnectDelayMillis=" + this.reconnectDelayMillis + ", immediateFail=" + this.immediateFail + ", layout=" + this.layout + ", bufferSize=" + this.bufferSize + ", socketOptions=" + this.socketOptions + "]";
        }
    }

    class Reconnector
    extends Log4jThread {
        private final CountDownLatch latch;
        private boolean shutdown;
        private final Object owner;

        public Reconnector(OutputStreamManager outputStreamManager) {
            super("TcpSocketManager-Reconnector");
            this.latch = new CountDownLatch(1);
            this.shutdown = false;
            this.owner = outputStreamManager;
        }

        public void latch() {
            try {
                this.latch.await();
                return;
            }
            catch (InterruptedException interruptedException) {
                return;
            }
        }

        public void shutdown() {
            this.shutdown = true;
        }

        @Override
        public void run() {
            while (!this.shutdown) {
                try {
                    Reconnector.sleep(TcpSocketManager.this.reconnectionDelayMillis);
                    this.reconnect();
                }
                catch (InterruptedException interruptedException) {
                    LOGGER.debug("Reconnection interrupted.");
                }
                catch (ConnectException connectException) {
                    LOGGER.debug("{}:{} refused connection", (Object)TcpSocketManager.this.host, (Object)TcpSocketManager.this.port);
                }
                catch (IOException iOException) {
                    LOGGER.debug("Unable to reconnect to {}:{}", (Object)TcpSocketManager.this.host, (Object)TcpSocketManager.this.port);
                }
                finally {
                    this.latch.countDown();
                }
            }
        }

        void reconnect() {
            FACTORY;
            Object object = TcpSocketManagerFactory.resolver.resolveHost(TcpSocketManager.this.host, TcpSocketManager.this.port);
            if (object.size() == 1) {
                LOGGER.debug("Reconnecting " + object.get(0));
                this.connect((InetSocketAddress)object.get(0));
                return;
            }
            InetSocketAddress inetSocketAddress = null;
            object = object.iterator();
            while (object.hasNext()) {
                inetSocketAddress = (InetSocketAddress)object.next();
                try {
                    LOGGER.debug("Reconnecting ".concat(String.valueOf(inetSocketAddress)));
                    this.connect(inetSocketAddress);
                    return;
                }
                catch (IOException iOException) {
                }
            }
            throw inetSocketAddress;
        }

        private void connect(InetSocketAddress inetSocketAddress) {
            Socket socket = TcpSocketManager.this.createSocket(inetSocketAddress);
            OutputStream outputStream = socket.getOutputStream();
            InetAddress inetAddress = TcpSocketManager.this.socket != null ? TcpSocketManager.this.socket.getInetAddress() : null;
            Object object = this.owner;
            synchronized (object) {
                Closer.closeSilently(TcpSocketManager.this.getOutputStream());
                TcpSocketManager.this.setOutputStream(outputStream);
                TcpSocketManager.this.socket = socket;
                TcpSocketManager.this.reconnector = null;
                this.shutdown = true;
            }
            object = inetAddress != null && inetAddress.getHostAddress().equals(inetSocketAddress.getAddress().getHostAddress()) ? "reestablished" : "established";
            LOGGER.debug("Connection to {}:{} {}: {}", (Object)TcpSocketManager.this.host, (Object)TcpSocketManager.this.port, object, (Object)TcpSocketManager.this.socket);
        }

        @Override
        public String toString() {
            return "Reconnector [latch=" + this.latch + ", shutdown=" + this.shutdown + "]";
        }
    }
}

