/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net;

import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.net.AbstractSocketManager;
import org.apache.logging.log4j.core.net.DatagramOutputStream;
import org.apache.logging.log4j.util.Strings;

public class DatagramSocketManager
extends AbstractSocketManager {
    private static final DatagramSocketManagerFactory FACTORY = new DatagramSocketManagerFactory();

    protected DatagramSocketManager(String string, OutputStream outputStream, InetAddress inetAddress, String string2, int n, Layout<? extends Serializable> layout, int n2) {
        super(string, outputStream, inetAddress, string2, n, layout, true, n2);
    }

    public static DatagramSocketManager getSocketManager(String string, int n, Layout<? extends Serializable> layout, int n2) {
        if (Strings.isEmpty(string)) {
            throw new IllegalArgumentException("A host name is required");
        }
        if (n <= 0) {
            throw new IllegalArgumentException("A port value is required");
        }
        return (DatagramSocketManager)DatagramSocketManager.getManager("UDP:" + string + ':' + n, new FactoryData(string, n, layout, n2), FACTORY);
    }

    @Override
    public Map<String, String> getContentFormat() {
        HashMap<String, String> hashMap = new HashMap<String, String>(super.getContentFormat());
        hashMap.put("protocol", "udp");
        hashMap.put("direction", "out");
        return hashMap;
    }

    static class DatagramSocketManagerFactory
    implements ManagerFactory<DatagramSocketManager, FactoryData> {
        private DatagramSocketManagerFactory() {
        }

        @Override
        public DatagramSocketManager createManager(String string, FactoryData factoryData) {
            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByName(factoryData.host);
            }
            catch (UnknownHostException unknownHostException) {
                LOGGER.error("Could not find address of " + factoryData.host, (Throwable)unknownHostException);
                return null;
            }
            DatagramOutputStream datagramOutputStream = new DatagramOutputStream(factoryData.host, factoryData.port, factoryData.layout.getHeader(), factoryData.layout.getFooter());
            return new DatagramSocketManager(string, (OutputStream)datagramOutputStream, inetAddress, factoryData.host, factoryData.port, factoryData.layout, factoryData.bufferSize);
        }
    }

    static class FactoryData {
        private final String host;
        private final int port;
        private final Layout<? extends Serializable> layout;
        private final int bufferSize;

        public FactoryData(String string, int n, Layout<? extends Serializable> layout, int n2) {
            this.host = string;
            this.port = n;
            this.layout = layout;
            this.bufferSize = n2;
        }
    }
}

