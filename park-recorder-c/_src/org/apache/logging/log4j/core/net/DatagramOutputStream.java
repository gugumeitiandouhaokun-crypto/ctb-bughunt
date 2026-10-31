/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net;

import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.status.StatusLogger;

public class DatagramOutputStream
extends OutputStream {
    protected static final Logger LOGGER = StatusLogger.getLogger();
    private static final int SHIFT_1 = 8;
    private static final int SHIFT_2 = 16;
    private static final int SHIFT_3 = 24;
    private DatagramSocket datagramSocket;
    private final InetAddress inetAddress;
    private final int port;
    private byte[] data;
    private final byte[] header;
    private final byte[] footer;

    public DatagramOutputStream(String string, int n, byte[] byArray, byte[] byArray2) {
        this.port = n;
        this.header = byArray;
        this.footer = byArray2;
        try {
            this.inetAddress = InetAddress.getByName(string);
        }
        catch (UnknownHostException unknownHostException) {
            string = "Could not find host ".concat(String.valueOf(string));
            LOGGER.error(string, (Throwable)unknownHostException);
            throw new AppenderLoggingException(string, unknownHostException);
        }
        try {
            this.datagramSocket = new DatagramSocket();
            return;
        }
        catch (SocketException socketException) {
            string = "Could not instantiate DatagramSocket to ".concat(String.valueOf(string));
            LOGGER.error(string, (Throwable)socketException);
            throw new AppenderLoggingException(string, socketException);
        }
    }

    @Override
    public synchronized void write(byte[] byArray, int n, int n2) {
        this.copy(byArray, n, n2);
    }

    @Override
    public synchronized void write(int n) {
        this.copy(new byte[]{(byte)(n >>> 24), (byte)(n >>> 16), (byte)(n >>> 8), (byte)n}, 0, 4);
    }

    @Override
    public synchronized void write(byte[] byArray) {
        this.copy(byArray, 0, byArray.length);
    }

    @Override
    public synchronized void flush() {
        try {
            if (this.data != null && this.datagramSocket != null && this.inetAddress != null) {
                if (this.footer != null) {
                    DatagramOutputStream datagramOutputStream = this;
                    datagramOutputStream.copy(datagramOutputStream.footer, 0, this.footer.length);
                }
                DatagramPacket datagramPacket = new DatagramPacket(this.data, this.data.length, this.inetAddress, this.port);
                this.datagramSocket.send(datagramPacket);
            }
            this.data = null;
            if (this.header != null) {
                DatagramOutputStream datagramOutputStream = this;
                datagramOutputStream.copy(datagramOutputStream.header, 0, this.header.length);
                return;
            }
        }
        catch (Throwable throwable) {
            this.data = null;
            if (this.header != null) {
                DatagramOutputStream datagramOutputStream = this;
                datagramOutputStream.copy(datagramOutputStream.header, 0, this.header.length);
            }
            throw throwable;
        }
    }

    @Override
    public synchronized void close() {
        if (this.datagramSocket != null) {
            if (this.data != null) {
                this.flush();
            }
            this.datagramSocket.close();
            this.datagramSocket = null;
        }
    }

    private void copy(byte[] byArray, int n, int n2) {
        int n3 = this.data == null ? 0 : this.data.length;
        byte[] byArray2 = new byte[n2 + n3];
        if (this.data != null) {
            System.arraycopy(this.data, 0, byArray2, 0, this.data.length);
        }
        System.arraycopy(byArray, n, byArray2, n3, n2);
        this.data = byArray2;
    }
}

