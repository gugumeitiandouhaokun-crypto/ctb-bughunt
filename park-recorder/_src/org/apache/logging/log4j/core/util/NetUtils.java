/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;

public final class NetUtils {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String UNKNOWN_LOCALHOST = "UNKNOWN_LOCALHOST";

    private NetUtils() {
    }

    public static String getLocalHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException unknownHostException) {
            try {
                Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
                while (enumeration.hasMoreElements()) {
                    Enumeration<InetAddress> enumeration2 = enumeration.nextElement().getInetAddresses();
                    while (enumeration2.hasMoreElements()) {
                        Object object = enumeration2.nextElement();
                        if (((InetAddress)object).isLoopbackAddress() || (object = ((InetAddress)object).getHostName()) == null) continue;
                        return object;
                    }
                }
            }
            catch (SocketException socketException) {
                LOGGER.error("Could not determine local host name", (Throwable)unknownHostException);
                return UNKNOWN_LOCALHOST;
            }
            LOGGER.error("Could not determine local host name", (Throwable)unknownHostException);
            return UNKNOWN_LOCALHOST;
        }
    }

    public static byte[] getMacAddress() {
        byte[] byArray = null;
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            try {
                Object object = NetworkInterface.getByInetAddress(inetAddress);
                if (NetUtils.isUpAndNotLoopback((NetworkInterface)object)) {
                    byArray = ((NetworkInterface)object).getHardwareAddress();
                }
                if (byArray == null) {
                    object = NetworkInterface.getNetworkInterfaces();
                    while (object.hasMoreElements() && byArray == null) {
                        NetworkInterface networkInterface = (NetworkInterface)object.nextElement();
                        if (!NetUtils.isUpAndNotLoopback(networkInterface)) continue;
                        byArray = networkInterface.getHardwareAddress();
                    }
                }
            }
            catch (SocketException socketException) {
                LOGGER.catching(socketException);
            }
            if (byArray == null || byArray.length == 0) {
                byArray = Arrays.copyOf(inetAddress.getAddress(), 6);
            }
        }
        catch (UnknownHostException unknownHostException) {}
        return byArray;
    }

    public static String getMacAddressString() {
        byte[] byArray = NetUtils.getMacAddress();
        if (byArray != null && byArray.length > 0) {
            StringBuilder stringBuilder = new StringBuilder(String.format("%02x", byArray[0]));
            for (int j = 1; j < byArray.length; ++j) {
                stringBuilder.append(":").append(String.format("%02x", byArray[j]));
            }
            return stringBuilder.toString();
        }
        return null;
    }

    private static boolean isUpAndNotLoopback(NetworkInterface networkInterface) {
        return networkInterface != null && !networkInterface.isLoopback() && networkInterface.isUp();
    }

    public static URI toURI(String string) {
        try {
            return new URI(string);
        }
        catch (URISyntaxException uRISyntaxException) {
            try {
                URL uRL = new URL(string);
                return new URI(uRL.getProtocol(), uRL.getHost(), uRL.getPath(), null);
            }
            catch (MalformedURLException | URISyntaxException exception) {
                return new File(string).toURI();
            }
        }
    }
}

