/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.net.SocketOptions;
import org.apache.logging.log4j.core.net.TcpSocketManager;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;
import org.apache.logging.log4j.util.Strings;

public class SslSocketManager
extends TcpSocketManager {
    public static final int DEFAULT_PORT = 6514;
    private static final SslSocketManagerFactory FACTORY = new SslSocketManagerFactory();
    private final SslConfiguration sslConfig;

    @Deprecated
    public SslSocketManager(String string, OutputStream outputStream, Socket socket, SslConfiguration sslConfiguration, InetAddress inetAddress, String string2, int n, int n2, int n3, boolean bl, Layout<? extends Serializable> layout, int n4) {
        super(string, outputStream, socket, inetAddress, string2, n, n2, n3, bl, layout, n4, null);
        this.sslConfig = sslConfiguration;
    }

    public SslSocketManager(String string, OutputStream outputStream, Socket socket, SslConfiguration sslConfiguration, InetAddress inetAddress, String string2, int n, int n2, int n3, boolean bl, Layout<? extends Serializable> layout, int n4, SocketOptions socketOptions) {
        super(string, outputStream, socket, inetAddress, string2, n, n2, n3, bl, layout, n4, socketOptions);
        this.sslConfig = sslConfiguration;
    }

    @Deprecated
    public static SslSocketManager getSocketManager(SslConfiguration sslConfiguration, String string, int n, int n2, int n3, boolean bl, Layout<? extends Serializable> layout, int n4) {
        return SslSocketManager.getSocketManager(sslConfiguration, string, n, n2, n3, bl, layout, n4, null);
    }

    public static SslSocketManager getSocketManager(SslConfiguration sslConfiguration, String string, int n, int n2, int n3, boolean bl, Layout<? extends Serializable> layout, int n4, SocketOptions socketOptions) {
        if (Strings.isEmpty(string)) {
            throw new IllegalArgumentException("A host name is required");
        }
        if (n <= 0) {
            n = 6514;
        }
        if (n3 == 0) {
            n3 = 30000;
        }
        return (SslSocketManager)SslSocketManager.getManager("TLS:" + string + ':' + n, new SslFactoryData(sslConfiguration, string, n, n2, n3, bl, layout, n4, socketOptions), FACTORY);
    }

    @Override
    protected Socket createSocket(InetSocketAddress inetSocketAddress) {
        Socket socket = SslSocketManager.createSslSocketFactory(this.sslConfig).createSocket();
        socket.connect(inetSocketAddress, this.getConnectTimeoutMillis());
        return socket;
    }

    private static SSLSocketFactory createSslSocketFactory(SslConfiguration object) {
        object = object != null ? ((SslConfiguration)object).getSslSocketFactory() : (SSLSocketFactory)SSLSocketFactory.getDefault();
        return object;
    }

    static Socket createSocket(InetSocketAddress inetSocketAddress, int n, SslConfiguration object, SocketOptions socketOptions) {
        object = (SSLSocket)SslSocketManager.createSslSocketFactory((SslConfiguration)object).createSocket();
        if (socketOptions != null) {
            socketOptions.apply((Socket)object);
        }
        ((Socket)object).connect(inetSocketAddress, n);
        if (socketOptions != null) {
            socketOptions.apply((Socket)object);
        }
        return object;
    }

    static class SslSocketManagerFactory
    extends TcpSocketManager.TcpSocketManagerFactory<SslSocketManager, SslFactoryData> {
        private SslSocketManagerFactory() {
        }

        @Override
        SslSocketManager createManager(String string, OutputStream outputStream, Socket socket, InetAddress inetAddress, SslFactoryData sslFactoryData) {
            return new SslSocketManager(string, outputStream, socket, sslFactoryData.sslConfiguration, inetAddress, sslFactoryData.host, sslFactoryData.port, sslFactoryData.connectTimeoutMillis, sslFactoryData.reconnectDelayMillis, sslFactoryData.immediateFail, sslFactoryData.layout, sslFactoryData.bufferSize, sslFactoryData.socketOptions);
        }

        @Override
        Socket createSocket(SslFactoryData sslFactoryData) {
            List<InetSocketAddress> list = resolver.resolveHost(sslFactoryData.host, sslFactoryData.port);
            InetSocketAddress inetSocketAddress2 = null;
            for (InetSocketAddress inetSocketAddress2 : list) {
                try {
                    return SslSocketManager.createSocket(inetSocketAddress2, sslFactoryData.connectTimeoutMillis, sslFactoryData.sslConfiguration, sslFactoryData.socketOptions);
                }
                catch (IOException iOException) {
                }
            }
            throw new IOException(this.errorMessage(sslFactoryData, list), (Throwable)((Object)inetSocketAddress2));
        }
    }

    static class SslFactoryData
    extends TcpSocketManager.FactoryData {
        protected SslConfiguration sslConfiguration;

        public SslFactoryData(SslConfiguration sslConfiguration, String string, int n, int n2, int n3, boolean bl, Layout<? extends Serializable> layout, int n4, SocketOptions socketOptions) {
            super(string, n, n2, n3, bl, layout, n4, socketOptions);
            this.sslConfiguration = sslConfiguration;
        }

        @Override
        public String toString() {
            return "SslFactoryData [sslConfiguration=" + this.sslConfiguration + ", host=" + this.host + ", port=" + this.port + ", connectTimeoutMillis=" + this.connectTimeoutMillis + ", reconnectDelayMillis=" + this.reconnectDelayMillis + ", immediateFail=" + this.immediateFail + ", layout=" + this.layout + ", bufferSize=" + this.bufferSize + ", socketOptions=" + this.socketOptions + "]";
        }
    }
}

